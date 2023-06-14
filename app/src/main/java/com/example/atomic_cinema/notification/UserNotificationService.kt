package com.example.atomic_cinema.notification


import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import com.example.atomic_cinema.MainActivity
import com.example.atomic_cinema.R

class UserNotificationService(private val context : Context) {

    private val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

    fun showNotification(){
        val intent = Intent(context, MainActivity::class.java).apply {
            putExtra("notification_data", "open_tickets_screen")
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }

        val pendingIntent = PendingIntent.getActivity(
            context, 1, intent, if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) PendingIntent.FLAG_IMMUTABLE else 0)

        val notification = NotificationCompat.Builder(context, USER_CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_baseline_campaign_24)
            .setContentTitle("Уведомление о сегоднешнем сеансе")
            .setContentText("Посмотрите ваши билеты")
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()

        notificationManager.notify(1, notification)
    }


    companion object{
        const val USER_CHANNEL_ID = "user_channel"
    }
}