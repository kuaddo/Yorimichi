package jp.shiita.yorimichi.ui.notes

import android.arch.lifecycle.ViewModelProvider
import android.arch.lifecycle.ViewModelProviders
import android.databinding.DataBindingUtil
import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.PagerSnapHelper
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import dagger.android.support.DaggerFragment
import jp.shiita.yorimichi.R
import jp.shiita.yorimichi.databinding.FragNotesBinding
import jp.shiita.yorimichi.ui.main.MainViewModel
import jp.shiita.yorimichi.util.loadAd
import jp.shiita.yorimichi.util.observe
import jp.shiita.yorimichi.util.toUploadDateString
import org.threeten.bp.LocalDateTime
import javax.inject.Inject

class NotesFragment : DaggerFragment() {
    @Inject lateinit var viewModelFactory: ViewModelProvider.Factory
    private val mainViewModel: MainViewModel
            by lazy { ViewModelProviders.of(activity!!, viewModelFactory).get(MainViewModel::class.java)}
    private val viewModel: NotesViewModel
            by lazy { ViewModelProviders.of(this, viewModelFactory).get(NotesViewModel::class.java) }
    private val placeId: String by lazy { arguments!!.getString(ARGS_PLACE_ID) }
    private val dateTime: String by lazy { arguments!!.getString(ARGS_DATE_TIME) }
    private val placeText: String by lazy { arguments!!.getString(ARGS_PLACE_TEXT) }
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
        mainViewModel.setDrawerLock(true)

        binding.adView.loadAd()
        noteAdapter = NoteAdapter(context!!, mutableListOf())
        binding.notesRecyclerView.let { rv ->
            val layoutManager = rv.layoutManager as LinearLayoutManager
            rv.adapter = noteAdapter
            rv.addOnScrollListener(object : RecyclerView.OnScrollListener() {
                var beforePage = -1
                override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                    super.onScrolled(recyclerView, dx, dy)
                    val first = layoutManager.findFirstVisibleItemPosition()
                    val last = layoutManager.findLastVisibleItemPosition()

                    // スクロールが完全に終了した瞬間
                    if (beforePage != first && first == last) {
                        beforePage = first
                        viewModel.setCurrentPage(first)
                    }
                }
            })
            PagerSnapHelper().attachToRecyclerView(rv)
        }

        viewModel.getPlacePosts(placeId, dateTime)
        viewModel.setPlaceText(placeText)
        observe()
    }

    private fun observe() {
        viewModel.posts.observe(this) { noteAdapter.reset(it) }
        viewModel.scrollBackEvent.observe(this) { binding.notesRecyclerView.scrollToPosition(it) }
        viewModel.scrollForwardEvent.observe(this) { binding.notesRecyclerView.scrollToPosition(it) }
    }

    companion object {
        val TAG: String = NotesFragment::class.java.simpleName
        private const val ARGS_PLACE_ID = "argsPlaceId"
        private const val ARGS_DATE_TIME = "argsDateTime"
        private const val ARGS_PLACE_TEXT = "argsPlaceText"

        fun newInstance(placeId: String, placeText: String, dateTime: LocalDateTime = LocalDateTime.now()) =
                NotesFragment().apply {
                    arguments = Bundle().apply {
                        putString(ARGS_PLACE_ID, placeId)
                        putString(ARGS_DATE_TIME, dateTime.toUploadDateString())
                        putString(ARGS_PLACE_TEXT, placeText)
                    }
                }
    }
}