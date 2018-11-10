package jp.shiita.yorimichi.ui.dialog

import android.app.Activity
import android.app.Dialog
import android.app.PendingIntent
import android.app.TimePickerDialog
import android.content.Intent
import android.os.Bundle
import android.support.v4.app.DialogFragment
import android.widget.TimePicker


class TimePickerDialogFragment : DialogFragment() {
    private val hour: Int by lazy { arguments!!.getInt(ARGS_RESULT_HOUR) }
    private val minute: Int by lazy { arguments!!.getInt(ARGS_RESULT_MINUTE) }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog =
            TimePickerDialog(context, ::sendResult, hour, minute, true)

    private fun sendResult(view: TimePicker?, hour: Int, minute: Int) {
        val intent = Intent().apply {
            putExtra(ARGS_RESULT_HOUR, hour)
            putExtra(ARGS_RESULT_MINUTE, minute)
        }
        if (targetFragment != null)
            targetFragment?.onActivityResult(targetRequestCode, Activity.RESULT_OK, intent)
        else
            activity?.createPendingResult(targetRequestCode, intent, PendingIntent.FLAG_ONE_SHOT)
                    ?.send(Activity.RESULT_OK)
    }

    companion object {
        val TAG: String = TimePickerDialogFragment::class.java.simpleName
        private const val ARGS_RESULT_HOUR = "argsResultHour"
        private const val ARGS_RESULT_MINUTE = "argsResultMinute"

        fun newInstance(hour: Int, minute: Int) = TimePickerDialogFragment().apply {
            arguments = Bundle().apply {
                putInt(ARGS_RESULT_HOUR, hour)
                putInt(ARGS_RESULT_MINUTE, minute)
            }
        }

        fun parseResult(intent: Intent): Pair<Int, Int> =
                intent.getIntExtra(ARGS_RESULT_HOUR, 0) to intent.getIntExtra(ARGS_RESULT_MINUTE, 0)
    }
}