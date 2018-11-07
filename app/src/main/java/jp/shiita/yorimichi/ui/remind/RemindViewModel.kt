package jp.shiita.yorimichi.ui.remind

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
import jp.shiita.yorimichi.live.SingleUnitLiveEvent
import jp.shiita.yorimichi.scheduler.BaseSchedulerProvider
import jp.shiita.yorimichi.util.combineLatest
import jp.shiita.yorimichi.util.map
import jp.shiita.yorimichi.util.toSimpleString
import org.threeten.bp.LocalDateTime
import java.net.URLEncoder
import javax.inject.Inject

class RemindViewModel @Inject constructor(
        private val repository: YorimichiRepository,
        private val scheduler: BaseSchedulerProvider
) : ViewModel() {
    val places: LiveData<List<PlaceResult.Place>> get() = _places
    val zoomBounds: LiveData<LatLngBounds> get() = _zoomBounds
    val selected: LiveData<Boolean> get() = _latLng.map { it != null }
    val timeString: LiveData<String> get() = _hour.combineLatest(_minute) { h, m -> "$h:$m" }
    val reachedVisible: LiveData<Boolean> get() = _reachedVisible
    val gotoVisible: LiveData<Boolean> get() = _gotoVisible
    val placeVisible: LiveData<Boolean> get() = _placeVisible
    val timeVisible: LiveData<Boolean> get() = _timeVisible
    val finishVisible: LiveData<Boolean> get() = _finishVisible
    val finishWithNeed: LiveData<Boolean> get() = _finishWithNeed

    val showTimePickerEvent: LiveData<Pair<Int, Int>> get() = _showTimePickerEvent
    val finishEvent: LiveData<Unit> get() = _finishEvent

    private val _places = MutableLiveData<List<PlaceResult.Place>>()
    private val _zoomBounds = MutableLiveData<LatLngBounds>()
    private val _latLng = MutableLiveData<LatLng>()
    private val _hour = MutableLiveData<Int>()
    private val _minute = MutableLiveData<Int>()
    private val _reachedVisible = MutableLiveData<Boolean>().apply { value = true }
    private val _gotoVisible = MutableLiveData<Boolean>()
    private val _placeVisible = MutableLiveData<Boolean>()
    private val _timeVisible = MutableLiveData<Boolean>()
    private val _finishVisible = MutableLiveData<Boolean>()
    private val _finishWithNeed = MutableLiveData<Boolean>()

    private val _showTimePickerEvent = SingleLiveEvent<Pair<Int, Int>>()
    private val _finishEvent = SingleUnitLiveEvent()

    init {
        val time = LocalDateTime.now().plusMinutes(30)
        _hour.value = time.hour
        _minute.value = time.minute
    }

    fun select(latLng: LatLng?) = _latLng.postValue(latLng)

    fun setTime(hour: Int, minute: Int) {
        _hour.postValue(hour)
        _minute.postValue(minute)
    }

    fun showTimePicker() = _showTimePickerEvent.postValue((_hour.value ?: 0) to (_minute.value ?: 0))

    fun need() {
        _reachedVisible.value = false
        _gotoVisible.value = true
    }

    fun noNeed() {
        _reachedVisible.value = false
        _finishVisible.value = true
        _finishWithNeed.value = false
    }

    fun goBack() {
        _gotoVisible.value = false
        _timeVisible.value = true
    }

    fun goOther() {
        _gotoVisible.value = false
        _placeVisible.value = true
    }

    fun gotoPlace() {
        _placeVisible.value = false
        _timeVisible.value = true
    }

    fun timeSelected() {
        _timeVisible.value = false
        _finishVisible.value = true
        _finishWithNeed.value = true
        // TODO: set notification, post null to _latLng
    }

    fun finish() {
        _finishEvent.call()
    }

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
}