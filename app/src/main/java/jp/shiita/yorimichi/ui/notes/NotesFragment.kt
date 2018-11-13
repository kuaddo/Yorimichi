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
import jp.shiita.yorimichi.util.observe
import javax.inject.Inject

class NotesFragment : DaggerFragment() {
    @Inject lateinit var viewModelFactory: ViewModelProvider.Factory
    private val mainViewModel: MainViewModel
            by lazy { ViewModelProviders.of(activity!!, viewModelFactory).get(MainViewModel::class.java)}
    private val viewModel: NotesViewModel
            by lazy { ViewModelProviders.of(this, viewModelFactory).get(NotesViewModel::class.java) }
    private val placeId: String by lazy { arguments!!.getString(ARGS_PLACE_ID) }
    private lateinit var binding: FragNotesBinding
    private lateinit var noteAdapter: NoteAdapter

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

        noteAdapter = NoteAdapter(context!!, mutableListOf())
        binding.notesRecyclerView.adapter = noteAdapter
        viewModel.getPlacePosts(placeId)

        observe()
    }

    private fun observe() {
        viewModel.posts.observe(this) { noteAdapter.reset(it) }
    }

    companion object {
        val TAG: String = NotesFragment::class.java.simpleName
        private const val ARGS_PLACE_ID = "argsPlaceId"
        fun newInstance(placeId: String) = NotesFragment().apply {
            arguments = Bundle().apply { putString(ARGS_PLACE_ID, placeId) }
        }
    }
}