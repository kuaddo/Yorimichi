package jp.shiita.yorimichi.ui.search

import android.arch.lifecycle.LiveData
import android.arch.lifecycle.MutableLiveData
import android.arch.lifecycle.ViewModel
import com.google.android.gms.maps.model.LatLng
import jp.shiita.yorimichi.live.SingleUnitLiveEvent
import jp.shiita.yorimichi.util.map
import javax.inject.Inject

class SearchViewModel @Inject constructor() : ViewModel() {
    val searchEvent: LiveData<Unit>
        get() = _searchEvent
    val latLng: LiveData<LatLng>
        get() = _latLng
    val selected: LiveData<Boolean>
        get() = _latLng.map { it != null }

    private val _searchEvent = SingleUnitLiveEvent()
    private val _latLng = MutableLiveData<LatLng>()

    fun search() {
        _searchEvent.call()
    }

    fun select(latLng: LatLng) {
        _latLng.postValue(latLng)
    }
}