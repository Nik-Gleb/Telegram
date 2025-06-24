package org.telegram.ui.profile;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.content.Context;
import android.database.DataSetObserver;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.text.TextPaint;
import android.view.View;

import androidx.viewpager.widget.PagerAdapter;
import androidx.viewpager.widget.ViewPager;

import org.telegram.messenger.AndroidUtilities;
import org.telegram.ui.ActionBar.ActionBarMenuItem;
import org.telegram.ui.Components.CubicBezierInterpolator;

public class PagerIndicatorView extends View {

    private final ProfileActivity mProfileActivity;
    private final RectF indicatorRect = new RectF();

    private final TextPaint textPaint;
    private final Paint backgroundPaint;

    private final ValueAnimator animator;
    private final float[] animatorValues = new float[]{0f, 1f};

    private final PagerAdapter adapter;

    private boolean isIndicatorVisible;

    public PagerIndicatorView(ProfileActivity mProfileActivity, Context context) {
        super(context);
        this.mProfileActivity = mProfileActivity;
        adapter = mProfileActivity.avatarsViewPager.getAdapter();
        setVisibility(GONE);

        textPaint = new TextPaint(Paint.ANTI_ALIAS_FLAG);
        textPaint.setColor(Color.WHITE);
        textPaint.setTypeface(Typeface.SANS_SERIF);
        textPaint.setTextAlign(Paint.Align.CENTER);
        textPaint.setTextSize(AndroidUtilities.dpf2(15f));
        backgroundPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        backgroundPaint.setColor(0x26000000);
        animator = ValueAnimator.ofFloat(0f, 1f);
        animator.setInterpolator(CubicBezierInterpolator.EASE_BOTH);
        animator.addUpdateListener(a -> {
            final float value = AndroidUtilities.lerp(animatorValues, a.getAnimatedFraction());
            if (mProfileActivity.searchItem != null && !mProfileActivity.isPulledDown) {
                mProfileActivity.searchItem.setScaleX(1f - value);
                mProfileActivity.searchItem.setScaleY(1f - value);
                mProfileActivity.searchItem.setAlpha(1f - value);
            }
            if (mProfileActivity.editItemVisible) {
                mProfileActivity.editItem.setScaleX(1f - value);
                mProfileActivity.editItem.setScaleY(1f - value);
                mProfileActivity.editItem.setAlpha(1f - value);
            }
            if (mProfileActivity.callItemVisible) {
                mProfileActivity.callItem.setScaleX(1f - value);
                mProfileActivity.callItem.setScaleY(1f - value);
                mProfileActivity.callItem.setAlpha(1f - value);
            }
            if (mProfileActivity.videoCallItemVisible) {
                mProfileActivity.videoCallItem.setScaleX(1f - value);
                mProfileActivity.videoCallItem.setScaleY(1f - value);
                mProfileActivity.videoCallItem.setAlpha(1f - value);
            }
            setScaleX(value);
            setScaleY(value);
            setAlpha(value);
        });
        boolean expanded = mProfileActivity.expandPhoto;
        animator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                if (isIndicatorVisible) {
                    if (mProfileActivity.searchItem != null) {
                        mProfileActivity.searchItem.setClickable(false);
                    }
                    if (mProfileActivity.editItemVisible) {
                        mProfileActivity.editItem.setVisibility(GONE);
                    }
                    if (mProfileActivity.callItemVisible) {
                        mProfileActivity.callItem.setVisibility(GONE);
                    }
                    if (mProfileActivity.videoCallItemVisible) {
                        mProfileActivity.videoCallItem.setVisibility(GONE);
                    }
                } else {
                    setVisibility(GONE);
                }
                mProfileActivity.updateStoriesViewBounds(false);
            }

            @Override
            public void onAnimationStart(Animator animation) {
                if (mProfileActivity.searchItem != null && !expanded) {
                    mProfileActivity.searchItem.setClickable(true);
                }
                if (mProfileActivity.editItemVisible) {
                    mProfileActivity.editItem.setVisibility(VISIBLE);
                }
                if (mProfileActivity.callItemVisible) {
                    mProfileActivity.callItem.setVisibility(VISIBLE);
                }
                if (mProfileActivity.videoCallItemVisible) {
                    mProfileActivity.videoCallItem.setVisibility(VISIBLE);
                }
                setVisibility(VISIBLE);
                mProfileActivity.updateStoriesViewBounds(false);
            }
        });
        mProfileActivity.avatarsViewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {

            private int prevPage;

            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
            }

            @Override
            public void onPageSelected(int position) {
                int realPosition = mProfileActivity.avatarsViewPager.getRealPosition(position);
                invalidateIndicatorRect(prevPage != realPosition);
                prevPage = realPosition;
                updateAvatarItems();
            }

            @Override
            public void onPageScrollStateChanged(int state) {
            }
        });
        adapter.registerDataSetObserver(new DataSetObserver() {
            @Override
            public void onChanged() {
                int count = mProfileActivity.avatarsViewPager.getRealCount();
                if (mProfileActivity.overlayCountVisible == 0 && count > 1 && count <= 20 && mProfileActivity.overlaysView.isOverlaysVisible()) {
                    mProfileActivity.overlayCountVisible = 1;
                }
                invalidateIndicatorRect(false);
                refreshVisibility(1f);
                updateAvatarItems();
            }
        });
    }

    private void updateAvatarItemsInternal() {
        if (mProfileActivity.otherItem == null || mProfileActivity.avatarsViewPager == null) {
            return;
        }
        if (mProfileActivity.isPulledDown) {
            int position = mProfileActivity.avatarsViewPager.getRealPosition();
            if (position == 0) {
                mProfileActivity.otherItem.hideSubItem(ProfileActivity.set_as_main);
                mProfileActivity.otherItem.showSubItem(ProfileActivity.add_photo);
            } else {
                mProfileActivity.otherItem.showSubItem(ProfileActivity.set_as_main);
                mProfileActivity.otherItem.hideSubItem(ProfileActivity.add_photo);
            }
        }
    }

    private void updateAvatarItems() {
        if (mProfileActivity.imageUpdater == null) {
            return;
        }
        if (mProfileActivity.otherItem.isSubMenuShowing()) {
            AndroidUtilities.runOnUIThread(this::updateAvatarItemsInternal, 500);
        } else {
            updateAvatarItemsInternal();
        }
    }

    public boolean isIndicatorVisible() {
        return isIndicatorVisible;
    }

    public boolean isIndicatorFullyVisible() {
        return isIndicatorVisible && !animator.isRunning();
    }

    public void setIndicatorVisible(boolean indicatorVisible, float durationFactor) {
        if (indicatorVisible != isIndicatorVisible) {
            isIndicatorVisible = indicatorVisible;
            animator.cancel();
            final float value = AndroidUtilities.lerp(animatorValues, animator.getAnimatedFraction());
            if (durationFactor <= 0f) {
                animator.setDuration(0);
            } else if (indicatorVisible) {
                animator.setDuration((long) ((1f - value) * 250f / durationFactor));
            } else {
                animator.setDuration((long) (value * 250f / durationFactor));
            }
            animatorValues[0] = value;
            animatorValues[1] = indicatorVisible ? 1f : 0f;
            animator.start();
        }
    }

    public void refreshVisibility(float durationFactor) {
        setIndicatorVisible(mProfileActivity.isPulledDown && mProfileActivity.avatarsViewPager.getRealCount() > 20, durationFactor);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        invalidateIndicatorRect(false);
    }

    private void invalidateIndicatorRect(boolean pageChanged) {
        if (pageChanged) {
            mProfileActivity.overlaysView.saveCurrentPageProgress();
        }
        mProfileActivity.overlaysView.invalidate();
        final float textWidth = textPaint.measureText(getCurrentTitle());
        indicatorRect.right = getMeasuredWidth() - AndroidUtilities.dp(54f) - (mProfileActivity.qrItem != null ? AndroidUtilities.dp(48) : 0);
        indicatorRect.left = indicatorRect.right - (textWidth + AndroidUtilities.dpf2(16f));
        indicatorRect.top = (mProfileActivity.getActionBar().getOccupyStatusBar() ? AndroidUtilities.statusBarHeight : 0) + AndroidUtilities.dp(15f);
        indicatorRect.bottom = indicatorRect.top + AndroidUtilities.dp(26);
        setPivotX(indicatorRect.centerX());
        setPivotY(indicatorRect.centerY());
        invalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        final float radius = AndroidUtilities.dpf2(12);
        canvas.drawRoundRect(indicatorRect, radius, radius, backgroundPaint);
        canvas.drawText(getCurrentTitle(), indicatorRect.centerX(), indicatorRect.top + AndroidUtilities.dpf2(18.5f), textPaint);
    }

    private String getCurrentTitle() {
        return adapter.getPageTitle(mProfileActivity.avatarsViewPager.getCurrentItem()).toString();
    }

    public ActionBarMenuItem getSecondaryMenuItem() {
        if (mProfileActivity.callItemVisible) {
            return mProfileActivity.callItem;
        } else if (mProfileActivity.editItemVisible) {
            return mProfileActivity.editItem;
        } else if (mProfileActivity.searchItem != null) {
            return mProfileActivity.searchItem;
        } else {
            return null;
        }
    }
}
