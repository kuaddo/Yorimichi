package jp.shiita.yorimichi.ui.main

import android.arch.lifecycle.ViewModelProvider
import android.arch.lifecycle.ViewModelProviders
import android.databinding.DataBindingUtil
import android.os.Bundle
import android.support.v4.view.GravityCompat
import android.support.v4.widget.DrawerLayout
import dagger.android.support.DaggerAppCompatActivity
import jp.shiita.yorimichi.R
import jp.shiita.yorimichi.databinding.ActMainBinding
import jp.shiita.yorimichi.ui.mypage.MyPageFragment
import jp.shiita.yorimichi.ui.note.NoteFragment
import jp.shiita.yorimichi.ui.searchresult.SearchResultFragment
import jp.shiita.yorimichi.ui.setting.SettingFragment
import jp.shiita.yorimichi.ui.shop.ShopFragment
import jp.shiita.yorimichi.util.addFragment
import jp.shiita.yorimichi.util.replaceFragment
import javax.inject.Inject


class MainActivity : DaggerAppCompatActivity() {
    @Inject lateinit var viewModelFactory: ViewModelProvider.Factory
    private val viewModel: MainViewModel
            by lazy { ViewModelProviders.of(this, viewModelFactory).get(MainViewModel::class.java) }
    private val binding: ActMainBinding
            by lazy { DataBindingUtil.setContentView<ActMainBinding>(this, R.layout.act_main) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setSupportActionBar(binding.toolbar)
        setupDrawer()
        binding.setLifecycleOwner(this)
        binding.viewModel = viewModel

        if (savedInstanceState == null) {
            supportFragmentManager.addFragment(R.id.container, MainFragment.newInstance())
        }
    }

    fun showDrawer() = binding.drawerLayout.openDrawer(GravityCompat.START)

    fun lockDrawer() = binding.drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED)

    fun unlockDrawer() = binding.drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED)

    fun showResultFragment() {
        supportFragmentManager.replaceFragment(R.id.container, SearchResultFragment.newInstance(), SearchResultFragment.TAG)
    }

    private fun setupDrawer() {
        binding.navView.setNavigationItemSelectedListener { item ->
            lockDrawer()
            when (item.itemId) {
                R.id.menu_drawer_my_page -> supportFragmentManager.replaceFragment(R.id.container, MyPageFragment.newInstance(), MyPageFragment.TAG)
                R.id.menu_drawer_note    -> supportFragmentManager.replaceFragment(R.id.container, NoteFragment.newInstance(), NoteFragment.TAG)
                R.id.menu_drawer_shop    -> supportFragmentManager.replaceFragment(R.id.container, ShopFragment.newInstance(), ShopFragment.TAG)
                R.id.menu_drawer_setting -> supportFragmentManager.replaceFragment(R.id.container, SettingFragment.newInstance(), SettingFragment.TAG)
            }
            binding.drawerLayout.closeDrawers()
            true
        }
    }
}
