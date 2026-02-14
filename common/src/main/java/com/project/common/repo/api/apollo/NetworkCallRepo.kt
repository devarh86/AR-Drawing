package com.project.common.repo.api.apollo

import android.os.Bundle
import android.util.Log
import androidx.annotation.Keep
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.apollographql.apollo3.ApolloClient
import com.apollographql.apollo3.exception.ApolloException
import com.example.analytics.Constants.firebaseAnalytics
import com.example.analytics.Events
import com.fahad.newtruelovebyfahad.GetEffectsQuery
import com.fahad.newtruelovebyfahad.GetFeatureScreenQuery
import com.fahad.newtruelovebyfahad.GetFiltersQuery
import com.fahad.newtruelovebyfahad.GetFrameQuery
import com.fahad.newtruelovebyfahad.GetHomeAndTemplateScreenDataQuery
import com.fahad.newtruelovebyfahad.GetMainScreenQuery
import com.fahad.newtruelovebyfahad.GetSearchFramesQuery
import com.fahad.newtruelovebyfahad.GetSearchTagsQuery
import com.fahad.newtruelovebyfahad.GetStickersQuery
import com.fahad.newtruelovebyfahad.GetTokenMutation
import com.project.common.repo.api.apollo.helper.ApiConstants.AUTHORIZATION
import com.project.common.repo.api.apollo.helper.ApiConstants.KEY
import com.project.common.repo.api.apollo.helper.ApiConstants.PASSWORD
import com.project.common.repo.api.apollo.helper.ApiConstants.USER_NAME
import com.project.common.repo.api.apollo.helper.Response
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.TimeZone
import java.util.concurrent.atomic.AtomicBoolean
import javax.inject.Inject
import javax.inject.Singleton


@Keep
@Singleton
class NetworkCallRepo @Inject constructor(
    private val apolloClient: ApolloClient,
) : ApiService {

    private val _token: MutableLiveData<Response<String>> = MutableLiveData()
    val token: LiveData<Response<String>> get() = _token

    fun clearToken() {
        _token.value = Response.Error("")
    }

    override suspend fun getToken(networkState: Boolean) {
        when (_token.value) {
            is Response.Loading -> {
                Log.d("Fahad", "initApiObservers: ")
            }

            is Response.Success -> {
                Log.d("Fahad", "initApiObservers: ")
            }

            else -> {
                _token.postValue(Response.Loading())
                withContext(IO) {
                    val loadingTime = System.currentTimeMillis()
                    try {
                        firebaseAnalytics?.logEvent(Events.ApiKeys.AUTH_TOKEN,
                            Bundle().apply {
                                putString(Events.ApiParams.STATE, Events.ApiStates.LOADING)
                                putString(Events.ApiParams.TIME, loadingTime.formattedTime())
                                putString(
                                    Events.ApiParams.INTERNET_STATE,
                                    if (networkState) "true" else "false"
                                )
                            }
                        )
                        val token = apolloClient.mutation(GetTokenMutation(USER_NAME, PASSWORD))
                            .execute()
                            .data?.authenticateApp?.token
                        token?.let {
                            firebaseAnalytics?.logEvent(Events.ApiKeys.AUTH_TOKEN,
                                Bundle().apply {
                                    putString(Events.ApiParams.STATE, Events.ApiStates.SUCCESS)
                                    putString(
                                        Events.ApiParams.TIME,
                                        System.currentTimeMillis().formattedTime()
                                    )
                                    putString(
                                        Events.ApiParams.TOTAL_TIME_CONSUMED,
                                        (System.currentTimeMillis() - loadingTime).formattedTime()
                                    )
                                    putString(
                                        Events.ApiParams.INTERNET_STATE,
                                        if (networkState) "true" else "false"
                                    )
                                }
                            )
                            _token.postValue(
                                Response.Success(
                                    it
                                )
                            )
                        } ?: run {
                            firebaseAnalytics?.logEvent(Events.ApiKeys.AUTH_TOKEN,
                                Bundle().apply {
                                    putString(Events.ApiParams.STATE, Events.ApiStates.FAILED)
                                    putString(
                                        Events.ApiParams.TIME,
                                        System.currentTimeMillis().formattedTime()
                                    )
                                    putString(
                                        Events.ApiParams.TOTAL_TIME_CONSUMED,
                                        (System.currentTimeMillis() - loadingTime).formattedTime()
                                    )
                                    putString(
                                        Events.ApiParams.INTERNET_STATE,
                                        if (networkState) "true" else "false"
                                    )
                                }
                            )
                            _token.postValue(
                                Response.Error("")
                            )
                        }

                    } catch (ex: Exception) {
                        firebaseAnalytics?.logEvent(Events.ApiKeys.AUTH_TOKEN,
                            Bundle().apply {
                                putString(Events.ApiParams.STATE, Events.ApiStates.EXCEPTION)
                                putString(
                                    Events.ApiParams.TIME,
                                    System.currentTimeMillis().formattedTime()
                                )
                                putString(
                                    Events.ApiParams.TOTAL_TIME_CONSUMED,
                                    (System.currentTimeMillis() - loadingTime).formattedTime()
                                )
                                putString(Events.ApiParams.ERROR_MESSAGE, ex.message ?: "")
                                putString(
                                    Events.ApiParams.INTERNET_STATE,
                                    if (networkState) "true" else "false"
                                )
                            }
                        )
                        _token.postValue(Response.Error(ex.message.toString()))
                    }
                }
            }
        }
    }

    private val _stickers: MutableLiveData<Response<GetStickersQuery.Data?>> =
        MutableLiveData()
    val stickers: LiveData<Response<GetStickersQuery.Data?>> get() = _stickers

    private val isLoadingSticker = AtomicBoolean(false)

    override suspend fun getStickers() {
        when (_stickers.value) {
            is Response.Loading -> {
                Log.d("Fahad", "initApiObservers: ")
            }

            is Response.Success -> {
                Log.d("Fahad", "initApiObservers: ")
            }

            else -> {
                if (isLoadingSticker.get()) {
                    return
                }

                isLoadingSticker.set(true)

                _stickers.postValue(Response.Loading())

                withContext(IO) {
                    val loadingTime = System.currentTimeMillis()
                    try {
                        firebaseAnalytics?.logEvent(Events.ApiKeys.STICKERS,
                            Bundle().apply {
                                putString(Events.ApiParams.STATE, Events.ApiStates.LOADING)
                                putString(Events.ApiParams.TIME, loadingTime.formattedTime())
                            }
                        )
                        val data = apolloClient
                            .query(GetStickersQuery("sticker"))
                            .addHttpHeader(AUTHORIZATION, KEY)
                            .execute()
                            .data

                        data?.let {
                            firebaseAnalytics?.logEvent(Events.ApiKeys.STICKERS,
                                Bundle().apply {
                                    putString(Events.ApiParams.STATE, Events.ApiStates.SUCCESS)
                                    putString(
                                        Events.ApiParams.TIME,
                                        System.currentTimeMillis().formattedTime()
                                    )
                                    putString(
                                        Events.ApiParams.TOTAL_TIME_CONSUMED,
                                        (System.currentTimeMillis() - loadingTime).formattedTime()
                                    )
                                }
                            )
                            _stickers.postValue(Response.Success(it))
                            isLoadingSticker.set(false)
                        } ?: run {
                            firebaseAnalytics?.logEvent(Events.ApiKeys.STICKERS,
                                Bundle().apply {
                                    putString(Events.ApiParams.STATE, Events.ApiStates.FAILED)
                                    putString(
                                        Events.ApiParams.TIME,
                                        System.currentTimeMillis().formattedTime()
                                    )
                                    putString(
                                        Events.ApiParams.TOTAL_TIME_CONSUMED,
                                        (System.currentTimeMillis() - loadingTime).formattedTime()
                                    )
                                }
                            )
                            _stickers.postValue(Response.Error(""))
                            isLoadingSticker.set(false)
                        }
                    } catch (ex: Exception) {
                        firebaseAnalytics?.logEvent(Events.ApiKeys.STICKERS,
                            Bundle().apply {
                                putString(Events.ApiParams.STATE, Events.ApiStates.EXCEPTION)
                                putString(
                                    Events.ApiParams.TIME,
                                    System.currentTimeMillis().formattedTime()
                                )
                                putString(
                                    Events.ApiParams.TOTAL_TIME_CONSUMED,
                                    (System.currentTimeMillis() - loadingTime).formattedTime()
                                )
                                putString(Events.ApiParams.ERROR_MESSAGE, ex.message ?: "")
                            }
                        )
                        _stickers.postValue(Response.Error(ex.message.toString()))
                        isLoadingSticker.set(false)
                    }
                }
            }
        }
    }

    private val _filters: MutableLiveData<Response<GetFiltersQuery.Data?>> =
        MutableLiveData()
    val filters: LiveData<Response<GetFiltersQuery.Data?>> get() = _filters

    private val isLoadingFilter = AtomicBoolean(false)

    override suspend fun getFilters() {
        when (_filters.value) {
            is Response.Loading -> {
                Log.d("Fahad", "initApiObservers: ")
            }

            is Response.Success -> {
                Log.d("Fahad", "initApiObservers: ")
            }

            else -> {

                if (isLoadingFilter.get()) {
                    return
                }

                isLoadingFilter.set(true)

                _filters.postValue(Response.Loading())
                withContext(IO) {
                    val loadingTime = System.currentTimeMillis()
                    try {
                        firebaseAnalytics?.logEvent(Events.ApiKeys.FILTERS,
                            Bundle().apply {
                                putString(Events.ApiParams.STATE, Events.ApiStates.LOADING)
                                putString(Events.ApiParams.TIME, loadingTime.formattedTime())
                            }
                        )
                        val data = apolloClient
                            .query(GetFiltersQuery("filter"))
                            .addHttpHeader(AUTHORIZATION, KEY)
                            .execute()
                            .data

                        data?.let {
                            firebaseAnalytics?.logEvent(Events.ApiKeys.FILTERS,
                                Bundle().apply {
                                    putString(Events.ApiParams.STATE, Events.ApiStates.SUCCESS)
                                    putString(
                                        Events.ApiParams.TIME,
                                        System.currentTimeMillis().formattedTime()
                                    )
                                    putString(
                                        Events.ApiParams.TOTAL_TIME_CONSUMED,
                                        (System.currentTimeMillis() - loadingTime).formattedTime()
                                    )
                                }
                            )
                            _filters.postValue(Response.Success(it))
                            isLoadingFilter.set(false)
                        } ?: run {
                            firebaseAnalytics?.logEvent(Events.ApiKeys.FILTERS,
                                Bundle().apply {
                                    putString(Events.ApiParams.STATE, Events.ApiStates.FAILED)
                                    putString(
                                        Events.ApiParams.TIME,
                                        System.currentTimeMillis().formattedTime()
                                    )
                                    putString(
                                        Events.ApiParams.TOTAL_TIME_CONSUMED,
                                        (System.currentTimeMillis() - loadingTime).formattedTime()
                                    )
                                }
                            )
                            _filters.postValue(Response.Error(""))
                            isLoadingFilter.set(false)
                        }
                    } catch (ex: Exception) {
                        firebaseAnalytics?.logEvent(Events.ApiKeys.FILTERS,
                            Bundle().apply {
                                putString(Events.ApiParams.STATE, Events.ApiStates.EXCEPTION)
                                putString(
                                    Events.ApiParams.TIME,
                                    System.currentTimeMillis().formattedTime()
                                )
                                putString(
                                    Events.ApiParams.TOTAL_TIME_CONSUMED,
                                    (System.currentTimeMillis() - loadingTime).formattedTime()
                                )
                                putString(Events.ApiParams.ERROR_MESSAGE, ex.message ?: "")
                            }
                        )
                        _filters.postValue(Response.Error(ex.message.toString()))
                        isLoadingFilter.set(false)
                    }
                }
            }
        }
    }

    private val _effects: MutableLiveData<Response<GetEffectsQuery.Data?>> = MutableLiveData()
    val effects: LiveData<Response<GetEffectsQuery.Data?>> get() = _effects

    private val isLoadingEffects = AtomicBoolean(false)

    override suspend fun getEffects() {
        when (_effects.value) {
            is Response.Loading -> {
                Log.d("Fahad", "initApiObservers: ")
            }

            is Response.Success -> {
                Log.d("Fahad", "initApiObservers: ")
            }

            else -> {
                _effects.postValue(Response.Loading())

                if (isLoadingEffects.get()) {
                    return
                }

                isLoadingEffects.set(true)

                withContext(IO) {
                    val loadingTime = System.currentTimeMillis()
                    try {
                        firebaseAnalytics?.logEvent(Events.ApiKeys.EFFECTS,
                            Bundle().apply {
                                putString(Events.ApiParams.STATE, Events.ApiStates.LOADING)
                                putString(Events.ApiParams.TIME, loadingTime.formattedTime())
                            }
                        )
                        val data = apolloClient
                            .query(GetEffectsQuery("effect"))
                            .addHttpHeader(AUTHORIZATION, KEY)
                            .execute()
                            .data

                        data?.let {
                            firebaseAnalytics?.logEvent(Events.ApiKeys.EFFECTS,
                                Bundle().apply {
                                    putString(Events.ApiParams.STATE, Events.ApiStates.SUCCESS)
                                    putString(
                                        Events.ApiParams.TIME,
                                        System.currentTimeMillis().formattedTime()
                                    )
                                    putString(
                                        Events.ApiParams.TOTAL_TIME_CONSUMED,
                                        (System.currentTimeMillis() - loadingTime).formattedTime()
                                    )
                                }
                            )
                            _effects.postValue(Response.Success(it))
                            isLoadingEffects.set(false)
                        } ?: run {
                            firebaseAnalytics?.logEvent(Events.ApiKeys.EFFECTS,
                                Bundle().apply {
                                    putString(Events.ApiParams.STATE, Events.ApiStates.FAILED)
                                    putString(
                                        Events.ApiParams.TIME,
                                        System.currentTimeMillis().formattedTime()
                                    )
                                    putString(
                                        Events.ApiParams.TOTAL_TIME_CONSUMED,
                                        (System.currentTimeMillis() - loadingTime).formattedTime()
                                    )
                                }
                            )
                            _effects.postValue(Response.Error(""))
                            isLoadingEffects.set(false)
                        }
                    } catch (ex: Exception) {
                        firebaseAnalytics?.logEvent(Events.ApiKeys.EFFECTS,
                            Bundle().apply {
                                putString(Events.ApiParams.STATE, Events.ApiStates.EXCEPTION)
                                putString(
                                    Events.ApiParams.TIME,
                                    System.currentTimeMillis().formattedTime()
                                )
                                putString(
                                    Events.ApiParams.TOTAL_TIME_CONSUMED,
                                    (System.currentTimeMillis() - loadingTime).formattedTime()
                                )
                                putString(Events.ApiParams.ERROR_MESSAGE, ex.message ?: "")
                            }
                        )
                        _effects.postValue(Response.Error(ex.message.toString()))
                        isLoadingEffects.set(false)
                    }
                }
            }
        }
    }

    private val _backgrounds: MutableLiveData<Response<GetStickersQuery.Data?>> = MutableLiveData()
    val backgrounds: LiveData<Response<GetStickersQuery.Data?>> get() = _backgrounds

    private val isLoadingBg = AtomicBoolean(false)

    override suspend fun getBackgrounds() {
        when (_backgrounds.value) {
            is Response.Loading -> {
                Log.d("Fahad", "initApiObservers: ")
            }

            is Response.Success -> {
                Log.d("Fahad", "initApiObservers: ")
            }

            else -> {

                if (isLoadingBg.get()) {
                    return
                }

                isLoadingBg.set(true)

                _backgrounds.postValue(Response.Loading())
                withContext(IO) {
                    val loadingTime = System.currentTimeMillis()
                    try {
                        firebaseAnalytics?.logEvent(Events.ApiKeys.BACKGROUNDS,
                            Bundle().apply {
                                putString(Events.ApiParams.STATE, Events.ApiStates.LOADING)
                                putString(Events.ApiParams.TIME, loadingTime.formattedTime())
                            }
                        )
                        val data = apolloClient
                            .query(GetStickersQuery("background"))
                            .addHttpHeader(AUTHORIZATION, KEY)
                            .execute()
                            .data
                        data?.let {
                            firebaseAnalytics?.logEvent(Events.ApiKeys.BACKGROUNDS,
                                Bundle().apply {
                                    putString(Events.ApiParams.STATE, Events.ApiStates.SUCCESS)
                                    putString(
                                        Events.ApiParams.TIME,
                                        System.currentTimeMillis().formattedTime()
                                    )
                                    putString(
                                        Events.ApiParams.TOTAL_TIME_CONSUMED,
                                        (System.currentTimeMillis() - loadingTime).formattedTime()
                                    )
                                }
                            )
                            _backgrounds.postValue(Response.Success(it))
                            isLoadingBg.set(false)
                        } ?: run {
                            firebaseAnalytics?.logEvent(Events.ApiKeys.BACKGROUNDS,
                                Bundle().apply {
                                    putString(Events.ApiParams.STATE, Events.ApiStates.FAILED)
                                    putString(
                                        Events.ApiParams.TIME,
                                        System.currentTimeMillis().formattedTime()
                                    )
                                    putString(
                                        Events.ApiParams.TOTAL_TIME_CONSUMED,
                                        (System.currentTimeMillis() - loadingTime).formattedTime()
                                    )
                                }
                            )
                            _backgrounds.postValue(Response.Error(""))
                            isLoadingBg.set(false)
                        }
                    } catch (ex: Exception) {
                        firebaseAnalytics?.logEvent(Events.ApiKeys.BACKGROUNDS,
                            Bundle().apply {
                                putString(Events.ApiParams.STATE, Events.ApiStates.EXCEPTION)
                                putString(
                                    Events.ApiParams.TIME,
                                    System.currentTimeMillis().formattedTime()
                                )
                                putString(
                                    Events.ApiParams.TOTAL_TIME_CONSUMED,
                                    (System.currentTimeMillis() - loadingTime).formattedTime()
                                )
                                putString(Events.ApiParams.ERROR_MESSAGE, ex.message ?: "")
                            }
                        )
                        _backgrounds.postValue(Response.Error(ex.message.toString()))
                        isLoadingBg.set(false)
                    }
                }
            }
        }
    }

    private var isAlreadyFeatureLoading = false

    private val _featureScreen: MutableLiveData<Response<GetFeatureScreenQuery.Data?>> =
        MutableLiveData()
    val featureScreen: LiveData<Response<GetFeatureScreenQuery.Data?>> get() = _featureScreen
    override suspend fun getFeatureScreen() {
        Log.i("TAG", "getFeatureScreen: ${_featureScreen.value}")
        when (_featureScreen.value) {
            is Response.Loading -> {
                Log.d("Fahad", "initApiObservers: ")
            }

            is Response.Success -> {
                Log.d("Fahad", "initApiObservers: ")
            }

            is Response.ShowSlowInternet -> {
                Log.d("Fahad", "slowInternet: ")
            }

            else -> {

                if (isAlreadyFeatureLoading)
                    return

                var isLoading = true

                isAlreadyFeatureLoading = true

                _featureScreen.postValue(Response.Loading())

                withContext(IO) {
                    val loadingTime = System.currentTimeMillis()
                    val initialTime = System.currentTimeMillis()
                    try {
                        firebaseAnalytics?.logEvent(Events.ApiKeys.FEATURE,
                            Bundle().apply {
                                putString(Events.ApiParams.STATE, Events.ApiStates.LOADING)
                                putString(Events.ApiParams.TIME, loadingTime.formattedTime())
                            }
                        )

                        val job = CoroutineScope(IO).launch {
                            while (isActive) {
                                try {

                                    if (!isLoading) {
                                        break
                                    }

                                    val time = System.currentTimeMillis()
                                    val timeConsume = time - initialTime
                                    Log.i("TAG", "getFeatureScreen: $timeConsume")
                                    if (timeConsume >= 10000) {
                                        _featureScreen.postValue(Response.ShowSlowInternet("Slow Internet"))
                                        break
                                    }
                                    delay(1000)
                                } catch (ex: java.lang.Exception) {
                                    break
                                }
                            }
                        }

                        val data = apolloClient
                            .query(GetFeatureScreenQuery())
                            .addHttpHeader(AUTHORIZATION, KEY)
                            .execute()
                            .data

                        isLoading = false
                        job.cancel()
                        data?.allTags?.let {

                            Log.i("TAG", "getFeatureScreen: $data")

                            firebaseAnalytics?.logEvent(Events.ApiKeys.FEATURE,
                                Bundle().apply {
                                    putString(Events.ApiParams.STATE, Events.ApiStates.SUCCESS)
                                    putString(
                                        Events.ApiParams.TIME,
                                        System.currentTimeMillis().formattedTime()
                                    )
                                    putString(
                                        Events.ApiParams.TOTAL_TIME_CONSUMED,
                                        (System.currentTimeMillis() - loadingTime).formattedTime()
                                    )
                                }
                            )
                            _featureScreen.postValue(Response.Success(data))
                            isAlreadyFeatureLoading = false
                        } ?: run {

                            Log.i("TAG", "getFeatureScreen: error")

                            firebaseAnalytics?.logEvent(Events.ApiKeys.FEATURE,
                                Bundle().apply {
                                    putString(Events.ApiParams.STATE, Events.ApiStates.FAILED)
                                    putString(
                                        Events.ApiParams.TIME,
                                        System.currentTimeMillis().formattedTime()
                                    )
                                    putString(
                                        Events.ApiParams.TOTAL_TIME_CONSUMED,
                                        (System.currentTimeMillis() - loadingTime).formattedTime()
                                    )
                                }
                            )
                            _featureScreen.postValue(Response.Error(""))
                            isAlreadyFeatureLoading = false
                        }
                    } catch (ex: Exception) {
                        Log.i("TAG", "getFeatureScreen: ${ex.message}")

                        isLoading = false


                        firebaseAnalytics?.logEvent(Events.ApiKeys.FEATURE,
                            Bundle().apply {
                                putString(Events.ApiParams.STATE, Events.ApiStates.EXCEPTION)
                                putString(
                                    Events.ApiParams.TIME,
                                    System.currentTimeMillis().formattedTime()
                                )
                                putString(
                                    Events.ApiParams.TOTAL_TIME_CONSUMED,
                                    (System.currentTimeMillis() - loadingTime).formattedTime()
                                )
                                putString(Events.ApiParams.ERROR_MESSAGE, ex.message ?: "")
                            }
                        )
                        _featureScreen.postValue(Response.Error(ex.message.toString()))
                        isAlreadyFeatureLoading = false
                    }
                }
            }
        }
    }

    //    var flow: Flow<Response<GetHomeAndTemplateScreenDataQuery.Data?>> = Flowo
    private var homeData: GetHomeAndTemplateScreenDataQuery.Data? = null

    private val isLoading = AtomicBoolean(false)

    override suspend fun getHomeAndTemplateScreen() =
        flow<Response<GetHomeAndTemplateScreenDataQuery.Data?>> {

//            Log.i("TAG", "getHomeAndTemplateScreen: ${isLoading.get()}")

            if (isLoading.compareAndSet(false, true)) {

                val loadingTime = System.currentTimeMillis()

                try {
                    homeData?.screens?.let {
                        emit(Response.Success(homeData))
                        isLoading.set(false)
                        return@flow
                    }
                    var isLoading = true
                    emit(Response.Loading())
                    logAnalyticsEvent(Events.ApiStates.LOADING, loadingTime)
                        val data = apolloClient
                            .query(GetHomeAndTemplateScreenDataQuery())
                            .addHttpHeader(AUTHORIZATION, KEY)
                            .execute()
                            .data

                        data?.screens?.let {
                            logAnalyticsEvent(Events.ApiStates.SUCCESS, loadingTime)
                            homeData = data
                            emit(Response.Success(data))
                        } ?: run {
                            handleDataError(loadingTime)
                            emit(Response.Error("Failed to fetch data"))
                        }


//                    Log.i("TAG", "getHomeAndTemplateScreen: $AUTHORIZATION, $KEY")
//                    }
                } catch (ex: ApolloException) {
                    handleException(ex, loadingTime)
                    emit(Response.Error("Network error"))
                } catch (ex: IOException) {
                    handleException(ex, loadingTime)
                    emit(Response.Error("IO error"))
                } catch (ex: Exception) {
                    handleException(ex, loadingTime)
                    emit(Response.Error("Unexpected error"))
                } finally {
                    isLoading.set(false)
                }
            } else {
                Log.i("TAG", "getHomeAndTemplateScreen: already loading")
                emit(Response.Loading())
            }
        }.flowOn(IO)

    private fun logAnalyticsEvent(state: String, loadingTime: Long) {
        firebaseAnalytics?.logEvent(Events.ApiKeys.HOME, Bundle().apply {
            putString(Events.ApiParams.STATE, state)
            putString(Events.ApiParams.TIME, loadingTime.formattedTime())
            putString(
                Events.ApiParams.TOTAL_TIME_CONSUMED,
                (System.currentTimeMillis() - loadingTime).formattedTime()
            )
        })
    }

    private fun handleDataError(loadingTime: Long) {
        logAnalyticsEvent(Events.ApiStates.FAILED, loadingTime)
//        Log.i("TAG", "getHomeAndTemplateScreen: Data is null or empty")
    }

    private fun handleException(ex: Exception, loadingTime: Long) {
        logAnalyticsEvent(Events.ApiStates.EXCEPTION, loadingTime)
//        Log.i("TAG", "getHomeAndTemplateScreenRepo: ${ex.message}")
    }

    private val _searchTags: MutableLiveData<Response<GetSearchTagsQuery.Data>> = MutableLiveData()
    val searchTags: LiveData<Response<GetSearchTagsQuery.Data>> get() = _searchTags

    private val searchTagsLoading = AtomicBoolean(false)

    override suspend fun getSearchTags() {

        when (_searchTags.value) {
            is Response.Loading -> {
                Log.d("Fahad", "initApiObservers: ")
            }

            is Response.Success -> {
                Log.d("Fahad", "initApiObservers: ")
            }

            else -> {

                if (searchTagsLoading.get()) {
                    return
                }

                searchTagsLoading.set(true)

                _mainScreen.postValue(Response.Loading())

                withContext(IO) {
                    val loadingTime = System.currentTimeMillis()
                    try {
                        firebaseAnalytics?.logEvent(Events.ApiKeys.MAIN,
                            Bundle().apply {
                                putString(Events.ApiParams.STATE, Events.ApiStates.LOADING)
                                putString(Events.ApiParams.TIME, loadingTime.formattedTime())
                            }
                        )
                        val data = apolloClient
                            .query(GetSearchTagsQuery())
                            .addHttpHeader(AUTHORIZATION, KEY)
                            .execute()
                            .data
                        data?.let {
                            firebaseAnalytics?.logEvent(Events.ApiKeys.MAIN,
                                Bundle().apply {
                                    putString(Events.ApiParams.STATE, Events.ApiStates.SUCCESS)
                                    putString(
                                        Events.ApiParams.TIME,
                                        System.currentTimeMillis().formattedTime()
                                    )
                                    putString(
                                        Events.ApiParams.TOTAL_TIME_CONSUMED,
                                        (System.currentTimeMillis() - loadingTime).formattedTime()
                                    )
                                }
                            )
                            _searchTags.postValue(Response.Success(it))
                            searchTagsLoading.set(false)
                        } ?: run {
                            firebaseAnalytics?.logEvent(Events.ApiKeys.MAIN,
                                Bundle().apply {
                                    putString(Events.ApiParams.STATE, Events.ApiStates.FAILED)
                                    putString(
                                        Events.ApiParams.TIME,
                                        System.currentTimeMillis().formattedTime()
                                    )
                                    putString(
                                        Events.ApiParams.TOTAL_TIME_CONSUMED,
                                        (System.currentTimeMillis() - loadingTime).formattedTime()
                                    )
                                }
                            )
                            _searchTags.postValue(Response.Error(""))
                            searchTagsLoading.set(false)
                        }
                    } catch (ex: Exception) {
                        firebaseAnalytics?.logEvent(Events.ApiKeys.MAIN,
                            Bundle().apply {
                                putString(Events.ApiParams.STATE, Events.ApiStates.EXCEPTION)
                                putString(
                                    Events.ApiParams.TIME,
                                    System.currentTimeMillis().formattedTime()
                                )
                                putString(
                                    Events.ApiParams.TOTAL_TIME_CONSUMED,
                                    (System.currentTimeMillis() - loadingTime).formattedTime()
                                )
                                putString(Events.ApiParams.ERROR_MESSAGE, ex.message ?: "")
                            }
                        )
                        _searchTags.postValue(Response.Error(ex.message.toString()))
                        searchTagsLoading.set(false)
                    }
                }
            }
        }
    }

    private val _mainScreen: MutableLiveData<Response<GetMainScreenQuery.Data?>> = MutableLiveData()
    val mainScreen: LiveData<Response<GetMainScreenQuery.Data?>> get() = _mainScreen
    private val _mainFromMainScreen: MutableLiveData<Response<GetMainScreenQuery.Data?>> =
        MutableLiveData()
    val mainFromMainScreen: LiveData<Response<GetMainScreenQuery.Data?>> get() = _mainFromMainScreen

    private val isLoadingMain = AtomicBoolean(false)

    override suspend fun getMainScreen() {
        Log.i("TAG", "getMainScreen: ${_mainScreen.value}")
        when (_mainScreen.value) {
            is Response.Loading -> {
                Log.d("Fahad", "initApiObservers: ")
            }

            is Response.Success -> {
                Log.d("Fahad", "initApiObservers: ")
            }

            else -> {

                if (isLoadingMain.get()) {
                    return
                }
                isLoadingMain.set(true)

               /* var isLoading = true*/
                _mainScreen.postValue(Response.Loading())

                withContext(IO) {
                    val loadingTime = System.currentTimeMillis()
                    val initialTime = System.currentTimeMillis()
                    try {
                        firebaseAnalytics?.logEvent(Events.ApiKeys.MAIN,
                            Bundle().apply {
                                putString(Events.ApiParams.STATE, Events.ApiStates.LOADING)
                                putString(Events.ApiParams.TIME, loadingTime.formattedTime())
                            }
                        )

                     /*   val job = CoroutineScope(IO).launch {
                            while (isActive) {
                                try {

                                    if (!isLoading) {
                                        break
                                    }

                                    val time = System.currentTimeMillis()
                                    val timeConsume = time - initialTime
                                    Log.i("TAG", "getFeatureScreen: $timeConsume")
                                   // if (timeConsume >= 10000) {
                                    if (timeConsume >= 1000) {
                                        _mainScreen.postValue(Response.ShowSlowInternet("Slow Internet"))
                                        _mainFromMainScreen.postValue(Response.ShowSlowInternet("Slow Internet"))
                                     //   _featureScreen.postValue(Response.ShowSlowInternet("Slow Internet"))
                                        break
                                    }
                                    delay(100)
                                   // delay(1000)
                                } catch (ex: java.lang.Exception) {
                                    break
                                }
                            }
                        }*/
                        val data = apolloClient
                            .query(GetMainScreenQuery())
                            .addHttpHeader(AUTHORIZATION, KEY)
                            .execute()
                            .data
                       /* isLoading = false
                        job.cancel()*/
                        data?.childCategories?.let {
                            firebaseAnalytics?.logEvent(Events.ApiKeys.MAIN,
                                Bundle().apply {
                                    putString(Events.ApiParams.STATE, Events.ApiStates.SUCCESS)
                                    putString(
                                        Events.ApiParams.TIME,
                                        System.currentTimeMillis().formattedTime()
                                    )
                                    putString(
                                        Events.ApiParams.TOTAL_TIME_CONSUMED,
                                        (System.currentTimeMillis() - loadingTime).formattedTime()
                                    )
                                }
                            )
                            _mainScreen.postValue(Response.Success(data))
                            _mainFromMainScreen.postValue(Response.Success(data))
                            isLoadingMain.set(false)
                        } ?: run {
                            firebaseAnalytics?.logEvent(Events.ApiKeys.MAIN,
                                Bundle().apply {
                                    putString(Events.ApiParams.STATE, Events.ApiStates.FAILED)
                                    putString(
                                        Events.ApiParams.TIME,
                                        System.currentTimeMillis().formattedTime()
                                    )
                                    putString(
                                        Events.ApiParams.TOTAL_TIME_CONSUMED,
                                        (System.currentTimeMillis() - loadingTime).formattedTime()
                                    )
                                }
                            )
                            _mainScreen.postValue(Response.Error(""))
                            _mainFromMainScreen.postValue(Response.Error(""))
                            isLoadingMain.set(false)
                        }
                    } catch (ex: Exception) {
                       /* isLoading = false*/
                        firebaseAnalytics?.logEvent(Events.ApiKeys.MAIN,
                            Bundle().apply {
                                putString(Events.ApiParams.STATE, Events.ApiStates.EXCEPTION)
                                putString(
                                    Events.ApiParams.TIME,
                                    System.currentTimeMillis().formattedTime()
                                )
                                putString(
                                    Events.ApiParams.TOTAL_TIME_CONSUMED,
                                    (System.currentTimeMillis() - loadingTime).formattedTime()
                                )
                                putString(Events.ApiParams.ERROR_MESSAGE, ex.message ?: "")
                            }
                        )
                        _mainScreen.postValue(Response.Error(ex.message.toString()))
                        _mainFromMainScreen.postValue(Response.Error(ex.message.toString()))
                        isLoadingMain.set(false)
                    }
                }
            }
        }
    }

    private val _frame: MutableLiveData<Response<GetFrameQuery.Data?>> = MutableLiveData()
    val frame: LiveData<Response<GetFrameQuery.Data?>> get() = _frame

    fun clearFrame() = _frame.postValue(Response.Error(""))
    override suspend fun getFrame(id: Int) {
        when (_frame.value) {
            is Response.Loading -> {
                Log.d("Fahad", "initApiObservers: ")
            }

            is Response.Success -> {
                Log.d("Fahad", "initApiObservers: ")
            }

            else -> {
                _frame.postValue(Response.Loading())
                withContext(IO) {
                    val loadingTime = System.currentTimeMillis()
                    try {
                        firebaseAnalytics?.logEvent(Events.ApiKeys.FRAME,
                            Bundle().apply {
                                putString(Events.ApiParams.STATE, Events.ApiStates.LOADING)
                                putString(Events.ApiParams.TIME, loadingTime.formattedTime())
                            }
                        )
                        val data = apolloClient
                            .query(GetFrameQuery(id))
                            .addHttpHeader(AUTHORIZATION, KEY)
                            .execute()
                            .data
                        data?.let {
                            firebaseAnalytics?.logEvent(Events.ApiKeys.FRAME,
                                Bundle().apply {
                                    putString(Events.ApiParams.STATE, Events.ApiStates.SUCCESS)
                                    putString(
                                        Events.ApiParams.TIME,
                                        System.currentTimeMillis().formattedTime()
                                    )
                                    putString(
                                        Events.ApiParams.TOTAL_TIME_CONSUMED,
                                        (System.currentTimeMillis() - loadingTime).formattedTime()
                                    )
                                }
                            )
                            _frame.postValue(Response.Success(it))
                        } ?: run {
                            firebaseAnalytics?.logEvent(Events.ApiKeys.FRAME,
                                Bundle().apply {
                                    putString(Events.ApiParams.STATE, Events.ApiStates.FAILED)
                                    putString(
                                        Events.ApiParams.TIME,
                                        System.currentTimeMillis().formattedTime()
                                    )
                                    putString(
                                        Events.ApiParams.TOTAL_TIME_CONSUMED,
                                        (System.currentTimeMillis() - loadingTime).formattedTime()
                                    )
                                }
                            )
                            _frame.postValue(Response.Error(""))
                        }
                    } catch (ex: Exception) {
                        firebaseAnalytics?.logEvent(Events.ApiKeys.FRAME,
                            Bundle().apply {
                                putString(Events.ApiParams.STATE, Events.ApiStates.EXCEPTION)
                                putString(
                                    Events.ApiParams.TIME,
                                    System.currentTimeMillis().formattedTime()
                                )
                                putString(
                                    Events.ApiParams.TOTAL_TIME_CONSUMED,
                                    (System.currentTimeMillis() - loadingTime).formattedTime()
                                )
                                putString(Events.ApiParams.ERROR_MESSAGE, ex.message ?: "")
                            }
                        )
                        _frame.postValue(Response.Error(ex.message.toString()))
                    }
                }
            }
        }
    }

    override suspend fun getSearchFrames(tag: String) = flow<Response<GetSearchFramesQuery.Data?>> {

//        isLoadingSearchFrames.set(true)

        emit(Response.Loading())

        val loadingTime = System.currentTimeMillis()
        try {
            firebaseAnalytics?.logEvent(Events.ApiKeys.SEARCH_FRAME,
                Bundle().apply {
                    putString(Events.ApiParams.STATE, Events.ApiStates.LOADING)
                    putString(Events.ApiParams.TIME, loadingTime.formattedTime())
                }
            )
            val data = apolloClient
                .query(GetSearchFramesQuery(tag))
                .addHttpHeader(AUTHORIZATION, KEY)
                .execute()
                .data

            data?.let {
                firebaseAnalytics?.logEvent(Events.ApiKeys.SEARCH_FRAME,
                    Bundle().apply {
                        putString(Events.ApiParams.STATE, Events.ApiStates.SUCCESS)
                        putString(
                            Events.ApiParams.TIME,
                            System.currentTimeMillis().formattedTime()
                        )
                        putString(
                            Events.ApiParams.TOTAL_TIME_CONSUMED,
                            (System.currentTimeMillis() - loadingTime).formattedTime()
                        )
                    }
                )
                emit(Response.Success(it))
//                    isLoadingSearchFrames.set(false)
            } ?: run {
                firebaseAnalytics?.logEvent(Events.ApiKeys.SEARCH_FRAME,
                    Bundle().apply {
                        putString(Events.ApiParams.STATE, Events.ApiStates.FAILED)
                        putString(
                            Events.ApiParams.TIME,
                            System.currentTimeMillis().formattedTime()
                        )
                        putString(
                            Events.ApiParams.TOTAL_TIME_CONSUMED,
                            (System.currentTimeMillis() - loadingTime).formattedTime()
                        )
                    }
                )
                emit(Response.Error(""))
//                    isLoadingSearchFrames.set(false)
            }
        } catch (ex: Exception) {
            firebaseAnalytics?.logEvent(Events.ApiKeys.SEARCH_FRAME,
                Bundle().apply {
                    putString(Events.ApiParams.STATE, Events.ApiStates.EXCEPTION)
                    putString(
                        Events.ApiParams.TIME,
                        System.currentTimeMillis().formattedTime()
                    )
                    putString(
                        Events.ApiParams.TOTAL_TIME_CONSUMED,
                        (System.currentTimeMillis() - loadingTime).formattedTime()
                    )
                    putString(Events.ApiParams.ERROR_MESSAGE, ex.message ?: "")
                }
            )
            emit(Response.Error(""))
//                isLoadingSearchFrames.set(false)
        }
    }.flowOn(IO)
}


fun Long.formattedTime() =
    SimpleDateFormat("mm:ss", Locale.getDefault()).apply { timeZone = TimeZone.getTimeZone("UTC") }
        .format(this) ?: ""