package com.example.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.data.model.ParsedQrData
import com.example.data.model.ParsedQrType
import com.example.ui.components.IosButton
import com.example.ui.components.IosCard
import com.example.ui.components.IosIcons
import com.example.util.QrTypeParser
import com.example.util.SecurityScanner
import com.example.util.SecurityStatus

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScanResultSheet(
    rawResult: String,
    onDismiss: () -> Unit,
    onTriggerAiAnalysis: (String) -> Unit,
    onToggleFavorite: (Boolean) -> Unit = {}
) {
    val context = LocalContext.current
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val parsedData: ParsedQrData = remember(rawResult) { QrTypeParser.parse(rawResult) }
    val securityReport = remember(rawResult) { SecurityScanner.inspect(rawResult) }
    var isFavorite by remember { mutableStateOf(false) }
    var showWifiPassword by remember { mutableStateOf(false) }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
        containerColor = MaterialTheme.colorScheme.surface
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .padding(bottom = 36.dp)
                .verticalScroll(rememberScrollState())
        ) {
            // Header Row: Type Badge + Action icons
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(42.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primaryContainer),
                        contentAlignment = Alignment.Center
                    ) {
                        val icon = when (parsedData.type) {
                            ParsedQrType.URL -> IosIcons.OpenLink
                            ParsedQrType.WIFI -> IosIcons.Wifi
                            ParsedQrType.CONTACT -> IosIcons.Contact
                            ParsedQrType.EMAIL -> IosIcons.Email
                            ParsedQrType.PHONE -> IosIcons.Phone
                            ParsedQrType.SMS -> IosIcons.ChatGuide
                            ParsedQrType.GEO -> IosIcons.Scanner
                            ParsedQrType.UPI -> IosIcons.ProBadge
                            ParsedQrType.TEXT -> IosIcons.Studio
                        }
                        Icon(
                            imageVector = icon,
                            contentDescription = parsedData.title,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = parsedData.title,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "Decoded Offline • Private",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Row {
                    IconButton(onClick = {
                        isFavorite = !isFavorite
                        onToggleFavorite(isFavorite)
                    }) {
                        Icon(
                            imageVector = if (isFavorite) IosIcons.StarFill else IosIcons.StarOutline,
                            contentDescription = "Favorite",
                            tint = if (isFavorite) MaterialTheme.colorScheme.tertiary else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    IconButton(onClick = onDismiss) {
                        Icon(
                            imageVector = IosIcons.Close,
                            contentDescription = "Close",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Security Radar Card (iOS style)
            val secBg = when (securityReport.status) {
                SecurityStatus.SAFE -> MaterialTheme.colorScheme.tertiary.copy(alpha = 0.12f)
                SecurityStatus.CAUTION -> MaterialTheme.colorScheme.error.copy(alpha = 0.12f)
                SecurityStatus.DANGER -> MaterialTheme.colorScheme.error.copy(alpha = 0.2f)
                SecurityStatus.NEUTRAL -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
            }
            val secTint = when (securityReport.status) {
                SecurityStatus.SAFE -> MaterialTheme.colorScheme.tertiary
                SecurityStatus.CAUTION, SecurityStatus.DANGER -> MaterialTheme.colorScheme.error
                SecurityStatus.NEUTRAL -> MaterialTheme.colorScheme.primary
            }

            IosCard(
                modifier = Modifier.fillMaxWidth(),
                backgroundColor = secBg,
                borderColor = secTint.copy(alpha = 0.4f),
                contentPadding = 14.dp
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = if (securityReport.status == SecurityStatus.SAFE) IosIcons.ShieldCheck else IosIcons.ShieldWarning,
                        contentDescription = "Security Status",
                        tint = secTint,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = securityReport.headline,
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "Safety Score: ${securityReport.score}/100",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                if (securityReport.details.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = securityReport.details.first(),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Main Content Card
            IosCard(
                modifier = Modifier.fillMaxWidth(),
                contentPadding = 16.dp
            ) {
                Text(
                    text = "CONTENT",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = parsedData.displayValue,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                if (parsedData.secondaryValue.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = parsedData.secondaryValue,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                // Wi-Fi specific details
                if (parsedData.type == ParsedQrType.WIFI && !parsedData.wifiPass.isNullOrEmpty()) {
                    Spacer(modifier = Modifier.height(10.dp))
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                            .padding(horizontal = 12.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = if (showWifiPassword) parsedData.wifiPass else "Password: ••••••••",
                            style = MaterialTheme.typography.bodyMedium,
                            fontFamily = FontFamily.Monospace,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        IconButton(
                            onClick = { showWifiPassword = !showWifiPassword },
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(
                                imageVector = if (showWifiPassword) IosIcons.Check else IosIcons.Key,
                                contentDescription = "Toggle password",
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }

                // Raw Code Accordion
                Spacer(modifier = Modifier.height(12.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
                        .padding(10.dp)
                ) {
                    Text(
                        text = parsedData.rawValue,
                        style = MaterialTheme.typography.bodySmall,
                        fontFamily = FontFamily.Monospace,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 4
                    )
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            // Action Buttons
            if (parsedData.type == ParsedQrType.UPI) {
                IosButton(
                    text = "Pay via PhonePe / UPI",
                    onClick = {
                        try {
                            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(parsedData.rawValue))
                            context.startActivity(intent)
                        } catch (e: Exception) {
                            Toast.makeText(context, "No UPI payment app installed", Toast.LENGTH_SHORT).show()
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    isPrimary = true,
                    icon = {
                        Icon(
                            imageVector = IosIcons.ProBadge,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onPrimary,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                )
                Spacer(modifier = Modifier.height(10.dp))
            } else if (parsedData.type == ParsedQrType.URL && !parsedData.url.isNullOrEmpty()) {
                IosButton(
                    text = "Open in Browser",
                    onClick = {
                        try {
                            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(parsedData.url))
                            context.startActivity(intent)
                        } catch (e: Exception) {
                            Toast.makeText(context, "Cannot open URL", Toast.LENGTH_SHORT).show()
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    isPrimary = true,
                    icon = {
                        Icon(
                            imageVector = IosIcons.OpenLink,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onPrimary,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                )
                Spacer(modifier = Modifier.height(10.dp))
            } else if (parsedData.type == ParsedQrType.PHONE && !parsedData.phoneNumber.isNullOrEmpty()) {
                IosButton(
                    text = "Call ${parsedData.phoneNumber}",
                    onClick = {
                        try {
                            val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:${parsedData.phoneNumber}"))
                            context.startActivity(intent)
                        } catch (e: Exception) {
                            Toast.makeText(context, "Cannot dial phone", Toast.LENGTH_SHORT).show()
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    isPrimary = true,
                    icon = {
                        Icon(
                            imageVector = IosIcons.Phone,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onPrimary,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                )
                Spacer(modifier = Modifier.height(10.dp))
            }

            // Google Search Button
            IosButton(
                text = "Search on Google",
                onClick = {
                    try {
                        val query = if (parsedData.type == ParsedQrType.URL && !parsedData.url.isNullOrEmpty()) {
                            parsedData.url
                        } else {
                            parsedData.rawValue
                        }
                        val searchUri = Uri.parse("https://www.google.com/search?q=" + Uri.encode(query))
                        val intent = Intent(Intent.ACTION_VIEW, searchUri)
                        context.startActivity(intent)
                    } catch (e: Exception) {
                        Toast.makeText(context, "Could not open Google search", Toast.LENGTH_SHORT).show()
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                isPrimary = false,
                icon = {
                    Icon(
                        imageVector = IosIcons.OpenLink,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(18.dp)
                    )
                }
            )
            Spacer(modifier = Modifier.height(10.dp))

            // Copy & Share Row
            Row(modifier = Modifier.fillMaxWidth()) {
                IosButton(
                    text = "Copy",
                    onClick = {
                        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                        val clip = ClipData.newPlainText("AuraQR Scan", parsedData.rawValue)
                        clipboard.setPrimaryClip(clip)
                        Toast.makeText(context, "Copied to clipboard", Toast.LENGTH_SHORT).show()
                    },
                    modifier = Modifier.weight(1f),
                    isPrimary = false,
                    icon = {
                        Icon(
                            imageVector = IosIcons.Copy,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                )
                Spacer(modifier = Modifier.width(10.dp))
                IosButton(
                    text = "Share",
                    onClick = {
                        val intent = Intent(Intent.ACTION_SEND).apply {
                            type = "text/plain"
                            putExtra(Intent.EXTRA_TEXT, parsedData.rawValue)
                        }
                        context.startActivity(Intent.createChooser(intent, "Share QR Content"))
                    },
                    modifier = Modifier.weight(1f),
                    isPrimary = false,
                    icon = {
                        Icon(
                            imageVector = IosIcons.Share,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                )
            }

            // AI Deep Analysis Button
            Spacer(modifier = Modifier.height(12.dp))
            IosButton(
                text = "AI Deep Inspection",
                onClick = {
                    onTriggerAiAnalysis(parsedData.rawValue)
                    onDismiss()
                },
                modifier = Modifier.fillMaxWidth(),
                isPrimary = false,
                icon = {
                    Icon(
                        imageVector = IosIcons.AiSparkles,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(18.dp)
                    )
                }
            )
        }
    }
}
