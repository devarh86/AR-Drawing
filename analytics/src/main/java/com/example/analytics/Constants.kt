package com.example.analytics

import com.google.errorprone.annotations.Keep
import com.google.firebase.analytics.FirebaseAnalytics

@Keep
object Constants {
    var firebaseAnalytics: FirebaseAnalytics? = null
    var screen = ""
    var parentScreen = ""
    var mainCategoryName: String = ""
}