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
import com.hipla.channel.common.Constant
import com.hipla.channel.common.LogConstant
import com.hipla.channel.databinding.DialogApplicationSuccessfulBinding
import com.hipla.channel.databinding.FragmentFlowConfirmBinding
import com.hipla.channel.entity.*
import com.hipla.channel.extension.*
import com.hipla.channel.viewmodel.ApplicationConfirmationViewModel
import com.hipla.channel.widget.OTPDialog
import timber.log.Timber
import java.lang.ref.WeakReference

class FlowConfirmationFragment : Fragment(R.layout.fragment_flow_confirm) {

    private lateinit var viewModel: ApplicationConfirmationViewModel
    private lateinit var binding: FragmentFlowConfirmBinding
    private var applicationSuccessDialog: AlertDialog? = null
    private var otpConfirmDialog: OTPDialog? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentFlowConfirmBinding.bind(view)
        viewModel =
            ViewModelProvider(this)[ApplicationConfirmationViewModel::class.java]
        viewModel.extractArguments(arguments)
        observeViewModel()
        setUI()
    }

    private fun setUI() {
        viewModel.applicationRequest?.let {
            setHeader(it)
        }
        viewModel.channelPartnerDetails?.let {
            binding.channelPartnerWidget.setChannelPartnerDetails(it)
        }
        binding.submit.setOnClickListener {
            viewModel.generateCustomerOTP()
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
            dialogBinding.flowSuccessTitle.text = getSuccessMessage()
            dialogBinding.appInfo.text = getConfirmationMessage(applicationRequest)
            dialogBinding.close.setOnClickListener {
                applicationSuccessDialog?.dismiss()
                goHome()
            }
            dialogBinding.okay.setOnClickListener {
                applicationSuccessDialog?.dismiss()
                goHome()
            }
            applicationSuccessDialog = dialogBuilder.show()
            applicationSuccessDialog?.setCancelable(false)
            applicationSuccessDialog?.setCanceledOnTouchOutside(false)
        }
    }

    private fun getSuccessMessage(): String {
        return if (viewModel.flowConfig.isApplication()) {
            getString(R.string.application_success)
        } else if (viewModel.flowConfig.isInventory()) {
            getString(R.string.inventory_success)
        } else {
            Constant.EMPTY_STRING
        }
    }

    private fun getConfirmationMessage(applicationRequest: ApplicationRequest): String {
        return if (viewModel.flowConfig.isApplication()) {
            getString(R.string.application_confirm_msg, applicationRequest.id.toString())
        } else if (viewModel.flowConfig.isInventory()) {
            getString(R.string.inventory_confirm_msg, applicationRequest.id.toString())
        } else {
            Constant.EMPTY_STRING
        }
    }

    private fun getFlowConfirmationMessage(applicationRequest: ApplicationRequest): String {
        return if (viewModel.flowConfig.isApplication()) {
            getString(
                R.string.application_confirm_application_no,
                viewModel.applicationRequest?.id.toString()
            )
        } else if (viewModel.flowConfig.isInventory()) {
            getString(R.string.application_confirm_inventory_no, viewModel.unitInfo?.name)
        } else {
            Constant.EMPTY_STRING
        }
    }

    private fun goHome() {
        findNavController().popBackStack(R.id.salesUserFragment, false)
    }

    private fun observeViewModel() {
        viewLifecycleOwner.lifecycleScope.launchSafely {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                launchSafely {
                    viewModel.appEvent.collect {
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
                                requireContext().showToastErrorMessage("Application updating failed")
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
        val flowConfirmationMessage = "Validate your details \n"
        val customerName = requireContext().getString(
            R.string.application_confirm_customer_name,
            "${applicationRequest.customerName} ${applicationRequest.customerLastName} "
        )
        val amountPayable = requireContext().getString(
            R.string.confirm_amount_payable,
            applicationRequest.amountPayable
        )
        binding.header.text = StringBuilder().apply {
            append(flowConfirmationMessage)
            append(customerName)
            append(amountPayable)
        }.toString()
    }


    private fun showOTPDialog(customerUserId: String) {
        Timber.tag(LogConstant.CUSTOMER_INFO).d(": $customerUserId.")
        if (otpConfirmDialog?.isShowing() != true) {
            otpConfirmDialog = OTPDialog(
                userId = customerUserId,
                dialogTitle = getString(R.string.verify_customer_otp),
                activityReference = WeakReference(requireActivity()),
                onSubmitListener = object : OTPDialog.OnOTPSubmitListener {
                    override fun onSubmit(otp: String) {
                        Timber.tag(LogConstant.CUSTOMER_INFO).d("submitting otp $otp")
                        viewModel.verifyCustomerOTP(otp)
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