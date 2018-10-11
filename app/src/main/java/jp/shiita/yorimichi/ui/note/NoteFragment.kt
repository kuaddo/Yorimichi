package jp.shiita.yorimichi.ui.note

import android.arch.lifecycle.ViewModelProvider
import android.arch.lifecycle.ViewModelProviders
import android.databinding.DataBindingUtil
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import dagger.android.support.DaggerFragment
import jp.shiita.yorimichi.R
import jp.shiita.yorimichi.databinding.FragNoteBinding
import jp.shiita.yorimichi.ui.main.MainViewModel
import javax.inject.Inject

class NoteFragment : DaggerFragment() {
    @Inject lateinit var viewModelFactory: ViewModelProvider.Factory
    private val mainViewModel: MainViewModel
            by lazy { ViewModelProviders.of(activity!!, viewModelFactory).get(MainViewModel::class.java)}
    private val viewModel: NoteViewModel
            by lazy { ViewModelProviders.of(this, viewModelFactory).get(NoteViewModel::class.java) }
    private lateinit var binding: FragNoteBinding

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = DataBindingUtil.inflate(inflater, R.layout.frag_note, container, false)
        return binding.root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        binding.setLifecycleOwner(this)
        binding.viewModel = viewModel
        mainViewModel.setupActionBar(R.string.title_note)

        observe()
    }

    private fun observe() {

    }

    companion object {
        val TAG: String = NoteFragment::class.java.simpleName
        fun newInstance() = NoteFragment()
    }
}