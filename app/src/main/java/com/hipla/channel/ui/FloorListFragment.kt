package com.hipla.channel.ui

import FloorListViewModel
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.hipla.channel.R
import com.hipla.channel.common.*
import com.hipla.channel.common.Utils.hide
import com.hipla.channel.common.Utils.show
import com.hipla.channel.databinding.FragmentFloorListBinding
import com.hipla.channel.entity.*
import com.hipla.channel.extension.canLoadNextGridPage
import com.hipla.channel.extension.isCurrentDestination
import com.hipla.channel.extension.launchSafely
import com.hipla.channel.extension.toJsonString
import com.hipla.channel.ui.adapter.FloorListAdapter
import timber.log.Timber

class FloorListFragment : Fragment(R.layout.fragment_floor_list) {

    private lateinit var viewModel: FloorListViewModel
    private lateinit var binding: FragmentFloorListBinding
    private lateinit var floorListAdapter : FloorListAdapter

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
        binding = FragmentFloorListBinding.bind(view)
        viewModel = ViewModelProvider(this)[FloorListViewModel::class.java]
        viewModel.extractArguments(arguments)
        floorListAdapter = FloorListAdapter(requireContext()) {
            Timber.tag(LogConstant.UNIT).d("floor selected: ${it.floorId}")
            showFloorConfirmationDialog(it)
        }
        setRecyclerView()
        observeViewModel()
        loadData()
    }

    private fun setRecyclerView() {
        binding.floorRecyclerView.run {
            layoutManager = GridLayoutManager(
                requireContext(), 4,
                RecyclerView.VERTICAL,
                false
            )
            adapter = floorListAdapter
            addOnScrollListener(scrollListener)
            addItemDecoration(
                DividerItemDecoration(
                    requireContext(),
                    DividerItemDecoration.HORIZONTAL
                )
            )
            addItemDecoration(
                DividerItemDecoration(
                    requireContext(),
                    DividerItemDecoration.VERTICAL
                )
            )
            if (viewModel.floorMasterList.isNotEmpty()) {
                displayFloors(viewModel.floorMasterList)
            }
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
        viewModel.floorListLiveData.observe(viewLifecycleOwner) {
            displayFloors(it)
        }
    }

    private fun displayFloors(floorInfoList: List<FloorInfo>) {
        if (floorListAdapter.isListAlreadyAppended(floorInfoList).not()) {
            floorListAdapter.append(floorInfoList)
            binding.floorRecyclerView.show()
            binding.floorListLoader.hide()
        }
    }

    private fun showFloorConfirmationDialog(floorInfo: FloorInfo) {
        AlertDialog.Builder(requireContext())
            .setTitle("Floor ${floorInfo.floorId} selected")
            .setMessage("Are you sure you want to continue?") // Specifying a listener allows you to take an action before dismissing the dialog.
            .setPositiveButton(
                android.R.string.yes
            ) { _, _ ->
                launchUnitInfoFragment(floorInfo)
            }
            .setNegativeButton(android.R.string.no, null)
            .show()
    }

    private fun launchUnitInfoFragment(floorInfo: FloorInfo) {
        findNavController().run {
            if (isCurrentDestination(R.id.floorListFragment)) {
                navigate(
                    resId = R.id.action_floorListFragment_to_unitListFragment,
                    args = Bundle().apply {
                        putString(KEY_SALES_USER_ID, arguments?.getString(KEY_SALES_USER_ID))
                        putString(KEY_FLOOR, floorInfo.toJsonString())
                        putString(KEY_FLOW_CONFIG, arguments?.getString(KEY_FLOW_CONFIG))
                    }
                )
            }
        }
    }
}