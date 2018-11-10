package jp.shiita.yorimichi.data

import com.chibatching.kotpref.KotprefModel
import com.google.android.gms.maps.model.LatLng

object UserInfo : KotprefModel() {
    var userId by stringPref()
    var latitude by stringPref("35.681253")     // defaultは東京駅
    var longitude by stringPref("139.766932")
    var points by intPref(0)
    var iconBucket by stringPref("gs://yorimichi_goods")
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