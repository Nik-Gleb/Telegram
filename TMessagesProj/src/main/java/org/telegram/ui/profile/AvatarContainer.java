package org.telegram.ui.profile;

import android.annotation.SuppressLint;
import android.view.View;
import android.widget.FrameLayout;

import androidx.annotation.NonNull;

import org.telegram.messenger.AndroidUtilities;

@SuppressLint("ViewConstructor")
final class AvatarContainer extends FrameLayout {

    private static final float
            NO_CHANGES = -1f,
            ACCURACY_BASE = 10f,
            ACCURACY_EXP = 3f,
            ACCURACY_VALUE = (float) Math.pow(ACCURACY_BASE, ACCURACY_EXP);

    @NonNull
    private final  View listview;

    private final float iconRatio;

    private final float iconCenter;

    private float
            scale = 1f,
            transition = 0f,
            container = 00f;

    AvatarContainer(
            @NonNull View listView,
            float iconSize,
            float iconExtra
    ) {
        super(listView.getContext());
        this.listview = listView;
        final float iconSizeDp = AndroidUtilities.dp2(iconSize);
        final float iconExtraDp = AndroidUtilities.dp2(iconExtra);
        this.iconRatio = iconSizeDp / iconExtraDp;
        this.iconCenter = (iconSizeDp + iconExtraDp) / 2f;
    }

    @Override
    public void setScaleX(float scaleX) {
        super.setScaleX(scaleX);
        if (scale == scaleX) return;
        scale = scaleX;
        update();
    }

    @Override
    public void setScaleY(float scaleY) {
        super.setScaleY(scaleY);
        if (scale == scaleY) return;
        scale = scaleY;
        update();
    }

    @Override
    public void setTranslationX(float translationX) {
        super.setTranslationX(translationX);
        if (transition == translationX) return;
        transition = translationX;
        update();
    }

    private void update() {
        final float container = container();
        invalidate(
                container < 0 ? this.container : container
        );
    }

    private float container() {
        final float size = listview.getMeasuredWidth();
        return size == container ? NO_CHANGES : size;
    }

    private void invalidate(float container) {
        final float center = container / 2f;
        progress0 = round((scale - 1f) * iconRatio);
        progress1 = round(transition / (center - iconCenter));
        //System.out.println("AVATAR = " + progress0 + " " + progress1 + " " + scale);
        this.container = container;
    }

    private float progress0 =  0;

    float progress0() {  ensure();  return progress0; }


    private float progress1 =  0;

    float progress1() {  ensure();  return progress1; }

    private void ensure() {
        final float container = container();
        if (
                container != -1f
        ) invalidate(container);
    }

    private static float round(float value) {
        return Math.round(value * ACCURACY_VALUE) / ACCURACY_VALUE;
    }
}
