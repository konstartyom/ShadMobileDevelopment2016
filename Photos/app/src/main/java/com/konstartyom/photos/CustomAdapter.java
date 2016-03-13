package com.konstartyom.photos;

import android.app.Activity;
import android.app.ActivityOptions;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Point;
import android.graphics.Rect;
import android.os.AsyncTask;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.support.v7.app.AppCompatActivity;
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

    private int mResolution;

    private Activity mParActivity;

    @Override
    public long getItemId(int position) {
        try {
            return mData.get(position).mFile.getCanonicalPath().hashCode();
        }
        catch(IOException _) {
            return 0;
        }
    }

    // BEGIN_INCLUDE(recyclerViewSampleViewHolder)
    /**
     * Provide a reference to the type of views that you are using (custom ViewHolder)
     */
    public static class ViewHolder extends RecyclerView.ViewHolder {
        private static String TAG = "CustomAdapterHolder";

        private final ImageView imageView;
        private int mPosition;
        private int mRefWidth;
        private int mRes;

        public ViewHolder(View v, int width, int resolution, final Activity activity,
                          final ArrayList<LazyImage> data) {
            super(v);
            // Define click listener for the ViewHolder's View.
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
            mRefWidth = width;
            mRes = resolution;
            imageView = (ImageView)v.findViewById(R.id.itemImageView);
            ViewGroup.LayoutParams params = (ViewGroup.LayoutParams) imageView.getLayoutParams();
            params.height = width;
            imageView.setLayoutParams(params);
            //imageView.setImageURI(Uri.parse("file:/" + ));
            //imageView = (TextView) v.findViewById(R.id.itemTextView);
        }

        public ImageView getImageView() {
            return imageView;
        }

        public int getRPosition() {return mPosition; }

        public void setRPosition(int pos) {mPosition = pos;}

        public int getResolution(){
            return mRes;
        }

        public int getmRefWidth() {return mRefWidth; }
    }
    // END_INCLUDE(recyclerViewSampleViewHolder)

    private class LazyImage{

        private File mFile;
        //private Bitmap mImg;

        /*private void setBitmap(Bitmap bmp){
            mImg = bmp;
        }*/

        public LazyImage(File file){
            mFile = file;
            //mImg = null;
        }

        private String getFileName(){
            try{
                return(mFile.getCanonicalPath());
            }
            catch(IOException _){
                return "";
            }
        }

        public void decode(final ViewHolder holder, final int position){
            final String fname = getFileName();
            Bitmap bmp = ImageCacher.getBitmapFromMemCache(fname);
            if(bmp == null) {
                holder.getImageView().setImageResource(R.drawable.stub);
                final File f = mFile;
                final LazyImage me = this;
                new AsyncTask<String, Integer, Bitmap>(){
                    @Override
                    protected Bitmap doInBackground(String ... _){
                        return ImageLoader.decodeBitmap(f, holder.getResolution(),
                                                holder.getResolution());
                    }

                    @Override
                    protected void onPostExecute(Bitmap result) {
                        ImageCacher.addBitmapToMemoryCache(fname, result);
                        //me.setBitmap(result);
                        if(position == holder.getRPosition()) {
                            holder.getImageView().setImageBitmap(result);
                        }
                        super.onPostExecute(result);
                    }
                }.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, null);
            }
            else {
                holder.getImageView().setImageBitmap(bmp);
            }
        }

        public String getImagePath(){
            try{
                return mFile.getCanonicalPath();
            }
            catch(IOException _){
                return null;
            }
        }
    }
    /**
     * Initialize the dataset of the Adapter.
     *
     * @param dataSet String[] containing the data to populate views to be used by RecyclerView.
     */
    public CustomAdapter(ArrayList<File> dataSet, int baseWidth,
                         int resolution, Activity activity) {
        setHasStableIds(true);
        mData = new ArrayList<>();
        for(File file : dataSet){
            mData.add(new LazyImage(file));
        }
        mBaseWidth = baseWidth;
        mResolution = resolution;
        mParActivity = activity;
    }

    // BEGIN_INCLUDE(recyclerViewOnCreateViewHolder)
    // Create new views (invoked by the layout manager)
    @Override
    public ViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
        // Create a new view.
        View v = LayoutInflater.from(viewGroup.getContext())
                .inflate(R.layout.row_item, viewGroup, false);

        return new ViewHolder(v, mBaseWidth, mResolution, mParActivity, mData);
    }
    // END_INCLUDE(recyclerViewOnCreateViewHolder)

    // BEGIN_INCLUDE(recyclerViewOnBindViewHolder)
    // Replace the contents of a view (invoked by the layout manager)
    @Override
    public void onBindViewHolder(ViewHolder viewHolder, final int position) {
        Log.d(TAG, "Element " + position + " set.");
        //TEST
        /*viewHolder.getImageView().setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                mData.remove(position);
                notifyDataSetChanged();
            }
        });*/
        //TEST

        // Get element from your dataset at this position and replace the contents of the view
        // with that element
        //ImageView imgView = viewHolder.getImageView();
        viewHolder.setRPosition(position);
        mData.get(position).decode(viewHolder,position);
        //viewHolder.getImageView().setImageBitmap(decodeBitmap(mData.get(position)), );
    }


    // END_INCLUDE(recyclerViewOnBindViewHolder)

    // Return the size of your dataset (invoked by the layout manager)
    @Override
    public int getItemCount() {
        return mData.size();
    }

    private static Rect calcUnclippedRect(View v){
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

}
