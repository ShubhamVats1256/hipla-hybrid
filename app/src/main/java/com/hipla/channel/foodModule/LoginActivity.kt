package com.hipla.channel.foodModule

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.ScrollView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.github.pwittchen.reactivenetwork.library.rx2.ReactiveNetwork.checkInternetConnectivity
import com.google.android.material.snackbar.Snackbar
import com.hipla.channel.BuildConfig
import com.hipla.channel.MainActivity
import com.hipla.channel.R
import com.hipla.channel.foodModule.network.Networking
import com.hipla.channel.foodModule.network.request.LoginRequest
import com.hipla.channel.foodModule.network.response.VipDataBase
import com.hipla.channel.foodModule.repository.CommonFactory
import com.hipla.channel.foodModule.repository.CommonRepository
import com.hipla.channel.foodModule.utils.KeyboardHideShow
import com.hipla.channel.foodModule.utils.PrefUtils
import com.hipla.channel.foodModule.viewmodel.LoginViewModel
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.*

class LoginActivity : AppCompatActivity() {

    private lateinit var btnLogin: Button
    private lateinit var etPasskey : EditText
    private lateinit var pbLogin: ProgressBar
    private lateinit var parentView: ScrollView

    private lateinit var loginViewModel: LoginViewModel
    lateinit var sharedPreference: PrefUtils


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        sharedPreference = PrefUtils(this)
       if(sharedPreference.getApiKey()!!.isNotEmpty()) {
                    val intent = Intent(this, MainActivity::class.java)
                    startActivity(intent)
                    finish()
        }
        setContentView(R.layout.activity_login)

        setUpUI()
        setUpViewModel()
        getData()
        errorObserver()
        loadingState()
    }

    private fun setUpUI() {
        btnLogin = findViewById(R.id.btn_login)
        etPasskey = findViewById(R.id.et_passcode)
        parentView = findViewById(R.id.scrollview_login)
        pbLogin = findViewById(R.id.pb_login)

        btnLogin.setOnClickListener {
            KeyboardHideShow.hideKeyboard(it, this)
            if (etPasskey.text.trim().isNotEmpty()) {
                prepareLogin()
            } else {
                showSnackBar("Passkey can not be empty ")
            }
        }
    }
    private fun setUpViewModel() {
         val networkService = Networking.create(BuildConfig.BASE_URL,this)
        loginViewModel = ViewModelProvider(
            this,
            CommonFactory(CommonRepository(networkService))
        ).get(LoginViewModel::class.java)
    }

    private fun prepareLogin() {
        val loginRequest = LoginRequest(
            UUID.randomUUID().toString(),
            "Tablet",
            etPasskey.text.trim().toString(),
            "Tablet"
        )
        login(loginRequest)
    }

    @SuppressLint("CheckResult")
    private fun login(login_request: LoginRequest) {

//        val single: Single<Boolean> = checkInternetConnectivity()
//        single
//            .subscribeOn(Schedulers.io())
//            .observeOn(AndroidSchedulers.mainThread())
//            .subscribe { isConnectedToInternet ->
//                if (isConnectedToInternet) {
                    loginViewModel.getLoginData(login_request)
//                } else {
//                    showSnackBar("No Internet Connection Available")
//                }
          //  }
    }

    private fun getData() {
        loginViewModel.loginData.observe(this) {
            it?.let {
                if (it.status == "success") {
                    if (it.data?.deviceCategory == "insideVIPRoom") {
                        it.data!!.apiKey?.let { it1 ->
                            sharedPreference.saveLoginData(
                                it1
                            )
                        }

                        it.data!!.employeeId?.let { it1 ->
                            sharedPreference.saveEmployeeId(
                                it1
                            )
                        }

                        fetchRoomDetails()






                    } else {
                        showSnackBar("You have not permitted for entry")
                    }
                } else {
                    it.message?.let { it1 -> showSnackBar(it1) }
                }
            }
        }
    }


    private fun errorObserver() {
        loginViewModel.errorMessage.observe(this, Observer {
            it?.let {
                showSnackBar(it)
            }
        })
    }

    private fun fetchRoomDetails() {
        val request = Networking.create(BuildConfig.BASE_URL,this)

        sharedPreference.getOrganizationId()?.let {
            request!!.fetchVipDetails(
                BuildConfig.BASE_URL+"v1/employee/mobile/vipRoom/"+sharedPreference.getEmployeeId(),
                sharedPreference.getApiKey()!!
            )
        }
            ?.enqueue(object : Callback<VipDataBase> {
                override fun onResponse(
                    call: Call<VipDataBase>,
                    response: Response<VipDataBase>
                ) {
                    try {
                        if (response.isSuccessful) {
                            response.body()?.let {
                                if (it.status == "success"){


                                    try {
                                        sharedPreference.saveOrgId(it.data.organizationId)
                                        sharedPreference.savebusinessId(it.data.businessId)


                                        val intent = Intent(this@LoginActivity, MainActivity::class.java)
                                        startActivity(intent)
                                        finish()

                                    }
                                    catch (e : Exception){
                                    }



                                }
                                else{
                                }}
                        }
                        else{
                        }
                    }
                    catch (e : Exception){
                    }
                }
                override fun onFailure(call: Call<VipDataBase>, t: Throwable) {
                }
            })
    }


    private fun loadingState() {
        loginViewModel.loading.observe(this, Observer {
            if (it) {
                pbLogin.visibility = View.VISIBLE
            } else
                pbLogin.visibility = View.GONE
        })
    }

    private fun showSnackBar(message: String) {
        Snackbar.make(
            parentView,
            message,
            Snackbar.LENGTH_LONG
        )
            .show()
    }
}