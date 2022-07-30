package com.hipla.channel.ui

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.hipla.channel.R
import com.hipla.channel.common.LogConstant
import com.hipla.channel.common.Utils
import com.hipla.channel.databinding.FragmentApplicationBinding
import com.hipla.channel.databinding.FragmentApplicationCustomerInfoBinding
import com.hipla.channel.screenHeightInDp
import com.hipla.channel.screenWidthDp
import com.hipla.channel.ui.adapter.SalesRecyclerAdapter
import com.hipla.channel.ui.decoration.SalesGridItemDecoration
import com.hipla.channel.viewmodel.MainViewModel

class ApplicationCustomerInfoFragment : Fragment(R.layout.fragment_application_customer_info) {

    private lateinit var viewModel: MainViewModel
    private lateinit var binding: FragmentApplicationCustomerInfoBinding

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentApplicationCustomerInfoBinding.bind(view)
        viewModel = ViewModelProvider(this)[MainViewModel::class.java]
    }

}