package jp.shiita.yorimichi.ui.remind

import android.arch.lifecycle.LiveData
import android.arch.lifecycle.MutableLiveData
import android.arch.lifecycle.ViewModel
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.addTo
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
import org.threeten.bp.ZoneOffset
import java.net.URLEncoder
import javax.inject.Inject

class RemindViewModel @Inject constructor(
        private val repository: YorimichiRepository,
        private val scheduler: BaseSchedulerProvider
) : ViewModel() {
    val places: LiveData<List<PlaceResult.Place>> get() = _places
    val zoomBounds: LiveData<LatLngBounds> get() = _zoomBounds
    val selected: LiveData<Boolean> get() = _selectedLatLng.map { it != null }
    val timeString: LiveData<String> get() = _hour.combineLatest(_minute) { h, m -> "%02d:%02d".format(h, m) }
    val reachedVisible: LiveData<Boolean> get() = _reachedVisible
    val gotoVisible: LiveData<Boolean> get() = _gotoVisible
    val placeVisible: LiveData<Boolean> get() = _placeVisible
    val timeVisible: LiveData<Boolean> get() = _timeVisible
    val finishVisible: LiveData<Boolean> get() = _finishVisible
    val finishWithNeed: LiveData<Boolean> get() = _finishWithNeed

    val showTimePickerEvent: LiveData<Pair<Int, Int>> get() = _showTimePickerEvent
    val notificationEvent: LiveData<Triple<Int, List<LatLng>, Long>> get() = _notificationEvent
    val finishEvent: LiveData<Unit> get() = _finishEvent

    private val _places = MutableLiveData<List<PlaceResult.Place>>()
    private val _zoomBounds = MutableLiveData<LatLngBounds>()
    private val _selectedLatLng = MutableLiveData<LatLng>()
    private val _hour = MutableLiveData<Int>()
    private val _minute = MutableLiveData<Int>()
    private val _reachedVisible = MutableLiveData<Boolean>().apply { value = true }
    private val _gotoVisible = MutableLiveData<Boolean>()
    private val _placeVisible = MutableLiveData<Boolean>()
    private val _timeVisible = MutableLiveData<Boolean>()
    private val _finishVisible = MutableLiveData<Boolean>()
    private val _finishWithNeed = MutableLiveData<Boolean>()

    private val _showTimePickerEvent = SingleLiveEvent<Pair<Int, Int>>()
    private val _notificationEvent = SingleLiveEvent<Triple<Int, List<LatLng>, Long>>()     // (minute, routes, timeInMillis)
    private val _finishEvent = SingleUnitLiveEvent()

    lateinit var startLatLng: LatLng
    private val disposables = CompositeDisposable()

    init {
        val time = LocalDateTime.now().plusMinutes(30)
        _hour.value = time.hour
        _minute.value = time.minute
    }

    override fun onCleared() = disposables.clear()

    fun select(latLng: LatLng?) = _selectedLatLng.postValue(latLng)

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
        val origin = UserInfo.latLng?.toSimpleString() ?: return
        val destination = if (_selectedLatLng.value == null) startLatLng.toSimpleString()
                          else                               _selectedLatLng.value!!.toSimpleString()
        val now = LocalDateTime.now()
        val hour = _hour.value ?: now.hour
        val minute = _minute.value ?: now.minute
        val dateTime = LocalDateTime.of(now.year, now.month, now.dayOfMonth, hour, minute).minusMinutes(5)  // 5分前
        repository.getDirection(origin, destination)
                .subscribeOn(scheduler.io())
                .observeOn(scheduler.ui())
                .subscribeBy(
                        onSuccess = {
                            val route = it.routes[0]
                            val routes = route.overviewPolyline.routes
                            val timeInMillis = dateTime.minusSeconds(route.totalDurationSecond.toLong())
                                    .toEpochSecond(ZoneOffset.ofHours(9)) * 1000L
                            _notificationEvent.postValue(Triple(route.totalDurationSecond / 60, routes, timeInMillis))
                        },
                        onError = {}
                )
                .addTo(disposables)

        _timeVisible.value = false
        _finishVisible.value = true
        _finishWithNeed.value = true
        _selectedLatLng.postValue(null)
    }

    fun finish() {
        _finishEvent.call()
    }

    fun searchPlaces(keywords: List<String>) {
        if (keywords.isEmpty()) return
        val latLng = UserInfo.latLng ?: return

        // "+"がエンコードされないように自前でエンコード処理を行う
        val keyword = keywords.joinToString(separator = "+OR+") { URLEncoder.encode(it, "UTF-8") }
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
                                _selectedLatLng.postValue(null)
                            }
                        },
                        onError = {}
                )
                .addTo(disposables)
    }
}