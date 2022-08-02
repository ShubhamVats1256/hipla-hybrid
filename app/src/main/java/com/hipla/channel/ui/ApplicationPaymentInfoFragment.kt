package com.hipla.channel.ui

import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.Intent
import android.graphics.Bitmap
import android.os.Bundle
import android.provider.MediaStore
import android.view.View
import androidx.appcompat.app.AlertDialog
import androidx.core.view.forEach
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.google.android.material.button.MaterialButton
import com.hipla.channel.R
import com.hipla.channel.common.LogConstant
import com.hipla.channel.databinding.DialogOtpConfirmBinding
import com.hipla.channel.databinding.DialogUploadPhotoBinding
import com.hipla.channel.databinding.FragmentApplicationPaymentInfoBinding
import com.hipla.channel.entity.*
import com.hipla.channel.extension.*
import com.hipla.channel.viewmodel.ApplicationPaymentInfoViewModel
import kotlinx.coroutines.launch
import timber.log.Timber


class ApplicationPaymentInfoFragment : Fragment(R.layout.fragment_application_payment_info) {

    private lateinit var viewModel: ApplicationPaymentInfoViewModel
    private lateinit var binding: FragmentApplicationPaymentInfoBinding
    private var uploadChequeDialog: AlertDialog? = null
    private var otpConfirmDialog: AlertDialog? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentApplicationPaymentInfoBinding.bind(view)
        viewModel = ViewModelProvider(this)[ApplicationPaymentInfoViewModel::class.java]
        viewModel.extractArguments(arguments)
        observeViewModel()
        setUI()
    }

    private fun observeViewModel() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.appEvent.collect {
                    when (it.id) {
                        OTP_VERIFYING -> {
                            requireActivity().toILoader().showLoader("Verifying OTP")
                        }
                        OTP_GENERATING -> {
                            requireActivity().toILoader().showLoader("Generating OTP")
                        }
                        OTP_GENERATE_FAILED -> {
                            requireContext().showToastLongDuration("OTP generation failed, Please try again")
                        }
                        OTP_SHOW_VERIFICATION_DIALOG -> {
                            val appEventData: AppEventWithData<*>? = it as? AppEventWithData<*>
                            showOTPDialog(appEventData?.extras.toString())
                        }
                        OTP_GENERATE_COMPLETE, OTP_VERIFICATION_COMPLETE -> {
                            requireActivity().toILoader().dismiss()
                        }
                        OTP_VERIFICATION_SUCCESS -> {
                            viewModel.updateApplicationRequest(
                                amountPayable = binding.amountPayable.content(),
                                chequeNo = binding.chequeNumber.content(),
                                paymentType = getPaymentTypeFromCheckedId()
                            )
                        }
                        OTP_VERIFICATION_INVALID -> {
                            requireContext().showToastLongDuration("Wrong OTP")
                        }
                        OTP_VERIFICATION_FAILED -> {
                            requireContext().showToastLongDuration("Unable to verify, server error")
                        }
                    }
                }
            }
        }
    }

    private fun showOTPDialog(customerUserId: String) {
        if (requireActivity().isDestroyed.not()) {
            val dialogBuilder: AlertDialog.Builder = AlertDialog.Builder(requireContext())
            val dialogBinding = DialogOtpConfirmBinding.inflate(requireActivity().layoutInflater)
            dialogBuilder.setView(dialogBinding.root)
            dialogBinding.identification.text = customerUserId
            dialogBinding.title.text = getString(R.string.verify_customer_otp)
            dialogBinding.back.setOnClickListener {
                otpConfirmDialog?.dismiss()
            }
            dialogBinding.otpEdit.onSubmit {
                otpConfirmDialog?.dismiss()
                dialogBinding.otpEdit.takeIf { it.hasValidData() }?.let {
                    Timber.tag(LogConstant.CUSTOMER_INFO).d("submitting otp ${it.content()}")
                    viewModel.verifyChannelPartnerOTP(it.content())
                    requireActivity().toILoader().showLoader(getString(R.string.verifying))
                }
            }
            otpConfirmDialog = dialogBuilder.show()
            otpConfirmDialog?.setCancelable(false)
            otpConfirmDialog?.setCanceledOnTouchOutside(false)
        }
    }

    private fun setUI() {
        binding.continueBtn.setOnClickListener {
            if (isMandatoryInfoFilled()) {
                Timber.tag(LogConstant.PAYMENT_INFO).d("payment mandatory field filled")
                viewModel.generateChannelPartnerOTP(binding.channelPartnerMobileNo.content())
            }
        }
        binding.paymentToggle.addOnButtonCheckedListener { group, checkedId, isChecked ->
            if(isChecked) {
                when(checkedId) {
                    R.id.cash -> {
                        (binding.paymentToggle.findViewById(R.id.cheque) as MaterialButton).isEnabled = true
                    }
                }
            }
        }
    }

    private fun getPaymentTypeFromCheckedId(): PaymentType {
        return when (binding.paymentToggle.checkedButtonId) {
            R.id.rtgs -> PaymentType.Rtgs()
            R.id.cheque -> PaymentType.Cheque()
            R.id.cash -> PaymentType.Cash()
            else -> PaymentType.Unknown()
        }
    }

    private fun isMandatoryInfoFilled(): Boolean {
        if (binding.channelPartnerMobileNo.hasValidData().not()) {
            binding.channelPartnerMobileNo.error = "Channel partner mobile number is mandatory";
            requireContext().showToastLongDuration("Channel partner mobile number is mandatory")
            return false
        }
        if (binding.amountPayable.hasValidData().not()) {
            binding.amountPayable.error = "Amount payable is mandatory";
            requireContext().showToastLongDuration("Amount payable is mandatory")
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