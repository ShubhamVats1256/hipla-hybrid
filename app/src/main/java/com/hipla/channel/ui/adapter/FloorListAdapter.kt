package com.hipla.channel.ui.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.hipla.channel.R
import com.hipla.channel.databinding.ListItemFloorBinding
import com.hipla.channel.entity.FloorInfo
import com.hipla.channel.extension.isAvailable

class FloorListAdapter(
    private val context: Context,
    private val onItemClicked: (FloorInfo) -> Unit
) : RecyclerView.Adapter<FloorListAdapter.RecyclerViewHolder>() {

    private val floorInfoList = mutableListOf<FloorInfo>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerViewHolder {
        val view: View =
            LayoutInflater.from(parent.context)
                .inflate(R.layout.list_item_floor, parent, false)
        return RecyclerViewHolder(view, floorInfoList) {
            onItemClicked(it)
        }
    }

    override fun onBindViewHolder(holder: RecyclerViewHolder, position: Int) {
        floorInfoList[position].run floorInfo@{
            holder.binding?.name?.text = buildString {
                append("Floor ")
                append(this@floorInfo.floorId)
            }
        }
    }

    fun isListAlreadyAppended(tempUnitSales: List<FloorInfo>): Boolean {
        return floorInfoList.lastOrNull()?.id == tempUnitSales.lastOrNull()?.id
    }

    override fun getItemCount(): Int = floorInfoList.size

    fun append(tempUnitList: List<FloorInfo>) {
        val startPosition = floorInfoList.size
        this.floorInfoList.addAll(tempUnitList)
        notifyItemRangeInserted(startPosition, floorInfoList.size)
    }

    class RecyclerViewHolder(
        itemView: View,
        floorInfoList: List<FloorInfo>,
        onItemClicked: (FloorInfo) -> Unit
    ) :
        RecyclerView.ViewHolder(itemView) {
        val binding = DataBindingUtil.bind<ListItemFloorBinding>(itemView)

        init {
            binding?.floor?.setOnClickListener {
                onItemClicked(floorInfoList[adapterPosition])
            }
        }
    }

}