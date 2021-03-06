package jp.shiita.yorimichi.ui.note

import android.app.Activity
import android.arch.lifecycle.ViewModelProvider
import android.arch.lifecycle.ViewModelProviders
import android.content.Intent
import android.databinding.DataBindingUtil
import android.os.Bundle
import android.view.*
import dagger.android.support.DaggerFragment
import jp.shiita.yorimichi.R
import jp.shiita.yorimichi.databinding.FragNoteBinding
import jp.shiita.yorimichi.ui.dialog.SimpleDialogFragment
import jp.shiita.yorimichi.ui.main.MainViewModel
import jp.shiita.yorimichi.util.loadAd
import jp.shiita.yorimichi.util.observe
import jp.shiita.yorimichi.util.toBytes
import javax.inject.Inject

class NoteFragment : DaggerFragment() {
    @Inject lateinit var viewModelFactory: ViewModelProvider.Factory
    private val mainViewModel: MainViewModel
            by lazy { ViewModelProviders.of(activity!!, viewModelFactory).get(MainViewModel::class.java)}
    private val viewModel: NoteViewModel
            by lazy { ViewModelProviders.of(this, viewModelFactory).get(NoteViewModel::class.java) }
    private val placeId: String by lazy { arguments!!.getString(ARGS_PLACE_ID) }
    private lateinit var binding: FragNoteBinding
    private lateinit var penAdapter: PenAdapter
    private lateinit var colorAdapter: ColorAdapter

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        setHasOptionsMenu(true)
        binding = DataBindingUtil.inflate(inflater, R.layout.frag_note, container, false)
        return binding.root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        binding.setLifecycleOwner(this)
        binding.viewModel = viewModel
        mainViewModel.setupActionBar(R.string.title_note)
        mainViewModel.setDrawerLock(true)

        binding.adView.loadAd()
        penAdapter = PenAdapter(context!!, viewModel::setPen)
        colorAdapter = ColorAdapter(context!!, mutableListOf(), viewModel::setPenColor)
        binding.penRecyclerView.adapter = penAdapter
        binding.colorRecyclerView.adapter = colorAdapter
        viewModel.getGoods()

        observe()
    }

    override fun onCreateOptionsMenu(menu: Menu?, inflater: MenuInflater?) {
        inflater?.inflate(R.menu.frag_note, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        when (item?.itemId) {
            R.id.menu_frag_note_upload -> {
                SimpleDialogFragment.newInstance(getString(R.string.dialog_upload_note_confirm_message), true).let {
                    it.setTargetFragment(this, REQUEST_UPLOAD_NOTE)
                    it.show(fragmentManager, SimpleDialogFragment.TAG)
                }
            }
            else -> return false
        }
        return true
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        when (requestCode) {
            REQUEST_UPLOAD_NOTE -> when (resultCode) {
                Activity.RESULT_OK -> viewModel.uploadNote(binding.paintView.getMainBitmap().toBytes(), placeId)
            }
        }
    }

    private fun observe() {
        // TODO: paintViewでもbindingする
        viewModel.pen.observe(this) { binding.paintView.setPen(it) }
        viewModel.penColor.observe(this) { binding.paintView.changePenColor(it) }
        viewModel.penColors.observe(this) { colorAdapter.reset(it) }
        viewModel.penWidth.observe(this) { binding.paintView.changePenWidth(it) }
        viewModel.canErase.observe(this) {
            if (it) {
                penAdapter.resetSelected()
            }
        }
        viewModel.uploadSuccessEvent.observe(this) {
            targetFragment?.onActivityResult(targetRequestCode, Activity.RESULT_OK, null)
            fragmentManager?.popBackStack()
        }
    }

    companion object {
        val TAG: String = NoteFragment::class.java.simpleName
        private const val REQUEST_UPLOAD_NOTE = 1000
        private const val ARGS_PLACE_ID = "argsPlaceId"
        fun newInstance(placeId: String) = NoteFragment().apply {
            arguments = Bundle().apply { putString(ARGS_PLACE_ID, placeId) }
        }
    }
}