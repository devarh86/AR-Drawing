package com.project.common.viewmodels

import android.util.Log
import androidx.annotation.Keep
import androidx.compose.foundation.lazy.layout.MutableIntervalList
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.fahad.newtruelovebyfahad.GetSearchFramesQuery
import com.fahad.newtruelovebyfahad.GetSearchTagsQuery
import com.project.common.db_table.RecentSearchTable
import com.project.common.repo.api.apollo.NetworkCallRepo
import com.project.common.repo.api.apollo.helper.Response
import com.project.common.repo.room.FavouriteRepo
import com.project.common.repo.room.RecentSearchRepo
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.Job
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.util.concurrent.atomic.AtomicBoolean
import javax.inject.Inject

@HiltViewModel
class SearchViewModel @Inject constructor(
    private val recentSearchRepo: RecentSearchRepo,
    private val networkCallRepo: NetworkCallRepo,
    private val favouriteRepo: FavouriteRepo
) : ViewModel() {

    private var getFramesJob: Job? = null

    private var latestSearchJob: Job? = null

    private var searchJob: Job? = null
    // Cache to store previous search results
    private val cache = mutableMapOf<String, List<String>>()

    private var sortedWords: MutableList<String> = mutableListOf()

    private var insertionCompleteLoading: AtomicBoolean = AtomicBoolean(false)

    private val _stateRecent = MutableStateFlow<RecentSearchViewStates>(RecentSearchViewStates.Idle)
    val stateRecent: StateFlow<RecentSearchViewStates> get() = _stateRecent

    private val _state = MutableStateFlow<SearchViewStates>(SearchViewStates.Idle)
    val state: StateFlow<SearchViewStates> get() = _state

    private val _searchFrameState = MutableStateFlow<SearchViewStates>(SearchViewStates.Idle)
    val searchFrameState: StateFlow<SearchViewStates> get() = _searchFrameState

    var searchFramesList: MutableList<GetSearchFramesQuery.Search?> = mutableListOf()

    private val _networkState = MutableLiveData<Boolean>()
    val networkState: LiveData<Boolean> get() = _networkState

    fun setNetworkState(networkState: Boolean) {
        _networkState.value = networkState
    }

    fun insertDataInTrie(data: GetSearchTagsQuery.Data?) {
        if (!insertionCompleteLoading.get() && sortedWords.isEmpty()) {
            insertionCompleteLoading.set(true)
            viewModelScope.launch(IO) {
                data?.let {
                    kotlin.runCatching {
                        val list: MutableList<String> = mutableListOf()
                        it.allTags?.forEach {
                            Log.i("TAG", "insertDataInTrie allTags : $it")
                            it?.tags?.forEach {
                                it?.tags?.forEach {
                                    it?.title?.let {
                                        list.add(it)
                                    }
                                }
                            }
                        }
                        sortedWords.addAll(list.sorted().distinct())
                        insertionCompleteLoading.set(false)
                    }.onFailure {
                        insertionCompleteLoading.set(false)
                    }
                } ?: run {
                    insertionCompleteLoading.set(false)
                }
            }
        }
    }

    fun search(substring: String) {

        searchJob?.cancel()
        kotlin.runCatching {
            searchJob = viewModelScope.launch(IO) {
                if (!insertionCompleteLoading.get()) {
                    kotlin.runCatching {
                        // Check cache first
                        if (cache.containsKey(substring)) {
                            cache[substring]?.let {
                                ensureActive()
                                _state.value = SearchViewStates.UpdateList(it.toMutableList())
                            }
                        } else {

                            val result = mutableListOf<String>()

                            for (i in 0 until sortedWords.size) {
                                ensureActive()
                                val word = sortedWords[i]
                                if (word.isNotBlank() && word.contains(substring, true)) {
                                    val newWord = word.removePrefix("#")
                                    result.add(newWord)
                                    _state.value = SearchViewStates.UpdateObject(newWord)
                                }
                            }

                            // Cache the result for future queries
                            cache[substring] = result
                            ensureActive()
                            _state.value = SearchViewStates.UpdateList(result)

                            if (cache.size > 50) {
                                cache.clear()
                            } else {
                            }
                        }
                    }
                }
            }
        }
    }

    fun resetSearchValue() {
        _state.value = SearchViewStates.Idle
    }

    fun addToRecent(query: String) {
        viewModelScope.launch(IO) {
            kotlin.runCatching {
                recentSearchRepo.insertRecentSearch(
                    RecentSearchTable(
                        0,
                        query,
                        System.currentTimeMillis()
                    )
                )

                if (recentSearchRepo.getRecentSearchCount() > 10) {
                    recentSearchRepo.deleteOldestChip()
                }
            }
        }
    }

    fun readAllRecentSearches() {
        viewModelScope.launch(IO) {
            kotlin.runCatching {
                val list = recentSearchRepo.getAllData()
                _stateRecent.value = RecentSearchViewStates.UpdateList(list)
            }
        }
    }

    fun removeAllRecentSearches() {
        viewModelScope.launch(IO) {
            kotlin.runCatching {
                recentSearchRepo.deleteAllRecentSearch()
            }
        }
    }

    fun getLatestSearch() {
//        if (latestSearchJob == null) {
//            latestSearchJob = viewModelScope.launch(IO) {
//                recentSearchRepo.getLatestRecent().collect {
//                    kotlin.runCatching {
//                        ensureActive()
//                        _stateRecent.value = RecentSearchViewStates.UpdateObject(it)
//                    }
//                }
//            }
//        }
    }

    fun removeSpecificRecentSearches(query: String) {
        viewModelScope.launch(IO) {
            recentSearchRepo.deleteSpecificRecentSearch(query)
        }
    }

    fun fragmentLifeCycleDestroy() {
        kotlin.runCatching {
            latestSearchJob?.cancel()
            latestSearchJob = null
        }
    }

    fun clearSearchedFrames() {
        kotlin.runCatching {
            getFramesJob?.cancel()
            searchFramesList.clear()
            _searchFrameState.value = SearchViewStates.Idle
        }
    }

    fun getFrames(tag: String) {

        getFramesJob?.cancel()

        getFramesJob = viewModelScope.launch(IO) {
            networkCallRepo.getSearchFrames(tag).collect {

                runCatching {

                    ensureActive()

                    when (it) {
                        is Response.Loading -> {
                            _searchFrameState.value = SearchViewStates.Loading
                        }

                        is Response.Error -> {
                            _searchFrameState.value = SearchViewStates.Error("Please try again")
                        }

                        is Response.Success -> {
                            ensureActive()
                            kotlin.runCatching {
                                it.data?.search?.let {
                                    searchFramesList.addAll(it)
                                    _searchFrameState.value =
                                        SearchViewStates.UpdateListFrames(it)
                                } ?: run {
                                    _searchFrameState.value = SearchViewStates.Error("")
                                }
                            }.onFailure {
                                _searchFrameState.value = SearchViewStates.Error("")
                            }
                        }

                        else -> {}
                    }
                }.onFailure {
                    _searchFrameState.value = SearchViewStates.Error("Please try again")
                }
            }
        }
    }
}

@Keep
sealed class SearchViewStates() {
    data object Loading : SearchViewStates()
    class Error(var message: String) : SearchViewStates()
    class UpdateObject(var objectValue: String) : SearchViewStates()
    class UpdateList(var list: MutableList<String>) : SearchViewStates()
    class UpdateListFrames(var list: List<GetSearchFramesQuery.Search?>) :
        SearchViewStates()

    data object Idle : SearchViewStates()
}

@Keep
sealed class RecentSearchViewStates() {
    data object Loading : RecentSearchViewStates()
    class Error(var message: String) : RecentSearchViewStates()
    class UpdateObject(var objectValue: RecentSearchTable) : RecentSearchViewStates()
    class UpdateList(var list: List<RecentSearchTable>) : RecentSearchViewStates()
    data object Idle : RecentSearchViewStates()
}
