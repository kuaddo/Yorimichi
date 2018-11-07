package jp.shiita.yorimichi.ui.remind

import android.arch.lifecycle.ViewModelProvider
import android.arch.lifecycle.ViewModelProviders
import android.databinding.DataBindingUtil
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.SupportMapFragment
import dagger.android.support.DaggerFragment
import jp.shiita.yorimichi.R
import jp.shiita.yorimichi.data.UserInfo
import jp.shiita.yorimichi.databinding.FragRemindBinding
import jp.shiita.yorimichi.util.observe
import javax.inject.Inject

class RemindFragment : DaggerFragment() {
    @Inject lateinit var viewModelFactory: ViewModelProvider.Factory
    private val viewModel: RemindViewModel
            by lazy { ViewModelProviders.of(this, viewModelFactory).get(RemindViewModel::class.java) }
    private lateinit var binding: FragRemindBinding
    private var map: GoogleMap? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        setHasOptionsMenu(true)
        binding = DataBindingUtil.inflate(inflater, R.layout.frag_remind, container, false)
        return binding.root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        binding.setLifecycleOwner(this)
        binding.viewModel = viewModel

        initMap()
        observe()
    }

    private fun initMap() {
        (childFragmentManager.findFragmentById(R.id.googleMap) as SupportMapFragment).getMapAsync { googleMap ->
            map = googleMap
            map?.animateCamera(CameraUpdateFactory.newLatLngZoom(UserInfo.latLng, INITIAL_ZOOM_LEVEL))
        }
    }

    private fun observe() {
        viewModel.finishEvent.observe(this) {
            // TODO: set result
            fragmentManager?.popBackStack()
        }
    }

    companion object {
        val TAG: String = RemindFragment::class.java.simpleName
        private const val INITIAL_ZOOM_LEVEL = 16f
        fun newInstance() = RemindFragment()
    }
}