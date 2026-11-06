package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.BookmarkBorder
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.model.StickerItem
import com.example.data.model.StickerPack
import com.example.data.model.TelegramStickersCatalog
import com.example.ui.theme.TelegramBlue

@Composable
fun TelegramStickerSheet(
    isDarkMode: Boolean,
    onSendSticker: (String) -> Unit,
    onCloseDrawer: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    var selectedPackId by remember { mutableStateOf<String>("recent") }
    var searchQuery by remember { mutableStateOf("") }
    var isSearchActive by remember { mutableStateOf(false) }
    var previewSticker by remember { mutableStateOf<StickerItem?>(null) }
    var favoriteStickerIds by remember { mutableStateOf(setOf("duck_cool", "react_fire")) }

    val packs = TelegramStickersCatalog.packs

    // Filtered stickers based on pack or search query
    val displayStickers: List<StickerItem> = remember(selectedPackId, searchQuery) {
        if (searchQuery.isNotBlank()) {
            val q = searchQuery.trim().lowercase()
            packs.flatMap { it.stickers }.filter {
                it.name.contains(q, ignoreCase = true) ||
                        it.description.contains(q, ignoreCase = true) ||
                        it.emoji.contains(q)
            }
        } else {
            when (selectedPackId) {
                "recent" -> TelegramStickersCatalog.recentStickers
                "favorites" -> packs.flatMap { it.stickers }.filter { it.id in favoriteStickerIds }
                else -> packs.firstOrNull { it.id == selectedPackId }?.stickers ?: emptyList()
            }
        }
    }

    val currentPack = remember(selectedPackId) {
        packs.firstOrNull { it.id == selectedPackId }
    }

    val backgroundColor = if (isDarkMode) Color(0xFF17212B) else Color(0xFFFFFFFF)
    val topBarBg = if (isDarkMode) Color(0xFF0E1621) else Color(0xFFF4F6F9)
    val dockBg = if (isDarkMode) Color(0xFF131C27) else Color(0xFFEFEFF4)

    Column(
        modifier = modifier
            .fillMaxWidth()
            .height(340.dp)
            .clip(RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp))
            .background(backgroundColor)
            .testTag("sticker_selection_drawer")
    ) {
        // Drag handle pill
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 8.dp, bottom = 4.dp),
            contentAlignment = Alignment.Center
        ) {
            Box(
                modifier = Modifier
                    .width(36.dp)
                    .height(4.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(if (isDarkMode) Color(0xFF2B3A4C) else Color(0xFFD1D5DB))
            )
        }

        // Top Drawer Controls & Search Bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            if (isSearchActive) {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = {
                        Text(
                            text = "جستجوی استیکرها (مثلاً اردک، آتش، سلام)...",
                            fontSize = 13.sp,
                            color = if (isDarkMode) Color(0xFF7F8B99) else Color(0xFF8E8E93)
                        )
                    },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = TelegramBlue,
                        unfocusedBorderColor = Color.Transparent,
                        focusedContainerColor = if (isDarkMode) Color(0xFF242F3D) else Color(0xFFF1F5F9),
                        unfocusedContainerColor = if (isDarkMode) Color(0xFF242F3D) else Color(0xFFF1F5F9),
                        focusedTextColor = if (isDarkMode) Color.White else Color.Black,
                        unfocusedTextColor = if (isDarkMode) Color.White else Color.Black
                    ),
                    shape = RoundedCornerShape(20.dp),
                    trailingIcon = {
                        IconButton(
                            onClick = {
                                isSearchActive = false
                                searchQuery = ""
                            }
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "بستن جستجو",
                                tint = if (isDarkMode) Color.White else Color.Gray,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    },
                    modifier = Modifier
                        .weight(1f)
                        .height(46.dp)
                        .testTag("sticker_search_input")
                )
            } else {
                // Header Title with current pack name
                Row(
                    modifier = Modifier.weight(1f),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = when (selectedPackId) {
                            "recent" -> "استیکرهای اخیر 🕒"
                            "favorites" -> "علاقه‌مندی‌ها ⭐"
                            else -> currentPack?.title ?: "استیکرها"
                        },
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isDarkMode) Color.White else Color(0xFF1E2834),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(TelegramBlue.copy(alpha = 0.15f))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = "${displayStickers.size} استیکر",
                            color = TelegramBlue,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }

                // Search Icon button
                IconButton(
                    onClick = { isSearchActive = true },
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(if (isDarkMode) Color(0xFF242F3D) else Color(0xFFF1F5F9))
                        .testTag("sticker_search_open_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = "جستجو",
                        tint = if (isDarkMode) Color(0xFF8E8E93) else Color(0xFF555555),
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }

        HorizontalDivider(
            color = if (isDarkMode) Color(0xFF222C38) else Color(0xFFF1F2F4),
            thickness = 1.dp
        )

        // Sticker Grid View
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
        ) {
            if (displayStickers.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(text = "🔍", fontSize = 36.sp)
                        Text(
                            text = "استیکری یافت نشد",
                            fontSize = 13.sp,
                            color = if (isDarkMode) Color(0xFF7F8B99) else Color(0xFF8E8E93)
                        )
                    }
                }
            } else {
                LazyVerticalGrid(
                    columns = GridCells.Adaptive(minSize = 68.dp),
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier
                        .fillMaxSize()
                        .testTag("stickers_grid")
                ) {
                    items(displayStickers, key = { it.id }) { sticker ->
                        val isFavorite = sticker.id in favoriteStickerIds
                        StickerCell(
                            sticker = sticker,
                            isFavorite = isFavorite,
                            isDarkMode = isDarkMode,
                            onClick = {
                                onSendSticker(sticker.emoji)
                            },
                            onLongClick = {
                                previewSticker = sticker
                            }
                        )
                    }
                }
            }
        }

        // Bottom Pack Navigation Rail (Signature Telegram sticker tray)
        Surface(
            color = dockBg,
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 6.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Recent Quick Tab
                PackTabButton(
                    icon = "🕒",
                    isSelected = selectedPackId == "recent" && !isSearchActive,
                    onClick = {
                        selectedPackId = "recent"
                        isSearchActive = false
                        searchQuery = ""
                    },
                    isDarkMode = isDarkMode,
                    testTag = "pack_tab_recent"
                )

                // Favorites Quick Tab
                PackTabButton(
                    icon = "⭐",
                    isSelected = selectedPackId == "favorites" && !isSearchActive,
                    onClick = {
                        selectedPackId = "favorites"
                        isSearchActive = false
                        searchQuery = ""
                    },
                    isDarkMode = isDarkMode,
                    testTag = "pack_tab_favorites"
                )

                Spacer(
                    modifier = Modifier
                        .width(1.dp)
                        .height(24.dp)
                        .background(if (isDarkMode) Color(0xFF2B3747) else Color(0xFFD1D5DB))
                        .padding(horizontal = 2.dp)
                )

                // Scrollable List of Sticker Packs
                LazyRow(
                    modifier = Modifier.weight(1f),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    contentPadding = PaddingValues(horizontal = 4.dp)
                ) {
                    items(packs, key = { it.id }) { pack ->
                        PackTabButton(
                            icon = pack.iconEmoji,
                            isSelected = selectedPackId == pack.id && !isSearchActive,
                            onClick = {
                                selectedPackId = pack.id
                                isSearchActive = false
                                searchQuery = ""
                            },
                            isDarkMode = isDarkMode,
                            testTag = "pack_tab_${pack.id}"
                        )
                    }
                }
            }
        }
    }

    // Long Press Large Preview Dialog (Telegram-style sticker zoom popup)
    previewSticker?.let { sticker ->
        Dialog(onDismissRequest = { previewSticker = null }) {
            Surface(
                shape = RoundedCornerShape(24.dp),
                color = if (isDarkMode) Color(0xFF17212B) else Color.White,
                shadowElevation = 16.dp,
                modifier = Modifier
                    .fillMaxWidth(0.85f)
                    .testTag("sticker_preview_dialog")
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    // Large visual sticker display
                    Box(
                        modifier = Modifier
                            .size(140.dp)
                            .clip(CircleShape)
                            .background(
                                Brush.radialGradient(
                                    colors = listOf(
                                        Color(sticker.accentColorHex).copy(alpha = 0.25f),
                                        Color.Transparent
                                    )
                                )
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = sticker.emoji,
                            fontSize = 72.sp,
                            modifier = Modifier.padding(8.dp)
                        )
                    }

                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = sticker.name,
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isDarkMode) Color.White else Color.Black
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = sticker.description,
                            fontSize = 12.sp,
                            color = if (isDarkMode) Color(0xFF8E8E93) else Color.Gray,
                            textAlign = TextAlign.Center
                        )
                    }

                    // Action buttons
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        // Favorite toggle button
                        val isFav = sticker.id in favoriteStickerIds
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(44.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(if (isDarkMode) Color(0xFF242F3D) else Color(0xFFF1F5F9))
                                .clickable {
                                    favoriteStickerIds = if (isFav) {
                                        favoriteStickerIds - sticker.id
                                    } else {
                                        favoriteStickerIds + sticker.id
                                    }
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Icon(
                                    imageVector = if (isFav) Icons.Default.Star else Icons.Outlined.BookmarkBorder,
                                    contentDescription = "علاقه‌مندی",
                                    tint = if (isFav) Color(0xFFFFD21E) else Color(0xFF8E8E93),
                                    modifier = Modifier.size(18.dp)
                                )
                                Text(
                                    text = if (isFav) "حذف نشان" else "نشان کردن",
                                    fontSize = 12.sp,
                                    color = if (isDarkMode) Color.White else Color.Black,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }

                        // Send Button
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(44.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(TelegramBlue)
                                .clickable {
                                    onSendSticker(sticker.emoji)
                                    previewSticker = null
                                }
                                .testTag("send_sticker_from_preview"),
                            contentAlignment = Alignment.Center
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Send,
                                    contentDescription = "ارسال",
                                    tint = Color.White,
                                    modifier = Modifier.size(16.dp)
                                )
                                Text(
                                    text = "ارسال استیکر",
                                    fontSize = 12.sp,
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold
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
private fun StickerCell(
    sticker: StickerItem,
    isFavorite: Boolean,
    isDarkMode: Boolean,
    onClick: () -> Unit,
    onLongClick: () -> Unit
) {
    var isPressed by remember { mutableStateOf(false) }
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.88f else 1f,
        animationSpec = spring(stiffness = Spring.StiffnessMedium),
        label = "sticker_scale"
    )

    Box(
        modifier = Modifier
            .size(68.dp)
            .scale(scale)
            .clip(RoundedCornerShape(14.dp))
            .background(
                if (isDarkMode) Color(0xFF222D3B) else Color(0xFFF6F8FA)
            )
            .pointerInput(Unit) {
                detectTapGestures(
                    onPress = {
                        isPressed = true
                        tryAwaitRelease()
                        isPressed = false
                    },
                    onTap = { onClick() },
                    onLongPress = { onLongClick() }
                )
            }
            .testTag("sticker_item_${sticker.id}"),
        contentAlignment = Alignment.Center
    ) {
        // Aesthetic subtle glow backdrop matching pack accent
        Box(
            modifier = Modifier
                .size(46.dp)
                .clip(CircleShape)
                .background(Color(sticker.accentColorHex).copy(alpha = 0.12f))
        )

        // Sticker Emoji Content
        Text(
            text = sticker.emoji,
            fontSize = 32.sp,
            textAlign = TextAlign.Center
        )

        // Favorite Indicator star badge
        if (isFavorite) {
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(4.dp)
                    .size(12.dp)
                    .clip(CircleShape)
                    .background(Color(0xFFFFD21E)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "★",
                    color = Color.Black,
                    fontSize = 8.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
private fun PackTabButton(
    icon: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    isDarkMode: Boolean,
    testTag: String
) {
    val indicatorColor = if (isSelected) TelegramBlue else Color.Transparent
    val textColor = if (isSelected) {
        if (isDarkMode) Color.White else Color.Black
    } else {
        if (isDarkMode) Color(0xFF7F8B99) else Color(0xFF8E8E93)
    }

    Box(
        modifier = Modifier
            .size(40.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(
                if (isSelected) {
                    if (isDarkMode) Color(0xFF242F3D) else Color.White
                } else {
                    Color.Transparent
                }
            )
            .clickable(onClick = onClick)
            .testTag(testTag),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = icon,
            fontSize = 20.sp,
            textAlign = TextAlign.Center
        )

        if (isSelected) {
            Box(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .width(16.dp)
                    .height(3.dp)
                    .clip(RoundedCornerShape(1.5.dp))
                    .background(TelegramBlue)
            )
        }
    }
}
