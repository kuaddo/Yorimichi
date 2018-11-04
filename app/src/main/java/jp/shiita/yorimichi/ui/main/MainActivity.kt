package jp.shiita.yorimichi.ui.main

import android.arch.lifecycle.ViewModelProvider
import android.arch.lifecycle.ViewModelProviders
import android.content.Intent
import android.databinding.DataBindingUtil
import android.os.Bundle
import android.support.v4.view.GravityCompat
import android.support.v4.widget.DrawerLayout
import android.view.MenuItem
import android.widget.ImageView
import android.widget.TextView
import dagger.android.support.DaggerAppCompatActivity
import jp.shiita.yorimichi.R
import jp.shiita.yorimichi.data.UserInfo
import jp.shiita.yorimichi.databinding.ActMainBinding
import jp.shiita.yorimichi.ui.main.MainViewModel.HomeAsUpType.OPEN_DRAWER
import jp.shiita.yorimichi.ui.main.MainViewModel.HomeAsUpType.POP_BACK_STACK
import jp.shiita.yorimichi.ui.mypage.MyPageFragment
import jp.shiita.yorimichi.ui.note.NoteFragment
import jp.shiita.yorimichi.ui.setting.SettingFragment
import jp.shiita.yorimichi.ui.shop.ShopFragment
import jp.shiita.yorimichi.util.addFragment
import jp.shiita.yorimichi.util.bindImageCloudStrage
import jp.shiita.yorimichi.util.observe
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

        viewModel.createOrUpdateUser()
        observe()
    }

    private fun observe() {
        viewModel.also { vm ->
            // ToolBar
            vm.titleEvent.observe(this) {
                supportActionBar?.let { bar ->
                    if (it == R.string.app_name) {
                        bar.setDisplayShowCustomEnabled(true)
                        bar.setDisplayShowTitleEnabled(false)
                        bar.setCustomView(R.layout.action_bar)
                    }
                    else {
                        bar.setDisplayShowCustomEnabled(false)
                        bar.setDisplayShowTitleEnabled(true)
                        bar.setTitle(it)
                    }
                }
            }
            vm.homeAsUpIndicator.observe(this) { supportActionBar?.setHomeAsUpIndicator(it) }
            vm.displayHomeAsUpEnabled.observe(this) { supportActionBar?.setDisplayHomeAsUpEnabled(it) }

            vm.drawerLock.observe(this) { if (it) lockDrawer() else unlockDrawer() }
            vm.finishAppMessage.observe(this) {
                SimpleDialogFragment.newInstance(getString(it), false).apply {
                    setTargetFragment(null, REQUEST_FINISH_DIALOG)
                    show(supportFragmentManager, SimpleDialogFragment.TAG)
                }
            }
            vm.updatePointEvent.observe(this) {
                binding.navView
                        .getHeaderView(0)
                        .findViewById<TextView>(R.id.pointsText)
                        .text = getString(R.string.drawer_point_value, UserInfo.points)
            }
            vm.updateIconEvent.observe(this) {
                binding.navView
                        .getHeaderView(0)
                        .findViewById<ImageView>(R.id.iconImage)
                        .bindImageCloudStrage("gs://${UserInfo.iconBucket}", UserInfo.iconFileName)
                // TODO: 応急処置
            }
        }
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        when (item?.itemId) {
            android.R.id.home -> when (viewModel.homeAsUpType) {
                OPEN_DRAWER -> binding.drawerLayout.openDrawer(GravityCompat.START)
                POP_BACK_STACK -> supportFragmentManager.popBackStack()
            }
            else -> return super.onOptionsItemSelected(item)
        }
        return true
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode != REQUEST_FINISH_DIALOG) return

        when (resultCode) {
            RESULT_OK, RESULT_CANCELED -> finish()
        }
    }

    private fun lockDrawer() = binding.drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED)

    private fun unlockDrawer() = binding.drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED)

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

    companion object {
        private const val REQUEST_FINISH_DIALOG = 0
    }
}
