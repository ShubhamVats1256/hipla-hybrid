package com.hipla.channel.ui

import androidx.lifecycle.ViewModelProvider
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.View
import com.hipla.channel.R
import com.hipla.channel.viewmodel.MainViewModel

class ApplicationFlowFragment : Fragment(R.layout.fragment_application) {

    private lateinit var viewModel: MainViewModel

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel = ViewModelProvider(this)[MainViewModel::class.java]
    }









}