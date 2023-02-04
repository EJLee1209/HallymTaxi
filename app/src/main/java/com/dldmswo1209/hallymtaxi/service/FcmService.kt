package com.dldmswo1209.hallymtaxi.service

import android.app.NotificationChannel
import android.app.NotificationManager
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.dldmswo1209.hallymtaxi.R
import com.dldmswo1209.hallymtaxi.common.dateToString
import com.dldmswo1209.hallymtaxi.model.Chat
import com.dldmswo1209.hallymtaxi.repository.RoomRepository
import com.google.firebase.Timestamp
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

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)
        val id = System.currentTimeMillis().toInt()

        val notificationManager = NotificationManagerCompat.from(applicationContext)

        if(notificationManager.getNotificationChannel(CHANNEL_ID) == null){
            val channel = NotificationChannel(CHANNEL_ID, CHANNEL_NAME, NotificationManager.IMPORTANCE_HIGH)
            notificationManager.createNotificationChannel(channel)
        }
        val builder: NotificationCompat.Builder =
            NotificationCompat.Builder(applicationContext, CHANNEL_ID)

        val chatId = remoteMessage.data["id"].toString()
        val roomId = remoteMessage.data["roomId"].toString()
        val userId = remoteMessage.data["userId"].toString()
        val userName = remoteMessage.data["userName"].toString()
        val message = remoteMessage.data["message"].toString()
        val messageType = remoteMessage.data["messageType"].toString()
        val dateTime = Timestamp.now().toDate().dateToString()
        val chat = Chat(chatId, roomId, userId, message, dateTime, messageType)

        val roomRepo = RoomRepository(this)
        roomRepo.saveChat(chat) // 채팅 저장

        Log.d("testt", "chatId: ${chatId}")
        Log.d("testt", "userId: ${userId}")
        Log.d("testt", "userName: ${userName}")
        Log.d("testt", "message: ${message}")
        Log.d("testt", "messageType: ${messageType}")
        Log.d("testt", "dateTime: ${dateTime}")

        builder.setContentTitle(userName)
            .setContentText(message)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setAutoCancel(true)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC).priority = NotificationCompat.PRIORITY_HIGH

        val notification = builder.build()
        notificationManager.notify(id, notification)
    }


}