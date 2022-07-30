package com.hipla.channel.ui

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.hipla.channel.*
import com.hipla.channel.common.Utils
import com.hipla.channel.databinding.FragmentApplicationBinding
import com.hipla.channel.ui.adapter.SalesGridAdapter
import com.hipla.channel.viewmodel.MainViewModel


class ApplicationFlowFragment : Fragment(R.layout.fragment_application) {

    private lateinit var viewModel: MainViewModel
    private lateinit var binding: FragmentApplicationBinding
    private lateinit var salesGridAdapter: SalesGridAdapter

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentApplicationBinding.bind(view)
        viewModel = ViewModelProvider(this)[MainViewModel::class.java]
        displayGrid()
    }

    private fun displayGrid() {
        val salesUserList = Utils.getSampleSalesUserList()
        salesGridAdapter  = SalesGridAdapter(requireContext(), salesUserList);
        val salesGridItemInDp : Int = (resources.getDimension(R.dimen.sales_grid_item_width) / resources.displayMetrics.density).toInt()
        Log.d("testfx", " sales grid item in dp :$salesGridItemInDp")
        Log.d("testfx", "screen width in dp :${requireContext().screenHeightInDp()}")
        val numColumns: Int = (requireContext().screenWidthDp() / salesGridItemInDp)
        Log.d("testfx", " screen width :${requireContext().screenWidthDp()}")
        Toast.makeText(requireContext(), "$numColumns", Toast.LENGTH_LONG).show()
        binding.salesGridView.run {
            adapter = salesGridAdapter
        }
    }

}