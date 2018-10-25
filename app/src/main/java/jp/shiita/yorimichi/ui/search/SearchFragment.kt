package jp.shiita.yorimichi.ui.search

import android.arch.lifecycle.ViewModelProvider
import android.arch.lifecycle.ViewModelProviders
import android.databinding.DataBindingUtil
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.MotionEvent
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
import jp.shiita.yorimichi.databinding.FragSearchBinding
import jp.shiita.yorimichi.ui.searchresult.SearchResultFragment
import jp.shiita.yorimichi.util.observe
import jp.shiita.yorimichi.util.replaceFragment
import javax.inject.Inject

class SearchFragment : DaggerFragment() {
    @Inject lateinit var viewModelFactory: ViewModelProvider.Factory
    private val viewModel: SearchViewModel
            by lazy { ViewModelProviders.of(this, viewModelFactory).get(SearchViewModel::class.java) }
    private lateinit var binding: FragSearchBinding
    private lateinit var categoryAdapter: CategoryAdapter
    private val latLng: LatLng? = if (UserInfo.latitude.isNotEmpty() && UserInfo.longitude.isNotEmpty())
        LatLng(UserInfo.latitude.toDouble(), UserInfo.longitude.toDouble()) else null
    private var map: GoogleMap? = null
    private var marker: Marker? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = DataBindingUtil.inflate(inflater, R.layout.frag_search, container, false)
        return binding.root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        binding.setLifecycleOwner(this)
        binding.viewModel = viewModel

        categoryAdapter = CategoryAdapter(context!!, mutableListOf(
            "shopping_mall" to false,
            "library" to false,
            "cafe" to false,
            "book_store" to false,
            "park" to false,
            "movie_theater" to false,
            "home_goods_store" to false,
            "clothing_store" to false,
            "bar" to false
        ))
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

        initMap()
        observe()
    }

    private fun initMap() {
        (childFragmentManager.findFragmentById(R.id.google_map) as SupportMapFragment).getMapAsync { googleMap ->
            map = googleMap
            map?.setOnMapLongClickListener { latLng ->
                viewModel.select(latLng)
                if (marker == null) {
                    marker = map?.addMarker(MarkerOptions().position(latLng))
                }
                else {
                    marker?.position = latLng
                }
            }

            latLng ?: return@getMapAsync
            map?.addCircle(CircleOptions()    // 現在地
                    .center(latLng)
                    .radius(10.0)
                    .fillColor(Color.BLUE)
                    .strokeColor(Color.BLUE))
            map?.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, INITIAL_ZOOM_LEVEL))
        }
    }

    private fun observe() {
        viewModel.searchEvent.observe(this) {
            // SearchFragmentはネストされたフラグメントであるため
            activity?.supportFragmentManager?.replaceFragment(
                    R.id.container,
                    SearchResultFragment.newInstance(),
                    SearchResultFragment.TAG)
        }
    }

    companion object {
        val TAG: String = SearchFragment::class.java.simpleName
        private const val INITIAL_ZOOM_LEVEL = 16f

        fun newInstance() = SearchFragment()
    }
}