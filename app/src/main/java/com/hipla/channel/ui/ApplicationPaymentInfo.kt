package com.hipla.channel.ui

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.hipla.channel.R
import com.hipla.channel.databinding.FragmentApplicationOtpConfirmBinding
import com.hipla.channel.databinding.FragmentApplicationPaymentInfoBinding
import com.hipla.channel.viewmodel.MainViewModel


class ApplicationPaymentInfo : Fragment(R.layout.fragment_application_payment_info) {

    private lateinit var viewModel: MainViewModel
    private lateinit var binding: FragmentApplicationPaymentInfoBinding

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentApplicationPaymentInfoBinding.bind(view)
        viewModel = ViewModelProvider(this)[MainViewModel::class.java]
    }
}