package com.example.ui.screens

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
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
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.data.local.PreferencesManager
import com.example.data.remote.FirebaseManager
import com.example.ui.components.IosButton
import com.example.ui.components.IosCard
import com.example.ui.components.IosGroupedCard
import com.example.ui.components.IosGroupedCell
import com.example.ui.components.IosIcons
import com.example.ui.theme.ThemeMode
import kotlinx.coroutines.launch

@Composable
fun SettingsScreen(
    prefsManager: PreferencesManager,
    onOpenChatbot: () -> Unit,
    onShowLogin: (String) -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    val currentTheme by prefsManager.themeMode.collectAsState()
    val dailyScans by prefsManager.dailyScanCount.collectAsState()
    val isHaptic by prefsManager.isHapticEnabled.collectAsState()
    val isAutoCopy by prefsManager.isAutoCopyEnabled.collectAsState()
    val isSafeBrowsing by prefsManager.isSafeBrowsingEnabled.collectAsState()
    val isSound by prefsManager.isSoundEnabled.collectAsState()
    val userEmail by prefsManager.userEmail.collectAsState()
    val userName by prefsManager.userName.collectAsState()
    val isPro by prefsManager.isProUnlocked.collectAsState()

    val isLoggedIn = !userEmail.isNullOrBlank()

    var showEditProfileDialog by remember { mutableStateOf(false) }
    var editName by remember { mutableStateOf(userName) }
    var editEmail by remember { mutableStateOf(userEmail ?: "") }
    var showAboutDialog by remember { mutableStateOf(false) }
    var showPrivacyDialog by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .statusBarsPadding()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp)
            .padding(bottom = 100.dp)
    ) {
        Spacer(modifier = Modifier.height(16.dp))

        // Title
        Text(
            text = "Settings",
            style = MaterialTheme.typography.displayLarge,
            color = MaterialTheme.colorScheme.onBackground,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(18.dp))

        // 1. User Profile & Account Card
        IosCard(
            modifier = Modifier.fillMaxWidth(),
            contentPadding = 16.dp,
            onClick = {
                if (!isLoggedIn) {
                    onShowLogin("Sign in to access your profile and unlock Pro features.")
                } else {
                    editName = userName
                    editEmail = userEmail ?: ""
                    showEditProfileDialog = true
                }
            }
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(54.dp)
                        .clip(CircleShape)
                        .background(if (isLoggedIn) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (isLoggedIn) IosIcons.Contact else IosIcons.Lock,
                        contentDescription = "Avatar",
                        tint = if (isLoggedIn) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(28.dp)
                    )
                }

                Spacer(modifier = Modifier.width(14.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = if (isLoggedIn) userName else "Sign In to AuraQR",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        if (isLoggedIn) {
                            Spacer(modifier = Modifier.width(6.dp))
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.15f))
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    text = "PRO",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.primary,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(2.dp))

                    Text(
                        text = if (isLoggedIn) userEmail ?: "" else "Unlock unlimited scans, AI vision, and cloud sync",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Icon(
                    imageVector = IosIcons.Forward,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                    modifier = Modifier.size(18.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(22.dp))

        // 2. AI Assistant Section
        IosGroupedCard(title = "Intelligent Support") {
            IosGroupedCell(
                leadingIcon = {
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = IosIcons.ChatGuide,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                },
                title = "Aura Guide Chatbot",
                subtitle = "Ask AI to guide and explain all features",
                trailing = {
                    Icon(
                        imageVector = IosIcons.Forward,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                        modifier = Modifier.size(16.dp)
                    )
                },
                showDivider = false,
                onClick = onOpenChatbot
            )
        }

        Spacer(modifier = Modifier.height(22.dp))

        // 3. Daily Usage & Quotas (Advanced)
        IosGroupedCard(
            title = "Usage & Daily Limit",
            footer = if (isLoggedIn) "You have unlimited scans as an authenticated member." else "Free tier allows 5 scans per day. Sign in to remove limit."
        ) {
            IosGroupedCell(
                leadingIcon = {
                    Icon(
                        imageVector = IosIcons.Scanner,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(22.dp)
                    )
                },
                title = "Today's Scans",
                value = if (isLoggedIn) "$dailyScans (Unlimited)" else "$dailyScans / 5 Used",
                showDivider = true
            )
            IosGroupedCell(
                leadingIcon = {
                    Icon(
                        imageVector = IosIcons.ProBadge,
                        contentDescription = null,
                        tint = if (isLoggedIn) MaterialTheme.colorScheme.tertiary else MaterialTheme.colorScheme.error,
                        modifier = Modifier.size(22.dp)
                    )
                },
                title = "Account Status",
                value = if (isLoggedIn) "Pro Active" else "Free Tier",
                showDivider = false,
                onClick = {
                    if (!isLoggedIn) onShowLogin("Unlock Pro features today!")
                }
            )
        }

        Spacer(modifier = Modifier.height(22.dp))

        // 4. Appearance & Preferences
        IosGroupedCard(title = "Appearance & Interface") {
            IosGroupedCell(
                title = "Theme Appearance",
                subtitle = when (currentTheme) {
                    ThemeMode.SYSTEM -> "Follows device system settings"
                    ThemeMode.LIGHT -> "Comfortable soft light mode"
                    ThemeMode.DARK -> "OLED midnight dark mode"
                },
                value = currentTheme.name,
                trailing = {
                    IconButton(
                        onClick = {
                            val nextMode = when (currentTheme) {
                                ThemeMode.SYSTEM -> ThemeMode.LIGHT
                                ThemeMode.LIGHT -> ThemeMode.DARK
                                ThemeMode.DARK -> ThemeMode.SYSTEM
                            }
                            prefsManager.setThemeMode(nextMode)
                        }
                    ) {
                        Icon(
                            imageVector = IosIcons.Refresh,
                            contentDescription = "Switch theme",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                },
                showDivider = true
            )

            IosGroupedCell(
                title = "Haptic Tactile Feedback",
                subtitle = "Vibrate subtly on successful scan",
                trailing = {
                    Switch(
                        checked = isHaptic,
                        onCheckedChange = { prefsManager.setHapticEnabled(it) },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = MaterialTheme.colorScheme.onPrimary,
                            checkedTrackColor = MaterialTheme.colorScheme.primary
                        )
                    )
                },
                showDivider = true
            )

            IosGroupedCell(
                title = "Safe Browsing Radar",
                subtitle = "Local phishing and IP host warning",
                trailing = {
                    Switch(
                        checked = isSafeBrowsing,
                        onCheckedChange = { prefsManager.setSafeBrowsingEnabled(it) },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = MaterialTheme.colorScheme.onPrimary,
                            checkedTrackColor = MaterialTheme.colorScheme.primary
                        )
                    )
                },
                showDivider = true
            )

            IosGroupedCell(
                title = "Auto-Copy to Clipboard",
                subtitle = "Instantly copy decoded text",
                trailing = {
                    Switch(
                        checked = isAutoCopy,
                        onCheckedChange = { prefsManager.setAutoCopyEnabled(it) },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = MaterialTheme.colorScheme.onPrimary,
                            checkedTrackColor = MaterialTheme.colorScheme.primary
                        )
                    )
                },
                showDivider = false
            )
        }

        Spacer(modifier = Modifier.height(22.dp))

        // 5. Cloud & Data Sync
        IosGroupedCard(title = "Data & Cloud Persistence") {
            IosGroupedCell(
                leadingIcon = {
                    Icon(
                        imageVector = IosIcons.Refresh,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(22.dp)
                    )
                },
                title = "Sync to Firestore",
                subtitle = "Backup scan history to cloud",
                trailing = {
                    Text(
                        text = if (isLoggedIn) "Enabled" else "Login Req.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                },
                showDivider = true,
                onClick = {
                    if (!isLoggedIn) {
                        onShowLogin("Sign in to sync your scans to Firestore.")
                    } else {
                        Toast.makeText(context, "Cloud sync is up to date!", Toast.LENGTH_SHORT).show()
                    }
                }
            )

            if (isLoggedIn) {
                IosGroupedCell(
                    leadingIcon = {
                        Icon(
                            imageVector = IosIcons.Delete,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.error,
                            modifier = Modifier.size(22.dp)
                        )
                    },
                    title = "Sign Out",
                    subtitle = "Log out from this device",
                    showDivider = false,
                    onClick = {
                        FirebaseManager.signOut()
                        prefsManager.logout()
                        Toast.makeText(context, "Signed out successfully", Toast.LENGTH_SHORT).show()
                    }
                )
            }
        }

        Spacer(modifier = Modifier.height(22.dp))

        // 6. About & Legal
        IosGroupedCard(
            title = "About & Compliance",
            footer = "SimpaQr v1.0 • Built with Kotlin & Jetpack Compose"
        ) {
            IosGroupedCell(
                leadingIcon = {
                    Icon(
                        imageVector = IosIcons.Info,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(22.dp)
                    )
                },
                title = "About SimpaQr",
                subtitle = "GitHub repo & app details (simpaqr-about.html)",
                trailing = {
                    Icon(
                        imageVector = IosIcons.Forward,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                        modifier = Modifier.size(16.dp)
                    )
                },
                showDivider = true,
                onClick = { showAboutDialog = true }
            )

            IosGroupedCell(
                leadingIcon = {
                    Icon(
                        imageVector = IosIcons.Privacy,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.tertiary,
                        modifier = Modifier.size(22.dp)
                    )
                },
                title = "Privacy Policy",
                subtitle = "Zero-tracking privacy terms (simpaqr-privacy.html)",
                trailing = {
                    Icon(
                        imageVector = IosIcons.Forward,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                        modifier = Modifier.size(16.dp)
                    )
                },
                showDivider = false,
                onClick = { showPrivacyDialog = true }
            )
        }
    }

    // Edit Profile Dialog
    if (showEditProfileDialog) {
        AlertDialog(
            onDismissRequest = { showEditProfileDialog = false },
            title = { Text("Edit Profile") },
            text = {
                Column {
                    OutlinedTextField(
                        value = editName,
                        onValueChange = { editName = it },
                        label = { Text("Display Name") },
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    OutlinedTextField(
                        value = editEmail,
                        onValueChange = { editEmail = it },
                        label = { Text("Email") },
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        prefsManager.setUserProfile(editEmail.trim(), editName.trim(), true)
                        showEditProfileDialog = false
                        Toast.makeText(context, "Profile updated", Toast.LENGTH_SHORT).show()
                    }
                ) {
                    Text("Save", color = MaterialTheme.colorScheme.primary)
                }
            },
            dismissButton = {
                TextButton(onClick = { showEditProfileDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    // About SimpaQr Dialog
    if (showAboutDialog) {
        AlertDialog(
            onDismissRequest = { showAboutDialog = false },
            title = { Text("About SimpaQr") },
            text = {
                Column {
                    Text("SimpaQr is an ultra-clean, Apple card-inspired barcode and QR intelligence suite.")
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("• 100% on-device offline barcode reading & generation")
                    Text("• 1-Tap UPI payment launches (PhonePe, GPay, Paytm)")
                    Text("• 10+ Studio Tools (AI visual analyzer, document AI, Wi-Fi, vCard)")
                    Text("• Local Threat Radar for link safety inspection")
                    Text("• Gemini 3.1 Pro High Thinking visual intelligence")
                    Text("• Multi-turn Simpa Guide chatbot")
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("GitHub Repository: simpaqr/simpaqr-about.html")
                }
            },
            confirmButton = {
                TextButton(onClick = { showAboutDialog = false }) {
                    Text("Done")
                }
            }
        )
    }

    // Privacy Dialog
    if (showPrivacyDialog) {
        AlertDialog(
            onDismissRequest = { showPrivacyDialog = false },
            title = { Text("Privacy Policy") },
            text = {
                Column {
                    Text("AuraQR was built with complete privacy in mind.")
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("• No camera streams or gallery photos are collected.")
                    Text("• Barcodes and QR codes are processed locally in memory.")
                    Text("• Optional Google Sign-In & Firestore sync for your convenience.")
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Compliant with Google Play Store policies (auraqr-privacy.html).")
                }
            },
            confirmButton = {
                TextButton(onClick = { showPrivacyDialog = false }) {
                    Text("Done")
                }
            }
        )
    }
}
