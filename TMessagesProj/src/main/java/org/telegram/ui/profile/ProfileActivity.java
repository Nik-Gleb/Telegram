/*
 * This is the source code of Telegram for Android v. 5.x.x.
 * It is licensed under GNU GPL v. 2 or later.
 * You should have received a copy of the license in this archive (see LICENSE).
 *
 * Copyright Nikolai Kudashov, 2013-2018.
 */

package org.telegram.ui.profile;

import static org.telegram.messenger.AndroidUtilities.dp;
import static org.telegram.messenger.AndroidUtilities.lerp;
import static org.telegram.messenger.ContactsController.PRIVACY_RULES_TYPE_ADDED_BY_PHONE;
import static org.telegram.messenger.LocaleController.formatPluralString;
import static org.telegram.messenger.LocaleController.formatString;
import static org.telegram.messenger.LocaleController.getString;
import static org.telegram.ui.Stars.StarsIntroActivity.formatStarsAmountShort;
import static org.telegram.ui.profile.ProfileActionHandler.handleMenuAction;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.animation.StateListAnimator;
import android.animation.ValueAnimator;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Dialog;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorFilter;
import android.graphics.Outline;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Point;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.media.MediaCodecInfo;
import android.media.MediaCodecList;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.text.SpannableString;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.style.CharacterStyle;
import android.text.style.ForegroundColorSpan;
import android.util.Property;
import android.util.SparseArray;
import android.util.SparseIntArray;
import android.util.TypedValue;
import android.view.Display;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewOutlineProvider;
import android.view.ViewTreeObserver;
import android.view.accessibility.AccessibilityNodeInfo;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.DecelerateInterpolator;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Keep;
import androidx.collection.LongSparseArray;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import androidx.core.graphics.ColorUtils;
import androidx.core.math.MathUtils;
import androidx.interpolator.view.animation.FastOutSlowInInterpolator;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.LinearSmoothScrollerCustom;
import androidx.recyclerview.widget.RecyclerView;

import org.telegram.PhoneFormat.PhoneFormat;
import org.telegram.messenger.AccountInstance;
import org.telegram.messenger.AndroidUtilities;
import org.telegram.messenger.ApplicationLoader;
import org.telegram.messenger.BillingController;
import org.telegram.messenger.BuildVars;
import org.telegram.messenger.ChatObject;
import org.telegram.messenger.ContactsController;
import org.telegram.messenger.DialogObject;
import org.telegram.messenger.DocumentObject;
import org.telegram.messenger.Emoji;
import org.telegram.messenger.FileLoader;
import org.telegram.messenger.FileLog;
import org.telegram.messenger.FlagSecureReason;
import org.telegram.messenger.ImageLoader;
import org.telegram.messenger.ImageLocation;
import org.telegram.messenger.ImageReceiver;
import org.telegram.messenger.LanguageDetector;
import org.telegram.messenger.LocaleController;
import org.telegram.messenger.MediaDataController;
import org.telegram.messenger.MessageObject;
import org.telegram.messenger.MessagesController;
import org.telegram.messenger.MessagesStorage;
import org.telegram.messenger.NotificationCenter;
import org.telegram.messenger.R;
import org.telegram.messenger.SendMessagesHelper;
import org.telegram.messenger.SharedConfig;
import org.telegram.messenger.SvgHelper;
import org.telegram.messenger.UserConfig;
import org.telegram.messenger.UserObject;
import org.telegram.messenger.Utilities;
import org.telegram.messenger.browser.Browser;
import org.telegram.tgnet.ConnectionsManager;
import org.telegram.tgnet.TLObject;
import org.telegram.tgnet.TLRPC;
import org.telegram.tgnet.tl.TL_account;
import org.telegram.tgnet.tl.TL_bots;
import org.telegram.tgnet.tl.TL_fragment;
import org.telegram.tgnet.tl.TL_stars;
import org.telegram.tgnet.tl.TL_stories;
import org.telegram.ui.ActionBar.ActionBar;
import org.telegram.ui.ActionBar.ActionBarMenu;
import org.telegram.ui.ActionBar.ActionBarMenuItem;
import org.telegram.ui.ActionBar.ActionBarMenuSubItem;
import org.telegram.ui.ActionBar.ActionBarPopupWindow;
import org.telegram.ui.ActionBar.AlertDialog;
import org.telegram.ui.ActionBar.BackDrawable;
import org.telegram.ui.ActionBar.BaseFragment;
import org.telegram.ui.ActionBar.INavigationLayout;
import org.telegram.ui.ActionBar.OKLCH;
import org.telegram.ui.ActionBar.SimpleTextView;
import org.telegram.ui.ActionBar.Theme;
import org.telegram.ui.ActionBar.ThemeDescription;
import org.telegram.ui.ActionIntroActivity;
import org.telegram.ui.AutoDeleteMessagesActivity;
import org.telegram.ui.Business.OpeningHoursActivity;
import org.telegram.ui.Business.ProfileHoursCell;
import org.telegram.ui.Cells.AboutLinkCell;
import org.telegram.ui.Cells.DrawerProfileCell;
import org.telegram.ui.Cells.GraySectionCell;
import org.telegram.ui.Cells.HeaderCell;
import org.telegram.ui.Cells.NotificationsCheckCell;
import org.telegram.ui.Cells.ProfileChannelCell;
import org.telegram.ui.Cells.SettingsSearchCell;
import org.telegram.ui.Cells.SettingsSuggestionCell;
import org.telegram.ui.Cells.ShadowSectionCell;
import org.telegram.ui.Cells.TextCell;
import org.telegram.ui.Cells.TextDetailCell;
import org.telegram.ui.Cells.TextInfoPrivacyCell;
import org.telegram.ui.Cells.UserCell;
import org.telegram.ui.ChangeUsernameActivity;
import org.telegram.ui.ChannelAdminLogActivity;
import org.telegram.ui.ChatActivity;
import org.telegram.ui.ChatRightsEditActivity;
import org.telegram.ui.Components.AlertsCreator;
import org.telegram.ui.Components.AnimatedEmojiDrawable;
import org.telegram.ui.Components.AnimatedFileDrawable;
import org.telegram.ui.Components.AnimatedTextView;
import org.telegram.ui.Components.AnimationProperties;
import org.telegram.ui.Components.AudioPlayerAlert;
import org.telegram.ui.Components.AutoDeletePopupWrapper;
import org.telegram.ui.Components.AvatarDrawable;
import org.telegram.ui.Components.BackButtonMenu;
import org.telegram.ui.Components.BackupImageView;
import org.telegram.ui.Components.Bulletin;
import org.telegram.ui.Components.BulletinFactory;
import org.telegram.ui.Components.CanvasButton;
import org.telegram.ui.Components.ChatActivityInterface;
import org.telegram.ui.Components.ChatAvatarContainer;
import org.telegram.ui.Components.ColoredImageSpan;
import org.telegram.ui.Components.CombinedDrawable;
import org.telegram.ui.Components.CrossfadeDrawable;
import org.telegram.ui.Components.CubicBezierInterpolator;
import org.telegram.ui.Components.DotDividerSpan;
import org.telegram.ui.Components.EmojiPacksAlert;
import org.telegram.ui.Components.EmptyStubSpan;
import org.telegram.ui.Components.Forum.ForumUtilities;
import org.telegram.ui.Components.FragmentContextView;
import org.telegram.ui.Components.HintView;
import org.telegram.ui.Components.ImageUpdater;
import org.telegram.ui.Components.ItemOptions;
import org.telegram.ui.Components.LayoutHelper;
import org.telegram.ui.Components.LinkSpanDrawable;
import org.telegram.ui.Components.MessagePrivateSeenView;
import org.telegram.ui.Components.Premium.LimitReachedBottomSheet;
import org.telegram.ui.Components.Premium.PremiumFeatureBottomSheet;
import org.telegram.ui.Components.Premium.PremiumPreviewBottomSheet;
import org.telegram.ui.Components.ProfileGalleryView;
import org.telegram.ui.Components.RLottieDrawable;
import org.telegram.ui.Components.RLottieImageView;
import org.telegram.ui.Components.RadialProgressView;
import org.telegram.ui.Components.Reactions.ReactionsLayoutInBubble;
import org.telegram.ui.Components.RecyclerListView;
import org.telegram.ui.Components.ScamDrawable;
import org.telegram.ui.Components.ShareAlert;
import org.telegram.ui.Components.SharedMediaLayout;
import org.telegram.ui.Components.StickerEmptyView;
import org.telegram.ui.Components.TimerDrawable;
import org.telegram.ui.Components.TranslateAlert2;
import org.telegram.ui.Components.TypefaceSpan;
import org.telegram.ui.Components.UndoView;
import org.telegram.ui.Components.VectorAvatarThumbDrawable;
import org.telegram.ui.Components.voip.VoIPHelper;
import org.telegram.ui.ContactAddActivity;
import org.telegram.ui.DialogsActivity;
import org.telegram.ui.FragmentUsernameBottomSheet;
import org.telegram.ui.Gifts.GiftSheet;
import org.telegram.ui.GroupCreateActivity;
import org.telegram.ui.LaunchActivity;
import org.telegram.ui.LocationActivity;
import org.telegram.ui.PhotoViewer;
import org.telegram.ui.PinchToZoomHelper;
import org.telegram.ui.PremiumPreviewFragment;
import org.telegram.ui.PrivacyControlActivity;
import org.telegram.ui.ProfileBirthdayEffect;
import org.telegram.ui.QrActivity;
import org.telegram.ui.RestrictedLanguagesSelectActivity;
import org.telegram.ui.SelectAnimatedEmojiDialog;
import org.telegram.ui.Stars.BotStarsController;
import org.telegram.ui.Stars.ProfileGiftsView;
import org.telegram.ui.Stars.StarGiftSheet;
import org.telegram.ui.Stars.StarsController;
import org.telegram.ui.StatisticActivity;
import org.telegram.ui.Stories.ProfileStoriesView;
import org.telegram.ui.Stories.StoriesController;
import org.telegram.ui.Stories.StoryViewer;
import org.telegram.ui.Stories.recorder.ButtonWithCounterView;
import org.telegram.ui.Stories.recorder.HintView2;
import org.telegram.ui.Stories.recorder.StoryRecorder;
import org.telegram.ui.TopicsFragment;
import org.telegram.ui.UserInfoActivity;
import org.telegram.ui.bots.BotBiometry;
import org.telegram.ui.bots.BotLocation;
import org.telegram.ui.bots.SetupEmojiStatusSheet;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicReference;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public class ProfileActivity extends BaseFragment implements NotificationCenter.NotificationCenterDelegate, DialogsActivity.DialogsActivityDelegate, SharedMediaLayout.SharedMediaPreloaderDelegate, ImageUpdater.ImageUpdaterDelegate, SharedMediaLayout.Delegate {
    private final static int PHONE_OPTION_CALL = 0,
        PHONE_OPTION_COPY = 1,
        PHONE_OPTION_TELEGRAM_CALL = 2,
        PHONE_OPTION_TELEGRAM_VIDEO_CALL = 3;

    private RecyclerListView listView;
    private RecyclerListView searchListView;
    public LinearLayoutManager layoutManager;
    public ProfileAdapter listAdapter;
    private SearchAdapter searchAdapter;
    private SimpleTextView[] nameTextView = new SimpleTextView[2];
    private String nameTextViewRightDrawableContentDescription = null;
    private String nameTextViewRightDrawable2ContentDescription = null;
    private SimpleTextView[] onlineTextView = new SimpleTextView[4];
    private AudioPlayerAlert.ClippingTextViewSwitcher mediaCounterTextView;
    private RLottieImageView writeButton;
    private AnimatorSet writeButtonAnimation;
    private AnimatorSet qrItemAnimation;
    private Drawable lockIconDrawable;
    private final Drawable[] verifiedDrawable = new Drawable[2];
    private final Drawable[] premiumStarDrawable = new Drawable[2];
    private Long emojiStatusGiftId;
    private final AnimatedEmojiDrawable.SwapAnimatedEmojiDrawable[] emojiStatusDrawable = new AnimatedEmojiDrawable.SwapAnimatedEmojiDrawable[2];
    private final AnimatedEmojiDrawable.SwapAnimatedEmojiDrawable[] botVerificationDrawable = new AnimatedEmojiDrawable.SwapAnimatedEmojiDrawable[2];
    private final Drawable[] verifiedCheckDrawable = new Drawable[2];
    private final CrossfadeDrawable[] verifiedCrossfadeDrawable = new CrossfadeDrawable[2];
    private final CrossfadeDrawable[] premiumCrossfadeDrawable = new CrossfadeDrawable[2];
    private ScamDrawable scamDrawable;
    private UndoView undoView;
    public OverlaysView overlaysView;
    public SharedMediaLayout sharedMediaLayout;
    public StickerEmptyView emptyView;
    public boolean sharedMediaLayoutAttached;
    private SharedMediaLayout.SharedMediaPreloader sharedMediaPreloader;
    private boolean preloadedChannelEmojiStatuses;

    private View blurredView;

    private RLottieDrawable cameraDrawable;
    public RLottieDrawable cellCameraDrawable;

    private HintView fwdRestrictedHint;
    public FrameLayout avatarContainer;
    private FrameLayout avatarContainer2;
    private DrawerProfileCell.AnimatedStatusView animatedStatusView;
    public AvatarImageView avatarImage;
    private View avatarOverlay;
    private AnimatorSet avatarAnimation;
    private RadialProgressView avatarProgressView;
    private ImageView timeItem;
    private ImageView starBgItem, starFgItem;
    private TimerDrawable timerDrawable;
    public ProfileGalleryView avatarsViewPager;
    private PagerIndicatorView avatarsViewPagerIndicatorView;
    private AvatarDrawable avatarDrawable;
    public ImageUpdater imageUpdater;
    private int avatarColor;
    TimerDrawable autoDeleteItemDrawable;
    private ProfileStoriesView storyView;
    public ProfileGiftsView giftsView;

    private View scrimView = null;
    private Paint scrimPaint = new Paint(Paint.ANTI_ALIAS_FLAG) {
        @Override
        public void setAlpha(int a) {
            super.setAlpha(a);
            fragmentView.invalidate();
        }
    };
    private Paint actionBarBackgroundPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private ActionBarPopupWindow scrimPopupWindow;
    public Theme.ResourcesProvider resourcesProvider;

    public int overlayCountVisible;

    private ImageLocation prevLoadedImageLocation;

    private int lastMeasuredContentWidth;
    private int lastMeasuredContentHeight;
    private int listContentHeight;
    private boolean openingAvatar;
    private boolean fragmentViewAttached;

    public boolean doNotSetForeground;

    private boolean[] isOnline = new boolean[1];

    public boolean callItemVisible;
    public boolean videoCallItemVisible;
    public boolean editItemVisible;
    private ActionBarMenuItem animatingItem;
    public ActionBarMenuItem callItem;
    public ActionBarMenuItem videoCallItem;
    public ActionBarMenuItem editItem;
    public ActionBarMenuItem otherItem;
    public ActionBarMenuItem searchItem;
    private ActionBarMenuSubItem editColorItem;
    private ActionBarMenuSubItem linkItem;
    private ActionBarMenuSubItem setUsernameItem;
    private ImageView ttlIconView;
    public ActionBarMenuItem qrItem;
    private ActionBarMenuSubItem autoDeleteItem;
    AutoDeletePopupWrapper autoDeletePopupWrapper;
    public float headerShadowAlpha = 1.0f;
    public int actionBarBackgroundColor;
    private TopView topView;
    public long userId;
    public long chatId;
    private long topicId;
    public boolean saved;
    private long dialogId;
    public boolean creatingChat;
    public boolean userBlocked;
    public boolean reportSpam;
    private long mergeDialogId;
    public boolean expandPhoto;
    private boolean needSendMessage;
    private boolean hasVoiceChatItem;
    public boolean isTopic;
    private boolean openSimilar;
    public boolean myProfile;
    public boolean openGifts;
    private boolean openedGifts;
    public boolean openCommonChats;

    private boolean scrolling;

    private boolean canSearchMembers;

    private boolean loadingUsers;
    private LongSparseArray<TLRPC.ChatParticipant> participantsMap = new LongSparseArray<>();
    private boolean usersEndReached;

    private long banFromGroup;
    private boolean openAnimationInProgress;
    private boolean transitionAnimationInProress;
    private boolean recreateMenuAfterAnimation;
    public int playProfileAnimation;
    private boolean needTimerImage;
    private boolean needStarImage;
    private boolean allowProfileAnimation = true;
    public boolean disableProfileAnimation = false;
    public float extraHeight;
    private float initialAnimationExtraHeight;
    private float avatarAnimationProgress;

    public int searchTransitionOffset;
    private float searchTransitionProgress;
    private Animator searchViewTransition;
    private boolean searchMode;

    private FlagSecureReason flagSecure;

    private HashMap<Integer, Integer> positionToOffset = new HashMap<>();

    private float avatarX;
    private float avatarY;
    private float avatarScale;
    private float nameX;
    private float nameY;
    private float onlineX;
    private float onlineY;
    private float expandProgress;
    private float listViewVelocityY;
    private ValueAnimator expandAnimator;
    private float currentExpandAnimatorValue;
    private float currentExpanAnimatorFracture;
    private float[] expandAnimatorValues = new float[]{0f, 1f};
    public boolean isInLandscapeMode;
    public boolean allowPullingDown;
    public boolean isPulledDown;

    private Paint whitePaint = new Paint();

    public boolean isBot;
    public BotLocation botLocation;
    public BotBiometry botBiometry;

    public TLRPC.ChatFull chatInfo;
    private TLRPC.UserFull userInfo;

    public ProfileChannelCell.ChannelMessageFetcher profileChannelMessageFetcher;
    public boolean createdBirthdayFetcher;
    public ProfileBirthdayEffect.BirthdayEffectFetcher birthdayFetcher;

    public CharSequence currentBio;

    private long selectedUser;
    private int onlineCount = -1;
    public ArrayList<Integer> sortedUsers;

    public TLRPC.EncryptedChat currentEncryptedChat;
    private TLRPC.Chat currentChat;
    public TL_bots.BotInfo botInfo;
    private TLRPC.ChannelParticipant currentChannelParticipant;
    public TL_account.TL_password currentPassword;

    public TLRPC.FileLocation avatar;
    public TLRPC.FileLocation avatarBig;
    public ImageLocation uploadingImageLocation;

    public final static int add_contact = 1;
    public final static int block_contact = 2;
    public final static int share_contact = 3;
    public final static int edit_contact = 4;
    public final static int delete_contact = 5;
    public final static int leave_group = 7;
    public final static int invite_to_group = 9;
    public final static int share = 10;
    public final static int edit_channel = 12;
    public final static int add_shortcut = 14;
    public final static int call_item = 15;
    public final static int video_call_item = 16;
    public final static int search_members = 17;
    public final static int add_member = 18;
    public final static int statistics = 19;
    public final static int start_secret_chat = 20;
    public final static int gallery_menu_save = 21;
    public final static int view_discussion = 22;
    public final static int delete_topic = 23;
    public final static int report = 24;

    public final static int edit_info = 30;
    public final static int logout = 31;
    private final static int search_button = 32;
    public final static int set_as_main = 33;
    public final static int edit_avatar = 34;
    public final static int delete_avatar = 35;
    public final static int add_photo = 36;
    public final static int qr_button = 37;
    public final static int gift_premium = 38;
    public final static int channel_stories = 39;
    public final static int edit_color = 40;
    public final static int edit_profile = 41;
    public final static int copy_link_profile = 42;
    public final static int set_username = 43;
    public final static int bot_privacy = 44;

    private Rect rect = new Rect();

    public TextCell setAvatarCell;

    public int rowCount;

    public int setAvatarRow;
    public int setAvatarSectionRow;
    public int channelRow;
    public int channelDividerRow;
    public int numberSectionRow;
    public int numberRow;
    public int birthdayRow;
    public int setUsernameRow;
    public int bioRow;
    public int phoneSuggestionSectionRow;
    public int graceSuggestionRow;
    public int graceSuggestionSectionRow;
    public int phoneSuggestionRow;
    public int passwordSuggestionSectionRow;
    public int passwordSuggestionRow;
    public int settingsSectionRow;
    public int settingsSectionRow2;
    public int notificationRow;
    public int languageRow;
    public int privacyRow;
    public int dataRow;
    public int chatRow;
    public int filtersRow;
    public int liteModeRow;
    public int stickersRow;
    public int devicesRow;
    public int devicesSectionRow;
    public int helpHeaderRow;
    public int questionRow;
    public int faqRow;
    public int policyRow;
    public int helpSectionCell;
    public int debugHeaderRow;
    public int sendLogsRow;
    public int sendLastLogsRow;
    public int clearLogsRow;
    public int switchBackendRow;
    public int versionRow;
    public int emptyRow;
    public int bottomPaddingRow;
    public int infoHeaderRow;
    public int phoneRow;
    public int locationRow;
    public int userInfoRow;
    public int channelInfoRow;
    public int usernameRow;
    public int notificationsDividerRow;
    public int notificationsRow;
    public int bizHoursRow;
    public int bizLocationRow;
    public int notificationsSimpleRow;
    private int infoStartRow, infoEndRow;
    public int infoSectionRow;
    public int affiliateRow;
    public int infoAffiliateRow;
    public int sendMessageRow;
    public int reportRow;
    public int reportReactionRow;
    public int reportDividerRow;
    public int addToContactsRow;
    public int addToGroupButtonRow;
    public int addToGroupInfoRow;
    public int premiumRow;
    public int starsRow;
    public int businessRow;
    public int premiumGiftingRow;
    public int premiumSectionsRow;
    public int botAppRow;
    public int botPermissionsHeader;
    @Keep
    public int botPermissionLocation;
    @Keep
    public int botPermissionEmojiStatus;
    public int botPermissionEmojiStatusReqId;
    @Keep
    public int botPermissionBiometry;
    public int botPermissionsDivider;

    public int settingsTimerRow;
    public int settingsKeyRow;
    public int secretSettingsSectionRow;

    public int membersHeaderRow;
    public int membersStartRow;
    public int membersEndRow;
    public int addMemberRow;
    public int subscribersRow;
    public int subscribersRequestsRow;
    public int administratorsRow;
    public int settingsRow;
    public int botStarsBalanceRow;
    public int botTonBalanceRow;
    public int channelBalanceRow;
    public int channelBalanceSectionRow;
    public int balanceDividerRow;
    public int blockedUsersRow;
    public int membersSectionRow;

    public int sharedMediaRow;

    public int unblockRow;
    public int joinRow;
    public int lastSectionRow;

    public boolean hoursExpanded;
    public boolean hoursShownMine;

    private int transitionIndex;
    public final ArrayList<TLRPC.ChatParticipant> visibleChatParticipants = new ArrayList<>();
    public final ArrayList<Integer> visibleSortedUsers = new ArrayList<>();
    private int usersForceShowingIn = 0;

    private boolean firstLayout = true;
    private boolean invalidateScroll = true;
    private boolean isQrItemVisible = true;

    PinchToZoomHelper pinchToZoomHelper;

    private View transitionOnlineText;
    private int actionBarAnimationColorFrom = 0;
    private int navigationBarAnimationColorFrom = 0;
    public int reportReactionMessageId = 0;
    public long reportReactionFromDialogId = 0;

    public boolean isFragmentPhoneNumber;

    private boolean showAddToContacts;
    public String vcardPhone;
    public String vcardFirstName;
    public String vcardLastName;

    public BaseFragment previousTransitionMainFragment;
    public ChatActivityInterface previousTransitionFragment;

    public HashSet<Integer> notificationsExceptionTopics = new HashSet<>();

    public CharacterStyle loadingSpan;

    private final Property<ProfileActivity, Float> HEADER_SHADOW = new AnimationProperties.FloatProperty<ProfileActivity>("headerShadow") {
        @Override
        public void setValue(ProfileActivity object, float value) {
            headerShadowAlpha = value;
            topView.invalidate();
        }

        @Override
        public Float get(ProfileActivity object) {
            return headerShadowAlpha;
        }
    };

    private PhotoViewer.PhotoViewerProvider provider = new PhotoViewer.EmptyPhotoViewerProvider() {

        @Override
        public PhotoViewer.PlaceProviderObject getPlaceForPhoto(MessageObject messageObject, TLRPC.FileLocation fileLocation, int index, boolean needPreview, boolean closing) {
            if (fileLocation == null) {
                return null;
            }

            TLRPC.FileLocation photoBig = null;
            if (userId != 0) {
                TLRPC.User user = getMessagesController().getUser(userId);
                if (user != null && user.photo != null && user.photo.photo_big != null) {
                    photoBig = user.photo.photo_big;
                }
            } else if (chatId != 0) {
                TLRPC.Chat chat = getMessagesController().getChat(chatId);
                if (chat != null && chat.photo != null && chat.photo.photo_big != null) {
                    photoBig = chat.photo.photo_big;
                }
            }

            if (photoBig != null && photoBig.local_id == fileLocation.local_id && photoBig.volume_id == fileLocation.volume_id && photoBig.dc_id == fileLocation.dc_id) {
                int[] coords = new int[2];
                avatarImage.getLocationInWindow(coords);
                PhotoViewer.PlaceProviderObject object = new PhotoViewer.PlaceProviderObject();
                object.viewX = coords[0];
                object.viewY = coords[1] - (Build.VERSION.SDK_INT >= 21 ? 0 : AndroidUtilities.statusBarHeight);
                object.parentView = avatarImage;
                object.imageReceiver = avatarImage.getImageReceiver();
                if (userId != 0) {
                    object.dialogId = userId;
                } else if (chatId != 0) {
                    object.dialogId = -chatId;
                }
                object.thumb = object.imageReceiver.getBitmapSafe();
                object.size = -1;
                object.radius = avatarImage.getImageReceiver().getRoundRadius(true);
                object.scale = avatarContainer.getScaleX();
                object.canEdit = userId == getUserConfig().clientUserId;
                return object;
            }
            return null;
        }

        @Override
        public void willHidePhotoViewer() {
            avatarImage.getImageReceiver().setVisible(true, true);
        }

        @Override
        public void openPhotoForEdit(String file, String thumb, boolean isVideo) {
            imageUpdater.openPhotoForEdit(file, thumb, 0, isVideo);
        }
    };
    private boolean fragmentOpened;
    public NestedFrameLayout contentView;
    private float titleAnimationsYDiff;
    private float customAvatarProgress;
    private float customPhotoOffset;
    public boolean hasFallbackPhoto;
    private boolean hasCustomPhoto;
    private ImageReceiver fallbackImage;
    private boolean loadingBoostsStats;
    private boolean waitCanSendStoryRequest;
    private FrameLayout bottomButtonsContainer;
    private FrameLayout[] bottomButtonContainer;
    private SpannableStringBuilder bottomButtonPostText;
    private ButtonWithCounterView[] bottomButton;
    private Runnable applyBulletin;

    public static ProfileActivity of(long dialogId) {
        Bundle bundle = new Bundle();
        if (dialogId >= 0) {
            bundle.putLong("user_id", dialogId);
        } else {
            bundle.putLong("chat_id", -dialogId);
        }
        return new ProfileActivity(bundle);
    }

    public long getTopicId() {
        return topicId;
    }

    public ProfileActivity(Bundle args) {
        this(args, null);
    }

    public ProfileActivity(Bundle args, SharedMediaLayout.SharedMediaPreloader preloader) {
        super(args);
        sharedMediaPreloader = preloader;
    }

    @Override
    public boolean onFragmentCreate() {
        userId = arguments.getLong("user_id", 0);
        chatId = arguments.getLong("chat_id", 0);
        topicId = arguments.getLong("topic_id", 0);
        saved = arguments.getBoolean("saved", false);
        openSimilar = arguments.getBoolean("similar", false);
        isTopic = topicId != 0;
        banFromGroup = arguments.getLong("ban_chat_id", 0);
        reportReactionMessageId = arguments.getInt("report_reaction_message_id", 0);
        reportReactionFromDialogId = arguments.getLong("report_reaction_from_dialog_id", 0);
        showAddToContacts = arguments.getBoolean("show_add_to_contacts", true);
        vcardPhone = PhoneFormat.stripExceptNumbers(arguments.getString("vcard_phone"));
        vcardFirstName = arguments.getString("vcard_first_name");
        vcardLastName = arguments.getString("vcard_last_name");
        reportSpam = arguments.getBoolean("reportSpam", false);
        myProfile = arguments.getBoolean("my_profile", false);
        openGifts = arguments.getBoolean("open_gifts", false);
        openCommonChats = arguments.getBoolean("open_common", false);
        if (!expandPhoto) {
            expandPhoto = arguments.getBoolean("expandPhoto", false);
            if (expandPhoto) {
                currentExpandAnimatorValue = 1f;
                needSendMessage = true;
            }
        }
        if (userId != 0) {
            dialogId = arguments.getLong("dialog_id", 0);
            if (dialogId != 0) {
                currentEncryptedChat = getMessagesController().getEncryptedChat(DialogObject.getEncryptedChatId(dialogId));
            }
            if (flagSecure != null) {
                flagSecure.invalidate();
            }
            TLRPC.User user = getMessagesController().getUser(userId);
            if (user == null) {
                return false;
            }

            getNotificationCenter().addObserver(this, NotificationCenter.contactsDidLoad);
            getNotificationCenter().addObserver(this, NotificationCenter.newSuggestionsAvailable);
            getNotificationCenter().addObserver(this, NotificationCenter.encryptedChatCreated);
            getNotificationCenter().addObserver(this, NotificationCenter.encryptedChatUpdated);
            getNotificationCenter().addObserver(this, NotificationCenter.blockedUsersDidLoad);
            getNotificationCenter().addObserver(this, NotificationCenter.botInfoDidLoad);
            getNotificationCenter().addObserver(this, NotificationCenter.userInfoDidLoad);
            getNotificationCenter().addObserver(this, NotificationCenter.privacyRulesUpdated);
            NotificationCenter.getGlobalInstance().addObserver(this, NotificationCenter.reloadInterface);

            userBlocked = getMessagesController().blockePeers.indexOfKey(userId) >= 0;
            if (user.bot) {
                isBot = true;
                getMediaDataController().loadBotInfo(user.id, user.id, true, classGuid);
            }
            userInfo = getMessagesController().getUserFull(userId);
            getMessagesController().loadFullUser(getMessagesController().getUser(userId), classGuid, true);
            participantsMap = null;

            if (UserObject.isUserSelf(user)) {
                imageUpdater = new ImageUpdater(true, ImageUpdater.FOR_TYPE_USER, true);
                imageUpdater.setOpenWithFrontfaceCamera(true);
                imageUpdater.parentFragment = this;
                imageUpdater.setDelegate(this);
                getMediaDataController().checkFeaturedStickers();
                getMessagesController().loadSuggestedFilters();
                getMessagesController().loadUserInfo(getUserConfig().getCurrentUser(), true, classGuid);
            }
            actionBarAnimationColorFrom = arguments.getInt("actionBarColor", 0);
        } else if (chatId != 0) {
            currentChat = getMessagesController().getChat(chatId);
            if (currentChat == null) {
                final CountDownLatch countDownLatch = new CountDownLatch(1);
                getMessagesStorage().getStorageQueue().postRunnable(() -> {
                    currentChat = getMessagesStorage().getChat(chatId);
                    countDownLatch.countDown();
                });
                try {
                    countDownLatch.await();
                } catch (Exception e) {
                    FileLog.e(e);
                }
                if (currentChat != null) {
                    getMessagesController().putChat(currentChat, true);
                } else {
                    return false;
                }
            }
            if (flagSecure != null) {
                flagSecure.invalidate();
            }

            if (currentChat.megagroup) {
                getChannelParticipants(true);
            } else {
                participantsMap = null;
            }
            getNotificationCenter().addObserver(this, NotificationCenter.chatInfoDidLoad);
            getNotificationCenter().addObserver(this, NotificationCenter.chatOnlineCountDidLoad);
            getNotificationCenter().addObserver(this, NotificationCenter.groupCallUpdated);
            getNotificationCenter().addObserver(this, NotificationCenter.channelRightsUpdated);
            getNotificationCenter().addObserver(this, NotificationCenter.chatWasBoostedByUser);
            NotificationCenter.getGlobalInstance().addObserver(this, NotificationCenter.uploadStoryEnd);
            sortedUsers = new ArrayList<>();
            updateOnlineCount(true);
            if (chatInfo == null) {
                chatInfo = getMessagesController().getChatFull(chatId);
            }
            if (ChatObject.isChannel(currentChat)) {
                getMessagesController().loadFullChat(chatId, classGuid, true);
            } else if (chatInfo == null) {
                chatInfo = getMessagesStorage().loadChatInfo(chatId, false, null, false, false);
            }

            updateExceptions();
        } else {
            return false;
        }
        if (sharedMediaPreloader == null) {
            sharedMediaPreloader = new SharedMediaLayout.SharedMediaPreloader(this);
        }
        sharedMediaPreloader.addDelegate(this);

        getNotificationCenter().addObserver(this, NotificationCenter.updateInterfaces);
        getNotificationCenter().addObserver(this, NotificationCenter.didReceiveNewMessages);
        getNotificationCenter().addObserver(this, NotificationCenter.closeChats);
        getNotificationCenter().addObserver(this, NotificationCenter.topicsDidLoaded);
        getNotificationCenter().addObserver(this, NotificationCenter.updateSearchSettings);
        getNotificationCenter().addObserver(this, NotificationCenter.reloadDialogPhotos);
        getNotificationCenter().addObserver(this, NotificationCenter.storiesUpdated);
        getNotificationCenter().addObserver(this, NotificationCenter.storiesReadUpdated);
        getNotificationCenter().addObserver(this, NotificationCenter.userIsPremiumBlockedUpadted);
        getNotificationCenter().addObserver(this, NotificationCenter.currentUserPremiumStatusChanged);
        getNotificationCenter().addObserver(this, NotificationCenter.starBalanceUpdated);
        getNotificationCenter().addObserver(this, NotificationCenter.botStarsUpdated);
        getNotificationCenter().addObserver(this, NotificationCenter.botStarsTransactionsLoaded);
        getNotificationCenter().addObserver(this, NotificationCenter.dialogDeleted);
        getNotificationCenter().addObserver(this, NotificationCenter.channelRecommendationsLoaded);
        getNotificationCenter().addObserver(this, NotificationCenter.starUserGiftsLoaded);
        NotificationCenter.getGlobalInstance().addObserver(this, NotificationCenter.emojiLoaded);
        updateRowsIds();
        if (listAdapter != null) {
            listAdapter.notifyDataSetChanged();
        }

        if (arguments.containsKey("preload_messages")) {
            getMessagesController().ensureMessagesLoaded(userId, 0, null);
        }

        if (userId != 0) {
            TLRPC.User user = getMessagesController().getUser(userId);

            if (UserObject.isUserSelf(user)) {
                TL_account.getPassword req = new TL_account.getPassword();
                getConnectionsManager().sendRequest(req, (response, error) -> {
                    if (response instanceof TL_account.TL_password) {
                        currentPassword = (TL_account.TL_password) response;
                    }
                });
            }
        }

        Bulletin.addDelegate(this, new Bulletin.Delegate() {
            @Override
            public int getTopOffset(int tag) {
                return AndroidUtilities.statusBarHeight;
            }

            @Override
            public int getBottomOffset(int tag) {
                if (bottomButtonsContainer == null) {
                    return 0;
                }
                final float gifts = Utilities.clamp01(1f - Math.abs(sharedMediaLayout.getTabProgress() - SharedMediaLayout.TAB_GIFTS));
                final float stories = Utilities.clamp01(1f - Math.abs(sharedMediaLayout.getTabProgress() - SharedMediaLayout.TAB_STORIES));
                final float archivedStories = Utilities.clamp01(1f - Math.abs(sharedMediaLayout.getTabProgress() - SharedMediaLayout.TAB_ARCHIVED_STORIES));
                return lerp((int) (dp(72) - bottomButtonsContainer.getTranslationY() - archivedStories * bottomButtonContainer[1].getTranslationY() - stories * bottomButtonContainer[0].getTranslationY()), 0, gifts);
            }

            @Override
            public boolean bottomOffsetAnimated() {
                return bottomButtonsContainer == null;
            }
        });

        if (userId != 0 && UserObject.isUserSelf(getMessagesController().getUser(userId)) && !myProfile) {
            getMessagesController().getContentSettings(null);
        }

        return true;
    }

    public void updateExceptions() {
        if (!isTopic && ChatObject.isForum(currentChat)) {
            getNotificationsController().loadTopicsNotificationsExceptions(-chatId, (topics) -> {
                ArrayList<Integer> arrayList = new ArrayList<>(topics);
                for (int i = 0; i < arrayList.size(); i++) {
                    if (getMessagesController().getTopicsController().findTopic(chatId, arrayList.get(i)) == null) {
                        arrayList.remove(i);
                        i--;
                    }
                }
                notificationsExceptionTopics.clear();
                notificationsExceptionTopics.addAll(arrayList);

                if (notificationsRow >= 0 && listAdapter != null) {
                    listAdapter.notifyItemChanged(notificationsRow);
                }
            });
        }
    }

    @Override
    public boolean isActionBarCrossfadeEnabled() {
        return !isPulledDown;
    }

    @Override
    public void onFragmentDestroy() {
        super.onFragmentDestroy();
        if (sharedMediaLayout != null) {
            sharedMediaLayout.onDestroy();
        }
        if (sharedMediaPreloader != null) {
            sharedMediaPreloader.onDestroy(this);
        }
        if (sharedMediaPreloader != null) {
            sharedMediaPreloader.removeDelegate(this);
        }

        getNotificationCenter().removeObserver(this, NotificationCenter.updateInterfaces);
        getNotificationCenter().removeObserver(this, NotificationCenter.closeChats);
        getNotificationCenter().removeObserver(this, NotificationCenter.didReceiveNewMessages);
        getNotificationCenter().removeObserver(this, NotificationCenter.topicsDidLoaded);
        getNotificationCenter().removeObserver(this, NotificationCenter.updateSearchSettings);
        getNotificationCenter().removeObserver(this, NotificationCenter.reloadDialogPhotos);
        getNotificationCenter().removeObserver(this, NotificationCenter.storiesUpdated);
        getNotificationCenter().removeObserver(this, NotificationCenter.storiesReadUpdated);
        getNotificationCenter().removeObserver(this, NotificationCenter.userIsPremiumBlockedUpadted);
        getNotificationCenter().removeObserver(this, NotificationCenter.currentUserPremiumStatusChanged);
        getNotificationCenter().removeObserver(this, NotificationCenter.starBalanceUpdated);
        getNotificationCenter().removeObserver(this, NotificationCenter.botStarsUpdated);
        getNotificationCenter().removeObserver(this, NotificationCenter.botStarsTransactionsLoaded);
        getNotificationCenter().removeObserver(this, NotificationCenter.dialogDeleted);
        getNotificationCenter().removeObserver(this, NotificationCenter.channelRecommendationsLoaded);
        getNotificationCenter().removeObserver(this, NotificationCenter.starUserGiftsLoaded);
        NotificationCenter.getGlobalInstance().removeObserver(this, NotificationCenter.emojiLoaded);
        if (avatarsViewPager != null) {
            avatarsViewPager.onDestroy();
        }
        if (userId != 0) {
            getNotificationCenter().removeObserver(this, NotificationCenter.newSuggestionsAvailable);
            getNotificationCenter().removeObserver(this, NotificationCenter.contactsDidLoad);
            getNotificationCenter().removeObserver(this, NotificationCenter.encryptedChatCreated);
            getNotificationCenter().removeObserver(this, NotificationCenter.encryptedChatUpdated);
            getNotificationCenter().removeObserver(this, NotificationCenter.blockedUsersDidLoad);
            getNotificationCenter().removeObserver(this, NotificationCenter.botInfoDidLoad);
            getNotificationCenter().removeObserver(this, NotificationCenter.userInfoDidLoad);
            getNotificationCenter().removeObserver(this, NotificationCenter.privacyRulesUpdated);
            NotificationCenter.getGlobalInstance().removeObserver(this, NotificationCenter.reloadInterface);
            getMessagesController().cancelLoadFullUser(userId);
        } else if (chatId != 0) {
            NotificationCenter.getGlobalInstance().removeObserver(this, NotificationCenter.uploadStoryEnd);
            getNotificationCenter().removeObserver(this, NotificationCenter.chatWasBoostedByUser);
            getNotificationCenter().removeObserver(this, NotificationCenter.chatInfoDidLoad);
            getNotificationCenter().removeObserver(this, NotificationCenter.chatOnlineCountDidLoad);
            getNotificationCenter().removeObserver(this, NotificationCenter.groupCallUpdated);
            getNotificationCenter().removeObserver(this, NotificationCenter.channelRightsUpdated);
        }
        if (avatarImage != null) {
            avatarImage.setImageDrawable(null);
        }
        if (imageUpdater != null) {
            imageUpdater.clear();
        }
        if (pinchToZoomHelper != null) {
            pinchToZoomHelper.clear();
        }
        if (birthdayFetcher != null && createdBirthdayFetcher) {
            birthdayFetcher.detach(true);
            birthdayFetcher = null;
        }

        if (applyBulletin != null) {
            Runnable runnable = applyBulletin;
            applyBulletin = null;
            AndroidUtilities.runOnUIThread(runnable);
        }
    }

    @Override
    public ActionBar createActionBar(Context context) {
        BaseFragment lastFragment = parentLayout.getLastFragment();
        if (lastFragment instanceof ChatActivity && ((ChatActivity) lastFragment).themeDelegate != null && ((ChatActivity) lastFragment).themeDelegate.getCurrentTheme() != null) {
            resourcesProvider = lastFragment.getResourceProvider();
        }
        ActionBar actionBar = new ActionBar(context, resourcesProvider) {
            @Override
            public boolean onTouchEvent(MotionEvent event) {
                avatarContainer.getHitRect(rect);
                if (rect.contains((int) event.getX(), (int) event.getY())) {
                    return false;
                }
                return super.onTouchEvent(event);
            }

            @Override
            public void setItemsColor(int color, boolean isActionMode) {
                super.setItemsColor(color, isActionMode);
                if (!isActionMode && ttlIconView != null) {
                    ttlIconView.setColorFilter(new PorterDuffColorFilter(color, PorterDuff.Mode.MULTIPLY));
                }
            }

            @Override
            protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
                super.onLayout(changed, left, top, right, bottom);
                updateStoriesViewBounds(false);
            }
        };
        actionBar.setForceSkipTouches(true);
        actionBar.setBackgroundColor(Color.TRANSPARENT);
        actionBar.setItemsBackgroundColor(peerColor != null ? 0x20ffffff : getThemedColor(Theme.key_avatar_actionBarSelectorBlue), false);
        actionBar.setItemsColor(getThemedColor(Theme.key_actionBarDefaultIcon), false);
        actionBar.setItemsColor(getThemedColor(Theme.key_actionBarDefaultIcon), true);
        actionBar.setBackButtonDrawable(new BackDrawable(false));
        actionBar.setCastShadows(false);
        actionBar.setAddToContainer(false);
        actionBar.setClipContent(true);
        actionBar.setOccupyStatusBar(Build.VERSION.SDK_INT >= 21 && !AndroidUtilities.isTablet() && !inBubbleMode);
        ImageView backButton = actionBar.getBackButton();
        backButton.setOnLongClickListener(e -> {
            ActionBarPopupWindow menu = BackButtonMenu.show(this, backButton, getDialogId(), getTopicId(), resourcesProvider);
            if (menu != null) {
                menu.setOnDismissListener(() -> dimBehindView(false));
                dimBehindView(backButton, 0.3f);
                if (undoView != null) {
                    undoView.hide(true, 1);
                }
                return true;
            } else {
                return false;
            }
        });
        return actionBar;
    }

    @Override
    public void setParentLayout(INavigationLayout layout) {
        super.setParentLayout(layout);

        if (flagSecure != null) {
            flagSecure.detach();
            flagSecure = null;
        }
        if (layout != null && layout.getParentActivity() != null) {
            flagSecure = new FlagSecureReason(layout.getParentActivity().getWindow(), () -> currentEncryptedChat != null || getMessagesController().isChatNoForwards(currentChat));
        }
    }

    @Override
    public View createView(Context context) {
        Theme.createProfileResources(context);
        Theme.createChatResources(context, false);
        BaseFragment lastFragment = parentLayout.getLastFragment();
        if (lastFragment instanceof ChatActivity && ((ChatActivity) lastFragment).themeDelegate != null && ((ChatActivity) lastFragment).themeDelegate.getCurrentTheme() != null) {
            resourcesProvider = lastFragment.getResourceProvider();
        }
        searchTransitionOffset = 0;
        searchTransitionProgress = 1f;
        searchMode = false;
        hasOwnBackground = true;
        extraHeight = AndroidUtilities.dp(88f);
        actionBar.setActionBarMenuOnItemClick(new ActionBar.ActionBarMenuOnItemClick() {@Override public void onItemClick(final int id) { handleMenuAction(ProfileActivity.this, id); }});

        if (sharedMediaLayout != null) {
            sharedMediaLayout.onDestroy();
        }
        final long did;
        if (dialogId != 0) {
            did = dialogId;
        } else if (userId != 0) {
            did = userId;
        } else {
            did = -chatId;
        }

        fragmentView = new NestedFrameLayout(this, context) {

            @Override
            public boolean dispatchTouchEvent(MotionEvent ev) {
                if (pinchToZoomHelper.isInOverlayMode()) {
                    return pinchToZoomHelper.onTouchEvent(ev);
                }
                if (sharedMediaLayout != null && sharedMediaLayout.isInFastScroll() && sharedMediaLayout.isPinnedToTop()) {
                    return sharedMediaLayout.dispatchFastScrollEvent(ev);
                }
                if (sharedMediaLayout != null && sharedMediaLayout.checkPinchToZoom(ev)) {
                    return true;
                }
                return super.dispatchTouchEvent(ev);
            }

            private boolean ignoreLayout;
            private Paint grayPaint = new Paint();

            @Override
            public boolean hasOverlappingRendering() {
                return false;
            }

            private boolean wasPortrait;

            @Override
            protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
                final int actionBarHeight = ActionBar.getCurrentActionBarHeight() + (actionBar.getOccupyStatusBar() ? AndroidUtilities.statusBarHeight : 0);
                if (listView != null) {
                    LayoutParams layoutParams = (LayoutParams) listView.getLayoutParams();
                    if (layoutParams.topMargin != actionBarHeight) {
                        layoutParams.topMargin = actionBarHeight;
                    }
                }
                if (searchListView != null) {
                    LayoutParams layoutParams = (LayoutParams) searchListView.getLayoutParams();
                    if (layoutParams.topMargin != actionBarHeight) {
                        layoutParams.topMargin = actionBarHeight;
                    }
                }

                int height = MeasureSpec.getSize(heightMeasureSpec);
                super.onMeasure(widthMeasureSpec, MeasureSpec.makeMeasureSpec(height, MeasureSpec.EXACTLY));

                boolean changed = false;
                if (lastMeasuredContentWidth != getMeasuredWidth() || lastMeasuredContentHeight != getMeasuredHeight()) {
                    changed = lastMeasuredContentWidth != 0 && lastMeasuredContentWidth != getMeasuredWidth();
                    listContentHeight = 0;
                    int count = listAdapter.getItemCount();
                    lastMeasuredContentWidth = getMeasuredWidth();
                    lastMeasuredContentHeight = getMeasuredHeight();
                    int ws = MeasureSpec.makeMeasureSpec(getMeasuredWidth(), MeasureSpec.EXACTLY);
                    int hs = MeasureSpec.makeMeasureSpec(listView.getMeasuredHeight(), MeasureSpec.UNSPECIFIED);
                    positionToOffset.clear();
                    for (int i = 0; i < count; i++) {
                        int type = listAdapter.getItemViewType(i);
                        positionToOffset.put(i, listContentHeight);
                        if (type == ProfileAdapter.VIEW_TYPE_SHARED_MEDIA) {
                            listContentHeight += listView.getMeasuredHeight();
                        } else {
                            RecyclerView.ViewHolder holder = listAdapter.createViewHolder(null, type);
                            listAdapter.onBindViewHolder(holder, i);
                            holder.itemView.measure(ws, hs);
                            listContentHeight += holder.itemView.getMeasuredHeight();
                        }
                    }

                    if (emptyView != null) {
                        ((LayoutParams) emptyView.getLayoutParams()).topMargin = AndroidUtilities.dp(88) + AndroidUtilities.statusBarHeight;
                    }
                }

                if (previousTransitionFragment != null) {
                    nameTextView[0].setRightPadding(nameTextView[0].getMeasuredWidth() - previousTransitionFragment.getAvatarContainer().getTitleTextView().getMeasuredWidth());
                }

                if (!fragmentOpened && (expandPhoto || openAnimationInProgress && playProfileAnimation == 2)) {
                    ignoreLayout = true;

                    if (expandPhoto) {
                        if (searchItem != null) {
                            searchItem.setAlpha(0.0f);
                            searchItem.setEnabled(false);
                            searchItem.setVisibility(GONE);
                        }
                        nameTextView[1].setTextColor(Color.WHITE);
                        nameTextView[1].setPivotY(nameTextView[1].getMeasuredHeight());
                        nameTextView[1].setScaleX(1.67f);
                        nameTextView[1].setScaleY(1.67f);
                        if (scamDrawable != null) {
                            scamDrawable.setColor(Color.argb(179, 255, 255, 255));
                        }
                        if (lockIconDrawable != null) {
                            lockIconDrawable.setColorFilter(Color.WHITE, PorterDuff.Mode.MULTIPLY);
                        }
                        if (verifiedCrossfadeDrawable[0] != null) {
                            verifiedCrossfadeDrawable[0].setProgress(1f);
                        }
                        if (verifiedCrossfadeDrawable[1] != null) {
                            verifiedCrossfadeDrawable[1].setProgress(1f);
                        }
                        if (premiumCrossfadeDrawable[0] != null) {
                            premiumCrossfadeDrawable[0].setProgress(1f);
                        }
                        if (premiumCrossfadeDrawable[1] != null) {
                            premiumCrossfadeDrawable[1].setProgress(1f);
                        }
                        updateEmojiStatusDrawableColor(1f);
                        onlineTextView[1].setTextColor(0xB3FFFFFF);
                        actionBar.setItemsBackgroundColor(Theme.ACTION_BAR_WHITE_SELECTOR_COLOR, false);
                        actionBar.setItemsColor(Color.WHITE, false);
                        overlaysView.setOverlaysVisible();
                        overlaysView.setAlphaValue(1.0f, false);
                        avatarImage.setForegroundAlpha(1.0f);
                        avatarContainer.setVisibility(View.GONE);
                        avatarsViewPager.resetCurrentItem();
                        avatarsViewPager.setVisibility(View.VISIBLE);
                        if (showStatusButton != null) {
                            showStatusButton.setBackgroundColor(0x23ffffff);
                        }
                        if (storyView != null) {
                            storyView.setExpandProgress(1f);
                        }
                        if (giftsView != null) {
                            giftsView.setExpandProgress(1f);
                        }
                        expandPhoto = false;
                        updateCollectibleHint();
                    }

                    allowPullingDown = true;
                    isPulledDown = true;
                    NotificationCenter.getGlobalInstance().postNotificationName(NotificationCenter.needCheckSystemBarColors, true);
                    if (otherItem != null) {
                        if (!getMessagesController().isChatNoForwards(currentChat)) {
                            otherItem.showSubItem(gallery_menu_save);
                        } else {
                            otherItem.hideSubItem(gallery_menu_save);
                        }
                        if (imageUpdater != null) {
                            otherItem.showSubItem(edit_avatar);
                            otherItem.showSubItem(delete_avatar);
                            otherItem.hideSubItem(logout);
                        }
                    }
                    currentExpanAnimatorFracture = 1.0f;

                    int paddingTop;
                    int paddingBottom;
                    if (isInLandscapeMode) {
                        paddingTop = AndroidUtilities.dp(88f);
                        paddingBottom = 0;
                    } else {
                        paddingTop = listView.getMeasuredWidth();
                        paddingBottom = Math.max(0, getMeasuredHeight() - (listContentHeight + AndroidUtilities.dp(88) + actionBarHeight));
                    }
                    if (banFromGroup != 0) {
                        paddingBottom += AndroidUtilities.dp(48);
                        listView.setBottomGlowOffset(AndroidUtilities.dp(48));
                    } else {
                        listView.setBottomGlowOffset(0);
                    }
                    initialAnimationExtraHeight = paddingTop - actionBarHeight;
                    if (playProfileAnimation == 0) {
                        extraHeight = initialAnimationExtraHeight;
                    }
                    layoutManager.scrollToPositionWithOffset(0, -actionBarHeight);
                    listView.setPadding(0, paddingTop, 0, paddingBottom);
                    measureChildWithMargins(listView, widthMeasureSpec, 0, heightMeasureSpec, 0);
                    listView.layout(0, actionBarHeight, listView.getMeasuredWidth(), actionBarHeight + listView.getMeasuredHeight());
                    ignoreLayout = false;
                } else if (fragmentOpened && !openAnimationInProgress && !firstLayout) {
                    ignoreLayout = true;

                    int paddingTop;
                    int paddingBottom;
                    if (isInLandscapeMode || AndroidUtilities.isTablet()) {
                        paddingTop = AndroidUtilities.dp(88f);
                        paddingBottom = 0;
                    } else {
                        paddingTop = listView.getMeasuredWidth();
                        paddingBottom = Math.max(0, getMeasuredHeight() - (listContentHeight + AndroidUtilities.dp(88) + actionBarHeight));
                    }
                    if (banFromGroup != 0) {
                        paddingBottom += AndroidUtilities.dp(48);
                        listView.setBottomGlowOffset(AndroidUtilities.dp(48));
                    } else {
                        listView.setBottomGlowOffset(0);
                    }
                    int currentPaddingTop = listView.getPaddingTop();
                    View view = null;
                    int pos = RecyclerView.NO_POSITION;
                    for (int i = 0; i < listView.getChildCount(); i++) {
                        int p = listView.getChildAdapterPosition(listView.getChildAt(i));
                        if (p != RecyclerView.NO_POSITION) {
                            view = listView.getChildAt(i);
                            pos = p;
                            break;
                        }
                    }
                    if (view == null) {
                        view = listView.getChildAt(0);
                        if (view != null) {
                            RecyclerView.ViewHolder holder = listView.findContainingViewHolder(view);
                            pos = holder.getAdapterPosition();
                            if (pos == RecyclerView.NO_POSITION) {
                                pos = holder.getPosition();
                            }
                        }
                    }

                    int top = paddingTop;
                    if (view != null) {
                        top = view.getTop();
                    }
                    boolean layout = false;
                    if ((actionBar.isSearchFieldVisible() || openSimilar) && sharedMediaRow >= 0) {
                        layoutManager.scrollToPositionWithOffset(sharedMediaRow, -paddingTop);
                        layout = true;
                    } else if (invalidateScroll || currentPaddingTop != paddingTop) {
                        if (savedScrollPosition >= 0) {
                            layoutManager.scrollToPositionWithOffset(savedScrollPosition, savedScrollOffset - paddingTop);
                        } else if ((!changed || !allowPullingDown) && view != null) {
                            if (pos == 0 && !allowPullingDown && top > AndroidUtilities.dp(88)) {
                                top = AndroidUtilities.dp(88);
                            }
                            layoutManager.scrollToPositionWithOffset(pos, top - paddingTop);
                            layout = true;
                        } else {
                            layoutManager.scrollToPositionWithOffset(0, AndroidUtilities.dp(88) - paddingTop);
                        }
                    }
                    if (currentPaddingTop != paddingTop || listView.getPaddingBottom() != paddingBottom) {
                        listView.setPadding(0, paddingTop, 0, paddingBottom);
                        layout = true;
                    }
                    if (layout) {
                        measureChildWithMargins(listView, widthMeasureSpec, 0, heightMeasureSpec, 0);
                        try {
                            listView.layout(0, actionBarHeight, listView.getMeasuredWidth(), actionBarHeight + listView.getMeasuredHeight());
                        } catch (Exception e) {
                            FileLog.e(e);
                        }
                    }
                    ignoreLayout = false;
                }

                boolean portrait = height > MeasureSpec.getSize(widthMeasureSpec);
                if (portrait != wasPortrait) {
                    post(() -> {
                        if (selectAnimatedEmojiDialog != null) {
                            selectAnimatedEmojiDialog.dismiss();
                            selectAnimatedEmojiDialog = null;
                        }
                    });
                    wasPortrait = portrait;
                }

                if (searchItem != null && qrItem != null) {
                    float translation = AndroidUtilities.dp(48) * currentExpandAnimatorValue;
                   // qrItem.setTranslationX(translation);
                }
            }

            @Override
            protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
                super.onLayout(changed, left, top, right, bottom);
                savedScrollPosition = -1;
                firstLayout = false;
                invalidateScroll = false;
                checkListViewScroll();
            }

            @Override
            public void requestLayout() {
                if (ignoreLayout) {
                    return;
                }
                super.requestLayout();
            }

            private final ArrayList<View> sortedChildren = new ArrayList<>();
            private final Comparator<View> viewComparator = (view, view2) -> (int) (view.getY() - view2.getY());


            @Override
            protected void dispatchDraw(Canvas canvas) {
                whitePaint.setColor(getThemedColor(Theme.key_windowBackgroundWhite));
                if (listView.getVisibility() == VISIBLE) {
                    grayPaint.setColor(getThemedColor(Theme.key_windowBackgroundGray));
                    if (transitionAnimationInProress) {
                        whitePaint.setAlpha((int) (255 * listView.getAlpha()));
                    }
                    if (transitionAnimationInProress) {
                        grayPaint.setAlpha((int) (255 * listView.getAlpha()));
                    }

                    int count = listView.getChildCount();
                    sortedChildren.clear();
                    boolean hasRemovingItems = false;
                    for (int i = 0; i < count; i++) {
                        View child = listView.getChildAt(i);
                        if (listView.getChildAdapterPosition(child) != RecyclerView.NO_POSITION) {
                            sortedChildren.add(listView.getChildAt(i));
                        } else {
                            hasRemovingItems = true;
                        }
                    }
                    Collections.sort(sortedChildren, viewComparator);
                    boolean hasBackground = false;
                    float lastY = listView.getY();
                    count = sortedChildren.size();
                    if (!openAnimationInProgress && count > 0 && !hasRemovingItems) {
                        lastY += sortedChildren.get(0).getY();
                    }
                    float alpha = 1f;
                    for (int i = 0; i < count; i++) {
                        View child = sortedChildren.get(i);
                        boolean currentHasBackground = child.getBackground() != null;
                        int currentY = (int) (listView.getY() + child.getY());
                        if (hasBackground == currentHasBackground) {
                            if (child.getAlpha() == 1f) {
                                alpha = 1f;
                            }
                            continue;
                        }
                        if (hasBackground) {
                            canvas.drawRect(listView.getX(), lastY, listView.getX() + listView.getMeasuredWidth(), currentY, grayPaint);
                        } else {
                            if (alpha != 1f) {
                                canvas.drawRect(listView.getX(), lastY, listView.getX() + listView.getMeasuredWidth(), currentY, grayPaint);
                                whitePaint.setAlpha((int) (255 * alpha));
                                canvas.drawRect(listView.getX(), lastY, listView.getX() + listView.getMeasuredWidth(), currentY, whitePaint);
                                whitePaint.setAlpha(255);
                            } else {
                                canvas.drawRect(listView.getX(), lastY, listView.getX() + listView.getMeasuredWidth(), currentY, whitePaint);
                            }
                        }
                        hasBackground = currentHasBackground;
                        lastY = currentY;
                        alpha = child.getAlpha();
                    }

                    if (hasBackground) {
                        canvas.drawRect(listView.getX(), lastY, listView.getX() + listView.getMeasuredWidth(), listView.getBottom(), grayPaint);
                    } else {
                        if (alpha != 1f) {
                            canvas.drawRect(listView.getX(), lastY, listView.getX() + listView.getMeasuredWidth(), listView.getBottom(), grayPaint);
                            whitePaint.setAlpha((int) (255 * alpha));
                            canvas.drawRect(listView.getX(), lastY, listView.getX() + listView.getMeasuredWidth(), listView.getBottom(), whitePaint);
                            whitePaint.setAlpha(255);
                        } else {
                            canvas.drawRect(listView.getX(), lastY, listView.getX() + listView.getMeasuredWidth(), listView.getBottom(), whitePaint);
                        }
                    }
                } else {
                    int top = searchListView.getTop();
                    canvas.drawRect(0, top + extraHeight + searchTransitionOffset, getMeasuredWidth(), top + getMeasuredHeight(), whitePaint);
                }
                super.dispatchDraw(canvas);
                if (profileTransitionInProgress && parentLayout.getFragmentStack().size() > 1) {
                    BaseFragment fragment = parentLayout.getFragmentStack().get(parentLayout.getFragmentStack().size() - 2);
                    if (fragment instanceof ChatActivity) {
                        ChatActivity chatActivity = (ChatActivity) fragment;
                        FragmentContextView fragmentContextView = chatActivity.getFragmentContextView();

                        if (fragmentContextView != null && fragmentContextView.isCallStyle()) {
                            float progress = extraHeight / AndroidUtilities.dpf2(fragmentContextView.getStyleHeight());
                            if (progress > 1f) {
                                progress = 1f;
                            }
                            canvas.save();
                            canvas.translate(fragmentContextView.getX(), fragmentContextView.getY());
                            fragmentContextView.setDrawOverlay(true);
                            fragmentContextView.setCollapseTransition(true, extraHeight, progress);
                            fragmentContextView.draw(canvas);
                            fragmentContextView.setCollapseTransition(false, extraHeight, progress);
                            fragmentContextView.setDrawOverlay(false);
                            canvas.restore();
                        }
                    }
                }

                if (scrimPaint.getAlpha() > 0) {
                    canvas.drawRect(0, 0, getWidth(), getHeight(), scrimPaint);
                }
                if (scrimView != null) {
                    int c = canvas.save();
                    canvas.translate(scrimView.getLeft(), scrimView.getTop());
                    if (scrimView == actionBar.getBackButton()) {
                        int r = Math.max(scrimView.getMeasuredWidth(), scrimView.getMeasuredHeight()) / 2;
                        int wasAlpha = actionBarBackgroundPaint.getAlpha();
                        actionBarBackgroundPaint.setAlpha((int) (wasAlpha * (scrimPaint.getAlpha() / 255f) / 0.3f));
                        canvas.drawCircle(r, r, r * 0.7f, actionBarBackgroundPaint);
                        actionBarBackgroundPaint.setAlpha(wasAlpha);
                    }
                    scrimView.draw(canvas);
                    canvas.restoreToCount(c);
                }
                if (blurredView != null && blurredView.getVisibility() == View.VISIBLE) {
                    if (blurredView.getAlpha() != 1f) {
                        if (blurredView.getAlpha() != 0) {
                            canvas.saveLayerAlpha(blurredView.getLeft(), blurredView.getTop(), blurredView.getRight(), blurredView.getBottom(), (int) (255 * blurredView.getAlpha()), Canvas.ALL_SAVE_FLAG);
                            canvas.translate(blurredView.getLeft(), blurredView.getTop());
                            blurredView.draw(canvas);
                            canvas.restore();
                        }
                    } else {
                        blurredView.draw(canvas);
                    }
                }
            }

            @Override
            protected boolean drawChild(Canvas canvas, View child, long drawingTime) {
                if (pinchToZoomHelper.isInOverlayMode() && (child == avatarContainer2 || child == actionBar || child == writeButton)) {
                    return true;
                }
                if (child == blurredView) {
                    return true;
                }
                return super.drawChild(canvas, child, drawingTime);
            }

            @Override
            protected void onAttachedToWindow() {
                super.onAttachedToWindow();
                fragmentViewAttached = true;
                for (int i = 0; i < emojiStatusDrawable.length; i++) {
                    if (emojiStatusDrawable[i] != null) {
                        emojiStatusDrawable[i].attach();
                    }
                }
                for (int i = 0; i < botVerificationDrawable.length; ++i) {
                    if (botVerificationDrawable[i] != null) {
                        botVerificationDrawable[i].attach();
                    }
                }
            }

            @Override
            protected void onDetachedFromWindow() {
                super.onDetachedFromWindow();
                fragmentViewAttached = false;
                for (int i = 0; i < emojiStatusDrawable.length; i++) {
                    if (emojiStatusDrawable[i] != null) {
                        emojiStatusDrawable[i].detach();
                    }
                }
                for (int i = 0; i < botVerificationDrawable.length; ++i) {
                    if (botVerificationDrawable[i] != null) {
                        botVerificationDrawable[i].detach();
                    }
                }
            }
        };

        if (myProfile) {
            bottomButtonsContainer = new FrameLayout(context);

            bottomButtonContainer = new FrameLayout[2];
            bottomButton = new ButtonWithCounterView[2];
            for (int a = 0; a < 2; ++a) {
                bottomButtonContainer[a] = new FrameLayout(context);
                bottomButtonContainer[a].setBackgroundColor(getThemedColor(Theme.key_windowBackgroundWhite));

                View shadow = new View(context);
                shadow.setBackgroundColor(getThemedColor(Theme.key_windowBackgroundGray));
                bottomButtonContainer[a].addView(shadow, LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, 1 / AndroidUtilities.density, Gravity.TOP | Gravity.FILL_HORIZONTAL));

                bottomButton[a] = new ButtonWithCounterView(context, resourcesProvider);
                if (a == 0) {
                    bottomButtonPostText = new SpannableStringBuilder("c");
                    bottomButtonPostText.setSpan(new ColoredImageSpan(R.drawable.filled_premium_camera), 0, 1, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                    bottomButtonPostText.append("  ").append(getString(R.string.StoriesAddPost));
                    bottomButton[a].setText(bottomButtonPostText, false);
                } else {
                    bottomButton[a].setText(getString(R.string.StorySave), false);
                }
                final int finalA = a;
                bottomButton[a].setOnClickListener(v -> {
                    if (finalA == 0 && !sharedMediaLayout.isActionModeShown()) {
                        if (!getMessagesController().storiesEnabled()) {
                            showDialog(new PremiumFeatureBottomSheet(this, PremiumPreviewFragment.PREMIUM_FEATURE_STORIES, true));
                            return;
                        }
                        getMessagesController().getMainSettings().edit().putBoolean("story_keep", true).apply();
                        StoryRecorder.getInstance(getParentActivity(), getCurrentAccount())
                                .closeToWhenSent(new StoryRecorder.ClosingViewProvider() {
                                    @Override
                                    public void preLayout(long dialogId, Runnable runnable) {
                                        avatarImage.setHasStories(needInsetForStories());
                                        if (dialogId == getDialogId()) {
                                            collapseAvatarInstant();
                                        }
                                        AndroidUtilities.runOnUIThread(runnable, 30);
                                    }

                                    @Override
                                    public StoryRecorder.SourceView getView(long dialogId) {
                                        if (dialogId != getDialogId()) {
                                            return null;
                                        }
                                        updateAvatarRoundRadius();
                                        return StoryRecorder.SourceView.fromAvatarImage(avatarImage, ChatObject.isForum(currentChat));
                                    }
                                })
                                .open(null);
                    } else {
                        final long dialogId = getUserConfig().getClientUserId();
                        if (applyBulletin != null) {
                            applyBulletin.run();
                            applyBulletin = null;
                        }
                        Bulletin.hideVisible();
                        boolean pin = sharedMediaLayout.getClosestTab() == SharedMediaLayout.TAB_ARCHIVED_STORIES;
                        int count = 0;
                        ArrayList<TL_stories.StoryItem> storyItems = new ArrayList<>();
                        SparseArray<MessageObject> actionModeMessageObjects = sharedMediaLayout.getActionModeSelected();
                        if (actionModeMessageObjects != null) {
                            for (int i = 0; i < actionModeMessageObjects.size(); ++i) {
                                MessageObject messageObject = actionModeMessageObjects.valueAt(i);
                                if (messageObject.storyItem != null) {
                                    storyItems.add(messageObject.storyItem);
                                    count++;
                                }
                            }
                        }
                        sharedMediaLayout.closeActionMode(false);
                        if (pin) {
                            sharedMediaLayout.scrollToPage(SharedMediaLayout.TAB_STORIES);
                        }
                        if (storyItems.isEmpty()) {
                            return;
                        }
                        boolean[] pastValues = new boolean[storyItems.size()];
                        for (int i = 0; i < storyItems.size(); ++i) {
                            TL_stories.StoryItem storyItem = storyItems.get(i);
                            pastValues[i] = storyItem.pinned;
                            storyItem.pinned = pin;
                        }
                        getMessagesController().getStoriesController().updateStoriesInLists(dialogId, storyItems);
                        final boolean[] undone = new boolean[] { false };
                        applyBulletin = () -> {
                            getMessagesController().getStoriesController().updateStoriesPinned(dialogId, storyItems, pin, null);
                        };
                        final Runnable undo = () -> {
                            undone[0] = true;
                            AndroidUtilities.cancelRunOnUIThread(applyBulletin);
                            for (int i = 0; i < storyItems.size(); ++i) {
                                TL_stories.StoryItem storyItem = storyItems.get(i);
                                storyItem.pinned = pastValues[i];
                            }
                            getMessagesController().getStoriesController().updateStoriesInLists(dialogId, storyItems);
                        };
                        Bulletin bulletin;
                        if (pin) {
                            bulletin = BulletinFactory.of(this).createSimpleBulletin(R.raw.contact_check, LocaleController.formatPluralString("StorySavedTitle", count), LocaleController.getString(R.string.StorySavedSubtitle), LocaleController.getString(R.string.Undo), undo).show();
                        } else {
                            bulletin = BulletinFactory.of(this).createSimpleBulletin(R.raw.chats_archived, LocaleController.formatPluralString("StoryArchived", count), LocaleController.getString(R.string.Undo), Bulletin.DURATION_PROLONG, undo).show();
                        }
                        bulletin.setOnHideListener(() -> {
                            if (!undone[0] && applyBulletin != null) {
                                applyBulletin.run();
                            }
                            applyBulletin = null;
                        });
                    }
                });
                bottomButtonContainer[a].addView(bottomButton[a], LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, 48, Gravity.BOTTOM | Gravity.FILL_HORIZONTAL, 12, 12, 12, 12));

                bottomButtonsContainer.addView(bottomButtonContainer[a], LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, LayoutHelper.MATCH_PARENT, Gravity.BOTTOM | Gravity.FILL_HORIZONTAL));
                if (a == 1 || !getMessagesController().storiesEnabled()) {
                    bottomButtonContainer[a].setTranslationY(dp(72));
                }
            }
        }

        final ArrayList<Integer> users = chatInfo != null && chatInfo.participants != null && chatInfo.participants.participants.size() > 5 ? sortedUsers : null;
        int initialTab = -1;
        if (openCommonChats) {
            initialTab = SharedMediaLayout.TAB_COMMON_GROUPS;
        } else if (openGifts && (userInfo != null && userInfo.stargifts_count > 0 || chatInfo != null && chatInfo.stargifts_count > 0)) {
            initialTab = SharedMediaLayout.TAB_GIFTS;
            openedGifts = true;
        } else if (openSimilar) {
            initialTab = SharedMediaLayout.TAB_RECOMMENDED_CHANNELS;
        } else if (users != null) {
            initialTab = SharedMediaLayout.TAB_GROUPUSERS;
        }
        sharedMediaLayout = new SharedMediaLayout(context, did, sharedMediaPreloader, userInfo != null ? userInfo.common_chats_count : 0, sortedUsers, chatInfo, userInfo, initialTab, this, this, SharedMediaLayout.VIEW_TYPE_PROFILE_ACTIVITY, resourcesProvider) {
            @Override
            protected int processColor(int color) {
                return dontApplyPeerColor(color, false);
            }
            @Override
            protected void onSelectedTabChanged() {
                updateSelectedMediaTabText();
            }
            @Override
            protected boolean includeSavedDialogs() {
                return dialogId == getUserConfig().getClientUserId() && !saved;
            }
            @Override
            protected boolean isSelf() {
                return myProfile;
            }

            @Override
            protected boolean isStoriesView() {
                return myProfile;
            }

            @Override
            protected void onSearchStateChanged(boolean expanded) {
                AndroidUtilities.removeAdjustResize(getParentActivity(), classGuid);

                listView.stopScroll();
                avatarContainer2.setPivotY(avatarContainer.getPivotY() + avatarContainer.getMeasuredHeight() / 2f);
                avatarContainer2.setPivotX(avatarContainer2.getMeasuredWidth() / 2f);
                AndroidUtilities.updateViewVisibilityAnimated(avatarContainer2, !expanded, 0.95f, true);

                callItem.setVisibility(expanded || !callItemVisible ? GONE : INVISIBLE);
                videoCallItem.setVisibility(expanded || !videoCallItemVisible ? GONE : INVISIBLE);
                editItem.setVisibility(expanded || !editItemVisible ? GONE : INVISIBLE);
                otherItem.setVisibility(expanded ? GONE : INVISIBLE);
                if (qrItem != null) {
                    qrItem.setVisibility(expanded ? GONE : INVISIBLE);
                }
                updateStoriesViewBounds(false);
            }

            @Override
            protected boolean onMemberClick(TLRPC.ChatParticipant participant, boolean isLong, View view) {
                return ProfileActivity.this.onMemberClick(participant, isLong, view);
            }

            @Override
            protected void drawBackgroundWithBlur(Canvas canvas, float y, Rect rectTmp2, Paint backgroundPaint) {
                contentView.drawBlurRect(canvas, listView.getY() + getY() + y, rectTmp2, backgroundPaint, true);
            }

            @Override
            protected void invalidateBlur() {
                if (contentView != null) {
                    contentView.invalidateBlur();
                }
            }

            @Override
            protected int getInitialTab() {
                return TAB_STORIES;
            }

            @Override
            protected void showActionMode(boolean show) {
                super.showActionMode(show);
                if (myProfile) {
                    disableScroll(show);

                    int a = getSelectedTab() - SharedMediaLayout.TAB_STORIES;
                    if (a < 0 || a > 1) return;
                    bottomButtonContainer[a]
                        .animate()
                        .translationY(show || a == 0 && MessagesController.getInstance(currentAccount).storiesEnabled() ? 0 : dp(72))
                        .setDuration(320)
                        .setInterpolator(CubicBezierInterpolator.EASE_OUT_QUINT)
                        .setUpdateListener(anm -> updateBottomButtonY())
                        .start();
                }
            }
            @Override
            protected void onTabProgress(float progress) {
                super.onTabProgress(progress);
                if (myProfile) {
                    int width = sharedMediaLayout == null ? AndroidUtilities.displaySize.x : sharedMediaLayout.getMeasuredWidth();
                    if (bottomButtonContainer[0] != null)
                        bottomButtonContainer[0].setTranslationX((SharedMediaLayout.TAB_STORIES - progress) * width);
                    if (bottomButtonContainer[1] != null)
                        bottomButtonContainer[1].setTranslationX((SharedMediaLayout.TAB_ARCHIVED_STORIES - progress) * width);
                    updateBottomButtonY();
                }
            }

            @Override
            protected void onActionModeSelectedUpdate(SparseArray<MessageObject> messageObjects) {
                super.onActionModeSelectedUpdate(messageObjects);
                if (myProfile) {
                    final int count = messageObjects.size();
                    int a = getSelectedTab() - SharedMediaLayout.TAB_STORIES;
                    if (a < 0 || a > 1) return;
                    if (a == 0) {
                        bottomButton[a].setText(count > 0 || !MessagesController.getInstance(currentAccount).storiesEnabled() ? formatPluralString("ArchiveStories", count) : bottomButtonPostText, true);
                    }
                    bottomButton[a].setCount(count, true);
                }
            }

            @Override
            public void openStoryRecorder() {
                StoryRecorder.getInstance(getParentActivity(), currentAccount)
                    .selectedPeerId(getDialogId())
                    .canChangePeer(false)
                    .closeToWhenSent(new StoryRecorder.ClosingViewProvider() {
                        @Override
                        public void preLayout(long dialogId, Runnable runnable) {
                            avatarImage.setHasStories(needInsetForStories());
                            if (dialogId == getDialogId()) {
                                collapseAvatarInstant();
                            }
                            AndroidUtilities.runOnUIThread(runnable, 30);
                        }

                        @Override
                        public StoryRecorder.SourceView getView(long dialogId) {
                            if (dialogId != getDialogId()) {
                                return null;
                            }
                            updateAvatarRoundRadius();
                            return StoryRecorder.SourceView.fromAvatarImage(avatarImage, ChatObject.isForum(currentChat));
                        }
                    })
                    .open(StoryRecorder.SourceView.fromFloatingButton(floatingButtonContainer), true);
            }

            @Override
            public void updateTabs(boolean animated) {
                super.updateTabs(animated);
                if (openGifts && !openedGifts && scrollSlidingTextTabStrip.hasTab(TAB_GIFTS)) {
                    openedGifts = true;
                    scrollToPage(TAB_GIFTS);
                }
            }
        };
        sharedMediaLayout.setLayoutParams(new RecyclerView.LayoutParams(RecyclerView.LayoutParams.MATCH_PARENT, RecyclerView.LayoutParams.MATCH_PARENT));
        ActionBarMenu menu = actionBar.createMenu();

        if (userId == getUserConfig().clientUserId && !myProfile) {
            qrItem = menu.addItem(qr_button, R.drawable.msg_qr_mini, getResourceProvider());
            qrItem.setContentDescription(LocaleController.getString(R.string.GetQRCode));
            updateQrItemVisibility(false);
            if (ContactsController.getInstance(currentAccount).getPrivacyRules(PRIVACY_RULES_TYPE_ADDED_BY_PHONE) == null) {
                ContactsController.getInstance(currentAccount).loadPrivacySettings();
            }
        }
        if (imageUpdater != null && !myProfile) {
            searchItem = menu.addItem(search_button, R.drawable.ic_ab_search).setIsSearchField(true).setActionBarMenuItemSearchListener(new ActionBarMenuItem.ActionBarMenuItemSearchListener() {

                @Override
                public Animator getCustomToggleTransition() {
                    searchMode = !searchMode;
                    if (!searchMode) {
                        searchItem.clearFocusOnSearchView();
                    }
                    if (searchMode) {
                        searchItem.getSearchField().setText("");
                    }
                    return searchExpandTransition(searchMode);
                }

                @Override
                public void onTextChanged(EditText editText) {
                    searchAdapter.search(editText.getText().toString().toLowerCase());
                }
            });
            searchItem.setContentDescription(LocaleController.getString(R.string.SearchInSettings));
            searchItem.setSearchFieldHint(LocaleController.getString(R.string.SearchInSettings));
            sharedMediaLayout.getSearchItem().setVisibility(View.GONE);
            if (sharedMediaLayout.getSearchOptionsItem() != null) {
                sharedMediaLayout.getSearchOptionsItem().setVisibility(View.GONE);
            }
            if (sharedMediaLayout.getSaveItem() != null) {
                sharedMediaLayout.getSaveItem().setVisibility(View.GONE);
            }
            if (expandPhoto) {
                searchItem.setVisibility(View.GONE);
            }
        }

        videoCallItem = menu.addItem(video_call_item, R.drawable.profile_video);
        videoCallItem.setContentDescription(LocaleController.getString(R.string.VideoCall));
        if (chatId != 0) {
            callItem = menu.addItem(call_item, R.drawable.msg_voicechat2);
            if (ChatObject.isChannelOrGiga(currentChat)) {
                callItem.setContentDescription(LocaleController.getString(R.string.VoipChannelVoiceChat));
            } else {
                callItem.setContentDescription(LocaleController.getString(R.string.VoipGroupVoiceChat));
            }
        } else {
            callItem = menu.addItem(call_item, R.drawable.ic_call);
            callItem.setContentDescription(LocaleController.getString(R.string.Call));
        }
        if (myProfile) {
            editItem = menu.addItem(edit_profile, R.drawable.group_edit_profile);
            editItem.setContentDescription(LocaleController.getString(R.string.Edit));
        } else {
            editItem = menu.addItem(edit_channel, R.drawable.group_edit_profile);
            editItem.setContentDescription(LocaleController.getString(R.string.Edit));
        }
        otherItem = menu.addItem(10, R.drawable.ic_ab_other, resourcesProvider);
        ttlIconView = new ImageView(context);
        ttlIconView.setColorFilter(new PorterDuffColorFilter(getThemedColor(Theme.key_actionBarDefaultIcon), PorterDuff.Mode.MULTIPLY));
        AndroidUtilities.updateViewVisibilityAnimated(ttlIconView, false, 0.8f, false);
        ttlIconView.setImageResource(R.drawable.msg_mini_autodelete_timer);
        otherItem.addView(ttlIconView, LayoutHelper.createFrame(12, 12, Gravity.CENTER_VERTICAL | Gravity.LEFT, 8, 2, 0, 0));
        otherItem.setContentDescription(LocaleController.getString(R.string.AccDescrMoreOptions));

        int scrollTo;
        int scrollToPosition = 0;
        Object writeButtonTag = null;
        if (listView != null && imageUpdater != null) {
            scrollTo = layoutManager.findFirstVisibleItemPosition();
            View topView = layoutManager.findViewByPosition(scrollTo);
            if (topView != null) {
                scrollToPosition = topView.getTop() - listView.getPaddingTop();
            } else {
                scrollTo = -1;
            }
            writeButtonTag = writeButton.getTag();
        } else {
            scrollTo = -1;
        }

        createActionBarMenu(false);

        listAdapter = new ProfileAdapter(context, resourcesProvider, this);
        searchAdapter = new SearchAdapter(this, context);
        avatarDrawable = new AvatarDrawable();
        avatarDrawable.setProfile(true);

        fragmentView.setWillNotDraw(false);
        contentView = ((NestedFrameLayout) fragmentView);
        contentView.needBlur = true;
        FrameLayout frameLayout = (FrameLayout) fragmentView;

        listView = new ClippedListView(this, context) {

            private VelocityTracker velocityTracker;

            @Override
            protected boolean canHighlightChildAt(View child, float x, float y) {
                return !(child instanceof AboutLinkCell);
            }

            @Override
            protected boolean allowSelectChildAtPosition(View child) {
                return child != sharedMediaLayout;
            }

            @Override
            public boolean hasOverlappingRendering() {
                return false;
            }

            @Override
            protected void requestChildOnScreen(View child, View focused) {

            }

            @Override
            public void invalidate() {
                super.invalidate();
                if (fragmentView != null) {
                    fragmentView.invalidate();
                }
            }

            @Override
            public boolean onInterceptTouchEvent(MotionEvent e) {
                if (sharedMediaLayout != null) {
                    if (sharedMediaLayout.canEditStories() && sharedMediaLayout.isActionModeShown() && sharedMediaLayout.getClosestTab() == SharedMediaLayout.TAB_BOT_PREVIEWS) {
                        return false;
                    }
                    if (sharedMediaLayout.canEditStories() && sharedMediaLayout.isActionModeShown() && sharedMediaLayout.getClosestTab() == SharedMediaLayout.TAB_STORIES) {
                        return false;
                    }
                    if (sharedMediaLayout.giftsContainer != null && sharedMediaLayout.giftsContainer.isReordering()) {
                        return false;
                    }
                }
                return super.onInterceptTouchEvent(e);
            }

            @Override
            public boolean onTouchEvent(MotionEvent e) {
                final int action = e.getAction();
                if (action == MotionEvent.ACTION_DOWN) {
                    if (velocityTracker == null) {
                        velocityTracker = VelocityTracker.obtain();
                    } else {
                        velocityTracker.clear();
                    }
                    velocityTracker.addMovement(e);
                } else if (action == MotionEvent.ACTION_MOVE) {
                    if (velocityTracker != null) {
                        velocityTracker.addMovement(e);
                        velocityTracker.computeCurrentVelocity(1000);
                        listViewVelocityY = velocityTracker.getYVelocity(e.getPointerId(e.getActionIndex()));
                    }
                } else if (action == MotionEvent.ACTION_UP || action == MotionEvent.ACTION_CANCEL) {
                    if (velocityTracker != null) {
                        velocityTracker.recycle();
                        velocityTracker = null;
                    }
                }
                final boolean result = super.onTouchEvent(e);
                if (action == MotionEvent.ACTION_UP || action == MotionEvent.ACTION_CANCEL) {
                    if (allowPullingDown) {
                        final View view = layoutManager.findViewByPosition(0);
                        if (view != null) {
                            if (isPulledDown) {
                                final int actionBarHeight = ActionBar.getCurrentActionBarHeight() + (actionBar.getOccupyStatusBar() ? AndroidUtilities.statusBarHeight : 0);
                                listView.smoothScrollBy(0, view.getTop() - listView.getMeasuredWidth() + actionBarHeight, CubicBezierInterpolator.EASE_OUT_QUINT);
                            } else {
                                listView.smoothScrollBy(0, view.getTop() - AndroidUtilities.dp(88), CubicBezierInterpolator.EASE_OUT_QUINT);
                            }
                        }
                    }
                }
                return result;
            }

            @Override
            public boolean drawChild(Canvas canvas, View child, long drawingTime) {
                if (getItemAnimator().isRunning() && child.getBackground() == null && child.getTranslationY() != 0) {
                    boolean useAlpha = listView.getChildAdapterPosition(child) == sharedMediaRow && child.getAlpha() != 1f;
                    if (useAlpha) {
                        whitePaint.setAlpha((int) (255 * listView.getAlpha() * child.getAlpha()));
                    }
                    canvas.drawRect(listView.getX(), child.getY(), listView.getX() + listView.getMeasuredWidth(), child.getY() + child.getHeight(), whitePaint);
                    if (useAlpha) {
                        whitePaint.setAlpha((int) (255 * listView.getAlpha()));
                    }
                }
                return super.drawChild(canvas, child, drawingTime);
            }

            @Override
            protected void dispatchDraw(Canvas canvas) {
                if (bizHoursRow >= 0 && infoStartRow >= 0 && infoEndRow >= 0) {
                    drawSectionBackground(canvas, infoStartRow, infoEndRow, getThemedColor(Theme.key_windowBackgroundWhite));
                }
                super.dispatchDraw(canvas);
            }

            @Override
            protected void onLayout(boolean changed, int l, int t, int r, int b) {
                super.onLayout(changed, l, t, r, b);
                updateBottomButtonY();
            }
        };
        listView.setVerticalScrollBarEnabled(false);
        DefaultItemAnimator defaultItemAnimator = new DefaultItemAnimator() {

            int animationIndex = -1;

            @Override
            protected void onAllAnimationsDone() {
                super.onAllAnimationsDone();
                AndroidUtilities.runOnUIThread(() -> {
                    getNotificationCenter().onAnimationFinish(animationIndex);
                });
            }

            @Override
            public void runPendingAnimations() {
                boolean removalsPending = !mPendingRemovals.isEmpty();
                boolean movesPending = !mPendingMoves.isEmpty();
                boolean changesPending = !mPendingChanges.isEmpty();
                boolean additionsPending = !mPendingAdditions.isEmpty();
                if (removalsPending || movesPending || additionsPending || changesPending) {
                    ValueAnimator valueAnimator = ValueAnimator.ofFloat(0, 1f);
                    valueAnimator.addUpdateListener(valueAnimator1 -> listView.invalidate());
                    valueAnimator.setDuration(getMoveDuration());
                    valueAnimator.start();
                    animationIndex = getNotificationCenter().setAnimationInProgress(animationIndex, null);
                }
                super.runPendingAnimations();
            }

            @Override
            protected long getAddAnimationDelay(long removeDuration, long moveDuration, long changeDuration) {
                return 0;
            }

            @Override
            protected void onMoveAnimationUpdate(RecyclerView.ViewHolder holder) {
                super.onMoveAnimationUpdate(holder);
                updateBottomButtonY();
            }
        };
        listView.setItemAnimator(defaultItemAnimator);
        defaultItemAnimator.setMoveDelay(0);
        defaultItemAnimator.setMoveDuration(320);
        defaultItemAnimator.setRemoveDuration(320);
        defaultItemAnimator.setAddDuration(320);
        defaultItemAnimator.setSupportsChangeAnimations(false);
        defaultItemAnimator.setDelayAnimations(false);
        defaultItemAnimator.setInterpolator(CubicBezierInterpolator.EASE_OUT_QUINT);
        listView.setClipToPadding(false);
        listView.setHideIfEmpty(false);

        layoutManager = new LinearLayoutManager(context) {

            @Override
            public boolean supportsPredictiveItemAnimations() {
                return imageUpdater != null;
            }

            @Override
            public int scrollVerticallyBy(int dy, RecyclerView.Recycler recycler, RecyclerView.State state) {
                final View view = layoutManager.findViewByPosition(0);
                if (view != null && !openingAvatar) {
                    final int canScroll = view.getTop() - AndroidUtilities.dp(88);
                    if (!allowPullingDown && canScroll > dy) {
                        dy = canScroll;
                        if (avatarsViewPager.hasImages() && avatarImage.getImageReceiver().hasNotThumb() && !AndroidUtilities.isAccessibilityScreenReaderEnabled() && !isInLandscapeMode && !AndroidUtilities.isTablet()) {
                            allowPullingDown = avatarBig == null;
                        }
                    } else if (allowPullingDown) {
                        if (dy >= canScroll) {
                            dy = canScroll;
                            allowPullingDown = false;
                        } else if (listView.getScrollState() == RecyclerListView.SCROLL_STATE_DRAGGING) {
                            if (!isPulledDown) {
                                dy /= 2;
                            }
                        }
                    }
                }
                return super.scrollVerticallyBy(dy, recycler, state);
            }
        };
        layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        layoutManager.mIgnoreTopPadding = false;
        listView.setLayoutManager(layoutManager);
        listView.setGlowColor(0);
        listView.setAdapter(listAdapter);
        frameLayout.addView(listView, LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, LayoutHelper.MATCH_PARENT, Gravity.TOP | Gravity.LEFT));
        listView.setOnItemClickListener((view, position, x, y) -> {
            ProfileListHandler.listClickHandle(this, context, view, position, x, y, did, lastFragment);
        });

        RecyclerListView.OnItemLongClickListener listener = new ProfileOnItemLongClickListener(this, context);
        listView.setOnItemLongClickListener(listener);

        if (openSimilar) {
            updateRowsIds();
            scrollToSharedMedia();
            savedScrollToSharedMedia = true;
            savedScrollPosition = sharedMediaRow;
            savedScrollOffset = 0;
        }

        if (searchItem != null) {
            searchListView = new RecyclerListView(context);
            searchListView.setVerticalScrollBarEnabled(false);
            searchListView.setLayoutManager(new LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false));
            searchListView.setGlowColor(getThemedColor(Theme.key_avatar_backgroundActionBarBlue));
            searchListView.setAdapter(searchAdapter);
            searchListView.setItemAnimator(null);
            searchListView.setVisibility(View.GONE);
            searchListView.setLayoutAnimation(null);
            searchListView.setBackgroundColor(getThemedColor(Theme.key_windowBackgroundWhite));
            frameLayout.addView(searchListView, LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, LayoutHelper.MATCH_PARENT, Gravity.TOP | Gravity.LEFT));
            searchListView.setOnItemClickListener((view, position) -> {
                if (position < 0) {
                    return;
                }
                Object object = numberRow;
                boolean add = true;
                if (searchAdapter.isSearchWas()) {
                    if (position < searchAdapter.searchResults.size()) {
                        object = searchAdapter.searchResults.get(position);
                    } else {
                        position -= searchAdapter.searchResults.size() + 1;
                        if (position >= 0 && position < searchAdapter.faqSearchResults.size()) {
                            object = searchAdapter.faqSearchResults.get(position);
                        }
                    }
                } else {
                    if (!searchAdapter.recentSearches.isEmpty()) {
                        position--;
                    }
                    if (position >= 0 && position < searchAdapter.recentSearches.size()) {
                        object = searchAdapter.recentSearches.get(position);
                    } else {
                        position -= searchAdapter.recentSearches.size() + 1;
                        if (position >= 0 && position < searchAdapter.faqSearchArray.size()) {
                            object = searchAdapter.faqSearchArray.get(position);
                            add = false;
                        }
                    }
                }
                if (object instanceof SearchAdapter.SearchResult) {
                    SearchAdapter.SearchResult result = (SearchAdapter.SearchResult) object;
                    result.open();
                } else if (object instanceof MessagesController.FaqSearchResult) {
                    MessagesController.FaqSearchResult result = (MessagesController.FaqSearchResult) object;
                    NotificationCenter.getInstance(currentAccount).postNotificationName(NotificationCenter.openArticle, searchAdapter.faqWebPage, result.url);
                }
                if (add && object != null) {
                    searchAdapter.addRecent(object);
                }
            });
            searchListView.setOnItemLongClickListener((view, position) -> {
                if (searchAdapter.isSearchWas() || searchAdapter.recentSearches.isEmpty()) {
                    return false;
                }
                AlertDialog.Builder builder = new AlertDialog.Builder(getParentActivity(), resourcesProvider);
                builder.setTitle(LocaleController.getString(R.string.ClearSearchAlertTitle));
                builder.setMessage(LocaleController.getString(R.string.ClearSearchAlert));
                builder.setPositiveButton(LocaleController.getString(R.string.ClearButton), (dialogInterface, i) -> searchAdapter.clearRecent());
                builder.setNegativeButton(LocaleController.getString(R.string.Cancel), null);
                AlertDialog dialog = builder.create();
                showDialog(dialog);
                TextView button = (TextView) dialog.getButton(DialogInterface.BUTTON_POSITIVE);
                if (button != null) {
                    button.setTextColor(Theme.getColor(Theme.key_text_RedBold));
                }
                return true;
            });
            searchListView.setOnScrollListener(new RecyclerView.OnScrollListener() {
                @Override
                public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                    if (newState == RecyclerView.SCROLL_STATE_DRAGGING) {
                        AndroidUtilities.hideKeyboard(getParentActivity().getCurrentFocus());
                    }
                }
            });
            searchListView.setAnimateEmptyView(true, RecyclerListView.EMPTY_VIEW_ANIMATION_TYPE_ALPHA_SCALE);

            emptyView = new StickerEmptyView(context, null, 1);
            emptyView.setAnimateLayoutChange(true);
            emptyView.subtitle.setVisibility(View.GONE);
            emptyView.setVisibility(View.GONE);
            frameLayout.addView(emptyView);

            searchAdapter.loadFaqWebPage();
        }

        if (banFromGroup != 0) {
            TLRPC.Chat chat = getMessagesController().getChat(banFromGroup);
            if (currentChannelParticipant == null) {
                TLRPC.TL_channels_getParticipant req = new TLRPC.TL_channels_getParticipant();
                req.channel = MessagesController.getInputChannel(chat);
                req.participant = getMessagesController().getInputPeer(userId);
                getConnectionsManager().sendRequest(req, (response, error) -> {
                    if (response != null) {
                        AndroidUtilities.runOnUIThread(() -> currentChannelParticipant = ((TLRPC.TL_channels_channelParticipant) response).participant);
                    }
                });
            }
            FrameLayout frameLayout1 = new FrameLayout(context) {
                @Override
                protected void onDraw(Canvas canvas) {
                    int bottom = Theme.chat_composeShadowDrawable.getIntrinsicHeight();
                    Theme.chat_composeShadowDrawable.setBounds(0, 0, getMeasuredWidth(), bottom);
                    Theme.chat_composeShadowDrawable.draw(canvas);
                    canvas.drawRect(0, bottom, getMeasuredWidth(), getMeasuredHeight(), Theme.chat_composeBackgroundPaint);
                }
            };
            frameLayout1.setWillNotDraw(false);

            frameLayout.addView(frameLayout1, LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, 51, Gravity.LEFT | Gravity.BOTTOM));
            frameLayout1.setOnClickListener(v -> {
                ChatRightsEditActivity fragment = new ChatRightsEditActivity(userId, banFromGroup, null, chat.default_banned_rights, currentChannelParticipant != null ? currentChannelParticipant.banned_rights : null, "", ChatRightsEditActivity.TYPE_BANNED, true, false, null);
                fragment.setDelegate(new ChatRightsEditActivity.ChatRightsEditActivityDelegate() {
                    @Override
                    public void didSetRights(int rights, TLRPC.TL_chatAdminRights rightsAdmin, TLRPC.TL_chatBannedRights rightsBanned, String rank) {
                        removeSelfFromStack();
                        TLRPC.User user = getMessagesController().getUser(userId);
                        if (user != null && chat != null && userId != 0 && fragment != null && fragment.banning && fragment.getParentLayout() != null) {
                            for (BaseFragment fragment : fragment.getParentLayout().getFragmentStack()) {
                                if (fragment instanceof ChannelAdminLogActivity) {
                                    ((ChannelAdminLogActivity) fragment).reloadLastMessages();
                                    AndroidUtilities.runOnUIThread(() -> {
                                        BulletinFactory.createRemoveFromChatBulletin(fragment, user, chat.title).show();
                                    });
                                    return;
                                }
                            }
                        }
                    }
                    @Override
                    public void didChangeOwner(TLRPC.User user) {
                        undoView.showWithAction(-chatId, currentChat.megagroup ? UndoView.ACTION_OWNER_TRANSFERED_GROUP : UndoView.ACTION_OWNER_TRANSFERED_CHANNEL, user);
                    }
                });
                presentFragment(fragment);
            });

            TextView textView = new TextView(context);
            textView.setTextColor(getThemedColor(Theme.key_text_RedRegular));
            textView.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 15);
            textView.setGravity(Gravity.CENTER);
            textView.setTypeface(AndroidUtilities.bold());
            textView.setText(LocaleController.getString(R.string.BanFromTheGroup));
            frameLayout1.addView(textView, LayoutHelper.createFrame(LayoutHelper.WRAP_CONTENT, LayoutHelper.WRAP_CONTENT, Gravity.CENTER, 0, 1, 0, 0));

            listView.setPadding(0, AndroidUtilities.dp(88), 0, AndroidUtilities.dp(48));
            listView.setBottomGlowOffset(AndroidUtilities.dp(48));
        } else {
            listView.setPadding(0, AndroidUtilities.dp(88), 0, 0);
        }

        topView = new TopView(this, context);
        topView.setBackgroundColorId(peerColor, false);
        topView.setBackgroundColor(getThemedColor(Theme.key_avatar_backgroundActionBarBlue));
        frameLayout.addView(topView);
        contentView.blurBehindViews.add(topView);

        animatedStatusView = new DrawerProfileCell.AnimatedStatusView(context, 20, 60);
        animatedStatusView.setPivotX(AndroidUtilities.dp(30));
        animatedStatusView.setPivotY(AndroidUtilities.dp(30));

        avatarContainer = new FrameLayout(context);
        avatarContainer2 = new FrameLayout(context) {

            CanvasButton canvasButton;

            @Override
            protected void dispatchDraw(Canvas canvas) {
                super.dispatchDraw(canvas);
                if (transitionOnlineText != null) {
                    canvas.save();
                    canvas.translate(onlineTextView[0].getX(), onlineTextView[0].getY());
                    canvas.saveLayerAlpha(0, 0, transitionOnlineText.getMeasuredWidth(), transitionOnlineText.getMeasuredHeight(), (int) (255 * (1f - avatarAnimationProgress)), Canvas.ALL_SAVE_FLAG);
                    transitionOnlineText.draw(canvas);
                    canvas.restore();
                    canvas.restore();
                    invalidate();
                }
                if (hasFallbackPhoto && photoDescriptionProgress != 0 && customAvatarProgress != 1f) {
                    float cy =  onlineTextView[1].getY() + onlineTextView[1].getMeasuredHeight() / 2f;
                    float size = AndroidUtilities.dp(22);
                    float x = AndroidUtilities.dp(28) - customPhotoOffset + onlineTextView[1].getX() - size;

                    fallbackImage.setImageCoords(x, cy - size / 2f, size, size);
                    fallbackImage.setAlpha(photoDescriptionProgress);
                    canvas.save();
                    float s = photoDescriptionProgress;
                    canvas.scale(s, s, fallbackImage.getCenterX(), fallbackImage.getCenterY());
                    fallbackImage.draw(canvas);
                    canvas.restore();

                    if (customAvatarProgress == 0) {
                        if (canvasButton == null) {
                            canvasButton = new CanvasButton(this);
                            canvasButton.setDelegate(() -> {
                                if (customAvatarProgress != 1f) {
                                    avatarsViewPager.scrollToLastItem();
                                }
                            });
                        }
                        AndroidUtilities.rectTmp.set(x - AndroidUtilities.dp(4), cy - AndroidUtilities.dp(14), x + onlineTextView[2].getTextWidth() + AndroidUtilities.dp(28) * (1f - customAvatarProgress) + AndroidUtilities.dp(4), cy + AndroidUtilities.dp(14));
                        canvasButton.setRect(AndroidUtilities.rectTmp);
                        canvasButton.setRounded(true);
                        canvasButton.setColor(Color.TRANSPARENT, ColorUtils.setAlphaComponent(Color.WHITE, 50));
                        canvasButton.draw(canvas);
                    } else {
                        if (canvasButton != null) {
                            canvasButton.cancelRipple();
                        }
                    }
                }
            }

            @Override
            public boolean onInterceptTouchEvent(MotionEvent ev) {
                return (canvasButton != null && canvasButton.checkTouchEvent(ev)) || super.onInterceptTouchEvent(ev);
            }

            @Override
            public boolean onTouchEvent(MotionEvent event) {
                return (canvasButton != null && canvasButton.checkTouchEvent(event)) || super.onTouchEvent(event);
            }

            @Override
            protected void onAttachedToWindow() {
                super.onAttachedToWindow();
                fallbackImage.onAttachedToWindow();
            }

            @Override
            protected void onDetachedFromWindow() {
                super.onDetachedFromWindow();
                fallbackImage.onDetachedFromWindow();
            }

            @Override
            protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
                super.onLayout(changed, left, top, right, bottom);
                updateCollectibleHint();
            }
        };
        fallbackImage = new ImageReceiver(avatarContainer2);
        fallbackImage.setRoundRadius(AndroidUtilities.dp(11));
        AndroidUtilities.updateViewVisibilityAnimated(avatarContainer2, true, 1f, false);
        frameLayout.addView(avatarContainer2, LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, LayoutHelper.MATCH_PARENT, Gravity.START, 0, 0, 0, 0));
        avatarContainer.setPivotX(0);
        avatarContainer.setPivotY(0);
        avatarContainer2.addView(avatarContainer, LayoutHelper.createFrame(42, 42, Gravity.TOP | Gravity.LEFT, 64, 0, 0, 0));
        avatarImage = new AvatarImageView(context) {
            @Override
            public void onInitializeAccessibilityNodeInfo(AccessibilityNodeInfo info) {
                super.onInitializeAccessibilityNodeInfo(info);
                if (getImageReceiver().hasNotThumb()) {
                    info.setText(LocaleController.getString(R.string.AccDescrProfilePicture));
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                        info.addAction(new AccessibilityNodeInfo.AccessibilityAction(AccessibilityNodeInfo.ACTION_CLICK, LocaleController.getString(R.string.Open)));
                        info.addAction(new AccessibilityNodeInfo.AccessibilityAction(AccessibilityNodeInfo.ACTION_LONG_CLICK, LocaleController.getString(R.string.AccDescrOpenInPhotoViewer)));
                    }
                } else {
                    info.setVisibleToUser(false);
                }
            }

            @Override
            protected void dispatchDraw(Canvas canvas) {
                super.dispatchDraw(canvas);
                if (animatedEmojiDrawable != null && animatedEmojiDrawable.getImageReceiver() != null) {
                    animatedEmojiDrawable.getImageReceiver().startAnimation();
                }
            }
        };
        avatarImage.getImageReceiver().setAllowDecodeSingleFrame(true);
        avatarImage.setRoundRadius(getSmallAvatarRoundRadius());
        avatarImage.setPivotX(0);
        avatarImage.setPivotY(0);
        avatarContainer.addView(avatarImage, LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, LayoutHelper.MATCH_PARENT));
        avatarImage.setOnClickListener(v -> {
            if (avatarBig != null) {
                return;
            }
            if (isTopic && !getMessagesController().premiumFeaturesBlocked()) {
                ArrayList<TLRPC.TL_forumTopic> topics = getMessagesController().getTopicsController().getTopics(chatId);
                if (topics != null) {
                    TLRPC.TL_forumTopic currentTopic = null;
                    for (int i = 0; currentTopic == null && i < topics.size(); ++i) {
                        TLRPC.TL_forumTopic topic = topics.get(i);
                        if (topic != null && topic.id == topicId) {
                            currentTopic = topic;
                        }
                    }
                    if (currentTopic != null && currentTopic.icon_emoji_id != 0) {
                        long documentId = currentTopic.icon_emoji_id;
                        TLRPC.Document document = AnimatedEmojiDrawable.findDocument(currentAccount, documentId);
                        if (document == null) {
                            return;
                        }
                        Bulletin bulletin = BulletinFactory.of(ProfileActivity.this).createContainsEmojiBulletin(document, BulletinFactory.CONTAINS_EMOJI_IN_TOPIC, set -> {
                            ArrayList<TLRPC.InputStickerSet> inputSets = new ArrayList<>(1);
                            inputSets.add(set);
                            EmojiPacksAlert alert = new EmojiPacksAlert(ProfileActivity.this, getParentActivity(), resourcesProvider, inputSets);
                            showDialog(alert);
                        });
                        if (bulletin != null) {
                            bulletin.show();
                        }
                    }
                }
                return;
            }
            if (expandAvatar()) {
                return;
            }
            openAvatar();
        });
        avatarImage.setHasStories(needInsetForStories());
        avatarImage.setOnLongClickListener(v -> {
            if (avatarBig != null || isTopic) {
                return false;
            }
            openAvatar();
            return false;
        });

        avatarProgressView = new RadialProgressView(context) {
            private Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);

            {
                paint.setColor(0x55000000);
            }

            @Override
            protected void onDraw(Canvas canvas) {
                if (avatarImage != null && avatarImage.getImageReceiver().hasNotThumb()) {
                    paint.setAlpha((int) (0x55 * avatarImage.getImageReceiver().getCurrentAlpha()));
                    canvas.drawCircle(getMeasuredWidth() / 2.0f, getMeasuredHeight() / 2.0f, getMeasuredWidth() / 2.0f, paint);
                }
                super.onDraw(canvas);
            }
        };
        avatarProgressView.setSize(AndroidUtilities.dp(26));
        avatarProgressView.setProgressColor(0xffffffff);
        avatarProgressView.setNoProgress(false);
        avatarContainer.addView(avatarProgressView, LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, LayoutHelper.MATCH_PARENT));

        timeItem = new ImageView(context);
        timeItem.setPadding(AndroidUtilities.dp(10), AndroidUtilities.dp(10), AndroidUtilities.dp(5), AndroidUtilities.dp(5));
        timeItem.setScaleType(ImageView.ScaleType.CENTER);
        timeItem.setAlpha(0.0f);
        timeItem.setImageDrawable(timerDrawable = new TimerDrawable(context, null));
        timeItem.setTranslationY(-1);
        frameLayout.addView(timeItem, LayoutHelper.createFrame(34, 34, Gravity.TOP | Gravity.LEFT));

        starBgItem = new ImageView(context);
        starBgItem.setImageResource(R.drawable.star_small_outline);
        starBgItem.setColorFilter(new PorterDuffColorFilter(getThemedColor(Theme.key_actionBarDefault), PorterDuff.Mode.SRC_IN));
        starBgItem.setAlpha(0.0f);
        starBgItem.setScaleY(0.0f);
        starBgItem.setScaleX(0.0f);
        frameLayout.addView(starBgItem, LayoutHelper.createFrame(20, 20, Gravity.TOP | Gravity.LEFT));

        starFgItem = new ImageView(context);
        starFgItem.setImageResource(R.drawable.star_small_inner);
        starFgItem.setAlpha(0.0f);
        starFgItem.setScaleY(0.0f);
        starFgItem.setScaleX(0.0f);
        frameLayout.addView(starFgItem, LayoutHelper.createFrame(20, 20, Gravity.TOP | Gravity.LEFT));

        showAvatarProgress(false, false);

        if (avatarsViewPager != null) {
            avatarsViewPager.onDestroy();
        }
        overlaysView = new OverlaysView(this, context);
        avatarsViewPager = new ProfileGalleryView(context, userId != 0 ? userId : -chatId, actionBar, listView, avatarImage, getClassGuid(), overlaysView) {
            @Override
            protected void setCustomAvatarProgress(float progress) {
                customAvatarProgress = progress;
                checkPhotoDescriptionAlpha();
            }
        };
        if (userId != getUserConfig().clientUserId && userInfo != null) {
            customAvatarProgress = userInfo.profile_photo == null ? 0 : 1;
        }
        if (!isTopic) {
            avatarsViewPager.setChatInfo(chatInfo);
        }
        avatarContainer2.addView(avatarsViewPager);
        avatarContainer2.addView(overlaysView);
        avatarImage.setAvatarsViewPager(avatarsViewPager);

        avatarsViewPagerIndicatorView = new PagerIndicatorView(this, context);
        avatarContainer2.addView(avatarsViewPagerIndicatorView, LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, LayoutHelper.MATCH_PARENT));

        frameLayout.addView(actionBar);

        float rightMargin = (54 + ((callItemVisible && userId != 0) ? 54 : 0));
        boolean hasTitleExpanded = false;
        int initialTitleWidth = LayoutHelper.WRAP_CONTENT;
        if (parentLayout != null && parentLayout.getLastFragment() instanceof ChatActivity) {
            ChatAvatarContainer avatarContainer = ((ChatActivity) parentLayout.getLastFragment()).getAvatarContainer();
            if (avatarContainer != null) {
                hasTitleExpanded = avatarContainer.getTitleTextView().getPaddingRight() != 0;
                if (avatarContainer.getLayoutParams() != null && avatarContainer.getTitleTextView() != null) {
                    rightMargin =
                            (((ViewGroup.MarginLayoutParams) avatarContainer.getLayoutParams()).rightMargin +
                                    (avatarContainer.getWidth() - avatarContainer.getTitleTextView().getRight())) / AndroidUtilities.density;
//                initialTitleWidth = (int) (avatarContainer.getTitleTextView().getWidth() / AndroidUtilities.density);
                }
            }
        }

        for (int a = 0; a < nameTextView.length; a++) {
            if (playProfileAnimation == 0 && a == 0) {
                continue;
            }
            nameTextView[a] = new SimpleTextView(context) {
                @Override
                public void onInitializeAccessibilityNodeInfo(AccessibilityNodeInfo info) {
                    super.onInitializeAccessibilityNodeInfo(info);
                    if (isFocusable() && (nameTextViewRightDrawableContentDescription != null || nameTextViewRightDrawable2ContentDescription != null)) {
                        StringBuilder s = new StringBuilder(getText());
                        if (nameTextViewRightDrawable2ContentDescription != null) {
                            if (s.length() > 0) s.append(", ");
                            s.append(nameTextViewRightDrawable2ContentDescription);
                        }
                        if (nameTextViewRightDrawableContentDescription != null) {
                            if (s.length() > 0) s.append(", ");
                            s.append(nameTextViewRightDrawableContentDescription);
                        }
                        info.setText(s);
                    }
                }
                @Override
                protected void onDraw(Canvas canvas) {
                    final int wasRightDrawableX = getRightDrawableX();
                    super.onDraw(canvas);
                    if (wasRightDrawableX != getRightDrawableX()) {
                        updateCollectibleHint();
                    }
                }
            };
            if (a == 1) {
                nameTextView[a].setTextColor(getThemedColor(Theme.key_profile_title));
            } else {
                nameTextView[a].setTextColor(getThemedColor(Theme.key_actionBarDefaultTitle));
            }
            nameTextView[a].setPadding(0, AndroidUtilities.dp(6), 0, AndroidUtilities.dp(a == 0 ? 12 : 4));
            nameTextView[a].setTextSize(18);
            nameTextView[a].setGravity(Gravity.LEFT);
            nameTextView[a].setTypeface(AndroidUtilities.bold());
            nameTextView[a].setLeftDrawableTopPadding(-AndroidUtilities.dp(1.3f));
            nameTextView[a].setPivotX(0);
            nameTextView[a].setPivotY(0);
            nameTextView[a].setAlpha(a == 0 ? 0.0f : 1.0f);
            if (a == 1) {
                nameTextView[a].setScrollNonFitText(true);
                nameTextView[a].setImportantForAccessibility(View.IMPORTANT_FOR_ACCESSIBILITY_NO);
            }
            nameTextView[a].setFocusable(a == 0);
            nameTextView[a].setEllipsizeByGradient(true);
            nameTextView[a].setRightDrawableOutside(a == 0);
            avatarContainer2.addView(nameTextView[a], LayoutHelper.createFrame(a == 0 ? initialTitleWidth : LayoutHelper.WRAP_CONTENT, LayoutHelper.WRAP_CONTENT, Gravity.LEFT | Gravity.TOP, 118, -6, (a == 0 ? rightMargin - (hasTitleExpanded ? 10 : 0) : 0), 0));
        }
        for (int a = 0; a < onlineTextView.length; a++) {
            if (a == 1) {
                onlineTextView[a] = new LinkSpanDrawable.ClickableSmallTextView(context) {

                    @Override
                    public void setAlpha(float alpha) {
                        super.setAlpha(alpha);
                        checkPhotoDescriptionAlpha();
                    }

                    @Override
                    public void setTranslationY(float translationY) {
                        super.setTranslationY(translationY);
                        onlineTextView[2].setTranslationY(translationY);
                        onlineTextView[3].setTranslationY(translationY);
                    }

                    @Override
                    public void setTranslationX(float translationX) {
                        super.setTranslationX(translationX);
                        onlineTextView[2].setTranslationX(translationX);
                        onlineTextView[3].setTranslationX(translationX);
                    }

                    @Override
                    public void setTextColor(int color) {
                        super.setTextColor(color);
                        if (onlineTextView[2] != null) {
                            onlineTextView[2].setTextColor(color);
                            onlineTextView[3].setTextColor(color);
                        }
                        if (showStatusButton != null) {
                            showStatusButton.setTextColor(Theme.multAlpha(Theme.adaptHSV(color, -.02f, +.15f), 1.4f));
                        }
                    }
                };
            } else {
                onlineTextView[a] = new LinkSpanDrawable.ClickableSmallTextView(context);
            }

            onlineTextView[a].setEllipsizeByGradient(true);
            onlineTextView[a].setTextColor(applyPeerColor(getThemedColor(Theme.key_avatar_subtitleInProfileBlue), true, null));
            onlineTextView[a].setTextSize(14);
            onlineTextView[a].setGravity(Gravity.LEFT);
            onlineTextView[a].setAlpha(a == 0 ? 0.0f : 1.0f);
            if (a == 1 || a == 2 || a == 3) {
                onlineTextView[a].setPadding(AndroidUtilities.dp(4), AndroidUtilities.dp(2), AndroidUtilities.dp(4), AndroidUtilities.dp(2));
            }
            if (a > 0) {
                onlineTextView[a].setImportantForAccessibility(View.IMPORTANT_FOR_ACCESSIBILITY_NO);
            }
            onlineTextView[a].setFocusable(a == 0);
            avatarContainer2.addView(onlineTextView[a], LayoutHelper.createFrame(LayoutHelper.WRAP_CONTENT, LayoutHelper.WRAP_CONTENT, Gravity.LEFT | Gravity.TOP, 118 - (a == 1 || a == 2 || a == 3? 4 : 0), (a == 1 || a == 2 || a == 3 ? -2 : 0), (a == 0 ? rightMargin - (hasTitleExpanded ? 10 : 0) : 8) - (a == 1 || a == 2 || a == 3 ? 4 : 0), 0));
        }
        checkPhotoDescriptionAlpha();
        avatarContainer2.addView(animatedStatusView);

        mediaCounterTextView = new AudioPlayerAlert.ClippingTextViewSwitcher(context) {
            @Override
            protected TextView createTextView() {
                TextView textView = new TextView(context);
                textView.setTextColor(getThemedColor(Theme.key_player_actionBarSubtitle));
                textView.setTextSize(TypedValue.COMPLEX_UNIT_PX, AndroidUtilities.dp(14));
                textView.setSingleLine(true);
                textView.setEllipsize(TextUtils.TruncateAt.END);
                textView.setGravity(Gravity.LEFT);
                return textView;
            }
        };
        mediaCounterTextView.setAlpha(0.0f);
        avatarContainer2.addView(mediaCounterTextView, LayoutHelper.createFrame(LayoutHelper.WRAP_CONTENT, LayoutHelper.WRAP_CONTENT, Gravity.LEFT | Gravity.TOP, 118.33f, -2, 8, 0));
        storyView = new ProfileStoriesView(context, currentAccount, getDialogId(), isTopic, avatarContainer, avatarImage, resourcesProvider) {
            @Override
            protected void onTap(StoryViewer.PlaceProvider provider) {
                long did = getDialogId();
                StoriesController storiesController = getMessagesController().getStoriesController();
                if (storiesController.hasStories(did) || storiesController.hasUploadingStories(did) || storiesController.isLastUploadingFailed(did)) {
                    getOrCreateStoryViewer().open(context, did, provider);
                } else if (userInfo != null && userInfo.stories != null && !userInfo.stories.stories.isEmpty() && userId != getUserConfig().clientUserId) {
                    getOrCreateStoryViewer().open(context, userInfo.stories, provider);
                } else if (chatInfo != null && chatInfo.stories != null && !chatInfo.stories.stories.isEmpty()) {
                    getOrCreateStoryViewer().open(context, chatInfo.stories, provider);
                } else {
                    expandAvatar();
                }
            }

            @Override
            protected void onLongPress() {
                openAvatar();
            }
        };
        updateStoriesViewBounds(false);
        if (userInfo != null) {
            storyView.setStories(userInfo.stories);
        } else if (chatInfo != null) {
            storyView.setStories(chatInfo.stories);
        }
        if (avatarImage != null) {
            avatarImage.setHasStories(needInsetForStories());
        }
        avatarContainer2.addView(storyView, LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, LayoutHelper.MATCH_PARENT));
        giftsView = new ProfileGiftsView(context, currentAccount, getDialogId(), avatarContainer, avatarImage, resourcesProvider);
        avatarContainer2.addView(giftsView, LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, LayoutHelper.MATCH_PARENT));
        updateProfileData(true);

        writeButton = new RLottieImageView(context);
        writeButtonSetBackground();
        if (userId != 0) {
            if (imageUpdater != null) {
                cameraDrawable = new RLottieDrawable(R.raw.camera_outline, String.valueOf(R.raw.camera_outline), AndroidUtilities.dp(56), AndroidUtilities.dp(56), false, null);
                cellCameraDrawable = new RLottieDrawable(R.raw.camera_outline, R.raw.camera_outline + "_cell", AndroidUtilities.dp(42), AndroidUtilities.dp(42), false, null);

                writeButton.setAnimation(cameraDrawable);
                writeButton.setContentDescription(LocaleController.getString(R.string.AccDescrChangeProfilePicture));
                writeButton.setPadding(AndroidUtilities.dp(2), 0, 0, AndroidUtilities.dp(2));
            } else {
                writeButton.setImageResource(R.drawable.profile_newmsg);
                writeButton.setContentDescription(LocaleController.getString(R.string.AccDescrOpenChat));
            }
        } else {
            writeButton.setImageResource(R.drawable.profile_discuss);
            writeButton.setContentDescription(LocaleController.getString(R.string.ViewDiscussion));
        }
        writeButton.setScaleType(ImageView.ScaleType.CENTER);

        frameLayout.addView(writeButton, LayoutHelper.createFrame(60, 60, Gravity.RIGHT | Gravity.TOP, 0, 0, 16, 0));
        writeButton.setOnClickListener(v -> {
            if (writeButton.getTag() != null) {
                return;
            }
            onWriteButtonClick();
        });
        needLayout(false);

        if (scrollTo != -1) {
            if (writeButtonTag != null) {
                writeButton.setTag(0);
                writeButton.setScaleX(0.2f);
                writeButton.setScaleY(0.2f);
                writeButton.setAlpha(0.0f);
            }
        }

        listView.setOnScrollListener(new RecyclerView.OnScrollListener() {

            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                if (newState == RecyclerView.SCROLL_STATE_DRAGGING) {
                    AndroidUtilities.hideKeyboard(getParentActivity().getCurrentFocus());
                }
                if (openingAvatar && newState != RecyclerView.SCROLL_STATE_SETTLING) {
                    openingAvatar = false;
                }
                if (searchItem != null) {
                    scrolling = newState != RecyclerView.SCROLL_STATE_IDLE;
                    searchItem.setEnabled(!scrolling && !isPulledDown);
                }
                sharedMediaLayout.scrollingByUser = listView.scrollingByUser;
            }

            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                if (fwdRestrictedHint != null) {
                    fwdRestrictedHint.hide();
                }
                checkListViewScroll();
                if (participantsMap != null && !usersEndReached && layoutManager.findLastVisibleItemPosition() > membersEndRow - 8) {
                    getChannelParticipants(false);
                }
                sharedMediaLayout.setPinnedToTop(sharedMediaLayout.getY() <= 0);
                updateBottomButtonY();
            }
        });

        undoView = new UndoView(context, null, false, resourcesProvider);
        frameLayout.addView(undoView, LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, LayoutHelper.WRAP_CONTENT, Gravity.BOTTOM | Gravity.LEFT, 8, 0, 8, 8));

        expandAnimator = ValueAnimator.ofFloat(0f, 1f);
        expandAnimator.addUpdateListener(anim -> {
            setAvatarExpandProgress(anim.getAnimatedFraction());
        });
        expandAnimator.setInterpolator(CubicBezierInterpolator.EASE_OUT_QUINT);
        expandAnimator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationStart(Animator animation) {

            }

            @Override
            public void onAnimationEnd(Animator animation) {
                actionBar.setItemsBackgroundColor(isPulledDown ? Theme.ACTION_BAR_WHITE_SELECTOR_COLOR : peerColor != null ? 0x20ffffff : getThemedColor(Theme.key_avatar_actionBarSelectorBlue), false);
                avatarImage.clearForeground();
                doNotSetForeground = false;
                updateStoriesViewBounds(false);
            }
        });
        updateRowsIds();

        updateSelectedMediaTabText();

        fwdRestrictedHint = new HintView(getParentActivity(), 9);
        fwdRestrictedHint.setAlpha(0);
        frameLayout.addView(fwdRestrictedHint, LayoutHelper.createFrame(LayoutHelper.WRAP_CONTENT, LayoutHelper.WRAP_CONTENT, Gravity.LEFT | Gravity.TOP, 12, 0, 12, 0));
        sharedMediaLayout.setForwardRestrictedHint(fwdRestrictedHint);

        ViewGroup decorView;
        if (Build.VERSION.SDK_INT >= 21) {
            decorView = (ViewGroup) getParentActivity().getWindow().getDecorView();
        } else {
            decorView = frameLayout;
        }
        pinchToZoomHelper = new PinchToZoomHelper(decorView, frameLayout) {

            Paint statusBarPaint;

            @Override
            protected void invalidateViews() {
                super.invalidateViews();
                fragmentView.invalidate();
                for (int i = 0; i < avatarsViewPager.getChildCount(); i++) {
                    avatarsViewPager.getChildAt(i).invalidate();
                }
                if (writeButton != null) {
                    writeButton.invalidate();
                }
            }

            @Override
            protected void drawOverlays(Canvas canvas, float alpha, float parentOffsetX, float parentOffsetY, float clipTop, float clipBottom) {
                if (alpha > 0) {
                    AndroidUtilities.rectTmp.set(0, 0, avatarsViewPager.getMeasuredWidth(), avatarsViewPager.getMeasuredHeight() + AndroidUtilities.dp(30));
                    canvas.saveLayerAlpha(AndroidUtilities.rectTmp, (int) (255 * alpha), Canvas.ALL_SAVE_FLAG);

                    avatarContainer2.draw(canvas);

                    if (actionBar.getOccupyStatusBar() && !SharedConfig.noStatusBar) {
                        if (statusBarPaint == null) {
                            statusBarPaint = new Paint();
                            statusBarPaint.setColor(ColorUtils.setAlphaComponent(Color.BLACK, (int) (255 * 0.2f)));
                        }
                        canvas.drawRect(actionBar.getX(), actionBar.getY(), actionBar.getX() + actionBar.getMeasuredWidth(), actionBar.getY() + AndroidUtilities.statusBarHeight, statusBarPaint);
                    }
                    canvas.save();
                    canvas.translate(actionBar.getX(), actionBar.getY());
                    actionBar.draw(canvas);
                    canvas.restore();

                    if (writeButton != null && writeButton.getVisibility() == View.VISIBLE && writeButton.getAlpha() > 0) {
                        canvas.save();
                        float s = 0.5f + 0.5f * alpha;
                        canvas.scale(s, s, writeButton.getX() + writeButton.getMeasuredWidth() / 2f, writeButton.getY() + writeButton.getMeasuredHeight() / 2f);
                        canvas.translate(writeButton.getX(), writeButton.getY());
                        writeButton.draw(canvas);
                        canvas.restore();
                    }
                    canvas.restore();
                }
            }

            @Override
            protected boolean zoomEnabled(View child, ImageReceiver receiver) {
                if (!super.zoomEnabled(child, receiver)) {
                    return false;
                }
                return listView.getScrollState() != RecyclerView.SCROLL_STATE_DRAGGING;
            }
        };
        pinchToZoomHelper.setCallback(new PinchToZoomHelper.Callback() {
            @Override
            public void onZoomStarted(MessageObject messageObject) {
                listView.cancelClickRunnables(true);
                if (sharedMediaLayout != null && sharedMediaLayout.getCurrentListView() != null) {
                    sharedMediaLayout.getCurrentListView().cancelClickRunnables(true);
                }
                topView.setBackgroundColor(ColorUtils.blendARGB(getAverageColor(pinchToZoomHelper.getPhotoImage()), getThemedColor(Theme.key_windowBackgroundWhite), 0.1f));
            }
        });
        avatarsViewPager.setPinchToZoomHelper(pinchToZoomHelper);
        scrimPaint.setAlpha(0);
        actionBarBackgroundPaint.setColor(getThemedColor(Theme.key_listSelector));
        contentView.blurBehindViews.add(sharedMediaLayout);
        updateTtlIcon();

        blurredView = new View(context) {
            @Override
            public void setAlpha(float alpha) {
                super.setAlpha(alpha);
                if (fragmentView != null) {
                    fragmentView.invalidate();
                }
            }
        };
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            blurredView.setForeground(new ColorDrawable(ColorUtils.setAlphaComponent(getThemedColor(Theme.key_windowBackgroundWhite), 100)));
        }
        blurredView.setFocusable(false);
        blurredView.setImportantForAccessibility(View.IMPORTANT_FOR_ACCESSIBILITY_NO);
        blurredView.setOnClickListener(e -> {
            finishPreviewFragment();
        });
        blurredView.setVisibility(View.GONE);
        blurredView.setFitsSystemWindows(true);
        contentView.addView(blurredView, LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, LayoutHelper.MATCH_PARENT));

        createBirthdayEffect();
        createFloatingActionButton(getContext());

        if (myProfile) {
            contentView.addView(bottomButtonsContainer, LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, 72 + (1 / AndroidUtilities.density), Gravity.BOTTOM | Gravity.FILL_HORIZONTAL));
        }

        if (openGifts || openCommonChats) {
            AndroidUtilities.runOnUIThread(this::scrollToSharedMedia);
        }

        return fragmentView;
    }

    private void updateBottomButtonY() {
        if (bottomButtonsContainer == null) {
            return;
        }
        bottomButtonsContainer.setTranslationY(sharedMediaLayout != null && sharedMediaLayout.isAttachedToWindow() ? Math.max(0, dp(72 + 64 + 48) - (listView.getMeasuredHeight() - sharedMediaLayout.getY())) : dp(72));
        final Bulletin bulletin = Bulletin.getVisibleBulletin();
        if (bulletin != null) {
            bulletin.updatePosition();
        }
    }

    FrameLayout floatingButtonContainer;
    RLottieImageView floatingButton;
    boolean floatingHidden;
    float floatingButtonHideProgress;
    boolean showBoostsAlert;

    private final AccelerateDecelerateInterpolator floatingInterpolator = new AccelerateDecelerateInterpolator();

    private void checkCanSendStoryForPosting() {
        TLRPC.Chat chat = MessagesController.getInstance(currentAccount).getChat(chatId);
        if (!ChatObject.isBoostSupported(chat)) {
            return;
        }
        StoriesController storiesController = getMessagesController().getStoriesController();
        waitCanSendStoryRequest = true;
        storiesController.canSendStoryFor(getDialogId(), canSend -> {
            waitCanSendStoryRequest = false;
            showBoostsAlert = !canSend;
            hideFloatingButton(!(sharedMediaLayout == null || sharedMediaLayout.getClosestTab() == SharedMediaLayout.TAB_STORIES || sharedMediaLayout.getClosestTab() == SharedMediaLayout.TAB_ARCHIVED_STORIES));
        }, false, resourcesProvider);
    }

    private void updateAvatarRoundRadius() {
        avatarImage.setRoundRadius((int) AndroidUtilities.lerp(getSmallAvatarRoundRadius(), 0f, currentExpandAnimatorValue));
    }

    private void createFloatingActionButton(Context context) {
        if (!getMessagesController().storiesEnabled()) {
            return;
        }
        if (getDialogId() > 0L) {
            return;
        }
        TLRPC.Chat currentChat = MessagesController.getInstance(currentAccount).getChat(chatId);
        if (!ChatObject.isBoostSupported(currentChat)) {
            return;
        }
        StoriesController storiesController = getMessagesController().getStoriesController();
        if (!storiesController.canPostStories(getDialogId())) {
            return;
        } else {
            checkCanSendStoryForPosting();
        }
        long dialogId = getDialogId();
        floatingButtonContainer = new FrameLayout(context);
        floatingButtonContainer.setVisibility(View.VISIBLE);
        contentView.addView(floatingButtonContainer, LayoutHelper.createFrame((Build.VERSION.SDK_INT >= 21 ? 56 : 60), (Build.VERSION.SDK_INT >= 21 ? 56 : 60), (LocaleController.isRTL ? Gravity.LEFT : Gravity.RIGHT) | Gravity.BOTTOM, LocaleController.isRTL ? 14 : 0, 0, LocaleController.isRTL ? 0 : 14, 14));
        floatingButtonContainer.setOnClickListener(v -> {
            if (showBoostsAlert) {
                if (loadingBoostsStats) {
                    return;
                }
                MessagesController messagesController = MessagesController.getInstance(currentAccount);
                loadingBoostsStats = true;
                messagesController.getBoostsController().getBoostsStats(dialogId, boostsStatus -> {
                    loadingBoostsStats = false;
                    if (boostsStatus == null) {
                        return;
                    }
                    messagesController.getBoostsController().userCanBoostChannel(dialogId, boostsStatus, canApplyBoost -> {
                        if (canApplyBoost == null) {
                            return;
                        }
                        BaseFragment lastFragment = LaunchActivity.getLastFragment();
                        LimitReachedBottomSheet.openBoostsForPostingStories(lastFragment, dialogId, canApplyBoost, boostsStatus, () -> {
                            TLRPC.Chat chat = getMessagesController().getChat(chatId);
                            presentFragment(StatisticActivity.create(chat));
                        });
                    });
                });
                return;
            }
            StoryRecorder recorder = StoryRecorder.getInstance(getParentActivity(), currentAccount)
                    .selectedPeerId(getDialogId())
                    .canChangePeer(false)
                    .closeToWhenSent(new StoryRecorder.ClosingViewProvider() {
                        @Override
                        public void preLayout(long dialogId, Runnable runnable) {
                            avatarImage.setHasStories(needInsetForStories());
                            if (dialogId == getDialogId()) {
                                collapseAvatarInstant();
                            }
                            AndroidUtilities.runOnUIThread(runnable, 30);
                        }

                        @Override
                        public StoryRecorder.SourceView getView(long dialogId) {
                            if (dialogId != getDialogId()) {
                                return null;
                            }
                            updateAvatarRoundRadius();
                            return StoryRecorder.SourceView.fromAvatarImage(avatarImage, ChatObject.isForum(currentChat));
                        }
                    });
            recorder.open(StoryRecorder.SourceView.fromFloatingButton(floatingButtonContainer), true);
        });

        floatingButton = new RLottieImageView(context);
        floatingButton.setScaleType(ImageView.ScaleType.CENTER);
        floatingButton.setColorFilter(new PorterDuffColorFilter(Theme.getColor(Theme.key_chats_actionIcon), PorterDuff.Mode.MULTIPLY));
        if (Build.VERSION.SDK_INT >= 21) {
            StateListAnimator animator = new StateListAnimator();
            animator.addState(new int[]{android.R.attr.state_pressed}, ObjectAnimator.ofFloat(floatingButtonContainer, View.TRANSLATION_Z, AndroidUtilities.dp(2), AndroidUtilities.dp(4)).setDuration(200));
            animator.addState(new int[]{}, ObjectAnimator.ofFloat(floatingButtonContainer, View.TRANSLATION_Z, AndroidUtilities.dp(4), AndroidUtilities.dp(2)).setDuration(200));
            floatingButtonContainer.setStateListAnimator(animator);
            floatingButtonContainer.setOutlineProvider(new ViewOutlineProvider() {
                @SuppressLint("NewApi")
                @Override
                public void getOutline(View view, Outline outline) {
                    outline.setOval(0, 0, AndroidUtilities.dp(56), AndroidUtilities.dp(56));
                }
            });
        }
        floatingButtonContainer.addView(floatingButton, LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, LayoutHelper.MATCH_PARENT));
        floatingButton.setAnimation(R.raw.write_contacts_fab_icon_camera, 56, 56);
        floatingButtonContainer.setContentDescription(LocaleController.getString(R.string.AccDescrCaptureStory));
        updateFloatingButtonColor();

        floatingHidden = true;
        floatingButtonHideProgress = 1.0f;
        updateFloatingButtonOffset();
    }

    private void collapseAvatarInstant() {
        if (allowPullingDown && currentExpandAnimatorValue > 0) {
            layoutManager.scrollToPositionWithOffset(0, AndroidUtilities.dp(88) - listView.getPaddingTop());
            listView.post(() -> {
                needLayout(true);
                if (expandAnimator.isRunning()) {
                    expandAnimator.cancel();
                }
                setAvatarExpandProgress(1f);
            });
        }
    }

    private void updateFloatingButtonColor() {
        if (getParentActivity() == null) {
            return;
        }
        Drawable drawable;
        if (floatingButtonContainer != null) {
            drawable = Theme.createSimpleSelectorCircleDrawable(AndroidUtilities.dp(56), dontApplyPeerColor(Theme.getColor(Theme.key_chats_actionBackground), false), dontApplyPeerColor(Theme.getColor(Theme.key_chats_actionPressedBackground), false));
            if (Build.VERSION.SDK_INT < 21) {
                Drawable shadowDrawable = ContextCompat.getDrawable(getParentActivity(), R.drawable.floating_shadow).mutate();
                shadowDrawable.setColorFilter(new PorterDuffColorFilter(0xff000000, PorterDuff.Mode.MULTIPLY));
                CombinedDrawable combinedDrawable = new CombinedDrawable(shadowDrawable, drawable, 0, 0);
                combinedDrawable.setIconSize(AndroidUtilities.dp(56), AndroidUtilities.dp(56));
                drawable = combinedDrawable;
            }
            floatingButtonContainer.setBackground(drawable);
        }
    }

    public void hideFloatingButton(boolean hide) {
        TLRPC.User bot = getMessagesController().getUser(userId);
        if (bot != null && bot.bot && bot.bot_can_edit && bot.bot_has_main_app) {
            StoriesController.BotPreviewsList list = (StoriesController.BotPreviewsList) getMessagesController().getStoriesController().getStoriesList(userId, StoriesController.StoriesList.TYPE_BOTS);
            ArrayList<StoriesController.UploadingStory> uploadingStories = getMessagesController().getStoriesController().getUploadingStories(userId);
            if (list != null && list.getCount() + (uploadingStories == null ? 0 : uploadingStories.size()) >= getMessagesController().botPreviewMediasMax) {
                hide = true;
            }
        }
        if (floatingHidden == hide || floatingButtonContainer == null || waitCanSendStoryRequest) {
            return;
        }
        floatingHidden = hide;
        AnimatorSet animatorSet = new AnimatorSet();
        ValueAnimator valueAnimator = ValueAnimator.ofFloat(floatingButtonHideProgress, floatingHidden ? 1f : 0f);
        valueAnimator.addUpdateListener(animation -> {
            floatingButtonHideProgress = (float) animation.getAnimatedValue();
            updateFloatingButtonOffset();
        });
        animatorSet.playTogether(valueAnimator);
        animatorSet.setDuration(300);
        animatorSet.setInterpolator(floatingInterpolator);
        floatingButtonContainer.setClickable(!hide);
        animatorSet.start();
    }

    private void updateFloatingButtonOffset() {
        if (floatingButtonContainer != null) {
            floatingButtonContainer.setTranslationY(AndroidUtilities.dp(100) * floatingButtonHideProgress);
        }
    }

    private boolean expandAvatar() {
        if (!AndroidUtilities.isTablet() && !isInLandscapeMode && avatarImage.getImageReceiver().hasNotThumb() && !AndroidUtilities.isAccessibilityScreenReaderEnabled()) {
            openingAvatar = true;
            allowPullingDown = true;
            View child = null;
            for (int i = 0; i < listView.getChildCount(); i++) {
                if (listView.getChildAdapterPosition(listView.getChildAt(i)) == 0) {
                    child = listView.getChildAt(i);
                    break;
                }
            }
            if (child != null) {
                RecyclerView.ViewHolder holder = listView.findContainingViewHolder(child);
                if (holder != null) {
                    Integer offset = positionToOffset.get(holder.getAdapterPosition());
                    if (offset != null) {
                        listView.smoothScrollBy(0, -(offset + (listView.getPaddingTop() - child.getTop() - actionBar.getMeasuredHeight())), CubicBezierInterpolator.EASE_OUT_QUINT);
                        return true;
                    }
                }
            }
        }
        return false;
    }

    private void setAvatarExpandProgress(float animatedFracture) {
        final int newTop = ActionBar.getCurrentActionBarHeight() + (actionBar.getOccupyStatusBar() ? AndroidUtilities.statusBarHeight : 0);
        final float value = currentExpandAnimatorValue = AndroidUtilities.lerp(expandAnimatorValues, currentExpanAnimatorFracture = animatedFracture);
        checkPhotoDescriptionAlpha();
        avatarContainer.setScaleX(avatarScale);
        avatarContainer.setScaleY(avatarScale);
        avatarContainer.setTranslationX(AndroidUtilities.lerp(avatarX, 0f, value));
        avatarContainer.setTranslationY(AndroidUtilities.lerp((float) Math.ceil(avatarY), 0f, value));
        avatarImage.setRoundRadius((int) AndroidUtilities.lerp(getSmallAvatarRoundRadius(), 0f, value));
        if (storyView != null) {
            storyView.setExpandProgress(value);
        }
        if (giftsView != null) {
            giftsView.setExpandProgress(value);
        }
        if (searchItem != null) {
            searchItem.setAlpha(1.0f - value);
            searchItem.setScaleY(1.0f - value);
            searchItem.setVisibility(View.VISIBLE);
            searchItem.setClickable(searchItem.getAlpha() > .5f);
            if (qrItem != null) {
                float translation = AndroidUtilities.dp(48) * value;
//                    if (searchItem.getVisibility() == View.VISIBLE)
//                        translation += AndroidUtilities.dp(48);
                qrItem.setTranslationX(translation);
                avatarsViewPagerIndicatorView.setTranslationX(translation - AndroidUtilities.dp(48));
            }
        }

        if (extraHeight > AndroidUtilities.dp(88f) && expandProgress < 0.33f) {
            refreshNameAndOnlineXY();
        }

        if (scamDrawable != null) {
            scamDrawable.setColor(ColorUtils.blendARGB(getThemedColor(Theme.key_avatar_subtitleInProfileBlue), Color.argb(179, 255, 255, 255), value));
        }

        if (lockIconDrawable != null) {
            lockIconDrawable.setColorFilter(ColorUtils.blendARGB(getThemedColor(Theme.key_chat_lockIcon), Color.WHITE, value), PorterDuff.Mode.MULTIPLY);
        }

        if (verifiedCrossfadeDrawable[0] != null) {
            verifiedCrossfadeDrawable[0].setProgress(value);
        }
        if (verifiedCrossfadeDrawable[1] != null) {
            verifiedCrossfadeDrawable[1].setProgress(value);
        }

        if (premiumCrossfadeDrawable[0] != null) {
            premiumCrossfadeDrawable[0].setProgress(value);
        }
        if (premiumCrossfadeDrawable[1] != null) {
            premiumCrossfadeDrawable[1].setProgress(value);
        }

        updateEmojiStatusDrawableColor(value);

        final float k = AndroidUtilities.dpf2(8f);

        final float nameTextViewXEnd = AndroidUtilities.dpf2(18f) - nameTextView[1].getLeft();
        final float nameTextViewYEnd = newTop + extraHeight - AndroidUtilities.dpf2(38f) - nameTextView[1].getBottom();
        final float nameTextViewCx = k + nameX + (nameTextViewXEnd - nameX) / 2f;
        final float nameTextViewCy = k + nameY + (nameTextViewYEnd - nameY) / 2f;
        final float nameTextViewX = (1 - value) * (1 - value) * nameX + 2 * (1 - value) * value * nameTextViewCx + value * value * nameTextViewXEnd;
        final float nameTextViewY = (1 - value) * (1 - value) * nameY + 2 * (1 - value) * value * nameTextViewCy + value * value * nameTextViewYEnd;

        final float onlineTextViewXEnd = AndroidUtilities.dpf2(16f) - onlineTextView[1].getLeft();
        final float onlineTextViewYEnd = newTop + extraHeight - AndroidUtilities.dpf2(18f) - onlineTextView[1].getBottom();
        final float onlineTextViewCx = k + onlineX + (onlineTextViewXEnd - onlineX) / 2f;
        final float onlineTextViewCy = k + onlineY + (onlineTextViewYEnd - onlineY) / 2f;
        final float onlineTextViewX = (1 - value) * (1 - value) * onlineX + 2 * (1 - value) * value * onlineTextViewCx + value * value * onlineTextViewXEnd;
        final float onlineTextViewY = (1 - value) * (1 - value) * onlineY + 2 * (1 - value) * value * onlineTextViewCy + value * value * onlineTextViewYEnd;

        nameTextView[1].setTranslationX(nameTextViewX);
        nameTextView[1].setTranslationY(nameTextViewY);
        onlineTextView[1].setTranslationX(onlineTextViewX + customPhotoOffset);
        onlineTextView[1].setTranslationY(onlineTextViewY);
        mediaCounterTextView.setTranslationX(onlineTextViewX);
        mediaCounterTextView.setTranslationY(onlineTextViewY);
        final Object onlineTextViewTag = onlineTextView[1].getTag();
        int statusColor;
        boolean online = false;
        if (onlineTextViewTag instanceof Integer) {
            statusColor = getThemedColor((Integer) onlineTextViewTag);
            online = (Integer) onlineTextViewTag == Theme.key_profile_status;
        } else {
            statusColor = getThemedColor(Theme.key_avatar_subtitleInProfileBlue);
        }
        onlineTextView[1].setTextColor(ColorUtils.blendARGB(applyPeerColor(statusColor, true, online), 0xB3FFFFFF, value));
        if (extraHeight > AndroidUtilities.dp(88f)) {
            nameTextView[1].setPivotY(AndroidUtilities.lerp(0, nameTextView[1].getMeasuredHeight(), value));
            nameTextView[1].setScaleX(AndroidUtilities.lerp(1.12f, 1.67f, value));
            nameTextView[1].setScaleY(AndroidUtilities.lerp(1.12f, 1.67f, value));
        }
        if (showStatusButton != null) {
            showStatusButton.setBackgroundColor(ColorUtils.blendARGB(Theme.multAlpha(Theme.adaptHSV(actionBarBackgroundColor, +0.18f, -0.1f), 0.5f), 0x23ffffff, currentExpandAnimatorValue));
        }

        needLayoutText(Math.min(1f, extraHeight / AndroidUtilities.dp(88f)));

        nameTextView[1].setTextColor(ColorUtils.blendARGB(peerColor != null ? Color.WHITE : getThemedColor(Theme.key_profile_title), Color.WHITE, currentExpandAnimatorValue));
        actionBar.setItemsColor(ColorUtils.blendARGB(peerColor != null ? Color.WHITE : getThemedColor(Theme.key_actionBarDefaultIcon), Color.WHITE, value), false);
        actionBar.setMenuOffsetSuppressed(true);

        avatarImage.setForegroundAlpha(value);

        final FrameLayout.LayoutParams params = (FrameLayout.LayoutParams) avatarContainer.getLayoutParams();
        params.width = (int) AndroidUtilities.lerp(AndroidUtilities.dpf2(42f), listView.getMeasuredWidth() / avatarScale, value);
        params.height = (int) AndroidUtilities.lerp(AndroidUtilities.dpf2(42f), (extraHeight + newTop) / avatarScale, value);
        params.leftMargin = (int) AndroidUtilities.lerp(AndroidUtilities.dpf2(64f), 0f, value);
        avatarContainer.requestLayout();

        updateCollectibleHint();
    }

    private int getSmallAvatarRoundRadius() {
        if (chatId != 0) {
            TLRPC.Chat chatLocal = getMessagesController().getChat(chatId);
            if (ChatObject.isForum(chatLocal)) {
                return AndroidUtilities.dp(needInsetForStories() ? 11 : 16);
            }
        }
        return AndroidUtilities.dp(21);
    }

    private void updateTtlIcon() {
        if (ttlIconView == null) {
            return;
        }
        boolean visible = false;
        if (currentEncryptedChat == null) {
            if (userInfo != null && userInfo.ttl_period > 0) {
                visible = true;
            } else if (chatInfo != null && ChatObject.canUserDoAdminAction(currentChat, ChatObject.ACTION_DELETE_MESSAGES) && chatInfo.ttl_period > 0) {
                visible = true;
            }
        }
        AndroidUtilities.updateViewVisibilityAnimated(ttlIconView, visible, 0.8f, fragmentOpened);
    }

    public long getDialogId() {
        if (dialogId != 0) {
            return dialogId;
        } else if (userId != 0) {
            return userId;
        } else {
            return -chatId;
        }
    }

    public void getEmojiStatusLocation(Rect rect) {
        if (nameTextView[1] == null) {
            return;
        }
        if (nameTextView[1].getRightDrawable() == null) {
            rect.set(nameTextView[1].getWidth() - 1, nameTextView[1].getHeight() / 2 - 1, nameTextView[1].getWidth() + 1, nameTextView[1].getHeight() / 2 + 1);
            return;
        }
        rect.set(nameTextView[1].getRightDrawable().getBounds());
        rect.offset((int) (rect.centerX() * (nameTextView[1].getScaleX() - 1f)), 0);
        rect.offset((int) nameTextView[1].getX(), (int) nameTextView[1].getY());
    }

    public void goToForum() {
        if (getParentLayout() != null && getParentLayout().getFragmentStack() != null) {
            for (int i = 0; i < getParentLayout().getFragmentStack().size(); ++i) {
                BaseFragment fragment = getParentLayout().getFragmentStack().get(i);
                if (fragment instanceof DialogsActivity) {
                    if (((DialogsActivity) fragment).rightSlidingDialogContainer != null) {
                        BaseFragment previewFragment = ((DialogsActivity) fragment).rightSlidingDialogContainer.getFragment();
                        if (previewFragment instanceof TopicsFragment && ((TopicsFragment) previewFragment).getDialogId() == getDialogId()) {
                            ((DialogsActivity) fragment).rightSlidingDialogContainer.finishPreview();
                        }
                    }
                } else if (fragment instanceof ChatActivity) {
                    if (((ChatActivity) fragment).getDialogId() == getDialogId()) {
                        getParentLayout().removeFragmentFromStack(fragment);
                        i--;
                    }
                } else if (fragment instanceof TopicsFragment) {
                    if (((TopicsFragment) fragment).getDialogId() == getDialogId()) {
                        getParentLayout().removeFragmentFromStack(fragment);
                        i--;
                    }
                } else if (fragment instanceof ProfileActivity) {
                    if (fragment != this && ((ProfileActivity) fragment).getDialogId() == getDialogId() && ((ProfileActivity) fragment).isTopic) {
                        getParentLayout().removeFragmentFromStack(fragment);
                        i--;
                    }
                }
            }
        }

        playProfileAnimation = 0;

        Bundle args = new Bundle();
        args.putLong("chat_id", chatId);
        presentFragment(TopicsFragment.getTopicsOrChat(this, args));
    }

    private SelectAnimatedEmojiDialog.SelectAnimatedEmojiDialogWindow selectAnimatedEmojiDialog;
    public void showStatusSelect() {
        if (selectAnimatedEmojiDialog != null) {
            return;
        }
        final SelectAnimatedEmojiDialog.SelectAnimatedEmojiDialogWindow[] popup = new SelectAnimatedEmojiDialog.SelectAnimatedEmojiDialogWindow[1];
        int xoff, yoff;
        getEmojiStatusLocation(AndroidUtilities.rectTmp2);
        int topMarginDp = nameTextView[1].getScaleX() < 1.5f ? 16 : 32;
        yoff = -(avatarContainer2.getHeight() - AndroidUtilities.rectTmp2.centerY()) - AndroidUtilities.dp(topMarginDp);
        int popupWidth = (int) Math.min(AndroidUtilities.dp(340 - 16), AndroidUtilities.displaySize.x * .95f);
        int ecenter = AndroidUtilities.rectTmp2.centerX();
        xoff = MathUtils.clamp(ecenter - popupWidth / 2, 0, AndroidUtilities.displaySize.x - popupWidth);
        ecenter -= xoff;
        SelectAnimatedEmojiDialog popupLayout = new SelectAnimatedEmojiDialog(this, getContext(), true, Math.max(0, ecenter), currentChat == null ? SelectAnimatedEmojiDialog.TYPE_EMOJI_STATUS : SelectAnimatedEmojiDialog.TYPE_EMOJI_STATUS_CHANNEL, true, resourcesProvider, topMarginDp) {
            @Override
            protected boolean willApplyEmoji(View view, Long documentId, TLRPC.Document document, TL_stars.TL_starGiftUnique gift, Integer until) {
                if (gift != null) {
                    final TL_stars.SavedStarGift savedStarGift = StarsController.getInstance(currentAccount).findUserStarGift(gift.id);
                    return savedStarGift == null || MessagesController.getGlobalMainSettings().getInt("statusgiftpage", 0) >= 2;
                }
                return true;
            }

            @Override
            public long getDialogId() {
                return ProfileActivity.this.getDialogId();
            }

            @Override
            protected void onEmojiSelected(View emojiView, Long documentId, TLRPC.Document document, TL_stars.TL_starGiftUnique gift, Integer until) {
                final TLRPC.EmojiStatus emojiStatus;
                if (gift != null) {
                    final TL_stars.SavedStarGift savedStarGift = StarsController.getInstance(currentAccount).findUserStarGift(gift.id);
                    if (savedStarGift != null && MessagesController.getGlobalMainSettings().getInt("statusgiftpage", 0) < 2) {
                        MessagesController.getGlobalMainSettings().edit().putInt("statusgiftpage", MessagesController.getGlobalMainSettings().getInt("statusgiftpage", 0) + 1).apply();
                        new StarGiftSheet(getContext(), currentAccount, UserConfig.getInstance(currentAccount).getClientUserId(), resourcesProvider)
                            .set(savedStarGift, null)
                            .setupWearPage()
                            .show();
                        if (popup[0] != null) {
                            selectAnimatedEmojiDialog = null;
                            popup[0].dismiss();
                        }
                        return;
                    }
                    final TLRPC.TL_inputEmojiStatusCollectible status = new TLRPC.TL_inputEmojiStatusCollectible();
                    status.collectible_id = gift.id;
                    if (until != null) {
                        status.flags |= 1;
                        status.until = until;
                    }
                    emojiStatus = status;
                } else if (documentId == null) {
                    emojiStatus = new TLRPC.TL_emojiStatusEmpty();
                } else {
                    final TLRPC.TL_emojiStatus status = new TLRPC.TL_emojiStatus();
                    status.document_id = documentId;
                    if (until != null) {
                        status.flags |= 1;
                        status.until = until;
                    }
                    emojiStatus = status;
                }
                emojiStatusGiftId = gift != null ? gift.id : null;
                getMessagesController().updateEmojiStatus(currentChat == null ? 0 : -currentChat.id, emojiStatus, gift);
                for (int a = 0; a < 2; ++a) {
                    if (emojiStatusDrawable[a] != null) {
                        if (documentId == null && currentChat == null) {
                            emojiStatusDrawable[a].set(getPremiumCrossfadeDrawable(a), true);
                        } else if (documentId != null) {
                            emojiStatusDrawable[a].set(documentId, true);
                        } else {
                            emojiStatusDrawable[a].set((Drawable) null, true);
                        }
                        emojiStatusDrawable[a].setParticles(gift != null, true);
                    }
                }
                if (documentId != null) {
                    animatedStatusView.animateChange(ReactionsLayoutInBubble.VisibleReaction.fromCustomEmoji(documentId));
                }
                updateEmojiStatusDrawableColor();
                updateEmojiStatusEffectPosition();
                if (popup[0] != null) {
                    selectAnimatedEmojiDialog = null;
                    popup[0].dismiss();
                }
            }
        };
        TLRPC.User user = getMessagesController().getUser(userId);
        if (user != null) {
            popupLayout.setExpireDateHint(DialogObject.getEmojiStatusUntil(user.emoji_status));
        }
        if (emojiStatusGiftId != null) {
            popupLayout.setSelected(emojiStatusGiftId);
        } else {
            popupLayout.setSelected(emojiStatusDrawable[1] != null && emojiStatusDrawable[1].getDrawable() instanceof AnimatedEmojiDrawable ? ((AnimatedEmojiDrawable) emojiStatusDrawable[1].getDrawable()).getDocumentId() : null);
        }
        popupLayout.setSaveState(3);
        popupLayout.setScrimDrawable(emojiStatusDrawable[1], nameTextView[1]);
        popup[0] = selectAnimatedEmojiDialog = new SelectAnimatedEmojiDialog.SelectAnimatedEmojiDialogWindow(popupLayout, LayoutHelper.WRAP_CONTENT, LayoutHelper.WRAP_CONTENT) {
            @Override
            public void dismiss() {
                super.dismiss();
                selectAnimatedEmojiDialog = null;
            }
        };
        int[] loc = new int[2];
        if (nameTextView[1] != null) {
            nameTextView[1].getLocationOnScreen(loc);
        }
        popup[0].showAsDropDown(fragmentView, xoff, yoff, Gravity.TOP | Gravity.LEFT);
        popup[0].dimBehind();
    }

    public TLRPC.Chat getCurrentChat() {
        return currentChat;
    }

    public TLRPC.UserFull getUserInfo() {
        return userInfo;
    }

    @Override
    public boolean isFragmentOpened() {
        return isFragmentOpened;
    }

    private void openAvatar() {
        if (listView.getScrollState() == RecyclerView.SCROLL_STATE_DRAGGING) {
            return;
        }
        if (userId != 0) {
            TLRPC.User user = getMessagesController().getUser(userId);
            if (user.photo != null && user.photo.photo_big != null) {
                PhotoViewer.getInstance().setParentActivity(ProfileActivity.this);
                if (user.photo.dc_id != 0) {
                    user.photo.photo_big.dc_id = user.photo.dc_id;
                }
                PhotoViewer.getInstance().openPhoto(user.photo.photo_big, provider);
            }
        } else if (chatId != 0) {
            TLRPC.Chat chat = getMessagesController().getChat(chatId);
            if (chat.photo != null && chat.photo.photo_big != null) {
                PhotoViewer.getInstance().setParentActivity(ProfileActivity.this);
                if (chat.photo.dc_id != 0) {
                    chat.photo.photo_big.dc_id = chat.photo.dc_id;
                }
                ImageLocation videoLocation;
                if (chatInfo != null && (chatInfo.chat_photo instanceof TLRPC.TL_photo) && !chatInfo.chat_photo.video_sizes.isEmpty()) {
                    videoLocation = ImageLocation.getForPhoto(chatInfo.chat_photo.video_sizes.get(0), chatInfo.chat_photo);
                } else {
                    videoLocation = null;
                }
                PhotoViewer.getInstance().openPhotoWithVideo(chat.photo.photo_big, videoLocation, provider);
            }
        }
    }

    public void onWriteButtonClick() {
        if (userId != 0) {
            if (imageUpdater != null) {
                TLRPC.User user = MessagesController.getInstance(currentAccount).getUser(UserConfig.getInstance(currentAccount).getClientUserId());
                if (user == null) {
                    user = UserConfig.getInstance(currentAccount).getCurrentUser();
                }
                if (user == null) {
                    return;
                }
                imageUpdater.openMenu(user.photo != null && user.photo.photo_big != null && !(user.photo instanceof TLRPC.TL_userProfilePhotoEmpty), () -> {
                    MessagesController.getInstance(currentAccount).deleteUserPhoto(null);
                    cameraDrawable.setCurrentFrame(0);
                    cellCameraDrawable.setCurrentFrame(0);
                }, dialog -> {
                    if (!imageUpdater.isUploadingImage()) {
                        cameraDrawable.setCustomEndFrame(86);
                        cellCameraDrawable.setCustomEndFrame(86);
                        writeButton.playAnimation();
                        if (setAvatarCell != null) {
                            setAvatarCell.getImageView().playAnimation();
                        }
                    } else {
                        cameraDrawable.setCurrentFrame(0, false);
                        cellCameraDrawable.setCurrentFrame(0, false);
                    }
                }, 0);
                cameraDrawable.setCurrentFrame(0);
                cameraDrawable.setCustomEndFrame(43);
                cellCameraDrawable.setCurrentFrame(0);
                cellCameraDrawable.setCustomEndFrame(43);
                writeButton.playAnimation();
                if (setAvatarCell != null) {
                    setAvatarCell.getImageView().playAnimation();
                }
            } else {
                if (playProfileAnimation != 0 && parentLayout != null && parentLayout.getFragmentStack() != null && parentLayout.getFragmentStack().size() >= 2 && parentLayout.getFragmentStack().get(parentLayout.getFragmentStack().size() - 2) instanceof ChatActivity) {
                    finishFragment();
                } else {
                    TLRPC.User user = getMessagesController().getUser(userId);
                    if (user == null || user instanceof TLRPC.TL_userEmpty) {
                        return;
                    }
                    Bundle args = new Bundle();
                    args.putLong("user_id", userId);
                    if (!getMessagesController().checkCanOpenChat(args, ProfileActivity.this)) {
                        return;
                    }
                    boolean removeFragment = arguments.getBoolean("removeFragmentOnChatOpen", true);
                    if (!AndroidUtilities.isTablet() && removeFragment) {
                        getNotificationCenter().removeObserver(ProfileActivity.this, NotificationCenter.closeChats);
                        getNotificationCenter().postNotificationName(NotificationCenter.closeChats);
                    }
                    int distance = getArguments().getInt("nearby_distance", -1);
                    if (distance >= 0) {
                        args.putInt("nearby_distance", distance);
                    }
                    ChatActivity chatActivity = new ChatActivity(args);
                    chatActivity.setPreloadedSticker(getMediaDataController().getGreetingsSticker(), false);
                    presentFragment(chatActivity, removeFragment);
                    if (AndroidUtilities.isTablet()) {
                        finishFragment();
                    }
                }
            }
        } else {
            openDiscussion();
        }
    }

    public void openDiscussion() {
        if (chatInfo == null || chatInfo.linked_chat_id == 0) {
            return;
        }
        Bundle args = new Bundle();
        args.putLong("chat_id", chatInfo.linked_chat_id);
        if (!getMessagesController().checkCanOpenChat(args, ProfileActivity.this)) {
            return;
        }
        presentFragment(new ChatActivity(args));
    }

    public boolean onMemberClick(TLRPC.ChatParticipant participant, boolean isLong, View view) {
        return onMemberClick(participant, isLong, false, view);
    }

    public boolean onMemberClick(TLRPC.ChatParticipant participant, boolean isLong, boolean resultOnly) {
        return onMemberClick(participant, isLong, resultOnly, null);
    }

    public boolean onMemberClick(TLRPC.ChatParticipant participant, boolean isLong, boolean resultOnly, View view) {
        if (getParentActivity() == null) {
            return false;
        }
        if (isLong) {
            TLRPC.User user = getMessagesController().getUser(participant.user_id);
            if (user == null || participant.user_id == getUserConfig().getClientUserId()) {
                return false;
            }
            selectedUser = participant.user_id;
            boolean allowKick;
            boolean canEditAdmin;
            boolean canRestrict;
            boolean editingAdmin;
            final TLRPC.ChannelParticipant channelParticipant;

            if (ChatObject.isChannel(currentChat)) {
                channelParticipant = ((TLRPC.TL_chatChannelParticipant) participant).channelParticipant;
                TLRPC.User u = getMessagesController().getUser(participant.user_id);
                canEditAdmin = ChatObject.canAddAdmins(currentChat);
                if (canEditAdmin && (channelParticipant instanceof TLRPC.TL_channelParticipantCreator || channelParticipant instanceof TLRPC.TL_channelParticipantAdmin && !channelParticipant.can_edit)) {
                    canEditAdmin = false;
                }
                allowKick = canRestrict = ChatObject.canBlockUsers(currentChat) && (!(channelParticipant instanceof TLRPC.TL_channelParticipantAdmin || channelParticipant instanceof TLRPC.TL_channelParticipantCreator) || channelParticipant.can_edit);
                if (currentChat.gigagroup) {
                    canRestrict = false;
                }
                editingAdmin = channelParticipant instanceof TLRPC.TL_channelParticipantAdmin;
            } else {
                channelParticipant = null;
                allowKick = currentChat.creator || participant instanceof TLRPC.TL_chatParticipant && (ChatObject.canBlockUsers(currentChat) || participant.inviter_id == getUserConfig().getClientUserId());
                canEditAdmin = currentChat.creator;
                canRestrict = currentChat.creator;
                editingAdmin = participant instanceof TLRPC.TL_chatParticipantAdmin;
            }

            boolean result = (canEditAdmin || canRestrict || allowKick);
            if (resultOnly || !result) {
                return result;
            }

            Utilities.Callback<Integer> openRightsEdit = action -> {
                if (channelParticipant != null) {
                    openRightsEdit(action, user, participant, channelParticipant.admin_rights, channelParticipant.banned_rights, channelParticipant.rank, editingAdmin);
                } else {
                    openRightsEdit(action, user, participant, null, null, "", editingAdmin);
                }
            };

            ItemOptions.makeOptions(this, view)
                .setScrimViewBackground(new ColorDrawable(Theme.getColor(Theme.key_windowBackgroundWhite)))
                .addIf(canEditAdmin, R.drawable.msg_admins, editingAdmin ? LocaleController.getString(R.string.EditAdminRights) : LocaleController.getString(R.string.SetAsAdmin), () -> openRightsEdit.run(0))
                .addIf(canRestrict, R.drawable.msg_permissions, LocaleController.getString(R.string.ChangePermissions), () -> {
                    if (channelParticipant instanceof TLRPC.TL_channelParticipantAdmin || participant instanceof TLRPC.TL_chatParticipantAdmin) {
                        showDialog(
                            new AlertDialog.Builder(getParentActivity(), resourcesProvider)
                                .setTitle(LocaleController.getString(R.string.AppName))
                                .setMessage(formatString("AdminWillBeRemoved", R.string.AdminWillBeRemoved, ContactsController.formatName(user.first_name, user.last_name)))
                                .setPositiveButton(LocaleController.getString(R.string.OK), (dialog, which) -> openRightsEdit.run(1))
                                .setNegativeButton(LocaleController.getString(R.string.Cancel), null)
                                .create()
                        );
                    } else {
                        openRightsEdit.run(1);
                    }
                })
                .addIf(allowKick, R.drawable.msg_remove, LocaleController.getString(R.string.KickFromGroup), true, () -> {
                    kickUser(selectedUser, participant);
                })
                .setMinWidth(190)
                .show();
        } else {
            if (participant.user_id == getUserConfig().getClientUserId()) {
                return false;
            }
            Bundle args = new Bundle();
            args.putLong("user_id", participant.user_id);
            args.putBoolean("preload_messages", true);
            presentFragment(new ProfileActivity(args));
        }
        return true;
    }

    private void openRightsEdit(int action, TLRPC.User user, TLRPC.ChatParticipant participant, TLRPC.TL_chatAdminRights adminRights, TLRPC.TL_chatBannedRights bannedRights, String rank, boolean editingAdmin) {
        boolean[] needShowBulletin = new boolean[1];
        ChatRightsEditActivity fragment = new ChatRightsEditActivity(user.id, chatId, adminRights, currentChat.default_banned_rights, bannedRights, rank, action, true, false, null) {
            @Override
            public void onTransitionAnimationEnd(boolean isOpen, boolean backward) {
                if (!isOpen && backward && needShowBulletin[0] && BulletinFactory.canShowBulletin(ProfileActivity.this)) {
                    BulletinFactory.createPromoteToAdminBulletin(ProfileActivity.this, user.first_name).show();
                }
            }
        };
        fragment.setDelegate(new ChatRightsEditActivity.ChatRightsEditActivityDelegate() {
            @Override
            public void didSetRights(int rights, TLRPC.TL_chatAdminRights rightsAdmin, TLRPC.TL_chatBannedRights rightsBanned, String rank) {
                if (action == 0) {
                    if (participant instanceof TLRPC.TL_chatChannelParticipant) {
                        TLRPC.TL_chatChannelParticipant channelParticipant1 = ((TLRPC.TL_chatChannelParticipant) participant);
                        if (rights == 1) {
                            channelParticipant1.channelParticipant = new TLRPC.TL_channelParticipantAdmin();
                            channelParticipant1.channelParticipant.flags |= 4;
                        } else {
                            channelParticipant1.channelParticipant = new TLRPC.TL_channelParticipant();
                        }
                        channelParticipant1.channelParticipant.inviter_id = getUserConfig().getClientUserId();
                        channelParticipant1.channelParticipant.peer = new TLRPC.TL_peerUser();
                        channelParticipant1.channelParticipant.peer.user_id = participant.user_id;
                        channelParticipant1.channelParticipant.date = participant.date;
                        channelParticipant1.channelParticipant.banned_rights = rightsBanned;
                        channelParticipant1.channelParticipant.admin_rights = rightsAdmin;
                        channelParticipant1.channelParticipant.rank = rank;
                    } else if (participant != null) {
                        TLRPC.ChatParticipant newParticipant;
                        if (rights == 1) {
                            newParticipant = new TLRPC.TL_chatParticipantAdmin();
                        } else {
                            newParticipant = new TLRPC.TL_chatParticipant();
                        }
                        newParticipant.user_id = participant.user_id;
                        newParticipant.date = participant.date;
                        newParticipant.inviter_id = participant.inviter_id;
                        int index = chatInfo.participants.participants.indexOf(participant);
                        if (index >= 0) {
                            chatInfo.participants.participants.set(index, newParticipant);
                        }
                    }
                    if (rights == 1 && !editingAdmin) {
                        needShowBulletin[0] = true;
                    }
                } else if (action == 1) {
                    if (rights == 0) {
                        if (currentChat.megagroup && chatInfo != null && chatInfo.participants != null) {
                            boolean changed = false;
                            for (int a = 0; a < chatInfo.participants.participants.size(); a++) {
                                TLRPC.ChannelParticipant p = ((TLRPC.TL_chatChannelParticipant) chatInfo.participants.participants.get(a)).channelParticipant;
                                if (MessageObject.getPeerId(p.peer) == participant.user_id) {
                                    chatInfo.participants_count--;
                                    chatInfo.participants.participants.remove(a);
                                    changed = true;
                                    break;
                                }
                            }
                            if (chatInfo != null && chatInfo.participants != null) {
                                for (int a = 0; a < chatInfo.participants.participants.size(); a++) {
                                    TLRPC.ChatParticipant p = chatInfo.participants.participants.get(a);
                                    if (p.user_id == participant.user_id) {
                                        chatInfo.participants.participants.remove(a);
                                        changed = true;
                                        break;
                                    }
                                }
                            }
                            if (changed) {
                                updateOnlineCount(true);
                                updateRowsIds();
                                listAdapter.notifyDataSetChanged();
                            }
                        }
                    }
                }
            }

            @Override
            public void didChangeOwner(TLRPC.User user) {
                undoView.showWithAction(-chatId, currentChat.megagroup ? UndoView.ACTION_OWNER_TRANSFERED_GROUP : UndoView.ACTION_OWNER_TRANSFERED_CHANNEL, user);
            }
        });
        presentFragment(fragment);
    }

    public boolean processOnClickOrPress(final int position, final View view, final float x, final float y) {
        if (position == usernameRow || position == setUsernameRow) {
            final String username;
            final TLRPC.TL_username usernameObj;
            if (userId != 0) {
                final TLRPC.User user = getMessagesController().getUser(userId);
                String username1 = UserObject.getPublicUsername(user);
                if (user == null || username1 == null) {
                    return false;
                }
                username = username1;
                usernameObj = DialogObject.findUsername(username, user);
            } else if (chatId != 0) {
                final TLRPC.Chat chat = getMessagesController().getChat(chatId);
                if (chat == null || topicId == 0 && !ChatObject.isPublic(chat)) {
                    return false;
                }
                username = ChatObject.getPublicUsername(chat);
                usernameObj = DialogObject.findUsername(username, chat);
            } else {
                return false;
            }
            if (userId == 0) {
                TLRPC.Chat chat = getMessagesController().getChat(chatId);
                String link;
                if (ChatObject.isPublic(chat)) {
                    link = "https://" + getMessagesController().linkPrefix + "/" + ChatObject.getPublicUsername(chat) + (topicId != 0 ? "/" + topicId : "");
                } else {
                    link = "https://" + getMessagesController().linkPrefix + "/c/" + chat.id + (topicId != 0 ? "/" + topicId : "");
                }
                ShareAlert shareAlert = new ShareAlert(getParentActivity(), null, link, false, link, false) {
                    @Override
                    protected void onSend(LongSparseArray<TLRPC.Dialog> dids, int count, TLRPC.TL_forumTopic topic, boolean showToast) {
                        if (!showToast) return;
                        AndroidUtilities.runOnUIThread(() -> {
                            BulletinFactory.createInviteSentBulletin(getParentActivity(), contentView, dids.size(), dids.size() == 1 ? dids.valueAt(0).id : 0, count, getThemedColor(Theme.key_undo_background), getThemedColor(Theme.key_undo_infoColor)).show();
                        }, 250);
                    }
                };
                showDialog(shareAlert);
                if (usernameObj != null && !usernameObj.editable) {
                    TL_fragment.TL_getCollectibleInfo req = new TL_fragment.TL_getCollectibleInfo();
                    TL_fragment.TL_inputCollectibleUsername input = new TL_fragment.TL_inputCollectibleUsername();
                    input.username = usernameObj.username;
                    req.collectible = input;
                    int reqId = getConnectionsManager().sendRequest(req, (res, err) -> AndroidUtilities.runOnUIThread(() -> {
                        if (res instanceof TL_fragment.TL_collectibleInfo) {
                            TL_fragment.TL_collectibleInfo info = (TL_fragment.TL_collectibleInfo) res;
                            TLObject obj;
                            if (userId != 0) {
                                obj = getMessagesController().getUser(userId);
                            } else {
                                obj = getMessagesController().getChat(chatId);
                            }
                            final String usernameStr = "@" + usernameObj.username;
                            final String date = LocaleController.getInstance().getFormatterBoostExpired().format(new Date(info.purchase_date * 1000L));
                            final String cryptoAmount = BillingController.getInstance().formatCurrency(info.crypto_amount, info.crypto_currency);
                            final String amount = BillingController.getInstance().formatCurrency(info.amount, info.currency);
                            BulletinFactory.of(shareAlert.bulletinContainer2, resourcesProvider)
                                .createImageBulletin(
                                    R.drawable.filled_username,
                                    AndroidUtilities.withLearnMore(AndroidUtilities.replaceTags(formatString(R.string.FragmentChannelUsername, usernameStr, date, cryptoAmount, TextUtils.isEmpty(amount) ? "" : "("+amount+")")), () -> {
                                        Bulletin.hideVisible();
                                        Browser.openUrl(getContext(), info.url);
                                    })
                                )
                                .setOnClickListener(v -> {
                                    Bulletin.hideVisible();
                                    Browser.openUrl(getContext(), info.url);
                                })
                                .show(false);
                        } else {
                            BulletinFactory.showError(err);
                        }
                    }));
                    getConnectionsManager().bindRequestToGuid(reqId, getClassGuid());
                }
            } else {
                if (editRow(view, position)) return true;

                if (usernameObj != null && !usernameObj.editable) {
                    TL_fragment.TL_getCollectibleInfo req = new TL_fragment.TL_getCollectibleInfo();
                    TL_fragment.TL_inputCollectibleUsername input = new TL_fragment.TL_inputCollectibleUsername();
                    input.username = usernameObj.username;
                    req.collectible = input;
                    int reqId = getConnectionsManager().sendRequest(req, (res, err) -> AndroidUtilities.runOnUIThread(() -> {
                        if (res instanceof TL_fragment.TL_collectibleInfo) {
                            TLObject obj;
                            if (userId != 0) {
                                obj = getMessagesController().getUser(userId);
                            } else {
                                obj = getMessagesController().getChat(chatId);
                            }
                            FragmentUsernameBottomSheet.open(getContext(), FragmentUsernameBottomSheet.TYPE_USERNAME, usernameObj.username, obj, (TL_fragment.TL_collectibleInfo) res, getResourceProvider());
                        } else {
                            BulletinFactory.showError(err);
                        }
                    }));
                    getConnectionsManager().bindRequestToGuid(reqId, getClassGuid());
                    return true;
                }

                try {
                    android.content.ClipboardManager clipboard = (android.content.ClipboardManager) ApplicationLoader.applicationContext.getSystemService(Context.CLIPBOARD_SERVICE);
                    String text = "@" + username;
                    BulletinFactory.of(this).createCopyBulletin(LocaleController.getString(R.string.UsernameCopied), resourcesProvider).show();
                    android.content.ClipData clip = android.content.ClipData.newPlainText("label", text);
                    clipboard.setPrimaryClip(clip);
                } catch (Exception e) {
                    FileLog.e(e);
                }
            }
            return true;
        } else if (position == phoneRow || position == numberRow) {
            if (editRow(view, position)) return true;

            final TLRPC.User user = getMessagesController().getUser(userId);
            if (user == null || user.phone == null || user.phone.length() == 0 || getParentActivity() == null) {
                return false;
            }

            if (position == phoneRow && user.phone.startsWith("888")) {
                TL_fragment.TL_inputCollectiblePhone input = new TL_fragment.TL_inputCollectiblePhone();
                final String phone = input.phone = user.phone;
                TL_fragment.TL_getCollectibleInfo req = new TL_fragment.TL_getCollectibleInfo();
                req.collectible = input;
                int reqId = getConnectionsManager().sendRequest(req, (res, err) -> AndroidUtilities.runOnUIThread(() -> {
                    if (res instanceof TL_fragment.TL_collectibleInfo) {
                        FragmentUsernameBottomSheet.open(getContext(), FragmentUsernameBottomSheet.TYPE_PHONE, phone, user, (TL_fragment.TL_collectibleInfo) res, getResourceProvider());
                    } else {
                        BulletinFactory.showError(err);
                    }
                }));
                getConnectionsManager().bindRequestToGuid(reqId, getClassGuid());
                return true;
            }

            ArrayList<CharSequence> items = new ArrayList<>();
            ArrayList<Integer> actions = new ArrayList<>();
            List<Integer> icons = new ArrayList<>();
            if (position == phoneRow) {
                if (userInfo != null && userInfo.phone_calls_available) {
                    icons.add(R.drawable.msg_calls);
                    items.add(LocaleController.getString(R.string.CallViaTelegram));
                    actions.add(PHONE_OPTION_TELEGRAM_CALL);
                    if (Build.VERSION.SDK_INT >= 18 && userInfo.video_calls_available) {
                        icons.add(R.drawable.msg_videocall);
                        items.add(LocaleController.getString(R.string.VideoCallViaTelegram));
                        actions.add(PHONE_OPTION_TELEGRAM_VIDEO_CALL);
                    }
                }
                if (!isFragmentPhoneNumber) {
                    icons.add(R.drawable.msg_calls_regular);
                    items.add(LocaleController.getString(R.string.Call));
                    actions.add(PHONE_OPTION_CALL);
                }
            }
            icons.add(R.drawable.msg_copy);
            items.add(LocaleController.getString(R.string.Copy));
            actions.add(PHONE_OPTION_COPY);

            AtomicReference<ActionBarPopupWindow> popupWindowRef = new AtomicReference<>();
            ActionBarPopupWindow.ActionBarPopupWindowLayout popupLayout = new ActionBarPopupWindow.ActionBarPopupWindowLayout(getContext(), R.drawable.popup_fixed_alert, resourcesProvider) {
                Path path = new Path();

                @Override
                protected boolean drawChild(Canvas canvas, View child, long drawingTime) {
                    canvas.save();
                    path.rewind();
                    AndroidUtilities.rectTmp.set(child.getLeft(), child.getTop(), child.getRight(), child.getBottom());
                    path.addRoundRect(AndroidUtilities.rectTmp, AndroidUtilities.dp(6), AndroidUtilities.dp(6), Path.Direction.CW);
                    canvas.clipPath(path);
                    boolean draw = super.drawChild(canvas, child, drawingTime);
                    canvas.restore();
                    return draw;
                }
            };
            popupLayout.setFitItems(true);

            for (int i = 0; i < icons.size(); i++) {
                int action = actions.get(i);
                ActionBarMenuItem.addItem(popupLayout, icons.get(i), items.get(i), false, resourcesProvider).setOnClickListener(v -> {
                    popupWindowRef.get().dismiss();
                    switch (action) {
                        case PHONE_OPTION_CALL:
                            try {
                                Intent intent = new Intent(Intent.ACTION_DIAL, Uri.parse("tel:+" + user.phone));
                                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                getParentActivity().startActivityForResult(intent, 500);
                            } catch (Exception e) {
                                FileLog.e(e);
                            }
                            break;
                        case PHONE_OPTION_COPY:
                            try {
                                android.content.ClipboardManager clipboard = (android.content.ClipboardManager) ApplicationLoader.applicationContext.getSystemService(Context.CLIPBOARD_SERVICE);
                                android.content.ClipData clip = android.content.ClipData.newPlainText("label", "+" + user.phone);
                                clipboard.setPrimaryClip(clip);
                                if (AndroidUtilities.shouldShowClipboardToast()) {
                                    BulletinFactory.of(this).createCopyBulletin(LocaleController.getString(R.string.PhoneCopied)).show();
                                }
                            } catch (Exception e) {
                                FileLog.e(e);
                            }
                            break;
                        case PHONE_OPTION_TELEGRAM_CALL:
                        case PHONE_OPTION_TELEGRAM_VIDEO_CALL:
                            if (getParentActivity() == null) {
                                return;
                            }
                            VoIPHelper.startCall(user, action == PHONE_OPTION_TELEGRAM_VIDEO_CALL, userInfo != null && userInfo.video_calls_available, getParentActivity(), userInfo, getAccountInstance());
                            break;
                    }
                });
            }
            if (isFragmentPhoneNumber) {
                FrameLayout gap = new FrameLayout(getContext());
                gap.setBackgroundColor(Theme.getColor(Theme.key_actionBarDefaultSubmenuSeparator, resourcesProvider));
                popupLayout.addView(gap, LayoutHelper.createLinear(LayoutHelper.MATCH_PARENT, 8));

                TextView fragmentInfoView = new TextView(getContext());
                fragmentInfoView.setPadding(AndroidUtilities.dp(13), AndroidUtilities.dp(8), AndroidUtilities.dp(13), AndroidUtilities.dp(8));
                fragmentInfoView.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 13);
                fragmentInfoView.setTextColor(Theme.getColor(Theme.key_actionBarDefaultSubmenuItem, resourcesProvider));
                fragmentInfoView.setLinkTextColor(Theme.getColor(Theme.key_windowBackgroundWhiteValueText, resourcesProvider));
                fragmentInfoView.setBackground(Theme.createRadSelectorDrawable(Theme.getColor(Theme.key_dialogButtonSelector, resourcesProvider), 0,6));

                SpannableStringBuilder spanned = new SpannableStringBuilder(AndroidUtilities.replaceTags(LocaleController.getString(R.string.AnonymousNumberNotice)));

                int startIndex = TextUtils.indexOf(spanned, '*');
                int lastIndex = TextUtils.lastIndexOf(spanned, '*');
                if (startIndex != -1 && lastIndex != -1 && startIndex != lastIndex) {
                    spanned.replace(lastIndex, lastIndex + 1, "");
                    spanned.replace(startIndex, startIndex + 1, "");
                    spanned.setSpan(new TypefaceSpan(AndroidUtilities.bold()), startIndex, lastIndex - 1, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                    spanned.setSpan(new ForegroundColorSpan(fragmentInfoView.getLinkTextColors().getDefaultColor()), startIndex, lastIndex - 1, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                }
                fragmentInfoView.setText(spanned);
                fragmentInfoView.setOnClickListener(v -> {
                    try {
                        v.getContext().startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://fragment.com")));
                    } catch (ActivityNotFoundException e) {
                        FileLog.e(e);
                    }
                });

                gap.setTag(R.id.fit_width_tag, 1);
                fragmentInfoView.setTag(R.id.fit_width_tag, 1);
                popupLayout.addView(fragmentInfoView, LayoutHelper.createLinear(LayoutHelper.WRAP_CONTENT, LayoutHelper.WRAP_CONTENT));
            }

            ActionBarPopupWindow popupWindow = new ActionBarPopupWindow(popupLayout, LayoutHelper.WRAP_CONTENT, LayoutHelper.WRAP_CONTENT);
            popupWindow.setPauseNotifications(true);
            popupWindow.setDismissAnimationDuration(220);
            popupWindow.setOutsideTouchable(true);
            popupWindow.setClippingEnabled(true);
            popupWindow.setAnimationStyle(R.style.PopupContextAnimation);
            popupWindow.setFocusable(true);
            popupLayout.measure(View.MeasureSpec.makeMeasureSpec(AndroidUtilities.dp(1000), View.MeasureSpec.AT_MOST), View.MeasureSpec.makeMeasureSpec(AndroidUtilities.dp(1000), View.MeasureSpec.AT_MOST));
            popupWindow.setInputMethodMode(ActionBarPopupWindow.INPUT_METHOD_NOT_NEEDED);
            popupWindow.getContentView().setFocusableInTouchMode(true);
            popupWindowRef.set(popupWindow);

            float px = x, py = y;
            View v = view;
            while (v != getFragmentView() && v != null) {
                px += v.getX();
                py += v.getY();
                v = (View) v.getParent();
            }
            if (AndroidUtilities.isTablet()) {
                View pv = parentLayout.getView();
                if (pv != null) {
                    px += pv.getX() + pv.getPaddingLeft();
                    py += pv.getY() + pv.getPaddingTop();
                }
            }
            px -= popupLayout.getMeasuredWidth() / 2f;
            popupWindow.showAtLocation(getFragmentView(), 0, (int) px, (int) py);
            popupWindow.dimBehind();
            return true;
        } else if (position == channelInfoRow || position == userInfoRow || position == locationRow || position == bioRow) {
            if (position == bioRow && (userInfo == null || TextUtils.isEmpty(userInfo.about))) {
                return false;
            }
            if (editRow(view, position)) return true;
            if (view instanceof AboutLinkCell && ((AboutLinkCell) view).onClick()) {
                return false;
            }
            String text;
            if (position == locationRow) {
                text = chatInfo != null && chatInfo.location instanceof TLRPC.TL_channelLocation ? ((TLRPC.TL_channelLocation) chatInfo.location).address : null;
            } else if (position == channelInfoRow) {
                text = chatInfo != null ? chatInfo.about : null;
            } else {
                text = userInfo != null ? userInfo.about : null;
            }
            final String finalText = text;
            if (TextUtils.isEmpty(finalText)) {
                return false;
            }
            final String[] fromLanguage = new String[1];
            fromLanguage[0] = "und";
            final boolean translateButtonEnabled = MessagesController.getInstance(currentAccount).getTranslateController().isContextTranslateEnabled();
            final boolean[] withTranslate = new boolean[1];
            withTranslate[0] = position == bioRow || position == channelInfoRow || position == userInfoRow;
            final String toLang = LocaleController.getInstance().getCurrentLocale().getLanguage();
            Runnable showMenu = () -> {
                if (getParentActivity() == null) {
                    return;
                }
                CharSequence[] items = withTranslate[0] ? new CharSequence[]{LocaleController.getString(R.string.Copy), LocaleController.getString(R.string.TranslateMessage)} : new CharSequence[]{LocaleController.getString(R.string.Copy)};
                int[] icons = withTranslate[0] ? new int[] {R.drawable.msg_copy, R.drawable.msg_translate} : new int[] {R.drawable.msg_copy};

                AtomicReference<ActionBarPopupWindow> popupWindowRef = new AtomicReference<>();
                ActionBarPopupWindow.ActionBarPopupWindowLayout popupLayout = new ActionBarPopupWindow.ActionBarPopupWindowLayout(getContext(), R.drawable.popup_fixed_alert, resourcesProvider) {
                    Path path = new Path();

                    @Override
                    protected boolean drawChild(Canvas canvas, View child, long drawingTime) {
                        canvas.save();
                        path.rewind();
                        AndroidUtilities.rectTmp.set(child.getLeft(), child.getTop(), child.getRight(), child.getBottom());
                        path.addRoundRect(AndroidUtilities.rectTmp, AndroidUtilities.dp(6), AndroidUtilities.dp(6), Path.Direction.CW);
                        canvas.clipPath(path);
                        boolean draw = super.drawChild(canvas, child, drawingTime);
                        canvas.restore();
                        return draw;
                    }
                };
                popupLayout.setFitItems(true);

                for (int i = 0; i < icons.length; i++) {
                    int j = i;
                    ActionBarMenuItem.addItem(popupLayout, icons[i], items[i], false, resourcesProvider).setOnClickListener(v -> {
                        popupWindowRef.get().dismiss();
                        try {
                            if (j == 0) {
                                AndroidUtilities.addToClipboard(finalText);
                                if (position == bioRow) {
                                    BulletinFactory.of(this).createCopyBulletin(LocaleController.getString(R.string.BioCopied)).show();
                                } else {
                                    BulletinFactory.of(this).createCopyBulletin(LocaleController.getString(R.string.TextCopied)).show();
                                }
                            } else if (j == 1) {
                                TranslateAlert2.showAlert(fragmentView.getContext(), this, currentAccount, fromLanguage[0], toLang, finalText, null, false, span -> {
                                    if (span != null) {
                                        openUrl(span.getURL(), null);
                                        return true;
                                    }
                                    return false;
                                }, null);
                            }
                        } catch (Exception e) {
                            FileLog.e(e);
                        }
                    });
                }

                ActionBarPopupWindow popupWindow = new ActionBarPopupWindow(popupLayout, LayoutHelper.WRAP_CONTENT, LayoutHelper.WRAP_CONTENT);
                popupWindow.setPauseNotifications(true);
                popupWindow.setDismissAnimationDuration(220);
                popupWindow.setOutsideTouchable(true);
                popupWindow.setClippingEnabled(true);
                popupWindow.setAnimationStyle(R.style.PopupContextAnimation);
                popupWindow.setFocusable(true);
                popupLayout.measure(View.MeasureSpec.makeMeasureSpec(AndroidUtilities.dp(1000), View.MeasureSpec.AT_MOST), View.MeasureSpec.makeMeasureSpec(AndroidUtilities.dp(1000), View.MeasureSpec.AT_MOST));
                popupWindow.setInputMethodMode(ActionBarPopupWindow.INPUT_METHOD_NOT_NEEDED);
                popupWindow.getContentView().setFocusableInTouchMode(true);
                popupWindowRef.set(popupWindow);

                float px = x, py = y;
                View v = view;
                while (v != null && v != getFragmentView()) {
                    px += v.getX();
                    py += v.getY();
                    v = (View) v.getParent();
                }
                if (AndroidUtilities.isTablet()) {
                    View pv = parentLayout.getView();
                    if (pv != null) {
                        px += pv.getX() + pv.getPaddingLeft();
                        py += pv.getY() + pv.getPaddingTop();
                    }
                }
                px -= popupLayout.getMeasuredWidth() / 2f;
                popupWindow.showAtLocation(getFragmentView(), 0, (int) px, (int) py);
                popupWindow.dimBehind();
            };
            if (withTranslate[0]) {
                if (LanguageDetector.hasSupport()) {
                    LanguageDetector.detectLanguage(finalText, (fromLang) -> {
                        fromLanguage[0] = fromLang;
                        withTranslate[0] = fromLang != null && (!fromLang.equals(toLang) || fromLang.equals("und")) && (
                            translateButtonEnabled && !RestrictedLanguagesSelectActivity.getRestrictedLanguages().contains(fromLang) ||
                            (currentChat != null && (currentChat.has_link || ChatObject.isPublic(currentChat))) && ("uk".equals(fromLang) || "ru".equals(fromLang)));
                        showMenu.run();
                    }, (error) -> {
                        FileLog.e("mlkit: failed to detect language in selection", error);
                        showMenu.run();
                    });
                } else {
                    showMenu.run();
                }
            } else {
                showMenu.run();
            }
            return true;
        } else if (position == bizHoursRow || position == bizLocationRow) {
            if (getParentActivity() == null || userInfo == null) {
                return false;
            }
            final String finalText;
            if (position == bizHoursRow) {
                if (userInfo.business_work_hours == null) return false;
                finalText = OpeningHoursActivity.toString(currentAccount, userInfo.user, userInfo.business_work_hours);
            } else if (position == bizLocationRow) {
                if (editRow(view, position)) return true;
                if (userInfo.business_location == null) return false;
                finalText = userInfo.business_location.address;
            } else return true;

            AtomicReference<ActionBarPopupWindow> popupWindowRef = new AtomicReference<>();
            ActionBarPopupWindow.ActionBarPopupWindowLayout popupLayout = new ActionBarPopupWindow.ActionBarPopupWindowLayout(getContext(), R.drawable.popup_fixed_alert, resourcesProvider) {
                Path path = new Path();

                @Override
                protected boolean drawChild(Canvas canvas, View child, long drawingTime) {
                    canvas.save();
                    path.rewind();
                    AndroidUtilities.rectTmp.set(child.getLeft(), child.getTop(), child.getRight(), child.getBottom());
                    path.addRoundRect(AndroidUtilities.rectTmp, AndroidUtilities.dp(6), AndroidUtilities.dp(6), Path.Direction.CW);
                    canvas.clipPath(path);
                    boolean draw = super.drawChild(canvas, child, drawingTime);
                    canvas.restore();
                    return draw;
                }
            };
            popupLayout.setFitItems(true);

            ActionBarMenuItem.addItem(popupLayout, R.drawable.msg_copy, LocaleController.getString(R.string.Copy), false, resourcesProvider).setOnClickListener(v -> {
                popupWindowRef.get().dismiss();
                try {
                    AndroidUtilities.addToClipboard(finalText);
                    if (position == bizHoursRow) {
                        BulletinFactory.of(this).createCopyBulletin(LocaleController.getString(R.string.BusinessHoursCopied)).show();
                    } else {
                        BulletinFactory.of(this).createCopyBulletin(LocaleController.getString(R.string.BusinessLocationCopied)).show();
                    }
                } catch (Exception e) {
                    FileLog.e(e);
                }
            });

            ActionBarPopupWindow popupWindow = new ActionBarPopupWindow(popupLayout, LayoutHelper.WRAP_CONTENT, LayoutHelper.WRAP_CONTENT);
            popupWindow.setPauseNotifications(true);
            popupWindow.setDismissAnimationDuration(220);
            popupWindow.setOutsideTouchable(true);
            popupWindow.setClippingEnabled(true);
            popupWindow.setAnimationStyle(R.style.PopupContextAnimation);
            popupWindow.setFocusable(true);
            popupLayout.measure(View.MeasureSpec.makeMeasureSpec(AndroidUtilities.dp(1000), View.MeasureSpec.AT_MOST), View.MeasureSpec.makeMeasureSpec(AndroidUtilities.dp(1000), View.MeasureSpec.AT_MOST));
            popupWindow.setInputMethodMode(ActionBarPopupWindow.INPUT_METHOD_NOT_NEEDED);
            popupWindow.getContentView().setFocusableInTouchMode(true);
            popupWindowRef.set(popupWindow);

            float px = x, py = y;
            View v = view;
            while (v != null && v != getFragmentView()) {
                px += v.getX();
                py += v.getY();
                v = (View) v.getParent();
            }
            if (AndroidUtilities.isTablet()) {
                View pv = parentLayout.getView();
                if (pv != null) {
                    px += pv.getX() + pv.getPaddingLeft();
                    py += pv.getY() + pv.getPaddingTop();
                }
            }
            px -= popupLayout.getMeasuredWidth() / 2f;
            popupWindow.showAtLocation(getFragmentView(), 0, (int) px, (int) py);
            popupWindow.dimBehind();
            return true;
        }
        return false;
    }

    public void leaveChatPressed() {
        boolean isForum = ChatObject.isForum(currentChat);
        AlertsCreator.createClearOrDeleteDialogAlert(ProfileActivity.this, false, currentChat, null, false, isForum, !isForum, (param) -> {
            playProfileAnimation = 0;
            getNotificationCenter().removeObserver(ProfileActivity.this, NotificationCenter.closeChats);
            getNotificationCenter().postNotificationName(NotificationCenter.closeChats);
            finishFragment();
            getNotificationCenter().postNotificationName(NotificationCenter.needDeleteDialog, -currentChat.id, null, currentChat, param);
        }, resourcesProvider);
    }

    private void getChannelParticipants(boolean reload) {
        if (loadingUsers || participantsMap == null || chatInfo == null) {
            return;
        }
        loadingUsers = true;
        final int delay = participantsMap.size() != 0 && reload ? 300 : 0;

        final TLRPC.TL_channels_getParticipants req = new TLRPC.TL_channels_getParticipants();
        req.channel = getMessagesController().getInputChannel(chatId);
        req.filter = new TLRPC.TL_channelParticipantsRecent();
        req.offset = reload ? 0 : participantsMap.size();
        req.limit = 200;
        int reqId = getConnectionsManager().sendRequest(req, (response, error) -> AndroidUtilities.runOnUIThread(() -> getNotificationCenter().doOnIdle(() -> {
            if (error == null) {
                TLRPC.TL_channels_channelParticipants res = (TLRPC.TL_channels_channelParticipants) response;
                getMessagesController().putUsers(res.users, false);
                getMessagesController().putChats(res.chats, false);
                if (res.users.size() < 200) {
                    usersEndReached = true;
                }
                if (req.offset == 0) {
                    participantsMap.clear();
                    chatInfo.participants = new TLRPC.TL_chatParticipants();
                    getMessagesStorage().putUsersAndChats(res.users, res.chats, true, true);
                    getMessagesStorage().updateChannelUsers(chatId, res.participants);
                }
                for (int a = 0; a < res.participants.size(); a++) {
                    TLRPC.TL_chatChannelParticipant participant = new TLRPC.TL_chatChannelParticipant();
                    participant.channelParticipant = res.participants.get(a);
                    participant.inviter_id = participant.channelParticipant.inviter_id;
                    participant.user_id = MessageObject.getPeerId(participant.channelParticipant.peer);
                    participant.date = participant.channelParticipant.date;
                    if (participantsMap.indexOfKey(participant.user_id) < 0) {
                        if (chatInfo.participants == null) {
                            chatInfo.participants = new TLRPC.TL_chatParticipants();
                        }
                        chatInfo.participants.participants.add(participant);
                        participantsMap.put(participant.user_id, participant);
                    }
                }
            }
            loadingUsers = false;
            saveScrollPosition();
            updateListAnimated(true);
        }), delay));
        getConnectionsManager().bindRequestToGuid(reqId, classGuid);
    }

    private AnimatorSet headerAnimatorSet;
    private AnimatorSet headerShadowAnimatorSet;
    public float mediaHeaderAnimationProgress;
    private boolean mediaHeaderVisible;
    private Property<ActionBar, Float> ACTIONBAR_HEADER_PROGRESS = new AnimationProperties.FloatProperty<ActionBar>("avatarAnimationProgress") {
        @Override
        public void setValue(ActionBar object, float value) {
            mediaHeaderAnimationProgress = value;
            if (storyView != null) {
                storyView.setActionBarActionMode(value);
            }
            if (giftsView != null) {
                giftsView.setActionBarActionMode(value);
            }
            topView.invalidate();

            int color1 = getThemedColor(Theme.key_profile_title);
            int color2 = getThemedColor(Theme.key_player_actionBarTitle);
            int c = AndroidUtilities.getOffsetColor(color1, color2, value, 1.0f);
            nameTextView[1].setTextColor(c);
            if (lockIconDrawable != null) {
                lockIconDrawable.setColorFilter(c, PorterDuff.Mode.MULTIPLY);
            }
            if (scamDrawable != null) {
                color1 = getThemedColor(Theme.key_avatar_subtitleInProfileBlue);
                scamDrawable.setColor(AndroidUtilities.getOffsetColor(color1, color2, value, 1.0f));
            }

            color1 = peerColor != null ? Color.WHITE : getThemedColor(Theme.key_actionBarDefaultIcon);
            color2 = getThemedColor(Theme.key_actionBarActionModeDefaultIcon);
            actionBar.setItemsColor(AndroidUtilities.getOffsetColor(color1, color2, value, 1.0f), false);

            color1 = peerColor != null ? Theme.ACTION_BAR_WHITE_SELECTOR_COLOR : peerColor != null ? 0x20ffffff : getThemedColor(Theme.key_avatar_actionBarSelectorBlue);
            color2 = getThemedColor(Theme.key_actionBarActionModeDefaultSelector);
            actionBar.setItemsBackgroundColor(AndroidUtilities.getOffsetColor(color1, color2, value, 1.0f), false);

            topView.invalidate();
            otherItem.setIconColor(peerColor != null ? Color.WHITE : getThemedColor(Theme.key_actionBarDefaultIcon));
            callItem.setIconColor(peerColor != null ? Color.WHITE : getThemedColor(Theme.key_actionBarDefaultIcon));
            videoCallItem.setIconColor(peerColor != null ? Color.WHITE : getThemedColor(Theme.key_actionBarDefaultIcon));
            editItem.setIconColor(peerColor != null ? Color.WHITE : getThemedColor(Theme.key_actionBarDefaultIcon));

            if (verifiedDrawable[0] != null) {
                color1 = getThemedColor(Theme.key_profile_verifiedBackground);
                color2 = getThemedColor(Theme.key_player_actionBarTitle);
                verifiedDrawable[0].setColorFilter(AndroidUtilities.getOffsetColor(color1, color2, value, 1.0f), PorterDuff.Mode.MULTIPLY);
            }
            if (verifiedDrawable[1] != null) {
                color1 = peerColor != null ? Theme.adaptHSV(ColorUtils.blendARGB(peerColor.getColor2(), peerColor.hasColor6(Theme.isCurrentThemeDark()) ? peerColor.getColor5() : peerColor.getColor3(), .4f), +.1f, Theme.isCurrentThemeDark() ? -.1f : -.08f) : getThemedColor(Theme.key_profile_verifiedBackground);
                color2 = getThemedColor(Theme.key_player_actionBarTitle);
                verifiedDrawable[1].setColorFilter(AndroidUtilities.getOffsetColor(color1, color2, value, 1.0f), PorterDuff.Mode.MULTIPLY);
            }

            if (verifiedCheckDrawable[0] != null) {
                color1 = getThemedColor(Theme.key_profile_verifiedCheck);
                color2 = getThemedColor(Theme.key_windowBackgroundWhite);
                verifiedCheckDrawable[0].setColorFilter(AndroidUtilities.getOffsetColor(color1, color2, value, 1.0f), PorterDuff.Mode.MULTIPLY);
            }
            if (verifiedCheckDrawable[1] != null) {
                color1 = peerColor != null ? Color.WHITE : dontApplyPeerColor(getThemedColor(Theme.key_profile_verifiedCheck));
                color2 = getThemedColor(Theme.key_windowBackgroundWhite);
                verifiedCheckDrawable[1].setColorFilter(AndroidUtilities.getOffsetColor(color1, color2, value, 1.0f), PorterDuff.Mode.MULTIPLY);
            }


            if (premiumStarDrawable[0] != null) {
                color1 = getThemedColor(Theme.key_profile_verifiedBackground);
                color2 = getThemedColor(Theme.key_player_actionBarTitle);
                premiumStarDrawable[0].setColorFilter(AndroidUtilities.getOffsetColor(color1, color2, value, 1.0f), PorterDuff.Mode.MULTIPLY);
            }
            if (premiumStarDrawable[1] != null) {
                color1 = dontApplyPeerColor(getThemedColor(Theme.key_profile_verifiedBackground));
                color2 = dontApplyPeerColor(getThemedColor(Theme.key_player_actionBarTitle));
                premiumStarDrawable[1].setColorFilter(AndroidUtilities.getOffsetColor(color1, color2, value, 1.0f), PorterDuff.Mode.MULTIPLY);
            }

            updateEmojiStatusDrawableColor();

            if (avatarsViewPagerIndicatorView.getSecondaryMenuItem() != null && (videoCallItemVisible || editItemVisible || callItemVisible)) {
                needLayoutText(Math.min(1f, extraHeight / AndroidUtilities.dp(88f)));
            }
        }

        @Override
        public Float get(ActionBar object) {
            return mediaHeaderAnimationProgress;
        }
    };

    private void setMediaHeaderVisible(boolean visible) {
        if (mediaHeaderVisible == visible) {
            return;
        }
        mediaHeaderVisible = visible;
        if (headerAnimatorSet != null) {
            headerAnimatorSet.cancel();
        }
        if (headerShadowAnimatorSet != null) {
            headerShadowAnimatorSet.cancel();
        }
        ActionBarMenuItem mediaSearchItem = sharedMediaLayout.getSearchItem();
        ImageView mediaOptionsItem = sharedMediaLayout.getSearchOptionsItem();
        TextView saveItem = sharedMediaLayout.getSaveItem();
        if (!mediaHeaderVisible) {
            if (callItemVisible) {
                callItem.setVisibility(View.VISIBLE);
            }
            if (videoCallItemVisible) {
                videoCallItem.setVisibility(View.VISIBLE);
            }
            if (editItemVisible) {
                editItem.setVisibility(View.VISIBLE);
            }
            otherItem.setVisibility(View.VISIBLE);
            if (mediaOptionsItem != null) {
                mediaOptionsItem.setVisibility(View.GONE);
            }
            if (saveItem != null) {
                saveItem.setVisibility(View.GONE);
            }
        } else {
            if (sharedMediaLayout.isSearchItemVisible()) {
                mediaSearchItem.setVisibility(View.VISIBLE);
            }
            if (mediaOptionsItem != null) {
                mediaOptionsItem.setVisibility(View.VISIBLE);
            }
            if (sharedMediaLayout.isOptionsItemVisible()) {
                sharedMediaLayout.photoVideoOptionsItem.setVisibility(View.VISIBLE);
                sharedMediaLayout.animateSearchToOptions(true, false);
            } else {
                sharedMediaLayout.photoVideoOptionsItem.setVisibility(View.INVISIBLE);
                sharedMediaLayout.animateSearchToOptions(false, false);
            }
        }
        updateStoriesViewBounds(false);

        if (actionBar != null) {
            actionBar.createMenu().requestLayout();
        }

        ArrayList<Animator> animators = new ArrayList<>();

        animators.add(ObjectAnimator.ofFloat(callItem, View.ALPHA, visible ? 0.0f : 1.0f));
        animators.add(ObjectAnimator.ofFloat(videoCallItem, View.ALPHA, visible ? 0.0f : 1.0f));
        animators.add(ObjectAnimator.ofFloat(otherItem, View.ALPHA, visible ? 0.0f : 1.0f));
        animators.add(ObjectAnimator.ofFloat(editItem, View.ALPHA, visible ? 0.0f : 1.0f));
        animators.add(ObjectAnimator.ofFloat(callItem, View.TRANSLATION_Y, visible ? -AndroidUtilities.dp(10) : 0.0f));
        animators.add(ObjectAnimator.ofFloat(videoCallItem, View.TRANSLATION_Y, visible ? -AndroidUtilities.dp(10) : 0.0f));
        animators.add(ObjectAnimator.ofFloat(otherItem, View.TRANSLATION_Y, visible ? -AndroidUtilities.dp(10) : 0.0f));
        animators.add(ObjectAnimator.ofFloat(editItem, View.TRANSLATION_Y, visible ? -AndroidUtilities.dp(10) : 0.0f));
        animators.add(ObjectAnimator.ofFloat(mediaSearchItem, View.ALPHA, visible ? 1.0f : 0.0f));
        animators.add(ObjectAnimator.ofFloat(mediaSearchItem, View.TRANSLATION_Y, visible ? 0.0f : AndroidUtilities.dp(10)));
        animators.add(ObjectAnimator.ofFloat(sharedMediaLayout.photoVideoOptionsItem, View.ALPHA, visible ? 1.0f : 0.0f));
        animators.add(ObjectAnimator.ofFloat(sharedMediaLayout.photoVideoOptionsItem, View.TRANSLATION_Y, visible ? 0.0f : AndroidUtilities.dp(10)));
        animators.add(ObjectAnimator.ofFloat(actionBar, ACTIONBAR_HEADER_PROGRESS, visible ? 1.0f : 0.0f));
        animators.add(ObjectAnimator.ofFloat(onlineTextView[1], View.ALPHA, visible ? 0.0f : 1.0f));
        if (myProfile) animators.add(ObjectAnimator.ofFloat(onlineTextView[3], View.ALPHA, visible ? 0.0f : 1.0f));
        animators.add(ObjectAnimator.ofFloat(mediaCounterTextView, View.ALPHA, visible ? 1.0f : 0.0f));
        if (visible) {
            animators.add(ObjectAnimator.ofFloat(this, HEADER_SHADOW, 0.0f));
        }
        if (storyView != null || giftsView != null) {
            ValueAnimator va = ValueAnimator.ofFloat(0, 1);
            va.addUpdateListener(a -> updateStoriesViewBounds(true));
            animators.add(va);
        }

        headerAnimatorSet = new AnimatorSet();
        headerAnimatorSet.playTogether(animators);
        headerAnimatorSet.setInterpolator(CubicBezierInterpolator.DEFAULT);
        headerAnimatorSet.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                if (headerAnimatorSet != null) {
                    if (mediaHeaderVisible) {
                        if (callItemVisible) {
                            callItem.setVisibility(View.GONE);
                        }
                        if (videoCallItemVisible) {
                            videoCallItem.setVisibility(View.GONE);
                        }
                        if (editItemVisible) {
                            editItem.setVisibility(View.GONE);
                        }
                        otherItem.setVisibility(View.GONE);
                    } else {
                        if (sharedMediaLayout.isSearchItemVisible()) {
                            mediaSearchItem.setVisibility(View.VISIBLE);
                        }

                        sharedMediaLayout.photoVideoOptionsItem.setVisibility(View.INVISIBLE);

                        headerShadowAnimatorSet = new AnimatorSet();
                        headerShadowAnimatorSet.playTogether(ObjectAnimator.ofFloat(ProfileActivity.this, HEADER_SHADOW, 1.0f));
                        headerShadowAnimatorSet.setDuration(100);
                        headerShadowAnimatorSet.addListener(new AnimatorListenerAdapter() {
                            @Override
                            public void onAnimationEnd(Animator animation) {
                                headerShadowAnimatorSet = null;
                            }
                        });
                        headerShadowAnimatorSet.start();
                    }
                }
                updateStoriesViewBounds(false);
                headerAnimatorSet = null;
            }

            @Override
            public void onAnimationCancel(Animator animation) {
                headerAnimatorSet = null;
            }
        });
        headerAnimatorSet.setDuration(150);
        headerAnimatorSet.start();
        NotificationCenter.getGlobalInstance().postNotificationName(NotificationCenter.needCheckSystemBarColors, true);
    }

    public void openAddMember() {
        Bundle args = new Bundle();
        args.putBoolean("addToGroup", true);
        args.putLong("chatId", currentChat.id);
        GroupCreateActivity fragment = new GroupCreateActivity(args);
        fragment.setInfo(chatInfo);
        if (chatInfo != null && chatInfo.participants != null) {
            LongSparseArray<TLObject> users = new LongSparseArray<>();
            for (int a = 0; a < chatInfo.participants.participants.size(); a++) {
                users.put(chatInfo.participants.participants.get(a).user_id, null);
            }
            fragment.setIgnoreUsers(users);
        }
        fragment.setDelegate2((users, fwdCount) -> {
            HashSet<Long> currentParticipants = new HashSet<>();
            ArrayList<TLRPC.User> addedUsers = new ArrayList<>();
            if (chatInfo != null && chatInfo.participants != null && chatInfo.participants.participants != null) {
                for (int i = 0; i < chatInfo.participants.participants.size(); i++) {
                    currentParticipants.add(chatInfo.participants.participants.get(i).user_id);
                }
            }
            getMessagesController().addUsersToChat(currentChat, ProfileActivity.this, users, fwdCount, user -> {
                addedUsers.add(user);
            }, restrictedUser -> {
                for (int i = 0; i < chatInfo.participants.participants.size(); i++) {
                    if (chatInfo.participants.participants.get(i).user_id == restrictedUser.id) {
                        chatInfo.participants.participants.remove(i);
                        updateListAnimated(true);
                        break;
                    }
                }
            }, () -> {
                int N = addedUsers.size();
                int[] finished = new int[1];
                for (int a = 0; a < N; a++) {
                    TLRPC.User user = addedUsers.get(a);
                    if (!currentParticipants.contains(user.id)) {
                        if (chatInfo.participants == null) {
                            chatInfo.participants = new TLRPC.TL_chatParticipants();
                        }
                        if (ChatObject.isChannel(currentChat)) {
                            TLRPC.TL_chatChannelParticipant channelParticipant1 = new TLRPC.TL_chatChannelParticipant();
                            channelParticipant1.channelParticipant = new TLRPC.TL_channelParticipant();
                            channelParticipant1.channelParticipant.inviter_id = getUserConfig().getClientUserId();
                            channelParticipant1.channelParticipant.peer = new TLRPC.TL_peerUser();
                            channelParticipant1.channelParticipant.peer.user_id = user.id;
                            channelParticipant1.channelParticipant.date = getConnectionsManager().getCurrentTime();
                            channelParticipant1.user_id = user.id;
                            chatInfo.participants.participants.add(channelParticipant1);
                        } else {
                            TLRPC.ChatParticipant participant = new TLRPC.TL_chatParticipant();
                            participant.user_id = user.id;
                            participant.inviter_id = getAccountInstance().getUserConfig().clientUserId;
                            chatInfo.participants.participants.add(participant);
                        }
                        chatInfo.participants_count++;
                        getMessagesController().putUser(user, false);
                    }
                }
                updateListAnimated(true);
            });

        });
        presentFragment(fragment);
    }

    private void checkListViewScroll() {
        if (listView.getVisibility() != View.VISIBLE) {
            return;
        }
        if (sharedMediaLayoutAttached) {
            sharedMediaLayout.setVisibleHeight(listView.getMeasuredHeight() - sharedMediaLayout.getTop());
        }

        if (listView.getChildCount() <= 0 || openAnimationInProgress) {
            return;
        }

        int newOffset = 0;
        View child = null;
        for (int i = 0; i < listView.getChildCount(); i++) {
            if (listView.getChildAdapterPosition(listView.getChildAt(i)) == 0) {
                child = listView.getChildAt(i);
                break;
            }
        }
        RecyclerListView.Holder holder = child == null ? null : (RecyclerListView.Holder) listView.findContainingViewHolder(child);
        int top = child == null ? 0 : child.getTop();
        int adapterPosition = holder != null ? holder.getAdapterPosition() : RecyclerView.NO_POSITION;
        if (top >= 0 && adapterPosition == 0) {
            newOffset = top;
        }
        boolean mediaHeaderVisible;
        boolean searchVisible = imageUpdater == null && actionBar.isSearchFieldVisible();
        if (sharedMediaRow != -1 && !searchVisible) {
            holder = (RecyclerListView.Holder) listView.findViewHolderForAdapterPosition(sharedMediaRow);
            mediaHeaderVisible = holder != null && holder.itemView.getTop() <= 0;
        } else {
            mediaHeaderVisible = searchVisible;
        }
        setMediaHeaderVisible(mediaHeaderVisible);

        if (extraHeight != newOffset && !transitionAnimationInProress) {
            extraHeight = newOffset;
            topView.invalidate();
            if (playProfileAnimation != 0) {
                allowProfileAnimation = extraHeight != 0;
            }
            needLayout(true);
        }
    }

    public void updateSelectedMediaTabText() {
        if (sharedMediaLayout == null || mediaCounterTextView == null) {
            return;
        }
        int id = sharedMediaLayout.getClosestTab();
        int[] mediaCount = sharedMediaPreloader.getLastMediaCount();
        if (id == SharedMediaLayout.TAB_PHOTOVIDEO) {
            if (mediaCount[MediaDataController.MEDIA_VIDEOS_ONLY] <= 0 && mediaCount[MediaDataController.MEDIA_PHOTOS_ONLY] <= 0) {
                if (mediaCount[MediaDataController.MEDIA_PHOTOVIDEO] <= 0) {
                    mediaCounterTextView.setText(LocaleController.getString(R.string.SharedMedia));
                } else {
                    mediaCounterTextView.setText(LocaleController.formatPluralString("Media", mediaCount[MediaDataController.MEDIA_PHOTOVIDEO]));
                }
            } else if (sharedMediaLayout.getPhotosVideosTypeFilter() == SharedMediaLayout.FILTER_PHOTOS_ONLY || mediaCount[MediaDataController.MEDIA_VIDEOS_ONLY] <= 0) {
                mediaCounterTextView.setText(LocaleController.formatPluralString("Photos", mediaCount[MediaDataController.MEDIA_PHOTOS_ONLY]));
            } else if (sharedMediaLayout.getPhotosVideosTypeFilter() == SharedMediaLayout.FILTER_VIDEOS_ONLY || mediaCount[MediaDataController.MEDIA_PHOTOS_ONLY] <= 0) {
                mediaCounterTextView.setText(LocaleController.formatPluralString("Videos", mediaCount[MediaDataController.MEDIA_VIDEOS_ONLY]));
            } else {
                String str = String.format("%s, %s", LocaleController.formatPluralString("Photos", mediaCount[MediaDataController.MEDIA_PHOTOS_ONLY]), LocaleController.formatPluralString("Videos", mediaCount[MediaDataController.MEDIA_VIDEOS_ONLY]));
                mediaCounterTextView.setText(str);
            }
        } else if (id == SharedMediaLayout.TAB_FILES) {
            if (mediaCount[MediaDataController.MEDIA_FILE] <= 0) {
                mediaCounterTextView.setText(LocaleController.getString(R.string.Files));
            } else {
                mediaCounterTextView.setText(LocaleController.formatPluralString("Files", mediaCount[MediaDataController.MEDIA_FILE]));
            }
        } else if (id == SharedMediaLayout.TAB_VOICE) {
            if (mediaCount[MediaDataController.MEDIA_AUDIO] <= 0) {
                mediaCounterTextView.setText(LocaleController.getString(R.string.Voice));
            } else {
                mediaCounterTextView.setText(LocaleController.formatPluralString("Voice", mediaCount[MediaDataController.MEDIA_AUDIO]));
            }
        } else if (id == SharedMediaLayout.TAB_LINKS) {
            if (mediaCount[MediaDataController.MEDIA_URL] <= 0) {
                mediaCounterTextView.setText(LocaleController.getString(R.string.SharedLinks));
            } else {
                mediaCounterTextView.setText(LocaleController.formatPluralString("Links", mediaCount[MediaDataController.MEDIA_URL]));
            }
        } else if (id == SharedMediaLayout.TAB_AUDIO) {
            if (mediaCount[MediaDataController.MEDIA_MUSIC] <= 0) {
                mediaCounterTextView.setText(LocaleController.getString(R.string.Music));
            } else {
                mediaCounterTextView.setText(LocaleController.formatPluralString("MusicFiles", mediaCount[MediaDataController.MEDIA_MUSIC]));
            }
        } else if (id == SharedMediaLayout.TAB_GIF) {
            if (mediaCount[MediaDataController.MEDIA_GIF] <= 0) {
                mediaCounterTextView.setText(LocaleController.getString(R.string.AccDescrGIFs));
            } else {
                mediaCounterTextView.setText(LocaleController.formatPluralString("GIFs", mediaCount[MediaDataController.MEDIA_GIF]));
            }
        } else if (id == SharedMediaLayout.TAB_COMMON_GROUPS) {
            mediaCounterTextView.setText(LocaleController.formatPluralString("CommonGroups", userInfo.common_chats_count));
        } else if (id == SharedMediaLayout.TAB_GROUPUSERS) {
            mediaCounterTextView.setText(onlineTextView[1].getText());
        } else if (id == SharedMediaLayout.TAB_STORIES) {
            if (isBot) {
                mediaCounterTextView.setText(sharedMediaLayout.getBotPreviewsSubtitle(false));
            } else {
                mediaCounterTextView.setText(LocaleController.formatPluralString("ProfileStoriesCount", sharedMediaLayout.getStoriesCount(id)));
            }
        } else if (id == SharedMediaLayout.TAB_BOT_PREVIEWS) {
            mediaCounterTextView.setText(sharedMediaLayout.getBotPreviewsSubtitle(true));
        } else if (id == SharedMediaLayout.TAB_ARCHIVED_STORIES) {
            mediaCounterTextView.setText(LocaleController.formatPluralString("ProfileStoriesArchiveCount", sharedMediaLayout.getStoriesCount(id)));
        } else if (id == SharedMediaLayout.TAB_RECOMMENDED_CHANNELS) {
            final MessagesController.ChannelRecommendations rec = MessagesController.getInstance(currentAccount).getChannelRecommendations(getDialogId());
            mediaCounterTextView.setText(LocaleController.formatPluralString(isBot ? "Bots" : "Channels", rec == null ? 0 : rec.chats.size() + rec.more));
        } else if (id == SharedMediaLayout.TAB_SAVED_MESSAGES) {
            int messagesCount = getMessagesController().getSavedMessagesController().getMessagesCount(getDialogId());
            mediaCounterTextView.setText(LocaleController.formatPluralString("SavedMessagesCount", Math.max(1, messagesCount)));
        } else if (id == SharedMediaLayout.TAB_GIFTS) {
            mediaCounterTextView.setText(LocaleController.formatPluralStringComma("ProfileGiftsCount", sharedMediaLayout.giftsContainer == null ? 0 : sharedMediaLayout.giftsContainer.getGiftsCount()));
        }
    }

    private void needLayout(boolean animated) {
        final int newTop = (actionBar.getOccupyStatusBar() ? AndroidUtilities.statusBarHeight : 0) + ActionBar.getCurrentActionBarHeight();

        FrameLayout.LayoutParams layoutParams;
        if (listView != null && !openAnimationInProgress) {
            layoutParams = (FrameLayout.LayoutParams) listView.getLayoutParams();
            if (layoutParams.topMargin != newTop) {
                layoutParams.topMargin = newTop;
                listView.setLayoutParams(layoutParams);
            }
        }

        if (avatarContainer != null) {
            final float diff = Math.min(1f, extraHeight / AndroidUtilities.dp(88f));

            listView.setTopGlowOffset((int) extraHeight);

            listView.setOverScrollMode(extraHeight > AndroidUtilities.dp(88f) && extraHeight < listView.getMeasuredWidth() - newTop ? View.OVER_SCROLL_NEVER : View.OVER_SCROLL_ALWAYS);

            if (writeButton != null) {
                writeButton.setTranslationY((actionBar.getOccupyStatusBar() ? AndroidUtilities.statusBarHeight : 0) + ActionBar.getCurrentActionBarHeight() + extraHeight + searchTransitionOffset - AndroidUtilities.dp(29.5f));

                boolean writeButtonVisible = diff > 0.2f && !searchMode && (imageUpdater == null || setAvatarRow == -1);
                if (writeButtonVisible && chatId != 0) {
                    writeButtonVisible = ChatObject.isChannel(currentChat) && !currentChat.megagroup && chatInfo != null && chatInfo.linked_chat_id != 0 && infoHeaderRow != -1;
                }
                if (!openAnimationInProgress) {
                    boolean currentVisible = writeButton.getTag() == null;
                    if (writeButtonVisible != currentVisible) {
                        if (writeButtonVisible) {
                            writeButton.setTag(null);
                        } else {
                            writeButton.setTag(0);
                        }
                        if (writeButtonAnimation != null) {
                            AnimatorSet old = writeButtonAnimation;
                            writeButtonAnimation = null;
                            old.cancel();
                        }
                        if (animated) {
                            writeButtonAnimation = new AnimatorSet();
                            if (writeButtonVisible) {
                                writeButtonAnimation.setInterpolator(new DecelerateInterpolator());
                                writeButtonAnimation.playTogether(
                                        ObjectAnimator.ofFloat(writeButton, View.SCALE_X, 1.0f),
                                        ObjectAnimator.ofFloat(writeButton, View.SCALE_Y, 1.0f),
                                        ObjectAnimator.ofFloat(writeButton, View.ALPHA, 1.0f)
                                );
                            } else {
                                writeButtonAnimation.setInterpolator(new AccelerateInterpolator());
                                writeButtonAnimation.playTogether(
                                        ObjectAnimator.ofFloat(writeButton, View.SCALE_X, 0.2f),
                                        ObjectAnimator.ofFloat(writeButton, View.SCALE_Y, 0.2f),
                                        ObjectAnimator.ofFloat(writeButton, View.ALPHA, 0.0f)
                                );
                            }
                            writeButtonAnimation.setDuration(150);
                            writeButtonAnimation.addListener(new AnimatorListenerAdapter() {
                                @Override
                                public void onAnimationEnd(Animator animation) {
                                    if (writeButtonAnimation != null && writeButtonAnimation.equals(animation)) {
                                        writeButtonAnimation = null;
                                    }
                                }
                            });
                            writeButtonAnimation.start();
                        } else {
                            writeButton.setScaleX(writeButtonVisible ? 1.0f : 0.2f);
                            writeButton.setScaleY(writeButtonVisible ? 1.0f : 0.2f);
                            writeButton.setAlpha(writeButtonVisible ? 1.0f : 0.0f);
                        }
                    }

                    if (qrItem != null) {
                        updateQrItemVisibility(animated);
                        if (!animated) {
                            float translation = AndroidUtilities.dp(48) * qrItem.getAlpha();
                            qrItem.setTranslationX(translation);
                            if (avatarsViewPagerIndicatorView != null) {
                                avatarsViewPagerIndicatorView.setTranslationX(translation - AndroidUtilities.dp(48));
                            }
                        }
                    }
                }

                if (storyView != null) {
                    storyView.setExpandCoords(avatarContainer2.getMeasuredWidth() - AndroidUtilities.dp(40), writeButtonVisible, (actionBar.getOccupyStatusBar() ? AndroidUtilities.statusBarHeight : 0) + ActionBar.getCurrentActionBarHeight() + extraHeight + searchTransitionOffset);
                }
                if (giftsView != null) {
                    giftsView.setExpandCoords(avatarContainer2.getMeasuredWidth() - AndroidUtilities.dp(40), writeButtonVisible, (actionBar.getOccupyStatusBar() ? AndroidUtilities.statusBarHeight : 0) + ActionBar.getCurrentActionBarHeight() + extraHeight + searchTransitionOffset);
                }
            }

            avatarX = -AndroidUtilities.dpf2(47f) * diff;
            avatarY = (actionBar.getOccupyStatusBar() ? AndroidUtilities.statusBarHeight : 0) + ActionBar.getCurrentActionBarHeight() / 2.0f * (1.0f + diff) - 21 * AndroidUtilities.density + 27 * AndroidUtilities.density * diff + actionBar.getTranslationY();

            float h = openAnimationInProgress ? initialAnimationExtraHeight : extraHeight;
            if (h > AndroidUtilities.dp(88f) || isPulledDown) {
                expandProgress = Math.max(0f, Math.min(1f, (h - AndroidUtilities.dp(88f)) / (listView.getMeasuredWidth() - newTop - AndroidUtilities.dp(88f))));
                avatarScale = AndroidUtilities.lerp((42f + 18f) / 42f, (42f + 42f + 18f) / 42f, Math.min(1f, expandProgress * 3f));
                if (storyView != null) {
                    storyView.invalidate();
                }
                if (giftsView != null) {
                    giftsView.invalidate();
                }

                final float durationFactor = Math.min(AndroidUtilities.dpf2(2000f), Math.max(AndroidUtilities.dpf2(1100f), Math.abs(listViewVelocityY))) / AndroidUtilities.dpf2(1100f);

                if (allowPullingDown && (openingAvatar || expandProgress >= 0.33f)) {
                    if (!isPulledDown) {
                        if (otherItem != null) {
                            if (!getMessagesController().isChatNoForwards(currentChat)) {
                                otherItem.showSubItem(gallery_menu_save);
                            } else {
                                otherItem.hideSubItem(gallery_menu_save);
                            }
                            if (imageUpdater != null) {
                                otherItem.showSubItem(add_photo);
                                otherItem.showSubItem(edit_avatar);
                                otherItem.showSubItem(delete_avatar);
                                otherItem.hideSubItem(set_as_main);
                                otherItem.hideSubItem(logout);
                            }
                        }
                        if (searchItem != null) {
                            searchItem.setEnabled(false);
                        }
                        isPulledDown = true;
                        NotificationCenter.getGlobalInstance().postNotificationName(NotificationCenter.needCheckSystemBarColors, true);
                        overlaysView.setOverlaysVisible(true, durationFactor);
                        avatarsViewPagerIndicatorView.refreshVisibility(durationFactor);
                        avatarsViewPager.setCreateThumbFromParent(true);
                        avatarsViewPager.getAdapter().notifyDataSetChanged();
                        expandAnimator.cancel();
                        float value = AndroidUtilities.lerp(expandAnimatorValues, currentExpanAnimatorFracture);
                        expandAnimatorValues[0] = value;
                        expandAnimatorValues[1] = 1f;
                        if (storyView != null && !storyView.isEmpty()) {
                            expandAnimator.setInterpolator(new FastOutSlowInInterpolator());
                            expandAnimator.setDuration((long) ((1f - value) * 1.3f * 250f / durationFactor));
                        } else {
                            expandAnimator.setInterpolator(CubicBezierInterpolator.EASE_BOTH);
                            expandAnimator.setDuration((long) ((1f - value) * 250f / durationFactor));
                        }
                        expandAnimator.addListener(new AnimatorListenerAdapter() {
                            @Override
                            public void onAnimationStart(Animator animation) {
                                setForegroundImage(false);
                                avatarsViewPager.setAnimatedFileMaybe(avatarImage.getImageReceiver().getAnimation());
                                avatarsViewPager.resetCurrentItem();
                            }

                            @Override
                            public void onAnimationEnd(Animator animation) {
                                expandAnimator.removeListener(this);
                                topView.setBackgroundColor(Color.BLACK);
                                avatarContainer.setVisibility(View.GONE);
                                avatarsViewPager.setVisibility(View.VISIBLE);
                            }
                        });
                        expandAnimator.start();
                    }
                    ViewGroup.LayoutParams params = avatarsViewPager.getLayoutParams();
                    params.width = listView.getMeasuredWidth();
                    params.height = (int) (h + newTop);
                    avatarsViewPager.requestLayout();
                    if (!expandAnimator.isRunning()) {
                        float additionalTranslationY = 0;
                        if (openAnimationInProgress && playProfileAnimation == 2) {
                            additionalTranslationY = -(1.0f - avatarAnimationProgress) * AndroidUtilities.dp(50);
                        }
                        onlineX = AndroidUtilities.dpf2(16f) - onlineTextView[1].getLeft();
                        nameTextView[1].setTranslationX(AndroidUtilities.dpf2(18f) - nameTextView[1].getLeft());
                        nameTextView[1].setTranslationY(newTop + h - AndroidUtilities.dpf2(38f) - nameTextView[1].getBottom() + additionalTranslationY);
                        onlineTextView[1].setTranslationX(onlineX + customPhotoOffset);
                        onlineTextView[1].setTranslationY(newTop + h - AndroidUtilities.dpf2(18f) - onlineTextView[1].getBottom() + additionalTranslationY);
                        mediaCounterTextView.setTranslationX(onlineTextView[1].getTranslationX());
                        mediaCounterTextView.setTranslationY(onlineTextView[1].getTranslationY());
                        updateCollectibleHint();
                    }
                } else {
                    if (isPulledDown) {
                        isPulledDown = false;
                        NotificationCenter.getGlobalInstance().postNotificationName(NotificationCenter.needCheckSystemBarColors, true);
                        if (otherItem != null) {
                            otherItem.hideSubItem(gallery_menu_save);
                            if (imageUpdater != null) {
                                otherItem.hideSubItem(set_as_main);
                                otherItem.hideSubItem(edit_avatar);
                                otherItem.hideSubItem(delete_avatar);
                                otherItem.showSubItem(add_photo);
                                otherItem.showSubItem(logout);
//                                otherItem.showSubItem(edit_name);
                            }
                        }
                        if (searchItem != null) {
                            searchItem.setEnabled(!scrolling);
                        }
                        overlaysView.setOverlaysVisible(false, durationFactor);
                        avatarsViewPagerIndicatorView.refreshVisibility(durationFactor);
                        expandAnimator.cancel();
                        avatarImage.getImageReceiver().setAllowStartAnimation(true);
                        avatarImage.getImageReceiver().startAnimation();

                        float value = AndroidUtilities.lerp(expandAnimatorValues, currentExpanAnimatorFracture);
                        expandAnimatorValues[0] = value;
                        expandAnimatorValues[1] = 0f;
                        expandAnimator.setInterpolator(CubicBezierInterpolator.EASE_BOTH);
                        if (!isInLandscapeMode) {
                            expandAnimator.setDuration((long) (value * 250f / durationFactor));
                        } else {
                            expandAnimator.setDuration(0);
                        }
                        topView.setBackgroundColor(getThemedColor(Theme.key_avatar_backgroundActionBarBlue));

                        if (!doNotSetForeground) {
                            BackupImageView imageView = avatarsViewPager.getCurrentItemView();
                            if (imageView != null) {
                                if (imageView.getImageReceiver().getDrawable() instanceof VectorAvatarThumbDrawable) {
                                    avatarImage.drawForeground(false);
                                } else {
                                    avatarImage.drawForeground(true);
                                    avatarImage.setForegroundImageDrawable(imageView.getImageReceiver().getDrawableSafe());
                                }
                            }
                        }
                        avatarImage.setForegroundAlpha(1f);
                        avatarContainer.setVisibility(View.VISIBLE);
                        avatarsViewPager.setVisibility(View.GONE);
                        expandAnimator.start();
                    }

                    avatarContainer.setScaleX(avatarScale);
                    avatarContainer.setScaleY(avatarScale);

                    if (expandAnimator == null || !expandAnimator.isRunning()) {
                        refreshNameAndOnlineXY();
                        nameTextView[1].setTranslationX(nameX);
                        nameTextView[1].setTranslationY(nameY);
                        onlineTextView[1].setTranslationX(onlineX + customPhotoOffset);
                        onlineTextView[1].setTranslationY(onlineY);
                        mediaCounterTextView.setTranslationX(onlineX);
                        mediaCounterTextView.setTranslationY(onlineY);
                        updateCollectibleHint();
                    }
                }
            }

            if (openAnimationInProgress && playProfileAnimation == 2) {
                float avX = 0;
                float avY = (actionBar.getOccupyStatusBar() ? AndroidUtilities.statusBarHeight : 0) + ActionBar.getCurrentActionBarHeight() / 2.0f - 21 * AndroidUtilities.density + actionBar.getTranslationY();

                nameTextView[0].setTranslationX(0);
                nameTextView[0].setTranslationY((float) Math.floor(avY) + AndroidUtilities.dp(1.3f));
                onlineTextView[0].setTranslationX(0);
                onlineTextView[0].setTranslationY((float) Math.floor(avY) + AndroidUtilities.dp(24));
                nameTextView[0].setScaleX(1.0f);
                nameTextView[0].setScaleY(1.0f);

                nameTextView[1].setPivotY(nameTextView[1].getMeasuredHeight());
                nameTextView[1].setScaleX(1.67f);
                nameTextView[1].setScaleY(1.67f);

                avatarScale = AndroidUtilities.lerp(1.0f, (42f + 42f + 18f) / 42f, avatarAnimationProgress);
                if (storyView != null) {
                    storyView.setExpandProgress(1f);
                }
                if (giftsView != null) {
                    giftsView.setExpandProgress(1f);
                }

                avatarImage.setRoundRadius((int) AndroidUtilities.lerp(getSmallAvatarRoundRadius(), 0f, avatarAnimationProgress));
                avatarContainer.setTranslationX(AndroidUtilities.lerp(avX, 0, avatarAnimationProgress));
                avatarContainer.setTranslationY(AndroidUtilities.lerp((float) Math.ceil(avY), 0f, avatarAnimationProgress));
                float extra = (avatarContainer.getMeasuredWidth() - AndroidUtilities.dp(42)) * avatarScale;
                timeItem.setTranslationX(avatarContainer.getX() + AndroidUtilities.dp(16) + extra);
                timeItem.setTranslationY(avatarContainer.getY() + AndroidUtilities.dp(15) + extra);
                starBgItem.setTranslationX(avatarContainer.getX() + AndroidUtilities.dp(28) + extra);
                starBgItem.setTranslationY(avatarContainer.getY() + AndroidUtilities.dp(24) + extra);
                starFgItem.setTranslationX(avatarContainer.getX() + AndroidUtilities.dp(28) + extra);
                starFgItem.setTranslationY(avatarContainer.getY() + AndroidUtilities.dp(24) + extra);
                avatarContainer.setScaleX(avatarScale);
                avatarContainer.setScaleY(avatarScale);

                overlaysView.setAlphaValue(avatarAnimationProgress, false);
                actionBar.setItemsColor(ColorUtils.blendARGB(peerColor != null ? Color.WHITE : getThemedColor(Theme.key_actionBarDefaultIcon), Color.WHITE, avatarAnimationProgress), false);

                if (scamDrawable != null) {
                    scamDrawable.setColor(ColorUtils.blendARGB(getThemedColor(Theme.key_avatar_subtitleInProfileBlue), Color.argb(179, 255, 255, 255), avatarAnimationProgress));
                }
                if (lockIconDrawable != null) {
                    lockIconDrawable.setColorFilter(ColorUtils.blendARGB(getThemedColor(Theme.key_chat_lockIcon), Color.WHITE, avatarAnimationProgress), PorterDuff.Mode.MULTIPLY);
                }
                if (verifiedCrossfadeDrawable[1] != null) {
                    verifiedCrossfadeDrawable[1].setProgress(avatarAnimationProgress);
                    nameTextView[1].invalidate();
                }
                if (premiumCrossfadeDrawable[1] != null) {
                    premiumCrossfadeDrawable[1].setProgress(avatarAnimationProgress);
                    nameTextView[1].invalidate();
                }
                updateEmojiStatusDrawableColor(avatarAnimationProgress);

                final FrameLayout.LayoutParams params = (FrameLayout.LayoutParams) avatarContainer.getLayoutParams();
                params.width = params.height = (int) AndroidUtilities.lerp(AndroidUtilities.dpf2(42f), (extraHeight + newTop) / avatarScale, avatarAnimationProgress);
                params.leftMargin = (int) AndroidUtilities.lerp(AndroidUtilities.dpf2(64f), 0f, avatarAnimationProgress);
                avatarContainer.requestLayout();

                updateCollectibleHint();
            } else if (extraHeight <= AndroidUtilities.dp(88f)) {
                avatarScale = (42 + 18 * diff) / 42.0f;
                if (storyView != null) {
                    storyView.invalidate();
                }
                if (giftsView != null) {
                    giftsView.invalidate();
                }
                float nameScale = 1.0f + 0.12f * diff;
                if (expandAnimator == null || !expandAnimator.isRunning()) {
                    avatarContainer.setScaleX(avatarScale);
                    avatarContainer.setScaleY(avatarScale);
                    avatarContainer.setTranslationX(avatarX);
                    avatarContainer.setTranslationY((float) Math.ceil(avatarY));
                    float extra = AndroidUtilities.dp(42) * avatarScale - AndroidUtilities.dp(42);
                    timeItem.setTranslationX(avatarContainer.getX() + AndroidUtilities.dp(16) + extra);
                    timeItem.setTranslationY(avatarContainer.getY() + AndroidUtilities.dp(15) + extra);
                    starBgItem.setTranslationX(avatarContainer.getX() + AndroidUtilities.dp(28) + extra);
                    starBgItem.setTranslationY(avatarContainer.getY() + AndroidUtilities.dp(24) + extra);
                    starFgItem.setTranslationX(avatarContainer.getX() + AndroidUtilities.dp(28) + extra);
                    starFgItem.setTranslationY(avatarContainer.getY() + AndroidUtilities.dp(24) + extra);
                }
                nameX = -21 * AndroidUtilities.density * diff;
                nameY = (float) Math.floor(avatarY) + AndroidUtilities.dp(1.3f) + AndroidUtilities.dp(7) * diff + titleAnimationsYDiff * (1f - avatarAnimationProgress);
                onlineX = -21 * AndroidUtilities.density * diff;
                onlineY = (float) Math.floor(avatarY) + AndroidUtilities.dp(24) + (float) Math.floor(11 * AndroidUtilities.density) * diff;
                if (showStatusButton != null) {
                    showStatusButton.setAlpha((int) (0xFF * diff));
                }
                for (int a = 0; a < nameTextView.length; a++) {
                    if (nameTextView[a] == null) {
                        continue;
                    }
                    if (expandAnimator == null || !expandAnimator.isRunning()) {
                        nameTextView[a].setTranslationX(nameX);
                        nameTextView[a].setTranslationY(nameY);

                        onlineTextView[a].setTranslationX(onlineX + customPhotoOffset);
                        onlineTextView[a].setTranslationY(onlineY);
                        if (a == 1) {
                            mediaCounterTextView.setTranslationX(onlineX);
                            mediaCounterTextView.setTranslationY(onlineY);
                        }
                    }
                    nameTextView[a].setScaleX(nameScale);
                    nameTextView[a].setScaleY(nameScale);
                }
                updateCollectibleHint();
            }

            if (!openAnimationInProgress && (expandAnimator == null || !expandAnimator.isRunning())) {
                needLayoutText(diff);
            }
        }

        if (isPulledDown || (overlaysView != null && overlaysView.animator != null && overlaysView.animator.isRunning())) {
            final ViewGroup.LayoutParams overlaysLp = overlaysView.getLayoutParams();
            overlaysLp.width = listView.getMeasuredWidth();
            overlaysLp.height = (int) (extraHeight + newTop);
            overlaysView.requestLayout();
        }

        updateEmojiStatusEffectPosition();
    }

    public void updateQrItemVisibility(boolean animated) {
        if (qrItem == null) {
            return;
        }
        boolean setQrVisible = isQrNeedVisible() && Math.min(1f, extraHeight / AndroidUtilities.dp(88f)) > .5f && searchTransitionProgress > .5f;
        if (animated) {
            if (setQrVisible != isQrItemVisible) {
                isQrItemVisible = setQrVisible;
                if (qrItemAnimation != null) {
                    qrItemAnimation.cancel();
                    qrItemAnimation = null;
                }
                qrItem.setClickable(isQrItemVisible);
                qrItemAnimation = new AnimatorSet();
                if (!(qrItem.getVisibility() == View.GONE && !setQrVisible)) {
                    qrItem.setVisibility(View.VISIBLE);
                }
                if (setQrVisible) {
                    qrItemAnimation.setInterpolator(new DecelerateInterpolator());
                    qrItemAnimation.playTogether(
                            ObjectAnimator.ofFloat(qrItem, View.ALPHA, 1.0f),
                            ObjectAnimator.ofFloat(qrItem, View.SCALE_Y, 1f),
                            ObjectAnimator.ofFloat(avatarsViewPagerIndicatorView, View.TRANSLATION_X, -AndroidUtilities.dp(48))
                    );
                } else {
                    qrItemAnimation.setInterpolator(new AccelerateInterpolator());
                    qrItemAnimation.playTogether(
                            ObjectAnimator.ofFloat(qrItem, View.ALPHA, 0.0f),
                            ObjectAnimator.ofFloat(qrItem, View.SCALE_Y, 0f),
                            ObjectAnimator.ofFloat(avatarsViewPagerIndicatorView, View.TRANSLATION_X, 0)
                    );
                }
                qrItemAnimation.setDuration(150);
                qrItemAnimation.addListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        qrItemAnimation = null;
                    }
                });
                qrItemAnimation.start();
            }
        } else {
            if (qrItemAnimation != null) {
                qrItemAnimation.cancel();
                qrItemAnimation = null;
            }
            isQrItemVisible = setQrVisible;
            qrItem.setClickable(isQrItemVisible);
            qrItem.setAlpha(setQrVisible ? 1.0f : 0.0f);
            qrItem.setVisibility(setQrVisible ? View.VISIBLE : View.GONE);
        }
    }

    public void setForegroundImage(boolean secondParent) {
        Drawable drawable = avatarImage.getImageReceiver().getDrawable();
        if (drawable instanceof VectorAvatarThumbDrawable) {
            avatarImage.setForegroundImage(null, null, drawable);
        } else if (drawable instanceof AnimatedFileDrawable) {
            AnimatedFileDrawable fileDrawable = (AnimatedFileDrawable) drawable;
            avatarImage.setForegroundImage(null, null, fileDrawable);
            if (secondParent) {
                fileDrawable.addSecondParentView(avatarImage);
            }
        } else {
            ImageLocation location = avatarsViewPager.getImageLocation(0);
            String filter;
            if (location != null && location.imageType == FileLoader.IMAGE_TYPE_ANIMATION) {
                filter = "avatar";
            } else {
                filter = null;
            }
            avatarImage.setForegroundImage(location, filter, drawable);
        }
    }

    private void refreshNameAndOnlineXY() {
        nameX = AndroidUtilities.dp(-21f) + avatarContainer.getMeasuredWidth() * (avatarScale - (42f + 18f) / 42f);
        nameY = (float) Math.floor(avatarY) + AndroidUtilities.dp(1.3f) + AndroidUtilities.dp(7f) + avatarContainer.getMeasuredHeight() * (avatarScale - (42f + 18f) / 42f) / 2f;
        onlineX = AndroidUtilities.dp(-21f) + avatarContainer.getMeasuredWidth() * (avatarScale - (42f + 18f) / 42f);
        onlineY = (float) Math.floor(avatarY) + AndroidUtilities.dp(24) + (float) Math.floor(11 * AndroidUtilities.density) + avatarContainer.getMeasuredHeight() * (avatarScale - (42f + 18f) / 42f) / 2f;
    }

    public RecyclerListView getListView() {
        return listView;
    }

    private void needLayoutText(float diff) {
        FrameLayout.LayoutParams layoutParams;
        float scale = nameTextView[1].getScaleX();
        float maxScale = extraHeight > AndroidUtilities.dp(88f) ? 1.67f : 1.12f;

        if (extraHeight > AndroidUtilities.dp(88f) && scale != maxScale) {
            return;
        }

        int viewWidth = AndroidUtilities.isTablet() ? AndroidUtilities.dp(490) : AndroidUtilities.displaySize.x;
        ActionBarMenuItem item = avatarsViewPagerIndicatorView.getSecondaryMenuItem();
        int extra = 0;
        if (editItemVisible) {
            extra += 48;
        }
        if (callItemVisible) {
            extra += 48;
        }
        if (videoCallItemVisible) {
            extra += 48;
        }
        if (searchItem != null) {
            extra += 48;
        }
        int buttonsWidth = AndroidUtilities.dp(118 + 8 + (40 + extra * (1.0f - mediaHeaderAnimationProgress)));
        int minWidth = viewWidth - buttonsWidth;

        int width = (int) (viewWidth - buttonsWidth * Math.max(0.0f, 1.0f - (diff != 1.0f ? diff * 0.15f / (1.0f - diff) : 1.0f)) - nameTextView[1].getTranslationX());
        float width2 = nameTextView[1].getPaint().measureText(nameTextView[1].getText().toString()) * scale + nameTextView[1].getSideDrawablesSize();
        layoutParams = (FrameLayout.LayoutParams) nameTextView[1].getLayoutParams();
        int prevWidth = layoutParams.width;
        if (width < width2) {
            layoutParams.width = Math.max(minWidth, (int) Math.ceil((width - AndroidUtilities.dp(24)) / (scale + ((maxScale - scale) * 7.0f))));
        } else {
            layoutParams.width = (int) Math.ceil(width2);
        }
        layoutParams.width = (int) Math.min((viewWidth - nameTextView[1].getX()) / scale - AndroidUtilities.dp(8), layoutParams.width);
        if (layoutParams.width != prevWidth) {
            nameTextView[1].requestLayout();
        }

        width2 = onlineTextView[1].getPaint().measureText(onlineTextView[1].getText().toString()) + onlineTextView[1].getRightDrawableWidth();
        layoutParams = (FrameLayout.LayoutParams) onlineTextView[1].getLayoutParams();
        FrameLayout.LayoutParams layoutParams2 = (FrameLayout.LayoutParams) mediaCounterTextView.getLayoutParams();
        prevWidth = layoutParams.width;
        layoutParams2.rightMargin = layoutParams.rightMargin = (int) Math.ceil(onlineTextView[1].getTranslationX() + AndroidUtilities.dp(8) + AndroidUtilities.dp(40) * (1.0f - diff));
        if (width < width2) {
            layoutParams2.width = layoutParams.width = (int) Math.ceil(width);
        } else {
            layoutParams2.width = layoutParams.width = LayoutHelper.WRAP_CONTENT;
        }
        if (prevWidth != layoutParams.width) {
            onlineTextView[2].getLayoutParams().width = layoutParams.width;
            onlineTextView[2].requestLayout();
            onlineTextView[3].getLayoutParams().width = layoutParams.width;
            onlineTextView[3].requestLayout();
            onlineTextView[1].requestLayout();
            mediaCounterTextView.requestLayout();
        }
    }

    private void fixLayout() {
        if (fragmentView == null) {
            return;
        }
        fragmentView.getViewTreeObserver().addOnPreDrawListener(new ViewTreeObserver.OnPreDrawListener() {
            @Override
            public boolean onPreDraw() {
                if (fragmentView != null) {
                    checkListViewScroll();
                    needLayout(true);
                    fragmentView.getViewTreeObserver().removeOnPreDrawListener(this);
                }
                return true;
            }
        });
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        if (sharedMediaLayout != null) {
            sharedMediaLayout.onConfigurationChanged(newConfig);
        }
        invalidateIsInLandscapeMode();
        if (isInLandscapeMode && isPulledDown) {
            final View view = layoutManager.findViewByPosition(0);
            if (view != null) {
                listView.scrollBy(0, view.getTop() - AndroidUtilities.dp(88));
            }
        }
        fixLayout();
    }

    private void invalidateIsInLandscapeMode() {
        final Point size = new Point();
        final Display display = getParentActivity().getWindowManager().getDefaultDisplay();
        display.getSize(size);
        isInLandscapeMode = size.x > size.y;
    }

    @SuppressWarnings("unchecked")
    @Override
    public void didReceivedNotification(int id, int account, final Object... args) {
        if (id == NotificationCenter.uploadStoryEnd || id == NotificationCenter.chatWasBoostedByUser) {
            checkCanSendStoryForPosting();
        } else if (id == NotificationCenter.updateInterfaces) {
            int mask = (Integer) args[0];
            boolean infoChanged = (mask & MessagesController.UPDATE_MASK_AVATAR) != 0 || (mask & MessagesController.UPDATE_MASK_NAME) != 0 || (mask & MessagesController.UPDATE_MASK_STATUS) != 0 || (mask & MessagesController.UPDATE_MASK_EMOJI_STATUS) != 0;
            if (userId != 0) {
                if (infoChanged) {
                    updateProfileData(true);
                }
                if ((mask & MessagesController.UPDATE_MASK_PHONE) != 0) {
                    if (listView != null) {
                        RecyclerListView.Holder holder = (RecyclerListView.Holder) listView.findViewHolderForPosition(phoneRow);
                        if (holder != null) {
                            listAdapter.onBindViewHolder(holder, phoneRow);
                        }
                    }
                }
            } else if (chatId != 0) {
                if ((mask & MessagesController.UPDATE_MASK_CHAT) != 0 || (mask & MessagesController.UPDATE_MASK_CHAT_AVATAR) != 0 || (mask & MessagesController.UPDATE_MASK_CHAT_NAME) != 0 || (mask & MessagesController.UPDATE_MASK_CHAT_MEMBERS) != 0 || (mask & MessagesController.UPDATE_MASK_STATUS) != 0 || (mask & MessagesController.UPDATE_MASK_EMOJI_STATUS) != 0) {
                    if ((mask & MessagesController.UPDATE_MASK_CHAT) != 0) {
                        updateListAnimated(true);
                    } else {
                        updateOnlineCount(true);
                    }
                    updateProfileData(true);
                }
                if (infoChanged) {
                    if (listView != null) {
                        int count = listView.getChildCount();
                        for (int a = 0; a < count; a++) {
                            View child = listView.getChildAt(a);
                            if (child instanceof UserCell) {
                                ((UserCell) child).update(mask);
                            }
                        }
                    }
                }
            }
        } else if (id == NotificationCenter.chatOnlineCountDidLoad) {
            Long chatId = (Long) args[0];
            if (chatInfo == null || currentChat == null || currentChat.id != chatId) {
                return;
            }
            chatInfo.online_count = (Integer) args[1];
            updateOnlineCount(true);
            updateProfileData(false);
        } else if (id == NotificationCenter.contactsDidLoad || id == NotificationCenter.channelRightsUpdated) {
            createActionBarMenu(true);
        } else if (id == NotificationCenter.encryptedChatCreated) {
            if (creatingChat) {
                AndroidUtilities.runOnUIThread(() -> {
                    getNotificationCenter().removeObserver(ProfileActivity.this, NotificationCenter.closeChats);
                    getNotificationCenter().postNotificationName(NotificationCenter.closeChats);
                    TLRPC.EncryptedChat encryptedChat = (TLRPC.EncryptedChat) args[0];
                    Bundle args2 = new Bundle();
                    args2.putInt("enc_id", encryptedChat.id);
                    presentFragment(new ChatActivity(args2), true);
                });
            }
        } else if (id == NotificationCenter.encryptedChatUpdated) {
            TLRPC.EncryptedChat chat = (TLRPC.EncryptedChat) args[0];
            if (currentEncryptedChat != null && chat.id == currentEncryptedChat.id) {
                currentEncryptedChat = chat;
                updateListAnimated(false);
                if (flagSecure != null) {
                    flagSecure.invalidate();
                }
            }
        } else if (id == NotificationCenter.blockedUsersDidLoad) {
            boolean oldValue = userBlocked;
            userBlocked = getMessagesController().blockePeers.indexOfKey(userId) >= 0;
            if (oldValue != userBlocked) {
                createActionBarMenu(true);
                updateListAnimated(false);
            }
        } else if (id == NotificationCenter.groupCallUpdated) {
            Long chatId = (Long) args[0];
            if (currentChat != null && chatId == currentChat.id && ChatObject.canManageCalls(currentChat)) {
                TLRPC.ChatFull chatFull = MessagesController.getInstance(currentAccount).getChatFull(chatId);
                if (chatFull != null) {
                    if (chatInfo != null) {
                        chatFull.participants = chatInfo.participants;
                    }
                    chatInfo = chatFull;
                }
                if (sharedMediaLayout != null) {
                    sharedMediaLayout.setChatInfo(chatInfo);
                }
                if (chatInfo != null && (chatInfo.call == null && !hasVoiceChatItem || chatInfo.call != null && hasVoiceChatItem)) {
                    createActionBarMenu(false);
                }
                if (storyView != null && chatInfo != null) {
                    storyView.setStories(chatInfo.stories);
                }
                if (giftsView != null) {
                    giftsView.update();
                }
                if (avatarImage != null) {
                    avatarImage.setHasStories(needInsetForStories());
                }
                if (chatId != 0) {
                    otherItem.setSubItemShown(gift_premium, !BuildVars.IS_BILLING_UNAVAILABLE && !getMessagesController().premiumPurchaseBlocked() && chatInfo != null && chatInfo.stargifts_available);
                }
            }
        } else if (id == NotificationCenter.chatInfoDidLoad) {
            final TLRPC.ChatFull chatFull = (TLRPC.ChatFull) args[0];
            if (chatFull.id == chatId) {
                final boolean byChannelUsers = (Boolean) args[2];
                if (chatInfo instanceof TLRPC.TL_channelFull) {
                    if (chatFull.participants == null) {
                        chatFull.participants = chatInfo.participants;
                    }
                }
                final boolean loadChannelParticipants = chatInfo == null && chatFull instanceof TLRPC.TL_channelFull;
                chatInfo = chatFull;
                if (mergeDialogId == 0 && chatInfo.migrated_from_chat_id != 0) {
                    mergeDialogId = -chatInfo.migrated_from_chat_id;
                    getMediaDataController().getMediaCount(mergeDialogId, topicId, MediaDataController.MEDIA_PHOTOVIDEO, classGuid, true);
                }
                fetchUsersFromChannelInfo();
                if (avatarsViewPager != null && !isTopic) {
                    avatarsViewPager.setChatInfo(chatInfo);
                }
                updateListAnimated(true);
                TLRPC.Chat newChat = getMessagesController().getChat(chatId);
                if (newChat != null) {
                    currentChat = newChat;
                    createActionBarMenu(true);
                }
                if (flagSecure != null) {
                    flagSecure.invalidate();
                }
                if (currentChat.megagroup && (loadChannelParticipants || !byChannelUsers)) {
                    getChannelParticipants(true);
                }

                updateAutoDeleteItem();
                updateTtlIcon();
                if (storyView != null && chatInfo != null) {
                    storyView.setStories(chatInfo.stories);
                }
                if (giftsView != null) {
                    giftsView.update();
                }
                if (avatarImage != null) {
                    avatarImage.setHasStories(needInsetForStories());
                }
                if (sharedMediaLayout != null) {
                    sharedMediaLayout.setChatInfo(chatInfo);
                }
            }
        } else if (id == NotificationCenter.closeChats) {
            removeSelfFromStack(true);
        } else if (id == NotificationCenter.botInfoDidLoad) {
            final TL_bots.BotInfo info = (TL_bots.BotInfo) args[0];
            if (info.user_id == userId) {
                botInfo = info;
                updateListAnimated(false);
            }
        } else if (id == NotificationCenter.userInfoDidLoad) {
            final long uid = (Long) args[0];
            if (uid == userId) {
                userInfo = (TLRPC.UserFull) args[1];
                if (storyView != null) {
                    storyView.setStories(userInfo.stories);
                }
                if (giftsView != null) {
                    giftsView.update();
                }
                if (avatarImage != null) {
                    avatarImage.setHasStories(needInsetForStories());
                }
                if (sharedMediaLayout != null) {
                    sharedMediaLayout.setUserInfo(userInfo);
                }
                if (imageUpdater != null) {
                    if (listAdapter != null && !TextUtils.equals(userInfo.about, currentBio)) {
                        listAdapter.notifyItemChanged(bioRow);
                    }
                } else {
                    if (!openAnimationInProgress && !callItemVisible) {
                        createActionBarMenu(true);
                    } else {
                        recreateMenuAfterAnimation = true;
                    }
                    updateListAnimated(false);
                    if (sharedMediaLayout != null) {
                        sharedMediaLayout.setCommonGroupsCount(userInfo.common_chats_count);
                        updateSelectedMediaTabText();
                        if (sharedMediaPreloader == null || sharedMediaPreloader.isMediaWasLoaded()) {
                            resumeDelayedFragmentAnimation();
                            needLayout(true);
                        }
                    }
                }
                updateAutoDeleteItem();
                updateTtlIcon();
                if (profileChannelMessageFetcher == null && !isSettings()) {
                    profileChannelMessageFetcher = new ProfileChannelCell.ChannelMessageFetcher(currentAccount);
                    profileChannelMessageFetcher.subscribe(() -> updateListAnimated(false));
                    profileChannelMessageFetcher.fetch(userInfo);
                }
                if (!isSettings()) {
                    ProfileBirthdayEffect.BirthdayEffectFetcher oldFetcher = birthdayFetcher;
                    birthdayFetcher = ProfileBirthdayEffect.BirthdayEffectFetcher.of(currentAccount, userInfo, birthdayFetcher);
                    createdBirthdayFetcher = birthdayFetcher != oldFetcher;
                    if (birthdayFetcher != null) {
                        birthdayFetcher.subscribe(this::createBirthdayEffect);
                    }
                }
                if (otherItem != null) {
                    if (hasPrivacyCommand()) {
                        otherItem.showSubItem(bot_privacy);
                    } else {
                        otherItem.hideSubItem(bot_privacy);
                    }
                }
            }
        } else if (id == NotificationCenter.privacyRulesUpdated) {
            if (qrItem != null) {
                updateQrItemVisibility(true);
            }
        } else if (id == NotificationCenter.didReceiveNewMessages) {
            final boolean scheduled = (Boolean) args[2];
            if (scheduled) {
                return;
            }
            final long did = getDialogId();
            if (did == (Long) args[0]) {
                boolean enc = DialogObject.isEncryptedDialog(did);
                ArrayList<MessageObject> arr = (ArrayList<MessageObject>) args[1];
                for (int a = 0; a < arr.size(); a++) {
                    MessageObject obj = arr.get(a);
                    if (currentEncryptedChat != null && obj.messageOwner.action instanceof TLRPC.TL_messageEncryptedAction && obj.messageOwner.action.encryptedAction instanceof TLRPC.TL_decryptedMessageActionSetMessageTTL) {
                        TLRPC.TL_decryptedMessageActionSetMessageTTL action = (TLRPC.TL_decryptedMessageActionSetMessageTTL) obj.messageOwner.action.encryptedAction;
                        if (listAdapter != null) {
                            listAdapter.notifyDataSetChanged();
                        }
                    }
                }
            }
        } else if (id == NotificationCenter.emojiLoaded) {
            if (listView != null) {
                listView.invalidateViews();
            }
        } else if (id == NotificationCenter.reloadInterface) {
            updateListAnimated(false);
        } else if (id == NotificationCenter.newSuggestionsAvailable) {
            final int prevRow1 = passwordSuggestionRow;
            final int prevRow2 = phoneSuggestionRow;
            final int prevRow3 = graceSuggestionRow;
            updateRowsIds();
            if (prevRow1 != passwordSuggestionRow || prevRow2 != phoneSuggestionRow || prevRow3 != graceSuggestionRow) {
                listAdapter.notifyDataSetChanged();
            }
        } else if (id == NotificationCenter.topicsDidLoaded) {
            if (isTopic) {
                updateProfileData(false);
            }
        } else if (id == NotificationCenter.updateSearchSettings) {
            if (searchAdapter != null) {
                searchAdapter.searchArray = searchAdapter.onCreateSearchArray(this);
                searchAdapter.recentSearches.clear();
                searchAdapter.updateSearchArray();
                searchAdapter.search(searchAdapter.lastSearchString);
            }
        } else if (id == NotificationCenter.reloadDialogPhotos) {
            updateProfileData(false);
        } else if (id == NotificationCenter.storiesUpdated || id == NotificationCenter.storiesReadUpdated) {
            if (avatarImage != null) {
                avatarImage.setHasStories(needInsetForStories());
                updateAvatarRoundRadius();
            }
            if (storyView != null) {
                if (userInfo != null) {
                    storyView.setStories(userInfo.stories);
                } else if (chatInfo != null) {
                    storyView.setStories(chatInfo.stories);
                }
            }
        } else if (id == NotificationCenter.userIsPremiumBlockedUpadted) {
            if (otherItem != null) {
                otherItem.setSubItemShown(start_secret_chat, DialogObject.isEmpty(getMessagesController().isUserContactBlocked(userId)));
            }
            updateEditColorIcon();
        } else if (id == NotificationCenter.currentUserPremiumStatusChanged) {
            updateEditColorIcon();
        } else if (id == NotificationCenter.starBalanceUpdated) {
            updateListAnimated(false);
        } else if (id == NotificationCenter.botStarsUpdated) {
            updateListAnimated(false);
        } else if (id == NotificationCenter.botStarsTransactionsLoaded) {
            updateListAnimated(false);
        } else if (id == NotificationCenter.dialogDeleted) {
            final long dialogId = (long) args[0];
            if (getDialogId() == dialogId) {
                if (parentLayout != null && parentLayout.getLastFragment() == this) {
                    finishFragment();
                } else {
                    removeSelfFromStack();
                }
            }
        } else if (id == NotificationCenter.channelRecommendationsLoaded) {
            final long dialogId = (long) args[0];
            if (sharedMediaRow < 0 && dialogId == getDialogId()) {
                updateRowsIds();
                updateSelectedMediaTabText();
                if (listAdapter != null) {
                    listAdapter.notifyDataSetChanged();
                }
            }
        } else if (id == NotificationCenter.starUserGiftsLoaded) {
            final long dialogId = (long) args[0];
            if (dialogId == getDialogId() && !isSettings()) {
                if (sharedMediaRow < 0) {
                    updateRowsIds();
                    updateSelectedMediaTabText();
                    if (listAdapter != null) {
                        listAdapter.notifyDataSetChanged();
                    }
                    AndroidUtilities.runOnUIThread(() -> {
                        if (sharedMediaLayout != null) {
                            sharedMediaLayout.updateTabs(true);
                            sharedMediaLayout.updateAdapters();
                        }
                    });
                } else if (sharedMediaLayout != null) {
                    sharedMediaLayout.updateTabs(true);
                }
            }
        }
    }

    private void updateAutoDeleteItem() {
        if (autoDeleteItem == null || autoDeletePopupWrapper == null) {
            return;
        }
        int ttl = 0;
        if (userInfo != null || chatInfo != null) {
            ttl = userInfo != null ? userInfo.ttl_period : chatInfo.ttl_period;
        }
        autoDeleteItemDrawable.setTime(ttl);
        autoDeletePopupWrapper.updateItems(ttl);
    }

    private void updateTimeItem() {
        if (timerDrawable == null) {
            return;
        }
        final boolean fromMonoforum = previousTransitionFragment instanceof ChatActivity && ChatObject.isMonoForum(((ChatActivity) previousTransitionFragment).getCurrentChat());
        if (fromMonoforum) {
            timeItem.setTag(null);
            timeItem.setVisibility(View.GONE);
        } else if (currentEncryptedChat != null) {
            timerDrawable.setTime(currentEncryptedChat.ttl);
            timeItem.setTag(1);
            timeItem.setVisibility(View.VISIBLE);
        } else if (userInfo != null) {
            timerDrawable.setTime(userInfo.ttl_period);
            if (needTimerImage && userInfo.ttl_period != 0) {
                timeItem.setTag(1);
                timeItem.setVisibility(View.VISIBLE);
            } else {
                timeItem.setTag(null);
                timeItem.setVisibility(View.GONE);
            }
        } else if (chatInfo != null) {
            timerDrawable.setTime(chatInfo.ttl_period);
            if (needTimerImage && chatInfo.ttl_period != 0) {
                timeItem.setTag(1);
                timeItem.setVisibility(View.VISIBLE);
            } else {
                timeItem.setTag(null);
                timeItem.setVisibility(View.GONE);
            }
        } else {
            timeItem.setTag(null);
            timeItem.setVisibility(View.GONE);
        }
    }

    private void updateStar() {
        if (starBgItem == null || starFgItem == null) return;
        if (needStarImage && currentChat != null && (currentChat.flags2 & 2048) != 0) {
            starFgItem.setTag(1);
            starFgItem.setVisibility(View.VISIBLE);
            starBgItem.setTag(1);
            starBgItem.setVisibility(View.VISIBLE);
        } else {
            starFgItem.setTag(null);
            starFgItem.setVisibility(View.GONE);
            starBgItem.setTag(null);
            starBgItem.setVisibility(View.GONE);
        }
    }

    @Override
    public boolean needDelayOpenAnimation() {
        if (playProfileAnimation == 0) {
            return true;
        }
        return false;
    }

    @Override
    public void mediaCountUpdated() {
        if (sharedMediaLayout != null && sharedMediaPreloader != null) {
            sharedMediaLayout.setNewMediaCounts(sharedMediaPreloader.getLastMediaCount());
        }
        updateSharedMediaRows();
        updateSelectedMediaTabText();

        if (userInfo != null) {
            resumeDelayedFragmentAnimation();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        if (sharedMediaLayout != null) {
            sharedMediaLayout.onResume();
        }
        invalidateIsInLandscapeMode();
        if (listAdapter != null) {
            // saveScrollPosition();
            firstLayout = true;
            listAdapter.notifyDataSetChanged();
        }
        if (!parentLayout.isInPreviewMode() && blurredView != null && blurredView.getVisibility() == View.VISIBLE) {
            blurredView.setVisibility(View.GONE);
            blurredView.setBackground(null);
        }

        if (imageUpdater != null) {
            imageUpdater.onResume();
            setParentActivityTitle(LocaleController.getString(R.string.Settings));
        }

        updateProfileData(true);
        fixLayout();
        if (nameTextView[1] != null) {
            setParentActivityTitle(nameTextView[1].getText());
        }
        if (userId != 0) {
            final TLRPC.User user = getMessagesController().getUser(userId);
            if (user != null && user.photo == null) {
                if (extraHeight >= AndroidUtilities.dp(88f)) {
                    expandAnimator.cancel();
                    expandAnimatorValues[0] = 1f;
                    expandAnimatorValues[1] = 0f;
                    setAvatarExpandProgress(1f);
                    avatarsViewPager.setVisibility(View.GONE);
                    extraHeight = AndroidUtilities.dp(88f);
                    allowPullingDown = false;
                    layoutManager.scrollToPositionWithOffset(0, AndroidUtilities.dp(88) - listView.getPaddingTop());
                }
            }
        }
        if (flagSecure != null) {
            flagSecure.attach();
        }
        updateItemsUsername();
    }

    @Override
    public void onPause() {
        super.onPause();
        if (undoView != null) {
            undoView.hide(true, 0);
        }
        if (imageUpdater != null) {
            imageUpdater.onPause();
        }
        if (flagSecure != null) {
            flagSecure.detach();
        }
        if (sharedMediaLayout != null) {
            sharedMediaLayout.onPause();
        }
    }

    @Override
    public boolean isSwipeBackEnabled(MotionEvent event) {
        if (avatarsViewPager != null && avatarsViewPager.getVisibility() == View.VISIBLE && avatarsViewPager.getRealCount() > 1) {
            avatarsViewPager.getHitRect(rect);
            if (event != null && rect.contains((int) event.getX(), (int) event.getY() - actionBar.getMeasuredHeight())) {
                return false;
            }
        }
        if (sharedMediaRow == -1 || sharedMediaLayout == null) {
            return true;
        }
        if (!sharedMediaLayout.isSwipeBackEnabled()) {
            return false;
        }
        sharedMediaLayout.getHitRect(rect);
        if (event != null && !rect.contains((int) event.getX(), (int) event.getY() - actionBar.getMeasuredHeight())) {
            return true;
        }
        return sharedMediaLayout.isCurrentTabFirst();
    }

    @Override
    public boolean canBeginSlide() {
        if (!sharedMediaLayout.isSwipeBackEnabled()) {
            return false;
        }
        return super.canBeginSlide();
    }

    public UndoView getUndoView() {
        return undoView;
    }

    public boolean onBackPressed() {
        if (closeSheet()) {
            return false;
        }
        return actionBar.isEnabled() && (sharedMediaRow == -1 || sharedMediaLayout == null || !sharedMediaLayout.closeActionMode());
    }

    public boolean isSettings() {
        return imageUpdater != null && !myProfile;
    }

    @Override
    public void onBecomeFullyHidden() {
        if (undoView != null) {
            undoView.hide(true, 0);
        }
        super.onBecomeFullyHidden();
        fullyVisible = false;
    }

    public void setPlayProfileAnimation(int type) {
        SharedPreferences preferences = MessagesController.getGlobalMainSettings();
        if (!AndroidUtilities.isTablet()) {
            needTimerImage = type != 0;
            needStarImage = type != 0;
            if (preferences.getBoolean("view_animations", true)) {
                playProfileAnimation = type;
            } else if (type == 2) {
                expandPhoto = true;
            }
        }
    }

    private void updateSharedMediaRows() {
        if (listAdapter == null) {
            return;
        }
        updateListAnimated(false);
    }

    public boolean isFragmentOpened;

    @Override
    public void onTransitionAnimationStart(boolean isOpen, boolean backward) {
        super.onTransitionAnimationStart(isOpen, backward);
        isFragmentOpened = isOpen;
        if ((!isOpen && backward || isOpen && !backward) && playProfileAnimation != 0 && allowProfileAnimation && !isPulledDown) {
            openAnimationInProgress = true;
        }
        if (isOpen) {
            if (imageUpdater != null) {
                transitionIndex = getNotificationCenter().setAnimationInProgress(transitionIndex, new int[]{NotificationCenter.dialogsNeedReload, NotificationCenter.closeChats, NotificationCenter.mediaCountDidLoad, NotificationCenter.mediaCountsDidLoad, NotificationCenter.userInfoDidLoad, NotificationCenter.needCheckSystemBarColors});
            } else {
                transitionIndex = getNotificationCenter().setAnimationInProgress(transitionIndex, new int[]{NotificationCenter.dialogsNeedReload, NotificationCenter.closeChats, NotificationCenter.mediaCountDidLoad, NotificationCenter.mediaCountsDidLoad, NotificationCenter.needCheckSystemBarColors});
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP && !backward && getParentActivity() != null) {
                navigationBarAnimationColorFrom = getParentActivity().getWindow().getNavigationBarColor();
            }
        }
        transitionAnimationInProress = true;
        checkPhotoDescriptionAlpha();
    }

    @Override
    public void onTransitionAnimationEnd(boolean isOpen, boolean backward) {
        if (isOpen) {
            if (!backward) {
                if (playProfileAnimation != 0 && allowProfileAnimation) {
                    if (playProfileAnimation == 1) {
                        currentExpandAnimatorValue = 0f;
                    }
                    openAnimationInProgress = false;
                    checkListViewScroll();
                    if (recreateMenuAfterAnimation) {
                        createActionBarMenu(true);
                    }
                }
                if (!fragmentOpened) {
                    fragmentOpened = true;
                    invalidateScroll = true;
                    fragmentView.requestLayout();
                }
            }
            getNotificationCenter().onAnimationFinish(transitionIndex);

            if (blurredView != null && blurredView.getVisibility() == View.VISIBLE) {
                blurredView.setVisibility(View.GONE);
                blurredView.setBackground(null);
            }
        }
        transitionAnimationInProress = false;
        checkPhotoDescriptionAlpha();
    }

    @Keep
    public float getAvatarAnimationProgress() {
        return avatarAnimationProgress;
    }

    public AboutLinkCell aboutLinkCell;

    @Keep
    public void setAvatarAnimationProgress(float progress) {
        avatarAnimationProgress = currentExpandAnimatorValue = progress;
        checkPhotoDescriptionAlpha();
        if (playProfileAnimation == 2) {
            avatarImage.setProgressToExpand(progress);
        }

        listView.setAlpha(progress);

        listView.setTranslationX(AndroidUtilities.dp(48) - AndroidUtilities.dp(48) * progress);

        int color;
        if (playProfileAnimation == 2 && avatarColor != 0) {
            color = avatarColor;
        } else {
            color = AvatarDrawable.getProfileBackColorForId(userId != 0 || ChatObject.isChannel(chatId, currentAccount) && !currentChat.megagroup ? 5 : chatId, resourcesProvider);
        }

        int actionBarColor = actionBarAnimationColorFrom != 0 ? actionBarAnimationColorFrom : getThemedColor(Theme.key_actionBarDefault);
        int actionBarColor2 = actionBarColor;
        if (SharedConfig.chatBlurEnabled()) {
            actionBarColor = ColorUtils.setAlphaComponent(actionBarColor, 0);
        }
        topView.setBackgroundColor(ColorUtils.blendARGB(actionBarColor, color, progress));
        timerDrawable.setBackgroundColor(ColorUtils.blendARGB(actionBarColor2, color, progress));

        color = AvatarDrawable.getIconColorForId(userId != 0 || ChatObject.isChannel(chatId, currentAccount) && !currentChat.megagroup ? 5 : chatId, resourcesProvider);
        int iconColor = peerColor != null ? Color.WHITE : getThemedColor(Theme.key_actionBarDefaultIcon);
        actionBar.setItemsColor(ColorUtils.blendARGB(iconColor, color, avatarAnimationProgress), false);

        color = getThemedColor(Theme.key_profile_title);
        int titleColor = getThemedColor(Theme.key_actionBarDefaultTitle);
        for (int i = 0; i < 2; i++) {
            if (nameTextView[i] == null || i == 1 && playProfileAnimation == 2) {
                continue;
            }
            nameTextView[i].setTextColor(ColorUtils.blendARGB(titleColor, color, progress));
        }

        color = isOnline[0] ? getThemedColor(Theme.key_profile_status) : AvatarDrawable.getProfileTextColorForId(userId != 0 || ChatObject.isChannel(chatId, currentAccount) && !currentChat.megagroup ? 5 : chatId, resourcesProvider);
        int subtitleColor = getThemedColor(isOnline[0] ? Theme.key_chat_status : Theme.key_actionBarDefaultSubtitle);
        for (int i = 0; i < 3; i++) {
            if (onlineTextView[i] == null || i == 1 || i == 2 && playProfileAnimation == 2) {
                continue;
            }
            onlineTextView[i].setTextColor(ColorUtils.blendARGB(i == 0 ? subtitleColor : applyPeerColor(subtitleColor, true, isOnline[0]), i == 0 ? color : applyPeerColor(color, true, isOnline[0]), progress));
        }
        extraHeight = initialAnimationExtraHeight * progress;
        color = AvatarDrawable.getProfileColorForId(userId != 0 ? userId : chatId, resourcesProvider);
        int color2 = AvatarDrawable.getColorForId(userId != 0 ? userId : chatId);
        if (color != color2) {
            avatarDrawable.setColor(ColorUtils.blendARGB(color2, color, progress));
            avatarImage.invalidate();
        }

        if (navigationBarAnimationColorFrom != 0) {
            color = ColorUtils.blendARGB(navigationBarAnimationColorFrom, getNavigationBarColor(), progress);
            setNavigationBarColor(color);
        }

        topView.invalidate();

        needLayout(true);
        if (fragmentView != null) {
            fragmentView.invalidate();
        }

        if (aboutLinkCell != null) {
            aboutLinkCell.invalidate();
        }

        if (getDialogId() > 0) {
            if (avatarImage != null) {
                avatarImage.setProgressToStoriesInsets(avatarAnimationProgress);
            }
            if (storyView != null) {
                storyView.setProgressToStoriesInsets(avatarAnimationProgress);
            }
            if (giftsView != null) {
                giftsView.setProgressToStoriesInsets(avatarAnimationProgress);
            }
        }
    }

    boolean profileTransitionInProgress;

    @Override
    public AnimatorSet onCustomTransitionAnimation(final boolean isOpen, final Runnable callback) {
        if (playProfileAnimation != 0 && allowProfileAnimation && !isPulledDown && !disableProfileAnimation) {
            if (timeItem != null) {
                timeItem.setAlpha(1.0f);
            }
            if (starFgItem != null) {
                starFgItem.setAlpha(1.0f);
                starFgItem.setScaleX(1.0f);
                starFgItem.setScaleY(1.0f);
            }
            if (starBgItem != null) {
                starBgItem.setAlpha(1.0f);
                starBgItem.setScaleX(1.0f);
                starBgItem.setScaleY(1.0f);
            }
            previousTransitionMainFragment = null;
            if (parentLayout != null && parentLayout.getFragmentStack().size() >= 2) {
                BaseFragment fragment = parentLayout.getFragmentStack().get(parentLayout.getFragmentStack().size() - 2);
                if (fragment instanceof ChatActivityInterface) {
                    previousTransitionFragment = (ChatActivityInterface) fragment;
                }
                if (fragment instanceof DialogsActivity) {
                    DialogsActivity dialogsActivity = (DialogsActivity) fragment;
                    if (dialogsActivity.rightSlidingDialogContainer != null && dialogsActivity.rightSlidingDialogContainer.currentFragment instanceof ChatActivityInterface) {
                        previousTransitionMainFragment = dialogsActivity;
                        previousTransitionFragment = (ChatActivityInterface) dialogsActivity.rightSlidingDialogContainer.currentFragment;
                    }
                }
            }
            final boolean fromChat = previousTransitionFragment instanceof ChatActivity && ((ChatActivity) previousTransitionFragment).getCurrentChat() != null;
            if (previousTransitionFragment != null) {
                updateTimeItem();
                updateStar();
            }
            final AnimatorSet animatorSet = new AnimatorSet();
            animatorSet.setDuration(playProfileAnimation == 2 ? 250 : 180);
            listView.setLayerType(View.LAYER_TYPE_HARDWARE, null);
            ActionBarMenu menu = actionBar.createMenu();
            if (menu.getItem(10) == null) {
                if (animatingItem == null) {
                    animatingItem = menu.addItem(10, R.drawable.ic_ab_other);
                }
            }
            if (isOpen) {
                for (int i = 0; i < 2; i++) {
                    FrameLayout.LayoutParams layoutParams = (FrameLayout.LayoutParams) onlineTextView[i + 1].getLayoutParams();
                    layoutParams.rightMargin = (int) (-21 * AndroidUtilities.density + AndroidUtilities.dp(8));
                    onlineTextView[i + 1].setLayoutParams(layoutParams);
                }


                if (playProfileAnimation != 2) {
                    int width = (int) Math.ceil(AndroidUtilities.displaySize.x - AndroidUtilities.dp(118 + 8) + 21 * AndroidUtilities.density);
                    float width2 = nameTextView[1].getPaint().measureText(nameTextView[1].getText().toString()) * 1.12f + nameTextView[1].getSideDrawablesSize();
                    FrameLayout.LayoutParams layoutParams = (FrameLayout.LayoutParams) nameTextView[1].getLayoutParams();
                    if (width < width2) {
                        layoutParams.width = (int) Math.ceil(width / 1.12f);
                    } else {
                        layoutParams.width = LayoutHelper.WRAP_CONTENT;
                    }
                    nameTextView[1].setLayoutParams(layoutParams);

                    initialAnimationExtraHeight = AndroidUtilities.dp(88f);
                } else {
                    FrameLayout.LayoutParams layoutParams = (FrameLayout.LayoutParams) nameTextView[1].getLayoutParams();
                    layoutParams.width = (int) ((AndroidUtilities.displaySize.x - AndroidUtilities.dp(32)) / 1.67f);
                    nameTextView[1].setLayoutParams(layoutParams);
                }
                fragmentView.setBackgroundColor(0);
                setAvatarAnimationProgress(0);
                ArrayList<Animator> animators = new ArrayList<>();
                animators.add(ObjectAnimator.ofFloat(this, "avatarAnimationProgress", 0.0f, 1.0f));
                if (writeButton != null && writeButton.getTag() == null) {
                    writeButton.setScaleX(0.2f);
                    writeButton.setScaleY(0.2f);
                    writeButton.setAlpha(0.0f);
                    animators.add(ObjectAnimator.ofFloat(writeButton, View.SCALE_X, 1.0f));
                    animators.add(ObjectAnimator.ofFloat(writeButton, View.SCALE_Y, 1.0f));
                    animators.add(ObjectAnimator.ofFloat(writeButton, View.ALPHA, 1.0f));
                }
                if (playProfileAnimation == 2) {
                    avatarColor = getAverageColor(avatarImage.getImageReceiver());
                    nameTextView[1].setTextColor(Color.WHITE);
                    onlineTextView[1].setTextColor(0xB3FFFFFF);
                    actionBar.setItemsBackgroundColor(Theme.ACTION_BAR_WHITE_SELECTOR_COLOR, false);
                    if (showStatusButton != null) {
                        showStatusButton.setBackgroundColor(0x23ffffff);
                    }
                    overlaysView.setOverlaysVisible();
                }
                for (int a = 0; a < 2; a++) {
                    nameTextView[a].setAlpha(a == 0 ? 1.0f : 0.0f);
                    animators.add(ObjectAnimator.ofFloat(nameTextView[a], View.ALPHA, a == 0 ? 0.0f : 1.0f));
                }
                if (storyView != null) {
                    if (getDialogId() > 0) {
                        storyView.setAlpha(0f);
                        animators.add(ObjectAnimator.ofFloat(storyView, View.ALPHA, 1.0f));
                    } else {
                        storyView.setAlpha(1f);
                        storyView.setFragmentTransitionProgress(0);
                        animators.add(ObjectAnimator.ofFloat(storyView, ProfileStoriesView.FRAGMENT_TRANSITION_PROPERTY, 1.0f));
                    }
                }
                if (giftsView != null) {
                    giftsView.setAlpha(0f);
                    animators.add(ObjectAnimator.ofFloat(giftsView, View.ALPHA, 1.0f));
                }
                if (timeItem.getTag() != null) {
                    animators.add(ObjectAnimator.ofFloat(timeItem, View.ALPHA, 1.0f, 0.0f));
                    animators.add(ObjectAnimator.ofFloat(timeItem, View.SCALE_X, 1.0f, 0.0f));
                    animators.add(ObjectAnimator.ofFloat(timeItem, View.SCALE_Y, 1.0f, 0.0f));
                }
                if (starFgItem.getTag() != null) {
                    animators.add(ObjectAnimator.ofFloat(starFgItem, View.ALPHA, 1.0f, 0.0f));
                    animators.add(ObjectAnimator.ofFloat(starFgItem, View.SCALE_X, 1.0f, 0.0f));
                    animators.add(ObjectAnimator.ofFloat(starFgItem, View.SCALE_Y, 1.0f, 0.0f));
                }
                if (starBgItem.getTag() != null) {
                    animators.add(ObjectAnimator.ofFloat(starBgItem, View.ALPHA, 1.0f, 0.0f));
                    animators.add(ObjectAnimator.ofFloat(starBgItem, View.SCALE_X, 1.0f, 0.0f));
                    animators.add(ObjectAnimator.ofFloat(starBgItem, View.SCALE_Y, 1.0f, 0.0f));
                }
                if (animatingItem != null) {
                    animatingItem.setAlpha(1.0f);
                    animators.add(ObjectAnimator.ofFloat(animatingItem, View.ALPHA, 0.0f));
                }
                if (callItemVisible && (chatId != 0 || fromChat)) {
                    callItem.setAlpha(0.0f);
                    animators.add(ObjectAnimator.ofFloat(callItem, View.ALPHA, 1.0f));
                }
                if (videoCallItemVisible) {
                    videoCallItem.setAlpha(0.0f);
                    animators.add(ObjectAnimator.ofFloat(videoCallItem, View.ALPHA, 1.0f));
                }
                if (editItemVisible) {
                    editItem.setAlpha(0.0f);
                    animators.add(ObjectAnimator.ofFloat(editItem, View.ALPHA, 1.0f));
                }
                if (ttlIconView.getTag() != null) {
                    ttlIconView.setAlpha(0f);
                    animators.add(ObjectAnimator.ofFloat(ttlIconView, View.ALPHA, 1.0f));
                }
                if (floatingButtonContainer != null) {
                    floatingButtonContainer.setAlpha(0f);
                    animators.add(ObjectAnimator.ofFloat(floatingButtonContainer, View.ALPHA, 1.0f));
                }
                if (avatarImage != null) {
                    avatarImage.setCrossfadeProgress(1.0f);
                    animators.add(ObjectAnimator.ofFloat(avatarImage, AvatarImageView.CROSSFADE_PROGRESS, 0.0f));
                }

                boolean onlineTextCrosafade = false;

                if (previousTransitionFragment != null) {
                    ChatAvatarContainer avatarContainer = previousTransitionFragment.getAvatarContainer();
                    if (avatarContainer != null && avatarContainer.getSubtitleTextView() instanceof SimpleTextView && ((SimpleTextView) avatarContainer.getSubtitleTextView()).getLeftDrawable() != null || avatarContainer.statusMadeShorter[0]) {
                        transitionOnlineText = avatarContainer.getSubtitleTextView();
                        avatarContainer2.invalidate();
                        onlineTextCrosafade = true;
                        onlineTextView[0].setAlpha(0f);
                        onlineTextView[1].setAlpha(0f);
                        animators.add(ObjectAnimator.ofFloat(onlineTextView[1], View.ALPHA, 1.0f));
                    }
                }

                if (!onlineTextCrosafade) {
                    for (int a = 0; a < 2; a++) {
                        onlineTextView[a].setAlpha(a == 0 ? 1.0f : 0.0f);
                        animators.add(ObjectAnimator.ofFloat(onlineTextView[a], View.ALPHA, a == 0 ? 0.0f : 1.0f));
                    }
                }
                animatorSet.playTogether(animators);
            } else {
                initialAnimationExtraHeight = extraHeight;
                ArrayList<Animator> animators = new ArrayList<>();
                animators.add(ObjectAnimator.ofFloat(this, "avatarAnimationProgress", 1.0f, 0.0f));
                if (writeButton != null) {
                    animators.add(ObjectAnimator.ofFloat(writeButton, View.SCALE_X, 0.2f));
                    animators.add(ObjectAnimator.ofFloat(writeButton, View.SCALE_Y, 0.2f));
                    animators.add(ObjectAnimator.ofFloat(writeButton, View.ALPHA, 0.0f));
                }
                for (int a = 0; a < 2; a++) {
                    animators.add(ObjectAnimator.ofFloat(nameTextView[a], View.ALPHA, a == 0 ? 1.0f : 0.0f));
                }
                if (storyView != null) {
                    if (dialogId > 0) {
                        animators.add(ObjectAnimator.ofFloat(storyView, View.ALPHA, 0.0f));
                    } else {
                        animators.add(ObjectAnimator.ofFloat(storyView, ProfileStoriesView.FRAGMENT_TRANSITION_PROPERTY, 0.0f));
                    }
                }
                if (giftsView != null) {
                    animators.add(ObjectAnimator.ofFloat(giftsView, View.ALPHA, 0.0f));
                }
                if (timeItem.getTag() != null) {
                    timeItem.setAlpha(0f);
                    animators.add(ObjectAnimator.ofFloat(timeItem, View.ALPHA, 0.0f, 1.0f));
                    animators.add(ObjectAnimator.ofFloat(timeItem, View.SCALE_X, 0.0f, 1.0f));
                    animators.add(ObjectAnimator.ofFloat(timeItem, View.SCALE_Y, 0.0f, 1.0f));
                }
                if (starFgItem.getTag() != null) {
                    starFgItem.setAlpha(0f);
                    animators.add(ObjectAnimator.ofFloat(starFgItem, View.ALPHA, 0.0f, 1.0f));
                    animators.add(ObjectAnimator.ofFloat(starFgItem, View.SCALE_X, 0.0f, 1.0f));
                    animators.add(ObjectAnimator.ofFloat(starFgItem, View.SCALE_Y, 0.0f, 1.0f));
                }
                if (starBgItem.getTag() != null) {
                    starBgItem.setAlpha(0f);
                    animators.add(ObjectAnimator.ofFloat(starBgItem, View.ALPHA, 0.0f, 1.0f));
                    animators.add(ObjectAnimator.ofFloat(starBgItem, View.SCALE_X, 0.0f, 1.0f));
                    animators.add(ObjectAnimator.ofFloat(starBgItem, View.SCALE_Y, 0.0f, 1.0f));
                }
                if (animatingItem != null) {
                    animatingItem.setAlpha(0.0f);
                    animators.add(ObjectAnimator.ofFloat(animatingItem, View.ALPHA, 1.0f));
                }
                if (callItemVisible && (chatId != 0 || fromChat)) {
                    callItem.setAlpha(1.0f);
                    animators.add(ObjectAnimator.ofFloat(callItem, View.ALPHA, 0.0f));
                }
                if (videoCallItemVisible) {
                    videoCallItem.setAlpha(1.0f);
                    animators.add(ObjectAnimator.ofFloat(videoCallItem, View.ALPHA, 0.0f));
                }
                if (editItemVisible) {
                    editItem.setAlpha(1.0f);
                    animators.add(ObjectAnimator.ofFloat(editItem, View.ALPHA, 0.0f));
                }
                if (ttlIconView != null) {
                    animators.add(ObjectAnimator.ofFloat(ttlIconView, View.ALPHA, ttlIconView.getAlpha(), 0.0f));
                }
                if (floatingButtonContainer != null) {
                    animators.add(ObjectAnimator.ofFloat(floatingButtonContainer, View.ALPHA, 0.0f));
                }
                if (avatarImage != null) {
                    animators.add(ObjectAnimator.ofFloat(avatarImage, AvatarImageView.CROSSFADE_PROGRESS, 1.0f));
                }

                boolean crossfadeOnlineText = false;
                BaseFragment previousFragment = parentLayout.getFragmentStack().size() > 1 ? parentLayout.getFragmentStack().get(parentLayout.getFragmentStack().size() - 2) : null;
                if (previousFragment instanceof ChatActivity) {
                    ChatAvatarContainer avatarContainer = ((ChatActivity) previousFragment).getAvatarContainer();
                    View subtitleTextView = avatarContainer.getSubtitleTextView();
                    if (subtitleTextView instanceof SimpleTextView && ((SimpleTextView) subtitleTextView).getLeftDrawable() != null || avatarContainer.statusMadeShorter[0]) {
                        transitionOnlineText = avatarContainer.getSubtitleTextView();
                        avatarContainer2.invalidate();
                        crossfadeOnlineText = true;
                        animators.add(ObjectAnimator.ofFloat(onlineTextView[0], View.ALPHA, 0.0f));
                        animators.add(ObjectAnimator.ofFloat(onlineTextView[1], View.ALPHA, 0.0f));
                    }
                }
                if (!crossfadeOnlineText) {
                    for (int a = 0; a < 2; a++) {
                        animators.add(ObjectAnimator.ofFloat(onlineTextView[a], View.ALPHA, a == 0 ? 1.0f : 0.0f));
                    }
                }
                animatorSet.playTogether(animators);
                if (birthdayEffect != null) {
                    birthdayEffect.hide();
                }
            }
            profileTransitionInProgress = true;
            ValueAnimator valueAnimator = ValueAnimator.ofFloat(0, 1f);
            valueAnimator.addUpdateListener(valueAnimator1 -> {
                if (fragmentView != null) {
                    fragmentView.invalidate();
                }
                updateStoriesViewBounds(true);
            });
            animatorSet.playTogether(valueAnimator);

            animatorSet.addListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    if (fragmentView == null) {
                        callback.run();
                        return;
                    }
                    avatarImage.setProgressToExpand(0);
                    listView.setLayerType(View.LAYER_TYPE_NONE, null);
                    if (animatingItem != null) {
                        ActionBarMenu menu = actionBar.createMenu();
                        menu.clearItems();
                        animatingItem = null;
                    }
                    callback.run();
                    if (playProfileAnimation == 2) {
                        playProfileAnimation = 1;
                        avatarImage.setForegroundAlpha(1.0f);
                        avatarContainer.setVisibility(View.GONE);
                        avatarsViewPager.resetCurrentItem();
                        avatarsViewPager.setVisibility(View.VISIBLE);
                    }
                    transitionOnlineText = null;
                    avatarContainer2.invalidate();
                    profileTransitionInProgress = false;
                    previousTransitionFragment = null;
                    previousTransitionMainFragment = null;
                    fragmentView.invalidate();
                }
            });
            animatorSet.setInterpolator(playProfileAnimation == 2 ? CubicBezierInterpolator.DEFAULT : new DecelerateInterpolator());

            AndroidUtilities.runOnUIThread(animatorSet::start, 50);
            return animatorSet;
        }
        return null;
    }

    private int getAverageColor(ImageReceiver imageReceiver) {
        if (imageReceiver.getDrawable() instanceof VectorAvatarThumbDrawable) {
            return ((VectorAvatarThumbDrawable)imageReceiver.getDrawable()).gradientTools.getAverageColor();
        }
        return AndroidUtilities.calcBitmapColor(avatarImage.getImageReceiver().getBitmap());
    }

    private void updateOnlineCount(boolean notify) {
        onlineCount = 0;
        int currentTime = getConnectionsManager().getCurrentTime();
        sortedUsers.clear();
        if (chatInfo instanceof TLRPC.TL_chatFull || chatInfo instanceof TLRPC.TL_channelFull && chatInfo.participants_count <= 200 && chatInfo.participants != null) {
            final ArrayList<Integer> sortNum = new ArrayList<>();
            for (int a = 0; a < chatInfo.participants.participants.size(); a++) {
                TLRPC.ChatParticipant participant = chatInfo.participants.participants.get(a);
                TLRPC.User user = getMessagesController().getUser(participant.user_id);
                if (user != null && user.status != null && (user.status.expires > currentTime || user.id == getUserConfig().getClientUserId()) && user.status.expires > 10000) {
                    onlineCount++;
                }
                sortedUsers.add(a);
                int sort = Integer.MIN_VALUE;
                if (user != null) {
                    if (user.bot) {
                        sort = -110;
                    } else if (user.self) {
                        sort = currentTime + 50000;
                    } else if (user.status != null) {
                        sort = user.status.expires;
                    }
                }
                sortNum.add(sort);
            }

            try {
                Collections.sort(sortedUsers, Comparator.comparingInt(hs -> sortNum.get((int) hs)).reversed());
            } catch (Exception e) {
                FileLog.e(e);
            }

            if (notify && listAdapter != null && membersStartRow > 0) {
                AndroidUtilities.updateVisibleRows(listView);
            }
            if (sharedMediaLayout != null && sharedMediaRow != -1 && (sortedUsers.size() > 5 || usersForceShowingIn == 2) && usersForceShowingIn != 1) {
                sharedMediaLayout.setChatUsers(sortedUsers, chatInfo);
            }
        } else if (chatInfo instanceof TLRPC.TL_channelFull && chatInfo.participants_count > 200) {
            onlineCount = chatInfo.online_count;
        }
    }

    public void setChatInfo(TLRPC.ChatFull value) {
        chatInfo = value;
        if (chatInfo != null && chatInfo.migrated_from_chat_id != 0 && mergeDialogId == 0) {
            mergeDialogId = -chatInfo.migrated_from_chat_id;
            getMediaDataController().getMediaCounts(mergeDialogId, topicId, classGuid);
        }
        if (sharedMediaLayout != null) {
            sharedMediaLayout.setChatInfo(chatInfo);
        }
        if (avatarsViewPager != null && !isTopic) {
            avatarsViewPager.setChatInfo(chatInfo);
        }
        if (storyView != null && chatInfo != null) {
            storyView.setStories(chatInfo.stories);
        }
        if (giftsView != null) {
            giftsView.update();
        }
        if (avatarImage != null) {
            avatarImage.setHasStories(needInsetForStories());
        }
        fetchUsersFromChannelInfo();
        if (chatId != 0) {
            otherItem.setSubItemShown(gift_premium, !BuildVars.IS_BILLING_UNAVAILABLE && !getMessagesController().premiumPurchaseBlocked() && chatInfo != null && chatInfo.stargifts_available);
        }
    }

    private boolean needInsetForStories() {
        return getMessagesController().getStoriesController().hasStories(getDialogId()) && !isTopic;
    }

    public void setUserInfo(
        TLRPC.UserFull value,
        ProfileChannelCell.ChannelMessageFetcher channelMessageFetcher,
        ProfileBirthdayEffect.BirthdayEffectFetcher birthdayAssetsFetcher
    ) {
        userInfo = value;
        if (storyView != null) {
            storyView.setStories(userInfo.stories);
        }
        if (giftsView != null) {
            giftsView.update();
        }
        if (avatarImage != null) {
            avatarImage.setHasStories(needInsetForStories());
        }
        if (sharedMediaLayout != null) {
            sharedMediaLayout.setUserInfo(userInfo);
        }
        if (profileChannelMessageFetcher == null) {
            profileChannelMessageFetcher = channelMessageFetcher;
        }
        if (profileChannelMessageFetcher == null) {
            profileChannelMessageFetcher = new ProfileChannelCell.ChannelMessageFetcher(currentAccount);
        }
        profileChannelMessageFetcher.subscribe(() -> updateListAnimated(false));
        profileChannelMessageFetcher.fetch(userInfo);
        if (birthdayFetcher == null) {
            birthdayFetcher = birthdayAssetsFetcher;
        }
        if (birthdayFetcher == null) {
            birthdayFetcher = ProfileBirthdayEffect.BirthdayEffectFetcher.of(currentAccount, userInfo, birthdayFetcher);
            createdBirthdayFetcher = birthdayFetcher != null;
        }
        if (birthdayFetcher != null) {
            birthdayFetcher.subscribe(this::createBirthdayEffect);
        }
        if (otherItem != null) {
            otherItem.setSubItemShown(start_secret_chat, DialogObject.isEmpty(getMessagesController().isUserContactBlocked(userId)));
            if (hasPrivacyCommand()) {
                otherItem.showSubItem(bot_privacy);
            } else {
                otherItem.hideSubItem(bot_privacy);
            }
        }
    }

    public boolean canSearchMembers() {
        return canSearchMembers;
    }

    private void fetchUsersFromChannelInfo() {
        if (currentChat == null || !currentChat.megagroup) {
            return;
        }
        if (chatInfo instanceof TLRPC.TL_channelFull && chatInfo.participants != null) {
            for (int a = 0; a < chatInfo.participants.participants.size(); a++) {
                TLRPC.ChatParticipant chatParticipant = chatInfo.participants.participants.get(a);
                participantsMap.put(chatParticipant.user_id, chatParticipant);
            }
        }
    }

    private void kickUser(long uid, TLRPC.ChatParticipant participant) {
        if (uid != 0) {
            TLRPC.User user = getMessagesController().getUser(uid);
            getMessagesController().deleteParticipantFromChat(chatId, user);
            if (currentChat != null && user != null && BulletinFactory.canShowBulletin(this)) {
                BulletinFactory.createRemoveFromChatBulletin(this, user, currentChat.title).show();
            }
            if (chatInfo.participants.participants.remove(participant)) {
                updateListAnimated(true);
            }
        } else {
            getNotificationCenter().removeObserver(this, NotificationCenter.closeChats);
            if (AndroidUtilities.isTablet()) {
                getNotificationCenter().postNotificationName(NotificationCenter.closeChats, -chatId);
            } else {
                getNotificationCenter().postNotificationName(NotificationCenter.closeChats);
            }
            getMessagesController().deleteParticipantFromChat(chatId, getMessagesController().getUser(getUserConfig().getClientUserId()));
            playProfileAnimation = 0;
            finishFragment();
        }
    }

    public boolean isChat() {
        return chatId != 0;
    }

    public void updateRowsIds() {
        int prevRowsCount = rowCount;
        rowCount = 0;

        setAvatarRow = -1;
        setAvatarSectionRow = -1;
        numberSectionRow = -1;
        numberRow = -1;
        birthdayRow = -1;
        setUsernameRow = -1;
        bioRow = -1;
        channelRow = -1;
        channelDividerRow = -1;
        phoneSuggestionSectionRow = -1;
        phoneSuggestionRow = -1;
        passwordSuggestionSectionRow = -1;
        graceSuggestionRow = -1;
        graceSuggestionSectionRow = -1;
        passwordSuggestionRow = -1;
        settingsSectionRow = -1;
        settingsSectionRow2 = -1;
        notificationRow = -1;
        languageRow = -1;
        premiumRow = -1;
        starsRow = -1;
        businessRow = -1;
        premiumGiftingRow = -1;
        premiumSectionsRow = -1;
        privacyRow = -1;
        dataRow = -1;
        chatRow = -1;
        filtersRow = -1;
        liteModeRow = -1;
        stickersRow = -1;
        devicesRow = -1;
        devicesSectionRow = -1;
        helpHeaderRow = -1;
        questionRow = -1;
        faqRow = -1;
        policyRow = -1;
        helpSectionCell = -1;
        debugHeaderRow = -1;
        sendLogsRow = -1;
        sendLastLogsRow = -1;
        clearLogsRow = -1;
        switchBackendRow = -1;
        versionRow = -1;
        botAppRow = -1;
        botPermissionsHeader = -1;
        botPermissionBiometry = -1;
        botPermissionEmojiStatus = -1;
        botPermissionLocation = -1;
        botPermissionsDivider = -1;

        sendMessageRow = -1;
        reportRow = -1;
        reportReactionRow = -1;
        addToContactsRow = -1;
        emptyRow = -1;
        infoHeaderRow = -1;
        phoneRow = -1;
        userInfoRow = -1;
        locationRow = -1;
        channelInfoRow = -1;
        usernameRow = -1;
        settingsTimerRow = -1;
        settingsKeyRow = -1;
        notificationsDividerRow = -1;
        reportDividerRow = -1;
        notificationsRow = -1;
        bizLocationRow = -1;
        bizHoursRow = -1;
        infoSectionRow = -1;
        affiliateRow = -1;
        infoAffiliateRow = -1;
        secretSettingsSectionRow = -1;
        bottomPaddingRow = -1;
        addToGroupButtonRow = -1;
        addToGroupInfoRow = -1;
        infoStartRow = -1;
        infoEndRow = -1;

        membersHeaderRow = -1;
        membersStartRow = -1;
        membersEndRow = -1;
        addMemberRow = -1;
        subscribersRow = -1;
        subscribersRequestsRow = -1;
        administratorsRow = -1;
        blockedUsersRow = -1;
        membersSectionRow = -1;
        channelBalanceSectionRow = -1;
        sharedMediaRow = -1;
        notificationsSimpleRow = -1;
        settingsRow = -1;
        botStarsBalanceRow = -1;
        botTonBalanceRow = -1;
        channelBalanceRow = -1;
        balanceDividerRow = -1;

        unblockRow = -1;
        joinRow = -1;
        lastSectionRow = -1;
        visibleChatParticipants.clear();
        visibleSortedUsers.clear();

        boolean hasMedia = false;
        if (sharedMediaPreloader != null) {
            int[] lastMediaCount = sharedMediaPreloader.getLastMediaCount();
            for (int a = 0; a < lastMediaCount.length; a++) {
                if (lastMediaCount[a] > 0) {
                    hasMedia = true;
                    break;
                }
            }
            if (!hasMedia) {
                hasMedia = sharedMediaPreloader.hasSavedMessages;
            }
            if (!hasMedia) {
                hasMedia = sharedMediaPreloader.hasPreviews;
            }
        }
        if (!hasMedia && userInfo != null) {
            hasMedia = userInfo.stories_pinned_available;
        }
        if (!hasMedia && userInfo != null && userInfo.bot_info != null) {
            hasMedia = userInfo.bot_info.has_preview_medias;
        }
        if (!hasMedia && (userInfo != null && userInfo.stargifts_count > 0 || chatInfo != null && chatInfo.stargifts_count > 0)) {
            hasMedia = true;
        }
        if (!hasMedia && chatInfo != null) {
            hasMedia = chatInfo.stories_pinned_available;
        }
        if (!hasMedia) {
            if (chatId != 0 && MessagesController.ChannelRecommendations.hasRecommendations(currentAccount, -chatId)) {
                hasMedia = true;
            } else if (isBot && userId != 0 && MessagesController.ChannelRecommendations.hasRecommendations(currentAccount, userId)) {
                hasMedia = true;
            }
        }

        if (userId != 0) {
            if (LocaleController.isRTL) {
                emptyRow = rowCount++;
            }
            TLRPC.User user = getMessagesController().getUser(userId);

            if (UserObject.isUserSelf(user) && !myProfile) {
                if (avatarBig == null && (user.photo == null || !(user.photo.photo_big instanceof TLRPC.TL_fileLocation_layer97) && !(user.photo.photo_big instanceof TLRPC.TL_fileLocationToBeDeprecated)) && (avatarsViewPager == null || avatarsViewPager.getRealCount() == 0)) {
                    setAvatarRow = rowCount++;
                    setAvatarSectionRow = rowCount++;
                }
                numberSectionRow = rowCount++;
                numberRow = rowCount++;
                setUsernameRow = rowCount++;
                bioRow = rowCount++;

                settingsSectionRow = rowCount++;

                Set<String> suggestions = getMessagesController().pendingSuggestions;
                if (suggestions.contains("PREMIUM_GRACE")) {
                    graceSuggestionRow = rowCount++;
                    graceSuggestionSectionRow = rowCount++;
                } else if (suggestions.contains("VALIDATE_PHONE_NUMBER")) {
                    phoneSuggestionRow = rowCount++;
                    phoneSuggestionSectionRow = rowCount++;
                } else if (suggestions.contains("VALIDATE_PASSWORD")) {
                    passwordSuggestionRow = rowCount++;
                    passwordSuggestionSectionRow = rowCount++;
                }

                settingsSectionRow2 = rowCount++;
                chatRow = rowCount++;
                privacyRow = rowCount++;
                notificationRow = rowCount++;
                dataRow = rowCount++;
                liteModeRow = rowCount++;
//                stickersRow = rowCount++;
                if (getMessagesController().filtersEnabled || !getMessagesController().dialogFilters.isEmpty()) {
                    filtersRow = rowCount++;
                }
                devicesRow = rowCount++;
                languageRow = rowCount++;
                devicesSectionRow = rowCount++;
                if (!getMessagesController().premiumFeaturesBlocked()) {
                    premiumRow = rowCount++;
                }
                if (getMessagesController().starsPurchaseAvailable()) {
                    starsRow = rowCount++;
                }
                if (!getMessagesController().premiumFeaturesBlocked()) {
                    businessRow = rowCount++;
                }
                if (!getMessagesController().premiumPurchaseBlocked()) {
                    premiumGiftingRow = rowCount++;
                }
                if (premiumRow >= 0 || starsRow >= 0 || businessRow >= 0 || premiumGiftingRow >= 0) {
                    premiumSectionsRow = rowCount++;
                }
                helpHeaderRow = rowCount++;
                questionRow = rowCount++;
                faqRow = rowCount++;
                policyRow = rowCount++;
                if (BuildVars.LOGS_ENABLED || BuildVars.DEBUG_PRIVATE_VERSION) {
                    helpSectionCell = rowCount++;
                    debugHeaderRow = rowCount++;
                }
                if (BuildVars.LOGS_ENABLED) {
                    sendLogsRow = rowCount++;
                    sendLastLogsRow = rowCount++;
                    clearLogsRow = rowCount++;
                }
                if (BuildVars.DEBUG_VERSION) {
                    switchBackendRow = rowCount++;
                }
                versionRow = rowCount++;
            } else {
                String username = UserObject.getPublicUsername(user);
                boolean hasInfo = userInfo != null && !TextUtils.isEmpty(userInfo.about) || user != null && !TextUtils.isEmpty(username);
                boolean hasPhone = user != null && (!TextUtils.isEmpty(user.phone) || !TextUtils.isEmpty(vcardPhone));

                if (userInfo != null && (userInfo.flags2 & 64) != 0 && (profileChannelMessageFetcher == null || !profileChannelMessageFetcher.loaded || profileChannelMessageFetcher.messageObject != null)) {
                    final TLRPC.Chat channel = getMessagesController().getChat(userInfo.personal_channel_id);
                    if (channel != null && (ChatObject.isPublic(channel) || !ChatObject.isNotInChat(channel))) {
                        channelRow = rowCount++;
                        channelDividerRow = rowCount++;
                    }
                }
                infoStartRow = rowCount;
                infoHeaderRow = rowCount++;
                if (!isBot && (hasPhone || !hasInfo)) {
                    phoneRow = rowCount++;
                }
                if (userInfo != null && !TextUtils.isEmpty(userInfo.about)) {
                    userInfoRow = rowCount++;
                }
                if (user != null && username != null) {
                    usernameRow = rowCount++;
                }
                if (userInfo != null) {
                    if (userInfo.birthday != null) {
                        birthdayRow = rowCount++;
                    }
                    if (userInfo.business_work_hours != null) {
                        bizHoursRow = rowCount++;
                    }
                    if (userInfo.business_location != null) {
                        bizLocationRow = rowCount++;
                    }
                }
//                if (phoneRow != -1 || userInfoRow != -1 || usernameRow != -1 || bizHoursRow != -1 || bizLocationRow != -1) {
//                    notificationsDividerRow = rowCount++;
//                }
                if (userId != getUserConfig().getClientUserId()) {
                    notificationsRow = rowCount++;
                }
                if (isBot && user != null && user.bot_has_main_app) {
                    botAppRow = rowCount++;
                }
                infoEndRow = rowCount - 1;
                infoSectionRow = rowCount++;

                if (isBot && userInfo != null && userInfo.starref_program != null && (userInfo.starref_program.flags & 2) == 0 && getMessagesController().starrefConnectAllowed) {
                    affiliateRow = rowCount++;
                    infoAffiliateRow = rowCount++;
                }

                if (isBot) {
                    if (botLocation == null && getContext() != null) botLocation = BotLocation.get(getContext(), currentAccount, userId);
                    if (botBiometry == null && getContext() != null) botBiometry = BotBiometry.get(getContext(), currentAccount, userId);
                    final boolean containsPermissionLocation = botLocation != null && botLocation.asked();
                    final boolean containsPermissionBiometry = botBiometry != null && botBiometry.asked();
                    final boolean containsPermissionEmojiStatus = userInfo != null && userInfo.bot_can_manage_emoji_status || SetupEmojiStatusSheet.getAccessRequested(getContext(), currentAccount, userId);

                    if (containsPermissionEmojiStatus || containsPermissionLocation || containsPermissionBiometry) {
                        botPermissionsHeader = rowCount++;
                        if (containsPermissionEmojiStatus) {
                            botPermissionEmojiStatus = rowCount++;
                        }
                        if (containsPermissionLocation) {
                            botPermissionLocation = rowCount++;
                        }
                        if (containsPermissionBiometry) {
                            botPermissionBiometry = rowCount++;
                        }
                        botPermissionsDivider = rowCount++;
                    }
                }

                if (currentEncryptedChat instanceof TLRPC.TL_encryptedChat) {
                    settingsTimerRow = rowCount++;
                    settingsKeyRow = rowCount++;
                    secretSettingsSectionRow = rowCount++;
                }

                if (user != null && !isBot && currentEncryptedChat == null && user.id != getUserConfig().getClientUserId()) {
                    if (userBlocked) {
                        unblockRow = rowCount++;
                        lastSectionRow = rowCount++;
                    }
                }


                boolean divider = false;
                if (user != null && user.bot) {
                    if (userInfo != null && userInfo.can_view_revenue && BotStarsController.getInstance(currentAccount).getTONBalance(userId) > 0) {
                        botTonBalanceRow = rowCount++;
                    }
                    if (BotStarsController.getInstance(currentAccount).getBotStarsBalance(userId).amount > 0 || BotStarsController.getInstance(currentAccount).hasTransactions(userId)) {
                        botStarsBalanceRow = rowCount++;
                    }
                }

                if (user != null && isBot && !user.bot_nochats) {
                    addToGroupButtonRow = rowCount++;
                    addToGroupInfoRow = rowCount++;
                } else if (botStarsBalanceRow >= 0) {
                    divider = true;
                }

                if (!myProfile && showAddToContacts && user != null && !user.contact && !user.bot && !UserObject.isService(user.id)) {
                    addToContactsRow = rowCount++;
                    divider = true;
                }
                if (!myProfile && reportReactionMessageId != 0 && !ContactsController.getInstance(currentAccount).isContact(userId)) {
                    reportReactionRow = rowCount++;
                    divider = true;
                }
                if (divider) {
                    reportDividerRow = rowCount++;
                }

                if (hasMedia || (user != null && user.bot && user.bot_can_edit) || userInfo != null && userInfo.common_chats_count != 0 || myProfile) {
                    sharedMediaRow = rowCount++;
                } else if (lastSectionRow == -1 && needSendMessage) {
                    sendMessageRow = rowCount++;
                    reportRow = rowCount++;
                    lastSectionRow = rowCount++;
                }
            }
        } else if (isTopic) {
            infoHeaderRow = rowCount++;
            usernameRow = rowCount++;
            notificationsSimpleRow = rowCount++;
            infoSectionRow = rowCount++;
            if (hasMedia) {
                sharedMediaRow = rowCount++;
            }
        } else if (chatId != 0) {
            if (chatInfo != null && (!TextUtils.isEmpty(chatInfo.about) || chatInfo.location instanceof TLRPC.TL_channelLocation) || ChatObject.isPublic(currentChat)) {
                if (LocaleController.isRTL && ChatObject.isChannel(currentChat) && chatInfo != null && !currentChat.megagroup && chatInfo.linked_chat_id != 0) {
                    emptyRow = rowCount++;
                }
                infoHeaderRow = rowCount++;
                if (chatInfo != null) {
                    if (!TextUtils.isEmpty(chatInfo.about)) {
                        channelInfoRow = rowCount++;
                    }
                    if (chatInfo.location instanceof TLRPC.TL_channelLocation) {
                        locationRow = rowCount++;
                    }
                }
                if (ChatObject.isPublic(currentChat)) {
                    usernameRow = rowCount++;
                }
            }
//            if (infoHeaderRow != -1) {
//                notificationsDividerRow = rowCount++;
//            }
            notificationsRow = rowCount++;
            infoSectionRow = rowCount++;

            if (ChatObject.isChannel(currentChat) && !currentChat.megagroup) {
                if (chatInfo != null && (currentChat.creator || chatInfo.can_view_participants)) {
                    membersHeaderRow = rowCount++;
                    subscribersRow = rowCount++;
                    if (chatInfo != null && chatInfo.requests_pending > 0) {
                        subscribersRequestsRow = rowCount++;
                    }
                    administratorsRow = rowCount++;
                    if (chatInfo != null && (chatInfo.banned_count != 0 || chatInfo.kicked_count != 0)) {
                        blockedUsersRow = rowCount++;
                    }
                    if (
                        chatInfo != null &&
                        chatInfo.can_view_stars_revenue && (
                            BotStarsController.getInstance(currentAccount).getBotStarsBalance(-chatId).amount > 0 ||
                            BotStarsController.getInstance(currentAccount).hasTransactions(-chatId)
                        ) ||
                        chatInfo != null &&
                        chatInfo.can_view_revenue &&
                        BotStarsController.getInstance(currentAccount).getTONBalance(-chatId) > 0
                    ) {
                        channelBalanceRow = rowCount++;
                    }
                    settingsRow = rowCount++;
                    channelBalanceSectionRow = rowCount++;
                }
            } else {
                if (
                    chatInfo != null &&
                        chatInfo.can_view_stars_revenue && (
                        BotStarsController.getInstance(currentAccount).getBotStarsBalance(-chatId).amount > 0 ||
                        BotStarsController.getInstance(currentAccount).hasTransactions(-chatId)
                    ) ||
                    chatInfo != null &&
                    chatInfo.can_view_revenue &&
                    BotStarsController.getInstance(currentAccount).getTONBalance(-chatId) > 0
                ) {
                    channelBalanceRow = rowCount++;
                    channelBalanceSectionRow = rowCount++;
                }
            }

            if (ChatObject.isChannel(currentChat)) {
                if (!isTopic && chatInfo != null && currentChat.megagroup && chatInfo.participants != null && chatInfo.participants.participants != null && !chatInfo.participants.participants.isEmpty()) {
                    if (!ChatObject.isNotInChat(currentChat) && ChatObject.canAddUsers(currentChat) && chatInfo.participants_count < getMessagesController().maxMegagroupCount) {
                        addMemberRow = rowCount++;
                    }
                    int count = chatInfo.participants.participants.size();
                    if ((count <= 5 || !hasMedia || usersForceShowingIn == 1) && usersForceShowingIn != 2) {
                        if (addMemberRow == -1) {
                            membersHeaderRow = rowCount++;
                        }
                        membersStartRow = rowCount;
                        rowCount += count;
                        membersEndRow = rowCount;
                        membersSectionRow = rowCount++;
                        visibleChatParticipants.addAll(chatInfo.participants.participants);
                        if (sortedUsers != null) {
                            visibleSortedUsers.addAll(sortedUsers);
                        }
                        usersForceShowingIn = 1;
                        if (sharedMediaLayout != null) {
                            sharedMediaLayout.setChatUsers(null, null);
                        }
                    } else {
                        if (addMemberRow != -1) {
                            membersSectionRow = rowCount++;
                        }
                        if (sharedMediaLayout != null) {
                            if (!sortedUsers.isEmpty()) {
                                usersForceShowingIn = 2;
                            }
                            sharedMediaLayout.setChatUsers(sortedUsers, chatInfo);
                        }
                    }
                } else {
                    if (!ChatObject.isNotInChat(currentChat) && ChatObject.canAddUsers(currentChat) && chatInfo != null && chatInfo.participants_hidden) {
                        addMemberRow = rowCount++;
                        membersSectionRow = rowCount++;
                    }
                    if (sharedMediaLayout != null) {
                        sharedMediaLayout.updateAdapters();
                    }
                }

                if (lastSectionRow == -1 && currentChat.left && !currentChat.kicked) {
                    long requestedTime = MessagesController.getNotificationsSettings(currentAccount).getLong("dialog_join_requested_time_" + dialogId, -1);
                    if (!(requestedTime > 0 && System.currentTimeMillis() - requestedTime < 1000 * 60 * 2)) {
                        joinRow = rowCount++;
                        lastSectionRow = rowCount++;
                    }
                }
            } else if (chatInfo != null) {
                if (!isTopic && chatInfo.participants != null && chatInfo.participants.participants != null && !(chatInfo.participants instanceof TLRPC.TL_chatParticipantsForbidden)) {
                    if (ChatObject.canAddUsers(currentChat) || currentChat.default_banned_rights == null || !currentChat.default_banned_rights.invite_users) {
                        addMemberRow = rowCount++;
                    }
                    int count = chatInfo.participants.participants.size();
                    if (count <= 5 || !hasMedia) {
                        if (addMemberRow == -1) {
                            membersHeaderRow = rowCount++;
                        }
                        membersStartRow = rowCount;
                        rowCount += chatInfo.participants.participants.size();
                        membersEndRow = rowCount;
                        membersSectionRow = rowCount++;
                        visibleChatParticipants.addAll(chatInfo.participants.participants);
                        if (sortedUsers != null) {
                            visibleSortedUsers.addAll(sortedUsers);
                        }
                        if (sharedMediaLayout != null) {
                            sharedMediaLayout.setChatUsers(null, null);
                        }
                    } else {
                        if (addMemberRow != -1) {
                            membersSectionRow = rowCount++;
                        }
                        if (sharedMediaLayout != null) {
                            sharedMediaLayout.setChatUsers(sortedUsers, chatInfo);
                        }
                    }
                } else {
                    if (!ChatObject.isNotInChat(currentChat) && ChatObject.canAddUsers(currentChat) && chatInfo.participants_hidden) {
                        addMemberRow = rowCount++;
                        membersSectionRow = rowCount++;
                    }
                    if (sharedMediaLayout != null) {
                        sharedMediaLayout.updateAdapters();
                    }
                }
            }

            if (hasMedia) {
                sharedMediaRow = rowCount++;
            }
        }
        if (sharedMediaRow == -1) {
            bottomPaddingRow = rowCount++;
        }
        final int actionBarHeight = actionBar != null ? ActionBar.getCurrentActionBarHeight() + (actionBar.getOccupyStatusBar() ? AndroidUtilities.statusBarHeight : 0) : 0;
        if (listView == null || prevRowsCount > rowCount || listContentHeight != 0 && listContentHeight + actionBarHeight + AndroidUtilities.dp(88) < listView.getMeasuredHeight()) {
            lastMeasuredContentWidth = 0;
        }
        if (listView != null) {
            listView.setTranslateSelectorPosition(bizHoursRow);
        }
    }

    private Drawable getScamDrawable(int type) {
        if (scamDrawable == null) {
            scamDrawable = new ScamDrawable(11, type);
            scamDrawable.setColor(getThemedColor(Theme.key_avatar_subtitleInProfileBlue));
        }
        return scamDrawable;
    }

    private Drawable getLockIconDrawable() {
        if (lockIconDrawable == null) {
            lockIconDrawable = Theme.chat_lockIconDrawable.getConstantState().newDrawable().mutate();
        }
        return lockIconDrawable;
    }

    private Drawable getVerifiedCrossfadeDrawable(int a) {
        if (verifiedCrossfadeDrawable[a] == null) {
            verifiedDrawable[a] = Theme.profile_verifiedDrawable.getConstantState().newDrawable().mutate();
            verifiedCheckDrawable[a] = Theme.profile_verifiedCheckDrawable.getConstantState().newDrawable().mutate();
            if (a == 1 && peerColor != null) {
                int color = Theme.adaptHSV(peerColor.hasColor6(Theme.isCurrentThemeDark()) ? peerColor.getColor5() : peerColor.getColor3(), +.1f, Theme.isCurrentThemeDark() ? -.1f : -.08f);
                verifiedDrawable[1].setColorFilter(AndroidUtilities.getOffsetColor(color, getThemedColor(Theme.key_player_actionBarTitle), mediaHeaderAnimationProgress, 1.0f), PorterDuff.Mode.MULTIPLY);
                color = Color.WHITE;
                verifiedCheckDrawable[1].setColorFilter(AndroidUtilities.getOffsetColor(color, getThemedColor(Theme.key_windowBackgroundWhite), mediaHeaderAnimationProgress, 1.0f), PorterDuff.Mode.MULTIPLY);
            }
            verifiedCrossfadeDrawable[a] = new CrossfadeDrawable(
                new CombinedDrawable(verifiedDrawable[a], verifiedCheckDrawable[a]),
                ContextCompat.getDrawable(getParentActivity(), R.drawable.verified_profile)
            );
        }
        return verifiedCrossfadeDrawable[a];
    }

    private Drawable getPremiumCrossfadeDrawable(int a) {
        if (premiumCrossfadeDrawable[a] == null) {
            premiumStarDrawable[a] = ContextCompat.getDrawable(getParentActivity(), R.drawable.msg_premium_liststar).mutate();
            int color = getThemedColor(Theme.key_profile_verifiedBackground);
            if (a == 1) {
                color = dontApplyPeerColor(color);
            }
            premiumStarDrawable[a].setColorFilter(color, PorterDuff.Mode.MULTIPLY);
            premiumCrossfadeDrawable[a] = new CrossfadeDrawable(premiumStarDrawable[a], ContextCompat.getDrawable(getParentActivity(), R.drawable.msg_premium_prolfilestar).mutate());
        }
        return premiumCrossfadeDrawable[a];
    }
    private Drawable getBotVerificationDrawable(long icon, boolean animated, int a) {
        if (botVerificationDrawable[a] == null) {
            botVerificationDrawable[a] = new AnimatedEmojiDrawable.SwapAnimatedEmojiDrawable(nameTextView[a], AndroidUtilities.dp(17), a == 0 ? AnimatedEmojiDrawable.CACHE_TYPE_EMOJI_STATUS : AnimatedEmojiDrawable.CACHE_TYPE_KEYBOARD);
            botVerificationDrawable[a].offset(0, dp(1));
            if (fragmentViewAttached) {
                botVerificationDrawable[a].attach();
            }
        }
        if (icon != 0) {
            botVerificationDrawable[a].set(icon, animated);
        } else {
            botVerificationDrawable[a].set((Drawable) null, animated);
        }
        updateEmojiStatusDrawableColor();
        return botVerificationDrawable[a];
    }

    private Drawable getEmojiStatusDrawable(TLRPC.EmojiStatus emojiStatus, boolean switchable, boolean animated, int a) {
        if (emojiStatusDrawable[a] == null) {
            emojiStatusDrawable[a] = new AnimatedEmojiDrawable.SwapAnimatedEmojiDrawable(nameTextView[a], AndroidUtilities.dp(24), a == 0 ? AnimatedEmojiDrawable.CACHE_TYPE_EMOJI_STATUS : AnimatedEmojiDrawable.CACHE_TYPE_KEYBOARD);
            if (fragmentViewAttached) {
                emojiStatusDrawable[a].attach();
            }
        }
        if (a == 1) {
            emojiStatusGiftId = null;
        }
        if (emojiStatus instanceof TLRPC.TL_emojiStatus) {
            final TLRPC.TL_emojiStatus status = (TLRPC.TL_emojiStatus) emojiStatus;
            if ((status.flags & 1) == 0 || status.until > (int) (System.currentTimeMillis() / 1000)) {
                emojiStatusDrawable[a].set(status.document_id, animated);
                emojiStatusDrawable[a].setParticles(false, animated);
            } else {
                emojiStatusDrawable[a].set(getPremiumCrossfadeDrawable(a), animated);
                emojiStatusDrawable[a].setParticles(false, animated);
            }
        } else if (emojiStatus instanceof TLRPC.TL_emojiStatusCollectible) {
            final TLRPC.TL_emojiStatusCollectible status = (TLRPC.TL_emojiStatusCollectible) emojiStatus;
            if ((status.flags & 1) == 0 || status.until > (int) (System.currentTimeMillis() / 1000)) {
                if (a == 1) {
                    emojiStatusGiftId = status.collectible_id;
                }
                emojiStatusDrawable[a].set(status.document_id, animated);
                emojiStatusDrawable[a].setParticles(true, animated);
            } else {
                emojiStatusDrawable[a].set(getPremiumCrossfadeDrawable(a), animated);
                emojiStatusDrawable[a].setParticles(false, animated);
            }
        } else {
            emojiStatusDrawable[a].set(getPremiumCrossfadeDrawable(a), animated);
            emojiStatusDrawable[a].setParticles(false, animated);
        }
        updateEmojiStatusDrawableColor();
        return emojiStatusDrawable[a];
    }

    private float lastEmojiStatusProgress;
    private void updateEmojiStatusDrawableColor() {
        updateEmojiStatusDrawableColor(lastEmojiStatusProgress);
    }
    private void updateEmojiStatusDrawableColor(float progress) {
        for (int a = 0; a < 2; ++a) {
            final int fromColor;
            if (peerColor != null && a == 1) {
                fromColor = ColorUtils.blendARGB(peerColor.getStoryColor1(Theme.isCurrentThemeDark()), 0xFFFFFFFF, 0.25f);
            } else {
                fromColor = AndroidUtilities.getOffsetColor(getThemedColor(Theme.key_profile_verifiedBackground), getThemedColor(Theme.key_player_actionBarTitle), mediaHeaderAnimationProgress, 1.0f);
            }
            final int color = ColorUtils.blendARGB(ColorUtils.blendARGB(fromColor, 0xffffffff, progress), getThemedColor(Theme.key_player_actionBarTitle), mediaHeaderAnimationProgress);
            if (emojiStatusDrawable[a] != null) {
                emojiStatusDrawable[a].setColor(color);
            }
            if (botVerificationDrawable[a] != null) {
                botVerificationDrawable[a].setColor(ColorUtils.blendARGB(ColorUtils.blendARGB(fromColor, 0x99ffffff, progress), getThemedColor(Theme.key_player_actionBarTitle), mediaHeaderAnimationProgress));
            }
            if (a == 1) {
                animatedStatusView.setColor(color);
            }
        }
        lastEmojiStatusProgress = progress;
    }

    private void updateEmojiStatusEffectPosition() {
        animatedStatusView.setScaleX(nameTextView[1].getScaleX());
        animatedStatusView.setScaleY(nameTextView[1].getScaleY());
        animatedStatusView.translate(
            nameTextView[1].getX() + nameTextView[1].getRightDrawableX() * nameTextView[1].getScaleX(),
            nameTextView[1].getY() + (nameTextView[1].getHeight() - (nameTextView[1].getHeight() - nameTextView[1].getRightDrawableY()) * nameTextView[1].getScaleY())
        );
    }

    private MessagesController.PeerColor peerColor;

    public void updateProfileData(boolean reload) {
        if (avatarContainer == null || nameTextView == null || getParentActivity() == null) {
            return;
        }
        String onlineTextOverride;
        int currentConnectionState = getConnectionsManager().getConnectionState();
        if (currentConnectionState == ConnectionsManager.ConnectionStateWaitingForNetwork) {
            onlineTextOverride = LocaleController.getString(R.string.WaitingForNetwork);
        } else if (currentConnectionState == ConnectionsManager.ConnectionStateConnecting) {
            onlineTextOverride = LocaleController.getString(R.string.Connecting);
        } else if (currentConnectionState == ConnectionsManager.ConnectionStateUpdating) {
            onlineTextOverride = LocaleController.getString(R.string.Updating);
        } else if (currentConnectionState == ConnectionsManager.ConnectionStateConnectingToProxy) {
            onlineTextOverride = LocaleController.getString(R.string.ConnectingToProxy);
        } else {
            onlineTextOverride = null;
        }

        BaseFragment prevFragment = null;
        if (parentLayout != null && parentLayout.getFragmentStack().size() >= 2) {
            BaseFragment fragment = parentLayout.getFragmentStack().get(parentLayout.getFragmentStack().size() - 2);
            if (fragment instanceof ChatActivityInterface) {
                prevFragment = fragment;
            }
            if (fragment instanceof DialogsActivity) {
                DialogsActivity dialogsActivity = (DialogsActivity) fragment;
                if (dialogsActivity.rightSlidingDialogContainer != null && dialogsActivity.rightSlidingDialogContainer.currentFragment instanceof ChatActivityInterface) {
                    previousTransitionMainFragment = dialogsActivity;
                    prevFragment = dialogsActivity.rightSlidingDialogContainer.currentFragment;
                }
            }
        }
        final boolean copyFromChatActivity = prevFragment instanceof ChatActivity && ((ChatActivity) prevFragment).avatarContainer != null && ((ChatActivity) prevFragment).getChatMode() == ChatActivity.MODE_SUGGESTIONS;

        TLRPC.TL_forumTopic topic = null;
        boolean shortStatus;

        hasFallbackPhoto = false;
        hasCustomPhoto = false;
        if (userId != 0) {
            TLRPC.User user = getMessagesController().getUser(userId);
            if (user == null) {
                return;
            }
            shortStatus = user.photo != null && user.photo.personal;
            TLRPC.FileLocation photoBig = null;
            if (user.photo != null) {
                photoBig = user.photo.photo_big;
            }
            avatarDrawable.setInfo(currentAccount, user);

            final MessagesController.PeerColor wasPeerColor = peerColor;
            peerColor = MessagesController.PeerColor.fromCollectible(user.emoji_status);
            if (peerColor == null) {
                final int colorId = UserObject.getProfileColorId(user);
                final MessagesController.PeerColors peerColors = MessagesController.getInstance(currentAccount).profilePeerColors;
                peerColor = peerColors == null ? null : peerColors.getColor(colorId);
            }
            if (wasPeerColor != peerColor) {
                updatedPeerColor();
            }
            if (topView != null) {
                topView.setBackgroundEmojiId(UserObject.getProfileEmojiId(user), user != null && user.emoji_status instanceof TLRPC.TL_emojiStatusCollectible, true);
            }
            setCollectibleGiftStatus(user.emoji_status instanceof TLRPC.TL_emojiStatusCollectible ? (TLRPC.TL_emojiStatusCollectible) user.emoji_status : null);

            final ImageLocation imageLocation = ImageLocation.getForUserOrChat(user, ImageLocation.TYPE_BIG);
            final ImageLocation thumbLocation = ImageLocation.getForUserOrChat(user, ImageLocation.TYPE_SMALL);
            final ImageLocation videoThumbLocation = ImageLocation.getForUserOrChat(user, ImageLocation.TYPE_VIDEO_BIG);
            VectorAvatarThumbDrawable vectorAvatarThumbDrawable = null;
            TLRPC.VideoSize vectorAvatar = null;
            if (userInfo != null) {
                vectorAvatar = FileLoader.getVectorMarkupVideoSize(user.photo != null && user.photo.personal ? userInfo.personal_photo : userInfo.profile_photo);
                if (vectorAvatar != null) {
                    vectorAvatarThumbDrawable = new VectorAvatarThumbDrawable(vectorAvatar, user.premium, VectorAvatarThumbDrawable.TYPE_PROFILE);
                }
            }
            final ImageLocation videoLocation = avatarsViewPager.getCurrentVideoLocation(thumbLocation, imageLocation);
            if (avatar == null) {
                avatarsViewPager.initIfEmpty(vectorAvatarThumbDrawable, imageLocation, thumbLocation, reload);
            }
            if (avatarBig == null) {
                if (vectorAvatar != null) {
                    avatarImage.setImageDrawable(vectorAvatarThumbDrawable);
                } else if (videoThumbLocation != null && !user.photo.personal) {
                    avatarImage.getImageReceiver().setVideoThumbIsSame(true);
                    avatarImage.setImage(videoThumbLocation, "avatar", thumbLocation, "50_50", avatarDrawable, user);
                } else {
                    avatarImage.setImage(videoLocation, ImageLoader.AUTOPLAY_FILTER, thumbLocation, "50_50", avatarDrawable, user);
                }
            }

            if (thumbLocation != null && setAvatarRow != -1 || thumbLocation == null && setAvatarRow == -1) {
                updateListAnimated(false);
                needLayout(true);
            }
            if (imageLocation != null && (prevLoadedImageLocation == null || imageLocation.photoId != prevLoadedImageLocation.photoId)) {
                prevLoadedImageLocation = imageLocation;
                getFileLoader().loadFile(imageLocation, user, null, FileLoader.PRIORITY_LOW, 1);
            }

            CharSequence newString = UserObject.getUserName(user);
            String newString2;
            boolean hiddenStatusButton = false;
            if (user.id == getUserConfig().getClientUserId()) {
                if (UserObject.hasFallbackPhoto(getUserInfo())) {
                    newString2 = "";
                    hasFallbackPhoto = true;
                    TLRPC.PhotoSize smallSize = FileLoader.getClosestPhotoSizeWithSize(getUserInfo().fallback_photo.sizes, 1000);
                    if (smallSize != null) {
                        fallbackImage.setImage(ImageLocation.getForPhoto(smallSize, getUserInfo().fallback_photo), "50_50", (Drawable) null, 0, null, UserConfig.getInstance(currentAccount).getCurrentUser(), 0);
                    }
                } else {
                    newString2 = LocaleController.getString(R.string.Online);
                }
            } else if (user.id == UserObject.VERIFY) {
                newString2 = LocaleController.getString(R.string.VerifyCodesNotifications);
            } else if (user.id == 333000 || user.id == 777000 || user.id == 42777) {
                newString2 = LocaleController.getString(R.string.ServiceNotifications);
            } else if (MessagesController.isSupportUser(user)) {
                newString2 = LocaleController.getString(R.string.SupportStatus);
            } else if (isBot) {
                if (user.bot_active_users != 0) {
                    newString2 = LocaleController.formatPluralStringComma("BotUsers", user.bot_active_users, ',');
                } else {
                    newString2 = LocaleController.getString(R.string.Bot);
                }
            } else {
                isOnline[0] = false;
                newString2 = LocaleController.formatUserStatus(currentAccount, user, isOnline, shortStatus ? new boolean[1] : null);
                hiddenStatusButton = user != null && !isOnline[0] && !getUserConfig().isPremium() && user.status != null && (user.status instanceof TLRPC.TL_userStatusRecently || user.status instanceof TLRPC.TL_userStatusLastMonth || user.status instanceof TLRPC.TL_userStatusLastWeek) && user.status.by_me;
                if (onlineTextView[1] != null && !mediaHeaderVisible) {
                    int key = isOnline[0] && peerColor == null ? Theme.key_profile_status : Theme.key_avatar_subtitleInProfileBlue;
                    onlineTextView[1].setTag(key);
                    if (!isPulledDown) {
                        onlineTextView[1].setTextColor(applyPeerColor(getThemedColor(key), true, isOnline[0]));
                    }
                }
            }
            hasCustomPhoto = user.photo != null && user.photo.personal;
            try {
                newString = Emoji.replaceEmoji(newString, nameTextView[1].getPaint().getFontMetricsInt(), false);
            } catch (Exception ignore) {
            }
            if (copyFromChatActivity) {
                ChatActivity chatActivity = (ChatActivity) prevFragment;
                BackupImageView fromAvatarImage = chatActivity.avatarContainer.getAvatarImageView();
                avatarImage.setAnimateFromImageReceiver(fromAvatarImage.getImageReceiver());
            }
            for (int a = 0; a < 2; a++) {
                if (nameTextView[a] == null) {
                    continue;
                }
                if (a == 0 && copyFromChatActivity) {
                    ChatActivity chatActivity = (ChatActivity) prevFragment;
                    SimpleTextView titleTextView = chatActivity.avatarContainer.getTitleTextView();
                    nameTextView[a].setText(titleTextView.getText());
                    nameTextView[a].setRightDrawable(titleTextView.getRightDrawable());
                    nameTextView[a].setRightDrawable2(titleTextView.getRightDrawable2());
                } else if (a == 0 && user.id != getUserConfig().getClientUserId() && !MessagesController.isSupportUser(user) && user.phone != null && user.phone.length() != 0 && getContactsController().contactsDict.get(user.id) == null &&
                        (getContactsController().contactsDict.size() != 0 || !getContactsController().isLoadingContacts())) {
                    nameTextView[a].setText(PhoneFormat.getInstance().format("+" + user.phone));
                } else {
                    nameTextView[a].setText(newString);
                }
                if (a == 0 && onlineTextOverride != null) {
                    onlineTextView[a].setText(onlineTextOverride);
                } else if (a == 0 && copyFromChatActivity) {
                    ChatActivity chatActivity = (ChatActivity) prevFragment;
                    if (chatActivity.avatarContainer.getSubtitleTextView() instanceof SimpleTextView) {
                        SimpleTextView textView = (SimpleTextView) chatActivity.avatarContainer.getSubtitleTextView();
                        onlineTextView[a].setText(textView.getText());
                    } else if (chatActivity.avatarContainer.getSubtitleTextView() instanceof AnimatedTextView) {
                        AnimatedTextView textView = (AnimatedTextView) chatActivity.avatarContainer.getSubtitleTextView();
                        onlineTextView[a].setText(textView.getText());
                    }
                } else {
                    onlineTextView[a].setText(newString2);
                }
                onlineTextView[a].setDrawablePadding(dp(9));
                onlineTextView[a].setRightDrawableInside(true);
                onlineTextView[a].setRightDrawable(a == 1 && hiddenStatusButton ? getShowStatusButton() : null);
                onlineTextView[a].setRightDrawableOnClick(a == 1 && hiddenStatusButton ? v -> {
                    MessagePrivateSeenView.showSheet(getContext(), currentAccount, getDialogId(), true, null, () -> {
                        getMessagesController().reloadUser(getDialogId());
                    }, resourcesProvider);
                } : null);
                Drawable leftIcon = currentEncryptedChat != null ? getLockIconDrawable() : null;
                boolean rightIconIsPremium = false, rightIconIsStatus = false;
                nameTextView[a].setRightDrawableOutside(a == 0);
                if (a == 0 && !copyFromChatActivity) {
                    if (user.scam || user.fake) {
                        nameTextView[a].setRightDrawable2(getScamDrawable(user.scam ? 0 : 1));
                        nameTextViewRightDrawable2ContentDescription = LocaleController.getString(R.string.ScamMessage);
                    } else if (user.verified) {
                        nameTextView[a].setRightDrawable2(getVerifiedCrossfadeDrawable(a));
                        nameTextViewRightDrawable2ContentDescription = LocaleController.getString(R.string.AccDescrVerified);
                    } else if (getMessagesController().isDialogMuted(dialogId != 0 ? dialogId : userId, topicId)) {
                        nameTextView[a].setRightDrawable2(getThemedDrawable(Theme.key_drawable_muteIconDrawable));
                        nameTextViewRightDrawable2ContentDescription = LocaleController.getString(R.string.NotificationsMuted);
                    } else {
                        nameTextView[a].setRightDrawable2(null);
                        nameTextViewRightDrawable2ContentDescription = null;
                    }
                    if (user != null && !getMessagesController().premiumFeaturesBlocked() && !MessagesController.isSupportUser(user) && DialogObject.getEmojiStatusDocumentId(user.emoji_status) != 0) {
                        rightIconIsStatus = true;
                        rightIconIsPremium = false;
                        nameTextView[a].setRightDrawable(getEmojiStatusDrawable(user.emoji_status, false, false, a));
                        nameTextViewRightDrawableContentDescription = LocaleController.getString(R.string.AccDescrPremium);
                    } else if (getMessagesController().isPremiumUser(user)) {
                        rightIconIsStatus = false;
                        rightIconIsPremium = true;
                        nameTextView[a].setRightDrawable(getEmojiStatusDrawable(null, false, false, a));
                        nameTextViewRightDrawableContentDescription = LocaleController.getString( R.string.AccDescrPremium);
                    } else {
                        nameTextView[a].setRightDrawable(null);
                        nameTextViewRightDrawableContentDescription = null;
                    }
                } else if (a == 1) {
                    if (user.scam || user.fake) {
                        nameTextView[a].setRightDrawable2(getScamDrawable(user.scam ? 0 : 1));
                    } else if (user.verified) {
                        nameTextView[a].setRightDrawable2(getVerifiedCrossfadeDrawable(a));
                    } else {
                        nameTextView[a].setRightDrawable2(null);
                    }
                    if (!getMessagesController().premiumFeaturesBlocked() && user != null && !MessagesController.isSupportUser(user) && DialogObject.getEmojiStatusDocumentId(user.emoji_status) != 0) {
                        rightIconIsStatus = true;
                        rightIconIsPremium = false;
                        nameTextView[a].setRightDrawable(getEmojiStatusDrawable(user.emoji_status, true, true, a));
                    } else if (getMessagesController().isPremiumUser(user)) {
                        rightIconIsStatus = false;
                        rightIconIsPremium = true;
                        nameTextView[a].setRightDrawable(getEmojiStatusDrawable(null, true, true, a));
                    } else {
                        nameTextView[a].setRightDrawable(null);
                    }
                }
                if (leftIcon == null && currentEncryptedChat == null && user.bot_verification_icon != 0) {
                    nameTextView[a].setLeftDrawableOutside(true);
                    leftIcon = getBotVerificationDrawable(user.bot_verification_icon, false, a);
                } else {
                    nameTextView[a].setLeftDrawableOutside(false);
                }
                nameTextView[a].setLeftDrawable(leftIcon);
                if (a == 1 && (rightIconIsStatus || rightIconIsPremium)) {
                    nameTextView[a].setRightDrawableOutside(true);
                }
                if (user.self && getMessagesController().isPremiumUser(user)) {
                    nameTextView[a].setRightDrawableOnClick(v -> {
                        showStatusSelect();
                    });
                }
                if (!user.self && getMessagesController().isPremiumUser(user)) {
                    final SimpleTextView textView = nameTextView[a];
                    nameTextView[a].setRightDrawableOnClick(v -> {
                        if (user.emoji_status instanceof TLRPC.TL_emojiStatusCollectible) {
                            TLRPC.TL_emojiStatusCollectible status = (TLRPC.TL_emojiStatusCollectible) user.emoji_status;
                            if (status != null) {
                                Browser.openUrl(getContext(), "https://" + getMessagesController().linkPrefix + "/nft/" + status.slug);
                            }
                            return;
                        }
                        PremiumPreviewBottomSheet premiumPreviewBottomSheet = new PremiumPreviewBottomSheet(ProfileActivity.this, currentAccount, user, resourcesProvider);
                        int[] coords = new int[2];
                        textView.getLocationOnScreen(coords);
                        premiumPreviewBottomSheet.startEnterFromX = textView.rightDrawableX;
                        premiumPreviewBottomSheet.startEnterFromY = textView.rightDrawableY;
                        premiumPreviewBottomSheet.startEnterFromScale = textView.getScaleX();
                        premiumPreviewBottomSheet.startEnterFromX1 = textView.getLeft();
                        premiumPreviewBottomSheet.startEnterFromY1 = textView.getTop();
                        premiumPreviewBottomSheet.startEnterFromView = textView;
                        if (textView.getRightDrawable() == emojiStatusDrawable[1] && emojiStatusDrawable[1] != null && emojiStatusDrawable[1].getDrawable() instanceof AnimatedEmojiDrawable) {
                            premiumPreviewBottomSheet.startEnterFromScale *= 0.98f;
                            TLRPC.Document document = ((AnimatedEmojiDrawable) emojiStatusDrawable[1].getDrawable()).getDocument();
                            if (document != null) {
                                BackupImageView icon = new BackupImageView(getContext());
                                String filter = "160_160";
                                ImageLocation mediaLocation;
                                String mediaFilter;
                                SvgHelper.SvgDrawable thumbDrawable = DocumentObject.getSvgThumb(document.thumbs, Theme.key_windowBackgroundWhiteGrayIcon, 0.2f);
                                TLRPC.PhotoSize thumb = FileLoader.getClosestPhotoSizeWithSize(document.thumbs, 90);
                                if ("video/webm".equals(document.mime_type)) {
                                    mediaLocation = ImageLocation.getForDocument(document);
                                    mediaFilter = filter + "_" + ImageLoader.AUTOPLAY_FILTER;
                                    if (thumbDrawable != null) {
                                        thumbDrawable.overrideWidthAndHeight(512, 512);
                                    }
                                } else {
                                    if (thumbDrawable != null && MessageObject.isAnimatedStickerDocument(document, false)) {
                                        thumbDrawable.overrideWidthAndHeight(512, 512);
                                    }
                                    mediaLocation = ImageLocation.getForDocument(document);
                                    mediaFilter = filter;
                                }
                                icon.setLayerNum(7);
                                icon.setRoundRadius(AndroidUtilities.dp(4));
                                icon.setImage(mediaLocation, mediaFilter, ImageLocation.getForDocument(thumb, document), "140_140", thumbDrawable, document);
                                if (((AnimatedEmojiDrawable) emojiStatusDrawable[1].getDrawable()).canOverrideColor()) {
                                    icon.setColorFilter(new PorterDuffColorFilter(getThemedColor(Theme.key_windowBackgroundWhiteBlueIcon), PorterDuff.Mode.SRC_IN));
                                    premiumPreviewBottomSheet.statusStickerSet = MessageObject.getInputStickerSet(document);
                                } else {
                                    premiumPreviewBottomSheet.statusStickerSet = MessageObject.getInputStickerSet(document);
                                }
                                premiumPreviewBottomSheet.overrideTitleIcon = icon;
                                premiumPreviewBottomSheet.isEmojiStatus = true;
                            }
                        }
                        showDialog(premiumPreviewBottomSheet);
                    });
                }
            }

            if (userId == UserConfig.getInstance(currentAccount).clientUserId) {
                onlineTextView[2].setText(LocaleController.getString(R.string.FallbackTooltip));
                onlineTextView[3].setText(LocaleController.getString(R.string.Online));
            } else {
                if (user.photo != null && user.photo.personal && user.photo.has_video) {
                    SpannableStringBuilder spannableStringBuilder = new SpannableStringBuilder(newString2);
                    spannableStringBuilder.setSpan(new EmptyStubSpan(), 0, newString2.length(), 0);
                    spannableStringBuilder.append(" d ");
                    spannableStringBuilder.append(LocaleController.getString(R.string.CustomAvatarTooltipVideo));
                    spannableStringBuilder.setSpan(new DotDividerSpan(), newString2.length() + 1, newString2.length() + 2, 0);
                    onlineTextView[2].setText(spannableStringBuilder);
                } else {
                    SpannableStringBuilder spannableStringBuilder = new SpannableStringBuilder(newString2);
                    spannableStringBuilder.setSpan(new EmptyStubSpan(), 0, newString2.length(), 0);
                    spannableStringBuilder.append(" d ");
                    spannableStringBuilder.append(LocaleController.getString(R.string.CustomAvatarTooltip));
                    spannableStringBuilder.setSpan(new DotDividerSpan(), newString2.length() + 1, newString2.length() + 2, 0);
                    onlineTextView[2].setText(spannableStringBuilder);
                }
            }

            onlineTextView[2].setVisibility(View.VISIBLE);
            if (!searchMode) {
                onlineTextView[3].setVisibility(View.VISIBLE);
            }

            if (previousTransitionFragment != null) {
                previousTransitionFragment.checkAndUpdateAvatar();
            }
            avatarImage.getImageReceiver().setVisible(!PhotoViewer.isShowingImage(photoBig) && (getLastStoryViewer() == null || getLastStoryViewer().transitionViewHolder.view != avatarImage), storyView != null);
        } else if (chatId != 0) {
            TLRPC.Chat chat = getMessagesController().getChat(chatId);
            if (chat != null) {
                currentChat = chat;
            } else {
                chat = currentChat;
            }
            if (flagSecure != null) {
                flagSecure.invalidate();
            }

            final MessagesController.PeerColor wasPeerColor = peerColor;
            peerColor = MessagesController.PeerColor.fromCollectible(chat.emoji_status);
            if (peerColor == null) {
                final int colorId = ChatObject.getProfileColorId(chat);
                MessagesController.PeerColors peerColors = MessagesController.getInstance(currentAccount).profilePeerColors;
                peerColor = peerColors == null ? null : peerColors.getColor(colorId);
            }
            if (wasPeerColor != peerColor) {
                updatedPeerColor();
            }
            if (topView != null) {
                topView.setBackgroundEmojiId(ChatObject.getProfileEmojiId(chat), chat != null && chat.emoji_status instanceof TLRPC.TL_emojiStatusCollectible, true);
            }
            setCollectibleGiftStatus(chat.emoji_status instanceof TLRPC.TL_emojiStatusCollectible ? (TLRPC.TL_emojiStatusCollectible) chat.emoji_status : null);

            if (isTopic) {
                topic = getMessagesController().getTopicsController().findTopic(chatId, topicId);
            }

            CharSequence statusString;
            CharSequence profileStatusString;
            boolean profileStatusIsButton = false;
            if (ChatObject.isChannel(chat)) {
                if (!isTopic && (chatInfo == null || !currentChat.megagroup && (chatInfo.participants_count == 0 || ChatObject.hasAdminRights(currentChat) || chatInfo.can_view_participants))) {
                    if (currentChat.megagroup) {
                        statusString = profileStatusString = LocaleController.getString(R.string.Loading).toLowerCase();
                    } else {
                        if (ChatObject.isPublic(chat)) {
                            statusString = profileStatusString = LocaleController.getString(R.string.ChannelPublic).toLowerCase();
                        } else {
                            statusString = profileStatusString = LocaleController.getString(R.string.ChannelPrivate).toLowerCase();
                        }
                    }
                } else {
                    if (isTopic) {
                        int count = 0;
                        if (topic != null) {
                            count = topic.totalMessagesCount - 1;
                        }
                        if (count > 0) {
                            statusString = LocaleController.formatPluralString("messages", count, count);
                        } else {
                            statusString = formatString("TopicProfileStatus", R.string.TopicProfileStatus, chat.title);
                        }
                        SpannableString arrowString = new SpannableString(">");
                        arrowString.setSpan(new ColoredImageSpan(R.drawable.arrow_newchat), 0, 1, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                        profileStatusString = new SpannableStringBuilder(chat.title).append(' ').append(arrowString);
                        profileStatusIsButton = true;
                    } else if (currentChat.megagroup) {
                        if (onlineCount > 1 && chatInfo.participants_count != 0) {
                            statusString = String.format("%s, %s", LocaleController.formatPluralString("Members", chatInfo.participants_count), LocaleController.formatPluralString("OnlineCount", Math.min(onlineCount, chatInfo.participants_count)));
                            profileStatusString = String.format("%s, %s", LocaleController.formatPluralStringComma("Members", chatInfo.participants_count), LocaleController.formatPluralStringComma("OnlineCount", Math.min(onlineCount, chatInfo.participants_count)));
                        } else {
                            if (chatInfo.participants_count == 0) {
                                if (chat.has_geo) {
                                    statusString = profileStatusString = LocaleController.getString(R.string.MegaLocation).toLowerCase();
                                } else if (ChatObject.isPublic(chat)) {
                                    statusString = profileStatusString = LocaleController.getString(R.string.MegaPublic).toLowerCase();
                                } else {
                                    statusString = profileStatusString = LocaleController.getString(R.string.MegaPrivate).toLowerCase();
                                }
                            } else {
                                statusString = LocaleController.formatPluralString("Members", chatInfo.participants_count);
                                profileStatusString = LocaleController.formatPluralStringComma("Members", chatInfo.participants_count);
                            }
                        }
                    } else {
                        int[] result = new int[1];
                        String shortNumber = LocaleController.formatShortNumber(chatInfo.participants_count, result);
                        if (currentChat.megagroup) {
                            statusString = LocaleController.formatPluralString("Members", chatInfo.participants_count);
                            profileStatusString = LocaleController.formatPluralStringComma("Members", chatInfo.participants_count);
                        } else {
                            statusString = LocaleController.formatPluralString("Subscribers", chatInfo.participants_count);
                            profileStatusString = LocaleController.formatPluralStringComma("Subscribers", chatInfo.participants_count);
                        }
                    }
                }
            } else {
                if (ChatObject.isKickedFromChat(chat)) {
                    statusString = profileStatusString = LocaleController.getString(R.string.YouWereKicked);
                } else if (ChatObject.isLeftFromChat(chat)) {
                    statusString = profileStatusString = LocaleController.getString(R.string.YouLeft);
                } else {
                    int count = chat.participants_count;
                    if (chatInfo != null && chatInfo.participants != null) {
                        count = chatInfo.participants.participants.size();
                    }
                    if (count != 0 && onlineCount > 1) {
                        statusString = profileStatusString = String.format("%s, %s", LocaleController.formatPluralString("Members", count), LocaleController.formatPluralString("OnlineCount", onlineCount));
                    } else {
                        statusString = profileStatusString = LocaleController.formatPluralString("Members", count);
                    }
                }
            }
            if (copyFromChatActivity) {
                ChatActivity chatActivity = (ChatActivity) prevFragment;
                if (chatActivity.avatarContainer.getSubtitleTextView() instanceof SimpleTextView) {
                    statusString = ((SimpleTextView) chatActivity.avatarContainer.getSubtitleTextView()).getText();
                } else if (chatActivity.avatarContainer.getSubtitleTextView() instanceof AnimatedTextView) {
                    statusString = ((AnimatedTextView) chatActivity.avatarContainer.getSubtitleTextView()).getText();
                }
                BackupImageView fromAvatarImage = chatActivity.avatarContainer.getAvatarImageView();
                avatarImage.setAnimateFromImageReceiver(fromAvatarImage.getImageReceiver());
            }

            boolean changed = false;
            for (int a = 0; a < 2; a++) {
                if (nameTextView[a] == null) {
                    continue;
                }
                if (a == 0 && copyFromChatActivity) {
                    ChatActivity chatActivity = (ChatActivity) prevFragment;
                    SimpleTextView titleTextView = chatActivity.avatarContainer.getTitleTextView();
                    if (nameTextView[a].setText(titleTextView.getText())) {
                        changed = true;
                    }
                    if (nameTextView[a].setRightDrawable(titleTextView.getRightDrawable())) {
                        changed = true;
                    }
                    if (nameTextView[a].setRightDrawable2(titleTextView.getRightDrawable2())) {
                        changed = true;
                    }
                } else if (isTopic) {
                    CharSequence title = topic == null ? "" : topic.title;
                    try {
                        title = Emoji.replaceEmoji(title, nameTextView[a].getPaint().getFontMetricsInt(), false);
                    } catch (Exception ignore) {
                    }
                    if (nameTextView[a].setText(title)) {
                        changed = true;
                    }
                } else if (ChatObject.isMonoForum(chat)) {
                    CharSequence title = getString(R.string.ChatMessageSuggestions);
                    if (nameTextView[a].setText(title)) {
                        changed = true;
                    }
                } else if (chat.title != null) {
                    CharSequence title = chat.title;
                    try {
                        title = Emoji.replaceEmoji(title, nameTextView[a].getPaint().getFontMetricsInt(), false);
                    } catch (Exception ignore) {
                    }
                    if (nameTextView[a].setText(title)) {
                        changed = true;
                    }
                }
                nameTextView[a].setLeftDrawableOutside(false);
                nameTextView[a].setLeftDrawable(null);
                nameTextView[a].setRightDrawableOutside(a == 0);
                nameTextView[a].setRightDrawableOnClick(null);
                if (a != 0) {
                    if (chat.scam || chat.fake) {
                        nameTextView[a].setRightDrawable2(getScamDrawable(chat.scam ? 0 : 1));
                        nameTextViewRightDrawableContentDescription = LocaleController.getString(R.string.ScamMessage);
                    } else if (chat.verified) {
                        nameTextView[a].setRightDrawable2(getVerifiedCrossfadeDrawable(a));
                        nameTextViewRightDrawableContentDescription = LocaleController.getString(R.string.AccDescrVerified);
                    } else {
                        nameTextView[a].setRightDrawable2(null);
                        nameTextViewRightDrawableContentDescription = null;
                    }
                    if (DialogObject.getEmojiStatusDocumentId(chat.emoji_status) != 0) {
                        nameTextView[a].setRightDrawable(getEmojiStatusDrawable(chat.emoji_status, true, false, a));
                        nameTextView[a].setRightDrawableOutside(true);
                        nameTextViewRightDrawableContentDescription = null;
                        if (ChatObject.canChangeChatInfo(chat)) {
                            nameTextView[a].setRightDrawableOnClick(v -> {
                                showStatusSelect();
                            });
                            if (preloadedChannelEmojiStatuses) {
                                preloadedChannelEmojiStatuses = true;
                                getMediaDataController().loadRestrictedStatusEmojis();
                            }
                        } else if (chat.emoji_status instanceof TLRPC.TL_emojiStatusCollectible) {
                            final String slug = ((TLRPC.TL_emojiStatusCollectible) chat.emoji_status).slug;
                            nameTextView[a].setRightDrawableOnClick(v -> {
                                Browser.openUrl(getContext(), "https://" + getMessagesController().linkPrefix + "/nft/" + slug);
                            });
                        }
                    }
                } else if (!copyFromChatActivity) {
                    if (chat.scam || chat.fake) {
                        nameTextView[a].setRightDrawable2(getScamDrawable(chat.scam ? 0 : 1));
                    } else if (chat.verified) {
                        nameTextView[a].setRightDrawable2(getVerifiedCrossfadeDrawable(a)) ;
                    } else if (getMessagesController().isDialogMuted(-chatId, topicId)) {
                        nameTextView[a].setRightDrawable2(getThemedDrawable(Theme.key_drawable_muteIconDrawable));
                    } else {
                        nameTextView[a].setRightDrawable2(null);
                    }
                    if (DialogObject.getEmojiStatusDocumentId(chat.emoji_status) != 0) {
                        nameTextView[a].setRightDrawable(getEmojiStatusDrawable(chat.emoji_status, false, false, a));
                        nameTextView[a].setRightDrawableOutside(true);
                    } else {
                        nameTextView[a].setRightDrawable(null);
                    }
                }
                if (chat.bot_verification_icon != 0) {
                    nameTextView[a].setLeftDrawableOutside(true);
                    nameTextView[a].setLeftDrawable(getBotVerificationDrawable(chat.bot_verification_icon, false, a));
                } else {
                    nameTextView[a].setLeftDrawable(null);
                }
                if (a == 0 && onlineTextOverride != null) {
                    onlineTextView[a].setText(onlineTextOverride);
                } else {
                    if (copyFromChatActivity || (currentChat.megagroup && chatInfo != null && onlineCount > 0) || isTopic) {
                        onlineTextView[a].setText(a == 0 ? statusString : profileStatusString);
                    } else if (a == 0 && ChatObject.isChannel(currentChat) && chatInfo != null && chatInfo.participants_count != 0 && (currentChat.megagroup || currentChat.broadcast)) {
                        int[] result = new int[1];
                        boolean ignoreShort = AndroidUtilities.isAccessibilityScreenReaderEnabled();
                        String shortNumber = ignoreShort ? String.valueOf(result[0] = chatInfo.participants_count) : LocaleController.formatShortNumber(chatInfo.participants_count, result);
                        if (currentChat.megagroup) {
                            if (chatInfo.participants_count == 0) {
                                if (chat.has_geo) {
                                    onlineTextView[a].setText(LocaleController.getString(R.string.MegaLocation).toLowerCase());
                                } else if (ChatObject.isPublic(chat)) {
                                    onlineTextView[a].setText(LocaleController.getString(R.string.MegaPublic).toLowerCase());
                                } else {
                                    onlineTextView[a].setText(LocaleController.getString(R.string.MegaPrivate).toLowerCase());
                                }
                            } else {
                                onlineTextView[a].setText(LocaleController.formatPluralString("Members", result[0]).replace(String.format("%d", result[0]), shortNumber));
                            }
                        } else {
                            onlineTextView[a].setText(LocaleController.formatPluralString("Subscribers", result[0]).replace(String.format("%d", result[0]), shortNumber));
                        }
                    } else {
                        onlineTextView[a].setText(a == 0 ? statusString : profileStatusString);
                    }
                }
                if (a == 1 && isTopic) {
                    if (profileStatusIsButton) {
                        onlineTextView[a].setOnClickListener(e -> goToForum());
                    } else {
                        onlineTextView[a].setOnClickListener(null);
                        onlineTextView[a].setClickable(false);
                    }
                }
            }
            if (changed) {
                needLayout(true);
            }

            TLRPC.FileLocation photoBig = null;
            if (chat.photo != null && !isTopic) {
                photoBig = chat.photo.photo_big;
            }

            final ImageLocation imageLocation;
            final ImageLocation thumbLocation;
            final ImageLocation videoLocation;
            if (isTopic) {
                imageLocation = null;
                thumbLocation = null;
                videoLocation = null;
                ForumUtilities.setTopicIcon(avatarImage, topic, true, true, resourcesProvider);
            } else if (ChatObject.isMonoForum(currentChat)) {
                TLRPC.Chat channel = getMessagesController().getMonoForumLinkedChat(currentChat.id);
                avatarDrawable.setInfo(currentAccount, channel);
                imageLocation = ImageLocation.getForUserOrChat(channel, ImageLocation.TYPE_BIG);
                thumbLocation = ImageLocation.getForUserOrChat(channel, ImageLocation.TYPE_SMALL);
                videoLocation = avatarsViewPager.getCurrentVideoLocation(thumbLocation, imageLocation);
            } else {
                avatarDrawable.setInfo(currentAccount, chat);
                imageLocation = ImageLocation.getForUserOrChat(chat, ImageLocation.TYPE_BIG);
                thumbLocation = ImageLocation.getForUserOrChat(chat, ImageLocation.TYPE_SMALL);
                videoLocation = avatarsViewPager.getCurrentVideoLocation(thumbLocation, imageLocation);
            }

            boolean initied = avatarsViewPager.initIfEmpty(null, imageLocation, thumbLocation, reload);
            if ((imageLocation == null || initied) && isPulledDown) {
                final View view = layoutManager.findViewByPosition(0);
                if (view != null) {
                    listView.smoothScrollBy(0, view.getTop() - AndroidUtilities.dp(88), CubicBezierInterpolator.EASE_OUT_QUINT);
                }
            }
            String filter;
            if (videoLocation != null && videoLocation.imageType == FileLoader.IMAGE_TYPE_ANIMATION) {
                filter = ImageLoader.AUTOPLAY_FILTER;
            } else {
                filter = null;
            }
            if (avatarBig == null && !isTopic) {
                avatarImage.setImage(videoLocation, filter, thumbLocation, "50_50", avatarDrawable, chat);
            }
            if (imageLocation != null && (prevLoadedImageLocation == null || imageLocation.photoId != prevLoadedImageLocation.photoId)) {
                prevLoadedImageLocation = imageLocation;
                getFileLoader().loadFile(imageLocation, chat, null, FileLoader.PRIORITY_LOW, 1);
            }
            avatarImage.getImageReceiver().setVisible(!PhotoViewer.isShowingImage(photoBig) && (getLastStoryViewer() == null || getLastStoryViewer().transitionViewHolder.view != avatarImage), storyView != null);
        }

        if (qrItem != null) {
            updateQrItemVisibility(true);
        }
        AndroidUtilities.runOnUIThread(this::updateEmojiStatusEffectPosition);
    }

    private void updatedPeerColor() {
        adaptedColors.clear();
        if (topView != null) {
            topView.setBackgroundColorId(peerColor, true);
        }
        if (onlineTextView[1] != null) {
            int statusColor;
            if (onlineTextView[1].getTag() instanceof Integer) {
                statusColor = getThemedColor((Integer) onlineTextView[1].getTag());
            } else {
                statusColor = getThemedColor(Theme.key_avatar_subtitleInProfileBlue);
            }
            onlineTextView[1].setTextColor(ColorUtils.blendARGB(applyPeerColor(statusColor, true, isOnline[0]), 0xB3FFFFFF, currentExpandAnimatorValue));
        }
        if (showStatusButton != null) {
            showStatusButton.setBackgroundColor(ColorUtils.blendARGB(Theme.multAlpha(Theme.adaptHSV(actionBarBackgroundColor, +0.18f, -0.1f), 0.5f), 0x23ffffff, currentExpandAnimatorValue));
        }
        if (actionBar != null) {
            actionBar.setItemsColor(ColorUtils.blendARGB(peerColor != null ? Color.WHITE : getThemedColor(Theme.key_actionBarDefaultIcon), getThemedColor(Theme.key_actionBarActionModeDefaultIcon), mediaHeaderAnimationProgress), false);
            actionBar.setItemsBackgroundColor(ColorUtils.blendARGB(peerColor != null ? Theme.ACTION_BAR_WHITE_SELECTOR_COLOR : peerColor != null ? 0x20ffffff : getThemedColor(Theme.key_avatar_actionBarSelectorBlue), getThemedColor(Theme.key_actionBarActionModeDefaultSelector), mediaHeaderAnimationProgress), false);
        }
        if (verifiedDrawable[1] != null) {
            final int color1 = peerColor != null ? Theme.adaptHSV(ColorUtils.blendARGB(peerColor.getColor2(), peerColor.hasColor6(Theme.isCurrentThemeDark()) ? peerColor.getColor5() : peerColor.getColor3(), .4f), +.1f, Theme.isCurrentThemeDark() ? -.1f : -.08f) : getThemedColor(Theme.key_profile_verifiedBackground);
            final int color2 = getThemedColor(Theme.key_player_actionBarTitle);
            verifiedDrawable[1].setColorFilter(AndroidUtilities.getOffsetColor(color1, color2, mediaHeaderAnimationProgress, 1.0f), PorterDuff.Mode.MULTIPLY);
        }
        if (verifiedCheckDrawable[1] != null) {
            final int color1 = peerColor != null ? Color.WHITE : dontApplyPeerColor(getThemedColor(Theme.key_profile_verifiedCheck));
            final int color2 = getThemedColor(Theme.key_windowBackgroundWhite);
            verifiedCheckDrawable[1].setColorFilter(AndroidUtilities.getOffsetColor(color1, color2, mediaHeaderAnimationProgress, 1.0f), PorterDuff.Mode.MULTIPLY);
        }
        if (nameTextView[1] != null) {
            nameTextView[1].setTextColor(ColorUtils.blendARGB(ColorUtils.blendARGB(peerColor != null ? Color.WHITE : getThemedColor(Theme.key_profile_title), getThemedColor(Theme.key_player_actionBarTitle), mediaHeaderAnimationProgress), Color.WHITE, currentExpandAnimatorValue));
        }
        if (autoDeletePopupWrapper != null && autoDeletePopupWrapper.textView != null) {
            autoDeletePopupWrapper.textView.invalidate();
        }
        AndroidUtilities.forEachViews(listView, view -> {
            if (view instanceof HeaderCell) {
                ((HeaderCell) view).setTextColor(dontApplyPeerColor(getThemedColor(Theme.key_windowBackgroundWhiteBlueHeader), false));
            } else if (view instanceof TextDetailCell) {
                ((TextDetailCell) view).updateColors();
            } else if (view instanceof TextCell) {
                ((TextCell) view).updateColors();
            } else if (view instanceof AboutLinkCell) {
                ((AboutLinkCell) view).updateColors();
            } else if (view instanceof NotificationsCheckCell) {
                ((NotificationsCheckCell) view).getCheckBox().invalidate();
            } else if (view instanceof ProfileHoursCell) {
                ((ProfileHoursCell) view).updateColors();
            } else if (view instanceof ProfileChannelCell) {
                ((ProfileChannelCell) view).updateColors();
            }
        });
        if (sharedMediaLayout != null && sharedMediaLayout.scrollSlidingTextTabStrip != null) {
            sharedMediaLayout.scrollSlidingTextTabStrip.updateColors();
        }
        if (sharedMediaLayout != null && sharedMediaLayout.giftsContainer != null) {
            sharedMediaLayout.giftsContainer.updateColors();
        }
        writeButtonSetBackground();
        updateEmojiStatusDrawableColor();
        if (storyView != null) {
            storyView.update();
        }
        if (giftsView != null) {
            giftsView.update();
        }
    }

    private int dontApplyPeerColor(int color) {
        return dontApplyPeerColor(color, true, null);
    }

    public int dontApplyPeerColor(int color, boolean actionBar) {
        return dontApplyPeerColor(color, actionBar, null);
    }

    private final SparseIntArray adaptedColors = new SparseIntArray();
    private int dontApplyPeerColor(int color, boolean actionBar, Boolean online) {
        return color;
    }

    private int applyPeerColor(int color, boolean actionBar, Boolean online) {
        if (!actionBar && isSettings()) return color;
        if (peerColor != null) {
            if (!actionBar) {
                int index = adaptedColors.indexOfKey(color);
                if (index < 0) {
                    final int baseColor = Theme.adaptHSV(peerColor.getBgColor1(Theme.isCurrentThemeDark()), Theme.isCurrentThemeDark() ? 0 : +.05f, Theme.isCurrentThemeDark() ? -.1f : -.04f);
                    int adapted = OKLCH.adapt(color, baseColor);
                    adaptedColors.put(color, adapted);
                    return adapted;
                } else {
                    return adaptedColors.valueAt(index);
                }
            }
            final int baseColor = getThemedColor(actionBar ? Theme.key_actionBarDefault : Theme.key_windowBackgroundWhiteBlueIcon);
            final int storyColor = ColorUtils.blendARGB(peerColor.getStoryColor1(Theme.isCurrentThemeDark()), peerColor.getStoryColor2(Theme.isCurrentThemeDark()), .5f);
            int accentColor = actionBar ? storyColor : peerColor.getBgColor1(Theme.isCurrentThemeDark());
            if (!Theme.hasHue(baseColor)) {
                return online != null && !online ? Theme.adaptHSV(Theme.multAlpha(storyColor, .7f), -.2f, +.2f) : storyColor;
            }
            return Theme.changeColorAccent(baseColor, accentColor, color, Theme.isCurrentThemeDark(), online != null && !online ? Theme.multAlpha(storyColor, .7f) : storyColor);
        }
        return color;
    }

    private int applyPeerColor2(int color) {
        if (peerColor != null) {
            int accentColor = peerColor.getBgColor2(Theme.isCurrentThemeDark());
            return Theme.changeColorAccent(getThemedColor(Theme.key_windowBackgroundWhiteBlueIcon), accentColor, color, Theme.isCurrentThemeDark(), accentColor);
        }
        return color;
    }

    private void createActionBarMenu(boolean animated) {
        if (actionBar == null || otherItem == null) {
            return;
        }
        Context context = actionBar.getContext();
        otherItem.removeAllSubItems();
        animatingItem = null;

        editItemVisible = false;
        callItemVisible = false;
        videoCallItemVisible = false;
        canSearchMembers = false;
        boolean selfUser = false;

        if (userId != 0) {
            TLRPC.User user = getMessagesController().getUser(userId);
            if (user == null) {
                return;
            }
            if (UserObject.isUserSelf(user)) {
                editItemVisible = myProfile;
                otherItem.addSubItem(edit_info, R.drawable.msg_edit, LocaleController.getString(R.string.EditInfo));
                if (imageUpdater != null) {
                    otherItem.addSubItem(add_photo, R.drawable.msg_addphoto, LocaleController.getString(R.string.AddPhoto));
                }
                editColorItem = otherItem.addSubItem(edit_color, R.drawable.menu_profile_colors, LocaleController.getString(R.string.ProfileColorEdit));
                updateEditColorIcon();
                if (myProfile) {
                    setUsernameItem = otherItem.addSubItem(set_username, R.drawable.menu_username_change, getString(R.string.ProfileUsernameEdit));
                    linkItem = otherItem.addSubItem(copy_link_profile, R.drawable.msg_link2, getString(R.string.ProfileCopyLink));
                    updateItemsUsername();
                }
                selfUser = true;
            } else {
                if (user.bot && user.bot_can_edit) {
                    editItemVisible = true;
                }

                if (userInfo != null && userInfo.phone_calls_available) {
                    callItemVisible = true;
                    videoCallItemVisible = Build.VERSION.SDK_INT >= 18 && userInfo.video_calls_available;
                }
                if (isBot || getContactsController().contactsDict.get(userId) == null) {
                    if (MessagesController.isSupportUser(user)) {
                        if (userBlocked) {
                            otherItem.addSubItem(block_contact, R.drawable.msg_block, LocaleController.getString(R.string.Unblock));
                        }
                        otherItem.addSubItem(add_shortcut, R.drawable.msg_home, LocaleController.getString(R.string.AddShortcut));
                    } else if (getDialogId() != UserObject.VERIFY) {
                        if (currentEncryptedChat == null) {
                            createAutoDeleteItem(context);
                        }
                        otherItem.addSubItem(add_shortcut, R.drawable.msg_home, LocaleController.getString(R.string.AddShortcut));
                        if (isBot) {
                            otherItem.addSubItem(share, R.drawable.msg_share, LocaleController.getString(R.string.BotShare));
                        } else {
                            otherItem.addSubItem(add_contact, R.drawable.msg_addcontact, LocaleController.getString(R.string.AddContact));
                        }
                        if (!TextUtils.isEmpty(user.phone)) {
                            otherItem.addSubItem(share_contact, R.drawable.msg_share, LocaleController.getString(R.string.ShareContact));
                        }
                        if (isBot) {
                            otherItem.addSubItem(bot_privacy, R.drawable.menu_privacy_policy, getString(R.string.BotPrivacyPolicy));
                            if (hasPrivacyCommand()) {
                                otherItem.showSubItem(bot_privacy);
                            } else {
                                otherItem.hideSubItem(bot_privacy);
                            }
                            otherItem.addSubItem(report, R.drawable.msg_report, LocaleController.getString(R.string.ReportBot)).setColors(getThemedColor(Theme.key_text_RedRegular), getThemedColor(Theme.key_text_RedRegular));
                            if (!userBlocked) {
                                otherItem.addSubItem(block_contact, R.drawable.msg_block2, LocaleController.getString(R.string.DeleteAndBlock)).setColors(getThemedColor(Theme.key_text_RedRegular), getThemedColor(Theme.key_text_RedRegular));
                            } else {
                                otherItem.addSubItem(block_contact, R.drawable.msg_retry, LocaleController.getString(R.string.BotRestart));
                            }
                        } else {
                            otherItem.addSubItem(block_contact, !userBlocked ? R.drawable.msg_block : R.drawable.msg_block, !userBlocked ? LocaleController.getString(R.string.BlockContact) : LocaleController.getString(R.string.Unblock));
                        }
                    }
                } else {
                    if (currentEncryptedChat == null) {
                        createAutoDeleteItem(context);
                    }
                    if (!TextUtils.isEmpty(user.phone)) {
                        otherItem.addSubItem(share_contact, R.drawable.msg_share, LocaleController.getString(R.string.ShareContact));
                    }
                    otherItem.addSubItem(block_contact, !userBlocked ? R.drawable.msg_block : R.drawable.msg_block, !userBlocked ? LocaleController.getString(R.string.BlockContact) : LocaleController.getString(R.string.Unblock));
                    otherItem.addSubItem(edit_contact, R.drawable.msg_edit, LocaleController.getString(R.string.EditContact));
                    otherItem.addSubItem(delete_contact, R.drawable.msg_delete, LocaleController.getString(R.string.DeleteContact));
                }
                if (!UserObject.isDeleted(user) && !isBot && currentEncryptedChat == null && !userBlocked && userId != 333000 && userId != 777000 && userId != 42777) {
                    if (!BuildVars.IS_BILLING_UNAVAILABLE && !user.self && !user.bot && !MessagesController.isSupportUser(user) && !getMessagesController().premiumPurchaseBlocked()) {
                        StarsController.getInstance(currentAccount).loadStarGifts();
                        otherItem.addSubItem(gift_premium, R.drawable.msg_gift_premium, LocaleController.getString(R.string.ProfileSendAGift));
                    }
                    otherItem.addSubItem(start_secret_chat, R.drawable.msg_secret, LocaleController.getString(R.string.StartEncryptedChat));
                    otherItem.setSubItemShown(start_secret_chat, DialogObject.isEmpty(getMessagesController().isUserContactBlocked(userId)));
                }
                if (!isBot && getContactsController().contactsDict.get(userId) != null) {
                    otherItem.addSubItem(add_shortcut, R.drawable.msg_home, LocaleController.getString(R.string.AddShortcut));
                }
            }
        } else if (chatId != 0) {
            TLRPC.Chat chat = getMessagesController().getChat(chatId);
            hasVoiceChatItem = false;

            if (topicId == 0 && ChatObject.canChangeChatInfo(chat)) {
                createAutoDeleteItem(context);
            }
            if (ChatObject.isChannel(chat)) {
                if (isTopic) {
                    if (ChatObject.canManageTopic(currentAccount, chat, topicId)) {
                        editItemVisible = true;
                    }
                } else {
                    if (ChatObject.hasAdminRights(chat) || chat.megagroup && ChatObject.canChangeChatInfo(chat)) {
                        editItemVisible = true;
                    }
                }
                if (chatInfo != null) {
                    if (ChatObject.canManageCalls(chat) && chatInfo.call == null) {
                        otherItem.addSubItem(call_item, R.drawable.msg_voicechat, chat.megagroup && !chat.gigagroup ? LocaleController.getString(R.string.StartVoipChat) : LocaleController.getString(R.string.StartVoipChannel));
                        hasVoiceChatItem = true;
                    }
                    if ((chatInfo.can_view_stats || chatInfo.can_view_revenue || chatInfo.can_view_stars_revenue || getMessagesController().getStoriesController().canPostStories(getDialogId())) && topicId == 0) {
                        otherItem.addSubItem(statistics, R.drawable.msg_stats, LocaleController.getString(R.string.Statistics));
                    }
                    ChatObject.Call call = getMessagesController().getGroupCall(chatId, false);
                    callItemVisible = call != null;
                }
                if (chat.megagroup) {
                    if (chatInfo == null || !chatInfo.participants_hidden || ChatObject.hasAdminRights(chat)) {
                        canSearchMembers = true;
                        otherItem.addSubItem(search_members, R.drawable.msg_search, LocaleController.getString(R.string.SearchMembers));
                    }
                    if (!chat.creator && !chat.left && !chat.kicked && !isTopic) {
                        otherItem.addSubItem(leave_group, R.drawable.msg_leave, LocaleController.getString(R.string.LeaveMegaMenu));
                    }
                    if (isTopic && ChatObject.canDeleteTopic(currentAccount, chat, topicId)) {
                        otherItem.addSubItem(delete_topic, R.drawable.msg_delete, LocaleController.getPluralString("DeleteTopics", 1));
                    }
                } else {
                    if (chat.creator || chat.admin_rights != null && chat.admin_rights.edit_stories) {
                        otherItem.addSubItem(channel_stories, R.drawable.msg_archive, LocaleController.getString(R.string.OpenChannelArchiveStories));
                    }
                    if (ChatObject.isPublic(chat)) {
                        otherItem.addSubItem(share, R.drawable.msg_share, LocaleController.getString(R.string.BotShare));
                    }
                    if (!BuildVars.IS_BILLING_UNAVAILABLE && !getMessagesController().premiumPurchaseBlocked()) {
                        StarsController.getInstance(currentAccount).loadStarGifts();
                        otherItem.addSubItem(gift_premium, R.drawable.msg_gift_premium, LocaleController.getString(R.string.ProfileSendAGiftToChannel));
                        otherItem.setSubItemShown(gift_premium, chatInfo != null && chatInfo.stargifts_available);
                    }
                    if (chatInfo != null && chatInfo.linked_chat_id != 0) {
                        otherItem.addSubItem(view_discussion, R.drawable.msg_discussion, LocaleController.getString(R.string.ViewDiscussion));
                    }
                    if (!currentChat.creator && !currentChat.left && !currentChat.kicked) {
                        otherItem.addSubItem(leave_group, R.drawable.msg_leave, LocaleController.getString(R.string.LeaveChannelMenu));
                    }
                }
            } else {
                if (chatInfo != null) {
                    if (ChatObject.canManageCalls(chat) && chatInfo.call == null) {
                        otherItem.addSubItem(call_item, R.drawable.msg_voicechat, LocaleController.getString(R.string.StartVoipChat));
                        hasVoiceChatItem = true;
                    }
                    ChatObject.Call call = getMessagesController().getGroupCall(chatId, false);
                    callItemVisible = call != null;
                }
                if (ChatObject.canChangeChatInfo(chat)) {
                    editItemVisible = true;
                }
                if (!ChatObject.isKickedFromChat(chat) && !ChatObject.isLeftFromChat(chat)) {
                    if (chatInfo == null || !chatInfo.participants_hidden || ChatObject.hasAdminRights(chat)) {
                        canSearchMembers = true;
                        otherItem.addSubItem(search_members, R.drawable.msg_search, LocaleController.getString(R.string.SearchMembers));
                    }
                }
                otherItem.addSubItem(leave_group, R.drawable.msg_leave, LocaleController.getString(R.string.DeleteAndExit));
            }
            if (topicId == 0) {
                otherItem.addSubItem(add_shortcut, R.drawable.msg_home, LocaleController.getString(R.string.AddShortcut));
            }
        }

        if (imageUpdater != null) {
            otherItem.addSubItem(set_as_main, R.drawable.msg_openprofile, LocaleController.getString(R.string.SetAsMain));
            otherItem.addSubItem(gallery_menu_save, R.drawable.msg_gallery, LocaleController.getString(R.string.SaveToGallery));
            //otherItem.addSubItem(edit_avatar, R.drawable.photo_paint, LocaleController.getString(R.string.EditPhoto));
            otherItem.addSubItem(delete_avatar, R.drawable.msg_delete, LocaleController.getString(R.string.Delete));
        } else {
            otherItem.addSubItem(gallery_menu_save, R.drawable.msg_gallery, LocaleController.getString(R.string.SaveToGallery));
        }
        if (getMessagesController().isChatNoForwards(currentChat)) {
            otherItem.hideSubItem(gallery_menu_save);
        }

        if (selfUser && !myProfile) {
            otherItem.addSubItem(logout, R.drawable.msg_leave, LocaleController.getString(R.string.LogOut));
        }
        if (!isPulledDown) {
            otherItem.hideSubItem(gallery_menu_save);
            otherItem.hideSubItem(set_as_main);
            otherItem.showSubItem(add_photo);
            otherItem.hideSubItem(edit_avatar);
            otherItem.hideSubItem(delete_avatar);
        }
        if (!mediaHeaderVisible) {
            if (callItemVisible) {
                if (callItem.getVisibility() != View.VISIBLE) {
                    callItem.setVisibility(View.VISIBLE);
                    if (animated) {
                        callItem.setAlpha(0);
                        callItem.animate().alpha(1f).setDuration(150).start();
                    }
                }
            } else {
                if (callItem.getVisibility() != View.GONE) {
                    callItem.setVisibility(View.GONE);
                }
            }
            if (videoCallItemVisible) {
                if (videoCallItem.getVisibility() != View.VISIBLE) {
                    videoCallItem.setVisibility(View.VISIBLE);
                    if (animated) {
                        videoCallItem.setAlpha(0);
                        videoCallItem.animate().alpha(1f).setDuration(150).start();
                    }
                }
            } else {
                if (videoCallItem.getVisibility() != View.GONE) {
                    videoCallItem.setVisibility(View.GONE);
                }
            }
            if (editItemVisible) {
                if (editItem.getVisibility() != View.VISIBLE) {
                    editItem.setVisibility(View.VISIBLE);
                    if (animated) {
                        editItem.setAlpha(0);
                        editItem.animate().alpha(1f).setDuration(150).start();
                    }
                }
            } else {
                if (editItem.getVisibility() != View.GONE) {
                    editItem.setVisibility(View.GONE);
                }
            }
        }
        if (avatarsViewPagerIndicatorView != null) {
            if (avatarsViewPagerIndicatorView.isIndicatorFullyVisible()) {
                if (editItemVisible) {
                    editItem.setVisibility(View.GONE);
                    editItem.animate().cancel();
                    editItem.setAlpha(1f);
                }
                if (callItemVisible) {
                    callItem.setVisibility(View.GONE);
                    callItem.animate().cancel();
                    callItem.setAlpha(1f);
                }
                if (videoCallItemVisible) {
                    videoCallItem.setVisibility(View.GONE);
                    videoCallItem.animate().cancel();
                    videoCallItem.setAlpha(1f);
                }
            }
        }
        if (sharedMediaLayout != null) {
            sharedMediaLayout.getSearchItem().requestLayout();
        }
        updateStoriesViewBounds(false);
    }

    private void createAutoDeleteItem(Context context) {
        autoDeletePopupWrapper = new AutoDeletePopupWrapper(context, otherItem.getPopupLayout().getSwipeBack(), new AutoDeletePopupWrapper.Callback() {

            @Override
            public void dismiss() {
                otherItem.toggleSubMenu();
            }

            @Override
            public void setAutoDeleteHistory(int time, int action) {
                ProfileActivity.this.setAutoDeleteHistory(time, action);
            }

            @Override
            public void showGlobalAutoDeleteScreen() {
                presentFragment(new AutoDeleteMessagesActivity());
                dismiss();
            }
        }, false, 0, resourcesProvider);
        if (dialogId > 0 || userId > 0) {
            int linkColor = dontApplyPeerColor(getThemedColor(Theme.key_windowBackgroundWhiteBlueText), false);
            autoDeletePopupWrapper.allowExtendedHint(linkColor);
        }
        int ttl = 0;
        if (userInfo != null || chatInfo != null) {
            ttl = userInfo != null ? userInfo.ttl_period : chatInfo.ttl_period;
        }
        autoDeleteItemDrawable = TimerDrawable.getTtlIcon(ttl);
        autoDeleteItem = otherItem.addSwipeBackItem(0, autoDeleteItemDrawable, LocaleController.getString(R.string.AutoDeletePopupTitle), autoDeletePopupWrapper.windowLayout);
        otherItem.addColoredGap();
        updateAutoDeleteItem();
    }

    @Override
    public Theme.ResourcesProvider getResourceProvider() {
        return resourcesProvider;
    }

    public int getThemedColor(int key) {
        return Theme.getColor(key, resourcesProvider);
    }

    @Override
    public Drawable getThemedDrawable(String drawableKey) {
        Drawable drawable = resourcesProvider != null ? resourcesProvider.getDrawable(drawableKey) : null;
        return drawable != null ? drawable : super.getThemedDrawable(drawableKey);
    }

    private void setAutoDeleteHistory(int time, int action) {
        long did = getDialogId();
        getMessagesController().setDialogHistoryTTL(did, time);
        if (userInfo != null || chatInfo != null) {
            undoView.showWithAction(did, action, getMessagesController().getUser(did), userInfo != null ? userInfo.ttl_period : chatInfo.ttl_period, null, null);
        }
    }

    @Override
    protected void onDialogDismiss(Dialog dialog) {
        if (listView != null) {
            listView.invalidateViews();
        }
    }

    @Override
    public boolean didSelectDialogs(DialogsActivity fragment, ArrayList<MessagesStorage.TopicKey> dids, CharSequence message, boolean param, boolean notify, int scheduleDate, TopicsFragment topicsFragment) {
        long did = dids.get(0).dialogId;
        Bundle args = new Bundle();
        args.putBoolean("scrollToTopOnResume", true);
        if (DialogObject.isEncryptedDialog(did)) {
            args.putInt("enc_id", DialogObject.getEncryptedChatId(did));
        } else if (DialogObject.isUserDialog(did)) {
            args.putLong("user_id", did);
        } else if (DialogObject.isChatDialog(did)) {
            args.putLong("chat_id", -did);
        }
        if (!getMessagesController().checkCanOpenChat(args, fragment)) {
            return false;
        }

        getNotificationCenter().removeObserver(this, NotificationCenter.closeChats);
        getNotificationCenter().postNotificationName(NotificationCenter.closeChats);
        presentFragment(new ChatActivity(args), true);
        removeSelfFromStack();
        TLRPC.User user = getMessagesController().getUser(userId);
        getSendMessagesHelper().sendMessage(SendMessagesHelper.SendMessageParams.of(user, did, null, null, null, null, notify, scheduleDate));
        if (!TextUtils.isEmpty(message)) {
            AccountInstance accountInstance = AccountInstance.getInstance(currentAccount);
            SendMessagesHelper.prepareSendingText(accountInstance, message.toString(), did, notify, scheduleDate, 0);
        }
        return true;
    }

    @Override
    public void onRequestPermissionsResultFragment(int requestCode, String[] permissions, int[] grantResults) {
        if (imageUpdater != null) {
            imageUpdater.onRequestPermissionsResultFragment(requestCode, permissions, grantResults);
        }
        if (requestCode == 101 || requestCode == 102) {
            final TLRPC.User user = getMessagesController().getUser(userId);
            if (user == null) {
                return;
            }
            boolean allGranted = true;
            for (int a = 0; a < grantResults.length; a++) {
                if (grantResults[a] != PackageManager.PERMISSION_GRANTED) {
                    allGranted = false;
                    break;
                }
            }
            if (grantResults.length > 0 && allGranted) {
                VoIPHelper.startCall(user, requestCode == 102, userInfo != null && userInfo.video_calls_available, getParentActivity(), userInfo, getAccountInstance());
            } else {
                VoIPHelper.permissionDenied(getParentActivity(), null, requestCode);
            }
        } else if (requestCode == 103) {
            if (currentChat == null) {
                return;
            }
            boolean allGranted = true;
            for (int a = 0; a < grantResults.length; a++) {
                if (grantResults[a] != PackageManager.PERMISSION_GRANTED) {
                    allGranted = false;
                    break;
                }
            }
            if (grantResults.length > 0 && allGranted) {
                ChatObject.Call call = getMessagesController().getGroupCall(chatId, false);
                VoIPHelper.startCall(currentChat, null, null, call == null, getParentActivity(), ProfileActivity.this, getAccountInstance());
            } else {
                VoIPHelper.permissionDenied(getParentActivity(), null, requestCode);
            }
        }
    }

    @Override
    public void dismissCurrentDialog() {
        if (imageUpdater != null && imageUpdater.dismissCurrentDialog(visibleDialog)) {
            return;
        }
        super.dismissCurrentDialog();
    }

    @Override
    public boolean dismissDialogOnPause(Dialog dialog) {
        return (imageUpdater == null || imageUpdater.dismissDialogOnPause(dialog)) && super.dismissDialogOnPause(dialog);
    }

    private Animator searchExpandTransition(boolean enter) {
        if (enter) {
            AndroidUtilities.requestAdjustResize(getParentActivity(), classGuid);
            AndroidUtilities.setAdjustResizeToNothing(getParentActivity(), classGuid);
        }
        if (searchViewTransition != null) {
            searchViewTransition.removeAllListeners();
            searchViewTransition.cancel();
        }
        ValueAnimator valueAnimator = ValueAnimator.ofFloat(searchTransitionProgress, enter ? 0f : 1f);
        float offset = extraHeight;
        searchListView.setTranslationY(offset);
        searchListView.setVisibility(View.VISIBLE);
        searchItem.setVisibility(View.VISIBLE);

        listView.setVisibility(View.VISIBLE);

        needLayout(true);

        avatarContainer.setVisibility(View.VISIBLE);
        nameTextView[1].setVisibility(View.VISIBLE);
        onlineTextView[1].setVisibility(View.VISIBLE);
        onlineTextView[3].setVisibility(View.VISIBLE);

        actionBar.onSearchFieldVisibilityChanged(searchTransitionProgress > 0.5f);
        int itemVisibility = searchTransitionProgress > 0.5f ? View.VISIBLE : View.GONE;
        if (otherItem != null) {
            otherItem.setVisibility(itemVisibility);
        }
        if (qrItem != null) {
            updateQrItemVisibility(false);
        }
        searchItem.setVisibility(itemVisibility);

        searchItem.getSearchContainer().setVisibility(searchTransitionProgress > 0.5f ? View.GONE : View.VISIBLE);
        searchListView.setEmptyView(emptyView);
        avatarContainer.setClickable(false);

        valueAnimator.addUpdateListener(animation -> {
            searchTransitionProgress = (float) valueAnimator.getAnimatedValue();
            float progressHalf = (searchTransitionProgress - 0.5f) / 0.5f;
            float progressHalfEnd = (0.5f - searchTransitionProgress) / 0.5f;
            if (progressHalf < 0) {
                progressHalf = 0f;
            }
            if (progressHalfEnd < 0) {
                progressHalfEnd = 0f;
            }

            searchTransitionOffset = (int) (-offset * (1f - searchTransitionProgress));
            searchListView.setTranslationY(offset * searchTransitionProgress);
            emptyView.setTranslationY(offset * searchTransitionProgress);
            listView.setTranslationY(-offset * (1f - searchTransitionProgress));

            listView.setScaleX(1f - 0.01f * (1f - searchTransitionProgress));
            listView.setScaleY(1f - 0.01f * (1f - searchTransitionProgress));
            listView.setAlpha(searchTransitionProgress);
            needLayout(true);

            listView.setAlpha(progressHalf);

            searchListView.setAlpha(1f - searchTransitionProgress);
            searchListView.setScaleX(1f + 0.05f * searchTransitionProgress);
            searchListView.setScaleY(1f + 0.05f * searchTransitionProgress);
            emptyView.setAlpha(1f - progressHalf);

            avatarContainer.setAlpha(progressHalf);
            if (storyView != null) {
                storyView.setAlpha(progressHalf);
            }
            nameTextView[1].setAlpha(progressHalf);
            onlineTextView[1].setAlpha(progressHalf);
            onlineTextView[3].setAlpha(progressHalf);

            searchItem.getSearchField().setAlpha(progressHalfEnd);
            if (enter && searchTransitionProgress < 0.7f) {
                searchItem.requestFocusOnSearchView();
            }

            searchItem.getSearchContainer().setVisibility(searchTransitionProgress < 0.5f ? View.VISIBLE : View.GONE);
            int visibility = searchTransitionProgress > 0.5f ? View.VISIBLE : View.GONE;
            if (otherItem != null) {
                otherItem.setVisibility(visibility);
                otherItem.setAlpha(progressHalf);
            }
            if (qrItem != null) {
                qrItem.setAlpha(progressHalf);
                updateQrItemVisibility(false);
            }
            searchItem.setVisibility(visibility);

            actionBar.onSearchFieldVisibilityChanged(searchTransitionProgress < 0.5f);

            if (otherItem != null) {
                otherItem.setAlpha(progressHalf);
            }
            if (qrItem != null) {
                qrItem.setAlpha(progressHalf);
            }
            searchItem.setAlpha(progressHalf);
            topView.invalidate();
            fragmentView.invalidate();
        });

        valueAnimator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                updateSearchViewState(enter);
                avatarContainer.setClickable(true);
                if (enter) {
                    searchItem.requestFocusOnSearchView();
                }
                needLayout(true);
                searchViewTransition = null;
                fragmentView.invalidate();

                if (enter) {
                    invalidateScroll = true;
                    saveScrollPosition();
                    AndroidUtilities.requestAdjustResize(getParentActivity(), classGuid);
                    emptyView.setPreventMoving(false);
                }
            }
        });

        if (!enter) {
            invalidateScroll = true;
            saveScrollPosition();
            AndroidUtilities.requestAdjustNothing(getParentActivity(), classGuid);
            emptyView.setPreventMoving(true);
        }

        valueAnimator.setDuration(220);
        valueAnimator.setInterpolator(CubicBezierInterpolator.DEFAULT);
        searchViewTransition = valueAnimator;
        return valueAnimator;
    }

    private void updateSearchViewState(boolean enter) {
        int hide = enter ? View.GONE : View.VISIBLE;
        listView.setVisibility(hide);
        searchListView.setVisibility(enter ? View.VISIBLE : View.GONE);
        searchItem.getSearchContainer().setVisibility(enter ? View.VISIBLE : View.GONE);

        actionBar.onSearchFieldVisibilityChanged(enter);

        avatarContainer.setVisibility(hide);
        if (storyView != null) {
            storyView.setVisibility(hide);
        }
        nameTextView[1].setVisibility(hide);
        onlineTextView[1].setVisibility(hide);
        onlineTextView[3].setVisibility(hide);

        if (otherItem != null) {
            otherItem.setAlpha(1f);
            otherItem.setVisibility(hide);
        }
        if (qrItem != null) {
            qrItem.setAlpha(1f);
            qrItem.setVisibility(enter || !isQrNeedVisible() ? View.GONE : View.VISIBLE);
        }
        searchItem.setVisibility(hide);

        avatarContainer.setAlpha(1f);
        if (storyView != null) {
            storyView.setAlpha(1f);
        }
        if (giftsView != null) {
            giftsView.setAlpha(1f);
        }
        nameTextView[1].setAlpha(1f);
        onlineTextView[1].setAlpha(1f);
        searchItem.setAlpha(1f);
        listView.setAlpha(1f);
        searchListView.setAlpha(1f);
        emptyView.setAlpha(1f);
        if (enter) {
            searchListView.setEmptyView(emptyView);
        } else {
            emptyView.setVisibility(View.GONE);
        }
    }

    @Override
    public void onUploadProgressChanged(float progress) {
        if (avatarProgressView == null) {
            return;
        }
        avatarProgressView.setProgress(progress);
        avatarsViewPager.setUploadProgress(uploadingImageLocation, progress);
    }

    @Override
    public void didStartUpload(boolean fromAvatarConstructor, boolean isVideo) {
        if (avatarProgressView == null) {
            return;
        }
        avatarProgressView.setProgress(0.0f);
    }

    public int avatarUploadingRequest;
    @Override
    public void didUploadPhoto(final TLRPC.InputFile photo, final TLRPC.InputFile video, double videoStartTimestamp, String videoPath, TLRPC.PhotoSize bigSize, final TLRPC.PhotoSize smallSize, boolean isVideo, TLRPC.VideoSize emojiMarkup) {
        AndroidUtilities.runOnUIThread(() -> {
            if (photo != null || video != null || emojiMarkup != null) {
                if (avatar == null) {
                    return;
                }
                TLRPC.TL_photos_uploadProfilePhoto req = new TLRPC.TL_photos_uploadProfilePhoto();
                if (photo != null) {
                    req.file = photo;
                    req.flags |= 1;
                }
                if (video != null) {
                    req.video = video;
                    req.flags |= 2;
                    req.video_start_ts = videoStartTimestamp;
                    req.flags |= 4;
                }
                if (emojiMarkup != null) {
                    req.video_emoji_markup = emojiMarkup;
                    req.flags |= 16;
                }
                avatarUploadingRequest = getConnectionsManager().sendRequest(req, (response, error) -> AndroidUtilities.runOnUIThread(() -> {
                    if (error == null) {
                        TLRPC.User user = getMessagesController().getUser(getUserConfig().getClientUserId());
                        if (user == null) {
                            user = getUserConfig().getCurrentUser();
                            if (user == null) {
                                return;
                            }
                            getMessagesController().putUser(user, false);
                        } else {
                            getUserConfig().setCurrentUser(user);
                        }

                        TLRPC.TL_photos_photo photos_photo = (TLRPC.TL_photos_photo) response;
                        ArrayList<TLRPC.PhotoSize> sizes = photos_photo.photo.sizes;
                        TLRPC.PhotoSize small = FileLoader.getClosestPhotoSizeWithSize(sizes, 150);
                        TLRPC.PhotoSize big = FileLoader.getClosestPhotoSizeWithSize(sizes, 800);
                        TLRPC.VideoSize videoSize = photos_photo.photo.video_sizes.isEmpty() ? null : FileLoader.getClosestVideoSizeWithSize(photos_photo.photo.video_sizes, 1000);
                        user.photo = new TLRPC.TL_userProfilePhoto();
                        user.photo.photo_id = photos_photo.photo.id;
                        if (small != null) {
                            user.photo.photo_small = small.location;
                        }
                        if (big != null) {
                            user.photo.photo_big = big.location;
                        }

                        if (small != null && avatar != null) {
                            File destFile = FileLoader.getInstance(currentAccount).getPathToAttach(small, true);
                            File src = FileLoader.getInstance(currentAccount).getPathToAttach(avatar, true);
                            src.renameTo(destFile);
                            String oldKey = avatar.volume_id + "_" + avatar.local_id + "@50_50";
                            String newKey = small.location.volume_id + "_" + small.location.local_id + "@50_50";
                            ImageLoader.getInstance().replaceImageInCache(oldKey, newKey, ImageLocation.getForUserOrChat(user, ImageLocation.TYPE_SMALL), false);
                        }

                        if (videoSize != null && videoPath != null) {
                            File destFile = FileLoader.getInstance(currentAccount).getPathToAttach(videoSize, "mp4", true);
                            File src = new File(videoPath);
                            src.renameTo(destFile);
                        } else if (big != null && avatarBig != null) {
                            File destFile = FileLoader.getInstance(currentAccount).getPathToAttach(big, true);
                            File src = FileLoader.getInstance(currentAccount).getPathToAttach(avatarBig, true);
                            src.renameTo(destFile);
                        }
                        getMessagesController().getDialogPhotos(user.id).addPhotoAtStart(((TLRPC.TL_photos_photo) response).photo);
                        ArrayList<TLRPC.User> users = new ArrayList<>();
                        users.add(user);
                        getMessagesStorage().putUsersAndChats(users, null, false, true);
                        TLRPC.UserFull userFull = getMessagesController().getUserFull(userId);
                        if (userFull != null) {
                            userFull.profile_photo = photos_photo.photo;
                            getMessagesStorage().updateUserInfo(userFull, false);
                        }
                    }

                    allowPullingDown = !AndroidUtilities.isTablet() && !isInLandscapeMode && avatarImage.getImageReceiver().hasNotThumb() && !AndroidUtilities.isAccessibilityScreenReaderEnabled();
                    avatar = null;
                    avatarBig = null;
                    avatarsViewPager.scrolledByUser = true;
                    avatarsViewPager.removeUploadingImage(uploadingImageLocation);
                    avatarsViewPager.setCreateThumbFromParent(false);
                    updateProfileData(true);
                    showAvatarProgress(false, true);
                    getNotificationCenter().postNotificationName(NotificationCenter.updateInterfaces, MessagesController.UPDATE_MASK_ALL);
                    getNotificationCenter().postNotificationName(NotificationCenter.mainUserInfoChanged);
                    getUserConfig().saveConfig(true);

                }));
            } else {
                avatar = smallSize.location;
                avatarBig = bigSize.location;
                avatarImage.setImage(ImageLocation.getForLocal(avatar), "50_50", avatarDrawable, null);
                if (setAvatarRow != -1) {
                    updateRowsIds();
                    if (listAdapter != null) {
                        listAdapter.notifyDataSetChanged();
                    }
                    needLayout(true);
                }
                avatarsViewPager.addUploadingImage(uploadingImageLocation = ImageLocation.getForLocal(avatarBig), ImageLocation.getForLocal(avatar));
                showAvatarProgress(true, false);
            }
            actionBar.createMenu().requestLayout();
        });
    }

    public void showAvatarProgress(boolean show, boolean animated) {
        if (avatarProgressView == null) {
            return;
        }
        if (avatarAnimation != null) {
            avatarAnimation.cancel();
            avatarAnimation = null;
        }
        if (animated) {
            avatarAnimation = new AnimatorSet();
            if (show) {
                avatarProgressView.setVisibility(View.VISIBLE);
                avatarAnimation.playTogether(ObjectAnimator.ofFloat(avatarProgressView, View.ALPHA, 1.0f));
            } else {
                avatarAnimation.playTogether(ObjectAnimator.ofFloat(avatarProgressView, View.ALPHA, 0.0f));
            }
            avatarAnimation.setDuration(180);
            avatarAnimation.addListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    if (avatarAnimation == null || avatarProgressView == null) {
                        return;
                    }
                    if (!show) {
                        avatarProgressView.setVisibility(View.INVISIBLE);
                    }
                    avatarAnimation = null;
                }

                @Override
                public void onAnimationCancel(Animator animation) {
                    avatarAnimation = null;
                }
            });
            avatarAnimation.start();
        } else {
            if (show) {
                avatarProgressView.setAlpha(1.0f);
                avatarProgressView.setVisibility(View.VISIBLE);
            } else {
                avatarProgressView.setAlpha(0.0f);
                avatarProgressView.setVisibility(View.INVISIBLE);
            }
        }
    }

    @Override
    public void onActivityResultFragment(int requestCode, int resultCode, Intent data) {
        if (imageUpdater != null) {
            imageUpdater.onActivityResult(requestCode, resultCode, data);
        }
    }

    @Override
    public void saveSelfArgs(Bundle args) {
        if (imageUpdater != null && imageUpdater.currentPicturePath != null) {
            args.putString("path", imageUpdater.currentPicturePath);
        }
    }

    @Override
    public void restoreSelfArgs(Bundle args) {
        if (imageUpdater != null) {
            imageUpdater.currentPicturePath = args.getString("path");
        }
    }

    public static void sendLogs(Activity activity, boolean last) {
        if (activity == null) {
            return;
        }
        AlertDialog progressDialog = new AlertDialog(activity, AlertDialog.ALERT_TYPE_SPINNER);
        progressDialog.setCanCancel(false);
        progressDialog.show();
        Utilities.globalQueue.postRunnable(() -> {
            try {
                File dir = AndroidUtilities.getLogsDir();
                if (dir == null) {
                    AndroidUtilities.runOnUIThread(progressDialog::dismiss);
                    return;
                }

                File zipFile = new File(dir, "logs.zip");
                if (zipFile.exists()) {
                    zipFile.delete();
                }

                ArrayList<File> files = new ArrayList<>();

                File[] logFiles = dir.listFiles();
                for (File f : logFiles) {
                    files.add(f);
                }

                File filesDir = ApplicationLoader.getFilesDirFixed();
                filesDir = new File(filesDir, "malformed_database/");
                if (filesDir.exists() && filesDir.isDirectory()) {
                    File[] malformedDatabaseFiles = filesDir.listFiles();
                    for (File file : malformedDatabaseFiles) {
                        files.add(file);
                    }
                }

                boolean[] finished = new boolean[1];
                long currentDate = System.currentTimeMillis();

                BufferedInputStream origin = null;
                ZipOutputStream out = null;
                try {
                    FileOutputStream dest = new FileOutputStream(zipFile);
                    out = new ZipOutputStream(new BufferedOutputStream(dest));
                    byte[] data = new byte[1024 * 64];

                    for (int i = 0; i < files.size(); i++) {
                        File file = files.get(i);
                        if (!file.getName().contains("cache4") && (last || file.getName().contains("_mtproto")) && (currentDate - file.lastModified()) > 24 * 60 * 60 * 1000) {
                            continue;
                        }
                        if (!file.exists()) {
                            continue;
                        }
                        FileInputStream fi = new FileInputStream(file);
                        origin = new BufferedInputStream(fi, data.length);

                        ZipEntry entry = new ZipEntry(file.getName());
                        out.putNextEntry(entry);
                        int count;
                        while ((count = origin.read(data, 0, data.length)) != -1) {
                            out.write(data, 0, count);
                        }
                        origin.close();
                        origin = null;
                    }
                    finished[0] = true;
                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    if (origin != null) {
                        origin.close();
                    }
                    if (out != null) {
                        out.close();
                    }
                }

                AndroidUtilities.runOnUIThread(() -> {
                    try {
                        progressDialog.dismiss();
                    } catch (Exception ignore) {

                    }
                    if (finished[0]) {
                        Uri uri;
                        if (Build.VERSION.SDK_INT >= 24) {
                            uri = FileProvider.getUriForFile(activity, ApplicationLoader.getApplicationId() + ".provider", zipFile);
                        } else {
                            uri = Uri.fromFile(zipFile);
                        }

                        Intent i = new Intent(Intent.ACTION_SEND);
                        if (Build.VERSION.SDK_INT >= 24) {
                            i.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                        }
                        i.setType("message/rfc822");
                        i.putExtra(Intent.EXTRA_EMAIL, "");
                        i.putExtra(Intent.EXTRA_SUBJECT, "Logs from " + LocaleController.getInstance().getFormatterStats().format(System.currentTimeMillis()));
                        i.putExtra(Intent.EXTRA_STREAM, uri);
                        if (activity != null) {
                            try {
                                activity.startActivityForResult(Intent.createChooser(i, "Select email application."), 500);
                            } catch (Exception e) {
                                FileLog.e(e);
                            }
                        }
                    } else {
                        if (activity != null) {
                            Toast.makeText(activity, LocaleController.getString(R.string.ErrorOccurred), Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

    public void openUrl(String url, Browser.Progress progress) {
        if (url.startsWith("@")) {
            getMessagesController().openByUserName(url.substring(1), ProfileActivity.this, 0, progress);
        } else if (url.startsWith("#") || url.startsWith("$")) {
            DialogsActivity fragment = new DialogsActivity(null);
            fragment.setSearchString(url);
            presentFragment(fragment);
        } else if (url.startsWith("/")) {
            if (parentLayout.getFragmentStack().size() > 1) {
                BaseFragment previousFragment = parentLayout.getFragmentStack().get(parentLayout.getFragmentStack().size() - 2);
                if (previousFragment instanceof ChatActivity) {
                    finishFragment();
                    ((ChatActivity) previousFragment).getChatActivityEnterView().setCommand(null, url, false, false);
                }
            }
        }
    }

    private void dimBehindView(View view, boolean enable) {
        scrimView = view;
        dimBehindView(enable);
    }

    private void dimBehindView(View view, float value) {
        scrimView = view;
        dimBehindView(value);
    }

    private void dimBehindView(boolean enable) {
        dimBehindView(enable ? 0.2f : 0);
    }

    private AnimatorSet scrimAnimatorSet = null;

    private void dimBehindView(float value) {
        boolean enable = value > 0;
        fragmentView.invalidate();
        if (scrimAnimatorSet != null) {
            scrimAnimatorSet.cancel();
        }
        scrimAnimatorSet = new AnimatorSet();
        ArrayList<Animator> animators = new ArrayList<>();
        ValueAnimator scrimPaintAlphaAnimator;
        if (enable) {
            animators.add(scrimPaintAlphaAnimator = ValueAnimator.ofFloat(0, value));
        } else {
            animators.add(scrimPaintAlphaAnimator = ValueAnimator.ofFloat(scrimPaint.getAlpha() / 255f, 0));
        }
        scrimPaintAlphaAnimator.addUpdateListener(a -> {
            scrimPaint.setAlpha((int) (255 * (float) a.getAnimatedValue()));
        });
        scrimAnimatorSet.playTogether(animators);
        scrimAnimatorSet.setDuration(enable ? 150 : 220);
        if (!enable) {
            scrimAnimatorSet.addListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    scrimView = null;
                    fragmentView.invalidate();
                }
            });
        }
        scrimAnimatorSet.start();
    }

    @Override
    public ArrayList<ThemeDescription> getThemeDescriptions() {
        if (resourcesProvider != null) {
            return null;
        }
        ThemeDescription.ThemeDescriptionDelegate themeDelegate = () -> {
            if (listView != null) {
                int count = listView.getChildCount();
                for (int a = 0; a < count; a++) {
                    View child = listView.getChildAt(a);
                    if (child instanceof UserCell) {
                        ((UserCell) child).update(0);
                    }
                }
            }
            if (!isPulledDown) {
                if (onlineTextView[1] != null) {
                    final Object onlineTextViewTag = onlineTextView[1].getTag();
                    for (int i = 0; i < 2; i++) {
                        if (onlineTextViewTag instanceof Integer) {
                            onlineTextView[i + 1].setTextColor(applyPeerColor(getThemedColor((Integer) onlineTextViewTag), true, isOnline[0]));
                        } else {
                            onlineTextView[i + 1].setTextColor(applyPeerColor(getThemedColor(Theme.key_avatar_subtitleInProfileBlue), true, true));
                        }
                    }
                }
                if (lockIconDrawable != null) {
                    lockIconDrawable.setColorFilter(getThemedColor(Theme.key_chat_lockIcon), PorterDuff.Mode.MULTIPLY);
                }
                if (scamDrawable != null) {
                    scamDrawable.setColor(getThemedColor(Theme.key_avatar_subtitleInProfileBlue));
                }
                if (nameTextView[1] != null) {
                    nameTextView[1].setTextColor(getThemedColor(Theme.key_profile_title));
                }
                if (actionBar != null) {
                    actionBar.setItemsColor(peerColor != null ? Color.WHITE : getThemedColor(Theme.key_actionBarDefaultIcon), false);
                    actionBar.setItemsBackgroundColor(peerColor != null ? 0x20ffffff : getThemedColor(Theme.key_avatar_actionBarSelectorBlue), false);
                }
            }
            updateEmojiStatusDrawableColor();
        };
        ArrayList<ThemeDescription> arrayList = new ArrayList<>();
        if (sharedMediaLayout != null) {
            arrayList.addAll(sharedMediaLayout.getThemeDescriptions());
        }

        arrayList.add(new ThemeDescription(listView, 0, null, null, null, null, Theme.key_windowBackgroundWhite));
        arrayList.add(new ThemeDescription(searchListView, 0, null, null, null, null, Theme.key_windowBackgroundWhite));
        arrayList.add(new ThemeDescription(listView, 0, null, null, null, null, Theme.key_windowBackgroundGray));
        arrayList.add(new ThemeDescription(actionBar, ThemeDescription.FLAG_AB_SUBMENUBACKGROUND, null, null, null, null, Theme.key_actionBarDefaultSubmenuBackground));
        arrayList.add(new ThemeDescription(actionBar, ThemeDescription.FLAG_AB_SUBMENUITEM, null, null, null, null, Theme.key_actionBarDefaultSubmenuItem));
        arrayList.add(new ThemeDescription(actionBar, ThemeDescription.FLAG_AB_SUBMENUITEM | ThemeDescription.FLAG_IMAGECOLOR, null, null, null, null, Theme.key_actionBarDefaultSubmenuItemIcon));
        arrayList.add(new ThemeDescription(null, 0, null, null, null, themeDelegate, Theme.key_actionBarDefaultIcon));
        arrayList.add(new ThemeDescription(null, 0, null, null, null, themeDelegate, Theme.key_avatar_actionBarSelectorBlue));
        arrayList.add(new ThemeDescription(null, 0, null, null, null, themeDelegate, Theme.key_chat_lockIcon));
        arrayList.add(new ThemeDescription(null, 0, null, null, null, themeDelegate, Theme.key_avatar_subtitleInProfileBlue));
        arrayList.add(new ThemeDescription(null, 0, null, null, null, themeDelegate, Theme.key_avatar_backgroundActionBarBlue));
        arrayList.add(new ThemeDescription(null, 0, null, null, null, themeDelegate, Theme.key_profile_title));
        arrayList.add(new ThemeDescription(null, 0, null, null, null, themeDelegate, Theme.key_profile_status));
        arrayList.add(new ThemeDescription(null, 0, null, null, null, themeDelegate, Theme.key_avatar_subtitleInProfileBlue));

        if (mediaCounterTextView != null) {
            arrayList.add(new ThemeDescription(mediaCounterTextView.getTextView(), ThemeDescription.FLAG_TEXTCOLOR, null, null, null, themeDelegate, Theme.key_player_actionBarSubtitle));
            arrayList.add(new ThemeDescription(mediaCounterTextView.getNextTextView(), ThemeDescription.FLAG_TEXTCOLOR, null, null, null, themeDelegate, Theme.key_player_actionBarSubtitle));
        }

        arrayList.add(new ThemeDescription(topView, ThemeDescription.FLAG_BACKGROUND, null, null, null, null, Theme.key_avatar_backgroundActionBarBlue));
        arrayList.add(new ThemeDescription(listView, ThemeDescription.FLAG_SELECTOR, null, null, null, null, Theme.key_listSelector));
        arrayList.add(new ThemeDescription(listView, 0, new Class[]{View.class}, Theme.dividerPaint, null, null, Theme.key_divider));

        arrayList.add(new ThemeDescription(avatarImage, 0, null, null, Theme.avatarDrawables, null, Theme.key_avatar_text));
        arrayList.add(new ThemeDescription(avatarImage, 0, null, null, new Drawable[]{avatarDrawable}, null, Theme.key_avatar_backgroundInProfileBlue));

        arrayList.add(new ThemeDescription(writeButton, ThemeDescription.FLAG_IMAGECOLOR, null, null, null, null, Theme.key_profile_actionIcon));
        arrayList.add(new ThemeDescription(writeButton, ThemeDescription.FLAG_BACKGROUNDFILTER, null, null, null, null, Theme.key_profile_actionBackground));
        arrayList.add(new ThemeDescription(writeButton, ThemeDescription.FLAG_BACKGROUNDFILTER | ThemeDescription.FLAG_DRAWABLESELECTEDSTATE, null, null, null, null, Theme.key_profile_actionPressedBackground));

        arrayList.add(new ThemeDescription(listView, ThemeDescription.FLAG_CHECKTAG, new Class[]{TextCell.class}, new String[]{"textView"}, null, null, null, Theme.key_windowBackgroundWhiteBlackText));
        arrayList.add(new ThemeDescription(listView, ThemeDescription.FLAG_CHECKTAG, new Class[]{TextCell.class}, new String[]{"textView"}, null, null, null, Theme.key_windowBackgroundWhiteGreenText2));
        arrayList.add(new ThemeDescription(listView, ThemeDescription.FLAG_CHECKTAG, new Class[]{TextCell.class}, new String[]{"textView"}, null, null, null, Theme.key_text_RedRegular));
        arrayList.add(new ThemeDescription(listView, ThemeDescription.FLAG_CHECKTAG, new Class[]{TextCell.class}, new String[]{"textView"}, null, null, null, Theme.key_windowBackgroundWhiteBlueText2));
        arrayList.add(new ThemeDescription(listView, ThemeDescription.FLAG_CHECKTAG, new Class[]{TextCell.class}, new String[]{"textView"}, null, null, null, Theme.key_windowBackgroundWhiteBlueButton));
        arrayList.add(new ThemeDescription(listView, 0, new Class[]{TextCell.class}, new String[]{"valueTextView"}, null, null, null, Theme.key_windowBackgroundWhiteValueText));
        arrayList.add(new ThemeDescription(listView, ThemeDescription.FLAG_CHECKTAG, new Class[]{TextCell.class}, new String[]{"imageView"}, null, null, null, Theme.key_windowBackgroundWhiteGrayIcon));
        arrayList.add(new ThemeDescription(listView, ThemeDescription.FLAG_CHECKTAG, new Class[]{TextCell.class}, new String[]{"imageView"}, null, null, null, Theme.key_windowBackgroundWhiteBlueIcon));

        arrayList.add(new ThemeDescription(listView, 0, new Class[]{TextDetailCell.class}, new String[]{"textView"}, null, null, null, Theme.key_windowBackgroundWhiteBlackText));
        arrayList.add(new ThemeDescription(listView, 0, new Class[]{TextDetailCell.class}, new String[]{"valueTextView"}, null, null, null, Theme.key_windowBackgroundWhiteGrayText2));

        arrayList.add(new ThemeDescription(listView, 0, new Class[]{HeaderCell.class}, new String[]{"textView"}, null, null, null, Theme.key_windowBackgroundWhiteBlueHeader));

        arrayList.add(new ThemeDescription(listView, 0, new Class[]{SettingsSuggestionCell.class}, new String[]{"textView"}, null, null, null, Theme.key_windowBackgroundWhiteBlueHeader));
        arrayList.add(new ThemeDescription(listView, 0, new Class[]{SettingsSuggestionCell.class}, new String[]{"detailTextView"}, null, null, null, Theme.key_windowBackgroundWhiteGrayText2));
        arrayList.add(new ThemeDescription(listView, ThemeDescription.FLAG_LINKCOLOR, new Class[]{SettingsSuggestionCell.class}, new String[]{"detailTextView"}, null, null, null, Theme.key_windowBackgroundWhiteLinkText));
        arrayList.add(new ThemeDescription(listView, 0, new Class[]{SettingsSuggestionCell.class}, new String[]{"yesButton"}, null, null, null, Theme.key_featuredStickers_buttonText));
        arrayList.add(new ThemeDescription(listView, ThemeDescription.FLAG_USEBACKGROUNDDRAWABLE, new Class[]{SettingsSuggestionCell.class}, new String[]{"yesButton"}, null, null, null, Theme.key_featuredStickers_addButton));
        arrayList.add(new ThemeDescription(listView, ThemeDescription.FLAG_USEBACKGROUNDDRAWABLE | ThemeDescription.FLAG_DRAWABLESELECTEDSTATE, new Class[]{SettingsSuggestionCell.class}, new String[]{"yesButton"}, null, null, null, Theme.key_featuredStickers_addButtonPressed));
        arrayList.add(new ThemeDescription(listView, 0, new Class[]{SettingsSuggestionCell.class}, new String[]{"noButton"}, null, null, null, Theme.key_featuredStickers_buttonText));
        arrayList.add(new ThemeDescription(listView, ThemeDescription.FLAG_USEBACKGROUNDDRAWABLE, new Class[]{SettingsSuggestionCell.class}, new String[]{"noButton"}, null, null, null, Theme.key_featuredStickers_addButton));
        arrayList.add(new ThemeDescription(listView, ThemeDescription.FLAG_USEBACKGROUNDDRAWABLE | ThemeDescription.FLAG_DRAWABLESELECTEDSTATE, new Class[]{SettingsSuggestionCell.class}, new String[]{"noButton"}, null, null, null, Theme.key_featuredStickers_addButtonPressed));

        arrayList.add(new ThemeDescription(listView, 0, new Class[]{NotificationsCheckCell.class}, new String[]{"textView"}, null, null, null, Theme.key_windowBackgroundWhiteBlackText));
        arrayList.add(new ThemeDescription(listView, 0, new Class[]{NotificationsCheckCell.class}, new String[]{"valueTextView"}, null, null, null, Theme.key_windowBackgroundWhiteGrayText2));
        arrayList.add(new ThemeDescription(listView, 0, new Class[]{NotificationsCheckCell.class}, new String[]{"checkBox"}, null, null, null, Theme.key_switchTrack));
        arrayList.add(new ThemeDescription(listView, 0, new Class[]{NotificationsCheckCell.class}, new String[]{"checkBox"}, null, null, null, Theme.key_switchTrackChecked));

        arrayList.add(new ThemeDescription(listView, ThemeDescription.FLAG_TEXTCOLOR, new Class[]{UserCell.class}, new String[]{"adminTextView"}, null, null, null, Theme.key_profile_creatorIcon));
        arrayList.add(new ThemeDescription(listView, 0, new Class[]{UserCell.class}, new String[]{"imageView"}, null, null, null, Theme.key_windowBackgroundWhiteGrayIcon));
        arrayList.add(new ThemeDescription(listView, 0, new Class[]{UserCell.class}, new String[]{"nameTextView"}, null, null, null, Theme.key_windowBackgroundWhiteBlackText));
        arrayList.add(new ThemeDescription(listView, 0, new Class[]{UserCell.class}, new String[]{"statusColor"}, null, null, themeDelegate, Theme.key_windowBackgroundWhiteGrayText));
        arrayList.add(new ThemeDescription(listView, 0, new Class[]{UserCell.class}, new String[]{"statusOnlineColor"}, null, null, themeDelegate, Theme.key_windowBackgroundWhiteBlueText));
        arrayList.add(new ThemeDescription(listView, 0, new Class[]{UserCell.class}, null, Theme.avatarDrawables, null, Theme.key_avatar_text));
        arrayList.add(new ThemeDescription(null, 0, null, null, null, themeDelegate, Theme.key_avatar_backgroundRed));
        arrayList.add(new ThemeDescription(null, 0, null, null, null, themeDelegate, Theme.key_avatar_backgroundOrange));
        arrayList.add(new ThemeDescription(null, 0, null, null, null, themeDelegate, Theme.key_avatar_backgroundViolet));
        arrayList.add(new ThemeDescription(null, 0, null, null, null, themeDelegate, Theme.key_avatar_backgroundGreen));
        arrayList.add(new ThemeDescription(null, 0, null, null, null, themeDelegate, Theme.key_avatar_backgroundCyan));
        arrayList.add(new ThemeDescription(null, 0, null, null, null, themeDelegate, Theme.key_avatar_backgroundBlue));
        arrayList.add(new ThemeDescription(null, 0, null, null, null, themeDelegate, Theme.key_avatar_backgroundPink));

        arrayList.add(new ThemeDescription(undoView, ThemeDescription.FLAG_BACKGROUNDFILTER, null, null, null, null, Theme.key_undo_background));
        arrayList.add(new ThemeDescription(undoView, 0, new Class[]{UndoView.class}, new String[]{"undoImageView"}, null, null, null, Theme.key_undo_cancelColor));
        arrayList.add(new ThemeDescription(undoView, 0, new Class[]{UndoView.class}, new String[]{"undoTextView"}, null, null, null, Theme.key_undo_cancelColor));
        arrayList.add(new ThemeDescription(undoView, 0, new Class[]{UndoView.class}, new String[]{"infoTextView"}, null, null, null, Theme.key_undo_infoColor));
        arrayList.add(new ThemeDescription(undoView, 0, new Class[]{UndoView.class}, new String[]{"textPaint"}, null, null, null, Theme.key_undo_infoColor));
        arrayList.add(new ThemeDescription(undoView, 0, new Class[]{UndoView.class}, new String[]{"progressPaint"}, null, null, null, Theme.key_undo_infoColor));
        arrayList.add(new ThemeDescription(undoView, ThemeDescription.FLAG_IMAGECOLOR, new Class[]{UndoView.class}, new String[]{"leftImageView"}, null, null, null, Theme.key_undo_infoColor));

        arrayList.add(new ThemeDescription(listView, ThemeDescription.FLAG_TEXTCOLOR, new Class[]{AboutLinkCell.class}, Theme.profile_aboutTextPaint, null, null, Theme.key_windowBackgroundWhiteBlackText));
        arrayList.add(new ThemeDescription(listView, ThemeDescription.FLAG_LINKCOLOR, new Class[]{AboutLinkCell.class}, Theme.profile_aboutTextPaint, null, null, Theme.key_windowBackgroundWhiteLinkText));
        arrayList.add(new ThemeDescription(listView, 0, new Class[]{AboutLinkCell.class}, Theme.linkSelectionPaint, null, null, Theme.key_windowBackgroundWhiteLinkSelection));

        arrayList.add(new ThemeDescription(listView, ThemeDescription.FLAG_BACKGROUNDFILTER, new Class[]{ShadowSectionCell.class}, null, null, null, Theme.key_windowBackgroundGrayShadow));

        arrayList.add(new ThemeDescription(listView, ThemeDescription.FLAG_BACKGROUNDFILTER, new Class[]{TextInfoPrivacyCell.class}, null, null, null, Theme.key_windowBackgroundGrayShadow));
        arrayList.add(new ThemeDescription(listView, 0, new Class[]{TextInfoPrivacyCell.class}, new String[]{"textView"}, null, null, null, Theme.key_windowBackgroundWhiteGrayText4));

        arrayList.add(new ThemeDescription(searchListView, 0, new Class[]{HeaderCell.class}, new String[]{"textView"}, null, null, null, Theme.key_windowBackgroundWhiteBlueHeader));

        arrayList.add(new ThemeDescription(searchListView, 0, new Class[]{GraySectionCell.class}, new String[]{"textView"}, null, null, null, Theme.key_graySectionText));
        arrayList.add(new ThemeDescription(searchListView, ThemeDescription.FLAG_CELLBACKGROUNDCOLOR, new Class[]{GraySectionCell.class}, null, null, null, Theme.key_graySection));

        arrayList.add(new ThemeDescription(searchListView, 0, new Class[]{SettingsSearchCell.class}, new String[]{"textView"}, null, null, null, Theme.key_windowBackgroundWhiteBlackText));
        arrayList.add(new ThemeDescription(searchListView, 0, new Class[]{SettingsSearchCell.class}, new String[]{"valueTextView"}, null, null, null, Theme.key_windowBackgroundWhiteGrayText2));
        arrayList.add(new ThemeDescription(searchListView, 0, new Class[]{SettingsSearchCell.class}, new String[]{"imageView"}, null, null, null, Theme.key_windowBackgroundWhiteGrayIcon));

        if (mediaHeaderVisible) {
            arrayList.add(new ThemeDescription(nameTextView[1], 0, null, null, verifiedCheckDrawable, null, Theme.key_player_actionBarTitle));
            arrayList.add(new ThemeDescription(nameTextView[1], 0, null, null, verifiedDrawable, null, Theme.key_windowBackgroundWhite));
        } else {
            arrayList.add(new ThemeDescription(nameTextView[1], 0, null, null, verifiedCheckDrawable, null, Theme.key_profile_verifiedCheck));
            arrayList.add(new ThemeDescription(nameTextView[1], 0, null, null, verifiedDrawable, null, Theme.key_profile_verifiedBackground));
        }

        return arrayList;
    }

    public void updateListAnimated(boolean updateOnlineCount) {
        updateListAnimated(updateOnlineCount, false);
    }

    private void updateListAnimated(boolean updateOnlineCount, boolean triedInLayout) {
        if (listAdapter == null) {
            if (updateOnlineCount) {
                updateOnlineCount(false);
            }
            updateRowsIds();
            return;
        }

        if (!triedInLayout && listView.isInLayout()) {
            if (!listView.isAttachedToWindow()) return;
            listView.post(() -> updateListAnimated(updateOnlineCount, true));
            return;
        }

        DiffCallback diffCallback = new DiffCallback(this);
        diffCallback.oldRowCount = rowCount;
        diffCallback.fillPositions(diffCallback.oldPositionToItem);
        diffCallback.oldChatParticipant.clear();
        diffCallback.oldChatParticipantSorted.clear();
        diffCallback.oldChatParticipant.addAll(visibleChatParticipants);
        diffCallback.oldChatParticipantSorted.addAll(visibleSortedUsers);
        diffCallback.oldMembersStartRow = membersStartRow;
        diffCallback.oldMembersEndRow = membersEndRow;
        if (updateOnlineCount) {
            updateOnlineCount(false);
        }
        saveScrollPosition();
        updateRowsIds();
        diffCallback.fillPositions(diffCallback.newPositionToItem);
        try {
            DiffUtil.calculateDiff(diffCallback).dispatchUpdatesTo(listAdapter);
        } catch (Exception e) {
            FileLog.e(e);
            listAdapter.notifyDataSetChanged();
        }
        if (savedScrollPosition >= 0) {
            layoutManager.scrollToPositionWithOffset(savedScrollPosition, savedScrollOffset - listView.getPaddingTop());
        }
        AndroidUtilities.updateVisibleRows(listView);
    }

    public int savedScrollPosition = -1;
    public int savedScrollOffset;
    boolean savedScrollToSharedMedia;

    public void saveScrollPosition() {
        if (listView != null && layoutManager != null && listView.getChildCount() > 0 && !savedScrollToSharedMedia) {
            View view = null;
            int position = -1;
            int top = Integer.MAX_VALUE;
            for (int i = 0; i < listView.getChildCount(); i++) {
                int childPosition = listView.getChildAdapterPosition(listView.getChildAt(i));
                View child = listView.getChildAt(i);
                if (childPosition != RecyclerListView.NO_POSITION && child.getTop() < top) {
                    view = child;
                    position = childPosition;
                    top = child.getTop();
                }
            }
            if (view != null) {
                savedScrollPosition = position;
                savedScrollOffset = view.getTop();
                if (savedScrollPosition == 0 && !allowPullingDown && savedScrollOffset > AndroidUtilities.dp(88)) {
                    savedScrollOffset = AndroidUtilities.dp(88);
                }

                layoutManager.scrollToPositionWithOffset(position, view.getTop() - listView.getPaddingTop());
            }
        }
    }

    public void scrollToSharedMedia() {
        scrollToSharedMedia(false);
    }
    public void scrollToSharedMedia(boolean animated) {
        if (sharedMediaRow >= 0) {
            if (animated) {
                LinearSmoothScrollerCustom linearSmoothScroller = new LinearSmoothScrollerCustom(getContext(), LinearSmoothScrollerCustom.POSITION_TOP, .6f);
                linearSmoothScroller.setTargetPosition(sharedMediaRow);
                linearSmoothScroller.setOffset(-listView.getPaddingTop());
                layoutManager.startSmoothScroll(linearSmoothScroller);
            } else {
                layoutManager.scrollToPositionWithOffset(sharedMediaRow, -listView.getPaddingTop());
            }
        }
    }

    public void onTextDetailCellImageClicked(View view) {
        View parent = (View) view.getParent();
        if (parent.getTag() != null && ((int) parent.getTag()) == usernameRow) {
            Bundle args = new Bundle();
            args.putLong("chat_id", chatId);
            args.putLong("user_id", userId);
            presentFragment(new QrActivity(args));
        } else if (parent.getTag() != null && ((int) parent.getTag()) == birthdayRow) {
            if (userId == getUserConfig().getClientUserId()) {
                presentFragment(new PremiumPreviewFragment("my_profile_gift"));
                return;
            }
            if (UserObject.areGiftsDisabled(userInfo)) {
                BulletinFactory.of(this).createSimpleBulletin(R.raw.error, AndroidUtilities.replaceTags(LocaleController.formatString(R.string.UserDisallowedGifts, DialogObject.getShortName(userId)))).show();
                return;
            }
            showDialog(new GiftSheet(getContext(), currentAccount, userId, null, null));
        }
    }

    private boolean fullyVisible;

    @Override
    public void onBecomeFullyVisible() {
        super.onBecomeFullyVisible();
        writeButtonSetBackground();
        fullyVisible = true;
        createBirthdayEffect();
    }

    private void writeButtonSetBackground() {
        if (writeButton == null) return;
        try {
            Drawable shadowDrawable = fragmentView.getContext().getResources().getDrawable(R.drawable.floating_shadow_profile).mutate();
            shadowDrawable.setColorFilter(new PorterDuffColorFilter(Color.BLACK, PorterDuff.Mode.MULTIPLY));
            int color1 = getThemedColor(Theme.key_profile_actionBackground);
            int color2 = getThemedColor(Theme.key_profile_actionPressedBackground);
            int iconColor = getThemedColor(Theme.key_profile_actionIcon);
            if (peerColor != null && Theme.hasHue(color1)) {
                color1 = Theme.adaptHSV(peerColor.getBgColor1(false), +.05f, -.04f);
                color2 = applyPeerColor2(color2);
                iconColor = Color.WHITE;
            }
            CombinedDrawable combinedDrawable = new CombinedDrawable(shadowDrawable,
                    Theme.createSimpleSelectorCircleDrawable(AndroidUtilities.dp(56), color1, color2),
                    0, 0);
            combinedDrawable.setIconSize(AndroidUtilities.dp(56), AndroidUtilities.dp(56));
            writeButton.setBackground(combinedDrawable);
            writeButton.setColorFilter(new PorterDuffColorFilter(iconColor, PorterDuff.Mode.MULTIPLY));
        } catch (Exception e) {}
    }

    @SuppressLint("NotifyDataSetChanged")
    public void openAddToContact(TLRPC.User user, Bundle args) {
        ContactAddActivity contactAddActivity = new ContactAddActivity(args, resourcesProvider);
        contactAddActivity.setDelegate(() -> {
            int currentAddToContactsRow = addToContactsRow;
            if (currentAddToContactsRow >= 0) {
                if (sharedMediaRow == -1) {
                    updateRowsIds();
                    listAdapter.notifyDataSetChanged();
                } else {
                    updateListAnimated(false);
                }
            }

            if (sharedMediaRow == -1) {
                if (isInLandscapeMode || AndroidUtilities.isTablet()) {
                    listView.setPadding(0, AndroidUtilities.dp(88), 0, 0);
                    expandAnimator.cancel();
                    expandAnimatorValues[0] = 1f;
                    expandAnimatorValues[1] = 0f;
                    setAvatarExpandProgress(1f);
                    extraHeight = AndroidUtilities.dp(88);
                } else {
                    final int actionBarHeight = ActionBar.getCurrentActionBarHeight() + (actionBar.getOccupyStatusBar() ? AndroidUtilities.statusBarHeight : 0);
                    int ws = View.MeasureSpec.makeMeasureSpec(listView.getMeasuredWidth(), View.MeasureSpec.EXACTLY);
                    int hs = View.MeasureSpec.makeMeasureSpec(listView.getMeasuredHeight(), View.MeasureSpec.UNSPECIFIED);
                    int contentHeight = 0;
                    for (int i = 0; i < listAdapter.getItemCount(); i++) {
                        RecyclerView.ViewHolder holder = listAdapter.createViewHolder(null, listAdapter.getItemViewType(i));
                        listAdapter.onBindViewHolder(holder, i);
                        holder.itemView.measure(ws, hs);
                        contentHeight += holder.itemView.getMeasuredHeight();
                    }
                    int paddingBottom = Math.max(0, fragmentView.getMeasuredHeight() - (contentHeight + AndroidUtilities.dp(88) + actionBarHeight));
                    listView.setPadding(0, listView.getPaddingTop(), 0, paddingBottom);
                }
            }
            undoView.showWithAction(dialogId, UndoView.ACTION_CONTACT_ADDED, user);
        });
        presentFragment(contactAddActivity);
    }

    private boolean isQrNeedVisible() {
        return true;
    }

    @Override
    public boolean isLightStatusBar() {
        int color;
        if (isPulledDown) {
            return false;
        }
        if (actionBar.isActionModeShowed()) {
            color = getThemedColor(Theme.key_actionBarActionModeDefault);
        } else if (mediaHeaderVisible) {
            color = getThemedColor(Theme.key_windowBackgroundWhite);
        } else if (peerColor != null) {
            color = peerColor.getBgColor2(Theme.isCurrentThemeDark());
        } else {
            color = getThemedColor(Theme.key_actionBarDefault);
        }
        return ColorUtils.calculateLuminance(color) > 0.7f;
    }

    public String getLink(String username, int topicId) {
        String link = getMessagesController().linkPrefix + "/" + username;
        if (topicId != 0) {
            link += "/" + topicId;
        }
        return link;
    }

    float photoDescriptionProgress = -1;
    private void checkPhotoDescriptionAlpha() {
        float p = photoDescriptionProgress;
        if (playProfileAnimation == 1 && (!fragmentOpened || openAnimationInProgress)) {
            photoDescriptionProgress = 0;
        } else if (playProfileAnimation == 2 && (!fragmentOpened || openAnimationInProgress)) {
            photoDescriptionProgress = onlineTextView[1] == null ? 0 : onlineTextView[1].getAlpha();
        } else {
            if (userId == UserConfig.getInstance(currentAccount).clientUserId) {
                photoDescriptionProgress = currentExpandAnimatorValue * (1f - customAvatarProgress);
            } else {
                photoDescriptionProgress = currentExpandAnimatorValue * customAvatarProgress;
            }
        }
//        if (p == photoDescriptionProgress) {
//            return;
//        }
        if (userId == UserConfig.getInstance(currentAccount).clientUserId) {
            if (hasFallbackPhoto) {
                customPhotoOffset = AndroidUtilities.dp(28) * photoDescriptionProgress;
                if (onlineTextView[2] != null) {
                    onlineTextView[2].setAlpha(currentExpandAnimatorValue);
                    onlineTextView[3].setAlpha(1f - currentExpandAnimatorValue);
                    //  onlineTextView[1].setAlpha(1f - expandProgress);
                    onlineTextView[1].setTranslationX(onlineX + customPhotoOffset);
                    avatarContainer2.invalidate();
                    if (showStatusButton != null) {
                        showStatusButton.setAlpha2(1f - currentExpandAnimatorValue);
                    }
                }
            } else {
                if (onlineTextView[2] != null) {
                    onlineTextView[2].setAlpha(0);
                    onlineTextView[3].setAlpha(0);
                }
                if (showStatusButton != null) {
                    showStatusButton.setAlpha2(1f);
                }
            }

        } else {
            if (hasCustomPhoto) {
                if (onlineTextView[2] != null) {
                    onlineTextView[2].setAlpha(photoDescriptionProgress);
                }
                if (showStatusButton != null) {
                    showStatusButton.setAlpha2(1f - photoDescriptionProgress);
                }
            } else {
                if (onlineTextView[2] != null) {
                    onlineTextView[2].setAlpha(0);
                }
                if (showStatusButton != null) {
                    showStatusButton.setAlpha2(1f);
                }
            }
        }
    }

    public void updateStoriesViewBounds(boolean animated) {
        if (storyView == null && giftsView == null || actionBar == null) {
            return;
        }
        float atop = actionBar.getOccupyStatusBar() ? AndroidUtilities.statusBarHeight : 0;
        float aleft = 0;
        float aright = actionBar.getWidth();

        if (actionBar.getBackButton() != null) {
            aleft = Math.max(aleft, actionBar.getBackButton().getRight());
        }
        if (actionBar.menu != null) {
            for (int i = 0; i < actionBar.menu.getChildCount(); ++i) {
                View child = actionBar.menu.getChildAt(i);
                if (child.getAlpha() <= 0 || child.getVisibility() != View.VISIBLE) {
                    continue;
                }
                int left = actionBar.menu.getLeft() + (int) child.getX();
                if (left < aright) {
                    aright = AndroidUtilities.lerp(aright, left, child.getAlpha());
                }
            }
        }
        if (storyView != null) {
            storyView.setBounds(aleft, aright, atop + (actionBar.getHeight() - atop) / 2f, !animated);
        }
        if (giftsView != null) {
            giftsView.setBounds(aleft, aright, atop + (actionBar.getHeight() - atop) / 2f, !animated);
        }
    }

    public void listCodecs(String type, StringBuilder info) {
        if (android.os.Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.M) {
            return;
        }
        try {
            final int allCodecsCount = MediaCodecList.getCodecCount();
            final ArrayList<Integer> decoderIndexes = new ArrayList<>();
            final ArrayList<Integer> encoderIndexes = new ArrayList<>();
            boolean first = true;
            for (int i = 0; i < allCodecsCount; ++i) {
                MediaCodecInfo codec = MediaCodecList.getCodecInfoAt(i);
                if (codec == null) {
                    continue;
                }
                String[] types = codec.getSupportedTypes();
                if (types == null) {
                    continue;
                }
                boolean found = false;
                for (int j = 0; j < types.length; ++j) {
                    if (types[j].equals(type)) {
                        found = true;
                        break;
                    }
                }
                if (found) {
                    (codec.isEncoder() ? encoderIndexes : decoderIndexes).add(i);
                }
            }
            if (decoderIndexes.isEmpty() && encoderIndexes.isEmpty()) {
                return;
            }
            info.append("\n").append(decoderIndexes.size()).append("+").append(encoderIndexes.size()).append(" ").append(type.substring(6)).append(" codecs:\n");
            for (int a = 0; a < decoderIndexes.size(); ++a) {
                if (a > 0) {
                    info.append("\n");
                }
                MediaCodecInfo codec = MediaCodecList.getCodecInfoAt(decoderIndexes.get(a));
                info.append("{d} ").append(codec.getName()).append(" (");
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q) {
                    if (codec.isHardwareAccelerated()) {
                        info.append("gpu"); // as Gpu
                    }
                    if (codec.isSoftwareOnly()) {
                        info.append("cpu"); // as Cpu
                    }
                    if (codec.isVendor()) {
                        info.append(", v"); // as Vendor
                    }
                }
                MediaCodecInfo.CodecCapabilities capabilities = codec.getCapabilitiesForType(type);
                info.append("; mi=").append(capabilities.getMaxSupportedInstances()).append(")");
            }
            for (int a = 0; a < encoderIndexes.size(); ++a) {
                if (a > 0 || !decoderIndexes.isEmpty()) {
                    info.append("\n");
                }
                MediaCodecInfo codec = MediaCodecList.getCodecInfoAt(encoderIndexes.get(a));
                info.append("{e} ").append(codec.getName()).append(" (");
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q) {
                    if (codec.isHardwareAccelerated()) {
                        info.append("gpu"); // as Gpu
                    }
                    if (codec.isSoftwareOnly()) {
                        info.append("cpu"); // as Cpu
                    }
                    if (codec.isVendor()) {
                        info.append(", v"); // as Vendor
                    }
                }
                MediaCodecInfo.CodecCapabilities capabilities = codec.getCapabilitiesForType(type);
                info.append("; mi=").append(capabilities.getMaxSupportedInstances()).append(")");
            }
            info.append("\n");
        } catch (Exception ignore) {}
    }

    @Override
    public void onTransitionAnimationProgress(boolean isOpen, float progress) {
        super.onTransitionAnimationProgress(isOpen, progress);
        if (blurredView != null && blurredView.getVisibility() == View.VISIBLE) {
            if (isOpen) {
                blurredView.setAlpha(1.0f - progress);
            } else {
                blurredView.setAlpha(progress);
            }
        }
    }

    public void prepareBlurBitmap() {
        if (blurredView == null) {
            return;
        }
        int w = (int) (fragmentView.getMeasuredWidth() / 6.0f);
        int h = (int) (fragmentView.getMeasuredHeight() / 6.0f);
        Bitmap bitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        canvas.scale(1.0f / 6.0f, 1.0f / 6.0f);
        fragmentView.draw(canvas);
        Utilities.stackBlurBitmap(bitmap, Math.max(7, Math.max(w, h) / 180));
        blurredView.setBackground(new BitmapDrawable(bitmap));
        blurredView.setAlpha(0.0f);
        blurredView.setVisibility(View.VISIBLE);
    }

    private ShowDrawable showStatusButton;
    public ShowDrawable getShowStatusButton() {
        if (showStatusButton == null) {
            showStatusButton = new ShowDrawable(LocaleController.getString(R.string.StatusHiddenShow));
            showStatusButton.setAlpha((int) (0xFF * Math.min(1f, extraHeight / AndroidUtilities.dp(88f))));
            showStatusButton.setBackgroundColor(ColorUtils.blendARGB(Theme.multAlpha(Theme.adaptHSV(actionBarBackgroundColor, +0.18f, -0.1f), 0.5f), 0x23ffffff, currentExpandAnimatorValue));
        }
        return showStatusButton;
    }

    public void setLoadingSpan(CharacterStyle span) {
        if (loadingSpan == span) return;
        loadingSpan = span;
        AndroidUtilities.forEachViews(listView, view -> {
            if (view instanceof TextDetailCell) {
                ((TextDetailCell) view).textView.setLoading(loadingSpan);
                ((TextDetailCell) view).valueTextView.setLoading(loadingSpan);
            }
        });
    }

    public ProfileBirthdayEffect birthdayEffect;

    private void createBirthdayEffect() {
        if (fragmentView == null || !fullyVisible || birthdayFetcher == null || getContext() == null)
            return;

        if (birthdayEffect != null) {
            birthdayEffect.updateFetcher(birthdayFetcher);
            birthdayEffect.invalidate();
            return;
        }

        birthdayEffect = new ProfileBirthdayEffect(this, birthdayFetcher);
        ((FrameLayout) fragmentView).addView(birthdayEffect, LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, LayoutHelper.MATCH_PARENT, Gravity.FILL_HORIZONTAL | Gravity.TOP));
    }

    public void openLocation(boolean inMapsApp) {
        if (userInfo == null || userInfo.business_location == null) return;
        if (userInfo.business_location.geo_point != null && !inMapsApp) {
            LocationActivity fragment = new LocationActivity(3) {
                @Override
                protected boolean disablePermissionCheck() {
                    return true;
                }
            };
            fragment.setResourceProvider(resourcesProvider);
            TLRPC.TL_message message = new TLRPC.TL_message();
            message.local_id = -1;
            message.peer_id = getMessagesController().getPeer(getDialogId());
            TLRPC.TL_messageMediaGeo media = new TLRPC.TL_messageMediaGeo();
            media.geo = userInfo.business_location.geo_point;
            media.address = userInfo.business_location.address;
            message.media = media;
            fragment.setSharingAllowed(false);
            fragment.setMessageObject(new MessageObject(UserConfig.selectedAccount, message, false, false));
            presentFragment(fragment);
        } else {
            String domain;
            if (BuildVars.isHuaweiStoreApp()) {
                domain = "mapapp://navigation";
            } else {
                domain = "http://maps.google.com/maps";
            }
//                    if (myLocation != null) {
//                        try {
//                            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(String.format(Locale.US, domain + "?saddr=%f,%f&daddr=%f,%f", myLocation.getLatitude(), myLocation.getLongitude(), daddrLat, daddrLong)));
//                            getParentActivity().startActivity(intent);
//                        } catch (Exception e) {
//                            FileLog.e(e);
//                        }
//                    } else {
            try {
                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(String.format(Locale.US, domain + "?q=" + userInfo.business_location.address )));
                getParentActivity().startActivity(intent);
            } catch (Exception e) {
                FileLog.e(e);
            }
//                    }
        }

    }

    public boolean editRow(View view, int position) {
        if (!myProfile) return false;

        if (view instanceof ProfileChannelCell) {
            view = ((ProfileChannelCell) view).dialogCell;
        }

        TLRPC.User user = getUserConfig().getCurrentUser();
        if (user == null) return false;
        TLRPC.UserFull userFull = userInfo == null ? getMessagesController().getUserFull(user.id) : userInfo;
        if (userFull == null) return false;

        String copyButton = getString(R.string.Copy);
        String textToCopy = null;
        if (position == channelInfoRow || position == userInfoRow || position == bioRow) {
            textToCopy = userFull.about;
        } else if (position == bizHoursRow) {
            textToCopy = OpeningHoursActivity.toString(currentAccount, user, userFull.business_work_hours);
            copyButton = getString(R.string.ProfileHoursCopy);
        } else if (position == bizLocationRow) {
            textToCopy = userFull.business_location.address;
            copyButton = getString(R.string.ProfileLocationCopy);
        } else if (position == usernameRow) {
            textToCopy = UserObject.getPublicUsername(user);
            if (textToCopy != null) textToCopy = "@" + textToCopy;
            copyButton = getString(R.string.ProfileCopyUsername);
        } else if (position == phoneRow) {
            textToCopy = user.phone;
        } else if (position == birthdayRow) {
            textToCopy = UserInfoActivity.birthdayString(userInfo.birthday);
        }

        ItemOptions itemOptions = ItemOptions.makeOptions(contentView, resourcesProvider, view);
        itemOptions.setGravity(Gravity.LEFT);

        if (position == bizLocationRow && userFull.business_location != null) {
            if (userFull.business_location.geo_point != null) {
                itemOptions.add(R.drawable.msg_view_file, getString(R.string.ProfileLocationView), () -> {
                    openLocation(false);
                });
            }
            itemOptions.add(R.drawable.msg_map, getString(R.string.ProfileLocationMaps), () -> {
                openLocation(true);
            });
        }

        if (textToCopy != null) {
            final String text = textToCopy;
            itemOptions.add(R.drawable.msg_copy, copyButton, () -> {
                AndroidUtilities.addToClipboard(text);
            });
        }

        if (position == bizHoursRow) {
            itemOptions.add(R.drawable.msg_edit, getString(R.string.ProfileHoursEdit), () -> {
                presentFragment(new OpeningHoursActivity());
            });
            itemOptions.add(R.drawable.msg_delete, getString(R.string.ProfileHoursRemove), true, () -> {
                AlertDialog.Builder builder = new AlertDialog.Builder(getParentActivity());
                builder.setTitle(LocaleController.getString(R.string.BusinessHoursClearTitle));
                builder.setMessage(LocaleController.getString(R.string.BusinessHoursClearMessage));
                builder.setPositiveButton(LocaleController.getString(R.string.Remove), (di, w) -> {
                    TL_account.updateBusinessWorkHours req = new TL_account.updateBusinessWorkHours();
                    if (userFull != null) {
                        userFull.business_work_hours = null;
                        userFull.flags2 &=~ 1;
                    }
                    getConnectionsManager().sendRequest(req, (res, err) -> AndroidUtilities.runOnUIThread(() -> {
                        if (err != null) {
                            BulletinFactory.showError(err);
                        } else if (res instanceof TLRPC.TL_boolFalse) {
                            BulletinFactory.of(this).createErrorBulletin(LocaleController.getString(R.string.UnknownError)).show();
                        }
                    }));
                    updateRowsIds();
                    listAdapter.notifyItemRemoved(position);
                    getMessagesStorage().updateUserInfo(userFull, false);
                });
                builder.setNegativeButton(LocaleController.getString(R.string.Cancel), null);
                showDialog(builder.create());
            });
        } else if (position == bizLocationRow) {
            itemOptions.add(R.drawable.msg_edit, getString(R.string.ProfileLocationEdit), () -> {
                presentFragment(new org.telegram.ui.Business.LocationActivity());
            });
            itemOptions.add(R.drawable.msg_delete, getString(R.string.ProfileLocationRemove), true, () -> {
                AlertDialog.Builder builder = new AlertDialog.Builder(getParentActivity());
                builder.setTitle(LocaleController.getString(R.string.BusinessLocationClearTitle));
                builder.setMessage(LocaleController.getString(R.string.BusinessLocationClearMessage));
                builder.setPositiveButton(LocaleController.getString(R.string.Remove), (di, w) -> {
                    TL_account.updateBusinessLocation req = new TL_account.updateBusinessLocation();
                    if (userFull != null) {
                        userFull.business_location = null;
                        userFull.flags2 &=~ 2;
                    }
                    getConnectionsManager().sendRequest(req, (res, err) -> AndroidUtilities.runOnUIThread(() -> {
                        if (err != null) {
                            BulletinFactory.showError(err);
                        } else if (res instanceof TLRPC.TL_boolFalse) {
                            BulletinFactory.of(this).createErrorBulletin(LocaleController.getString(R.string.UnknownError)).show();
                        }
                    }));
                    updateRowsIds();
                    listAdapter.notifyItemRemoved(position);
                    getMessagesStorage().updateUserInfo(userFull, false);
                });
                builder.setNegativeButton(LocaleController.getString(R.string.Cancel), null);
                showDialog(builder.create());
            });
        } else if (position == usernameRow) {
            itemOptions.add(R.drawable.msg_edit, getString(R.string.ProfileUsernameEdit), () -> {
                presentFragment(new ChangeUsernameActivity());
            });
        } else if (position == channelInfoRow || position == userInfoRow || position == bioRow) {
            itemOptions.add(R.drawable.msg_edit, getString(R.string.ProfileEditBio), () -> {
                presentFragment(new UserInfoActivity());
            });
        } else if (position == phoneRow) {
            itemOptions.add(R.drawable.menu_storage_path, getString(R.string.ProfilePhoneEdit), () -> {
                presentFragment(new ActionIntroActivity(ActionIntroActivity.ACTION_TYPE_CHANGE_PHONE_NUMBER));
            });
        } else if (position == birthdayRow) {
            itemOptions.add(R.drawable.msg_edit, getString(R.string.ProfileBirthdayChange), () -> {
                showDialog(AlertsCreator.createBirthdayPickerDialog(getContext(), getString(R.string.EditProfileBirthdayTitle), getString(R.string.EditProfileBirthdayButton), userFull.birthday, birthday -> {
                    TL_account.updateBirthday req = new TL_account.updateBirthday();
                    req.flags |= 1;
                    req.birthday = birthday;
                    TL_account.TL_birthday oldBirthday = userFull != null ? userFull.birthday : null;
                    if (userFull != null) {
                        userFull.flags2 |= 32;
                        userFull.birthday = birthday;
                    }
                    getMessagesController().invalidateContentSettings();
                    getConnectionsManager().sendRequest(req, (res, err) -> AndroidUtilities.runOnUIThread(() -> {
                        if (res instanceof TLRPC.TL_boolTrue) {
                            BulletinFactory.of(ProfileActivity.this)
                                    .createSimpleBulletin(R.raw.contact_check, LocaleController.getString(R.string.PrivacyBirthdaySetDone))
                                    .setDuration(Bulletin.DURATION_PROLONG).show();
                        } else {
                            if (userFull != null) {
                                if (oldBirthday == null) {
                                    userFull.flags2 &=~ 32;
                                } else {
                                    userFull.flags2 |= 32;
                                }
                                userFull.birthday = oldBirthday;
                                getMessagesStorage().updateUserInfo(userFull, false);
                            }
                            if (err != null && err.text != null && err.text.startsWith("FLOOD_WAIT_")) {
                                if (getContext() != null) {
                                    showDialog(
                                        new AlertDialog.Builder(getContext(), resourceProvider)
                                            .setTitle(getString(R.string.PrivacyBirthdayTooOftenTitle))
                                            .setMessage(getString(R.string.PrivacyBirthdayTooOftenMessage))
                                            .setPositiveButton(getString(R.string.OK), null)
                                            .create()
                                    );
                                }
                            } else {
                                BulletinFactory.of(ProfileActivity.this)
                                    .createSimpleBulletin(R.raw.error, LocaleController.getString(R.string.UnknownError))
                                    .show();
                            }
                        }
                    }), ConnectionsManager.RequestFlagDoNotWaitFloodWait);
                }, () -> {
                    BaseFragment.BottomSheetParams params = new BaseFragment.BottomSheetParams();
                    params.transitionFromLeft = true;
                    params.allowNestedScroll = false;
                    showAsSheet(new PrivacyControlActivity(PrivacyControlActivity.PRIVACY_RULES_TYPE_BIRTHDAY), params);
                }, getResourceProvider()).create());
            });
            itemOptions.add(R.drawable.msg_delete, getString(R.string.Remove), true, () -> {
                AlertDialog.Builder builder = new AlertDialog.Builder(getParentActivity());
                builder.setTitle(LocaleController.getString(R.string.BirthdayClearTitle));
                builder.setMessage(LocaleController.getString(R.string.BirthdayClearMessage));
                builder.setPositiveButton(LocaleController.getString(R.string.Remove), (di, w) -> {
                    TL_account.updateBirthday req = new TL_account.updateBirthday();
                    if (userFull != null) {
                        userFull.birthday = null;
                        userFull.flags2 &=~ 32;
                    }
                    getMessagesController().invalidateContentSettings();
                    getConnectionsManager().sendRequest(req, (res, err) -> AndroidUtilities.runOnUIThread(() -> {
                        if (err != null) {
                            BulletinFactory.showError(err);
                        } else if (res instanceof TLRPC.TL_boolFalse) {
                            BulletinFactory.of(this).createErrorBulletin(LocaleController.getString(R.string.UnknownError)).show();
                        }
                    }));
                    updateListAnimated(false);
                    getMessagesStorage().updateUserInfo(userFull, false);
                });
                builder.setNegativeButton(LocaleController.getString(R.string.Cancel), null);
                showDialog(builder.create());
            });
        } else if (position == channelRow) {
            TLRPC.Chat channel = getMessagesController().getChat(userFull.personal_channel_id);
            if (channel != null && ChatObject.getPublicUsername(channel) != null) {
                itemOptions.add(R.drawable.msg_copy, getString(R.string.ProfileChannelCopy), () -> {
                    AndroidUtilities.addToClipboard("https://" + getMessagesController().linkPrefix + "/" + ChatObject.getPublicUsername(channel));
                });
            }
            itemOptions.add(R.drawable.msg_edit, getString(R.string.ProfileChannelChange), () -> {
                presentFragment(new UserInfoActivity());
            });
            itemOptions.add(R.drawable.msg_delete, getString(R.string.Remove), true, () -> {
                AlertDialog.Builder builder = new AlertDialog.Builder(getParentActivity());
                builder.setTitle(LocaleController.getString(R.string.ProfileChannelClearTitle));
                builder.setMessage(LocaleController.getString(R.string.ProfileChannelClearMessage));
                builder.setPositiveButton(LocaleController.getString(R.string.Remove), (di, w) -> {
                    TL_account.updatePersonalChannel req = new TL_account.updatePersonalChannel();
                    req.channel = new TLRPC.TL_inputChannelEmpty();
                    if (userFull != null) {
                        userFull.personal_channel_id = 0;
                        userFull.personal_channel_message = 0;
                        userFull.flags2 &=~ 64;
                    }
                    getConnectionsManager().sendRequest(req, (res, err) -> AndroidUtilities.runOnUIThread(() -> {
                        if (err != null) {
                            BulletinFactory.showError(err);
                        } else if (res instanceof TLRPC.TL_boolFalse) {
                            BulletinFactory.of(this).createErrorBulletin(LocaleController.getString(R.string.UnknownError)).show();
                        }
                    }));
                    updateListAnimated(false);
                    getMessagesStorage().updateUserInfo(userFull, false);
                });
                builder.setNegativeButton(LocaleController.getString(R.string.Cancel), null);
                showDialog(builder.create());
            });
        }

        if (itemOptions.getItemsCount() <= 0) {
            return false;
        }

        itemOptions.show();

        return true;
    }

    private void updateItemsUsername() {
        if (!myProfile || setUsernameItem == null || linkItem == null) return;
        TLRPC.User user = getMessagesController().getUser(userId);
        if (user == null) {
            return;
        }
        final boolean hasUsername = UserObject.getPublicUsername(user) != null;
        setUsernameItem.setIcon(hasUsername ? R.drawable.menu_username_change : R.drawable.menu_username_set);
        setUsernameItem.setText(hasUsername ? getString(R.string.ProfileUsernameEdit) : getString(R.string.ProfileUsernameSet));
        linkItem.setVisibility(UserObject.getPublicUsername(user) != null ? View.VISIBLE : View.GONE);
    }

    private void updateEditColorIcon() {
        if (getContext() == null || editColorItem == null) return;
        if (getUserConfig().isPremium()) {
            editColorItem.setIcon(R.drawable.menu_profile_colors);
        } else {
            Drawable icon = ContextCompat.getDrawable(getContext(), R.drawable.menu_profile_colors_locked);
            icon.setColorFilter(new PorterDuffColorFilter(getThemedColor(Theme.key_actionBarDefaultSubmenuItemIcon), PorterDuff.Mode.SRC_IN));
            Drawable lockIcon = ContextCompat.getDrawable(getContext(), R.drawable.msg_gallery_locked2);
            lockIcon.setColorFilter(new PorterDuffColorFilter(ColorUtils.blendARGB(Color.WHITE, Color.BLACK, 0.5f), PorterDuff.Mode.MULTIPLY));
            CombinedDrawable combinedDrawable = new CombinedDrawable(icon, lockIcon, dp(1), -dp(1)) {
                @Override
                public void setColorFilter(ColorFilter colorFilter) {}
            };
            editColorItem.setIcon(combinedDrawable);
        }
    }

    public boolean hasPrivacyCommand() {
        if (!isBot) return false;
        if (userInfo == null || userInfo.bot_info == null) return false;
        if (userInfo.bot_info.privacy_policy_url != null) return true;
        for (TLRPC.TL_botCommand command : userInfo.bot_info.commands) {
            if ("privacy".equals(command.command)) {
                return true;
            }
        }
        return true;
    }


    private HintView2 collectibleHint;
    private int collectibleHintBackgroundColor;
    private Boolean collectibleHintVisible;
    private TLRPC.TL_emojiStatusCollectible collectibleStatus;

    public void setCollectibleGiftStatus(TLRPC.TL_emojiStatusCollectible status) {
        if (avatarContainer2 == null) return;
        if (collectibleStatus == status) return;
        if (collectibleStatus != null && status != null && collectibleStatus.collectible_id == status.collectible_id) return;
        collectibleStatus = status;
        if (collectibleHint != null) {
            collectibleHint.hide();
        }
        if (status != null && !TextUtils.isEmpty(status.slug)) {
            collectibleHintVisible = null;
            collectibleHint = new HintView2(getContext(), HintView2.DIRECTION_BOTTOM);
            collectibleHintBackgroundColor = Theme.blendOver(status.center_color | 0xFF000000, Theme.multAlpha(status.pattern_color | 0xFF000000, .5f));
            collectibleHint.setPadding(dp(4), 0, dp(4), dp(2));
            collectibleHint.setFlicker(.66f, Theme.multAlpha(status.text_color | 0xFF000000, 0.5f));
            avatarContainer2.addView(collectibleHint, LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, 24));
            collectibleHint.setTextSize(9.33f);
            collectibleHint.setTextTypeface(AndroidUtilities.bold());
            collectibleHint.setText(status.title);
            collectibleHint.setDuration(-1);
            collectibleHint.setInnerPadding(4.66f + 1, 2.66f, 4.66f + 1, 2.66f);
            collectibleHint.setArrowSize(4, 2.66f);
            collectibleHint.setRoundingWithCornerEffect(false);
            collectibleHint.setRounding(16);
            collectibleHint.show();
            final String slug = status.slug;
            collectibleHint.setOnClickListener(v -> {
                Browser.openUrl(getContext(), "https://" + getMessagesController().linkPrefix + "/nft/" + slug);
            });
            if (extraHeight < dp(82)) {
                collectibleHintVisible = false;
                collectibleHint.setAlpha(0.0f);
            }
            updateCollectibleHint();
            AndroidUtilities.runOnUIThread(collectibleHint::hide, 6 * 1000);
        }
    }

    public void updateCollectibleHint() {
        if (collectibleHint == null) return;
        collectibleHint.setJointPx(0, -collectibleHint.getPaddingLeft() + nameTextView[1].getX() + (nameTextView[1].getRightDrawableX() - nameTextView[1].getRightDrawableWidth() * lerp(0.45f, 0.25f, currentExpandAnimatorValue)) * nameTextView[1].getScaleX());
        final float expanded = AndroidUtilities.lerp(expandAnimatorValues, currentExpanAnimatorFracture);
        collectibleHint.setTranslationY(-collectibleHint.getPaddingBottom() + nameTextView[1].getY() - dp(24) + lerp(dp(6), -dp(12), expanded));
        collectibleHint.setBgColor(ColorUtils.blendARGB(collectibleHintBackgroundColor, 0x50000000, expanded));
        final boolean visible = extraHeight >= dp(82);
        if (collectibleHintVisible == null || collectibleHintVisible != visible) {
            collectibleHint.animate().alpha((collectibleHintVisible = visible) ? 1.0f : 0.0f).setInterpolator(CubicBezierInterpolator.EASE_OUT).setDuration(200).start();
        }
    }

}