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
import com.hipla.channel.extension.showToastLongDuration
import com.hipla.channel.extension.toApplicationRequest
import com.hipla.channel.extension.toILoader
import com.hipla.channel.viewmodel.ApplicationConfirmationViewModel
import kotlinx.coroutines.launch
import timber.log.Timber

class ApplicationConfirmationFragment : Fragment(R.layout.fragment_application_confirm) {

    private lateinit var viewModel: ApplicationConfirmationViewModel
    private lateinit var binding: FragmentApplicationConfirmBinding
    private var applicationSuccessDialog: AlertDialog? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentApplicationConfirmBinding.bind(view)
        viewModel = ViewModelProvider(this)[ApplicationConfirmationViewModel::class.java]
        observeViewModel()
        setUI()
        viewModel.extractArguments(arguments)?.let {
            setHeader(it)
        }
        requireActivity().toILoader().dismiss()
    }

    private fun setUI() {
        binding.submit.setOnClickListener {
            viewModel.updateApplication()
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
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.appEvent.collect {
                    when (it.id) {
                        APPLICATION_UPDATING -> {
                            requireActivity().toILoader().showLoader("Updating Application")
                        }
                        APPLICATION_UPDATING_SUCCESS -> {
                            Timber.tag(LogConstant.APP_CONFIRM)
                                .d("application update success")
                            requireActivity().toILoader().dismiss()
                            it.toApplicationRequest()?.let { appRequest ->
                                Timber.tag(LogConstant.APP_CONFIRM)
                                    .d("application request extracted")
                                showApplicationSuccessful(appRequest)
                            }
                        }
                        APPLICATION_UPDATING_FAILED -> {
                            Timber.tag(LogConstant.APP_CONFIRM)
                                .d("application update failed")
                            requireActivity().toILoader().dismiss()
                            requireContext().showToastLongDuration("Application updating failed")
                        }
                    }
                }
            }
        }
    }

    private fun setHeader(applicationRequest: ApplicationRequest) {
        Timber.tag(LogConstant.APP_CONFIRM).d("setting info customer name ${applicationRequest.customerName}r")
        Timber.tag(LogConstant.APP_CONFIRM).d("setting info application no ${applicationRequest.id}r")
        val customerName = requireContext().getString(
            R.string.application_confirm_customer_name,
            "${applicationRequest.customerName} ${applicationRequest.customerLastName} "
        )
        val applicationNo = requireContext().getString(
            R.string.application_confirm_application_no,
            applicationRequest.id.toString()
        )
        binding.header.text = customerName + applicationNo
    }

}