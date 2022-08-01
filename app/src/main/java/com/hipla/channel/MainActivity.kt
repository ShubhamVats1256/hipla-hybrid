package com.hipla.channel

import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.hipla.channel.contract.ILoader
import com.hipla.channel.databinding.DialogLoaderBinding

class MainActivity : AppCompatActivity(), ILoader {

    private var loaderDialog: AlertDialog? = null
    private var dialogLoaderBinding: DialogLoaderBinding? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
    }

    override fun showLoader(message: String) {
        if (isDestroyed.not()) {
            var dialogBuilder: AlertDialog.Builder? = null
            if (loaderDialog == null) {
                dialogBuilder = AlertDialog.Builder(this)
                dialogLoaderBinding = DialogLoaderBinding.inflate(layoutInflater)
                dialogBuilder.setView(dialogLoaderBinding?.root)
            }
            dialogLoaderBinding?.message?.text = message
            loaderDialog = dialogBuilder?.show()
        }
    }

    override fun dismiss() {
        loaderDialog?.dismiss()
    }


}