package com.hipla.channel.ui.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.hipla.channel.R
import com.hipla.channel.common.image.setCircularImage
import com.hipla.channel.databinding.ListItemSalesUserBinding
import com.hipla.channel.entity.SalesUser


class SalesRecyclerAdapter(
    private val onItemClicked: (Int) -> Unit
) :
    RecyclerView.Adapter<SalesRecyclerAdapter.RecyclerViewHolder>() {

    private val salesUserList = mutableListOf<SalesUser>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerViewHolder {
        val view: View =
            LayoutInflater.from(parent.context).inflate(R.layout.list_item_sales_user, parent, false)
        return RecyclerViewHolder(view) {
            onItemClicked(it)
        }
    }

    override fun onBindViewHolder(holder: RecyclerViewHolder, position: Int) {
        salesUserList[position].run {
            holder.binding?.name?.text = this.name
            holder.binding?.profilePic?.setCircularImage(this.profilePic)
        }
    }

    override fun getItemCount(): Int = salesUserList.size

    fun append(tempSalesList: List<SalesUser>) {
        val startPosition = salesUserList.size
        this.salesUserList.addAll(tempSalesList)
        notifyItemRangeInserted(startPosition, salesUserList.size)
    }

    class RecyclerViewHolder(itemView: View, onItemClicked: (Int) -> Unit) :
        RecyclerView.ViewHolder(itemView) {
        val binding = DataBindingUtil.bind<ListItemSalesUserBinding>(itemView)

        init {
            binding?.root?.setOnClickListener {
                onItemClicked(adapterPosition);
            }
        }
    }

}