package com.hipla.channel.ui

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.viewpager2.adapter.FragmentStateAdapter
import androidx.viewpager2.widget.ViewPager2
import com.hipla.channel.R
import com.hipla.channel.databinding.FragmentMainBinding
import com.hipla.channel.viewmodel.MainViewModel

class MainFragment : Fragment(R.layout.fragment_main) {

    private lateinit var viewModel: MainViewModel
    private lateinit var binding: FragmentMainBinding
    private var mainPageAdapter: MainPageAdapter? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentMainBinding.bind(view)
        viewModel = ViewModelProvider(this)[MainViewModel::class.java]
        setupViewPager()
    }

    private fun setupViewPager() {
        binding.pager.run {
            mainPageAdapter = MainPageAdapter(this@MainFragment)
            adapter = mainPageAdapter
            setCurrentItem(APPLICATION, false)
            registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
                override fun onPageScrolled(
                    position: Int,
                    positionOffset: Float,
                    positionOffsetPixels: Int
                ) {
                }

                override fun onPageSelected(position: Int) {
                    when (position) {
                        APPLICATION -> {
                            // sent message to activity to change tab
                        }
                    }
                }

                override fun onPageScrollStateChanged(state: Int) {}
            })
        }
    }


    inner class MainPageAdapter(fragment: Fragment) : FragmentStateAdapter(fragment) {
        override fun getItemCount(): Int = 2

        override fun createFragment(position: Int): Fragment {
            return when (position) {
                APPLICATION -> ApplicationFlowFragment()
                else -> ApplicationCustomerInfoFragment()
            }
        }
    }

    companion object {
        const val APPLICATION = 0
        const val INVENTORY = 1
    }

}