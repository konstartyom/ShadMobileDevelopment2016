package com.konstartyom.photos;


import android.graphics.Bitmap;
import android.graphics.Rect;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.ViewTreeObserver;

import java.util.concurrent.Callable;

import it.sephiroth.android.library.imagezoom.ImageViewTouch;
import it.sephiroth.android.library.imagezoom.ImageViewTouchBase;

public class ImageViewActivity extends AppCompatActivity {

    private static AnimationHelper animHelper = new AnimationHelper();
    private static final String TAG = "ImageViewActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.large_image);
        Bundle b = getIntent().getExtras();
        final ImageDescriptor file = b.getParcelable("filename");
        Log.d(TAG, "File is " + file.getClass().getName());
        Log.d(TAG, "Unique repr: " + file.getUniqueRepr());
        final ImageViewTouch image = (ImageViewTouch)findViewById(R.id.largeImageView);
        setImgViewListeners(image);
        final Rect fromRect = b.getParcelable("fromrect");
        final AppCompatActivity me = this;
        final View parentL = findViewById(R.id.largeImageContainer);

        new AsyncTask<String, Integer, Bitmap>() {
            // calculate size based on screen size

            @Override
            protected Bitmap doInBackground(String... _) {
                int size = Math.max(GlobalContext.getScreenWidth(),
                        GlobalContext.getScreenHeight()) / 2;
                return file.toBitmap(size, size);
            }

            @Override
            protected void onPostExecute(Bitmap bitmap) {
                image.setDisplayType(ImageViewTouchBase.DisplayType.FIT_TO_SCREEN);
                image.setImageBitmap(bitmap);
                Rect r = new Rect();
                image.getGlobalVisibleRect(r);
                animHelper.animateTo(me, fromRect, image, parentL,
                        getResources().getColor(R.color.colorLargeImageBG));
            }
        }.execute();
    }

    void setImgViewListeners(ImageViewTouch view){
        final ImageViewActivity me = this;
        view.setSingleTapListener(
                new ImageViewTouch.OnImageViewTouchSingleTapListener() {

                    @Override
                    public void onSingleTapConfirmed() {
                        me.animatedFinish();
                    }
                }
        );
    }

    @Override
    protected void onPause() {
        super.onPause();
        overridePendingTransition(0, 0);
    }

    private void animatedFinish(){
        final AppCompatActivity me = this;
        ((ImageViewTouch) findViewById(R.id.largeImageView)).resetMatrix();
        animHelper.animateFrom(new Callable<Integer>(){
            @Override
            public Integer call(){
                me.finish();
                return null;
            }
        });
    }

    @Override
    public void onBackPressed(){
        animatedFinish();
    }
}
