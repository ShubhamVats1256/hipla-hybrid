package com.hipla.channel.widget


import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.databinding.DataBindingUtil
import com.hipla.channel.R
import com.hipla.channel.common.Utils.invisible
import com.hipla.channel.common.image.setCircularImage
import com.hipla.channel.databinding.ChannelPartnerDetailsBinding
import com.hipla.channel.entity.response.UserDetails


class ChannelPartnerWidget : ConstraintLayout {
    private val binding: ChannelPartnerDetailsBinding = DataBindingUtil.inflate(
        LayoutInflater.from(context),
        R.layout.channel_partner_details,
        this,
        true
    )

    constructor(context: Context) : super(context)

    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs)

    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    )

    fun setChannelPartnerDetails(userInfo: UserDetails) {
        userInfo.name?.let {
            binding.channelPartnerName.text = it
        } ?: binding.channelPartnerName.invisible()

        userInfo.profilePic?.let {
            binding.profilePic.setCircularImage(it)
        }
        userInfo.emailId?.let {
            binding.email.text = it
        } ?: binding.email.invisible()

        userInfo.phoneNumber?.let {
            binding.mobileNo.text = it
        } ?: binding.mobileNo.invisible()
    }
}

