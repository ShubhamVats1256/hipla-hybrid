package com.hipla.channel.foodModule.adapter

import android.app.Activity
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.snackbar.Snackbar
import com.hipla.channel.R

import com.hipla.channel.foodModule.network.response.PantryData
import com.hipla.channel.foodModule.utils.PrefUtils

class FoodListAdapter(
    val context: Context,
    private val pantryList: ArrayList<PantryData>,
    var itemClickListener: ItemClickListener
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private val item: Int = 0
    private val loading: Int = 1
    private var isLoadingAdded: Boolean = false
    private var retryPageLoad: Boolean = false
    private var errorMsg: String? = ""
    lateinit var sharedPreference : PrefUtils

    private var pantryData: MutableList<PantryData> = ArrayList()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {

        sharedPreference = PrefUtils(context)

        return if (viewType == item) {


            val v = LayoutInflater.from(parent.context)
                .inflate(R.layout.layout_food_item, parent, false)
            PantryViewHolder(v)



        } else {
            val v = LayoutInflater.from(parent.context)
                .inflate(R.layout.layout_loading_progress_bar, parent, false)
            LoadingViewHolder(v)
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val pantryData = pantryData[position]
        if (getItemViewType(position) == item) {
            val pantryViewHolder: PantryViewHolder = holder as PantryViewHolder
            pantryData.pantryItem?.name?.let {
                pantryViewHolder.tvFoodName.text = it
            }
            if (sharedPreference.getHideStock()){
                pantryViewHolder.tvPlus.visibility = View.VISIBLE
                pantryData.stock?.let {
                    pantryViewHolder.tvStock.text = "Available Stock $it"
                }
            }
            else{
                pantryViewHolder.tvStock.visibility = View.GONE
            }

            pantryViewHolder.tvPlus.setOnClickListener {
                var itemCount = pantryViewHolder.tvCount.text.toString().toInt()
                if (pantryData.stock!!.toInt() > itemCount) {
                    ++itemCount
                    pantryViewHolder.tvCount.setText(itemCount.toString())
                    itemClickListener.itemClick(pantryData, itemCount)
                }
                else{
                    showSnackbar("Only $itemCount Item Available!")
                }
            }

            pantryViewHolder.tvMinus.setOnClickListener {
                var itemCount = pantryViewHolder.tvCount.text.toString().toInt()
                --itemCount
                if(itemCount == 0){
                    pantryViewHolder.llInc.visibility = View.GONE
                    pantryViewHolder.btnAdd.visibility = View.VISIBLE
                }else {
                    pantryViewHolder.tvCount.setText(itemCount.toString())
                }
                itemClickListener.itemClick(pantryData,itemCount)
            }

            pantryViewHolder.btnAdd.setOnClickListener {
                if (pantryData.stock!!.toInt() > 0) {
                    itemClickListener.itemClick(pantryData, 1)
                    pantryViewHolder.llInc.visibility = View.VISIBLE
                    it.visibility = View.GONE
                }
                else {
                    showSnackbar("Out Of Stock!")
                }
            }


        }
        else {
            val loadingVH: LoadingViewHolder = holder as LoadingViewHolder
            loadingVH.pbLoadMore.visibility = View.VISIBLE
        }
    }

    override fun getItemCount(): Int {
        return if (pantryData.size > 0) pantryData.size else 0
    }

    override fun getItemViewType(position: Int): Int {
        return if (position == 0) {
            item
        } else {
            if (position == pantryData.size - 1 && isLoadingAdded) {
                loading
            } else {
                item
            }
        }
    }


    class PantryViewHolder(var container: View) : RecyclerView.ViewHolder(
        container
    ) {

        val tvFoodName = itemView.findViewById(R.id.tv_food_item) as TextView
        val btnAdd = itemView.findViewById(R.id.btn_add) as Button
        val llInc  = itemView.findViewById(R.id.ll_inc) as LinearLayout
        val tvMinus = itemView.findViewById(R.id.tv_increase) as TextView
        val tvPlus = itemView.findViewById(R.id.tv_decrease) as TextView
        val tvCount = itemView.findViewById(R.id.tv_count) as TextView
        val tvStock = itemView.findViewById(R.id.tv_stock) as TextView
    }

    class LoadingViewHolder(var container: View) : RecyclerView.ViewHolder(
        container
    ) {
        val pbLoadMore = itemView.findViewById(R.id.pb_all_pantry_loading) as ProgressBar
    }

    fun showRetry(show: Boolean, errorMsg: String) {
        retryPageLoad = show
        notifyItemChanged(pantryData.size - 1)
        this.errorMsg = errorMsg
    }

    fun addAll(pantryResponseData: List<PantryData>) {
        for (pantry in pantryResponseData) {
            add(pantry)
        }
    }

    private fun add(pantryResponseData: PantryData) {
        pantryData.add(pantryResponseData)
        notifyItemInserted(pantryData.size - 1)
    }

    fun update(position: Int, pantryResponseData: PantryData) {
        pantryData.set(position, pantryResponseData)
        notifyItemChanged(position)
    }

    fun clear() {
        pantryData.clear()
        notifyDataSetChanged()
    }

    fun addLoadingFooter() {
        isLoadingAdded = true
        add(PantryData())
    }

    fun removeLoadingFooter() {
        isLoadingAdded = false

        val position: Int = pantryData.size - 1
        val pantry: PantryData = pantryData[position]

        if (pantry != null) {
            pantryData.removeAt(position)
            notifyItemRemoved(position)
        }
    }

    private fun showSnackbar(message: String) {
        Snackbar.make(
            (context as Activity).findViewById(android.R.id.content),
            message,
            Snackbar.LENGTH_LONG
        )
            .show()
    }

    interface ItemClickListener {
        fun itemClick(pantryData: PantryData, count : Int)
    }
}