package com.hipla.channel.ui

import android.os.Bundle
import android.view.KeyEvent.ACTION_UP
import android.view.View
import android.view.inputmethod.EditorInfo
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import com.hipla.channel.R
import com.hipla.channel.common.*
import com.hipla.channel.common.Utils.hide
import com.hipla.channel.databinding.FragmentApplicationCustomerInfoBinding
import com.hipla.channel.entity.*
import com.hipla.channel.extension.*
import com.hipla.channel.viewmodel.ApplicationCustomerInfoViewModel
import timber.log.Timber

class ApplicationCustomerInfoFragment : Fragment(R.layout.fragment_application_customer_info) {

    private lateinit var viewModel: ApplicationCustomerInfoViewModel
    private lateinit var binding: FragmentApplicationCustomerInfoBinding

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentApplicationCustomerInfoBinding.bind(view)
        viewModel =
            ViewModelProvider(this)[ApplicationCustomerInfoViewModel::class.java]
        viewModel.extractArguments(arguments)
        observeViewModel()
        setUI()
    }

    private fun setUI() {
        setHeader()
        setBackBtn()
        setContinueBtn()
        setCustomerFirstName()
        setCustomerLastName()
        setCustomerPhone()
        setPanNo()
        setFloorPreference()
        setFocusRequest()



        // dev settings
/*        binding.continueBtn.setOnClickListener {
            if (isMandatoryCustomerInfoFilled().not()) {
                Timber.tag(LogConstant.CUSTOMER_INFO).d("can create application")
                applicationCustomerInfoViewModel.createApplicationRequest(
                    customerFirstName = "Shiva",
                    customerLastName = "Guru",
                    customerPhone = "9916555886",
                    panNo = "DYESSS123234",
                    floorId = 1
                )
            } else {
                Timber.tag(LogConstant.CUSTOMER_INFO)
                    .e("validation failed, cannot create application")
            }
        }*/
    }

    private fun setHeader() {
        binding.header.text = getFormTitle()
    }



    private fun getFormTitle(): String {
        return if (viewModel.flowConfig.isApplication()) {
            "Application Form"
        } else if (viewModel.flowConfig.isInventory()) {
            "Booking for ${viewModel.unitInfo?.name} in floor ${viewModel.unitInfo?.id}"
        } else {
            Constant.EMPTY_STRING
        }
    }

    private fun setBackBtn() {
        binding.backBtn.setOnClickListener {
            findNavController().navigateUp()
        }
    }

    private fun setContinueBtn() {
        binding.continueBtn.setOnClickListener {
            if (isMandatoryCustomerInfoFilled()) {
                Timber.tag(LogConstant.CUSTOMER_INFO).d("can create application")
                viewModel.createApplicationRequest(
                    customerFirstName = binding.customerFirstName.content(),
                    customerLastName = binding.customerLastName.content(),
                    customerPhone = binding.customerNumber.content(),
                    panNo = binding.panCardNumber.content(),
                    floorId = viewModel.getCustomerFloorPreferenceId()!!
                )
            } else {
                Timber.tag(LogConstant.CUSTOMER_INFO)
                    .e("validation failed, cannot create application")
            }
        }
    }

    private fun setPanNo() {
        viewModel.getCustomerPanNo()?.let {
            binding.panCardNumber.setText(it)
        }
    }

    private fun setCustomerPhone() {
        viewModel.getCustomerMobileNo()?.let {
            binding.customerNumber.setText(it)
        }
    }

    private fun setCustomerLastName() {
        viewModel.getCustomerLastName()?.let {
            binding.customerLastName.setText(it)
        }
    }

    private fun setCustomerFirstName() {
        viewModel.getCustomerFirstName()?.let {
            binding.customerFirstName.setText(it)
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
                        putString(
                            KEY_APPLICATION_SERVER_INF0,
                            viewModel.applicationCreateResponse?.toJsonString()
                        )
                        putString(KEY_UNIT, arguments?.getString(KEY_UNIT))
                        putString(KEY_FLOW_CONFIG, arguments?.getString(KEY_FLOW_CONFIG))
                    }
                )
            }
        }
    }

    private fun isMandatoryCustomerInfoFilled(): Boolean {
        if (binding.customerFirstName.hasValidData().not()) {
            binding.customerFirstName.error = "Customer first name is mandatory";
            requireContext().showToastErrorMessage("Customer first name is mandatory")
            return false
        }
        if (binding.customerLastName.hasValidData().not()) {
            binding.customerLastName.error = "Customer last name is mandatory";
            requireContext().showToastErrorMessage("Customer last name is mandatory")
            return false
        }
        if (binding.customerNumber.hasValidData().not()) {
            binding.customerNumber.error = "Customer number is mandatory";
            requireContext().showToastErrorMessage("Customer number is mandatory")
            return false
        }
        if (binding.panCardNumber.hasValidData().not()) {
            binding.panCardNumber.error = "Customer PAN number is mandatory";
            requireContext().showToastErrorMessage("Customer  PAN number  is mandatory")
            return false
        }
        if (binding.panCardNumber.isValidPAN().not()) {
            binding.panCardNumber.error = "Customer PAN should have 10 digits";
            requireContext().showToastErrorMessage("Customer PAN should have 10 digits")
            return false
        }
        if (viewModel.isFloorPreferenceSelected().not()) {
            requireContext().showToastErrorMessage("Kindly select floor preference")
            return false
        }
        return true
    }

    private fun observeViewModel() {
        viewLifecycleOwner.lifecycleScope.launchSafely {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                launchSafely {
                    viewModel.appEvent.collect {
                        when (it.id) {
                            APP_EVENT_APPLICATION_SUCCESS -> {
                                requireActivity().IActivityHelper().dismiss()
                                val appEventData: AppEventWithData<*>? = it as? AppEventWithData<*>
                                val applicationRequest = appEventData?.extras as? ApplicationRequest
                                launchPaymentInfoFragment(applicationRequest)
                            }
                            APP_EVENT_APPLICATION_FAILED -> {
                                requireActivity().IActivityHelper().dismiss()
                                requireContext().showToastErrorMessage("Application creation failed")
                            }
                            APP_EVENT_APPLICATION_COMPLETE -> {
                                requireActivity().IActivityHelper().dismiss()
                            }
                        }
                    }
                }
            }
        }
    }

    private fun setFocusRequest() {
        binding.customerLastName.setOnEditorActionListener { v, actionId, event ->
            return@setOnEditorActionListener when (actionId) {
                EditorInfo.IME_ACTION_SEND -> {
                    binding.customerNumber.requestFocus()
                    true
                }
                else -> false
            }
        }

        binding.customerNumber.setOnEditorActionListener { v, actionId, event ->
            return@setOnEditorActionListener when (actionId) {
                EditorInfo.IME_ACTION_SEND -> {
                    binding.panCardNumber.requestFocus()
                    true
                }
                else -> false
            }
        }


        binding.panCardNumber.setOnEditorActionListener { v, actionId, event ->
            return@setOnEditorActionListener when (actionId) {
                EditorInfo.IME_ACTION_SEND -> {
                    requireActivity().IActivityHelper().hideKeyboard()
                    true
                }
                else -> false
            }
        }

    }


    private fun setFloorPreference() {
        if (viewModel.flowConfig.isApplication()) {
            val floorAdapter: ArrayAdapter<String> =
                ArrayAdapter(
                    requireContext(),
                    R.layout.autocomplete_list_item,
                    viewModel.floorList.map { it.name })
            binding.floorPreference.run {
                setAdapter(floorAdapter)
                setOnTouchListener { _, motionEvent ->
                    if (motionEvent.action == ACTION_UP) {
                        showDropDown()
                    }
                    return@setOnTouchListener false;
                }
                onItemClickListener =
                    AdapterView.OnItemClickListener { _, _, position, _ ->
                        viewModel.selectedFloorId(position)
                    }
            }
            viewModel.getCustomerFloorPreferenceId()?.let {
                binding.floorPreference.setText(
                    binding.floorPreference.adapter.getItem(it).toString(),
                    false
                )
            }
        } else {
            binding.floorPreference.hide()
        }
    }
}