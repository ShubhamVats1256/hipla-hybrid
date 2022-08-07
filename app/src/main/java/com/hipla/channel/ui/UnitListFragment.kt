package com.hipla.channel.ui

import UnitSalesViewModel
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.hipla.channel.R
import com.hipla.channel.common.KEY_FLOW_CONFIG
import com.hipla.channel.common.KEY_SALES_USER_ID
import com.hipla.channel.common.LogConstant
import com.hipla.channel.databinding.FragmentUnitListBinding
import com.hipla.channel.entity.*
import com.hipla.channel.extension.*
import com.hipla.channel.ui.adapter.UnitListAdapter
import com.hipla.channel.ui.decoration.SalesGridItemDecoration
import timber.log.Timber

class UnitListFragment : Fragment(R.layout.fragment_unit_list) {

    private lateinit var viewModel: UnitSalesViewModel
    private lateinit var binding: FragmentUnitListBinding
    private lateinit var unitListAdapter: UnitListAdapter
    private var bookingConfirmationDialog: AlertDialog? = null

    private val scrollListener: RecyclerView.OnScrollListener =
        object : RecyclerView.OnScrollListener() {
            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                super.onScrollStateChanged(recyclerView, newState)
                if (recyclerView.canLoadNextGridPage(newState)) {
                    loadData()
                }
            }
        }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentUnitListBinding.bind(view)
        viewModel = ViewModelProvider(this)[UnitSalesViewModel::class.java]
        unitListAdapter = UnitListAdapter {
            // dev setting
            Timber.tag(LogConstant.UNIT).d("Unit selected: $it")
            //launchCustomerInfoFragment("105")
        }
        setRecyclerView()
        observeViewModel()
        loadData()
    }

    private fun setRecyclerView() {
        binding.unitRecyclerView.run {
            layoutManager = GridLayoutManager(
                requireContext(),
                6,
                RecyclerView.VERTICAL,
                false
            )
            adapter = unitListAdapter
            addItemDecoration(SalesGridItemDecoration())
            addOnScrollListener(scrollListener)
        }
    }

    private fun loadData() = viewModel.fetchUnits()

    private fun observeViewModel() {
        viewLifecycleOwner.lifecycleScope.launchSafely {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                observeUnitList()
                observeAppEvents()
            }
        }
    }

    private fun observeAppEvents() {
        viewLifecycleOwner.lifecycleScope.launchSafely {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                launchSafely {
                    viewModel.appEvent.collect {
                        when (it.id) {
                            UNIT_LIST_LOADING -> {

                            }
                            UNIT_LIST_SUCCESS -> {

                            }
                            UNIT_LIST_ERROR -> {

                            }
                        }
                    }
                }
            }
        }
    }

    private fun observeUnitList() {
        viewModel.unitListLiveData.observe(viewLifecycleOwner) {
            displaySalesUserList(it)
        }
    }

    private fun displaySalesUserList(unitInfoList: List<UnitInfo>) {
        unitListAdapter.append(unitInfoList)
    }

    private fun launchCustomerInfoFragment(salesUserId: String?) {
        salesUserId ?: return
        findNavController().run {
            if (isCurrentDestination(R.id.salesUserFragment)) {
                navigate(
                    resId = R.id.action_salesUserFragment_to_customerInfoFragment,
                    args = Bundle().apply {
                        putString(KEY_SALES_USER_ID, salesUserId)
                        putString(KEY_FLOW_CONFIG, arguments?.getString(KEY_FLOW_CONFIG))
                    }
                )
            }
        }
    }
}