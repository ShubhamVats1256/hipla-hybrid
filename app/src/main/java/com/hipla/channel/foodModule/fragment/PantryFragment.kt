package com.hipla.channel.foodModule.fragment

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.snackbar.Snackbar
import com.hipla.channel.BuildConfig
import com.hipla.channel.R
import com.hipla.channel.extension.IActivityHelper
import com.hipla.channel.foodModule.adapter.PantryListAdapter
import com.hipla.channel.foodModule.network.NetworkService
import com.hipla.channel.foodModule.network.Networking
import com.hipla.channel.foodModule.network.response.AllPantryResponseData
import com.hipla.channel.foodModule.repository.CommonFactory
import com.hipla.channel.foodModule.repository.CommonRepository
import com.hipla.channel.foodModule.utils.PrefUtils
import com.hipla.channel.foodModule.viewmodel.AllPantryViewModel
import com.hipla.channel.viewmodel.SalesUserViewModel
import com.hipla.sentinelvms.sentinelKt.foodModule.network.request.AllPantryRequest
import com.hipla.sentinelvms.sentinelKt.foodModule.network.request.EmployeeId
import com.hipla.sentinelvms.sentinelKt.foodModule.network.request.Sort
import com.hipla.sentinelvms.sentinelKt.foodModule.utility.PaginationScrollListener

class PantryFragment : Fragment() {

    private lateinit var rvPantryList : RecyclerView
    private lateinit var pbPantry : ProgressBar
    private lateinit var parentView : ConstraintLayout
    private var pantryListData: ArrayList<AllPantryResponseData> = ArrayList()
    private lateinit var viewModel: SalesUserViewModel
    private lateinit var pantryViewModel: AllPantryViewModel
    private lateinit var pantryListAdapter: PantryListAdapter
    private val searchList = listOf<String>()
    private var skip: Int = 0
    private var isLoading: Boolean = false
    private var isLastPage: Boolean = false
    private var totalPages: Int = 1
    lateinit var sharedPreference : PrefUtils
    private var take: Int = 10

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        sharedPreference = PrefUtils(requireContext())

        val networkService = Networking.create(BuildConfig.BASE_URL,requireContext())

        if (networkService != null) {
            setUpViewModel(networkService)
        }

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_pantry, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel = ViewModelProvider(this)[SalesUserViewModel::class.java]
        setUpUI(view)
        setAdapter()

        //getData()
        getDefaultPantryData()
        setData()
        setPaginationData()
        loadingState()
        errorObserver()
        requireActivity().IActivityHelper().setTitle("PANTRY")
    }

    private fun setUpUI(view : View){
        rvPantryList  = view.findViewById(R.id.rv_pantry_list)
        pbPantry = view.findViewById(R.id.pb_pantry)
        parentView = view.findViewById(R.id.cl_pantry)
    }

    private fun setUpViewModel(networkService : NetworkService){
        pantryViewModel = ViewModelProvider(
            this,
            CommonFactory(CommonRepository(networkService))
        ).get(AllPantryViewModel::class.java)

    //    pantryViewModel = ViewModelProvider(requireActivity())[AllPantryViewModel::class.java]

    }

    private fun setAdapter(){
        rvPantryList.layoutManager = LinearLayoutManager(
            requireContext(),
            RecyclerView.VERTICAL,
            false
        )
        pantryListAdapter = PantryListAdapter(
            requireContext(),
            pantryListData,
            object : PantryListAdapter.ItemClickListener {
                override fun itemClick(pantryResponseData: AllPantryResponseData) {
                    activity?.supportFragmentManager!!.beginTransaction().replace(
                        R.id.navHost,
                        FoodFragment(pantryResponseData)
                    ).addToBackStack(null).commit()
                }
            }
        )
        rvPantryList.adapter = pantryListAdapter
        rvPantryList.setHasFixedSize(true)
        rvPantryList.itemAnimator = DefaultItemAnimator()

        rvPantryList.addOnScrollListener(object : PaginationScrollListener(rvPantryList.layoutManager as LinearLayoutManager) {
            override fun loadMoreItems() {
                isLoading = true
                skip += 10

                Handler(Looper.myLooper()!!).postDelayed({
                    loadNextPage()
                }, 1000)
            }

            override fun getTotalPageCount(): Int {
                return totalPages
            }

            override fun isLastPage(): Boolean {
                return isLastPage
            }

            override fun isLoading(): Boolean {
                return isLoading
            }

        })

    }

    private fun getData() {
        val sort = Sort("pantry.createdAt", "DESC")
        val pantryRequest = AllPantryRequest(searchList, sort, skip, take)
        val hashMap = HashMap<String, String>()
        hashMap["Content-Type"] = "application/json;charset=UTF-8"
        pantryViewModel.getAllPantry(
            sharedPreference.getApiKey().toString(),
            pantryRequest, hashMap
        )
    }






    private fun getDefaultPantryData() {
        val pantryRequest = sharedPreference.getEmployeeId()?.let { EmployeeId(it) }
        val hashMap = HashMap<String, String>()
        hashMap["Content-Type"] = "application/json;charset=UTF-8"

        pantryRequest?.let {
            pantryViewModel.getDefaultPantry(
                 sharedPreference.getApiKey().toString(),
                it, hashMap
            )
        }

    }



    fun loadNextPage() {
        val sort = Sort("pantry.createdAt", "DESC")
        val pantryRequest = AllPantryRequest(searchList, sort, skip, take)
        val hashMap = HashMap<String, String>()
        hashMap["Content-Type"] = "application/json;charset=UTF-8"
        pantryViewModel.getAllPantryDataPagination(
            sharedPreference.getApiKey()!!,
            pantryRequest, hashMap
        )

    }

    private fun setData() {
        pantryViewModel.allPantryData.observe(requireActivity()) {
            it?.let {
                if (it.status == "success") {
                    if (it.data.isNotEmpty()) {
                        pantryListData.clear()
                        pantryListAdapter.addAll(it.data)
                        if (it.data.isNotEmpty()) {
                            pantryListAdapter.addLoadingFooter()
                        } else {
                            isLastPage = true
                        }
                    } else {
                        //   showSnackbar("Data Not Found")
                    }
                } else {
                    // showSnackbar(it.message)
                }
            }
        }

        pantryViewModel.defaultPantryData.observe(requireActivity()) {
            it?.let {
                if (it.status == "success") {
                    activity?.supportFragmentManager!!.beginTransaction().replace(
                        R.id.main,
                        FoodFragment(it.data)
                    ).commit()
                } else {
                    getData()
                }
            }
        }


    }

    private fun setPaginationData() {
        pantryViewModel.allPantryDataPagination.observe(requireActivity()) {
            it?.let {
                if (it.status == "success") {
                    pantryListAdapter.removeLoadingFooter()
                    isLoading = false
                    pantryListAdapter.addAll(it.data)
                    if (it.data.isNotEmpty()) {
                        pantryListAdapter.addLoadingFooter()
                    } else {
                        isLastPage = true
                    }
                } else {
                    //showSnackbar(it.message)
                }
            }
        }
    }

    private fun loadingState() {
        pantryViewModel.loading.observe(requireActivity()) {
            if (it) {
                pbPantry.visibility = View.VISIBLE
            } else
                pbPantry.visibility = View.GONE
        }
    }

    private fun errorObserver() {
        pantryViewModel.errorMessage.observe(requireActivity()) {
            it?.let {
                showSnackbar(it)
            }
        }
    }

    private fun showSnackbar(message: String) {
        Snackbar.make(
            parentView,
            message,
            Snackbar.LENGTH_LONG
        )
            .show()
    }
}
