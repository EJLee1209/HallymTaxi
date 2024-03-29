package com.dldmswo1209.hallymtaxi.data.remote

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Intent
import android.provider.Settings.Global
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import androidx.room.Room
import com.dldmswo1209.hallymtaxi.R
import com.dldmswo1209.hallymtaxi.common.MyApplication
import com.dldmswo1209.hallymtaxi.data.local.AppDatabase
import com.dldmswo1209.hallymtaxi.data.model.Chat
import com.dldmswo1209.hallymtaxi.data.model.RoomInfo
import com.dldmswo1209.hallymtaxi.ui.SplashActivity
import com.dldmswo1209.hallymtaxi.util.Keys
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import kotlinx.coroutines.*

class FcmService : FirebaseMessagingService() {
    private var broadcaster: LocalBroadcastManager? = null
    private var roomDB: AppDatabase? = null
    private var myCoroutineScope = CoroutineScope(Dispatchers.IO)
    companion object {
        const val CHANNEL_ID = "com.dldmswo1209.hallymtaxi"
        const val CHANNEL_NAME = "com.dldmswo1209.hallymtaxi"

    }

    override fun onCreate() {
        super.onCreate()
        broadcaster = LocalBroadcastManager.getInstance(this)
        roomDB = Room.databaseBuilder(applicationContext, AppDatabase::class.java, Keys.DATABASE_NAME)
            .fallbackToDestructiveMigration()
            .build()
    }

    override fun onNewToken(token: String) {
        super.onNewToken(token)
    }

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)

        val myApplication = application as MyApplication
        val notificationManager = NotificationManagerCompat.from(applicationContext)

        if (notificationManager.getNotificationChannel(CHANNEL_ID) == null) {
            val channel =
                NotificationChannel(CHANNEL_ID, CHANNEL_NAME, NotificationManager.IMPORTANCE_HIGH)
            notificationManager.createNotificationChannel(channel)
        }

        val roomId = remoteMessage.data["roomId"].toString()
        val userId = remoteMessage.data["userId"].toString()
        val userName = remoteMessage.data["userName"].toString()
        val message = remoteMessage.data["message"].toString()
        val messageType = remoteMessage.data["messageType"].toString()
        val dateTime = remoteMessage.data["dateTime"].toString()
        val chat = Chat(
            roomId = roomId,
            userId = userId,
            userName = userName,
            msg = message,
            dateTime = dateTime,
            messageType = messageType
        )

        myCoroutineScope.launch {
            roomDB?.chatDao()?.saveChat(chat)

            val pendingIntent = createPendingIntent(roomId)

            val builder: NotificationCompat.Builder =
                NotificationCompat.Builder(applicationContext, CHANNEL_ID).apply {
                    setSmallIcon(R.mipmap.ic_main_round)
                    setContentTitle(userName)
                    setContentText(message)
                    setAutoCancel(true)
                    setContentIntent(pendingIntent)
                    setVisibility(NotificationCompat.VISIBILITY_PUBLIC).priority =
                        NotificationCompat.PRIORITY_HIGH
                }

            if (!myApplication.getIsViewChatRoom()) { // 채팅방을 보고 있지 않은 경우에만 notification 생성
                notificationManager.notify(System.currentTimeMillis().toInt(), builder.build())
                roomDB?.roomInfoDao()?.insertRoomInfo(RoomInfo(roomId, message, dateTime, true, isActivate = true))
            } else {
                roomDB?.roomInfoDao()?.insertRoomInfo(RoomInfo(roomId, message, dateTime, false, isActivate = true))
            }

            val notificationMessage = Intent(Keys.INTENT_FILTER_NEW_MESSAGE)
            broadcaster?.sendBroadcast(notificationMessage) // 브로드 캐스트 리시버를 통해 노티가 온 경우 알려준다.
        }

    }

    private fun createPendingIntent(
        stringExtra: String
    ): PendingIntent = PendingIntent.getActivity(
        applicationContext,
        0,
        Intent(applicationContext, SplashActivity::class.java).also {
            it.action = Intent.ACTION_MAIN
            it.addCategory(Intent.CATEGORY_LAUNCHER)
            it.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_SINGLE_TOP
            it.putExtra("roomId", stringExtra)
        },
        PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_ONE_SHOT
    )
}