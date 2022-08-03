package com.hipla.channel.ui

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
import com.hipla.channel.common.KEY_SALES_USER_ID
import com.hipla.channel.databinding.DialogOtpConfirmBinding
import com.hipla.channel.databinding.FragmentApplicationBinding
import com.hipla.channel.entity.*
import com.hipla.channel.extension.*
import com.hipla.channel.ui.adapter.SalesRecyclerAdapter
import com.hipla.channel.ui.decoration.SalesGridItemDecoration
import com.hipla.channel.viewmodel.ApplicationFlowViewModel
import kotlinx.coroutines.launch


class SalesUseFragment : Fragment(R.layout.fragment_application) {

    private lateinit var viewModel: ApplicationFlowViewModel
    private lateinit var binding: FragmentApplicationBinding
    private lateinit var salesRecyclerAdapter: SalesRecyclerAdapter
    private var otpConfirmDialog: AlertDialog? = null

    private val scrollListener: RecyclerView.OnScrollListener =
        object : RecyclerView.OnScrollListener() {
            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                super.onScrollStateChanged(recyclerView, newState)
                if (recyclerView.canLoadNextGridPage(newState)) {
                    viewModel.loadUsers()
                }
            }
        }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentApplicationBinding.bind(view)
        viewModel = ViewModelProvider(this)[ApplicationFlowViewModel::class.java]
        salesRecyclerAdapter = SalesRecyclerAdapter {
            launchCustomerInfoFragment()
            //viewModel.generateOTP(it)
        }
        setRecyclerView()
        observeViewModel()
        loadData()
    }

    private fun setRecyclerView() {
        binding.salesRecyclerView.run {
            layoutManager = GridLayoutManager(
                requireContext(),
                4,
                RecyclerView.VERTICAL,
                false
            )
            adapter = salesRecyclerAdapter
            addItemDecoration(SalesGridItemDecoration())
            addOnScrollListener(scrollListener)
        }
    }

    private fun loadData() = viewModel.loadUsers()

    private fun observeViewModel() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                observeSalesUserList()
                observeAppEvents()
            }
        }
    }

    private fun observeAppEvents() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.appEvent.collect {
                    when (it.id) {
                        APP_EVENT_START_APPLICATION_FLOW -> {
                            requireActivity().toILoader().dismiss()
                            launchCustomerInfoFragment(it.toSalesUserId())
                        }
                        OTP_VERIFYING -> {
                            requireActivity().toILoader().showLoader("Verifying OTP")
                        }
                        OTP_GENERATING -> {
                            requireActivity().toILoader().showLoader("Generating OTP")
                        }
                        OTP_VERIFICATION_SUCCESS -> {
                            requireActivity().toILoader().dismiss()
                        }
                        OTP_GENERATE_FAILED -> {
                            requireActivity().toILoader().dismiss()
                            requireContext().showToastLongDuration("OTP generation failed, Please try again")
                        }
                        OTP_SHOW_VERIFICATION_DIALOG -> {
                            showOTPDialog(it.toSalesUserId());
                        }
                        OTP_GENERATE_COMPLETE, OTP_VERIFICATION_COMPLETE, APP_EVENT_APPLICATION_COMPLETE -> {
                            requireActivity().toILoader().dismiss()
                        }
                        OTP_VERIFICATION_INVALID -> {
                            requireActivity().toILoader().dismiss()
                            requireContext().showToastLongDuration("Wrong OTP")
                        }
                        OTP_VERIFICATION_FAILED -> {
                            requireActivity().toILoader().dismiss()
                            requireContext().showToastLongDuration("Unable to verify, server error")
                        }
                    }
                }
            }
        }
    }

    private fun observeSalesUserList() {
        viewModel.salesUsersLiveData.observe(viewLifecycleOwner) {
            displaySalesUserList(it)
        }
    }

    private fun displaySalesUserList(salesUserList: List<SalesUser>) {
        salesRecyclerAdapter.append(salesUserList)
    }

    private fun showOTPDialog(salesUserId: String?) {
        salesUserId ?: return
        if (requireActivity().isDestroyed.not()) {
            val dialogBuilder: AlertDialog.Builder = AlertDialog.Builder(requireContext())
            val dialogBinding = DialogOtpConfirmBinding.inflate(requireActivity().layoutInflater)
            dialogBuilder.setView(dialogBinding.root)
            dialogBinding.identification.text = salesUserId
            dialogBinding.submit.setOnClickListener {
                dialogBinding.otpEdit.takeIf { it.hasValidData() }?.let {
                }
            }
            dialogBinding.back.setOnClickListener {
                otpConfirmDialog?.dismiss()
            }
            dialogBinding.otpEdit.onSubmit {
                otpConfirmDialog?.dismiss()
                viewModel.verifyOtp(salesUserId, dialogBinding.otpEdit.content())
            }
            otpConfirmDialog = dialogBuilder.show()
            otpConfirmDialog?.setCancelable(false)
            otpConfirmDialog?.setCanceledOnTouchOutside(false)
        }
    }

    private fun launchCustomerInfoFragment(salesUserId: String? = "105") {
        salesUserId ?: return
        findNavController().run {
            if (isCurrentDestination(R.id.mainFragment)) {
                navigate(
                    resId = R.id.action_mainFragment_to_customerInfoFragment,
                    args = Bundle().apply {
                        putString(KEY_SALES_USER_ID, salesUserId)
                    })
            }
        }
    }
}