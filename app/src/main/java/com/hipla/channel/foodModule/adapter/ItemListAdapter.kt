package com.hipla.channel.foodModule.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.hipla.channel.R
import com.hipla.channel.foodModule.utility.SelectedPantryData

class ItemListAdapter(val context: Context, private val itemList: ArrayList<SelectedPantryData>) : RecyclerView.Adapter<ItemListAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val v = LayoutInflater.from(parent.context)
            .inflate(R.layout.layout_order_summary_item, parent, false)
        return ViewHolder(v)
    }

    //this method is binding the data on the list
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bindItems(itemList[position], context)
    }

    //this method is giving the size of the list
    override fun getItemCount(): Int {
        return itemList.size
    }

    //the class is hodling the list view
    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        fun bindItems(
            selectedPantryData: SelectedPantryData,
            context: Context
        ) {
            val tvName = itemView.findViewById(R.id.tv_item) as TextView
            val tvQuantity = itemView.findViewById(R.id.tv_qty) as TextView
            selectedPantryData.pantryData.pantryItem?.name?.let {
                tvName.text = it
            }
            selectedPantryData.quantity?.let {
                tvQuantity.text = it.toString()
            }

        }
    }
}