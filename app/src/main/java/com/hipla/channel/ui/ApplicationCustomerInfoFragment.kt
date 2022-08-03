package com.hipla.channel.ui

import android.os.Bundle
import android.view.KeyEvent.ACTION_UP
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import com.hipla.channel.R
import com.hipla.channel.common.KEY_APP_REQ
import com.hipla.channel.common.LogConstant
import com.hipla.channel.databinding.DialogOtpConfirmBinding
import com.hipla.channel.databinding.FragmentApplicationCustomerInfoBinding
import com.hipla.channel.entity.*
import com.hipla.channel.extension.*
import com.hipla.channel.viewmodel.ApplicationCustomerInfoViewModel
import kotlinx.coroutines.launch
import timber.log.Timber

class ApplicationCustomerInfoFragment : Fragment(R.layout.fragment_application_customer_info) {

    private lateinit var applicationCustomerInfoViewModel: ApplicationCustomerInfoViewModel
    private lateinit var binding: FragmentApplicationCustomerInfoBinding
    private var floorList = generateFloors()
    private var selectedFloorPreference: FloorDetails? = null
    private var otpConfirmDialog: AlertDialog? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentApplicationCustomerInfoBinding.bind(view)
        applicationCustomerInfoViewModel =
            ViewModelProvider(this)[ApplicationCustomerInfoViewModel::class.java]
        setFloorPreference()
        observeViewModel()
        setUI()
    }

    private fun setUI() {
        binding.backBtn.setOnClickListener {
            findNavController().navigateUp()
        }
        binding.continueBtn.setOnClickListener {
            launchPaymentInfoFragment(
                ApplicationRequest(
                    id = 52,
                    customerName = "Shiva",
                    customerLastName = "Guru",
                    panNumber = "pan",
                    floorPreferenceId = 1,
                    customerPhoneNumber = "9916555886"
                )
            )
            if (isMandatoryCustomerInfoFilled()) {
                Timber.tag(LogConstant.CUSTOMER_INFO).d("can create application")
                applicationCustomerInfoViewModel.generateCustomerOTP(
                    applicationCustomerInfoViewModel.createApplicationRequest(
                        customerFirstName = binding.customerFirstName.content(),
                        customerLastName = binding.customerLastName.content(),
                        customerPhone = binding.customerNumber.content(),
                        panNo = binding.panCardNumber.content(),
                        floorId = selectedFloorPreference?.id ?: 0
                    )
                )
            } else {
                Timber.tag(LogConstant.CUSTOMER_INFO)
                    .e("validation failed, cannot create application")
            }
        }
    }

    private fun launchPaymentInfoFragment(applicationRequest: ApplicationRequest?) {
        findNavController().run {
            if (isCurrentDestination(R.id.customerInfoFragment)) {
                navigate(
                    resId = R.id.action_customerInfoFragment_to_paymentInfoFragment,
                    Bundle().apply {
                        putString(
                            KEY_APP_REQ,
                            applicationRequest?.toJsonString()
                        )
                    }
                )
            }
        }
    }

    private fun showOTPDialog(customerUserId: String) {
        Timber.tag(LogConstant.CUSTOMER_INFO).d(": $customerUserId.")
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
                    applicationCustomerInfoViewModel.verifyCustomerOTP(it.content())
                    requireActivity().toILoader().showLoader(getString(R.string.verifying))
                }
            }
            otpConfirmDialog = dialogBuilder.show()
            otpConfirmDialog?.setCancelable(false)
            otpConfirmDialog?.setCanceledOnTouchOutside(false)
        }
    }

    private fun isMandatoryCustomerInfoFilled(): Boolean {
        if (binding.customerFirstName.hasValidData().not()) {
            binding.customerFirstName.error = "Customer first name is mandatory";
            requireContext().showToastLongDuration("Customer first name is mandatory")
            return false
        }
        if (binding.customerLastName.hasValidData().not()) {
            binding.customerLastName.error = "Customer last name is mandatory";
            requireContext().showToastLongDuration("Customer last name is mandatory")
            return false
        }
        if (binding.customerNumber.hasValidData().not()) {
            binding.customerNumber.error = "Customer number is mandatory";
            requireContext().showToastLongDuration("Customer number is mandatory")
            return false
        }
        if (binding.panCardNumber.hasValidData().not()) {
            binding.panCardNumber.error = "Customer PAN number is mandatory";
            requireContext().showToastLongDuration("Customer  PAN number  is mandatory")
            return false
        }
        return true
    }

    private fun observeViewModel() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                applicationCustomerInfoViewModel.appEvent.collect {
                    when (it.id) {
                        APP_EVENT_APPLICATION_SUCCESS -> {
                            requireActivity().toILoader().dismiss()
                            val appEventData: AppEventWithData<*>? = it as? AppEventWithData<*>
                            val applicationRequest = appEventData?.extras as? ApplicationRequest
                            launchPaymentInfoFragment(applicationRequest)
                        }
                        APP_EVENT_APPLICATION_FAILED -> {
                            requireActivity().toILoader().dismiss()
                            requireContext().showToastLongDuration("Application creation failed")
                        }
                        OTP_VERIFYING -> {
                            requireActivity().toILoader().showLoader("Verifying OTP")
                        }
                        OTP_GENERATING -> {
                            requireActivity().toILoader().showLoader("Generating OTP")
                        }
                        OTP_GENERATE_FAILED -> {
                            requireActivity().toILoader().dismiss()
                            requireContext().showToastLongDuration("OTP generation failed, Please try again")
                        }
                        OTP_SHOW_VERIFICATION_DIALOG -> {
                            val appEventData: AppEventWithData<*>? = it as? AppEventWithData<*>
                            showOTPDialog((appEventData?.extras) as String)
                        }
                        OTP_GENERATE_COMPLETE, OTP_VERIFICATION_COMPLETE, APP_EVENT_APPLICATION_COMPLETE -> {
                            requireActivity().toILoader().dismiss()
                        }
                        OTP_VERIFICATION_INVALID -> {
                            requireActivity().toILoader().dismiss()
                            requireContext().showToastLongDuration("Wrong OTP")
                        }
                        OTP_VERIFICATION_FAILED -> {
                            requireActivity().toILoader().dismiss()
                            requireContext().showToastLongDuration("Unable to verify, server error")
                        }
                    }
                }
            }
        }
    }


    private fun setFloorPreference() {
        val floorAdapter: ArrayAdapter<String> =
            ArrayAdapter(
                requireContext(),
                R.layout.autocomplete_list_item,
                floorList.map { it.name })
        binding.floorPreference.run {
            setAdapter(floorAdapter)
            setOnTouchListener { _, motionEvent ->
                if (motionEvent.action == ACTION_UP) {
                    showDropDown()
                }
                return@setOnTouchListener false;
            }
            onItemClickListener =
                AdapterView.OnItemClickListener { parent, view, position, id ->
                    Timber.tag(LogConstant.CUSTOMER_INFO)
                        .d("floor preference ${floorList[position].name}")
                    selectedFloorPreference = floorList[position]
                }
        }
    }

}