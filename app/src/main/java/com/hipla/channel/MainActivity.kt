package com.hipla.channel

import android.content.pm.PackageManager
import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.hipla.channel.common.LogConstant
import com.hipla.channel.contract.ILoader
import com.hipla.channel.databinding.DialogLoaderBinding
import com.hipla.channel.extension.showToastLongDuration
import timber.log.Timber

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

    override fun onResume() {
        super.onResume()
        checkPermission()
    }

    fun checkPermission() {
        if (ContextCompat.checkSelfPermission(
                this,
                "android.permission.CAMERA"
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            Timber.tag(LogConstant.HIPLA).d("camera permission already granted")
        } else {
            requestPermissions(
                arrayOf("android.permission.CAMERA"),
                PERMISSION_REQUEST_CODE
            )
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            PERMISSION_REQUEST_CODE -> {
                if ((grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                    Timber.tag(LogConstant.HIPLA).d("camera permission granted by user")
                } else {
                    Timber.tag(LogConstant.HIPLA).d("camera permission denied by user")
                    showToastLongDuration("Camera permission is mandatory")
                    finish()
                }
                return
            }
        }
    }

    companion object {
        const val PERMISSION_REQUEST_CODE = 555
    }


}
