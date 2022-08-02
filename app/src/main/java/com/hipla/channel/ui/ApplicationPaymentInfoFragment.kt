package com.hipla.channel.ui

import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.Intent
import android.graphics.Bitmap
import android.os.Bundle
import android.provider.MediaStore
import android.view.View
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.hipla.channel.R
import com.hipla.channel.common.LogConstant
import com.hipla.channel.databinding.DialogUploadPhotoBinding
import com.hipla.channel.databinding.FragmentApplicationPaymentInfoBinding
import com.hipla.channel.extension.hasValidData
import com.hipla.channel.extension.showToastLongDuration
import com.hipla.channel.viewmodel.ApplicationFlowViewModel
import com.hipla.channel.viewmodel.ApplicationPaymentInfoViewModel
import timber.log.Timber


class ApplicationPaymentInfoFragment : Fragment(R.layout.fragment_application_payment_info) {

    private lateinit var viewModel: ApplicationPaymentInfoViewModel
    private lateinit var binding: FragmentApplicationPaymentInfoBinding
    private var uploadChequeDialog: AlertDialog? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentApplicationPaymentInfoBinding.bind(view)
        viewModel = ViewModelProvider(this)[ApplicationPaymentInfoViewModel::class.java]
        viewModel.extractArguments(arguments)
    }

    private fun isMandatoryInfoFilled(): Boolean {
        if (binding.channelPartnerMobileNo.hasValidData().not()) {
            binding.channelPartnerMobileNo.error = "Channel partner mobile number is mandatory";
            requireContext().showToastLongDuration("Channel partner mobile number is mandatory")
            return false
        }
        return true
    }

    private fun showUploadChequeDialog(bitmap: Bitmap) {
        if (requireActivity().isDestroyed.not()) {
            val dialogBuilder: AlertDialog.Builder = AlertDialog.Builder(requireContext())
            val dialogBinding = DialogUploadPhotoBinding.inflate(requireActivity().layoutInflater)
            dialogBuilder.setView(dialogBinding.root)
            dialogBinding.chequePhoto.setImageBitmap(bitmap)
            dialogBinding.upload.setOnClickListener {
            }
            dialogBinding.close.setOnClickListener {
                uploadChequeDialog?.dismiss()
            }
            uploadChequeDialog = dialogBuilder.show()
            uploadChequeDialog?.setCancelable(false)
            uploadChequeDialog?.setCanceledOnTouchOutside(false)
        }
    }

    private fun takePicture() {
        Timber.tag(LogConstant.PAYMENT_INFO).d("capture image")
        val takePictureIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        try {
            startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE)
        } catch (e: ActivityNotFoundException) {
            requireContext().showToastLongDuration("This device does not have camera application to proceed")
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        Timber.tag(LogConstant.PAYMENT_INFO).d("on activity result")
        if (requestCode == REQUEST_IMAGE_CAPTURE) {
            if (resultCode == Activity.RESULT_OK) {
                Timber.tag(LogConstant.PAYMENT_INFO).d("picture taken successfully")
                val bitmap = data?.extras?.get("data") as Bitmap
                showUploadChequeDialog(bitmap)
            } else {
                requireContext().showToastLongDuration("Photo capture cancelled")
            }
        }
    }

    companion object {
        const val REQUEST_IMAGE_CAPTURE = 1000
    }


}