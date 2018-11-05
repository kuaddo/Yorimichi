package jp.shiita.yorimichi.data

import com.google.android.gms.maps.model.LatLng

data class Location(
        val lat: Double,
        val lng: Double
) {
    val latLng get() = LatLng(lat, lng)
}