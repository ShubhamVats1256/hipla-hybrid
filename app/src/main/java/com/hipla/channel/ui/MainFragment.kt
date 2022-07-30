package com.hipla.channel.ui
import androidx.lifecycle.ViewModelProvider
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.View
import com.hipla.channel.R
import com.hipla.channel.databinding.FragmentMainBinding
import com.hipla.channel.viewmodel.MainViewModel

class MainFragment : Fragment(R.layout.fragment_main) {

    private lateinit var viewModel: MainViewModel
    private lateinit var binding: FragmentMainBinding

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding =  FragmentMainBinding.bind(view)
        viewModel = ViewModelProvider(this)[MainViewModel::class.java]
    }

}