package jp.shiita.yorimichi.ui.search

import android.arch.lifecycle.LiveData
import android.arch.lifecycle.MutableLiveData
import android.arch.lifecycle.ViewModel
import com.google.android.gms.maps.model.LatLng
import jp.shiita.yorimichi.live.SingleUnitLiveEvent
import jp.shiita.yorimichi.util.map
import javax.inject.Inject

class SearchViewModel @Inject constructor() : ViewModel() {
    val searchEvent       : LiveData<Unit>    get() = _searchEvent
    val requiredTimeString: LiveData<String>  get() = requiredTimeMinute.map { toTimeString(it) }
    val latLng            : LiveData<LatLng>  get() = _latLng
    val selected          : LiveData<Boolean> get() = _latLng.map { it != null }

    val requiredTimeMinute = MutableLiveData<Int>().apply { value = 80 }

    private val _searchEvent        = SingleUnitLiveEvent()
    private val _latLng             = MutableLiveData<LatLng>()

    fun search() {
        _searchEvent.call()
    }

    fun select(latLng: LatLng) = _latLng.postValue(latLng)

    private fun toTimeString(timeMinute: Int): String =
            if (timeMinute < 60) "%02d分".format(timeMinute)
            else "%d時間%02d分".format(timeMinute / 60, timeMinute % 60)
}