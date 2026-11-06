package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.Brightness4
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Group
import androidx.compose.material3.SmallFloatingActionButton
import androidx.compose.ui.platform.LocalContext
import android.widget.Toast
import com.example.ui.components.TelegramBottomDock
import com.example.ui.components.TelegramNavTab
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.Done
import androidx.compose.material.icons.filled.DoneAll
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Help
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.NotificationsOff
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.PushPin
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.PrimaryScrollableTabRow
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.ChatContact
import com.example.data.remote.FirestoreConversation
import com.example.data.repository.TelegramRepository
import com.example.ui.components.FirestoreConversationList
import com.example.ui.components.TelegramAvatar
import com.example.ui.i18n.LocalAppLanguage
import com.example.ui.i18n.LocalAppStrings
import com.example.ui.theme.TelegramBlue
import com.example.ui.theme.TelegramCheckmarkBlue
import com.example.ui.theme.TelegramUnreadBadge
import com.example.ui.viewmodel.ChatFilterTab

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatListScreen(
    contacts: List<ChatContact>,
    searchQuery: String,
    selectedFilterTab: ChatFilterTab,
    isDarkMode: Boolean,
    onlineStatusText: String = "آنلاین 🌐",
    onOpenDrawer: () -> Unit,
    onSearchChange: (String) -> Unit,
    onSelectFilter: (ChatFilterTab) -> Unit,
    onSelectChat: (String) -> Unit,
    onOpenAddFriend: () -> Unit,
    onOpenNewGroup: () -> Unit,
    onOpenOnlineRooms: () -> Unit,
    onOpenContactsList: () -> Unit = {},
    onTogglePin: (String) -> Unit,
    onToggleMute: (String) -> Unit,
    onDeleteChat: (String) -> Unit,
    firestoreConversations: List<FirestoreConversation> = emptyList(),
    isFirestoreLoading: Boolean = false,
    firestoreErrorMessage: String? = null,
    onRefreshFirestore: () -> Unit = {},
    onCreateFirestoreChat: () -> Unit = {},
    onSelectFirestoreConversation: (FirestoreConversation) -> Unit = {},
    currentBottomTab: TelegramNavTab = TelegramNavTab.CHATS,
    onTabSelected: (TelegramNavTab) -> Unit = {},
    userInitial: String = "A",
    onToggleTheme: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    var isSearchActive by remember { mutableStateOf(false) }
    var showMoreMenu by remember { mutableStateOf(false) }
    val strings = LocalAppStrings.current
    val context = LocalContext.current

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(if (isDarkMode) Color(0xFF17212B) else TelegramBlue)
            ) {
                // Top App Bar matching Screenshot 3
                TopAppBar(
                    title = {
                        if (isSearchActive) {
                            TextField(
                                value = searchQuery,
                                onValueChange = onSearchChange,
                                placeholder = {
                                    Text(
                                        text = strings.searchPlaceholder,
                                        color = Color.White.copy(alpha = 0.7f),
                                        fontSize = 16.sp
                                    )
                                },
                                colors = TextFieldDefaults.colors(
                                    focusedContainerColor = Color.Transparent,
                                    unfocusedContainerColor = Color.Transparent,
                                    disabledContainerColor = Color.Transparent,
                                    focusedIndicatorColor = Color.Transparent,
                                    unfocusedIndicatorColor = Color.Transparent,
                                    cursorColor = Color.White,
                                    focusedTextColor = Color.White,
                                    unfocusedTextColor = Color.White
                                ),
                                singleLine = true,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("search_chats_input")
                            )
                        } else {
                            Text(
                                text = "نکسا",
                                color = Color.White,
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.testTag("main_app_title")
                            )
                        }
                    },
                    navigationIcon = {
                        if (isSearchActive) {
                            IconButton(
                                onClick = {
                                    isSearchActive = false
                                    onSearchChange("")
                                },
                                modifier = Modifier.testTag("close_search_button")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = "Close Search",
                                    tint = Color.White
                                )
                            }
                        } else {
                            IconButton(
                                onClick = onOpenDrawer,
                                modifier = Modifier.testTag("drawer_menu_button")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Menu,
                                    contentDescription = "Menu",
                                    tint = Color.White
                                )
                            }
                        }
                    },
                    actions = {
                        if (!isSearchActive) {
                            IconButton(
                                onClick = { isSearchActive = true },
                                modifier = Modifier.testTag("search_chats_button")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Search,
                                    contentDescription = "Search",
                                    tint = Color.White
                                )
                            }
                            Box {
                                IconButton(
                                    onClick = { showMoreMenu = true },
                                    modifier = Modifier.testTag("chats_more_menu_button")
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.MoreVert,
                                        contentDescription = "More Options",
                                        tint = Color.White
                                    )
                                }
                                DropdownMenu(
                                    expanded = showMoreMenu,
                                    onDismissRequest = { showMoreMenu = false }
                                ) {
                                    DropdownMenuItem(
                                        text = { Text("گروه جدید") },
                                        leadingIcon = { Icon(Icons.Default.Group, contentDescription = null) },
                                        onClick = {
                                            showMoreMenu = false
                                            onOpenNewGroup()
                                        }
                                    )
                                    DropdownMenuItem(
                                        text = { Text("مخاطبین") },
                                        leadingIcon = { Icon(Icons.Default.People, contentDescription = null) },
                                        onClick = {
                                            showMoreMenu = false
                                            onOpenContactsList()
                                        }
                                    )
                                    DropdownMenuItem(
                                        text = { Text("پیام‌های ذخیره شده") },
                                        leadingIcon = { Icon(Icons.Default.Bookmark, contentDescription = null) },
                                        onClick = {
                                            showMoreMenu = false
                                            onSelectChat("saved_messages")
                                        }
                                    )
                                    DropdownMenuItem(
                                        text = { Text("اتاق‌های گفتگوی آنلاین") },
                                        leadingIcon = { Icon(Icons.Default.Language, contentDescription = null) },
                                        onClick = {
                                            showMoreMenu = false
                                            onOpenOnlineRooms()
                                        }
                                    )
                                    DropdownMenuItem(
                                        text = { Text("کانال‌ها و گفتگوهای ابری") },
                                        leadingIcon = { Icon(Icons.Default.CloudDone, contentDescription = null) },
                                        onClick = {
                                            showMoreMenu = false
                                            onSelectFilter(ChatFilterTab.CHANNELS)
                                        }
                                    )
                                    DropdownMenuItem(
                                        text = { Text(if (isDarkMode) "تم روشن" else "تم تیره") },
                                        leadingIcon = { Icon(Icons.Default.Brightness4, contentDescription = null) },
                                        onClick = {
                                            showMoreMenu = false
                                            onToggleTheme()
                                        }
                                    )
                                }
                            }
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = if (isDarkMode) Color(0xFF17212B) else TelegramBlue
                    )
                )

                // Category Filter Tabs
                PrimaryScrollableTabRow(
                    selectedTabIndex = selectedFilterTab.ordinal,
                    edgePadding = 8.dp,
                    containerColor = if (isDarkMode) Color(0xFF17212B) else TelegramBlue,
                    contentColor = if (isDarkMode) Color(0xFF2AABEE) else Color.White,
                    divider = {
                        HorizontalDivider(
                            color = if (isDarkMode) Color(0xFF1B232E) else Color.Transparent,
                            thickness = 1.dp
                        )
                    }
                ) {
                    ChatFilterTab.values().forEach { tab ->
                        val isSelected = selectedFilterTab == tab
                        val tabTitle = when (tab) {
                            ChatFilterTab.ALL -> strings.tabAll
                            ChatFilterTab.PRIVATE -> strings.tabPersonal
                            ChatFilterTab.GROUPS -> strings.tabGroups
                            ChatFilterTab.CHANNELS -> "کانال‌ها 📢"
                            ChatFilterTab.UNREAD -> strings.tabUnread
                        }
                        Tab(
                            selected = isSelected,
                            onClick = { onSelectFilter(tab) },
                            text = {
                                Text(
                                    text = tabTitle,
                                    fontSize = 14.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                    color = if (isSelected) {
                                        if (isDarkMode) Color(0xFF2AABEE) else Color.White
                                    } else {
                                        if (isDarkMode) Color(0xFF8E8E93) else Color.White.copy(alpha = 0.7f)
                                    }
                                )
                            }
                        )
                    }
                }
            }
        },
        floatingActionButton = {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.padding(bottom = 72.dp)
            ) {
                // Camera Small FAB (Screenshot 3)
                SmallFloatingActionButton(
                    onClick = {
                        Toast.makeText(context, "دوربین استوری نکسا", Toast.LENGTH_SHORT).show()
                    },
                    containerColor = if (isDarkMode) Color(0xFF1E2834) else Color(0xFFF1F5F9),
                    contentColor = if (isDarkMode) Color.White else Color(0xFF1E2834),
                    shape = CircleShape,
                    modifier = Modifier
                        .size(42.dp)
                        .testTag("camera_quick_fab")
                ) {
                    Icon(
                        imageVector = Icons.Default.CameraAlt,
                        contentDescription = "Camera",
                        modifier = Modifier.size(20.dp)
                    )
                }

                // Main Edit/Compose FAB - Open Contacts
                FloatingActionButton(
                    onClick = onOpenContactsList,
                    containerColor = if (isDarkMode) Color(0xFF2481CC) else TelegramBlue,
                    contentColor = Color.White,
                    shape = CircleShape,
                    modifier = Modifier
                        .size(56.dp)
                        .testTag("contacts_pencil_fab")
                ) {
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = "مخاطبین",
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
        },
        bottomBar = {
            TelegramBottomDock(
                currentTab = currentBottomTab,
                onTabSelected = onTabSelected,
                userInitial = userInitial,
                isDarkMode = isDarkMode
            )
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(MaterialTheme.colorScheme.background)
        ) {
            if (selectedFilterTab == ChatFilterTab.CHANNELS) {
                FirestoreConversationList(
                    conversations = firestoreConversations,
                    isLoading = isFirestoreLoading,
                    errorMessage = firestoreErrorMessage,
                    isDarkMode = isDarkMode,
                    onSelectConversation = onSelectFirestoreConversation,
                    onRefresh = onRefreshFirestore,
                    onCreateNewConversation = onCreateFirestoreChat
                )
            } else {
                Column(modifier = Modifier.fillMaxSize()) {
                    // Search bar at the top of the message list component to filter displayed conversations based on content or contact name
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp, vertical = 8.dp)
                            .testTag("conversation_search_bar")
                            .testTag("message_list_search_bar"),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant
                        ),
                        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                    ) {
                        OutlinedTextField(
                            value = searchQuery,
                            onValueChange = onSearchChange,
                            placeholder = {
                                Text(
                                    text = "جستجوی گفتگو بر اساس نام مخاطب یا متن پیام...",
                                    fontSize = 13.5.sp,
                                    color = Color.Gray
                                )
                            },
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Default.Search,
                                    contentDescription = "Search Conversations",
                                    tint = if (searchQuery.isNotBlank()) TelegramBlue else Color.Gray,
                                    modifier = Modifier.size(20.dp)
                                )
                            },
                            trailingIcon = {
                                if (searchQuery.isNotBlank()) {
                                    IconButton(
                                        onClick = { onSearchChange("") },
                                        modifier = Modifier.testTag("clear_conversation_search_button")
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Close,
                                            contentDescription = "Clear Search",
                                            tint = Color.Gray,
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                }
                            },
                            singleLine = true,
                            shape = RoundedCornerShape(12.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = MaterialTheme.colorScheme.primary,
                                unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant,
                                focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                                unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                                focusedTextColor = MaterialTheme.colorScheme.onSurface,
                                unfocusedTextColor = MaterialTheme.colorScheme.onSurface
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("conversation_search_input")
                                .testTag("search_conversations_input")
                        )
                    }

                    if (contacts.isEmpty()) {
                        // Empty state
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(32.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Text(
                                text = if (searchQuery.isNotBlank()) "گفتگویی مطابق با «$searchQuery» یافت نشد 🔍" else "گفتگویی یافت نشد 💬",
                                fontSize = 17.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = if (searchQuery.isNotBlank()) "نام مخاطب یا متن پیام را مجدداً بررسی کنید." else "برای شروع گفتگو، از لیست مخاطبین یا افزودن دوست استفاده کنید.",
                                fontSize = 13.5.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Button(
                                onClick = onOpenContactsList,
                                colors = ButtonDefaults.buttonColors(containerColor = TelegramBlue)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.People,
                                    contentDescription = "Contacts",
                                    tint = Color.White,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("مشاهده مخاطبین و شروع پیوی")
                            }
                        }
                    } else {
                        LazyColumn(
                            modifier = Modifier
                                .fillMaxSize()
                                .testTag("conversations_list")
                        ) {
                            items(contacts, key = { it.id }) { contact ->
                                ChatItemRow(
                                    contact = contact,
                                    isDarkMode = isDarkMode,
                                    onClick = { onSelectChat(contact.id) },
                                    onTogglePin = { onTogglePin(contact.id) },
                                    onToggleMute = { onToggleMute(contact.id) },
                                    onDelete = { onDeleteChat(contact.id) }
                                )
                                HorizontalDivider(
                                    modifier = Modifier.padding(start = 76.dp),
                                    color = MaterialTheme.colorScheme.outlineVariant,
                                    thickness = 0.8.dp
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ChatItemRow(
    contact: ChatContact,
    isDarkMode: Boolean,
    onClick: () -> Unit,
    onTogglePin: () -> Unit,
    onToggleMute: () -> Unit,
    onDelete: () -> Unit
) {
    var showMenu by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(horizontal = 12.dp, vertical = 8.dp)
            .testTag("chat_item_${contact.id}")
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Avatar
            TelegramAvatar(
                name = contact.name,
                avatarColorHex = contact.avatarColorHex,
                size = 54.dp,
                isOnline = contact.isOnline,
                isGroup = contact.isGroup,
                isSavedMessages = contact.isSavedMessages,
                showOnlineBadge = true
            )

            Spacer(modifier = Modifier.width(12.dp))

            // Main Text Info
            Column(modifier = Modifier.weight(1f)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(
                            text = contact.name,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurface,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        if (contact.isOnline && !contact.isGroup && !contact.isSavedMessages) {
                            Spacer(modifier = Modifier.width(6.dp))
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(if (isDarkMode) Color(0x2210B981) else Color(0x1A10B981))
                                    .padding(horizontal = 5.dp, vertical = 1.dp)
                            ) {
                                Text(
                                    text = "آنلاین 🟢",
                                    fontSize = 10.sp,
                                    color = if (isDarkMode) Color(0xFF34D399) else Color(0xFF059669),
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    // Timestamp
                    Text(
                        text = TelegramRepository.formatTimestamp(contact.lastMessageTime),
                        fontSize = 12.sp,
                        color = Color.Gray
                    )
                }

                Spacer(modifier = Modifier.height(3.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        modifier = Modifier.weight(1f),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        if (contact.lastMessageIsOutgoing) {
                            val isRead = contact.lastMessageStatus == "READ"
                            Icon(
                                imageVector = if (isRead) Icons.Default.DoneAll else Icons.Default.Done,
                                contentDescription = if (isRead) "خوانده شده" else "ارسال شده",
                                tint = if (isRead) {
                                    if (isDarkMode) Color(0xFF34D399) else Color(0xFF10B981)
                                } else {
                                    Color.Gray
                                },
                                modifier = Modifier
                                    .size(15.dp)
                                    .testTag(if (isRead) "chat_item_double_check_${contact.id}" else "chat_item_single_check_${contact.id}")
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                        }
                        Text(
                            text = contact.lastMessageText.ifBlank { "گفتگو خالی است" },
                            fontSize = 14.sp,
                            color = if (contact.unreadCount > 0) MaterialTheme.colorScheme.onSurface else Color.Gray,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }

                    Spacer(modifier = Modifier.width(6.dp))

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        if (contact.isMuted) {
                            Icon(
                                imageVector = Icons.Default.NotificationsOff,
                                contentDescription = "Muted",
                                tint = Color.Gray,
                                modifier = Modifier.size(16.dp)
                            )
                        }

                        if (contact.isPinned) {
                            Icon(
                                imageVector = Icons.Default.PushPin,
                                contentDescription = "Pinned",
                                tint = TelegramBlue,
                                modifier = Modifier.size(16.dp)
                            )
                        }

                        if (contact.unreadCount > 0) {
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(TelegramUnreadBadge)
                                    .padding(horizontal = 7.dp, vertical = 2.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = contact.unreadCount.toString(),
                                    color = Color.White,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }

                        // Options Menu Trigger
                        IconButton(
                            onClick = { showMenu = true },
                            modifier = Modifier.size(24.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.MoreVert,
                                contentDescription = "Options",
                                tint = Color.Gray,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }
            }
        }

        // Dropdown Menu for Pin, Mute, Delete
        DropdownMenu(
            expanded = showMenu,
            onDismissRequest = { showMenu = false }
        ) {
            DropdownMenuItem(
                text = { Text(if (contact.isPinned) "برداشتن سنجاق (Unpin)" else "سنجاق کردن (Pin)") },
                onClick = {
                    onTogglePin()
                    showMenu = false
                }
            )
            DropdownMenuItem(
                text = { Text(if (contact.isMuted) "صدادار کردن (Unmute)" else "بی‌صدا کردن (Mute)") },
                onClick = {
                    onToggleMute()
                    showMenu = false
                }
            )
            DropdownMenuItem(
                text = { Text("حذف گفتگو (Delete)", color = Color.Red) },
                onClick = {
                    onDelete()
                    showMenu = false
                }
            )
        }
    }
}
