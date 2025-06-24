package org.telegram.ui.profile;

import android.content.Context;

import org.telegram.ui.Components.RecyclerListView;
import org.telegram.ui.ProfileActivity;
import org.telegram.ui.Stories.StoriesListPlaceProvider;

public class ClippedListView extends RecyclerListView implements StoriesListPlaceProvider.ClippedView {
    private final ProfileActivity mProfileActivity;

    public ClippedListView(ProfileActivity mProfileActivity, Context context) {
        super(context);
        this.mProfileActivity = mProfileActivity;
    }

    @Override
    public void updateClip(int[] clip) {
        clip[0] = mProfileActivity.getActionBar().getMeasuredHeight();
        clip[1] = getMeasuredHeight() - getPaddingBottom();
    }
}
