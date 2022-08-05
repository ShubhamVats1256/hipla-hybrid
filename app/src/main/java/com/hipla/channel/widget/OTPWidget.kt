package com.hipla.channel.widget

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.databinding.DataBindingUtil
import com.hipla.channel.R
import com.hipla.channel.databinding.DialogOtpConfirmBinding

class OTPWidget : ConstraintLayout {
    private var otpConfirmBinding: DialogOtpConfirmBinding
    private val otpStringBuilder = StringBuilder()
    private var onSubmitListener: OnOTPSubmitListener? = null

    constructor(context: Context) : super(context)
    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs)
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    )

    init {
        val inflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        otpConfirmBinding =
            DataBindingUtil.inflate(inflater, R.layout.dialog_otp_confirm, this, true)

        otpConfirmBinding.submit.setOnClickListener {
            if (otpStringBuilder.toString().isNotEmpty()) {
                onSubmitListener?.onSubmit(otpStringBuilder.toString())
            }
        }

        otpConfirmBinding.keyClear.setOnClickListener {
            if (otpStringBuilder.toString().isNotEmpty()) {
                otpStringBuilder.deleteCharAt(otpStringBuilder.length - 1)
                updateDisplay()
            }
        }

        otpConfirmBinding.key0.setOnClickListener {
            appendText("0")
        }
        otpConfirmBinding.key1.setOnClickListener {
            appendText("1")
        }
        otpConfirmBinding.key2.setOnClickListener {
            appendText("2")
        }
        otpConfirmBinding.key3.setOnClickListener {
            appendText("3")
        }
        otpConfirmBinding.key4.setOnClickListener {
            appendText("4")
        }
        otpConfirmBinding.key5.setOnClickListener {
            appendText("5")
        }
        otpConfirmBinding.key6.setOnClickListener {
            appendText("6")
        }
        otpConfirmBinding.key7.setOnClickListener {
            appendText("7")
        }
        otpConfirmBinding.key8.setOnClickListener {
            appendText("8")
        }
        otpConfirmBinding.key9.setOnClickListener {
            appendText("9")
        }
    }

    fun setOnSubmitListener(onSubmitListener: OnOTPSubmitListener) {
        this.onSubmitListener = onSubmitListener
    }

    fun setTitle(title: String) {
        otpConfirmBinding.title.text = title
    }

    private fun appendText(text: String) {
        otpStringBuilder.append(text)
        updateDisplay()
    }

    private fun updateDisplay() {
        otpConfirmBinding.otpEdit.setText(otpStringBuilder.toString())
    }

    interface OnOTPSubmitListener {
        fun onSubmit(otp: String)
    }

}