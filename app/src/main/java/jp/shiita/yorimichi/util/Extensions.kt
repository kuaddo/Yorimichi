package jp.shiita.yorimichi.util

import android.arch.lifecycle.*
import android.graphics.*
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.location.Location
import android.support.annotation.ColorInt
import android.support.annotation.IdRes
import android.support.v4.app.Fragment
import android.support.v4.app.FragmentManager
import android.support.v4.graphics.drawable.DrawableCompat
import android.util.Base64
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdView
import com.google.android.gms.maps.model.LatLng
import jp.shiita.yorimichi.BuildConfig
import jp.shiita.yorimichi.data.UserInfo
import org.threeten.bp.LocalDateTime
import org.threeten.bp.format.DateTimeFormatter
import java.io.ByteArrayOutputStream


val Location.latLng
    get() = LatLng(latitude, longitude)

fun <T> LiveData<T>.observe(owner: LifecycleOwner, observer: (T) -> Unit) =
        observe(owner, Observer<T> { if (it != null) observer(it) })

fun <X, Y> LiveData<X>.map(func: (X) -> Y): LiveData<Y> = Transformations.map(this, func)

fun <T1, T2, S> LiveData<T1>.combineLatest(source: LiveData<T2>, func: (T1, T2) -> S): LiveData<S> {
    val result = MediatorLiveData<S>()
    fun setValue() = value?.let { v1 -> source.value?.let { v2 -> result.value = func(v1, v2) } }
    result.addSource(this) { setValue() }
    result.addSource(source) { setValue() }
    return result
}

fun FragmentManager.addFragment(@IdRes containerViewId: Int, fragment: Fragment) {
    beginTransaction()
            .add(containerViewId, fragment)
            .commit()
}

fun FragmentManager.addFragmentBS(@IdRes containerViewId: Int, fragment: Fragment, tag: String) {
    beginTransaction()
            .add(containerViewId, fragment)
            .addToBackStack(tag)
            .commit()
}

fun FragmentManager.replaceFragment(@IdRes containerViewId: Int, fragment: Fragment, tag: String) {
    beginTransaction()
            .replace(containerViewId, fragment)
            .addToBackStack(tag)
            .commit()
}

fun LatLng.toSimpleString() = "$latitude,$longitude"

fun Bitmap.toBytes(): ByteArray = ByteArrayOutputStream().let { stream ->
    compress(Bitmap.CompressFormat.PNG, 100, stream)
    stream.toByteArray()
}

fun AdView.loadAd() {
    val builder = AdRequest.Builder()
    val latLng = UserInfo.latLng
    if (latLng != null)
        builder.setLocation(getLocation(latLng.latitude, latLng.longitude))
    BuildConfig.ADMOB_TEST_DEVICES.forEach { builder.addTestDevice(it) }
    loadAd(builder.build())
}

fun ByteArray.toBase64(): String = Base64.encodeToString(this, Base64.NO_WRAP)

/**
 * [Bitmap]を取得しない場合に利用するTint
 */
fun Drawable.setTintCompat(@ColorInt color: Int): Drawable = DrawableCompat.wrap(this).mutate().also {
    DrawableCompat.setTint(it, color)
    DrawableCompat.setTintMode(it, PorterDuff.Mode.SRC_IN)
}

fun Drawable.getBitmap(): Bitmap = when(this) {
    is BitmapDrawable -> bitmap
    else -> {
        val bitmap = Bitmap.createBitmap(intrinsicWidth, intrinsicHeight, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        setBounds(0, 0, canvas.width, canvas.height)
        draw(canvas)
        bitmap
    }
}

/**
 * [Bitmap]を取得する場合に利用するTint
 */
fun Drawable.getBitmap(@ColorInt color: Int): Bitmap {
    val bitmap = Bitmap.createBitmap(intrinsicWidth, intrinsicHeight, Bitmap.Config.ARGB_8888)
    val paint = Paint().apply { colorFilter = PorterDuffColorFilter(color, PorterDuff.Mode.SRC_IN) }
    val canvas = Canvas(bitmap)
    canvas.drawBitmap(getBitmap(), 0f, 0f, paint)
    return bitmap
}

/**
 * 球面三角法を利用した距離計算
 * @return 距離(m)
 */
fun LatLng.distance(latLng: LatLng): Int {
    val r = 6378137.0     // 赤道半径
    val radLat1 = Math.toRadians(latitude)
    val radLng1 = Math.toRadians(longitude)
    val radLat2 = Math.toRadians(latLng.latitude)
    val radLng2 = Math.toRadians(latLng.longitude)
    return (r * 2 * Math.asin(Math.sqrt(Math.pow(Math.sin((radLat1 - radLat2) / 2), 2.0)
            + Math.cos(radLat1) * Math.cos(radLat2) * Math.pow(Math.sin((radLng1 - radLng2) / 2), 2.0)))).toInt()
}

fun LocalDateTime.toSimpleString(): String = format(simpleDateTimeFormatter)

fun LocalDateTime.toSimpleDateString(): String = format(simpleDateFormatter)

/**
 * 時刻の次の日の00:00:00をyyyy-MM-dd HH:mm:ss形式で文字列化
 * 到着時刻とノート記述時刻の差に対応するために利用
 */
fun LocalDateTime.toUploadDateString(): String = plusDays(1)
        .minusHours(hour.toLong())
        .minusMinutes(minute.toLong())
        .minusSeconds(second.toLong())
        .format(uploadDateFormatter)

private fun getLocation(lat: Double, lng: Double) = Location("dummy provider").apply {
    latitude = lat
    longitude = lng
}

private val simpleDateTimeFormatter = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm")

private val simpleDateFormatter = DateTimeFormatter.ofPattern("MM月dd日")

private val uploadDateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")