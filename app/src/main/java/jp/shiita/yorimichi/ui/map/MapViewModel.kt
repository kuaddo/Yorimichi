package jp.shiita.yorimichi.ui.map

import android.arch.lifecycle.LiveData
import android.arch.lifecycle.MutableLiveData
import android.arch.lifecycle.ViewModel
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import io.reactivex.Observable
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.addTo
import io.reactivex.rxkotlin.subscribeBy
import jp.shiita.yorimichi.data.PlaceResult
import jp.shiita.yorimichi.data.UserInfo
import jp.shiita.yorimichi.data.api.YorimichiRepository
import jp.shiita.yorimichi.live.SingleLiveEvent
import jp.shiita.yorimichi.live.SingleUnitLiveEvent
import jp.shiita.yorimichi.scheduler.BaseSchedulerProvider
import jp.shiita.yorimichi.util.distance
import jp.shiita.yorimichi.util.toSimpleString
import java.net.URLEncoder
import java.util.concurrent.TimeUnit
import javax.inject.Inject

class MapViewModel @Inject constructor(
        private val repository: YorimichiRepository,
        private val scheduler: BaseSchedulerProvider
) : ViewModel() {
    val latLng: LiveData<LatLng> get() = _latLng
    val places: LiveData<List<PlaceResult.Place>> get() = _places
    val routes: LiveData<List<LatLng>> get() = _routes
    val targetPlace: LiveData<PlaceResult.Place> get() = _targetPlace
    val bucket: LiveData<String> get() = _bucket
    val fileName: LiveData<String> get() = _fileName
    val zoomBounds: LiveData<LatLngBounds> get() = _zoomBounds
    val smallPinPositions: LiveData<List<Int>> get() = _smallPinPositions
    val largePinPositions: LiveData<List<Int>> get() = _largePinPositions
    val selectedSmallPinPositions: LiveData<List<Int>> get() = _selectedSmallPinPositions
    val selectedLargePinPositions: LiveData<List<Int>> get() = _selectedLargePinPositions
    val showsSearchResult: LiveData<Boolean> get() = _showsSearchResult
    val showsChick: LiveData<Boolean> get() = _showsChick
    val isNear: LiveData<Boolean> get() = _isNear
    val isNearByLatestVisitLatLng: LiveData<Boolean> get() = _isNearByLatestVisitLatLng
    val canWriteNote: LiveData<Boolean> get() = _canWriteNote
    val chickMessage: LiveData<String> get() = _chickMessage

    val moveCameraEvent: LiveData<LatLng> get() = _moveCameraEvent
    val moveCameraZoomEvent: LiveData<LatLng> get() = _moveCameraZoomEvent
    val pointsEvent: LiveData<Int> get() = _pointsEvent
    val reachedEvent: LiveData<LatLng> get() = _reachedEvent
    val switchRotateEvent: LiveData<Boolean> get() = _switchRotateEvent
    val chickMessageChangeEvent: LiveData<Unit> get() = _chickMessageChangeEvent
    val showWriteNoteEvent: LiveData<Unit> get() = _showWriteNoteEvent
    val showReadNoteEvent: LiveData<Unit> get() = _showReadNoteEvent

    private val _latLng                    = MutableLiveData<LatLng>()
    private val _latestVisitLatLng         = MutableLiveData<LatLng>().apply { value = UserInfo.latestVisitLatLng }
    private val _places                    = MutableLiveData<List<PlaceResult.Place>>()
    private val _routes                    = MutableLiveData<List<LatLng>>()
    private val _targetPlace               = MutableLiveData<PlaceResult.Place>()
    private val _bucket                    = MutableLiveData<String>().apply { value = UserInfo.iconBucket }
    private val _fileName                  = MutableLiveData<String>().apply { value = UserInfo.iconFileName }
    private val _zoomBounds                = MutableLiveData<LatLngBounds>()
    private val _smallPinPositions         = MutableLiveData<List<Int>>()
    private val _largePinPositions         = MutableLiveData<List<Int>>()
    private val _selectedSmallPinPositions = MutableLiveData<List<Int>>()
    private val _selectedLargePinPositions = MutableLiveData<List<Int>>()
    private val _showsSearchResult         = MutableLiveData<Boolean>()
    private val _showsChick                = MutableLiveData<Boolean>()
    private val _isNear                    = MutableLiveData<Boolean>()
    private val _isNearByLatestVisitLatLng = MutableLiveData<Boolean>()
    private val _canWriteNote              = MutableLiveData<Boolean>().apply { value = UserInfo.canWriteNote }
    private val _chickMessage              = MutableLiveData<String>()

    private val _moveCameraEvent      = SingleLiveEvent<LatLng>()
    private val _moveCameraZoomEvent  = SingleLiveEvent<LatLng>()
    private val _pointsEvent          = SingleLiveEvent<Int>()
    private val _reachedEvent         = SingleLiveEvent<LatLng>()   // start地点のLatLng
    private val _switchRotateEvent    = SingleLiveEvent<Boolean>()
    private val _chickMessageChangeEvent = SingleUnitLiveEvent()
    private val _showWriteNoteEvent = SingleUnitLiveEvent()
    private val _showReadNoteEvent = SingleUnitLiveEvent()

    private var isLocationObserved = false
    private var placesSize = -1
    private var selectedPosition = -1
    private var first = -1
    private var last = -1
    private var startLatLng: LatLng? = null

    private val disposables = CompositeDisposable()
    var rotationEnabled = false

    override fun onCleared() = disposables.clear()

    init {
        Observable.interval(1, TimeUnit.MINUTES)
                .subscribeBy { _chickMessageChangeEvent.call() }
                .addTo(disposables)
    }

    fun setLatLng(latLng: LatLng) {
        _latLng.value = latLng
        _targetPlace.value?.let { place ->
            if (place.latLng.distance(latLng) < NEAR_DISTANCE) {
                _isNear.postValue(true)
            }
            else {
                _isNear.postValue(false)
            }
        }
        _latestVisitLatLng.value?.let { latestVisitLatLng ->
            if (latestVisitLatLng.distance(latLng) < NEAR_DISTANCE) {
                _isNearByLatestVisitLatLng.postValue(true)
            }
            else {
                _isNearByLatestVisitLatLng.postValue(false)
            }
        }
        if (!isLocationObserved) {
            isLocationObserved = true
            searchPlacesDefault()
        }
    }

    fun setIcon(bucket: String, name: String) {
        _bucket.postValue(bucket)
        _fileName.postValue(name)
    }

    fun setTarget(place: PlaceResult.Place) {
        _targetPlace.postValue(place)
        searchDirection("place_id:${place.placeId}")
    }

    fun setChickMessage(message: String) = _chickMessage.postValue(message)

    fun resetCanWriteNote() {
        UserInfo.canWriteNote = false
        _canWriteNote.postValue(false)
    }

    fun resetLatestVisitLatLng() {
        UserInfo.latestVisitLatLng = null
        UserInfo.latestPlaceId = ""
        UserInfo.latestPlaceText = ""
        _latestVisitLatLng.postValue(null)
        _isNearByLatestVisitLatLng.postValue(false)
        resetCanWriteNote()
    }

    fun showReadNote() = _showReadNoteEvent.call()

    fun showWriteNote() = _showWriteNoteEvent.call()

    fun searchPlacesDefault() {
        // TODO: 初期検索キーワードを実装。とりあえずカフェにする
        searchPlaces(listOf("カフェ"), radius = 1000)
    }

    fun searchPlaces(keywords: List<String>, radius: Int) {
        clearRoutes()

        if (keywords.isEmpty()) return
        if (!isLocationObserved) return
        val latLng = this.latLng.value ?: return

        // "+"がエンコードされないように自前でエンコード処理を行う
        val keyword = keywords.joinToString(separator = "+OR+") { URLEncoder.encode(it, "UTF-8") }
        repository.getPlacesWithKeyword(latLng.toSimpleString(), radius, keyword)
                .subscribeOn(scheduler.io())
                .observeOn(scheduler.ui())
                .subscribeBy(
                        onSuccess = { result ->
                            if (result.results.isEmpty()) {
                                clearPlaces()
                                _moveCameraZoomEvent.postValue(latLng)
                            }
                            else {
                                placesSize = result.results.size
                                result.results.forEach { it.setDistance(latLng) }
                                _zoomBounds.postValue(result.calcBounds(latLng.latitude, latLng.longitude))
                                _places.postValue(result.results.sortedBy { it.getDistance() })
                                _showsSearchResult.postValue(true)
                            }
                        },
                        onError = {
                            clearPlaces()
                            _moveCameraZoomEvent.postValue(latLng)
                        }
                )
                .addTo(disposables)
    }

    fun searchDirection(destination: String) {
        clearPlaces()

        val latLng = this.latLng.value ?: return
        startLatLng = latLng
        repository.getDirection(latLng.toSimpleString(), destination)
                .subscribeOn(scheduler.io())
                .observeOn(scheduler.ui())
                .subscribeBy(
                        onSuccess = {
                            val routes = it.routes[0].overviewPolyline.routes
                            _routes.postValue(routes)
                            _moveCameraZoomEvent.postValue(latLng)
                            _showsChick.postValue(true)
                            rotationEnabled = true
                        },
                        onError = {}
                )
                .addTo(disposables)
    }

    fun setRoutesViaNotification(routes: List<LatLng>) {
        clearPlaces()
        isLocationObserved = true   // searchPlacesが呼ばれることを防止
        _routes.postValue(routes)
        _moveCameraZoomEvent.postValue(routes[0])
        _showsChick.postValue(true)
        rotationEnabled = true
    }

    fun switchRotate() {
        rotationEnabled = !rotationEnabled
        _switchRotateEvent.postValue(rotationEnabled)
    }

    fun onScrolled(first: Int, last: Int) {
        this.first = first
        this.last = last
        updatePinPositions()
    }

    fun onSelected(position: Int, latLng: LatLng?) {
        selectedPosition = position
        updatePinPositions()
        if (latLng != null) _moveCameraEvent.postValue(latLng)
    }

    fun reached() {
        val placeId = _targetPlace.value?.placeId ?: ""
        val placeText = _targetPlace.value?.name ?: ""
        clearRoutes()

        repository.addPoints(UserInfo.userId, 20)
                .subscribeOn(scheduler.io())
                .observeOn(scheduler.ui())
                .subscribeBy(
                        onSuccess = {
                            UserInfo.points = it.points
                            UserInfo.canWriteNote = true
                            _pointsEvent.value = 20
                        },
                        onError = {}
                )
                .addTo(disposables)

        repository.visitPlace(UserInfo.userId, placeId)
                .subscribeOn(scheduler.io())
                .observeOn(scheduler.ui())
                .subscribeBy(
                        onComplete = {
                            UserInfo.latestVisitLatLng = UserInfo.latLng
                            UserInfo.latestPlaceId = placeId
                            UserInfo.latestPlaceText = placeText
                            _latestVisitLatLng.value = UserInfo.latestVisitLatLng
                            _reachedEvent.value = startLatLng
                            _canWriteNote.value = true
                        },
                        onError = {}
                )
                .addTo(disposables)
    }

    private fun clearPlaces() {
        _places.postValue(emptyList())
        _zoomBounds.postValue(null)
        _showsSearchResult.postValue(false)
        _smallPinPositions.postValue(null)
        _largePinPositions.postValue(null)
        _selectedLargePinPositions.postValue(null)
        _selectedSmallPinPositions.postValue(null)
    }

    fun clearRoutes() {
        _routes.postValue(emptyList())
        _targetPlace.postValue(null)
        _showsChick.postValue(false)
        _isNear.postValue(false)
        rotationEnabled = false
    }

    private fun updatePinPositions() {
        // UIに即反映したいのでpostValueは使わない
        _smallPinPositions.value = (0 until first).toMutableList().apply {
            addAll((last + 1 until placesSize))
            remove(selectedPosition)
        }
        _largePinPositions.value = (first..last).toMutableList().apply { remove(selectedPosition) }
        if      (selectedPosition in first..last)        _selectedLargePinPositions.value = listOf(selectedPosition)
        else if (selectedPosition in 0 until placesSize) _selectedSmallPinPositions.value = listOf(selectedPosition)
    }

    companion object {
        const val NEAR_DISTANCE = 50
    }
}