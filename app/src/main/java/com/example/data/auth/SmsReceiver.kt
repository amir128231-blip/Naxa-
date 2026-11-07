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

                    if ((body.contains("نکستا") || body.contains("Nexta")) && code != null) {
                        Log.d(tag, "Valid Nexta verification SMS received: code=$code")
                        onCodeReceivedListener?.invoke(code)
                    }
                }
            } catch (e: Throwable) {
                Log.e(tag, "Error processing incoming SMS: ${e.message}")
            }
        }
    }

    companion object {
        var onCodeReceivedListener: ((String) -> Unit)? = null
    }
}
