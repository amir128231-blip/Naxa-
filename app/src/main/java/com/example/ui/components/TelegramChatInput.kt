package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.AttachFile
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Mood
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
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
import com.example.data.model.ChatMessage
import com.example.ui.i18n.LocalAppStrings
import com.example.ui.theme.TelegramBlue
import java.util.Locale

@Composable
fun TelegramChatInput(
    replyingTo: ChatMessage?,
    isRecordingVoice: Boolean,
    recordingSeconds: Int,
    isDarkMode: Boolean,
    onSendMessage: (String) -> Unit,
    onStartVoiceRecording: () -> Unit,
    onFinishVoiceRecording: () -> Unit,
    onCancelVoiceRecording: () -> Unit,
    onCancelReply: () -> Unit,
    onToggleStickers: () -> Unit,
    onToggleAttachments: () -> Unit,
    modifier: Modifier = Modifier
) {
    var text by remember { mutableStateOf("") }
    val strings = LocalAppStrings.current

    val infiniteTransition = rememberInfiniteTransition(label = "mic_pulse")
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(600),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseAlpha"
    )

    Surface(
        modifier = modifier.fillMaxWidth(),
        color = if (isDarkMode) MaterialTheme.colorScheme.surface else Color(0xFFFFFFFF),
        shadowElevation = 4.dp
    ) {
        Column {
            // Replying To Preview Bar
            AnimatedVisibility(visible = replyingTo != null) {
                if (replyingTo != null) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(if (isDarkMode) MaterialTheme.colorScheme.surfaceVariant else Color(0xFFF0F4F8))
                            .padding(horizontal = 12.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .width(3.dp)
                                .height(32.dp)
                                .background(if (isDarkMode) MaterialTheme.colorScheme.primary else TelegramBlue, RoundedCornerShape(2.dp))
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "پاسخ به ${replyingTo.senderName}",
                                color = if (isDarkMode) MaterialTheme.colorScheme.primary else TelegramBlue,
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp
                            )
                            Text(
                                text = replyingTo.text,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                fontSize = 12.sp,
                                maxLines = 1
                            )
                        }
                        IconButton(onClick = onCancelReply) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Cancel Reply",
                                tint = Color.Gray,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }
            }

            // Input Bar Content (Message Composition Area)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 6.dp, vertical = 6.dp)
                    .testTag("message_composition_area"),
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (isRecordingVoice) {
                    // Voice recording active bar
                    Row(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(24.dp))
                            .background(if (isDarkMode) MaterialTheme.colorScheme.surfaceVariant else Color(0xFFF0F4F8))
                            .padding(horizontal = 14.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Red blinking recording dot
                        Box(
                            modifier = Modifier
                                .size(10.dp)
                                .clip(CircleShape)
                                .background(Color.Red.copy(alpha = pulseAlpha))
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        val mins = recordingSeconds / 60
                        val secs = recordingSeconds % 60
                        Text(
                            text = String.format(Locale.US, "%02d:%02d", mins, secs),
                            fontWeight = FontWeight.Bold,
                            color = Color.Red,
                            fontSize = 14.sp
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = "در حال ضبط ویس...",
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontSize = 13.sp,
                            modifier = Modifier.weight(1f)
                        )
                        Text(
                            text = strings.cancelText,
                            color = Color.Gray,
                            fontSize = 13.sp,
                            modifier = Modifier
                                .clickable { onCancelVoiceRecording() }
                                .padding(4.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(6.dp))

                    // Stop and Send Voice button
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .clip(CircleShape)
                            .background(TelegramBlue)
                            .clickable { onFinishVoiceRecording() }
                            .testTag("finish_voice_button"),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.Send,
                            contentDescription = "Send Voice",
                            tint = Color.White,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                } else {
                    // Normal typing bar
                    IconButton(
                        onClick = onToggleAttachments,
                        modifier = Modifier.size(40.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.AttachFile,
                            contentDescription = "Attachments",
                            tint = Color(0xFF8E8E93),
                            modifier = Modifier.size(24.dp)
                        )
                    }

                    // Input Text Field
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(24.dp))
                            .background(if (isDarkMode) MaterialTheme.colorScheme.surfaceVariant else Color(0xFFF4F6F8))
                            .padding(horizontal = 4.dp),
                        contentAlignment = Alignment.CenterStart
                    ) {
                        TextField(
                            value = text,
                            onValueChange = { text = it },
                            placeholder = {
                                Text(
                                    text = strings.chatInputPlaceholder,
                                    color = if (isDarkMode) MaterialTheme.colorScheme.onSurfaceVariant else Color(0xFF8E8E93),
                                    fontSize = 15.sp
                                )
                            },
                            colors = TextFieldDefaults.colors(
                                focusedContainerColor = Color.Transparent,
                                unfocusedContainerColor = Color.Transparent,
                                disabledContainerColor = Color.Transparent,
                                focusedIndicatorColor = Color.Transparent,
                                unfocusedIndicatorColor = Color.Transparent,
                                focusedTextColor = MaterialTheme.colorScheme.onSurface,
                                unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
                                cursorColor = if (isDarkMode) MaterialTheme.colorScheme.primary else TelegramBlue
                            ),
                            trailingIcon = {
                                IconButton(
                                    onClick = onToggleStickers,
                                    modifier = Modifier
                                        .size(36.dp)
                                        .testTag("sticker_button")
                                        .testTag("stickers_toggle_button")
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Mood,
                                        contentDescription = "Stickers",
                                        tint = Color(0xFF8E8E93),
                                        modifier = Modifier.size(22.dp)
                                    )
                                }
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("chat_text_input")
                                .testTag("message_input_text_field")
                        )
                    }

                    Spacer(modifier = Modifier.width(6.dp))

                    // Send Button or Voice Record Button
                    if (text.isNotBlank()) {
                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .clip(CircleShape)
                                .background(if (isDarkMode) MaterialTheme.colorScheme.primary else TelegramBlue)
                                .clickable {
                                    val textToSend = text.trim()
                                    text = "" // Updates local text state immediately
                                    onSendMessage(textToSend) // Dispatches to local database state
                                }
                                .testTag("send_message_button")
                                .testTag("message_send_button"),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.Send,
                                contentDescription = "Send",
                                tint = if (isDarkMode) MaterialTheme.colorScheme.onPrimary else Color.White,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    } else {
                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .clip(CircleShape)
                                .background(if (isDarkMode) MaterialTheme.colorScheme.surfaceVariant else Color(0xFFF0F4F8))
                                .clickable { onStartVoiceRecording() }
                                .testTag("mic_record_button"),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Mic,
                                contentDescription = "Record Voice",
                                tint = if (isDarkMode) MaterialTheme.colorScheme.primary else TelegramBlue,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}
