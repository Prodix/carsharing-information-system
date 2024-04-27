package com.syndicate.carsharing.data

import android.util.Log
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import kotlinx.coroutines.delay

class Timer {

    private val _hours: MutableState<Int> = mutableIntStateOf(0)
    private val _minutes: MutableState<Int> = mutableIntStateOf(5)
    private val _seconds: MutableState<Int> = mutableIntStateOf(0)
    private var _isStarted: MutableState<Boolean> = mutableStateOf(false)

    private var _defaultHours: Int = 5
    private var _defaultMinutes: Int = 5
    private var _defaultSeconds: Int = 0

    var onTimerEnd: (() -> Unit)? = null

    var isStarted: Boolean
        get() = _isStarted.value
        set(value) {
            _isStarted.value = value
        }

    val defaultMinutes: Int
        get() = _defaultMinutes

    val defaultSeconds: Int
        get() = _defaultSeconds

    val defaultHours: Int
        get() = _defaultHours

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

    var hours: Int
        get() = _hours.value
        set(value) {
            _hours.value = value
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

    constructor (hours: Int, minutes: Int, seconds: Int) {
        _defaultMinutes = minutes
        _defaultHours = hours
        _defaultSeconds = seconds
        _minutes.value = minutes
        _hours.value = hours
        _seconds.value = seconds
    }

    fun restart() {
        _minutes.value = _defaultMinutes
        _seconds.value = _defaultSeconds
        _hours.value = _defaultHours
    }

    fun changeStartTime(minutes: Int, seconds: Int) {
        _defaultMinutes = minutes
        _defaultSeconds = seconds
        _defaultHours = 0
        restart()
    }

    fun changeStartTime(hours: Int, minutes: Int, seconds: Int) {
        _defaultMinutes = minutes
        _defaultSeconds = seconds
        _defaultHours = hours
        restart()
    }

    suspend fun start() {

        _isStarted.value = true

        while (_isStarted.value) {
            if (_seconds.value == 0 && _minutes.value != 0) {
                _minutes.value -= 1
                _seconds.value = 59
            } else if (_seconds.value == 0 && _minutes.value == 0) {
                if (_hours.value != 0) {
                    _hours.value--
                    _minutes.value = 59
                    _seconds.value = 59
                } else {
                    _isStarted.value = false
                    onTimerEnd?.let { it() }
                }
            } else {
                _seconds.value -= 1
            }
            delay(1000L)
            Log.d("timer", this.toString())
        }
    }

    fun stop() {
        _isStarted.value = false
    }

    override fun toString(): String {
        return "${(if (_hours.value != 0) (if (_hours.value < 10) "0${_hours.value}:" else "${_hours.value}:") else "")}${(if (_minutes.value < 10) 0 else "")}${_minutes.value}:${(if (_seconds.value < 10) 0 else "")}${_seconds.value}"
    }
}