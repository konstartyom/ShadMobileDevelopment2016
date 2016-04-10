package com.konstartyom.photos;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ArgbEvaluator;
import android.animation.ObjectAnimator;
import android.app.Activity;
import android.graphics.Point;
import android.graphics.Rect;
import android.util.Log;
import android.view.View;
import android.view.animation.DecelerateInterpolator;
import android.widget.ImageView;

import java.util.concurrent.Callable;

public class AnimationHelper {
    private static final String TAG = "AnimHelper";

    private static final int ANIM_TIME = 400;

    private Animator mCurrentAnimator;

    Rect mStartBounds;
    ImageView mToView;
    View mBackgroundView;
    Rect mFinalBounds;
    Rect mFinalImageBounds;
    Point mGlobalOffset;
    float mStartScale;
    int mBgColor;

    private void setParams(Rect fromRect, ImageView toView, View background, int bgColor) {
        mStartBounds = fromRect;
        mToView = toView;
        mBackgroundView = background;
        mBgColor = bgColor;
    }

    private void calculateCoordinates() {
        mFinalBounds = new Rect();
        mGlobalOffset = new Point();
        mToView.getGlobalVisibleRect(mFinalBounds, mGlobalOffset);
        mStartBounds.offset(-mGlobalOffset.x, -mGlobalOffset.y);
        mFinalBounds.offset(-mGlobalOffset.x, -mGlobalOffset.y);

        Log.d("startBounds = " + mStartBounds.toString(), TAG);
        Log.d("finalBounds = " + mFinalBounds.toString(), TAG);

        mFinalImageBounds = mToView.getDrawable().getBounds();
        if ((float) mFinalBounds.width() / mFinalBounds.height()
                > (float) mFinalImageBounds.width() / mFinalImageBounds.height()) {
            float imgScale = (float) mFinalImageBounds.height() / mFinalBounds.height();
            float finalImageWidth = mFinalImageBounds.width() / imgScale;
            float deltaWidth = (mFinalBounds.width() - finalImageWidth) / 2;
            mFinalImageBounds.top = mFinalBounds.top;
            mFinalImageBounds.bottom = mFinalBounds.bottom;
            mFinalImageBounds.left = mFinalBounds.left;
            mFinalImageBounds.left += deltaWidth;
            mFinalImageBounds.right = mFinalBounds.right;
            mFinalImageBounds.right -= deltaWidth;
        } else {
            float imgScale = (float) mFinalImageBounds.width() / mFinalBounds.width();
            float finalImageHeight = mFinalImageBounds.height() / imgScale;
            float deltaHeight = (mFinalBounds.height() - finalImageHeight) / 2;
            mFinalImageBounds.left = mFinalBounds.left;
            mFinalImageBounds.right = mFinalBounds.right;
            mFinalImageBounds.top = mFinalBounds.top;
            mFinalImageBounds.top += deltaHeight;
            mFinalImageBounds.bottom = mFinalBounds.bottom;
            mFinalImageBounds.bottom -= deltaHeight;
        }

        Log.d("finalImageBounds = " + mFinalImageBounds.toString(), TAG);

        if ((float) mFinalImageBounds.width() / mFinalImageBounds.height()
                > (float) mStartBounds.width() / mStartBounds.height()) {
            mStartScale = (float) mStartBounds.height() / mFinalImageBounds.height();
        } else {
            mStartScale = (float) mStartBounds.width() / mFinalImageBounds.width();
        }
        float startWidth = mStartScale * mFinalBounds.width();
        float deltaWidth = (startWidth - mStartBounds.width()) / 2;
        mStartBounds.left -= deltaWidth;
        mStartBounds.right += deltaWidth;
        float startHeight = mStartScale * mFinalBounds.height();
        float deltaHeight = (startHeight - mStartBounds.height()) / 2;
        mStartBounds.top -= deltaHeight;
        mStartBounds.bottom += deltaHeight;
    }

    public void animateTo(final Activity activity, Rect fromRect, ImageView toView,
                          View background, int bgColor) {
        if (mCurrentAnimator != null) {
            // workaround issue with double click
            activity.finish();
            return;
        }
        setParams(fromRect, toView, background, bgColor);
        calculateCoordinates();
        mToView.setVisibility(View.VISIBLE);
        mBackgroundView.setVisibility(View.VISIBLE);

        mToView.setPivotX(0f);
        mToView.setPivotY(0f);

        ObjectAnimator alphaAnim =
                ObjectAnimator.ofInt(mBackgroundView, "backgroundColor", 0x00000000, mBgColor);
        alphaAnim.setEvaluator(new ArgbEvaluator());

        AnimatorSet set = new AnimatorSet();
        set
                .play(ObjectAnimator.ofFloat(mToView, View.X,
                        mStartBounds.left, mFinalBounds.left))
                .with(ObjectAnimator.ofFloat(mToView, View.Y,
                        mStartBounds.top, mFinalBounds.top))
                .with(ObjectAnimator.ofFloat(mToView, View.SCALE_X,
                        mStartScale, 1f))
                .with(ObjectAnimator.ofFloat(mToView, View.SCALE_Y,
                        mStartScale, 1f))
                .with(alphaAnim);
        set.setDuration(ANIM_TIME);
        set.setInterpolator(new DecelerateInterpolator());

        set.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                mCurrentAnimator = null;
            }

            @Override
            public void onAnimationCancel(Animator animation) {
                mCurrentAnimator = null;
            }
        });
        set.start();
        mCurrentAnimator = set;
    }

    public void animateFrom(final Callable<Integer> func) {
        if (mCurrentAnimator != null) {
            mCurrentAnimator.cancel();
        }
        final float startScaleFinal = mStartScale;
        ObjectAnimator alphaAnim =
                ObjectAnimator.ofInt(mBackgroundView, "backgroundColor", mBgColor, 0x00000000);
        alphaAnim.setEvaluator(new ArgbEvaluator());
        AnimatorSet set = new AnimatorSet();
        set.play(ObjectAnimator
                .ofFloat(mToView, View.X, mStartBounds.left))
                .with(ObjectAnimator
                        .ofFloat(mToView,
                                View.Y, mStartBounds.top))
                .with(ObjectAnimator
                        .ofFloat(mToView,
                                View.SCALE_X, startScaleFinal))
                .with(ObjectAnimator
                        .ofFloat(mToView,
                                View.SCALE_Y, startScaleFinal))
                .with(alphaAnim);
        set.setDuration(ANIM_TIME);
        set.setInterpolator(new DecelerateInterpolator());
        set.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                //mFromView.setAlpha(1f);
                mToView.setVisibility(View.GONE);
                mBackgroundView.setVisibility(View.GONE);
                mCurrentAnimator = null;
                try {
                    func.call();
                } catch (Exception _) {
                }
            }

            @Override
            public void onAnimationCancel(Animator animation) {
                mToView.setVisibility(View.GONE);
                mCurrentAnimator = null;
                try {
                    func.call();
                } catch (Exception _) {
                }
            }
        });
        set.start();
        mCurrentAnimator = set;
    }
}
