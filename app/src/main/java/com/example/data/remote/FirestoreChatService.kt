package com.example.data.remote

import android.util.Log
import com.google.firebase.FirebaseApp
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

data class FirestoreConversation(
    val id: String = "",
    val title: String = "",
    val lastMessage: String = "",
    val lastSender: String = "",
    val timestamp: Long = System.currentTimeMillis(),
    val unreadCount: Int = 0,
    val avatarColorHex: Long = 0xFF2AABEE,
    val isGroup: Boolean = false,
    val participants: List<String> = emptyList()
)

class FirestoreChatService(
    private val scope: CoroutineScope
) {
    private val tag = "FirestoreChatService"

    val firestore: FirebaseFirestore? by lazy {
        try {
            if (FirebaseApp.getApps(com.google.firebase.FirebaseApp.getInstance().applicationContext).isNotEmpty()) {
                FirebaseFirestore.getInstance()
            } else {
                null
            }
        } catch (e: Throwable) {
            Log.e(tag, "Firestore initialization error: ${e.message}")
            null
        }
    }

    private val _conversations = MutableStateFlow<List<FirestoreConversation>>(getDefaultSampleConversations())
    val conversations: StateFlow<List<FirestoreConversation>> = _conversations.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    init {
        listenToConversations()
    }

    fun listenToConversations() {
        val db = firestore
        if (db == null) {
            Log.d(tag, "Firestore not available, using default conversations")
            return
        }

        scope.launch(Dispatchers.IO) {
            _isLoading.value = true
            try {
                db.collection("conversations")
                    .orderBy("timestamp", Query.Direction.DESCENDING)
                    .limit(50)
                    .addSnapshotListener { snapshot, error ->
                        _isLoading.value = false
                        if (error != null) {
                            Log.e(tag, "Firestore listen error: ${error.message}")
                            _errorMessage.value = error.message
                            return@addSnapshotListener
                        }

                        if (snapshot != null && !snapshot.isEmpty) {
                            val items = snapshot.documents.mapNotNull { doc ->
                                try {
                                    FirestoreConversation(
                                        id = doc.id,
                                        title = doc.getString("title") ?: "چت ابری",
                                        lastMessage = doc.getString("lastMessage") ?: "",
                                        lastSender = doc.getString("lastSender") ?: "",
                                        timestamp = doc.getLong("timestamp") ?: System.currentTimeMillis(),
                                        unreadCount = (doc.getLong("unreadCount") ?: 0L).toInt(),
                                        avatarColorHex = doc.getLong("avatarColorHex") ?: 0xFF2AABEE,
                                        isGroup = doc.getBoolean("isGroup") ?: false,
                                        participants = (doc.get("participants") as? List<*>)?.filterIsInstance<String>() ?: emptyList()
                                    )
                                } catch (e: Exception) {
                                    null
                                }
                            }
                            if (items.isNotEmpty()) {
                                _conversations.value = items
                                _errorMessage.value = null
                            }
                        } else {
                            // If remote collection is currently empty, seed with initial conversations
                            seedInitialConversationsIfEmpty()
                        }
                    }
            } catch (e: Exception) {
                _isLoading.value = false
                Log.e(tag, "Failed to setup firestore listener: ${e.message}")
            }
        }
    }

    suspend fun refreshConversations() {
        val db = firestore ?: return
        _isLoading.value = true
        _errorMessage.value = null
        try {
            val snapshot = db.collection("conversations")
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .limit(50)
                .get()
                .await()

            val items = snapshot.documents.mapNotNull { doc ->
                try {
                    FirestoreConversation(
                        id = doc.id,
                        title = doc.getString("title") ?: "چت ابری",
                        lastMessage = doc.getString("lastMessage") ?: "",
                        lastSender = doc.getString("lastSender") ?: "",
                        timestamp = doc.getLong("timestamp") ?: System.currentTimeMillis(),
                        unreadCount = (doc.getLong("unreadCount") ?: 0L).toInt(),
                        avatarColorHex = doc.getLong("avatarColorHex") ?: 0xFF2AABEE,
                        isGroup = doc.getBoolean("isGroup") ?: false,
                        participants = (doc.get("participants") as? List<*>)?.filterIsInstance<String>() ?: emptyList()
                    )
                } catch (e: Exception) {
                    null
                }
            }
            if (items.isNotEmpty()) {
                _conversations.value = items
            }
        } catch (e: Exception) {
            _errorMessage.value = "خطا در دریافت اطلاعات ابری: ${e.message}"
            Log.e(tag, "Error fetching firestore conversations", e)
        } finally {
            _isLoading.value = false
        }
    }

    suspend fun saveConversation(conv: FirestoreConversation): Boolean {
        val db = firestore
        // Always update in-memory state
        val currentList = _conversations.value.toMutableList()
        val existingIndex = currentList.indexOfFirst { it.id == conv.id }
        if (existingIndex >= 0) {
            currentList[existingIndex] = conv
        } else {
            currentList.add(0, conv)
        }
        _conversations.value = currentList

        if (db == null) return true

        return try {
            val data = hashMapOf(
                "title" to conv.title,
                "lastMessage" to conv.lastMessage,
                "lastSender" to conv.lastSender,
                "timestamp" to conv.timestamp,
                "unreadCount" to conv.unreadCount,
                "avatarColorHex" to conv.avatarColorHex,
                "isGroup" to conv.isGroup,
                "participants" to conv.participants
            )
            db.collection("conversations").document(conv.id).set(data, SetOptions.merge()).await()
            true
        } catch (e: Exception) {
            Log.e(tag, "Failed to save conversation to firestore: ${e.message}")
            false
        }
    }

    suspend fun createNewConversation(title: String, initialMessage: String, senderName: String, isGroup: Boolean): String {
        val id = "firestore_chat_" + System.currentTimeMillis()
        val newConv = FirestoreConversation(
            id = id,
            title = title,
            lastMessage = initialMessage,
            lastSender = senderName,
            timestamp = System.currentTimeMillis(),
            unreadCount = 0,
            avatarColorHex = listOf(0xFF2AABEE, 0xFFE91E63, 0xFF9C27B0, 0xFF4CAF50, 0xFFFF9800).random(),
            isGroup = isGroup,
            participants = listOf(senderName)
        )
        saveConversation(newConv)
        return id
    }

    private val inMemoryReactions = mutableMapOf<String, String?>()
    private val localReactionListeners = mutableListOf<(chatId: String, timestamp: Long, reaction: String?) -> Unit>()

    private fun notifyReactionListeners(chatId: String, timestamp: Long, reaction: String?) {
        localReactionListeners.forEach { listener ->
            try {
                listener(chatId, timestamp, reaction)
            } catch (e: Exception) {
                Log.e(tag, "Local reaction listener error", e)
            }
        }
    }

    suspend fun syncMessageReaction(
        chatId: String,
        messageTimestamp: Long,
        text: String,
        senderName: String,
        reaction: String?
    ): Boolean {
        val db = firestore
        val docId = "${chatId}_${messageTimestamp}"
        val reactionValue = reaction ?: ""
        val hasReaction = !reaction.isNullOrBlank()

        // Keep local memory store synchronized
        inMemoryReactions[docId] = reaction
        notifyReactionListeners(chatId, messageTimestamp, reaction)

        val data = hashMapOf(
            "chatId" to chatId,
            "messageTimestamp" to messageTimestamp,
            "text" to text,
            "senderName" to senderName,
            "reaction" to reactionValue,
            "reactionState" to reactionValue,
            "hasReaction" to hasReaction,
            "updatedAt" to System.currentTimeMillis()
        )

        if (db == null) {
            Log.d(tag, "Firestore not initialized, reaction cached in memory for $docId")
            return true
        }

        return try {
            // 1. Store in conversations -> messages subcollection (the message document)
            db.collection("conversations")
                .document(chatId)
                .collection("messages")
                .document(docId)
                .set(data, SetOptions.merge())
                .await()

            // 2. Store in root messages collection document
            db.collection("messages")
                .document(docId)
                .set(data, SetOptions.merge())
                .await()

            // 3. Store in dedicated message_reactions collection for real-time listeners
            db.collection("message_reactions")
                .document(docId)
                .set(data, SetOptions.merge())
                .await()

            Log.d(tag, "Successfully synced reaction $reaction to Firestore message document for $docId")
            true
        } catch (e: Exception) {
            Log.e(tag, "Failed to sync reaction to firestore message document: ${e.message}")
            false
        }
    }

    suspend fun saveMessageToFirestore(
        chatId: String,
        messageTimestamp: Long,
        text: String,
        senderName: String,
        messageType: String = "TEXT",
        reaction: String? = null
    ): Boolean {
        val db = firestore
        val docId = "${chatId}_${messageTimestamp}"
        val data = hashMapOf(
            "chatId" to chatId,
            "messageTimestamp" to messageTimestamp,
            "text" to text,
            "senderName" to senderName,
            "messageType" to messageType,
            "reaction" to (reaction ?: ""),
            "reactionState" to (reaction ?: ""),
            "hasReaction" to (!reaction.isNullOrBlank()),
            "updatedAt" to System.currentTimeMillis()
        )

        if (db == null) return true

        return try {
            db.collection("conversations")
                .document(chatId)
                .collection("messages")
                .document(docId)
                .set(data, SetOptions.merge())
                .await()

            db.collection("messages")
                .document(docId)
                .set(data, SetOptions.merge())
                .await()
            true
        } catch (e: Exception) {
            Log.e(tag, "Failed to save message to firestore: ${e.message}")
            false
        }
    }

    fun listenToChatReactions(
        chatId: String,
        onReactionReceived: (timestamp: Long, reaction: String?) -> Unit
    ): ListenerRegistration? {
        // Register in local listeners
        val localListener: (String, Long, String?) -> Unit = { cId, ts, r ->
            if (cId == chatId) {
                onReactionReceived(ts, r)
            }
        }
        localReactionListeners.add(localListener)

        val db = firestore ?: return null
        return try {
            db.collection("message_reactions")
                .whereEqualTo("chatId", chatId)
                .addSnapshotListener { snapshot, error ->
                    if (error != null) {
                        Log.e(tag, "Reaction listener error: ${error.message}")
                        return@addSnapshotListener
                    }
                    snapshot?.documentChanges?.forEach { change ->
                        val doc = change.document
                        val ts = doc.getLong("messageTimestamp") ?: 0L
                        val rawReaction = doc.getString("reaction")
                        val reaction = if (rawReaction.isNullOrBlank()) null else rawReaction
                        if (ts > 0L) {
                            onReactionReceived(ts, reaction)
                        }
                    }
                }
        } catch (e: Exception) {
            Log.e(tag, "Failed to register reaction listener: ${e.message}")
            null
        }
    }

    private fun seedInitialConversationsIfEmpty() {
        scope.launch(Dispatchers.IO) {
            val db = firestore ?: return@launch
            try {
                val samples = getDefaultSampleConversations()
                for (sample in samples) {
                    val data = hashMapOf(
                        "title" to sample.title,
                        "lastMessage" to sample.lastMessage,
                        "lastSender" to sample.lastSender,
                        "timestamp" to sample.timestamp,
                        "unreadCount" to sample.unreadCount,
                        "avatarColorHex" to sample.avatarColorHex,
                        "isGroup" to sample.isGroup,
                        "participants" to sample.participants
                    )
                    db.collection("conversations").document(sample.id).set(data, SetOptions.merge())
                }
            } catch (e: Exception) {
                Log.d(tag, "Initial seed skipped or failed: ${e.message}")
            }
        }
    }

    private fun getDefaultSampleConversations(): List<FirestoreConversation> {
        val now = System.currentTimeMillis()
        return listOf(
            FirestoreConversation(
                id = "fs_tech_chat",
                title = "کانال رسمی اخبار تکنولوژی 📱",
                lastMessage = "نسخه جدید مسنجر ابری با همگام‌سازی بلادرنگ فایراستور منتشر شد.",
                lastSender = "اخبار",
                timestamp = now - 1000 * 60 * 42,
                unreadCount = 0,
                avatarColorHex = 0xFF2AABEE,
                isGroup = true,
                participants = listOf("اخبار")
            ),
            FirestoreConversation(
                id = "fs_private_sara",
                title = "سارا علوی (چت ابری)",
                lastMessage = "سلام! پیام‌های این بخش از فضای ابری نکستا همگام‌سازی می‌شوند.",
                lastSender = "سارا علوی",
                timestamp = now - 1000 * 60 * 180,
                unreadCount = 1,
                avatarColorHex = 0xFFE91E63,
                isGroup = false,
                participants = listOf("سارا علوی", "من")
            )
        )
    }
}
