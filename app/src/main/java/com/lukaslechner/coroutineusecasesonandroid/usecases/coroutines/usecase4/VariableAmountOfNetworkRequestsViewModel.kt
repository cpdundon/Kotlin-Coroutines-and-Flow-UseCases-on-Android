package com.lukaslechner.coroutineusecasesonandroid.usecases.coroutines.usecase4

import androidx.lifecycle.viewModelScope
import com.lukaslechner.coroutineusecasesonandroid.base.BaseViewModel
import com.lukaslechner.coroutineusecasesonandroid.mock.AndroidVersion
import com.lukaslechner.coroutineusecasesonandroid.mock.MockApi
import com.lukaslechner.coroutineusecasesonandroid.mock.VersionFeatures
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.launch

class VariableAmountOfNetworkRequestsViewModel(
    private val mockApi: MockApi = mockApi()
) : BaseViewModel<UiState>() {

    fun performNetworkRequestsSequentially() {
        uiState.value = UiState.Loading
        viewModelScope.launch {
            try {
                val recentVersions = mockApi.getRecentAndroidVersions()
                val versionFeatures = recentVersions.map { androidVersion ->
                    mockApi.getAndroidVersionFeatures(androidVersion.apiLevel)
                }
                uiState.value = UiState.Success(versionFeatures)
            } catch (exception: Exception) {
                uiState.value = UiState.Error("Network Request failed")
            }
        }
    }

    fun performNetworkRequestsConcurrently() {
        // Exercise 2
        uiState.value = UiState.Loading
        var recentVersions = listOf<AndroidVersion>()
        val jobGetVersions = viewModelScope.launch {
            try {
                recentVersions = mockApi.getRecentAndroidVersions()
            } catch (e: Exception) {
                uiState.value = UiState.Error("Network Request failed")
            }
        }

        viewModelScope.launch {
            try {
                jobGetVersions.join()

                val versionFeatureDeferredList = recentVersions.map { v ->
                    viewModelScope.async { mockApi.getAndroidVersionFeatures(v.apiLevel) }
                }

                val versionFeatureList = versionFeatureDeferredList.awaitAll()
                uiState.value = UiState.Success(versionFeatureList)
            } catch (e: Exception) {
                uiState.value = UiState.Error("Network Request failed")
            }
        }
    }

}