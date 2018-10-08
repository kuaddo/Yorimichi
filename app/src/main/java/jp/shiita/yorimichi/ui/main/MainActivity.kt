package jp.shiita.yorimichi.ui.main

import android.arch.lifecycle.ViewModelProvider
import android.arch.lifecycle.ViewModelProviders
import android.databinding.DataBindingUtil
import android.os.Bundle
import dagger.android.support.DaggerAppCompatActivity
import jp.shiita.yorimichi.R
import jp.shiita.yorimichi.databinding.ActMainBinding
import javax.inject.Inject

class MainActivity : DaggerAppCompatActivity() {
    @Inject lateinit var viewModelFactory: ViewModelProvider.Factory
    @Inject lateinit var mainFragment: MainFragment
    private val viewModel: MainViewModel
            by lazy { ViewModelProviders.of(this, viewModelFactory).get(MainViewModel::class.java) }
    private val binding: ActMainBinding
            by lazy { DataBindingUtil.setContentView<ActMainBinding>(this, R.layout.act_main) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding.setLifecycleOwner(this)
        binding.viewModel = viewModel

        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction().run {
                add(R.id.container, mainFragment)
                commit()
            }
        }
    }
}
