package jp.shiita.yorimichi.ui.main

import android.arch.lifecycle.ViewModelProvider
import android.arch.lifecycle.ViewModelProviders
import android.content.Intent
import android.databinding.DataBindingUtil
import android.os.Bundle
import android.os.PersistableBundle
import android.support.v4.view.GravityCompat
import android.support.v4.widget.DrawerLayout
import android.support.v7.widget.RecyclerView
import android.view.MenuItem
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import com.stephentuso.welcome.WelcomeHelper
import dagger.android.support.DaggerAppCompatActivity
import jp.shiita.yorimichi.R
import jp.shiita.yorimichi.data.UserInfo
import jp.shiita.yorimichi.databinding.ActMainBinding
import jp.shiita.yorimichi.ui.dialog.SimpleDialogFragment
import jp.shiita.yorimichi.ui.history.HistoryFragment
import jp.shiita.yorimichi.ui.main.MainViewModel.HomeAsUpType.OPEN_DRAWER
import jp.shiita.yorimichi.ui.main.MainViewModel.HomeAsUpType.POP_BACK_STACK
import jp.shiita.yorimichi.ui.setting.SettingFragment
import jp.shiita.yorimichi.ui.shop.ShopFragment
import jp.shiita.yorimichi.ui.tutorial.TutorialActivity
import jp.shiita.yorimichi.util.addFragment
import jp.shiita.yorimichi.util.bindImageCloudStorage
import jp.shiita.yorimichi.util.observe
import jp.shiita.yorimichi.util.replaceFragment
import javax.inject.Inject


class MainActivity : DaggerAppCompatActivity() {
    @Inject lateinit var viewModelFactory: ViewModelProvider.Factory
    private val viewModel: MainViewModel
            by lazy { ViewModelProviders.of(this, viewModelFactory).get(MainViewModel::class.java) }
    private val binding: ActMainBinding
            by lazy { DataBindingUtil.setContentView<ActMainBinding>(this, R.layout.act_main) }
    private lateinit var adapter: IconAdapter
    private lateinit var welcomeHelper: WelcomeHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setSupportActionBar(binding.toolbar)
        setupDrawer()
        binding.setLifecycleOwner(this)
        binding.viewModel = viewModel

        welcomeHelper = WelcomeHelper(this, TutorialActivity::class.java)
        val isShownTutorial = welcomeHelper.show(savedInstanceState, REQUEST_TUTORIAL)

        adapter = IconAdapter(this, mutableListOf(), viewModel::changeIcon)
        binding.navView
                .getHeaderView(0).also { header ->
                    val recyclerView = header.findViewById<RecyclerView>(R.id.recyclerView)
                    recyclerView.adapter = adapter
                    header.findViewById<ImageView>(R.id.iconImage).setOnClickListener {
                        if (recyclerView.visibility == View.VISIBLE) recyclerView.visibility = View.GONE
                        else                                         recyclerView.visibility = View.VISIBLE
                    }
                }

        if (savedInstanceState == null && !isShownTutorial) {
            supportFragmentManager.addFragment(R.id.container, MainFragment.newInstance())
        }

        viewModel.createOrUpdateUser()
        observe()
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

        when (requestCode) {
            REQUEST_TUTORIAL -> when (resultCode) {
                // tutorialを表示した場合には、後からfragmentをaddする
                RESULT_OK -> supportFragmentManager.addFragment(R.id.container, MainFragment.newInstance())
                // backボタンでチュートリアルを中断した場合には、アプリを終了する
                RESULT_CANCELED -> finish()
            }
            REQUEST_FINISH_DIALOG -> when (resultCode) {
                RESULT_OK, RESULT_CANCELED -> finish()
            }
        }
    }

    override fun onSaveInstanceState(outState: Bundle?, outPersistentState: PersistableBundle?) {
        super.onSaveInstanceState(outState, outPersistentState)
        welcomeHelper.onSaveInstanceState(outState)
    }

    private fun observe() {
        viewModel.also { vm ->
            vm.icons.observe(this) { adapter.reset(it) }

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
                        .bindImageCloudStorage(UserInfo.iconBucket, UserInfo.iconFileName)
            }
        }
    }

    private fun lockDrawer() = binding.drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED)

    private fun unlockDrawer() = binding.drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED)

    private fun setupDrawer() {
        binding.navView.setNavigationItemSelectedListener { item ->
            lockDrawer()
            when (item.itemId) {
                R.id.menu_drawer_note    -> supportFragmentManager.replaceFragment(R.id.container, HistoryFragment.newInstance(), HistoryFragment.TAG)
                R.id.menu_drawer_shop    -> supportFragmentManager.replaceFragment(R.id.container, ShopFragment.newInstance(), ShopFragment.TAG)
                R.id.menu_drawer_setting -> supportFragmentManager.replaceFragment(R.id.container, SettingFragment.newInstance(), SettingFragment.TAG)
            }
            binding.drawerLayout.closeDrawers()
            true
        }
    }

    companion object {
        private const val REQUEST_TUTORIAL = 1
        private const val REQUEST_FINISH_DIALOG = 2
    }
}
