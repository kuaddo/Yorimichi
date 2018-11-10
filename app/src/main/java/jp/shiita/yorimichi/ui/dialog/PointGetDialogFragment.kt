package jp.shiita.yorimichi.ui.dialog

import android.app.Dialog
import android.os.Bundle
import android.support.v4.app.DialogFragment
import android.view.View
import android.view.Window
import android.view.WindowManager
import android.widget.TextView
import jp.shiita.yorimichi.R

class PointGetDialogFragment : DialogFragment() {
    private val gotPoints by lazy { arguments!!.getInt(ARGS_GOT_POINTS) }
    private val additionalPoints by lazy { arguments!!.getInt(ARGS_ADDITIONAL_POINTS) }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return Dialog(activity!!).also { d ->
            d.window?.run {
                requestFeature(Window.FEATURE_NO_TITLE)
                setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN)
                setBackgroundDrawableResource(R.color.colorTransparent)
            }
            d.setContentView(R.layout.frag_point_get_dialog)
            d.findViewById<TextView>(R.id.mainTextView).text = getString(R.string.dialog_point_get_main_message, gotPoints)
            d.findViewById<TextView>(R.id.movieTextView).text = getString(R.string.dialog_point_get_movie_message, additionalPoints)
            d.findViewById<View>(R.id.closeButton).setOnClickListener { dismiss() }
            d.findViewById<View>(R.id.watchMovieButton).setOnClickListener {  }     // TODO: add reward add
        }
    }

    companion object {
        val TAG: String = PointGetDialogFragment::class.java.simpleName
        private const val ARGS_GOT_POINTS = "argsGotPoints"
        private const val ARGS_ADDITIONAL_POINTS = "argsAdditionalPoints"

        fun newInstance(gotPoints: Int, additionalPoints: Int) = PointGetDialogFragment().apply {
            arguments = Bundle().apply {
                putInt(ARGS_GOT_POINTS, gotPoints)
                putInt(ARGS_ADDITIONAL_POINTS, additionalPoints)
            }
        }
    }
}