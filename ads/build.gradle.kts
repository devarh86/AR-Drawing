plugins {
    id("com.android.library")
    id("org.jetbrains.kotlin.android")
    id("kotlin-kapt")
    id("com.google.dagger.hilt.android")
    id("org.jetbrains.kotlin.plugin.compose")
}

android {
    namespace = "com.example.ads"
    compileSdk = 35

    defaultConfig {
        minSdk = 24
        targetSdk = 35

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        consumerProguardFiles("consumer-rules.pro")
    }

    buildTypes {
        debug {
            isMinifyEnabled = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
// activity?.showRewarded( search this in whole project  also remove interstitial

            resValue("string", "splash_inter_high", "ca-app-pub-4276074242154795/7571205893")
            resValue("string", "splash_inter_medium", "ca-app-pub-4276074242154795/6148127797")
            resValue("string", "splash_inter_backup", "ca-app-pub-4276074242154795/2320563423")



            resValue("string", "lang_inter_high", "ca-app-pub-4276074242154795/8856652811")
            resValue("string", "lang_inter_medium", "ca-app-pub-4276074242154795/8688846445")
            resValue("string", "lang_inter_backup", "ca-app-pub-4276074242154795/4643474436")


            resValue("string", "all_inter_high", "ca-app-pub-4276074242154795/9519088369")
            resValue("string", "all_inter_medium", "ca-app-pub-4276074242154795/4302969446")
            resValue("string", "all_inter_backup", "ca-app-pub-4276074242154795/5329110653")

            resValue("string", "started_inter_high", "ca-app-pub-4276074242154795/4376897731")
            resValue("string", "started_inter_medium", "ca-app-pub-4276074242154795/9862934809")
            resValue("string", "started_inter_backup", "ca-app-pub-4276074242154795/3311228076")

            resValue("string", "splash_native_high", "ca-app-pub-4276074242154795/1871498798")
            resValue("string", "splash_native_medium", "ca-app-pub-4276074242154795/6014047498")
            resValue("string", "splash_native_backup", "ca-app-pub-4276074242154795/3870976174")


            resValue("string", "resume_app_open_high", "ca-app-pub-4276074242154795/3974762424")
            resValue("string", "resume_app_open_medium", "ca-app-pub-4276074242154795/4732761587")
            resValue("string", "resume_app_open_low", "ca-app-pub-4276074242154795/4981432777")

            resValue("string", "reward_high", "ca-app-pub-4276074242154795/2617884817")
            resValue("string", "reward_medium", "ca-app-pub-4276074242154795/3412091739")
            resValue("string", "reward_low", "ca-app-pub-4276074242154795/6861733220")


            //Rewarded_Unlock1 this id is used for reward frames unlock
            resValue("string", "reward_frames_high", "ca-app-pub-4276074242154795/4759993383")
            resValue("string", "reward_frames_medium", "ca-app-pub-4276074242154795/8494497910")
            resValue("string", "reward_frames_low", "ca-app-pub-4276074242154795/2133830042")




            resValue("string", "splash_app_open_high", "ca-app-pub-4276074242154795/5403673406")
            resValue("string", "splash_app_open_medium", "ca-app-pub-4276074242154795/1294690396")
            resValue("string", "splash_app_open_low", "ca-app-pub-4276074242154795/7668527059")



            resValue("string", "app_id", "ca-app-pub-4276074242154795~9558261597")
            resValue("string", "app_open", "ca-app-pub-4276074242154795/5352522071")
            resValue("string", "banner", "ca-app-pub-3940256099942544/9214589741")
            resValue("string", "collapsable_banner", "ca-app-pub-4276074242154795/6701610537")
            resValue("string", "collapsable_banner_floor", "ca-app-pub-4276074242154795/4230697159")

            resValue("string", "native_gallery_back_up", "ca-app-pub-4276074242154795/6965907177")
            resValue("string", "native_gallery_medium", "ca-app-pub-4276074242154795/4360791104")
            resValue("string", "native_gallery_high", "ca-app-pub-4276074242154795/8407777513")
            resValue("string", "open_guide_back_up", "ca-app-pub-4276074242154795/8824382350")

            resValue("string", "native_uninstall_all", "ca-app-pub-4276074242154795/7153483120")
            resValue("string", "native_uninstall_medium", "ca-app-pub-4276074242154795/8526274547")
            resValue("string", "native_uninstall_high", "ca-app-pub-4276074242154795/1449730305")

            resValue("string", "large_banner_new", "ca-app-pub-3940256099942544/9214589741")
            resValue("string", "banner_new_floor", "ca-app-pub-3940256099942544/9214589741")
            resValue("string", "large_banner", "ca-app-pub-3940256099942544/9214589741")
            resValue("string", "survey_native_backup", "ca-app-pub-4276074242154795/3076408872")
            resValue("string", "open_screen_high", "ca-app-pub-4276074242154795/2775634311")
            resValue("string", "open_screen_medium", "ca-app-pub-4276074242154795/3076408872")
            resValue("string", "open_screen_backup", "ca-app-pub-4276074242154795/4564224676")
            resValue("string", "inline_banner", "ca-app-pub-4276074242154795/4176177401")
            resValue("string", "rewarded_interstitial", "ca-app-pub-4276074242154795/9445037725")
            resValue("string", "interstitial", "ca-app-pub-4276074242154795/9935225147")
            resValue("string", "interstitial_back_all", "ca-app-pub-4276074242154795/3930966483")
            resValue("string", "interstitial_back_medium", "ca-app-pub-4276074242154795/6971228643")
            resValue("string", "interstitial_back_high", "ca-app-pub-4276074242154795/8046822382")

            resValue("string", "interstitial_uninstall_all", "ca-app-pub-4276074242154795/9882991773")
            resValue("string", "interstitial_uninstall_medium", "ca-app-pub-4276074242154795/9520626162")
            resValue("string", "interstitial_uninstall_high", "ca-app-pub-4276074242154795/8666709490")

            resValue("string", "interstitial_my_work_all", "ca-app-pub-4276074242154795/5867913150")
            resValue("string", "interstitial_my_work_medium", "ca-app-pub-4276074242154795/9005383829")
            resValue("string", "interstitial_my_work_high", "ca-app-pub-4276074242154795/6382988287")
            resValue("string", "interstitial_save_all", "ca-app-pub-4276074242154795/3930966483")
            resValue("string", "interstitial_save_medium", "ca-app-pub-4276074242154795/6971228643")
            resValue("string", "interstitial_save_high", " ca-app-pub-4276074242154795/8046822382")
            resValue("string", "interstitial_splash", " ca-app-pub-4276074242154795/2296387706")
            resValue("string", "static_interstitial", "")
//            resValue("string", "rewarded", "ca-app-pub-4276074242154795/2617884817")
            resValue("string", "native_advanced_video", "ca-app-pub-4276074242154795/4068176010")
            resValue("string", "bigo_app_id", "10994727")
//            resValue("string", "bigo_interstitial", "10182906-10158798")
//            resValue("string", "bigo_banner", "10182906-10156618")
//            resValue("string", "bigo_rewarded", "10182906-10001431")
            resValue("string", "bigo_popup", "10994727-10821290")
//            resValue("string", "bigo_native", "10182906-10087503")
//            resValue("string", "bigo_splash_id", "10182906-10158798")


            /* facebook app id for singular*/
            resValue("string", "facebook_id", "1116803502100807")

            /*new ad ids*/
            resValue("string", "native_language_back_up", "ca-app-pub-4276074242154795/8582719444")
            resValue("string", "native_language_alt_back_up", "ca-app-pub-4276074242154795/3419679911")





            resValue("string", "on_boarding_four_medium", "ca-app-pub-4276074242154795/1078551057")
            resValue("string", "on_boarding_four_back_up", "ca-app-pub-4276074242154795/2733030612")

            resValue("string", "banner_splash_all", "ca-app-pub-4276074242154795/9559746150")
            resValue("string", "banner_splash_medium", "ca-app-pub-4276074242154795/3324717704")
            resValue("string", "interstitial_splash_back_up", "ca-app-pub-4276074242154795/2296387706")

            resValue("string", "native_backup_id", "ca-app-pub-4276074242154795/9500207846")
            resValue("string", "banner_splash_high", "ca-app-pub-4276074242154795/7195679958")

            resValue("string", "banner_on_board_second_id", "ca-app-pub-4276074242154795/6306976403")
            resValue("string", "interstitial_splash_medium", "ca-app-pub-4276074242154795/8385472699")

            resValue("string", "banner_editor", "")
            resValue("string", "banner_editor_medium", "")
            resValue("string", "banner_editor_backup", "")

            resValue("string", "banner_second_id", "ca-app-pub-4276074242154795/7564138209")
            resValue("string", "banner_medium", "  ca-app-pub-4276074242154795/6932618784")

            resValue("string", "save_inter_high", "ca-app-pub-4276074242154795/8591243404")
            resValue("string", "save_inter_medium", "ca-app-pub-4276074242154795/9369138450")
            resValue("string", "save_inter_backup", "ca-app-pub-4276074242154795/1832170033")




            resValue("string", "guidescrn_banner_all", "ca-app-pub-4276074242154795/2544785166")
            resValue("string", "guidescrn_banner_medium", "ca-app-pub-4276074242154795/8279966185")
            resValue("string", "guidescrn_banner_high", "ca-app-pub-4276074242154795/1231703496")

            resValue("string", "home_banner_backup", "ca-app-pub-4276074242154795/7564138209")
            resValue("string", "home_banner_medium", "ca-app-pub-4276074242154795/9200723312")//12
            resValue("string", "home_large_banner_high", "ca-app-pub-4276074242154795/6494107841")//41


            resValue("string", "onboard_one_medium", "ca-app-pub-4276074242154795/9322876771")
            resValue("string", "onboard_two_medium", "ca-app-pub-4276074242154795/6689247649")
            resValue("string", "onboard_three_medium", "ca-app-pub-4276074242154795/9518419520")
            resValue("string", "onboard_full_medium", "ca-app-pub-4276074242154795/5811108808")


//            resValue("string", "banner_bottom_backup", "ca-app-pub-4276074242154795/1261661145")
//            resValue("string", "banner_bottom_medium", "ca-app-pub-4276074242154795/6932618784")

            resValue("string", "banner_bottom_backup", "ca-app-pub-3940256099942544/9214589741")
            resValue("string", "banner_bottom_medium", "ca-app-pub-3940256099942544/9214589741")//436


            resValue("string", "banner_bottom_high", "ca-app-pub-4276074242154795/6313570053")


            /*updated ids with latest work*/

            // exit and processing dialogs
            resValue("string", "native_advanced_video_backup", "ca-app-pub-4276074242154795/6975873202")
            resValue("string", "native_advanced_video_medium", "ca-app-pub-4276074242154795/2583445708")
            resValue("string", "native_advanced_video_high", "ca-app-pub-4276074242154795/3500941555")

            //language screen1 ids
            resValue("string", "native_language_back_up", "ca-app-pub-4276074242154795/3891712383")
            resValue("string", "native_language_medium", "ca-app-pub-4276074242154795/9553843423")

            //language screen2 ids
            resValue("string", "native_high_setting_language", "ca-app-pub-4276074242154795/8193901008")
            resValue("string", "native_high_setting_medium", "ca-app-pub-4276074242154795/4689598825")
            resValue("string", "native_high_setting_backup", "ca-app-pub-4276074242154795/2392809792")


            //nativeLanguageTwo
            resValue("string", "native_language_alt_medium", "ca-app-pub-4276074242154795/6409354074")


            //onBoarding one ids
            resValue("string", "on_boarding_one_back_up", "ca-app-pub-4276074242154795/4206147974")
            resValue("string", "on_boarding_one_medium", "ca-app-pub-4276074242154795/6592638165")

            //onBoardingTwo ids
            resValue("string", "on_boarding_two_medium", "ca-app-pub-4276074242154795/2893066300")
            resValue("string", "on_boarding_two_back_up", "ca-app-pub-4276074242154795/1579984639")

            //onBoardingThree ids
            resValue("string", "on_boarding_three_medium", "ca-app-pub-4276074242154795/3255387759")
            resValue("string", "on_boarding_three_back_up", "ca-app-pub-4276074242154795/1299308257")

            //fullNative onBoarding ids
            resValue("string", "full_native_on_board_medium", "ca-app-pub-4276074242154795/5811108808")
            resValue("string", "full_native_on_board_backup", "ca-app-pub-4276074242154795/8766646458")

            //nativeDialog ids
            resValue("string", "dialog_native_backup", "ca-app-pub-4276074242154795/6975873202")
            resValue("string", "dialog_native_medium", "ca-app-pub-4276074242154795/2583445708")
            resValue("string", "dialog_native_high", "ca-app-pub-4276074242154795/3500941555")

            //exit and save native id
            resValue("string", "exit_save_native_high", "ca-app-pub-4276074242154795/4610608126")
            resValue("string", "exit_save_native_medium", "ca-app-pub-4276074242154795/5306836023")
            resValue("string", "exit_save_native_backup", "ca-app-pub-4276074242154795/3993754358")
            //all banner ad
            resValue("string", "banner_overall_highs", "ca-app-pub-4276074242154795/6171101804")
            resValue("string", "banner_overall_med", "ca-app-pub-4276074242154795/9918775128")
            resValue("string", "banner_overall_low", "ca-app-pub-4276074242154795/3110372936")


            //nativeInProcessDialog ids
            resValue("string", "processing_native_high", "ca-app-pub-4276074242154795/4610608126")
            resValue("string", "processing_native_medium", "ca-app-pub-4276074242154795/5306836023")
            resValue("string", "processing_native_backup", "ca-app-pub-4276074242154795/3993754358")


            resValue("string", "survey_native_all", "ca-app-pub-4276074242154795/7891421120")
            resValue("string", "survey_native_medium", "ca-app-pub-4276074242154795/6822915870")
            resValue("string", "survey_native_high", "ca-app-pub-4276074242154795/3054227104")

            resValue("string", "banner_onboarding_high", "ca-app-pub-4276074242154795/8196257978")
            resValue("string", "banner_onboarding_medium", "ca-app-pub-4276074242154795/8367825977")
            resValue("string", "banner_onboarding_all", "ca-app-pub-4276074242154795/7656329786")
        }

        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )




            resValue("string", "splash_inter_high", "")
            resValue("string", "splash_inter_medium", "")
            resValue("string", "splash_inter_backup", "")



            resValue("string", "on_boarding_four_medium", "ca-app-pub-4276074242154795/1078551057")
            resValue("string", "on_boarding_four_back_up", "ca-app-pub-4276074242154795/2733030612")


            resValue("string", "lang_inter_high", "ca-app-pub-4276074242154795/1097473173")
            resValue("string", "lang_inter_medium", "ca-app-pub-4276074242154795/9526627533")
            resValue("string", "lang_inter_backup", "ca-app-pub-4276074242154795/8584370512")


            resValue("string", "splash_native_high", "ca-app-pub-4276074242154795/1871498798")
            resValue("string", "splash_native_medium", "ca-app-pub-4276074242154795/6014047498")
            resValue("string", "splash_native_backup", "ca-app-pub-4276074242154795/3870976174")

            resValue("string", "native_language_back_up", "ca-app-pub-4276074242154795/8462733975")
            resValue("string", "native_language_alt_back_up", "ca-app-pub-4276074242154795/3419679911")


            resValue("string", "all_inter_high", "ca-app-pub-4276074242154795/9519088369")
            resValue("string", "all_inter_medium", "ca-app-pub-4276074242154795/4302969446")
            resValue("string", "all_inter_backup", "ca-app-pub-4276074242154795/5329110653")

            resValue("string", "started_inter_high", "ca-app-pub-4276074242154795/4376897731")
            resValue("string", "started_inter_medium", "ca-app-pub-4276074242154795/9862934809")
            resValue("string", "started_inter_backup", "ca-app-pub-4276074242154795/3311228076")


            resValue("string", "resume_app_open_high", "ca-app-pub-4276074242154795/3974762424")
            resValue("string", "resume_app_open_medium", "ca-app-pub-4276074242154795/4732761587")
            resValue("string", "resume_app_open_low", "ca-app-pub-4276074242154795/4981432777")

            resValue("string", "reward_high", "ca-app-pub-4276074242154795/4109691467")
            resValue("string", "reward_medium", "ca-app-pub-4276074242154795/6368915497")
            resValue("string", "reward_low", "ca-app-pub-4276074242154795/5560721546")

            //Rewarded_Unlock1 this id is used for reward frames unlock
            resValue("string", "reward_frames_high", "ca-app-pub-4276074242154795/4247639877")
            resValue("string", "reward_frames_medium", "ca-app-pub-4276074242154795/7933386400")
            resValue("string", "reward_frames_low", "ca-app-pub-4276074242154795/1483528127")


            resValue("string", "splash_app_open_high", "ca-app-pub-4276074242154795/5403673406")
            resValue("string", "splash_app_open_medium", "ca-app-pub-4276074242154795/1294690396")
            resValue("string", "splash_app_open_low", "ca-app-pub-4276074242154795/7668527059")

            //exit and save native id
            resValue("string", "exit_save_native_high", "ca-app-pub-4276074242154795/4610608126")
            resValue("string", "exit_save_native_medium", "ca-app-pub-4276074242154795/5306836023")
            resValue("string", "exit_save_native_backup", "ca-app-pub-4276074242154795/3993754358")
            //all banner ad
            resValue("string", "banner_overall_highs", "ca-app-pub-4276074242154795/6171101804")
            resValue("string", "banner_overall_med", "ca-app-pub-4276074242154795/9918775128")
            resValue("string", "banner_overall_low", "ca-app-pub-4276074242154795/3110372936")


            //nativeInProcessDialog ids
            resValue("string", "processing_native_high", "ca-app-pub-4276074242154795/4610608126")
            resValue("string", "processing_native_medium", "ca-app-pub-4276074242154795/5306836023")
            resValue("string", "processing_native_backup", "ca-app-pub-4276074242154795/3993754358")


            //language screen1 ids
            resValue("string", "native_language_back_up", "ca-app-pub-4276074242154795/3891712383")
            resValue("string", "native_language_medium", "ca-app-pub-4276074242154795/9553843423")

            //language screen2 ids
            resValue("string", "native_high_setting_language", "ca-app-pub-4276074242154795/1097473173")
            resValue("string", "native_high_setting_medium", "ca-app-pub-4276074242154795/9526627533")
            resValue("string", "native_high_setting_backup", "ca-app-pub-4276074242154795/8584370512")





            resValue("string", "banner_onboarding_high", "ca-app-pub-4276074242154795/8196257978")
            resValue("string", "banner_onboarding_medium", "ca-app-pub-4276074242154795/8367825977")
            resValue("string", "banner_onboarding_all", "ca-app-pub-4276074242154795/7656329786")

            resValue("string", "app_id", "ca-app-pub-4276074242154795~9558261597")
            resValue("string", "app_open", "ca-app-pub-4276074242154795/5352522071")
            resValue("string", "banner", "ca-app-pub-4276074242154795/2208299436")
            resValue("string", "collapsable_banner", "ca-app-pub-4276074242154795/6701610537")
            resValue("string", "collapsable_banner_floor", "ca-app-pub-4276074242154795/4230697159")

            resValue("string", "interstitial_back_all", "ca-app-pub-4276074242154795/3930966483")
            resValue("string", "interstitial_back_medium", "ca-app-pub-4276074242154795/6971228643")
            resValue("string", "interstitial_back_high", "ca-app-pub-4276074242154795/8046822382")

            resValue("string", "interstitial_uninstall_all", "ca-app-pub-4276074242154795/9882991773")
            resValue("string", "interstitial_uninstall_medium", "ca-app-pub-4276074242154795/9520626162")
            resValue("string", "interstitial_uninstall_high", "ca-app-pub-4276074242154795/8666709490")


            resValue("string", "interstitial_my_work_all", "ca-app-pub-4276074242154795/5867913150")
            resValue("string", "interstitial_my_work_medium", "ca-app-pub-4276074242154795/9005383829")
            resValue("string", "interstitial_my_work_high", "ca-app-pub-4276074242154795/6382988287")
            resValue("string", "interstitial_save_all", "ca-app-pub-4276074242154795/3930966483")
            resValue("string", "interstitial_save_medium", "ca-app-pub-4276074242154795/6971228643")
            resValue("string", "interstitial_save_high", "ca-app-pub-4276074242154795/8046822382")

            resValue("string", "guidescrn_banner_all", "ca-app-pub-4276074242154795/2544785166")
            resValue("string", "guidescrn_banner_medium", "ca-app-pub-4276074242154795/8279966185")
            resValue("string", "guidescrn_banner_high", "ca-app-pub-4276074242154795/1231703496")

            resValue("string", "large_banner_new", "ca-app-pub-4276074242154795/1287452587")
            resValue("string", "banner_new_floor", "ca-app-pub-4276074242154795/1036171145")
            resValue("string", "large_banner", "ca-app-pub-4276074242154795/3359575483")
            resValue("string", "open_guide_back_up", "ca-app-pub-4276074242154795/8824382350")
            resValue("string", "inline_banner", "ca-app-pub-4276074242154795/4176177401")
            resValue("string", "rewarded_interstitial", "ca-app-pub-4276074242154795/9445037725")
            resValue("string", "interstitial", "ca-app-pub-4276074242154795/9935225147")
            resValue("string", "interstitial_splash", " ca-app-pub-4276074242154795/2296387706")
            resValue("string", "static_interstitial", "")
//            resValue("string", "rewarded", "ca-app-pub-4276074242154795/2617884817")
            resValue("string", "native_advanced_video", "ca-app-pub-4276074242154795/4068176010")
            resValue("string", "bigo_app_id", "10994727")
//            resValue("string", "bigo_interstitial", "10182906-10158798")
//            resValue("string", "bigo_banner", "10182906-10156618")
//            resValue("string", "bigo_rewarded", "10182906-10001431")
            resValue("string", "bigo_popup", "10994727-10821290")
//            resValue("string", "bigo_native", "10182906-10087503")
//            resValue("string", "bigo_splash_id", "10182906-10158798")


            resValue("string", "survey_native_backup", "ca-app-pub-4276074242154795/3076408872")
            resValue("string", "survey_native_all", "ca-app-pub-4276074242154795/7891421120")
            resValue("string", "survey_native_medium", "ca-app-pub-4276074242154795/6822915870")
            resValue("string", "survey_native_high", "ca-app-pub-4276074242154795/3054227104")
            resValue("string", "native_gallery_back_up", "ca-app-pub-4276074242154795/6965907177")
            resValue("string", "native_gallery_medium", "ca-app-pub-4276074242154795/4360791104")
            resValue("string", "native_gallery_high", "ca-app-pub-4276074242154795/8407777513")

            resValue("string", "native_uninstall_all", "ca-app-pub-4276074242154795/7153483120")
            resValue("string", "native_uninstall_medium", "ca-app-pub-4276074242154795/8526274547")
            resValue("string", "native_uninstall_high", "ca-app-pub-4276074242154795/1449730305")

            resValue("string", "open_screen_high", "ca-app-pub-4276074242154795/2775634311")
            resValue("string", "open_screen_medium", "ca-app-pub-4276074242154795/3076408872")
            resValue("string", "open_screen_backup", "ca-app-pub-4276074242154795/4564224676")
            /* facebook app id for singular*/
            resValue("string", "facebook_id", "1116803502100807")





            resValue("string", "interstitial_splash_back_up", "ca-app-pub-4276074242154795/2296387706")

            resValue("string", "native_backup_id", "ca-app-pub-4276074242154795/9500207846")

            resValue("string", "banner_splash_high", "ca-app-pub-4276074242154795/7195679958")
            resValue("string", "banner_splash_all", "ca-app-pub-4276074242154795/9559746150")
            resValue("string", "banner_splash_medium", "ca-app-pub-4276074242154795/3324717704")

            resValue("string", "banner_on_board_second_id", "ca-app-pub-4276074242154795/6306976403")
            resValue("string", "interstitial_splash_medium", "ca-app-pub-4276074242154795/8385472699")

            resValue("string", "banner_editor", "")
            resValue("string", "banner_editor_medium", "")
            resValue("string", "banner_editor_backup", "")

            resValue("string", "banner_second_id", "ca-app-pub-4276074242154795/7564138209")

            resValue("string", "banner_medium", "  ca-app-pub-4276074242154795/6932618784")/* need to discuss*/

            resValue("string", "save_inter_high", "ca-app-pub-4276074242154795/8591243404")
            resValue("string", "save_inter_medium", "ca-app-pub-4276074242154795/9369138450")
            resValue("string", "save_inter_backup", "ca-app-pub-4276074242154795/1832170033")


            resValue("string", "home_banner_backup", "ca-app-pub-4276074242154795/7564138209")
            resValue("string", "home_banner_medium", "ca-app-pub-4276074242154795/9200723312")//12
            resValue("string", "home_large_banner_high", "ca-app-pub-4276074242154795/6494107841")//41


            resValue("string", "onboard_one_medium", "ca-app-pub-4276074242154795/9322876771")
            resValue("string", "onboard_two_medium", "ca-app-pub-4276074242154795/6689247649")
            resValue("string", "onboard_three_medium", "ca-app-pub-4276074242154795/9518419520")
            resValue("string", "onboard_full_medium", "ca-app-pub-4276074242154795/5811108808")


//            resValue("string", "banner_bottom_backup", "ca-app-pub-4276074242154795/1261661145")
//            resValue("string", "banner_bottom_medium", "ca-app-pub-4276074242154795/6932618784")

            resValue("string", "banner_bottom_backup", "ca-app-pub-4276074242154795/1261661145")
            resValue("string", "banner_bottom_medium", "ca-app-pub-4276074242154795/2208299436")//436

            resValue("string", "banner_bottom_high", "ca-app-pub-4276074242154795/6313570053")


            /*updated ids with latest work*/

            // exit and processing dialogs
            resValue("string", "native_advanced_video_backup", "ca-app-pub-4276074242154795/6975873202")
            resValue("string", "native_advanced_video_medium", "ca-app-pub-4276074242154795/2583445708")
            resValue("string", "native_advanced_video_high", "ca-app-pub-4276074242154795/3500941555")


            //nativeLanguageTwo
            resValue("string", "native_language_alt_medium", "ca-app-pub-4276074242154795/6409354074")

            //onBoardingOne ids
            resValue("string", "on_boarding_one_back_up", "ca-app-pub-4276074242154795/4206147974")
            resValue("string", "on_boarding_one_medium", "ca-app-pub-4276074242154795/6592638165")

            //onBoardingTwo ids
            resValue("string", "on_boarding_two_medium", "ca-app-pub-4276074242154795/2893066300")
            resValue("string", "on_boarding_two_back_up", "ca-app-pub-4276074242154795/1579984639")

            //onBoardingThree ids
            resValue("string", "on_boarding_three_medium", "ca-app-pub-4276074242154795/3255387759")
            resValue("string", "on_boarding_three_back_up", "ca-app-pub-4276074242154795/1299308257")

            //fullNative onBoarding ids
            resValue("string", "full_native_on_board_medium", "ca-app-pub-4276074242154795/5811108808")
            resValue("string", "full_native_on_board_backup", "ca-app-pub-4276074242154795/8766646458")

            //nativeDialog ids
            resValue("string", "dialog_native_backup", "ca-app-pub-4276074242154795/6975873202")
            resValue("string", "dialog_native_medium", "ca-app-pub-4276074242154795/2583445708")
            resValue("string", "dialog_native_high", "ca-app-pub-4276074242154795/3500941555")


            // resValue("string", "banner_bottom_high", "ca-app-pub-4276074242154795/7502230890")
            //testing ids


        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    kotlinOptions {
        jvmTarget = "1.8"
    }

    buildFeatures {
        viewBinding = true
    }
}

dependencies {
    implementation("androidx.core:core-ktx:1.15.0")
    implementation("androidx.appcompat:appcompat:1.7.0")
    implementation("com.google.android.material:material:1.12.0")

    // admob
    api("com.google.android.gms:play-services-ads:24.6.0")
    api("com.google.android.ump:user-messaging-platform:3.2.0")
    // pangle
    api("com.google.ads.mediation:pangle:7.5.0.4.0")
    //facebook
    api("com.google.ads.mediation:facebook:6.20.0.0")
    //liftoff|vungle
    api("com.google.ads.mediation:vungle:7.5.0.0")
    //mintegral
    api("com.google.ads.mediation:mintegral:16.9.91.1")
    //aplovin
    api("com.google.ads.mediation:applovin:13.4.0.0")
    //unity
    api("com.unity3d.ads:unity-ads:4.16.0")
    api("com.google.ads.mediation:unity:4.16.0.0")
    //inmobi
    api("com.google.ads.mediation:inmobi:10.8.7.0")
    //ironsource
    api("com.google.ads.mediation:ironsource:8.9.1.0")
    //moloco
    api("com.google.ads.mediation:moloco:4.2.0.0")

    //shimmer
    api("com.facebook.shimmer:shimmer:0.5.0")
    // analytics
    api(project(":analytics"))
    api(project(":common"))
    // sdp and ssp\
    implementation("com.intuit.sdp:sdp-android:1.1.0")
    implementation("com.intuit.ssp:ssp-android:1.1.0")
    // lottie animation
    implementation("com.airbnb.android:lottie:6.3.0")
    // Networking
    implementation("com.squareup.retrofit2:retrofit:2.9.0")
    implementation("com.squareup.okhttp3:okhttp:4.12.0")
    implementation("com.squareup.okhttp3:logging-interceptor:4.9.3")
    implementation("com.squareup.retrofit2:converter-gson:2.9.0")
    // hilt DI
    implementation("com.google.dagger:hilt-android:2.56.1")
    kapt("com.google.dagger:hilt-compiler:2.56.1")
    implementation("androidx.hilt:hilt-navigation-fragment:1.2.0")
    // glide
    implementation("com.github.bumptech.glide:glide:4.16.0")
    annotationProcessor("com.github.bumptech.glide:compiler:4.15.1")
    // google in app billing
    api(project(":inapp"))

    // firebase
    api(platform("com.google.firebase:firebase-bom:33.12.0"))
    api("com.google.firebase:firebase-analytics-ktx")
    api("com.google.firebase:firebase-config")
    api("com.google.firebase:firebase-analytics")
    api("com.google.firebase:firebase-crashlytics-ktx") {
        exclude(group = "androidx.datastore", module = "datastore-preferences")
    }

    // LifeCycles ViewModel,LiveData,Runtime and Process
    implementation("androidx.lifecycle:lifecycle-livedata-ktx:2.8.7")
    implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:2.8.7")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.8.7")
    implementation("androidx.lifecycle:lifecycle-process:2.8.7")

    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.2.1")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.6.1")
}