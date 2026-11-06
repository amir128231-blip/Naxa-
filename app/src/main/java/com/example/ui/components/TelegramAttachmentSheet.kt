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
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Audiotrack
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Poll
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

data class AttachmentOption(
    val title: String,
    val icon: ImageVector,
    val color: Color,
    val sampleMessage: String
)

@Composable
fun TelegramAttachmentSheet(
    isDarkMode: Boolean,
    onSelectAttachment: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val options = listOf(
        AttachmentOption("گالری", Icons.Default.Image, Color(0xFF00B0FF), "📷 [عکس ارسالی: کافه دورهمی]"),
        AttachmentOption("فایل", Icons.Default.Description, Color(0xFFFF9100), "📄 [فایل: جزوه_پروژه.pdf - 2.4 MB]"),
        AttachmentOption("موقعیت", Icons.Default.LocationOn, Color(0xFF00C853), "📍 [موقعیت مکانی: کافه دیدار - خیابان ولیعصر]"),
        AttachmentOption("مخاطب", Icons.Default.Person, Color(0xFF651FFF), "👤 [کارت مخاطب: مهندس احمدی - 09121234567]"),
        AttachmentOption("موسیقی", Icons.Default.Audiotrack, Color(0xFFFF4081), "🎵 [فایل صوتی: آهنگ آرامش‌بخش پیانو.mp3]"),
        AttachmentOption("نظرسنجی", Icons.Default.Poll, Color(0xFF00E676), "📊 [نظرسنجی: آخر هفته بریم کوه یا سینما؟]")
    )

    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(if (isDarkMode) MaterialTheme.colorScheme.surface else Color(0xFFFFFFFF))
            .padding(16.dp)
    ) {
        Text(
            text = "اشتراک‌گذاری و ارسال فایل",
            fontSize = 14.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(bottom = 12.dp)
        )

        LazyVerticalGrid(
            columns = GridCells.Fixed(3),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            items(options) { opt ->
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier
                        .clip(CircleShape)
                        .clickable { onSelectAttachment(opt.sampleMessage) }
                        .padding(8.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(52.dp)
                            .clip(CircleShape)
                            .background(opt.color),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = opt.icon,
                            contentDescription = opt.title,
                            tint = Color.White,
                            modifier = Modifier.size(26.dp)
                        )
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = opt.title,
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        }
    }
}
