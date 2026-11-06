package com.example.data.remote

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class CloudMessageDto(
    val id: String = "",
    val channel: String = "general",
    val senderName: String = "",
    val senderUsername: String = "",
    val senderAvatarColor: Long = 0xFF2AABEE,
    val text: String = "",
    val timestamp: Long = 0L,
    val messageType: String = "TEXT",
    val voiceDurationSeconds: Int = 0,
    val stickerEmoji: String? = null,
    val replyToSender: String? = null,
    val replyToText: String? = null,
    val reaction: String? = null
)
