package com.hipla.channel.ui

import android.app.Activity.RESULT_OK
import android.content.ActivityNotFoundException
import android.content.Intent
import android.graphics.Bitmap
import android.os.Bundle
import android.provider.MediaStore
import android.util.DisplayMetrics
import android.view.View
import android.view.WindowManager
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
import com.hipla.channel.common.LogConstant
import com.hipla.channel.databinding.DialogOtpConfirmBinding
import com.hipla.channel.databinding.DialogUploadPhotoBinding
import com.hipla.channel.databinding.FragmentApplicationBinding
import com.hipla.channel.entity.SalesUser
import com.hipla.channel.extension.*
import com.hipla.channel.ui.adapter.SalesRecyclerAdapter
import com.hipla.channel.ui.decoration.SalesGridItemDecoration
import com.hipla.channel.viewmodel.ApplicationFlowViewModel
import kotlinx.coroutines.launch
import timber.log.Timber


class ApplicationFlowFragment : Fragment(R.layout.fragment_application) {

    private lateinit var viewModel: ApplicationFlowViewModel
    private lateinit var binding: FragmentApplicationBinding
    private lateinit var salesRecyclerAdapter: SalesRecyclerAdapter
    var otpConfirmDialog: AlertDialog? = null
    var uploadChequeDialog: AlertDialog? = null

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
            // viewModel.generateOTP(it)
            // showOTPDialog(it)
            //launchCustomerInfoFragment(it)
            takePicture()
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

    private fun showOTPDialog(salesUser: SalesUser) {
        if (requireActivity().isDestroyed.not()) {
            val dialogBuilder: AlertDialog.Builder = AlertDialog.Builder(requireContext())
            val dialogBinding = DialogOtpConfirmBinding.inflate(requireActivity().layoutInflater)
            dialogBuilder.setView(dialogBinding.root)
            dialogBinding.identification.text = salesUser.id.toString()
            dialogBinding.submit.setOnClickListener {
                //requireActivity().toILoader().showLoader("Verifying")
                dialogBinding.otpEdit.takeIf { it.hasValidData() }?.let {
                }
            }
            dialogBinding.back.setOnClickListener {
                otpConfirmDialog?.dismiss()
            }
            dialogBinding.otpEdit.onSubmit {
                otpConfirmDialog?.dismiss()
                viewModel.verifyOtp(salesUser, dialogBinding.otpEdit.content())
            }
            otpConfirmDialog = dialogBuilder.show()
            otpConfirmDialog?.setCancelable(false)
            otpConfirmDialog?.setCanceledOnTouchOutside(false)
        }
    }

    private fun launchCustomerInfoFragment(salesUser: SalesUser) {
        findNavController().run {
            if (isCurrentDestination(R.id.mainFragment)) {
                navigate(
                    resId = R.id.action_mainFragment_to_customerInfoFragment,
                    args = Bundle().apply {
                        putString(KEY_SALES_USER_ID, salesUser.id.toString())
                    })
            }
        }
    }

    private fun showUploadChequeDialog(bitmap: Bitmap) {
        if (requireActivity().isDestroyed.not()) {
            val dialogBuilder: AlertDialog.Builder = AlertDialog.Builder(requireContext())
            val dialogBinding = DialogUploadPhotoBinding.inflate(requireActivity().layoutInflater)
            dialogBuilder.setView(dialogBinding.root)
            dialogBinding.chequePhoto.setImageBitmap(bitmap)
            dialogBinding.upload.setOnClickListener {
            }
            dialogBinding.close.setOnClickListener {
                uploadChequeDialog?.dismiss()
            }
            uploadChequeDialog = dialogBuilder.show()
            val displayMetrics = DisplayMetrics()
            requireActivity().windowManager.getDefaultDisplay().getMetrics(displayMetrics)
            val displayWidth = displayMetrics.widthPixels
            val displayHeight = displayMetrics.heightPixels
            val layoutParams = WindowManager.LayoutParams()
            layoutParams.copyFrom(uploadChequeDialog?.window?.attributes)
            layoutParams.width = ((displayWidth * 0.5f).toInt())
            layoutParams.height = ((displayHeight * 1f).toInt())
            uploadChequeDialog?.window?.attributes = layoutParams
            uploadChequeDialog?.setCancelable(false)
            uploadChequeDialog?.setCanceledOnTouchOutside(false)
        }
    }

    private fun takePicture() {
        Timber.tag(LogConstant.FLOW_APP).d("capture image")
        val takePictureIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        //takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, fileUri);

        try {
            startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE)
        } catch (e: ActivityNotFoundException) {
            requireContext().ShowToastLongDuration("This device does not have camera application to proceed")
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        Timber.tag(LogConstant.FLOW_APP).d("on activity result")
        if (requestCode == REQUEST_IMAGE_CAPTURE) {
            if (resultCode == RESULT_OK) {
                Timber.tag(LogConstant.FLOW_APP).d("picture taken successfully")
                val bitmap = data?.extras?.get("data") as Bitmap
                showUploadChequeDialog(bitmap)
            } else {
                requireContext().ShowToastLongDuration("Photo capture cancelled")
            }
        }
    }

    companion object {
        const val REQUEST_IMAGE_CAPTURE = 1000
    }

}