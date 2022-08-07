package com.hipla.channel.common.image
import android.util.Log
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.hipla.channel.common.LogConstant

class GlideLogging<R> : RequestListener<R> {
    override fun onLoadFailed(
        exception: GlideException?,
        model: Any?,
        target: com.bumptech.glide.request.target.Target<R>,
        isFirstResource: Boolean
    ): Boolean {
        if (exception != null) {
            Log.e(LogConstant.LOG_GLIDE, exception.message ?: "glide image load error")
        }
        return false
    }

    override fun onResourceReady(
        resource: R,
        model: Any?,
        target: com.bumptech.glide.request.target.Target<R>?,
        dataSource: DataSource?,
        isFirstResource: Boolean
    ): Boolean {
        return false
    }
}