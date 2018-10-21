package jp.shiita.yorimichi.ui.main

import android.arch.lifecycle.ViewModelProvider
import android.arch.lifecycle.ViewModelProviders
import android.databinding.DataBindingUtil
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v4.app.FragmentManager
import android.support.v4.app.FragmentPagerAdapter
import android.support.v4.view.ViewPager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import dagger.android.support.DaggerFragment
import jp.shiita.yorimichi.R
import jp.shiita.yorimichi.databinding.FragMainBinding
import jp.shiita.yorimichi.ui.main.MainFragment.PagerAdapter.Companion.MAP_FRAGMENT
import jp.shiita.yorimichi.ui.main.MainFragment.PagerAdapter.Companion.SEARCH_FRAGMENT
import jp.shiita.yorimichi.ui.map.MapFragment
import jp.shiita.yorimichi.ui.search.SearchFragment
import javax.inject.Inject

class MainFragment : DaggerFragment() {
    @Inject lateinit var viewModelFactory: ViewModelProvider.Factory
    private val viewModel: MainViewModel
            by lazy { ViewModelProviders.of(activity!!, viewModelFactory).get(MainViewModel::class.java) }
    private lateinit var binding: FragMainBinding
    private var currentPosition = 0

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = DataBindingUtil.inflate(inflater, R.layout.frag_main, container, false)
        return binding.root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel.setupActionBar(R.string.app_name, R.drawable.ic_menu, true, MainViewModel.HomeAsUpType.OPEN_DRAWER)
        viewModel.setDrawerLock(false)
        binding.setLifecycleOwner(this)
        binding.viewModel = viewModel

        binding.viewPager.also { vp ->
            vp.adapter = PagerAdapter(childFragmentManager, listOf(MapFragment.newInstance(), SearchFragment.newInstance()))
            vp.addOnPageChangeListener(object : ViewPager.OnPageChangeListener {
                override fun onPageScrollStateChanged(state: Int) {}

                override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {}

                override fun onPageSelected(position: Int) {
                    currentPosition = position
                    when (position) {
                        MAP_FRAGMENT -> viewModel.setDrawerLock(false)
                        SEARCH_FRAGMENT -> viewModel.setDrawerLock(true)
                    }
                }
            })
        }

        binding.tabLayout.also { tl ->
            tl.setupWithViewPager(binding.viewPager)
            tl.getTabAt(MAP_FRAGMENT)?.setCustomView(R.layout.tab_map)
            tl.getTabAt(SEARCH_FRAGMENT)?.setCustomView(R.layout.tab_search)
        }
    }

    override fun onDestroyView() {
        binding.viewPager.clearOnPageChangeListeners()
        super.onDestroyView()
    }

    private class PagerAdapter(fragmentManager: FragmentManager, private val fragments: List<Fragment>) : FragmentPagerAdapter(fragmentManager) {
        override fun getItem(position: Int) = fragments[position]

        override fun getCount(): Int = 2

        companion object {
            const val MAP_FRAGMENT = 0
            const val SEARCH_FRAGMENT = 1
        }
    }

    companion object {
        val TAG: String = MainFragment::class.java.simpleName
        fun newInstance() = MainFragment()
    }
}