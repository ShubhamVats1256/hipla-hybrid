package com.hipla.channel.ui

import android.R
import android.app.Dialog
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.*
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider

import com.hipla.channel.databinding.FragmentDashboardBinding
import com.hipla.channel.entity.PushOtpRequest
import com.hipla.channel.entity.response.GenerateOtpRequestBase
import com.hipla.channel.extension.showToastErrorMessage
import com.hipla.channel.foodModule.network.EndPoints
import com.hipla.channel.foodModule.network.Networking
import com.hipla.channel.foodModule.repository.CommonFactory
import com.hipla.channel.foodModule.repository.CommonRepository
import com.hipla.channel.foodModule.utils.PrefUtils
import com.hipla.channel.foodModule.utils.setOnClickListener
import com.hipla.channel.viewmodel.GenerateOtpViewmodel
import com.mukesh.OtpView
import java.util.*
import com.hipla.channel.BuildConfig
import com.hipla.channel.MainActivity
import com.hipla.channel.extension.IActivityHelper
import com.hipla.channel.widget.OTPDialog
import java.lang.ref.WeakReference

class DashboardFragement : Fragment() {

    private var binding : FragmentDashboardBinding? = null
    private lateinit var sharedPreference: PrefUtils
    private var otpDialog: OTPDialog? = null
    private lateinit var generateOtpViewmodel: GenerateOtpViewmodel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requireActivity().window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        sharedPreference = PrefUtils(requireContext())



        // Inflate the layout for this fragment
        binding = FragmentDashboardBinding.inflate(layoutInflater)
        requireActivity().IActivityHelper().setTitle("Feedback")
        return  binding!!.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        sharedPreference.clearSharedPreference()
        setUpViewModel()
        getData()

        binding!!.tvCancel.setOnClickListener {
            val intent = Intent(activity, MainActivity::class.java)
            intent.addFlags(
                Intent.FLAG_ACTIVITY_CLEAR_TASK
                        or Intent.FLAG_ACTIVITY_NEW_TASK
            )
            startActivity(intent)
        }


        binding!!.tvNext.setOnClickListener(5000) {

            if (binding!!.etMobileNumber.text!!.length<10){
                binding!!.etMobileNumber.error = "Please enter valid number"
                showSnackBar("Please enter valid number")
                return@setOnClickListener
            }
            else{
                binding!!.tvNext.isEnabled = false
                binding!!.tvNext.isClickable = false
                sharedPreference.saveUserMobileNumber( binding!!.etMobileNumber.text.toString())
                val manualSubmitRequest = GenerateOtpRequestBase( binding!!.etMobileNumber.text.toString())
                generateOtpViewmodel.generateOtp(manualSubmitRequest)
            }

        }

    }


    private fun setUpViewModel(){
        val networkService = Networking.create(BuildConfig.BASE_URL,requireContext())

        generateOtpViewmodel = ViewModelProvider(
            this,
            CommonFactory(CommonRepository(networkService))
        ).get(GenerateOtpViewmodel::class.java)

    }

    private fun getData() {
        generateOtpViewmodel.generateOtpData.observe(viewLifecycleOwner) {
            it?.let {
                binding!!.tvNext.isEnabled = true
                binding!!.tvNext.isClickable = true

                try {
                    sharedPreference.saveReferenceId(it.referenceId,it.record.id)

                    showOTPDialog("212")
                    //verifyOtpDialog()



                }
                catch (e :Exception){
                    showSnackBar(e.localizedMessage)
                }

            }
        }

        generateOtpViewmodel.errorMessage.observe(viewLifecycleOwner) {
            binding!!.tvNext.isEnabled = true
            binding!!.tvNext.isClickable = true
            it?.let {
                try {
                    showSnackBar(it)
                }
                catch (e :Exception){
                    showSnackBar(e.localizedMessage)
                }

            }
        }


        generateOtpViewmodel.submiteOtpData.observe(viewLifecycleOwner) {
            it?.let {
                try {
                    if (it.status == "success"){

                        activity?.supportFragmentManager!!.beginTransaction().replace(
                            com.hipla.channel.R.id.navHost,
                            FeedbackFormFragment()
                        ).commit()


//                        if (nextScreen == "USER_ROLE"){
//                            requireActivity().supportFragmentManager.beginTransaction().replace(R.id.rl_main, SelectRoleFragment()).commit()
//                        }
//                        else   if (nextScreen == "EXISTING_USER"){
//                            requireActivity().supportFragmentManager.beginTransaction().replace(R.id.rl_main, ExistingCustomerFragment()).commit()
//                        }
//                        else{
//                            showSnackBar("No Next Screen found")
//                        }


                    }
                    else{

                        showSnackBar(it.message)
                        // showDialog()
                    }
                }
                catch (e :Exception){
                    showSnackBar(e.localizedMessage)
                }
            }
        }

        generateOtpViewmodel.submiteOtperrorMessage.observe(viewLifecycleOwner) {
            showSnackBar(it)
        }
    }




    override fun onDestroyView() {
        super.onDestroyView()

        binding = null
    }


    private fun showOTPDialog(salesUserId: String?) {
        salesUserId ?: return
        if (otpDialog?.isShowing() != true) {
            otpDialog = OTPDialog(
                userId = salesUserId,
                dialogTitle = requireContext().getString(com.hipla.channel.R.string.enter_otp),
                activityReference = WeakReference(requireActivity()),
                onSubmitListener = object : OTPDialog.OnOTPSubmitListener {
                    override fun onSubmit(otp: String) {


                            val manualSubmitRequest = PushOtpRequest( otp,
                                sharedPreference.getReferenceId()!!, sharedPreference.getUserId()!!
                            )
                            generateOtpViewmodel.submitOtp(manualSubmitRequest)




                    }
                }).show()
        }
    }


    private fun showSnackBar(message: String) {
        requireContext().showToastErrorMessage(message)
    }


}
