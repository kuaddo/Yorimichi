package jp.shiita.yorimichi.ui.searchresult

import android.arch.lifecycle.ViewModelProvider
import android.arch.lifecycle.ViewModelProviders
import android.databinding.DataBindingUtil
import android.graphics.Bitmap
import android.graphics.Color
import android.os.Bundle
import android.support.v4.content.res.ResourcesCompat
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.*
import dagger.android.support.DaggerFragment
import jp.shiita.yorimichi.R
import jp.shiita.yorimichi.data.UserInfo
import jp.shiita.yorimichi.databinding.FragSearchResultBinding
import jp.shiita.yorimichi.ui.main.MainViewModel
import jp.shiita.yorimichi.util.getBitmap
import jp.shiita.yorimichi.util.observe
import javax.inject.Inject


class SearchResultFragment : DaggerFragment() {
    @Inject lateinit var viewModelFactory: ViewModelProvider.Factory
    private val mainViewModel: MainViewModel
            by lazy { ViewModelProviders.of(activity!!, viewModelFactory).get(MainViewModel::class.java)}
    private val viewModel: SearchResultViewModel
            by lazy { ViewModelProviders.of(this, viewModelFactory).get(SearchResultViewModel::class.java) }
    private lateinit var binding: FragSearchResultBinding
    private lateinit var searchResultAdapter: PlaceAdapter
    private val latLng: LatLng? = UserInfo.latLng
    private var map: GoogleMap? = null
    private lateinit var markers: List<Marker?>
    private lateinit var smallDescriptor: BitmapDescriptor
    private lateinit var largeDescriptor: BitmapDescriptor

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = DataBindingUtil.inflate(inflater, R.layout.frag_search_result, container, false)
        return binding.root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        binding.setLifecycleOwner(this)
        binding.viewModel = viewModel
        mainViewModel.setupActionBar(R.string.title_search_result)

        searchResultAdapter = PlaceAdapter(context!!, mutableListOf())
        binding.recyclerView.also { rv ->
            val layoutManager = rv.layoutManager as LinearLayoutManager
            rv.adapter = searchResultAdapter
            rv.clearOnScrollListeners()
            rv.addOnScrollListener(object : RecyclerView.OnScrollListener() {
                private var oldFirst = 0
                private var oldLast = 0

                override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                    super.onScrolled(recyclerView, dx, dy)
                    val first = layoutManager.findFirstVisibleItemPosition()
                    val last = layoutManager.findLastVisibleItemPosition()
                    if (first > oldFirst) (oldFirst until first).forEach { markers[it]?.setIcon(smallDescriptor) }    // invisible
                    if (last < oldLast) (last + 1..oldLast).forEach      { markers[it]?.setIcon(smallDescriptor) }    // invisible
                    (first..last).forEach                                { markers[it]?.setIcon(largeDescriptor) }    // visible

                    oldFirst = first
                    oldLast = last
                }
            })
        }

        initDescriptor()
        initMap()
        observe()
    }

    private fun initDescriptor() {
        val pinDrawable = ResourcesCompat.getDrawable(resources, R.drawable.ic_pin_large, null)!!
        val largeBitmap = pinDrawable.getBitmap(ResourcesCompat.getColor(resources, R.color.colorPrimary, null))
        val smallBitmap = Bitmap.createScaledBitmap(largeBitmap, largeBitmap.width / 2, largeBitmap.height / 2, false)
        smallDescriptor = BitmapDescriptorFactory.fromBitmap(smallBitmap)
        largeDescriptor = BitmapDescriptorFactory.fromBitmap(largeBitmap)
    }

    private fun initMap() {
        (childFragmentManager.findFragmentById(R.id.google_map) as SupportMapFragment).getMapAsync { googleMap ->
            map = googleMap
            latLng ?: return@getMapAsync

            map?.addCircle(CircleOptions()    // 現在地
                    .center(latLng)
                    .radius(10.0)
                    .fillColor(Color.BLUE)
                    .strokeColor(Color.BLUE))

            viewModel.searchPlaces(latLng.latitude, latLng.longitude)
        }
    }

    private fun observe() {
        viewModel.places.observe(this) { places ->
            searchResultAdapter.reset(places)
            markers = places.map {
                val marker = MarkerOptions()
                        .position(it.latLng)
                        .icon(smallDescriptor)
                map?.addMarker(marker)
            }
        }
        viewModel.zoomBounds.observe(this) { map?.moveCamera(CameraUpdateFactory.newLatLngBounds(it, 0)) }
    }

    companion object {
        val TAG: String = SearchResultFragment::class.java.simpleName

        fun newInstance() = SearchResultFragment()
    }
}