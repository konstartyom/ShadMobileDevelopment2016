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
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;

public class CustomAdapter extends RecyclerView.Adapter<CustomAdapter.ViewHolder> {
    private static final String TAG = "CustomAdapter";

    private ArrayList<LazyImage> mData;

    private int mBaseWidth;

    private Activity mParActivity;

    @Override
    public long getItemId(int position) {
            return mData.get(position).getDescriptor().getUniqueRepr().hashCode();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        private static String TAG = "CustomAdapterHolder";

        private final ImageView imageView;

        public ViewHolder(View v, int width, final Activity activity,
                          final ArrayList<LazyImage> data) {
            super(v);
            v.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent i = new Intent(activity, ImageViewActivity.class);
                    i.putExtra("filename", data.get(getPosition()).getDescriptor());
                    i.putExtra("fromrect", calcUnclippedRect(v));
                    i.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
                    activity.startActivity(i);
                    Log.d(TAG, "Element " + getPosition() + " clicked.");
                }
            });
            imageView = (ImageView) v.findViewById(R.id.itemImageView);
            ViewGroup.LayoutParams params = imageView.getLayoutParams();
            params.height = width;
            imageView.setLayoutParams(params);
        }

        public ImageView getImageView() {
            return imageView;
        }
    }

    public static Executor sExecutor = Executors.newFixedThreadPool(8);

    private class LazyImage {

        private ImageDescriptor mFile;
        private boolean mBusy;

        public LazyImage(ImageDescriptor file) {
            mFile = file;
            mBusy = false;
        }

        public ImageDescriptor getDescriptor(){
            return mFile;
        }

        public void decode(final ViewHolder holder, final int position) {
            final Integer imageSize = calcNeededImageSize(mBaseWidth);
            final String fname = imageSize.toString() + mFile.getUniqueRepr();
            Bitmap bmp = GlobalContext.getImageCacher().getBitmapFromMemCache(fname);
            if (bmp == null) {
                holder.getImageView().setImageResource(R.drawable.stub);
                if(!mBusy) {
                    mBusy = true;
                    new AsyncTask<String, Integer, Bitmap>() {
                        @Override
                        protected Bitmap doInBackground(String... _) {
                            Bitmap bmp = mFile.toBitmap(imageSize, imageSize);
                            // handle null case here
                            if(bmp == null){
                                Log.d(TAG,"Something went wrong");
                            }
                            return bmp;
                        }

                        @Override
                        protected void onPostExecute(Bitmap result) {
                            GlobalContext.getImageCacher().addBitmapToMemoryCache(fname, result);
                            if (position == holder.getAdapterPosition()){
                                holder.getImageView().setImageBitmap(result);
                            }
                            else{
                                notifyItemChanged(position);
                            }
                            mBusy = false;
                            super.onPostExecute(result);
                        }
                    }.executeOnExecutor(sExecutor, null);
                }
            } else {
                holder.getImageView().setImageBitmap(bmp);
            }
        }
    }

    public CustomAdapter(ArrayList<ImageDescriptor> dataSet, int baseWidth, Activity activity) {
        setHasStableIds(true);
        mData = new ArrayList<>();
        for (ImageDescriptor file : dataSet) {
            mData.add(new LazyImage(file));
        }
        mBaseWidth = baseWidth;
        mParActivity = activity;
    }

    public void setData(ArrayList<ImageDescriptor> data){
        mData.clear();
        for (ImageDescriptor file : data) {
            mData.add(new LazyImage(file));
        }
        notifyDataSetChanged();
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
        return Math.min(256, Integer.highestOneBit(width));
    }

}
