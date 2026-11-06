package com.example

import android.app.Application
import android.util.Log
import com.google.firebase.FirebaseApp

class TelegramApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        try {
            // Initialize default FirebaseApp if google-services is packaged, otherwise handle gracefully
            val apps = FirebaseApp.getApps(this)
            if (apps.isEmpty()) {
                Log.d("TelegramApplication", "Running in offline/local security mode without remote google-services.json")
            } else {
                Log.d("TelegramApplication", "FirebaseApp initialized from configuration: ${apps.first().name}")
            }
        } catch (e: Throwable) {
            Log.e("TelegramApplication", "FirebaseApp initialization handled: ${e.message}")
        }
    }
}
