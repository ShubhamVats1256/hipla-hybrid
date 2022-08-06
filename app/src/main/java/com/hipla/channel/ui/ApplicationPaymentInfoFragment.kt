package com.hipla.channel.ui

import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.view.View
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import com.google.android.material.datepicker.MaterialDatePicker
import com.hipla.channel.R
import com.hipla.channel.common.KEY_APP_REQ
import com.hipla.channel.common.KEY_PARTNER
import com.hipla.channel.common.LogConstant
import com.hipla.channel.common.Utils.show
import com.hipla.channel.common.image.setRoundedImageWithDefaultCornerRadius
import com.hipla.channel.databinding.DialogUploadPhotoBinding
import com.hipla.channel.databinding.FragmentApplicationPaymentInfoBinding
import com.hipla.channel.entity.*
import com.hipla.channel.extension.*
import com.hipla.channel.viewmodel.ApplicationPaymentInfoViewModel
import com.hipla.channel.widget.OTPDialog
import timber.log.Timber
import java.lang.ref.WeakReference
import java.text.SimpleDateFormat
import java.util.*

class ApplicationPaymentInfoFragment : Fragment(R.layout.fragment_application_payment_info) {

    private lateinit var viewModel: ApplicationPaymentInfoViewModel
    private lateinit var binding: FragmentApplicationPaymentInfoBinding
    private var uploadChequeDialog: AlertDialog? = null
    private var otpConfirmDialog: OTPDialog? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentApplicationPaymentInfoBinding.bind(view)
        viewModel = ViewModelProvider(this)[ApplicationPaymentInfoViewModel::class.java]
        viewModel.extractArguments(arguments)
        observeViewModel()
        setUI()
    }

    private fun observeViewModel() {
        viewLifecycleOwner.lifecycleScope.launchSafely {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                launchSafely {
                    viewModel.appEvent.collect {
                        when (it.id) {
                            OTP_VERIFYING -> {
                                requireActivity().IActivityHelper().showLoader("Verifying OTP")
                            }
                            OTP_GENERATING -> {
                                requireActivity().IActivityHelper().showLoader("Generating OTP")
                            }
                            IMAGE_UPLOADING -> {
                                requireActivity().IActivityHelper().showLoader("Uploading proof...")
                            }
                            IMAGE_UPLOADED_FAILED -> {
                                requireContext().showToastErrorMessage("Proof upload failed")
                                requireActivity().IActivityHelper().dismiss()
                            }
                            IMAGE_UPLOADED_SUCCESSFULLY -> {
                                requireActivity().IActivityHelper().dismiss()
                                requireContext().showToastSuccessMessage("Proof uploaded successfully")
                                setPaymentProof((it as AppEventWithData<*>).extras as? String)
                            }
                            OTP_GENERATE_FAILED -> {
                                requireActivity().IActivityHelper().dismiss()
                                requireContext().showToastErrorMessage("OTP generation failed, Please try again")
                            }
                            OTP_SHOW_VERIFICATION_DIALOG -> {
                                requireActivity().IActivityHelper().dismiss()
                                val appEventData: AppEventWithData<*>? = it as? AppEventWithData<*>
                                showOTPDialog(appEventData?.extras.toString())
                            }
                            OTP_GENERATE_COMPLETE, OTP_VERIFICATION_COMPLETE -> {
                                requireActivity().IActivityHelper().dismiss()
                            }
                            OTP_VERIFICATION_SUCCESS -> {
                                requireContext().showToastSuccessMessage("Channel Partner Verified")
                                requireActivity().IActivityHelper().hideKeyboard()
                                channelPartnerVerified()
                            }
                            OTP_VERIFICATION_INVALID -> {
                                requireActivity().IActivityHelper().dismiss()
                                requireContext().showToastErrorMessage("Wrong OTP")
                            }
                            OTP_VERIFICATION_FAILED -> {
                                requireActivity().IActivityHelper().dismiss()
                                requireContext().showToastErrorMessage("Unable to verify, server error")
                            }
                            FETCH_USER_ERROR -> {
                                requireActivity().IActivityHelper().dismiss()
                                requireContext().showToastErrorMessage("Unable to fetch channel partner details")
                            }
                        }
                    }
                }
            }
        }
    }

    private fun channelPartnerVerified() {
        binding.partnerVerifiedIcon.show()
        updateApplicationRequest()
    }

    private fun setPaymentProof(proofUrl: String?) {
         binding.paymentProofIv.setRoundedImageWithDefaultCornerRadius(proofUrl)
    }

    private fun updateApplicationRequest() {
        viewModel.updateApplicationRequest(
            amountPayable = binding.amountPayable.content(),
            chequeNo = binding.paymentRefNo.content(),
            paymentType = getPaymentTypeFromCheckedId()
        ).also { appRequest ->
            findNavController().run {
                Timber.tag(LogConstant.CUSTOMER_INFO)
                    .d("partner mobile no ${viewModel.channelPartnerMobileNo}")
                if (isCurrentDestination(R.id.paymentInfoFragment)) {
                    navigate(
                        resId = R.id.action_paymentInfoFragment_to_applicationConfirmFragment,
                        Bundle().apply {
                            putString(
                                KEY_APP_REQ,
                                appRequest?.toJsonString()
                            )
                            putString(
                                KEY_PARTNER,
                                viewModel.channelPartnerDetails?.toJsonString()
                            )
                        }
                    )
                }
            }
        }
    }

    private fun showOTPDialog(customerUserId: String) {
        if (otpConfirmDialog?.isShowing() != true) {
            otpConfirmDialog = OTPDialog(
                userId = customerUserId,
                dialogTitle = getString(R.string.verify_channel_partner_otp),
                activityReference = WeakReference(requireActivity()),
                onSubmitListener = object : OTPDialog.OnOTPSubmitListener {
                    override fun onSubmit(otp: String) {
                        Timber.tag(LogConstant.CUSTOMER_INFO).d("submitting otp $otp")
                        viewModel.verifyChannelPartnerOTP(
                            otp = otp,
                            channelPartnerMobileNo = binding.channelPartnerMobileNo.content()
                        )
                        requireActivity().IActivityHelper()
                            .showLoader(getString(R.string.verifying))
                    }
                }).show()
        }
    }

    private fun setUI() {
        setContinueBtn()
        setPaymentToggle()
        setBackBtn()
        setDate()
        setUploadButton()
        // dev settings
        setTestData()
    }

    // dev settings
    fun setTestData() {
        binding.amountPayable.setText("100000")
        binding.paymentRefNo.setText("Ref1999")
        binding.channelPartnerMobileNo.setText("9962222626")
    }

    private fun setUploadButton() {
        binding.uploadProof.setOnClickListener {
            takePicture()
        }
    }

    private fun setDate() {
        binding.date.setOnClickListener {
            val materialDateBuilder = MaterialDatePicker.Builder.datePicker()
            materialDateBuilder.setTitleText("Select Payment Date")
            val picker = materialDateBuilder.build()
            picker.show(requireActivity().supportFragmentManager, picker.toString())
            picker.addOnPositiveButtonClickListener {
                val simpleFormat = SimpleDateFormat("dd/MM/yyyy", Locale.US)
                binding.date.text = simpleFormat.format(Date(it));
            }
        }
    }

    private fun setPaymentToggle() {
        binding.paymentToggle.addOnButtonCheckedListener { _, checkedId, isChecked ->
            if (isChecked) {
                when (checkedId) {
                    R.id.cash -> {
                        setReferenceEditTextHint("Cash Receipt Number")
                    }
                    R.id.cheque -> {
                        setReferenceEditTextHint("Cheque Number")
                    }
                    R.id.rtgs -> {
                        setReferenceEditTextHint("RTGS Number")
                    }
                }
            }
            setUploadButtonText(getPaymentTitle())
        }
    }

    private fun getPaymentTitle(): String {
        return when (binding.paymentToggle.checkedButtonId) {
            R.id.cheque -> {
                getString(R.string.upload_cheque)
            }
            R.id.rtgs -> {
                getString(R.string.your_rtgs_photo)
            }
            else -> {
                getString(R.string.upload_cash)
            }
        }
    }

    private fun setBackBtn() {
        binding.backBtn.setOnClickListener {
            findNavController().navigateUp()
        }
    }

    private fun setUploadButtonText(message: String) {
        binding.uploadProof.text = message
    }

    private fun setReferenceEditTextHint(hint: String) {
        binding.paymentRefNo.hint = hint
    }

    private fun setContinueBtn() {
        binding.continueBtn.setOnClickListener {
            Timber.tag(LogConstant.PAYMENT_INFO).d("payment mandatory field filled")
            if (isMandatoryInfoFilled()) {
                if (viewModel.isChannelPartnerVerified(binding.channelPartnerMobileNo.content())
                        .not()
                ) {
                    Timber.tag(LogConstant.PAYMENT_INFO).d("channel partner not verified")
                    viewModel.generateChannelPartnerOTP(binding.channelPartnerMobileNo.content())
                } else {
                    Timber.tag(LogConstant.PAYMENT_INFO).d("channel partner already verified")
                    updateApplicationRequest()
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
            requireContext().showToastErrorMessage("Channel partner mobile number is mandatory")
            return false
        }
        if (binding.amountPayable.hasValidData().not()) {
            binding.amountPayable.error = "Amount payable is mandatory";
            requireContext().showToastErrorMessage("Amount payable is mandatory")
            return false
        }
        if (viewModel.isPaymentProofUploaded().not()) {
            requireContext().showToastErrorMessage("kindly ${getPaymentTitle()}")
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
            dialogBinding.paymentTitle.text = binding.continueBtn.text
            dialogBinding.upload.setOnClickListener {
                Timber.tag(LogConstant.PAYMENT_INFO).d("upload image")
                uploadChequeDialog?.dismiss()
                viewModel.uploadImage(bitmap)
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
            requireContext().showToastErrorMessage("This device does not have camera application to proceed")
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        Timber.tag(LogConstant.PAYMENT_INFO).d("on activity result")
        if (requestCode == REQUEST_IMAGE_CAPTURE) {
            if (resultCode == Activity.RESULT_OK) {
                val bitmap = data?.extras?.get("data") as Bitmap
                val imageUri: Uri? = data.data
                Timber.tag(LogConstant.PAYMENT_INFO).d("picture taken successfully : $imageUri")
                showUploadChequeDialog(bitmap)
            } else {
                requireContext().showToastSuccessMessage("Photo capture cancelled")
            }
        }
    }

    override fun onDestroyView() {
        uploadChequeDialog?.dismiss()
        super.onDestroyView()
    }

    companion object {
        const val REQUEST_IMAGE_CAPTURE = 1000
    }

}