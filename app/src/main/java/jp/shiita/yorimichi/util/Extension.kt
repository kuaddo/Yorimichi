package jp.shiita.yorimichi.util

import android.arch.lifecycle.LifecycleOwner
import android.arch.lifecycle.LiveData
import android.arch.lifecycle.Observer
import android.arch.lifecycle.Transformations
import android.graphics.Bitmap
import android.location.Location
import android.support.annotation.IdRes
import android.support.v4.app.Fragment
import android.support.v4.app.FragmentManager
import android.util.Base64
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdView
import com.google.android.gms.maps.model.LatLng
import jp.shiita.yorimichi.BuildConfig
import jp.shiita.yorimichi.data.UserInfo
import java.io.ByteArrayOutputStream

val Location.latLng
    get() = LatLng(latitude, longitude)

fun <T> LiveData<T>.observe(owner: LifecycleOwner, observer: (T) -> Unit) =
        observe(owner, Observer<T> { if (it != null) observer(it) })

fun <X, Y> LiveData<X>.map(func: (X) -> Y) = Transformations.map(this, func)

fun FragmentManager.addFragment(@IdRes containerViewId: Int, fragment: Fragment) {
    beginTransaction()
            .add(containerViewId, fragment)
            .commit()
}

fun FragmentManager.replaceFragment(@IdRes containerViewId: Int, fragment: Fragment, tag: String) {
    beginTransaction()
            .replace(containerViewId, fragment)
            .addToBackStack(tag)
            .commit()
}

fun LatLng.toSimpleString() = "($latitude, $longitude)"

fun Bitmap.toBytes(): ByteArray = ByteArrayOutputStream().let { stream ->
    compress(Bitmap.CompressFormat.PNG, 100, stream)
    stream.toByteArray()
}

fun AdView.loadAd() {
    val builder = AdRequest.Builder()
    if (UserInfo.latitude.isNotEmpty() && UserInfo.longitude.isNotEmpty())
        builder.setLocation(getLocation(UserInfo.latitude.toDouble(), UserInfo.longitude.toDouble()))
    BuildConfig.ADMOB_TEST_DEVICES.forEach { builder.addTestDevice(it) }
    loadAd(builder.build())
}

fun ByteArray.toBase64(): String = Base64.encodeToString(this, Base64.NO_WRAP)


private fun getLocation(lat: Double, lng: Double) = Location("dummy provider").apply {
    latitude = lat
    longitude = lng
}
