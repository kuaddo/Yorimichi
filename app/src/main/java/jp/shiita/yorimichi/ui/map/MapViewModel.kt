package jp.shiita.yorimichi.ui.map

import android.arch.lifecycle.LiveData
import android.arch.lifecycle.MutableLiveData
import android.arch.lifecycle.ViewModel
import android.util.Log
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.subscribeBy
import jp.shiita.yorimichi.data.PlaceResult
import jp.shiita.yorimichi.data.UserInfo
import jp.shiita.yorimichi.data.api.YorimichiRepository
import jp.shiita.yorimichi.live.SingleLiveEvent
import jp.shiita.yorimichi.scheduler.BaseSchedulerProvider
import jp.shiita.yorimichi.util.toSimpleString
import java.net.URLEncoder
import javax.inject.Inject

class MapViewModel @Inject constructor(
        private val repository: YorimichiRepository,
        private val scheduler: BaseSchedulerProvider
) : ViewModel() {
    val latLng: LiveData<LatLng> get() = _latLng
    val places: LiveData<List<PlaceResult.Place>> get() = _places
    val routes: LiveData<List<LatLng>> get() = _routes
    val bucket: LiveData<String> get() = _bucket
    val fileName: LiveData<String> get() = _fileName
    val zoomBounds: LiveData<LatLngBounds> get() = _zoomBounds
    val moveCameraEvent: LiveData<LatLng> get() = _moveCameraEvent
    val moveCameraZoomEvent: LiveData<LatLng> get() = _moveCameraZoomEvent
    val smallPinPositions: LiveData<List<Int>> get() = _smallPinPositions
    val largePinPositions: LiveData<List<Int>> get() = _largePinPositions
    val selectedSmallPinPositions: LiveData<List<Int>> get() = _selectedSmallPinPositions
    val selectedLargePinPositions: LiveData<List<Int>> get() = _selectedLargePinPositions
    val showsSearchResult: LiveData<Boolean> get() = _showsSearchResult
    val showsChick: LiveData<Boolean> get() = _showsChick

    private val _latLng                    = MutableLiveData<LatLng>()
    private val _places                    = MutableLiveData<List<PlaceResult.Place>>()
    private val _routes                    = MutableLiveData<List<LatLng>>()
    private val _bucket                    = MutableLiveData<String>().apply { value = "gs://${UserInfo.iconBucket}" }    // TODO: 応急処置
    private val _fileName                  = MutableLiveData<String>().apply { value = UserInfo.iconFileName }
    private val _zoomBounds                = MutableLiveData<LatLngBounds>()
    private val _smallPinPositions         = MutableLiveData<List<Int>>()
    private val _largePinPositions         = MutableLiveData<List<Int>>()
    private val _selectedSmallPinPositions = MutableLiveData<List<Int>>()
    private val _selectedLargePinPositions = MutableLiveData<List<Int>>()
    private val _showsSearchResult         = MutableLiveData<Boolean>()
    private val _showsChick                = MutableLiveData<Boolean>()

    private val _moveCameraEvent      = SingleLiveEvent<LatLng>()
    private val _moveCameraZoomEvent = SingleLiveEvent<LatLng>()

    private var isLocationObserved = false
    private var placesSize = -1
    private var selectedPosition = -1
    private var first = -1
    private var last = -1

    private val disposables = CompositeDisposable()

    override fun onCleared() = disposables.clear()

    fun setLatLng(latLng: LatLng) {
        _latLng.value = latLng
        if (!isLocationObserved) {
            isLocationObserved = true
            searchPlacesDefault()
        }
    }

    fun setIcon(bucket: String, name: String) {
        _bucket.postValue("gs://$bucket")   // TODO: 応急処置
        _fileName.postValue(name)
    }

    fun searchPlacesDefault() {
        // TODO: 初期検索キーワードを実装。とりあえずカフェにする
        searchPlaces(listOf("カフェ"), radius = 2000)
    }

    fun searchPlaces(categories: List<String>, radius: Int) {
        clearRoutes()

        if (categories.isEmpty()) return
        if (!isLocationObserved) return
        val latLng = this.latLng.value ?: return

        // "+"がエンコードされないように自前でエンコード処理を行う
        val keywords = categories.map { URLEncoder.encode(it, "UTF-8") }.joinToString(separator = "+OR+")
        Log.d("keywords", keywords)
        repository.getPlacesWithKeyword(latLng.toSimpleString(), radius, keywords)
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
                                calcZoomBounds(result.results, latLng.latitude, latLng.longitude)
                                _places.postValue(result.results.sortedBy { it.getDistance() })
                                _showsSearchResult.postValue(true)
                            }
                        },
                        onError = {
                            clearPlaces()
                            _moveCameraZoomEvent.postValue(latLng)
                        }
                )
    }

    fun searchDirection(placeId: String) {
        clearPlaces()

        val latLng = this.latLng.value ?: return
        repository.getDirection(latLng.toSimpleString(), "place_id:$placeId")
                .subscribeOn(scheduler.io())
                .observeOn(scheduler.ui())
                .subscribeBy(
                        onSuccess = {
                            val routes = it.routes[0].overviewPolyline.routes
                            _routes.postValue(routes)
                            _moveCameraZoomEvent.postValue(latLng)
                            _showsChick.postValue(true)
                        },
                        onError = {}
                )
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

    private fun clearPlaces() {
        _places.postValue(emptyList())
        _zoomBounds.postValue(null)
        _showsSearchResult.postValue(false)
        _smallPinPositions.postValue(null)
        _largePinPositions.postValue(null)
        _selectedLargePinPositions.postValue(null)
        _selectedSmallPinPositions.postValue(null)
    }

    private fun clearRoutes() {
        _routes.postValue(emptyList())
        _showsChick.postValue(false)
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

    private fun calcZoomBounds(places: List<PlaceResult.Place>, currentLat: Double, currentLng: Double) {
        var minLat = currentLat
        var minLng = currentLng
        var maxLat = currentLat
        var maxLng = currentLng
        places.forEach {
            minLat = minOf(minLat, it.lat)
            minLng = minOf(minLng, it.lng)
            maxLat = maxOf(maxLat, it.lat)
            maxLng = maxOf(maxLng, it.lng)
        }
        val dLat = (maxLat - minLat) * 0.1
        val dLng = (maxLng - minLng) * 0.1

        _zoomBounds.postValue(LatLngBounds(LatLng(minLat - dLat, minLng - dLng), LatLng(maxLat + dLat, maxLng + dLng)))
    }
}