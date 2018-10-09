package jp.shiita.yorimichi.ui.main

import android.arch.lifecycle.ViewModelProvider
import android.arch.lifecycle.ViewModelProviders
import android.databinding.DataBindingUtil
import android.os.Bundle
import dagger.android.support.DaggerAppCompatActivity
import jp.shiita.yorimichi.R
import jp.shiita.yorimichi.databinding.ActMainBinding
import jp.shiita.yorimichi.ui.searchresult.SearchResultFragment
import jp.shiita.yorimichi.util.addFragment
import jp.shiita.yorimichi.util.replaceFragment
import javax.inject.Inject

class MainActivity : DaggerAppCompatActivity() {
    @Inject lateinit var viewModelFactory: ViewModelProvider.Factory
    @Inject lateinit var mainFragment: MainFragment
    @Inject lateinit var searchResultFragment: SearchResultFragment
    private val viewModel: MainViewModel
            by lazy { ViewModelProviders.of(this, viewModelFactory).get(MainViewModel::class.java) }
    private val binding: ActMainBinding
            by lazy { DataBindingUtil.setContentView<ActMainBinding>(this, R.layout.act_main) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setSupportActionBar(binding.toolbar)
        binding.setLifecycleOwner(this)
        binding.viewModel = viewModel

        if (savedInstanceState == null) {
            supportFragmentManager.addFragment(R.id.container, mainFragment)
        }
    }

    fun showResultFragment() {
        supportFragmentManager.replaceFragment(R.id.container, searchResultFragment)
    }
}
