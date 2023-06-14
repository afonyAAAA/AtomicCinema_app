package com.example.atomic_cinema.notification

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.navigation.NavHostController
import com.example.atomic_cinema.navigation.NavRoutes


class UserNotificationReceiver(val navHostController: NavHostController) : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent?) {
        val service = UserNotificationService(context)
        service.showNotification()
    }
}