package jp.shiita.yorimichi.ui.search

import android.arch.lifecycle.ViewModelProvider
import android.arch.lifecycle.ViewModelProviders
import android.databinding.DataBindingUtil
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import dagger.android.support.DaggerFragment
import jp.shiita.yorimichi.R
import jp.shiita.yorimichi.databinding.FragSearchBinding
import jp.shiita.yorimichi.ui.main.MainActivity
import jp.shiita.yorimichi.util.observe
import javax.inject.Inject

class SearchFragment @Inject constructor() : DaggerFragment() {
    @Inject lateinit var viewModelFactory: ViewModelProvider.Factory
    private val viewModel: SearchViewModel
            by lazy { ViewModelProviders.of(this, viewModelFactory).get(SearchViewModel::class.java) }
    private lateinit var binding: FragSearchBinding

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = DataBindingUtil.inflate(inflater, R.layout.frag_search, container, false)
        return binding.root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        binding.setLifecycleOwner(this)
        binding.viewModel = viewModel

        observe()
    }

    private fun observe() {
        viewModel.searchEvent.observe(this) { _ ->
            (activity as MainActivity).showResultFragment()
        }
    }

    companion object {
        fun newInstance() = SearchFragment()
    }
}