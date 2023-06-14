package com.example.atomic_cinema

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import com.example.atomic_cinema.notification.UserNotificationService
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class AtomicCinemaApp : Application(){
    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
    }

    private fun createNotificationChannel() {
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
            val channel = NotificationChannel(
                UserNotificationService.USER_CHANNEL_ID,
                "Seances",
                NotificationManager.IMPORTANCE_DEFAULT
            )
            channel.description = "Необходим для того чтобы получать уведомления о предстоящих сеансах"

            val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }
}

