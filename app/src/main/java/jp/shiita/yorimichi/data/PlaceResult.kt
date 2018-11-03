package jp.shiita.yorimichi.data

import com.google.android.gms.maps.model.LatLng
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

        fun setDistance(targetLat: Double, targetLng: Double) =
                setDistance(LatLng(targetLat, targetLng))
    }

    data class Geometry(
            val location: Location,
            val viewport: Viewport
    )

    data class Location(
            val lat: Double,
            val lng: Double
    )

    data class Viewport(
            val northeast: Location,
            val southwest: Location
    )

    data class Photo(
            val width: Int,
            val height: Int,
            val photoReference: String
    )
}