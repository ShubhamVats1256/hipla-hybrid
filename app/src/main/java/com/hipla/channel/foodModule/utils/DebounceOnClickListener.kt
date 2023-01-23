package com.hipla.channel.foodModule.utils

import android.view.View

class DebounceOnClickListener(
    private val interval: Long,
    private val listenerBlock: (View) -> Unit
): View.OnClickListener {
    private var lastClickTime = 0L

    override fun onClick(v: View) {
        val time = System.currentTimeMillis()
        if (time - lastClickTime >= interval) {
            lastClickTime = time
            listenerBlock(v)
        }
    }
}

fun View.setOnClickListener(debounceInterval: Long, listenerBlock: (View) -> Unit) =
    setOnClickListener(DebounceOnClickListener(debounceInterval, listenerBlock))