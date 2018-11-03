package jp.shiita.yorimichi.ui.mypage

import android.arch.lifecycle.ViewModelProvider
import android.arch.lifecycle.ViewModelProviders
import android.databinding.DataBindingUtil
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import dagger.android.support.DaggerFragment
import jp.shiita.yorimichi.R
import jp.shiita.yorimichi.databinding.FragMyPageBinding
import jp.shiita.yorimichi.ui.main.MainViewModel
import javax.inject.Inject

class MyPageFragment : DaggerFragment() {
    @Inject lateinit var viewModelFactory: ViewModelProvider.Factory
    private val mainViewModel: MainViewModel
            by lazy { ViewModelProviders.of(activity!!, viewModelFactory).get(MainViewModel::class.java)}
    private val viewModel: MyPageViewModel
            by lazy { ViewModelProviders.of(this, viewModelFactory).get(MyPageViewModel::class.java) }
    private lateinit var binding: FragMyPageBinding

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        setHasOptionsMenu(true)
        binding = DataBindingUtil.inflate(inflater, R.layout.frag_my_page, container, false)
        return binding.root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        binding.setLifecycleOwner(this)
        binding.viewModel = viewModel
        mainViewModel.setupActionBar(R.string.title_my_page)

        observe()
    }

    private fun observe() {

    }

    companion object {
        val TAG: String = MyPageFragment::class.java.simpleName
        fun newInstance() = MyPageFragment()
    }
}