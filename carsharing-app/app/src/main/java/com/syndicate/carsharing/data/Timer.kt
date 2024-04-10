package com.syndicate.carsharing.data

import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableIntStateOf
import kotlinx.coroutines.delay

class Timer {

    private val _minutes: MutableState<Int> = mutableIntStateOf(5)
    private val _seconds: MutableState<Int> = mutableIntStateOf(0)
    private var _isStarted: Boolean = false

    private var _defaultMinutes: Int = 5
    private var _defaultSeconds: Int = 0

    var onTimerEnd: () -> Unit = { }

    var minutes: Int
        get() = _minutes.value
        set(value) {
            _minutes.value = value
        }

    var seconds: Int
        get() = _seconds.value
        set(value) {
            _seconds.value = value
        }

    constructor () {
        _minutes.value = 5
        _seconds.value = 0
    }

    constructor (minutes: Int, seconds: Int) {
        _defaultMinutes = minutes
        _defaultSeconds = seconds
        _minutes.value = minutes
        _seconds.value = seconds
    }

    fun restart() {
        _minutes.value = _defaultMinutes
        _seconds.value = _defaultSeconds
    }

    fun changeStartTime(minutes: Int, seconds: Int) {
        _defaultMinutes = minutes
        _defaultSeconds = seconds
    }

    suspend fun start() {

        _isStarted = true

        while (_isStarted) {
            if (_seconds.value == 0 && _minutes.value != 0) {
                _minutes.value -= 1
                _seconds.value = 59
            } else if (_seconds.value == 0 && _minutes.value == 0) {
                _isStarted = false
                onTimerEnd()
            } else {
                _seconds.value -= 1
            }
            delay(1000L)
        }
    }

    fun stop() {
        _isStarted = false
    }

    override fun toString(): String {
        return "${_minutes.value}:${if (_seconds.value.toString().length == 1) "0${_seconds.value}" else _seconds.value}"
    }

}