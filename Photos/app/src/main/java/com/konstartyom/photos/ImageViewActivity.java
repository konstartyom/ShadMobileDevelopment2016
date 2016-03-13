package com.konstartyom.photos;


import android.graphics.Rect;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.ImageView;

import java.util.concurrent.Callable;

import it.sephiroth.android.library.imagezoom.ImageViewTouch;
import it.sephiroth.android.library.imagezoom.ImageViewTouchBase;

public class ImageViewActivity extends AppCompatActivity {

    private static AnimationHelper animHelper = new AnimationHelper();

    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        Bundle b = getIntent().getExtras();
        String file = b.getCharSequence("filename").toString();
        setContentView(R.layout.large_image);
        final ImageViewTouch image = (ImageViewTouch)findViewById(R.id.largeImageView);
        setImgViewListeners(image);
        image.setDisplayType(ImageViewTouchBase.DisplayType.FIT_TO_SCREEN);
        image.setImageBitmap(ImageLoader.decodeBitmapFromName(file, 1000, 1000));
        final Rect fromRect = b.getParcelable("fromrect");
        final View parentL = findViewById(R.id.largeImageContainer);
        parentL.getViewTreeObserver()
                .addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                Rect r = new Rect();
                image.getGlobalVisibleRect(r);
                if (!r.isEmpty()) {
                    getResources().getColor(R.color.colorLargeImageBG);
                    animHelper.setParams(fromRect, image, findViewById(R.id.largeImageContainer),
                            getResources().getColor(R.color.colorLargeImageBG));
                    animHelper.animateTo();
                    parentL.getViewTreeObserver().removeGlobalOnLayoutListener(this);
                }
            }
        });
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
