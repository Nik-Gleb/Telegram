package org.telegram.ui.profile;

import static org.telegram.messenger.LocaleController.formatString;
import static org.telegram.messenger.LocaleController.getString;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.TextView;

import org.telegram.messenger.AndroidUtilities;
import org.telegram.messenger.BuildVars;
import org.telegram.messenger.ChatObject;
import org.telegram.messenger.ContactsController;
import org.telegram.messenger.DialogObject;
import org.telegram.messenger.FileLoader;
import org.telegram.messenger.FileLog;
import org.telegram.messenger.ImageLocation;
import org.telegram.messenger.LocaleController;
import org.telegram.messenger.MediaController;
import org.telegram.messenger.MediaDataController;
import org.telegram.messenger.MessagesController;
import org.telegram.messenger.NotificationCenter;
import org.telegram.messenger.R;
import org.telegram.messenger.SendMessagesHelper;
import org.telegram.messenger.UserConfig;
import org.telegram.messenger.UserObject;
import org.telegram.tgnet.TLRPC;
import org.telegram.ui.AccountFrozenAlert;
import org.telegram.ui.ActionBar.AlertDialog;
import org.telegram.ui.ActionBar.BaseFragment;
import org.telegram.ui.ActionBar.Theme;
import org.telegram.ui.BasePermissionsActivity;
import org.telegram.ui.ChangeUsernameActivity;
import org.telegram.ui.ChatActivity;
import org.telegram.ui.ChatEditActivity;
import org.telegram.ui.ChatRightsEditActivity;
import org.telegram.ui.ChatUsersActivity;
import org.telegram.ui.Components.AlertsCreator;
import org.telegram.ui.Components.Bulletin;
import org.telegram.ui.Components.BulletinFactory;
import org.telegram.ui.Components.CubicBezierInterpolator;
import org.telegram.ui.Components.MediaActivity;
import org.telegram.ui.Components.Premium.PremiumFeatureBottomSheet;
import org.telegram.ui.Components.UndoView;
import org.telegram.ui.Components.voip.VoIPHelper;
import org.telegram.ui.ContactAddActivity;
import org.telegram.ui.DialogsActivity;
import org.telegram.ui.Gifts.GiftSheet;
import org.telegram.ui.LaunchActivity;
import org.telegram.ui.LogoutActivity;
import org.telegram.ui.PeerColorActivity;
import org.telegram.ui.PhotoViewer;
import org.telegram.ui.PremiumPreviewFragment;
import org.telegram.ui.QrActivity;
import org.telegram.ui.ReportBottomSheet;
import org.telegram.ui.StatisticActivity;
import org.telegram.ui.TopicCreateFragment;
import org.telegram.ui.UserInfoActivity;
import org.telegram.ui.bots.BotWebViewAttachedSheet;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public final class ProfileActionHandler {
    public static void handleMenuAction(final ProfileActivity mProfileActivity, int id) {
        if (mProfileActivity.getParentActivity() == null) {
            return;
        }
        if (id == -1) {
            mProfileActivity.finishFragment();
        } else if (id == mProfileActivity.block_contact) {
            TLRPC.User user = mProfileActivity.getMessagesController().getUser(mProfileActivity.userId);
            if (user == null) {
                return;
            }
            if (!mProfileActivity.isBot || MessagesController.isSupportUser(user)) {
                if (mProfileActivity.userBlocked) {
                    mProfileActivity.getMessagesController().unblockPeer(mProfileActivity.userId);
                    if (BulletinFactory.canShowBulletin(mProfileActivity)) {
                        BulletinFactory.createBanBulletin(mProfileActivity, false).show();
                    }
                } else {
                    if (mProfileActivity.reportSpam) {
                        AlertsCreator.showBlockReportSpamAlert(mProfileActivity, mProfileActivity.userId, user, null, mProfileActivity.currentEncryptedChat, false, null, param -> {
                            if (param == 1) {
                                mProfileActivity.getNotificationCenter().removeObserver(mProfileActivity, NotificationCenter.closeChats);
                                mProfileActivity.getNotificationCenter().postNotificationName(NotificationCenter.closeChats);
                                mProfileActivity.playProfileAnimation = 0;
                                mProfileActivity.finishFragment();
                            } else {
                                mProfileActivity.getNotificationCenter().postNotificationName(NotificationCenter.peerSettingsDidLoad, mProfileActivity.userId);
                            }
                        }, mProfileActivity.resourcesProvider);
                    } else {
                        AlertDialog.Builder builder = new AlertDialog.Builder(mProfileActivity.getParentActivity(), mProfileActivity.resourcesProvider);
                        builder.setTitle(LocaleController.getString(R.string.BlockUser));
                        builder.setMessage(AndroidUtilities.replaceTags(formatString("AreYouSureBlockContact2", R.string.AreYouSureBlockContact2, ContactsController.formatName(user.first_name, user.last_name))));
                        builder.setPositiveButton(LocaleController.getString(R.string.BlockContact), (dialogInterface, i) -> {
                            mProfileActivity.getMessagesController().blockPeer(mProfileActivity.userId);
                            if (BulletinFactory.canShowBulletin(mProfileActivity)) {
                                BulletinFactory.createBanBulletin(mProfileActivity, true).show();
                            }
                        });
                        builder.setNegativeButton(LocaleController.getString(R.string.Cancel), null);
                        AlertDialog dialog = builder.create();
                        mProfileActivity.showDialog(dialog);
                        TextView button = (TextView) dialog.getButton(DialogInterface.BUTTON_POSITIVE);
                        if (button != null) {
                            button.setTextColor(mProfileActivity.getThemedColor(Theme.key_text_RedBold));
                        }
                    }
                }
            } else {
                if (!mProfileActivity.userBlocked) {
                    AlertsCreator.createClearOrDeleteDialogAlert(mProfileActivity, false, mProfileActivity.getCurrentChat(), user, mProfileActivity.currentEncryptedChat != null, true, true, (param) -> {
                        if (mProfileActivity.getParentLayout() != null) {
                            List<BaseFragment> fragmentStack = mProfileActivity.getParentLayout().getFragmentStack();
                            BaseFragment prevFragment = fragmentStack == null || fragmentStack.size() < 2 ? null : fragmentStack.get(fragmentStack.size() - 2);
                            if (prevFragment instanceof ChatActivity) {
                                mProfileActivity.getParentLayout().removeFragmentFromStack(fragmentStack.size() - 2);
                            }
                        }
                        mProfileActivity.finishFragment();
                        mProfileActivity.getNotificationCenter().postNotificationName(NotificationCenter.needDeleteDialog, mProfileActivity.getDialogId(), user, mProfileActivity.getCurrentChat(), param);
                    }, mProfileActivity.getResourceProvider());
                } else {
                    mProfileActivity.getMessagesController().unblockPeer(mProfileActivity.userId, ()-> mProfileActivity.getSendMessagesHelper().sendMessage(SendMessagesHelper.SendMessageParams.of("/start", mProfileActivity.userId, null, null, null, false, null, null, null, true, 0, null, false)));
                    mProfileActivity.finishFragment();
                }
            }
        } else if (id == mProfileActivity.add_contact) {
            TLRPC.User user = mProfileActivity.getMessagesController().getUser(mProfileActivity.userId);
            Bundle args = new Bundle();
            args.putLong("user_id", user.id);
            args.putBoolean("addContact", true);
            mProfileActivity.openAddToContact(user, args);
        } else if (id == mProfileActivity.share_contact) {
            Bundle args = new Bundle();
            args.putBoolean("onlySelect", true);
            args.putInt("dialogsType", DialogsActivity.DIALOGS_TYPE_FORWARD);
            args.putString("selectAlertString", LocaleController.getString(R.string.SendContactToText));
            args.putString("selectAlertStringGroup", LocaleController.getString(R.string.SendContactToGroupText));
            DialogsActivity fragment = new DialogsActivity(args);
            fragment.setDelegate(mProfileActivity);
            mProfileActivity.presentFragment(fragment);
        } else if (id == mProfileActivity.edit_contact) {
            Bundle args = new Bundle();
            args.putLong("user_id", mProfileActivity.userId);
            mProfileActivity.presentFragment(new ContactAddActivity(args, mProfileActivity.resourcesProvider));
        } else if (id == mProfileActivity.delete_contact) {
            final TLRPC.User user = mProfileActivity.getMessagesController().getUser(mProfileActivity.userId);
            if (user == null || mProfileActivity.getParentActivity() == null) {
                return;
            }
            AlertDialog.Builder builder = new AlertDialog.Builder(mProfileActivity.getParentActivity(), mProfileActivity.resourcesProvider);
            builder.setTitle(LocaleController.getString(R.string.DeleteContact));
            builder.setMessage(LocaleController.getString(R.string.AreYouSureDeleteContact));
            builder.setPositiveButton(LocaleController.getString(R.string.Delete), (dialogInterface, i) -> {
                ArrayList<TLRPC.User> arrayList = new ArrayList<>();
                arrayList.add(user);
                mProfileActivity.getContactsController().deleteContact(arrayList, true);
                if (user != null) {
                    user.contact = false;
                    mProfileActivity.updateListAnimated(false);
                }
            });
            builder.setNegativeButton(LocaleController.getString(R.string.Cancel), null);
            AlertDialog dialog = builder.create();
            mProfileActivity.showDialog(dialog);
            TextView button = (TextView) dialog.getButton(DialogInterface.BUTTON_POSITIVE);
            if (button != null) {
                button.setTextColor(mProfileActivity.getThemedColor(Theme.key_text_RedBold));
            }
        } else if (id == mProfileActivity.leave_group) {
            mProfileActivity.leaveChatPressed();
        } else if (id == mProfileActivity.delete_topic) {
            AlertDialog.Builder builder = new AlertDialog.Builder(mProfileActivity.getContext());
            builder.setTitle(LocaleController.getPluralString("DeleteTopics", 1));
            TLRPC.TL_forumTopic topic = MessagesController.getInstance(mProfileActivity.getCurrentAccount()).getTopicsController().findTopic(mProfileActivity.chatId, mProfileActivity.getTopicId());
            builder.setMessage(formatString("DeleteSelectedTopic", R.string.DeleteSelectedTopic, topic == null ? "topic" : topic.title));
            builder.setPositiveButton(LocaleController.getString(R.string.Delete), (dialog, which) -> {
                ArrayList<Integer> topicIds = new ArrayList<>();
                topicIds.add((int) mProfileActivity.getTopicId());
                mProfileActivity.getMessagesController().getTopicsController().deleteTopics(mProfileActivity.chatId, topicIds);
                mProfileActivity.playProfileAnimation = 0;
                if (mProfileActivity.getParentLayout() != null && mProfileActivity.getParentLayout().getFragmentStack() != null) {
                    for (int i = 0; i < mProfileActivity.getParentLayout().getFragmentStack().size(); ++i) {
                        BaseFragment fragment = mProfileActivity.getParentLayout().getFragmentStack().get(i);
                        if (fragment instanceof ChatActivity && ((ChatActivity) fragment).getTopicId() == mProfileActivity.getTopicId()) {
                            fragment.removeSelfFromStack();
                        }
                    }
                }
                mProfileActivity.finishFragment();

                Context context = mProfileActivity.getContext();
                if (context != null) {
                    BulletinFactory.of(Bulletin.BulletinWindow.make(context), mProfileActivity.resourcesProvider).createSimpleBulletin(R.raw.ic_delete, LocaleController.getPluralString("TopicsDeleted", 1)).show();
                }
                dialog.dismiss();
            });
            builder.setNegativeButton(LocaleController.getString(R.string.Cancel), (dialog, which) -> dialog.dismiss());
            AlertDialog alertDialog = builder.create();
            alertDialog.show();
            TextView button = (TextView) alertDialog.getButton(DialogInterface.BUTTON_POSITIVE);
            if (button != null) {
                button.setTextColor(Theme.getColor(Theme.key_text_RedBold));
            }
        } else if (id == mProfileActivity.report) {
            ReportBottomSheet.openChat(mProfileActivity, mProfileActivity.getDialogId());
        } else if (id == mProfileActivity.edit_channel) {
            if (mProfileActivity.isTopic) {
                Bundle args = new Bundle();
                args.putLong("chat_id", mProfileActivity.chatId);
                TopicCreateFragment fragment = TopicCreateFragment.create(mProfileActivity.chatId, mProfileActivity.getTopicId());
                mProfileActivity.presentFragment(fragment);
            } else {
                Bundle args = new Bundle();
                if (mProfileActivity.chatId != 0) {
                    args.putLong("chat_id", mProfileActivity.chatId);
                } else if (mProfileActivity.isBot) {
                    args.putLong("user_id", mProfileActivity.userId);
                }
                ChatEditActivity fragment = new ChatEditActivity(args);
                if (mProfileActivity.chatInfo != null) {
                    fragment.setInfo(mProfileActivity.chatInfo);
                } else {
                    fragment.setInfo(mProfileActivity.getUserInfo());
                }
                mProfileActivity.presentFragment(fragment);
            }
        } else if (id == mProfileActivity.edit_profile) {
            mProfileActivity.presentFragment(new UserInfoActivity());
        } else if (id == mProfileActivity.invite_to_group) {
            final TLRPC.User user = mProfileActivity.getMessagesController().getUser(mProfileActivity.userId);
            if (user == null) {
                return;
            }
            Bundle args = new Bundle();
            args.putBoolean("onlySelect", true);
            args.putInt("dialogsType", DialogsActivity.DIALOGS_TYPE_ADD_USERS_TO);
            args.putBoolean("resetDelegate", false);
            args.putBoolean("closeFragment", false);
//                    args.putString("addToGroupAlertString", LocaleController.formatString("AddToTheGroupAlertText", R.string.AddToTheGroupAlertText, UserObject.getUserName(user), "%1$s"));
            DialogsActivity fragment = new DialogsActivity(args);
            fragment.setDelegate((fragment1, dids, message, param, notify, scheduleDate, topicsFragment) -> {
                long did = dids.get(0).dialogId;

                TLRPC.Chat chat = MessagesController.getInstance(mProfileActivity.getCurrentAccount()).getChat(-did);
                if (chat != null && (chat.creator || chat.admin_rights != null && chat.admin_rights.add_admins)) {
                    mProfileActivity.getMessagesController().checkIsInChat(false, chat, user, (isInChatAlready, rightsAdmin, currentRank) -> AndroidUtilities.runOnUIThread(() -> {
                        ChatRightsEditActivity editRightsActivity = new ChatRightsEditActivity(mProfileActivity.userId, -did, rightsAdmin, null, null, currentRank, ChatRightsEditActivity.TYPE_ADD_BOT, true, !isInChatAlready, null);
                        editRightsActivity.setDelegate(new ChatRightsEditActivity.ChatRightsEditActivityDelegate() {
                            @Override
                            public void didSetRights(int rights, TLRPC.TL_chatAdminRights rightsAdmin, TLRPC.TL_chatBannedRights rightsBanned, String rank) {
                                mProfileActivity.disableProfileAnimation = true;
                                fragment.removeSelfFromStack();
                                mProfileActivity.getNotificationCenter().removeObserver(mProfileActivity, NotificationCenter.closeChats);
                                mProfileActivity.getNotificationCenter().postNotificationName(NotificationCenter.closeChats);
                            }

                            @Override
                            public void didChangeOwner(TLRPC.User user) {
                            }
                        });
                        mProfileActivity.presentFragment(editRightsActivity);
                    }));
                } else {
                    AlertDialog.Builder builder = new AlertDialog.Builder(mProfileActivity.getParentActivity(), mProfileActivity.resourcesProvider);
                    builder.setTitle(LocaleController.getString(R.string.AddBot));
                    String chatName = chat == null ? "" : chat.title;
                    builder.setMessage(AndroidUtilities.replaceTags(formatString("AddMembersAlertNamesText", R.string.AddMembersAlertNamesText, UserObject.getUserName(user), chatName)));
                    builder.setNegativeButton(LocaleController.getString(R.string.Cancel), null);
                    builder.setPositiveButton(LocaleController.getString(R.string.AddBot), (di, i) -> {
                        mProfileActivity.disableProfileAnimation = true;

                        Bundle args1 = new Bundle();
                        args1.putBoolean("scrollToTopOnResume", true);
                        args1.putLong("chat_id", -did);
                        if (!mProfileActivity.getMessagesController().checkCanOpenChat(args1, fragment1)) {
                            return;
                        }
                        ChatActivity chatActivity = new ChatActivity(args1);
                        mProfileActivity.getNotificationCenter().removeObserver(mProfileActivity, NotificationCenter.closeChats);
                        mProfileActivity.getNotificationCenter().postNotificationName(NotificationCenter.closeChats);
                        mProfileActivity.getMessagesController().addUserToChat(-did, user, 0, null, chatActivity, true, null, null);
                        mProfileActivity.presentFragment(chatActivity, true);
                    });
                    mProfileActivity.showDialog(builder.create());
                }
                return true;
            });
            mProfileActivity.presentFragment(fragment);
        } else if (id == mProfileActivity.share) {
            try {
                String text = null;
                if (mProfileActivity.userId != 0) {
                    TLRPC.User user = mProfileActivity.getMessagesController().getUser(mProfileActivity.userId);
                    if (user == null) {
                        return;
                    }
                    if (mProfileActivity.botInfo != null && mProfileActivity.getUserInfo() != null && !TextUtils.isEmpty(mProfileActivity.getUserInfo().about)) {
                        text = String.format("%s https://" + mProfileActivity.getMessagesController().linkPrefix + "/%s", mProfileActivity.getUserInfo().about, UserObject.getPublicUsername(user));
                    } else {
                        text = String.format("https://" + mProfileActivity.getMessagesController().linkPrefix + "/%s", UserObject.getPublicUsername(user));
                    }
                } else if (mProfileActivity.chatId != 0) {
                    TLRPC.Chat chat = mProfileActivity.getMessagesController().getChat(mProfileActivity.chatId);
                    if (chat == null) {
                        return;
                    }
                    if (mProfileActivity.chatInfo != null && !TextUtils.isEmpty(mProfileActivity.chatInfo.about)) {
                        text = String.format("%s\nhttps://" + mProfileActivity.getMessagesController().linkPrefix + "/%s", mProfileActivity.chatInfo.about, ChatObject.getPublicUsername(chat));
                    } else {
                        text = String.format("https://" + mProfileActivity.getMessagesController().linkPrefix + "/%s", ChatObject.getPublicUsername(chat));
                    }
                }
                if (TextUtils.isEmpty(text)) {
                    return;
                }
                Intent intent = new Intent(Intent.ACTION_SEND);
                intent.setType("text/plain");
                intent.putExtra(Intent.EXTRA_TEXT, text);
                mProfileActivity.startActivityForResult(Intent.createChooser(intent, LocaleController.getString(R.string.BotShare)), 500);
            } catch (Exception e) {
                FileLog.e(e);
            }
        } else if (id == mProfileActivity.add_shortcut) {
            try {
                long did;
                if (mProfileActivity.currentEncryptedChat != null) {
                    did = DialogObject.makeEncryptedDialogId(mProfileActivity.currentEncryptedChat.id);
                } else if (mProfileActivity.userId != 0) {
                    did = mProfileActivity.userId;
                } else if (mProfileActivity.chatId != 0) {
                    did = -mProfileActivity.chatId;
                } else {
                    return;
                }
                mProfileActivity.getMediaDataController().installShortcut(did, MediaDataController.SHORTCUT_TYPE_USER_OR_CHAT);
            } catch (Exception e) {
                FileLog.e(e);
            }
        } else if (id == mProfileActivity.call_item || id == mProfileActivity.video_call_item) {
            if (mProfileActivity.userId != 0) {
                TLRPC.User user = mProfileActivity.getMessagesController().getUser(mProfileActivity.userId);
                if (user != null) {
                    VoIPHelper.startCall(user, id == mProfileActivity.video_call_item, mProfileActivity.getUserInfo() != null && mProfileActivity.getUserInfo().video_calls_available, mProfileActivity.getParentActivity(), mProfileActivity.getUserInfo(), mProfileActivity.getAccountInstance());
                }
            } else if (mProfileActivity.chatId != 0) {
                ChatObject.Call call = mProfileActivity.getMessagesController().getGroupCall(mProfileActivity.chatId, false);
                if (call == null) {
                    VoIPHelper.showGroupCallAlert(mProfileActivity, mProfileActivity.getCurrentChat(), null, false, mProfileActivity.getAccountInstance());
                } else {
                    VoIPHelper.startCall(mProfileActivity.getCurrentChat(), null, null, false, mProfileActivity.getParentActivity(), mProfileActivity, mProfileActivity.getAccountInstance());
                }
            }
        } else if (id == mProfileActivity.search_members) {
            Bundle args = new Bundle();
            args.putLong("chat_id", mProfileActivity.chatId);
            args.putInt("type", ChatUsersActivity.TYPE_USERS);
            args.putBoolean("open_search", true);
            ChatUsersActivity fragment = new ChatUsersActivity(args);
            fragment.setInfo(mProfileActivity.chatInfo);
            mProfileActivity.presentFragment(fragment);
        } else if (id == mProfileActivity.add_member) {
            mProfileActivity.openAddMember();
        } else if (id == mProfileActivity.statistics) {
            TLRPC.Chat chat = mProfileActivity.getMessagesController().getChat(mProfileActivity.chatId);
            mProfileActivity.presentFragment(StatisticActivity.create(chat, false));
        } else if (id == mProfileActivity.view_discussion) {
            mProfileActivity.openDiscussion();
        } else if (id == mProfileActivity.gift_premium) {
            if (mProfileActivity.getUserInfo() != null && UserObject.areGiftsDisabled(mProfileActivity.getUserInfo())) {
                BaseFragment lastFragment = LaunchActivity.getSafeLastFragment();
                if (lastFragment != null) {
                    BulletinFactory.of(lastFragment).createSimpleBulletin(R.raw.error, AndroidUtilities.replaceTags(LocaleController.formatString(R.string.UserDisallowedGifts, DialogObject.getShortName(mProfileActivity.getDialogId())))).show();
                }
                return;
            }
            if (mProfileActivity.getCurrentChat() != null) {
                MessagesController.getGlobalMainSettings().edit().putInt("channelgifthint", 3).apply();
            }
            mProfileActivity.showDialog(new GiftSheet(mProfileActivity.getContext(), mProfileActivity.getCurrentAccount(), mProfileActivity.getDialogId(), null, null));
        } else if (id == mProfileActivity.channel_stories) {
            Bundle args = new Bundle();
            args.putInt("type", MediaActivity.TYPE_ARCHIVED_CHANNEL_STORIES);
            args.putLong("dialog_id", -mProfileActivity.chatId);
            MediaActivity fragment = new MediaActivity(args, null);
            fragment.setChatInfo(mProfileActivity.chatInfo);
            mProfileActivity.presentFragment(fragment);
        } else if (id == mProfileActivity.start_secret_chat) {
            AlertDialog.Builder builder = new AlertDialog.Builder(mProfileActivity.getParentActivity(), mProfileActivity.resourcesProvider);
            builder.setTitle(LocaleController.getString(R.string.AreYouSureSecretChatTitle));
            builder.setMessage(LocaleController.getString(R.string.AreYouSureSecretChat));
            builder.setPositiveButton(LocaleController.getString(R.string.Start), (dialogInterface, i) -> {
                if (MessagesController.getInstance(mProfileActivity.getCurrentAccount()).isFrozen()) {
                    AccountFrozenAlert.show(mProfileActivity.getCurrentAccount());
                    return;
                }
                mProfileActivity.creatingChat = true;
                mProfileActivity.getSecretChatHelper().startSecretChat(mProfileActivity.getParentActivity(), mProfileActivity.getMessagesController().getUser(mProfileActivity.userId));
            });
            builder.setNegativeButton(LocaleController.getString(R.string.Cancel), null);
            mProfileActivity.showDialog(builder.create());
        } else if (id == mProfileActivity.bot_privacy) {
            BotWebViewAttachedSheet.openPrivacy(mProfileActivity.getCurrentAccount(), mProfileActivity.userId);
        } else if (id == mProfileActivity.gallery_menu_save) {
            if (mProfileActivity.getParentActivity() == null) {
                return;
            }
            if (Build.VERSION.SDK_INT >= 23 && (Build.VERSION.SDK_INT <= 28 || BuildVars.NO_SCOPED_STORAGE) && mProfileActivity.getParentActivity().checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                mProfileActivity.getParentActivity().requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, BasePermissionsActivity.REQUEST_CODE_EXTERNAL_STORAGE);
                return;
            }
            ImageLocation location = mProfileActivity.avatarsViewPager.getImageLocation(mProfileActivity.avatarsViewPager.getRealPosition());
            if (location == null) {
                return;
            }
            final boolean isVideo = location.imageType == FileLoader.IMAGE_TYPE_ANIMATION;
            File f = FileLoader.getInstance(mProfileActivity.getCurrentAccount()).getPathToAttach(location.location, isVideo ? "mp4" : null, true);
            if (isVideo && !f.exists()) {
                f = new File(FileLoader.getDirectory(FileLoader.MEDIA_DIR_IMAGE), FileLoader.getAttachFileName(location.location, "mp4"));
            }
            if (f.exists()) {
                MediaController.saveFile(f.toString(), mProfileActivity.getParentActivity(), 0, null, null, uri -> {
                    if (mProfileActivity.getParentActivity() == null) {
                        return;
                    }
                    BulletinFactory.createSaveToGalleryBulletin(mProfileActivity, isVideo, null).show();
                });
            }
        } else if (id == mProfileActivity.edit_info) {
            mProfileActivity.presentFragment(new UserInfoActivity());
        } else if (id == mProfileActivity.edit_color) {
            if (!mProfileActivity.getUserConfig().isPremium()) {
                mProfileActivity.showDialog(new PremiumFeatureBottomSheet(mProfileActivity, PremiumPreviewFragment.PREMIUM_FEATURE_NAME_COLOR, true));
                return;
            }
            mProfileActivity.presentFragment(new PeerColorActivity(0).startOnProfile().setOnApplied(mProfileActivity));
        } else if (id == mProfileActivity.copy_link_profile) {
            TLRPC.User user = mProfileActivity.getMessagesController().getUser(mProfileActivity.userId);
            AndroidUtilities.addToClipboard(mProfileActivity.getMessagesController().linkPrefix + "/" + UserObject.getPublicUsername(user));
        } else if (id == mProfileActivity.set_username) {
            mProfileActivity.presentFragment(new ChangeUsernameActivity());
        } else if (id == mProfileActivity.logout) {
            mProfileActivity.presentFragment(new LogoutActivity());
        } else if (id == mProfileActivity.set_as_main) {
            int position = mProfileActivity.avatarsViewPager.getRealPosition();
            TLRPC.Photo photo = mProfileActivity.avatarsViewPager.getPhoto(position);
            if (photo == null) {
                return;
            }
            mProfileActivity.avatarsViewPager.startMovePhotoToBegin(position);

            TLRPC.TL_photos_updateProfilePhoto req = new TLRPC.TL_photos_updateProfilePhoto();
            req.id = new TLRPC.TL_inputPhoto();
            req.id.id = photo.id;
            req.id.access_hash = photo.access_hash;
            req.id.file_reference = photo.file_reference;
            UserConfig userConfig = mProfileActivity.getUserConfig();
            mProfileActivity.getConnectionsManager().sendRequest(req, (response, error) -> AndroidUtilities.runOnUIThread(() -> {
                mProfileActivity.avatarsViewPager.finishSettingMainPhoto();
                if (response instanceof TLRPC.TL_photos_photo) {
                    TLRPC.TL_photos_photo photos_photo = (TLRPC.TL_photos_photo) response;
                    mProfileActivity.getMessagesController().putUsers(photos_photo.users, false);
                    TLRPC.User user = mProfileActivity.getMessagesController().getUser(userConfig.clientUserId);
                    if (photos_photo.photo instanceof TLRPC.TL_photo) {
                        mProfileActivity.avatarsViewPager.replaceFirstPhoto(photo, photos_photo.photo);
                        if (user != null) {
                            user.photo.photo_id = photos_photo.photo.id;
                            userConfig.setCurrentUser(user);
                            userConfig.saveConfig(true);
                        }
                    }
                }
            }));
            mProfileActivity.getUndoView().showWithAction(mProfileActivity.userId, UndoView.ACTION_PROFILE_PHOTO_CHANGED, photo.video_sizes.isEmpty() ? null : 1);
            TLRPC.User user = mProfileActivity.getMessagesController().getUser(userConfig.clientUserId);

            TLRPC.PhotoSize bigSize = FileLoader.getClosestPhotoSizeWithSize(photo.sizes, 800);
            if (user != null) {
                TLRPC.PhotoSize smallSize = FileLoader.getClosestPhotoSizeWithSize(photo.sizes, 90);
                user.photo.photo_id = photo.id;
                user.photo.photo_small = smallSize.location;
                user.photo.photo_big = bigSize.location;
                userConfig.setCurrentUser(user);
                userConfig.saveConfig(true);
                NotificationCenter.getInstance(mProfileActivity.getCurrentAccount()).postNotificationName(NotificationCenter.mainUserInfoChanged);
                mProfileActivity.updateProfileData(true);
            }
            mProfileActivity.avatarsViewPager.commitMoveToBegin();
        } else if (id == mProfileActivity.edit_avatar) {
            if (MessagesController.getInstance(mProfileActivity.getCurrentAccount()).isFrozen()) {
                AccountFrozenAlert.show(mProfileActivity.getCurrentAccount());
                return;
            }
            int position = mProfileActivity.avatarsViewPager.getRealPosition();
            ImageLocation location = mProfileActivity.avatarsViewPager.getImageLocation(position);
            if (location == null) {
                return;
            }

            File f = FileLoader.getInstance(mProfileActivity.getCurrentAccount()).getPathToAttach(PhotoViewer.getFileLocation(location), PhotoViewer.getFileLocationExt(location), true);
            boolean isVideo = location.imageType == FileLoader.IMAGE_TYPE_ANIMATION;
            String thumb;
            if (isVideo) {
                ImageLocation imageLocation = mProfileActivity.avatarsViewPager.getRealImageLocation(position);
                thumb = FileLoader.getInstance(mProfileActivity.getCurrentAccount()).getPathToAttach(PhotoViewer.getFileLocation(imageLocation), PhotoViewer.getFileLocationExt(imageLocation), true).getAbsolutePath();
            } else {
                thumb = null;
            }
            mProfileActivity.imageUpdater.openPhotoForEdit(f.getAbsolutePath(), thumb, 0, isVideo);
        } else if (id == mProfileActivity.delete_avatar) {
            AlertDialog.Builder builder = new AlertDialog.Builder(mProfileActivity.getParentActivity(), mProfileActivity.resourcesProvider);
            ImageLocation location = mProfileActivity.avatarsViewPager.getImageLocation(mProfileActivity.avatarsViewPager.getRealPosition());
            if (location == null) {
                return;
            }
            if (location.imageType == FileLoader.IMAGE_TYPE_ANIMATION) {
                builder.setTitle(LocaleController.getString(R.string.AreYouSureDeleteVideoTitle));
                builder.setMessage(getString(R.string.AreYouSureDeleteVideo));
            } else {
                builder.setTitle(LocaleController.getString(R.string.AreYouSureDeletePhotoTitle));
                builder.setMessage(getString(R.string.AreYouSureDeletePhoto));
            }
            builder.setPositiveButton(LocaleController.getString(R.string.Delete), (dialogInterface, i) -> {
                int position = mProfileActivity.avatarsViewPager.getRealPosition();
                TLRPC.Photo photo = mProfileActivity.avatarsViewPager.getPhoto(position);
                TLRPC.UserFull userFull = mProfileActivity.getUserInfo();
                if (mProfileActivity.avatar != null && position == 0) {
                    mProfileActivity.imageUpdater.cancel();
                    if (mProfileActivity.avatarUploadingRequest != 0) {
                        mProfileActivity.getConnectionsManager().cancelRequest(mProfileActivity.avatarUploadingRequest, true);
                    }
                    mProfileActivity.allowPullingDown = !AndroidUtilities.isTablet() && !mProfileActivity.isInLandscapeMode && mProfileActivity.avatarImage.getImageReceiver().hasNotThumb() && !AndroidUtilities.isAccessibilityScreenReaderEnabled();
                    mProfileActivity.avatar = null;
                    mProfileActivity.avatarBig = null;
                    mProfileActivity.avatarsViewPager.scrolledByUser = true;
                    mProfileActivity.avatarsViewPager.removeUploadingImage(mProfileActivity.uploadingImageLocation);
                    mProfileActivity.avatarsViewPager.setCreateThumbFromParent(false);
                    mProfileActivity.updateProfileData(true);
                    mProfileActivity.showAvatarProgress(false, true);
                    mProfileActivity.getNotificationCenter().postNotificationName(NotificationCenter.updateInterfaces, MessagesController.UPDATE_MASK_ALL);
                    mProfileActivity.getNotificationCenter().postNotificationName(NotificationCenter.mainUserInfoChanged);
                    mProfileActivity.getUserConfig().saveConfig(true);
                    return;
                }
                if (mProfileActivity.hasFallbackPhoto && photo != null && userFull != null && userFull.fallback_photo != null && userFull.fallback_photo.id == photo.id) {
                    userFull.fallback_photo = null;
                    userFull.flags &= ~4194304;
                    mProfileActivity.getMessagesStorage().updateUserInfo(userFull, true);
                    mProfileActivity.updateProfileData(false);
                }
                if (mProfileActivity.avatarsViewPager.getRealCount() == 1) {
                    mProfileActivity.setForegroundImage(true);
                }
                if (photo == null || mProfileActivity.avatarsViewPager.getRealPosition() == 0) {
                    TLRPC.Photo nextPhoto = mProfileActivity.avatarsViewPager.getPhoto(1);
                    if (nextPhoto != null) {
                        mProfileActivity.getUserConfig().getCurrentUser().photo =new TLRPC.TL_userProfilePhoto();
                        TLRPC.PhotoSize smallSize = FileLoader.getClosestPhotoSizeWithSize(nextPhoto.sizes, 90);
                        TLRPC.PhotoSize bigSize = FileLoader.getClosestPhotoSizeWithSize(nextPhoto.sizes, 1000);
                        if (smallSize != null && bigSize != null) {
                            mProfileActivity.getUserConfig().getCurrentUser().photo.photo_small = smallSize.location;
                            mProfileActivity.getUserConfig().getCurrentUser().photo.photo_big = bigSize.location;
                        }
                    } else {
                        mProfileActivity.getUserConfig().getCurrentUser().photo = new TLRPC.TL_userProfilePhotoEmpty();
                    }
                    mProfileActivity.getMessagesController().deleteUserPhoto(null);
                } else {
                    TLRPC.TL_inputPhoto inputPhoto = new TLRPC.TL_inputPhoto();
                    inputPhoto.id = photo.id;
                    inputPhoto.access_hash = photo.access_hash;
                    inputPhoto.file_reference = photo.file_reference;
                    if (inputPhoto.file_reference == null) {
                        inputPhoto.file_reference = new byte[0];
                    }
                    mProfileActivity.getMessagesController().deleteUserPhoto(inputPhoto);
                    mProfileActivity.getMessagesStorage().clearUserPhoto(mProfileActivity.userId, photo.id);
                }
                if (mProfileActivity.avatarsViewPager.removePhotoAtIndex(position) || mProfileActivity.avatarsViewPager.getRealCount() <= 0) {
                    mProfileActivity.avatarsViewPager.setVisibility(View.GONE);
                    mProfileActivity.avatarImage.setForegroundAlpha(1f);
                    mProfileActivity.avatarContainer.setVisibility(View.VISIBLE);
                    mProfileActivity.doNotSetForeground = true;
                    final View view = mProfileActivity.layoutManager.findViewByPosition(0);
                    if (view != null) {
                        mProfileActivity.getListView().smoothScrollBy(0, view.getTop() - AndroidUtilities.dp(88), CubicBezierInterpolator.EASE_OUT_QUINT);
                    }
                }
            });
            builder.setNegativeButton(LocaleController.getString(R.string.Cancel), null);
            AlertDialog alertDialog = builder.create();
            mProfileActivity.showDialog(alertDialog);
            TextView button = (TextView) alertDialog.getButton(DialogInterface.BUTTON_POSITIVE);
            if (button != null) {
                button.setTextColor(mProfileActivity.getThemedColor(Theme.key_text_RedBold));
            }
        } else if (id == mProfileActivity.add_photo) {
            mProfileActivity.onWriteButtonClick();
        } else if (id == mProfileActivity.qr_button) {
            if (mProfileActivity.qrItem != null && mProfileActivity.qrItem.getAlpha() > 0) {
                Bundle args = new Bundle();
                args.putLong("chat_id", mProfileActivity.chatId);
                args.putLong("user_id", mProfileActivity.userId);
                mProfileActivity.presentFragment(new QrActivity(args));
            }
        }
    }
}
