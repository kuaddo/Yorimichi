package jp.shiita.yorimichi.ui.map

import android.Manifest
import android.arch.lifecycle.ViewModelProvider
import android.arch.lifecycle.ViewModelProviders
import android.content.pm.PackageManager
import android.databinding.DataBindingUtil
import android.graphics.Bitmap
import android.graphics.Color
import android.os.Bundle
import android.support.v4.app.ActivityCompat
import android.support.v4.content.res.ResourcesCompat
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.*
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.*
import dagger.android.support.DaggerFragment
import jp.shiita.yorimichi.R
import jp.shiita.yorimichi.data.PlaceResult
import jp.shiita.yorimichi.data.UserInfo
import jp.shiita.yorimichi.databinding.FragMapBinding
import jp.shiita.yorimichi.live.LocationLiveData
import jp.shiita.yorimichi.live.MagneticLiveData
import jp.shiita.yorimichi.receiver.NotificationBroadcastReceiver
import jp.shiita.yorimichi.ui.dialog.PointGetDialogFragment
import jp.shiita.yorimichi.ui.main.MainViewModel
import jp.shiita.yorimichi.ui.remind.RemindFragment
import jp.shiita.yorimichi.util.*
import javax.inject.Inject

class MapFragment : DaggerFragment() {
    @Inject lateinit var viewModelFactory: ViewModelProvider.Factory
    private val mainViewModel: MainViewModel
            by lazy { ViewModelProviders.of(activity!!, viewModelFactory).get(MainViewModel::class.java)}
    private val viewModel: MapViewModel
            by lazy { ViewModelProviders.of(this, viewModelFactory).get(MapViewModel::class.java) }
    private val locationLiveData: LocationLiveData by lazy { LocationLiveData(context!!) }
    private val magneticLiveData: MagneticLiveData by lazy { MagneticLiveData(context!!) }
    private lateinit var binding: FragMapBinding
    private lateinit var searchResultAdapter: PlaceAdapter
    private var map: GoogleMap? = null
    private var markers: MutableList<Triple<Marker?, Int, Float>> = mutableListOf()
    private lateinit var smallDescriptor: BitmapDescriptor
    private lateinit var largeDescriptor: BitmapDescriptor
    private lateinit var selectedSmallDescriptor: BitmapDescriptor
    private lateinit var selectedLargeDescriptor: BitmapDescriptor

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        setHasOptionsMenu(true)
        binding = DataBindingUtil.inflate(inflater, R.layout.frag_map, container, false)
        return binding.root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        binding.setLifecycleOwner(this)
        binding.viewModel = viewModel

        searchResultAdapter = PlaceAdapter(context!!, mutableListOf(), ::selectPlace, viewModel::setTarget)
        binding.recyclerView.also { rv ->
            val layoutManager = rv.layoutManager as LinearLayoutManager
            rv.adapter = searchResultAdapter
            rv.clearOnScrollListeners()
            rv.addOnScrollListener(object : RecyclerView.OnScrollListener() {
                override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                    super.onScrolled(recyclerView, dx, dy)
                    val first = layoutManager.findFirstVisibleItemPosition()
                    val last = layoutManager.findLastVisibleItemPosition()
                    viewModel.onScrolled(first, last)
                }
            })
        }

        if (ActivityCompat.checkSelfPermission(context!!, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
            ActivityCompat.checkSelfPermission(context!!, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION),
                    REQUEST_LOCATION_PERMISSION)
        }
        else {
            initDescriptor()
            initMap()
            observe()
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        if (requestCode != REQUEST_LOCATION_PERMISSION) return

        if (grantResults[0] == PackageManager.PERMISSION_GRANTED ||
            grantResults[1] == PackageManager.PERMISSION_GRANTED) {
            initDescriptor()
            initMap()
            observe()
        }
        else {
            mainViewModel.finishAppLocationPermissionDenied()
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?, inflater: MenuInflater?) {
        if (viewModel.places.value?.isNotEmpty() == true) {
            inflater?.inflate(R.menu.frag_map_search_result, menu)
        }
        else if (viewModel.routes.value?.isNotEmpty() == true) {
            inflater?.inflate(R.menu.frag_map_route, menu)
        }
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        when (item?.itemId) {
            R.id.menu_frag_map_search_result_sort_dist_asc  -> {
                sortMarkerByDistAsc()
                searchResultAdapter.sortByDistAsc()
                viewModel.onSelected(searchResultAdapter.getSelectedPosition(), null)
            }
            R.id.menu_frag_map_search_result_sort_dist_desc -> {
                sortMarkerByDistDesc()
                searchResultAdapter.sortByDistDesc()
                viewModel.onSelected(searchResultAdapter.getSelectedPosition(), null)
            }
            R.id.menu_frag_map_search_result_sort_rate_asc -> {
                sortMarkerByRateAsc()
                searchResultAdapter.sortByRateAsc()
                viewModel.onSelected(searchResultAdapter.getSelectedPosition(), null)
            }
            R.id.menu_frag_map_search_result_sort_rate_desc -> {
                sortMarkerByRateDesc()
                searchResultAdapter.sortByRateDesc()
                viewModel.onSelected(searchResultAdapter.getSelectedPosition(), null)
            }
            R.id.menu_frag_map_finish_guide -> {
                resetMap()
                viewModel.clearRoutes()
                activity?.invalidateOptionsMenu()
            }
            else -> return false
        }
        return true
    }

    private fun initDescriptor() {
        val pinDrawable = ResourcesCompat.getDrawable(resources, R.drawable.ic_pin_large, null)!!
        val largeBitmap = pinDrawable.getBitmap(ResourcesCompat.getColor(resources, R.color.colorPrimary, null))
        val selectedLargeBitmap = pinDrawable.getBitmap(ResourcesCompat.getColor(resources, R.color.colorStar, null))
        val width = largeBitmap.width
        val height = largeBitmap.height

        val smallBitmap = Bitmap.createScaledBitmap(largeBitmap, width / 2, height / 2, false)
        val selectedSmallBitmap = Bitmap.createScaledBitmap(selectedLargeBitmap, width / 2, height / 2, false)
        smallDescriptor = BitmapDescriptorFactory.fromBitmap(smallBitmap)
        largeDescriptor = BitmapDescriptorFactory.fromBitmap(largeBitmap)
        selectedSmallDescriptor = BitmapDescriptorFactory.fromBitmap(selectedSmallBitmap)
        selectedLargeDescriptor = BitmapDescriptorFactory.fromBitmap(selectedLargeBitmap)
    }

    private fun initMap() {
        if (ActivityCompat.checkSelfPermission(context!!, Manifest.permission.ACCESS_FINE_LOCATION)   != PackageManager.PERMISSION_GRANTED &&
            ActivityCompat.checkSelfPermission(context!!, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return
        }

        (childFragmentManager.findFragmentById(R.id.googleMap) as SupportMapFragment).getMapAsync { googleMap ->
            map = googleMap
            map?.moveCamera(CameraUpdateFactory.newLatLng(UserInfo.latLng))
            map?.isMyLocationEnabled = true
            map?.uiSettings?.isCompassEnabled = false

            map?.setOnMarkerClickListener { marker ->
                val position = marker?.tag as? Int ?: 0
                binding.recyclerView.scrollToPosition(position)
                selectPlace(position)
                true        // cameraのアニメーションは自前でやる
            }

            val routes = activity?.intent?.let {
                val lats = it.getDoubleArrayExtra(NotificationBroadcastReceiver.ARGS_LATS) ?: return@let null
                val lngs = it.getDoubleArrayExtra(NotificationBroadcastReceiver.ARGS_LNGS) ?: return@let null
                lats.zip(lngs).map { LatLng(it.first, it.second) }
            }
            if (routes != null) {
                resetMap()
                viewModel.setRoutesViaNotification(routes)
            }
            else {
                if (viewModel.places.value == null && viewModel.routes.value == null)
                    viewModel.searchPlacesDefault()

                viewModel.places.value?.let { addPlaces(it) }
                viewModel.routes.value?.let { addRoute(it) }
            }
        }
    }

    private fun observe() {
        locationLiveData.observe(this) { viewModel.setLatLng(it.latLng) }
        magneticLiveData.observe(this) {
            if (viewModel.rotationEnabled) {
                binding.iconImage.rotation = -it
                map?.let { m ->
                    val position = CameraPosition.Builder(m.cameraPosition)
                            .target(UserInfo.latLng)
                            .bearing(it)
                            .build()
                    m.moveCamera(CameraUpdateFactory.newCameraPosition(position))
                }
            }
        }
        mainViewModel.searchEvent.observe(this) { (categories, radius) -> viewModel.searchPlaces(categories, radius) }
        mainViewModel.directionsEvent.observe(this) { viewModel.searchDirection(it.toSimpleString()) }
        mainViewModel.updateIconEvent.observe(this) { viewModel.setIcon(UserInfo.iconBucket, UserInfo.iconFileName) }
        viewModel.latLng.observe(this) { UserInfo.latLng = it }
        viewModel.places.observe(this) { addPlaces(it) }
        viewModel.routes.observe(this) { addRoute(it) }
        viewModel.zoomBounds.observe(this) { map?.moveCamera(CameraUpdateFactory.newLatLngBounds(it, 0)) }
        viewModel.smallPinPositions.observe(this) { positions -> if (markers.isNotEmpty()) positions.forEach { markers[it].first?.setIcon(smallDescriptor) }}
        viewModel.largePinPositions.observe(this) { positions -> if (markers.isNotEmpty()) positions.forEach { markers[it].first?.setIcon(largeDescriptor) }}
        viewModel.selectedSmallPinPositions.observe(this) { positions -> if (markers.isNotEmpty()) positions.forEach { markers[it].first?.setIcon(selectedSmallDescriptor) }}
        viewModel.selectedLargePinPositions.observe(this) { positions -> if (markers.isNotEmpty()) positions.forEach { markers[it].first?.setIcon(selectedLargeDescriptor) }}
        viewModel.moveCameraEvent.observe(this) { map?.animateCamera(CameraUpdateFactory.newLatLng(it)) }
        viewModel.moveCameraZoomEvent.observe(this) { map?.animateCamera(CameraUpdateFactory.newLatLngZoom(it, INITIAL_ZOOM_LEVEL))}
        viewModel.pointsEvent.observe(this) {
            PointGetDialogFragment.newInstance(it, it).show(activity?.supportFragmentManager, PointGetDialogFragment.TAG)
            mainViewModel.updatePoints()
        }
        viewModel.reachedEvent.observe(this) { startLatLng ->
            resetMap()
            activity?.invalidateOptionsMenu()
            activity?.supportFragmentManager?.addFragmentBS(R.id.container, RemindFragment.newInstance(startLatLng), RemindFragment.TAG)
        }
        viewModel.switchRotateEvent.observe(this) {
            if (it) setRotateEnable()
            else    setRotateDisable()
        }
    }

    private fun resetMap() {
        markers.clear()
        map?.clear()
        setRotateDisable()
    }

    private fun addPlaces(places: List<PlaceResult.Place>) {
        if (places.isEmpty()) return
        resetMap()
        activity?.invalidateOptionsMenu()
        searchResultAdapter.reset(places)
        markers.addAll(places.map {
            val marker = MarkerOptions()
                    .position(it.latLng)
                    .icon(smallDescriptor)
            Triple(map?.addMarker(marker), it.getDistance(), it.rating)
        })
        markers.forEachIndexed { i, (marker, _) -> marker?.tag = i }
    }

    private fun addRoute(routes: List<LatLng>) {
        if (routes.isEmpty()) return
        resetMap()
        activity?.invalidateOptionsMenu()
        map?.addPolyline(PolylineOptions()
                .color(Color.BLUE)
                .addAll(routes))
        setRotateEnable()
    }

    private fun setRotateEnable() {
        map?.uiSettings?.let { ui ->
            ui.setAllGesturesEnabled(false)
            ui.isZoomGesturesEnabled = true
            ui.isMyLocationButtonEnabled = false
        }
    }

    private fun setRotateDisable() {
        map?.uiSettings?.let { ui ->
            ui.setAllGesturesEnabled(true)
            ui.isMyLocationButtonEnabled = true
        }
        binding.iconImage.rotation = 0f
        map?.let { m ->
            val position = CameraPosition.Builder(m.cameraPosition)
                    .bearing(0f)
                    .build()
            m.moveCamera(CameraUpdateFactory.newCameraPosition(position))
        }
    }

    private fun selectPlace(position: Int) {
        searchResultAdapter.select(position)
        viewModel.onSelected(position, searchResultAdapter.getItem(position).latLng)
    }

    private fun sortMarkerByDistAsc() {
        markers.sortBy { it.second }
        markers.forEachIndexed { i, (marker, _) -> marker?.tag = i }
    }

    private fun sortMarkerByDistDesc() {
        markers.sortByDescending { it.second }
        markers.forEachIndexed { i, (marker, _) -> marker?.tag = i }
    }

    private fun sortMarkerByRateAsc() {
        markers.sortBy { it.third }
        markers.forEachIndexed { i, (marker, _) -> marker?.tag = i }
    }

    private fun sortMarkerByRateDesc() {
        markers.sortByDescending { it.third }
        markers.forEachIndexed { i, (marker, _) -> marker?.tag = i }
    }

    companion object {
        val TAG: String = MapFragment::class.java.simpleName
        private const val REQUEST_LOCATION_PERMISSION = 1000
        private const val INITIAL_ZOOM_LEVEL = 16f
        fun newInstance() = MapFragment()
    }
}