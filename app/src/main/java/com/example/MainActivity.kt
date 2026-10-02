package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.example.data.local.AppDatabase
import com.example.data.local.PreferencesManager
import com.example.data.model.ScanRecord
import com.example.data.remote.FirebaseManager
import com.example.ui.components.BespokeFloatingNavBar
import com.example.ui.components.LoginBottomSheet
import com.example.ui.screens.AiAnalyzerScreen
import com.example.ui.screens.AuraGuideChatScreen
import com.example.ui.screens.HistoryScreen
import com.example.ui.screens.ScanResultSheet
import com.example.ui.screens.ScannerScreen
import com.example.ui.screens.SettingsScreen
import com.example.ui.screens.SplashScreen
import com.example.ui.screens.StudioScreen
import com.example.ui.screens.StudioTool
import com.example.ui.screens.studio.BatchInventoryScreen
import com.example.ui.screens.studio.PdfDocAnalyzerScreen
import com.example.ui.screens.studio.QrDesignerScreen
import com.example.ui.screens.studio.SecretLockerScreen
import com.example.ui.screens.studio.ThreatRadarStudioScreen
import com.example.ui.screens.studio.UpiStudioScreen
import com.example.ui.screens.studio.VcardStudioScreen
import com.example.ui.screens.studio.WifiStudioScreen
import com.example.ui.theme.SimpaQrTheme
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val prefsManager = PreferencesManager(this)
        val database = AppDatabase.getInstance(this)
        val scanDao = database.scanDao()

        setContent {
            val themeMode by prefsManager.themeMode.collectAsState()

            SimpaQrTheme(themeMode = themeMode) {
                var isSplashActive by remember { mutableStateOf(true) }

                if (isSplashActive) {
                    SplashScreen(
                        onSplashFinished = { isSplashActive = false }
                    )
                } else {
                    SimpaMainApp(
                        prefsManager = prefsManager,
                        scanDao = scanDao
                    )
                }
            }
        }
    }
}

@Composable
fun SimpaMainApp(
    prefsManager: PreferencesManager,
    scanDao: com.example.data.local.ScanDao
) {
    val scope = rememberCoroutineScope()

    // 0: Home/Scan, 1: Studio, 2: Create, 3: Activity/History, 4: Profile/Settings
    var selectedTabIndex by remember { mutableIntStateOf(0) }
    var activeStudioTool by remember { mutableStateOf<StudioTool?>(null) }
    var activeScanRecord by remember { mutableStateOf<ScanRecord?>(null) }
    var showLoginSheet by remember { mutableStateOf(false) }
    var loginPromptReason by remember { mutableStateOf("Sign In to unlock Unlimited Scans & Pro Features") }

    val userEmail by prefsManager.userEmail.collectAsState()
    val isLoggedIn = !userEmail.isNullOrBlank()

    val totalScansCount by scanDao.getTotalScansCount().collectAsState(initial = 0)

    // Back handler for studio sub-screens
    if (activeStudioTool != null) {
        BackHandler {
            activeStudioTool = null
        }
    } else if (selectedTabIndex != 0) {
        BackHandler {
            selectedTabIndex = 0
        }
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Main Content Area with smooth crossfade
            AnimatedContent(
                targetState = Pair(selectedTabIndex, activeStudioTool),
                transitionSpec = { fadeIn() togetherWith fadeOut() },
                label = "screen_transition"
            ) { (tabIndex, tool) ->
                if (tool != null) {
                    when (tool) {
                        StudioTool.AI_IMAGE -> {
                            AiAnalyzerScreen(
                                onBack = { activeStudioTool = null }
                            )
                        }
                        StudioTool.PDF_DOCUMENT -> {
                            PdfDocAnalyzerScreen(
                                onBack = { activeStudioTool = null }
                            )
                        }
                        StudioTool.UPI_PAYMENT -> {
                            UpiStudioScreen(
                                onBack = { activeStudioTool = null },
                                onSaveToHistory = { record ->
                                    scope.launch {
                                        val id = scanDao.insertScan(record)
                                        FirebaseManager.syncScanToFirestore(record.copy(id = id))
                                    }
                                }
                            )
                        }
                        StudioTool.QR_DESIGNER -> {
                            QrDesignerScreen(
                                onBack = { activeStudioTool = null },
                                onSaveToHistory = { record ->
                                    scope.launch {
                                        val id = scanDao.insertScan(record)
                                        FirebaseManager.syncScanToFirestore(record.copy(id = id))
                                    }
                                }
                            )
                        }
                        StudioTool.WIFI_HUB -> {
                            WifiStudioScreen(
                                onBack = { activeStudioTool = null },
                                onSaveToHistory = { record ->
                                    scope.launch {
                                        val id = scanDao.insertScan(record)
                                        FirebaseManager.syncScanToFirestore(record.copy(id = id))
                                    }
                                }
                            )
                        }
                        StudioTool.VCARD_CARD -> {
                            VcardStudioScreen(
                                onBack = { activeStudioTool = null },
                                onSaveToHistory = { record ->
                                    scope.launch {
                                        val id = scanDao.insertScan(record)
                                        FirebaseManager.syncScanToFirestore(record.copy(id = id))
                                    }
                                }
                            )
                        }
                        StudioTool.THREAT_RADAR -> {
                            ThreatRadarStudioScreen(
                                onBack = { activeStudioTool = null }
                            )
                        }
                        StudioTool.BATCH_INVENTORY -> {
                            BatchInventoryScreen(
                                scanDao = scanDao,
                                onBack = { activeStudioTool = null },
                                onLaunchScanner = {
                                    activeStudioTool = null
                                    selectedTabIndex = 0
                                }
                            )
                        }
                        StudioTool.SECRET_LOCKER -> {
                            SecretLockerScreen(
                                onBack = { activeStudioTool = null },
                                onSaveToHistory = { record ->
                                    scope.launch {
                                        val id = scanDao.insertScan(record)
                                        FirebaseManager.syncScanToFirestore(record.copy(id = id))
                                    }
                                }
                            )
                        }
                        StudioTool.AI_GUIDE -> {
                            AuraGuideChatScreen(
                                onBack = { activeStudioTool = null }
                            )
                        }
                    }
                } else {
                    when (tabIndex) {
                        0 -> {
                            ScannerScreen(
                                prefsManager = prefsManager,
                                onScanDetected = { record ->
                                    scope.launch {
                                        val id = scanDao.insertScan(record)
                                        val saved = record.copy(id = id)
                                        activeScanRecord = saved
                                        FirebaseManager.syncScanToFirestore(saved)
                                    }
                                },
                                onShowLogin = { reason ->
                                    loginPromptReason = reason
                                    showLoginSheet = true
                                },
                                onOpenAiAnalyzer = {
                                    activeStudioTool = StudioTool.AI_IMAGE
                                }
                            )
                        }

                        1 -> {
                            StudioScreen(
                                onSelectTool = { selectedTool ->
                                    activeStudioTool = selectedTool
                                }
                            )
                        }

                        2 -> {
                            // Center "Create" tab: directly launches Smart QR Designer!
                            QrDesignerScreen(
                                onBack = { selectedTabIndex = 0 },
                                onSaveToHistory = { record ->
                                    scope.launch {
                                        val id = scanDao.insertScan(record)
                                        FirebaseManager.syncScanToFirestore(record.copy(id = id))
                                    }
                                }
                            )
                        }

                        3 -> {
                            HistoryScreen(
                                scanDao = scanDao,
                                onSelectScan = { record ->
                                    activeScanRecord = record
                                }
                            )
                        }

                        4 -> {
                            SettingsScreen(
                                prefsManager = prefsManager,
                                onOpenChatbot = {
                                    activeStudioTool = StudioTool.AI_GUIDE
                                },
                                onShowLogin = { reason ->
                                    loginPromptReason = reason
                                    showLoginSheet = true
                                }
                            )
                        }
                    }
                }
            }

            // Bespoke Floating Navigation Bar (Only visible when on top-level tabs)
            if (activeStudioTool == null) {
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .navigationBarsPadding()
                ) {
                    BespokeFloatingNavBar(
                        selectedIndex = selectedTabIndex,
                        onTabSelected = { newIndex ->
                            if (newIndex == 4 && !isLoggedIn) {
                                loginPromptReason = "Sign in to access your Settings and manage Pro features."
                                showLoginSheet = true
                            }
                            selectedTabIndex = newIndex
                        },
                        activityBadgeCount = totalScansCount
                    )
                }
            }
        }
    }

    // Modal Scan Result Sheet
    if (activeScanRecord != null) {
        ScanResultSheet(
            rawResult = activeScanRecord!!.rawContent,
            onDismiss = { activeScanRecord = null },
            onTriggerAiAnalysis = { _ ->
                activeScanRecord = null
                activeStudioTool = StudioTool.AI_IMAGE
            },
            onToggleFavorite = { isFav ->
                val current = activeScanRecord ?: return@ScanResultSheet
                scope.launch {
                    val updated = current.copy(isFavorite = isFav)
                    scanDao.updateScan(updated)
                    activeScanRecord = updated
                }
            }
        )
    }

    // Modal Login Bottom Sheet
    if (showLoginSheet) {
        LoginBottomSheet(
            onDismiss = { showLoginSheet = false },
            onSuccess = { email, name ->
                prefsManager.setUserProfile(email, name, true)
                showLoginSheet = false
            },
            triggerReason = loginPromptReason
        )
    }
}
