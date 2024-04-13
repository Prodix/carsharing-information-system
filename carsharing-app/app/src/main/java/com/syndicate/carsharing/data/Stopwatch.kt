package com.syndicate.carsharing.data

import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import kotlinx.coroutines.delay

class Stopwatch {

    private var _seconds: MutableState<Int> = mutableIntStateOf(0)
    private var _minutes: MutableState<Int> = mutableIntStateOf(0)
    private var _isStarted: Boolean = false

    var seconds: Int
        get() = _seconds.value
        set(value) {
            _seconds.value = value
        }

    var minutes: Int
        get() = _minutes.value
        set(value) {
            _minutes.value = value
        }

    val isStarted: Boolean
        get() = _isStarted

    fun stop() {
        _isStarted = false
    }

    suspend fun restart() {
        stop()
        _seconds.value = 0;
        _minutes.value = 0;
        start()
    }

    suspend fun start() {
        _isStarted = true
        while (_isStarted) {
            _seconds.value++
            if (_seconds.value == 60) {
                _seconds.value = 0
                _minutes.value++
            }
            delay(1000L)
        }
        _seconds.value = 0
        _minutes.value = 0
    }

    override fun toString(): String {
        return "${(if (_minutes.value < 10) 0 else "")}${_minutes.value}:${(if (_seconds.value < 10) 0 else "")}${_seconds.value}"
    }
}