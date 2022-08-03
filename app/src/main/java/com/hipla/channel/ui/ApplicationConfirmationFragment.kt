package com.hipla.channel.ui

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.hipla.channel.R
import com.hipla.channel.databinding.FragmentApplicationCustomerInfoBinding
import com.hipla.channel.entity.*
import com.hipla.channel.extension.toILoader
import com.hipla.channel.viewmodel.ApplicationConfirmationViewmModel
import kotlinx.coroutines.launch


class ApplicationConfirmationFragment : Fragment(R.layout.fragment_application_confirm) {

    private lateinit var viewModel: ApplicationConfirmationViewmModel
    private lateinit var binding: FragmentApplicationCustomerInfoBinding

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentApplicationCustomerInfoBinding.bind(view)
        viewModel = ViewModelProvider(this)[ApplicationConfirmationViewmModel::class.java]
        viewModel.extractArguments(arguments);
        observeViewModel()
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
                    }
                }
            }
        }
    }

}