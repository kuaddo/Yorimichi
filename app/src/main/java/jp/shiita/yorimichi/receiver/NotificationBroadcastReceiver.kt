package jp.shiita.yorimichi.receiver

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import android.support.v4.app.NotificationCompat
import android.support.v4.content.res.ResourcesCompat
import jp.shiita.yorimichi.R
import jp.shiita.yorimichi.ui.main.MainActivity

class NotificationBroadcastReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {
        if (context == null || intent == null || intent.action != ACTION_NOTIFICATION) return
        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val minute = intent.getIntExtra(ARGS_MINUTE, 0)
        val lats = intent.getDoubleArrayExtra(ARGS_LATS)
        val lngs = intent.getDoubleArrayExtra(ARGS_LNGS)

        createChannel(manager)
        val pendingIntent = PendingIntent.getActivity(context, REQUEST_SHOW_ROUTES, Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            putExtra(ARGS_LATS, lats)
            putExtra(ARGS_LNGS, lngs)
        }, PendingIntent.FLAG_UPDATE_CURRENT)

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_notification)
                .setContentTitle("そろそろ出発した方がいいよ！")
                .setContentText("予想移動時間${minute}分")
                .setColor(ResourcesCompat.getColor(context.resources, R.color.colorPrimary, null))
                .setContentIntent(pendingIntent)
                .build()
        manager.notify(NOTIFICATION_ID, notification)
    }

    private fun createChannel(manager: NotificationManager) {
        val name = "出発時間をお知らせ"
        if (Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            if (manager.getNotificationChannel(CHANNEL_ID) == null) {
                val channel = NotificationChannel(CHANNEL_ID, name, NotificationManager.IMPORTANCE_HIGH)
                channel.description = "出発時間の5分前に通知でお知らせします"
                manager.createNotificationChannel(channel)
            }
        }
    }

    companion object {
        const val ACTION_NOTIFICATION = "jp.shiita.yorimichi.action.notification"
        const val REQUEST_SHOW_ROUTES = 0
        const val ARGS_MINUTE = "argsMinute"
        const val ARGS_LATS = "argsLats"
        const val ARGS_LNGS = "argsLngs"
        private const val CHANNEL_ID = "remind"
        private const val NOTIFICATION_ID = 0
    }
}