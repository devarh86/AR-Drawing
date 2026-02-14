package com.project.common.utils.enums

import androidx.annotation.Keep

@Keep
enum class PurchaseTag(val tag: String) {
    PRO("Paid"),
    REWARDED("Rewarded"),
    FREE("Free");
}