package com.project.common.remote_config

import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName
@Keep
data class AdConfigModel(
    @SerializedName("splash_screen") val splashScreen: SplashScreenConfig? = null,
    @SerializedName("app_interstitial") val appInterstitial: InterstitialConfig? = null,
    @SerializedName("rewarded") val rewarded: BasicAdConfig? = null,
    @SerializedName("rewarded_frames") val rewardedFrames: BasicAdConfig? = null,
    @SerializedName("app_open_resume") val appOpenResume: BasicAdConfig? = null,
    @SerializedName("language_screen") val languageScreen: LanguageScreenConfig? = null,
    @SerializedName("onboarding_1_native") val onboarding1Native: NativeAdConfig? = null,
    @SerializedName("onboarding_2_native") val onboarding2Native: NativeAdConfig? = null,
    @SerializedName("onboarding_3_native") val onboarding3Native: NativeAdConfig? = null,
    @SerializedName("onboarding_4_native") val onboarding4Native: NativeAdConfig? = null,
    @SerializedName("processing_native") val processingNative: NativeAdConfig? = null,
    @SerializedName("exit_save_native") val exitSaveNative: NativeAdConfig? = null,
    @SerializedName("app_banner") val appBanner: NativeAdConfig? = null,
    @SerializedName("global_settings") val globalSettings: GlobalSettings? = null
)
@Keep
data class SplashScreenConfig(
    @SerializedName("native") val native: NativeAdConfig? = null,
    @SerializedName("interstitial") val interstitial: InterstitialConfig? = null,
    @SerializedName("app_open") val appOpen: BasicAdConfig? = null,
    @SerializedName("timeout") val timeout: Int? = null,
    @SerializedName("time") val time: Int? = null,
    @SerializedName("splash_pro_home") val splashProHome: Boolean? = null,
)
@Keep
data class InterstitialConfig(
    @SerializedName("floor") val floor: Int? = null,
    @SerializedName("start_count") val startCount: Int? = null,
    @SerializedName("after_start_count") val afterStartCount: Int? = null,
    @SerializedName("always_show") val alwaysShow: Boolean? = null,
    @SerializedName("is_enabled") val isEnabled: Boolean? = null,
    @SerializedName("ad_unit_ids") val adUnitIds: List<String>? = null
)
@Keep
data class BasicAdConfig(
    @SerializedName("floor") val floor: Int? = null,
    @SerializedName("is_enabled") val isEnabled: Boolean? = null,
    @SerializedName("ad_unit_ids") val adUnitIds: List<String>? = null
)
@Keep
data class NativeAdConfig(
    @SerializedName("is_enabled") val isEnabled: Boolean? = null,
    @SerializedName("reload_limit") val reloadLimit: Int? = null,
    @SerializedName("ad_unit_ids") val adUnitIds: List<String>? = null
)
@Keep
data class LanguageScreenConfig(
    @SerializedName("native_before_selection") val nativeBeforeSelection: NativeAdConfig? = null,
    @SerializedName("native_after_selection") val nativeAfterSelection: NativeAdConfig? = null,
    @SerializedName("interstitial") val interstitial: InterstitialConfig? = null
)
@Keep
data class GlobalSettings(
    @SerializedName("reward_ads") val rewardAds: RewardAdsConfig? = null,
    @SerializedName("button_new_flow") val buttonNewFlow: Boolean? = null,
    @SerializedName("show_valentine_pop_up") val showValentinePopUp: Boolean? = null,
    @SerializedName("tutorial_scr") val tutorialScr: Boolean? = null,
    @SerializedName("auto_scroll_time") val autoScrollTime: Int? = null,
    @SerializedName("native_reload_limit") val nativeReloadLimit: Int? = null,
    @SerializedName("native_on_resume") val nativeOnResume: Boolean? = null,
    @SerializedName("notification") val notification: NotificationConfig? = null
)
@Keep
data class RewardAdsConfig(
    @SerializedName("all_reward") val allReward: Boolean? = null,
    @SerializedName("reward_time") val rewardTime: Int? = null
)
@Keep
data class NotificationConfig(
    @SerializedName("lockscreen") val lockscreen: Boolean? = null,
    @SerializedName("lockscreen_country") val lockscreenCountry: Int? = null,
    @SerializedName("status_bar_country") val statusBarCountry: Int? = null,
    @SerializedName("time_push_1") val timePush1: Int? = null,
    @SerializedName("time_push_2") val timePush2: Int? = null
)
