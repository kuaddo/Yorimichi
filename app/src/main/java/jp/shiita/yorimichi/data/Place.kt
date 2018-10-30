package jp.shiita.yorimichi.data

import com.google.android.gms.maps.model.LatLng

data class Place(
        val results: List<Result>,
        val nextPageToken: String,
        val status: String
) {
    data class Result(
            val id: String,
            val name: String,
            val lat: Double,
            val lng: Double,
            val icon: String,
            val photos: List<Photo>,
            val placeId: String,
            val rating: Double,
            val reference: String,
            val types: List<String>,
            val vicinity: String
    ) {
        val latLng get() = LatLng(lat, lng)
    }

    data class Photo(
            val width: Int,
            val height: Int,
            val photoReference: String
    )
}