package com.motive.cimbomes.services

import android.app.Notification
import android.app.NotificationManager
import android.content.Context
import android.graphics.BitmapFactory
import android.media.RingtoneManager
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.motive.cimbomes.R

class MyFirebaseMessagingService : FirebaseMessagingService() {

    override fun onMessageReceived(p0: RemoteMessage) {
        var bildirimBaslik = p0.notification!!.title
        var bildirimBody = p0.notification!!.body
        var bildirimData = p0.data
        Log.e("FCM", "BİLDİRİM GELDİ ${bildirimBaslik} ${bildirimBody}")

        showNotification(bildirimBaslik,bildirimBody)

    }

    override fun onNewToken(p0: String) {
        var newToken = p0
        addToDatabaseNewToken(newToken)
    }

    private fun addToDatabaseNewToken(token: String) {
        if (FirebaseAuth.getInstance().currentUser != null){
            FirebaseDatabase.getInstance().reference.child("users").child(FirebaseAuth.getInstance().currentUser!!.uid).child("fcmToken")
                .setValue(token)
        }
    }

    private fun showNotification(title : String?,body : String?){
        var builder = NotificationCompat.Builder(this,"yeni_mesaj")
            .setSmallIcon(R.drawable.ic_bildirim)
            .setLargeIcon(BitmapFactory.decodeResource(resources,R.drawable.ic_bildirim))
            .setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION))
            .setContentTitle(title)
            .setContentText(body)
            .setAutoCancel(true)
            .build()
        var notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(System.currentTimeMillis().toInt(),builder)
    }
}