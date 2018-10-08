package jp.shiita.yorimichi.ui.main

import android.arch.lifecycle.ViewModelProvider
import android.arch.lifecycle.ViewModelProviders
import android.databinding.DataBindingUtil
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import dagger.android.support.DaggerFragment
import jp.shiita.yorimichi.R
import jp.shiita.yorimichi.databinding.FragMainBinding
import javax.inject.Inject

class MainFragment @Inject constructor() : DaggerFragment() {
    @Inject lateinit var viewModelFactory: ViewModelProvider.Factory
    private val viewModel: MainViewModel
            by lazy { ViewModelProviders.of(activity!!, viewModelFactory).get(MainViewModel::class.java) }
    private lateinit var binding: FragMainBinding

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        setHasOptionsMenu(true)
        binding = DataBindingUtil.inflate(inflater, R.layout.frag_main, container, false)
        return binding.root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        binding.setLifecycleOwner(this)
        binding.viewModel = viewModel

        viewModel.start()
    }
}