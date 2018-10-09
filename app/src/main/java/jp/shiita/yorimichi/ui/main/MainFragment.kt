package jp.shiita.yorimichi.ui.main

import android.arch.lifecycle.ViewModelProvider
import android.arch.lifecycle.ViewModelProviders
import android.databinding.DataBindingUtil
import android.os.Bundle
import android.support.v4.app.FragmentManager
import android.support.v4.app.FragmentPagerAdapter
import android.support.v4.view.ViewPager
import android.support.v7.app.AppCompatActivity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import dagger.android.support.DaggerFragment
import jp.shiita.yorimichi.R
import jp.shiita.yorimichi.databinding.FragMainBinding
import jp.shiita.yorimichi.ui.BlankFragment
import jp.shiita.yorimichi.ui.main.MainFragment.PagerAdapter.Companion.MAP_FRAGMENT
import jp.shiita.yorimichi.ui.main.MainFragment.PagerAdapter.Companion.SEARCH_FRAGMENT
import jp.shiita.yorimichi.ui.search.SearchFragment
import javax.inject.Inject

class MainFragment @Inject constructor() : DaggerFragment() {
    @Inject lateinit var viewModelFactory: ViewModelProvider.Factory
    private val viewModel: MainViewModel
            by lazy { ViewModelProviders.of(activity!!, viewModelFactory).get(MainViewModel::class.java) }
    private lateinit var binding: FragMainBinding

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = DataBindingUtil.inflate(inflater, R.layout.frag_main, container, false)
        return binding.root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        binding.setLifecycleOwner(this)
        binding.viewModel = viewModel

        binding.viewPager.also { vp ->
            vp.adapter = PagerAdapter(childFragmentManager)
            vp.addOnPageChangeListener(object : ViewPager.OnPageChangeListener {
                override fun onPageScrollStateChanged(state: Int) {}

                override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {}

                override fun onPageSelected(position: Int) {
                    val actionBar = (activity as AppCompatActivity).supportActionBar ?: return
                    when (position) {
                        MAP_FRAGMENT -> {
                            actionBar.setDisplayHomeAsUpEnabled(false)
                            actionBar.setTitle(R.string.app_name)
                        }
                        SEARCH_FRAGMENT -> {
                            actionBar.setDisplayHomeAsUpEnabled(false)
                            actionBar.setTitle(R.string.title_search)
                        }
                    }
                }
            })
        }

        binding.tabLayout.also { tl ->
            tl.setupWithViewPager(binding.viewPager)
            tl.getTabAt(MAP_FRAGMENT)?.also { tab ->
                tab.setText(R.string.tab_map)
                tab.setIcon(R.drawable.ic_map)
            }
            tl.getTabAt(SEARCH_FRAGMENT)?.also { tab ->
                tab.setText(R.string.tab_search)
                tab.setIcon(R.drawable.ic_search)
            }
        }
    }

    override fun onDestroyView() {
        binding.viewPager.clearOnPageChangeListeners()
        super.onDestroyView()
    }

    private class PagerAdapter(fragmentManager: FragmentManager) : FragmentPagerAdapter(fragmentManager) {
        override fun getItem(position: Int) = when (position) {
            MAP_FRAGMENT    -> BlankFragment.newInstance()
            SEARCH_FRAGMENT -> SearchFragment.newInstance()
            else -> error("invalid position")
        }

        override fun getCount(): Int = 2

        companion object {
            const val MAP_FRAGMENT = 0
            const val SEARCH_FRAGMENT = 1
        }
    }
}