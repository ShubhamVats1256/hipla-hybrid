package com.hipla.channel.foodModule.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.hipla.channel.R
import com.hipla.channel.foodModule.network.response.AllPantryResponseData

class PantryListAdapter(val context : Context, private val pantryList: ArrayList<AllPantryResponseData>, var itemClickListener: ItemClickListener) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private val item: Int = 0
    private val loading: Int = 1
    private var isLoadingAdded: Boolean = false
    private var retryPageLoad: Boolean = false
    private var errorMsg: String? = ""

    //

    private var allPantryResponseData: MutableList<AllPantryResponseData> = ArrayList()
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return if (viewType == item) {
            val v = LayoutInflater.from(parent.context)
                .inflate(R.layout.layout_all_pantry_item, parent, false)
            PantryViewHolder(v)
        } else {
            val v = LayoutInflater.from(parent.context)
                .inflate(R.layout.layout_loading_progress_bar, parent, false)
            LoadingViewHolder(v)
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val pantryListData = allPantryResponseData[position]
        if (getItemViewType(position) == item) {
            val pantryViewHolder: PantryViewHolder = holder as PantryViewHolder

            pantryListData.name?.let {
                pantryViewHolder.tvPantryName.text = it
            }

            pantryViewHolder.itemView.setOnClickListener {
                itemClickListener.itemClick(pantryListData)
            }
        } else {
            val loadingVH: LoadingViewHolder = holder as LoadingViewHolder
            loadingVH.pbLoadMore.visibility = View.VISIBLE
        }
    }

    override fun getItemCount(): Int {
        return if (allPantryResponseData.size > 0) allPantryResponseData.size else 0
    }

    override fun getItemViewType(position: Int): Int {
        return if (position == 0) {
            item
        } else {
            if (position == allPantryResponseData.size - 1 && isLoadingAdded) {
                loading
            } else {
                item
            }
        }
    }


    class PantryViewHolder(var container: View) : RecyclerView.ViewHolder(
        container
    ) {

        val tvPantryName = itemView.findViewById(R.id.tv_pantry_item) as TextView

    }

    class LoadingViewHolder(var container: View) : RecyclerView.ViewHolder(
        container
    ) {
        val pbLoadMore = itemView.findViewById(R.id.pb_all_pantry_loading) as ProgressBar
    }

    fun showRetry(show: Boolean, errorMsg: String) {
        retryPageLoad = show
        notifyItemChanged(allPantryResponseData.size - 1)
        this.errorMsg = errorMsg
    }

    fun addAll(pantryResponseData: List<AllPantryResponseData>) {
        for (pantry in pantryResponseData) {
            add(pantry)
        }
    }

    private fun add(pantryResponseData: AllPantryResponseData) {
        allPantryResponseData.add(pantryResponseData)
        notifyItemInserted(allPantryResponseData.size - 1)
    }

    fun update(position: Int, pantryResponseData: AllPantryResponseData) {
        allPantryResponseData.set(position, pantryResponseData)
        notifyItemChanged(position)
    }

    fun clear() {
        allPantryResponseData.clear()
        notifyDataSetChanged()
    }

    fun addLoadingFooter() {
        isLoadingAdded = true
        add(AllPantryResponseData())
    }

    fun removeLoadingFooter() {
        isLoadingAdded = false

            val position: Int = allPantryResponseData.size - 1
            val pantry: AllPantryResponseData = allPantryResponseData[position]

            if (pantry != null) {
                allPantryResponseData.removeAt(position)
                notifyItemRemoved(position)
            }



    }

    interface ItemClickListener {
        fun itemClick(allPantryResponse: AllPantryResponseData)
    }
}