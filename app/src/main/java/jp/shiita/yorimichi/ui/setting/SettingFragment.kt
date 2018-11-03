package jp.shiita.yorimichi.ui.setting

import android.arch.lifecycle.ViewModelProvider
import android.arch.lifecycle.ViewModelProviders
import android.content.Intent
import android.databinding.DataBindingUtil
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.google.android.gms.oss.licenses.OssLicensesMenuActivity
import dagger.android.support.DaggerFragment
import jp.shiita.yorimichi.R
import jp.shiita.yorimichi.databinding.FragSettingBinding
import jp.shiita.yorimichi.ui.main.MainViewModel
import jp.shiita.yorimichi.util.observe
import javax.inject.Inject

class SettingFragment : DaggerFragment() {
    @Inject lateinit var viewModelFactory: ViewModelProvider.Factory
    private val mainViewModel: MainViewModel
            by lazy { ViewModelProviders.of(activity!!, viewModelFactory).get(MainViewModel::class.java)}
    private val viewModel: SettingViewModel
            by lazy { ViewModelProviders.of(this, viewModelFactory).get(SettingViewModel::class.java) }
    private lateinit var binding: FragSettingBinding

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        setHasOptionsMenu(true)
        binding = DataBindingUtil.inflate(inflater, R.layout.frag_setting, container, false)
        return binding.root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        binding.setLifecycleOwner(this)
        binding.viewModel = viewModel
        mainViewModel.setupActionBar(R.string.title_setting)

        observe()
    }

    private fun observe() {
        viewModel.showLicensesEvent.observe(this) {
            startActivity(Intent(context, OssLicensesMenuActivity::class.java))
        }
    }

    companion object {
        val TAG: String = SettingFragment::class.java.simpleName
        fun newInstance() = SettingFragment()
    }
}