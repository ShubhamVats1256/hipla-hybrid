package com.hipla.channel.ui

import android.os.Bundle
import android.view.KeyEvent.ACTION_UP
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.hipla.channel.R
import com.hipla.channel.databinding.FragmentApplicationCustomerInfoBinding
import com.hipla.channel.entity.generateFloors
import com.hipla.channel.viewmodel.ApplicationFlowViewModel


class ApplicationCustomerInfoFragment : Fragment(R.layout.fragment_application_customer_info) {

    private lateinit var viewModel: ApplicationFlowViewModel
    private lateinit var binding: FragmentApplicationCustomerInfoBinding
    private var floorList = generateFloors();

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentApplicationCustomerInfoBinding.bind(view)
        viewModel = ViewModelProvider(this)[ApplicationFlowViewModel::class.java]
        setFloorPreference()
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
            onItemClickListener = AdapterView.OnItemClickListener { parent, view, position, id ->

            }
        }
    }

}