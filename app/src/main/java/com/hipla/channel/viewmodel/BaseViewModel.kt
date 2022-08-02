package com.hipla.channel.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hipla.channel.entity.AppEvent
import com.hipla.channel.extension.launchSafely
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.launch
import timber.log.Timber

abstract class BaseViewModel : ViewModel() {

    val appEvent = MutableSharedFlow<AppEvent>(replay = 0, extraBufferCapacity = Int.MAX_VALUE, BufferOverflow.SUSPEND)

    fun launchIO(block: suspend () -> Unit): Job {
        return viewModelScope.launch(Dispatchers.IO) {
            try {
                block.invoke()
            } catch (e: CancellationException) {
                Timber.e(e)
            }
        }
    }

    fun launchOnMain(block: suspend () -> Unit): Job {
        return viewModelScope.launchSafely {
            try {
                block.invoke()
            } catch (e: CancellationException) {
                Timber.e(e)
            }
        }
    }

}