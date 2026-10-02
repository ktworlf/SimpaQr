package com.example.ui.screens

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
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.IosIcons
import com.example.ui.theme.AccentPrimary

enum class StudioTool {
    AI_IMAGE,
    PDF_DOCUMENT,
    UPI_PAYMENT,
    QR_DESIGNER,
    WIFI_HUB,
    VCARD_CARD,
    THREAT_RADAR,
    BATCH_INVENTORY,
    SECRET_LOCKER,
    AI_GUIDE
}

data class StudioItem(
    val tool: StudioTool,
    val title: String,
    val description: String,
    val icon: ImageVector,
    val badgeColor: Color,
    val iconColor: Color,
    val tagText: String = "Open Tool"
)

@Composable
fun StudioScreen(
    onSelectTool: (StudioTool) -> Unit
) {
    val tools = listOf(
        StudioItem(
            tool = StudioTool.AI_IMAGE,
            title = "AI Visual Analyzer",
            description = "Inspect photos, posters, and hidden barcodes using Gemini 3.1 Pro with High Thinking.",
            icon = IosIcons.AiSparkles,
            badgeColor = Color(0xFFEEF2FF),
            iconColor = Color(0xFF4F46E5)
        ),
        StudioItem(
            tool = StudioTool.PDF_DOCUMENT,
            title = "PDF & Document AI",
            description = "Extract and verify invoices, certificates, and multi-barcode documents.",
            icon = IosIcons.Studio,
            badgeColor = Color(0xFFF0FDF4),
            iconColor = Color(0xFF10B981)
        ),
        StudioItem(
            tool = StudioTool.UPI_PAYMENT,
            title = "UPI Payment QR",
            description = "Generate 1-tap payment QRs for PhonePe, Google Pay, Paytm, & BHIM with instant test launch.",
            icon = IosIcons.ProBadge,
            badgeColor = Color(0xFFFFFBEB),
            iconColor = Color(0xFFF59E0B)
        ),
        StudioItem(
            tool = StudioTool.QR_DESIGNER,
            title = "Smart QR Designer",
            description = "Full studio with custom accent colors, Wi-Fi, URLs, text, and photo gallery export.",
            icon = IosIcons.Scanner,
            badgeColor = Color(0xFFF5F3FF),
            iconColor = Color(0xFF7C3AED)
        ),
        StudioItem(
            tool = StudioTool.WIFI_HUB,
            title = "Wi-Fi EasyConnect",
            description = "Create guest connection cards with automatic password reveal and 1-tap copy.",
            icon = IosIcons.Wifi,
            badgeColor = Color(0xFFE0F2FE),
            iconColor = Color(0xFF0284C7)
        ),
        StudioItem(
            tool = StudioTool.VCARD_CARD,
            title = "vCard Business Card",
            description = "Apple-styled digital business cards with direct phone address book integration.",
            icon = IosIcons.Contact,
            badgeColor = Color(0xFFFCE7F3),
            iconColor = Color(0xFFDB2777)
        ),
        StudioItem(
            tool = StudioTool.THREAT_RADAR,
            title = "Link Threat Radar",
            description = "Local phishing detection, suspicious TLD audit, and 1-tap Google search verification.",
            icon = IosIcons.ShieldCheck,
            badgeColor = Color(0xFFFEF2F2),
            iconColor = Color(0xFFEF4444)
        ),
        StudioItem(
            tool = StudioTool.BATCH_INVENTORY,
            title = "Batch Scanner Mode",
            description = "High-speed continuous barcode scanner for warehouse inventory and CSV export.",
            icon = IosIcons.Refresh,
            badgeColor = Color(0xFFECFDF5),
            iconColor = Color(0xFF059669)
        ),
        StudioItem(
            tool = StudioTool.SECRET_LOCKER,
            title = "Secret Crypto Locker",
            description = "PIN-encrypted confidential QR codes with local cryptographic obfuscation.",
            icon = IosIcons.Lock,
            badgeColor = Color(0xFFF1F5F9),
            iconColor = Color(0xFF475569)
        ),
        StudioItem(
            tool = StudioTool.AI_GUIDE,
            title = "Simpa AI Guide",
            description = "Multi-turn conversational chatbot to guide and troubleshoot any QR code issue.",
            icon = IosIcons.ChatGuide,
            badgeColor = Color(0xFFEEF2FF),
            iconColor = Color(0xFF4F46E5)
        )
    )

    LazyVerticalGrid(
        columns = GridCells.Adaptive(minSize = 160.dp),
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .statusBarsPadding()
            .padding(horizontal = 20.dp),
        horizontalArrangement = Arrangement.spacedBy(14.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Section Header
        item(span = { GridItemSpan(maxLineSpan) }) {
            Column(modifier = Modifier.padding(top = 16.dp, bottom = 8.dp)) {
                Text(
                    text = "Studio & Tools",
                    style = MaterialTheme.typography.displayLarge,
                    color = MaterialTheme.colorScheme.onBackground,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "10+ professional offline utilities and AI engines",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        // Tools Grid Cards (Matching User's Reference Layout)
        items(tools, key = { it.tool.name }) { item ->
            StudioCardItem(
                item = item,
                onClick = { onSelectTool(item.tool) }
            )
        }

        // Bottom space so floating nav bar does not overlap
        item(span = { GridItemSpan(maxLineSpan) }) {
            Spacer(modifier = Modifier.height(100.dp))
        }
    }
}

@Composable
private fun StudioCardItem(
    item: StudioItem,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(
                elevation = 6.dp,
                shape = RoundedCornerShape(22.dp),
                spotColor = Color.Black.copy(alpha = 0.06f),
                ambientColor = Color.Black.copy(alpha = 0.04f)
            )
            .clip(RoundedCornerShape(22.dp))
            .background(MaterialTheme.colorScheme.surface)
            .border(
                width = 0.8.dp,
                color = MaterialTheme.colorScheme.outline.copy(alpha = 0.25f),
                shape = RoundedCornerShape(22.dp)
            )
            .clickable(onClick = onClick)
            .padding(16.dp)
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            // Top Row: Soft Pastel Icon squircle + Top-Right arrow pill
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(42.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(item.badgeColor),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = item.icon,
                        contentDescription = item.title,
                        tint = item.iconColor,
                        modifier = Modifier.size(22.dp)
                    )
                }

                Box(
                    modifier = Modifier
                        .size(28.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = IosIcons.Forward,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(14.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Title
            Text(
                text = item.title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(6.dp))

            // Description
            Text(
                text = item.description,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                lineHeight = 18.sp,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(14.dp))

            // Bottom Action Pill
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(34.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = item.tagText,
                    style = MaterialTheme.typography.labelSmall,
                    color = AccentPrimary,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}
