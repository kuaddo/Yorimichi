package jp.shiita.yorimichi.ui.map

import android.Manifest
import android.arch.lifecycle.ViewModelProvider
import android.arch.lifecycle.ViewModelProviders
import android.content.pm.PackageManager
import android.databinding.DataBindingUtil
import android.graphics.Bitmap
import android.os.Bundle
import android.support.v4.app.ActivityCompat
import android.support.v4.content.res.ResourcesCompat
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.*
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.BitmapDescriptor
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import dagger.android.support.DaggerFragment
import jp.shiita.yorimichi.R
import jp.shiita.yorimichi.data.UserInfo
import jp.shiita.yorimichi.databinding.FragMapBinding
import jp.shiita.yorimichi.live.LocationLiveData
import jp.shiita.yorimichi.ui.main.MainViewModel
import jp.shiita.yorimichi.util.getBitmap
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
    private lateinit var searchResultAdapter: PlaceAdapter
    private var map: GoogleMap? = null
    private var markers: MutableList<Pair<Marker?, Int>> = mutableListOf()
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

        searchResultAdapter = PlaceAdapter(context!!, mutableListOf(), ::selectPlace)
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
        inflater?.inflate(R.menu.frag_search_result, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        when (item?.itemId) {
            R.id.menu_frag_search_result_sort_dist_asc  -> {
                sortMarkerByDistAsc()
                searchResultAdapter.sortByDistAsc()
                viewModel.onSelected(searchResultAdapter.getSelectedPosition(), null)
            }
            R.id.menu_frag_search_result_sort_dist_desc -> {
                sortMarkerByDistDesc()
                searchResultAdapter.sortByDistDesc()
                viewModel.onSelected(searchResultAdapter.getSelectedPosition(), null)
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
            map?.isMyLocationEnabled = true

            map?.setOnMarkerClickListener { marker ->
                val position = marker?.tag as? Int ?: 0
                binding.recyclerView.scrollToPosition(position)
                selectPlace(position)
                true        // cameraのアニメーションは自前でやる
            }

            viewModel.searchPlaces()
        }
    }

    private fun observe() {
        locationLiveData.observe(this) { viewModel.setLatLng(it.latLng) }
        mainViewModel.searchEvent.observe(this) { viewModel.searchPlaces() }
        viewModel.latLng.observe(this) { UserInfo.latLng = it }
        viewModel.places.observe(this) { places ->
            searchResultAdapter.reset(places)
            markers.clear()
            map?.clear()
            markers.addAll(places.map {
                val marker = MarkerOptions()
                        .position(it.latLng)
                        .icon(smallDescriptor)
                map?.addMarker(marker) to it.getDistance()
            })
            markers.forEachIndexed { i, (marker, _) -> marker?.tag = i }
        }
        viewModel.zoomBounds.observe(this) { map?.moveCamera(CameraUpdateFactory.newLatLngBounds(it, 0)) }
        viewModel.moveCameraEvent.observe(this) { map?.animateCamera(CameraUpdateFactory.newLatLng(it)) }
        viewModel.smallPinPositions.observe(this) { positions -> positions.forEach { markers[it].first?.setIcon(smallDescriptor) }}
        viewModel.largePinPositions.observe(this) { positions -> positions.forEach { markers[it].first?.setIcon(largeDescriptor) }}
        viewModel.selectedSmallPinPositions.observe(this) { positions -> positions.forEach { markers[it].first?.setIcon(selectedSmallDescriptor) }}
        viewModel.selectedLargePinPositions.observe(this) { positions -> positions.forEach { markers[it].first?.setIcon(selectedLargeDescriptor) }}
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

    companion object {
        val TAG: String = MapFragment::class.java.simpleName
        private const val REQUEST_LOCATION_PERMISSION = 0
        fun newInstance() = MapFragment()
    }
}