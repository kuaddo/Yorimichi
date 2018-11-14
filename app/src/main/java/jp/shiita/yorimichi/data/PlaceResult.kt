package jp.shiita.yorimichi.data

import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import jp.shiita.yorimichi.util.distance

data class PlaceResult(
        val results: List<Place>,
        val nextPageToken: String,
        val status: String
) {
    data class Place(
            val id: String,
            val name: String,
            val geometry: Geometry,
            val icon: String,
            val photos: List<Photo>,
            val placeId: String,
            val rating: Float,
            val reference: String,
            val types: List<String>,
            val vicinity: String
    ) {
        val lat get() = geometry.location.lat
        val lng get() = geometry.location.lng
        val latLng get() = LatLng(lat, lng)
        var selected = false        // RecyclerViewで選択されているか

        private var _distance = 0   // m

        fun getDistance() = _distance

        fun getDistanceForDisplay() = "${_distance}m"

        fun setDistance(targetLatLng: LatLng) {
            _distance = latLng.distance(targetLatLng)
        }
    }

    data class Geometry(
            val location: Location,
            val viewport: Viewport
    )

    data class Photo(
            val width: Int,
            val height: Int,
            val photoReference: String
    )

    fun calcBounds(currentLat: Double, currentLng: Double): LatLngBounds {
        var minLat = currentLat
        var minLng = currentLng
        var maxLat = currentLat
        var maxLng = currentLng
        results.forEach {
            minLat = minOf(minLat, it.lat)
            minLng = minOf(minLng, it.lng)
            maxLat = maxOf(maxLat, it.lat)
            maxLng = maxOf(maxLng, it.lng)
        }
        val dLat = (maxLat - minLat) * 0.1
        val dLng = (maxLng - minLng) * 0.1

        return LatLngBounds(LatLng(minLat - dLat, minLng - dLng), LatLng(maxLat + dLat, maxLng + dLng))
    }
}