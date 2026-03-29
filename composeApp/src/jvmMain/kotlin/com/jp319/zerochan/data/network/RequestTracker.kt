package com.jp319.zerochan.data.network

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlin.time.Duration.Companion.milliseconds

object RequestTracker {
    private val _burstCount = MutableStateFlow(0)
    val burstCount = _burstCount.asStateFlow()

    init {
        // Start the "Leaky Bucket" drain
        // Removes 1 request from the burst counter every 1 second
        CoroutineScope(Dispatchers.Default).launch {
            while (true) {
                delay(1000.milliseconds)
                _burstCount.update { if (it > 0) it - 1 else 0 }
            }
        }
    }

    fun recordRequest() {
        _burstCount.update { it + 1 }
    }
}