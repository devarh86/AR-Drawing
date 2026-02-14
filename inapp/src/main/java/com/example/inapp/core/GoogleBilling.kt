package com.example.inapp.core

import android.app.Activity
import android.content.Context
import android.util.Log
import com.android.billingclient.api.AcknowledgePurchaseParams
import com.android.billingclient.api.BillingClient
import com.android.billingclient.api.BillingClientStateListener
import com.android.billingclient.api.BillingFlowParams
import com.android.billingclient.api.BillingFlowParams.SubscriptionUpdateParams
import com.android.billingclient.api.BillingResult
import com.android.billingclient.api.ProductDetails
import com.android.billingclient.api.Purchase
import com.android.billingclient.api.Purchase.PurchaseState
import com.android.billingclient.api.PurchasesUpdatedListener
import com.android.billingclient.api.QueryProductDetailsParams
import com.android.billingclient.api.QueryPurchaseHistoryParams
import com.android.billingclient.api.QueryPurchasesParams
import com.android.billingclient.api.acknowledgePurchase
import com.android.billingclient.api.queryProductDetails
import com.android.billingclient.api.queryPurchaseHistory
import com.android.billingclient.api.queryPurchasesAsync
import com.example.inapp.helpers.Constants.IN_APP_PRODUCT_DETAILS
import com.example.inapp.helpers.Constants.IN_APP_PURCHASED_HISTORY_PRODUCT_DETAILS
import com.example.inapp.helpers.Constants.IN_APP_PURCHASED_PRODUCT_DETAILS
import com.example.inapp.helpers.Constants.SKU_LIST
import com.example.inapp.helpers.Constants.SKU_LIST_IN_APP
import com.example.inapp.helpers.Constants.SUB_PRODUCT_DETAILS
import com.example.inapp.helpers.Constants.SUB_PURCHASED_HISTORY_PRODUCT_DETAILS
import com.example.inapp.helpers.Constants.SUB_PURCHASED_PRODUCT_DETAILS
import com.example.inapp.helpers.Constants.isProVersion
import com.example.inapp.helpers.Constants.proScreenReady
import com.example.inapp.helpers.showToast
import com.google.firebase.analytics.FirebaseAnalytics
import com.singular.sdk.Singular
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Date
import java.util.regex.Pattern
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.time.Duration.Companion.days

const val TAG = "FAHAD_GOOGLE_BILLING"

@Singleton
class GoogleBilling @Inject constructor(@ApplicationContext private val context: Context) {

    var firebaseAnalytics: FirebaseAnalytics? = null

    private val purchasesUpdatedListener =
        PurchasesUpdatedListener { billingResult, purchases ->
            purchases?.let {
                when (billingResult.responseCode) {
                    BillingClient.BillingResponseCode.OK -> {
                        for (purchase in purchases) {
                            GlobalScope.launch {
                                if (SKU_LIST_IN_APP.isNotEmpty() && purchase.products.contains(
                                        SKU_LIST_IN_APP[0]
                                    )
                                ) handlePurchaseInApp(
                                    purchase, true
                                )
                                else handlePurchase(purchase, true)
                            }
                        }
                    }

                    BillingClient.BillingResponseCode.USER_CANCELED -> {}
                    else -> {}
                }
            }
        }

    private var connectionCounter = 0

    private var billingClient = BillingClient.newBuilder(context)
        .setListener(purchasesUpdatedListener)
        .enablePendingPurchases()
        .build()

    init {
        IN_APP_PRODUCT_DETAILS.clear()
        SUB_PRODUCT_DETAILS.clear()
        IN_APP_PURCHASED_PRODUCT_DETAILS.clear()
        SUB_PURCHASED_PRODUCT_DETAILS.clear()
        startConnection()
    }

    private fun startConnection() {
        connectionCounter += 1
        billingClient.startConnection(object : BillingClientStateListener {
            override fun onBillingSetupFinished(billingResult: BillingResult) {
                if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                    IN_APP_PRODUCT_DETAILS.clear()
                    SUB_PRODUCT_DETAILS.clear()
                    IN_APP_PURCHASED_PRODUCT_DETAILS.clear()
                    SUB_PURCHASED_PRODUCT_DETAILS.clear()
                    GlobalScope.launch {
                        SKU_LIST.forEach { fetchProductDetails(it) }
                        SKU_LIST_IN_APP.forEach { fetchProductDetailsInApp(it) }
                        fetchActivePurchases()
                        fetchActivePurchasesInApp()
                    }.invokeOnCompletion {
                        proScreenReady = true
                    }
                }
            }

            override fun onBillingServiceDisconnected() {
                if (connectionCounter < 3) {
                    startConnection()
                }
            }
        })
    }

    private suspend fun fetchActivePurchases() {
        fetchPurchaseHistory()
        fetchPurchasedProducts()
    }

    private suspend fun fetchActivePurchasesInApp() {
        fetchPurchaseHistoryInApp()
        fetchPurchasedProductsInApp()
    }

    private suspend fun fetchProductDetails(productSku: String) {
        val productList = ArrayList<QueryProductDetailsParams.Product>()
        productList.add(
            QueryProductDetailsParams.Product.newBuilder()
                .setProductId(productSku)
                .setProductType(BillingClient.ProductType.SUBS)
                .build()
        )
        val params = QueryProductDetailsParams.newBuilder()
        params.setProductList(productList)

        val productDetailsResult = withContext(Dispatchers.IO) {
            billingClient.queryProductDetails(params.build())
        }

        productDetailsResult.apply {
            when (billingResult.responseCode) {
                BillingClient.BillingResponseCode.OK -> {
                    productDetailsList?.forEach {
                        synchronized(SUB_PRODUCT_DETAILS) {
                            SUB_PRODUCT_DETAILS.add(it)
                        }
                        Log.d(TAG, "fetchProductDetails:productId ${it.productId}")
                        Log.d(TAG, "fetchProductDetails:name ${it.name}")
                        Log.d(TAG, "fetchProductDetails:productType ${it.productType}")
                        Log.d(TAG, "fetchProductDetails:description ${it.description}")
                        it.oneTimePurchaseOfferDetails?.let { oneTimeDetails ->
                            Log.d(
                                TAG,
                                "fetchProductDetails:formattedPrice ${oneTimeDetails.formattedPrice}"
                            )
                            Log.d(
                                TAG,
                                "fetchProductDetails:priceAmountMicros ${oneTimeDetails.priceAmountMicros}"
                            )
                            Log.d(
                                TAG,
                                "fetchProductDetails:priceCurrencyCode ${oneTimeDetails.priceCurrencyCode}"
                            )
                        }
                        it.subscriptionOfferDetails?.let { subDetails ->
                            subDetails.forEachIndexed { index, item ->
                                Log.d(
                                    TAG,
                                    "fetchProductDetails:basePlanId $index ${item.basePlanId}"
                                )
                                Log.d(TAG, "fetchProductDetails:offerId $index ${item.offerId}")
                                item.offerTags.forEach {
                                    Log.d(
                                        TAG,
                                        "fetchProductDetails:offerTags $index ${it}"
                                    )
                                }
                                Log.d(
                                    TAG,
                                    "fetchProductDetails:offerToken $index ${item.offerToken}"
                                )
                                item.pricingPhases.pricingPhaseList.forEach {
                                    Log.d(
                                        TAG,
                                        "fetchProductDetails:priceAmountMicros $index ${it.priceAmountMicros}"
                                    )
                                    Log.d(
                                        TAG,
                                        "fetchProductDetails:billingCycleCount $index ${it.billingCycleCount}"
                                    )
                                    Log.d(
                                        TAG,
                                        "fetchProductDetails:billingPeriod $index ${it.billingPeriod}"
                                    )
                                    Log.d(
                                        TAG,
                                        "fetchProductDetails:formattedPrice $index ${it.formattedPrice}"
                                    )
                                    Log.d(
                                        TAG,
                                        "fetchProductDetails:priceCurrencyCode $index ${it.priceCurrencyCode}"
                                    )
                                    Log.d(
                                        TAG,
                                        "fetchProductDetails:recurrenceMode $index ${it.recurrenceMode}"
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    private suspend fun fetchProductDetailsInApp(productSku: String) {
        val productList = ArrayList<QueryProductDetailsParams.Product>()
        productList.add(
            QueryProductDetailsParams.Product.newBuilder()
                .setProductId(productSku)
                .setProductType(BillingClient.ProductType.INAPP)
                .build()
        )
        val params = QueryProductDetailsParams.newBuilder()
        params.setProductList(productList)

        val productDetailsResult = withContext(Dispatchers.IO) {
            billingClient.queryProductDetails(params.build())
        }

        productDetailsResult.apply {
            when (billingResult.responseCode) {
                BillingClient.BillingResponseCode.OK -> {
                    productDetailsList?.forEach {
                        synchronized(IN_APP_PRODUCT_DETAILS) {
                            IN_APP_PRODUCT_DETAILS.add(it)
                        }
                        Log.d(TAG, "fetchProductDetails:productId ${it.productId}")
                        Log.d(TAG, "fetchProductDetails:name ${it.name}")
                        Log.d(TAG, "fetchProductDetails:productType ${it.productType}")
                        Log.d(TAG, "fetchProductDetails:description ${it.description}")
                        it.oneTimePurchaseOfferDetails?.let { oneTimeDetails ->
                            Log.d(
                                TAG,
                                "fetchProductDetails:formattedPrice ${oneTimeDetails.formattedPrice}"
                            )
                            Log.d(
                                TAG,
                                "fetchProductDetails:priceAmountMicros ${oneTimeDetails.priceAmountMicros}"
                            )
                            Log.d(
                                TAG,
                                "fetchProductDetails:priceCurrencyCode ${oneTimeDetails.priceCurrencyCode}"
                            )
                        }
                        it.subscriptionOfferDetails?.let { subDetails ->
                            subDetails.forEachIndexed { index, item ->
                                Log.d(
                                    TAG,
                                    "fetchProductDetails:basePlanId $index ${item.basePlanId}"
                                )
                                Log.d(TAG, "fetchProductDetails:offerId $index ${item.offerId}")
                                item.offerTags.forEach {
                                    Log.d(
                                        TAG,
                                        "fetchProductDetails:offerTags $index ${it}"
                                    )
                                }
                                Log.d(
                                    TAG,
                                    "fetchProductDetails:offerToken $index ${item.offerToken}"
                                )
                                item.pricingPhases.pricingPhaseList.forEach {
                                    Log.d(
                                        TAG,
                                        "fetchProductDetails:priceAmountMicros $index ${it.priceAmountMicros}"
                                    )
                                    Log.d(
                                        TAG,
                                        "fetchProductDetails:billingCycleCount $index ${it.billingCycleCount}"
                                    )
                                    Log.d(
                                        TAG,
                                        "fetchProductDetails:billingPeriod $index ${it.billingPeriod}"
                                    )
                                    Log.d(
                                        TAG,
                                        "fetchProductDetails:formattedPrice $index ${it.formattedPrice}"
                                    )
                                    Log.d(
                                        TAG,
                                        "fetchProductDetails:priceCurrencyCode $index ${it.priceCurrencyCode}"
                                    )
                                    Log.d(
                                        TAG,
                                        "fetchProductDetails:recurrenceMode $index ${it.recurrenceMode}"
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    fun lifeTimePurchase(activity: Activity, itemSKU: String) {
        val productList = IN_APP_PRODUCT_DETAILS.filter { it.productId == itemSKU }
        (if (productList.isNotEmpty()) productList[0] else null)?.let { product ->
            if (!IN_APP_PURCHASED_PRODUCT_DETAILS.any { it.products.contains(itemSKU) } || !IN_APP_PURCHASED_HISTORY_PRODUCT_DETAILS.any {
                    it.products.contains(
                        itemSKU
                    )
                }) {
                launchGoogleBillingFlowInApp(activity, product)
            } else {
                context.showToast("Already Purchased!")
            }
        } ?: run {
            context.showToast("SKU not valid")
        }
    }

    fun subscribe(activity: Activity, itemSKU: String) {
        val productList = SUB_PRODUCT_DETAILS.filter { it.productId == itemSKU }
        (if (productList.isNotEmpty()) productList[0] else null)?.let { product ->
            if (!SUB_PURCHASED_PRODUCT_DETAILS.any { it.products.contains(itemSKU) } || !SUB_PURCHASED_HISTORY_PRODUCT_DETAILS.any {
                    it.products.contains(
                        itemSKU
                    )
                }) {
                launchGoogleBillingFlow(activity, product)
            } else {
                context.showToast("Already Purchased!")
            }
        } ?: run {
            context.showToast("SKU not valid")
        }
    }

    private fun launchGoogleBillingFlow(activity: Activity, productDetails: ProductDetails) {
        val productDetailsParamsList = productDetails.run {
            subscriptionOfferDetails?.run {
                if (this.isNotEmpty() && productDetails.productType == BillingClient.ProductType.SUBS) {
                    get(0).offerToken.run {
                        listOf(
                            BillingFlowParams.ProductDetailsParams.newBuilder()
                                .setProductDetails(productDetails)
                                .setOfferToken(this)
                                .build()
                        )
                    }
                } else {
                    listOf(
                        BillingFlowParams.ProductDetailsParams.newBuilder()
                            .setProductDetails(productDetails)
                            .build()
                    )
                }
            } ?: run {
                listOf(
                    BillingFlowParams.ProductDetailsParams.newBuilder()
                        .setProductDetails(productDetails)
                        .build()
                )
            }
        }
        val billingFlowParams = BillingFlowParams.newBuilder()
            .setProductDetailsParamsList(productDetailsParamsList)
            .build()

        val billingResult = billingClient.launchBillingFlow(activity, billingFlowParams)
        when (billingResult.responseCode) {
            BillingClient.BillingResponseCode.OK -> {
//                firebaseAnalytics?.logEvent("subscription_panel_open", null)
//                Log.d("MyFirebaseEvent", "subscription_panel_open")
            }

            BillingClient.BillingResponseCode.USER_CANCELED -> {}
            else -> {
                context.showToast("Billing Launch Failed")
            }
        }
    }

    private fun launchGoogleBillingFlowInApp(activity: Activity, productDetails: ProductDetails) {
        val productDetailsParamsList = productDetails.run {
            subscriptionOfferDetails?.run {
                if (this.isNotEmpty() && productDetails.productType == BillingClient.ProductType.INAPP) {
                    get(0).offerToken.run {
                        listOf(
                            BillingFlowParams.ProductDetailsParams.newBuilder()
                                .setProductDetails(productDetails)
                                .setOfferToken(this)
                                .build()
                        )
                    }
                } else {
                    listOf(
                        BillingFlowParams.ProductDetailsParams.newBuilder()
                            .setProductDetails(productDetails)
                            .build()
                    )
                }
            } ?: run {
                listOf(
                    BillingFlowParams.ProductDetailsParams.newBuilder()
                        .setProductDetails(productDetails)
                        .build()
                )
            }
        }
        val billingFlowParams = BillingFlowParams.newBuilder()
            .setProductDetailsParamsList(productDetailsParamsList)
            .build()

        val billingResult = billingClient.launchBillingFlow(activity, billingFlowParams)
        when (billingResult.responseCode) {
            BillingClient.BillingResponseCode.OK -> {
//                firebaseAnalytics?.logEvent("inapp_panel_open", null)
//                Log.d("MyFirebaseEvent", "subscription_panel_open")
            }

            BillingClient.BillingResponseCode.USER_CANCELED -> {}
            else -> {
                context.showToast("Billing Launch Failed")
            }
        }
    }

    fun upgradeOrDowngradeSubscription(
        activity: Activity,
        productDetails: ProductDetails,
        updateProductId: String,
        updateOfferId: String,
        OldProductID: String
    ) {
        val oldToken = getOldPurchaseToken(OldProductID)
        if (oldToken.trim().isNotEmpty()) {
            val productDetailsParamsList = ArrayList<BillingFlowParams.ProductDetailsParams>()
            if (productDetails.productType == BillingClient.ProductType.SUBS && productDetails.subscriptionOfferDetails != null) {
                val offerToken = getOfferToken(
                    productDetails.subscriptionOfferDetails,
                    updateProductId,
                    updateOfferId
                )
                if (offerToken.trim { it <= ' ' } != "") {
                    productDetailsParamsList.add(
                        BillingFlowParams.ProductDetailsParams.newBuilder()
                            .setProductDetails(productDetails).setOfferToken(offerToken).build()
                    )
                } else {
                    Log.d(
                        TAG,
                        "The offer id: $updateProductId doesn't seem to exist on Play Console"
                    )
                    return
                }
            } else {
                productDetailsParamsList.add(
                    BillingFlowParams.ProductDetailsParams.newBuilder()
                        .setProductDetails(productDetails).build()
                )
            }
            val billingFlowParams =
                BillingFlowParams.newBuilder().setProductDetailsParamsList(productDetailsParamsList)
                    .setSubscriptionUpdateParams(
                        SubscriptionUpdateParams.newBuilder()
                            .setOldPurchaseToken(oldToken)
                            .setSubscriptionReplacementMode(
                                SubscriptionUpdateParams.ReplacementMode.DEFERRED
                            ).build()
                    ).build()
            billingClient.launchBillingFlow(activity, billingFlowParams)
        } else {
            Log.d(TAG, "old purchase token not found")
        }
    }

    private fun getOfferToken(
        offerList: List<ProductDetails.SubscriptionOfferDetails>?,
        productId: String,
        offerId: String
    ): String {
        for (product in offerList!!) {
            if (product.offerId != null && product.offerId == offerId && product.basePlanId == productId) {
                return product.offerToken
            } else if (offerId.trim { it <= ' ' } == "" && product.basePlanId == productId && product.offerId == null) {
                return product.offerToken
            }
        }
        Log.d(TAG, "No Offer find")
        return ""
    }

    private fun getOldPurchaseToken(basePlanKey: String): String {
        SUB_PRODUCT_DETAILS.forEach { pro ->
            if (pro.productType == BillingClient.ProductType.SUBS) {
                pro.subscriptionOfferDetails?.forEach { sub ->
                    if (sub.basePlanId == basePlanKey) {
                        SUB_PURCHASED_PRODUCT_DETAILS.forEach {
                            if (it.products.first() == pro.productId) {
                                return it.purchaseToken
                            }
                        }
                    }
                }
            }
        }
        return ""
    }

    private suspend fun fetchPurchasedProducts() {
        val params = QueryPurchasesParams.newBuilder()
            .setProductType(BillingClient.ProductType.SUBS)
        val purchasesResult =
            withContext(Dispatchers.IO) { billingClient.queryPurchasesAsync(params.build()) }
        when (purchasesResult.billingResult.responseCode) {
            BillingClient.BillingResponseCode.OK -> {
                purchasesResult.purchasesList.forEach { purchase ->
                    Log.d(TAG, "purchase Products: ${purchase.originalJson}")
                    if (purchase.products.isNotEmpty()) {
                        val purchaseTime = purchase.purchaseTime
                        val systemTime = System.currentTimeMillis()
                        val periodTime = when (purchase.products[0]) {
                            SKU_LIST[0] -> 30.days.inWholeMilliseconds
                            SKU_LIST[1] -> 365.days.inWholeMilliseconds
                            SKU_LIST[2] -> 7.days.inWholeMilliseconds
                            SKU_LIST[3] -> 365.days.inWholeMilliseconds
                            SKU_LIST[4] -> 30.days.inWholeMilliseconds
                            else -> 1
                        }
                        val ST_PT = systemTime - purchaseTime
                        val remainingTime = ST_PT % periodTime
                        val subRemainingTime = periodTime - remainingTime
                        val checkerTime = systemTime + subRemainingTime

//                        Log.d(TAG, "checkerTime = ${setTime(checkerTime)}")
//                        Log.d(TAG, "purchaseTime = ${setTime(purchase.purchaseTime)}")
//                        Log.d(TAG, "currentTime = ${setTime(System.currentTimeMillis())}")
                        if (checkerTime > System.currentTimeMillis()) {
                            isProVersion.postValue(true)
                            Log.d(TAG, "isProVersion = ${isProVersion()}")
                        }
                    }
                    handlePurchase(purchase)
                }
            }

            else -> {}
        }
    }

    private suspend fun fetchPurchasedProductsInApp() {
        val params = QueryPurchasesParams.newBuilder()
            .setProductType(BillingClient.ProductType.INAPP)
        val purchasesResult =
            withContext(Dispatchers.IO) { billingClient.queryPurchasesAsync(params.build()) }
        when (purchasesResult.billingResult.responseCode) {
            BillingClient.BillingResponseCode.OK -> {
                purchasesResult.purchasesList.forEach { purchase ->
                    Log.d(TAG, "purchase Products: ${purchase.originalJson}")
                    if (purchase.products.isNotEmpty()) {
                        isProVersion.postValue(true)
                    }
                    handlePurchaseInApp(purchase)
                }
            }

            else -> {}
        }
    }

    private suspend fun fetchPurchaseHistory() {
        val params = QueryPurchaseHistoryParams.newBuilder()
            .setProductType(BillingClient.ProductType.SUBS)

        val purchaseHistoryResult =
            withContext(Dispatchers.IO) { billingClient.queryPurchaseHistory(params.build()) }

        when (purchaseHistoryResult.billingResult.responseCode) {
            BillingClient.BillingResponseCode.OK -> {
                SUB_PURCHASED_HISTORY_PRODUCT_DETAILS.clear()
                Log.d(TAG, "fetchPurchaseHistory: ${purchaseHistoryResult.purchaseHistoryRecordList}")
                purchaseHistoryResult.purchaseHistoryRecordList?.let {
                    if (it.isNotEmpty()) {
                        it.forEach {
                            Log.d(TAG, "fetchPurchaseHistory: ${it.originalJson}")
                        }
                    }
                }
            }

            else -> {}
        }
    }

    private suspend fun fetchPurchaseHistoryInApp() {
        val params = QueryPurchaseHistoryParams.newBuilder()
            .setProductType(BillingClient.ProductType.INAPP)

        val purchaseHistoryResult =
            withContext(Dispatchers.IO) { billingClient.queryPurchaseHistory(params.build()) }

        when (purchaseHistoryResult.billingResult.responseCode) {
            BillingClient.BillingResponseCode.OK -> {
                IN_APP_PURCHASED_HISTORY_PRODUCT_DETAILS.clear()
                purchaseHistoryResult.purchaseHistoryRecordList?.let {
                    if (it.isNotEmpty()) {
                        isProVersion.postValue(true)
                        it.forEach {
                            Log.d(TAG, "fetchPurchaseHistory: ${it.originalJson}")
                        }
                    }
                }
            }

            else -> {}
        }
    }

    private fun setTime(dateInMillis: Long): String {
        val formatter = SimpleDateFormat("dd/MM/yyyy hh:mm:ss")
        return formatter.format(Date(dateInMillis))
    }

    private suspend fun handlePurchase(purchase: Purchase, showEvent: Boolean = false) {
        if (purchase.purchaseState == PurchaseState.PURCHASED) {
            if (!purchase.isAcknowledged) {
                val acknowledgePurchaseParams = AcknowledgePurchaseParams.newBuilder()
                    .setPurchaseToken(purchase.purchaseToken)
                val ackPurchaseResult = withContext(Dispatchers.IO) {
                    billingClient.acknowledgePurchase(acknowledgePurchaseParams.build())
                }
                if (ackPurchaseResult.responseCode == BillingClient.BillingResponseCode.OK) {
                    Log.d(TAG, "purchase Products: ${purchase.originalJson}")
                    isProVersion.postValue(true)
                    SUB_PURCHASED_PRODUCT_DETAILS.add(purchase)
                    if (showEvent) {
                        firebaseAnalytics?.logEvent("purchase_subscription_success", null)
                        Log.d("MyFirebaseEvent", "purchase_subscription_success")

                        runCatching {
                            setPurchaseItemEventToSingular(purchase.products[0], purchase)
                        }
                    }
                }
            }
        }
    }

    private suspend fun setPurchaseItemEventToSingular(productSku: String?, purchase: Purchase) {
        productSku?.let {
            val productList = ArrayList<QueryProductDetailsParams.Product>()
            productList.add(
                QueryProductDetailsParams.Product.newBuilder()
                    .setProductId(productSku)
                    .setProductType(BillingClient.ProductType.SUBS)
                    .build()
            )
            val params = QueryProductDetailsParams.newBuilder()
            params.setProductList(productList)

            val productDetailsResult = withContext(Dispatchers.IO) {
                billingClient.queryProductDetails(params.build())
            }

            productDetailsResult.apply {
                when (billingResult.responseCode) {
                    BillingClient.BillingResponseCode.OK -> {
                        productDetailsList?.forEach {
//                       synchronized(SUB_PRODUCT_DETAILS) {
//                           SUB_PRODUCT_DETAILS.add(it)
//                       }
                            Log.d(TAG, "fetchProductDetails:productId ${it.productId}")
                            Log.d(TAG, "fetchProductDetails:name ${it.name}")
                            Log.d(TAG, "fetchProductDetails:productType ${it.productType}")
                            Log.d(TAG, "fetchProductDetails:description ${it.description}")
                            it.oneTimePurchaseOfferDetails?.let { oneTimeDetails ->
                                Log.d(
                                    TAG,
                                    "fetchProductDetails:formattedPrice ${oneTimeDetails.formattedPrice}"
                                )
                                Log.d(
                                    TAG,
                                    "fetchProductDetails:priceAmountMicros ${oneTimeDetails.priceAmountMicros}"
                                )
                                Log.d(
                                    TAG,
                                    "fetchProductDetails:priceCurrencyCode ${oneTimeDetails.priceCurrencyCode}"
                                )


                                runCatching {
                                    (oneTimeDetails.priceAmountMicros / 1000000.0).let { doubleAmount ->
                                        Singular.revenue(
                                            oneTimeDetails.priceCurrencyCode,
                                            doubleAmount,
                                            purchase
                                        )
                                    }
                                }
//                                getDoubleAmountFromFormattedPrice(formattedPrice = oneTimeDetails.formattedPrice)?.let { doubleAmount ->
//                                    Log.d(
//                                        TAG,
//                                        "fetchProductDetails subscriptionOfferDetails:double Amount  ${it}"
//                                    )
//                                    Singular.revenue(
//                                        oneTimeDetails.priceCurrencyCode,
//                                        doubleAmount,
//                                        purchase
//                                    )
//                                }
                            }
                            it.subscriptionOfferDetails?.let { subDetails ->
                                subDetails.forEachIndexed { index, item ->

//                               Singular.customRevenue(eventName, currency, price.toDouble(), purchase)


                                    item.pricingPhases.pricingPhaseList.forEach {
//
                                        Log.d(
                                            TAG,
                                            "fetchProductDetails subscriptionOfferDetails:priceAmountMicros $index ${it.priceAmountMicros}"
                                        )
                                        Log.d(
                                            TAG,
                                            "fetchProductDetails subscriptionOfferDetails:billingCycleCount $index ${it.billingCycleCount}"
                                        )
                                        Log.d(
                                            TAG,
                                            "fetchProductDetails subscriptionOfferDetails:billingPeriod $index ${it.billingPeriod}"
                                        )
                                        Log.d(
                                            TAG,
                                            "fetchProductDetails subscriptionOfferDetails:formattedPrice $index ${it.formattedPrice}"
                                        )
                                        runCatching {
                                            (it.priceAmountMicros / 1000000.0).let { doubleAmount ->
                                                Singular.revenue(
                                                    it.priceCurrencyCode,
                                                    doubleAmount,
                                                    purchase
                                                )
                                            }
                                        }
//                                        getDoubleAmountFromFormattedPrice(formattedPrice = it.formattedPrice)?.let { doubleAmount ->
//                                            Log.d(
//                                                TAG,
//                                                "fetchProductDetails subscriptionOfferDetails:double Amount $index ${doubleAmount}"
//                                            )
//
//                                        }

                                        Log.d(
                                            TAG,
                                            "fetchProductDetails subscriptionOfferDetails :priceCurrencyCode $index ${it.priceCurrencyCode}"
                                        )
                                        Log.d(
                                            TAG,
                                            "fetchProductDetails subscriptionOfferDetails :recurrenceMode $index ${it.recurrenceMode}"
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    private suspend fun handlePurchaseInApp(purchase: Purchase, showEvent: Boolean = false) {
        if (purchase.purchaseState == PurchaseState.PURCHASED) {
            if (!purchase.isAcknowledged) {
                val acknowledgePurchaseParams = AcknowledgePurchaseParams.newBuilder()
                    .setPurchaseToken(purchase.purchaseToken)
                val ackPurchaseResult = withContext(Dispatchers.IO) {
                    billingClient.acknowledgePurchase(acknowledgePurchaseParams.build())
                }
                if (ackPurchaseResult.responseCode == BillingClient.BillingResponseCode.OK) {
                    Log.d(TAG, "purchase Products: ${purchase.originalJson}")
                    isProVersion.postValue(true)
                    IN_APP_PURCHASED_PRODUCT_DETAILS.add(purchase)
                    if (showEvent) {
                        firebaseAnalytics?.logEvent("purchase_inapp_success", null)
                        Log.d("MyFirebaseEvent", "purchase_inapp_success")

                        runCatching {
                            setPurchaseItemEventToSingularInApp(purchase.products[0], purchase)
                        }
                    }
                }
            }
        }
    }

    private suspend fun setPurchaseItemEventToSingularInApp(
        productSku: String?,
        purchase: Purchase
    ) {
        Log.e(TAG, "fetchProductDetailsInApp: ")
        try {
            productSku?.let {
                val productList = ArrayList<QueryProductDetailsParams.Product>()
                productList.add(
                    QueryProductDetailsParams.Product.newBuilder()
                        .setProductId(productSku)
                        .setProductType(BillingClient.ProductType.INAPP)
                        .build()
                )
                val params = QueryProductDetailsParams.newBuilder()
                params.setProductList(productList)

                val productDetailsResult = withContext(Dispatchers.IO) {
                    billingClient.queryProductDetails(params.build())
                }

                Log.e(TAG, "fetchProductDetailsInApp 22: ")

                productDetailsResult.apply {
                    when (billingResult.responseCode) {
                        BillingClient.BillingResponseCode.OK -> {
                            Log.e(TAG, "BillingResponseCode ok: ")
                            productDetailsList?.forEach {
                                synchronized(IN_APP_PRODUCT_DETAILS) {
                                    IN_APP_PRODUCT_DETAILS.add(it)
                                }
                                Log.e(TAG, "fetchProductDetails:productId ${it.productId}")
                                Log.e(TAG, "fetchProductDetails:name ${it.name}")
                                Log.e(TAG, "fetchProductDetails:productType ${it.productType}")
                                Log.e(TAG, "fetchProductDetails:description ${it.description}")
                                it.oneTimePurchaseOfferDetails?.let { oneTimeDetails ->
                                    Log.d(
                                        TAG,
                                        "fetchProductDetails:formattedPrice ${oneTimeDetails.formattedPrice}"
                                    )
                                    Log.d(
                                        TAG,
                                        "fetchProductDetails:priceAmountMicros ${oneTimeDetails.priceAmountMicros}"
                                    )
                                    Log.d(
                                        TAG,
                                        "fetchProductDetails:priceCurrencyCode ${oneTimeDetails.priceCurrencyCode}"
                                    )
                                    runCatching {
                                        (oneTimeDetails.priceAmountMicros / 1000000.0).let { doubleAmount ->
                                            Singular.revenue(
                                                oneTimeDetails.priceCurrencyCode,
                                                doubleAmount,
                                                purchase
                                            )
                                        }
                                    }
//                                    getDoubleAmountFromFormattedPrice(formattedPrice = oneTimeDetails.formattedPrice)?.let { doubleAmount ->
//                                        Log.d(
//                                            TAG,
//                                            "fetchProductDetails subscriptionOfferDetails:double Amount  ${it}"
//                                        )
//                                        Singular.revenue(
//                                            oneTimeDetails.priceCurrencyCode,
//                                            doubleAmount,
//                                            purchase
//                                        )
//                                    }
                                }
                                it.subscriptionOfferDetails?.let { subDetails ->
                                    subDetails.forEachIndexed { index, item ->
                                        Log.d(
                                            TAG,
                                            "fetchProductDetails:basePlanId $index ${item.basePlanId}"
                                        )
                                        Log.d(
                                            TAG,
                                            "fetchProductDetails:offerId $index ${item.offerId}"
                                        )
                                        item.offerTags.forEach {
                                            Log.d(
                                                TAG,
                                                "fetchProductDetails:offerTags $index ${it}"
                                            )
                                        }
                                        Log.d(
                                            TAG,
                                            "fetchProductDetails:offerToken $index ${item.offerToken}"
                                        )
                                        item.pricingPhases.pricingPhaseList.forEach {
                                            Log.d(
                                                TAG,
                                                "fetchProductDetails:priceAmountMicros $index ${it.priceAmountMicros}"
                                            )
                                            Log.d(
                                                TAG,
                                                "fetchProductDetails:billingCycleCount $index ${it.billingCycleCount}"
                                            )
                                            Log.d(
                                                TAG,
                                                "fetchProductDetails:billingPeriod $index ${it.billingPeriod}"
                                            )
                                            Log.d(
                                                TAG,
                                                "fetchProductDetails:formattedPrice $index ${it.formattedPrice}"
                                            )
                                            Log.d(
                                                TAG,
                                                "fetchProductDetails:priceCurrencyCode $index ${it.priceCurrencyCode}"
                                            )
                                            Log.d(
                                                TAG,
                                                "fetchProductDetails:recurrenceMode $index ${it.recurrenceMode}"
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        } catch (e: Exception) {

        }
    }

    private fun getDoubleAmountFromFormattedPrice(formattedPrice: String): Double? {
        // Regular expression pattern to match decimal and integer numbers

        // Regular expression pattern to match decimal numbers with commas as thousands separators
        try {
            val pattern = Pattern.compile("\\d{1,3}(,\\d{3})*\\.\\d+")

            val matcher = pattern.matcher(formattedPrice)

// Check if the pattern is found
            return if (matcher.find()) {
                val decimalNumber = matcher.group()
                decimalNumber.replace(",", "")
                    .toDouble() // Remove commas before converting to double
            } else {
                println("Decimal number not found in the string.")
                null
            }
        } catch (ex: java.lang.Exception) {
            return null
        }
    }
}