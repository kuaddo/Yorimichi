package jp.shiita.yorimichi.ui.history

import android.arch.lifecycle.ViewModelProvider
import android.arch.lifecycle.ViewModelProviders
import android.databinding.DataBindingUtil
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import dagger.android.support.DaggerFragment
import jp.shiita.yorimichi.R
import jp.shiita.yorimichi.data.PlaceResult
import jp.shiita.yorimichi.databinding.FragHistoryBinding
import jp.shiita.yorimichi.ui.main.MainViewModel
import jp.shiita.yorimichi.ui.notes.NotesFragment
import jp.shiita.yorimichi.util.observe
import jp.shiita.yorimichi.util.replaceFragment
import org.threeten.bp.LocalDateTime
import javax.inject.Inject

class HistoryFragment : DaggerFragment() {
    @Inject lateinit var viewModelFactory: ViewModelProvider.Factory
    private val mainViewModel: MainViewModel
            by lazy { ViewModelProviders.of(activity!!, viewModelFactory).get(MainViewModel::class.java)}
    private val viewModel: HistoryViewModel
            by lazy { ViewModelProviders.of(this, viewModelFactory).get(HistoryViewModel::class.java) }
    private lateinit var binding: FragHistoryBinding
    private lateinit var adapter: HistoryAdapter

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        setHasOptionsMenu(true)
        binding = DataBindingUtil.inflate(inflater, R.layout.frag_history, container, false)
        return binding.root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        binding.setLifecycleOwner(this)
        binding.viewModel = viewModel
        mainViewModel.setupActionBar(R.string.title_history)

        adapter = HistoryAdapter(context!!, mutableListOf(), mutableListOf(), ::showNotesFragment)
        binding.historyRecyclerView.adapter = adapter

        viewModel.getHistory()
        observe()
    }

    private fun observe() {
        viewModel.placesAndDateTimes.observe(this) { adapter.reset(it.first, it.second) }
    }

    private fun showNotesFragment(place: PlaceResult.Place, dateTime: LocalDateTime) {
        val fragment = NotesFragment.newInstance(place.placeId, place.name, dateTime)
        fragmentManager?.replaceFragment(R.id.container, fragment, NotesFragment.TAG)
    }

    companion object {
        val TAG: String = HistoryFragment::class.java.simpleName
        fun newInstance() = HistoryFragment()
    }
}