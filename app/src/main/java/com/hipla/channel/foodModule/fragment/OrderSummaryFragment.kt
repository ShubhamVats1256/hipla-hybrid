package com.hipla.channel.foodModule.fragment

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.hipla.channel.BuildConfig
import com.hipla.channel.R
import com.hipla.channel.foodModule.adapter.ItemListAdapter
import com.hipla.channel.foodModule.network.Networking
import com.hipla.channel.foodModule.utils.PrefUtils
import com.hipla.channel.foodModule.viewmodel.OrderPlaceViewModel
import com.hipla.channel.foodModule.network.NetworkService
import com.hipla.channel.foodModule.repository.CommonFactory
import com.hipla.channel.foodModule.repository.CommonRepository
import com.hipla.sentinelvms.sentinelKt.foodModule.network.request.OrderRequest
import com.hipla.sentinelvms.sentinelKt.foodModule.network.response.Items
import com.hipla.channel.foodModule.utility.SelectedPantryData
import com.hipla.channel.foodModule.viewmodel.PantryViewModel


class OrderSummaryFragment(selectedPantryList : ArrayList<SelectedPantryData>) : DialogFragment() {

    private lateinit var rvSelectedFoodList : RecyclerView
    private lateinit var itemListAdapter : ItemListAdapter
    private var itemList = selectedPantryList
    private lateinit var btnPlaceOrder : Button
    private lateinit var et_instructions : EditText
    private lateinit var btnClose : Button
    private lateinit var ivClose : ImageView

    private lateinit var orderPlaceViewModel: OrderPlaceViewModel
    lateinit var sharedPreference : PrefUtils

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
        dialog!!.window?.setBackgroundDrawableResource(R.drawable.bottom_sheet_rounded_corner);
        return inflater.inflate(R.layout.fragment_order_summary, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)


        setUpUi(view)

        setAdapter()
        setData()

    }

    private fun setUpUi(view : View){
        rvSelectedFoodList = view.findViewById(R.id.rv_food_list)
        btnPlaceOrder = view.findViewById(R.id.btn_place_order)
        btnClose = view.findViewById(R.id.btn_close)
        ivClose = view.findViewById(R.id.iv_close)
        et_instructions = view.findViewById(R.id.et_instructions)


        btnClose.setOnClickListener{
            dismiss()
        }

        ivClose.setOnClickListener{
            dismiss()
        }

        btnPlaceOrder.setOnClickListener{
            var orderItemList : ArrayList<Items> = ArrayList()
            for(item in itemList){
                val foodItem = Items(item.pantryData.pantryItemId,item.quantity)
                orderItemList.add(foodItem)
            }

            if (orderItemList.isNotEmpty()){
                val orderRequest = OrderRequest(itemList[0].pantryData.pantryId,
                    sharedPreference.getOrganizationId(),
                    sharedPreference.getBusinessId(),
                    sharedPreference.getEmployeeId(),et_instructions.text.toString(),orderItemList)
                orderPlaceViewModel.sendOrderDetail(
                    sharedPreference.getApiKey().toString(),orderRequest)
            }
        }
    }

    private fun setUpViewModel(networkService : NetworkService){

        orderPlaceViewModel = ViewModelProvider(
            this,
            CommonFactory(CommonRepository(networkService))
        ).get(OrderPlaceViewModel::class.java)
    }

    private fun setAdapter(){
        rvSelectedFoodList.layoutManager = LinearLayoutManager(
            requireContext(),
            RecyclerView.VERTICAL,
            false
        )

        itemListAdapter = ItemListAdapter(
            requireContext(),
            itemList)
        rvSelectedFoodList.adapter = itemListAdapter
        rvSelectedFoodList.setHasFixedSize(true)
        rvSelectedFoodList.itemAnimator = DefaultItemAnimator()
    }

    private fun setData(){
        orderPlaceViewModel.orderPlaceData.observe(requireActivity()) {
            if (it.status == "success") {
                dismiss()
                Toast.makeText(context, "ORDERED SUCCESSFULLY!", Toast.LENGTH_LONG).show()
                activity?.supportFragmentManager?.beginTransaction()
                    ?.replace(R.id.navHost, OrderHistoryFragment())?.addToBackStack("0")?.commit()
//                activity?.supportFragmentManager!!.beginTransaction().replace(
//                    R.id.rl_main,
//                    OrderHistoryFragment()
//                ).addToBackStack(null).commit()

            }
        }
    }

    companion object {
        var TAG: String = "CustomSuccessFragment"

        fun newInstance(selectedPantryList : ArrayList<SelectedPantryData>): OrderSummaryFragment? {
            return OrderSummaryFragment(selectedPantryList)
        }
    }


    override fun onStart() {
        super.onStart()
        val width = (resources.displayMetrics.widthPixels * 0.85).toInt()
        val height = (resources.displayMetrics.heightPixels * 0.40).toInt()
        dialog!!.window?.setLayout(width, ViewGroup.LayoutParams.WRAP_CONTENT)
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
    }

}