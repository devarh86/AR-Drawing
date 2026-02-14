package com.example.ads.utils

import android.app.Activity
import androidx.core.content.ContextCompat
import com.example.ads.Constants.InterstitialUnInstall
import com.example.ads.Constants.adGalleryNative
import com.example.ads.Constants.adGalleryNativeFloor
import com.example.ads.Constants.adUnInstallNativeFloor
import com.example.ads.Constants.enableHomeInterAd
import com.example.ads.Constants.enableObLastInterAd
import com.example.ads.Constants.interstitialBack
import com.example.ads.Constants.interstitialBackAfterStartCount
import com.example.ads.Constants.interstitialBackAlwaysShow
import com.example.ads.Constants.interstitialBackFloor
import com.example.ads.Constants.interstitialBackStartCount
import com.example.ads.Constants.interstitialHomeAfterStartCount
import com.example.ads.Constants.interstitialHomeAlwaysShow
import com.example.ads.Constants.interstitialHomeFloor
import com.example.ads.Constants.interstitialHomeStartCount
import com.example.ads.Constants.interstitialMyWork
import com.example.ads.Constants.interstitialMyWorkAfterStartCount
import com.example.ads.Constants.interstitialMyWorkAlwaysShow
import com.example.ads.Constants.interstitialMyWorkFloor
import com.example.ads.Constants.interstitialMyWorkStartCount
import com.example.ads.Constants.interstitialObLastAfterStartCount
import com.example.ads.Constants.interstitialObLastAlwaysShow
import com.example.ads.Constants.interstitialObLastFloor
import com.example.ads.Constants.interstitialObLastStartCount
import com.example.ads.Constants.interstitialSave
import com.example.ads.Constants.interstitialSaveAfterStartCount
import com.example.ads.Constants.interstitialSaveAlwaysShow
import com.example.ads.Constants.interstitialSaveFloor
import com.example.ads.Constants.interstitialSaveStartCount
import com.example.ads.Constants.interstitialUninstallAfterStartCount
import com.example.ads.Constants.interstitialUninstallAlwaysShow
import com.example.ads.Constants.interstitialUninstallFloor
import com.example.ads.Constants.interstitialUninstallStartCount
import com.example.ads.Constants.introScreenAdButtonPosition
import com.example.ads.Constants.introScreenAdUi
import com.example.ads.Constants.introductionFloor
import com.example.ads.Constants.lfoOneNativeId
import com.example.ads.Constants.lfoTwoNativeId
import com.example.ads.Constants.loadInterstitialSave
import com.example.ads.Constants.loadNativeForGalleryProcessing
import com.example.ads.Constants.loadNativeFullOne
import com.example.ads.Constants.loadNativeFullTwo
import com.example.ads.Constants.loadNativeLanguageSetting
import com.example.ads.Constants.loadNativeLfOne
import com.example.ads.Constants.loadNativeLfTwo
import com.example.ads.Constants.loadNativeMetaLayout
import com.example.ads.Constants.loadNativeObFour
import com.example.ads.Constants.loadNativeObOne
import com.example.ads.Constants.loadNativeObThree
import com.example.ads.Constants.loadNativeObTwo
import com.example.ads.Constants.loadNativeOld
import com.example.ads.Constants.nativeDialogsId
import com.example.ads.Constants.nativeProcessingId
import com.example.ads.Constants.nativeReasonUninstall
import com.example.ads.Constants.nativeReloadLimit
import com.example.ads.Constants.newAdsConfig
import com.example.ads.Constants.onBoardingFourNativeId
import com.example.ads.Constants.onBoardingFullOneNativeId
import com.example.ads.Constants.onBoardingFullTwoNativeId
import com.example.ads.Constants.onBoardingOneNativeId
import com.example.ads.Constants.onBoardingThreeNativeId
import com.example.ads.Constants.onBoardingTwoNativeId
import com.example.ads.Constants.openScreenNative
import com.example.ads.Constants.questionFloor
import com.example.ads.Constants.questionScreenNative
import com.example.ads.Constants.splashTimeOut
import com.example.ads.Constants.surveyFloor
import com.example.ads.Constants.surveyScreenNative
import com.example.ads.R
import com.example.ads.model.AdConfigModel
import com.example.ads.model.AdsClassification
import com.example.ads.model.InterstitialAdModel
import com.project.common.utils.setString


//new added

fun Activity.allBanner(): AdConfigModel? {
    return try {
        newAdsConfig?.appBanner?.let {
            AdConfigModel(
                idHigh = it.adUnitIds?.getOrNull(0) ?: ContextCompat.getString(this, R.string.banner_overall_highs),
                idMedium = it.adUnitIds?.getOrNull(1) ?: ContextCompat.getString(this, R.string.banner_overall_med),
                idBackUp = it.adUnitIds?.getOrNull(2) ?: ContextCompat.getString(this, R.string.banner_overall_low),
                enable = it.isEnabled ?: false,
                whichAd = AdsClassification.BANNER,
            )
        }
    } catch (ex: kotlin.Exception) {
        null
    }
}

fun Activity.nativeExitConfig(): AdConfigModel? {
    return try {
        newAdsConfig?.exitSaveNative?.let {
            AdConfigModel(
                idHigh = it.adUnitIds?.getOrNull(0) ?: ContextCompat.getString(this, R.string.exit_save_native_high),
                idMedium = it.adUnitIds?.getOrNull(1) ?: ContextCompat.getString(this, R.string.exit_save_native_medium),
                idBackUp = it.adUnitIds?.getOrNull(2) ?: ContextCompat.getString(this, R.string.exit_save_native_backup),
                reloadLimit = it.reloadLimit ?: 2,
                whichAd = AdsClassification.NATIVE,
                isAboveCtr = false,
                isMetaLayout = false,
                enable = it.isEnabled ?: false,
                adMobLayoutId = R.layout.medium_native_ad,
                currentActivityOrFragment = "EXIT"
            )
        } ?: run {
            null
        }
    } catch (ex: Exception) {
        null
    }
}

fun Activity.nativeDialogsConfig(): AdConfigModel? {
    return try {
        newAdsConfig?.processingNative?.let {
            AdConfigModel(
                idHigh = it.adUnitIds?.getOrNull(0) ?: ContextCompat.getString(this, R.string.processing_native_high),
                idMedium = it.adUnitIds?.getOrNull(1) ?: ContextCompat.getString(this, R.string.processing_native_medium),
                idBackUp = it.adUnitIds?.getOrNull(2) ?: ContextCompat.getString(this, R.string.processing_native_backup),
                reloadLimit = it.reloadLimit ?: 2,
                whichAd = AdsClassification.NATIVE,
                isAboveCtr = false,
                isMetaLayout = false,
                enable = it.isEnabled ?: false,
                adMobLayoutId = R.layout.medium_native_ad,
                currentActivityOrFragment = "Dialog"
            )
        } ?: run {
            null
        }
    } catch (ex: Exception) {
        null
    }
}

fun Activity.nativeProcessingConfigEnhancer(): AdConfigModel? {
    return try {
        newAdsConfig?.processingNative?.let {
            AdConfigModel(
                idHigh = it.adUnitIds?.getOrNull(0) ?: ContextCompat.getString(this, R.string.processing_native_high),
                idMedium = it.adUnitIds?.getOrNull(1) ?: ContextCompat.getString(this, R.string.processing_native_medium),
                idBackUp = it.adUnitIds?.getOrNull(2) ?: ContextCompat.getString(this, R.string.processing_native_backup),
                reloadLimit = it.reloadLimit ?: 2,
                whichAd = AdsClassification.NATIVE,
                isAboveCtr = false,
                isMetaLayout = false,
                enable = it.isEnabled ?: false,
                adMobLayoutId = R.layout.medium_native_ad,
                currentActivityOrFragment = "GALLERY_PROCESS"
            )
        } ?: run {
            null
        }
    } catch (ex: Exception) {
        null
    }
}


fun Activity.nativeProcessingConfig(): AdConfigModel? {
    return try {
        AdConfigModel(
            idHigh = ContextCompat.getString(this, R.string.processing_native_high),
            idMedium = ContextCompat.getString(this, R.string.processing_native_medium),
            idBackUp = ContextCompat.getString(this, R.string.processing_native_backup),
            reloadLimit = 2,
            whichAd = AdsClassification.NATIVE,
            isAboveCtr = false,
            isMetaLayout = false,
            enable = loadNativeForGalleryProcessing,
            adMobLayoutId = R.layout.layout_native_large_process,
            metaLayoutId = R.layout.layout_native_large_process,
            currentActivityOrFragment = "GALLERY_PROCESS"
        )
    } catch (ex: Exception) {
        null
    }
}

fun Activity.nativeShareConfig(): AdConfigModel? {
    return try {
        AdConfigModel(
            idHigh = ContextCompat.getString(this, R.string.processing_native_high),
            idMedium = ContextCompat.getString(this, R.string.processing_native_medium),
            idBackUp = ContextCompat.getString(this, R.string.processing_native_backup),
            reloadLimit = 2,
            whichAd = AdsClassification.NATIVE,
            isAboveCtr = false,
            isMetaLayout = false,
            enable = true,
            adMobLayoutId = R.layout.layout_native_large_onboarding,
            currentActivityOrFragment = "SHARE"
        )
    } catch (ex: Exception) {
        null
    }
}


fun Activity.interstitialBack(): AdConfigModel? {
    return try {
        AdConfigModel(
            idHigh = ContextCompat.getString(this, R.string.interstitial_back_high),
            idMedium = ContextCompat.getString(this, R.string.interstitial_back_medium),
            idBackUp = ContextCompat.getString(this, R.string.interstitial_back_all),
            reloadLimit = interstitialBackFloor,
            whichAd = AdsClassification.INTERSTITIAL,
            enable = interstitialBack,
            currentActivityOrFragment = "BACK",
            interstitialAdModel = InterstitialAdModel().apply {
                interstitialAdFirstShowCount = interstitialBackStartCount
                interstitialAdAlwaysShow = interstitialBackAlwaysShow
                interstitialAdAfterFirstShowSteps = interstitialBackAfterStartCount
                interstitialAdCurrentCounter = if (interstitialAdFirstShowCount > 0) 0 else 1
            }
        )
    } catch (ex: Exception) {
        null
    }
}

fun Activity.interstitialMyWork(): AdConfigModel? {
    return try {
        AdConfigModel(
            idHigh = ContextCompat.getString(this, R.string.interstitial_my_work_high),
            idMedium = ContextCompat.getString(this, R.string.interstitial_my_work_medium),
            idBackUp = ContextCompat.getString(this, R.string.interstitial_my_work_all),
            reloadLimit = interstitialMyWorkFloor,
            whichAd = AdsClassification.INTERSTITIAL,
            enable = interstitialMyWork,
            currentActivityOrFragment = "MYWORK",
            interstitialAdModel = InterstitialAdModel().apply {
                interstitialAdFirstShowCount = interstitialMyWorkStartCount
                interstitialAdAlwaysShow = interstitialMyWorkAlwaysShow
                interstitialAdAfterFirstShowSteps = interstitialMyWorkAfterStartCount
                interstitialAdCurrentCounter = if (interstitialAdFirstShowCount > 0) 0 else 1
            }
        )
    } catch (ex: Exception) {
        null
    }
}

fun Activity.interstitialSave(): AdConfigModel? {
    return try {
        AdConfigModel(
            idHigh = ContextCompat.getString(this, R.string.interstitial_save_high),
            idMedium = ContextCompat.getString(this, R.string.interstitial_save_medium),
            idBackUp = ContextCompat.getString(this, R.string.interstitial_save_all),
            reloadLimit = interstitialSaveFloor,
            whichAd = AdsClassification.INTERSTITIAL,
            enable = interstitialSave,
            currentActivityOrFragment = "SAVE",
            interstitialAdModel = InterstitialAdModel().apply {
                interstitialAdFirstShowCount = interstitialSaveStartCount
                interstitialAdAlwaysShow = interstitialSaveAlwaysShow
                interstitialAdAfterFirstShowSteps = interstitialSaveAfterStartCount
                interstitialAdCurrentCounter = if (interstitialAdFirstShowCount > 0) 0 else 1
            }
        )
    } catch (ex: Exception) {
        null
    }
}

fun Activity.survey(): AdConfigModel? {
    return try {
        AdConfigModel(
            idHigh = ContextCompat.getString(this, R.string.survey_native_high),
            idMedium = ContextCompat.getString(this, R.string.survey_native_medium),
            idBackUp = ContextCompat.getString(this, R.string.survey_native_all),
            reloadLimit = surveyFloor,
            whichAd = AdsClassification.NATIVE,
            isAboveCtr = false,
            isMetaLayout = false,
            enable = surveyScreenNative,
            adMobLayoutId = R.layout.layout_native_large_button_above,
            metaLayoutId = R.layout.meta_native_layout_onboarding,
            currentActivityOrFragment = "SUR_SCR"
        )
    } catch (ex: Exception) {
        null
    }
}

fun Activity.intro(): AdConfigModel? {
    return try {
        AdConfigModel(
            idHigh = ContextCompat.getString(this, R.string.open_screen_high),
            idMedium = ContextCompat.getString(this, R.string.open_screen_medium),
            idBackUp = ContextCompat.getString(this, R.string.open_screen_backup),
            reloadLimit = introductionFloor,
            whichAd = AdsClassification.NATIVE,
            isAboveCtr = false,
            isMetaLayout = false,
            enable = openScreenNative,
            adMobLayoutId = getLayoutIntro(false),
            metaLayoutId = getLayoutIntro(true),
            currentActivityOrFragment = "INTRO"
        )
    } catch (ex: Exception) {
        null
    }
}


/*config check*/

fun Activity.unInstallNative(): AdConfigModel? {
    return try {
        AdConfigModel(
            idHigh = ContextCompat.getString(this, R.string.native_uninstall_high),
            idMedium = ContextCompat.getString(this, R.string.native_uninstall_medium),
            idBackUp = ContextCompat.getString(this, R.string.native_uninstall_all),
            reloadLimit = adUnInstallNativeFloor,
            whichAd = AdsClassification.NATIVE,
            isAboveCtr = false,
            isMetaLayout = false,
            enable = nativeReasonUninstall,
            adMobLayoutId = R.layout.layout_native_small_questions,
            metaLayoutId = R.layout.layout_native_small_questions,
            currentActivityOrFragment = "UNINSTALL_NATIVE"
        )
    } catch (ex: Exception) {
        null
    }
}

fun Activity.unInstallNative2nd(): AdConfigModel? {
    return try {
        AdConfigModel(
            idHigh = ContextCompat.getString(this, R.string.native_uninstall_high),
            idMedium = ContextCompat.getString(this, R.string.native_uninstall_medium),
            idBackUp = ContextCompat.getString(this, R.string.native_uninstall_all),
            reloadLimit = adUnInstallNativeFloor,
            whichAd = AdsClassification.NATIVE,
            isAboveCtr = false,
            isMetaLayout = false,
            enable = nativeReasonUninstall,
            adMobLayoutId = getLayoutIntro(false),
            metaLayoutId = getLayoutIntro(true),
            currentActivityOrFragment = "UNINSTALL_NATIVE"
        )
    } catch (ex: Exception) {
        null
    }
}

fun Activity.galleryBottom(): AdConfigModel? {
    return try {
        AdConfigModel(
            idHigh = ContextCompat.getString(this, R.string.native_gallery_high),
            idMedium = ContextCompat.getString(this, R.string.native_gallery_medium),
            idBackUp = ContextCompat.getString(this, R.string.native_gallery_back_up),
            reloadLimit = adGalleryNativeFloor,
            whichAd = AdsClassification.NATIVE,
            isAboveCtr = false,
            isMetaLayout = false,
            enable = adGalleryNative,
            adMobLayoutId = R.layout.layout_native_small_questions,
            metaLayoutId = R.layout.layout_native_small_questions,
            currentActivityOrFragment = "GALLERY"
        )
    } catch (ex: Exception) {
        null
    }
}


fun getLayoutIntro(isMeta: Boolean): Int {

    val layoutAdmob = if (introScreenAdButtonPosition == "Above")
        R.layout.layout_native_large_button_above_intro
    else {
        R.layout.layout_native_large_intro
    }
    val layoutMeta = if (introScreenAdButtonPosition == "Above")
        R.layout.meta_native_layout_intro
    else {
        R.layout.meta_native_layout_intro_bottom
    }
    return if (!isMeta) {
        if (introScreenAdUi == "full_layout_meta") {
            layoutMeta
        } else {
            layoutAdmob
        }
    } else {
        if (introScreenAdUi == "full_layout_admob") {
            layoutAdmob
        } else {
            layoutMeta
        }
    }
}

fun Activity.question(): AdConfigModel? {
    return try {
        AdConfigModel(
            idHigh = ContextCompat.getString(this, R.string.open_screen_high),
            idMedium = ContextCompat.getString(this, R.string.open_screen_medium),
            idBackUp = ContextCompat.getString(this, R.string.open_screen_backup),
            reloadLimit = questionFloor,
            whichAd = AdsClassification.NATIVE,
            isAboveCtr = false,
            isMetaLayout = false,
            enable = questionScreenNative,
            adMobLayoutId = R.layout.layout_native_small_questions,
            metaLayoutId = R.layout.meta_native_layout_onboarding,
            currentActivityOrFragment = "QUES_SCR"
        )
    } catch (ex: Exception) {
        null
    }
    //  adMobLayoutId = R.layout.layout_native_large_button_above,
}


fun Activity.nativeLanguageSetting(): AdConfigModel? {
    return try {
        AdConfigModel(
            idHigh = ContextCompat.getString(this, R.string.native_high_setting_language),
            idMedium = ContextCompat.getString(this, R.string.native_high_setting_medium),
            idBackUp = ContextCompat.getString(this, R.string.native_high_setting_backup),
            reloadLimit = nativeReloadLimit.toInt(),
            whichAd = AdsClassification.NATIVE,
            isAboveCtr = false,
            isMetaLayout = loadNativeMetaLayout,
            enable = loadNativeLanguageSetting,
            adMobLayoutId = if (loadNativeOld) R.layout.layout_native_large_onboarding_old else R.layout.layout_native_large_onboarding,
            metaLayoutId = R.layout.meta_native_layout_onboarding,
            currentActivityOrFragment = "LF_SETTING"
        )
    } catch (ex: Exception) {
        null
    }
}

fun nativeSplash(): AdConfigModel? {
    return try {
        // DEBUG ID:: ca-app-pub-3940256099942544/2247696110

        // RELEASE ID:: idHigh = ContextCompat.getString(this, R.string.splash_native_high),
        //              idMedium = ContextCompat.getString(this, R.string.splash_native_medium),
        //              idBackUp = ContextCompat.getString(this, R.string.splash_native_backup),

        AdConfigModel(
            idHigh = "ca-app-pub-3940256099942544/2247696110",
            idMedium = "ca-app-pub-3940256099942544/2247696110",
            idBackUp = "ca-app-pub-3940256099942544/2247696110",
            reloadLimit = 2,
            whichAd = AdsClassification.NATIVE,
            isAboveCtr = false,
            isMetaLayout = loadNativeMetaLayout,
            enable = true,
            adMobLayoutId = if (loadNativeOld) R.layout.layout_native_large_onboarding_old else R.layout.layout_native_large_onboarding,
            metaLayoutId = R.layout.meta_native_layout_onboarding,
            currentActivityOrFragment = "nativeSplash"
        )

    } catch (ex: Exception) {
        null
    }
}

fun Activity.nativeLanguageOne(): AdConfigModel? {
    return try {
        newAdsConfig?.languageScreen?.nativeBeforeSelection?.let {
            AdConfigModel(
                idHigh = lfoOneNativeId,
                idMedium = it.adUnitIds?.getOrNull(1) ?: ContextCompat.getString(this, R.string.native_language_medium),
                idBackUp = it.adUnitIds?.getOrNull(2) ?: ContextCompat.getString(this, R.string.native_language_back_up),
                reloadLimit = it.reloadLimit ?: nativeReloadLimit.toInt(),
                whichAd = AdsClassification.NATIVE,
                isAboveCtr = false,
                isMetaLayout = loadNativeMetaLayout,
                enable = loadNativeLfOne,
                adMobLayoutId = if (loadNativeOld) R.layout.layout_native_large_onboarding_old else R.layout.layout_native_large_onboarding,
                metaLayoutId = R.layout.meta_native_layout_onboarding,
                currentActivityOrFragment = "LFO1"
            )
        } ?: run {
            null
        }

    } catch (ex: Exception) {
        null
    }
}

fun Activity.nativeLanguageTwo(): AdConfigModel? {
    return try {
        newAdsConfig?.languageScreen?.nativeAfterSelection?.let {
            AdConfigModel(
                idHigh = lfoTwoNativeId,
                idMedium = it.adUnitIds?.getOrNull(1) ?: ContextCompat.getString(this, R.string.native_language_alt_medium),
                idBackUp = it.adUnitIds?.getOrNull(2) ?: ContextCompat.getString(this, R.string.native_language_alt_back_up),
                reloadLimit = it.reloadLimit ?: nativeReloadLimit.toInt(),
                whichAd = AdsClassification.NATIVE,
                isAboveCtr = false,
                isMetaLayout = loadNativeMetaLayout,
                enable = loadNativeLfTwo,
                adMobLayoutId = if (loadNativeOld) R.layout.layout_native_large_onboarding_old else R.layout.layout_native_large_onboarding,
                metaLayoutId = R.layout.meta_native_layout_onboarding,
                currentActivityOrFragment = "LFO2"
            )
        } ?: run {
            null
        }
    } catch (ex: Exception) {
        null
    }
}

fun Activity.onBoardNativeOne(): AdConfigModel? {
    return try {
        newAdsConfig?.onboarding1Native?.let {
            AdConfigModel(
                idHigh = onBoardingOneNativeId,
                idMedium = it.adUnitIds?.getOrNull(1) ?: ContextCompat.getString(this, R.string.on_boarding_one_medium),
                idBackUp = it.adUnitIds?.getOrNull(2) ?: ContextCompat.getString(this, R.string.on_boarding_one_back_up),
                reloadLimit = it.reloadLimit ?: nativeReloadLimit.toInt(),
                whichAd = AdsClassification.NATIVE,
                isAboveCtr = false,
                isMetaLayout = loadNativeMetaLayout,
                enable = loadNativeObOne,
                adMobLayoutId = if (loadNativeOld) R.layout.layout_native_large_onboarding_old else R.layout.layout_native_large_onboarding,
                metaLayoutId = R.layout.meta_native_layout_onboarding,
                currentActivityOrFragment = "OB1"
            )
        } ?: run {
            null
        }

    } catch (ex: Exception) {
        null
    }
}

fun Activity.onBoardNativeTwo(): AdConfigModel? {
    return try {
        newAdsConfig?.onboarding2Native?.let {
            AdConfigModel(
                idHigh = onBoardingTwoNativeId,
                idMedium = it.adUnitIds?.getOrNull(1) ?: ContextCompat.getString(this, R.string.on_boarding_two_medium),
                idBackUp = it.adUnitIds?.getOrNull(2) ?: ContextCompat.getString(this, R.string.on_boarding_two_back_up),
                reloadLimit = it.reloadLimit ?: nativeReloadLimit.toInt(),
                whichAd = AdsClassification.NATIVE,
                isAboveCtr = false,
                isMetaLayout = loadNativeMetaLayout,
                enable = loadNativeObTwo,
                adMobLayoutId = if (loadNativeOld) R.layout.layout_native_large_onboarding_old else R.layout.layout_native_large_onboarding,
                metaLayoutId = R.layout.meta_native_layout_onboarding,
                currentActivityOrFragment = "OB2"
            )
        } ?: run {
            null
        }

    } catch (ex: Exception) {
        null
    }
}

fun Activity.onBoardNativeThree(): AdConfigModel? {
    return try {
        newAdsConfig?.onboarding3Native?.let {
            AdConfigModel(
                idHigh = onBoardingThreeNativeId,
                idMedium = it.adUnitIds?.getOrNull(1) ?: ContextCompat.getString(this, R.string.on_boarding_three_medium),
                idBackUp = it.adUnitIds?.getOrNull(2) ?: ContextCompat.getString(this, R.string.on_boarding_three_back_up),
                reloadLimit = it.reloadLimit ?: nativeReloadLimit.toInt(),
                whichAd = AdsClassification.NATIVE,
                isAboveCtr = false,
                isMetaLayout = loadNativeMetaLayout,
                enable = loadNativeObThree,
                adMobLayoutId = if (loadNativeOld) R.layout.layout_native_large_onboarding_old else R.layout.layout_native_large_onboarding,
                metaLayoutId = R.layout.meta_native_layout_onboarding,
                currentActivityOrFragment = "OB3"
            )
        } ?: run {
            null
        }

    } catch (ex: Exception) {
        null
    }
}

fun Activity.onBoardNativeFour(): AdConfigModel? {
    return try {
        newAdsConfig?.onboarding4Native?.let {
            AdConfigModel(
                idHigh = onBoardingFourNativeId,
                idMedium = it.adUnitIds?.getOrNull(1) ?: ContextCompat.getString(this, R.string.on_boarding_four_medium),
                idBackUp = it.adUnitIds?.getOrNull(2) ?: ContextCompat.getString(this, R.string.on_boarding_four_back_up),
                reloadLimit = it.reloadLimit ?: nativeReloadLimit.toInt(),
                whichAd = AdsClassification.NATIVE,
                isAboveCtr = false,
                isMetaLayout = loadNativeMetaLayout,
                enable = loadNativeObFour,
                adMobLayoutId = if (loadNativeOld) R.layout.layout_native_large_onboarding_old else R.layout.layout_native_large_onboarding,
                metaLayoutId = R.layout.meta_native_layout_onboarding,
                currentActivityOrFragment = "OB4"
            )
        } ?: run {
            null
        }

    } catch (ex: Exception) {
        null
    }
}

fun Activity.fullNativeOne(): AdConfigModel? {
    return try {
        AdConfigModel(
            idHigh = onBoardingFullOneNativeId,
            idMedium = ContextCompat.getString(this, R.string.full_native_on_board_medium),
            idBackUp = ContextCompat.getString(this, R.string.full_native_on_board_backup),
            reloadLimit = nativeReloadLimit.toInt(),
            whichAd = AdsClassification.NATIVE,
            isAboveCtr = false,
            isMetaLayout = loadNativeMetaLayout,
            enable = loadNativeFullOne,
            adMobLayoutId = R.layout.custom_native_admob_free_size,
            metaLayoutId = R.layout.custom_native_admob_free_size_meta,
            currentActivityOrFragment = "FN1"
        )
    } catch (ex: Exception) {
        null
    }
}

fun Activity.fullNativeTwo(): AdConfigModel? {
    return try {
        AdConfigModel(
            idHigh = onBoardingFullTwoNativeId,
            idMedium = ContextCompat.getString(this, R.string.full_native_on_board_medium),
            idBackUp = ContextCompat.getString(this, R.string.full_native_on_board_backup),
            reloadLimit = nativeReloadLimit.toInt(),
            whichAd = AdsClassification.NATIVE,
            isAboveCtr = false,
            isMetaLayout = loadNativeMetaLayout,
            enable = loadNativeFullTwo,
            adMobLayoutId = R.layout.custom_native_admob_free_size,
            metaLayoutId = R.layout.custom_native_admob_free_size_meta,
            currentActivityOrFragment = "FN2"
        )
    } catch (ex: Exception) {
        null
    }
}

fun Activity.unInstallInterstitial(): AdConfigModel? {
    return try {
        AdConfigModel(
            idHigh = ContextCompat.getString(this, R.string.interstitial_uninstall_high),
            idMedium = ContextCompat.getString(this, R.string.interstitial_uninstall_medium),
            idBackUp = ContextCompat.getString(this, R.string.interstitial_uninstall_all),
            enable = InterstitialUnInstall,
            reloadLimit = interstitialUninstallFloor,
            whichAd = AdsClassification.INTERSTITIAL,
            currentActivityOrFragment = "UNINSTALL",
            interstitialAdModel = InterstitialAdModel().apply {
                interstitialAdFirstShowCount = interstitialUninstallStartCount
                interstitialAdAlwaysShow = interstitialUninstallAlwaysShow
                interstitialAdAfterFirstShowSteps = interstitialUninstallAfterStartCount
                interstitialAdCurrentCounter = if (interstitialAdFirstShowCount > 0) 0 else 1
            }
        )
    } catch (ex: Exception) {
        null
    }
}

fun Activity.homeInterstitial(): AdConfigModel? {
    return try {
        val interAd = newAdsConfig?.appInterstitial
        interAd?.let {
            AdConfigModel(
                idHigh = it.adUnitIds?.getOrNull(0) ?: ContextCompat.getString(this, R.string.all_inter_high),
                idMedium = it.adUnitIds?.getOrNull(1) ?: ContextCompat.getString(this, R.string.all_inter_medium),
                idBackUp = it.adUnitIds?.getOrNull(2) ?: ContextCompat.getString(this, R.string.all_inter_backup),
                enable = enableHomeInterAd,
                reloadLimit = interstitialHomeFloor,
                whichAd = AdsClassification.INTERSTITIAL,
                currentActivityOrFragment = "ALL_INTER",
                interstitialAdModel = InterstitialAdModel().apply {
                    interstitialAdFirstShowCount = interstitialHomeStartCount
                    interstitialAdAlwaysShow = interstitialHomeAlwaysShow
                    interstitialAdAfterFirstShowSteps = interstitialHomeAfterStartCount
                    interstitialAdCurrentCounter = if (interstitialAdFirstShowCount > 0) 0 else 1
                }
            )
        }
    } catch (ex: Exception) {
        null
    }
}


fun obLastInterstitial(): AdConfigModel? {
    return try {
        AdConfigModel(
            idHigh = "ca-app-pub-4276074242154795/8046822382",
            idMedium = "ca-app-pub-4276074242154795/6971228643",
            idBackUp = "ca-app-pub-4276074242154795/3930966483",
            enable = enableObLastInterAd,
            reloadLimit = interstitialObLastFloor,
            whichAd = AdsClassification.INTERSTITIAL,
            currentActivityOrFragment = "OB_LAST",
            interstitialAdModel = InterstitialAdModel().apply {
                interstitialAdFirstShowCount = interstitialObLastStartCount
                interstitialAdAlwaysShow = interstitialObLastAlwaysShow
                interstitialAdAfterFirstShowSteps = interstitialObLastAfterStartCount
                interstitialAdCurrentCounter = if (interstitialAdFirstShowCount > 0) 0 else 1
            }
        )
    } catch (ex: Exception) {
        null
    }
}


//old
fun Activity.saveInterstitial(): AdConfigModel? {
    return try {
        AdConfigModel(
            idHigh = ContextCompat.getString(this, R.string.save_inter_high),
            idMedium = ContextCompat.getString(this, R.string.save_inter_medium),
            idBackUp = ContextCompat.getString(this, R.string.save_inter_backup),
            enable = loadInterstitialSave,
            whichAd = AdsClassification.INTERSTITIAL,
        )
    } catch (ex: Exception) {
        null
    }
}

fun Activity.languageInterstitial(): AdConfigModel? {
    return try {
        val interAd = newAdsConfig?.languageScreen?.interstitial
        interAd?.let {
            AdConfigModel(
                idHigh = it.adUnitIds?.getOrNull(0) ?: ContextCompat.getString(this, R.string.lang_inter_high),
                idMedium = it.adUnitIds?.getOrNull(1) ?: ContextCompat.getString(this, R.string.lang_inter_medium),
                idBackUp = it.adUnitIds?.getOrNull(2) ?: ContextCompat.getString(this, R.string.lang_inter_backup),
                enable = it.isEnabled == true,
                reloadLimit = it.floor ?: 2,
                whichAd = AdsClassification.INTERSTITIAL,
                currentActivityOrFragment = "LANGUAGE_INTER",
                interstitialAdModel = InterstitialAdModel().apply {
                    interstitialAdFirstShowCount = it.startCount ?: 1
                    interstitialAdAlwaysShow = it.alwaysShow ?: false
                    interstitialAdAfterFirstShowSteps = it.afterStartCount ?: 1
                    interstitialAdCurrentCounter = if (interstitialAdFirstShowCount > 0) 0 else 1
                }
            )
        }

    } catch (ex: Exception) {
        null
    }
}

fun Activity.splashInterstitial(): AdConfigModel? {
    return try {
        val interAd = newAdsConfig?.splashScreen?.interstitial
        interAd?.let {
//            Log.i("SplashIDSINTER", "splashInterstitial: ${it.adUnitIds?.get(0)}   ${it.adUnitIds?.get(1)}  ${
//                it.adUnitIds?.get(2)}  ${ it.isEnabled }  ${ it.floor }  ${ it.startCount }  ${ it.afterStartCount }  ${ it.alwaysShow }")
            AdConfigModel(
                idHigh = it.adUnitIds?.getOrNull(0) ?: ContextCompat.getString(this, R.string.splash_inter_high),
                idMedium = it.adUnitIds?.getOrNull(1) ?: ContextCompat.getString(this, R.string.splash_inter_medium),
                idBackUp = it.adUnitIds?.getOrNull(2) ?: ContextCompat.getString(this, R.string.splash_inter_backup),
                enable = it.isEnabled == true,
                reloadLimit = it.floor ?: 2,
                whichAd = AdsClassification.INTERSTITIAL,
                currentActivityOrFragment = "SPLASH",
                interstitialAdModel = InterstitialAdModel().apply {
                    interstitialAdFirstShowCount = it.startCount ?: 1
                    interstitialAdAlwaysShow = it.alwaysShow ?: false
                    interstitialAdAfterFirstShowSteps = it.afterStartCount ?: 1
                    interstitialAdCurrentCounter = 0
                    waitTime = (newAdsConfig?.splashScreen?.timeout ?: splashTimeOut).toLong()
                }
            )
        }
//        AdConfigModel(
//            idHigh = ContextCompat.getString(this, R.string.splash_inter_high),
//            idMedium =  ContextCompat.getString(this, R.string.splash_inter_medium),
//            idBackUp = ContextCompat.getString(this, R.string.splash_inter_backup),
//            enable = true,
//            reloadLimit = 2,
//            whichAd = AdsClassification.INTERSTITIAL,
//            currentActivityOrFragment = "SPLASH",
//            interstitialAdModel = InterstitialAdModel().apply {
//                interstitialAdFirstShowCount =1
//                interstitialAdAlwaysShow = false
//                interstitialAdAfterFirstShowSteps =1
//                interstitialAdCurrentCounter = 0
//                waitTime = (newAdsConfig?.splashScreen?.timeout?: splashTimeOut).toLong()
//            }
//        )

    } catch (ex: java.lang.Exception) {
        null
    }
}


fun Activity?.nativeOnProcessing(): Pair<String, String> {
    return this?.let {
        try {
            Pair(nativeProcessingId, setString(R.string.processing_native_backup))
        } catch (ex: Exception) {
            Pair(nativeProcessingId, "")
        }
    } ?: Pair(nativeProcessingId, "")
}

fun Activity?.nativeForDialogs(): Pair<String, String> {
    return this?.let {
        try {
            Pair(nativeDialogsId, setString(com.example.ads.R.string.dialog_native_backup))
        } catch (ex: Exception) {
            Pair(nativeProcessingId, "")
        }
    } ?: Pair(nativeProcessingId, "")
}

