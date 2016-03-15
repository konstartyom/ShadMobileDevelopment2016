package com.konstartyom.photos;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Rect;
import android.os.AsyncTask;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

public class CustomAdapter extends RecyclerView.Adapter<CustomAdapter.ViewHolder> {
    private static final String TAG = "CustomAdapter";

    private ArrayList<LazyImage> mData;

    private int mBaseWidth;

    private Activity mParActivity;

    @Override
    public long getItemId(int position) {
        try {
            return mData.get(position).mFile.getCanonicalPath().hashCode();
        } catch (IOException _) {
            return 0;
        }
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        private static String TAG = "CustomAdapterHolder";

        private final ImageView imageView;
        private int mPosition;

        public ViewHolder(View v, int width, final Activity activity,
                          final ArrayList<LazyImage> data) {
            super(v);
            v.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent i = new Intent(activity, ImageViewActivity.class);
                    i.putExtra("filename", data.get(getPosition()).getImagePath());
                    i.putExtra("fromrect", calcUnclippedRect(v));
                    i.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
                    activity.startActivity(i);
                    Log.d(TAG, "Element " + getPosition() + " clicked.");
                }
            });
            mPosition = 0;
            imageView = (ImageView) v.findViewById(R.id.itemImageView);
            ViewGroup.LayoutParams params = imageView.getLayoutParams();
            params.height = width;
            imageView.setLayoutParams(params);
        }

        public ImageView getImageView() {
            return imageView;
        }

        public int getRPosition() {
            return mPosition;
        }

        public void setRPosition(int pos) {
            mPosition = pos;
        }
    }

    private class LazyImage {

        private File mFile;

        public LazyImage(File file) {
            mFile = file;
        }

        private String getFileName() {
            try {
                return (mFile.getCanonicalPath());
            } catch (IOException _) {
                return "";
            }
        }

        public void decode(final ViewHolder holder, final int position) {
            final Integer imageSize = calcNeededImageSize(mBaseWidth);
            final String fname = imageSize.toString() + getFileName();
            Bitmap bmp = ImageCacher.getBitmapFromMemCache(fname);
            if (bmp == null) {
                holder.getImageView().setImageResource(R.drawable.stub);
                final File f = mFile;
                new AsyncTask<String, Integer, Bitmap>() {
                    @Override
                    protected Bitmap doInBackground(String... _) {
                        return ImageLoader.decodeBitmap(f, imageSize, imageSize);
                    }

                    @Override
                    protected void onPostExecute(Bitmap result) {
                        ImageCacher.addBitmapToMemoryCache(fname, result);
                        if (position == holder.getRPosition()) {
                            holder.getImageView().setImageBitmap(result);
                        }
                        super.onPostExecute(result);
                    }
                }.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, null);
            } else {
                holder.getImageView().setImageBitmap(bmp);
            }
        }

        public String getImagePath() {
            try {
                return mFile.getCanonicalPath();
            } catch (IOException _) {
                return null;
            }
        }
    }

    public CustomAdapter(ArrayList<File> dataSet, int baseWidth, Activity activity) {
        setHasStableIds(true);
        mData = new ArrayList<>();
        for (File file : dataSet) {
            mData.add(new LazyImage(file));
        }
        mBaseWidth = baseWidth;
        mParActivity = activity;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
        View v = LayoutInflater.from(viewGroup.getContext())
                .inflate(R.layout.row_item, viewGroup, false);

        return new ViewHolder(v, mBaseWidth, mParActivity, mData);
    }

    @Override
    public void onBindViewHolder(ViewHolder viewHolder, final int position) {
        Log.d(TAG, "Element " + position + " set.");
        viewHolder.setRPosition(position);
        mData.get(position).decode(viewHolder, position);
    }

    @Override
    public int getItemCount() {
        return mData.size();
    }

    private static Rect calcUnclippedRect(View v) {
        Rect resultRect = new Rect();
        Rect localRect = new Rect();
        Rect drawingRect = new Rect();

        v.getLocalVisibleRect(localRect);
        Log.d("Local rect: " + localRect, TAG);
        v.getGlobalVisibleRect(resultRect);
        Log.d("Global rect: " + resultRect, TAG);
        v.getDrawingRect(drawingRect);
        Log.d("Drawing rect: " + drawingRect, TAG);
        resultRect.top -= localRect.top - drawingRect.top;
        resultRect.bottom -= localRect.bottom - drawingRect.bottom;
        resultRect.left -= localRect.left - drawingRect.left;
        resultRect.right -= localRect.right - drawingRect.right;
        return resultRect;
    }

    private static int calcNeededImageSize(int width) {
        int max = width;
        int count = 0;
        while (max > 0) {
            max >>>= 1;
            ++count;
        }
        return Math.min(256, 1 << (count - 1));
    }

}
