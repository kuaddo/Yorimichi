package jp.shiita.yorimichi.data

import com.chibatching.kotpref.KotprefModel
import com.google.android.gms.maps.model.LatLng

object UserInfo : KotprefModel() {
    var userId by stringPref()
    var latitude by stringPref()
    var longitude by stringPref()
    var points by intPref(0)
    var iconBucket by stringPref("yorimichi_goods")
    var iconFileName by stringPref("icon_normal.png")

    var latLng: LatLng?
        get() =
            if (UserInfo.latitude.isNotEmpty() && UserInfo.longitude.isNotEmpty())
                LatLng(UserInfo.latitude.toDouble(), UserInfo.longitude.toDouble())
            else null
        set(value) {
            if (value != null) {
                latitude = value.latitude.toString()
                longitude = value.longitude.toString()
            }
        }
}