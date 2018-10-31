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
import android.view.*
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
    private var markers: MutableList<Pair<Marker?, Int>> = mutableListOf()
    private lateinit var smallDescriptor: BitmapDescriptor
    private lateinit var largeDescriptor: BitmapDescriptor
    private lateinit var selectedSmallDescriptor: BitmapDescriptor
    private lateinit var selectedLargeDescriptor: BitmapDescriptor

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        setHasOptionsMenu(true)
        binding = DataBindingUtil.inflate(inflater, R.layout.frag_search_result, container, false)
        return binding.root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        binding.setLifecycleOwner(this)
        binding.viewModel = viewModel
        mainViewModel.setupActionBar(R.string.title_search_result)

        searchResultAdapter = PlaceAdapter(context!!, mutableListOf(), viewModel::onSelected)
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

        initDescriptor()
        initMap()
        observe()
    }

    override fun onCreateOptionsMenu(menu: Menu?, inflater: MenuInflater?) {
        inflater?.inflate(R.menu.frag_search_result, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        when (item?.itemId) {
            R.id.menu_frag_search_result_sort_dist_asc  -> {
                searchResultAdapter.sortDistAsc()
                markers.sortBy { it.second }
                viewModel.updatePinPositions()
            }
            R.id.menu_frag_search_result_sort_dist_desc -> {
                markers.sortByDescending { it.second }
                searchResultAdapter.sortDistDesc()
                viewModel.updatePinPositions()
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
            markers.clear()
            markers.addAll(places.map {
                val marker = MarkerOptions()
                        .position(it.latLng)
                        .icon(smallDescriptor)
                map?.addMarker(marker) to it.getDistance()
            })
        }
        viewModel.zoomBounds.observe(this) { map?.moveCamera(CameraUpdateFactory.newLatLngBounds(it, 0)) }
        viewModel.smallPinPositions.observe(this) { positions -> positions.forEach { markers[it].first?.setIcon(smallDescriptor) }}
        viewModel.largePinPositions.observe(this) { positions -> positions.forEach { markers[it].first?.setIcon(largeDescriptor) }}
        viewModel.selectedSmallPinPositions.observe(this) { positions -> positions.forEach { markers[it].first?.setIcon(selectedSmallDescriptor) }}
        viewModel.selectedLargePinPositions.observe(this) { positions -> positions.forEach { markers[it].first?.setIcon(selectedLargeDescriptor) }}
    }

    companion object {
        val TAG: String = SearchResultFragment::class.java.simpleName

        fun newInstance() = SearchResultFragment()
    }
}