package jp.shiita.yorimichi.ui.notes

import android.arch.lifecycle.ViewModelProvider
import android.arch.lifecycle.ViewModelProviders
import android.databinding.DataBindingUtil
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import dagger.android.support.DaggerFragment
import jp.shiita.yorimichi.R
import jp.shiita.yorimichi.databinding.FragNotesBinding
import jp.shiita.yorimichi.ui.main.MainViewModel
import javax.inject.Inject

class NotesFragment : DaggerFragment() {
    @Inject lateinit var viewModelFactory: ViewModelProvider.Factory
    private val mainViewModel: MainViewModel
            by lazy { ViewModelProviders.of(activity!!, viewModelFactory).get(MainViewModel::class.java)}
    private val viewModel: NotesViewModel
            by lazy { ViewModelProviders.of(this, viewModelFactory).get(NotesViewModel::class.java) }
    private lateinit var binding: FragNotesBinding

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        setHasOptionsMenu(true)
        binding = DataBindingUtil.inflate(inflater, R.layout.frag_notes, container, false)
        return binding.root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        binding.setLifecycleOwner(this)
        binding.viewModel = viewModel
        mainViewModel.setupActionBar(R.string.title_read_notes)

        observe()
    }

    private fun observe() {

    }

    companion object {
        val TAG: String = NotesFragment::class.java.simpleName
        fun newInstance() = NotesFragment()
    }
}