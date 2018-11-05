package jp.shiita.yorimichi.ui.remind

import android.databinding.DataBindingUtil
import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.SupportMapFragment
import jp.shiita.yorimichi.R
import jp.shiita.yorimichi.data.UserInfo
import jp.shiita.yorimichi.databinding.FragRemindBinding

class RemindFragment : Fragment() {
    private lateinit var binding: FragRemindBinding
    private var map: GoogleMap? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        setHasOptionsMenu(true)
        binding = DataBindingUtil.inflate(inflater, R.layout.frag_remind, container, false)
        return binding.root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        // reached
        binding.needButton.setOnClickListener {
            binding.reachedLayout.visibility = View.GONE
            binding.gotoLayout.visibility = View.VISIBLE
        }
        binding.noNeedButton.setOnClickListener {
            binding.reachedLayout.visibility = View.GONE
            binding.finishLayout.visibility = View.VISIBLE
        }

        // goto
        binding.goBackButton.setOnClickListener {
            binding.gotoLayout.visibility = View.GONE
            binding.timeLayout.visibility = View.VISIBLE
        }
        binding.goOtherButton.setOnClickListener {
            binding.gotoLayout.visibility = View.GONE
            binding.placeLayout.visibility = View.VISIBLE
        }

        // place
        binding.gotoPlaceButton.setOnClickListener {
            binding.placeLayout.visibility = View.GONE
            binding.timeLayout.visibility = View.VISIBLE
        }

        // time
        binding.timeSelectedButton.setOnClickListener {
            binding.timeLayout.visibility = View.GONE
            binding.finishLayout.visibility = View.VISIBLE
            binding.finishText.text = getText(R.string.remind_need_finish_message)
            // TODO: set notification
        }

        // finish
        binding.finishButton.setOnClickListener {
            // TODO: set result
            fragmentManager?.popBackStack()
        }

        initMap()
    }

    private fun initMap() {
        (childFragmentManager.findFragmentById(R.id.googleMap) as SupportMapFragment).getMapAsync { googleMap ->
            map = googleMap
            map?.animateCamera(CameraUpdateFactory.newLatLngZoom(UserInfo.latLng, INITIAL_ZOOM_LEVEL))
        }
    }

    companion object {
        val TAG: String = RemindFragment::class.java.simpleName
        private const val INITIAL_ZOOM_LEVEL = 16f
        fun newInstance() = RemindFragment()
    }
}