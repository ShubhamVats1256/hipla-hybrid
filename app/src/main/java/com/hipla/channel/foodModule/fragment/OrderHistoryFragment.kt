package com.hipla.channel.foodModule.fragment

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.ProgressBar
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.snackbar.Snackbar
import com.hipla.channel.BuildConfig
import com.hipla.channel.R
import com.hipla.channel.extension.IActivityHelper
import com.hipla.channel.foodModule.adapter.OrderListAdapter
import com.hipla.channel.foodModule.network.Networking
import com.hipla.channel.foodModule.utils.PrefUtils
import com.hipla.channel.foodModule.viewmodel.OrderHistoryViewModel
import com.hipla.channel.foodModule.network.NetworkService
import com.hipla.channel.foodModule.repository.CommonFactory
import com.hipla.channel.foodModule.repository.CommonRepository
import com.hipla.channel.foodModule.viewmodel.OrderPlaceViewModel
import com.hipla.channel.viewmodel.SalesUserViewModel
import com.hipla.sentinelvms.sentinelKt.foodModule.network.request.PantryRequest
import com.hipla.sentinelvms.sentinelKt.foodModule.network.request.Sort
import com.hipla.sentinelvms.sentinelKt.foodModule.network.response.OrderHistoryResponseData
import com.hipla.sentinelvms.sentinelKt.foodModule.utility.PaginationScrollListener


class OrderHistoryFragment : Fragment() {

    private lateinit var btn_back : Button
    private lateinit var rvHistoryList : RecyclerView
    private lateinit var pbHistory : ProgressBar
    private lateinit var parentView : ConstraintLayout
    var historyListData: ArrayList<OrderHistoryResponseData> = ArrayList()
    private lateinit var viewModel: SalesUserViewModel
    private lateinit var orderHistoryViewModel: OrderHistoryViewModel
    private lateinit var historyListAdapter: OrderListAdapter
    private lateinit var imageBussinessLogo : ImageView
    private var searchList: ArrayList<PantryRequest.Search> = ArrayList()
    private var skip: Int = 0
    private var isLoading: Boolean = false
    private var isLastPage: Boolean = false
    private var totalPages: Int = 1
    private var take: Int = 10
    private lateinit var sharedPreference : PrefUtils
    private var positionValue: Int = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        sharedPreference = PrefUtils(requireContext())

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_order_history, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val networkService = Networking.create(BuildConfig.BASE_URL,requireContext())

        if (networkService != null) {
            setUpViewModel(networkService)
        }
        viewModel = ViewModelProvider(this)[SalesUserViewModel::class.java]

        setUpUI(view)

        setAdapter()
        getData()
        setData()
        setPaginationData()
        loadingState()
        errorObserver()
        requireActivity().IActivityHelper().setTitle("PANTRY")

    }

    private fun setUpUI(view : View){
        rvHistoryList  = view.findViewById(R.id.rv_history_list)
        pbHistory = view.findViewById(R.id.pb_history)
        parentView = view.findViewById(R.id.cl_history)
        btn_back = view.findViewById(R.id.btn_back)

        btn_back.setOnClickListener {
            //  setFragment(PantryFragment())
        }
    }

    private fun setUpViewModel(networkService : NetworkService){

        orderHistoryViewModel = ViewModelProvider(
            this,
            CommonFactory(CommonRepository(networkService))
        ).get(OrderHistoryViewModel::class.java)

    }

    private fun setAdapter(){

        rvHistoryList.layoutManager = LinearLayoutManager(
            requireContext(),
            RecyclerView.VERTICAL,
            false
        )
        historyListAdapter = OrderListAdapter(
            requireContext(),
            historyListData,
            object : OrderListAdapter.ItemClickListener {
                override fun itemClick(orderHistoryResponseData: OrderHistoryResponseData) {
                    /*   activity?.supportFragmentManager!!.beginTransaction().replace(
                           R.id.rl_main,
                           FoodFragment(pantryResponseData)
                       ).addToBackStack(null).commit()*/

                }
            }
        )
        Log.e("ORDERHISTORYLIST>>",searchList.toString())


        rvHistoryList.adapter = historyListAdapter
        rvHistoryList.setHasFixedSize(true)
        rvHistoryList.itemAnimator = DefaultItemAnimator()

        rvHistoryList.addOnScrollListener(object : PaginationScrollListener(rvHistoryList.layoutManager as LinearLayoutManager) {
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
        searchList.clear()
        var search =
            sharedPreference.getEmployeeId()?.let {
                PantryRequest.Search("pantryOrder.employeeId",
                    it,"=")
            }
        if (search != null) {
            searchList.add(search)
        }
        val sort = Sort("pantryOrder.createdAt", "DESC")
        val pantryRequest = PantryRequest(searchList, sort, skip, take)
        val hashMap = HashMap<String, String>()
        hashMap["Content-Type"] = "application/json;charset=UTF-8"

        orderHistoryViewModel.getHistory(
            sharedPreference.getApiKey().toString(),
            pantryRequest, hashMap
        )

    }


    fun loadNextPage() {
        searchList.clear()
        var search =
            sharedPreference.getEmployeeId()?.let {
                PantryRequest.Search("pantryOrder.employeeId",
                    it,"=")
            }
        if (search != null) {
            searchList.add(search)
        }
        val sort = Sort("pantryOrder.createdAt", "DESC")
        val pantryRequest = PantryRequest(searchList, sort, skip, take)
        val hashMap = HashMap<String, String>()
        hashMap["Content-Type"] = "application/json;charset=UTF-8"

        orderHistoryViewModel.getHistoryDataPagination(
            sharedPreference.getApiKey().toString(),
            pantryRequest, hashMap
        )

    }

    private fun setData() {
        orderHistoryViewModel.historyData.observe(requireActivity()) {
            it?.let {
                if (it.status == "success") {
                    if (it.data.isNotEmpty()) {
                        historyListData.clear()
                        Log.e("RESPONSEHISTORY>>", it.data[0].toString())

                        historyListAdapter.addAll(it.data)
                        if (it.data.isNotEmpty()) {
                            historyListAdapter.addLoadingFooter()
                        } else {
                            isLastPage = true
                        }
                    } else {
                        //    showSnackbar("Data Not Found")
                    }
                } else {


                    showSnackbar(it.message)
                }
            }
        }
    }

    private fun setPaginationData() {
        orderHistoryViewModel.historyDataPagination.observe(requireActivity(), Observer {
            it?.let {
                if (it.status == "success") {
                    historyListAdapter.removeLoadingFooter()
                    isLoading = false
                    historyListAdapter.addAll(it.data)
                    if (it.data.isNotEmpty()) {
                        historyListAdapter.addLoadingFooter()
                    } else {
                        isLastPage = true
                    }
                } else {
                    //  showSnackbar(it.message)
                }
            }
        })
    }

    private fun loadingState() {
        orderHistoryViewModel.loading.observe(requireActivity(), Observer {
            if (it) {
                pbHistory.visibility = View.VISIBLE
            } else
                pbHistory.visibility = View.GONE
        })
    }

    private fun errorObserver() {
        orderHistoryViewModel.errorMessage.observe(requireActivity(), Observer {
            it?.let {

                showSnackbar(it)
            }
        })
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