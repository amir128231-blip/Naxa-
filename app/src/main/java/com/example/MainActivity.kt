package com.example

import android.Manifest
import android.app.Activity
import android.content.pm.PackageManager
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.runtime.CompositionLocalProvider
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.components.AddFriendDialog
import com.example.ui.components.ContactProfileSheet
import com.example.ui.components.ContactsListDialog
import com.example.ui.components.CreateFirestoreConversationDialog
import com.example.ui.components.JoinPublicRoomDialog
import com.example.ui.components.LanguageSelectionDialog
import com.example.ui.components.NewChannelDialog
import com.example.ui.components.NewGroupDialog
import com.example.ui.components.ProfileSettingsDialog
import com.example.ui.components.TelegramCallDialog
import com.example.ui.components.TelegramDrawerContent
import com.example.ui.i18n.AppLanguage
import com.example.ui.i18n.LocalAppLanguage
import com.example.ui.i18n.LocalAppStrings
import com.example.ui.i18n.getStringsForLanguage
import com.example.ui.screens.ChatListScreen
import com.example.ui.screens.ConversationScreen
import com.example.ui.screens.ContactsScreen
import com.example.ui.screens.SettingsScreen
import com.example.ui.components.TelegramNavTab
import com.example.ui.screens.AddFriendMechanismDialog
import com.example.ui.screens.PhoneAuthScreen
import com.example.ui.theme.TelegramAppTheme
import com.example.ui.viewmodel.TelegramViewModel
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val viewModel: TelegramViewModel = viewModel()
            val isDarkMode by viewModel.isDarkMode.collectAsStateWithLifecycle()
            val currentLanguage by viewModel.currentLanguage.collectAsStateWithLifecycle()
            val showAuthScreen by viewModel.showAuthScreen.collectAsStateWithLifecycle()
            val authState by viewModel.authState.collectAsStateWithLifecycle()
            val strings = remember(currentLanguage) { getStringsForLanguage(currentLanguage) }

            val configuration = LocalConfiguration.current
            LaunchedEffect(configuration) {
                viewModel.syncLanguageWithSystemIfNeeded()
            }

            val isAppLocked by viewModel.isAppLocked.collectAsStateWithLifecycle()

            CompositionLocalProvider(
                LocalAppLanguage provides currentLanguage,
                LocalAppStrings provides strings,
                LocalLayoutDirection provides currentLanguage.layoutDirection
            ) {
                TelegramAppTheme(darkTheme = isDarkMode) {
                    if (isAppLocked) {
                        com.example.ui.components.AppLockScreen(
                            isDarkMode = isDarkMode,
                            onVerifyPasscode = { pin -> viewModel.verifyAppPasscode(pin) },
                            onEmergencySignOut = { viewModel.signOut() }
                        )
                    } else if (showAuthScreen) {
                        val activity = this@MainActivity
                        PhoneAuthScreen(
                            authState = authState,
                            onSendCode = { act, phone -> viewModel.sendPhoneVerificationCode(act, phone) },
                            onVerifyCode = { code -> viewModel.verifyPhoneCode(code) },
                            onResendCode = { act -> viewModel.resendPhoneCode(act) },
                            onGoogleSignIn = { act -> viewModel.signInWithGoogle(act) },
                            onSimulateGoogleSignIn = { viewModel.simulateGoogleSignIn() },
                            onSimulateTestSignIn = { phone -> viewModel.simulateTestSignIn(phone) },
                            onSkipToGuest = { /* Code verification is strictly required */ },
                            onBack = { viewModel.resetAuthState() },
                            activity = activity
                        )
                    } else {
                        TelegramApp(viewModel = viewModel)
                    }
                }
            }
        }
    }
}

@Composable
fun TelegramApp(viewModel: TelegramViewModel) {
    val contacts by viewModel.filteredContacts.collectAsStateWithLifecycle()
    val allContacts by viewModel.repository.contacts.collectAsStateWithLifecycle(initialValue = emptyList())
    val currentChat by viewModel.currentChat.collectAsStateWithLifecycle()
    val currentMessages by viewModel.currentMessages.collectAsStateWithLifecycle()
    val searchQuery by viewModel.searchQuery.collectAsStateWithLifecycle()
    val selectedFilterTab by viewModel.selectedFilterTab.collectAsStateWithLifecycle()
    val replyingTo by viewModel.replyingTo.collectAsStateWithLifecycle()
    val isRecordingVoice by viewModel.isRecordingVoice.collectAsStateWithLifecycle()
    val recordingSeconds by viewModel.recordingSeconds.collectAsStateWithLifecycle()
    val isFriendTyping by viewModel.isFriendTyping.collectAsStateWithLifecycle()
    val profileDetailContact by viewModel.profileDetailContact.collectAsStateWithLifecycle()
    val showAddFriendDialog by viewModel.showAddFriendDialog.collectAsStateWithLifecycle()
    val showContactsListDialog by viewModel.showContactsListDialog.collectAsStateWithLifecycle()
    val showContactsScreen by viewModel.showContactsScreen.collectAsStateWithLifecycle()
    val showNewGroupDialog by viewModel.showNewGroupDialog.collectAsStateWithLifecycle()
    val showNewChannelDialog by viewModel.showNewChannelDialog.collectAsStateWithLifecycle()
    val showStickerSheet by viewModel.showStickerSheet.collectAsStateWithLifecycle()
    val showAttachmentSheet by viewModel.showAttachmentSheet.collectAsStateWithLifecycle()
    val activeCallState by viewModel.activeCallState.collectAsStateWithLifecycle()
    val isDarkMode by viewModel.isDarkMode.collectAsStateWithLifecycle()
    val userProfile by viewModel.userProfile.collectAsStateWithLifecycle()
    val firebaseUser by viewModel.firebaseUser.collectAsStateWithLifecycle()
    val syncStatus by viewModel.syncStatus.collectAsStateWithLifecycle()
    val showOnlineRoomsDialog by viewModel.showOnlineRoomsDialog.collectAsStateWithLifecycle()
    val showProfileSettingsDialog by viewModel.showProfileSettingsDialog.collectAsStateWithLifecycle()
    val showLanguageDialog by viewModel.showLanguageDialog.collectAsStateWithLifecycle()
    val currentLanguage by viewModel.currentLanguage.collectAsStateWithLifecycle()
    val syncPhoneContactsMessage by viewModel.syncPhoneContactsMessage.collectAsStateWithLifecycle()
    val isSyncingPhoneContacts by viewModel.isSyncingPhoneContacts.collectAsStateWithLifecycle()
    val firestoreConversations by viewModel.firestoreConversations.collectAsStateWithLifecycle()
    val isFirestoreLoading by viewModel.isFirestoreLoading.collectAsStateWithLifecycle()
    val firestoreErrorMessage by viewModel.firestoreErrorMessage.collectAsStateWithLifecycle()
    val showCreateFirestoreDialog by viewModel.showCreateFirestoreDialog.collectAsStateWithLifecycle()
    val currentBottomTab by viewModel.currentBottomTab.collectAsStateWithLifecycle()

    val context = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()

    // Permission request launcher for READ_CONTACTS
    val contactsPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            Toast.makeText(context, "مجوز دسترسی به مخاطبین تایید شد. در حال همگام‌سازی...", Toast.LENGTH_SHORT).show()
            viewModel.syncPhoneContacts()
        } else {
            Toast.makeText(
                context,
                "برای نمایش مخاطبین گوشی، لطفاً مجوز دسترسی را تایید کنید.",
                Toast.LENGTH_LONG
            ).show()
        }
    }

    // Function to trigger contacts sync with runtime permission check
    val requestSyncContacts = {
        val permissionCheck = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.READ_CONTACTS
        )
        if (permissionCheck == PackageManager.PERMISSION_GRANTED) {
            viewModel.syncPhoneContacts()
        } else {
            contactsPermissionLauncher.launch(Manifest.permission.READ_CONTACTS)
        }
    }

    // Show toast / snackbar when sync finishes
    LaunchedEffect(syncPhoneContactsMessage) {
        syncPhoneContactsMessage?.let { msg ->
            Toast.makeText(context, msg, Toast.LENGTH_LONG).show()
            viewModel.clearSyncPhoneMessage()
        }
    }

    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)

    // Handle system back button
    BackHandler(enabled = currentChat != null || drawerState.isOpen || showContactsScreen || currentBottomTab != TelegramNavTab.CHATS) {
        if (drawerState.isOpen) {
            coroutineScope.launch { drawerState.close() }
        } else if (currentBottomTab != TelegramNavTab.CHATS) {
            viewModel.selectBottomTab(TelegramNavTab.CHATS)
        } else if (showContactsScreen) {
            viewModel.setContactsScreenVisible(false)
        } else if (currentChat != null) {
            viewModel.closeChat()
        }
    }

    ModalNavigationDrawer(
        drawerState = drawerState,
        gesturesEnabled = currentChat == null && !showContactsScreen && currentBottomTab == TelegramNavTab.CHATS,
        drawerContent = {
            TelegramDrawerContent(
                userProfile = userProfile,
                isDarkMode = isDarkMode,
                firebaseUser = firebaseUser,
                onToggleDarkMode = { viewModel.toggleDarkMode() },
                onOpenSavedMessages = { viewModel.selectChat("saved_messages") },
                onOpenAddFriend = {
                    coroutineScope.launch { drawerState.close() }
                    viewModel.setAddFriendDialogVisible(true)
                },
                onOpenNewGroup = { viewModel.setNewGroupDialogVisible(true) },
                onOpenOnlineRooms = { viewModel.setOnlineRoomsDialogVisible(true) },
                onOpenSettings = {
                    coroutineScope.launch { drawerState.close() }
                    viewModel.selectBottomTab(TelegramNavTab.SETTINGS)
                },
                onOpenLanguageSelection = { viewModel.setLanguageDialogVisible(true) },
                onOpenSupportBot = { viewModel.openSupportBot() },
                onOpenPhoneAuth = { viewModel.setShowAuthScreen(true) },
                onSignOut = { viewModel.signOut() },
                onOpenContactsList = {
                    coroutineScope.launch { drawerState.close() }
                    viewModel.selectBottomTab(TelegramNavTab.CONTACTS)
                },
                onSyncDeviceContacts = requestSyncContacts,
                onCloseDrawer = { coroutineScope.launch { drawerState.close() } }
            )
        }
    ) {
        val activeChat = currentChat
        if (activeChat != null) {
            ConversationScreen(
                contact = activeChat,
                messages = currentMessages,
                replyingTo = replyingTo,
                isRecordingVoice = isRecordingVoice,
                recordingSeconds = recordingSeconds,
                isFriendTyping = isFriendTyping,
                showStickerSheet = showStickerSheet,
                showAttachmentSheet = showAttachmentSheet,
                isDarkMode = isDarkMode,
                onBack = { viewModel.closeChat() },
                onOpenProfile = { viewModel.openContactProfile(activeChat) },
                onStartCall = { type -> viewModel.startCall(type, activeChat) },
                onSendMessage = { text -> viewModel.sendMessage(text) },
                onStartVoiceRecording = { viewModel.startVoiceRecording() },
                onFinishVoiceRecording = { viewModel.finishVoiceRecording() },
                onCancelVoiceRecording = { viewModel.cancelVoiceRecording() },
                onReplyToMessage = { msg -> viewModel.setReplyingTo(msg) },
                onCancelReply = { viewModel.setReplyingTo(null) },
                onToggleStickers = { viewModel.toggleStickerSheet() },
                onSendSticker = { sticker -> viewModel.sendSticker(sticker) },
                onToggleAttachments = { viewModel.toggleAttachmentSheet() },
                onSelectAttachment = { attachMsg ->
                    viewModel.sendMessage(attachMsg)
                    viewModel.setAttachmentSheetVisible(false)
                },
                onReactMessage = { msgId, reaction -> viewModel.addReaction(msgId, reaction) },
                onDeleteMessage = { msgId -> viewModel.deleteMessage(msgId) },
                onClearChat = { viewModel.clearChat(activeChat.id) },
                onDeleteChat = { viewModel.deleteChat(activeChat.id) },
                onToggleTheme = { viewModel.toggleDarkMode() },
                onToggleOnlineStatus = { viewModel.toggleContactOnlineStatus(activeChat.id) }
            )
        } else if (currentBottomTab == TelegramNavTab.SETTINGS) {
            SettingsScreen(
                userProfile = userProfile,
                isDarkMode = isDarkMode,
                currentBottomTab = currentBottomTab,
                onTabSelected = { tab -> viewModel.selectBottomTab(tab) },
                onToggleDarkMode = { viewModel.toggleDarkMode() },
                onOpenAccountEdit = { viewModel.setProfileSettingsDialogVisible(true) },
                onOpenLanguageSelection = { viewModel.setLanguageDialogVisible(true) },
                onOpenSupportBot = { viewModel.openSupportBot() },
                onSignOut = { viewModel.signOut() }
            )
        } else if (showContactsScreen || currentBottomTab == TelegramNavTab.CONTACTS) {
            ContactsScreen(
                contacts = allContacts,
                isDarkMode = isDarkMode,
                onBack = {
                    viewModel.setContactsScreenVisible(false)
                    viewModel.selectBottomTab(TelegramNavTab.CHATS)
                },
                onSelectContact = { chatId ->
                    viewModel.setContactsScreenVisible(false)
                    viewModel.selectBottomTab(TelegramNavTab.CHATS)
                    viewModel.selectChat(chatId)
                },
                onStartCall = { type, contact ->
                    viewModel.startCall(type, contact)
                },
                onAddFriend = { name, username, phone, bio, avatarColor ->
                    viewModel.addFriend(name, username, phone, bio, avatarColor, openChatImmediately = true)
                },
                onDeleteFriend = { contactId ->
                    viewModel.deleteFriend(contactId)
                },
                onSyncDeviceContacts = requestSyncContacts,
                isSyncingDeviceContacts = isSyncingPhoneContacts,
                onOpenNewGroup = { viewModel.setNewGroupDialogVisible(true) },
                onOpenNewChannel = { viewModel.setNewChannelDialogVisible(true) }
            )
        } else {
            ChatListScreen(
                contacts = contacts,
                searchQuery = searchQuery,
                selectedFilterTab = selectedFilterTab,
                isDarkMode = isDarkMode,
                onlineStatusText = syncStatus.labelFa,
                onOpenDrawer = { coroutineScope.launch { drawerState.open() } },
                onSearchChange = { query -> viewModel.setSearchQuery(query) },
                onSelectFilter = { tab -> viewModel.setFilterTab(tab) },
                onSelectChat = { chatId -> viewModel.selectChat(chatId) },
                onOpenAddFriend = {
                    requestSyncContacts()
                    viewModel.selectBottomTab(TelegramNavTab.CONTACTS)
                },
                onOpenNewGroup = { viewModel.setNewGroupDialogVisible(true) },
                onOpenOnlineRooms = { viewModel.setOnlineRoomsDialogVisible(true) },
                onOpenContactsList = {
                    requestSyncContacts()
                    viewModel.selectBottomTab(TelegramNavTab.CONTACTS)
                },
                onTogglePin = { chatId -> viewModel.togglePin(chatId) },
                onToggleMute = { chatId -> viewModel.toggleMute(chatId) },
                onDeleteChat = { chatId -> viewModel.deleteChat(chatId) },
                firestoreConversations = firestoreConversations,
                isFirestoreLoading = isFirestoreLoading,
                firestoreErrorMessage = firestoreErrorMessage,
                onRefreshFirestore = { viewModel.refreshFirestoreConversations() },
                onCreateFirestoreChat = { viewModel.setCreateFirestoreDialogVisible(true) },
                onSelectFirestoreConversation = { conv -> viewModel.openFirestoreConversation(conv) },
                currentBottomTab = currentBottomTab,
                onTabSelected = { tab -> viewModel.selectBottomTab(tab) },
                userInitial = userProfile.name.take(1).ifBlank { "A" },
                onToggleTheme = { viewModel.toggleDarkMode() }
            )
        }
    }

    // Contact Profile Sheet
    val profileContact = profileDetailContact
    if (profileContact != null) {
        ContactProfileSheet(
            contact = profileContact,
            isDarkMode = isDarkMode,
            onDismiss = { viewModel.closeContactProfile() },
            onStartCall = { type -> viewModel.startCall(type, profileContact) },
            onToggleMute = { chatId -> viewModel.toggleMute(chatId) },
            onClearChat = { chatId -> viewModel.clearChat(chatId) },
            onDeleteChat = { chatId ->
                viewModel.deleteChat(chatId)
                viewModel.closeContactProfile()
            },
            onOpenChat = { chatId ->
                viewModel.selectChat(chatId)
                viewModel.closeContactProfile()
            },
            onToggleOnline = { viewModel.toggleContactOnlineStatus(profileContact.id) }
        )
    }

    // Add Friend Dialog / Mechanism
    if (showAddFriendDialog) {
        AddFriendMechanismDialog(
            isDarkMode = isDarkMode,
            onDismiss = { viewModel.setAddFriendDialogVisible(false) },
            onAddFriend = { name, username, phone, bio, avatarColor, openChat ->
                viewModel.addFriend(
                    name = name,
                    username = username,
                    phone = phone,
                    bio = bio,
                    avatarColor = avatarColor,
                    openChatImmediately = openChat
                )
            }
        )
    }

    // Contacts & Private Chat (پیوی شخصی) Dialog
    if (showContactsListDialog) {
        ContactsListDialog(
            contacts = allContacts,
            onDismiss = { viewModel.setContactsListDialogVisible(false) },
            onOpenContactChat = { chatId ->
                viewModel.setContactsListDialogVisible(false)
                viewModel.selectChat(chatId)
            },
            onOpenAddNewContact = {
                viewModel.setContactsListDialogVisible(false)
                viewModel.setAddFriendDialogVisible(true)
            },
            onSyncDeviceContacts = requestSyncContacts,
            isSyncingDeviceContacts = isSyncingPhoneContacts
        )
    }

    // New Group Dialog
    if (showNewGroupDialog) {
        NewGroupDialog(
            availableContacts = allContacts,
            onDismiss = { viewModel.setNewGroupDialogVisible(false) },
            onCreate = { name, members ->
                viewModel.createGroup(name, members)
            }
        )
    }

    // New Channel Dialog
    if (showNewChannelDialog) {
        NewChannelDialog(
            onDismiss = { viewModel.setNewChannelDialogVisible(false) },
            onCreate = { name, description ->
                viewModel.createChannel(name, description)
            }
        )
    }

    // Active Audio / Video Call Dialog
    val callState = activeCallState
    if (callState != null && callState.second != null) {
        TelegramCallDialog(
            callType = callState.first,
            contact = callState.second!!,
            onEndCall = { viewModel.endCall() }
        )
    }

    // Join Public Room Dialog
    if (showOnlineRoomsDialog) {
        JoinPublicRoomDialog(
            onDismiss = { viewModel.setOnlineRoomsDialogVisible(false) },
            onJoin = { roomCode, roomTitle ->
                viewModel.joinOnlineRoom(roomCode, roomTitle)
            }
        )
    }

    // Profile Settings Dialog
    if (showProfileSettingsDialog) {
        ProfileSettingsDialog(
            currentName = userProfile.name,
            currentUsername = userProfile.username,
            currentPhone = userProfile.phone,
            currentBio = userProfile.bio,
            currentAvatarUri = userProfile.avatarUri,
            firebaseUser = firebaseUser,
            onOpenPhoneAuth = { viewModel.setShowAuthScreen(true) },
            onSignOut = { viewModel.signOut() },
            onDismiss = { viewModel.setProfileSettingsDialogVisible(false) },
            onSave = { name, bio, phone, avatarUri ->
                viewModel.updateMyProfile(name, bio, phone, avatarUri)
            }
        )
    }

    // Language Selection Dialog
    if (showLanguageDialog) {
        LanguageSelectionDialog(
            currentLanguage = currentLanguage,
            onDismiss = { viewModel.setLanguageDialogVisible(false) },
            onSelectLanguage = { selected ->
                viewModel.selectLanguage(selected)
            },
            onResetToSystem = {
                viewModel.resetToSystemLanguage()
            }
        )
    }

    // Create Firestore Conversation Dialog
    if (showCreateFirestoreDialog) {
        CreateFirestoreConversationDialog(
            onDismiss = { viewModel.setCreateFirestoreDialogVisible(false) },
            onCreate = { title, initialMessage ->
                viewModel.createFirestoreConversation(title, initialMessage)
            }
        )
    }
}
