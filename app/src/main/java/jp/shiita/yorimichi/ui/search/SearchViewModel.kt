package jp.shiita.yorimichi.ui.search

import android.arch.lifecycle.LiveData
import android.arch.lifecycle.MutableLiveData
import android.arch.lifecycle.ViewModel
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import io.reactivex.rxkotlin.subscribeBy
import jp.shiita.yorimichi.data.PlaceResult
import jp.shiita.yorimichi.data.UserInfo
import jp.shiita.yorimichi.data.api.YorimichiRepository
import jp.shiita.yorimichi.live.SingleLiveEvent
import jp.shiita.yorimichi.scheduler.BaseSchedulerProvider
import jp.shiita.yorimichi.util.map
import jp.shiita.yorimichi.util.toSimpleString
import java.net.URLEncoder
import javax.inject.Inject

class SearchViewModel @Inject constructor(
        private val repository: YorimichiRepository,
        private val scheduler: BaseSchedulerProvider
) : ViewModel() {
    val places: LiveData<List<PlaceResult.Place>> get() = _places
    val zoomBounds: LiveData<LatLngBounds> get() = _zoomBounds
    val requiredTimeString: LiveData<String> get() = requiredTimeMinute.map { toTimeString(it) }
    val selected          : LiveData<Boolean> get() = _latLng.map { it != null }

    val searchRadiusEvent : LiveData<Int> get() = _searchRadiusEvent
    val directionsEvent: LiveData<LatLng> get() = _directionsEvent

    val requiredTimeMinute = MutableLiveData<Int>().apply { value = 80 }

    private val _places = MutableLiveData<List<PlaceResult.Place>>()
    private val _zoomBounds = MutableLiveData<LatLngBounds>()
    private val _latLng = MutableLiveData<LatLng>()

    private val _searchRadiusEvent = SingleLiveEvent<Int>()
    private val _directionsEvent = SingleLiveEvent<LatLng>()

    fun search() {
        // 1分で歩ける距離は80mらしい
        val radius = requiredTimeMinute.value!! * 80
        _searchRadiusEvent.postValue(radius)
    }

    fun setRoute() {
        _directionsEvent.postValue(_latLng.value)
        _latLng.postValue(null)
    }

    fun select(latLng: LatLng?) = _latLng.postValue(latLng)

    fun searchPlaces(keywords: List<String>) {
        if (keywords.isEmpty()) return
        val latLng = UserInfo.latLng ?: return

        // "+"がエンコードされないように自前でエンコード処理を行う
        val keyword = keywords.map { URLEncoder.encode(it, "UTF-8") }.joinToString(separator = "+OR+")
        // 50kmはNearby Searchの限界値
        repository.getPlacesWithKeyword(latLng.toSimpleString(), 50000, keyword)
                .subscribeOn(scheduler.io())
                .observeOn(scheduler.ui())
                .subscribeBy(
                        onSuccess = { result ->
                            if (result.results.isEmpty()) {
                            }
                            else {
                                result.results.forEach { it.setDistance(latLng) }
                                _zoomBounds.postValue(result.calcBounds(latLng.latitude, latLng.longitude))
                                _places.postValue(result.results.sortedBy { it.getDistance() })
                                _latLng.postValue(null)
                            }
                        },
                        onError = {}
                )
    }

    private fun toTimeString(timeMinute: Int): String =
            if (timeMinute < 60) "%02d分".format(timeMinute)
            else "%d時間%02d分".format(timeMinute / 60, timeMinute % 60)
}