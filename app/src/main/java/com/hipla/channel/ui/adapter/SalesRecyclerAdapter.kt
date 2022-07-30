package com.hipla.channel.ui.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.hipla.channel.R
import com.hipla.channel.common.image.setCircularImage
import com.hipla.channel.databinding.SalesGridItemBinding
import com.hipla.channel.entity.SalesUser


class SalesRecyclerAdapter(private val salesUserList: List<SalesUser>) :
    RecyclerView.Adapter<SalesRecyclerAdapter.RecyclerViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerViewHolder {
        val view: View =
            LayoutInflater.from(parent.context).inflate(R.layout.sales_grid_item, parent, false)
        return RecyclerViewHolder(view)
    }

    override fun onBindViewHolder(holder: RecyclerViewHolder, position: Int) {
        salesUserList[position].run {
            holder.binding?.name?.text = this.name
            holder.binding?.profilePic?.setCircularImage(this.profileImage)
        }
    }

    override fun getItemCount(): Int = salesUserList.size

    class RecyclerViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val binding = DataBindingUtil.bind<SalesGridItemBinding>(itemView)
    }

}