package com.hipla.channel.ui

import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.hipla.channel.R
import com.hipla.channel.common.KEY_FLOW_CONFIG
import com.hipla.channel.databinding.FragmentHomeBinding
import com.hipla.channel.entity.FlowConfig
import com.hipla.channel.extension.isCurrentDestination
import com.hipla.channel.extension.toJsonString
import com.hipla.channel.foodModule.fragment.FoodFragment
import com.hipla.channel.foodModule.fragment.PantryFragment

class HomeFragment : Fragment(R.layout.fragment_home) {

    private lateinit var fragmentHomeBinding: FragmentHomeBinding

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        fragmentHomeBinding = FragmentHomeBinding.bind(view)
        setUI()
    }

    private fun setUI() {
        fragmentHomeBinding.applicationFlow.setOnClickListener {
            launchSalesUserFragment(FlowConfig.createApplicationFlowConfig())
        }

        fragmentHomeBinding.inventoryFlow.setOnClickListener {
            launchSalesUserFragment(FlowConfig.createInventoryFlowConfig())
        }

        fragmentHomeBinding.pantryFlow.setOnClickListener {
            activity?.supportFragmentManager!!.beginTransaction().replace(
                R.id.navHost,
                PantryFragment()
            ).commit()
        }

        fragmentHomeBinding.feedbackFlow.setOnClickListener {
            activity?.supportFragmentManager!!.beginTransaction().replace(
                R.id.navHost,
                DashboardFragement()
            ).commit()
        }


    }

    private fun launchSalesUserFragment(flowConfig: FlowConfig) {
        try {
            findNavController().run {
                if (isCurrentDestination(R.id.homeFragment)) {
                    navigate(
                        resId = R.id.action_homeFragment_to_salesUserFragment,
                        args = Bundle().apply {
                            putString(KEY_FLOW_CONFIG, flowConfig.toJsonString())
                        })
                }
            }
        }
        catch (e : Exception){
            Log.e("EXCEPTION>>",e.toString())
        }


    }

}