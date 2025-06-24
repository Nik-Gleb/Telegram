package org.telegram.ui.profile;

import static org.telegram.messenger.AndroidUtilities.dp;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.Shader;
import android.widget.FrameLayout;

import androidx.core.graphics.ColorUtils;

import org.telegram.messenger.AndroidUtilities;
import org.telegram.messenger.MessagesController;
import org.telegram.ui.ActionBar.ActionBar;
import org.telegram.ui.ActionBar.ActionBarMenu;
import org.telegram.ui.ActionBar.Theme;
import org.telegram.ui.Components.AnimatedColor;
import org.telegram.ui.Components.AnimatedEmojiDrawable;
import org.telegram.ui.Components.AnimatedFloat;
import org.telegram.ui.Components.CubicBezierInterpolator;
import org.telegram.ui.Components.SizeNotifierFrameLayout;
import org.telegram.ui.DialogsActivity;
import org.telegram.ui.PeerColorActivity;
import org.telegram.ui.Stars.StarGiftPatterns;

public class TopView extends FrameLayout {

    private final ProfileActivity mProfileActivity;
    private int currentColor;
    private Paint paint = new Paint();

    public TopView(ProfileActivity mProfileActivity, Context context) {
        super(context);
        this.mProfileActivity = mProfileActivity;
        setWillNotDraw(false);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        setMeasuredDimension(MeasureSpec.getSize(widthMeasureSpec), MeasureSpec.getSize(widthMeasureSpec) + AndroidUtilities.dp(3));
    }

    @Override
    public void setBackgroundColor(int color) {
        if (color != currentColor) {
            currentColor = color;
            paint.setColor(color);
            invalidate();
            if (!hasColorById) {
                mProfileActivity.actionBarBackgroundColor = currentColor;
            }
        }
    }

    private boolean hasColorById;
    private final AnimatedFloat hasColorAnimated = new AnimatedFloat(this, 350, CubicBezierInterpolator.EASE_OUT_QUINT);
    public int color1, color2;
    private final AnimatedColor color1Animated = new AnimatedColor(this, 350, CubicBezierInterpolator.EASE_OUT_QUINT);
    private final AnimatedColor color2Animated = new AnimatedColor(this, 350, CubicBezierInterpolator.EASE_OUT_QUINT);

    private int backgroundGradientColor1, backgroundGradientColor2, backgroundGradientHeight;
    private LinearGradient backgroundGradient;
    private final Paint backgroundPaint = new Paint(Paint.ANTI_ALIAS_FLAG);

    public void setBackgroundColorId(MessagesController.PeerColor peerColor, boolean animated) {
        if (peerColor != null) {
            hasColorById = true;
            color1 = peerColor.getBgColor1(Theme.isCurrentThemeDark());
            color2 = peerColor.getBgColor2(Theme.isCurrentThemeDark());
            mProfileActivity.actionBarBackgroundColor = ColorUtils.blendARGB(color1, color2, 0.25f);
            if (peerColor.patternColor != 0) {
                emojiColor = peerColor.patternColor;
            } else {
                emojiColor = PeerColorActivity.adaptProfileEmojiColor(color1);
            }
        } else {
            mProfileActivity.actionBarBackgroundColor = currentColor;
            hasColorById = false;
            if (AndroidUtilities.computePerceivedBrightness(mProfileActivity.getThemedColor(Theme.key_actionBarDefault)) > .8f) {
                emojiColor = mProfileActivity.getThemedColor(Theme.key_windowBackgroundWhiteBlueText);
            } else if (AndroidUtilities.computePerceivedBrightness(mProfileActivity.getThemedColor(Theme.key_actionBarDefault)) < .2f) {
                emojiColor = Theme.multAlpha(mProfileActivity.getThemedColor(Theme.key_actionBarDefaultTitle), .5f);
            } else {
                emojiColor = PeerColorActivity.adaptProfileEmojiColor(mProfileActivity.getThemedColor(Theme.key_actionBarDefault));
            }
        }
        if (!animated) {
            color1Animated.set(color1, true);
            color2Animated.set(color2, true);
        }
        invalidate();
    }

    private int emojiColor;
    private final AnimatedEmojiDrawable.SwapAnimatedEmojiDrawable emoji = new AnimatedEmojiDrawable.SwapAnimatedEmojiDrawable(this, false, dp(20), AnimatedEmojiDrawable.CACHE_TYPE_ALERT_PREVIEW_STATIC);

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        emoji.attach();
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        emoji.detach();
    }

    public final AnimatedFloat emojiLoadedT = new AnimatedFloat(this, 0, 440, CubicBezierInterpolator.EASE_OUT_QUINT);
    public final AnimatedFloat emojiFullT = new AnimatedFloat(this, 0, 440, CubicBezierInterpolator.EASE_OUT_QUINT);

    private boolean hasEmoji;
    private boolean emojiIsCollectible;

    public void setBackgroundEmojiId(long emojiId, boolean isCollectible, boolean animated) {
        emoji.set(emojiId, animated);
        emoji.setColor(emojiColor);
        emojiIsCollectible = isCollectible;
        if (!animated) {
            emojiFullT.force(isCollectible);
        }
        hasEmoji = hasEmoji || emojiId != 0 && emojiId != -1;
        invalidate();
    }

    private boolean emojiLoaded;

    private boolean isEmojiLoaded() {
        if (emojiLoaded) {
            return true;
        }
        if (emoji != null && emoji.getDrawable() instanceof AnimatedEmojiDrawable) {
            AnimatedEmojiDrawable drawable = (AnimatedEmojiDrawable) emoji.getDrawable();
            if (drawable.getImageReceiver() != null && drawable.getImageReceiver().hasImageLoaded()) {
                return emojiLoaded = true;
            }
        }
        return false;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        final int height = ActionBar.getCurrentActionBarHeight() + (mProfileActivity.getActionBar().getOccupyStatusBar() ? AndroidUtilities.statusBarHeight : 0);
        final float v = mProfileActivity.extraHeight + height + mProfileActivity.searchTransitionOffset;

        int y1 = (int) (v * (1.0f - mProfileActivity.mediaHeaderAnimationProgress));

        if (y1 != 0) {
            if (mProfileActivity.previousTransitionFragment != null && mProfileActivity.previousTransitionFragment.getContentView() != null) {
                blurBounds.set(0, 0, getMeasuredWidth(), y1);
                if (mProfileActivity.previousTransitionFragment.getActionBar() != null && !mProfileActivity.previousTransitionFragment.getContentView().blurWasDrawn() && mProfileActivity.previousTransitionFragment.getActionBar().getBackground() == null) {
                    paint.setColor(Theme.getColor(Theme.key_actionBarDefault, mProfileActivity.previousTransitionFragment.getResourceProvider()));
                    canvas.drawRect(blurBounds, paint);
                } else if (mProfileActivity.previousTransitionMainFragment != null && mProfileActivity.previousTransitionMainFragment instanceof DialogsActivity && mProfileActivity.previousTransitionMainFragment.getFragmentView() instanceof SizeNotifierFrameLayout) {
                    mProfileActivity.previousTransitionMainFragment.getActionBar().blurScrimPaint.setColor(Theme.getColor(Theme.key_actionBarDefault, mProfileActivity.previousTransitionMainFragment.getResourceProvider()));
                    ((SizeNotifierFrameLayout) mProfileActivity.previousTransitionMainFragment.getFragmentView()).drawBlurRect(canvas, getY(), blurBounds, mProfileActivity.previousTransitionMainFragment.getActionBar().blurScrimPaint, true);
                } else {
                    mProfileActivity.previousTransitionFragment.getContentView().drawBlurRect(canvas, getY(), blurBounds, mProfileActivity.previousTransitionFragment.getActionBar().blurScrimPaint, true);
                }
            }
            paint.setColor(currentColor);
            final int color1 = color1Animated.set(this.color1);
            final int color2 = color2Animated.set(this.color2);
            final int gradientHeight = AndroidUtilities.statusBarHeight + AndroidUtilities.dp(144);
            if (backgroundGradient == null || backgroundGradientColor1 != color1 || backgroundGradientColor2 != color2 || backgroundGradientHeight != gradientHeight) {
                backgroundGradient = new LinearGradient(0, 0, 0, backgroundGradientHeight = gradientHeight, new int[]{backgroundGradientColor2 = color2, backgroundGradientColor1 = color1}, new float[]{0, 1}, Shader.TileMode.CLAMP);
                backgroundPaint.setShader(backgroundGradient);
            }
            final float progressToGradient = (mProfileActivity.playProfileAnimation == 0 ? 1f : mProfileActivity.getAvatarAnimationProgress()) * hasColorAnimated.set(hasColorById);
            if (progressToGradient < 1) {
                canvas.drawRect(0, 0, getMeasuredWidth(), y1, paint);
            }
            if (progressToGradient > 0) {
                backgroundPaint.setAlpha((int) (0xFF * progressToGradient));
                canvas.drawRect(0, 0, getMeasuredWidth(), y1, backgroundPaint);
            }
            if (hasEmoji) {
                final float loadedScale = emojiLoadedT.set(isEmojiLoaded());
                final float full = emojiFullT.set(emojiIsCollectible);
                if (loadedScale > 0) {
                    canvas.save();
                    canvas.clipRect(0, 0, getMeasuredWidth(), y1);
                    StarGiftPatterns.drawProfilePattern(canvas, emoji, getMeasuredWidth(), ((mProfileActivity.getActionBar().getOccupyStatusBar() ? AndroidUtilities.statusBarHeight : 0) + dp(144)) - (1f - mProfileActivity.extraHeight / dp(88)) * dp(50), Math.min(1f, mProfileActivity.extraHeight / dp(88)), full);
                    canvas.restore();
                }
            }
            if (mProfileActivity.previousTransitionFragment != null) {
                ActionBar actionBar = mProfileActivity.previousTransitionFragment.getActionBar();
                ActionBarMenu menu = actionBar.menu;
                if (actionBar != null && menu != null) {
                    int restoreCount = canvas.save();
                    canvas.translate(actionBar.getX() + menu.getX(), actionBar.getY() + menu.getY());
                    canvas.saveLayerAlpha(0, 0, menu.getMeasuredWidth(), menu.getMeasuredHeight(), (int) (255 * (1f - mProfileActivity.getAvatarAnimationProgress())), Canvas.ALL_SAVE_FLAG);
                    menu.draw(canvas);
                    canvas.restoreToCount(restoreCount);
                }
            }
        }
        if (y1 != v) {
            int color = mProfileActivity.getThemedColor(Theme.key_windowBackgroundWhite);
            paint.setColor(color);
            blurBounds.set(0, y1, getMeasuredWidth(), (int) v);
            ((SizeNotifierFrameLayout)mProfileActivity.contentView).drawBlurRect(canvas, getY(), blurBounds, paint, true);
        }

        if (mProfileActivity.getParentLayout() != null) {
            mProfileActivity.getParentLayout().drawHeaderShadow(canvas, (int) (mProfileActivity.headerShadowAlpha * 255), (int) v);
        }
    }

    private Rect blurBounds = new Rect();
}
