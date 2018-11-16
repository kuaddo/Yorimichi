package jp.shiita.yorimichi.data

import com.chibatching.kotpref.KotprefModel
import com.google.android.gms.maps.model.LatLng
import jp.shiita.yorimichi.R

object UserInfo : KotprefModel() {
    var userId by stringPref()
    var latitude by stringPref("35.681253")     // defaultは東京駅
    var longitude by stringPref("139.766932")
    var latestVisitLatitude by stringPref("")
    var latestVisitLongitude by stringPref("")
    var latestPlaceId by stringPref("")
    var latestPlaceText by stringPref("")
    var canWriteNote by booleanPref(false)
    var points by intPref(0)
    var iconBucket by stringPref("gs://yorimichi_goods")
    var iconFileName by stringPref("icon_normal.png")
    val autoSearchCategory by stringSetPref { mutableSetOf(context.resources.getStringArray(R.array.place_types)[0]) }
    var originalCategory1 by stringPref("")
    var originalCategory2 by stringPref("")
    var originalCategory3 by stringPref("")

    var latLng: LatLng?
        get() =
            if (UserInfo.latitude.isNotBlank() && UserInfo.longitude.isNotBlank())
                LatLng(UserInfo.latitude.toDouble(), UserInfo.longitude.toDouble())
            else null
        set(value) {
            if (value != null) {
                latitude = value.latitude.toString()
                longitude = value.longitude.toString()
            }
            else {
                latitude = ""
                longitude = ""
            }
        }

    var latestVisitLatLng: LatLng?
        get() =
            if (UserInfo.latestVisitLatitude.isNotBlank() && UserInfo.latestVisitLongitude.isNotBlank())
                LatLng(UserInfo.latestVisitLatitude.toDouble(), UserInfo.latestVisitLongitude.toDouble())
            else null
        set(value) {
            if (value != null) {
                latestVisitLatitude = value.latitude.toString()
                latestVisitLongitude = value.longitude.toString()
            }
            else {
                latestVisitLatitude = ""
                latestVisitLongitude = ""
            }
        }

    val originalCategories: List<String>
        get() = listOf(originalCategory1, originalCategory2, originalCategory3)
                .filter { it.isNotBlank() }
}