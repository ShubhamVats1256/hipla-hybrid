package com.hipla.channel.ui.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.hipla.channel.R
import com.hipla.channel.databinding.ListItemUnitBinding
import com.hipla.channel.entity.UnitInfo

class UnitListAdapter(
    private val onItemClicked: (UnitInfo) -> Unit
) : RecyclerView.Adapter<UnitListAdapter.RecyclerViewHolder>() {

    private val unitInfoList = mutableListOf<UnitInfo>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerViewHolder {
        val view: View =
            LayoutInflater.from(parent.context)
                .inflate(R.layout.list_item_unit, parent, false)
        return RecyclerViewHolder(view, unitInfoList) {
            onItemClicked(it)
        }
    }

    override fun onBindViewHolder(holder: RecyclerViewHolder, position: Int) {
        unitInfoList[position].run {
            holder.binding?.name?.text = this.name
        }
    }

    override fun getItemCount(): Int = unitInfoList.size

    fun append(tempUnitList: List<UnitInfo>) {
        val startPosition = unitInfoList.size
        this.unitInfoList.addAll(tempUnitList)
        notifyItemRangeInserted(startPosition, unitInfoList.size)
    }

    class RecyclerViewHolder(
        itemView: View,
        unitInfoList: List<UnitInfo>,
        onItemClicked: (UnitInfo) -> Unit
    ) :
        RecyclerView.ViewHolder(itemView) {
        val binding = DataBindingUtil.bind<ListItemUnitBinding>(itemView)

        init {
            binding?.unit?.setOnClickListener {
                onItemClicked(unitInfoList[adapterPosition])
            }
        }
    }

}