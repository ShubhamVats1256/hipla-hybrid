package com.hipla.channel.ui

import UnitsViewModel
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
import com.hipla.channel.BuildConfig
import com.hipla.channel.R
import com.hipla.channel.common.KEY_FLOW_CONFIG
import com.hipla.channel.common.KEY_SALES_USER_ID
import com.hipla.channel.common.KEY_UNIT
import com.hipla.channel.common.LogConstant
import com.hipla.channel.common.Utils.hide
import com.hipla.channel.common.Utils.show
import com.hipla.channel.databinding.FragmentUnitListBinding
import com.hipla.channel.entity.UNIT_LIST_ERROR
import com.hipla.channel.entity.UNIT_LIST_LOADING
import com.hipla.channel.entity.UNIT_LIST_SUCCESS
import com.hipla.channel.entity.UnitInfo
import com.hipla.channel.extension.*
import com.hipla.channel.foodModule.network.NetworkService
import com.hipla.channel.foodModule.network.Networking
import com.hipla.channel.foodModule.repository.CommonFactory
import com.hipla.channel.foodModule.repository.CommonRepository
import com.hipla.channel.foodModule.viewmodel.AllPantryViewModel
import com.hipla.channel.ui.adapter.UnitListAdapter
import com.hipla.channel.viewmodel.UnitAvailabiltyViewModel
import timber.log.Timber


class UnitListFragment : Fragment(R.layout.fragment_unit_list) {

    private lateinit var viewModel: UnitsViewModel
    private lateinit var binding: FragmentUnitListBinding
    private lateinit var unitListAdapter: UnitListAdapter
    private lateinit var unitInfoToSend: UnitInfo
    private var bookingConfirmationDialog: AlertDialog? = null
    private lateinit var unitAvailabiltyViewModel: UnitAvailabiltyViewModel

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
        viewModel = ViewModelProvider(this)[UnitsViewModel::class.java]
        viewModel.extractArguments(arguments)
        binding.header.text = buildString {
            append("Units in block ")
            append(viewModel.selectedFloorInfo.name)
        }
        unitListAdapter = UnitListAdapter(requireContext()) {
            Timber.tag(LogConstant.UNIT).d("Unit selected: $it")
            unitInfoToSend = it
            unitAvailabiltyViewModel.checkUnitAvailabilty("business/v1/unit/checkstatus/"+it.id)
            binding.unitListLoader.show()

          //  showBookingConfirmationDialog(it)
        }

        val networkService = Networking.create(BuildConfig.BASE_URL,requireContext())

        if (networkService != null) {
            setUpViewModel(networkService)
        }
        setRecyclerView()
        observeViewModel()
        loadData()
    }

    private fun setUpViewModel(networkService : NetworkService){
        unitAvailabiltyViewModel = ViewModelProvider(
            this,
            CommonFactory(CommonRepository(networkService))
        )[UnitAvailabiltyViewModel::class.java]

        //    pantryViewModel = ViewModelProvider(requireActivity())[AllPantryViewModel::class.java]

    }

    private fun setRecyclerView() {
        binding.unitRecyclerView.run {
            layoutManager = GridLayoutManager(
                requireContext(), 6,
                RecyclerView.VERTICAL,
                false
            )
            adapter = unitListAdapter
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
            if (viewModel.unitMasterList.isNotEmpty()) {
                displayUnits(viewModel.unitMasterList)
            }
        }
    }

    private fun loadData() = viewModel.fetchUnits("business/v1/unit/by?buildingId="+viewModel.selectedFloorInfo.id)

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
            displayUnits(it)
        }

        unitAvailabiltyViewModel.errorMessage.observe(viewLifecycleOwner) {
            requireContext().showToastErrorMessage(it)
            binding.unitListLoader.hide()
        }

        unitAvailabiltyViewModel.unitAvailabiltyData.observe(viewLifecycleOwner) {

            try {
                binding.unitListLoader.hide()
                if (it.record.bookingStatus == "AVAILABLE"){
                    showBookingConfirmationDialog(unitInfoToSend)
                    unitAvailabiltyViewModel.unitAvailabiltyData.postValue(null)

                }
                else{
                    requireContext().showToastErrorMessage(it.record.bookingStatus)
                }
            }
            catch (e :Exception){}




        }


        viewModel.error.observe(viewLifecycleOwner) {
            binding.unitListLoader.hide()
            binding.ivNoMeeting.show()
            binding.unitRecyclerView.hide()
        }




    }

    private fun displayUnits(unitInfoList: List<UnitInfo>) {
        if (unitListAdapter.isListAlreadyAppended(unitInfoList).not()) {
            unitListAdapter.append(unitInfoList)
            binding.unitRecyclerView.show()
            binding.unitListLoader.hide()
        }
    }

    private fun showBookingConfirmationDialog(unitInfo: UnitInfo) {
        AlertDialog.Builder(requireContext())
            .setTitle("${unitInfo.name} selected")
            .setMessage("Are you sure you want to continue booking ${unitInfo.name}") // Specifying a listener allows you to take an action before dismissing the dialog.
            .setPositiveButton(
                android.R.string.yes
            ) { _, _ ->
                launchCustomerInfoFragment(unitInfo)
                null
            }
            .setNegativeButton(android.R.string.no, null)
            .show()
    }

    private fun launchCustomerInfoFragment(unitInfo: UnitInfo) {
        findNavController().run {
            if (isCurrentDestination(R.id.unitListFragment)) {
                navigate(
                    resId = R.id.action_unitListFragment_to_customerInfoFragment,
                    args = Bundle().apply {
                        putString(KEY_SALES_USER_ID, arguments?.getString(KEY_SALES_USER_ID))
                        putString(KEY_UNIT, unitInfo.toJsonString())
                        putString(KEY_FLOW_CONFIG, arguments?.getString(KEY_FLOW_CONFIG))
                    }
                )
            }
        }
    }
}