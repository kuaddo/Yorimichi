package jp.shiita.yorimichi.ui.map

import android.arch.lifecycle.LiveData
import android.arch.lifecycle.MutableLiveData
import android.arch.lifecycle.ViewModel
import com.google.android.gms.maps.model.LatLng
import jp.shiita.yorimichi.live.SingleLiveEvent
import javax.inject.Inject

class MapViewModel @Inject constructor() : ViewModel() {
    val latLng: LiveData<LatLng> get() = _latLng
    val cameraMoveEvent: LiveData<LatLng> get() = _cameraMoveEvent

    private val _latLng = MutableLiveData<LatLng>()
    private val _cameraMoveEvent = SingleLiveEvent<LatLng>()

    private var isLocationObserved = false

    fun setLatLng(latLng: LatLng) {
        _latLng.postValue(latLng)
        if (!isLocationObserved) {
            isLocationObserved = true
            _cameraMoveEvent.postValue(latLng)
        }
    }
}