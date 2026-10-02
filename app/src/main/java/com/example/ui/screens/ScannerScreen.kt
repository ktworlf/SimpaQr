package com.example.ui.screens

import android.Manifest
import android.content.Context
import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.Camera
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
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
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import com.example.data.local.PreferencesManager
import com.example.data.model.ScanRecord
import com.example.ui.components.IosButton
import com.example.ui.components.IosIcons
import com.example.util.HapticUtil
import com.example.util.QrCodeUtil
import com.example.util.QrTypeParser
import com.example.util.SecurityScanner
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.concurrent.Executors

@Composable
fun ScannerScreen(
    prefsManager: PreferencesManager,
    onScanDetected: (ScanRecord) -> Unit,
    onShowLogin: (String) -> Unit,
    onOpenAiAnalyzer: () -> Unit
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val scope = rememberCoroutineScope()

    var hasCameraPermission by remember { mutableStateOf(false) }
    var isFlashOn by remember { mutableStateOf(false) }
    var isFrontCamera by remember { mutableStateOf(false) }
    var zoomRatio by remember { mutableFloatStateOf(1f) }
    var cameraControl: Camera? by remember { mutableStateOf(null) }
    var isBatchMode by remember { mutableStateOf(false) }
    var batchCount by remember { mutableIntStateOf(0) }
    var isProcessingGallery by remember { mutableStateOf(false) }

    // Quota state
    var canScan by remember { mutableStateOf(prefsManager.canUserScan()) }
    var remainingScans by remember { mutableIntStateOf(prefsManager.remainingScans()) }
    val isPro = prefsManager.isProUnlocked.value || !prefsManager.userEmail.value.isNullOrBlank()

    // Permission launcher
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        hasCameraPermission = isGranted
    }

    LaunchedEffect(Unit) {
        permissionLauncher.launch(Manifest.permission.CAMERA)
    }

    // Gallery Picker launcher
    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            isProcessingGallery = true
            scope.launch(Dispatchers.IO) {
                try {
                    val bitmap: Bitmap = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                        ImageDecoder.decodeBitmap(ImageDecoder.createSource(context.contentResolver, uri)) { decoder, _, _ ->
                            decoder.isMutableRequired = true
                        }
                    } else {
                        @Suppress("DEPRECATION")
                        MediaStore.Images.Media.getBitmap(context.contentResolver, uri)
                    }

                    val decodedText = QrCodeUtil.decodeBitmap(bitmap)
                    withContext(Dispatchers.Main) {
                        isProcessingGallery = false
                        if (decodedText != null) {
                            if (!prefsManager.canUserScan()) {
                                onShowLogin("Daily scan limit (5/5) reached. Sign in for unlimited scans!")
                                return@withContext
                            }
                            prefsManager.incrementDailyScan()
                            canScan = prefsManager.canUserScan()
                            remainingScans = prefsManager.remainingScans()

                            HapticUtil.triggerScanSuccess(context)
                            val parsed = QrTypeParser.parse(decodedText)
                            val report = SecurityScanner.inspect(decodedText)
                            val record = ScanRecord(
                                rawContent = decodedText,
                                title = parsed.title,
                                subtitle = parsed.displayValue,
                                qrType = parsed.type.name,
                                scanSource = "GALLERY",
                                securityStatus = report.status.name,
                                safetyNotes = report.headline
                            )
                            onScanDetected(record)
                        } else {
                            Toast.makeText(context, "No QR or barcode found in image", Toast.LENGTH_SHORT).show()
                        }
                    }
                } catch (e: Exception) {
                    withContext(Dispatchers.Main) {
                        isProcessingGallery = false
                        Toast.makeText(context, "Could not read image", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
    }

    val cameraExecutor = remember { Executors.newSingleThreadExecutor() }
    DisposableEffect(Unit) {
        onDispose {
            cameraExecutor.shutdown()
        }
    }

    // Scanning Reticle Laser animation
    val infiniteTransition = rememberInfiniteTransition(label = "laser")
    val laserY by infiniteTransition.animateFloat(
        initialValue = 0.1f,
        targetValue = 0.9f,
        animationSpec = infiniteRepeatable(
            animation = tween(1800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "laser_pos"
    )

    var lastScannedContent by remember { mutableStateOf("") }
    var lastScannedTimestamp by remember { mutableStateOf(0L) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
    ) {
        if (hasCameraPermission) {
            AndroidView(
                factory = { ctx ->
                    val previewView = PreviewView(ctx).apply {
                        scaleType = PreviewView.ScaleType.FILL_CENTER
                    }

                    val cameraProviderFuture = ProcessCameraProvider.getInstance(ctx)
                    cameraProviderFuture.addListener({
                        val cameraProvider = cameraProviderFuture.get()
                        val preview = Preview.Builder().build().also {
                            it.setSurfaceProvider(previewView.surfaceProvider)
                        }

                        val cameraSelector = if (isFrontCamera) {
                            CameraSelector.DEFAULT_FRONT_CAMERA
                        } else {
                            CameraSelector.DEFAULT_BACK_CAMERA
                        }

                        val imageAnalysis = ImageAnalysis.Builder()
                            .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                            .build()

                        imageAnalysis.setAnalyzer(cameraExecutor) { imageProxy ->
                            val result = QrCodeUtil.decodeImageProxy(imageProxy)
                            if (result != null) {
                                val now = System.currentTimeMillis()
                                // De-bounce duplicates within 2.5 seconds unless in batch mode
                                if (result != lastScannedContent || (now - lastScannedTimestamp > 2500)) {
                                    lastScannedContent = result
                                    lastScannedTimestamp = now

                                    scope.launch(Dispatchers.Main) {
                                        if (!prefsManager.canUserScan()) {
                                            onShowLogin("Daily scan limit (5/5) reached. Sign in for unlimited scans!")
                                            return@launch
                                        }

                                        prefsManager.incrementDailyScan()
                                        canScan = prefsManager.canUserScan()
                                        remainingScans = prefsManager.remainingScans()

                                        if (prefsManager.isHapticEnabled.value) {
                                            HapticUtil.triggerScanSuccess(context)
                                        }

                                        if (isBatchMode) {
                                            batchCount++
                                        }

                                        val parsed = QrTypeParser.parse(result)
                                        val secReport = SecurityScanner.inspect(result)
                                        val record = ScanRecord(
                                            rawContent = result,
                                            title = parsed.title,
                                            subtitle = parsed.displayValue,
                                            qrType = parsed.type.name,
                                            scanSource = if (isBatchMode) "BATCH" else "CAMERA",
                                            securityStatus = secReport.status.name,
                                            safetyNotes = secReport.headline
                                        )
                                        onScanDetected(record)
                                    }
                                }
                            }
                        }

                        try {
                            cameraProvider.unbindAll()
                            val cam = cameraProvider.bindToLifecycle(
                                lifecycleOwner,
                                cameraSelector,
                                preview,
                                imageAnalysis
                            )
                            cameraControl = cam
                        } catch (exc: Exception) {
                            exc.printStackTrace()
                        }
                    }, ContextCompat.getMainExecutor(ctx))

                    previewView
                },
                modifier = Modifier.fillMaxSize()
            )
        } else {
            // Permission request screen
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(32.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Icon(
                    imageVector = IosIcons.Scanner,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(64.dp)
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "Camera Access Required",
                    style = MaterialTheme.typography.titleLarge,
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "AuraQR processes all barcodes locally on your device for complete privacy.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.White.copy(alpha = 0.7f),
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                )
                Spacer(modifier = Modifier.height(24.dp))
                IosButton(
                    text = "Grant Permission",
                    onClick = { permissionLauncher.launch(Manifest.permission.CAMERA) },
                    modifier = Modifier.fillMaxWidth(0.7f),
                    isPrimary = true
                )
            }
        }

        // Viewfinder Cutout & Laser Canvas
        Canvas(modifier = Modifier.fillMaxSize()) {
            val boxWidth = size.width * 0.72f
            val boxHeight = boxWidth
            val left = (size.width - boxWidth) / 2
            val top = (size.height - boxHeight) / 2 - 40.dp.toPx()

            // Draw viewfinder rounded corner brackets
            val cornerLen = 32.dp.toPx()
            val strokeW = 4.dp.toPx()
            val bracketColor = Color(0xFF0A84FF)

            // Top-Left
            drawLine(bracketColor, Offset(left, top), Offset(left + cornerLen, top), strokeW)
            drawLine(bracketColor, Offset(left, top), Offset(left, top + cornerLen), strokeW)
            // Top-Right
            drawLine(bracketColor, Offset(left + boxWidth, top), Offset(left + boxWidth - cornerLen, top), strokeW)
            drawLine(bracketColor, Offset(left + boxWidth, top), Offset(left + boxWidth, top + cornerLen), strokeW)
            // Bottom-Left
            drawLine(bracketColor, Offset(left, top + boxHeight), Offset(left + cornerLen, top + boxHeight), strokeW)
            drawLine(bracketColor, Offset(left, top + boxHeight), Offset(left, top + boxHeight - cornerLen), strokeW)
            // Bottom-Right
            drawLine(bracketColor, Offset(left + boxWidth, top + boxHeight), Offset(left + boxWidth - cornerLen, top + boxHeight), strokeW)
            drawLine(bracketColor, Offset(left + boxWidth, top + boxHeight), Offset(left + boxWidth, top + boxHeight - cornerLen), strokeW)

            // Scanning Laser Line
            val currentLaserY = top + (boxHeight * laserY)
            drawLine(
                brush = Brush.horizontalGradient(
                    listOf(
                        Color.Transparent,
                        Color(0xFF38BDF8),
                        Color(0xFF0A84FF),
                        Color(0xFF38BDF8),
                        Color.Transparent
                    )
                ),
                start = Offset(left + 8.dp.toPx(), currentLaserY),
                end = Offset(left + boxWidth - 8.dp.toPx(), currentLaserY),
                strokeWidth = 3.dp.toPx()
            )
        }

        // Top Navigation & Quota Overlay
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(horizontal = 16.dp, vertical = 12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Quota Pill Badge
                Box(
                    modifier = Modifier
                        .clip(CircleShape)
                        .background(Color.Black.copy(alpha = 0.6f))
                        .clickable {
                            if (!isPro) {
                                onShowLogin("Sign in to unlock Unlimited Scans & Cloud Sync")
                            }
                        }
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = if (isPro) IosIcons.ProBadge else IosIcons.Lock,
                            contentDescription = null,
                            tint = if (isPro) Color(0xFFFBBF24) else Color(0xFF60A5FA),
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = if (isPro) "PRO UNLIMITED" else "$remainingScans/5 FREE SCANS",
                            style = MaterialTheme.typography.labelSmall,
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 0.5.sp
                        )
                    }
                }

                // Top Controls: Flash, Flip, AI Vision
                Row {
                    // Flash Toggle
                    IconButton(
                        onClick = {
                            isFlashOn = !isFlashOn
                            cameraControl?.cameraControl?.enableTorch(isFlashOn)
                        },
                        modifier = Modifier
                            .size(38.dp)
                            .clip(CircleShape)
                            .background(Color.Black.copy(alpha = 0.55f))
                    ) {
                        Icon(
                            imageVector = if (isFlashOn) IosIcons.FlashOn else IosIcons.FlashOff,
                            contentDescription = "Torch",
                            tint = if (isFlashOn) Color(0xFFFBBF24) else Color.White,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    // Flip Camera
                    IconButton(
                        onClick = {
                            isFrontCamera = !isFrontCamera
                        },
                        modifier = Modifier
                            .size(38.dp)
                            .clip(CircleShape)
                            .background(Color.Black.copy(alpha = 0.55f))
                    ) {
                        Icon(
                            imageVector = IosIcons.FlipCamera,
                            contentDescription = "Flip Camera",
                            tint = Color.White,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    // AI Analyzer Shortcut
                    IconButton(
                        onClick = onOpenAiAnalyzer,
                        modifier = Modifier
                            .size(38.dp)
                            .clip(CircleShape)
                            .background(Color.Black.copy(alpha = 0.55f))
                    ) {
                        Icon(
                            imageVector = IosIcons.AiSparkles,
                            contentDescription = "AI Image Analyzer",
                            tint = Color(0xFF38BDF8),
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }

            if (isBatchMode) {
                Spacer(modifier = Modifier.height(10.dp))
                Box(
                    modifier = Modifier
                        .align(Alignment.CenterHorizontally)
                        .clip(CircleShape)
                        .background(Color(0xFF059669).copy(alpha = 0.85f))
                        .padding(horizontal = 14.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = "Batch Mode Active • $batchCount scanned",
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        // Bottom Controls: Gallery Picker, Zoom, Batch Toggle
        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .navigationBarsPadding()
                .padding(bottom = 20.dp, start = 20.dp, end = 20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Zoom Stepper (1x, 2x, 3x)
            Row(
                modifier = Modifier
                    .clip(CircleShape)
                    .background(Color.Black.copy(alpha = 0.6f))
                    .padding(horizontal = 6.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                listOf(1f, 2f, 3f).forEach { zoom ->
                    val isSelected = zoomRatio == zoom
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(if (isSelected) Color.White.copy(alpha = 0.25f) else Color.Transparent)
                            .clickable {
                                zoomRatio = zoom
                                cameraControl?.cameraControl?.setZoomRatio(zoom)
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "${zoom.toInt()}x",
                            style = MaterialTheme.typography.labelSmall,
                            color = if (isSelected) Color(0xFF38BDF8) else Color.White,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Lower Action Row: Gallery button & Batch toggle
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Import from Gallery Button
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    IconButton(
                        onClick = { galleryLauncher.launch("image/*") },
                        modifier = Modifier
                            .size(54.dp)
                            .clip(CircleShape)
                            .background(Color.Black.copy(alpha = 0.6f))
                    ) {
                        if (isProcessingGallery) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(24.dp),
                                color = Color.White,
                                strokeWidth = 2.dp
                            )
                        } else {
                            Icon(
                                imageVector = IosIcons.Gallery,
                                contentDescription = "Scan Image",
                                tint = Color.White,
                                modifier = Modifier.size(26.dp)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Photos",
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.White.copy(alpha = 0.8f)
                    )
                }

                // Batch Mode Toggle Button
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    IconButton(
                        onClick = {
                            isBatchMode = !isBatchMode
                            if (!isBatchMode) batchCount = 0
                        },
                        modifier = Modifier
                            .size(54.dp)
                            .clip(CircleShape)
                            .background(
                                if (isBatchMode) Color(0xFF059669) else Color.Black.copy(alpha = 0.6f)
                            )
                    ) {
                        Icon(
                            imageVector = IosIcons.Refresh,
                            contentDescription = "Batch Mode",
                            tint = Color.White,
                            modifier = Modifier.size(26.dp)
                        )
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = if (isBatchMode) "Batch On" else "Batch Mode",
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.White.copy(alpha = 0.8f)
                    )
                }
            }
        }
    }
}
