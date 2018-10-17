package jp.shiita.yorimichi.ui.search

import android.arch.lifecycle.LiveData
import android.arch.lifecycle.ViewModel
import jp.shiita.yorimichi.live.SingleUnitLiveEvent
import javax.inject.Inject

class SearchViewModel @Inject constructor() : ViewModel() {
    val searchEvent: LiveData<Unit>
        get() = _searchEvent

    private val _searchEvent = SingleUnitLiveEvent()

    fun search() {
        _searchEvent.call()
    }
}