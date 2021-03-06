package jp.shiita.yorimichi.ui.remind

import android.Manifest
import android.app.Activity
import android.app.AlarmManager
import android.app.PendingIntent
import android.arch.lifecycle.ViewModelProvider
import android.arch.lifecycle.ViewModelProviders
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.databinding.DataBindingUtil
import android.os.Build
import android.os.Bundle
import android.support.v4.app.ActivityCompat
import android.support.v4.content.res.ResourcesCompat
import android.support.v7.widget.SearchView
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
import jp.shiita.yorimichi.databinding.FragRemindBinding
import jp.shiita.yorimichi.receiver.NotificationBroadcastReceiver
import jp.shiita.yorimichi.receiver.NotificationBroadcastReceiver.Companion.ARGS_LATS
import jp.shiita.yorimichi.receiver.NotificationBroadcastReceiver.Companion.ARGS_LNGS
import jp.shiita.yorimichi.receiver.NotificationBroadcastReceiver.Companion.ARGS_MINUTE
import jp.shiita.yorimichi.receiver.NotificationBroadcastReceiver.Companion.REQUEST_SHOW_ROUTES
import jp.shiita.yorimichi.ui.dialog.TimePickerDialogFragment
import jp.shiita.yorimichi.ui.search.SearchFragment
import jp.shiita.yorimichi.util.getBitmap
import jp.shiita.yorimichi.util.observe
import javax.inject.Inject

class RemindFragment : DaggerFragment() {
    @Inject lateinit var viewModelFactory: ViewModelProvider.Factory
    private val viewModel: RemindViewModel
            by lazy { ViewModelProviders.of(this, viewModelFactory).get(RemindViewModel::class.java) }
    private lateinit var binding: FragRemindBinding
    private lateinit var descriptor: BitmapDescriptor
    private lateinit var selectedDescriptor: BitmapDescriptor
    private var map: GoogleMap? = null
    private var marker: Marker? = null
    private var markers: MutableList<Marker?> = mutableListOf()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        setHasOptionsMenu(true)
        binding = DataBindingUtil.inflate(inflater, R.layout.frag_remind, container, false)
        return binding.root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        binding.setLifecycleOwner(this)
        binding.viewModel = viewModel
        viewModel.startLatLng = arguments!!.getParcelable(ARGS_START_LAT_LNG)!!

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

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (resultCode != Activity.RESULT_OK || data == null) return

        when (requestCode) {
            REQUEST_SHOW_TIME_PICKER -> {
                val (hour, minute) = TimePickerDialogFragment.parseResult(data)
                viewModel.setTime(hour, minute)
            }
        }
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

        (childFragmentManager.findFragmentById(R.id.googleMap) as SupportMapFragment).getMapAsync { googleMap ->
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
                    marker?.tag = SearchFragment.MarkerTag(latLng, true)
                }
                else {
                    marker?.let {
                        it.isVisible = true
                        it.position = latLng
                        it.tag = SearchFragment.MarkerTag(latLng, true)
                    }
                }
            }
            map?.setOnMarkerClickListener { marker ->
                resetMarkerSelected()
                marker?.tag?.let { if (it is SearchFragment.MarkerTag) {
                    it.selected = true
                    viewModel.select(it.latLng)
                } }
                marker?.setIcon(selectedDescriptor)
                false
            }
        }
    }

    private fun observe() {
        viewModel.places.observe(this) { places ->
            clearMap()
            markers.addAll(places.map {
                val marker = map?.addMarker(MarkerOptions()
                        .position(it.latLng)
                        .icon(descriptor))
                marker?.tag = SearchFragment.MarkerTag(it.latLng, false)
                marker
            })
        }
        viewModel.zoomBounds.observe(this) { map?.moveCamera(CameraUpdateFactory.newLatLngBounds(it, 0)) }
        viewModel.showTimePickerEvent.observe(this) { timePair ->
            TimePickerDialogFragment.newInstance(timePair.first, timePair.second).also { fragment ->
                fragment.setTargetFragment(this, REQUEST_SHOW_TIME_PICKER)
            }.show(fragmentManager, TimePickerDialogFragment.TAG)
        }
        viewModel.notificationEvent.observe(this) {
            setNotification(it.first, it.second, it.third)
        }
        viewModel.finishEvent.observe(this) {
            // TODO: set result
            fragmentManager?.popBackStack()
        }
    }

    private fun resetMarkerSelected() {
        marker?.tag?.let {
            if (it is SearchFragment.MarkerTag && it.selected) {
                it.selected = false
                marker?.isVisible = false
            }
        }
        markers.forEach { m ->
            m?.tag?.let {
                if (it is SearchFragment.MarkerTag && it.selected) {
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

    private fun setNotification(minute: Int, routes: List<LatLng>, timeInMillis: Long) {
        val manager = context?.getSystemService(Context.ALARM_SERVICE) as? AlarmManager ?: return
        val intent = Intent(context, NotificationBroadcastReceiver::class.java).apply {
            action = NotificationBroadcastReceiver.ACTION_NOTIFICATION
            putExtra(ARGS_MINUTE, minute)
            putExtra(ARGS_LATS, routes.map { it.latitude }.toDoubleArray())
            putExtra(ARGS_LNGS, routes.map { it.longitude }.toDoubleArray())
        }
        val pendingIntent = PendingIntent.getBroadcast(context, REQUEST_SHOW_ROUTES, intent, PendingIntent.FLAG_UPDATE_CURRENT)

        if (Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M)
            manager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, timeInMillis, pendingIntent)
        else
            manager.setExact(AlarmManager.RTC_WAKEUP, timeInMillis, pendingIntent)
    }

    companion object {
        val TAG: String = RemindFragment::class.java.simpleName
        private const val REQUEST_SHOW_TIME_PICKER = 1000
        private const val ARGS_START_LAT_LNG = "argsStartLatLng"
        private const val INITIAL_ZOOM_LEVEL = 16f

        fun newInstance(startLatLng: LatLng) = RemindFragment().apply {
            arguments = Bundle().apply { putParcelable(ARGS_START_LAT_LNG, startLatLng) }
        }
    }
}