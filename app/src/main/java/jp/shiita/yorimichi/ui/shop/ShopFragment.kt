package jp.shiita.yorimichi.ui.shop

import android.arch.lifecycle.ViewModelProvider
import android.arch.lifecycle.ViewModelProviders
import android.databinding.DataBindingUtil
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import dagger.android.support.DaggerFragment
import jp.shiita.yorimichi.R
import jp.shiita.yorimichi.databinding.FragShopBinding
import jp.shiita.yorimichi.ui.main.MainViewModel
import jp.shiita.yorimichi.ui.notes.NoteAdapter
import jp.shiita.yorimichi.util.observe
import javax.inject.Inject

class ShopFragment : DaggerFragment() {
    @Inject lateinit var viewModelFactory: ViewModelProvider.Factory
    private val mainViewModel: MainViewModel
            by lazy { ViewModelProviders.of(activity!!, viewModelFactory).get(MainViewModel::class.java)}
    private val viewModel: ShopViewModel
            by lazy { ViewModelProviders.of(this, viewModelFactory).get(ShopViewModel::class.java) }
    private lateinit var binding: FragShopBinding
    private lateinit var noteAdapter: NoteAdapter

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        setHasOptionsMenu(true)
        binding = DataBindingUtil.inflate(inflater, R.layout.frag_shop, container, false)
        return binding.root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        binding.setLifecycleOwner(this)
        binding.viewModel = viewModel
        mainViewModel.setupActionBar(R.string.title_shop)

        noteAdapter = NoteAdapter(context!!, mutableListOf())
        binding.notesRecyclerView.adapter = noteAdapter

        observe()
    }

    private fun observe() {
        viewModel.posts.observe(this) { noteAdapter.reset(it) }
        viewModel.pointsEvent.observe(this) { mainViewModel.updatePoints() }
    }

    companion object {
        val TAG: String = ShopFragment::class.java.simpleName
        fun newInstance() = ShopFragment()
    }
}