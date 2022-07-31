package com.hipla.channel.ui

import android.os.Bundle
import android.view.KeyEvent.ACTION_UP
import android.view.View
import android.widget.ArrayAdapter
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.hipla.channel.R
import com.hipla.channel.databinding.FragmentApplicationCustomerInfoBinding
import com.hipla.channel.viewmodel.MainViewModel


class ApplicationCustomerInfoFragment : Fragment(R.layout.fragment_application_customer_info) {

    private lateinit var viewModel: MainViewModel
    private lateinit var binding: FragmentApplicationCustomerInfoBinding
    private var floorList =
        arrayOf("First floor", "Second floor", "Third floor", "Fourth floor", "First floor", "Second floor", "Third floor", "Fourth floor")

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentApplicationCustomerInfoBinding.bind(view)
        viewModel = ViewModelProvider(this)[MainViewModel::class.java]
        setFloorPreference()
    }

    private fun setFloorPreference() {
        val floorAdapter: ArrayAdapter<String> =
            ArrayAdapter(requireContext(), R.layout.autocomplete_list_item, floorList)
        binding.floorPreference.run {
            setAdapter(floorAdapter)
            setOnTouchListener { _, motionEvent ->
                if (motionEvent.action == ACTION_UP) {
                    showDropDown()
                }
                return@setOnTouchListener false;
            }
        }
    }

}