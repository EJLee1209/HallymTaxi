package com.dldmswo1209.hallymtaxi.service

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Intent
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.app.Person
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.dldmswo1209.hallymtaxi.R
import com.dldmswo1209.hallymtaxi.common.GlobalVariable
import com.dldmswo1209.hallymtaxi.model.Chat
import com.dldmswo1209.hallymtaxi.model.RoomInfo
import com.dldmswo1209.hallymtaxi.repository.RoomRepository
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage

class FcmService: FirebaseMessagingService() {
    private var broadcaster: LocalBroadcastManager? = null

    companion object{
        const val CHANNEL_ID = "hallym_univ"
        const val CHANNEL_NAME = "hallym_univ"
    }

    override fun onCreate() {
        super.onCreate()
        broadcaster = LocalBroadcastManager.getInstance(this);
    }

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        Log.d("testt", "onNewToken: $token")

    }

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)

        val globalVariable = application as GlobalVariable

        val notificationManager = NotificationManagerCompat.from(applicationContext)

        if(notificationManager.getNotificationChannel(CHANNEL_ID) == null){
            val channel = NotificationChannel(CHANNEL_ID, CHANNEL_NAME, NotificationManager.IMPORTANCE_HIGH)
            notificationManager.createNotificationChannel(channel)
        }
        val builder: NotificationCompat.Builder =
            NotificationCompat.Builder(applicationContext, CHANNEL_ID)

        val roomId = remoteMessage.data["roomId"].toString()
        val userId = remoteMessage.data["userId"].toString()
        val userName = remoteMessage.data["userName"].toString()
        val message = remoteMessage.data["message"].toString()
        val messageType = remoteMessage.data["messageType"].toString()
        val dateTime = remoteMessage.data["dateTime"].toString()
        val chat = Chat(roomId = roomId, userId = userId, msg = message, dateTime = dateTime, messageType = messageType)

        val roomRepo = RoomRepository(this)
        roomRepo.saveChat(chat) // 채팅 저장

        val sender = Person.Builder().setName(userName).build()
        val me = Person.Builder().setName("Me").build()
        builder.setStyle(NotificationCompat.MessagingStyle(me)
            .setConversationTitle("한림카풀")
            .addMessage(message, System.currentTimeMillis(), sender)
        )
            .setSmallIcon(R.mipmap.ic_launcher)
            .setAutoCancel(true)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC).priority = NotificationCompat.PRIORITY_MAX


        val notification = builder.build()
        if(!globalVariable.getIsViewChatRoom()) { // 채팅방을 보고 있지 않은 경우에만 notification 생성
            notificationManager.notify(System.currentTimeMillis().toInt(), notification)
            roomRepo.insertRoomInfo(RoomInfo(roomId, message, dateTime, true))
        }else{
            roomRepo.insertRoomInfo(RoomInfo(roomId, message, dateTime, false))
        }

        val notificationMessage = Intent("newMessage")
        broadcaster?.sendBroadcast(notificationMessage) // 브로드 캐스트 리시버를 통해 노티가 온 경우 알려준다.
    }



}