package jp.shiita.yorimichi.data

import com.chibatching.kotpref.KotprefModel
import com.google.android.gms.maps.model.LatLng

object UserInfo : KotprefModel() {
    var userId by stringPref()
    var latitude by stringPref()
    var longitude by stringPref()

    val latLng: LatLng?
        get() =
            if (UserInfo.latitude.isNotEmpty() && UserInfo.longitude.isNotEmpty())
                LatLng(UserInfo.latitude.toDouble(), UserInfo.longitude.toDouble())
            else null
}