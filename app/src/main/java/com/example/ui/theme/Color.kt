package com.example.ui.theme

import androidx.compose.ui.graphics.Color

// Nexa Distinctive Brand Colors (Modern Indigo, Electric Iris & Bioluminescent Accents)
val NexaViolet = Color(0xFF6366F1)
val NexaIndigoDark = Color(0xFF4F46E5)
val NexaPurple = Color(0xFF7C3AED)
val NexaAccentCyan = Color(0xFF06B6D4)

// Legacy alias mappings for seamless compatibility
val TelegramBlue = Color(0xFF5B5CE6) // Upgraded to elegant royal violet-indigo
val TelegramLightBlue = Color(0xFF7C83FD)
val TelegramDarkBlue = Color(0xFF4338CA)
val TelegramSky = Color(0xFF6366F1)

// Chat Bubbles - Light Mode
val TelegramBubbleOutgoingLight = Color(0xFFEEF2FF)
val TelegramBubbleIncomingLight = Color(0xFFFFFFFF)
val TelegramChatBgLight = Color(0xFFF8FAFC)

// Distinctive Nexa Modern Dark Palette (Obsidian Velvet & Electric Iris)
val NexaDarkBackground = Color(0xFF0A0C14) // Deep velvety cosmic obsidian canvas
val NexaDarkSurface = Color(0xFF111422)    // Midnight slate elevated surface
val NexaDarkSurfaceVariant = Color(0xFF191D30) // Elevated architectural container/card
val NexaDarkSurfaceContainerHigh = Color(0xFF1E2338)
val NexaDarkSurfaceContainerHighest = Color(0xFF272D46)
val NexaDarkPrimary = Color(0xFF818CF8)   // Radiant Electric Iris
val NexaDarkOnPrimary = Color(0xFF0F0E2A)
val NexaDarkPrimaryContainer = Color(0xFF2E2A72)
val NexaDarkOnPrimaryContainer = Color(0xFFE0E7FF)
val NexaDarkSecondary = Color(0xFF22D3EE) // Bioluminescent Neon Cyan accent
val NexaDarkOnSecondary = Color(0xFF083344)
val NexaDarkSecondaryContainer = Color(0xFF155E75)
val NexaDarkOnSecondaryContainer = Color(0xFFCFFAFE)
val NexaDarkTertiary = Color(0xFFF472B6)  // Luminous Rose accent
val NexaDarkOnTertiary = Color(0xFF4C0519)
val NexaDarkTertiaryContainer = Color(0xFF701A75)
val NexaDarkOnTertiaryContainer = Color(0xFFFCE7F3)
val NexaDarkOnBackground = Color(0xFFF1F5F9)
val NexaDarkOnSurface = Color(0xFFF1F5F9)
val NexaDarkOnSurfaceVariant = Color(0xFF94A3B8)
val NexaDarkOutline = Color(0xFF2B334C)
val NexaDarkOutlineVariant = Color(0xFF1C2236)
val NexaDarkError = Color(0xFFF87171)
val NexaDarkOnError = Color(0xFF450A0A)
val NexaDarkErrorContainer = Color(0xFF7F1D1D)
val NexaDarkOnErrorContainer = Color(0xFFFECACA)

// Chat Bubbles - Dark Mode (Jeweled Royal Violet-Indigo outgoing & Obsidian Slate incoming)
val TelegramBubbleOutgoingDark = Color(0xFF4F46E5) // Distinctive vibrant jewel tone
val TelegramBubbleIncomingDark = Color(0xFF181C2E) // Refined obsidian card tone
val TelegramChatBgDark = NexaDarkBackground
val TelegramSurfaceDark = NexaDarkSurface
val TelegramHeaderDark = NexaDarkSurface

// Status & Accents
val TelegramOnlineGreen = NexaDarkSecondary // Bioluminescent cyan/mint for distinctive modern status
val TelegramCheckmarkBlue = Color(0xFF818CF8)
val TelegramCheckmarkGreen = Color(0xFF22D3EE)
val TelegramUnreadBadge = Color(0xFF818CF8)
val TelegramMutedGray = Color(0xFF94A3B8)
val TelegramDivider = Color(0xFFE2E8F0)

// Light Theme Palette
val LightPrimary = TelegramBlue
val LightOnPrimary = Color.White
val LightPrimaryContainer = Color(0xFFE0E7FF)
val LightOnPrimaryContainer = Color(0xFF1E1B4B)
val LightSecondary = Color(0xFF0284C7)
val LightOnSecondary = Color.White
val LightSecondaryContainer = Color(0xFFE0F2FE)
val LightOnSecondaryContainer = Color(0xFF0369A1)
val LightTertiary = Color(0xFFDB2777)
val LightOnTertiary = Color.White
val LightTertiaryContainer = Color(0xFFFCE7F3)
val LightOnTertiaryContainer = Color(0xFF831843)
val LightBackground = Color(0xFFF8FAFC)
val LightSurface = Color.White
val LightOnSurface = Color(0xFF0F172A)
val LightSurfaceVariant = Color(0xFFF1F5F9)
val LightOnSurfaceVariant = Color(0xFF64748B)
val LightOutline = Color(0xFFCBD5E1)
val LightOutlineVariant = Color(0xFFE2E8F0)

// Authentic Telegram Android Dark Palette (Matching Telegram v12 Android)
val TelegramAndroidDarkBg = Color(0xFF0E1621) // Exact background from screenshot
val TelegramAndroidDarkSurface = Color(0xFF17212B) // Exact grouped card and header surface
val TelegramAndroidDarkSurfaceVariant = Color(0xFF242F3D) // Card item background and active pills
val TelegramAndroidDarkPrimary = Color(0xFF2481CC) // Telegram brand blue
val TelegramAndroidDarkOnPrimary = Color(0xFFFFFFFF)
val TelegramAndroidDarkOnSurface = Color(0xFFFFFFFF)
val TelegramAndroidDarkOnSurfaceVariant = Color(0xFF8E8E93) // Authentic subtitle and metadata gray
val TelegramAndroidDarkOutline = Color(0xFF242F3D)
val TelegramAndroidDarkOutlineVariant = Color(0xFF1B232E) // Divider color between card rows

// Dark Theme Palette - Semantic mappings
val DarkPrimary = TelegramAndroidDarkPrimary
val DarkOnPrimary = TelegramAndroidDarkOnPrimary
val DarkPrimaryContainer = Color(0xFF1D3C5A)
val DarkOnPrimaryContainer = Color(0xFFE1F0FF)
val DarkSecondary = Color(0xFF2AABEE)
val DarkOnSecondary = Color(0xFF0E1621)
val DarkSecondaryContainer = Color(0xFF1E3A52)
val DarkOnSecondaryContainer = Color(0xFFD6EEFF)
val DarkTertiary = Color(0xFFAF52DE)
val DarkOnTertiary = Color(0xFFFFFFFF)
val DarkTertiaryContainer = Color(0xFF381E48)
val DarkOnTertiaryContainer = Color(0xFFF3E5F9)
val DarkBackground = TelegramAndroidDarkBg
val DarkSurface = TelegramAndroidDarkSurface
val DarkOnSurface = TelegramAndroidDarkOnSurface
val DarkSurfaceVariant = TelegramAndroidDarkSurfaceVariant
val DarkOnSurfaceVariant = TelegramAndroidDarkOnSurfaceVariant
val DarkOutline = TelegramAndroidDarkOutline
val DarkOutlineVariant = TelegramAndroidDarkOutlineVariant
