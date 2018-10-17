package jp.shiita.yorimichi.live

import android.Manifest
import android.arch.lifecycle.LiveData
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.os.Looper
import android.support.v4.app.ActivityCompat
import com.google.android.gms.location.*


class LocationLiveData(private val context: Context) : LiveData<Location>() {
    private var fusedLocationProviderClient: FusedLocationProviderClient? = LocationServices.getFusedLocationProviderClient(context)

    private val locationCallback = object : LocationCallback() {
        override fun onLocationResult(result: LocationResult) {
            value = result.lastLocation
        }
    }

    override fun onActive() {
        super.onActive()
        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
            ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return
        }

        val locationRequest = LocationRequest.create().apply {
            priority = LocationRequest.PRIORITY_HIGH_ACCURACY
            interval = 5000         // 5sec
            fastestInterval = 5000  // 5sec
        }
        fusedLocationProviderClient?.requestLocationUpdates(
                locationRequest,
                locationCallback,
                Looper.myLooper())
    }

    override fun onInactive() {
        fusedLocationProviderClient?.removeLocationUpdates(locationCallback)
    }
}