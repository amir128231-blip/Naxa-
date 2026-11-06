package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Done
import androidx.compose.material.icons.filled.DoneAll
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Reply
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.ChatMessage
import com.example.data.model.MessageType
import com.example.ui.i18n.LocalAppStrings
import com.example.ui.theme.TelegramBlue
import com.example.ui.theme.TelegramBubbleIncomingDark
import com.example.ui.theme.TelegramBubbleIncomingLight
import com.example.ui.theme.TelegramBubbleOutgoingDark
import com.example.ui.theme.TelegramBubbleOutgoingLight
import com.example.ui.theme.TelegramCheckmarkBlue
import com.example.ui.theme.TelegramCheckmarkGreen
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun TelegramChatBubble(
    message: ChatMessage,
    isDarkMode: Boolean,
    isGroup: Boolean,
    onReply: (ChatMessage) -> Unit,
    onReact: (Long, String) -> Unit,
    onDelete: (Long) -> Unit,
    modifier: Modifier = Modifier
) {
    var showActionDialog by remember { mutableStateOf(false) }
    var isVoicePlaying by remember { mutableStateOf(false) }
    val clipboardManager = LocalClipboardManager.current
    val strings = LocalAppStrings.current

    val haptic = LocalHapticFeedback.current

    val isOutgoing = message.isOutgoing
    val alignment = if (isOutgoing) Alignment.End else Alignment.Start

    val isSticker = message.messageType == MessageType.STICKER.name

    val bubbleBg = when {
        isSticker -> Color.Transparent
        isOutgoing -> if (isDarkMode) TelegramBubbleOutgoingDark else TelegramBubbleOutgoingLight
        else -> if (isDarkMode) TelegramBubbleIncomingDark else TelegramBubbleIncomingLight
    }

    val bubbleShape = when {
        isSticker -> RoundedCornerShape(20.dp)
        isOutgoing -> RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp, bottomStart = 16.dp, bottomEnd = 4.dp)
        else -> RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp, bottomStart = 4.dp, bottomEnd = 16.dp)
    }

    val formattedTime = remember(message.timestamp) {
        val sdf = SimpleDateFormat("HH:mm", Locale.getDefault())
        sdf.format(Date(message.timestamp))
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp, vertical = 2.dp),
        horizontalAlignment = alignment
    ) {
        Box(
            modifier = Modifier
                .widthIn(min = 70.dp, max = 320.dp)
                .clip(bubbleShape)
                .background(bubbleBg)
                .testTag("chat_bubble_${message.id}")
                .pointerInput(message.id) {
                    detectTapGestures(
                        onLongPress = {
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            showActionDialog = true
                        },
                        onTap = {
                            if (showActionDialog) {
                                showActionDialog = false
                            }
                        }
                    )
                }
                .padding(horizontal = 10.dp, vertical = 6.dp)
        ) {
            Column {
                // Sender name for groups (incoming messages)
                if (isGroup && !isOutgoing) {
                    Text(
                        text = message.senderName,
                        color = TelegramBlue,
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp,
                        modifier = Modifier.padding(bottom = 2.dp)
                    )
                }

                // Reply preview if replying to another message
                if (message.replyToText != null) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 4.dp)
                            .clip(RoundedCornerShape(6.dp))
                            .background(
                                if (isDarkMode) Color(0x33000000) else Color(0x1A000000)
                            )
                            .padding(4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .width(3.dp)
                                .height(28.dp)
                                .background(TelegramBlue, RoundedCornerShape(2.dp))
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Column {
                            Text(
                                text = message.replyToSender ?: "پیام",
                                color = TelegramBlue,
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 11.sp
                            )
                            Text(
                                text = message.replyToText,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                fontSize = 11.sp,
                                maxLines = 1
                            )
                        }
                    }
                }

                // Content based on message type
                when (message.messageType) {
                    MessageType.VOICE.name -> {
                        VoiceMessageContent(
                            durationSeconds = message.voiceDurationSeconds,
                            isPlaying = isVoicePlaying,
                            isOutgoing = isOutgoing,
                            isDarkMode = isDarkMode,
                            onTogglePlay = { isVoicePlaying = !isVoicePlaying }
                        )
                    }
                    MessageType.STICKER.name -> {
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier
                                .padding(vertical = 4.dp)
                                .align(if (isOutgoing) Alignment.End else Alignment.Start)
                        ) {
                            Text(
                                text = message.stickerEmoji ?: message.text,
                                fontSize = 64.sp,
                                modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                            )
                        }
                    }
                    else -> {
                        Text(
                            text = message.text,
                            color = if (isOutgoing) {
                                if (isDarkMode) Color.White else Color(0xFF0F172A)
                            } else {
                                MaterialTheme.colorScheme.onSurface
                            },
                            fontSize = 15.sp,
                            lineHeight = 20.sp,
                            modifier = Modifier.padding(bottom = 2.dp)
                        )
                    }
                }

                // Time and read receipts row
                if (isSticker) {
                    Box(
                        modifier = Modifier
                            .align(Alignment.End)
                            .clip(RoundedCornerShape(10.dp))
                            .background(Color(0x77000000))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(3.dp)
                        ) {
                            Text(
                                text = formattedTime,
                                fontSize = 10.sp,
                                color = Color.White
                            )

                            if (isOutgoing) {
                                val isRead = message.status == "READ"
                                val icon = if (isRead) Icons.Default.DoneAll else Icons.Default.Done
                                val tint = if (isRead) Color(0xFF34D399) else Color(0xFFCBD5E1)
                                Icon(
                                    imageVector = icon,
                                    contentDescription = if (isRead) "خوانده شده (تیک دوم)" else "ارسال شده (تیک اول)",
                                    tint = tint,
                                    modifier = Modifier
                                        .size(14.dp)
                                        .testTag(if (isRead) "read_receipt_double_check_${message.id}" else "sent_receipt_single_check_${message.id}")
                                )
                            }
                        }
                    }
                } else {
                    Row(
                        modifier = Modifier.align(Alignment.End),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(3.dp)
                    ) {
                        Text(
                            text = formattedTime,
                            fontSize = 11.sp,
                            color = if (isOutgoing) {
                                if (isDarkMode) Color(0xFFC7D2FE) else Color(0xFF6B7280)
                            } else {
                                if (isDarkMode) MaterialTheme.colorScheme.onSurfaceVariant else Color(0xFF707579)
                            }
                        )

                        if (isOutgoing) {
                            val isRead = message.status == "READ"
                            val icon = if (isRead) Icons.Default.DoneAll else Icons.Default.Done
                            val tint = if (isRead) {
                                if (isDarkMode) Color(0xFF34D399) else Color(0xFF10B981)
                            } else {
                                if (isDarkMode) Color(0xFF94A3B8) else Color(0xFF64748B)
                            }
                            Icon(
                                imageVector = icon,
                                contentDescription = if (isRead) "خوانده شده (تیک دوم)" else "ارسال شده (تیک اول)",
                                tint = tint,
                                modifier = Modifier
                                    .size(15.dp)
                                    .testTag(if (isRead) "read_receipt_double_check_${message.id}" else "sent_receipt_single_check_${message.id}")
                            )
                        }
                    }
                }
            }
        }

        // Reaction Badge if present
        if (!message.reaction.isNullOrBlank()) {
            Box(
                modifier = Modifier
                    .padding(top = 3.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(
                        if (isDarkMode) Color(0xFF1E2C3A) else Color(0xFFE8F2FA)
                    )
                    .border(
                        width = 1.dp,
                        color = if (isDarkMode) TelegramBlue.copy(alpha = 0.4f) else TelegramBlue.copy(alpha = 0.3f),
                        shape = RoundedCornerShape(12.dp)
                    )
                    .pointerInput(message.id, message.reaction) {
                        detectTapGestures(
                            onTap = { onReact(message.id, message.reaction) },
                            onLongPress = {
                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                showActionDialog = true
                            }
                        )
                    }
                    .testTag("reaction_badge_${message.id}")
                    .padding(horizontal = 8.dp, vertical = 3.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = message.reaction,
                        fontSize = 14.sp
                    )
                    Text(
                        text = "1",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isDarkMode) Color(0xFF90CAF9) else TelegramBlue
                    )
                    Icon(
                        imageVector = Icons.Default.CloudDone,
                        contentDescription = "Synced via Firestore",
                        tint = if (isDarkMode) Color(0xFF64B5F6) else TelegramBlue,
                        modifier = Modifier.size(12.dp)
                    )
                }
            }
        }

        // Quick Reaction / Action bar popup when long pressed
        AnimatedVisibility(
            visible = showActionDialog,
            enter = fadeIn() + scaleIn(initialScale = 0.85f),
            exit = fadeOut() + scaleOut(targetScale = 0.85f)
        ) {
            val uniqueReactions = remember {
                listOf("❤️", "👍", "👎", "🔥", "👏", "😂", "🎉", "😍", "🤩", "⚡", "🚀", "😢", "🙏", "💯", "🕊️", "💎", "🤯", "🤝")
            }

            Surface(
                modifier = Modifier
                    .padding(top = 6.dp, bottom = 4.dp)
                    .widthIn(max = 330.dp)
                    .testTag("reaction_picker_menu_${message.id}"),
                shape = RoundedCornerShape(20.dp),
                color = if (isDarkMode) Color(0xFF1C2733) else Color(0xFFFFFFFF),
                shadowElevation = 8.dp,
                tonalElevation = 4.dp,
                border = androidx.compose.foundation.BorderStroke(
                    0.5.dp,
                    if (isDarkMode) Color(0x33FFFFFF) else Color(0x1F000000)
                )
            ) {
                Column(modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp)) {
                    // Title with cloud sync indicator and close button
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 6.dp, start = 4.dp, end = 2.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Text(
                                text = "واکنش به پیام",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = if (isDarkMode) Color(0xFFB0BEC5) else Color(0xFF707579)
                            )
                            Icon(
                                imageVector = Icons.Default.CloudDone,
                                contentDescription = "Firestore Sync",
                                tint = TelegramBlue,
                                modifier = Modifier.size(13.dp)
                            )
                            Text(
                                text = "سند ابری",
                                fontSize = 10.sp,
                                color = TelegramBlue,
                                fontWeight = FontWeight.Medium
                            )
                        }
                        IconButton(
                            onClick = { showActionDialog = false },
                            modifier = Modifier
                                .size(24.dp)
                                .testTag("close_reaction_menu")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Close",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }

                    // Unique emoji reactions scrollable row
                    LazyRow(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 2.dp),
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        items(uniqueReactions) { emoji ->
                            val isSelected = message.reaction == emoji
                            Box(
                                contentAlignment = Alignment.Center,
                                modifier = Modifier
                                    .size(42.dp)
                                    .clip(CircleShape)
                                    .background(
                                        when {
                                            isSelected -> TelegramBlue.copy(alpha = 0.25f)
                                            else -> Color.Transparent
                                        }
                                    )
                                    .border(
                                        width = if (isSelected) 1.5.dp else 0.dp,
                                        color = if (isSelected) TelegramBlue else Color.Transparent,
                                        shape = CircleShape
                                    )
                                    .clickable {
                                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                        onReact(message.id, emoji)
                                        showActionDialog = false
                                    }
                                    .testTag("reaction_option_${emoji}")
                            ) {
                                Text(
                                    text = emoji,
                                    fontSize = 24.sp
                                )
                            }
                        }
                    }

                    if (isOutgoing) {
                        val isRead = message.status == "READ"
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 6.dp, vertical = 4.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (isRead) (if (isDarkMode) Color(0x2210B981) else Color(0x1A10B981)) else (if (isDarkMode) Color(0x2264748B) else Color(0x1464748B)))
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Icon(
                                imageVector = if (isRead) Icons.Default.DoneAll else Icons.Default.Done,
                                contentDescription = null,
                                tint = if (isRead) (if (isDarkMode) Color(0xFF34D399) else Color(0xFF10B981)) else Color(0xFF94A3B8),
                                modifier = Modifier.size(15.dp)
                            )
                            Text(
                                text = if (isRead) "خوانده شده توسط مخاطب (تیک دوم ✓✓)" else "تحویل به سرور، منتظر خواندن (تیک اول ✓)",
                                fontSize = 11.sp,
                                color = if (isRead) (if (isDarkMode) Color(0xFF34D399) else Color(0xFF047857)) else MaterialTheme.colorScheme.onSurfaceVariant,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(6.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(0.5.dp)
                            .background(if (isDarkMode) Color(0x33FFFFFF) else Color(0x1F000000))
                    )
                    Spacer(modifier = Modifier.height(6.dp))

                    // Action buttons (Reply, Copy, Delete)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceAround,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .clickable {
                                    onReply(message)
                                    showActionDialog = false
                                }
                                .padding(horizontal = 8.dp, vertical = 6.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Reply,
                                contentDescription = "Reply",
                                tint = if (isDarkMode) MaterialTheme.colorScheme.primary else TelegramBlue,
                                modifier = Modifier.size(16.dp)
                            )
                            Text(
                                text = strings.replyTitle,
                                fontSize = 12.sp,
                                color = if (isDarkMode) MaterialTheme.colorScheme.primary else TelegramBlue,
                                fontWeight = FontWeight.SemiBold
                            )
                        }

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .clickable {
                                    clipboardManager.setText(AnnotatedString(message.text))
                                    showActionDialog = false
                                }
                                .padding(horizontal = 8.dp, vertical = 6.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.ContentCopy,
                                contentDescription = "Copy",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(16.dp)
                            )
                            Text(
                                text = strings.copyTitle,
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurface,
                                fontWeight = FontWeight.Medium
                            )
                        }

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .clickable {
                                    onDelete(message.id)
                                    showActionDialog = false
                                }
                                .padding(horizontal = 8.dp, vertical = 6.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Delete,
                                contentDescription = "Delete",
                                tint = Color(0xFFEF5350),
                                modifier = Modifier.size(16.dp)
                            )
                            Text(
                                text = strings.deleteTitle,
                                fontSize = 12.sp,
                                color = Color(0xFFEF5350),
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun VoiceMessageContent(
    durationSeconds: Int,
    isPlaying: Boolean,
    isOutgoing: Boolean,
    isDarkMode: Boolean,
    onTogglePlay: () -> Unit
) {
    val durationText = remember(durationSeconds) {
        val mins = durationSeconds / 60
        val secs = durationSeconds % 60
        String.format(Locale.US, "%d:%02d", mins, secs)
    }

    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
    ) {
        // Play / Pause Circle button
        Box(
            modifier = Modifier
                .size(38.dp)
                .clip(CircleShape)
                .background(if (isDarkMode) MaterialTheme.colorScheme.primary else TelegramBlue)
                .clickable { onTogglePlay() },
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                contentDescription = if (isPlaying) "Pause" else "Play",
                tint = if (isDarkMode) MaterialTheme.colorScheme.onPrimary else Color.White,
                modifier = Modifier.size(22.dp)
            )
        }

        Spacer(modifier = Modifier.width(8.dp))

        Column(modifier = Modifier.weight(1f)) {
            // Waveform bar visualizer
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                val heights = listOf(8, 14, 20, 10, 18, 24, 12, 16, 22, 10, 15, 20, 14, 8, 18, 12)
                heights.forEachIndexed { index, h ->
                    val isPlayed = isPlaying && index < 8
                    Box(
                        modifier = Modifier
                            .width(3.dp)
                            .height(h.dp)
                            .background(
                                if (isPlayed) TelegramBlue else Color.Gray.copy(alpha = 0.5f),
                                RoundedCornerShape(1.5.dp)
                            )
                    )
                }
            }

            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = durationText,
                fontSize = 11.sp,
                color = if (isDarkMode) Color(0xFFA0A5AA) else Color(0xFF707579)
            )
        }
    }
}
