package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.ChatContact
import com.example.data.model.ChatMessage
import com.example.ui.components.TelegramAttachmentSheet
import com.example.ui.components.TelegramAvatar
import com.example.ui.components.TelegramChatBubble
import com.example.ui.components.TelegramChatInput
import com.example.ui.components.TelegramStickerSheet
import com.example.ui.i18n.LocalAppStrings
import com.example.ui.theme.TelegramBlue
import com.example.ui.theme.TelegramChatBgDark
import com.example.ui.theme.TelegramChatBgLight
import com.example.ui.theme.TelegramHeaderDark

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ConversationScreen(
    contact: ChatContact,
    messages: List<ChatMessage>,
    replyingTo: ChatMessage?,
    isRecordingVoice: Boolean,
    recordingSeconds: Int,
    isFriendTyping: Boolean,
    showStickerSheet: Boolean,
    showAttachmentSheet: Boolean,
    isDarkMode: Boolean,
    onBack: () -> Unit,
    onOpenProfile: () -> Unit,
    onStartCall: (String) -> Unit,
    onSendMessage: (String) -> Unit,
    onStartVoiceRecording: () -> Unit,
    onFinishVoiceRecording: () -> Unit,
    onCancelVoiceRecording: () -> Unit,
    onReplyToMessage: (ChatMessage) -> Unit,
    onCancelReply: () -> Unit,
    onToggleStickers: () -> Unit,
    onSendSticker: (String) -> Unit,
    onToggleAttachments: () -> Unit,
    onSelectAttachment: (String) -> Unit,
    onReactMessage: (Long, String) -> Unit,
    onDeleteMessage: (Long) -> Unit,
    onClearChat: () -> Unit,
    onDeleteChat: () -> Unit,
    onToggleTheme: () -> Unit = {},
    onToggleOnlineStatus: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val listState = rememberLazyListState()
    var showTopMenu by remember { mutableStateOf(false) }
    val strings = LocalAppStrings.current

    // Scroll to bottom whenever messages change
    LaunchedEffect(messages.size, isFriendTyping) {
        if (messages.isNotEmpty()) {
            listState.animateScrollToItem(messages.size - 1)
        }
    }

    Scaffold(
        modifier = modifier
            .fillMaxSize()
            .imePadding(),
        topBar = {
            TopAppBar(
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .clickable { onOpenProfile() }
                            .padding(vertical = 4.dp)
                            .testTag("chat_header_profile")
                    ) {
                        TelegramAvatar(
                            name = contact.name,
                            avatarColorHex = contact.avatarColorHex,
                            size = 40.dp,
                            isOnline = contact.isOnline,
                            isGroup = contact.isGroup,
                            isSavedMessages = contact.isSavedMessages,
                            showOnlineBadge = true
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = contact.name,
                                color = Color.White,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.SemiBold,
                                maxLines = 1
                            )
                            val statusText = when {
                                isFriendTyping -> strings.typingText
                                contact.isSavedMessages -> strings.savedMessagesSub
                                contact.isGroup -> contact.lastSeen
                                contact.id == "bot_support" || contact.name.contains("پشتیبانی") -> strings.smartSupportBot
                                contact.isOnline -> strings.onlinePrivateChat
                                contact.lastSeen.isNotBlank() -> contact.lastSeen
                                else -> strings.privateChat
                            }
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                if (contact.isOnline && !contact.isGroup && !contact.isSavedMessages && !isFriendTyping) {
                                    Box(
                                        modifier = Modifier
                                            .size(7.dp)
                                            .clip(CircleShape)
                                            .background(Color(0xFF4ADE80))
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                }
                                Text(
                                    text = statusText,
                                    color = when {
                                        isFriendTyping -> Color(0xFFFFD54F)
                                        contact.isOnline && !contact.isGroup && !contact.isSavedMessages -> Color(0xFF67E8F9)
                                        else -> Color.White.copy(alpha = 0.85f)
                                    },
                                    fontSize = 12.sp,
                                    maxLines = 1,
                                    fontWeight = if (contact.isOnline && !contact.isGroup) FontWeight.Medium else FontWeight.Normal
                                )
                            }
                        }
                    }
                },
                navigationIcon = {
                    IconButton(
                        onClick = onBack,
                        modifier = Modifier.testTag("back_button")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = Color.White
                        )
                    }
                },
                actions = {
                    if (!contact.isSavedMessages) {
                        IconButton(onClick = { onStartCall("audio") }) {
                            Icon(
                                imageVector = Icons.Default.Call,
                                contentDescription = "Call",
                                tint = Color.White
                            )
                        }
                        IconButton(onClick = { onStartCall("video") }) {
                            Icon(
                                imageVector = Icons.Default.Videocam,
                                contentDescription = "Video Call",
                                tint = Color.White
                            )
                        }
                    }
                    IconButton(
                        onClick = onToggleTheme,
                        modifier = Modifier.testTag("toggle_theme_button")
                    ) {
                        Icon(
                            imageVector = if (isDarkMode) Icons.Default.LightMode else Icons.Default.DarkMode,
                            contentDescription = if (isDarkMode) "تم روشن" else "تم تاریک",
                            tint = Color.White
                        )
                    }

                    IconButton(onClick = { showTopMenu = true }) {
                        Icon(
                            imageVector = Icons.Default.MoreVert,
                            contentDescription = "More",
                            tint = Color.White
                        )
                    }

                    DropdownMenu(
                        expanded = showTopMenu,
                        onDismissRequest = { showTopMenu = false }
                    ) {
                        DropdownMenuItem(
                            text = {
                                Text(if (isDarkMode) "تغییر به تم روشن ☀️" else "تغییر به تم تاریک 🌙")
                            },
                            onClick = {
                                showTopMenu = false
                                onToggleTheme()
                            }
                        )
                        DropdownMenuItem(
                            text = { Text(strings.viewProfileTitle) },
                            onClick = {
                                showTopMenu = false
                                onOpenProfile()
                            }
                        )
                        if (!contact.isSavedMessages && !contact.isGroup) {
                            DropdownMenuItem(
                                text = {
                                    Text(if (contact.isOnline) "تغییر وضعیت به آفلاین 🔴" else "تغییر وضعیت به آنلاین 🟢")
                                },
                                onClick = {
                                    showTopMenu = false
                                    onToggleOnlineStatus()
                                }
                            )
                        }
                        DropdownMenuItem(
                            text = { Text(strings.clearHistoryTitle, color = Color.Red) },
                            onClick = {
                                showTopMenu = false
                                onClearChat()
                            }
                        )
                        DropdownMenuItem(
                            text = { Text(strings.deleteChatTitle, color = Color.Red) },
                            onClick = {
                                showTopMenu = false
                                onDeleteChat()
                            }
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = if (isDarkMode) MaterialTheme.colorScheme.surface else TelegramBlue
                )
            )
        },
        bottomBar = {
            Column(modifier = Modifier.fillMaxWidth()) {
                // Quick suggested prompts for Support Bot
                if (contact.id == "bot_support" || contact.name.contains("پشتیبانی")) {
                    val quickPrompts = listOf(
                        "راهنما 📚" to "/help",
                        "چت آنلاین سراسری 🌐" to "/online",
                        "کد روم دو نفره 🔑" to "/room",
                        "امکانات نکسا ⚡" to "/features",
                        "پیام صوتی (ویس) 🎤" to "چطور ویس بفرستم؟",
                        "تم شب و روز 🌙" to "چطور تم رو عوض کنم؟"
                    )
                    LazyRow(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(if (isDarkMode) MaterialTheme.colorScheme.surfaceVariant else Color(0xFFF1F5F9))
                            .padding(horizontal = 8.dp, vertical = 6.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(quickPrompts) { (title, command) ->
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(16.dp))
                                    .background(if (isDarkMode) MaterialTheme.colorScheme.surface else Color.White)
                                    .clickable { onSendMessage(command) }
                                    .padding(horizontal = 12.dp, vertical = 6.dp)
                            ) {
                                Text(
                                    text = title,
                                    color = if (isDarkMode) MaterialTheme.colorScheme.primary else TelegramBlue,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }
                    }
                }

                TelegramChatInput(
                    replyingTo = replyingTo,
                    isRecordingVoice = isRecordingVoice,
                    recordingSeconds = recordingSeconds,
                    isDarkMode = isDarkMode,
                    onSendMessage = onSendMessage,
                    onStartVoiceRecording = onStartVoiceRecording,
                    onFinishVoiceRecording = onFinishVoiceRecording,
                    onCancelVoiceRecording = onCancelVoiceRecording,
                    onCancelReply = onCancelReply,
                    onToggleStickers = onToggleStickers,
                    onToggleAttachments = onToggleAttachments
                )

                AnimatedVisibility(visible = showStickerSheet) {
                    TelegramStickerSheet(
                        isDarkMode = isDarkMode,
                        onSendSticker = onSendSticker,
                        onCloseDrawer = onToggleStickers
                    )
                }

                AnimatedVisibility(visible = showAttachmentSheet) {
                    TelegramAttachmentSheet(
                        isDarkMode = isDarkMode,
                        onSelectAttachment = onSelectAttachment
                    )
                }
            }
        }
    ) { innerPadding ->
        // Chat Canvas
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(MaterialTheme.colorScheme.background)
        ) {
            LazyColumn(
                state = listState,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 4.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                // Top Date Chip
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 12.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(12.dp))
                                .background(
                                    if (isDarkMode) Color(0x66000000) else Color(0x33000000)
                                )
                                .padding(horizontal = 12.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = "امروز (Today)",
                                color = Color.White,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }

                // Messages list
                items(messages, key = { it.id }) { message ->
                    TelegramChatBubble(
                        message = message,
                        isDarkMode = isDarkMode,
                        isGroup = contact.isGroup,
                        onReply = onReplyToMessage,
                        onReact = onReactMessage,
                        onDelete = onDeleteMessage
                    )
                }

                // Animated typing indicator bubble
                if (isFriendTyping) {
                    item {
                        Row(
                            modifier = Modifier
                                .padding(horizontal = 8.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(16.dp))
                                    .background(
                                        if (isDarkMode) MaterialTheme.colorScheme.surfaceVariant else Color.White
                                    )
                                    .padding(horizontal = 14.dp, vertical = 8.dp)
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Text(
                                        text = "${contact.name} در حال نوشتن است...",
                                        fontSize = 12.sp,
                                        color = TelegramBlue,
                                        fontWeight = FontWeight.Medium
                                    )
                                }
                            }
                        }
                    }
                }

                item {
                    Spacer(modifier = Modifier.height(8.dp))
                }
            }
        }
    }
}
