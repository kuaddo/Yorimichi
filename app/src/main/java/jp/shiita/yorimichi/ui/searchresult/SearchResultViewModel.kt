package jp.shiita.yorimichi.ui.searchresult

import android.arch.lifecycle.LiveData
import android.arch.lifecycle.MutableLiveData
import android.arch.lifecycle.ViewModel
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import jp.shiita.yorimichi.data.PlaceResult
import javax.inject.Inject

class SearchResultViewModel @Inject constructor() : ViewModel() {
    val places: LiveData<List<PlaceResult.Place>> get() = _places
    val zoomBounds: LiveData<LatLngBounds> get() = _zoomBounds

    private val _places = MutableLiveData<List<PlaceResult.Place>>()
    private val _zoomBounds = MutableLiveData<LatLngBounds>()

    fun searchPlaces(lat: Double, lng: Double) {
        val place = PlaceResult.Place(
                "",
                "ベックスコーヒーショップ横浜中央口店",
                lat,
                lng,
                "https://maps.gstatic.com/mapfiles/place_api/icons/cafe-71.png",
                emptyList(),
                "",
                3.6f,
                "",
                listOf("cafe", "store", "point_of_interest", "food" ,"establishment"),
                "")

        val ps = listOf(
                place.copy(rating = 0.3f, lat = place.lat + 0.001, lng = place.lng + 0.001).also { it.setDistance(lat, lng) },
                place.copy(rating = 2.5f, lat = place.lat + 0.011, lng = place.lng + 0.001).also { it.setDistance(lat, lng) },
                place.copy(rating = 1.6f, lat = place.lat + 0.003, lng = place.lng + 0.021).also { it.setDistance(lat, lng) },
                place.copy(rating = 5.0f, lat = place.lat + 0.004, lng = place.lng + 0.004).also { it.setDistance(lat, lng) },
                place.copy(rating = 4.9f, lat = place.lat + 0.020, lng = place.lng + 0.005).also { it.setDistance(lat, lng) },
                place.copy(rating = 0.0f, lat = place.lat + 0.010, lng = place.lng + 0.023).also { it.setDistance(lat, lng) },
                place.copy(rating = 3.4f, lat = place.lat + 0.004, lng = place.lng + 0.001).also { it.setDistance(lat, lng) })

        var minLat = lat
        var minLng = lng
        var maxLat = lat
        var maxLng = lng
        ps.forEach {
            minLat = minOf(minLat, it.lat)
            minLng = minOf(minLng, it.lng)
            maxLat = maxOf(maxLat, it.lat)
            maxLng = maxOf(maxLng, it.lng)
        }
        val dLat = (maxLat - minLat) * 0.1
        val dLng = (maxLng - minLng) * 0.1

        _places.postValue(ps)
        _zoomBounds.postValue(LatLngBounds(LatLng(minLat - dLat, minLng - dLng), LatLng(maxLat + dLat, maxLng + dLng)))
    }
}