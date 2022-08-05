package com.hipla.channel.widget

import android.app.Activity
import androidx.appcompat.app.AlertDialog
import com.hipla.channel.databinding.DialogOtpConfirmBinding
import java.lang.ref.WeakReference

class OTPDialog(
    private val userId: String,
    private val dialogTitle: String,
    private val activityReference: WeakReference<Activity>,
    private val onSubmitListener: OnOTPSubmitListener
) {

    private var otpConfirmBinding: DialogOtpConfirmBinding = DialogOtpConfirmBinding.inflate(activityReference.get()!!.layoutInflater)
    private val otpStringBuilder = StringBuilder()
    private var otpConfirmDialog: AlertDialog? = null

    init {
        otpConfirmBinding.keySubmit.setOnClickListener {
            if (otpStringBuilder.toString().isNotEmpty()) {
                otpConfirmDialog?.dismiss()
                onSubmitListener.onSubmit(otpStringBuilder.toString())
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

        otpConfirmBinding.back.setOnClickListener {
            dismiss()
        }

    }

    private fun appendText(text: String) {
        otpStringBuilder.append(text)
        updateDisplay()
    }

    private fun updateDisplay() {
        otpConfirmBinding.otpDisplay.text = otpStringBuilder.toString()
    }

    fun show(): OTPDialog? {
        if (otpConfirmDialog?.isShowing != true && activityReference.get()?.isDestroyed?.not() == true) {
            val dialogBuilder: AlertDialog.Builder = AlertDialog.Builder(activityReference.get()!!)
                  dialogBuilder.setView(otpConfirmBinding.root)
            otpConfirmBinding.identification.text = userId
            otpConfirmBinding.back.setOnClickListener {
                otpConfirmDialog?.dismiss()
            }
            otpConfirmBinding.title.text = dialogTitle
            otpConfirmDialog = dialogBuilder.show()
            otpConfirmDialog?.setCancelable(false)
            otpConfirmDialog?.setCanceledOnTouchOutside(false)
            return this
        }
        return null
    }

    fun isShowing() = otpConfirmDialog?.isShowing

    private fun dismiss() {
        otpConfirmDialog?.dismiss()
    }

    interface OnOTPSubmitListener {
        fun onSubmit(otp: String)
    }

}

