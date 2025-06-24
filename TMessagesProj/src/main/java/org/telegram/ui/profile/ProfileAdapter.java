package org.telegram.ui.profile;

import static org.telegram.messenger.AndroidUtilities.dp;
import static org.telegram.messenger.LocaleController.formatString;
import static org.telegram.messenger.LocaleController.getString;
import static org.telegram.ui.Stars.StarsIntroActivity.formatStarsAmountShort;
import static org.telegram.ui.bots.AffiliateProgramFragment.percents;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.text.SpannableString;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.TextPaint;
import android.text.TextUtils;
import android.text.style.ClickableSpan;
import android.text.style.ForegroundColorSpan;
import android.text.style.URLSpan;
import android.text.util.Linkify;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import org.telegram.PhoneFormat.PhoneFormat;
import org.telegram.messenger.AndroidUtilities;
import org.telegram.messenger.ApplicationLoader;
import org.telegram.messenger.BirthdayController;
import org.telegram.messenger.ChatObject;
import org.telegram.messenger.DialogObject;
import org.telegram.messenger.Emoji;
import org.telegram.messenger.FileLog;
import org.telegram.messenger.LocaleController;
import org.telegram.messenger.MessagesController;
import org.telegram.messenger.NotificationCenter;
import org.telegram.messenger.NotificationsController;
import org.telegram.messenger.R;
import org.telegram.messenger.UserConfig;
import org.telegram.messenger.UserObject;
import org.telegram.messenger.browser.Browser;
import org.telegram.tgnet.TLObject;
import org.telegram.tgnet.TLRPC;
import org.telegram.tgnet.tl.TL_bots;
import org.telegram.tgnet.tl.TL_fragment;
import org.telegram.tgnet.tl.TL_stars;
import org.telegram.ui.ActionBar.ActionBar;
import org.telegram.ui.ActionBar.Theme;
import org.telegram.ui.ActionIntroActivity;
import org.telegram.ui.Business.ProfileHoursCell;
import org.telegram.ui.Business.ProfileLocationCell;
import org.telegram.ui.Cells.AboutLinkCell;
import org.telegram.ui.Cells.DividerCell;
import org.telegram.ui.Cells.HeaderCell;
import org.telegram.ui.Cells.NotificationsCheckCell;
import org.telegram.ui.Cells.ProfileChannelCell;
import org.telegram.ui.Cells.SettingsSuggestionCell;
import org.telegram.ui.Cells.ShadowSectionCell;
import org.telegram.ui.Cells.TextCell;
import org.telegram.ui.Cells.TextCheckCell;
import org.telegram.ui.Cells.TextDetailCell;
import org.telegram.ui.Cells.TextInfoPrivacyCell;
import org.telegram.ui.Cells.UserCell;
import org.telegram.ui.ChannelMonetizationLayout;
import org.telegram.ui.Components.AnimatedEmojiDrawable;
import org.telegram.ui.Components.AnimatedEmojiSpan;
import org.telegram.ui.Components.BulletinFactory;
import org.telegram.ui.Components.IdenticonDrawable;
import org.telegram.ui.Components.LayoutHelper;
import org.telegram.ui.Components.Premium.PremiumGradient;
import org.telegram.ui.Components.Premium.ProfilePremiumCell;
import org.telegram.ui.Components.RecyclerListView;
import org.telegram.ui.Components.UndoView;
import org.telegram.ui.FragmentUsernameBottomSheet;
import org.telegram.ui.Stars.BotStarsController;
import org.telegram.ui.Stars.StarsController;
import org.telegram.ui.Stars.StarsIntroActivity;
import org.telegram.ui.Stories.recorder.ButtonWithCounterView;
import org.telegram.ui.TwoStepVerificationSetupActivity;
import org.telegram.ui.UserInfoActivity;
import org.telegram.ui.bots.AffiliateProgramFragment;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.time.LocalDate;
import java.time.Period;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;

public final class ProfileAdapter extends RecyclerListView.SelectionAdapter {
    public final static int VIEW_TYPE_HEADER = 1,
            VIEW_TYPE_TEXT_DETAIL = 2,
            VIEW_TYPE_ABOUT_LINK = 3,
            VIEW_TYPE_TEXT = 4,
            VIEW_TYPE_DIVIDER = 5,
            VIEW_TYPE_NOTIFICATIONS_CHECK = 6,
            VIEW_TYPE_SHADOW = 7,
            VIEW_TYPE_USER = 8,
            VIEW_TYPE_EMPTY = 11,
            VIEW_TYPE_BOTTOM_PADDING = 12,
            VIEW_TYPE_SHARED_MEDIA = 13,
            VIEW_TYPE_VERSION = 14,
            VIEW_TYPE_SUGGESTION = 15,
            VIEW_TYPE_ADDTOGROUP_INFO = 17,
            VIEW_TYPE_PREMIUM_TEXT_CELL = 18,
            VIEW_TYPE_TEXT_DETAIL_MULTILINE = 19,
            VIEW_TYPE_NOTIFICATIONS_CHECK_SIMPLE = 20,
            VIEW_TYPE_LOCATION = 21,
            VIEW_TYPE_HOURS = 22,
            VIEW_TYPE_CHANNEL = 23,
            VIEW_TYPE_STARS_TEXT_CELL = 24,
            VIEW_TYPE_BOT_APP = 25,
            VIEW_TYPE_SHADOW_TEXT = 26,
            VIEW_TYPE_COLORFUL_TEXT = 27;

    private final Context mContext;
    private final Theme.ResourcesProvider mResourcesProvider;
    private final ProfileActivity mProfileActivity;

    public ProfileAdapter(
            Context context,
            Theme.ResourcesProvider resourcesProvider,
            ProfileActivity profileActivity
    ) {
        mContext = context;
        mResourcesProvider = resourcesProvider;
        mProfileActivity = profileActivity;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view;
        switch (viewType) {
            case VIEW_TYPE_HEADER: {
                view = new HeaderCell(mContext, 23, mResourcesProvider);
                view.setBackgroundColor(mProfileActivity.getThemedColor(Theme.key_windowBackgroundWhite));
                break;
            }
            case VIEW_TYPE_TEXT_DETAIL_MULTILINE:
            case VIEW_TYPE_TEXT_DETAIL:
                final TextDetailCell textDetailCell = new TextDetailCell(mContext, mResourcesProvider, viewType == VIEW_TYPE_TEXT_DETAIL_MULTILINE) {
                    @Override
                    protected int processColor(int color) {
                        return mProfileActivity.dontApplyPeerColor(color, false);
                    }
                };
                textDetailCell.setContentDescriptionValueFirst(true);
                view = textDetailCell;
                view.setBackgroundColor(mProfileActivity.getThemedColor(Theme.key_windowBackgroundWhite));
                break;
            case VIEW_TYPE_ABOUT_LINK: {
                view = mProfileActivity.aboutLinkCell = new AboutLinkCell(mContext, mProfileActivity, mResourcesProvider) {
                    @Override
                    protected void didPressUrl(String url, Browser.Progress progress) {
                        mProfileActivity.openUrl(url, progress);
                    }

                    @Override
                    protected void didResizeEnd() {
                        mProfileActivity.layoutManager.mIgnoreTopPadding = false;
                    }

                    @Override
                    protected void didResizeStart() {
                        mProfileActivity.layoutManager.mIgnoreTopPadding = true;
                    }

                    @Override
                    protected int processColor(int color) {
                        return mProfileActivity.dontApplyPeerColor(color, false);
                    }
                };
                view.setBackgroundColor(mProfileActivity.getThemedColor(Theme.key_windowBackgroundWhite));
                break;
            }
            case VIEW_TYPE_TEXT: {
                view = new TextCell(mContext, mResourcesProvider) {
                    @Override
                    protected int processColor(int color) {
                        return mProfileActivity.dontApplyPeerColor(color, false);
                    }
                };
                view.setBackgroundColor(mProfileActivity.getThemedColor(Theme.key_windowBackgroundWhite));
                break;
            }
            case VIEW_TYPE_DIVIDER: {
                view = new DividerCell(mContext, mResourcesProvider);
                view.setBackgroundColor(mProfileActivity.getThemedColor(Theme.key_windowBackgroundWhite));
                view.setPadding(AndroidUtilities.dp(20), AndroidUtilities.dp(4), 0, 0);
                break;
            }
            case VIEW_TYPE_NOTIFICATIONS_CHECK: {
                view = new NotificationsCheckCell(mContext, 23, 70, false, mResourcesProvider) {
                    @Override
                    protected int processColor(int color) {
                        return mProfileActivity.dontApplyPeerColor(color, false);
                    }
                };
                view.setBackgroundColor(mProfileActivity.getThemedColor(Theme.key_windowBackgroundWhite));
                break;
            }
            case VIEW_TYPE_NOTIFICATIONS_CHECK_SIMPLE: {
                view = new TextCheckCell(mContext, mResourcesProvider);
                view.setBackgroundColor(mProfileActivity.getThemedColor(Theme.key_windowBackgroundWhite));
                break;
            }
            case VIEW_TYPE_SHADOW: {
                view = new ShadowSectionCell(mContext, mResourcesProvider);
                break;
            }
            case VIEW_TYPE_SHADOW_TEXT: {
                view = new TextInfoPrivacyCell(mContext, mResourcesProvider);
                break;
            }
            case VIEW_TYPE_COLORFUL_TEXT: {
                view = new AffiliateProgramFragment.ColorfulTextCell(mContext, mResourcesProvider);
                view.setBackgroundColor(mProfileActivity.getThemedColor(Theme.key_windowBackgroundWhite));
                break;
            }
            case VIEW_TYPE_USER: {
                view = new UserCell(mContext, mProfileActivity.addMemberRow == -1 ? 9 : 6, 0, true, mResourcesProvider);
                view.setBackgroundColor(mProfileActivity.getThemedColor(Theme.key_windowBackgroundWhite));
                break;
            }
            case VIEW_TYPE_EMPTY: {
                view = new View(mContext) {
                    @Override
                    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
                        super.onMeasure(MeasureSpec.makeMeasureSpec(MeasureSpec.getSize(widthMeasureSpec), MeasureSpec.EXACTLY), MeasureSpec.makeMeasureSpec(AndroidUtilities.dp(32), MeasureSpec.EXACTLY));
                    }
                };
                break;
            }
            case VIEW_TYPE_BOTTOM_PADDING: {
                view = new View(mContext) {

                    private int lastPaddingHeight = 0;
                    private int lastListViewHeight = 0;

                    @Override
                    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
                        if (lastListViewHeight != mProfileActivity.getListView().getMeasuredHeight()) {
                            lastPaddingHeight = 0;
                        }
                        lastListViewHeight = mProfileActivity.getListView().getMeasuredHeight();
                        int n = mProfileActivity.getListView().getChildCount();
                        if (n == mProfileActivity.listAdapter.getItemCount()) {
                            int totalHeight = 0;
                            for (int i = 0; i < n; i++) {
                                View view = mProfileActivity.getListView().getChildAt(i);
                                int p = mProfileActivity.getListView().getChildAdapterPosition(view);
                                if (p >= 0 && p != mProfileActivity.bottomPaddingRow) {
                                    totalHeight += mProfileActivity.getListView().getChildAt(i).getMeasuredHeight();
                                }
                            }
                            int paddingHeight = (mProfileActivity.fragmentView == null ? 0 : mProfileActivity.fragmentView.getMeasuredHeight()) - ActionBar.getCurrentActionBarHeight() - AndroidUtilities.statusBarHeight - totalHeight;
                            if (paddingHeight > AndroidUtilities.dp(88)) {
                                paddingHeight = 0;
                            }
                            if (paddingHeight <= 0) {
                                paddingHeight = 0;
                            }
                            setMeasuredDimension(mProfileActivity.getListView().getMeasuredWidth(), lastPaddingHeight = paddingHeight);
                        } else {
                            setMeasuredDimension(mProfileActivity.getListView().getMeasuredWidth(), lastPaddingHeight);
                        }
                    }
                };
                view.setBackground(new ColorDrawable(Color.TRANSPARENT));
                break;
            }
            case VIEW_TYPE_SHARED_MEDIA: {
                if (mProfileActivity.sharedMediaLayout.getParent() != null) {
                    ((ViewGroup) mProfileActivity.sharedMediaLayout.getParent()).removeView(mProfileActivity.sharedMediaLayout);
                }
                view = mProfileActivity.sharedMediaLayout;
                break;
            }
            case VIEW_TYPE_ADDTOGROUP_INFO: {
                view = new TextInfoPrivacyCell(mContext, mResourcesProvider);
                view.setBackgroundColor(mProfileActivity.getThemedColor(Theme.key_windowBackgroundWhite));
                break;
            }
            case VIEW_TYPE_LOCATION:
                view = new ProfileLocationCell(mContext, mResourcesProvider);
                view.setBackgroundColor(mProfileActivity.getThemedColor(Theme.key_windowBackgroundWhite));
                break;
            case VIEW_TYPE_HOURS:
                view = new ProfileHoursCell(mContext, mResourcesProvider) {
                    @Override
                    protected int processColor(int color) {
                        return mProfileActivity.dontApplyPeerColor(color, false);
                    }
                };
                view.setBackgroundColor(mProfileActivity.getThemedColor(Theme.key_windowBackgroundWhite));
                break;
            case VIEW_TYPE_VERSION:
            default: {
                TextInfoPrivacyCell cell = new TextInfoPrivacyCell(mContext, 10, mResourcesProvider);
                cell.getTextView().setGravity(Gravity.CENTER_HORIZONTAL);
                cell.getTextView().setTextColor(mProfileActivity.getThemedColor(Theme.key_windowBackgroundWhiteGrayText3));
                cell.getTextView().setMovementMethod(null);
                try {
                    PackageInfo pInfo = ApplicationLoader.applicationContext.getPackageManager().getPackageInfo(ApplicationLoader.applicationContext.getPackageName(), 0);
                    int code = pInfo.versionCode / 10;
                    String abi = "";
                    switch (pInfo.versionCode % 10) {
                        case 1:
                        case 2:
                            abi = "store bundled " + Build.CPU_ABI + " " + Build.CPU_ABI2;
                            break;
                        default:
                        case 9:
                            if (ApplicationLoader.isStandaloneBuild()) {
                                abi = "direct " + Build.CPU_ABI + " " + Build.CPU_ABI2;
                            } else {
                                abi = "universal " + Build.CPU_ABI + " " + Build.CPU_ABI2;
                            }
                            break;
                    }
                    cell.setText(formatString("TelegramVersion", R.string.TelegramVersion, String.format(Locale.US, "v%s (%d) %s", pInfo.versionName, code, abi)));
                } catch (Exception e) {
                    FileLog.e(e);
                }
                cell.getTextView().setPadding(0, AndroidUtilities.dp(14), 0, AndroidUtilities.dp(14));
                view = cell;
                view.setBackgroundDrawable(Theme.getThemedDrawable(mContext, R.drawable.greydivider_bottom, mProfileActivity.getThemedColor(Theme.key_windowBackgroundGrayShadow)));
                break;
            }
            case VIEW_TYPE_SUGGESTION: {
                view = new SettingsSuggestionCell(mContext, mResourcesProvider) {
                    @Override
                    protected void onYesClick(int type) {
                        AndroidUtilities.runOnUIThread(() -> {
                            mProfileActivity.getNotificationCenter().removeObserver(mProfileActivity, NotificationCenter.newSuggestionsAvailable);
                            if (type == SettingsSuggestionCell.TYPE_GRACE) {
                                mProfileActivity.getMessagesController().removeSuggestion(0, "PREMIUM_GRACE");
                                Browser.openUrl(getContext(), mProfileActivity.getMessagesController().premiumManageSubscriptionUrl);
                            } else {
                                mProfileActivity.getMessagesController().removeSuggestion(0, type == SettingsSuggestionCell.TYPE_PHONE ? "VALIDATE_PHONE_NUMBER" : "VALIDATE_PASSWORD");
                            }
                            mProfileActivity.getNotificationCenter().addObserver(mProfileActivity, NotificationCenter.newSuggestionsAvailable);
                            mProfileActivity.updateListAnimated(false);
                        });
                    }

                    @Override
                    protected void onNoClick(int type) {
                        if (type == SettingsSuggestionCell.TYPE_PHONE) {
                            mProfileActivity.presentFragment(new ActionIntroActivity(ActionIntroActivity.ACTION_TYPE_CHANGE_PHONE_NUMBER));
                        } else {
                            mProfileActivity.presentFragment(new TwoStepVerificationSetupActivity(TwoStepVerificationSetupActivity.TYPE_VERIFY, null));
                        }
                    }
                };
                break;
            }
            case VIEW_TYPE_PREMIUM_TEXT_CELL:
            case VIEW_TYPE_STARS_TEXT_CELL:
                view = new ProfilePremiumCell(mContext, viewType == VIEW_TYPE_PREMIUM_TEXT_CELL ? 0 : 1, mResourcesProvider);
                view.setBackgroundColor(mProfileActivity.getThemedColor(Theme.key_windowBackgroundWhite));
                break;
            case VIEW_TYPE_CHANNEL:
                view = new ProfileChannelCell(mProfileActivity) {
                    @Override
                    public int processColor(int color) {
                        return mProfileActivity.dontApplyPeerColor(color, false);
                    }
                };
                view.setBackgroundColor(mProfileActivity.getThemedColor(Theme.key_windowBackgroundWhite));
                break;
            case VIEW_TYPE_BOT_APP:
                FrameLayout frameLayout = new FrameLayout(mContext);
                ButtonWithCounterView button = new ButtonWithCounterView(mContext, mResourcesProvider);
                button.setText(LocaleController.getString(R.string.ProfileBotOpenApp), false);
                button.setOnClickListener(v -> {
                    TLRPC.User bot = mProfileActivity.getMessagesController().getUser(mProfileActivity.userId);
                    mProfileActivity.getMessagesController().openApp(mProfileActivity, bot, null, mProfileActivity.getClassGuid(), null);
                });
                frameLayout.addView(button, LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, 48, Gravity.FILL, 18, 14, 18, 14));
                view = frameLayout;
                view.setBackgroundColor(mProfileActivity.getThemedColor(Theme.key_windowBackgroundWhite));
                break;
        }
        if (viewType != VIEW_TYPE_SHARED_MEDIA) {
            view.setLayoutParams(new RecyclerView.LayoutParams(RecyclerView.LayoutParams.MATCH_PARENT, RecyclerView.LayoutParams.WRAP_CONTENT));
        }
        return new RecyclerListView.Holder(view);
    }

    @Override
    public void onViewAttachedToWindow(RecyclerView.ViewHolder holder) {
        if (holder.itemView == mProfileActivity.sharedMediaLayout) {
            mProfileActivity.sharedMediaLayoutAttached = true;
        }
        if (holder.itemView instanceof TextDetailCell) {
            ((TextDetailCell) holder.itemView).textView.setLoading(mProfileActivity.loadingSpan);
            ((TextDetailCell) holder.itemView).valueTextView.setLoading(mProfileActivity.loadingSpan);
        }
    }

    @Override
    public void onViewDetachedFromWindow(RecyclerView.ViewHolder holder) {
        if (holder.itemView == mProfileActivity.sharedMediaLayout) {
            mProfileActivity.sharedMediaLayoutAttached = false;
        }
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, @SuppressLint("RecyclerView") int position) {
        switch (holder.getItemViewType()) {
            case VIEW_TYPE_HEADER:
                HeaderCell headerCell = (HeaderCell) holder.itemView;
                if (position == mProfileActivity.infoHeaderRow) {
                    if (ChatObject.isChannel(mProfileActivity.getCurrentChat()) && !mProfileActivity.getCurrentChat().megagroup && mProfileActivity.channelInfoRow != -1) {
                        headerCell.setText(LocaleController.getString(R.string.ReportChatDescription));
                    } else {
                        headerCell.setText(LocaleController.getString(R.string.Info));
                    }
                } else if (position == mProfileActivity.membersHeaderRow) {
                    headerCell.setText(LocaleController.getString(R.string.ChannelMembers));
                } else if (position == mProfileActivity.settingsSectionRow2) {
                    headerCell.setText(LocaleController.getString(R.string.SETTINGS));
                } else if (position == mProfileActivity.numberSectionRow) {
                    headerCell.setText(LocaleController.getString(R.string.Account));
                } else if (position == mProfileActivity.helpHeaderRow) {
                    headerCell.setText(LocaleController.getString(R.string.SettingsHelp));
                } else if (position == mProfileActivity.debugHeaderRow) {
                    headerCell.setText(LocaleController.getString(R.string.SettingsDebug));
                } else if (position == mProfileActivity.botPermissionsHeader) {
                    headerCell.setText(LocaleController.getString(R.string.BotProfilePermissions));
                }
                headerCell.setTextColor(mProfileActivity.dontApplyPeerColor(mProfileActivity.getThemedColor(Theme.key_windowBackgroundWhiteBlueHeader), false));
                break;
            case VIEW_TYPE_TEXT_DETAIL_MULTILINE:
            case VIEW_TYPE_TEXT_DETAIL:
                TextDetailCell detailCell = (TextDetailCell) holder.itemView;
                boolean containsQr = false;
                boolean containsGift = false;
                if (position == mProfileActivity.birthdayRow) {
                    TLRPC.UserFull userFull = mProfileActivity.getMessagesController().getUserFull(mProfileActivity.userId);
                    if (userFull != null && userFull.birthday != null) {
                        final boolean today = BirthdayController.isToday(userFull);
                        final boolean withYear = (userFull.birthday.flags & 1) != 0;
                        final int age = withYear ? Period.between(LocalDate.of(userFull.birthday.year, userFull.birthday.month, userFull.birthday.day), LocalDate.now()).getYears() : -1;

                        String text = UserInfoActivity.birthdayString(userFull.birthday);

                        if (withYear) {
                            text = LocaleController.formatPluralString(today ? "ProfileBirthdayTodayValueYear" : "ProfileBirthdayValueYear", age, text);
                        } else {
                            text = LocaleController.formatString(today ? R.string.ProfileBirthdayTodayValue : R.string.ProfileBirthdayValue, text);
                        }

                        detailCell.setTextAndValue(
                                Emoji.replaceWithRestrictedEmoji(text, detailCell.textView, () -> {
                                    if (holder.getAdapterPosition() == position && mProfileActivity.birthdayRow == position && holder.getItemViewType() == VIEW_TYPE_TEXT_DETAIL) {
                                        onBindViewHolder(holder, position);
                                    }
                                }),
                                LocaleController.getString(today ? R.string.ProfileBirthdayToday : R.string.ProfileBirthday),
                                mProfileActivity.isTopic || mProfileActivity.bizHoursRow != -1 || mProfileActivity.bizLocationRow != -1
                        );

                        containsGift = !mProfileActivity.myProfile && today && !mProfileActivity.getMessagesController().premiumPurchaseBlocked();
                    }
                } else if (position == mProfileActivity.phoneRow) {
                    String text;
                    TLRPC.User user = mProfileActivity.getMessagesController().getUser(mProfileActivity.userId);
                    String phoneNumber;
                    if (user != null && !TextUtils.isEmpty(mProfileActivity.vcardPhone)) {
                        text = PhoneFormat.getInstance().format("+" + mProfileActivity.vcardPhone);
                        phoneNumber = mProfileActivity.vcardPhone;
                    } else if (user != null && !TextUtils.isEmpty(user.phone)) {
                        text = PhoneFormat.getInstance().format("+" + user.phone);
                        phoneNumber = user.phone;
                    } else {
                        text = LocaleController.getString(R.string.PhoneHidden);
                        phoneNumber = null;
                    }
                    mProfileActivity.isFragmentPhoneNumber = phoneNumber != null && phoneNumber.matches("888\\d{8}");
                    detailCell.setTextAndValue(text, LocaleController.getString(mProfileActivity.isFragmentPhoneNumber ? R.string.AnonymousNumber : R.string.PhoneMobile), false);
                } else if (position == mProfileActivity.usernameRow) {
                    String username = null;
                    CharSequence text;
                    CharSequence value;
                    ArrayList<TLRPC.TL_username> usernames = new ArrayList<>();
                    if (mProfileActivity.userId != 0) {
                        final TLRPC.User user = mProfileActivity.getMessagesController().getUser(mProfileActivity.userId);
                        if (user != null) {
                            usernames.addAll(user.usernames);
                        }
                        TLRPC.TL_username usernameObj = null;
                        if (user != null && !TextUtils.isEmpty(user.username)) {
                            usernameObj = DialogObject.findUsername(user.username, usernames);
                            username = user.username;
                        }
                        usernames = user == null ? new ArrayList<>() : new ArrayList<>(user.usernames);
                        if (TextUtils.isEmpty(username) && usernames != null) {
                            for (int i = 0; i < usernames.size(); ++i) {
                                TLRPC.TL_username u = usernames.get(i);
                                if (u != null && u.active && !TextUtils.isEmpty(u.username)) {
                                    usernameObj = u;
                                    username = u.username;
                                    break;
                                }
                            }
                        }
                        value = LocaleController.getString(R.string.Username);
                        if (username != null) {
                            text = "@" + username;
                            if (usernameObj != null && !usernameObj.editable) {
                                text = new SpannableString(text);
                                ((SpannableString) text).setSpan(makeUsernameLinkSpan(usernameObj), 0, text.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                            }
                        } else {
                            text = "—";
                        }
                        containsQr = true;
                    } else if (mProfileActivity.getCurrentChat() != null) {
                        TLRPC.Chat chat = mProfileActivity.getMessagesController().getChat(mProfileActivity.chatId);
                        username = ChatObject.getPublicUsername(chat);
                        if (chat != null) {
                            usernames.addAll(chat.usernames);
                        }
                        if (ChatObject.isPublic(chat)) {
                            containsQr = true;
                            text = mProfileActivity.getMessagesController().linkPrefix + "/" + username + (mProfileActivity.getTopicId() != 0 ? "/" + mProfileActivity.getTopicId() : "");
                            value = LocaleController.getString(R.string.InviteLink);
                        } else {
                            text = mProfileActivity.getMessagesController().linkPrefix + "/c/" + mProfileActivity.chatId + (mProfileActivity.getTopicId() != 0 ? "/" + mProfileActivity.getTopicId() : "");
                            value = LocaleController.getString(R.string.InviteLinkPrivate);
                        }
                    } else {
                        text = "";
                        value = "";
                        usernames = new ArrayList<>();
                    }
                    detailCell.setTextAndValue(text, alsoUsernamesString(username, usernames, value), (mProfileActivity.isTopic || mProfileActivity.bizHoursRow != -1 || mProfileActivity.bizLocationRow != -1) && mProfileActivity.birthdayRow < 0);
                } else if (position == mProfileActivity.locationRow) {
                    if (mProfileActivity.chatInfo != null && mProfileActivity.chatInfo.location instanceof TLRPC.TL_channelLocation) {
                        TLRPC.TL_channelLocation location = (TLRPC.TL_channelLocation) mProfileActivity.chatInfo.location;
                        detailCell.setTextAndValue(location.address, LocaleController.getString(R.string.AttachLocation), false);
                    }
                } else if (position == mProfileActivity.numberRow) {
                    TLRPC.User user = UserConfig.getInstance(mProfileActivity.getCurrentAccount()).getCurrentUser();
                    String value;
                    if (user != null && user.phone != null && !user.phone.isEmpty()) {
                        value = PhoneFormat.getInstance().format("+" + user.phone);
                    } else {
                        value = LocaleController.getString(R.string.NumberUnknown);
                    }
                    detailCell.setTextAndValue(value, LocaleController.getString(R.string.TapToChangePhone), true);
                    detailCell.setContentDescriptionValueFirst(false);
                } else if (position == mProfileActivity.setUsernameRow) {
                    TLRPC.User user = UserConfig.getInstance(mProfileActivity.getCurrentAccount()).getCurrentUser();
                    String text = "";
                    CharSequence value = LocaleController.getString(R.string.Username);
                    String username = null;
                    if (user != null && !user.usernames.isEmpty()) {
                        for (int i = 0; i < user.usernames.size(); ++i) {
                            TLRPC.TL_username u = user.usernames.get(i);
                            if (u != null && u.active && !TextUtils.isEmpty(u.username)) {
                                username = u.username;
                                break;
                            }
                        }
                        if (username == null) {
                            username = user.username;
                        }
                        if (username == null || TextUtils.isEmpty(username)) {
                            text = LocaleController.getString(R.string.UsernameEmpty);
                        } else {
                            text = "@" + username;
                        }
                        value = alsoUsernamesString(username, user.usernames, value);
                    } else {
                        username = UserObject.getPublicUsername(user);
                        if (user != null && !TextUtils.isEmpty(username)) {
                            text = "@" + username;
                        } else {
                            text = LocaleController.getString(R.string.UsernameEmpty);
                        }
                    }
                    detailCell.setTextAndValue(text, value, true);
                    detailCell.setContentDescriptionValueFirst(true);
                }
                if (containsGift) {
                    Drawable drawable = ContextCompat.getDrawable(detailCell.getContext(), R.drawable.msg_input_gift);
                    drawable.setColorFilter(new PorterDuffColorFilter(mProfileActivity.dontApplyPeerColor(mProfileActivity.getThemedColor(Theme.key_switch2TrackChecked), false), PorterDuff.Mode.MULTIPLY));
                    if (UserObject.areGiftsDisabled(mProfileActivity.getUserInfo())) {
                        detailCell.setImage(null);
                        detailCell.setImageClickListener(null);
                    } else {
                        detailCell.setImage(drawable, LocaleController.getString(R.string.GiftPremium));
                        detailCell.setImageClickListener(mProfileActivity::onTextDetailCellImageClicked);
                    }
                } else if (containsQr) {
                    Drawable drawable = ContextCompat.getDrawable(detailCell.getContext(), R.drawable.msg_qr_mini);
                    drawable.setColorFilter(new PorterDuffColorFilter(mProfileActivity.dontApplyPeerColor(mProfileActivity.getThemedColor(Theme.key_switch2TrackChecked), false), PorterDuff.Mode.MULTIPLY));
                    detailCell.setImage(drawable, LocaleController.getString(R.string.GetQRCode));
                    detailCell.setImageClickListener(mProfileActivity::onTextDetailCellImageClicked);
                } else {
                    detailCell.setImage(null);
                    detailCell.setImageClickListener(null);
                }
                detailCell.setTag(position);
                detailCell.textView.setLoading(mProfileActivity.loadingSpan);
                detailCell.valueTextView.setLoading(mProfileActivity.loadingSpan);
                break;
            case VIEW_TYPE_ABOUT_LINK:
                AboutLinkCell aboutLinkCell = (AboutLinkCell) holder.itemView;
                if (position == mProfileActivity.userInfoRow) {
                    TLRPC.User user = mProfileActivity.getUserInfo().user != null ? mProfileActivity.getUserInfo().user : mProfileActivity.getMessagesController().getUser(mProfileActivity.getUserInfo().id);
                    boolean addlinks = mProfileActivity.isBot || (user != null && user.premium && mProfileActivity.getUserInfo().about != null);
                    aboutLinkCell.setTextAndValue(mProfileActivity.getUserInfo().about, LocaleController.getString(R.string.UserBio), addlinks);
                } else if (position == mProfileActivity.channelInfoRow) {
                    String text = mProfileActivity.chatInfo.about;
                    while (text.contains("\n\n\n")) {
                        text = text.replace("\n\n\n", "\n\n");
                    }
                    aboutLinkCell.setText(text, ChatObject.isChannel(mProfileActivity.getCurrentChat()) && !mProfileActivity.getCurrentChat().megagroup);
                } else if (position == mProfileActivity.bioRow) {
                    String value;
                    if (mProfileActivity.getUserInfo() == null || !TextUtils.isEmpty(mProfileActivity.getUserInfo().about)) {
                        value = mProfileActivity.getUserInfo() == null ? LocaleController.getString(R.string.Loading) : mProfileActivity.getUserInfo().about;
                        aboutLinkCell.setTextAndValue(value, LocaleController.getString(R.string.UserBio), mProfileActivity.getUserConfig().isPremium());
                        mProfileActivity.currentBio = mProfileActivity.getUserInfo() != null ? mProfileActivity.getUserInfo().about : null;
                    } else {
                        aboutLinkCell.setTextAndValue(LocaleController.getString(R.string.UserBio), LocaleController.getString(R.string.UserBioDetail), false);
                        mProfileActivity.currentBio = null;
                    }
                    aboutLinkCell.setMoreButtonDisabled(true);
                }
                break;
            case VIEW_TYPE_PREMIUM_TEXT_CELL:
            case VIEW_TYPE_STARS_TEXT_CELL:
            case VIEW_TYPE_TEXT:
                TextCell textCell = (TextCell) holder.itemView;
                textCell.setColors(Theme.key_windowBackgroundWhiteGrayIcon, Theme.key_windowBackgroundWhiteBlackText);
                textCell.setTag(Theme.key_windowBackgroundWhiteBlackText);
                if (position == mProfileActivity.settingsTimerRow) {
                    TLRPC.EncryptedChat encryptedChat = mProfileActivity.getMessagesController().getEncryptedChat(DialogObject.getEncryptedChatId(mProfileActivity.getDialogId()));
                    String value;
                    if (encryptedChat.ttl == 0) {
                        value = LocaleController.getString(R.string.ShortMessageLifetimeForever);
                    } else {
                        value = LocaleController.formatTTLString(encryptedChat.ttl);
                    }
                    textCell.setTextAndValue(LocaleController.getString(R.string.MessageLifetime), value, false, false);
                } else if (position == mProfileActivity.unblockRow) {
                    textCell.setText(LocaleController.getString(R.string.Unblock), false);
                    textCell.setColors(-1, Theme.key_text_RedRegular);
                } else if (position == mProfileActivity.settingsKeyRow) {
                    IdenticonDrawable identiconDrawable = new IdenticonDrawable();
                    TLRPC.EncryptedChat encryptedChat = mProfileActivity.getMessagesController().getEncryptedChat(DialogObject.getEncryptedChatId(mProfileActivity.getDialogId()));
                    identiconDrawable.setEncryptedChat(encryptedChat);
                    textCell.setTextAndValueDrawable(LocaleController.getString(R.string.EncryptionKey), identiconDrawable, false);
                } else if (position == mProfileActivity.joinRow) {
                    textCell.setColors(-1, Theme.key_windowBackgroundWhiteBlueText2);
                    if (mProfileActivity.getCurrentChat().megagroup) {
                        textCell.setText(LocaleController.getString(R.string.ProfileJoinGroup), false);
                    } else {
                        textCell.setText(LocaleController.getString(R.string.ProfileJoinChannel), false);
                    }
                } else if (position == mProfileActivity.subscribersRow) {
                    if (mProfileActivity.chatInfo != null) {
                        if (ChatObject.isChannel(mProfileActivity.getCurrentChat()) && !mProfileActivity.getCurrentChat().megagroup) {
                            textCell.setTextAndValueAndIcon(LocaleController.getString(R.string.ChannelSubscribers), LocaleController.formatNumber(mProfileActivity.chatInfo.participants_count, ','), R.drawable.msg_groups, position != mProfileActivity.membersSectionRow - 1);
                        } else {
                            textCell.setTextAndValueAndIcon(LocaleController.getString(R.string.ChannelMembers), LocaleController.formatNumber(mProfileActivity.chatInfo.participants_count, ','), R.drawable.msg_groups, position != mProfileActivity.membersSectionRow - 1);
                        }
                    } else {
                        if (ChatObject.isChannel(mProfileActivity.getCurrentChat()) && !mProfileActivity.getCurrentChat().megagroup) {
                            textCell.setTextAndIcon(LocaleController.getString(R.string.ChannelSubscribers), R.drawable.msg_groups, position != mProfileActivity.membersSectionRow - 1);
                        } else {
                            textCell.setTextAndIcon(LocaleController.getString(R.string.ChannelMembers), R.drawable.msg_groups, position != mProfileActivity.membersSectionRow - 1);
                        }
                    }
                } else if (position == mProfileActivity.subscribersRequestsRow) {
                    if (mProfileActivity.chatInfo != null) {
                        textCell.setTextAndValueAndIcon(LocaleController.getString(R.string.SubscribeRequests), String.format("%d", mProfileActivity.chatInfo.requests_pending), R.drawable.msg_requests, position != mProfileActivity.membersSectionRow - 1);
                    }
                } else if (position == mProfileActivity.administratorsRow) {
                    if (mProfileActivity.chatInfo != null) {
                        textCell.setTextAndValueAndIcon(LocaleController.getString(R.string.ChannelAdministrators), String.format("%d", mProfileActivity.chatInfo.admins_count), R.drawable.msg_admins, position != mProfileActivity.membersSectionRow - 1);
                    } else {
                        textCell.setTextAndIcon(LocaleController.getString(R.string.ChannelAdministrators), R.drawable.msg_admins, position != mProfileActivity.membersSectionRow - 1);
                    }
                } else if (position == mProfileActivity.settingsRow) {
                    textCell.setTextAndIcon(LocaleController.getString(R.string.ChannelAdminSettings), R.drawable.msg_customize, position != mProfileActivity.membersSectionRow - 1);
                } else if (position == mProfileActivity.channelBalanceRow) {
                    final TL_stars.StarsAmount stars_balance = BotStarsController.getInstance(mProfileActivity.getCurrentAccount()).getBotStarsBalance(-mProfileActivity.chatId);
                    final long ton_balance = BotStarsController.getInstance(mProfileActivity.getCurrentAccount()).getTONBalance(-mProfileActivity.chatId);
                    SpannableStringBuilder ssb = new SpannableStringBuilder();
                    if (ton_balance > 0) {
                        if (ton_balance / 1_000_000_000.0 > 1000.0) {
                            ssb.append("TON ").append(AndroidUtilities.formatWholeNumber((int) (ton_balance / 1_000_000_000.0), 0));
                        } else {
                            DecimalFormatSymbols symbols = new DecimalFormatSymbols(Locale.US);
                            symbols.setDecimalSeparator('.');
                            DecimalFormat formatterTON = new DecimalFormat("#.##", symbols);
                            formatterTON.setMinimumFractionDigits(2);
                            formatterTON.setMaximumFractionDigits(3);
                            formatterTON.setGroupingUsed(false);
                            ssb.append("TON ").append(formatterTON.format(ton_balance / 1_000_000_000.0));
                        }
                    }
                    if (stars_balance.amount > 0) {
                        if (ssb.length() > 0) ssb.append(" ");
                        ssb.append("XTR ").append(formatStarsAmountShort(stars_balance));
                    }
                    textCell.setTextAndValueAndIcon(getString(R.string.ChannelStars), ChannelMonetizationLayout.replaceTON(StarsIntroActivity.replaceStarsWithPlain(ssb, .7f), textCell.getTextView().getPaint()), R.drawable.menu_feature_paid, true);
                } else if (position == mProfileActivity.botStarsBalanceRow) {
                    final TL_stars.StarsAmount stars_balance = BotStarsController.getInstance(mProfileActivity.getCurrentAccount()).getBotStarsBalance(mProfileActivity.userId);
                    SpannableStringBuilder ssb = new SpannableStringBuilder();
                    if (stars_balance.amount > 0) {
                        ssb.append("XTR ").append(formatStarsAmountShort(stars_balance));
                    }
                    textCell.setTextAndValueAndIcon(getString(R.string.BotBalanceStars), ChannelMonetizationLayout.replaceTON(StarsIntroActivity.replaceStarsWithPlain(ssb, .7f), textCell.getTextView().getPaint()), R.drawable.menu_premium_main, true);
                } else if (position == mProfileActivity.botTonBalanceRow) {
                    long ton_balance = BotStarsController.getInstance(mProfileActivity.getCurrentAccount()).getTONBalance(mProfileActivity.userId);
                    SpannableStringBuilder ssb = new SpannableStringBuilder();
                    if (ton_balance > 0) {
                        if (ton_balance / 1_000_000_000.0 > 1000.0) {
                            ssb.append("TON ").append(AndroidUtilities.formatWholeNumber((int) (ton_balance / 1_000_000_000.0), 0));
                        } else {
                            DecimalFormatSymbols symbols = new DecimalFormatSymbols(Locale.US);
                            symbols.setDecimalSeparator('.');
                            DecimalFormat formatterTON = new DecimalFormat("#.##", symbols);
                            formatterTON.setMinimumFractionDigits(2);
                            formatterTON.setMaximumFractionDigits(3);
                            formatterTON.setGroupingUsed(false);
                            ssb.append("TON ").append(formatterTON.format(ton_balance / 1_000_000_000.0));
                        }
                    }
                    textCell.setTextAndValueAndIcon(getString(R.string.BotBalanceTON), ChannelMonetizationLayout.replaceTON(StarsIntroActivity.replaceStarsWithPlain(ssb, .7f), textCell.getTextView().getPaint()), R.drawable.msg_ton, true);
                } else if (position == mProfileActivity.blockedUsersRow) {
                    if (mProfileActivity.chatInfo != null) {
                        textCell.setTextAndValueAndIcon(LocaleController.getString(R.string.ChannelBlacklist), String.format("%d", Math.max(mProfileActivity.chatInfo.banned_count, mProfileActivity.chatInfo.kicked_count)), R.drawable.msg_user_remove, position != mProfileActivity.membersSectionRow - 1);
                    } else {
                        textCell.setTextAndIcon(LocaleController.getString(R.string.ChannelBlacklist), R.drawable.msg_user_remove, position != mProfileActivity.membersSectionRow - 1);
                    }
                } else if (position == mProfileActivity.addMemberRow) {
                    textCell.setColors(Theme.key_windowBackgroundWhiteBlueIcon, Theme.key_windowBackgroundWhiteBlueButton);
                    boolean isNextPositionMember = position + 1 >= mProfileActivity.membersStartRow && position + 1 < mProfileActivity.membersEndRow;
                    textCell.setTextAndIcon(LocaleController.getString(R.string.AddMember), R.drawable.msg_contact_add, mProfileActivity.membersSectionRow == -1 || isNextPositionMember);
                } else if (position == mProfileActivity.sendMessageRow) {
                    textCell.setText(LocaleController.getString(R.string.SendMessageLocation), true);
                } else if (position == mProfileActivity.addToContactsRow) {
                    textCell.setTextAndIcon(LocaleController.getString(R.string.AddToContacts), R.drawable.msg_contact_add, false);
                    textCell.setColors(Theme.key_windowBackgroundWhiteBlueIcon, Theme.key_windowBackgroundWhiteBlueButton);
                } else if (position == mProfileActivity.reportReactionRow) {
                    TLRPC.Chat chat = mProfileActivity.getMessagesController().getChat(-mProfileActivity.reportReactionFromDialogId);
                    if (chat != null && ChatObject.canBlockUsers(chat)) {
                        textCell.setTextAndIcon(LocaleController.getString(R.string.ReportReactionAndBan), R.drawable.msg_block2, false);
                    } else {
                        textCell.setTextAndIcon(LocaleController.getString(R.string.ReportReaction), R.drawable.msg_report, false);
                    }

                    textCell.setColors(Theme.key_text_RedBold, Theme.key_text_RedRegular);
                    textCell.setColors(Theme.key_text_RedBold, Theme.key_text_RedRegular);
                } else if (position == mProfileActivity.reportRow) {
                    textCell.setText(LocaleController.getString(R.string.ReportUserLocation), false);
                    textCell.setColors(-1, Theme.key_text_RedRegular);
                    textCell.setColors(-1, Theme.key_text_RedRegular);
                } else if (position == mProfileActivity.languageRow) {
                    textCell.setTextAndValueAndIcon(LocaleController.getString(R.string.Language), LocaleController.getCurrentLanguageName(), false, R.drawable.msg2_language, false);
                    textCell.setImageLeft(23);
                } else if (position == mProfileActivity.notificationRow) {
                    textCell.setTextAndIcon(LocaleController.getString(R.string.NotificationsAndSounds), R.drawable.msg2_notifications, true);
                } else if (position == mProfileActivity.privacyRow) {
                    textCell.setTextAndIcon(LocaleController.getString(R.string.PrivacySettings), R.drawable.msg2_secret, true);
                } else if (position == mProfileActivity.dataRow) {
                    textCell.setTextAndIcon(LocaleController.getString(R.string.DataSettings), R.drawable.msg2_data, true);
                } else if (position == mProfileActivity.chatRow) {
                    textCell.setTextAndIcon(LocaleController.getString(R.string.ChatSettings), R.drawable.msg2_discussion, true);
                } else if (position == mProfileActivity.filtersRow) {
                    textCell.setTextAndIcon(LocaleController.getString(R.string.Filters), R.drawable.msg2_folder, true);
                } else if (position == mProfileActivity.stickersRow) {
                    textCell.setTextAndIcon(LocaleController.getString(R.string.StickersName), R.drawable.msg2_sticker, true);
                } else if (position == mProfileActivity.liteModeRow) {
                    textCell.setTextAndIcon(LocaleController.getString(R.string.PowerUsage), R.drawable.msg2_battery, true);
                } else if (position == mProfileActivity.questionRow) {
                    textCell.setTextAndIcon(LocaleController.getString(R.string.AskAQuestion), R.drawable.msg2_ask_question, true);
                } else if (position == mProfileActivity.faqRow) {
                    textCell.setTextAndIcon(LocaleController.getString(R.string.TelegramFAQ), R.drawable.msg2_help, true);
                } else if (position == mProfileActivity.policyRow) {
                    textCell.setTextAndIcon(LocaleController.getString(R.string.PrivacyPolicy), R.drawable.msg2_policy, false);
                } else if (position == mProfileActivity.sendLogsRow) {
                    textCell.setText(LocaleController.getString(R.string.DebugSendLogs), true);
                } else if (position == mProfileActivity.sendLastLogsRow) {
                    textCell.setText(LocaleController.getString(R.string.DebugSendLastLogs), true);
                } else if (position == mProfileActivity.clearLogsRow) {
                    textCell.setText(LocaleController.getString(R.string.DebugClearLogs), mProfileActivity.switchBackendRow != -1);
                } else if (position == mProfileActivity.switchBackendRow) {
                    textCell.setText("Switch Backend", false);
                } else if (position == mProfileActivity.devicesRow) {
                    textCell.setTextAndIcon(LocaleController.getString(R.string.Devices), R.drawable.msg2_devices, true);
                } else if (position == mProfileActivity.setAvatarRow) {
                    mProfileActivity.cellCameraDrawable.setCustomEndFrame(86);
                    mProfileActivity.cellCameraDrawable.setCurrentFrame(85, false);
                    textCell.setTextAndIcon(LocaleController.getString(R.string.SetProfilePhoto), mProfileActivity.cellCameraDrawable, false);
                    textCell.setColors(Theme.key_windowBackgroundWhiteBlueIcon, Theme.key_windowBackgroundWhiteBlueButton);
                    textCell.getImageView().setPadding(0, 0, 0, AndroidUtilities.dp(8));
                    textCell.setImageLeft(12);
                    mProfileActivity.setAvatarCell = textCell;
                } else if (position == mProfileActivity.addToGroupButtonRow) {
                    textCell.setTextAndIcon(LocaleController.getString(R.string.AddToGroupOrChannel), R.drawable.msg_groups_create, false);
                } else if (position == mProfileActivity.premiumRow) {
                    textCell.setTextAndIcon(LocaleController.getString(R.string.TelegramPremium), new AnimatedEmojiDrawable.WrapSizeDrawable(PremiumGradient.getInstance().premiumStarMenuDrawable, dp(24), dp(24)), true);
                    textCell.setImageLeft(23);
                } else if (position == mProfileActivity.starsRow) {
                    StarsController c = StarsController.getInstance(mProfileActivity.getCurrentAccount());
                    long balance = c.getBalance().amount;
                    textCell.setTextAndValueAndIcon(LocaleController.getString(R.string.MenuTelegramStars), c.balanceAvailable() && balance > 0 ? LocaleController.formatNumber((int) balance, ',') : "", new AnimatedEmojiDrawable.WrapSizeDrawable(PremiumGradient.getInstance().goldenStarMenuDrawable, dp(24), dp(24)), true);
                    textCell.setImageLeft(23);
                } else if (position == mProfileActivity.businessRow) {
                    textCell.setTextAndIcon(LocaleController.getString(R.string.TelegramBusiness), R.drawable.menu_shop, true);
                    textCell.setImageLeft(23);
                } else if (position == mProfileActivity.premiumGiftingRow) {
                    textCell.setTextAndIcon(LocaleController.getString(R.string.SendAGift), R.drawable.menu_gift, false);
                    textCell.setImageLeft(23);
                } else if (position == mProfileActivity.botPermissionLocation) {
                    textCell.setTextAndCheckAndColorfulIcon(LocaleController.getString(R.string.BotProfilePermissionLocation), mProfileActivity.botLocation != null && mProfileActivity.botLocation.granted(), R.drawable.filled_access_location, mProfileActivity.getThemedColor(Theme.key_color_green), mProfileActivity.botPermissionBiometry != -1);
                } else if (position == mProfileActivity.botPermissionBiometry) {
                    textCell.setTextAndCheckAndColorfulIcon(LocaleController.getString(R.string.BotProfilePermissionBiometry), mProfileActivity.botBiometry != null && mProfileActivity.botBiometry.granted(), R.drawable.filled_access_fingerprint, mProfileActivity.getThemedColor(Theme.key_color_orange), false);
                } else if (position == mProfileActivity.botPermissionEmojiStatus) {
                    textCell.setTextAndCheckAndColorfulIcon(LocaleController.getString(R.string.BotProfilePermissionEmojiStatus), mProfileActivity.getUserInfo() != null && mProfileActivity.getUserInfo().bot_can_manage_emoji_status, R.drawable.filled_access_sleeping, mProfileActivity.getThemedColor(Theme.key_color_lightblue), mProfileActivity.botPermissionLocation != -1 || mProfileActivity.botPermissionBiometry != -1);
                }
                textCell.valueTextView.setTextColor(mProfileActivity.dontApplyPeerColor(mProfileActivity.getThemedColor(Theme.key_windowBackgroundWhiteValueText), false));
                break;
            case VIEW_TYPE_NOTIFICATIONS_CHECK:
                NotificationsCheckCell checkCell = (NotificationsCheckCell) holder.itemView;
                if (position == mProfileActivity.notificationsRow) {
                    SharedPreferences preferences = MessagesController.getNotificationsSettings(mProfileActivity.getCurrentAccount());
                    long did;
                    if (mProfileActivity.getDialogId() != 0) {
                        did = mProfileActivity.getDialogId();
                    } else if (mProfileActivity.userId != 0) {
                        did = mProfileActivity.userId;
                    } else {
                        did = -mProfileActivity.chatId;
                    }
                    String key = NotificationsController.getSharedPrefKey(did, mProfileActivity.getTopicId());
                    boolean enabled = false;
                    boolean custom = preferences.getBoolean("custom_" + key, false);
                    boolean hasOverride = preferences.contains("notify2_" + key);
                    int value = preferences.getInt("notify2_" + key, 0);
                    int delta = preferences.getInt("notifyuntil_" + key, 0);
                    String val;
                    if (value == 3 && delta != Integer.MAX_VALUE) {
                        delta -= mProfileActivity.getConnectionsManager().getCurrentTime();
                        if (delta <= 0) {
                            if (custom) {
                                val = LocaleController.getString(R.string.NotificationsCustom);
                            } else {
                                val = LocaleController.getString(R.string.NotificationsOn);
                            }
                            enabled = true;
                        } else if (delta < 60 * 60) {
                            val = formatString("WillUnmuteIn", R.string.WillUnmuteIn, LocaleController.formatPluralString("Minutes", delta / 60));
                        } else if (delta < 60 * 60 * 24) {
                            val = formatString("WillUnmuteIn", R.string.WillUnmuteIn, LocaleController.formatPluralString("Hours", (int) Math.ceil(delta / 60.0f / 60)));
                        } else if (delta < 60 * 60 * 24 * 365) {
                            val = formatString("WillUnmuteIn", R.string.WillUnmuteIn, LocaleController.formatPluralString("Days", (int) Math.ceil(delta / 60.0f / 60 / 24)));
                        } else {
                            val = null;
                        }
                    } else {
                        if (value == 0) {
                            if (hasOverride) {
                                enabled = true;
                            } else {
                                enabled = mProfileActivity.getNotificationsController().isGlobalNotificationsEnabled(did, false, false);
                            }
                        } else if (value == 1) {
                            enabled = true;
                        }
                        if (enabled && custom) {
                            val = LocaleController.getString(R.string.NotificationsCustom);
                        } else {
                            val = enabled ? LocaleController.getString(R.string.NotificationsOn) : LocaleController.getString(R.string.NotificationsOff);
                        }
                    }
                    if (val == null) {
                        val = LocaleController.getString(R.string.NotificationsOff);
                    }
                    if (mProfileActivity.notificationsExceptionTopics != null && !mProfileActivity.notificationsExceptionTopics.isEmpty()) {
                        val = String.format(Locale.US, LocaleController.getPluralString("NotificationTopicExceptionsDesctription", mProfileActivity.notificationsExceptionTopics.size()), val, mProfileActivity.notificationsExceptionTopics.size());
                    }
                    checkCell.setAnimationsEnabled(mProfileActivity.isFragmentOpened());
                    checkCell.setTextAndValueAndCheck(LocaleController.getString(R.string.Notifications), val, enabled, mProfileActivity.botAppRow >= 0);
                }
                break;
            case VIEW_TYPE_SHADOW:
                View sectionCell = holder.itemView;
                sectionCell.setTag(position);
                Drawable drawable;
                if (position == mProfileActivity.infoSectionRow && mProfileActivity.lastSectionRow == -1 && mProfileActivity.secretSettingsSectionRow == -1 && mProfileActivity.sharedMediaRow == -1 && mProfileActivity.membersSectionRow == -1 || position == mProfileActivity.secretSettingsSectionRow || position == mProfileActivity.lastSectionRow || position == mProfileActivity.membersSectionRow && mProfileActivity.lastSectionRow == -1 && mProfileActivity.sharedMediaRow == -1) {
                    sectionCell.setBackgroundDrawable(Theme.getThemedDrawable(mContext, R.drawable.greydivider_bottom, mProfileActivity.getThemedColor(Theme.key_windowBackgroundGrayShadow)));
                } else {
                    sectionCell.setBackgroundDrawable(Theme.getThemedDrawable(mContext, R.drawable.greydivider, mProfileActivity.getThemedColor(Theme.key_windowBackgroundGrayShadow)));
                }
                break;
            case VIEW_TYPE_SHADOW_TEXT: {
                TextInfoPrivacyCell cell = (TextInfoPrivacyCell) holder.itemView;
                cell.setLinkTextRippleColor(null);
                if (position == mProfileActivity.infoSectionRow) {
                    final long did = mProfileActivity.getDialogId();
                    TLObject obj = mProfileActivity.getMessagesController().getUserOrChat(did);
                    TL_bots.botVerification bot_verification = mProfileActivity.getUserInfo() != null ? mProfileActivity.getUserInfo().bot_verification : mProfileActivity.chatInfo != null ? mProfileActivity.chatInfo.bot_verification : null;
                    if (mProfileActivity.botAppRow >= 0 || bot_verification != null) {
                        cell.setFixedSize(0);
                        final TLRPC.User user = mProfileActivity.getMessagesController().getUser(mProfileActivity.userId);
                        final boolean botOwner = user != null && user.bot && user.bot_can_edit;
                        SpannableStringBuilder sb = new SpannableStringBuilder();

                        if (mProfileActivity.botAppRow >= 0) {
                            sb.append(AndroidUtilities.replaceSingleTag(getString(botOwner ? R.string.ProfileBotOpenAppInfoOwner : R.string.ProfileBotOpenAppInfo), () -> {
                                Browser.openUrl(mContext, getString(botOwner ? R.string.ProfileBotOpenAppInfoOwnerLink : R.string.ProfileBotOpenAppInfoLink));
                            }));
                            if (bot_verification != null) {
                                sb.append("\n\n\n");
                            }
                        }
                        if (bot_verification != null) {
                            sb.append("x");
                            sb.setSpan(new AnimatedEmojiSpan(bot_verification.icon, cell.getTextView().getPaint().getFontMetricsInt()), sb.length() - 1, sb.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                            sb.append(" ");
                            SpannableString description = new SpannableString(bot_verification.description);
                            try {
                                AndroidUtilities.addLinksSafe(description, Linkify.WEB_URLS, false, false);
                                URLSpan[] spans = description.getSpans(0, description.length(), URLSpan.class);
                                for (int i = 0; i < spans.length; ++i) {
                                    URLSpan span = spans[i];
                                    int start = description.getSpanStart(span);
                                    int end = description.getSpanEnd(span);
                                    final String url = span.getURL();

                                    description.removeSpan(span);
                                    description.setSpan(new URLSpan(url) {
                                        @Override
                                        public void onClick(View widget) {
                                            Browser.openUrl(mContext, url);
                                        }

                                        @Override
                                        public void updateDrawState(@NonNull TextPaint ds) {
                                            ds.setUnderlineText(true);
                                        }
                                    }, start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                                }
                            } catch (Exception e) {
                                FileLog.e(e);
                            }
                            sb.append(description);
                        }

                        cell.setLinkTextRippleColor(Theme.multAlpha(mProfileActivity.getThemedColor(Theme.key_windowBackgroundWhiteGrayText4), 0.2f));
                        cell.setText(sb);
                    } else {
                        cell.setFixedSize(14);
                        cell.setText(null);
                    }
                } else if (position == mProfileActivity.infoAffiliateRow) {
                    final TLRPC.User botUser = mProfileActivity.getMessagesController().getUser(mProfileActivity.userId);
                    if (botUser != null && botUser.bot && botUser.bot_can_edit) {
                        cell.setFixedSize(0);
                        cell.setText(formatString(R.string.ProfileBotAffiliateProgramInfoOwner, UserObject.getUserName(botUser), percents(mProfileActivity.getUserInfo() != null && mProfileActivity.getUserInfo().starref_program != null ? mProfileActivity.getUserInfo().starref_program.commission_permille : 0)));
                    } else {
                        cell.setFixedSize(0);
                        cell.setText(formatString(R.string.ProfileBotAffiliateProgramInfo, UserObject.getUserName(botUser), percents(mProfileActivity.getUserInfo() != null && mProfileActivity.getUserInfo().starref_program != null ? mProfileActivity.getUserInfo().starref_program.commission_permille : 0)));
                    }
                }
                if (position == mProfileActivity.infoSectionRow && mProfileActivity.lastSectionRow == -1 && mProfileActivity.secretSettingsSectionRow == -1 && mProfileActivity.sharedMediaRow == -1 && mProfileActivity.membersSectionRow == -1 || position == mProfileActivity.secretSettingsSectionRow || position == mProfileActivity.lastSectionRow || position == mProfileActivity.membersSectionRow && mProfileActivity.lastSectionRow == -1 && mProfileActivity.sharedMediaRow == -1) {
                    cell.setBackgroundDrawable(Theme.getThemedDrawable(mContext, R.drawable.greydivider_bottom, mProfileActivity.getThemedColor(Theme.key_windowBackgroundGrayShadow)));
                } else {
                    cell.setBackgroundDrawable(Theme.getThemedDrawable(mContext, R.drawable.greydivider, mProfileActivity.getThemedColor(Theme.key_windowBackgroundGrayShadow)));
                }
                break;
            }
            case VIEW_TYPE_COLORFUL_TEXT: {
                AffiliateProgramFragment.ColorfulTextCell cell = (AffiliateProgramFragment.ColorfulTextCell) holder.itemView;
                cell.set(mProfileActivity.getThemedColor(Theme.key_color_green), R.drawable.filled_affiliate, getString(R.string.ProfileBotAffiliateProgram), null);
                cell.setPercent(mProfileActivity.getUserInfo() != null && mProfileActivity.getUserInfo().starref_program != null ? percents(mProfileActivity.getUserInfo().starref_program.commission_permille) : null);
                break;
            }
            case VIEW_TYPE_USER:
                UserCell userCell = (UserCell) holder.itemView;
                TLRPC.ChatParticipant part;
                try {
                    if (!mProfileActivity.visibleSortedUsers.isEmpty()) {
                        part = mProfileActivity.visibleChatParticipants.get(mProfileActivity.visibleSortedUsers.get(position - mProfileActivity.membersStartRow));
                    } else {
                        part = mProfileActivity.visibleChatParticipants.get(position - mProfileActivity.membersStartRow);
                    }
                } catch (Exception e) {
                    part = null;
                    FileLog.e(e);
                }
                if (part != null) {
                    String role;
                    if (part instanceof TLRPC.TL_chatChannelParticipant) {
                        TLRPC.ChannelParticipant channelParticipant = ((TLRPC.TL_chatChannelParticipant) part).channelParticipant;
                        if (!TextUtils.isEmpty(channelParticipant.rank)) {
                            role = channelParticipant.rank;
                        } else {
                            if (channelParticipant instanceof TLRPC.TL_channelParticipantCreator) {
                                role = LocaleController.getString(R.string.ChannelCreator);
                            } else if (channelParticipant instanceof TLRPC.TL_channelParticipantAdmin) {
                                role = LocaleController.getString(R.string.ChannelAdmin);
                            } else {
                                role = null;
                            }
                        }
                    } else {
                        if (part instanceof TLRPC.TL_chatParticipantCreator) {
                            role = LocaleController.getString(R.string.ChannelCreator);
                        } else if (part instanceof TLRPC.TL_chatParticipantAdmin) {
                            role = getString(R.string.ChannelAdmin);
                        } else {
                            role = null;
                        }
                    }
                    userCell.setAdminRole(role);
                    userCell.setData(mProfileActivity.getMessagesController().getUser(part.user_id), null, null, 0, position != mProfileActivity.membersEndRow - 1);
                }
                break;
            case VIEW_TYPE_BOTTOM_PADDING:
                holder.itemView.requestLayout();
                break;
            case VIEW_TYPE_SUGGESTION:
                SettingsSuggestionCell suggestionCell = (SettingsSuggestionCell) holder.itemView;
                if (position == mProfileActivity.passwordSuggestionRow) {
                    suggestionCell.setType(SettingsSuggestionCell.TYPE_PASSWORD);
                } else if (position == mProfileActivity.phoneSuggestionRow) {
                    suggestionCell.setType(SettingsSuggestionCell.TYPE_PHONE);
                } else if (position == mProfileActivity.graceSuggestionRow) {
                    suggestionCell.setType(SettingsSuggestionCell.TYPE_GRACE);
                }
                break;
            case VIEW_TYPE_ADDTOGROUP_INFO:
                TextInfoPrivacyCell addToGroupInfo = (TextInfoPrivacyCell) holder.itemView;
                addToGroupInfo.setBackground(Theme.getThemedDrawable(mContext, R.drawable.greydivider, mProfileActivity.getThemedColor(Theme.key_windowBackgroundGrayShadow)));
                addToGroupInfo.setText(LocaleController.getString(R.string.BotAddToGroupOrChannelInfo));
                break;
            case VIEW_TYPE_NOTIFICATIONS_CHECK_SIMPLE:
                TextCheckCell textCheckCell = (TextCheckCell) holder.itemView;
                textCheckCell.setTextAndCheck(LocaleController.getString(R.string.Notifications), !mProfileActivity.getMessagesController().isDialogMuted(mProfileActivity.getDialogId(), mProfileActivity.getTopicId()), false);
                break;
            case VIEW_TYPE_LOCATION:
                ((ProfileLocationCell) holder.itemView).set(mProfileActivity.getUserInfo() != null ? mProfileActivity.getUserInfo().business_location : null, mProfileActivity.notificationsDividerRow < 0 && !mProfileActivity.myProfile);
                break;
            case VIEW_TYPE_HOURS:
                ProfileHoursCell hoursCell = (ProfileHoursCell) holder.itemView;
                hoursCell.setOnTimezoneSwitchClick(view -> {
                    mProfileActivity.hoursShownMine = !mProfileActivity.hoursShownMine;
                    if (!mProfileActivity.hoursExpanded) {
                        mProfileActivity.hoursExpanded = true;
                    }
                    mProfileActivity.saveScrollPosition();
                    view.requestLayout();
                    mProfileActivity.listAdapter.notifyItemChanged(mProfileActivity.bizHoursRow);
                    if (mProfileActivity.savedScrollPosition >= 0) {
                        mProfileActivity.layoutManager.scrollToPositionWithOffset(mProfileActivity.savedScrollPosition, mProfileActivity.savedScrollOffset - mProfileActivity.getListView().getPaddingTop());
                    }
                });
                hoursCell.set(mProfileActivity.getUserInfo() != null ? mProfileActivity.getUserInfo().business_work_hours : null, mProfileActivity.hoursExpanded, mProfileActivity.hoursShownMine, mProfileActivity.notificationsDividerRow < 0 && !mProfileActivity.myProfile || mProfileActivity.bizLocationRow >= 0);
                break;
            case VIEW_TYPE_CHANNEL:
                ((ProfileChannelCell) holder.itemView).set(
                        mProfileActivity.getMessagesController().getChat(mProfileActivity.getUserInfo().personal_channel_id),
                        mProfileActivity.profileChannelMessageFetcher != null ? mProfileActivity.profileChannelMessageFetcher.messageObject : null
                );
                break;
            case VIEW_TYPE_BOT_APP:

                break;
        }
    }

    private CharSequence alsoUsernamesString(String originalUsername, ArrayList<TLRPC.TL_username> alsoUsernames, CharSequence fallback) {
        if (alsoUsernames == null) {
            return fallback;
        }
        alsoUsernames = new ArrayList<>(alsoUsernames);
        for (int i = 0; i < alsoUsernames.size(); ++i) {
            if (
                    !alsoUsernames.get(i).active ||
                            originalUsername != null && originalUsername.equals(alsoUsernames.get(i).username)
            ) {
                alsoUsernames.remove(i--);
            }
        }
        if (alsoUsernames.size() > 0) {
            SpannableStringBuilder usernames = new SpannableStringBuilder();
            for (int i = 0; i < alsoUsernames.size(); ++i) {
                TLRPC.TL_username usernameObj = alsoUsernames.get(i);
                final String usernameRaw = usernameObj.username;
                SpannableString username = new SpannableString("@" + usernameRaw);
                username.setSpan(makeUsernameLinkSpan(usernameObj), 0, username.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                username.setSpan(new ForegroundColorSpan(mProfileActivity.dontApplyPeerColor(mProfileActivity.getThemedColor(Theme.key_chat_messageLinkIn), false)), 0, username.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                usernames.append(username);
                if (i < alsoUsernames.size() - 1) {
                    usernames.append(", ");
                }
            }
            String string = getString(R.string.UsernameAlso);
            SpannableStringBuilder finalString = new SpannableStringBuilder(string);
            final String toFind = "%1$s";
            int index = string.indexOf(toFind);
            if (index >= 0) {
                finalString.replace(index, index + toFind.length(), usernames);
            }
            return finalString;
        } else {
            return fallback;
        }
    }

    private final HashMap<TLRPC.TL_username, ClickableSpan> usernameSpans = new HashMap<TLRPC.TL_username, ClickableSpan>();

    public ClickableSpan makeUsernameLinkSpan(TLRPC.TL_username usernameObj) {
        ClickableSpan span = usernameSpans.get(usernameObj);
        if (span != null) return span;

        final String usernameRaw = usernameObj.username;
        span = new ClickableSpan() {
            @Override
            public void onClick(@NonNull View view) {
                if (!usernameObj.editable) {
                    if (mProfileActivity.loadingSpan == this) return;
                    mProfileActivity.setLoadingSpan(this);
                    TL_fragment.TL_getCollectibleInfo req = new TL_fragment.TL_getCollectibleInfo();
                    TL_fragment.TL_inputCollectibleUsername input = new TL_fragment.TL_inputCollectibleUsername();
                    input.username = usernameObj.username;
                    req.collectible = input;
                    int reqId = mProfileActivity.getConnectionsManager().sendRequest(req, (res, err) -> AndroidUtilities.runOnUIThread(() -> {
                        mProfileActivity.setLoadingSpan(null);
                        if (res instanceof TL_fragment.TL_collectibleInfo) {
                            TLObject obj;
                            if (mProfileActivity.userId != 0) {
                                obj = mProfileActivity.getMessagesController().getUser(mProfileActivity.userId);
                            } else {
                                obj = mProfileActivity.getMessagesController().getChat(mProfileActivity.chatId);
                            }
                            if (mContext == null) {
                                return;
                            }
                            FragmentUsernameBottomSheet.open(mContext, FragmentUsernameBottomSheet.TYPE_USERNAME, usernameObj.username, obj, (TL_fragment.TL_collectibleInfo) res, mResourcesProvider);
                        } else {
                            BulletinFactory.showError(err);
                        }
                    }));
                    mProfileActivity.getConnectionsManager().bindRequestToGuid(reqId, mProfileActivity.getClassGuid());
                } else {
                    mProfileActivity.setLoadingSpan(null);
                    String urlFinal = mProfileActivity.getMessagesController().linkPrefix + "/" + usernameRaw;
                    if (mProfileActivity.getCurrentChat() == null || !mProfileActivity.getCurrentChat().noforwards) {
                        AndroidUtilities.addToClipboard(urlFinal);
                        mProfileActivity.getUndoView().showWithAction(0, UndoView.ACTION_USERNAME_COPIED, null);
                    }
                }
            }

            @Override
            public void updateDrawState(@NonNull TextPaint ds) {
                ds.setUnderlineText(false);
                ds.setColor(ds.linkColor);
            }
        };
        usernameSpans.put(usernameObj, span);
        return span;
    }

    @Override
    public void onViewRecycled(@NonNull RecyclerView.ViewHolder holder) {
        if (holder.getAdapterPosition() == mProfileActivity.setAvatarRow) {
            mProfileActivity.setAvatarCell = null;
        }
    }

    @Override
    public boolean isEnabled(RecyclerView.ViewHolder holder) {
        if (mProfileActivity.notificationRow != -1) {
            int position = holder.getAdapterPosition();
            return position == mProfileActivity.notificationRow || position == mProfileActivity.numberRow || position == mProfileActivity.privacyRow ||
                    position == mProfileActivity.languageRow || position == mProfileActivity.setUsernameRow || position == mProfileActivity.bioRow ||
                    position == mProfileActivity.versionRow || position == mProfileActivity.dataRow || position == mProfileActivity.chatRow ||
                    position == mProfileActivity.questionRow || position == mProfileActivity.devicesRow || position == mProfileActivity.filtersRow || position == mProfileActivity.stickersRow ||
                    position == mProfileActivity.faqRow || position == mProfileActivity.policyRow || position == mProfileActivity.sendLogsRow || position == mProfileActivity.sendLastLogsRow ||
                    position == mProfileActivity.clearLogsRow || position == mProfileActivity.switchBackendRow || position == mProfileActivity.setAvatarRow ||
                    position == mProfileActivity.addToGroupButtonRow || position == mProfileActivity.premiumRow || position == mProfileActivity.premiumGiftingRow ||
                    position == mProfileActivity.businessRow || position == mProfileActivity.liteModeRow || position == mProfileActivity.birthdayRow || position == mProfileActivity.channelRow ||
                    position == mProfileActivity.starsRow;
        }
        if (holder.itemView instanceof UserCell) {
            UserCell userCell = (UserCell) holder.itemView;
            Object object = userCell.getCurrentObject();
            if (object instanceof TLRPC.User) {
                TLRPC.User user = (TLRPC.User) object;
                if (UserObject.isUserSelf(user)) {
                    return false;
                }
            }
        }
        int type = holder.getItemViewType();
        return type != VIEW_TYPE_HEADER && type != VIEW_TYPE_DIVIDER && type != VIEW_TYPE_SHADOW &&
                type != VIEW_TYPE_EMPTY && type != VIEW_TYPE_BOTTOM_PADDING && type != VIEW_TYPE_SHARED_MEDIA &&
                type != 9 && type != 10 && type != VIEW_TYPE_BOT_APP; // These are legacy ones, left for compatibility
    }

    @Override
    public int getItemCount() {
        return mProfileActivity.rowCount;
    }

    @Override
    public int getItemViewType(int position) {
        if (position == mProfileActivity.infoHeaderRow || position == mProfileActivity.membersHeaderRow || position == mProfileActivity.settingsSectionRow2 ||
                position == mProfileActivity.numberSectionRow || position == mProfileActivity.helpHeaderRow || position == mProfileActivity.debugHeaderRow || position == mProfileActivity.botPermissionsHeader) {
            return VIEW_TYPE_HEADER;
        } else if (position == mProfileActivity.phoneRow || position == mProfileActivity.locationRow || position == mProfileActivity.numberRow || position == mProfileActivity.birthdayRow) {
            return VIEW_TYPE_TEXT_DETAIL;
        } else if (position == mProfileActivity.usernameRow || position == mProfileActivity.setUsernameRow) {
            return VIEW_TYPE_TEXT_DETAIL_MULTILINE;
        } else if (position == mProfileActivity.userInfoRow || position == mProfileActivity.channelInfoRow || position == mProfileActivity.bioRow) {
            return VIEW_TYPE_ABOUT_LINK;
        } else if (position == mProfileActivity.settingsTimerRow || position == mProfileActivity.settingsKeyRow || position == mProfileActivity.reportRow || position == mProfileActivity.reportReactionRow ||
                position == mProfileActivity.subscribersRow || position == mProfileActivity.subscribersRequestsRow || position == mProfileActivity.administratorsRow || position == mProfileActivity.settingsRow || position == mProfileActivity.blockedUsersRow ||
                position == mProfileActivity.addMemberRow || position == mProfileActivity.joinRow || position == mProfileActivity.unblockRow ||
                position == mProfileActivity.sendMessageRow || position == mProfileActivity.notificationRow || position == mProfileActivity.privacyRow ||
                position == mProfileActivity.languageRow || position == mProfileActivity.dataRow || position == mProfileActivity.chatRow ||
                position == mProfileActivity.questionRow || position == mProfileActivity.devicesRow || position == mProfileActivity.filtersRow || position == mProfileActivity.stickersRow ||
                position == mProfileActivity.faqRow || position == mProfileActivity.policyRow || position == mProfileActivity.sendLogsRow || position == mProfileActivity.sendLastLogsRow ||
                position == mProfileActivity.clearLogsRow || position == mProfileActivity.switchBackendRow || position == mProfileActivity.setAvatarRow || position == mProfileActivity.addToGroupButtonRow ||
                position == mProfileActivity.addToContactsRow || position == mProfileActivity.liteModeRow || position == mProfileActivity.premiumGiftingRow || position == mProfileActivity.businessRow || position == mProfileActivity.botStarsBalanceRow || position == mProfileActivity.botTonBalanceRow || position == mProfileActivity.channelBalanceRow || position == mProfileActivity.botPermissionLocation || position == mProfileActivity.botPermissionBiometry || position == mProfileActivity.botPermissionEmojiStatus) {
            return VIEW_TYPE_TEXT;
        } else if (position == mProfileActivity.notificationsDividerRow) {
            return VIEW_TYPE_DIVIDER;
        } else if (position == mProfileActivity.notificationsRow) {
            return VIEW_TYPE_NOTIFICATIONS_CHECK;
        } else if (position == mProfileActivity.notificationsSimpleRow) {
            return VIEW_TYPE_NOTIFICATIONS_CHECK_SIMPLE;
        } else if (position == mProfileActivity.lastSectionRow || position == mProfileActivity.membersSectionRow ||
                position == mProfileActivity.secretSettingsSectionRow || position == mProfileActivity.settingsSectionRow || position == mProfileActivity.devicesSectionRow ||
                position == mProfileActivity.helpSectionCell || position == mProfileActivity.setAvatarSectionRow || position == mProfileActivity.passwordSuggestionSectionRow ||
                position == mProfileActivity.phoneSuggestionSectionRow || position == mProfileActivity.premiumSectionsRow || position == mProfileActivity.reportDividerRow ||
                position == mProfileActivity.channelDividerRow || position == mProfileActivity.graceSuggestionSectionRow || position == mProfileActivity.balanceDividerRow ||
                position == mProfileActivity.botPermissionsDivider || position == mProfileActivity.channelBalanceSectionRow
        ) {
            return VIEW_TYPE_SHADOW;
        } else if (position >= mProfileActivity.membersStartRow && position < mProfileActivity.membersEndRow) {
            return VIEW_TYPE_USER;
        } else if (position == mProfileActivity.emptyRow) {
            return VIEW_TYPE_EMPTY;
        } else if (position ==mProfileActivity. bottomPaddingRow) {
            return VIEW_TYPE_BOTTOM_PADDING;
        } else if (position == mProfileActivity.sharedMediaRow) {
            return VIEW_TYPE_SHARED_MEDIA;
        } else if (position == mProfileActivity.versionRow) {
            return VIEW_TYPE_VERSION;
        } else if (position == mProfileActivity.passwordSuggestionRow || position == mProfileActivity.phoneSuggestionRow || position == mProfileActivity.graceSuggestionRow) {
            return VIEW_TYPE_SUGGESTION;
        } else if (position == mProfileActivity.addToGroupInfoRow) {
            return VIEW_TYPE_ADDTOGROUP_INFO;
        } else if (position == mProfileActivity.premiumRow) {
            return VIEW_TYPE_PREMIUM_TEXT_CELL;
        } else if (position == mProfileActivity.starsRow) {
            return VIEW_TYPE_STARS_TEXT_CELL;
        } else if (position == mProfileActivity.bizLocationRow) {
            return VIEW_TYPE_LOCATION;
        } else if (position == mProfileActivity.bizHoursRow) {
            return VIEW_TYPE_HOURS;
        } else if (position == mProfileActivity.channelRow) {
            return VIEW_TYPE_CHANNEL;
        } else if (position == mProfileActivity.botAppRow) {
            return VIEW_TYPE_BOT_APP;
        } else if (position == mProfileActivity.infoSectionRow || position == mProfileActivity.infoAffiliateRow) {
            return VIEW_TYPE_SHADOW_TEXT;
        } else if (position == mProfileActivity.affiliateRow) {
            return VIEW_TYPE_COLORFUL_TEXT;
        }
        return 0;
    }
}
