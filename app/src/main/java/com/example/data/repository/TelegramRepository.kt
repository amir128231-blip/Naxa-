package com.example.data.repository

import com.example.data.bot.TelegramSupportBotEngine
import com.example.data.local.ContactDao
import com.example.data.local.MessageDao
import com.example.data.model.ChatContact
import com.example.data.model.ChatMessage
import com.example.data.model.MessageStatus
import com.example.data.model.MessageType
import com.example.data.remote.CloudMessageDto
import com.example.data.remote.OnlineChatManager
import com.example.data.remote.OnlineSyncStatus
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.random.Random

class TelegramRepository(
    private val contactDao: ContactDao,
    private val messageDao: MessageDao,
    private val scope: CoroutineScope
) {
    val contacts: Flow<List<ChatContact>> = contactDao.getAllContacts()

    val onlineChatManager = OnlineChatManager(scope)
    val syncStatus: StateFlow<OnlineSyncStatus> = onlineChatManager.syncStatus

    fun getMessagesForChat(chatId: String): Flow<List<ChatMessage>> {
        return messageDao.getMessagesForChat(chatId)
    }

    fun observeContact(chatId: String): Flow<ChatContact?> {
        return contactDao.observeContactById(chatId)
    }

    suspend fun getContact(chatId: String): ChatContact? {
        return contactDao.getContactById(chatId)
    }

    suspend fun initializeDefaultData() {
        val now = System.currentTimeMillis()

        // Clean up all sample/demo contacts so ONLY Nexa Saved Messages remains
        val demoContactIds = listOf(
            "contact_sara",
            "contact_ali",
            "contact_maryam",
            "group_dorehami",
            "bot_support"
        )
        for (demoId in demoContactIds) {
            contactDao.deleteContact(demoId)
            messageDao.clearChatHistory(demoId)
        }

        // Initialize or update Nexa Cloud Storage (Saved Messages) as the exclusive default chat
        val savedContact = ChatContact(
            id = "saved_messages",
            name = "پیام‌های ذخیره شده نکسا ☁️",
            username = "@saved_messages",
            bio = "فضای ابری شخصی نکسا برای نگهداری یادداشت‌ها، ویس و فایل‌ها",
            avatarColorHex = 0xFF2481CC,
            isOnline = true,
            lastSeen = "فضای ابری متصل",
            isGroup = false,
            isSavedMessages = true,
            unreadCount = 0,
            lastMessageText = "به فضای ذخیره‌سازی نکسا خوش آمدید ☁️",
            lastMessageTime = now,
            lastMessageIsOutgoing = true,
            lastMessageStatus = MessageStatus.READ.name,
            isPinned = true
        )
        contactDao.insertOrUpdateContact(savedContact)

        val existingMessages = messageDao.getLatestMessage("saved_messages")
        if (existingMessages == null) {
            val defaultMessages = listOf(
                ChatMessage(
                    chatId = "saved_messages",
                    senderId = "saved_messages",
                    senderName = "Nexa Cloud",
                    text = "به فضای ابری و پیام‌های ذخیره‌شده نکسا خوش آمدید! ☁️✨\n\nدقیقاً مانند فضای ابری تلگرام، می‌توانید هر متن، ویس صوتی، عکس، استیکر و یادداشتی را اینجا بفرستید تا برای همیشه در فضای شخصی شما محفوظ و در دسترس بماند.",
                    timestamp = now,
                    isOutgoing = true,
                    status = MessageStatus.READ.name,
                    messageType = MessageType.TEXT.name
                )
            )
            messageDao.insertMessages(defaultMessages)
        }
    }

    suspend fun sendMessage(
        chatId: String,
        text: String,
        replyToId: Long? = null,
        replyToSender: String? = null,
        replyToText: String? = null,
        senderMyName: String = "من"
    ): Long {
        val now = System.currentTimeMillis()
        val initialStatus = if (chatId == "saved_messages") MessageStatus.READ.name else MessageStatus.SENT.name
        val message = ChatMessage(
            chatId = chatId,
            senderId = "me",
            senderName = senderMyName,
            text = text,
            timestamp = now,
            isOutgoing = true,
            status = initialStatus,
            messageType = MessageType.TEXT.name,
            replyToId = replyToId,
            replyToSender = replyToSender,
            replyToText = replyToText
        )
        val id = messageDao.insertMessage(message)
        contactDao.updateLastMessageInfo(chatId, text, now, isOutgoing = true, status = initialStatus)

        // For non-saved messages, simulate realistic read receipt (single check ✓ to double check ✓✓)
        if (chatId != "saved_messages") {
            scope.launch(Dispatchers.IO) {
                delay(1400)
                messageDao.updateMessageStatus(id, MessageStatus.READ.name)
                contactDao.updateLastMessageStatus(chatId, MessageStatus.READ.name)
            }
        }

        // Broadcast to cloud if it's an online room or channel
        if (chatId.startsWith("online_") || chatId.startsWith("room_")) {
            scope.launch(Dispatchers.IO) {
                val cloudMsg = CloudMessageDto(
                    id = "msg_${System.currentTimeMillis()}_${Random.nextInt(100, 999)}",
                    channel = chatId,
                    senderName = senderMyName,
                    senderUsername = "@amir_dev",
                    text = text,
                    timestamp = now,
                    messageType = "TEXT",
                    replyToSender = replyToSender,
                    replyToText = replyToText
                )
                onlineChatManager.sendCloudMessage(cloudMsg)
            }
        } else if (chatId != "saved_messages") {
            // Trigger realistic friend reply for local simulated contacts
            scheduleSimulatedFriendReply(chatId, text)
        }

        return id
    }

    suspend fun sendVoiceMessage(chatId: String, durationSeconds: Int, senderMyName: String = "من") {
        val now = System.currentTimeMillis()
        val initialStatus = if (chatId == "saved_messages") MessageStatus.READ.name else MessageStatus.SENT.name
        val message = ChatMessage(
            chatId = chatId,
            senderId = "me",
            senderName = senderMyName,
            text = "پیام صوتی ($durationSeconds ثانیه)",
            timestamp = now,
            isOutgoing = true,
            status = initialStatus,
            messageType = MessageType.VOICE.name,
            voiceDurationSeconds = durationSeconds
        )
        val id = messageDao.insertMessage(message)
        contactDao.updateLastMessageInfo(chatId, "🎤 پیام صوتی ($durationSeconds ثانیه)", now, isOutgoing = true, status = initialStatus)

        if (chatId != "saved_messages") {
            scope.launch(Dispatchers.IO) {
                delay(1400)
                messageDao.updateMessageStatus(id, MessageStatus.READ.name)
                contactDao.updateLastMessageStatus(chatId, MessageStatus.READ.name)
            }
        }

        if (chatId.startsWith("online_") || chatId.startsWith("room_")) {
            scope.launch(Dispatchers.IO) {
                val cloudMsg = CloudMessageDto(
                    id = "msg_${System.currentTimeMillis()}_${Random.nextInt(100, 999)}",
                    channel = chatId,
                    senderName = senderMyName,
                    senderUsername = "@amir_dev",
                    text = "پیام صوتی ($durationSeconds ثانیه)",
                    timestamp = now,
                    messageType = "VOICE",
                    voiceDurationSeconds = durationSeconds
                )
                onlineChatManager.sendCloudMessage(cloudMsg)
            }
        } else if (chatId != "saved_messages") {
            scheduleSimulatedFriendReply(chatId, "ویس")
        }
    }

    suspend fun sendSticker(chatId: String, emoji: String, senderMyName: String = "من") {
        val now = System.currentTimeMillis()
        val initialStatus = if (chatId == "saved_messages") MessageStatus.READ.name else MessageStatus.SENT.name
        val message = ChatMessage(
            chatId = chatId,
            senderId = "me",
            senderName = senderMyName,
            text = emoji,
            timestamp = now,
            isOutgoing = true,
            status = initialStatus,
            messageType = MessageType.STICKER.name,
            stickerEmoji = emoji
        )
        val id = messageDao.insertMessage(message)
        contactDao.updateLastMessageInfo(chatId, "$emoji استیکر", now, isOutgoing = true, status = initialStatus)

        if (chatId != "saved_messages") {
            scope.launch(Dispatchers.IO) {
                delay(1400)
                messageDao.updateMessageStatus(id, MessageStatus.READ.name)
                contactDao.updateLastMessageStatus(chatId, MessageStatus.READ.name)
            }
        }

        if (chatId.startsWith("online_") || chatId.startsWith("room_")) {
            scope.launch(Dispatchers.IO) {
                val cloudMsg = CloudMessageDto(
                    id = "msg_${System.currentTimeMillis()}_${Random.nextInt(100, 999)}",
                    channel = chatId,
                    senderName = senderMyName,
                    senderUsername = "@amir_dev",
                    text = emoji,
                    timestamp = now,
                    messageType = "STICKER",
                    stickerEmoji = emoji
                )
                onlineChatManager.sendCloudMessage(cloudMsg)
            }
        } else if (chatId != "saved_messages") {
            scheduleSimulatedFriendReply(chatId, emoji)
        }
    }

    fun scheduleSimulatedFriendReply(chatId: String, userText: String) {
        scope.launch(Dispatchers.IO) {
            val contact = contactDao.getContactById(chatId) ?: return@launch

            // Mark user's previous outgoing messages as read when friend starts responding
            messageDao.markOutgoingMessagesAsRead(chatId)
            contactDao.updateLastMessageStatus(chatId, MessageStatus.READ.name)

            // If it's the Support Bot, respond with TelegramSupportBotEngine
            if (chatId == "bot_support") {
                delay(700 + Random.nextLong(500))
                val now = System.currentTimeMillis()
                val botReplyText = TelegramSupportBotEngine.generateReply(userText)

                val botMsg = ChatMessage(
                    chatId = chatId,
                    senderId = "bot_support",
                    senderName = "پشتیبانی نکسا",
                    text = botReplyText,
                    timestamp = now,
                    isOutgoing = false,
                    status = MessageStatus.SENT.name,
                    messageType = MessageType.TEXT.name
                )
                messageDao.insertMessage(botMsg)
                contactDao.updateLastMessage(chatId, botReplyText.lines().firstOrNull() ?: botReplyText, now)
                return@launch
            }

            delay(1500 + Random.nextLong(1000))
            val now = System.currentTimeMillis()

            val replyText: String
            val replyType: MessageType
            var voiceDuration = 0
            var stickerEmoji: String? = null

            val lower = userText.lowercase()

            when {
                lower.contains("سلام") || lower.contains("درود") || lower.contains("چطوری") || lower.contains("خوبی") -> {
                    replyType = MessageType.TEXT
                    val options = listOf(
                        "سلام عزیزم! قربونت، تو چطوری؟ اوضاع چطوره؟ ✨",
                        "سلام رفیق جان! من عالی‌ام، خوشحالم پیام دادی 🌸",
                        "درود بر تو! خوبی؟ چه خبرها؟",
                        "سلام! چقدر خوب شد نوشتی، اتفاقاً به فکرت بودم 😊"
                    )
                    replyText = options.random()
                }
                lower.contains("کافه") || lower.contains("قرار") || lower.contains("ساعت") || lower.contains("کجا") -> {
                    replyType = MessageType.TEXT
                    val options = listOf(
                        "عالیه! من ساعت ۶ عصر کاملاً آزادم. بریم همون کافه همیشگی؟ ☕",
                        "من پایه‌ام! لوکیشن رو برام بفرست سر ساعت اونجام 📍",
                        "فوق‌العاده‌ست، به بچه‌ها هم بگم بیان یا دو نفری بریم؟"
                    )
                    replyText = options.random()
                }
                lower.contains("ویس") || userText.startsWith("🎤") -> {
                    val isVoice = Random.nextBoolean()
                    if (isVoice) {
                        replyType = MessageType.VOICE
                        voiceDuration = Random.nextInt(7, 24)
                        replyText = "پیام صوتی ($voiceDuration ثانیه)"
                    } else {
                        replyType = MessageType.TEXT
                        replyText = "وویست رو گوش دادم، دمت گرم دقیقاً موافقم! 👏"
                    }
                }
                userText.length <= 4 && (userText.contains("❤️") || userText.contains("😂") || userText.contains("👍") || userText.contains("🔥")) -> {
                    replyType = MessageType.STICKER
                    val stickers = listOf("🚀", "🌟", "🎉", "🔥", "❤️", "😍", "✌️", "😎")
                    stickerEmoji = stickers.random()
                    replyText = stickerEmoji
                }
                lower.contains("پروژه") || lower.contains("کار") || lower.contains("کد") || lower.contains("برنامه") -> {
                    replyType = MessageType.TEXT
                    val options = listOf(
                        "پروژه خیلی خوب پیش رفته، تقریباً تمومه! تو به کجا رسیدی؟ 💻",
                        "اتفاقاً تستش کردم، سرعتش فوق‌العاده بالاست 👌",
                        "منم کارهای مربوط به خودم رو تموم کردم، آماده ارائه‌ام."
                    )
                    replyText = options.random()
                }
                else -> {
                    replyType = MessageType.TEXT
                    val options = listOf(
                        "ایول! کاملاً درسته، همین برنامه رو بریم جلو 👍",
                        "جالبه! فردا دقیق‌تر در موردش صحبت کنیم 🤝",
                        "خیلی عالیه، خوشم اومد! 🌟",
                        "مرسی رفیق، حتماً خبرش رو بهت می‌دم.",
                        "موافقم، هماهنگش کنیم به زودی!"
                    )
                    replyText = options.random()
                }
            }

            val senderName = if (contact.isGroup) {
                listOf("سارا رضایی", "علی محمدی", "رضا کریمی").random()
            } else {
                contact.name
            }

            val friendMsg = ChatMessage(
                chatId = chatId,
                senderId = if (contact.isGroup) "member_${Random.nextInt(100)}" else chatId,
                senderName = senderName,
                text = replyText,
                timestamp = now,
                isOutgoing = false,
                status = MessageStatus.SENT.name,
                messageType = replyType.name,
                voiceDurationSeconds = voiceDuration,
                stickerEmoji = stickerEmoji
            )

            messageDao.insertMessage(friendMsg)
            contactDao.updateLastMessage(chatId, if (contact.isGroup) "$senderName: $replyText" else replyText, now)
        }
    }

    suspend fun getMessageById(messageId: Long): ChatMessage? {
        return messageDao.getMessageById(messageId)
    }

    suspend fun updateReaction(messageId: Long, reaction: String?) {
        messageDao.updateReaction(messageId, reaction)
    }

    suspend fun updateReactionByTimestamp(chatId: String, timestamp: Long, reaction: String?) {
        messageDao.updateReactionByTimestamp(chatId, timestamp, reaction)
    }

    suspend fun markChatAsRead(chatId: String) {
        contactDao.markChatAsRead(chatId)
        messageDao.markIncomingMessagesAsRead(chatId)
    }

    suspend fun togglePin(chatId: String) {
        contactDao.togglePin(chatId)
    }

    suspend fun toggleMute(chatId: String) {
        contactDao.toggleMute(chatId)
    }

    suspend fun deleteContact(chatId: String) {
        contactDao.deleteContact(chatId)
        messageDao.clearChatHistory(chatId)
    }

    suspend fun clearChatHistory(chatId: String) {
        messageDao.clearChatHistory(chatId)
        contactDao.updateLastMessage(chatId, "", System.currentTimeMillis())
    }

    suspend fun deleteMessage(messageId: Long) {
        messageDao.deleteMessage(messageId)
    }

    suspend fun addNewContact(
        name: String,
        username: String,
        phone: String,
        bio: String = "",
        avatarColor: Long = 0xFF2AABEE
    ): String {
        val cleanUsername = if (username.startsWith("@")) username else "@$username"
        val id = "contact_${System.currentTimeMillis()}"
        val newContact = ChatContact(
            id = id,
            name = name,
            username = cleanUsername,
            phone = phone,
            bio = if (bio.isBlank()) "کاربر نکسا" else bio,
            avatarColorHex = avatarColor,
            isOnline = true,
            lastSeen = "آنلاین",
            isGroup = false,
            lastMessageText = "گفتگو آغاز شد",
            lastMessageTime = System.currentTimeMillis()
        )
        contactDao.insertOrUpdateContact(newContact)

        // Welcome message
        val welcomeMsg = ChatMessage(
            chatId = id,
            senderId = id,
            senderName = name,
            text = "سلام! مخاطب جدید اضافه شد 👋 آماده گفتگو هستیم.",
            timestamp = System.currentTimeMillis(),
            isOutgoing = false,
            status = MessageStatus.SENT.name
        )
        messageDao.insertMessage(welcomeMsg)
        return id
    }

    suspend fun syncDeviceContacts(deviceContacts: List<com.example.data.util.DeviceContactInfo>): Int {
        var addedCount = 0
        val existingList = contactDao.getAllContacts().first()
        val existingPhones = existingList.map { it.phone.replace("[^0-9+]".toRegex(), "") }.filter { it.isNotBlank() }.toSet()
        val colors = listOf(
            0xFF2AABEE, 0xFFE91E63, 0xFF9C27B0, 0xFF4CAF50, 0xFFFF9800, 0xFF009688, 0xFF673AB7, 0xFF00BCD4
        )

        for (item in deviceContacts) {
            val normPhone = item.phoneNumber.replace("[^0-9+]".toRegex(), "")
            if (normPhone.isNotBlank() && existingPhones.contains(normPhone)) {
                continue // already imported
            }

            val contactId = "contact_device_${item.id.replace("[^a-zA-Z0-9_]".toRegex(), "")}_${System.currentTimeMillis() % 10000}"
            val color = colors.random()
            val cleanUser = "@" + item.displayName.lowercase().replace(" ", "_").replace("[^a-z0-9_]".toRegex(), "").ifEmpty { "user_${Random.nextInt(100, 999)}" }

            val newContact = ChatContact(
                id = contactId,
                name = item.displayName,
                username = cleanUser,
                phone = item.phoneNumber,
                bio = "مخاطب ذخیره شده در گوشی 📱",
                avatarColorHex = color,
                isOnline = false,
                lastSeen = "مخاطب دفترچه تلفن",
                isGroup = false,
                lastMessageText = "",
                lastMessageTime = 0L,
                unreadCount = 0
            )
            contactDao.insertOrUpdateContact(newContact)
            addedCount++
        }
        return addedCount
    }

    suspend fun createNewChannel(name: String, description: String): String {
        val id = "channel_${System.currentTimeMillis()}"
        val now = System.currentTimeMillis()
        val newChannel = ChatContact(
            id = id,
            name = name,
            username = "@channel_${Random.nextInt(1000, 9999)}",
            bio = description.ifBlank { "کانال عمومی نکسا 📢" },
            avatarColorHex = 0xFF00B0FF,
            isOnline = true,
            lastSeen = "کانال عمومی",
            isGroup = true,
            lastMessageText = "کانال ایجاد شد 📢",
            lastMessageTime = now
        )
        contactDao.insertOrUpdateContact(newChannel)

        val createMsg = ChatMessage(
            chatId = id,
            senderId = "me",
            senderName = "شما",
            text = "کانال «$name» ایجاد شد. 📢",
            timestamp = now,
            isOutgoing = true,
            status = MessageStatus.READ.name
        )
        messageDao.insertMessage(createMsg)
        return id
    }

    suspend fun createNewGroup(name: String, memberNames: List<String>): String {
        val id = "group_${System.currentTimeMillis()}"
        val now = System.currentTimeMillis()
        val newGroup = ChatContact(
            id = id,
            name = name,
            username = "@group_${Random.nextInt(1000, 9999)}",
            bio = "گروه ایجاد شده با ${memberNames.size + 1} عضو",
            avatarColorHex = 0xFFFF5722,
            isOnline = true,
            lastSeen = "${memberNames.size + 1} عضو",
            isGroup = true,
            lastMessageText = "گروه ایجاد شد 🎉",
            lastMessageTime = now
        )
        contactDao.insertOrUpdateContact(newGroup)

        val createMsg = ChatMessage(
            chatId = id,
            senderId = "me",
            senderName = "شما",
            text = "گروه «$name» ایجاد شد. خوش آمدید! 🌟",
            timestamp = now,
            isOutgoing = true,
            status = MessageStatus.READ.name
        )
        messageDao.insertMessage(createMsg)
        return id
    }

    suspend fun joinOrCreateOnlineRoom(roomCode: String, roomTitle: String): String {
        val cleanCode = roomCode.trim().lowercase().replace("@", "").replace("#", "").ifEmpty { "general" }
        val id = "online_$cleanCode"
        val existing = contactDao.getContactById(id)
        if (existing != null) {
            return id
        }
        val now = System.currentTimeMillis()
        val onlineRoom = ChatContact(
            id = id,
            name = roomTitle.ifEmpty { "اتاق آنلاین #$cleanCode" },
            username = "@room_$cleanCode",
            bio = "چت‌روم زنده اینترنتی جهانی - کد: $cleanCode",
            avatarColorHex = 0xFF00B0FF,
            isOnline = true,
            lastSeen = "متصل به سرور ابری",
            isGroup = true,
            lastMessageText = "به اتاق آنلاین پیوستید 🌐",
            lastMessageTime = now,
            isPinned = false
        )
        contactDao.insertOrUpdateContact(onlineRoom)

        val joinMsg = ChatMessage(
            chatId = id,
            senderId = "system",
            senderName = "سیستم ابری",
            text = "شما به اتاق گفتگوی آنلاین «$cleanCode» متصل شدید! هر کس دیگری که این کد را بزند می‌تواند با شما به صورت زنده چت کند 🌐",
            timestamp = now,
            isOutgoing = false,
            status = MessageStatus.READ.name
        )
        messageDao.insertMessage(joinMsg)
        return id
    }

    suspend fun ensureSupportBotContact(): String {
        val id = "bot_support"
        val existing = contactDao.getContactById(id)
        val now = System.currentTimeMillis()
        val botContact = ChatContact(
            id = id,
            name = "پشتیبانی نکسا (Nexa Support) ✨",
            username = "@nexa_support",
            phone = "",
            bio = "مرکز رسمی پشتیبانی و راهنمای پیام‌رسان نکسا",
            avatarColorHex = 0xFF5B4DDF,
            isOnline = true,
            lastSeen = "همیشه پاسخگو",
            isGroup = false,
            isSavedMessages = false,
            unreadCount = existing?.unreadCount ?: 0,
            lastMessageText = existing?.lastMessageText ?: "سلام! پشتیبانی نکسا در خدمت شماست ✨",
            lastMessageTime = existing?.lastMessageTime ?: now,
            isPinned = true
        )
        contactDao.insertOrUpdateContact(botContact)

        if (existing == null) {
            val welcomeMsg = ChatMessage(
                chatId = id,
                senderId = "bot_support",
                senderName = "پشتیبانی نکسا",
                text = "سلام به پیام‌رسان نکسا خوش آمدید! ✨\n\nتیم پشتیبانی نکسا آماده پاسخگویی به سوالات شما درباره امکانات برنامه، چت آنلاین، تماس، استیکر و تنظیمات حساب کاربری است. برای مشاهده راهنما می‌توانید عبارت /help را ارسال نمایید.",
                timestamp = now,
                isOutgoing = false,
                status = MessageStatus.SENT.name,
                messageType = MessageType.TEXT.name
            )
            messageDao.insertMessage(welcomeMsg)
        }
        return id
    }

    fun startListeningToRoom(chatId: String) {
        if (!chatId.startsWith("online_") && !chatId.startsWith("room_")) return

        onlineChatManager.startListeningToChannel(chatId) { incomingList ->
            scope.launch(Dispatchers.IO) {
                for (cloudDto in incomingList) {
                    // Check if not sent by me
                    if (cloudDto.senderUsername != "@amir_dev") {
                        val newMsg = ChatMessage(
                            chatId = chatId,
                            senderId = cloudDto.senderUsername.ifEmpty { "user_${cloudDto.senderName}" },
                            senderName = cloudDto.senderName,
                            text = cloudDto.text,
                            timestamp = cloudDto.timestamp,
                            isOutgoing = false,
                            status = MessageStatus.READ.name,
                            messageType = cloudDto.messageType,
                            voiceDurationSeconds = cloudDto.voiceDurationSeconds,
                            stickerEmoji = cloudDto.stickerEmoji,
                            replyToSender = cloudDto.replyToSender,
                            replyToText = cloudDto.replyToText,
                            reaction = cloudDto.reaction
                        )
                        messageDao.insertMessage(newMsg)
                        contactDao.updateLastMessage(chatId, "${cloudDto.senderName}: ${cloudDto.text}", cloudDto.timestamp)
                    }
                }
            }
        }
    }

    fun stopListeningToRoom(chatId: String) {
        onlineChatManager.stopListening(chatId)
    }

    suspend fun toggleContactOnlineStatus(contactId: String) {
        val contact = contactDao.getContactById(contactId) ?: return
        val newOnline = !contact.isOnline
        val newLastSeen = if (newOnline) "آنلاین" else "آخرین بازدید لحظاتی پیش"
        contactDao.updateOnlineStatus(contactId, newOnline, newLastSeen)
    }

    suspend fun updateContactOnlineStatus(contactId: String, isOnline: Boolean, lastSeen: String) {
        contactDao.updateOnlineStatus(contactId, isOnline, lastSeen)
    }

    companion object {
        fun formatTimestamp(timestamp: Long): String {
            val now = System.currentTimeMillis()
            val diff = now - timestamp
            val sdfTime = SimpleDateFormat("HH:mm", Locale.getDefault())
            val date = Date(timestamp)

            return when {
                diff < 60000 -> "هم‌اکنون"
                diff < 86400000 -> sdfTime.format(date)
                diff < 86400000 * 2 -> "دیروز"
                else -> SimpleDateFormat("MMM dd", Locale.getDefault()).format(date)
            }
        }
    }
}
