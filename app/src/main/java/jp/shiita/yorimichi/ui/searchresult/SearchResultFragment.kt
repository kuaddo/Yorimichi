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
    private var map: GoogleMap? = null
    private val latLng = LatLng(UserInfo.latitude.toDouble(), UserInfo.longitude.toDouble())

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = DataBindingUtil.inflate(inflater, R.layout.frag_search_result, container, false)
        return binding.root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        binding.setLifecycleOwner(this)
        binding.viewModel = viewModel
        mainViewModel.setupActionBar(R.string.title_search_result)

        initMap()
    }

    private fun initMap() {
        (childFragmentManager.findFragmentById(R.id.google_map) as SupportMapFragment).getMapAsync { googleMap ->
            map = googleMap
            map?.addCircle(CircleOptions()    // 現在地
                    .center(latLng)
                    .radius(10.0)
                    .fillColor(Color.BLUE)
                    .strokeColor(Color.BLUE))
            map?.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, INITIAL_ZOOM_LEVEL))
        }
    }

    companion object {
        val TAG: String = SearchResultFragment::class.java.simpleName
        private const val INITIAL_ZOOM_LEVEL = 16f

        fun newInstance() = SearchResultFragment()
    }
}