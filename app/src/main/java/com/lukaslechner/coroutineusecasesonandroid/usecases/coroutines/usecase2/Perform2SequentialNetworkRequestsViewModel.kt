package com.lukaslechner.coroutineusecasesonandroid.usecases.coroutines.usecase2

import androidx.lifecycle.viewModelScope
import com.lukaslechner.coroutineusecasesonandroid.base.BaseViewModel
import com.lukaslechner.coroutineusecasesonandroid.mock.MockApi
import kotlinx.coroutines.launch
import timber.log.Timber

class Perform2SequentialNetworkRequestsViewModel(
    private val mockApi: MockApi = mockApi()
) : BaseViewModel<UiState>() {

    fun perform2SequentialNetworkRequest() {
        // TODO: Exercise 1
        // switch to branch "coroutine_course_full" to see solution
        uiState.value = UiState.Loading
        viewModelScope.launch {
            try {
                val recentAndroidVersions = mockApi.getRecentAndroidVersions()
                var apiLevel = -1
                recentAndroidVersions.forEach { if (it.apiLevel > apiLevel) apiLevel = it.apiLevel }
                println("DUNDON $apiLevel")
                val versionFeatures = mockApi.getAndroidVersionFeatures(apiLevel)
                uiState.value = UiState.Success(versionFeatures)
            } catch (exception: Exception) {
                Timber.e(exception)
                uiState.value = UiState.Error("Network Request failed!")
            }
        }
    }
}