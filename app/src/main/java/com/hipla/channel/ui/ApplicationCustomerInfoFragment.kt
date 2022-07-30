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
import com.hipla.channel.screenHeightInDp
import com.hipla.channel.screenWidthDp
import com.hipla.channel.ui.adapter.SalesRecyclerAdapter
import com.hipla.channel.ui.decoration.SalesGridItemDecoration
import com.hipla.channel.viewmodel.MainViewModel

class ApplicationCustomerInfoFragment : Fragment(R.layout.fragment_application) {

    private lateinit var viewModel: MainViewModel
    private lateinit var binding: FragmentApplicationBinding
    private lateinit var salesRecyclerAdapter: SalesRecyclerAdapter

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentApplicationBinding.bind(view)
        viewModel = ViewModelProvider(this)[MainViewModel::class.java]
        displayGrid()
    }

    private fun displayGrid() {
        val salesUserList = Utils.getSampleSalesUserList()
        salesRecyclerAdapter = SalesRecyclerAdapter(salesUserList) {
            Toast.makeText(requireContext(), "item clicked $it", Toast.LENGTH_LONG).show()
        }
        val salesGridItemInDp: Int =
            (resources.getDimension(R.dimen.sales_grid_item_width) / resources.displayMetrics.density).toInt()
        Log.d(LogConstant.FLOW_APP, " sales grid item in dp :$salesGridItemInDp")
        Log.d(LogConstant.FLOW_APP, "screen width in dp :${requireContext().screenHeightInDp()}")
        val numColumns: Int = (requireContext().screenWidthDp() / salesGridItemInDp)
        Log.d(LogConstant.FLOW_APP, " screen width :${requireContext().screenWidthDp()}")
        Toast.makeText(requireContext(), "$numColumns", Toast.LENGTH_LONG).show()
        binding.salesRecyclerView.run {
            layoutManager = GridLayoutManager(requireContext(), 3, RecyclerView.VERTICAL, false)
            adapter = salesRecyclerAdapter
            addItemDecoration(SalesGridItemDecoration())
        }
    }

}