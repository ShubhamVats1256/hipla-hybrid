package com.hipla.sentinelvms.sentinelKt.utils.extentions

import android.content.Context
import android.widget.Toast

fun Context.toast(content: String) = Toast.makeText(this, content, Toast.LENGTH_SHORT).show()

