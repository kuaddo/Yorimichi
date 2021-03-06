package jp.shiita.yorimichi.ui.search

import android.Manifest
import android.arch.lifecycle.ViewModelProvider
import android.arch.lifecycle.ViewModelProviders
import android.content.pm.PackageManager
import android.databinding.DataBindingUtil
import android.os.Bundle
import android.support.v4.app.ActivityCompat
import android.support.v4.content.res.ResourcesCompat
import android.support.v7.widget.SearchView
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.*
import dagger.android.support.DaggerFragment
import jp.shiita.yorimichi.R
import jp.shiita.yorimichi.data.UserInfo
import jp.shiita.yorimichi.databinding.FragSearchBinding
import jp.shiita.yorimichi.ui.main.MainViewModel
import jp.shiita.yorimichi.util.getBitmap
import jp.shiita.yorimichi.util.observe
import javax.inject.Inject

class SearchFragment : DaggerFragment() {
    @Inject lateinit var viewModelFactory: ViewModelProvider.Factory
    private val mainViewModel: MainViewModel
            by lazy { ViewModelProviders.of(activity!!, viewModelFactory).get(MainViewModel::class.java)}
    private val viewModel: SearchViewModel
            by lazy { ViewModelProviders.of(this, viewModelFactory).get(SearchViewModel::class.java) }
    private lateinit var binding: FragSearchBinding
    private lateinit var categoryAdapter: CategoryAdapter
    private lateinit var descriptor: BitmapDescriptor
    private lateinit var selectedDescriptor: BitmapDescriptor
    private var map: GoogleMap? = null
    private var marker: Marker? = null
    private var markers: MutableList<Marker?> = mutableListOf()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        setHasOptionsMenu(true)
        binding = DataBindingUtil.inflate(inflater, R.layout.frag_search, container, false)
        return binding.root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        binding.setLifecycleOwner(this)
        binding.viewModel = viewModel

        val categories = resources.getStringArray(R.array.place_types).map { it to false }.toMutableList()
        categories[0] = categories[0].copy(second = true)
        categories.addAll(0, UserInfo.originalCategories.map { it to false })
        categoryAdapter = CategoryAdapter(context!!, categories)
        binding.categoryRecyclerView.adapter = categoryAdapter

        // GoogleMapsのジェスチャーがScrollView内で動くように
        binding.transparentView.setOnTouchListener { _, event -> when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                binding.scrollView.requestDisallowInterceptTouchEvent(true)
                false
            }
            MotionEvent.ACTION_UP -> {
                binding.scrollView.requestDisallowInterceptTouchEvent(false)
                true
            }
            MotionEvent.ACTION_MOVE -> {
                binding.scrollView.requestDisallowInterceptTouchEvent(true)
                false
            }
            else -> true
        } }

        binding.searchView.isSubmitButtonEnabled = true
        binding.searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener{
            override fun onQueryTextSubmit(text: String?): Boolean {
                text ?: return false
                val keywords = text.split(Regex("\\s+"))
                if (keywords.isEmpty()) return false
                viewModel.searchPlaces(keywords)
                return false
            }

            override fun onQueryTextChange(text: String?): Boolean = false
        })

        initMap()
        observe()
    }

    /**
     * BitmapDescriptorFactory.fromBitmap()の内部で用いられている
     * IBitmapDescriptorFactoryはMapの準備が出来てから初期化されることに注意する
     */
    private fun initDescriptor() {
        val pinDrawable = ResourcesCompat.getDrawable(resources, R.drawable.ic_pin_large, null)!!
        val bitmap = pinDrawable.getBitmap(ResourcesCompat.getColor(resources, R.color.colorPrimary, null))
        val selectedBitmap = pinDrawable.getBitmap(ResourcesCompat.getColor(resources, R.color.colorStar, null))
        descriptor = BitmapDescriptorFactory.fromBitmap(bitmap)
        selectedDescriptor = BitmapDescriptorFactory.fromBitmap(selectedBitmap)
    }

    private fun initMap() {
        if (ActivityCompat.checkSelfPermission(context!!, Manifest.permission.ACCESS_FINE_LOCATION)   != PackageManager.PERMISSION_GRANTED &&
            ActivityCompat.checkSelfPermission(context!!, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return
        }

        (childFragmentManager.findFragmentById(R.id.google_map) as SupportMapFragment).getMapAsync { googleMap ->
            initDescriptor()
            map = googleMap
            map?.moveCamera(CameraUpdateFactory.newLatLngZoom(UserInfo.latLng, INITIAL_ZOOM_LEVEL))
            map?.isMyLocationEnabled = true

            map?.setOnMapLongClickListener { latLng ->
                viewModel.select(latLng)
                resetMarkerSelected()
                if (marker == null) {
                    marker = map?.addMarker(MarkerOptions()
                            .position(latLng)
                            .icon(selectedDescriptor))
                    marker?.tag = MarkerTag(latLng, true)
                }
                else {
                    marker?.let {
                        it.isVisible = true
                        it.position = latLng
                        it.tag = MarkerTag(latLng, true)
                    }
                }
            }
            map?.setOnMarkerClickListener { marker ->
                resetMarkerSelected()
                marker?.tag?.let { if (it is MarkerTag) {
                    it.selected = true
                    viewModel.select(it.latLng)
                } }
                marker?.setIcon(selectedDescriptor)
                false
            }
        }
    }

    private fun observe() {
        viewModel.searchRadiusEvent.observe(this) { mainViewModel.search(categoryAdapter.getSelectedCategories(), it) }
        viewModel.directionsEvent.observe(this) {
            clearMap()
            binding.searchView.setQuery("", false)
            mainViewModel.callDirectionsEvent(it)
        }
        viewModel.places.observe(this) { places ->
            clearMap()
            markers.addAll(places.map {
                val marker = map?.addMarker(MarkerOptions()
                        .position(it.latLng)
                        .icon(descriptor))
                marker?.tag = MarkerTag(it.latLng, false)
                marker
            })
        }
        viewModel.zoomBounds.observe(this) { map?.moveCamera(CameraUpdateFactory.newLatLngBounds(it, 0)) }
    }

    private fun resetMarkerSelected() {
        marker?.tag?.let {
            if (it is MarkerTag && it.selected) {
                it.selected = false
                marker?.isVisible = false
            }
        }
        markers.forEach { m ->
            m?.tag?.let {
                if (it is MarkerTag && it.selected) {
                    it.selected = false
                    m.setIcon(descriptor)
                }
            }
        }
    }

    private fun clearMap() {
        map?.clear()
        marker = null
        markers.clear()
    }

    data class MarkerTag(val latLng: LatLng, var selected: Boolean)

    companion object {
        val TAG: String = SearchFragment::class.java.simpleName
        private const val INITIAL_ZOOM_LEVEL = 16f

        fun newInstance() = SearchFragment()
    }
}