package org.telegram.ui.profile;

import android.util.SparseIntArray;

import androidx.recyclerview.widget.DiffUtil;

import org.telegram.tgnet.TLRPC;
import org.telegram.ui.ProfileActivity;

import java.util.ArrayList;

public class DiffCallback extends DiffUtil.Callback {

    private final ProfileActivity mProfileActivity;
    public int oldRowCount;

    public SparseIntArray oldPositionToItem = new SparseIntArray();
    public SparseIntArray newPositionToItem = new SparseIntArray();
    public ArrayList<TLRPC.ChatParticipant> oldChatParticipant = new ArrayList<>();
    public ArrayList<Integer> oldChatParticipantSorted = new ArrayList<>();
    public int oldMembersStartRow;
    public int oldMembersEndRow;

    public DiffCallback(ProfileActivity mProfileActivity) {
        this.mProfileActivity = mProfileActivity;
    }

    @Override
    public int getOldListSize() {
        return oldRowCount;
    }

    @Override
    public int getNewListSize() {
        return mProfileActivity.rowCount;
    }

    @Override
    public boolean areItemsTheSame(int oldItemPosition, int newItemPosition) {
        if (newItemPosition >= mProfileActivity.membersStartRow && newItemPosition < mProfileActivity.membersEndRow) {
            if (oldItemPosition >= oldMembersStartRow && oldItemPosition < oldMembersEndRow) {
                TLRPC.ChatParticipant oldItem;
                TLRPC.ChatParticipant newItem;
                if (!oldChatParticipantSorted.isEmpty()) {
                    oldItem = oldChatParticipant.get(oldChatParticipantSorted.get(oldItemPosition - oldMembersStartRow));
                } else {
                    oldItem = oldChatParticipant.get(oldItemPosition - oldMembersStartRow);
                }

                if (!mProfileActivity.sortedUsers.isEmpty()) {
                    newItem = mProfileActivity.visibleChatParticipants.get(mProfileActivity.visibleSortedUsers.get(newItemPosition - mProfileActivity.membersStartRow));
                } else {
                    newItem = mProfileActivity.visibleChatParticipants.get(newItemPosition - mProfileActivity.membersStartRow);
                }
                return oldItem.user_id == newItem.user_id;
            }
        }
        int oldIndex = oldPositionToItem.get(oldItemPosition, -1);
        int newIndex = newPositionToItem.get(newItemPosition, -1);
        return oldIndex == newIndex && oldIndex >= 0;
    }

    @Override
    public boolean areContentsTheSame(int oldItemPosition, int newItemPosition) {
        return areItemsTheSame(oldItemPosition, newItemPosition);
    }

    public void fillPositions(SparseIntArray sparseIntArray) {
        sparseIntArray.clear();
        int pointer = 0;
        put(++pointer, mProfileActivity.setAvatarRow, sparseIntArray);
        put(++pointer, mProfileActivity.setAvatarSectionRow, sparseIntArray);
        put(++pointer, mProfileActivity.numberSectionRow, sparseIntArray);
        put(++pointer, mProfileActivity.numberRow, sparseIntArray);
        put(++pointer, mProfileActivity.setUsernameRow, sparseIntArray);
        put(++pointer, mProfileActivity.bioRow, sparseIntArray);
        put(++pointer, mProfileActivity.phoneSuggestionRow, sparseIntArray);
        put(++pointer, mProfileActivity.phoneSuggestionSectionRow, sparseIntArray);
        put(++pointer, mProfileActivity.passwordSuggestionRow, sparseIntArray);
        put(++pointer, mProfileActivity.passwordSuggestionSectionRow, sparseIntArray);
        put(++pointer, mProfileActivity.graceSuggestionRow, sparseIntArray);
        put(++pointer, mProfileActivity.graceSuggestionSectionRow, sparseIntArray);
        put(++pointer, mProfileActivity.settingsSectionRow, sparseIntArray);
        put(++pointer, mProfileActivity.settingsSectionRow2, sparseIntArray);
        put(++pointer, mProfileActivity.notificationRow, sparseIntArray);
        put(++pointer, mProfileActivity.languageRow, sparseIntArray);
        put(++pointer, mProfileActivity.premiumRow, sparseIntArray);
        put(++pointer, mProfileActivity.starsRow, sparseIntArray);
        put(++pointer, mProfileActivity.businessRow, sparseIntArray);
        put(++pointer, mProfileActivity.premiumSectionsRow, sparseIntArray);
        put(++pointer, mProfileActivity.premiumGiftingRow, sparseIntArray);
        put(++pointer, mProfileActivity.privacyRow, sparseIntArray);
        put(++pointer, mProfileActivity.dataRow, sparseIntArray);
        put(++pointer, mProfileActivity.liteModeRow, sparseIntArray);
        put(++pointer, mProfileActivity.chatRow, sparseIntArray);
        put(++pointer, mProfileActivity.filtersRow, sparseIntArray);
        put(++pointer, mProfileActivity.stickersRow, sparseIntArray);
        put(++pointer, mProfileActivity.devicesRow, sparseIntArray);
        put(++pointer, mProfileActivity.devicesSectionRow, sparseIntArray);
        put(++pointer, mProfileActivity.helpHeaderRow, sparseIntArray);
        put(++pointer, mProfileActivity.questionRow, sparseIntArray);
        put(++pointer, mProfileActivity.faqRow, sparseIntArray);
        put(++pointer, mProfileActivity.policyRow, sparseIntArray);
        put(++pointer, mProfileActivity.helpSectionCell, sparseIntArray);
        put(++pointer, mProfileActivity.debugHeaderRow, sparseIntArray);
        put(++pointer, mProfileActivity.sendLogsRow, sparseIntArray);
        put(++pointer, mProfileActivity.sendLastLogsRow, sparseIntArray);
        put(++pointer, mProfileActivity.clearLogsRow, sparseIntArray);
        put(++pointer, mProfileActivity.switchBackendRow, sparseIntArray);
        put(++pointer, mProfileActivity.versionRow, sparseIntArray);
        put(++pointer, mProfileActivity.emptyRow, sparseIntArray);
        put(++pointer, mProfileActivity.bottomPaddingRow, sparseIntArray);
        put(++pointer, mProfileActivity.infoHeaderRow, sparseIntArray);
        put(++pointer, mProfileActivity.phoneRow, sparseIntArray);
        put(++pointer, mProfileActivity.locationRow, sparseIntArray);
        put(++pointer, mProfileActivity.userInfoRow, sparseIntArray);
        put(++pointer, mProfileActivity.channelInfoRow, sparseIntArray);
        put(++pointer, mProfileActivity.usernameRow, sparseIntArray);
        put(++pointer, mProfileActivity.notificationsDividerRow, sparseIntArray);
        put(++pointer, mProfileActivity.reportDividerRow, sparseIntArray);
        put(++pointer, mProfileActivity.notificationsRow, sparseIntArray);
        put(++pointer, mProfileActivity.infoSectionRow, sparseIntArray);
        put(++pointer, mProfileActivity.affiliateRow, sparseIntArray);
        put(++pointer, mProfileActivity.infoAffiliateRow, sparseIntArray);
        put(++pointer, mProfileActivity.sendMessageRow, sparseIntArray);
        put(++pointer, mProfileActivity.reportRow, sparseIntArray);
        put(++pointer, mProfileActivity.reportReactionRow, sparseIntArray);
        put(++pointer, mProfileActivity.addToContactsRow, sparseIntArray);
        put(++pointer, mProfileActivity.settingsTimerRow, sparseIntArray);
        put(++pointer, mProfileActivity.settingsKeyRow, sparseIntArray);
        put(++pointer, mProfileActivity.secretSettingsSectionRow, sparseIntArray);
        put(++pointer, mProfileActivity.membersHeaderRow, sparseIntArray);
        put(++pointer, mProfileActivity.addMemberRow, sparseIntArray);
        put(++pointer, mProfileActivity.subscribersRow, sparseIntArray);
        put(++pointer, mProfileActivity.subscribersRequestsRow, sparseIntArray);
        put(++pointer, mProfileActivity.administratorsRow, sparseIntArray);
        put(++pointer, mProfileActivity.settingsRow, sparseIntArray);
        put(++pointer, mProfileActivity.blockedUsersRow, sparseIntArray);
        put(++pointer, mProfileActivity.membersSectionRow, sparseIntArray);
        put(++pointer, mProfileActivity.channelBalanceSectionRow, sparseIntArray);
        put(++pointer, mProfileActivity.sharedMediaRow, sparseIntArray);
        put(++pointer, mProfileActivity.unblockRow, sparseIntArray);
        put(++pointer, mProfileActivity.addToGroupButtonRow, sparseIntArray);
        put(++pointer, mProfileActivity.addToGroupInfoRow, sparseIntArray);
        put(++pointer, mProfileActivity.joinRow, sparseIntArray);
        put(++pointer, mProfileActivity.lastSectionRow, sparseIntArray);
        put(++pointer, mProfileActivity.notificationsSimpleRow, sparseIntArray);
        put(++pointer, mProfileActivity.bizHoursRow, sparseIntArray);
        put(++pointer, mProfileActivity.bizLocationRow, sparseIntArray);
        put(++pointer, mProfileActivity.birthdayRow, sparseIntArray);
        put(++pointer, mProfileActivity.channelRow, sparseIntArray);
        put(++pointer, mProfileActivity.botStarsBalanceRow, sparseIntArray);
        put(++pointer, mProfileActivity.botTonBalanceRow, sparseIntArray);
        put(++pointer, mProfileActivity.channelBalanceRow, sparseIntArray);
        put(++pointer, mProfileActivity.balanceDividerRow, sparseIntArray);
        put(++pointer, mProfileActivity.botAppRow, sparseIntArray);
        put(++pointer, mProfileActivity.botPermissionsHeader, sparseIntArray);
        put(++pointer, mProfileActivity.botPermissionLocation, sparseIntArray);
        put(++pointer, mProfileActivity.botPermissionEmojiStatus, sparseIntArray);
        put(++pointer, mProfileActivity.botPermissionBiometry, sparseIntArray);
        put(++pointer, mProfileActivity.botPermissionsDivider, sparseIntArray);
        put(++pointer, mProfileActivity.channelDividerRow, sparseIntArray);
    }

    private void put(int id, int position, SparseIntArray sparseIntArray) {
        if (position >= 0) {
            sparseIntArray.put(position, id);
        }
    }
}
