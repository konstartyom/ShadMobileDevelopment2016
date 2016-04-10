package com.konstartyom.photos;

import android.content.Context;
import android.util.DisplayMetrics;

public class GlobalContext {
    private static ImageDescHolder mImageDescHolder;
    private static ImageCacher mImageCacher;
    private static Context mContext;
    private static DisplayMetrics mDispMetrics;

    public static ImageDescHolder getImageDescHolder(){
        return mImageDescHolder;
    }

    public static void setImageDescHolder(ImageDescHolder newHolder){
        mImageDescHolder = newHolder;
    }

    public static ImageCacher getImageCacher(){
        return mImageCacher;
    }

    // should be called as early as possible
    public static void init(MainActivity me){
        mContext = me.getApplicationContext();
        // next lines depends on the previous
        mImageCacher = new ImageCacher();
        mImageDescHolder = new ImageDescHolder();
        mDispMetrics = new DisplayMetrics();
        me.getWindowManager().getDefaultDisplay().getMetrics(mDispMetrics);
    }

    public static int getScreenWidth(){
        return mDispMetrics.widthPixels;
    }

    public static int getScreenHeight(){
        return mDispMetrics.heightPixels;
    }

    public static Context getApplicationContext(){
        return mContext;
    }
}
