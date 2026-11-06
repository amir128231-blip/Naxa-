package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

enum class MessageType {
    TEXT,
    VOICE,
    IMAGE,
    STICKER
}

enum class MessageStatus {
    SENDING,
    SENT,
    READ
}

@Entity(tableName = "contacts")
data class ChatContact(
    @PrimaryKey
    val id: String,
    val name: String,
    val username: String,
    val phone: String = "",
    val bio: String = "",
    val avatarColorHex: Long = 0xFF2AABEE,
    val isOnline: Boolean = true,
    val lastSeen: String = "آنلاین",
    val isGroup: Boolean = false,
    val isSavedMessages: Boolean = false,
    val unreadCount: Int = 0,
    val lastMessageText: String = "",
    val lastMessageTime: Long = System.currentTimeMillis(),
    val lastMessageIsOutgoing: Boolean = false,
    val lastMessageStatus: String = MessageStatus.READ.name,
    val isPinned: Boolean = false,
    val isMuted: Boolean = false
)

@Entity(tableName = "messages")
data class ChatMessage(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val chatId: String,
    val senderId: String,
    val senderName: String,
    val text: String,
    val timestamp: Long = System.currentTimeMillis(),
    val isOutgoing: Boolean,
    val status: String = MessageStatus.READ.name,
    val messageType: String = MessageType.TEXT.name,
    val voiceDurationSeconds: Int = 0,
    val stickerEmoji: String? = null,
    val replyToId: Long? = null,
    val replyToSender: String? = null,
    val replyToText: String? = null,
    val reaction: String? = null
)
