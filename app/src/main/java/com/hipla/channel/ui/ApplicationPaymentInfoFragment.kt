package com.hipla.channel.ui

import android.app.Activity
import android.content.ContentResolver
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import com.github.dhaval2404.imagepicker.ImagePicker
import com.google.android.material.datepicker.CalendarConstraints
import com.google.android.material.datepicker.CalendarConstraints.DateValidator
import com.google.android.material.datepicker.CompositeDateValidator
import com.google.android.material.datepicker.DateValidatorPointBackward
import com.google.android.material.datepicker.MaterialDatePicker
import com.hipla.channel.R
import com.hipla.channel.common.*
import com.hipla.channel.common.Utils.hide
import com.hipla.channel.common.Utils.show
import com.hipla.channel.common.Utils.tryCatch
import com.hipla.channel.common.image.setRoundedImageWithDefaultCornerRadius
import com.hipla.channel.databinding.DialogUploadPhotoBinding
import com.hipla.channel.databinding.FragmentApplicationPaymentInfoBinding
import com.hipla.channel.entity.*
import com.hipla.channel.extension.*
import com.hipla.channel.viewmodel.ApplicationPaymentInfoViewModel
import com.hipla.channel.widget.OTPDialog
import timber.log.Timber
import java.io.File
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
                                setPaymentProof()
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

    private fun setPaymentProof() {
        tryCatch {
            binding.paymentProofIv.setRoundedImageWithDefaultCornerRadius(viewModel.getPaymentProofReadUrl())
        }
    }

    private fun updateApplicationRequest() {
        viewModel.updateApplicationRequest(
            amountPayable = binding.amountPayable.content(),
            chequeNo = binding.paymentRefNo.content(),
            paymentType = getPaymentTypeFromCheckedId()
        ).also { appRequest ->
            findNavController().run {
                Timber.tag(LogConstant.CUSTOMER_INFO)
                    .d("partner mobile no ${viewModel.getChannelPartnerPhoneNo()}")
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
                            putString(KEY_UNIT, arguments?.getString(KEY_UNIT))
                            putString(KEY_FLOW_CONFIG, arguments?.getString(KEY_FLOW_CONFIG))
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
        setHeader()
        setContinueBtn()
        setBackBtn()
        setDate()
        setUploadButton()
        setAmountPayable()
        setPaymentToggle()
        setPaymentReferenceNo()
        setChannelPartnerMobileNo()
        setPaymentDate()
        setChannelPartnerVerifiedIcon(binding.channelPartnerMobileNo.content())
        setPaymentProofImage()
        // dev settings
        //setTestData()
    }

    private fun setHeader() {
        binding.header.text = getFormTitle()
    }

    private fun getFormTitle(): String {
        return if (viewModel.flowConfig.isApplication()) {
            "Payment Information"
        } else if (viewModel.flowConfig.isInventory()) {
            "Booking for ${viewModel.unitInfo?.name}"
        } else {
            Constant.EMPTY_STRING
        }
    }

    private fun setPaymentDate() {
        viewModel.getPaymentDate()?.let {
            binding.paymentDate.text = it
        }
    }

    private fun setChannelPartnerMobileNo() {
        viewModel.getChannelPartnerPhoneNo()?.let {
            binding.channelPartnerMobileNo.setText(it)
        }
        binding.channelPartnerMobileNo.addTextChangedListener(object : TextWatcher {

            override fun afterTextChanged(s: Editable) {

            }

            override fun beforeTextChanged(
                s: CharSequence, start: Int,
                count: Int, after: Int
            ) {
            }

            override fun onTextChanged(
                userTypedPartnerNo: CharSequence, start: Int,
                before: Int, count: Int
            ) {
                setChannelPartnerVerifiedIcon(userTypedPartnerNo.toString())
            }
        })
    }

    private fun setPaymentReferenceNo() {
        viewModel.getPaymentReferenceNo()?.let {
            binding.paymentRefNo.setText(it)
        }
    }

    private fun setAmountPayable() {
        viewModel.getAmountPayable()?.let {
            binding.amountPayable.setText(it)
        }
    }

    private fun setChannelPartnerVerifiedIcon(mobileNo: String) {
        if (viewModel.isChannelPartnerVerified(mobileNo)) {
            Timber.tag(LogConstant.PAYMENT_INFO)
                .d("channel partner verified, showing verified icon")
            binding.partnerVerifiedIcon.show()
        } else {
            Timber.tag(LogConstant.PAYMENT_INFO)
                .d("channel partner not verified, hiding verified icon")
            binding.partnerVerifiedIcon.hide()
        }
    }

    // dev settings
    fun setTestData() {
        binding.amountPayable.setText("100000")
        binding.paymentRefNo.setText("Ref1999")
        binding.channelPartnerMobileNo.setText("9962222626")
    }

    private fun setPaymentProofImage() {
        binding.paymentProofIv.setRoundedImageWithDefaultCornerRadius(viewModel.getPaymentProofReadUrl());
    }

    private fun setUploadButton() {
        binding.uploadProof.setOnClickListener {
            takePicture()
        }
    }

    private fun setDate() {
        viewModel.getPaymentDate()?.let {
            binding.paymentDate.text = it
        }
        binding.paymentDate.setOnClickListener {
            val materialDateBuilder = MaterialDatePicker.Builder.datePicker()
            // prepare date range
            val constraintsBuilderRange = CalendarConstraints.Builder()
            val oneDayInMillis: Long = 1000 * 60 * 60 * 24
            val endDateInMs = System.currentTimeMillis() + oneDayInMillis * 2// days from today
            val dateValidatorMax: DateValidator =
                DateValidatorPointBackward.before(endDateInMs)
            val listValidators = ArrayList<DateValidator>()
            listValidators.add(dateValidatorMax)
            val validators = CompositeDateValidator.allOf(listValidators)
            constraintsBuilderRange.setValidator(validators)
            materialDateBuilder.setCalendarConstraints(constraintsBuilderRange.build())
            materialDateBuilder.setTitleText("Select Date")
            val picker = materialDateBuilder.build()
            picker.show(requireActivity().supportFragmentManager, picker.toString())
            picker.addOnPositiveButtonClickListener {
                val simpleFormat = SimpleDateFormat("dd/MM/yyyy", Locale.US)
                viewModel.setPaymentDate(simpleFormat.format(Date(it)))
                binding.paymentDate.text = viewModel.getPaymentDate()
            }
        }
    }

    private fun setPaymentToggle() {
        binding.paymentToggle.addOnButtonCheckedListener { _, checkedId, isChecked ->
            if (isChecked) {
                when (checkedId) {
                    R.id.dd -> {
                        setReferenceEditTextHint("DD Number")
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
        viewModel.getSelectedPaymentType().let {
            binding.paymentToggle.check(getSelectedPaymentCheckedId(it))
        }
    }

    private fun getSelectedPaymentCheckedId(selectedPaymentType: PaymentType): Int {
        return when (selectedPaymentType) {
            PaymentType.DD() -> {
                R.id.dd
            }
            PaymentType.Rtgs() -> {
                R.id.rtgs
            }
            else -> {
                R.id.cheque
            }
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
            R.id.dd -> {
                getString(R.string.upload_dd)
            }
            else -> {
                getString(R.string.upload_receipt)
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
            R.id.dd -> PaymentType.DD()
            else -> PaymentType.Unknown()
        }
    }

    private fun isMandatoryInfoFilled(): Boolean {

        if (binding.amountPayable.hasValidData().not()) {
            binding.amountPayable.error = "Amount payable is mandatory";
            requireContext().showToastErrorMessage("Amount payable is mandatory")
            return false
        }


        if (binding.amountPayable.text.toString().toFloat() < 500000) {
            binding.amountPayable.error = "Minimum amount payable is 5,00,000";
            requireContext().showToastErrorMessage("Minimum amount payable is 5,00,000")
            return false
        }

        if (binding.paymentDate.text.toString() == "Date") {
            requireContext().showToastErrorMessage("Please select date")
            return false
        }

        if (binding.channelPartnerMobileNo.hasValidData().not()) {
            binding.channelPartnerMobileNo.error = "Channel partner mobile number is mandatory";
            requireContext().showToastErrorMessage("Channel partner mobile number is mandatory")
            return false
        }

        if (viewModel.isPaymentProofUploaded().not()) {
            requireContext().showToastErrorMessage("Kindly ${getPaymentTitle()}")
            return false
        }
        return true
    }

    private fun showUploadChequeDialog(bitmap: Bitmap) {
        if (requireActivity().isDestroyed.not()) {
            val dialogBuilder: AlertDialog.Builder = AlertDialog.Builder(requireContext())
            val dialogBinding =
                DialogUploadPhotoBinding.inflate(requireActivity().layoutInflater)
            dialogBuilder.setView(dialogBinding.root)
            dialogBinding.chequePhoto.setImageBitmap(bitmap)
            dialogBinding.paymentTitle.text = getPaymentTitle()
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
        tryCatch {
            Timber.tag(LogConstant.PAYMENT_INFO).d("capture image")
            ImagePicker.with(this)
                .saveDir(File(requireActivity().cacheDir, "ImagePicker"))
                .cameraOnly()
                .start(REQUEST_IMAGE_CAPTURE)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        Timber.tag(LogConstant.PAYMENT_INFO).d("on activity result")
        if (requestCode == REQUEST_IMAGE_CAPTURE) {
            if (resultCode == Activity.RESULT_OK) {
                val imageUri: Uri? = data?.data
                Timber.tag(LogConstant.PAYMENT_INFO).d("picture taken successfully : $imageUri")
                var bitmap: Bitmap? = null
                val contentResolver: ContentResolver = requireActivity().contentResolver
                try {
                    val startTime = System.currentTimeMillis()
                    val source = ImageDecoder.createSource(
                        contentResolver,
                        imageUri!!
                    )
                    bitmap = ImageDecoder.decodeBitmap(source)
                    Timber.tag(LogConstant.PAYMENT_INFO).d("bitmap rendered from fileUri ${System.currentTimeMillis() - startTime}")
                    showUploadChequeDialog(bitmap)
                } catch (e: Exception) {
                    e.printStackTrace()
                }
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
