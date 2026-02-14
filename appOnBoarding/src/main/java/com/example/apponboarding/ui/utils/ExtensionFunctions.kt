package com.example.apponboarding.ui.utils

import android.app.Activity
import android.view.View
import com.example.ads.Constants.lfoOneNativeId
import com.example.ads.Constants.lfoTwoNativeId
import com.example.ads.Constants.onBoardingFullOneNativeId
import com.example.ads.Constants.onBoardingFullTwoNativeId
import com.example.ads.Constants.onBoardingOneNativeId
import com.example.ads.Constants.onBoardingThreeNativeId
import com.example.ads.Constants.onBoardingTwoNativeId
import com.project.common.utils.setString
import java.lang.Exception


fun View.hide() {
    this.visibility = View.GONE
}

fun View.show() {
    this.visibility = View.VISIBLE
}



fun Activity?.languageOne(): Pair<String, String> {
    return this?.let {
        try {
            Pair(
                lfoOneNativeId,setString(com.example.ads.R.string.native_language_back_up)
            )
        } catch (ex: Exception) {
            Pair(lfoOneNativeId, "")
        }
    } ?: Pair(lfoOneNativeId, "")
}

fun Activity?.languageTwo():Pair<String,String>{
    return this?.let {
        try {
            Pair(lfoTwoNativeId,setString(com.example.ads.R.string.native_language_alt_back_up))
        }catch (ex:Exception){
            Pair(lfoTwoNativeId,"")
        }


    } ?: Pair(lfoTwoNativeId,"")

}

fun Activity?.fullNativeOne():Pair<String,String>{

    return this?.let {
        try {
            Pair(onBoardingFullOneNativeId,setString(com.example.ads.R.string.full_native_on_board_backup))
        }catch (ex:Exception){
         Pair(onBoardingFullOneNativeId,"")
        }
    } ?: Pair(onBoardingFullOneNativeId,"")


}


fun Activity?.onHomeBanner(): Pair<String, String> {
    return this?.let {
        try {
            Pair(setString(com.example.ads.R.string.home_large_banner_high), setString(com.example.ads.R.string.home_banner_medium))
        } catch (ex: Exception) {
            Pair(setString(com.example.ads.R.string.home_large_banner_high), "")
        }
    } ?: Pair(setString(com.example.ads.R.string.home_large_banner_high), "")
}



fun Activity?.fullNativeTwo(): Pair<String, String> {
    return this?.let {
        try {
            Pair(
                onBoardingFullTwoNativeId,setString(com.example.ads.R.string.full_native_on_board_backup)
            )
        } catch (ex: Exception) {
            Pair(onBoardingFullTwoNativeId, "")
        }
    } ?: Pair(onBoardingFullTwoNativeId, "")
}


fun Activity?.onBoardNativeOne(): Pair<String, String> {
    return this?.let {
        try {
            Pair(onBoardingOneNativeId, setString(com.example.ads.R.string.on_boarding_one_back_up))
        } catch (ex: Exception) {
            Pair(onBoardingOneNativeId, "")
        }
    } ?: Pair(onBoardingOneNativeId, "")
}





fun Activity?.onBoardNativeTwo(): Pair<String, String> {
    return this?.let {
        try {
            Pair(
                onBoardingTwoNativeId,
                setString(com.example.ads.R.string.on_boarding_two_back_up)
            )
        } catch (ex: Exception) {
            Pair(onBoardingTwoNativeId, "")
        }
    } ?: Pair(onBoardingTwoNativeId, "")
}

fun Activity?.onBoardNativeThree(): Pair<String, String> {
    return this?.let {
        try {
            Pair(
                onBoardingThreeNativeId,
                setString(com.example.ads.R.string.on_boarding_three_back_up)
            )
        } catch (ex: Exception) {
            Pair(onBoardingThreeNativeId, "")
        }
    } ?: Pair(onBoardingThreeNativeId, "")
}







