package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.HeadsetMic
import androidx.compose.material.icons.filled.Help
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.filled.PhoneAndroid
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.VerifiedUser
import androidx.compose.material.icons.filled.Share
import com.google.firebase.auth.FirebaseUser
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.NavigationDrawerItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.ui.i18n.LocalAppLanguage
import com.example.ui.i18n.LocalAppStrings
import com.example.ui.theme.TelegramBlue
import com.example.ui.viewmodel.CurrentUserProfile

@Composable
fun TelegramDrawerContent(
    userProfile: CurrentUserProfile,
    isDarkMode: Boolean,
    firebaseUser: FirebaseUser? = null,
    onToggleDarkMode: () -> Unit,
    onOpenSavedMessages: () -> Unit,
    onOpenAddFriend: () -> Unit,
    onOpenNewGroup: () -> Unit,
    onOpenOnlineRooms: () -> Unit,
    onOpenSettings: () -> Unit,
    onOpenLanguageSelection: () -> Unit,
    onOpenSupportBot: () -> Unit,
    onOpenPhoneAuth: () -> Unit = {},
    onSignOut: () -> Unit = {},
    onOpenContactsList: () -> Unit = {},
    onSyncDeviceContacts: () -> Unit = {},
    onCloseDrawer: () -> Unit
) {
    val strings = LocalAppStrings.current
    val currentLang = LocalAppLanguage.current
    ModalDrawerSheet(
        modifier = Modifier.width(300.dp),
        drawerContainerColor = if (isDarkMode) MaterialTheme.colorScheme.surface else Color.White
    ) {
        Column(
            modifier = Modifier
                .fillMaxHeight()
                .fillMaxWidth()
        ) {
            // Profile Info Header Card
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(if (isDarkMode) MaterialTheme.colorScheme.surfaceVariant else TelegramBlue)
                    .clickable {
                        onCloseDrawer()
                        onOpenSettings()
                    }
                    .padding(horizontal = 16.dp, vertical = 20.dp)
            ) {
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.Top
                    ) {
                        // User Profile Picture / Avatar
                        val profilePicUri = userProfile.avatarUri ?: firebaseUser?.photoUrl?.toString()
                        if (!profilePicUri.isNullOrBlank()) {
                            AsyncImage(
                                model = profilePicUri,
                                contentDescription = "Profile Picture",
                                contentScale = ContentScale.Crop,
                                modifier = Modifier
                                    .size(64.dp)
                                    .clip(CircleShape)
                            )
                        } else {
                            Box(
                                modifier = Modifier
                                    .size(64.dp)
                                    .clip(CircleShape)
                                    .background(Color.White.copy(alpha = 0.25f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = userProfile.name.take(1).ifEmpty { "N" },
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 26.sp
                                )
                            }
                        }

                        // Dark mode toggle button
                        IconButton(onClick = onToggleDarkMode) {
                            Icon(
                                imageVector = if (isDarkMode) Icons.Default.LightMode else Icons.Default.DarkMode,
                                contentDescription = "Toggle Dark Mode",
                                tint = Color.White
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = userProfile.name,
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 17.sp
                        )
                        if (firebaseUser != null) {
                            Spacer(modifier = Modifier.width(6.dp))
                            Icon(
                                imageVector = Icons.Default.VerifiedUser,
                                contentDescription = "Nexta Verified",
                                tint = Color(0xFF4CAF50),
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(2.dp))
                    val identifier = firebaseUser?.email ?: firebaseUser?.phoneNumber ?: userProfile.email ?: userProfile.phone
                    Text(
                        text = "$identifier • ${userProfile.username}",
                        color = Color.White.copy(alpha = 0.8f),
                        fontSize = 13.sp
                    )
                    if (firebaseUser != null || userProfile.isGoogleUser) {
                        val authNote = if (userProfile.isGoogleUser || firebaseUser?.email != null) {
                            "متصل به حساب رسمی نکستا ✓"
                        } else {
                            "تایید شده در نکستا ✓"
                        }
                        Text(
                            text = authNote,
                            color = Color.White.copy(alpha = 0.9f),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Navigation Items
            DrawerMenuItem(
                icon = Icons.Default.Bookmark,
                label = strings.drawerSavedMessages,
                onClick = {
                    onCloseDrawer()
                    onOpenSavedMessages()
                }
            )

            DrawerMenuItem(
                icon = Icons.Default.Group,
                label = strings.drawerNewGroup,
                onClick = {
                    onCloseDrawer()
                    onOpenNewGroup()
                }
            )

            DrawerMenuItem(
                icon = Icons.Default.PersonAdd,
                label = strings.drawerAddFriend,
                onClick = {
                    onCloseDrawer()
                    onOpenAddFriend()
                }
            )

            DrawerMenuItem(
                icon = Icons.Default.People,
                label = strings.drawerContacts,
                onClick = {
                    onCloseDrawer()
                    onOpenContactsList()
                }
            )

            DrawerMenuItem(
                icon = Icons.Default.PhoneAndroid,
                label = strings.drawerSyncDeviceContacts,
                onClick = {
                    onCloseDrawer()
                    onSyncDeviceContacts()
                }
            )

            DrawerMenuItem(
                icon = Icons.Default.Call,
                label = strings.drawerCalls,
                onClick = { onCloseDrawer() }
            )

            HorizontalDivider(
                modifier = Modifier.padding(vertical = 8.dp),
                color = MaterialTheme.colorScheme.outlineVariant
            )

            DrawerMenuItem(
                icon = Icons.Default.Language,
                label = "${strings.drawerLanguage} • ${currentLang.flagEmoji} ${currentLang.nativeName}",
                onClick = {
                    onCloseDrawer()
                    onOpenLanguageSelection()
                }
            )

            DrawerMenuItem(
                icon = Icons.Default.Settings,
                label = strings.drawerSettings,
                onClick = {
                    onCloseDrawer()
                    onOpenSettings()
                }
            )

            // Account / Sign out item
            val isUserSignedIn = firebaseUser != null || userProfile.phone.isNotBlank()
            val signIdentifier = firebaseUser?.phoneNumber ?: userProfile.phone
            if (isUserSignedIn) {
                DrawerMenuItem(
                    icon = Icons.Default.ExitToApp,
                    label = "خروج از حساب کاربری ($signIdentifier)",
                    onClick = {
                        onCloseDrawer()
                        onSignOut()
                    }
                )
            }

            DrawerMenuItem(
                icon = Icons.Default.Share,
                label = strings.drawerInviteFriends,
                onClick = { onCloseDrawer() }
            )

            Spacer(modifier = Modifier.weight(1f))

            // App Version Footer
            Text(
                text = "${strings.appTitle} Messenger\nv2.4.0 • ارتباط سریع، امن و پایدار",
                color = Color.Gray,
                fontSize = 11.sp,
                lineHeight = 16.sp,
                modifier = Modifier.padding(16.dp)
            )
        }
    }
}

@Composable
private fun DrawerMenuItem(
    icon: ImageVector,
    label: String,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            tint = Color.Gray,
            modifier = Modifier.size(24.dp)
        )
        Spacer(modifier = Modifier.width(16.dp))
        Text(
            text = label,
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}
