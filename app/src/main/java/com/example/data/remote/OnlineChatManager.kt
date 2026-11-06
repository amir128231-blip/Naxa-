package com.example.data.remote

import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.util.concurrent.TimeUnit

enum class OnlineSyncStatus(val labelFa: String) {
    CONNECTING("در حال اتصال..."),
    CONNECTED("آنلاین و متصل 🌐"),
    OFFLINE("آفلاین (شبکه محلی)")
}

class OnlineChatManager(
    private val scope: CoroutineScope
) {
    private val client = OkHttpClient.Builder()
        .connectTimeout(8, TimeUnit.SECONDS)
        .readTimeout(8, TimeUnit.SECONDS)
        .writeTimeout(8, TimeUnit.SECONDS)
        .build()

    // Using a reliable public JSON storage bin for synchronized cloud rooms
    // Prefix room channel to allow users or groups to communicate across devices
    private val baseUrl = "https://kvdb.io"
    // Unique shared public bucket for this Telegram app instance
    private val appBucketKey = "tg_chat_hub_global_2026"

    private val _syncStatus = MutableStateFlow(OnlineSyncStatus.CONNECTING)
    val syncStatus: StateFlow<OnlineSyncStatus> = _syncStatus.asStateFlow()

    private val _onlineUsersCount = MutableStateFlow(1)
    val onlineUsersCount: StateFlow<Int> = _onlineUsersCount.asStateFlow()

    private var pollJob: Job? = null
    private var lastSeenTimestamp: Long = 0L

    // In-memory synced messages cache per channel
    private val channelMessageListeners = mutableMapOf<String, (List<CloudMessageDto>) -> Unit>()

    init {
        startHeartbeat()
    }

    private fun startHeartbeat() {
        scope.launch(Dispatchers.IO) {
            while (isActive) {
                try {
                    // Quick ping to check internet connectivity
                    val request = Request.Builder()
                        .url("https://www.google.com/generate_204")
                        .head()
                        .build()
                    val response = client.newCall(request).execute()
                    if (response.isSuccessful) {
                        _syncStatus.value = OnlineSyncStatus.CONNECTED
                    } else {
                        _syncStatus.value = OnlineSyncStatus.OFFLINE
                    }
                    response.close()
                } catch (e: Exception) {
                    _syncStatus.value = OnlineSyncStatus.OFFLINE
                }
                delay(12000)
            }
        }
    }

    fun startListeningToChannel(channelId: String, onNewMessages: (List<CloudMessageDto>) -> Unit) {
        channelMessageListeners[channelId] = onNewMessages
        if (pollJob == null || pollJob?.isCancelled == true) {
            pollJob = scope.launch(Dispatchers.IO) {
                while (isActive) {
                    try {
                        fetchChannelMessages(channelId)
                    } catch (e: Exception) {
                        Log.d("OnlineChatManager", "Poll error: ${e.message}")
                    }
                    delay(3000) // Poll every 3 seconds for fast real-time synchronization
                }
            }
        }
    }

    fun stopListening(channelId: String) {
        channelMessageListeners.remove(channelId)
        if (channelMessageListeners.isEmpty()) {
            pollJob?.cancel()
            pollJob = null
        }
    }

    suspend fun sendCloudMessage(message: CloudMessageDto): Boolean {
        return kotlinx.coroutines.withContext(Dispatchers.IO) {
            try {
                val safeChannel = message.channel.replace("@", "").replace("#", "").trim().ifEmpty { "general" }
                val url = "$baseUrl/$appBucketKey/ch_$safeChannel"

                // First, retrieve existing list
                val existing = try {
                    val getReq = Request.Builder().url(url).get().build()
                    val resp = client.newCall(getReq).execute()
                    val body = resp.body?.string() ?: "[]"
                    resp.close()
                    parseMessageList(body)
                } catch (e: Exception) {
                    emptyList<CloudMessageDto>()
                }

                val updatedList = (existing + message).takeLast(60) // Keep latest 60 messages
                val jsonPayload = serializeMessageList(updatedList)

                val postReq = Request.Builder()
                    .url(url)
                    .post(jsonPayload.toRequestBody("application/json; charset=utf-8".toMediaType()))
                    .build()

                val postResp = client.newCall(postReq).execute()
                val success = postResp.isSuccessful
                postResp.close()

                if (success) {
                    _syncStatus.value = OnlineSyncStatus.CONNECTED
                    // Immediately notify listener
                    channelMessageListeners[message.channel]?.invoke(listOf(message))
                }
                success
            } catch (e: Exception) {
                Log.e("OnlineChatManager", "Failed to send online message", e)
                false
            }
        }
    }

    private suspend fun fetchChannelMessages(channelId: String) {
        val safeChannel = channelId.replace("@", "").replace("#", "").trim().ifEmpty { "general" }
        val url = "$baseUrl/$appBucketKey/ch_$safeChannel"

        val request = Request.Builder().url(url).get().build()
        val response = client.newCall(request).execute()
        if (response.isSuccessful) {
            val body = response.body?.string() ?: "[]"
            val messages = parseMessageList(body)
            // Filter only new messages
            val newOnes = messages.filter { it.timestamp > lastSeenTimestamp }
            if (newOnes.isNotEmpty()) {
                lastSeenTimestamp = newOnes.maxOf { it.timestamp }
                channelMessageListeners[channelId]?.invoke(newOnes)
            }
        }
        response.close()
    }

    private fun parseMessageList(jsonString: String): List<CloudMessageDto> {
        val list = mutableListOf<CloudMessageDto>()
        try {
            if (jsonString.isBlank() || !jsonString.startsWith("[")) return emptyList()
            val array = JSONArray(jsonString)
            for (i in 0 until array.length()) {
                val obj = array.getJSONObject(i)
                list.add(
                    CloudMessageDto(
                        id = obj.optString("id", ""),
                        channel = obj.optString("channel", "general"),
                        senderName = obj.optString("senderName", "کاربر آنلاین"),
                        senderUsername = obj.optString("senderUsername", "@user"),
                        senderAvatarColor = obj.optLong("senderAvatarColor", 0xFF2AABEE),
                        text = obj.optString("text", ""),
                        timestamp = obj.optLong("timestamp", System.currentTimeMillis()),
                        messageType = obj.optString("messageType", "TEXT"),
                        voiceDurationSeconds = obj.optInt("voiceDurationSeconds", 0),
                        stickerEmoji = if (obj.has("stickerEmoji")) obj.getString("stickerEmoji") else null,
                        replyToSender = if (obj.has("replyToSender")) obj.getString("replyToSender") else null,
                        replyToText = if (obj.has("replyToText")) obj.getString("replyToText") else null,
                        reaction = if (obj.has("reaction")) obj.getString("reaction") else null
                    )
                )
            }
        } catch (e: Exception) {
            Log.e("OnlineChatManager", "Error parsing messages: ${e.message}")
        }
        return list
    }

    private fun serializeMessageList(messages: List<CloudMessageDto>): String {
        val array = JSONArray()
        for (m in messages) {
            val obj = JSONObject()
            obj.put("id", m.id)
            obj.put("channel", m.channel)
            obj.put("senderName", m.senderName)
            obj.put("senderUsername", m.senderUsername)
            obj.put("senderAvatarColor", m.senderAvatarColor)
            obj.put("text", m.text)
            obj.put("timestamp", m.timestamp)
            obj.put("messageType", m.messageType)
            obj.put("voiceDurationSeconds", m.voiceDurationSeconds)
            m.stickerEmoji?.let { obj.put("stickerEmoji", it) }
            m.replyToSender?.let { obj.put("replyToSender", it) }
            m.replyToText?.let { obj.put("replyToText", it) }
            m.reaction?.let { obj.put("reaction", it) }
            array.put(obj)
        }
        return array.toString()
    }
}
