package com.hipla.channel.ui

import android.os.Bundle
import android.view.View
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
import com.hipla.channel.databinding.FragmentApplicationBinding
import com.hipla.channel.entity.*
import com.hipla.channel.extension.*
import com.hipla.channel.ui.adapter.SalesRecyclerAdapter
import com.hipla.channel.ui.decoration.SalesGridItemDecoration
import com.hipla.channel.viewmodel.ApplicationFlowViewModel
import com.hipla.channel.widget.OTPDialog
import kotlinx.coroutines.launch
import java.lang.ref.WeakReference

class SalesUseFragment : Fragment(R.layout.fragment_application) {

    private lateinit var viewModel: ApplicationFlowViewModel
    private lateinit var binding: FragmentApplicationBinding
    private lateinit var salesRecyclerAdapter: SalesRecyclerAdapter
    private var otpDialog: OTPDialog? = null

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
            viewModel.generateOTP(it)
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
        viewLifecycleOwner.lifecycleScope.launchSafely {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                observeSalesUserList()
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
                            APP_EVENT_START_APPLICATION_FLOW -> {
                                requireActivity().IActivityHelper().dismiss()
                                launchCustomerInfoFragment(it.toSalesUserId())
                            }
                            OTP_VERIFYING -> {
                                requireActivity().IActivityHelper().showLoader("Verifying OTP")
                            }
                            OTP_GENERATING -> {
                                requireActivity().IActivityHelper().showLoader("Generating OTP")
                            }
                            OTP_VERIFICATION_SUCCESS -> {
                                requireActivity().IActivityHelper().dismiss()
                            }
                            OTP_GENERATE_FAILED -> {
                                requireActivity().IActivityHelper().dismiss()
                                requireContext().showToastErrorMessage("OTP generation failed, Please try again")
                            }
                            OTP_SHOW_VERIFICATION_DIALOG -> {
                                showOTPDialog(it.toSalesUserId());
                            }
                            OTP_GENERATE_SUCCESS, OTP_GENERATE_COMPLETE, OTP_VERIFICATION_COMPLETE, APP_EVENT_APPLICATION_COMPLETE -> {
                                requireActivity().IActivityHelper().dismiss()
                            }
                            OTP_VERIFICATION_INVALID -> {
                                requireActivity().IActivityHelper().dismiss()
                                requireContext().showToastErrorMessage("Wrong OTP")
                            }
                            OTP_VERIFICATION_FAILED -> {
                                requireActivity().IActivityHelper().dismiss()
                                requireContext().showToastErrorMessage("Unable to verify, server error")
                            }
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
        if(otpDialog?.isShowing() != true) {
            otpDialog = OTPDialog(
                userId = salesUserId,
                dialogTitle = requireContext().getString(R.string.enter_otp),
                activityReference = WeakReference(requireActivity()),
                onSubmitListener = object : OTPDialog.OnOTPSubmitListener {
                    override fun onSubmit(otp: String) {
                        viewModel.verifyOtp(salesUserId, otp)
                    }
                }).show()
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