package com.dldmswo1209.hallymtaxi.service

import android.app.NotificationChannel
import android.app.NotificationManager
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.dldmswo1209.hallymtaxi.R
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage

class FcmService: FirebaseMessagingService() {

    companion object{
        const val CHANNEL_ID = "hallym_univ"
        const val CHANNEL_NAME = "hallym_univ"
    }


    override fun onNewToken(token: String) {
        super.onNewToken(token)
        Log.d("testt", "onNewToken: $token")

    }

    override fun onMessageReceived(message: RemoteMessage) {
        super.onMessageReceived(message)
        val id = System.currentTimeMillis().toInt()
        // 수신한 메시지를 처리
        val notificationManager = NotificationManagerCompat.from(applicationContext)

        if(notificationManager.getNotificationChannel(CHANNEL_ID) == null){
            val channel = NotificationChannel(CHANNEL_ID, CHANNEL_NAME, NotificationManager.IMPORTANCE_DEFAULT)
            notificationManager.createNotificationChannel(channel)
        }
        var builder : NotificationCompat.Builder =
            NotificationCompat.Builder(applicationContext, CHANNEL_ID)

        val title = message.notification?.title
        val body = message.notification?.body

        builder.setContentTitle(title)
            .setContentText(body)
            .setSmallIcon(R.drawable.ic_launcher_background)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setAutoCancel(true)
            .priority = NotificationCompat.PRIORITY_HIGH

        val notification = builder.build()
        notificationManager.notify(id, notification)

    }


}