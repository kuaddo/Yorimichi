package jp.shiita.yorimichi.ui.searchresult

import android.arch.lifecycle.ViewModelProvider
import android.arch.lifecycle.ViewModelProviders
import android.databinding.DataBindingUtil
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.CircleOptions
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import dagger.android.support.DaggerFragment
import jp.shiita.yorimichi.R
import jp.shiita.yorimichi.data.UserInfo
import jp.shiita.yorimichi.databinding.FragSearchResultBinding
import jp.shiita.yorimichi.ui.main.MainViewModel
import javax.inject.Inject


class SearchResultFragment : DaggerFragment() {
    @Inject lateinit var viewModelFactory: ViewModelProvider.Factory
    private val mainViewModel: MainViewModel
            by lazy { ViewModelProviders.of(activity!!, viewModelFactory).get(MainViewModel::class.java)}
    private val viewModel: SearchResultViewModel
            by lazy { ViewModelProviders.of(this, viewModelFactory).get(SearchResultViewModel::class.java) }
    private lateinit var binding: FragSearchResultBinding
    private lateinit var searchResultAdapter: SearchResultAdapter
    private val latLng: LatLng? = if (UserInfo.latitude.isNotEmpty() && UserInfo.longitude.isNotEmpty())
        LatLng(UserInfo.latitude.toDouble(), UserInfo.longitude.toDouble()) else null
    private var map: GoogleMap? = null
    private lateinit var markers: List<Marker?>

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = DataBindingUtil.inflate(inflater, R.layout.frag_search_result, container, false)
        return binding.root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        binding.setLifecycleOwner(this)
        binding.viewModel = viewModel
        mainViewModel.setupActionBar(R.string.title_search_result)

        searchResultAdapter = SearchResultAdapter(context!!, mutableListOf(), ::showMarker, ::hideMarker)
        binding.recyclerView.adapter = searchResultAdapter

        initMap()
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
            val locations = (-4..4).flatMap { dLat -> (-4..4).map { dLng -> LatLng(latLng.latitude + 0.001 * dLat, latLng.longitude + 0.001 * dLng) } }
            val options = locations.map {
                MarkerOptions()
                        .position(it)
                        .alpha(0f)
            }
            searchResultAdapter.addAll(locations)
            markers = options.map { map?.addMarker(it) }
            map?.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, INITIAL_ZOOM_LEVEL))
        }
    }

    private fun showMarker(position: Int) {
        markers[position]?.alpha = 1f
    }

    private fun hideMarker(position: Int) {
        markers[position]?.alpha = 0f
    }

    companion object {
        val TAG: String = SearchResultFragment::class.java.simpleName
        private const val INITIAL_ZOOM_LEVEL = 16f

        fun newInstance() = SearchResultFragment()
    }
}