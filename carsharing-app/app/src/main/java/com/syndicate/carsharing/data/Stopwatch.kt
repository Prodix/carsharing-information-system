package com.syndicate.carsharing.data

import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import kotlinx.coroutines.delay

class Stopwatch {

    private var _seconds: MutableState<Int> = mutableIntStateOf(0)
    private var _hours: MutableState<Int> = mutableIntStateOf(0)
    private var _minutes: MutableState<Int> = mutableIntStateOf(0)
    private var _isStarted: MutableState<Boolean> = mutableStateOf(false)

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

    var hours: Int
        get() = _hours.value
        set(value) {
            _hours.value = value
        }

    var isStarted: Boolean
        get() = _isStarted.value
        set(value) {
            _isStarted.value = value
        }

    fun stop() {
        _isStarted.value = false
    }

    suspend fun restart() {
        stop()
        clear()
        start()
    }

    fun clear() {
        _seconds.value = 0;
        _minutes.value = 0;
        _hours.value = 0;
    }

    suspend fun start() {
        _isStarted.value = true
        while (_isStarted.value) {
            _seconds.value++
            if (_seconds.value == 60) {
                _seconds.value = 0
                _minutes.value++
                if (_minutes.value == 60) {
                    _minutes.value = 0

                }
            }
            delay(1000L)
        }
    }

    override fun toString(): String {
        return "${(if (_hours.value != 0) "${(if (_hours.value < 10) 0 else "")}${_hours.value}:" else "")}${(if (_minutes.value < 10) 0 else "")}${_minutes.value}:${(if (_seconds.value < 10) 0 else "")}${_seconds.value}"
    }

    operator fun plus(stopwatch: Stopwatch): Any {
        val resultStopwatch = Stopwatch()
        resultStopwatch.minutes = this.minutes + stopwatch.minutes
        resultStopwatch.seconds = this.seconds + stopwatch.seconds
        resultStopwatch.hours = this.hours + stopwatch.hours

        if (resultStopwatch.seconds >= 60) {
            resultStopwatch.minutes += resultStopwatch.seconds / 60
            resultStopwatch.seconds %= 60
        }

        if (resultStopwatch.minutes >= 60) {
            resultStopwatch.hours += resultStopwatch.minutes / 60
            resultStopwatch.minutes %= 60
        }

        return resultStopwatch
    }
}