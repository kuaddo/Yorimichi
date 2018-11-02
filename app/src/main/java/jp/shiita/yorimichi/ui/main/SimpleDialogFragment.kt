package jp.shiita.yorimichi.ui.main

import android.app.Activity
import android.app.Dialog
import android.app.PendingIntent
import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import android.support.v4.app.DialogFragment
import android.support.v7.app.AlertDialog
import jp.shiita.yorimichi.R

class SimpleDialogFragment : DialogFragment() {
    private val message by lazy { arguments!!.getString(ARGS_MESSAGE) }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return AlertDialog.Builder(activity!!)
                .setMessage(message)
                .setPositiveButton(R.string.dialog_finish_positive) { _, _ -> sendResult(Activity.RESULT_OK) }
                .create()
    }

    override fun onCancel(dialog: DialogInterface?) {
        super.onCancel(dialog)
        sendResult(Activity.RESULT_CANCELED)
    }

    private fun sendResult(resultCode: Int) {
        if (targetFragment != null)
            targetFragment?.onActivityResult(targetRequestCode, resultCode, Intent())
        else
            activity?.createPendingResult(targetRequestCode, Intent(), PendingIntent.FLAG_ONE_SHOT)
                    ?.send(resultCode)
    }

    companion object {
        val TAG: String = SimpleDialogFragment::class.java.simpleName
        private const val ARGS_MESSAGE = "argsMessage"
        fun newInstance(message: String) = SimpleDialogFragment().apply {
            arguments = Bundle().apply { putString(ARGS_MESSAGE, message) }
        }
    }
}