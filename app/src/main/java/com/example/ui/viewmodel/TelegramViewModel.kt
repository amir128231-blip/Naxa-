package com.example.ui.viewmodel

import android.app.Activity
import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.auth.FirebaseAuthManager
import com.example.data.auth.PhoneAuthState
import com.example.data.local.TelegramDatabase
import com.example.data.model.ChatContact
import com.example.data.model.ChatMessage
import com.example.data.remote.OnlineSyncStatus
import com.example.data.repository.TelegramRepository
import com.google.firebase.auth.FirebaseUser
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import com.example.ui.i18n.AppLanguage
import com.example.ui.components.TelegramNavTab

enum class ChatFilterTab(val titleFa: String, val titleEn: String) {
    ALL("همه", "All"),
    PRIVATE("شخصی", "Personal"),
    GROUPS("گروه‌ها", "Groups"),
    CHANNELS("کانال‌ها", "Channels"),
    UNREAD("خوانده نشده", "Unread")
}

data class CurrentUserProfile(
    val name: String = "کاربر نکسا",
    val username: String = "@nexa_user",
    val phone: String = "+98 912 123 4567",
    val email: String? = null,
    val bio: String = "همیشه متصل با پیام‌رسان هوشمند نکسا ✨",
    val avatarUri: String? = null,
    val avatarColorHex: Long = 0xFF6366F1,
    val isGoogleUser: Boolean = false
)

class TelegramViewModel(application: Application) : AndroidViewModel(application) {
    private val database = TelegramDatabase.getDatabase(application)
    val repository = TelegramRepository(
        database.contactDao(),
        database.messageDao(),
        viewModelScope
    )

    val authManager = FirebaseAuthManager(application, viewModelScope)
    val authState: StateFlow<PhoneAuthState> = authManager.authState
    val firebaseUser: StateFlow<FirebaseUser?> = authManager.currentUser

    val securityManager = com.example.data.security.AppSecurityManager(application)
    val isAppLocked: StateFlow<Boolean> = securityManager.isAppLocked
    val isPasscodeEnabled: StateFlow<Boolean> = securityManager.isPasscodeEnabled
    val isTwoFactorEnabled: StateFlow<Boolean> = securityManager.isTwoFactorEnabled
    val isEndToEndEncryptionActive: StateFlow<Boolean> = securityManager.isEndToEndEncryptionActive
    val isScreenProtectionEnabled: StateFlow<Boolean> = securityManager.isScreenProtectionEnabled

    private val _showAuthScreen = MutableStateFlow(authManager.currentUser.value == null)
    val showAuthScreen: StateFlow<Boolean> = _showAuthScreen.asStateFlow()

    private val _userProfile = MutableStateFlow(CurrentUserProfile())
    val userProfile: StateFlow<CurrentUserProfile> = _userProfile.asStateFlow()

    private val _activeChatId = MutableStateFlow<String?>(null)
    val activeChatId: StateFlow<String?> = _activeChatId.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _selectedFilterTab = MutableStateFlow(ChatFilterTab.ALL)
    val selectedFilterTab: StateFlow<ChatFilterTab> = _selectedFilterTab.asStateFlow()

    private val _replyingTo = MutableStateFlow<ChatMessage?>(null)
    val replyingTo: StateFlow<ChatMessage?> = _replyingTo.asStateFlow()

    private val _isRecordingVoice = MutableStateFlow(false)
    val isRecordingVoice: StateFlow<Boolean> = _isRecordingVoice.asStateFlow()

    private val _recordingSeconds = MutableStateFlow(0)
    val recordingSeconds: StateFlow<Int> = _recordingSeconds.asStateFlow()
    private var recordingJob: Job? = null

    private val _isFriendTyping = MutableStateFlow(false)
    val isFriendTyping: StateFlow<Boolean> = _isFriendTyping.asStateFlow()

    private val _profileDetailContact = MutableStateFlow<ChatContact?>(null)
    val profileDetailContact: StateFlow<ChatContact?> = _profileDetailContact.asStateFlow()

    private val _showAddFriendDialog = MutableStateFlow(false)
    val showAddFriendDialog: StateFlow<Boolean> = _showAddFriendDialog.asStateFlow()

    private val _showContactsListDialog = MutableStateFlow(false)
    val showContactsListDialog: StateFlow<Boolean> = _showContactsListDialog.asStateFlow()

    private val _showContactsScreen = MutableStateFlow(false)
    val showContactsScreen: StateFlow<Boolean> = _showContactsScreen.asStateFlow()

    private val _showNewGroupDialog = MutableStateFlow(false)
    val showNewGroupDialog: StateFlow<Boolean> = _showNewGroupDialog.asStateFlow()

    private val _showNewChannelDialog = MutableStateFlow(false)
    val showNewChannelDialog: StateFlow<Boolean> = _showNewChannelDialog.asStateFlow()

    private val _showStickerSheet = MutableStateFlow(false)
    val showStickerSheet: StateFlow<Boolean> = _showStickerSheet.asStateFlow()

    private val _showAttachmentSheet = MutableStateFlow(false)
    val showAttachmentSheet: StateFlow<Boolean> = _showAttachmentSheet.asStateFlow()

    private val _activeCallState = MutableStateFlow<Pair<String, ChatContact?>?>(null) // (callType, contact)
    val activeCallState: StateFlow<Pair<String, ChatContact?>?> = _activeCallState.asStateFlow()

    private val _isDarkMode = MutableStateFlow(false)
    val isDarkMode: StateFlow<Boolean> = _isDarkMode.asStateFlow()

    val syncStatus: StateFlow<OnlineSyncStatus> = repository.syncStatus

    private val _showOnlineRoomsDialog = MutableStateFlow(false)
    val showOnlineRoomsDialog: StateFlow<Boolean> = _showOnlineRoomsDialog.asStateFlow()

    private val _showProfileSettingsDialog = MutableStateFlow(false)
    val showProfileSettingsDialog: StateFlow<Boolean> = _showProfileSettingsDialog.asStateFlow()

    private val appPreferences by lazy {
        application.getSharedPreferences("nexa_settings", android.content.Context.MODE_PRIVATE)
    }

    private fun resolveInitialLanguage(): AppLanguage {
        val saved = appPreferences.getString("app_language_code", null)
        return if (saved != null) {
            AppLanguage.fromCode(saved)
        } else {
            AppLanguage.ENGLISH
        }
    }

    private val _showLanguageDialog = MutableStateFlow(false)
    val showLanguageDialog: StateFlow<Boolean> = _showLanguageDialog.asStateFlow()

    private val _currentLanguage = MutableStateFlow(resolveInitialLanguage())
    val currentLanguage: StateFlow<AppLanguage> = _currentLanguage.asStateFlow()

    private val _currentBottomTab = MutableStateFlow(TelegramNavTab.CHATS)
    val currentBottomTab: StateFlow<TelegramNavTab> = _currentBottomTab.asStateFlow()

    fun selectBottomTab(tab: TelegramNavTab) {
        _currentBottomTab.value = tab
        when (tab) {
            TelegramNavTab.CONTACTS -> {
                _showContactsScreen.value = true
            }
            TelegramNavTab.PROFILE -> {
                _showProfileSettingsDialog.value = true
            }
            else -> {
                _showContactsScreen.value = false
            }
        }
    }

    // Firestore Integration
    val firestoreService = com.example.data.remote.FirestoreChatService(viewModelScope)
    val firestoreConversations: StateFlow<List<com.example.data.remote.FirestoreConversation>> = firestoreService.conversations
    val isFirestoreLoading: StateFlow<Boolean> = firestoreService.isLoading
    val firestoreErrorMessage: StateFlow<String?> = firestoreService.errorMessage
    private var firestoreReactionListener: com.google.firebase.firestore.ListenerRegistration? = null

    private val _showCreateFirestoreDialog = MutableStateFlow(false)
    val showCreateFirestoreDialog: StateFlow<Boolean> = _showCreateFirestoreDialog.asStateFlow()

    fun setCreateFirestoreDialogVisible(visible: Boolean) {
        _showCreateFirestoreDialog.value = visible
    }

    fun refreshFirestoreConversations() {
        viewModelScope.launch {
            firestoreService.refreshConversations()
        }
    }

    fun createFirestoreConversation(title: String, initialMessage: String) {
        viewModelScope.launch {
            val myName = _userProfile.value.name
            val id = firestoreService.createNewConversation(title, initialMessage, myName, isGroup = true)
            _showCreateFirestoreDialog.value = false
            repository.addNewContact(
                name = title,
                username = "@$id",
                phone = "",
                bio = "گفتگوی ابری نکستا",
                avatarColor = 0xFFFF9800
            )
            repository.sendMessage(
                chatId = id,
                text = initialMessage,
                replyToId = null,
                replyToSender = null,
                replyToText = null,
                senderMyName = myName
            )
            selectChat(id)
        }
    }

    fun openFirestoreConversation(conv: com.example.data.remote.FirestoreConversation) {
        viewModelScope.launch {
            repository.addNewContact(
                name = conv.title,
                username = "@${conv.id}",
                phone = "",
                bio = "گفتگوی ابری نکستا",
                avatarColor = conv.avatarColorHex
            )
            if (conv.lastMessage.isNotBlank()) {
                repository.sendMessage(
                    chatId = conv.id,
                    text = conv.lastMessage,
                    replyToId = null,
                    replyToSender = null,
                    replyToText = null,
                    senderMyName = conv.lastSender.ifBlank { "Firestore" }
                )
            }
            selectChat(conv.id)
        }
    }

    fun setLanguageDialogVisible(visible: Boolean) {
        _showLanguageDialog.value = visible
    }

    fun selectLanguage(language: AppLanguage) {
        _currentLanguage.value = language
        appPreferences.edit().putString("app_language_code", language.code).apply()
        _showLanguageDialog.value = false
    }

    fun resetToSystemLanguage() {
        appPreferences.edit().remove("app_language_code").apply()
        _currentLanguage.value = AppLanguage.fromSystemLocale()
        _showLanguageDialog.value = false
    }

    fun syncLanguageWithSystemIfNeeded() {
        val saved = appPreferences.getString("app_language_code", null)
        if (saved != null) {
            val prefLang = AppLanguage.fromCode(saved)
            if (_currentLanguage.value != prefLang) {
                _currentLanguage.value = prefLang
            }
        }
    }

    // Filtered Contacts Flow
    val filteredContacts: StateFlow<List<ChatContact>> = combine(
        repository.contacts,
        _searchQuery,
        _selectedFilterTab
    ) { contactsList, query, filter ->
        contactsList.filter { contact ->
            // Only show contacts in the main chat list that have active messages/conversations,
            // or are saved messages, or are groups/channels
            val hasConversations = contact.isSavedMessages || contact.isGroup || contact.lastMessageText.isNotBlank()
            if (!hasConversations) {
                return@filter false
            }

            val matchesQuery = query.isBlank() ||
                    contact.name.contains(query, ignoreCase = true) ||
                    contact.username.contains(query, ignoreCase = true) ||
                    contact.lastMessageText.contains(query, ignoreCase = true)

            val matchesFilter = when (filter) {
                ChatFilterTab.ALL -> true
                ChatFilterTab.PRIVATE -> !contact.isGroup && !contact.isSavedMessages
                ChatFilterTab.GROUPS -> contact.isGroup
                ChatFilterTab.CHANNELS -> contact.id.startsWith("channel_") || contact.id.startsWith("fs_") || contact.id.startsWith("firestore_")
                ChatFilterTab.UNREAD -> contact.unreadCount > 0
            }

            matchesQuery && matchesFilter
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    // Current Chat Details
    val currentChat: StateFlow<ChatContact?> = _activeChatId.flatMapLatest { id ->
        if (id == null) flowOf(null) else repository.observeContact(id)
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = null
    )

    // Current Messages
    val currentMessages: StateFlow<List<ChatMessage>> = _activeChatId.flatMapLatest { id ->
        if (id == null) flowOf(emptyList()) else repository.getMessagesForChat(id)
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    init {
        viewModelScope.launch {
            repository.initializeDefaultData()
        }

        viewModelScope.launch {
            authManager.authState.collect { state ->
                if (state is PhoneAuthState.Authenticated) {
                    val user = state.user
                    val phone = user?.phoneNumber
                    val fbDisplayName = user?.displayName
                    val fbPhotoUri = user?.photoUrl?.toString()
                    val fbEmail = user?.email
                    val isGoogle = user?.providerData?.any { it.providerId == "google.com" } == true ||
                            !fbEmail.isNullOrBlank()

                    val cleanUsername = if (!fbEmail.isNullOrBlank()) {
                        "@" + fbEmail.substringBefore("@")
                    } else {
                        _userProfile.value.username
                    }

                    _userProfile.value = _userProfile.value.copy(
                        name = if (!fbDisplayName.isNullOrBlank()) fbDisplayName else _userProfile.value.name,
                        phone = if (!phone.isNullOrBlank()) phone else if (!fbEmail.isNullOrBlank()) fbEmail else _userProfile.value.phone,
                        email = fbEmail,
                        username = cleanUsername,
                        bio = if (isGoogle) "عضو تایید شده نکسا • متصل با حساب گوگل 🌐" else _userProfile.value.bio,
                        avatarUri = fbPhotoUri ?: _userProfile.value.avatarUri,
                        isGoogleUser = isGoogle
                    )
                    _showAuthScreen.value = false
                }
            }
        }
    }

    fun selectChat(chatId: String) {
        val oldChat = _activeChatId.value
        if (oldChat != null && (oldChat.startsWith("online_") || oldChat.startsWith("room_"))) {
            repository.stopListeningToRoom(oldChat)
        }
        firestoreReactionListener?.remove()
        _activeChatId.value = chatId
        _replyingTo.value = null
        _showStickerSheet.value = false
        _showAttachmentSheet.value = false
        if (chatId.startsWith("online_") || chatId.startsWith("room_")) {
            repository.startListeningToRoom(chatId)
        }

        // Start real-time Firestore reaction sync for this conversation
        firestoreReactionListener = firestoreService.listenToChatReactions(chatId) { timestamp, remoteReaction ->
            viewModelScope.launch {
                repository.updateReactionByTimestamp(chatId, timestamp, remoteReaction)
            }
        }

        viewModelScope.launch {
            repository.markChatAsRead(chatId)
        }
    }

    fun closeChat() {
        val oldChat = _activeChatId.value
        if (oldChat != null && (oldChat.startsWith("online_") || oldChat.startsWith("room_"))) {
            repository.stopListeningToRoom(oldChat)
        }
        firestoreReactionListener?.remove()
        firestoreReactionListener = null
        _activeChatId.value = null
        _replyingTo.value = null
        _showStickerSheet.value = false
        _showAttachmentSheet.value = false
    }

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun setFilterTab(tab: ChatFilterTab) {
        _selectedFilterTab.value = tab
    }

    fun setReplyingTo(message: ChatMessage?) {
        _replyingTo.value = message
    }

    fun toggleDarkMode() {
        _isDarkMode.value = !_isDarkMode.value
    }

    fun openContactProfile(contact: ChatContact) {
        _profileDetailContact.value = contact
    }

    fun closeContactProfile() {
        _profileDetailContact.value = null
    }

    fun setAddFriendDialogVisible(visible: Boolean) {
        _showAddFriendDialog.value = visible
    }

    fun setContactsListDialogVisible(visible: Boolean) {
        _showContactsListDialog.value = visible
    }

    fun setContactsScreenVisible(visible: Boolean) {
        _showContactsScreen.value = visible
    }

    fun setNewGroupDialogVisible(visible: Boolean) {
        _showNewGroupDialog.value = visible
    }

    fun setNewChannelDialogVisible(visible: Boolean) {
        _showNewChannelDialog.value = visible
    }

    fun toggleStickerSheet() {
        _showStickerSheet.value = !_showStickerSheet.value
        if (_showStickerSheet.value) {
            _showAttachmentSheet.value = false
        }
    }

    fun setStickerSheetVisible(visible: Boolean) {
        _showStickerSheet.value = visible
    }

    fun toggleAttachmentSheet() {
        _showAttachmentSheet.value = !_showAttachmentSheet.value
        if (_showAttachmentSheet.value) {
            _showStickerSheet.value = false
        }
    }

    fun setAttachmentSheetVisible(visible: Boolean) {
        _showAttachmentSheet.value = visible
    }

    fun startCall(type: String, contact: ChatContact) {
        _activeCallState.value = Pair(type, contact)
    }

    fun endCall() {
        _activeCallState.value = null
    }

    fun sendMessage(text: String) {
        val chatId = _activeChatId.value ?: return
        if (text.isBlank()) return

        val reply = _replyingTo.value
        val myName = _userProfile.value.name
        viewModelScope.launch {
            repository.sendMessage(
                chatId = chatId,
                text = text.trim(),
                replyToId = reply?.id,
                replyToSender = reply?.senderName,
                replyToText = reply?.text,
                senderMyName = myName
            )
            _replyingTo.value = null

            val msgTime = System.currentTimeMillis()
            firestoreService.saveMessageToFirestore(
                chatId = chatId,
                messageTimestamp = msgTime,
                text = text.trim(),
                senderName = myName,
                messageType = "TEXT",
                reaction = null
            )

            // Sync with Firestore if it's a Firestore conversation
            if (chatId.startsWith("fs_") || chatId.startsWith("firestore_")) {
                val currentContact = currentChat.value
                firestoreService.saveConversation(
                    com.example.data.remote.FirestoreConversation(
                        id = chatId,
                        title = currentContact?.name ?: "گفتگوی ابری",
                        lastMessage = text.trim(),
                        lastSender = myName,
                        timestamp = msgTime,
                        avatarColorHex = currentContact?.avatarColorHex ?: 0xFFFF9800
                    )
                )
            }

            // Show typing indicator if friend will reply
            if (chatId != "saved_messages" && !chatId.startsWith("online_") && !chatId.startsWith("room_")) {
                delay(600)
                _isFriendTyping.value = true
                delay(1600)
                _isFriendTyping.value = false
            }
        }
    }

    fun startVoiceRecording() {
        _isRecordingVoice.value = true
        _recordingSeconds.value = 0
        recordingJob?.cancel()
        recordingJob = viewModelScope.launch {
            while (_isRecordingVoice.value) {
                delay(1000)
                _recordingSeconds.value += 1
            }
        }
    }

    fun finishVoiceRecording() {
        val chatId = _activeChatId.value ?: return
        val duration = _recordingSeconds.value
        _isRecordingVoice.value = false
        recordingJob?.cancel()
        val finalDuration = if (duration == 0) 3 else duration
        val myName = _userProfile.value.name

        viewModelScope.launch {
            repository.sendVoiceMessage(chatId, finalDuration, senderMyName = myName)
            if (chatId != "saved_messages" && !chatId.startsWith("online_") && !chatId.startsWith("room_")) {
                delay(700)
                _isFriendTyping.value = true
                delay(1600)
                _isFriendTyping.value = false
            }
        }
    }

    fun cancelVoiceRecording() {
        _isRecordingVoice.value = false
        _recordingSeconds.value = 0
        recordingJob?.cancel()
    }

    fun sendSticker(emoji: String) {
        val chatId = _activeChatId.value ?: return
        _showStickerSheet.value = false
        val myName = _userProfile.value.name
        viewModelScope.launch {
            repository.sendSticker(chatId, emoji, senderMyName = myName)
            if (chatId != "saved_messages" && !chatId.startsWith("online_") && !chatId.startsWith("room_")) {
                delay(600)
                _isFriendTyping.value = true
                delay(1500)
                _isFriendTyping.value = false
            }
        }
    }

    fun addReaction(messageId: Long, reaction: String) {
        viewModelScope.launch {
            val msg = repository.getMessageById(messageId)
            val newReaction = if (msg?.reaction == reaction) null else reaction
            repository.updateReaction(messageId, newReaction)

            if (msg != null) {
                val chatId = msg.chatId
                val myName = _userProfile.value.name
                firestoreService.syncMessageReaction(
                    chatId = chatId,
                    messageTimestamp = msg.timestamp,
                    text = msg.text,
                    senderName = myName,
                    reaction = newReaction
                )
            }
        }
    }

    fun deleteMessage(messageId: Long) {
        viewModelScope.launch {
            repository.deleteMessage(messageId)
        }
    }

    fun clearChat(chatId: String) {
        viewModelScope.launch {
            repository.clearChatHistory(chatId)
        }
    }

    fun togglePin(chatId: String) {
        viewModelScope.launch {
            repository.togglePin(chatId)
        }
    }

    fun toggleMute(chatId: String) {
        viewModelScope.launch {
            repository.toggleMute(chatId)
        }
    }

    fun toggleContactOnlineStatus(contactId: String) {
        viewModelScope.launch {
            repository.toggleContactOnlineStatus(contactId)
            val currentDetail = _profileDetailContact.value
            if (currentDetail != null && currentDetail.id == contactId) {
                val newOnline = !currentDetail.isOnline
                val newLastSeen = if (newOnline) "آنلاین" else "آخرین بازدید لحظاتی پیش"
                _profileDetailContact.value = currentDetail.copy(isOnline = newOnline, lastSeen = newLastSeen)
            }
        }
    }

    fun deleteChat(chatId: String) {
        viewModelScope.launch {
            repository.deleteContact(chatId)
            if (_activeChatId.value == chatId) {
                closeChat()
            }
        }
    }

    fun addFriend(
        name: String,
        username: String,
        phone: String,
        bio: String,
        avatarColor: Long = 0xFF2AABEE,
        openChatImmediately: Boolean = true
    ) {
        viewModelScope.launch {
            val finalColor = if (avatarColor != 0L) {
                avatarColor
            } else {
                listOf(
                    0xFF2AABEE, 0xFFE91E63, 0xFF9C27B0, 0xFF4CAF50, 0xFFFF9800, 0xFF009688
                ).random()
            }
            val newId = repository.addNewContact(
                name = name,
                username = username,
                phone = phone,
                bio = bio,
                avatarColor = finalColor
            )
            _showAddFriendDialog.value = false
            if (openChatImmediately) {
                _showContactsScreen.value = false
                _showContactsListDialog.value = false
                selectChat(newId)
            }
        }
    }

    fun deleteFriend(contactId: String) {
        viewModelScope.launch {
            repository.deleteContact(contactId)
        }
    }

    fun createGroup(name: String, memberNames: List<String>) {
        viewModelScope.launch {
            val newId = repository.createNewGroup(name, memberNames)
            _showNewGroupDialog.value = false
            selectChat(newId)
        }
    }

    fun createChannel(name: String, description: String) {
        viewModelScope.launch {
            val newId = repository.createNewChannel(name, description)
            _showNewChannelDialog.value = false
            selectChat(newId)
        }
    }

    fun setOnlineRoomsDialogVisible(visible: Boolean) {
        _showOnlineRoomsDialog.value = visible
    }

    fun setProfileSettingsDialogVisible(visible: Boolean) {
        _showProfileSettingsDialog.value = visible
    }

    fun joinOnlineRoom(roomCode: String, roomTitle: String) {
        viewModelScope.launch {
            val chatId = repository.joinOrCreateOnlineRoom(roomCode, roomTitle)
            _showOnlineRoomsDialog.value = false
            selectChat(chatId)
        }
    }

    fun updateMyProfile(name: String, bio: String, phone: String, avatarUri: String? = _userProfile.value.avatarUri) {
        _userProfile.value = _userProfile.value.copy(
            name = name,
            bio = bio,
            phone = phone,
            avatarUri = avatarUri
        )
        // Also update Firebase Auth user profile if signed in
        viewModelScope.launch {
            try {
                val fbUser = authManager.currentUser.value
                if (fbUser != null) {
                    val profileUpdates = com.google.firebase.auth.userProfileChangeRequest {
                        displayName = name
                        if (avatarUri != null) {
                            photoUri = android.net.Uri.parse(avatarUri)
                        }
                    }
                    fbUser.updateProfile(profileUpdates)
                }
            } catch (e: Throwable) {
                android.util.Log.e("TelegramViewModel", "Error updating Firebase user profile: ${e.message}")
            }
        }
        _showProfileSettingsDialog.value = false
    }

    fun updateProfilePicture(uriString: String) {
        _userProfile.value = _userProfile.value.copy(avatarUri = uriString)
        viewModelScope.launch {
            try {
                val fbUser = authManager.currentUser.value
                if (fbUser != null) {
                    val profileUpdates = com.google.firebase.auth.userProfileChangeRequest {
                        photoUri = android.net.Uri.parse(uriString)
                    }
                    fbUser.updateProfile(profileUpdates)
                }
            } catch (e: Throwable) {
                android.util.Log.e("TelegramViewModel", "Error updating Firebase photoUri: ${e.message}")
            }
        }
    }

    private val _syncPhoneContactsMessage = MutableStateFlow<String?>(null)
    val syncPhoneContactsMessage: StateFlow<String?> = _syncPhoneContactsMessage.asStateFlow()

    private val _isSyncingPhoneContacts = MutableStateFlow(false)
    val isSyncingPhoneContacts: StateFlow<Boolean> = _isSyncingPhoneContacts.asStateFlow()

    fun clearSyncPhoneMessage() {
        _syncPhoneContactsMessage.value = null
    }

    fun syncPhoneContacts() {
        viewModelScope.launch {
            _isSyncingPhoneContacts.value = true
            try {
                val context = getApplication<Application>().applicationContext
                val deviceContacts = com.example.data.util.DeviceContactsHelper.readDeviceContacts(context)
                if (deviceContacts.isEmpty()) {
                    _syncPhoneContactsMessage.value = "هیچ مخاطبی در حافظه یا سیم‌کارت گوشی یافت نشد."
                } else {
                    val added = repository.syncDeviceContacts(deviceContacts)
                    _syncPhoneContactsMessage.value = if (added > 0) {
                        "$added مخاطب با موفقیت همگام‌سازی و به پیوی اضافه شد 🎉"
                    } else {
                        "تمام ${deviceContacts.size} مخاطب گوشی قبلاً اضافه شده‌اند و در لیست هستند."
                    }
                }
            } catch (e: Exception) {
                _syncPhoneContactsMessage.value = "خطا در خواندن مخاطبین گوشی: ${e.message}"
            } finally {
                _isSyncingPhoneContacts.value = false
            }
        }
    }

    fun openSupportBot() {
        // Support bot removed per request
    }

    // Firebase Authentication (Phone & Google Sign-In)
    fun setShowAuthScreen(show: Boolean) {
        // Only allow dismissing if user is authenticated
        if (!show && authManager.currentUser.value == null && _userProfile.value.phone.isBlank()) {
            return
        }
        _showAuthScreen.value = show
    }

    fun signInWithGoogle(activity: Activity) {
        authManager.signInWithGoogle(activity)
    }

    fun simulateGoogleSignIn(
        email: String = "amir.nexa@gmail.com",
        displayName: String = "امیرحسین رضایی (Google)"
    ) {
        authManager.simulateGoogleSignIn(email = email, displayName = displayName)
        _userProfile.value = _userProfile.value.copy(
            name = displayName,
            email = email,
            username = "@" + email.substringBefore("@"),
            phone = email,
            bio = "عضو تایید شده نکسا • متصل با حساب گوگل 🌐",
            isGoogleUser = true
        )
        _showAuthScreen.value = false
    }

    fun sendEmailVerificationCode(email: String) {
        authManager.startEmailVerification(email)
    }

    fun resendEmailCode() {
        authManager.resendEmailVerification()
    }

    fun signInWithDirectEmail(email: String) {
        val cleanEmail = email.trim().lowercase()
        val displayName = cleanEmail.substringBefore("@").replaceFirstChar { it.uppercase() }
        simulateGoogleSignIn(email = cleanEmail, displayName = displayName)
    }

    fun sendPhoneVerificationCode(activity: Activity, phoneNumber: String) {
        authManager.startPhoneNumberVerification(activity, phoneNumber)
    }

    fun verifyPhoneCode(code: String) {
        authManager.verifyCode(code)
    }

    fun resendPhoneCode(activity: Activity) {
        authManager.resendVerificationCode(activity)
    }

    fun simulateTestSignIn(phone: String) {
        authManager.simulateTestSignIn(phone)
        _userProfile.value = _userProfile.value.copy(phone = phone)
        _showAuthScreen.value = false
    }

    fun signOut() {
        authManager.signOut()
        _showAuthScreen.value = true
    }

    fun resetAuthState() {
        authManager.resetState()
    }

    fun setAppPasscode(passcode: String): Boolean {
        return securityManager.setPasscode(passcode)
    }

    fun disableAppPasscode(currentPasscode: String): Boolean {
        return securityManager.disablePasscode(currentPasscode)
    }

    fun verifyAppPasscode(passcode: String): Boolean {
        return securityManager.verifyPasscode(passcode)
    }

    fun lockAppNow() {
        securityManager.lockApp()
    }

    fun toggleTwoFactorAuth(enabled: Boolean, password: String = "123456"): Boolean {
        return if (enabled) {
            securityManager.setupTwoFactorAuth(password)
        } else {
            securityManager.disableTwoFactorAuth(password)
        }
    }

    fun toggleEndToEndEncryption(enabled: Boolean) {
        securityManager.toggleEndToEndEncryption(enabled)
    }

    fun toggleScreenProtection(enabled: Boolean) {
        securityManager.toggleScreenProtection(enabled)
    }

    override fun onCleared() {
        super.onCleared()
        firestoreReactionListener?.remove()
        firestoreReactionListener = null
    }
}
