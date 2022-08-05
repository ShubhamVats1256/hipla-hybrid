package com.hipla.channel.ui

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import com.hipla.channel.R
import com.hipla.channel.common.LogConstant
import com.hipla.channel.databinding.DialogApplicationSuccessfulBinding
import com.hipla.channel.databinding.FragmentApplicationConfirmBinding
import com.hipla.channel.entity.*
import com.hipla.channel.extension.*
import com.hipla.channel.viewmodel.ApplicationConfirmationViewModel
import com.hipla.channel.widget.OTPDialog
import timber.log.Timber
import java.lang.ref.WeakReference

class ApplicationConfirmationFragment : Fragment(R.layout.fragment_application_confirm) {

    private lateinit var applicationConfirmationViewModel: ApplicationConfirmationViewModel
    private lateinit var binding: FragmentApplicationConfirmBinding
    private var applicationSuccessDialog: AlertDialog? = null
    private var otpConfirmDialog: OTPDialog? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentApplicationConfirmBinding.bind(view)
        applicationConfirmationViewModel =
            ViewModelProvider(this)[ApplicationConfirmationViewModel::class.java]
        observeViewModel()
        setUI()
    }

    private fun setUI() {
        applicationConfirmationViewModel.extractArguments(arguments)?.let {
            setHeader(it)
        }
        binding.submit.setOnClickListener {
            applicationConfirmationViewModel.generateCustomerOTP()
        }
        binding.backBtn.setOnClickListener {
            findNavController().navigateUp()
        }
    }

    private fun showApplicationSuccessful(applicationRequest: ApplicationRequest) {
        Timber.tag(LogConstant.CUSTOMER_INFO).d("showing application request successful dialog")
        if (requireActivity().isDestroyed.not()) {
            val dialogBuilder: AlertDialog.Builder = AlertDialog.Builder(requireContext())
            val dialogBinding =
                DialogApplicationSuccessfulBinding.inflate(requireActivity().layoutInflater)
            dialogBuilder.setView(dialogBinding.root)
            dialogBinding.appInfo.text =
                getString(R.string.application_confirm_msg, applicationRequest.id.toString())
            dialogBinding.close.setOnClickListener {
                applicationSuccessDialog?.dismiss()
                findNavController().popBackStack(R.id.mainFragment, false)
            }
            dialogBinding.appInfo.text = getString(
                R.string.application_confirm_application_no,
                applicationRequest.id.toString()
            )
            applicationSuccessDialog = dialogBuilder.show()
            applicationSuccessDialog?.setCancelable(false)
            applicationSuccessDialog?.setCanceledOnTouchOutside(false)
        }
    }

    private fun observeViewModel() {
        viewLifecycleOwner.lifecycleScope.launchSafely {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                launchSafely {
                    applicationConfirmationViewModel.appEvent.collect {
                        when (it.id) {
                            APPLICATION_UPDATING -> {
                                requireActivity().IActivityHelper()
                                    .showLoader("Updating Application")
                            }
                            APPLICATION_UPDATING_SUCCESS -> {
                                Timber.tag(LogConstant.APP_CONFIRM)
                                    .d("application update success")
                                requireActivity().IActivityHelper().dismiss()
                                it.toApplicationRequest()?.let { appRequest ->
                                    Timber.tag(LogConstant.APP_CONFIRM)
                                        .d("application request extracted")
                                    showApplicationSuccessful(appRequest)
                                }
                            }
                            APPLICATION_UPDATING_FAILED -> {
                                Timber.tag(LogConstant.APP_CONFIRM)
                                    .d("application update failed")
                                requireActivity().IActivityHelper().dismiss()
                                requireContext().showToastMessage("Application updating failed")
                            }
                            OTP_VERIFYING -> {
                                requireActivity().IActivityHelper().showLoader("Verifying OTP")
                            }
                            OTP_GENERATING -> {
                                requireActivity().IActivityHelper().showLoader("Generating OTP")
                            }
                            OTP_VERIFICATION_SUCCESS -> {
                                requireActivity().IActivityHelper().dismiss()
                            }
                            OTP_GENERATE_FAILED -> {
                                requireActivity().IActivityHelper().dismiss()
                                requireContext().showToastErrorMessage("OTP generation failed, Please try again")
                            }
                            OTP_SHOW_VERIFICATION_DIALOG -> {
                                val appEventData: AppEventWithData<*>? = it as? AppEventWithData<*>
                                showOTPDialog((appEventData?.extras) as String)
                            }
                            OTP_GENERATE_COMPLETE, OTP_VERIFICATION_COMPLETE -> {
                                requireActivity().IActivityHelper().dismiss()
                            }
                            OTP_VERIFICATION_INVALID -> {
                                requireActivity().IActivityHelper().dismiss()
                                requireContext().showToastErrorMessage("Wrong OTP")
                            }
                            OTP_VERIFICATION_FAILED -> {
                                requireActivity().IActivityHelper().dismiss()
                                requireContext().showToastErrorMessage("Unable to verify, server error")
                            }
                        }
                    }
                }
            }
        }
    }

    private fun setHeader(applicationRequest: ApplicationRequest) {
        Timber.tag(LogConstant.APP_CONFIRM)
            .d("setting info customer name ${applicationRequest.customerName}r")
        Timber.tag(LogConstant.APP_CONFIRM)
            .d("setting info application no ${applicationRequest.id}r")
        val customerName = requireContext().getString(
            R.string.application_confirm_customer_name,
            "${applicationRequest.customerName} ${applicationRequest.customerLastName} "
        )
        val applicationNo = requireContext().getString(
            R.string.application_confirm_application_no,
            applicationRequest.id.toString()
        )
        val amountPayable = requireContext().getString(
            R.string.confirm_amount_payable,
            applicationRequest.amountPayable
        )
        val channelPartnerMobileNoText = requireContext().getString(
            R.string.confirm_channel_partner_no,
            applicationConfirmationViewModel.channelPartnerMobileNo
        )
        binding.header.text = StringBuilder().apply {
            append(customerName)
            append(applicationNo)
            append(amountPayable)
            append(channelPartnerMobileNoText)
        }.toString()
    }

    private fun showOTPDialog(customerUserId: String) {
        Timber.tag(LogConstant.CUSTOMER_INFO).d(": $customerUserId.")
        if (otpConfirmDialog?.isShowing() != true) {
            otpConfirmDialog = OTPDialog(
                userId = customerUserId,
                dialogTitle = getString(R.string.verify_channel_partner_otp),
                activityReference = WeakReference(requireActivity()),
                onSubmitListener = object : OTPDialog.OnOTPSubmitListener {
                    override fun onSubmit(otp: String) {
                        Timber.tag(LogConstant.CUSTOMER_INFO).d("submitting otp $otp")
                        applicationConfirmationViewModel.verifyCustomerOTP(otp)
                        requireActivity().IActivityHelper()
                            .showLoader(getString(R.string.verifying))
                    }
                }).show()
        }
    }

    override fun onDestroyView() {
        applicationSuccessDialog?.dismiss()
        otpConfirmDialog?.dismiss()
        super.onDestroyView()
    }

}