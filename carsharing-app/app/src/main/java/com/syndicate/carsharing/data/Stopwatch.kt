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

    private var _timeToStop: Triple<Int, Int, Int> = Triple(-1,-1,-1)

    var timeToStop: Triple<Int, Int, Int>
        get() = _timeToStop
        set(value) {
            _timeToStop = value
        }

    var seconds: Int
        get() = _seconds.value
        set(value) {
            _seconds.value = value
        }

    var minutes: Int
        get() = _minutes.value
        set(value) {
            if (value >= 60) {
                _hours.value = value / 60
                _minutes.value = value % 60
            } else {
                _minutes.value = value
            }
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

    var action: () -> Unit = { }

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
                    _hours.value++
                }
            }
            if (Triple(_hours.value, _minutes.value, _seconds.value) == timeToStop) {
                action()
            }
            if (_isStarted.value)
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