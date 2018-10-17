package jp.shiita.yorimichi.util

import android.arch.lifecycle.LifecycleOwner
import android.arch.lifecycle.LiveData
import android.arch.lifecycle.Observer
import android.location.Location
import android.support.annotation.IdRes
import android.support.v4.app.Fragment
import android.support.v4.app.FragmentManager
import com.google.android.gms.maps.model.LatLng

fun <T> LiveData<T>.observe(owner: LifecycleOwner, observer: (T) -> Unit) =
        observe(owner, Observer<T> { if (it != null) observer(it) })

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

val Location.latLng
        get() = LatLng(latitude, longitude)