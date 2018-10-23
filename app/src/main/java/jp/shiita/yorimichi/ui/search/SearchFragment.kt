package jp.shiita.yorimichi.ui.search

import android.arch.lifecycle.ViewModelProvider
import android.arch.lifecycle.ViewModelProviders
import android.databinding.DataBindingUtil
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.google.android.gms.maps.SupportMapFragment
import dagger.android.support.DaggerFragment
import jp.shiita.yorimichi.R
import jp.shiita.yorimichi.databinding.FragSearchBinding
import jp.shiita.yorimichi.ui.searchresult.SearchResultFragment
import jp.shiita.yorimichi.util.observe
import jp.shiita.yorimichi.util.replaceFragment
import javax.inject.Inject

class SearchFragment : DaggerFragment() {
    @Inject lateinit var viewModelFactory: ViewModelProvider.Factory
    private val viewModel: SearchViewModel
            by lazy { ViewModelProviders.of(this, viewModelFactory).get(SearchViewModel::class.java) }
    private lateinit var binding: FragSearchBinding
    private lateinit var categoryAdapter: CategoryAdapter

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = DataBindingUtil.inflate(inflater, R.layout.frag_search, container, false)
        return binding.root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        binding.setLifecycleOwner(this)
        binding.viewModel = viewModel

        categoryAdapter = CategoryAdapter(context!!, mutableListOf(
            "shopping_mall" to false,
            "library" to false,
            "cafe" to false,
            "book_store" to false,
            "park" to false,
            "movie_theater" to false,
            "home_goods_store" to false,
            "clothing_store" to false,
            "bar" to false
        ))
        binding.categoryRecyclerView.adapter = categoryAdapter


        initMap()
        observe()
    }

    private fun initMap() {
        (childFragmentManager.findFragmentById(R.id.google_map) as SupportMapFragment).getMapAsync {}
    }

    private fun observe() {
        viewModel.searchEvent.observe(this) {
            // SearchFragmentはネストされたフラグメントであるため
            activity?.supportFragmentManager?.replaceFragment(
                    R.id.container,
                    SearchResultFragment.newInstance(),
                    SearchResultFragment.TAG)
        }
    }

    companion object {
        val TAG: String = SearchFragment::class.java.simpleName
        fun newInstance() = SearchFragment()
    }
}