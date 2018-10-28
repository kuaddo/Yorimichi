package jp.shiita.yorimichi.ui.main

import android.app.Dialog
import android.content.DialogInterface
import android.os.Bundle
import android.support.v4.app.DialogFragment
import android.support.v7.app.AlertDialog
import jp.shiita.yorimichi.R

class FinishDialogFragment : DialogFragment() {
    private val message by lazy { arguments!!.getString(ARGS_MESSAGE) }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return AlertDialog.Builder(activity!!)
                .setMessage(message)
                .setPositiveButton(R.string.dialog_finish_positive) { _, _ -> activity?.finish() }
                .create()
    }

    override fun onCancel(dialog: DialogInterface?) {
        super.onCancel(dialog)
        activity?.finish()
    }

    companion object {
        val TAG: String = FinishDialogFragment::class.java.simpleName
        private const val ARGS_MESSAGE = "argsMessage"
        fun newInstance(message: String) = FinishDialogFragment().apply {
            arguments = Bundle().apply { putString(ARGS_MESSAGE, message) }
        }
    }
}