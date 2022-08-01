package com.hipla.channel.ui

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.hipla.channel.R
import com.hipla.channel.databinding.FragmentApplicationOtpConfirmBinding
import com.hipla.channel.viewmodel.ApplicationFlowViewModel


class ApplicationOTPConfirmFragment : Fragment(R.layout.fragment_application_otp_confirm) {

    private lateinit var viewModel: ApplicationFlowViewModel
    private lateinit var binding: FragmentApplicationOtpConfirmBinding

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentApplicationOtpConfirmBinding.bind(view)
        viewModel = ViewModelProvider(this)[ApplicationFlowViewModel::class.java]
    }

}