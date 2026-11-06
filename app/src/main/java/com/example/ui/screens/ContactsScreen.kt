package com.example.ui.screens

import android.content.Intent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Campaign
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.GroupAdd
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.PhoneAndroid
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.ChatContact
import com.example.ui.components.TelegramAvatar
import com.example.ui.i18n.LocalAppStrings
import com.example.ui.theme.TelegramBlue
import com.example.ui.theme.TelegramOnlineGreen

private val AVATAR_COLOR_PALETTE = listOf(
    0xFF2AABEE, // Telegram Sky Blue
    0xFF00BFA5, // Emerald Green
    0xFF9C27B0, // Royal Purple
    0xFFE91E63, // Neon Rose
    0xFFFF9800, // Amber Orange
    0xFF3F51B5, // Indigo
    0xFFE53935  // Crimson
)

enum class ContactFilterOption {
    ALL,
    ONLINE,
    RECENT
}

/**
 * Dedicated Telegram-style Contacts & Friends List Screen
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ContactsScreen(
    contacts: List<ChatContact>,
    isDarkMode: Boolean,
    onBack: () -> Unit,
    onSelectContact: (String) -> Unit,
    onStartCall: (String, ChatContact) -> Unit = { _, _ -> },
    onAddFriend: (name: String, username: String, phone: String, bio: String, avatarColor: Long) -> Unit,
    onDeleteFriend: (String) -> Unit = {},
    onSyncDeviceContacts: () -> Unit = {},
    isSyncingDeviceContacts: Boolean = false,
    onOpenNewGroup: () -> Unit = {},
    onOpenNewChannel: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    var searchQuery by remember { mutableStateOf("") }
    var showAddFriendDialog by remember { mutableStateOf(false) }
    var filterOption by remember { mutableStateOf(ContactFilterOption.ALL) }
    var sortAlphabetical by remember { mutableStateOf(true) }

    val strings = LocalAppStrings.current
    val context = LocalContext.current

    // Only filter personal individual contacts (exclude groups and cloud saved messages)
    val personalContacts = remember(contacts) {
        contacts.filter { !it.isGroup && !it.isSavedMessages }
    }

    val onlineCount = remember(personalContacts) {
        personalContacts.count { it.isOnline }
    }

    val filteredContacts by remember(personalContacts, searchQuery, filterOption, sortAlphabetical) {
        derivedStateOf {
            var list = personalContacts.filter { contact ->
                val matchesFilter = when (filterOption) {
                    ContactFilterOption.ALL -> true
                    ContactFilterOption.ONLINE -> contact.isOnline
                    ContactFilterOption.RECENT -> contact.lastMessageText.isNotBlank()
                }

                val matchesSearch = if (searchQuery.isBlank()) {
                    true
                } else {
                    contact.name.contains(searchQuery, ignoreCase = true) ||
                            contact.username.contains(searchQuery, ignoreCase = true) ||
                            contact.phone.contains(searchQuery, ignoreCase = true)
                }

                matchesFilter && matchesSearch
            }

            if (sortAlphabetical) {
                list = list.sortedBy { it.name.trim() }
            } else {
                list = list.sortedByDescending { it.lastMessageTime }
            }
            list
        }
    }

    // Grouping by first letter for indexed list
    val groupedContacts by remember(filteredContacts) {
        derivedStateOf {
            filteredContacts.groupBy { contact ->
                val firstChar = contact.name.trim().firstOrNull()?.uppercaseChar() ?: '#'
                if (firstChar.isLetter()) firstChar.toString() else "#"
            }
        }
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = if (isDarkMode) MaterialTheme.colorScheme.background else Color(0xFFF4F4F5),
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "مخاطبین",
                            color = Color.White,
                            fontSize = 19.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "${personalContacts.size} مخاطب • $onlineCount آنلاین",
                            color = Color.White.copy(alpha = 0.85f),
                            fontSize = 12.sp
                        )
                    }
                },
                navigationIcon = {
                    IconButton(
                        onClick = onBack,
                        modifier = Modifier.testTag("contacts_back_button")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = Color.White
                        )
                    }
                },
                actions = {
                    IconButton(
                        onClick = { sortAlphabetical = !sortAlphabetical },
                        modifier = Modifier.testTag("contacts_sort_toggle")
                    ) {
                        Icon(
                            imageVector = Icons.Default.FilterList,
                            contentDescription = "Sort Contacts",
                            tint = if (sortAlphabetical) Color.White else Color(0xFFFFD54F)
                        )
                    }
                    IconButton(
                        onClick = { showAddFriendDialog = true },
                        modifier = Modifier.testTag("contacts_add_friend_top_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.PersonAdd,
                            contentDescription = "Add Friend",
                            tint = Color.White
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = if (isDarkMode) MaterialTheme.colorScheme.surface else TelegramBlue
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddFriendDialog = true },
                containerColor = if (isDarkMode) MaterialTheme.colorScheme.primary else TelegramBlue,
                contentColor = if (isDarkMode) MaterialTheme.colorScheme.onPrimary else Color.White,
                shape = CircleShape,
                modifier = Modifier
                    .padding(8.dp)
                    .testTag("add_friend_fab")
            ) {
                Icon(
                    imageVector = Icons.Default.PersonAdd,
                    contentDescription = "افزودن مخاطب جدید",
                    modifier = Modifier.size(24.dp)
                )
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Search Bar at the Top of Contacts List
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp, vertical = 8.dp),
                shape = RoundedCornerShape(12.dp),
                color = if (isDarkMode) MaterialTheme.colorScheme.surfaceVariant else Color.White,
                shadowElevation = 1.dp
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 2.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = "جستجوی مخاطبین",
                        tint = if (searchQuery.isNotBlank()) TelegramBlue else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    TextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        placeholder = {
                            Text(
                                text = "جستجوی مخاطب با نام...",
                                fontSize = 14.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                            )
                        },
                        singleLine = true,
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = Color.Transparent,
                            unfocusedContainerColor = Color.Transparent,
                            disabledContainerColor = Color.Transparent,
                            focusedIndicatorColor = Color.Transparent,
                            unfocusedIndicatorColor = Color.Transparent,
                            cursorColor = TelegramBlue
                        ),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("contacts_top_search_bar")
                    )
                    if (searchQuery.isNotBlank()) {
                        IconButton(
                            onClick = { searchQuery = "" },
                            modifier = Modifier
                                .size(28.dp)
                                .testTag("clear_contacts_search")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Clear,
                                contentDescription = "پاک کردن",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }
            }

            if (searchQuery.isNotBlank()) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 2.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "یافته‌ها: ${filteredContacts.size} مخاطب",
                        fontSize = 12.5.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = TelegramBlue
                    )
                    Text(
                        text = "عبارت: «$searchQuery»",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            // Filter Pills Row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(if (isDarkMode) MaterialTheme.colorScheme.surface else Color.White)
                    .padding(horizontal = 16.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                FilterChip(
                    selected = filterOption == ContactFilterOption.ALL,
                    onClick = { filterOption = ContactFilterOption.ALL },
                    label = { Text("همه (${personalContacts.size})", fontSize = 12.5.sp) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = TelegramBlue.copy(alpha = 0.15f),
                        selectedLabelColor = TelegramBlue
                    )
                )
                FilterChip(
                    selected = filterOption == ContactFilterOption.ONLINE,
                    onClick = { filterOption = ContactFilterOption.ONLINE },
                    label = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(7.dp)
                                    .clip(CircleShape)
                                    .background(TelegramOnlineGreen)
                            )
                            Spacer(modifier = Modifier.width(5.dp))
                            Text("آنلاین‌ها ($onlineCount)", fontSize = 12.5.sp)
                        }
                    },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = TelegramOnlineGreen.copy(alpha = 0.15f),
                        selectedLabelColor = TelegramOnlineGreen
                    )
                )
                FilterChip(
                    selected = filterOption == ContactFilterOption.RECENT,
                    onClick = { filterOption = ContactFilterOption.RECENT },
                    label = { Text("گفتگوهای اخیر", fontSize = 12.5.sp) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = TelegramBlue.copy(alpha = 0.15f),
                        selectedLabelColor = TelegramBlue
                    )
                )
            }

            HorizontalDivider(
                color = if (isDarkMode) MaterialTheme.colorScheme.outlineVariant else Color(0xFFE0E0E0),
                thickness = 0.8.dp
            )

            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(bottom = 80.dp)
            ) {
                // Top Quick Action Rows (only visible when not searching)
                if (searchQuery.isBlank()) {
                    item {
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 12.dp, vertical = 8.dp),
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = if (isDarkMode) MaterialTheme.colorScheme.surfaceVariant else Color.White
                            ),
                            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                        ) {
                            Column(modifier = Modifier.fillMaxWidth()) {
                                // New Group Item
                                ContactActionRow(
                                    icon = Icons.Default.GroupAdd,
                                    iconColor = Color(0xFF0088CC),
                                    title = "گروه جدید",
                                    subtitle = "ایجاد گروه برای گفتگوی گروهی با دوستان",
                                    onClick = onOpenNewGroup,
                                    testTag = "action_new_group_row"
                                )

                                HorizontalDivider(
                                    modifier = Modifier.padding(horizontal = 56.dp),
                                    color = if (isDarkMode) MaterialTheme.colorScheme.outlineVariant else Color(0xFFF0F0F0)
                                )

                                // New Channel Item
                                ContactActionRow(
                                    icon = Icons.Default.Campaign,
                                    iconColor = Color(0xFFFF9500),
                                    title = "کانال جدید",
                                    subtitle = "ایجاد کانال عمومی برای انتشار پیام و رسانه",
                                    onClick = onOpenNewChannel,
                                    testTag = "action_new_channel_row"
                                )

                                HorizontalDivider(
                                    modifier = Modifier.padding(horizontal = 56.dp),
                                    color = if (isDarkMode) MaterialTheme.colorScheme.outlineVariant else Color(0xFFF0F0F0)
                                )

                                // Sync Device Contacts Item
                                ContactActionRow(
                                    icon = Icons.Default.PhoneAndroid,
                                    iconColor = Color(0xFF4CAF50),
                                    title = "همگام‌سازی مخاطبین گوشی",
                                    subtitle = if (isSyncingDeviceContacts) "در حال دریافت مخاطبین دستگاه..." else "افزودن خودکار مخاطبین دفترچه تلفن",
                                    isLoading = isSyncingDeviceContacts,
                                    onClick = onSyncDeviceContacts,
                                    testTag = "action_sync_contacts_row"
                                )

                                HorizontalDivider(
                                    modifier = Modifier.padding(horizontal = 56.dp),
                                    color = if (isDarkMode) MaterialTheme.colorScheme.outlineVariant else Color(0xFFF0F0F0)
                                )

                                // Add Contact Item
                                ContactActionRow(
                                    icon = Icons.Default.PersonAdd,
                                    iconColor = Color(0xFF9C27B0),
                                    title = "افزودن مخاطب جدید",
                                    subtitle = "ثبت دستی با نام کاربری یا شماره موبایل",
                                    onClick = { showAddFriendDialog = true },
                                    testTag = "action_add_friend_row"
                                )
                            }
                        }
                    }
                }

                // Empty State
                if (filteredContacts.isEmpty()) {
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 48.dp, horizontal = 24.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(72.dp)
                                        .clip(CircleShape)
                                        .background(TelegramBlue.copy(alpha = 0.12f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Person,
                                        contentDescription = null,
                                        tint = TelegramBlue,
                                        modifier = Modifier.size(36.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.height(14.dp))
                                Text(
                                    text = if (searchQuery.isNotBlank()) "مخاطبی با این مشخصات یافت نشد" else "هنوز دوستی اضافه نشده است",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 15.sp,
                                    color = if (isDarkMode) Color.White else Color(0xFF222222)
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = if (searchQuery.isNotBlank()) "عبارت دیگری را جستجو کنید یا دوست جدیدی اضافه نمایید." else "با دکمه زیر اولین دوست خود را به لیست مخاطبین بیفزایید.",
                                    fontSize = 12.5.sp,
                                    color = Color.Gray,
                                    textAlign = TextAlign.Center
                                )
                                Spacer(modifier = Modifier.height(16.dp))
                                Button(
                                    onClick = { showAddFriendDialog = true },
                                    colors = ButtonDefaults.buttonColors(containerColor = TelegramBlue),
                                    shape = RoundedCornerShape(10.dp),
                                    modifier = Modifier.testTag("empty_state_add_friend_btn")
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.PersonAdd,
                                        contentDescription = null,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("افزودن اولین دوست", fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                } else {
                    // Grouped contacts items
                    groupedContacts.forEach { (sectionHeader, contactGroup) ->
                        item(key = "header_$sectionHeader") {
                            SectionHeaderBadge(letter = sectionHeader, isDarkMode = isDarkMode)
                        }

                        items(contactGroup, key = { it.id }) { contact ->
                            ContactListItemCard(
                                contact = contact,
                                isDarkMode = isDarkMode,
                                onClick = { onSelectContact(contact.id) },
                                onStartAudioCall = { onStartCall("audio", contact) },
                                onStartVideoCall = { onStartCall("video", contact) },
                                onDelete = { onDeleteFriend(contact.id) }
                            )
                        }
                    }
                }
            }
        }
    }

    // Add Friend Dialog / Mechanism
    if (showAddFriendDialog) {
        AddFriendMechanismDialog(
            isDarkMode = isDarkMode,
            onDismiss = { showAddFriendDialog = false },
            onAddFriend = { name, username, phone, bio, avatarColor, openChat ->
                onAddFriend(name, username, phone, bio, avatarColor)
                showAddFriendDialog = false
            }
        )
    }
}

/**
 * Top action row for contacts menu (Add Friend, Sync, Invite)
 */
@Composable
private fun ContactActionRow(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    iconColor: Color,
    title: String,
    subtitle: String,
    isLoading: Boolean = false,
    onClick: () -> Unit,
    testTag: String
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(enabled = !isLoading, onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 12.dp)
            .testTag(testTag),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(iconColor.copy(alpha = 0.15f)),
            contentAlignment = Alignment.Center
        ) {
            if (isLoading) {
                CircularProgressIndicator(
                    color = iconColor,
                    strokeWidth = 2.dp,
                    modifier = Modifier.size(20.dp)
                )
            } else {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = iconColor,
                    modifier = Modifier.size(22.dp)
                )
            }
        }

        Spacer(modifier = Modifier.width(14.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                fontSize = 14.5.sp,
                fontWeight = FontWeight.Bold,
                color = iconColor
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = subtitle,
                fontSize = 11.5.sp,
                color = Color.Gray,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

/**
 * Alphabet Section Header
 */
@Composable
private fun SectionHeaderBadge(letter: String, isDarkMode: Boolean) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(6.dp))
                .background(if (isDarkMode) MaterialTheme.colorScheme.surfaceVariant else Color(0xFFE3F2FD))
                .padding(horizontal = 10.dp, vertical = 3.dp)
        ) {
            Text(
                text = letter,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = if (isDarkMode) MaterialTheme.colorScheme.primary else TelegramBlue
            )
        }
        Spacer(modifier = Modifier.width(8.dp))
        HorizontalDivider(
            modifier = Modifier.weight(1f),
            color = if (isDarkMode) MaterialTheme.colorScheme.outlineVariant else Color(0xFFE0E0E0),
            thickness = 0.5.dp
        )
    }
}

/**
 * Individual Contact Item Card
 */
@Composable
private fun ContactListItemCard(
    contact: ChatContact,
    isDarkMode: Boolean,
    onClick: () -> Unit,
    onStartAudioCall: () -> Unit,
    onStartVideoCall: () -> Unit,
    onDelete: () -> Unit
) {
    val clipboardManager = LocalClipboardManager.current
    var showMenu by remember { mutableStateOf(false) }

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .testTag("contact_item_${contact.id}"),
        color = Color.Transparent
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Avatar with built-in online badge
            TelegramAvatar(
                name = contact.name,
                avatarColorHex = contact.avatarColorHex,
                size = 46.dp,
                isOnline = contact.isOnline,
                showOnlineBadge = true
            )

            Spacer(modifier = Modifier.width(14.dp))

            // Info Column
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = contact.name,
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                    color = if (isDarkMode) Color.White else Color(0xFF1E1E1E),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(2.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = if (contact.isOnline) "آنلاین" else contact.lastSeen,
                        fontSize = 12.sp,
                        color = if (contact.isOnline) TelegramOnlineGreen else Color.Gray
                    )

                    if (contact.username.isNotBlank()) {
                        Text(
                            text = " • ${contact.username}",
                            fontSize = 11.5.sp,
                            color = TelegramBlue.copy(alpha = 0.85f),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

                if (contact.phone.isNotBlank()) {
                    Text(
                        text = contact.phone,
                        fontSize = 11.sp,
                        color = Color.Gray.copy(alpha = 0.8f)
                    )
                }
            }

            // Quick Call Actions
            IconButton(
                onClick = onStartAudioCall,
                modifier = Modifier
                    .size(36.dp)
                    .testTag("call_audio_${contact.id}")
            ) {
                Icon(
                    imageVector = Icons.Default.Call,
                    contentDescription = "Voice Call",
                    tint = TelegramBlue,
                    modifier = Modifier.size(19.dp)
                )
            }

            IconButton(
                onClick = onStartVideoCall,
                modifier = Modifier
                    .size(36.dp)
                    .testTag("call_video_${contact.id}")
            ) {
                Icon(
                    imageVector = Icons.Default.Videocam,
                    contentDescription = "Video Call",
                    tint = TelegramBlue,
                    modifier = Modifier.size(20.dp)
                )
            }

            // Dropdown Menu for more actions
            Box {
                IconButton(
                    onClick = { showMenu = true },
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.MoreVert,
                        contentDescription = "More Options",
                        tint = Color.Gray,
                        modifier = Modifier.size(18.dp)
                    )
                }

                DropdownMenu(
                    expanded = showMenu,
                    onDismissRequest = { showMenu = false }
                ) {
                    DropdownMenuItem(
                        text = { Text("کپی شماره تلفن") },
                        leadingIcon = {
                            Icon(Icons.Default.ContentCopy, contentDescription = null, modifier = Modifier.size(18.dp))
                        },
                        onClick = {
                            clipboardManager.setText(AnnotatedString(contact.phone))
                            showMenu = false
                        }
                    )
                    DropdownMenuItem(
                        text = { Text("کپی نام کاربری") },
                        leadingIcon = {
                            Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(18.dp))
                        },
                        onClick = {
                            clipboardManager.setText(AnnotatedString(contact.username))
                            showMenu = false
                        }
                    )
                    DropdownMenuItem(
                        text = { Text("حذف مخاطب", color = Color(0xFFE53935)) },
                        leadingIcon = {
                            Icon(Icons.Default.Delete, contentDescription = null, tint = Color(0xFFE53935), modifier = Modifier.size(18.dp))
                        },
                        onClick = {
                            onDelete()
                            showMenu = false
                        }
                    )
                }
            }
        }
    }
}

/**
 * Dedicated Mechanism to Add New Friends
 */
@Composable
fun AddFriendMechanismDialog(
    isDarkMode: Boolean,
    onDismiss: () -> Unit,
    onAddFriend: (name: String, username: String, phone: String, bio: String, avatarColor: Long, openChat: Boolean) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var username by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var bio by remember { mutableStateOf("") }
    var selectedColor by remember { mutableStateOf(AVATAR_COLOR_PALETTE[0]) }
    var openChatImmediately by remember { mutableStateOf(true) }

    var nameError by remember { mutableStateOf(false) }
    var phoneError by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Box(
                    modifier = Modifier
                        .size(38.dp)
                        .clip(CircleShape)
                        .background(TelegramBlue.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.PersonAdd,
                        contentDescription = null,
                        tint = TelegramBlue,
                        modifier = Modifier.size(22.dp)
                    )
                }
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Text(
                        text = "افزودن دوست جدید",
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "اطلاعات مخاطب را برای افزودن به لیست دوستان وارد کنید",
                        fontSize = 11.5.sp,
                        color = Color.Gray
                    )
                }
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Live Avatar Preview with color chooser
                Box(contentAlignment = Alignment.Center) {
                    TelegramAvatar(
                        name = if (name.isNotBlank()) name else "؟",
                        avatarColorHex = selectedColor,
                        size = 64.dp
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Color Selection Palette
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    AVATAR_COLOR_PALETTE.forEach { colorHex ->
                        val isSelected = selectedColor == colorHex
                        Box(
                            modifier = Modifier
                                .size(26.dp)
                                .clip(CircleShape)
                                .background(Color(colorHex))
                                .clickable { selectedColor = colorHex }
                                .border(
                                    width = if (isSelected) 2.5.dp else 0.dp,
                                    color = if (isSelected) Color.White else Color.Transparent,
                                    shape = CircleShape
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            if (isSelected) {
                                Icon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(14.dp)
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Name Input
                OutlinedTextField(
                    value = name,
                    onValueChange = {
                        name = it
                        if (nameError && it.isNotBlank()) nameError = false
                    },
                    label = { Text("نام و نام خانوادگی *") },
                    placeholder = { Text("مثال: مریم کریمی") },
                    singleLine = true,
                    isError = nameError,
                    supportingText = if (nameError) {
                        { Text("وارد کردن نام الزامی است", color = MaterialTheme.colorScheme.error) }
                    } else null,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("add_friend_name_input"),
                    shape = RoundedCornerShape(10.dp)
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Phone Number Input
                OutlinedTextField(
                    value = phone,
                    onValueChange = {
                        phone = it
                        if (phoneError && it.isNotBlank()) phoneError = false
                    },
                    label = { Text("شماره همراه *") },
                    placeholder = { Text("09123456789 یا +989123456789") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                    singleLine = true,
                    isError = phoneError,
                    supportingText = if (phoneError) {
                        { Text("شماره همراه را به درستی وارد کنید", color = MaterialTheme.colorScheme.error) }
                    } else null,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("add_friend_phone_input"),
                    shape = RoundedCornerShape(10.dp)
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Username Input
                OutlinedTextField(
                    value = username,
                    onValueChange = { username = it },
                    label = { Text("نام کاربری (آیدی)") },
                    placeholder = { Text("مثال: maryam_k@") },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("add_friend_username_input"),
                    shape = RoundedCornerShape(10.dp)
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Bio / Note Input
                OutlinedTextField(
                    value = bio,
                    onValueChange = { bio = it },
                    label = { Text("بیوگرافی یا یادداشت (اختیاری)") },
                    placeholder = { Text("مثال: همکار، دوست دانشگاه") },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("add_friend_bio_input"),
                    shape = RoundedCornerShape(10.dp)
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val trimmedName = name.trim()
                    val trimmedPhone = phone.trim()

                    if (trimmedName.isBlank()) {
                        nameError = true
                        return@Button
                    }
                    if (trimmedPhone.isBlank()) {
                        phoneError = true
                        return@Button
                    }

                    val cleanUsername = if (username.isBlank()) {
                        "@user_${System.currentTimeMillis() % 10000}"
                    } else if (username.startsWith("@")) {
                        username.trim()
                    } else {
                        "@${username.trim()}"
                    }

                    onAddFriend(
                        trimmedName,
                        cleanUsername,
                        trimmedPhone,
                        bio.trim(),
                        selectedColor,
                        openChatImmediately
                    )
                },
                colors = ButtonDefaults.buttonColors(containerColor = TelegramBlue),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.testTag("submit_add_friend_btn")
            ) {
                Text("افزودن و ذخیره", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss,
                modifier = Modifier.testTag("cancel_add_friend_btn")
            ) {
                Text("انصراف", color = Color.Gray)
            }
        }
    )
}
