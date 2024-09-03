package com.lukaslechner.coroutineusecasesonandroid.usecases.coroutines.usecase7

import androidx.lifecycle.viewModelScope
import com.lukaslechner.coroutineusecasesonandroid.base.BaseViewModel
import com.lukaslechner.coroutineusecasesonandroid.mock.MockApi
import com.lukaslechner.coroutineusecasesonandroid.mock.VersionFeatures
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeout
import timber.log.Timber

class TimeoutAndRetryViewModel(
    private val api: MockApi = mockApi()
) : BaseViewModel<UiState>() {

    fun performNetworkRequest() {
        uiState.value = UiState.Loading
        val numberOfRetries = 2
        val timeout = 1000L

        // TODO: Exercise 3
        // switch to branch "coroutine_course_full" to see solution

        // run api.getAndroidVersionFeatures(27) and api.getAndroidVersionFeatures(28) in parallel

        viewModelScope.launch {
            try {
                val startTime = System.currentTimeMillis()
                val featuresOf27 = retry(numberOfRetries, timeout) { async { api.getAndroidVersionFeatures(27) } }
                val featuresOf28 = retry(numberOfRetries, timeout) { async { api.getAndroidVersionFeatures(28) } }
                val featureList = listOf(featuresOf27, featuresOf28).awaitAll()
                uiState.value = UiState.Success(featureList)
                println("DUNDON: Time diff -> ${System.currentTimeMillis() - startTime}")
            } catch (e: Exception) {
                Timber.e(e)
                uiState.value = UiState.Error("Network Request failed")
            }
        }
    }

    private suspend fun <T> retry(
        times: Int,
        timeout: Long,
        initialDelayMillis: Long = 100,
        maxDelayMillis: Long = 1000,
        factor: Int = 2,
        block: suspend () -> T
    ): T {
        var currentDelay = initialDelayMillis
        repeat(times) {
            try {
                return withTimeout(timeout) { block() }
            } catch (tce: TimeoutCancellationException) {
                Timber.e(tce)
            } catch (exception: Exception) {
                Timber.e(exception)
            }
            delay(currentDelay)
            currentDelay = (currentDelay * factor).coerceAtMost(maxDelayMillis)
        }
        return withTimeout(timeout) { block() } // last attempt
    }
}