package com.example.data.auth

import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.provider.Telephony
import android.util.Log
import androidx.core.app.NotificationCompat

/**
 * BroadcastReceiver listening for incoming SMS messages to verify carrier delivery
 * and notify the user with explicit "نکستا (Nexta)" branding.
 */
class SmsReceiver : BroadcastReceiver() {
    private val tag = "NextaSmsReceiver"
    private val channelId = "nexa_sms_verification_channel"

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Telephony.Sms.Intents.SMS_RECEIVED_ACTION) {
            try {
                val messages = Telephony.Sms.Intents.getMessagesFromIntent(intent)
                for (sms in messages) {
                    val body = sms.displayMessageBody ?: continue
                    val sender = sms.displayOriginatingAddress ?: "نکستا"
                    Log.d(tag, "SMS received from $sender: $body")

                    // Extract 6-digit verification code if present
                    val codeRegex = Regex("""\b(\d{6})\b""")
                    val match = codeRegex.find(body)
                    val code = match?.value

                    if (body.contains("نکستا") || body.contains("Nexta") || code != null) {
                        Log.d(tag, "Valid Nexta verification SMS received: code=$code")

                        // Show high-priority notification with clear Nexta sender identification
                        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager
                        val notification = NotificationCompat.Builder(context, channelId)
                            .setSmallIcon(android.R.drawable.stat_notify_chat)
                            .setContentTitle("📩 پیامک دریافتی از طرف: نکستا (Nexta)")
                            .setContentText(if (code != null) "کد تایید ورود به نکستا: $code" else "پیامک جدید از پیام‌رسان نکستا")
                            .setSubText("نکستا • Nexta")
                            .setStyle(
                                NotificationCompat.BigTextStyle()
                                    .setBigContentTitle("📩 پیامک جدید از طرف: نکستا (Nexta)")
                                    .setSummaryText("نکستا (Nexta)")
                                    .bigText("پیامک دریافتی از طرف پیام‌رسان نکستا (Nexta):\n\n${if (code != null) "کد تایید ورود: $code\n\n" else ""}$body")
                            )
                            .setPriority(NotificationCompat.PRIORITY_MAX)
                            .setAutoCancel(true)
                            .build()

                        notificationManager?.notify(7720, notification)
                    }
                }
            } catch (e: Throwable) {
                Log.e(tag, "Error processing incoming SMS: ${e.message}")
            }
        }
    }
}
