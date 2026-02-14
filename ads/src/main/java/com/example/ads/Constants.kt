package com.example.ads

import com.example.ads.admobs.scripts.AppOpen
import com.example.ads.admobs.scripts.Banner
import com.example.ads.admobs.scripts.BannerBlendBoarding
import com.example.ads.admobs.scripts.BannerOnBoarding
import com.example.ads.admobs.scripts.BannerSplash
import com.example.ads.admobs.scripts.BannerSurvey
import com.example.ads.admobs.scripts.Interstitial
import com.example.ads.admobs.scripts.InterstitialNew
import com.example.ads.admobs.scripts.LargeBanner
import com.example.ads.admobs.scripts.Native
import com.example.ads.admobs.scripts.Rewarded
import com.example.ads.admobs.scripts.RewardedInterstitial
import com.example.ads.crosspromo.api.retrofit.model.CrossPromoItem
import com.google.errorprone.annotations.Keep
import com.google.firebase.analytics.FirebaseAnalytics
import com.project.common.remote_config.AdConfigModel
import java.util.concurrent.atomic.AtomicBoolean

@Keep
object Constants {

    var proSplashOrHome: Boolean = true
    var generalNotificationHideAppEnable = true
    var newAdsConfig:AdConfigModel? = null
    var enableObLastInterAd = false
    var interstitialObLastFloor = 0
    var interstitialObLastStartCount = 0
    var interstitialObLastAfterStartCount = 1
    var interstitialObLastAlwaysShow = false
    var loadNativeSplash = false
    var loadBannerSurvey = true
    var loadBannerOnBoardOne = true
    var loadBannerOnBoardTwo = true
    var loadBannerOnBoardThree = true
    var loadBannerOnBoardFour = true
    var loadBannerOnBoardMedium = false
    var loadSplashAppOpen = true
    var remoteRevenue = 0.5f
    var enableHomeInterAd = false
    var interstitialHomeFloor = 2
    var interstitialHomeStartCount = 0
    var interstitialHomeAfterStartCount = 1
    var interstitialHomeAlwaysShow = false
    var homeNewInterStrategy = true
    var galleryButtonNewFlow = true
    var proCounter = 4L

    var nativeReasonUninstall = true
    var adUnInstallNativeFloor = 4
    var interstitialUninstallFloor = 2
    var interstitialUninstallAfterStartCount = 3
    var interstitialUninstallStartCount = 1
    var interstitialUninstallAlwaysShow = true
    var flowUninstall = true
    var InterstitialUnInstall = true
    var loadInterstitialUnInstall = true

    var openTutorial = true
    var tutorialScr = true
    var bannerTutorial = true

    var interstitialBack = true
    var interstitialBackFloor = 4
    var interstitialBackStartCount = 1
    var interstitialBackAfterStartCount = 3
    var interstitialBackAlwaysShow = false

    var interstitialMyWork = true
    var interstitialMyWorkFloor = 4
    var interstitialMyWorkStartCount = 1
    var interstitialMyWorkAfterStartCount = 3
    var interstitialMyWorkAlwaysShow = false

    var interstitialSave = true
    var interstitialSaveFloor = 4
    var interstitialSaveStartCount = 1
    var interstitialSaveAfterStartCount = 3
    var interstitialSaveAlwaysShow = true

    var showBlendGuideScreen = true

    var proNewContinue = "Start Free Trial"
    var immediateInterstitial = true
    var interstitialStrategyOld = false
    var enablePopUpSave = false

    var adSelectPhoto = "native"
    var adGalleryNative = true
    var adGalleryNativeFloor = 4
    var surveyFloor = 0
    var flowSelectPhotoScr = "new"
    var surveyScreenNative = true
    var surveyScreenEnable = false

    var introScreenAdButtonPosition = "Above"
    var introScreenUiButton = "light"
    var introScreenAdUi = "full_layout_admob"
    var openScreenIntroduction = true
    var introductionFloor = 4
    var questionFloor = 4
    var openScreenNative = true

    var ADS_SDK_INITIALIZE = AtomicBoolean(false)
    var ADS_SDK_INITIALIZE_BIGO = AtomicBoolean(false)
    var CAN_LOAD_ADS = false
    var failureMsg: String? = ""
    var firebaseAnalytics: FirebaseAnalytics? = null
    var OTHER_AD_ON_DISPLAY = false
    var rewardedShown = false
    var rewardTime = 3000L
    var bnrFailCounter = 0
    var showHomeScreen = false
    var showGiftIconHome = false
    var showGiftIconFeature = false
    var offerTypeYearly = false
    var showAdAfterSplash = 2L
    var showAppOpen: Boolean = true
    var offerSession = 2L
    var showRoboPro = false
    var proScreenVariant = 0L
    var showOfferPanel = false
    var loadNativeOnResume = true
    var loadBannerOnResume = true
    var interstitialOnClick = 3L
    var bigoPopUpOnClick = 3L
    var showCollageClickAd = true
    var showAllInterstitialAd = true
    var showAllAppOpenAd = true
    var showAppOpenAd = true
    var splashTime = 500L
    var showEditorTextClickAd = true
    var showDiscardClickAd = true
    var showTemplateFrameClickAd = true
    var showSearchFrameClickAd = true
    var showRecentlyUsedFrameClickAd = true
    var showDraftFrameClickAd = true
    var showStylesFrameClickAd = true
    var showTodaySpecialFrameClickAd = true
    var showMostUsedFrameClickAd = true
    var showForYouFrameClickAd = true
    var showFavouriteFrameClickAd = true
    var showCategoriesFrameClickAd = true
    var showCategoriesFrameHomeClickAd = true
    var showCategoriesFrameTemplateClickAd = true
    var showCropAd = true
    var showCropBackAd = true
    var showNativeExit = true
    var showReplaceAd = true
    var showImageSelectionNextAd = true
    var showImageSelectionBackAd = true
    var showDraftClickAd = true
    var profilePictureShowAd: Boolean = true
    var homeMenuShowAd: Boolean = true
    var templatesMenuShowAd: Boolean = true
    var featureMenuShowAd: Boolean = true
    var styleMenuShowAd: Boolean = true
    var myWorkMenuShowAd: Boolean = true
    var blendShowAd: Boolean = true
    var effectsShowAd: Boolean = true
    var dripArtShowAd: Boolean = true
    var spiralShowAd: Boolean = true
    var roboOfferShowAd: Boolean = true
    var roboProShowAd: Boolean = true
    var nativeProcessBlend: Boolean = true
    var nativeDiscardEditor: Boolean = true

    var introScreen: Boolean = true
    var showSaveAd: Boolean = true
    var showProSave: Boolean = true
    var seeAllAdaptiveBanner: Boolean = false
    var appOpen = AppOpen()
    var appOpenBlendGuide = AppOpen()
    var appOpenSplash = AppOpen()
    var appIsActive = false
    var banner = Banner()
    var largeBanner = LargeBanner()
    var onBoardingBannerOne = BannerOnBoarding()
    var onBoardingBannerTwo = BannerOnBoarding()
    var onBoardingBannerThree = BannerOnBoarding()
    var onBoardingBannerFour = BannerOnBoarding()
 //   var mNewInterstitial = InterstitialNew()
    var mNewInterstitial: InterstitialNew? = InterstitialNew()

    var native = Native()
    var bannerSplash = BannerSplash()
    var rewarded = Rewarded()
    var bannerBlendBoarding = BannerBlendBoarding()
    var bannerSurvey = BannerSurvey()
    var rewardedInterstitial = RewardedInterstitial()
    var interstitial = Interstitial()
    var interstitialNew = InterstitialNew()
    var actionGallery = false
    var crossPromoAdsList: List<CrossPromoItem> = emptyList()
    var placement: String = ""
    var appIsForeground = false
    var needToLoadAppOpen = true
    var showAllReward = false

    var showRewardAdFavourite = false


    var showRewardAdSearch = false
    var showRewardAdFrameEditorFrames = true
    var showRewardAdPipEditorFrames = false
    var showRewardAdPhotoEditorEffects = false
    var allBannerReloadLimit = 2L


    var onBoardingOneNativeId = "ca-app-pub-4276074242154795/1085154440"
    var onBoardingTwoNativeId = "ca-app-pub-4276074242154795/4531227820"
    var onBoardingThreeNativeId = "ca-app-pub-4276074242154795/7094662041"
    var onBoardingFourNativeId = "ca-app-pub-4276074242154795/4171618250"



    var onBoardingFullOneNativeId = "ca-app-pub-4276074242154795/5811108808"
    var onBoardingFullTwoNativeId = "ca-app-pub-4276074242154795/5811108808"

    var lfoOneNativeId = "ca-app-pub-4276074242154795/8257067458"
    var lfoTwoNativeId = "ca-app-pub-4276074242154795/1097473173"
    var splashInterAdId = "ca-app-pub-4276074242154795/7571205893"






    var saveInterAdId = "ca-app-pub-4276074242154795/3006951685" //
    var nativeProcessingId = "ca-app-pub-4276074242154795/9500207846"

    var nativeDialogsId = "ca-app-pub-4276074242154795/3500941555"

    var splashTimeOut = 20000L
    var autoScrollFullNative = 6000L
    var nativeReloadLimit = 2L
    var loadNativeFullMetaLayout = true
    var loadNativeFullOne = true
    var loadNativeFullTwo = true
    var loadNativeLanguageSetting = true
    var loadNativeOld = true
    var popupEventValentine = false
    var bannerReload = true
    var loadBannerSplash = true
    var loadInterstitialSave = true
    var loadInterstitialSplash = true

    var languageCode = "en"
    var loadNativeLfOne = true
    var loadNativeLfTwo = true
    var loadNativeObOne = true
    var loadNativeObTwo = true
    var loadNativeObThree = true
    var loadNativeObFour = true
    var loadNativeForDialog = true
    var loadNativeForGalleryProcessing = false
    var loadNativeMetaLayout = false
    var loadBannerAfterFailed = false
    var questionScreenNative = true
    var questionScreenEnable = false
}