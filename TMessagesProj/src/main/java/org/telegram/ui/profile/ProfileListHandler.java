package org.telegram.ui.profile;

import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import org.telegram.messenger.AndroidUtilities;
import org.telegram.messenger.BirthdayController;
import org.telegram.messenger.ChatObject;
import org.telegram.messenger.DialogObject;
import org.telegram.messenger.FileLog;
import org.telegram.messenger.LocaleController;
import org.telegram.messenger.MediaDataController;
import org.telegram.messenger.MessagesController;
import org.telegram.messenger.NotificationCenter;
import org.telegram.messenger.NotificationsController;
import org.telegram.messenger.R;
import org.telegram.messenger.SharedConfig;
import org.telegram.messenger.browser.Browser;
import org.telegram.tgnet.ConnectionsManager;
import org.telegram.tgnet.TLRPC;
import org.telegram.tgnet.tl.TL_bots;
import org.telegram.ui.ActionBar.AlertDialog;
import org.telegram.ui.ActionBar.BaseFragment;
import org.telegram.ui.ActionBar.Theme;
import org.telegram.ui.ActionIntroActivity;
import org.telegram.ui.Cells.CheckBoxCell;
import org.telegram.ui.Cells.NotificationsCheckCell;
import org.telegram.ui.Cells.TextCell;
import org.telegram.ui.Cells.TextDetailCell;
import org.telegram.ui.ChangeUsernameActivity;
import org.telegram.ui.ChatActivity;
import org.telegram.ui.ChatUsersActivity;
import org.telegram.ui.Components.AlertsCreator;
import org.telegram.ui.Components.BulletinFactory;
import org.telegram.ui.Components.ChatNotificationsPopupWrapper;
import org.telegram.ui.Components.JoinGroupAlert;
import org.telegram.ui.Components.LayoutHelper;
import org.telegram.ui.Components.Premium.boosts.UserSelectorBottomSheet;
import org.telegram.ui.Components.RecyclerListView;
import org.telegram.ui.DataSettingsActivity;
import org.telegram.ui.FiltersSetupActivity;
import org.telegram.ui.IdenticonActivity;
import org.telegram.ui.LanguageSelectActivity;
import org.telegram.ui.LiteModeSettingsActivity;
import org.telegram.ui.LocationActivity;
import org.telegram.ui.MemberRequestsActivity;
import org.telegram.ui.NotificationsSettingsActivity;
import org.telegram.ui.PremiumPreviewFragment;
import org.telegram.ui.PrivacySettingsActivity;
import org.telegram.ui.ProfileActivity;
import org.telegram.ui.ProfileNotificationsActivity;
import org.telegram.ui.ReportBottomSheet;
import org.telegram.ui.SessionsActivity;
import org.telegram.ui.Stars.BotStarsActivity;
import org.telegram.ui.Stars.BotStarsController;
import org.telegram.ui.Stars.StarsIntroActivity;
import org.telegram.ui.StatisticActivity;
import org.telegram.ui.StickersActivity;
import org.telegram.ui.ThemeActivity;
import org.telegram.ui.TopicsNotifySettingsFragments;
import org.telegram.ui.UserInfoActivity;
import org.telegram.ui.bots.AffiliateProgramFragment;
import org.telegram.ui.bots.ChannelAffiliateProgramsFragment;

public class ProfileListHandler {
    public static void listClickHandle(final ProfileActivity mProfileActivity, Context context, View view, int position, float x, float y, long did, BaseFragment lastFragment) {
        if (mProfileActivity.getParentActivity() == null) {
            return;
        }
        mProfileActivity.getListView().stopScroll();
        if (position == mProfileActivity.affiliateRow) {
            TLRPC.User user = mProfileActivity.getMessagesController().getUser(mProfileActivity.userId);
            if (mProfileActivity.getUserInfo() != null && mProfileActivity.getUserInfo().starref_program != null) {
                final long selfId = mProfileActivity.getUserConfig().getClientUserId();
                BotStarsController.getInstance(mProfileActivity.getCurrentAccount()).getConnectedBot(mProfileActivity.getContext(), selfId, mProfileActivity.userId, connectedBot -> {
                    if (connectedBot == null) {
                        ChannelAffiliateProgramsFragment.showConnectAffiliateAlert(context, mProfileActivity.getCurrentAccount(), mProfileActivity.getUserInfo().starref_program, mProfileActivity.getUserConfig().getClientUserId(), mProfileActivity.resourcesProvider, false);
                    } else {
                        ChannelAffiliateProgramsFragment.showShareAffiliateAlert(context, mProfileActivity.getCurrentAccount(), connectedBot, selfId, mProfileActivity.resourcesProvider);
                    }
                });
            } else if (user != null && user.bot_can_edit) {
                mProfileActivity.presentFragment(new AffiliateProgramFragment(mProfileActivity.userId));
            }
        } else if (position == mProfileActivity.notificationsSimpleRow) {
            boolean muted = mProfileActivity.getMessagesController().isDialogMuted(did, mProfileActivity.getTopicId());
            mProfileActivity.getNotificationsController().muteDialog(did, mProfileActivity.getTopicId(), !muted);
            BulletinFactory.createMuteBulletin(mProfileActivity, !muted, null).show();
            mProfileActivity.updateExceptions();
            if (mProfileActivity.notificationsSimpleRow >= 0 && mProfileActivity.listAdapter != null) {
                mProfileActivity.listAdapter.notifyItemChanged(mProfileActivity.notificationsSimpleRow);
            }
        } else if (position == mProfileActivity.addToContactsRow) {
            TLRPC.User user = mProfileActivity.getMessagesController().getUser(mProfileActivity.userId);
            Bundle args = new Bundle();
            args.putLong("user_id", user.id);
            args.putBoolean("addContact", true);
            args.putString("phone", mProfileActivity.vcardPhone);
            args.putString("first_name_card", mProfileActivity.vcardFirstName);
            args.putString("last_name_card", mProfileActivity.vcardLastName);
            mProfileActivity.openAddToContact(user, args);
        } else if (position == mProfileActivity.reportReactionRow) {
            AlertDialog.Builder builder = new AlertDialog.Builder(mProfileActivity.getParentActivity(), mProfileActivity.resourcesProvider);
            builder.setTitle(LocaleController.getString(R.string.ReportReaction));
            builder.setMessage(LocaleController.getString(R.string.ReportAlertReaction));

            TLRPC.Chat chat = mProfileActivity.getMessagesController().getChat(-mProfileActivity.reportReactionFromDialogId);
            CheckBoxCell[] cells = new CheckBoxCell[1];
            if (chat != null && ChatObject.canBlockUsers(chat)) {
                LinearLayout linearLayout = new LinearLayout(mProfileActivity.getParentActivity());
                linearLayout.setOrientation(LinearLayout.VERTICAL);
                cells[0] = new CheckBoxCell(mProfileActivity.getParentActivity(), 1, mProfileActivity.resourcesProvider);
                cells[0].setBackgroundDrawable(Theme.getSelectorDrawable(false));
                cells[0].setText(LocaleController.getString(R.string.BanUser), "", true, false);
                cells[0].setPadding(LocaleController.isRTL ? AndroidUtilities.dp(16) : AndroidUtilities.dp(8), 0, LocaleController.isRTL ? AndroidUtilities.dp(8) : AndroidUtilities.dp(16), 0);
                linearLayout.addView(cells[0], LayoutHelper.createLinear(LayoutHelper.MATCH_PARENT, LayoutHelper.WRAP_CONTENT));
                cells[0].setOnClickListener(v -> {
                    cells[0].setChecked(!cells[0].isChecked(), true);
                });
                builder.setView(linearLayout);
            }

            builder.setPositiveButton(LocaleController.getString(R.string.ReportChat), (dialog, which) -> {
                TLRPC.TL_messages_reportReaction req = new TLRPC.TL_messages_reportReaction();
                req.user_id = mProfileActivity.getMessagesController().getInputUser(mProfileActivity.userId);
                req.peer = mProfileActivity.getMessagesController().getInputPeer(mProfileActivity.reportReactionFromDialogId);
                req.id = mProfileActivity.reportReactionMessageId;
                ConnectionsManager.getInstance(mProfileActivity.getCurrentAccount()).sendRequest(req, (response, error) -> {

                });

                if (cells[0] != null && cells[0].isChecked()) {
                    TLRPC.User user = mProfileActivity.getMessagesController().getUser(mProfileActivity.userId);
                    mProfileActivity.getMessagesController().deleteParticipantFromChat(-mProfileActivity.reportReactionFromDialogId, user);
                }

                mProfileActivity.reportReactionMessageId = 0;
                mProfileActivity.updateListAnimated(false);
                BulletinFactory.of(mProfileActivity).createReportSent(mProfileActivity.resourcesProvider).show();
            });
            builder.setNegativeButton(LocaleController.getString(R.string.Cancel), (dialog, which) -> {
                dialog.dismiss();
            });
            AlertDialog dialog = builder.show();
            TextView button = (TextView) dialog.getButton(DialogInterface.BUTTON_POSITIVE);
            if (button != null) {
                button.setTextColor(Theme.getColor(Theme.key_text_RedBold));
            }
        } else if (position == mProfileActivity.settingsKeyRow) {
            Bundle args = new Bundle();
            args.putInt("chat_id", DialogObject.getEncryptedChatId(mProfileActivity.getDialogId()));
            mProfileActivity.presentFragment(new IdenticonActivity(args));
        } else if (position == mProfileActivity.settingsTimerRow) {
            mProfileActivity.showDialog(AlertsCreator.createTTLAlert(mProfileActivity.getParentActivity(), mProfileActivity.currentEncryptedChat, mProfileActivity.resourcesProvider).create());
        } else if (position == mProfileActivity.notificationsRow) {
            if (LocaleController.isRTL && x <= AndroidUtilities.dp(76) || !LocaleController.isRTL && x >= view.getMeasuredWidth() - AndroidUtilities.dp(76)) {
                NotificationsCheckCell checkCell = (NotificationsCheckCell) view;
                boolean checked = !checkCell.isChecked();

                boolean defaultEnabled = mProfileActivity.getNotificationsController().isGlobalNotificationsEnabled(did, false, false);

                String key = NotificationsController.getSharedPrefKey(did, mProfileActivity.getTopicId());
                if (checked) {
                    SharedPreferences preferences = MessagesController.getNotificationsSettings(mProfileActivity.getCurrentAccount());
                    SharedPreferences.Editor editor = preferences.edit();
                    if (defaultEnabled) {
                        editor.remove("notify2_" + key);
                    } else {
                        editor.putInt("notify2_" + key, 0);
                    }
                    if (mProfileActivity.getTopicId() == 0) {
                        mProfileActivity.getMessagesStorage().setDialogFlags(did, 0);
                        TLRPC.Dialog dialog = mProfileActivity.getMessagesController().dialogs_dict.get(did);
                        if (dialog != null) {
                            dialog.notify_settings = new TLRPC.TL_peerNotifySettings();
                        }
                    }
                    editor.apply();
                } else {
                    int untilTime = Integer.MAX_VALUE;
                    SharedPreferences preferences = MessagesController.getNotificationsSettings(mProfileActivity.getCurrentAccount());
                    SharedPreferences.Editor editor = preferences.edit();
                    long flags;
                    if (!defaultEnabled) {
                        editor.remove("notify2_" + key);
                        flags = 0;
                    } else {
                        editor.putInt("notify2_" + key, 2);
                        flags = 1;
                    }
                    mProfileActivity.getNotificationsController().removeNotificationsForDialog(did);
                    if (mProfileActivity.getTopicId() == 0) {
                        mProfileActivity.getMessagesStorage().setDialogFlags(did, flags);
                        TLRPC.Dialog dialog = mProfileActivity.getMessagesController().dialogs_dict.get(did);
                        if (dialog != null) {
                            dialog.notify_settings = new TLRPC.TL_peerNotifySettings();
                            if (defaultEnabled) {
                                dialog.notify_settings.mute_until = untilTime;
                            }
                        }
                    }
                    editor.apply();
                }
                mProfileActivity.updateExceptions();
                mProfileActivity.getNotificationsController().updateServerNotificationsSettings(did, mProfileActivity.getTopicId());
                checkCell.setChecked(checked);
                RecyclerListView.Holder holder = (RecyclerListView.Holder) mProfileActivity.getListView().findViewHolderForPosition(mProfileActivity.notificationsRow);
                if (holder != null) {
                    mProfileActivity.listAdapter.onBindViewHolder(holder, mProfileActivity.notificationsRow);
                }
                return;
            }
            ChatNotificationsPopupWrapper chatNotificationsPopupWrapper = new ChatNotificationsPopupWrapper(context, mProfileActivity.getCurrentAccount(), null, true, true, new ChatNotificationsPopupWrapper.Callback() {
                @Override
                public void toggleSound() {
                    SharedPreferences preferences = MessagesController.getNotificationsSettings(mProfileActivity.getCurrentAccount());
                    boolean enabled = !preferences.getBoolean("sound_enabled_" + NotificationsController.getSharedPrefKey(did, mProfileActivity.getTopicId()), true);
                    preferences.edit().putBoolean("sound_enabled_" + NotificationsController.getSharedPrefKey(did, mProfileActivity.getTopicId()), enabled).apply();
                    if (BulletinFactory.canShowBulletin(mProfileActivity)) {
                        BulletinFactory.createSoundEnabledBulletin(mProfileActivity, enabled ? NotificationsController.SETTING_SOUND_ON : NotificationsController.SETTING_SOUND_OFF, mProfileActivity.getResourceProvider()).show();
                    }
                }

                @Override
                public void muteFor(int timeInSeconds) {
                    if (timeInSeconds == 0) {
                        if (mProfileActivity.getMessagesController().isDialogMuted(did, mProfileActivity.getTopicId())) {
                            toggleMute();
                        }
                        if (BulletinFactory.canShowBulletin(mProfileActivity)) {
                            BulletinFactory.createMuteBulletin(mProfileActivity, NotificationsController.SETTING_MUTE_UNMUTE, timeInSeconds, mProfileActivity.getResourceProvider()).show();
                        }
                    } else {
                        mProfileActivity.getNotificationsController().muteUntil(did, mProfileActivity.getTopicId(), timeInSeconds);
                        if (BulletinFactory.canShowBulletin(mProfileActivity)) {
                            BulletinFactory.createMuteBulletin(mProfileActivity, NotificationsController.SETTING_MUTE_CUSTOM, timeInSeconds, mProfileActivity.getResourceProvider()).show();
                        }
                        mProfileActivity.updateExceptions();
                        if (mProfileActivity.notificationsRow >= 0 && mProfileActivity.listAdapter != null) {
                            mProfileActivity.listAdapter.notifyItemChanged(mProfileActivity.notificationsRow);
                        }
                    }
                }

                @Override
                public void showCustomize() {
                    if (did != 0) {
                        Bundle args = new Bundle();
                        args.putLong("dialog_id", did);
                        args.putLong("topic_id", mProfileActivity.getTopicId());
                        mProfileActivity.presentFragment(new ProfileNotificationsActivity(args, mProfileActivity.resourcesProvider));
                    }
                }

                @Override
                public void toggleMute() {
                    boolean muted = mProfileActivity.getMessagesController().isDialogMuted(did, mProfileActivity.getTopicId());
                    mProfileActivity.getNotificationsController().muteDialog(did, mProfileActivity.getTopicId(), !muted);
                    if (mProfileActivity.fragmentView != null) {
                        BulletinFactory.createMuteBulletin(mProfileActivity, !muted, null).show();
                    }
                    mProfileActivity.updateExceptions();
                    if (mProfileActivity.notificationsRow >= 0 && mProfileActivity.listAdapter != null) {
                        mProfileActivity.listAdapter.notifyItemChanged(mProfileActivity.notificationsRow);
                    }
                }

                @Override
                public void openExceptions() {
                    Bundle bundle = new Bundle();
                    bundle.putLong("dialog_id", did);
                    TopicsNotifySettingsFragments notifySettings = new TopicsNotifySettingsFragments(bundle);
                    notifySettings.setExceptions(mProfileActivity.notificationsExceptionTopics);
                    mProfileActivity.presentFragment(notifySettings);
                }
            }, mProfileActivity.getResourceProvider());
            chatNotificationsPopupWrapper.update(did, mProfileActivity.getTopicId(), mProfileActivity.notificationsExceptionTopics);
            if (AndroidUtilities.isTablet()) {
                View v = mProfileActivity.getParentLayout().getView();
                x += v.getX() + v.getPaddingLeft();
                y += v.getY() + v.getPaddingTop();
            }
            chatNotificationsPopupWrapper.showAsOptions(mProfileActivity, view, x, y);
        } else if (position == mProfileActivity.unblockRow) {
            mProfileActivity.getMessagesController().unblockPeer(mProfileActivity.userId);
            if (BulletinFactory.canShowBulletin(mProfileActivity)) {
                BulletinFactory.createBanBulletin(mProfileActivity, false).show();
            }
        } else if (position == mProfileActivity.addToGroupButtonRow) {
            try {
                mProfileActivity.getActionBar().getActionBarMenuOnItemClick().onItemClick(mProfileActivity.invite_to_group);
            } catch (Exception e) {
                FileLog.e(e);
            }
        } else if (position == mProfileActivity.sendMessageRow) {
            mProfileActivity.onWriteButtonClick();
        } else if (position == mProfileActivity.reportRow) {
            ReportBottomSheet.openChat(mProfileActivity, mProfileActivity.getDialogId());
        } else if (position >= mProfileActivity.membersStartRow && position < mProfileActivity.membersEndRow) {
            TLRPC.ChatParticipant participant;
            if (!mProfileActivity.sortedUsers.isEmpty()) {
                participant = mProfileActivity.chatInfo.participants.participants.get(mProfileActivity.sortedUsers.get(position - mProfileActivity.membersStartRow));
            } else {
                participant = mProfileActivity.chatInfo.participants.participants.get(position - mProfileActivity.membersStartRow);
            }
            mProfileActivity.onMemberClick(participant, false, view);
        } else if (position == mProfileActivity.addMemberRow) {
            mProfileActivity.openAddMember();
        } else if (position == mProfileActivity.usernameRow) {
            mProfileActivity.processOnClickOrPress(position, view, x, y);
        } else if (position == mProfileActivity.locationRow) {
            if (mProfileActivity.chatInfo.location instanceof TLRPC.TL_channelLocation) {
                LocationActivity fragment = new LocationActivity(LocationActivity.LOCATION_TYPE_GROUP_VIEW);
                fragment.setChatLocation(mProfileActivity.chatId, (TLRPC.TL_channelLocation) mProfileActivity.chatInfo.location);
                mProfileActivity.presentFragment(fragment);
            }
        } else if (position == mProfileActivity.joinRow) {
            mProfileActivity.getMessagesController().addUserToChat(mProfileActivity.getCurrentChat().id, mProfileActivity.getUserConfig().getCurrentUser(), 0, null, mProfileActivity, true, () -> {
                mProfileActivity.updateRowsIds();
                if (mProfileActivity.listAdapter != null) {
                    mProfileActivity.listAdapter.notifyDataSetChanged();
                }
            }, err -> {
                if (err != null && "INVITE_REQUEST_SENT".equals(err.text)) {
                    SharedPreferences preferences = MessagesController.getNotificationsSettings(mProfileActivity.getCurrentAccount());
                    preferences.edit().putLong("dialog_join_requested_time_" + mProfileActivity.getDialogId(), System.currentTimeMillis()).commit();
                    JoinGroupAlert.showBulletin(context, mProfileActivity, ChatObject.isChannel(mProfileActivity.getCurrentChat()) && !mProfileActivity.getCurrentChat().megagroup);
                    mProfileActivity.updateRowsIds();
                    if (mProfileActivity.listAdapter != null) {
                        mProfileActivity.listAdapter.notifyDataSetChanged();
                    }
                    if (lastFragment instanceof ChatActivity) {
                        ((ChatActivity) lastFragment).showBottomOverlayProgress(false, true);
                    }
                    return false;
                }
                return true;
            });
            NotificationCenter.getGlobalInstance().postNotificationName(NotificationCenter.closeSearchByActiveAction);
        } else if (position == mProfileActivity.subscribersRow) {
            Bundle args = new Bundle();
            args.putLong("chat_id", mProfileActivity.chatId);
            args.putInt("type", ChatUsersActivity.TYPE_USERS);
            ChatUsersActivity fragment = new ChatUsersActivity(args);
            fragment.setInfo(mProfileActivity.chatInfo);
            mProfileActivity.presentFragment(fragment);
        } else if (position == mProfileActivity.subscribersRequestsRow) {
            MemberRequestsActivity activity = new MemberRequestsActivity(mProfileActivity.chatId);
            mProfileActivity.presentFragment(activity);
        } else if (position == mProfileActivity.administratorsRow) {
            Bundle args = new Bundle();
            args.putLong("chat_id", mProfileActivity.chatId);
            args.putInt("type", ChatUsersActivity.TYPE_ADMIN);
            ChatUsersActivity fragment = new ChatUsersActivity(args);
            fragment.setInfo(mProfileActivity.chatInfo);
            mProfileActivity.presentFragment(fragment);
        } else if (position == mProfileActivity.settingsRow) {
            mProfileActivity.editItem.performClick();
        } else if (position == mProfileActivity.botStarsBalanceRow) {
            mProfileActivity.presentFragment(new BotStarsActivity(BotStarsActivity.TYPE_STARS, mProfileActivity.userId));
        } else if (position == mProfileActivity.botTonBalanceRow) {
            mProfileActivity.presentFragment(new BotStarsActivity(BotStarsActivity.TYPE_TON, mProfileActivity.userId));
        } else if (position == mProfileActivity.channelBalanceRow) {
            Bundle args = new Bundle();
            args.putLong("chat_id", mProfileActivity.chatId);
            args.putBoolean("start_from_monetization", true);
            mProfileActivity.presentFragment(new StatisticActivity(args));
        } else if (position == mProfileActivity.blockedUsersRow) {
            Bundle args = new Bundle();
            args.putLong("chat_id", mProfileActivity.chatId);
            args.putInt("type", ChatUsersActivity.TYPE_BANNED);
            ChatUsersActivity fragment = new ChatUsersActivity(args);
            fragment.setInfo(mProfileActivity.chatInfo);
            mProfileActivity.presentFragment(fragment);
        } else if (position == mProfileActivity.notificationRow) {
            mProfileActivity.presentFragment(new NotificationsSettingsActivity());
        } else if (position == mProfileActivity.privacyRow) {
            mProfileActivity.presentFragment(new PrivacySettingsActivity().setCurrentPassword(mProfileActivity.currentPassword));
        } else if (position == mProfileActivity.dataRow) {
            mProfileActivity.presentFragment(new DataSettingsActivity());
        } else if (position == mProfileActivity.chatRow) {
            mProfileActivity.presentFragment(new ThemeActivity(ThemeActivity.THEME_TYPE_BASIC));
        } else if (position == mProfileActivity.filtersRow) {
            mProfileActivity.presentFragment(new FiltersSetupActivity());
        } else if (position == mProfileActivity.stickersRow) {
            mProfileActivity.presentFragment(new StickersActivity(MediaDataController.TYPE_IMAGE, null));
        } else if (position == mProfileActivity.liteModeRow) {
            mProfileActivity.presentFragment(new LiteModeSettingsActivity());
        } else if (position == mProfileActivity.devicesRow) {
            mProfileActivity.presentFragment(new SessionsActivity(0));
        } else if (position == mProfileActivity.questionRow) {
            mProfileActivity.showDialog(AlertsCreator.createSupportAlert(mProfileActivity, mProfileActivity.resourcesProvider));
        } else if (position == mProfileActivity.faqRow) {
            Browser.openUrl(mProfileActivity.getParentActivity(), LocaleController.getString(R.string.TelegramFaqUrl));
        } else if (position == mProfileActivity.policyRow) {
            Browser.openUrl(mProfileActivity.getParentActivity(), LocaleController.getString(R.string.PrivacyPolicyUrl));
        } else if (position == mProfileActivity.sendLogsRow) {
            mProfileActivity.sendLogs(mProfileActivity.getParentActivity(), false);
        } else if (position == mProfileActivity.sendLastLogsRow) {
            mProfileActivity.sendLogs(mProfileActivity.getParentActivity(), true);
        } else if (position == mProfileActivity.clearLogsRow) {
            FileLog.cleanupLogs();
        } else if (position == mProfileActivity.switchBackendRow) {
            if (mProfileActivity.getParentActivity() == null) {
                return;
            }
            AlertDialog.Builder builder1 = new AlertDialog.Builder(mProfileActivity.getParentActivity(), mProfileActivity.resourcesProvider);
            builder1.setMessage(LocaleController.getString(R.string.AreYouSure));
            builder1.setTitle(LocaleController.getString(R.string.AppName));
            builder1.setPositiveButton(LocaleController.getString(R.string.OK), (dialogInterface, i) -> {
                SharedConfig.pushAuthKey = null;
                SharedConfig.pushAuthKeyId = null;
                SharedConfig.saveConfig();
                mProfileActivity.getConnectionsManager().switchBackend(true);
            });
            builder1.setNegativeButton(LocaleController.getString(R.string.Cancel), null);
            mProfileActivity.showDialog(builder1.create());
        } else if (position == mProfileActivity.languageRow) {
            mProfileActivity.presentFragment(new LanguageSelectActivity());
        } else if (position == mProfileActivity.setUsernameRow) {
            mProfileActivity.presentFragment(new ChangeUsernameActivity());
        } else if (position == mProfileActivity.bioRow) {
            mProfileActivity.presentFragment(new UserInfoActivity());
        } else if (position == mProfileActivity.numberRow) {
            mProfileActivity.presentFragment(new ActionIntroActivity(ActionIntroActivity.ACTION_TYPE_CHANGE_PHONE_NUMBER));
        } else if (position == mProfileActivity.setAvatarRow) {
            mProfileActivity.onWriteButtonClick();
        } else if (position == mProfileActivity.premiumRow) {
            mProfileActivity.presentFragment(new PremiumPreviewFragment("settings"));
        } else if (position == mProfileActivity.starsRow) {
            mProfileActivity.presentFragment(new StarsIntroActivity());
        } else if (position == mProfileActivity.businessRow) {
            mProfileActivity.presentFragment(new PremiumPreviewFragment(PremiumPreviewFragment.FEATURES_BUSINESS, "settings"));
        } else if (position == mProfileActivity.premiumGiftingRow) {
            UserSelectorBottomSheet.open(0, BirthdayController.getInstance(mProfileActivity.getCurrentAccount()).getState());
        } else if (position == mProfileActivity.botPermissionLocation) {
            if (mProfileActivity.botLocation != null) {
                mProfileActivity.botLocation.setGranted(!mProfileActivity.botLocation.granted(), () -> {
                    ((TextCell) view).setChecked(mProfileActivity.botLocation.granted());
                });
            }
        } else if (position == mProfileActivity.botPermissionBiometry) {
            if (mProfileActivity.botBiometry != null) {
                mProfileActivity.botBiometry.setGranted(!mProfileActivity.botBiometry.granted());
                ((TextCell) view).setChecked(mProfileActivity.botBiometry.granted());
            }
        } else if (position == mProfileActivity.botPermissionEmojiStatus) {
            ((TextCell) view).setChecked(!((TextCell) view).isChecked());
            if (mProfileActivity.botPermissionEmojiStatusReqId > 0) {
                mProfileActivity.getConnectionsManager().cancelRequest(mProfileActivity.botPermissionEmojiStatusReqId, true);
            }
            TL_bots.toggleUserEmojiStatusPermission req = new TL_bots.toggleUserEmojiStatusPermission();
            req.bot = mProfileActivity.getMessagesController().getInputUser(mProfileActivity.userId);
            req.enabled = ((TextCell) view).isChecked();
            if (mProfileActivity.getUserInfo() != null) {
                mProfileActivity.getUserInfo().bot_can_manage_emoji_status = req.enabled;
            }
            final int[] reqId = new int[1];
            reqId[0] = mProfileActivity.botPermissionEmojiStatusReqId = mProfileActivity.getConnectionsManager().sendRequest(req, (res, err) -> AndroidUtilities.runOnUIThread(() -> {
                if (!(res instanceof TLRPC.TL_boolTrue)) {
                    BulletinFactory.of(mProfileActivity).showForError(err);
                }
                if (mProfileActivity.botPermissionEmojiStatusReqId == reqId[0]) {
                    mProfileActivity.botPermissionEmojiStatusReqId = 0;
                }
            }));
        } else if (position == mProfileActivity.bizHoursRow) {
            mProfileActivity.hoursExpanded = !mProfileActivity.hoursExpanded;
            mProfileActivity.saveScrollPosition();
            view.requestLayout();
            mProfileActivity.listAdapter.notifyItemChanged(mProfileActivity.bizHoursRow);
            if (mProfileActivity.savedScrollPosition >= 0) {
                mProfileActivity.layoutManager.scrollToPositionWithOffset(mProfileActivity.savedScrollPosition, mProfileActivity.savedScrollOffset - mProfileActivity.getListView().getPaddingTop());
            }
        } else if (position == mProfileActivity.bizLocationRow) {
            mProfileActivity.openLocation(false);
        } else if (position == mProfileActivity.channelRow) {
            if (mProfileActivity.getUserInfo() == null) return;
            Bundle args = new Bundle();
            args.putLong("chat_id", mProfileActivity.getUserInfo().personal_channel_id);
            mProfileActivity.presentFragment(new ChatActivity(args));
        } else if (position == mProfileActivity.birthdayRow) {
            if (mProfileActivity.birthdayEffect != null && mProfileActivity.birthdayEffect.start()) {
                return;
            }
            if (mProfileActivity.editRow(view, position)) {
                return;
            }
            TextDetailCell cell = (TextDetailCell) view;
            if (cell.hasImage()) {
                mProfileActivity.onTextDetailCellImageClicked(cell.getImageView());
            }
        } else {
            mProfileActivity.processOnClickOrPress(position, view, x, y);
        }
    }
}