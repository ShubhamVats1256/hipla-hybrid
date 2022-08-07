package com.hipla.channel.common.image

import android.graphics.drawable.Drawable
import android.widget.ImageView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.bumptech.glide.load.resource.bitmap.FitCenter
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.RequestOptions
import com.bumptech.glide.request.target.Target
import com.hipla.channel.R
import com.hipla.channel.common.Utils.hide
import com.hipla.channel.common.Utils.show
import com.hipla.channel.common.Utils.tryCatch

fun ImageView.setRoundedImageWithDefaultCornerRadius(
    url: String?,
) {
    setRoundedImage(
        url,
        resources.getDimension(R.dimen.rounded_image_default_corner_radius).toInt()
    )
}

fun ImageView.setRoundedImage(
    url: String?,
    cornerRadius: Int,
    placeHolderRes: Int? = null,
) {
    if (url.isNullOrEmpty()) {
        return
    }
    tryCatch {
        val glideReq = Glide.with(this)
            .load(url)
            .listener(GlideLogging())

        if (placeHolderRes != null) {
            glideReq.placeholder(ContextCompat.getDrawable(context, placeHolderRes))
        }

        glideReq.apply {
            apply(RequestOptions().apply {
                transform(CenterCrop(), RoundedCorners(cornerRadius))
            }).into(this@setRoundedImage)
        }
    }
}

fun ImageView.loadImageBasedOnOrientation(
    url: String,
    radius: Int,
    thumbnailSize: Int,
) {
    if (url.isEmpty()) return
    tryCatch {
        Glide.with(this.context)
            .load(url)
            .listener(object : RequestListener<Drawable> {
                override fun onLoadFailed(
                    e: GlideException?,
                    model: Any?,
                    target: Target<Drawable>?,
                    isFirstResource: Boolean,
                ): Boolean {
                    this@loadImageBasedOnOrientation.hide()
                    return false
                }

                override fun onResourceReady(
                    resource: Drawable?,
                    model: Any?,
                    target: Target<Drawable>?,
                    dataSource: DataSource?,
                    isFirstResource: Boolean,
                ): Boolean {
                    resource?.let {
                        val orientationSize: Int = (thumbnailSize * 1.3).toInt()
                        val params: ConstraintLayout.LayoutParams =
                            this@loadImageBasedOnOrientation.layoutParams as ConstraintLayout.LayoutParams
                        when {
                            // landscape image
                            it.intrinsicWidth > it.intrinsicHeight -> {
                                params.width = orientationSize
                                params.height = thumbnailSize
                            }
                            // square image
                            it.intrinsicWidth == it.intrinsicHeight -> {
                                params.width = thumbnailSize
                                params.height = thumbnailSize
                            }
                            // portrait image
                            else -> {
                                params.width = thumbnailSize
                                params.height = orientationSize
                            }
                        }
                        with(this@loadImageBasedOnOrientation) {
                            post {
                                this@loadImageBasedOnOrientation.show()
                                layoutParams = params
                            }
                        }
                    }
                    return false
                }
            })
            .apply(RequestOptions().apply {
                placeholder(R.drawable.rounded_image_placeholder)
                transform(FitCenter(), RoundedCorners(radius))
            })
            .into(this)
    }
}

fun ImageView.setCircularImage(url: String?, placeHolderRes: Int? = null) {
    if (url.isNullOrEmpty()) {
        return
    }
    tryCatch {
        val placeholderDrawable = placeHolderRes ?: R.drawable.circular_image_placeholder
        Glide.with(this)
            .load(url)
            .placeholder(ContextCompat.getDrawable(context, placeholderDrawable))
            .listener(GlideLogging())
            .circleCrop()
            .into(this)
    }
}
