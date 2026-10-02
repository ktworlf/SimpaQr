package com.example.ui.screens.studio

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.IosButton
import com.example.ui.components.IosCard
import com.example.ui.components.IosIcons
import com.example.util.SecurityReport
import com.example.util.SecurityScanner
import com.example.util.SecurityStatus

@Composable
fun ThreatRadarStudioScreen(
    onBack: () -> Unit
) {
    val context = LocalContext.current
    var urlToTest by remember { mutableStateOf("https://") }
    var report by remember { mutableStateOf<SecurityReport?>(null) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .statusBarsPadding()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp)
            .padding(bottom = 90.dp)
    ) {
        Spacer(modifier = Modifier.height(14.dp))

        // Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = onBack,
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Icon(
                    imageVector = IosIcons.Back,
                    contentDescription = "Back",
                    tint = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.size(20.dp)
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(
                    text = "Link Threat Radar",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Text(
                    text = "Local phishing, IP spoofing, & SSL audit",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }

        Spacer(modifier = Modifier.height(18.dp))

        // Input Card
        IosCard(
            modifier = Modifier.fillMaxWidth(),
            contentPadding = 16.dp
        ) {
            OutlinedTextField(
                value = urlToTest,
                onValueChange = {
                    urlToTest = it
                    report = null
                },
                label = { Text("Paste Link or Domain to Inspect") },
                singleLine = true,
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
                )
            )

            Spacer(modifier = Modifier.height(14.dp))

            IosButton(
                text = "Run Security Audit",
                onClick = {
                    report = SecurityScanner.inspect(urlToTest.trim())
                },
                modifier = Modifier.fillMaxWidth(),
                isPrimary = true,
                icon = {
                    Icon(
                        imageVector = IosIcons.ShieldCheck,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onPrimary,
                        modifier = Modifier.size(18.dp)
                    )
                }
            )
        }

        if (report != null) {
            val r = report!!
            Spacer(modifier = Modifier.height(18.dp))

            val secBg = when (r.status) {
                SecurityStatus.SAFE -> MaterialTheme.colorScheme.tertiary.copy(alpha = 0.12f)
                SecurityStatus.CAUTION -> MaterialTheme.colorScheme.error.copy(alpha = 0.12f)
                SecurityStatus.DANGER -> MaterialTheme.colorScheme.error.copy(alpha = 0.2f)
                SecurityStatus.NEUTRAL -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
            }
            val secTint = when (r.status) {
                SecurityStatus.SAFE -> MaterialTheme.colorScheme.tertiary
                SecurityStatus.CAUTION, SecurityStatus.DANGER -> MaterialTheme.colorScheme.error
                SecurityStatus.NEUTRAL -> MaterialTheme.colorScheme.primary
            }

            IosCard(
                modifier = Modifier.fillMaxWidth(),
                backgroundColor = secBg,
                borderColor = secTint.copy(alpha = 0.4f),
                contentPadding = 18.dp
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = if (r.status == SecurityStatus.SAFE) IosIcons.ShieldCheck else IosIcons.ShieldWarning,
                        contentDescription = null,
                        tint = secTint,
                        modifier = Modifier.size(28.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = r.headline,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "Safety Trust Index: ${r.score} / 100",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                Text(
                    text = "AUDIT BREAKDOWN",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(6.dp))

                r.details.forEach { detail ->
                    Row(
                        modifier = Modifier.padding(vertical = 3.dp),
                        verticalAlignment = Alignment.Top
                    ) {
                        Text("• ", color = secTint, fontWeight = FontWeight.Bold)
                        Text(
                            text = detail,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface,
                            lineHeight = 20.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Google Search Verification Button
                IosButton(
                    text = "Verify on Google Search",
                    onClick = {
                        val searchUri = Uri.parse("https://www.google.com/search?q=" + Uri.encode(r.domain ?: urlToTest))
                        context.startActivity(Intent(Intent.ACTION_VIEW, searchUri))
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
            }
        }
    }
}
