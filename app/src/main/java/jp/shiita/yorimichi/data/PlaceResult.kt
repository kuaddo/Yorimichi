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
            val lat: Double,
            val lng: Double,
            val icon: String,
            val photos: List<Photo>,
            val placeId: String,
            val rating: Float,
            val reference: String,
            val types: List<String>,
            val vicinity: String
    ) {
        val latLng get() = LatLng(lat, lng)

        private var _distance = 0   // m

        fun getDistance() = _distance

        fun getDistanceForDisplay() = "${_distance}m"

        fun setDistance(targetLatLng: LatLng) {
            _distance = latLng.distance(targetLatLng)
        }

        fun setDistance(targetLat: Double, targetLng: Double) =
                setDistance(LatLng(targetLat, targetLng))
    }

    data class Photo(
            val width: Int,
            val height: Int,
            val photoReference: String
    )
}