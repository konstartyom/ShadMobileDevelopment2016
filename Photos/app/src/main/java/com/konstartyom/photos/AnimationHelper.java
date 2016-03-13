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

    // Hold a reference to the current animator,
    // so that it can be canceled mid-way.
    private Animator mCurrentAnimator;

    // The system "short" animation time duration, in milliseconds. This
    // duration is ideal for subtle animations or animations that occur
    // very frequently.

    Rect mStartBounds;
    ImageView mToView;
    View mBackgroundView;
    Rect mFinalBounds;
    Rect mFinalImageBounds;
    Point mGlobalOffset;
    float mStartScale;
    int mBgColor;

    private void setParams(Rect fromRect, ImageView toView, View background, int bgColor){
        mStartBounds = fromRect;
        mToView = toView;
        mBackgroundView = background;
        mBgColor = bgColor;
    }

    private void calculateCoordinates(){
        mFinalBounds = new Rect();
        mGlobalOffset = new Point();

        // The start bounds are the global visible rectangle of the thumbnail,
        // and the final bounds are the global visible rectangle of the container
        // view. Also set the container view's offset as the origin for the
        // bounds, since that's the origin for the positioning animation
        // properties (X, Y).
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

        // Adjust the start bounds to be the same aspect ratio as the final
        // bounds using the "center crop" technique. This prevents undesirable
        // stretching during the animation. Also calculate the start scaling
        // factor (the end scaling factor is always 1.0).
        if ((float) mFinalImageBounds.width() / mFinalImageBounds.height()
                > (float) mStartBounds.width() / mStartBounds.height()) {
            // Extend start bounds horizontally
            mStartScale = (float) mStartBounds.height() / mFinalImageBounds.height();
            /*float startWidth = mStartScale * mFinalBounds.width();
            float deltaWidth = (startWidth - mStartBounds.width()) / 2;
            mStartBounds.left -= deltaWidth;
            mStartBounds.right += deltaWidth;
            float startHeight = mStartScale * mFinalBounds.height();
            float deltaHeight = (startHeight - mStartBounds.height()) / 2;
            mStartBounds.top -= deltaHeight;
            mStartBounds.bottom += deltaHeight;*/
        } else {
            // Extend start bounds vertically
            mStartScale = (float) mStartBounds.width() / mFinalImageBounds.width();
            /*float startHeight = mStartScale * mFinalBounds.height();
            float deltaHeight = (startHeight - mStartBounds.height()) / 2;
            mStartBounds.top -= deltaHeight;
            mStartBounds.bottom += deltaHeight;*/
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

        // Set the pivot point for SCALE_X and SCALE_Y transformations
        // to the top-left corner of the zoomed-in view (the default
        // is the center of the view).
        mToView.setPivotX(0f);
        mToView.setPivotY(0f);

        // Construct and run the parallel animation of the four translation and
        // scale properties (X, Y, SCALE_X, and SCALE_Y).
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

    public void animateFrom(final Callable<Integer> func){
        if (mCurrentAnimator != null) {
            mCurrentAnimator.cancel();
        }
        final float startScaleFinal = mStartScale;
        ObjectAnimator alphaAnim =
                ObjectAnimator.ofInt(mBackgroundView, "backgroundColor", mBgColor, 0x00000000);
        alphaAnim.setEvaluator(new ArgbEvaluator());
        // Animate the four positioning/sizing properties in parallel,
        // back to their original values.
        AnimatorSet set = new AnimatorSet();
        set.play(ObjectAnimator
                .ofFloat(mToView, View.X, mStartBounds.left))
                .with(ObjectAnimator
                        .ofFloat(mToView,
                                View.Y,mStartBounds.top))
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
                mCurrentAnimator = null;
                try {
                    func.call();
                } catch (Exception _) {
                }
            }

            @Override
            public void onAnimationCancel(Animator animation) {
                //mFromView.setAlpha(1f);
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

    /*public void animate() {
        // If there's an animation in progress, cancel it
        // immediately and proceed with this one.
        if (mCurrentAnimator != null) {
            mCurrentAnimator.cancel();
        }

        // Calculate the starting and ending bounds for the zoomed-in image.
        // This step involves lots of math. Yay, math.
        final Rect startBounds = mFromRect;
        final Rect finalBounds = new Rect();
        final Point globalOffset = new Point();

        // The start bounds are the global visible rectangle of the thumbnail,
        // and the final bounds are the global visible rectangle of the container
        // view. Also set the container view's offset as the origin for the
        // bounds, since that's the origin for the positioning animation
        // properties (X, Y).
        mToView.getGlobalVisibleRect(finalBounds, globalOffset);
        startBounds.offset(-globalOffset.x, -globalOffset.y);
        finalBounds.offset(-globalOffset.x, -globalOffset.y);

        Log.d("startBounds = " + startBounds.toString(), TAG);
        Log.d("finalBounds = " + finalBounds.toString(), TAG);

        // Adjust the start bounds to be the same aspect ratio as the final
        // bounds using the "center crop" technique. This prevents undesirable
        // stretching during the animation. Also calculate the start scaling
        // factor (the end scaling factor is always 1.0).
        float startScale;
        if ((float) finalBounds.width() / finalBounds.height()
                > (float) startBounds.width() / startBounds.height()) {
            // Extend start bounds horizontally
            startScale = (float) startBounds.height() / finalBounds.height();
            float startWidth = startScale * finalBounds.width();
            float deltaWidth = (startWidth - startBounds.width()) / 2;
            startBounds.left -= deltaWidth;
            startBounds.right += deltaWidth;
        } else {
            // Extend start bounds vertically
            startScale = (float) startBounds.width() / finalBounds.width();
            float startHeight = startScale * finalBounds.height();
            float deltaHeight = (startHeight - startBounds.height()) / 2;
            startBounds.top -= deltaHeight;
            startBounds.bottom += deltaHeight;
        }

        // Hide the thumbnail and show the zoomed-in view. When the animation
        // begins, it will position the zoomed-in view in the place of the
        // thumbnail.
        //mFromView.setAlpha(0f);
        mToView.setVisibility(View.VISIBLE);

        // Set the pivot point for SCALE_X and SCALE_Y transformations
        // to the top-left corner of the zoomed-in view (the default
        // is the center of the view).
        mToView.setPivotX(0f);
        mToView.setPivotY(0f);

        // Construct and run the parallel animation of the four translation and
        // scale properties (X, Y, SCALE_X, and SCALE_Y).
        AnimatorSet set = new AnimatorSet();
        set
                .play(ObjectAnimator.ofFloat(mToView, View.X,
                        startBounds.left, finalBounds.left))
                .with(ObjectAnimator.ofFloat(mToView, View.Y,
                        startBounds.top, finalBounds.top))
                .with(ObjectAnimator.ofFloat(mToView, View.SCALE_X,
                        startScale, 1f))
                .with(ObjectAnimator.ofFloat(mToView, View.SCALE_Y,
                        startScale, 1f));
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

        // Upon clicking the zoomed-in image, it should zoom back down
        // to the original bounds and show the thumbnail instead of
        // the expanded image.
        final float startScaleFinal = startScale;
        mToView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mCurrentAnimator != null) {
                    mCurrentAnimator.cancel();
                }

                // Animate the four positioning/sizing properties in parallel,
                // back to their original values.
                AnimatorSet set = new AnimatorSet();
                set.play(ObjectAnimator
                        .ofFloat(mToView, View.X, startBounds.left))
                        .with(ObjectAnimator
                                .ofFloat(mToView,
                                        View.Y,startBounds.top))
                        .with(ObjectAnimator
                                .ofFloat(mToView,
                                        View.SCALE_X, startScaleFinal))
                        .with(ObjectAnimator
                                .ofFloat(mToView,
                                        View.SCALE_Y, startScaleFinal));
                set.setDuration(ANIM_TIME);
                set.setInterpolator(new DecelerateInterpolator());
                set.addListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        //mFromView.setAlpha(1f);
                        mToView.setVisibility(View.GONE);
                        mCurrentAnimator = null;
                    }

                    @Override
                    public void onAnimationCancel(Animator animation) {
                        //mFromView.setAlpha(1f);
                        mToView.setVisibility(View.GONE);
                        mCurrentAnimator = null;
                    }
                });
                set.start();
                mCurrentAnimator = set;
            }
        });
    }*/
}
