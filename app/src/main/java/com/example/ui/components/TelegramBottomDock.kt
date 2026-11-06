package com.example.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.ChatBubble
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

enum class TelegramNavTab(val labelFa: String, val testTag: String) {
    CHATS("گفتگوها", "nav_tab_chats"),
    CONTACTS("مخاطبین", "nav_tab_contacts"),
    SETTINGS("تنظیمات", "nav_tab_settings"),
    PROFILE("پروفایل", "nav_tab_profile")
}

@Composable
fun TelegramBottomDock(
    currentTab: TelegramNavTab,
    onTabSelected: (TelegramNavTab) -> Unit,
    userInitial: String = "A",
    isDarkMode: Boolean = true,
    modifier: Modifier = Modifier
) {
    // Exact Telegram Android floating pill dock
    val containerBg = if (isDarkMode) Color(0xFF17212B) else Color.White
    val borderColor = if (isDarkMode) Color(0xFF242F3D).copy(alpha = 0.6f) else Color(0xFFE2E8F0)

    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 10.dp),
        contentAlignment = Alignment.Center
    ) {
        Surface(
            modifier = Modifier
                .widthIn(max = 420.dp)
                .fillMaxWidth()
                .height(64.dp)
                .shadow(elevation = 10.dp, shape = CircleShape)
                .border(width = 0.8.dp, color = borderColor, shape = CircleShape),
            shape = CircleShape,
            color = containerBg
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp),
                horizontalArrangement = Arrangement.SpaceAround,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Tab 1: Chats (گفتگوها)
                BottomDockItem(
                    tab = TelegramNavTab.CHATS,
                    isSelected = currentTab == TelegramNavTab.CHATS,
                    icon = Icons.Default.ChatBubble,
                    isDarkMode = isDarkMode,
                    onClick = { onTabSelected(TelegramNavTab.CHATS) }
                )

                // Tab 2: Contacts (مخاطبین)
                BottomDockItem(
                    tab = TelegramNavTab.CONTACTS,
                    isSelected = currentTab == TelegramNavTab.CONTACTS,
                    icon = Icons.Default.AccountCircle,
                    isDarkMode = isDarkMode,
                    onClick = { onTabSelected(TelegramNavTab.CONTACTS) }
                )

                // Tab 3: Settings (تنظیمات)
                BottomDockItem(
                    tab = TelegramNavTab.SETTINGS,
                    isSelected = currentTab == TelegramNavTab.SETTINGS,
                    icon = Icons.Default.Settings,
                    isDarkMode = isDarkMode,
                    onClick = { onTabSelected(TelegramNavTab.SETTINGS) }
                )

                // Tab 4: Profile (پروفایل)
                BottomDockProfileItem(
                    isSelected = currentTab == TelegramNavTab.PROFILE,
                    userInitial = userInitial,
                    isDarkMode = isDarkMode,
                    onClick = { onTabSelected(TelegramNavTab.PROFILE) }
                )
            }
        }
    }
}

@Composable
private fun BottomDockItem(
    tab: TelegramNavTab,
    isSelected: Boolean,
    icon: ImageVector,
    isDarkMode: Boolean,
    onClick: () -> Unit
) {
    val activePillColor = if (isDarkMode) Color(0xFF1E3A52) else Color(0xFFE3F2FD)
    val activeIconTint = if (isDarkMode) Color(0xFF2AABEE) else Color(0xFF2481CC)
    val inactiveIconTint = if (isDarkMode) Color(0xFF8E8E93) else Color(0xFF707579)

    val pillBackground by animateColorAsState(
        targetValue = if (isSelected) activePillColor else Color.Transparent,
        animationSpec = tween(durationMillis = 200),
        label = "pillBg"
    )

    Column(
        modifier = Modifier
            .clip(RoundedCornerShape(16.dp))
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick
            )
            .testTag(tab.testTag)
            .padding(horizontal = 6.dp, vertical = 4.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier
                .size(width = 46.dp, height = 28.dp)
                .clip(RoundedCornerShape(14.dp))
                .background(pillBackground),
            contentAlignment = Alignment.Center
        ) {
            androidx.compose.material3.Icon(
                imageVector = icon,
                contentDescription = tab.labelFa,
                tint = if (isSelected) activeIconTint else inactiveIconTint,
                modifier = Modifier.size(20.dp)
            )
        }

        Text(
            text = tab.labelFa,
            fontSize = 11.sp,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
            color = if (isSelected) {
                if (isDarkMode) Color(0xFF2AABEE) else Color(0xFF2481CC)
            } else {
                inactiveIconTint
            },
            modifier = Modifier.padding(top = 1.dp)
        )
    }
}

@Composable
private fun BottomDockProfileItem(
    isSelected: Boolean,
    userInitial: String,
    isDarkMode: Boolean,
    onClick: () -> Unit
) {
    val activePillColor = if (isDarkMode) Color(0xFF1E3A52) else Color(0xFFE3F2FD)
    val inactiveLabelColor = if (isDarkMode) Color(0xFF8E8E93) else Color(0xFF707579)

    val pillBackground by animateColorAsState(
        targetValue = if (isSelected) activePillColor else Color.Transparent,
        animationSpec = tween(durationMillis = 200),
        label = "profilePillBg"
    )

    Column(
        modifier = Modifier
            .clip(RoundedCornerShape(16.dp))
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick
            )
            .testTag(TelegramNavTab.PROFILE.testTag)
            .padding(horizontal = 6.dp, vertical = 4.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier
                .size(width = 46.dp, height = 28.dp)
                .clip(RoundedCornerShape(14.dp))
                .background(pillBackground),
            contentAlignment = Alignment.Center
        ) {
            Box(
                modifier = Modifier
                    .size(22.dp)
                    .clip(CircleShape)
                    .background(Color(0xFF38B2AC)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = userInitial.take(1).uppercase(),
                    color = Color.White,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        Text(
            text = TelegramNavTab.PROFILE.labelFa,
            fontSize = 11.sp,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
            color = if (isSelected) {
                if (isDarkMode) Color(0xFF2AABEE) else Color(0xFF2481CC)
            } else {
                inactiveLabelColor
            },
            modifier = Modifier.padding(top = 1.dp)
        )
    }
}
