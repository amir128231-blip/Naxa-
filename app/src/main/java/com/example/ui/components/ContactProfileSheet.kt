package com.example.ui.components

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
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.NotificationsOff
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.ChatContact
import com.example.ui.theme.TelegramBlue

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ContactProfileSheet(
    contact: ChatContact,
    isDarkMode: Boolean,
    onDismiss: () -> Unit,
    onStartCall: (String) -> Unit,
    onToggleMute: (String) -> Unit,
    onClearChat: (String) -> Unit,
    onDeleteChat: (String) -> Unit,
    onOpenChat: ((String) -> Unit)? = null,
    onToggleOnline: (() -> Unit)? = null
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val clipboardManager = LocalClipboardManager.current
    var copiedTextNotice by remember { mutableStateOf<String?>(null) }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = if (isDarkMode) MaterialTheme.colorScheme.surface else Color(0xFFFFFFFF)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 32.dp)
        ) {
            // Header: Avatar & Name
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 12.dp)
            ) {
                TelegramAvatar(
                    name = contact.name,
                    avatarColorHex = contact.avatarColorHex,
                    size = 90.dp,
                    isOnline = contact.isOnline,
                    isGroup = contact.isGroup,
                    isSavedMessages = contact.isSavedMessages,
                    showOnlineBadge = true
                )
                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    text = contact.name,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(4.dp))
                if (!contact.isSavedMessages && !contact.isGroup) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        modifier = Modifier
                            .clip(RoundedCornerShape(16.dp))
                            .background(if (contact.isOnline) (if (isDarkMode) Color(0x2210B981) else Color(0x1A10B981)) else (if (isDarkMode) Color(0x2264748B) else Color(0x1464748B)))
                            .clickable(enabled = onToggleOnline != null) { onToggleOnline?.invoke() }
                            .padding(horizontal = 10.dp, vertical = 4.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(if (contact.isOnline) Color(0xFF10B981) else Color(0xFF94A3B8))
                        )
                        Text(
                            text = if (contact.isOnline) "آنلاین 🟢 (لمس جهت تغییر)" else "${contact.lastSeen} ⚪ (لمس جهت آنلاین شدن)",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium,
                            color = if (contact.isOnline) (if (isDarkMode) Color(0xFF34D399) else Color(0xFF059669)) else Color.Gray
                        )
                    }
                } else {
                    Text(
                        text = if (contact.isOnline) "آنلاین" else contact.lastSeen,
                        fontSize = 13.sp,
                        color = if (contact.isOnline) TelegramBlue else Color.Gray
                    )
                }
            }

            // Quick Call Actions Row
            if (!contact.isSavedMessages) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    if (onOpenChat != null) {
                        ProfileActionButton(
                            icon = Icons.Default.Chat,
                            label = "ارسال پیام (پیوی)",
                            onClick = {
                                onDismiss()
                                onOpenChat(contact.id)
                            }
                        )
                    }
                    ProfileActionButton(
                        icon = Icons.Default.Call,
                        label = "تماس صوتی",
                        onClick = {
                            onDismiss()
                            onStartCall("audio")
                        }
                    )
                    ProfileActionButton(
                        icon = Icons.Default.Videocam,
                        label = "تماس تصویری",
                        onClick = {
                            onDismiss()
                            onStartCall("video")
                        }
                    )
                    ProfileActionButton(
                        icon = Icons.Default.Share,
                        label = "اشتراک پروفایل",
                        onClick = {
                            clipboardManager.setText(AnnotatedString(contact.username))
                            copiedTextNotice = "نام کاربری کپی شد"
                        }
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))
            HorizontalDivider(
                color = MaterialTheme.colorScheme.outlineVariant,
                thickness = 6.dp
            )
            Spacer(modifier = Modifier.height(8.dp))

            // Details List
            Column(modifier = Modifier.padding(horizontal = 20.dp)) {
                Text(
                    text = "اطلاعات حساب کاربری",
                    color = TelegramBlue,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(vertical = 8.dp)
                )

                if (contact.phone.isNotBlank()) {
                    ProfileInfoRow(
                        title = contact.phone,
                        subtitle = "شماره موبایل",
                        onCopy = {
                            clipboardManager.setText(AnnotatedString(contact.phone))
                            copiedTextNotice = "شماره تلفن کپی شد"
                        }
                    )
                }

                ProfileInfoRow(
                    title = contact.username,
                    subtitle = "نام کاربری",
                    onCopy = {
                        clipboardManager.setText(AnnotatedString(contact.username))
                        copiedTextNotice = "نام کاربری کپی شد"
                    }
                )

                if (contact.bio.isNotBlank()) {
                    ProfileInfoRow(
                        title = contact.bio,
                        subtitle = "بیوگرافی",
                        onCopy = {
                            clipboardManager.setText(AnnotatedString(contact.bio))
                            copiedTextNotice = "بیوگرافی کپی شد"
                        }
                    )
                }

                if (copiedTextNotice != null) {
                    Text(
                        text = "✓ $copiedTextNotice",
                        color = TelegramBlue,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier.padding(vertical = 4.dp)
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Notifications Toggle
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = if (contact.isMuted) Icons.Default.NotificationsOff else Icons.Default.Notifications,
                            contentDescription = "Notifications",
                            tint = if (contact.isMuted) Color.Gray else TelegramBlue,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(16.dp))
                        Column {
                            Text(
                                text = "اعلان‌ها (Notifications)",
                                fontSize = 15.sp,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = if (contact.isMuted) "بی‌صدا" else "فعال",
                                fontSize = 12.sp,
                                color = Color.Gray
                            )
                        }
                    }
                    Switch(
                        checked = !contact.isMuted,
                        onCheckedChange = { onToggleMute(contact.id) },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color.White,
                            checkedTrackColor = TelegramBlue
                        )
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Clear Chat & Delete actions
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .clickable {
                            onClearChat(contact.id)
                            onDismiss()
                        }
                        .padding(vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Clear History",
                        tint = Color.Red,
                        modifier = Modifier.size(22.dp)
                    )
                    Spacer(modifier = Modifier.width(16.dp))
                    Text(
                        text = "پاک کردن تاریخچه گفتگو",
                        color = Color.Red,
                        fontSize = 14.sp
                    )
                }
            }
        }
    }
}

@Composable
private fun ProfileActionButton(
    icon: ImageVector,
    label: String,
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .clip(RoundedCornerShape(12.dp))
            .clickable { onClick() }
            .padding(8.dp)
    ) {
        Box(
            modifier = Modifier
                .size(48.dp)
                .clip(CircleShape)
                .background(TelegramBlue.copy(alpha = 0.12f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = TelegramBlue,
                modifier = Modifier.size(24.dp)
            )
        }
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = label,
            fontSize = 12.sp,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

@Composable
private fun ProfileInfoRow(
    title: String,
    subtitle: String,
    onCopy: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onCopy() }
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                fontSize = 15.sp,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = subtitle,
                fontSize = 12.sp,
                color = Color.Gray
            )
        }
        IconButton(onClick = onCopy) {
            Icon(
                imageVector = Icons.Default.ContentCopy,
                contentDescription = "Copy",
                tint = Color.Gray,
                modifier = Modifier.size(18.dp)
            )
        }
    }
}
