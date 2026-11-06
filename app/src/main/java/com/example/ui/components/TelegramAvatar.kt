package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.HeadsetMic
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.TelegramBlue
import com.example.ui.theme.TelegramOnlineGreen

@Composable
fun TelegramAvatar(
    name: String,
    avatarColorHex: Long,
    modifier: Modifier = Modifier,
    size: Dp = 50.dp,
    isOnline: Boolean = false,
    isGroup: Boolean = false,
    isSavedMessages: Boolean = false,
    showOnlineBadge: Boolean = true
) {
    Box(
        modifier = modifier.size(size),
        contentAlignment = Alignment.Center
    ) {
        val bgCol = if (isSavedMessages) TelegramBlue else Color(avatarColorHex)

        Box(
            modifier = Modifier
                .size(size)
                .clip(CircleShape)
                .background(bgCol),
            contentAlignment = Alignment.Center
        ) {
            when {
                isSavedMessages -> {
                    Icon(
                        imageVector = Icons.Default.Bookmark,
                        contentDescription = "Saved Messages",
                        tint = Color.White,
                        modifier = Modifier.size(size * 0.55f)
                    )
                }
                isGroup -> {
                    Icon(
                        imageVector = Icons.Default.Group,
                        contentDescription = "Group",
                        tint = Color.White,
                        modifier = Modifier.size(size * 0.55f)
                    )
                }
                name.contains("پشتیبانی") || name.contains("Support") -> {
                    Icon(
                        imageVector = Icons.Default.HeadsetMic,
                        contentDescription = "Support Bot",
                        tint = Color.White,
                        modifier = Modifier.size(size * 0.55f)
                    )
                }
                else -> {
                    val initial = name.trim().take(1).uppercase()
                    if (initial.isNotEmpty()) {
                        Text(
                            text = initial,
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = (size.value * 0.42f).sp
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = name,
                            tint = Color.White,
                            modifier = Modifier.size(size * 0.55f)
                        )
                    }
                }
            }
        }

        if (showOnlineBadge && isOnline && !isSavedMessages && !isGroup) {
            val badgeSize = (size.value * 0.28f).coerceAtLeast(10f).dp
            Box(
                modifier = Modifier
                    .size(badgeSize)
                    .align(Alignment.BottomEnd)
                    .clip(CircleShape)
                    .background(TelegramOnlineGreen)
                    .border(2.dp, MaterialTheme.colorScheme.surface, CircleShape)
            )
        }
    }
}
