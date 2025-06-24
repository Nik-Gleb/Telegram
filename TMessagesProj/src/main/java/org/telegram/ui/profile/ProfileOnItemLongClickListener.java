package org.telegram.ui.profile;

import static org.telegram.messenger.LocaleController.getString;

import android.app.Activity;
import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ConfigurationInfo;
import android.content.pm.PackageManager;
import android.os.Build;
import android.view.View;
import android.webkit.CookieManager;
import android.webkit.WebStorage;
import android.webkit.WebView;
import android.widget.Toast;

import androidx.collection.LongSparseArray;

import org.telegram.messenger.AndroidUtilities;
import org.telegram.messenger.ApplicationLoader;
import org.telegram.messenger.AuthTokensHelper;
import org.telegram.messenger.BuildVars;
import org.telegram.messenger.ChatThemeController;
import org.telegram.messenger.FileLog;
import org.telegram.messenger.MessagesController;
import org.telegram.messenger.NotificationCenter;
import org.telegram.messenger.R;
import org.telegram.messenger.SharedConfig;
import org.telegram.tgnet.ConnectionsManager;
import org.telegram.tgnet.TLRPC;
import org.telegram.ui.ActionBar.AlertDialog;
import org.telegram.ui.ActionBar.Theme;
import org.telegram.ui.Components.BulletinFactory;
import org.telegram.ui.Components.FloatingDebug.FloatingDebugController;
import org.telegram.ui.Components.InstantCameraView;
import org.telegram.ui.Components.Paint.PersistColorPalette;
import org.telegram.ui.Components.RecyclerListView;
import org.telegram.ui.Components.ShareAlert;
import org.telegram.ui.Components.voip.VoIPHelper;
import org.telegram.ui.LaunchActivity;
import org.telegram.ui.RestrictedLanguagesSelectActivity;
import org.telegram.ui.Stories.recorder.DualCameraView;
import org.telegram.ui.UserInfoActivity;
import org.telegram.ui.bots.BotBiometry;
import org.telegram.ui.bots.BotDownloads;
import org.telegram.ui.bots.BotLocation;
import org.telegram.ui.bots.SetupEmojiStatusSheet;

import java.util.Set;

public class ProfileOnItemLongClickListener implements RecyclerListView.OnItemLongClickListener {

    private final ProfileActivity mProfileActivity;
    private final Context context;
    private int pressCount;

    public ProfileOnItemLongClickListener(ProfileActivity mProfileActivity, Context context) {
        this.mProfileActivity = mProfileActivity;
        this.context = context;
        pressCount = 0;
    }

    @Override
    public boolean onItemClick(View view, int position) {
        if (position == mProfileActivity.versionRow) {
            pressCount++;
            if (pressCount >= 2 || BuildVars.DEBUG_PRIVATE_VERSION) {
                AlertDialog.Builder builder = new AlertDialog.Builder(mProfileActivity.getParentActivity(), mProfileActivity.resourcesProvider);
                builder.setTitle(getString(R.string.DebugMenu));
                CharSequence[] items;
                items = new CharSequence[]{
                        getString(R.string.DebugMenuImportContacts),
                        getString(R.string.DebugMenuReloadContacts),
                        getString(R.string.DebugMenuResetContacts),
                        getString(R.string.DebugMenuResetDialogs),
                        BuildVars.DEBUG_VERSION ? null : (BuildVars.LOGS_ENABLED ? getString("DebugMenuDisableLogs", R.string.DebugMenuDisableLogs) : getString("DebugMenuEnableLogs", R.string.DebugMenuEnableLogs)),
                        SharedConfig.inappCamera ? getString("DebugMenuDisableCamera", R.string.DebugMenuDisableCamera) : getString("DebugMenuEnableCamera", R.string.DebugMenuEnableCamera),
                        getString("DebugMenuClearMediaCache", R.string.DebugMenuClearMediaCache),
                        getString(R.string.DebugMenuCallSettings),
                        null,
                        BuildVars.DEBUG_PRIVATE_VERSION || ApplicationLoader.isStandaloneBuild() || ApplicationLoader.isBetaBuild() ? getString("DebugMenuCheckAppUpdate", R.string.DebugMenuCheckAppUpdate) : null,
                        getString("DebugMenuReadAllDialogs", R.string.DebugMenuReadAllDialogs),
                        BuildVars.DEBUG_PRIVATE_VERSION ? (SharedConfig.disableVoiceAudioEffects ? "Enable voip audio effects" : "Disable voip audio effects") : null,
                        BuildVars.DEBUG_PRIVATE_VERSION ? "Clean app update" : null,
                        BuildVars.DEBUG_PRIVATE_VERSION ? "Reset suggestions" : null,
                        BuildVars.DEBUG_PRIVATE_VERSION ? getString(R.string.DebugMenuClearWebViewCache) : null,
                        getString(R.string.DebugMenuClearWebViewCookies),
                        Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT ? getString(SharedConfig.debugWebView ? R.string.DebugMenuDisableWebViewDebug : R.string.DebugMenuEnableWebViewDebug) : null,
                        (AndroidUtilities.isTabletInternal() && BuildVars.DEBUG_PRIVATE_VERSION) ? (SharedConfig.forceDisableTabletMode ? "Enable tablet mode" : "Disable tablet mode") : null,
                        BuildVars.DEBUG_PRIVATE_VERSION ? getString(SharedConfig.isFloatingDebugActive ? R.string.FloatingDebugDisable : R.string.FloatingDebugEnable) : null,
                        BuildVars.DEBUG_PRIVATE_VERSION ? "Force remove premium suggestions" : null,
                        BuildVars.DEBUG_PRIVATE_VERSION ? "Share device info" : null,
                        BuildVars.DEBUG_PRIVATE_VERSION ? "Force performance class" : null,
                        BuildVars.DEBUG_PRIVATE_VERSION && !InstantCameraView.allowBigSizeCameraDebug() ? (!SharedConfig.bigCameraForRound ? "Force big camera for round" : "Disable big camera for round") : null,
                        getString(DualCameraView.dualAvailableStatic(mProfileActivity.getContext()) ? "DebugMenuDualOff" : "DebugMenuDualOn"),
                        BuildVars.DEBUG_VERSION ? (SharedConfig.useSurfaceInStories ? "back to TextureView in stories" : "use SurfaceView in stories") : null,
                        BuildVars.DEBUG_PRIVATE_VERSION ? (SharedConfig.photoViewerBlur ? "do not blur in photoviewer" : "blur in photoviewer") : null,
                        !SharedConfig.payByInvoice ? "Enable Invoice Payment" : "Disable Invoice Payment",
                        BuildVars.DEBUG_PRIVATE_VERSION ? "Update Attach Bots" : null,
                        Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP ? (!SharedConfig.isUsingCamera2(mProfileActivity.getCurrentAccount()) ? "Use Camera 2 API" : "Use old Camera 1 API") : null,
                        BuildVars.DEBUG_VERSION ? "Clear Mini Apps Permissions and Files" : null,
                        BuildVars.DEBUG_PRIVATE_VERSION ? "Clear all login tokens" : null,
                        SharedConfig.canBlurChat() && Build.VERSION.SDK_INT >= 31 ? (SharedConfig.useNewBlur ? "back to cpu blur" : "use new gpu blur") : null,
                        SharedConfig.adaptableColorInBrowser ? "Disabled adaptive browser colors" : "Enable adaptive browser colors",
                        SharedConfig.debugVideoQualities ? "Disable video qualities debug" : "Enable video qualities debug",
                        Build.VERSION.SDK_INT >= Build.VERSION_CODES.P ? getString(SharedConfig.useSystemBoldFont ? R.string.DebugMenuDontUseSystemBoldFont : R.string.DebugMenuUseSystemBoldFont) : null,
                        "Reload app config",
                        !SharedConfig.forceForumTabs ? "Force Forum Tabs" : "Do Not Force Forum Tabs"
                };

                builder.setItems(items, (dialog, which) -> {
                    if (which == 0) { // Import Contacts
                        mProfileActivity.getUserConfig().syncContacts = true;
                        mProfileActivity.getUserConfig().saveConfig(false);
                        mProfileActivity.getContactsController().forceImportContacts();
                    } else if (which == 1) { // Reload Contacts
                        mProfileActivity.getContactsController().loadContacts(false, 0);
                    } else if (which == 2) { // Reset Imported Contacts
                        mProfileActivity.getContactsController().resetImportedContacts();
                    } else if (which == 3) { // Reset Dialogs
                        mProfileActivity.getMessagesController().forceResetDialogs();
                    } else if (which == 4) { // Logs
                        BuildVars.LOGS_ENABLED = !BuildVars.LOGS_ENABLED;
                        SharedPreferences sharedPreferences = ApplicationLoader.applicationContext.getSharedPreferences("systemConfig", Context.MODE_PRIVATE);
                        sharedPreferences.edit().putBoolean("logsEnabled", BuildVars.LOGS_ENABLED).commit();
                        mProfileActivity.updateRowsIds();
                        mProfileActivity.listAdapter.notifyDataSetChanged();
                        if (BuildVars.LOGS_ENABLED) {
                            FileLog.d("app start time = " + ApplicationLoader.startTime);
                            try {
                                FileLog.d("buildVersion = " + ApplicationLoader.applicationContext.getPackageManager().getPackageInfo(ApplicationLoader.applicationContext.getPackageName(), 0).versionCode);
                            } catch (Exception e) {
                                FileLog.e(e);
                            }
                        }
                    } else if (which == 5) { // In-app camera
                        SharedConfig.toggleInappCamera();
                    } else if (which == 6) { // Clear sent media cache
                        mProfileActivity.getMessagesStorage().clearSentMedia();
                        SharedConfig.setNoSoundHintShowed(false);
                        SharedPreferences.Editor editor = MessagesController.getGlobalMainSettings().edit();
                        editor.remove("archivehint").remove("proximityhint").remove("archivehint_l").remove("speedhint").remove("gifhint").remove("reminderhint").remove("soundHint").remove("themehint").remove("bganimationhint").remove("filterhint").remove("n_0").remove("storyprvhint").remove("storyhint").remove("storyhint2").remove("storydualhint").remove("storysvddualhint").remove("stories_camera").remove("dualcam").remove("dualmatrix").remove("dual_available").remove("archivehint").remove("askNotificationsAfter").remove("askNotificationsDuration").remove("viewoncehint").remove("voicepausehint").remove("taptostorysoundhint").remove("nothanos").remove("voiceoncehint").remove("savedhint").remove("savedsearchhint").remove("savedsearchtaghint").remove("groupEmojiPackHintShown").remove("newppsms").remove("monetizationadshint").remove("seekSpeedHintShowed").remove("unsupport_video/av01").remove("channelgifthint").remove("statusgiftpage").remove("multistorieshint").remove("channelsuggesthint").remove("trimvoicehint").apply();
                        MessagesController.getEmojiSettings(mProfileActivity.getCurrentAccount()).edit().remove("featured_hidden").remove("emoji_featured_hidden").commit();
                        SharedConfig.textSelectionHintShows = 0;
                        SharedConfig.lockRecordAudioVideoHint = 0;
                        SharedConfig.stickersReorderingHintUsed = false;
                        SharedConfig.forwardingOptionsHintShown = false;
                        SharedConfig.replyingOptionsHintShown = false;
                        SharedConfig.messageSeenHintCount = 3;
                        SharedConfig.emojiInteractionsHintCount = 3;
                        SharedConfig.dayNightThemeSwitchHintCount = 3;
                        SharedConfig.fastScrollHintCount = 3;
                        SharedConfig.stealthModeSendMessageConfirm = 2;
                        SharedConfig.updateStealthModeSendMessageConfirm(2);
                        SharedConfig.setStoriesReactionsLongPressHintUsed(false);
                        SharedConfig.setStoriesIntroShown(false);
                        SharedConfig.setMultipleReactionsPromoShowed(false);
                        ChatThemeController.getInstance(mProfileActivity.getCurrentAccount()).clearCache();
                        mProfileActivity.getNotificationCenter().postNotificationName(NotificationCenter.newSuggestionsAvailable);
                        RestrictedLanguagesSelectActivity.cleanup();
                        PersistColorPalette.getInstance(mProfileActivity.getCurrentAccount()).cleanup();
                        SharedPreferences prefs = mProfileActivity.getMessagesController().getMainSettings();
                        editor = prefs.edit();
                        editor.remove("peerColors").remove("profilePeerColors").remove("boostingappearance").remove("bizbothint").remove("movecaptionhint");
                        for (String key : prefs.getAll().keySet()) {
                            if (key.contains("show_gift_for_") || key.contains("bdayhint_") || key.contains("bdayanim_") || key.startsWith("ask_paid_message_") || key.startsWith("topicssidetabs")) {
                                editor.remove(key);
                            }
                        }
                        editor.apply();
                        editor = MessagesController.getNotificationsSettings(mProfileActivity.getCurrentAccount()).edit();
                        for (String key : MessagesController.getNotificationsSettings(mProfileActivity.getCurrentAccount()).getAll().keySet()) {
                            if (key.startsWith("dialog_bar_botver")) {
                                editor.remove(key);
                            }
                        }
                        editor.apply();
                    } else if (which == 7) { // Call settings
                        VoIPHelper.showCallDebugSettings(mProfileActivity.getParentActivity());
                    } else if (which == 8) { // ?
                        SharedConfig.toggleRoundCamera16to9();
                    } else if (which == 9) { // Check app update
                        ((LaunchActivity) mProfileActivity.getParentActivity()).checkAppUpdate(true, null);
                    } else if (which == 10) { // Read all chats
                        mProfileActivity.getMessagesStorage().readAllDialogs(-1);
                    } else if (which == 11) { // Voip audio effects
                        SharedConfig.toggleDisableVoiceAudioEffects();
                    } else if (which == 12) { // Clean app update
                        SharedConfig.pendingAppUpdate = null;
                        SharedConfig.saveConfig();
                        NotificationCenter.getGlobalInstance().postNotificationName(NotificationCenter.appUpdateAvailable);
                    } else if (which == 13) { // Reset suggestions
                        Set<String> suggestions = mProfileActivity.getMessagesController().pendingSuggestions;
                        suggestions.add("VALIDATE_PHONE_NUMBER");
                        suggestions.add("VALIDATE_PASSWORD");
                        mProfileActivity.getNotificationCenter().postNotificationName(NotificationCenter.newSuggestionsAvailable);
                    } else if (which == 14) { // WebView Cache
                        ApplicationLoader.applicationContext.deleteDatabase("webview.db");
                        ApplicationLoader.applicationContext.deleteDatabase("webviewCache.db");
                        WebStorage.getInstance().deleteAllData();
                        try {
                            WebView webView = new WebView(ApplicationLoader.applicationContext);
                            webView.clearHistory();
                            webView.destroy();
                        } catch (Exception e) {
                        }
                    } else if (which == 15) {
                        CookieManager cookieManager = CookieManager.getInstance();
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                            cookieManager.removeAllCookies(null);
                            cookieManager.flush();
                        }
                    } else if (which == 16) { // WebView debug
                        SharedConfig.toggleDebugWebView();
                        Toast.makeText(mProfileActivity.getParentActivity(), getString(SharedConfig.debugWebView ? R.string.DebugMenuWebViewDebugEnabled : R.string.DebugMenuWebViewDebugDisabled), Toast.LENGTH_SHORT).show();
                    } else if (which == 17) { // Tablet mode
                        SharedConfig.toggleForceDisableTabletMode();

                        Activity activity = AndroidUtilities.findActivity(context);
                        final PackageManager pm = activity.getPackageManager();
                        final Intent intent = pm.getLaunchIntentForPackage(activity.getPackageName());
                        activity.finishAffinity(); // Finishes all activities.
                        activity.startActivity(intent);    // Start the launch activity
                        System.exit(0);
                    } else if (which == 18) {
                        FloatingDebugController.setActive((LaunchActivity) mProfileActivity.getParentActivity(), !FloatingDebugController.isActive());
                    } else if (which == 19) {
                        mProfileActivity.getMessagesController().loadAppConfig();
                        TLRPC.TL_help_dismissSuggestion req = new TLRPC.TL_help_dismissSuggestion();
                        req.suggestion = "VALIDATE_PHONE_NUMBER";
                        req.peer = new TLRPC.TL_inputPeerEmpty();
                        mProfileActivity.getConnectionsManager().sendRequest(req, (response, error) -> {
                            TLRPC.TL_help_dismissSuggestion req2 = new TLRPC.TL_help_dismissSuggestion();
                            req2.suggestion = "VALIDATE_PASSWORD";
                            req2.peer = new TLRPC.TL_inputPeerEmpty();
                            mProfileActivity.getConnectionsManager().sendRequest(req2, (res2, err2) -> {
                                mProfileActivity.getMessagesController().loadAppConfig();
                            });
                        });
                    } else if (which == 20) {
                        int androidVersion = Build.VERSION.SDK_INT;
                        int cpuCount = ConnectionsManager.CPU_COUNT;
                        int memoryClass = ((ActivityManager) ApplicationLoader.applicationContext.getSystemService(Context.ACTIVITY_SERVICE)).getMemoryClass();
                        long minFreqSum = 0, minFreqCount = 0;
                        long maxFreqSum = 0, maxFreqCount = 0;
                        long curFreqSum = 0, curFreqCount = 0;
                        long capacitySum = 0, capacityCount = 0;
                        StringBuilder cpusInfo = new StringBuilder();
                        for (int i = 0; i < cpuCount; i++) {
                            Long minFreq = AndroidUtilities.getSysInfoLong("/sys/devices/system/cpu/cpu" + i + "/cpufreq/cpuinfo_min_freq");
                            Long curFreq = AndroidUtilities.getSysInfoLong("/sys/devices/system/cpu/cpu" + i + "/cpufreq/cpuinfo_cur_freq");
                            Long maxFreq = AndroidUtilities.getSysInfoLong("/sys/devices/system/cpu/cpu" + i + "/cpufreq/cpuinfo_max_freq");
                            Long capacity = AndroidUtilities.getSysInfoLong("/sys/devices/system/cpu/cpu" + i + "/cpu_capacity");
                            cpusInfo.append("#").append(i).append(" ");
                            if (minFreq != null) {
                                cpusInfo.append("min=").append(minFreq / 1000L).append(" ");
                                minFreqSum += (minFreq / 1000L);
                                minFreqCount++;
                            }
                            if (curFreq != null) {
                                cpusInfo.append("cur=").append(curFreq / 1000L).append(" ");
                                curFreqSum += (curFreq / 1000L);
                                curFreqCount++;
                            }
                            if (maxFreq != null) {
                                cpusInfo.append("max=").append(maxFreq / 1000L).append(" ");
                                maxFreqSum += (maxFreq / 1000L);
                                maxFreqCount++;
                            }
                            if (capacity != null) {
                                cpusInfo.append("cpc=").append(capacity).append(" ");
                                capacitySum += capacity;
                                capacityCount++;
                            }
                            cpusInfo.append("\n");
                        }
                        StringBuilder info = new StringBuilder();
                        info.append(Build.MANUFACTURER).append(", ").append(Build.MODEL).append(" (").append(Build.PRODUCT).append(", ").append(Build.DEVICE).append(") ").append(" (android ").append(Build.VERSION.SDK_INT).append(")\n");
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                            info.append("SoC: ").append(Build.SOC_MANUFACTURER).append(", ").append(Build.SOC_MODEL).append("\n");
                        }
                        String gpuModel = AndroidUtilities.getSysInfoString("/sys/kernel/gpu/gpu_model");
                        if (gpuModel != null) {
                            info.append("GPU: ").append(gpuModel);
                            Long minClock = AndroidUtilities.getSysInfoLong("/sys/kernel/gpu/gpu_min_clock");
                            Long mminClock = AndroidUtilities.getSysInfoLong("/sys/kernel/gpu/gpu_mm_min_clock");
                            Long maxClock = AndroidUtilities.getSysInfoLong("/sys/kernel/gpu/gpu_max_clock");
                            if (minClock != null) {
                                info.append(", min=").append(minClock / 1000L);
                            }
                            if (mminClock != null) {
                                info.append(", mmin=").append(mminClock / 1000L);
                            }
                            if (maxClock != null) {
                                info.append(", max=").append(maxClock / 1000L);
                            }
                            info.append("\n");
                        }
                        ConfigurationInfo configurationInfo = ((ActivityManager) ApplicationLoader.applicationContext.getSystemService(Context.ACTIVITY_SERVICE)).getDeviceConfigurationInfo();
                        info.append("GLES Version: ").append(configurationInfo.getGlEsVersion()).append("\n");
                        info.append("Memory: class=").append(AndroidUtilities.formatFileSize(memoryClass * 1024L * 1024L));
                        ActivityManager.MemoryInfo memoryInfo = new ActivityManager.MemoryInfo();
                        ((ActivityManager) ApplicationLoader.applicationContext.getSystemService(Context.ACTIVITY_SERVICE)).getMemoryInfo(memoryInfo);
                        info.append(", total=").append(AndroidUtilities.formatFileSize(memoryInfo.totalMem));
                        info.append(", avail=").append(AndroidUtilities.formatFileSize(memoryInfo.availMem));
                        info.append(", low?=").append(memoryInfo.lowMemory);
                        info.append(" (threshold=").append(AndroidUtilities.formatFileSize(memoryInfo.threshold)).append(")");
                        info.append("\n");
                        info.append("Current class: ").append(SharedConfig.performanceClassName(SharedConfig.getDevicePerformanceClass())).append(", measured: ").append(SharedConfig.performanceClassName(SharedConfig.measureDevicePerformanceClass()));
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                            info.append(", suggest=").append(Build.VERSION.MEDIA_PERFORMANCE_CLASS);
                        }
                        info.append("\n");
                        info.append(cpuCount).append(" CPUs");
                        if (minFreqCount > 0) {
                            info.append(", avgMinFreq=").append(minFreqSum / minFreqCount);
                        }
                        if (curFreqCount > 0) {
                            info.append(", avgCurFreq=").append(curFreqSum / curFreqCount);
                        }
                        if (maxFreqCount > 0) {
                            info.append(", avgMaxFreq=").append(maxFreqSum / maxFreqCount);
                        }
                        if (capacityCount > 0) {
                            info.append(", avgCapacity=").append(capacitySum / capacityCount);
                        }
                        info.append("\n").append(cpusInfo);

                        mProfileActivity.listCodecs("video/avc", info);
                        mProfileActivity.listCodecs("video/hevc", info);
                        mProfileActivity.listCodecs("video/x-vnd.on2.vp8", info);
                        mProfileActivity.listCodecs("video/x-vnd.on2.vp9", info);

                        mProfileActivity.showDialog(new ShareAlert(mProfileActivity.getParentActivity(), null, info.toString(), false, null, false) {
                            @Override
                            protected void onSend(LongSparseArray<TLRPC.Dialog> dids, int count, TLRPC.TL_forumTopic topic, boolean showToast) {
                                if (!showToast) return;
                                AndroidUtilities.runOnUIThread(() -> {
                                    BulletinFactory.createInviteSentBulletin(mProfileActivity.getParentActivity(), mProfileActivity.contentView, dids.size(), dids.size() == 1 ? dids.valueAt(0).id : 0, count, getThemedColor(Theme.key_undo_background), getThemedColor(Theme.key_undo_infoColor)).show();
                                }, 250);
                            }
                        });
                    } else if (which == 21) {
                        AlertDialog.Builder builder2 = new AlertDialog.Builder(mProfileActivity.getParentActivity(), mProfileActivity.resourcesProvider);
                        builder2.setTitle("Force performance class");
                        int currentClass = SharedConfig.getDevicePerformanceClass();
                        int trueClass = SharedConfig.measureDevicePerformanceClass();
                        builder2.setItems(new CharSequence[]{
                                AndroidUtilities.replaceTags((currentClass == SharedConfig.PERFORMANCE_CLASS_HIGH ? "**HIGH**" : "HIGH") + (trueClass == SharedConfig.PERFORMANCE_CLASS_HIGH ? " (measured)" : "")),
                                AndroidUtilities.replaceTags((currentClass == SharedConfig.PERFORMANCE_CLASS_AVERAGE ? "**AVERAGE**" : "AVERAGE") + (trueClass == SharedConfig.PERFORMANCE_CLASS_AVERAGE ? " (measured)" : "")),
                                AndroidUtilities.replaceTags((currentClass == SharedConfig.PERFORMANCE_CLASS_LOW ? "**LOW**" : "LOW") + (trueClass == SharedConfig.PERFORMANCE_CLASS_LOW ? " (measured)" : ""))
                        }, (dialog2, which2) -> {
                            int newClass = 2 - which2;
                            if (newClass == trueClass) {
                                SharedConfig.overrideDevicePerformanceClass(-1);
                            } else {
                                SharedConfig.overrideDevicePerformanceClass(newClass);
                            }
                        });
                        builder2.setNegativeButton(getString("Cancel", R.string.Cancel), null);
                        builder2.show();
                    } else if (which == 22) {
                        SharedConfig.toggleRoundCamera();
                    } else if (which == 23) {
                        boolean enabled = DualCameraView.dualAvailableStatic(mProfileActivity.getContext());
                        MessagesController.getGlobalMainSettings().edit().putBoolean("dual_available", !enabled).apply();
                        try {
                            Toast.makeText(mProfileActivity.getParentActivity(), getString(!enabled ? R.string.DebugMenuDualOnToast : R.string.DebugMenuDualOffToast), Toast.LENGTH_SHORT).show();
                        } catch (Exception e) {
                        }
                    } else if (which == 24) {
                        SharedConfig.toggleSurfaceInStories();
                        for (int i = 0; i < mProfileActivity.getParentLayout().getFragmentStack().size(); i++) {
                            mProfileActivity.getParentLayout().getFragmentStack().get(i).clearSheets();
                        }
                    } else if (which == 25) {
                        SharedConfig.togglePhotoViewerBlur();
                    } else if (which == 26) {
                        SharedConfig.togglePaymentByInvoice();
                    } else if (which == 27) {
                        mProfileActivity.getMediaDataController().loadAttachMenuBots(false, true);
                    } else if (which == 28) {
                        SharedConfig.toggleUseCamera2(mProfileActivity.getCurrentAccount());
                    } else if (which == 29) {
                        BotBiometry.clear();
                        BotLocation.clear();
                        BotDownloads.clear();
                        SetupEmojiStatusSheet.clear();
                    } else if (which == 30) {
                        AuthTokensHelper.clearLogInTokens();
                    } else if (which == 31) {
                        SharedConfig.toggleUseNewBlur();
                    } else if (which == 32) {
                        SharedConfig.toggleBrowserAdaptableColors();
                    } else if (which == 33) {
                        SharedConfig.toggleDebugVideoQualities();
                    } else if (which == 34) {
                        SharedConfig.toggleUseSystemBoldFont();
                    } else if (which == 35) {
                        MessagesController.getInstance(mProfileActivity.getCurrentAccount()).loadAppConfig(true);
                    } else if (which == 36) {
                        SharedConfig.toggleForceForumTabs();
                    }
                });
                builder.setNegativeButton(getString("Cancel", R.string.Cancel), null);
                mProfileActivity.showDialog(builder.create());
            } else {
                try {
                    Toast.makeText(mProfileActivity.getParentActivity(), getString("DebugMenuLongPress", R.string.DebugMenuLongPress), Toast.LENGTH_SHORT).show();
                } catch (Exception e) {
                    FileLog.e(e);
                }
            }
            return true;
        } else if (position >= mProfileActivity.membersStartRow && position < mProfileActivity.membersEndRow) {
            final TLRPC.ChatParticipant participant;
            if (!mProfileActivity.sortedUsers.isEmpty()) {
                participant = mProfileActivity.visibleChatParticipants.get(mProfileActivity.sortedUsers.get(position - mProfileActivity.membersStartRow));
            } else {
                participant = mProfileActivity.visibleChatParticipants.get(position - mProfileActivity.membersStartRow);
            }
            return mProfileActivity.onMemberClick(participant, true, view);
        } else if (position == mProfileActivity.birthdayRow) {
            if (mProfileActivity.editRow(view, position)) return true;
            if (mProfileActivity.getUserInfo() == null) return false;
            try {
                AndroidUtilities.addToClipboard(UserInfoActivity.birthdayString(mProfileActivity.getUserInfo().birthday));
                BulletinFactory.of(mProfileActivity).createCopyBulletin(getString(R.string.BirthdayCopied)).show();
            } catch (Exception e) {
                FileLog.e(e);
            }
            return true;
        } else {
            if (mProfileActivity.editRow(view, position)) return true;
            return mProfileActivity.processOnClickOrPress(position, view, view.getWidth() / 2f, (int) (view.getHeight() * .75f));
        }
    }
}
