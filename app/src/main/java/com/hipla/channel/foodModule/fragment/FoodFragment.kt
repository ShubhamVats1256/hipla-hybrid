package com.hipla.channel.foodModule.fragment

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.ProgressBar
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.snackbar.Snackbar
import com.hipla.channel.BuildConfig
import com.hipla.channel.R
import com.hipla.channel.foodModule.adapter.FoodListAdapter
import com.hipla.channel.foodModule.network.Networking
import com.hipla.channel.foodModule.network.response.AllPantryResponseData
import com.hipla.channel.foodModule.utils.PrefUtils
import com.hipla.channel.foodModule.viewmodel.PantryViewModel
import com.hipla.channel.foodModule.network.NetworkService
import com.hipla.sentinelvms.sentinelKt.foodModule.network.request.PantryRequest
import com.hipla.sentinelvms.sentinelKt.foodModule.network.request.Sort
import com.hipla.channel.foodModule.network.response.PantryData
import com.hipla.channel.foodModule.repository.CommonFactory
import com.hipla.channel.foodModule.repository.CommonRepository
import com.hipla.sentinelvms.sentinelKt.foodModule.utility.PaginationScrollListener
import com.hipla.channel.foodModule.utility.SelectedPantryData
import com.hipla.channel.foodModule.viewmodel.AllPantryViewModel


class FoodFragment(pantryResponseData: AllPantryResponseData) : androidx.fragment.app.Fragment() {
    private lateinit var btnMyOrder : Button
    private lateinit var btnContinue : Button
    private lateinit var btn_back : Button
    private lateinit var rvFoodList : RecyclerView
    private lateinit var pbFoodOrder : ProgressBar
    private lateinit var parentView : ConstraintLayout
    private lateinit var imageBussinessLogo : ImageView
    private lateinit var sharedPreference : PrefUtils


    private lateinit var pantryViewModel: PantryViewModel
    var foodListData :ArrayList<PantryData> = ArrayList()
    var selectedPantryList : ArrayList<SelectedPantryData> = ArrayList()
    private lateinit var foodListAdapter: FoodListAdapter
    private var skip: Int = 0
    private var isLoading: Boolean = false
    private var isLastPage: Boolean = false
    private var totalPages: Int = 1
    private var take: Int = 10
    private var searchList: ArrayList<PantryRequest.Search> = ArrayList()
    private var pantryData: AllPantryResponseData = pantryResponseData

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val networkService = Networking.create(BuildConfig.BASE_URL,requireContext())

        if (networkService != null) {
            setUpViewModel(networkService)
        }

        sharedPreference = PrefUtils(requireContext())
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_food, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)



        setUpUI(view)
        setAdapter()
        setListener()
        getData()
        setData()
        setPaginationData()
        loadingState()
        errorObserver()
    }

    private fun setUpUI(view :View){
        btnMyOrder = view.findViewById(R.id.btn_my_order)
        btnContinue = view.findViewById(R.id.btn_continue)
        btn_back = view.findViewById(R.id.btn_back)
        rvFoodList = view.findViewById(R.id.rv_food_list)
        pbFoodOrder = view.findViewById(R.id.pb_food_order)
        parentView = view.findViewById(R.id.cl_food_fragment)

    }

    private fun setUpViewModel(networkService : NetworkService){


        pantryViewModel = ViewModelProvider(
            this,
            CommonFactory(CommonRepository(networkService))
        ).get(PantryViewModel::class.java)


    }

    private fun setAdapter(){

        rvFoodList.layoutManager = LinearLayoutManager(
            requireContext(),
            RecyclerView.VERTICAL,
            false
        )
        foodListAdapter = FoodListAdapter(
            requireContext(),
            foodListData,
            object : FoodListAdapter.ItemClickListener {
                override fun itemClick(pantryData: PantryData, itemCount : Int) {
                    val selectedPantryData = SelectedPantryData(pantryData,itemCount)
                    if(selectedPantryList.size > 0){
                        for(item in selectedPantryList){
                            if(item.pantryData.id.equals(pantryData.id)) {
                                selectedPantryList.remove(item)
                                break
                            }
                        }
                        selectedPantryList.add(selectedPantryData)
                    }else{
                        selectedPantryList.add(selectedPantryData)
                    }
                }
            }
        )
        rvFoodList.adapter = foodListAdapter
        rvFoodList.setHasFixedSize(true)
        rvFoodList.itemAnimator = DefaultItemAnimator()

        rvFoodList.addOnScrollListener(object : PaginationScrollListener(rvFoodList.layoutManager as LinearLayoutManager) {
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

    private fun setListener() {
        btnMyOrder.setOnClickListener{
            activity?.supportFragmentManager!!.beginTransaction().replace(
                R.id.navHost,
                OrderHistoryFragment()
            ).addToBackStack(null).commit()
        }

        btnContinue.setOnClickListener{

            val myCollection = selectedPantryList
            val iterator = myCollection.iterator()
            while(iterator.hasNext()){
                val item = iterator.next()
                if(item.quantity == 0){
                    selectedPantryList.remove(item)
                }
            }

            if (selectedPantryList.isNotEmpty()){
                val customSuccessFragment = OrderSummaryFragment.newInstance(selectedPantryList)
                val ft = activity?.supportFragmentManager?.beginTransaction()
                customSuccessFragment?.let { it1 -> ft!!.add(it1, OrderSummaryFragment.TAG) }
                ft!!.commitAllowingStateLoss()
            }
            else{
                showSnackbar("Add some item!")
            }


        }


    }


    private fun getData() {
        searchList.clear()
        val searchPantryId = pantryData.id?.let {
            PantryRequest.Search("pantryStock.pantryId",
                it,"=")
        }
        val searchPantryActive = PantryRequest.Search("pantryStock.isActive","true","=")
        searchPantryId?.let { searchList.add(it) }
        searchList.add(searchPantryActive)
        val sort = Sort("pantryStock.createdAt", "DESC")
        val pantryRequest = PantryRequest(searchList, sort, skip, take)
        val hashMap = HashMap<String, String>()
        hashMap["Content-Type"] = "application/json;charset=UTF-8"

        pantryViewModel.getPantryDetail(
            sharedPreference.getApiKey()!!,
            pantryRequest, hashMap
        )

    }


    fun loadNextPage() {
        searchList.clear()
        val searchPantryId = pantryData.id?.let {
            PantryRequest.Search("pantryStock.pantryId",
                it,"=")
        }
        val searchPantryActive = PantryRequest.Search("pantryStock.isActive","true","=")
        searchPantryId?.let { searchList.add(it) }
        searchList.add(searchPantryActive)
        val sort = Sort("pantryStock.createdAt", "DESC")
        val pantryRequest = PantryRequest(searchList, sort, skip, take)
        val hashMap = HashMap<String, String>()
        hashMap["Content-Type"] = "application/json;charset=UTF-8"

        pantryViewModel.getPantryDetailPagination(
            sharedPreference.getApiKey().toString(),
            pantryRequest, hashMap
        )

    }

    private fun setData() {
        pantryViewModel.pantryDetail.observe(requireActivity(), {
            it?.let {
                if (it.status == "success") {
                    if (it.data?.isNotEmpty()!!) {
                        foodListData.clear()
                        foodListAdapter.addAll(it.data!!)
                        if (it.data!!.isNotEmpty()) {
                            foodListAdapter.addLoadingFooter()
                        } else {
                            isLastPage = true
                        }
                    } else {
                        //    showSnackbar("Data Not Found")
                    }
                } else {
                    //   it.message?.let { it1 -> showSnackbar(it1) }
                }
            }
        })
    }

    private fun setPaginationData() {
        pantryViewModel.pantryDetailPagination.observe(requireActivity(), Observer {
            it?.let {
                if (it.status == "success") {
                    foodListAdapter.removeLoadingFooter()
                    isLoading = false
                    it.data?.let { it1 -> foodListAdapter.addAll(it1) }
                    if (it.data?.isNotEmpty()!!) {
                        foodListAdapter.addLoadingFooter()
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
        pantryViewModel.loading.observe(requireActivity(), Observer {
            if (it) {
                pbFoodOrder.visibility = View.VISIBLE
            } else
                pbFoodOrder.visibility = View.GONE
        })
    }

    private fun errorObserver() {
        pantryViewModel.errorMessage.observe(requireActivity(), Observer {
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

    override fun onDestroyView() {
        super.onDestroyView()
        pantryViewModel.pantryDetail.removeObserver { requireActivity() }
        pantryViewModel.pantryDetailPagination.removeObserver { requireActivity() }
    }
}