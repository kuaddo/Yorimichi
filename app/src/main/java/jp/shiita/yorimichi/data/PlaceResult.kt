package jp.shiita.yorimichi.data

import com.google.android.gms.maps.model.LatLng
import java.lang.Math.*

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

        private var _distance = 0

        fun getDistance() = _distance

        fun getDistanceForDisplay() = "${_distance}m"

        fun setDistance(targetLatLng: LatLng) = setDistance(targetLatLng.latitude, targetLatLng.longitude)

        fun setDistance(targetLat: Double, targetLng: Double) {
            _distance = calcDistance(targetLat, targetLng, lat, lng)
        }

        /**
         * 球面三角法
         */
        private fun calcDistance(lat1: Double, lng1: Double, lat2: Double, lng2: Double): Int {
            val radLat1 = toRadians(lat1)
            val radLng1 = toRadians(lng1)
            val radLat2 = toRadians(lat2)
            val radLng2 = toRadians(lng2)
            return (r * 2 * asin(sqrt(pow(sin((radLat1 - radLat2) / 2), 2.0)
                    + cos(radLat1) * cos(radLat2) * pow(sin((radLng1 - radLng2) / 2), 2.0)))).toInt()
        }

        companion object {
            const val r = 6378137.0     // 赤道半径
        }
    }

    data class Photo(
            val width: Int,
            val height: Int,
            val photoReference: String
    )
}