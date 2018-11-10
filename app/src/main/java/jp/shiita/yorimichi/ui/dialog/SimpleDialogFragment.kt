package jp.shiita.yorimichi.ui.dialog

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
    private val showsCancel by lazy { arguments!!.getBoolean(ARGS_SHOWS_CANCEL) }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val builder = AlertDialog.Builder(activity!!)
                .setMessage(message)
                .setPositiveButton(R.string.dialog_simple_positive) { _, _ -> sendResult(Activity.RESULT_OK) }
        if (showsCancel) builder.setNegativeButton(R.string.dialog_simple_negative) { _, _ -> sendResult(Activity.RESULT_CANCELED) }
        return builder.create()
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
        private const val ARGS_SHOWS_CANCEL = "argsShowsCancel"

        fun newInstance(message: String, showsCancel: Boolean) = SimpleDialogFragment().apply {
            arguments = Bundle().apply {
                putString(ARGS_MESSAGE, message)
                putBoolean(ARGS_SHOWS_CANCEL, showsCancel)
            }
        }
    }
}