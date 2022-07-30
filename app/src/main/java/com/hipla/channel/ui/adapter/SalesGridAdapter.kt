package com.hipla.channel.ui.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import androidx.databinding.DataBindingUtil
import com.hipla.channel.R
import com.hipla.channel.common.image.setCircularImage
import com.hipla.channel.databinding.SalesGridItemBinding
import com.hipla.channel.entity.SalesUser

class SalesGridAdapter(
    private val context: Context,
    private val salesUserList: List<SalesUser>,
) : BaseAdapter() {

    private var layoutInflater: LayoutInflater? = null
    private var binding: SalesGridItemBinding? = null

    override fun getCount(): Int {
        return salesUserList.size
    }

    override fun getItem(position: Int): Any? {
        return salesUserList[position];
    }

    override fun getItemId(position: Int): Long {
        return salesUserList[position].id.toLong()
    }

    override fun getView(
        position: Int,
        convertView: View?,
        parent: ViewGroup
    ): View? {
        if (layoutInflater == null) {
            layoutInflater = LayoutInflater.from(context)
        }
        if (binding == null) {
            binding =
                DataBindingUtil.inflate(layoutInflater!!, R.layout.sales_grid_item, parent, false)
        }
        salesUserList[position].run {
            binding?.name?.text = this.name
            if (profileImage != null) {
                binding?.profilePic?.setCircularImage(this.profileImage)
            } else {
                binding?.profilePic?.setImageResource(R.drawable.circular_image_placeholder)
            }
        }
        return binding?.root;
    }
}