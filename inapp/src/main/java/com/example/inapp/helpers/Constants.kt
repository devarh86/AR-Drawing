package com.example.inapp.helpers

import android.util.Log
import androidx.annotation.Keep
import androidx.lifecycle.MutableLiveData
import com.android.billingclient.api.ProductDetails
import com.android.billingclient.api.Purchase
import com.android.billingclient.api.PurchaseHistoryRecord
import java.lang.Exception

object Constants {

    var isProVersion = MutableLiveData(false)
    fun isProVersion() = isProVersion.value ?: false
    fun isProVersionGallery() = isProVersion.value ?: false
    var proScreenReady: Boolean = false
    var SINGULAR_SECRET = "a628b434bcfc30722e78c79b697c3530"
    var SINGULAR_API_KEY = "mobify_dd1a3006"
    var proNewEnable = true
    val SKU_LIST =
        listOf(
            "one_month",
            "one_year",
            "weekly_offer",
            "monthly",
            "yearly",
            "weekly"
        )

    val SUB_PRODUCT_DETAILS = arrayListOf<ProductDetails>()
    val SUB_PURCHASED_PRODUCT_DETAILS = arrayListOf<Purchase>()
    val SUB_PURCHASED_HISTORY_PRODUCT_DETAILS = arrayListOf<PurchaseHistoryRecord>()
    val SKU_LIST_IN_APP = listOf<String>()

    val IN_APP_PRODUCT_DETAILS = arrayListOf<ProductDetails>()
    val IN_APP_PURCHASED_PRODUCT_DETAILS = arrayListOf<Purchase>()
    val IN_APP_PURCHASED_HISTORY_PRODUCT_DETAILS = arrayListOf<PurchaseHistoryRecord>()

    fun getProductDetail(itemSKU: String): Pair<String, String>? {
        val productList = SUB_PRODUCT_DETAILS.filter { it.productId == itemSKU }
        if (productList.isNotEmpty()) {
            productList[0].subscriptionOfferDetails?.let { subOfferDetails ->
                if (subOfferDetails.isNotEmpty()) {
                    subOfferDetails[0].pricingPhases.pricingPhaseList.let { pricingPhaseList ->
                        if (pricingPhaseList.isNotEmpty()) {
                            return Pair(pricingPhaseList[0].formattedPrice, "")
                        }
                    }
                }
            }
        }
        return null
    }



    fun getProductDetailMicroValueNew(itemSKU: String): ProModel? {
        val productList = SUB_PRODUCT_DETAILS.filter { it.productId == itemSKU }
        if (productList.isNotEmpty()) {
            productList[0].subscriptionOfferDetails?.let { subOfferDetails ->
                if (subOfferDetails.isNotEmpty()) {
                    subOfferDetails[0].pricingPhases.pricingPhaseList.let { pricingPhaseList ->
                        if (pricingPhaseList.isNotEmpty()) {
                            runCatching {
                                Log.i(
                                    "TAG",
                                    "getProductDetailMicroValue: ${pricingPhaseList[1]?.priceAmountMicros}"
                                )
                            }

                            runCatching {
                                Log.i(
                                    "TAG",
                                    "getProductDetailMicroValue: ${pricingPhaseList}"
                                )
                            }

                            try {
                                val price =
                                    (pricingPhaseList[0].priceAmountMicros / 1000000.0).toString()

                                if (price.toDouble() == 0.0) {
                                    if (pricingPhaseList.size > 1) {
                                        val obj = ProModel(
                                            currency = pricingPhaseList[1].priceCurrencyCode,
                                            price = (pricingPhaseList[1].priceAmountMicros / 1000000.0).toString(),
                                            isTrailActive = true
                                        )
                                        return obj
                                    } else {
                                        val obj = ProModel(
                                            currency = "",
                                            price = "0",
                                            isTrailActive = false
                                        )
                                        return obj
                                    }
                                } else {
                                    val obj = ProModel(
                                        currency = pricingPhaseList[0].priceCurrencyCode,
                                        price = price,
                                        isTrailActive = false
                                    )
                                    return obj
                                }
                            } catch (ex: Exception) {
                                val obj = ProModel(
                                    currency = "",
                                    price = "0",
                                    isTrailActive = false
                                )
                                return obj
                            }
                        }
                    }
                }
            }
        }
        return null
    }


    fun getProductDetailInApp(itemSKU: String): Pair<String, String>? {
        val productList = IN_APP_PRODUCT_DETAILS.filter { it.productId == itemSKU }
        if (productList.isNotEmpty()) {
            productList[0].oneTimePurchaseOfferDetails?.let {
                return Pair(it.formattedPrice, "")
            }
        }
        return null
    }

    //    fun getProductDetailMicroValue(itemSKU: String): Pair<Pair<String,String>, String>? {
    fun getProductDetailMicroValue(itemSKU: String): Pair<String, String>? {
        val productList = SUB_PRODUCT_DETAILS.filter { it.productId == itemSKU }
        if (productList.isNotEmpty()) {
            productList[0].subscriptionOfferDetails?.let { subOfferDetails ->
                if (subOfferDetails.isNotEmpty()) {
                    subOfferDetails[0].pricingPhases.pricingPhaseList.let { pricingPhaseList ->
                        if (pricingPhaseList.isNotEmpty()) {
                            runCatching {
                                Log.i(
                                    "TAG",
                                    "getProductDetailMicroValue: ${pricingPhaseList[1]?.priceAmountMicros}"
                                )
                            }

                            runCatching {
                                Log.i(
                                    "TAG",
                                    "getProductDetailMicroValue: ${pricingPhaseList}"
                                )
                            }

//                            haveTrial = false

                            val newPrice: Pair<String, String> = try {
                                val price =
                                    (pricingPhaseList[0].priceAmountMicros / 1000000.0).toString()

                                if (price.toDouble() == 0.0) {
                                    if (pricingPhaseList.size > 1) {
//                                        haveTrial = true
                                        Pair(
                                            (pricingPhaseList[1].priceAmountMicros / 1000000.0).toString(),
                                            pricingPhaseList[1].priceCurrencyCode
                                        )
                                    } else {
                                        Pair("0", "")
                                    }
                                } else {
                                    Pair(price, pricingPhaseList[0].priceCurrencyCode)
                                }
                            } catch (ex: Exception) {
                                Pair("0", "")
                            }

                            return newPrice
                        }
                    }
                }
            }
        }
        return null
    }

//    var haveTrial = false

    fun getProductDetailMicroValueInApp(itemSKU: String): Pair<Double, String>? {
        val productList = IN_APP_PRODUCT_DETAILS.filter { it.productId == itemSKU }
        if (productList.isNotEmpty()) {
            productList[0].oneTimePurchaseOfferDetails?.let { subOfferDetails ->
                return Pair(
                    (subOfferDetails.priceAmountMicros / 1000000.0),
                    subOfferDetails.priceCurrencyCode
                )
            }
        }
        return null
    }
}

@Keep
data class ProModel(
    var currency: String = "",
    var price: String = "0.0",
    var isTrailActive: Boolean = false
)