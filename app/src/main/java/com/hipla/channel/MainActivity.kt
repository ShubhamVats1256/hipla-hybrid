package com.hipla.channel

import android.content.Context
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.inputmethod.InputMethodManager
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import com.hipla.channel.common.LogConstant
import com.hipla.channel.common.Utils.hide
import com.hipla.channel.common.Utils.show
import com.hipla.channel.common.Utils.tryCatch
import com.hipla.channel.contract.IActivityHelper
import com.hipla.channel.databinding.ActivityMainBinding
import com.hipla.channel.databinding.DialogLoaderBinding
import com.hipla.channel.extension.showToastErrorMessage
import timber.log.Timber

class MainActivity : AppCompatActivity(), IActivityHelper {

    private var loaderDialog: AlertDialog? = null
    private var dialogLoaderBinding: DialogLoaderBinding? = null
    private lateinit var inputMethodManager: InputMethodManager
    private val navController: NavController by lazy {
        (supportFragmentManager.findFragmentById(R.id.navHost) as NavHostFragment).navController
    }
    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        inputMethodManager = getSystemService(
            Context.INPUT_METHOD_SERVICE
        ) as InputMethodManager
        setupNavigation()
    }

    private fun setupNavigation() {
        navController.addOnDestinationChangedListener { _, destination, _ ->
            when (destination.id) {
                R.id.homeFragment -> {
                    binding.flowTitle.hide()
                }
                else -> {
                    binding.flowTitle.show()
                }
            }
        }
    }

    override fun showLoader(message: String) {
        tryCatch {
            if (isDestroyed.not() && loaderDialog?.isShowing != true) {
                var dialogBuilder: AlertDialog.Builder? = null
                if (dialogLoaderBinding == null) {
                    dialogBuilder = AlertDialog.Builder(this)
                    dialogLoaderBinding = DialogLoaderBinding.inflate(layoutInflater)
                    dialogBuilder.setView(dialogLoaderBinding?.root)
                }
                dialogLoaderBinding?.message?.text = message
                loaderDialog?.dismiss()
                loaderDialog = dialogBuilder?.show()
                loaderDialog?.setCancelable(false)
                loaderDialog?.setCanceledOnTouchOutside(false)
            }
        }
    }

    override fun dismiss() {
        loaderDialog?.dismiss()
    }

    override fun hideKeyboard() {
        tryCatch {
            inputMethodManager
                .hideSoftInputFromWindow(
                    this.currentFocus?.windowToken, 0
                )
        }
    }

    override fun onResume() {
        super.onResume()
        checkCameraPermission()
    }
/*
    fun getListOfPermissionToBeRequested() {
        val permissionRequestList = arrayListOf<String>().apply {
            add(   android.Manifest.permission.CAMERA)
            add(   android.Manifest.permission.WRITE_EXTERNAL_STORAGE)
        }
        if (ContextCompat.checkSelfPermission(
                this,

            ) != PackageManager.PERMISSION_GRANTED) {
            permissionRequestList.add(permissionRequestList)
        }
    }*/

    private fun checkCameraPermission() {
        if (ContextCompat.checkSelfPermission(
                this,
                android.Manifest.permission.CAMERA
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
                    showToastErrorMessage("Camera permission is mandatory")
                    finish()
                }
                return
            }
        }
    }

    override fun onDestroy() {
        loaderDialog?.dismiss()
        super.onDestroy()
    }

    companion object {
        const val PERMISSION_REQUEST_CODE = 555

    }


}
