package jp.shiita.yorimichi.ui.map

import android.Manifest
import android.arch.lifecycle.ViewModelProvider
import android.arch.lifecycle.ViewModelProviders
import android.content.pm.PackageManager
import android.databinding.DataBindingUtil
import android.location.Location
import android.os.Bundle
import android.support.v4.app.ActivityCompat
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.SupportMapFragment
import dagger.android.support.DaggerFragment
import jp.shiita.yorimichi.R
import jp.shiita.yorimichi.databinding.FragMapBinding
import jp.shiita.yorimichi.live.LocationLiveData
import jp.shiita.yorimichi.ui.main.MainViewModel
import jp.shiita.yorimichi.util.latLng
import jp.shiita.yorimichi.util.observe
import javax.inject.Inject


class MapFragment : DaggerFragment() {
    @Inject lateinit var viewModelFactory: ViewModelProvider.Factory
    private val mainViewModel: MainViewModel
            by lazy { ViewModelProviders.of(activity!!, viewModelFactory).get(MainViewModel::class.java)}
    private val viewModel: MapViewModel
            by lazy { ViewModelProviders.of(this, viewModelFactory).get(MapViewModel::class.java) }
    private val locationLiveData: LocationLiveData
            by lazy { LocationLiveData(context!!) }
    private lateinit var binding: FragMapBinding
    private var map: GoogleMap? = null
    private var isLocationObserved = false

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = DataBindingUtil.inflate(inflater, R.layout.frag_map, container, false)
        return binding.root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        binding.setLifecycleOwner(this)
        binding.viewModel = viewModel

        if (ActivityCompat.checkSelfPermission(context!!, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
            ActivityCompat.checkSelfPermission(context!!, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION),
                    REQUEST_LOCATION_PERMISSION)
        }
        else {
            initMap()
            observe()
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        if (requestCode != REQUEST_LOCATION_PERMISSION) return

        if (grantResults[0] == PackageManager.PERMISSION_GRANTED ||
            grantResults[1] == PackageManager.PERMISSION_GRANTED) {
            initMap()
            observe()
        }
        else {
            mainViewModel.finishApp()
        }
    }

    private fun initMap() {
        if (ActivityCompat.checkSelfPermission(context!!, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
            ActivityCompat.checkSelfPermission(context!!, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return
        }

        (childFragmentManager.findFragmentById(R.id.google_map) as SupportMapFragment).getMapAsync { googleMap ->
            map = googleMap
            if (!isLocationObserved) {
                map?.uiSettings?.setAllGesturesEnabled(false)
                map?.isMyLocationEnabled = true
            }
        }
    }

    private fun observe() {
        locationLiveData.observe(this, ::plotCurrentLocation)
    }

    private fun plotCurrentLocation(location: Location) {
        if (!isLocationObserved) {
            isLocationObserved = true
            map?.animateCamera(CameraUpdateFactory.newLatLngZoom(location.latLng, INITIAL_ZOOM_LEVEL), object : GoogleMap.CancelableCallback {
                override fun onFinish() {
                    map?.uiSettings?.setAllGesturesEnabled(true)
                }

                override fun onCancel() {
                    map?.uiSettings?.setAllGesturesEnabled(true)
                }
            })
        }
    }

    companion object {
        val TAG: String = MapFragment::class.java.simpleName
        const val REQUEST_LOCATION_PERMISSION = 0
        const val INITIAL_ZOOM_LEVEL = 16f
        fun newInstance() = MapFragment()
    }
}