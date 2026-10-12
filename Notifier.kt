package com.demopay.app

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat

/** Local notifications only (no push server, no internet). */
object Notifier {
    private const val CHANNEL = "demo_payments"

    private fun ensureChannel(ctx: Context) {
        if (Build.VERSION.SDK_INT >= 26) {
            val ch = NotificationChannel(CHANNEL, "Demo payments", NotificationManager.IMPORTANCE_DEFAULT)
            ch.description = "Demo payment confirmations (not real payments)"
            val nm = ctx.getSystemService(NotificationManager::class.java)
            nm.createNotificationChannel(ch)
        }
    }

    fun paymentComplete(ctx: Context, txn: Txn) {
        if (Build.VERSION.SDK_INT >= 33 &&
            ContextCompat.checkSelfPermission(ctx, Manifest.permission.POST_NOTIFICATIONS) !=
            PackageManager.PERMISSION_GRANTED
        ) return
        ensureChannel(ctx)
        val text = formatMoney(txn.amountPaise) + " sent to " + txn.payeeName + ". DEMO - not a real payment."
        val n = NotificationCompat.Builder(ctx, CHANNEL)
            .setSmallIcon(R.drawable.ic_notify)
            .setContentTitle("Payment Complete (DEMO)")
            .setContentText(text)
            .setStyle(NotificationCompat.BigTextStyle().bigText(text + "\nRef: " + txn.ref))
            .setColor(0xFF1B8A3C.toInt())
            .setAutoCancel(true)
            .build()
        try {
            NotificationManagerCompat.from(ctx).notify(txn.id.hashCode(), n)
        } catch (e: SecurityException) {
            // permission revoked meanwhile
        }
    }
}
