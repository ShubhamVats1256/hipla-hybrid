package com.hipla.channel.foodModule.adapter

import android.content.Context
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.ListView
import android.widget.ProgressBar
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.hipla.channel.R
import com.hipla.sentinelvms.sentinelKt.foodModule.network.response.OrderHistoryResponseData
import com.hipla.sentinelvms.sentinelKt.utils.extentions.KeyboardHideShow


class OrderListAdapter (val context: Context,
                        private val historyList: ArrayList<OrderHistoryResponseData>,
                        var itemClickListener: ItemClickListener
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private val item: Int = 0
    private val loading: Int = 1
    private var isLoadingAdded: Boolean = false
    private var retryPageLoad: Boolean = false
    private var errorMsg: String? = ""

    private var historyData: MutableList<OrderHistoryResponseData> = ArrayList()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return if (viewType == item) {
            val v = LayoutInflater.from(parent.context)
                .inflate(R.layout.layout_order_item, parent, false)
            HistoryViewHolder(v)
        } else {
            val v = LayoutInflater.from(parent.context)
                .inflate(R.layout.layout_loading_progress_bar, parent, false)
            LoadingViewHolder(v)
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val history = historyData[position]
        if (getItemViewType(position) == item) {
            val historyViewHolder: HistoryViewHolder = holder as HistoryViewHolder
            val arrayList = ArrayList<String>()
            try {
                if ( history.pantryOrderitems !=null){
                    history.pantryOrderitems!!.forEach { i ->
                        arrayList.add(i.quantity.toString()+" " + i.pantryItem.name!!)
                    }

                    val arrayAdapter: ArrayAdapter<*>
                    arrayAdapter = ArrayAdapter(context,
                        android.R.layout.simple_list_item_1, arrayList)
                    historyViewHolder.itemlist.adapter = arrayAdapter
                    val itemcount: Int = arrayList.size
                    val params: ViewGroup.LayoutParams =  historyViewHolder.itemlist.layoutParams
                    params.height = itemcount * 60
                    historyViewHolder.itemlist.layoutParams = params
                    historyViewHolder.itemlist.requestLayout()

                }

                if ( history.deliverAt!=null){

                    history.deliverAt?.let {

                        historyViewHolder.tv_deliver_at.text ="Deliver At: " +KeyboardHideShow.getStartEndTime(
                            it
                        )
                    }
                }

                if ( history.status!=null){
                    history.status?.let {
                        historyViewHolder.tvStatus.text = it
                    }
                }

                if ( history.createdAt!=null){
                    history.createdAt?.let {
                        historyViewHolder.tvOrderDate.text = KeyboardHideShow.getStartEndTime(
                            it
                        ) + "  " +KeyboardHideShow.getDate(
                            it
                        )
                    }
                }



                if ( history.status!=null){
                    history.status?.let {
                        historyViewHolder.tvStatus.text = it
                        if(it == "rejected"){
                            historyViewHolder.tvStatus.setTextColor(Color.parseColor("#D82020"))
                        }else if(it == "accepted"){
                            historyViewHolder.tvStatus.setTextColor(Color.parseColor("#298708"))
                        }else if( it == "pending"){
                            historyViewHolder.tvStatus.setTextColor(Color.parseColor("#E59318"))
                        }else if( it == "canceled"){
                            historyViewHolder.tvStatus.setTextColor(Color.parseColor("#D82020"))
                        }else if( it == "delivered"){
                            historyViewHolder.tvStatus.setTextColor(Color.parseColor("#298708"))
                        }
                    }
                }



                if ( history.pantry?.name !=null){
                    history.pantry?.name?.let {
                        historyViewHolder.tvVenue.text = it
                    }
                }

            }
            catch (e :Exception){
                //ignore
            }


        } else {
            val loadingVH: LoadingViewHolder = holder as LoadingViewHolder
            loadingVH.pbLoadMore.visibility = View.VISIBLE
        }
    }

    override fun getItemCount(): Int {
        return if (historyData.size > 0) historyData.size else 0
    }

    override fun getItemViewType(position: Int): Int {
        return if (position == 0) {
            item
        } else {
            if (position == historyData.size - 1 && isLoadingAdded) {
                loading
            } else {
                item
            }
        }
    }


    class HistoryViewHolder(var container: View) : RecyclerView.ViewHolder(
        container
    ) {

        val itemlist = itemView.findViewById(R.id.itemlist) as ListView
        val tvOrderDate = itemView.findViewById(R.id.tv_date) as TextView
        val tv_deliver_at = itemView.findViewById(R.id.tv_deliver_at) as TextView
        val tvVenue = itemView.findViewById(R.id.tv_venue) as TextView
        val tvStatus = itemView.findViewById(R.id.tv_status) as TextView
    }

    class LoadingViewHolder(var container: View) : RecyclerView.ViewHolder(
        container
    ) {
        val pbLoadMore = itemView.findViewById(R.id.pb_all_pantry_loading) as ProgressBar
    }

    fun showRetry(show: Boolean, errorMsg: String) {
        retryPageLoad = show
        notifyItemChanged(historyData.size - 1)
        this.errorMsg = errorMsg
    }

    fun addAll(orderHistoryResponseData: List<OrderHistoryResponseData>) {
        for (history in orderHistoryResponseData) {
            add(history)
        }
    }

    private fun add(orderHistoryResponseData: OrderHistoryResponseData) {
        historyData.add(orderHistoryResponseData)
        notifyItemInserted(historyData.size - 1)
    }

    fun update(position: Int, orderHistoryResponseData: OrderHistoryResponseData) {
        historyData.set(position, orderHistoryResponseData)
        notifyItemChanged(position)
    }

    fun clear() {
        historyData.clear()
        notifyDataSetChanged()
    }

    fun addLoadingFooter() {
        isLoadingAdded = true
        add(OrderHistoryResponseData())
    }

    fun removeLoadingFooter() {
        isLoadingAdded = false

        val position: Int = historyData.size - 1
        val history: OrderHistoryResponseData = historyData[position]

        if (history != null) {
            historyData.removeAt(position)
            notifyItemRemoved(position)
        }
    }

    interface ItemClickListener {
        fun itemClick(orderHistoryResponseData: OrderHistoryResponseData)
    }
}