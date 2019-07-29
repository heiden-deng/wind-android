package com.akaxin.client.util;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.support.annotation.AnimRes;
import android.support.annotation.AnimatorRes;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;

import com.akaxin.client.R;

/**
 * Created by alexfan on 2018/4/8.
 */

public abstract class AnimationUtil {

    private static void showViewWithAnim(final View view, @AnimRes int animationId) {
        Animation animation = AnimationUtils.loadAnimation(view.getContext(), animationId);
        if (view.getVisibility() == View.VISIBLE) return;
        animation.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
                view.setVisibility(View.VISIBLE);
            }

            @Override
            public void onAnimationEnd(Animation animation) {
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
        view.startAnimation(animation);
    }

    private static void hideViewWithAnim(final View view, @AnimRes int animationId) {
        Animation animation = AnimationUtils.loadAnimation(view.getContext(), animationId);
        if (view.getVisibility() != View.VISIBLE) return;
        animation.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                view.setVisibility(View.INVISIBLE);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
        view.startAnimation(animation);
    }

    public static void scaleUp(final View view) {
        hideViewWithAnim(view, R.anim.scale_up_slow);
    }

    public static void scaleDown(final View view) {
        hideViewWithAnim(view, R.anim.scale_down_slow);
    }

    public static void leftSlideFadeIn(final View view) {
        showViewWithAnim(view, R.anim.anim_left_slide_fade_in);
    }

    public static void rightSlideFadeOut(final View view) {
        hideViewWithAnim(view, R.anim.anim_right_slide_fade_out);
    }

    public static void crossFade(final View fromView, final View toView) {
        if (fromView.getVisibility() != View.VISIBLE && toView.getVisibility() == View.VISIBLE)
            return;

        toView.setAlpha(0f);
        toView.setVisibility(View.VISIBLE);

        // Animate the toView to 100% opacity, and clear any animation
        // listener set on the view.
        toView.animate()
                .alpha(1f)
                .setDuration(400)
                .setListener(null);

        // Animate the fromView to 0% opacity. After the animation ends,
        // set its visibility to INVISIBLE as an optimization step (it won't
        // participate in layout passes, etc.)
        fromView.animate()
                .alpha(0f)
                .setDuration(400)
                .setListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        fromView.setVisibility(View.INVISIBLE);
                    }
                });
    }
}
