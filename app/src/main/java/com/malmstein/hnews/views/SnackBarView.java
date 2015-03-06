package com.malmstein.hnews.views;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.DecelerateInterpolator;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.malmstein.hnews.R;
import com.novoda.notils.exception.DeveloperError;

public class SnackBarView extends LinearLayout {

    public static final int DEFAULT_BG_COLOR = 0xEA333333;

    private TextView croutonText;

    private OnClickListener onTextClickListener;

    private CroutonAutoHideRunnable croutonAutoHideRunnable;
    private long dismissAnimationDuration;
    private FrameLayout croutonContainer;

    public SnackBarView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public SnackBarView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        setOrientation(VERTICAL);
        setVisibility(INVISIBLE);
        croutonAutoHideRunnable = new CroutonAutoHideRunnable();
    }

    @Override
    public void setOrientation(int orientation) {
        if (orientation != VERTICAL) {
            throw new DeveloperError("CroutonView supports only vertical orientation");
        }
        super.setOrientation(orientation);
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();

        LayoutInflater layoutInflater = LayoutInflater.from(getContext());
        layoutInflater.inflate(R.layout.merge_snackbar, this, true);

        croutonContainer = (FrameLayout) findViewById(R.id.crouton_container);
        croutonContainer.setBackgroundColor(DEFAULT_BG_COLOR);

        croutonText = (TextView) findViewById(R.id.crouton_text);
        croutonText.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (onTextClickListener != null) {
                    onTextClickListener.onClick(SnackBarView.this);
                }
            }
        });

    }

    @Override
    public void setBackgroundColor(int color) {
        croutonContainer.setBackgroundColor(color);
    }

    @Override
    public void setBackgroundResource(int resid) {
        croutonContainer.setBackgroundResource(resid);
    }

    @Override
    public void setBackground(Drawable background) {
        croutonContainer.setBackground(background);
    }

    @Override
    public void setBackgroundDrawable(Drawable background) {
        croutonContainer.setBackgroundDrawable(background);
    }

    public ShowCroutonAction showSnackBar(CharSequence message) {
        return new ShowCroutonAction(this, message);
    }

    public void hideCrouton() {
        hideCrouton(true);
    }

    public void hideCrouton(long animationDuration) {
        dismissAnimationDuration = animationDuration;
        hideCrouton(true);
    }

    public void hideCrouton(boolean animate) {
        cancelPendingCroutonAutoHide();

        if (getVisibility() != INVISIBLE) {
            if (animate) {
                animateCroutonDisappearance(dismissAnimationDuration);
            } else {
                setVisibility(INVISIBLE);
            }
        }
    }

    private void animateCroutonAppearance(long animationDuration) {
        cancelPendingCroutonAutoHide();

        setVisibility(VISIBLE);
        setTranslationY(getHeight());
        animate()
                .translationY(0f)
                .setInterpolator(new DecelerateInterpolator())
                .setDuration(animationDuration)
                .withEndAction(new Runnable() {
                    @Override
                    public void run() {
                        setEnabled(true);
                    }
                });
    }

    private void scheduleAutoHideIfNeeded(long autohideDelay) {
        if (autohideDelay > 0) {
            postDelayed(croutonAutoHideRunnable, autohideDelay);
        }
    }

    private void cancelPendingCroutonAutoHide() {
        removeCallbacks(croutonAutoHideRunnable);
    }

    private void animateCroutonDisappearance(long animationDuration) {
        cancelPendingCroutonAutoHide();

        setTranslationY(0f);
        setEnabled(false);
        animate()
                .translationY(getHeight())
                .setInterpolator(new AccelerateInterpolator())
                .setDuration(animationDuration)
                .withEndAction(new Runnable() {
                    @Override
                    public void run() {
                        setVisibility(INVISIBLE);
                    }
                });
    }

    public static class ShowCroutonAction {

        private static final int DISMISS_ANIMATION_SPEEDUP_FACTOR = 3;
        public static final long DEFAULT_ANIMATION_DURATION_MS = 500;
        public static final long DEFAULT_AUTOHIDE_DELAY_MS = 5000;

        protected SnackBarView crouton;
        protected long animationDuration;
        protected long autohideDelay;
        private long dismissAnimationDuration;

        protected ShowCroutonAction(SnackBarView crouton, CharSequence message) {
            crouton.onTextClickListener = null;
            crouton.croutonText.setText(message);
            crouton.croutonText.setBackground(null);
            crouton.croutonContainer.setBackgroundColor(DEFAULT_BG_COLOR);

            this.crouton = crouton;
            this.animationDuration = DEFAULT_ANIMATION_DURATION_MS;
            this.autohideDelay = DEFAULT_AUTOHIDE_DELAY_MS;
        }

        public ShowCroutonAction withBackgroundColor(int color) {
            crouton.croutonContainer.setBackgroundColor(color);
            return this;
        }

        public ShowCroutonAction withBackgroundColor(int baseColor, int alpha) {
            crouton.croutonContainer.setBackgroundColor(computeBackgroundColor(baseColor, alpha));
            return this;
        }

        public ShowCroutonAction withTextClickListener(OnClickListener l) {
            crouton.onTextClickListener = l;
            crouton.croutonText.setBackgroundDrawable(crouton.getResources().getDrawable(R.drawable.selector_snackbar));
            return this;
        }

        public ShowCroutonAction withAnimationDuration(long duration) {
            this.animationDuration = duration;
            return this;
        }

        public ShowCroutonAction withAutohideDelay(long delay) {
            this.autohideDelay = delay;
            return this;
        }

        public ShowCroutonAction withDismissAnimationDuration(long duration) {
            this.dismissAnimationDuration = duration;
            return this;
        }

        public void animating() {
            show(true);
        }

        public void withoutAnimating() {
            show(false);
        }

        private void show(boolean animate) {
            crouton.cancelPendingCroutonAutoHide();

            updateDismissAnimationDuration();
            crouton.dismissAnimationDuration = dismissAnimationDuration;

            if (crouton.getVisibility() != VISIBLE) {
                if (animate) {
                    crouton.animateCroutonAppearance(animationDuration);
                } else {
                    crouton.setVisibility(VISIBLE);
                }
            }

            crouton.scheduleAutoHideIfNeeded(autohideDelay);
        }

        private void updateDismissAnimationDuration() {
            if (dismissAnimationDuration <= 0) {
                dismissAnimationDuration = animationDuration / DISMISS_ANIMATION_SPEEDUP_FACTOR;
            }
        }

        private static int computeBackgroundColor(int baseColor, int alpha) {
            return Color.argb(alpha, getRed(baseColor), getGreen(baseColor), getBlue(baseColor));
        }

        private static int getRed(int startColor) {
            return (startColor >> 16) & 0xFF;
        }

        private static int getGreen(int startColor) {
            return (startColor >> 8) & 0xFF;
        }

        private static int getBlue(int startColor) {
            return startColor & 0xFF;
        }
    }

    private class CroutonAutoHideRunnable implements Runnable {

        @Override
        public void run() {
            animateCroutonDisappearance(dismissAnimationDuration);
        }

    }

}
