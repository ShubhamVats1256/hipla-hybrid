package com.hipla.channel.common

import android.view.View
import com.hipla.channel.common.LogConstant.APP_EXCEPTION
import com.hipla.channel.entity.SalesUser
import timber.log.Timber

object Utils {

    inline fun tryCatch(block: () -> Unit) {
        try {
            block.invoke()
        } catch (e: Exception) {
            Timber.tag(APP_EXCEPTION).e(e.toString())
        }
    }

    fun View.show() {
        visibility = View.VISIBLE
    }

    fun View.hide() {
        visibility = View.GONE
    }

    fun View.invisible() {
        visibility = View.INVISIBLE
    }

}
