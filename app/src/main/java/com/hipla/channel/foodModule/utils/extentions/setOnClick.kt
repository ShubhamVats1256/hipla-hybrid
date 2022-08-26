package com.hipla.sentinelvms.sentinelKt.utils.extentions

import android.view.View

fun View.setOnCLick(intervalMillis: Long = 0, doClick: (View) -> Unit) =
    setOnClickListener(DebouncingOnClickListener(intervalMillis = intervalMillis, doClick = doClick))

