package com.example.ui.screens

import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.BatteryChargingFull
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.ChatBubble
import androidx.compose.material.icons.filled.Computer
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.Help
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PieChart
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.VerifiedUser
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material.icons.filled.VpnKey
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.TelegramBottomDock
import com.example.ui.components.TelegramNavTab
import com.example.ui.viewmodel.CurrentUserProfile

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    userProfile: CurrentUserProfile,
    isDarkMode: Boolean,
    currentBottomTab: TelegramNavTab = TelegramNavTab.SETTINGS,
    onTabSelected: (TelegramNavTab) -> Unit = {},
    onToggleDarkMode: () -> Unit,
    onOpenAccountEdit: () -> Unit,
    onOpenLanguageSelection: () -> Unit,
    onOpenSupportBot: () -> Unit,
    onSignOut: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    // State for interactive bottom sheets
    var activeSheet by remember { mutableStateOf<SettingsDetailSheet?>(null) }
    var showMenu by remember { mutableStateOf(false) }
    var isSearchActive by remember { mutableStateOf(false) }
    var searchQuery by remember { mutableStateOf("") }

    // Chat Settings State
    var messageTextSize by remember { mutableFloatStateOf(16f) }
    var enableAnimations by remember { mutableStateOf(true) }
    var selectedWallpaper by remember { mutableStateOf("تیره نکسا") }

    // Notifications State
    var privateChatsNotify by remember { mutableStateOf(true) }
    var groupsNotify by remember { mutableStateOf(true) }
    var inAppVibrate by remember { mutableStateOf(true) }
    var inAppSound by remember { mutableStateOf(true) }

    // Privacy State
    var passcodeLockEnabled by remember { mutableStateOf(false) }
    var twoStepAuthEnabled by remember { mutableStateOf(true) }
    var lastSeenPrivacy by remember { mutableStateOf("همه") }

    // Data & Storage State
    var autoDownloadCellular by remember { mutableStateOf(false) }
    var autoDownloadWifi by remember { mutableStateOf(true) }
    var calculatedCacheSize by remember { mutableStateOf("48.2 مگابایت") }

    // Power Saving State
    var powerSavingEnabled by remember { mutableStateOf(false) }

    val bgColor = if (isDarkMode) Color(0xFF0E1621) else Color(0xFFF1F5F9)
    val cardBg = if (isDarkMode) Color(0xFF17212B) else Color.White
    val topBarBg = if (isDarkMode) Color(0xFF17212B) else Color(0xFF2481CC)
    val dividerColor = if (isDarkMode) Color(0xFF1B232E) else Color(0xFFF1F5F9)

    // Ensure strict RTL layout matching Telegram Persian
    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
        Scaffold(
            modifier = modifier.fillMaxSize(),
            containerColor = bgColor,
            topBar = {
                TopAppBar(
                    title = {
                        if (isSearchActive) {
                            TextField(
                                value = searchQuery,
                                onValueChange = { searchQuery = it },
                                placeholder = {
                                    Text(
                                        text = "جستجو در تنظیمات...",
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
                                    .testTag("settings_search_input")
                            )
                        } else {
                            Text(
                                text = "تنظیمات",
                                color = Color.White,
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.testTag("settings_screen_title")
                            )
                        }
                    },
                    actions = {
                        if (isSearchActive) {
                            IconButton(onClick = {
                                isSearchActive = false
                                searchQuery = ""
                            }) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                    contentDescription = "Close Search",
                                    tint = Color.White
                                )
                            }
                        } else {
                            IconButton(
                                onClick = { isSearchActive = true },
                                modifier = Modifier.testTag("settings_search_button")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Search,
                                    contentDescription = "Search Settings",
                                    tint = Color.White
                                )
                            }
                            Box {
                                IconButton(
                                    onClick = { showMenu = true },
                                    modifier = Modifier.testTag("settings_more_menu_button")
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.MoreVert,
                                        contentDescription = "Options",
                                        tint = Color.White
                                    )
                                }
                                DropdownMenu(
                                    expanded = showMenu,
                                    onDismissRequest = { showMenu = false }
                                ) {
                                    DropdownMenuItem(
                                        text = { Text("ویرایش نام") },
                                        onClick = {
                                            showMenu = false
                                            onOpenAccountEdit()
                                        }
                                    )
                                    DropdownMenuItem(
                                        text = { Text("تغییر عکس پروفایل") },
                                        onClick = {
                                            showMenu = false
                                            onOpenAccountEdit()
                                        }
                                    )
                                    DropdownMenuItem(
                                        text = { Text("خروج از حساب") },
                                        onClick = {
                                            showMenu = false
                                            onSignOut()
                                        }
                                    )
                                }
                            }
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = topBarBg
                    )
                )
            },
            bottomBar = {
                TelegramBottomDock(
                    currentTab = currentBottomTab,
                    onTabSelected = onTabSelected,
                    userInitial = userProfile.name.take(1).ifBlank { "A" },
                    isDarkMode = isDarkMode
                )
            }
        ) { innerPadding ->
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(horizontal = 14.dp, vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // Main Settings Grouped Card (Screenshots 1 & 2)
                item {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("settings_main_card"),
                        shape = RoundedCornerShape(18.dp),
                        colors = CardDefaults.cardColors(containerColor = cardBg),
                        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                    ) {
                        Column(modifier = Modifier.fillMaxWidth()) {
                            // 1. حساب کاربری
                            SettingsItemRow(
                                title = "حساب کاربری",
                                subtitle = "شماره، نام کاربری، بیوگرافی",
                                icon = Icons.Default.Person,
                                iconBgColor = Color(0xFF2481CC), // Telegram Blue
                                isDarkMode = isDarkMode,
                                onClick = onOpenAccountEdit,
                                testTag = "setting_account"
                            )

                            HorizontalDivider(
                                color = dividerColor,
                                thickness = 0.8.dp,
                                modifier = Modifier.padding(start = 68.dp)
                            )

                            // 2. تنظیمات گفتگو
                            SettingsItemRow(
                                title = "تنظیمات گفتگو",
                                subtitle = "پس‌زمینه، حالت شب، انیمیشن‌ها",
                                icon = Icons.Default.ChatBubble,
                                iconBgColor = Color(0xFFFF9500), // Orange
                                isDarkMode = isDarkMode,
                                onClick = { activeSheet = SettingsDetailSheet.CHAT_SETTINGS },
                                testTag = "setting_chat"
                            )

                            HorizontalDivider(
                                color = dividerColor,
                                thickness = 0.8.dp,
                                modifier = Modifier.padding(start = 68.dp)
                            )

                            // 3. حریم خصوصی و امنیت
                            SettingsItemRow(
                                title = "حریم خصوصی و امنیت",
                                subtitle = "آخرین بازدید، دستگاه‌ها، کلیدهای عبور",
                                icon = Icons.Default.VpnKey,
                                iconBgColor = Color(0xFF34C759), // Green
                                isDarkMode = isDarkMode,
                                onClick = { activeSheet = SettingsDetailSheet.PRIVACY },
                                testTag = "setting_privacy"
                            )

                            HorizontalDivider(
                                color = dividerColor,
                                thickness = 0.8.dp,
                                modifier = Modifier.padding(start = 68.dp)
                            )

                            // 4. اعلان‌ها
                            SettingsItemRow(
                                title = "اعلان‌ها",
                                subtitle = "صداها، تماس‌ها، نشانگرها",
                                icon = Icons.Default.VolumeUp,
                                iconBgColor = Color(0xFFFF3B30), // Red
                                isDarkMode = isDarkMode,
                                onClick = { activeSheet = SettingsDetailSheet.NOTIFICATIONS },
                                testTag = "setting_notifications"
                            )

                            HorizontalDivider(
                                color = dividerColor,
                                thickness = 0.8.dp,
                                modifier = Modifier.padding(start = 68.dp)
                            )

                            // 5. داده‌ها و ذخیره‌سازی
                            SettingsItemRow(
                                title = "داده‌ها و ذخیره‌سازی",
                                subtitle = "تنظیمات دانلود رسانه",
                                icon = Icons.Default.PieChart,
                                iconBgColor = Color(0xFF007AFF), // Blue
                                isDarkMode = isDarkMode,
                                onClick = { activeSheet = SettingsDetailSheet.DATA_STORAGE },
                                testTag = "setting_data_storage"
                            )

                            HorizontalDivider(
                                color = dividerColor,
                                thickness = 0.8.dp,
                                modifier = Modifier.padding(start = 68.dp)
                            )

                            // 6. پوشه‌های گفتگو
                            SettingsItemRow(
                                title = "پوشه‌های گفتگو",
                                subtitle = "مرتب‌سازی گفتگوها در پوشه‌ها",
                                icon = Icons.Default.Folder,
                                iconBgColor = Color(0xFF5AC8FA), // Sky blue
                                isDarkMode = isDarkMode,
                                onClick = { activeSheet = SettingsDetailSheet.CHAT_FOLDERS },
                                testTag = "setting_folders"
                            )

                            HorizontalDivider(
                                color = dividerColor,
                                thickness = 0.8.dp,
                                modifier = Modifier.padding(start = 68.dp)
                            )

                            // 7. دستگاه‌ها
                            SettingsItemRow(
                                title = "دستگاه‌ها",
                                subtitle = "مدیریت دستگاه‌های متصل شده",
                                icon = Icons.Default.Computer,
                                iconBgColor = Color(0xFF30B0C7), // Teal
                                isDarkMode = isDarkMode,
                                onClick = { activeSheet = SettingsDetailSheet.DEVICES },
                                testTag = "setting_devices"
                            )

                            HorizontalDivider(
                                color = dividerColor,
                                thickness = 0.8.dp,
                                modifier = Modifier.padding(start = 68.dp)
                            )

                            // 8. ذخیره انرژی
                            SettingsItemRow(
                                title = "ذخیره انرژی",
                                subtitle = "کاهش مصرف انرژی در شارژ کم",
                                icon = Icons.Default.BatteryChargingFull,
                                iconBgColor = Color(0xFFFF9500), // Orange
                                isDarkMode = isDarkMode,
                                onClick = { activeSheet = SettingsDetailSheet.POWER_SAVING },
                                testTag = "setting_power_saving"
                            )

                            HorizontalDivider(
                                color = dividerColor,
                                thickness = 0.8.dp,
                                modifier = Modifier.padding(start = 68.dp)
                            )

                            // 9. زبان
                            SettingsItemRow(
                                title = "زبان",
                                subtitle = "فارسی",
                                icon = Icons.Default.Language,
                                iconBgColor = Color(0xFFAF52DE), // Purple
                                isDarkMode = isDarkMode,
                                onClick = onOpenLanguageSelection,
                                testTag = "setting_language"
                            )
                        }
                    }
                }

                // Help & Support Grouped Card ("راهنمایی") (Screenshots 1 & 2)
                item {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("settings_help_card"),
                        shape = RoundedCornerShape(18.dp),
                        colors = CardDefaults.cardColors(containerColor = cardBg),
                        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                    ) {
                        Column(modifier = Modifier.fillMaxWidth()) {
                            // Section header
                            Text(
                                text = "راهنمایی",
                                color = if (isDarkMode) Color(0xFF2AABEE) else Color(0xFF2481CC),
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(start = 16.dp, end = 16.dp, top = 14.dp, bottom = 6.dp)
                            )

                            // 1. پرسش‌های متداول نکسا
                            SettingsItemRow(
                                title = "پرسش‌های متداول نکسا",
                                subtitle = null,
                                icon = Icons.Default.Help,
                                iconBgColor = Color(0xFF007AFF), // Blue
                                isDarkMode = isDarkMode,
                                onClick = { activeSheet = SettingsDetailSheet.FAQ },
                                testTag = "help_faq"
                            )

                            HorizontalDivider(
                                color = dividerColor,
                                thickness = 0.8.dp,
                                modifier = Modifier.padding(start = 68.dp)
                            )

                            // 3. قابلیت‌های نکسا
                            SettingsItemRow(
                                title = "قابلیت‌های نکسا",
                                subtitle = null,
                                icon = Icons.Default.Lightbulb,
                                iconBgColor = Color(0xFFAF52DE), // Purple
                                isDarkMode = isDarkMode,
                                onClick = { activeSheet = SettingsDetailSheet.FEATURES },
                                testTag = "help_features"
                            )

                            HorizontalDivider(
                                color = dividerColor,
                                thickness = 0.8.dp,
                                modifier = Modifier.padding(start = 68.dp)
                            )

                            // 4. سیاست حریم خصوصی
                            SettingsItemRow(
                                title = "سیاست حریم خصوصی",
                                subtitle = null,
                                icon = Icons.Default.VerifiedUser,
                                iconBgColor = Color(0xFF34C759), // Green
                                isDarkMode = isDarkMode,
                                onClick = { activeSheet = SettingsDetailSheet.PRIVACY_POLICY },
                                testTag = "help_privacy_policy"
                            )
                        }
                    }
                }

                // Official Version Footer
                item {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 10.dp, bottom = 40.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "نکسا برای اندروید v12.10.0 (7031) v12.10.0",
                            color = if (isDarkMode) Color(0xFF707579) else Color(0xFF8E8E93),
                            fontSize = 12.sp,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "store bundled arm64-v8a",
                            color = if (isDarkMode) Color(0xFF707579) else Color(0xFF8E8E93),
                            fontSize = 12.sp,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        }
    }

    // Modal Bottom Sheets for Real Setting Customizations
    when (activeSheet) {
        SettingsDetailSheet.CHAT_SETTINGS -> {
            ModalBottomSheet(
                onDismissRequest = { activeSheet = null },
                containerColor = cardBg,
                sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
            ) {
                CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp)
                    ) {
                        Text(
                            text = "تنظیمات گفتگو",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isDarkMode) Color.White else Color(0xFF0F172A)
                        )
                        Spacer(modifier = Modifier.height(16.dp))

                        // Night Mode Switch
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = "حالت شب (تم تیره)",
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = if (isDarkMode) Color.White else Color(0xFF0F172A)
                                )
                                Text(
                                    text = "فعال‌سازی تم تاریک مناسب شب",
                                    fontSize = 12.sp,
                                    color = Color(0xFF8E8E93)
                                )
                            }
                            Switch(
                                checked = isDarkMode,
                                onCheckedChange = { onToggleDarkMode() },
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = Color.White,
                                    checkedTrackColor = Color(0xFF2481CC)
                                )
                            )
                        }

                        Spacer(modifier = Modifier.height(18.dp))

                        // Text Size Slider
                        Text(
                            text = "اندازه قلم پیام‌ها: ${messageTextSize.toInt()} sp",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium,
                            color = if (isDarkMode) Color.White else Color(0xFF0F172A)
                        )
                        Slider(
                            value = messageTextSize,
                            onValueChange = { messageTextSize = it },
                            valueRange = 12f..24f,
                            colors = SliderDefaults.colors(
                                thumbColor = Color(0xFF2481CC),
                                activeTrackColor = Color(0xFF2481CC)
                            )
                        )

                        Spacer(modifier = Modifier.height(14.dp))

                        // Animations Switch
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "جلوه‌های پویانمایی نرم",
                                fontSize = 15.sp,
                                color = if (isDarkMode) Color.White else Color(0xFF0F172A)
                            )
                            Switch(
                                checked = enableAnimations,
                                onCheckedChange = { enableAnimations = it },
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = Color.White,
                                    checkedTrackColor = Color(0xFF2481CC)
                                )
                            )
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        Text(
                            text = "پس‌زمینه فعلی: $selectedWallpaper",
                            fontSize = 13.sp,
                            color = Color(0xFF2481CC)
                        )

                        Spacer(modifier = Modifier.height(16.dp))
                    }
                }
            }
        }

        SettingsDetailSheet.PRIVACY -> {
            ModalBottomSheet(
                onDismissRequest = { activeSheet = null },
                containerColor = cardBg
            ) {
                CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp)
                    ) {
                        Text(
                            text = "حریم خصوصی و امنیت",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isDarkMode) Color.White else Color(0xFF0F172A)
                        )
                        Spacer(modifier = Modifier.height(16.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "قفل با گذرواژه (Passcode)",
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = if (isDarkMode) Color.White else Color(0xFF0F172A)
                                )
                                Text(
                                    text = if (passcodeLockEnabled) "فعال • برای هر بار ورود نیاز به رمز ۴ رقمی است" else "ورود به برنامه فقط با رمز امنیتی ۴ رقمی",
                                    fontSize = 12.sp,
                                    color = if (passcodeLockEnabled) Color(0xFF10B981) else Color(0xFF8E8E93)
                                )
                            }
                            Switch(
                                checked = passcodeLockEnabled,
                                onCheckedChange = { enable ->
                                    passcodeLockEnabled = enable
                                    if (enable) {
                                        Toast.makeText(context, "رمز امنیتی پیش‌فرض (1234) فعال شد. می‌توانید هر زمان آن را تغییر دهید.", Toast.LENGTH_LONG).show()
                                    } else {
                                        Toast.makeText(context, "قفل گذرواژه غیرفعال گردید.", Toast.LENGTH_SHORT).show()
                                    }
                                }
                            )
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        // End-to-End Encryption row
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "رمزنگاری سرتاسری (E2E Encryption)",
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = if (isDarkMode) Color.White else Color(0xFF0F172A)
                                )
                                Text(
                                    text = "محافظت از پیام‌ها و داده‌های محلی با الگوریتم AES-256",
                                    fontSize = 12.sp,
                                    color = Color(0xFF10B981)
                                )
                            }
                            Switch(
                                checked = true,
                                onCheckedChange = {
                                    Toast.makeText(context, "رمزنگاری پیشرفته همیشه در پس‌زمینه فعال است.", Toast.LENGTH_SHORT).show()
                                }
                            )
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "تأیید هویت دو مرحله‌ای (2FA)",
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = if (isDarkMode) Color.White else Color(0xFF0F172A)
                                )
                                Text(
                                    text = "محافظت مضاعف با گذرواژه ابری و کد اعتبارسنجی پیامکی",
                                    fontSize = 12.sp,
                                    color = Color(0xFF8E8E93)
                                )
                            }
                            Switch(
                                checked = twoStepAuthEnabled,
                                onCheckedChange = { 
                                    twoStepAuthEnabled = it
                                    Toast.makeText(context, if (it) "تایید دو مرحله‌ای فعال شد" else "تایید دو مرحله‌ای غیرفعال شد", Toast.LENGTH_SHORT).show()
                                }
                            )
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        // Active Sessions / Devices
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .clickable {
                                    Toast.makeText(context, "تنها ۱ نشست فعال (همین دستگاه) متصل است.", Toast.LENGTH_SHORT).show()
                                }
                                .padding(vertical = 4.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = "دستگاه‌های متصل و نشست‌های فعال",
                                    fontSize = 15.sp,
                                    color = if (isDarkMode) Color.White else Color(0xFF0F172A)
                                )
                                Text(
                                    text = "این دستگاه (Android Active Session)",
                                    fontSize = 12.sp,
                                    color = Color(0xFF2481CC)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        Text(
                            text = "نمایش آخرین بازدید: $lastSeenPrivacy",
                            fontSize = 14.sp,
                            color = Color(0xFF2481CC)
                        )

                        Spacer(modifier = Modifier.height(16.dp))
                    }
                }
            }
        }

        SettingsDetailSheet.NOTIFICATIONS -> {
            ModalBottomSheet(
                onDismissRequest = { activeSheet = null },
                containerColor = cardBg
            ) {
                CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp)
                    ) {
                        Text(
                            text = "اعلان‌ها و صداها",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isDarkMode) Color.White else Color(0xFF0F172A)
                        )
                        Spacer(modifier = Modifier.height(16.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("اعلان گفتگوهای خصوصی", fontSize = 15.sp, color = if (isDarkMode) Color.White else Color(0xFF0F172A))
                            Switch(checked = privateChatsNotify, onCheckedChange = { privateChatsNotify = it })
                        }
                        Spacer(modifier = Modifier.height(12.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("اعلان گروه‌ها", fontSize = 15.sp, color = if (isDarkMode) Color.White else Color(0xFF0F172A))
                            Switch(checked = groupsNotify, onCheckedChange = { groupsNotify = it })
                        }
                        Spacer(modifier = Modifier.height(12.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("لرزش درون برنامه‌ای", fontSize = 15.sp, color = if (isDarkMode) Color.White else Color(0xFF0F172A))
                            Switch(checked = inAppVibrate, onCheckedChange = { inAppVibrate = it })
                        }
                        Spacer(modifier = Modifier.height(12.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("صدای درون برنامه‌ای", fontSize = 15.sp, color = if (isDarkMode) Color.White else Color(0xFF0F172A))
                            Switch(checked = inAppSound, onCheckedChange = { inAppSound = it })
                        }

                        Spacer(modifier = Modifier.height(16.dp))
                    }
                }
            }
        }

        SettingsDetailSheet.DATA_STORAGE -> {
            ModalBottomSheet(
                onDismissRequest = { activeSheet = null },
                containerColor = cardBg
            ) {
                CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp)
                    ) {
                        Text(
                            text = "داده‌ها و ذخیره‌سازی",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isDarkMode) Color.White else Color(0xFF0F172A)
                        )
                        Spacer(modifier = Modifier.height(16.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text("دانلود خودکار با اینترنت همراه", fontSize = 15.sp, color = if (isDarkMode) Color.White else Color(0xFF0F172A))
                                Text("فقط عکس‌ها", fontSize = 12.sp, color = Color(0xFF8E8E93))
                            }
                            Switch(checked = autoDownloadCellular, onCheckedChange = { autoDownloadCellular = it })
                        }
                        Spacer(modifier = Modifier.height(12.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text("دانلود خودکار در وای‌فای", fontSize = 15.sp, color = if (isDarkMode) Color.White else Color(0xFF0F172A))
                                Text("عکس‌ها و ویدیوها تا ۱۰ مگابایت", fontSize = 12.sp, color = Color(0xFF8E8E93))
                            }
                            Switch(checked = autoDownloadWifi, onCheckedChange = { autoDownloadWifi = it })
                        }
                        Spacer(modifier = Modifier.height(18.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text("حافظه کش مصرف شده", fontSize = 15.sp, color = if (isDarkMode) Color.White else Color(0xFF0F172A))
                                Text(calculatedCacheSize, fontSize = 12.sp, color = Color(0xFF2481CC), fontWeight = FontWeight.Bold)
                            }
                            TextButton(onClick = {
                                calculatedCacheSize = "۰ بایت"
                                Toast.makeText(context, "حافظه کش پاک‌سازی شد", Toast.LENGTH_SHORT).show()
                            }) {
                                Text("پاک‌سازی کش", color = Color(0xFFFF3B30), fontWeight = FontWeight.Bold)
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))
                    }
                }
            }
        }

        SettingsDetailSheet.DEVICES -> {
            ModalBottomSheet(
                onDismissRequest = { activeSheet = null },
                containerColor = cardBg
            ) {
                CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp)
                    ) {
                        Text(
                            text = "دستگاه‌های متصل",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isDarkMode) Color.White else Color(0xFF0F172A)
                        )
                        Spacer(modifier = Modifier.height(16.dp))

                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = if (isDarkMode) Color(0xFF1E2834) else Color(0xFFF8FAFC)
                            )
                        ) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                Text(
                                    text = "این دستگاه (جلسه فعال)",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF2481CC)
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "Nexa Android v12.10.0 • آنلاین",
                                    fontSize = 13.sp,
                                    color = if (isDarkMode) Color.White else Color(0xFF0F172A)
                                )
                                Text(
                                    text = "آدرس آی‌پی: 185.192.110.12 (ایران)",
                                    fontSize = 12.sp,
                                    color = Color(0xFF8E8E93)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        TextButton(
                            onClick = {
                                Toast.makeText(context, "تمام جلسات دیگر لغو شدند", Toast.LENGTH_SHORT).show()
                                activeSheet = null
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = "پایان دادن به تمام نشست‌های دیگر",
                                color = Color(0xFFFF3B30),
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Spacer(modifier = Modifier.height(16.dp))
                    }
                }
            }
        }

        SettingsDetailSheet.POWER_SAVING -> {
            ModalBottomSheet(
                onDismissRequest = { activeSheet = null },
                containerColor = cardBg
            ) {
                CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp)
                    ) {
                        Text(
                            text = "ذخیره انرژی",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isDarkMode) Color.White else Color(0xFF0F172A)
                        )
                        Spacer(modifier = Modifier.height(16.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text("حالت ذخیره مصرف انرژی", fontSize = 15.sp, color = if (isDarkMode) Color.White else Color(0xFF0F172A))
                                Text("غیرفعال‌سازی انیمیشن‌ها در شارژ کمتر از ۲۰٪", fontSize = 12.sp, color = Color(0xFF8E8E93))
                            }
                            Switch(
                                checked = powerSavingEnabled,
                                onCheckedChange = { powerSavingEnabled = it }
                            )
                        }

                        Spacer(modifier = Modifier.height(16.dp))
                    }
                }
            }
        }

        SettingsDetailSheet.CHAT_FOLDERS -> {
            ModalBottomSheet(
                onDismissRequest = { activeSheet = null },
                containerColor = cardBg
            ) {
                CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp)
                    ) {
                        Text(
                            text = "پوشه‌های گفتگو",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isDarkMode) Color.White else Color(0xFF0F172A)
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "پوشه‌ها به شما کمک می‌کنند گفتگوهای کاری، شخصی، کانال‌ها و گروه‌ها را در تب‌های مجزا سازماندهی کنید.",
                            fontSize = 13.sp,
                            color = Color(0xFF8E8E93)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "پوشه‌های پیش‌فرض فعال: همه، شخصی، گروه‌ها، کانال‌ها",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium,
                            color = Color(0xFF2481CC)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                    }
                }
            }
        }

        SettingsDetailSheet.FAQ -> {
            ModalBottomSheet(
                onDismissRequest = { activeSheet = null },
                containerColor = cardBg
            ) {
                CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp)
                    ) {
                        Text(
                            text = "پرسش‌های متداول نکسا",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isDarkMode) Color.White else Color(0xFF0F172A)
                        )
                        Spacer(modifier = Modifier.height(16.dp))

                        FaqItem(
                            question = "نکسا چیست؟",
                            answer = "نکسا یک برنامه پیام‌رسان بر پایه فضای ابری است که بر روی سرعت و امنیت تمرکز دارد. داده‌ها به صورت آنی بین تمام دستگاه‌های شما همگام می‌شوند."
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        FaqItem(
                            question = "پیام‌های ذخیره شده چیست؟",
                            answer = "فضای ابری شخصی و رایگان برای نگهداری یادداشت‌ها، فایل‌ها و پیام‌های مهم که در دسترس همه دستگاه‌های شماست."
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        FaqItem(
                            question = "آیا نکسا امن است؟",
                            answer = "بله، تمام ارتباطات نکسا با رمزنگاری پیشرفته MTProto محافظت می‌شوند و در صورت نیاز گفتگوی محرمانه سرتاسر رمزگذاری شده در دسترس است."
                        )

                        Spacer(modifier = Modifier.height(16.dp))
                    }
                }
            }
        }

        SettingsDetailSheet.FEATURES -> {
            ModalBottomSheet(
                onDismissRequest = { activeSheet = null },
                containerColor = cardBg
            ) {
                CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp)
                    ) {
                        Text(
                            text = "قابلیت‌های نکسا",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isDarkMode) Color.White else Color(0xFF0F172A)
                        )
                        Spacer(modifier = Modifier.height(14.dp))
                        Text("• گروه‌های ابرگروه تا ۲۰۰,۰۰۰ عضو", fontSize = 14.sp, color = if (isDarkMode) Color.White else Color(0xFF0F172A))
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("• کانال‌های بدون محدودیت مخاطب برای پخش پیام", fontSize = 14.sp, color = if (isDarkMode) Color.White else Color(0xFF0F172A))
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("• فضای ابری نامحدود برای ذخیره فایل تا ۲ گیگابایت", fontSize = 14.sp, color = if (isDarkMode) Color.White else Color(0xFF0F172A))
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("• ربات‌های قدرتمند و تعاملی نکسا", fontSize = 14.sp, color = if (isDarkMode) Color.White else Color(0xFF0F172A))
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("• استیکرهای متحرک با کیفیت بالا و بسته استیکر اختصاصی", fontSize = 14.sp, color = if (isDarkMode) Color.White else Color(0xFF0F172A))

                        Spacer(modifier = Modifier.height(16.dp))
                    }
                }
            }
        }

        SettingsDetailSheet.PRIVACY_POLICY -> {
            ModalBottomSheet(
                onDismissRequest = { activeSheet = null },
                containerColor = cardBg
            ) {
                CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp)
                    ) {
                        Text(
                            text = "سیاست حریم خصوصی نکسا",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isDarkMode) Color.White else Color(0xFF0F172A)
                        )
                        Spacer(modifier = Modifier.height(14.dp))
                        Text(
                            text = "نکسا هرگز از داده‌های شما برای تبلیغات هدفمند استفاده نمی‌کند. پیام‌های شما تنها برای نمایش در دستگاه‌های شما روی سرورهای توزیع‌شده ابری قرار دارند و با بالاترین استانداردهای امنیتی محافظت می‌شوند.",
                            fontSize = 14.sp,
                            lineHeight = 22.sp,
                            color = if (isDarkMode) Color(0xFFE2E8F0) else Color(0xFF334155)
                        )

                        Spacer(modifier = Modifier.height(16.dp))
                    }
                }
            }
        }

        null -> {}
    }
}

@Composable
private fun SettingsItemRow(
    title: String,
    subtitle: String?,
    icon: ImageVector,
    iconBgColor: Color,
    isDarkMode: Boolean,
    onClick: () -> Unit,
    testTag: String
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .testTag(testTag)
            .padding(horizontal = 16.dp, vertical = 13.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Colored squircle/circle badge on the start side (right side in RTL)
        Box(
            modifier = Modifier
                .size(38.dp)
                .clip(CircleShape)
                .background(iconBgColor),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = title,
                tint = Color.White,
                modifier = Modifier.size(20.dp)
            )
        }

        Spacer(modifier = Modifier.width(16.dp))

        Column(
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = title,
                fontSize = 15.sp,
                fontWeight = FontWeight.SemiBold,
                color = if (isDarkMode) Color.White else Color(0xFF0F172A)
            )

            if (!subtitle.isNullOrBlank()) {
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = subtitle,
                    fontSize = 12.5.sp,
                    color = if (isDarkMode) Color(0xFF8E8E93) else Color(0xFF707579)
                )
            }
        }
    }
}

@Composable
private fun FaqItem(
    question: String,
    answer: String
) {
    var expanded by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { expanded = !expanded },
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFF1E2834).copy(alpha = 0.5f)
        )
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(
                text = question,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF2481CC)
            )
            AnimatedVisibility(
                visible = expanded,
                enter = fadeIn() + expandVertically(),
                exit = fadeOut() + shrinkVertically()
            ) {
                Column {
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = answer,
                        fontSize = 13.sp,
                        color = Color.White.copy(alpha = 0.9f),
                        lineHeight = 20.sp
                    )
                }
            }
        }
    }
}

private enum class SettingsDetailSheet {
    CHAT_SETTINGS,
    PRIVACY,
    NOTIFICATIONS,
    DATA_STORAGE,
    DEVICES,
    POWER_SAVING,
    CHAT_FOLDERS,
    FAQ,
    FEATURES,
    PRIVACY_POLICY
}
