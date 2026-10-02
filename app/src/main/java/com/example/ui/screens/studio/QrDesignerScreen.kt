package com.example.ui.screens.studio

import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Color as AndroidColor
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.widget.Toast
import androidx.compose.foundation.Image
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
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.data.model.ParsedQrType
import com.example.data.model.ScanRecord
import com.example.ui.components.IosButton
import com.example.ui.components.IosCard
import com.example.ui.components.IosIcons
import com.example.ui.components.IosPillChip
import com.example.util.QrCodeUtil
import java.io.OutputStream

@Composable
fun QrDesignerScreen(
    onBack: () -> Unit,
    onSaveToHistory: (ScanRecord) -> Unit
) {
    val context = LocalContext.current
    var selectedType by remember { mutableStateOf(ParsedQrType.URL) }

    // Input fields
    var urlText by remember { mutableStateOf("https://") }
    var plainText by remember { mutableStateOf("") }
    var phoneNumber by remember { mutableStateOf("") }
    var emailAddress by remember { mutableStateOf("") }

    // Palette choices
    val colorOptions = listOf(
        Pair("Indigo", AndroidColor.parseColor("#4F46E5")),
        Pair("Obsidian", AndroidColor.parseColor("#1C1C1E")),
        Pair("Sapphire", AndroidColor.parseColor("#007AFF")),
        Pair("Emerald", AndroidColor.parseColor("#10B981")),
        Pair("Crimson", AndroidColor.parseColor("#EF4444"))
    )
    var selectedColorIndex by remember { mutableIntStateOf(0) }

    val encodedContent = remember(selectedType, urlText, plainText, phoneNumber, emailAddress) {
        when (selectedType) {
            ParsedQrType.URL -> urlText.trim()
            ParsedQrType.PHONE -> "tel:$phoneNumber"
            ParsedQrType.EMAIL -> "mailto:$emailAddress"
            ParsedQrType.TEXT -> plainText.trim()
            else -> plainText.trim()
        }
    }

    val qrBitmap: Bitmap? = remember(encodedContent, selectedColorIndex) {
        if (encodedContent.isNotBlank() && encodedContent != "https://") {
            QrCodeUtil.generateQrBitmap(
                content = encodedContent,
                size = 512,
                foregroundColor = colorOptions[selectedColorIndex].second,
                backgroundColor = AndroidColor.WHITE
            )
        } else null
    }

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
                    text = "Smart QR Designer",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Text(
                    text = "Custom styling, palette, & high-res export",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }

        Spacer(modifier = Modifier.height(18.dp))

        // Type Selectors
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            IosPillChip(
                text = "URL",
                isSelected = selectedType == ParsedQrType.URL,
                onClick = { selectedType = ParsedQrType.URL }
            )
            IosPillChip(
                text = "Text",
                isSelected = selectedType == ParsedQrType.TEXT,
                onClick = { selectedType = ParsedQrType.TEXT }
            )
            IosPillChip(
                text = "Phone",
                isSelected = selectedType == ParsedQrType.PHONE,
                onClick = { selectedType = ParsedQrType.PHONE }
            )
            IosPillChip(
                text = "Email",
                isSelected = selectedType == ParsedQrType.EMAIL,
                onClick = { selectedType = ParsedQrType.EMAIL }
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Input Card
        IosCard(
            modifier = Modifier.fillMaxWidth(),
            contentPadding = 16.dp
        ) {
            when (selectedType) {
                ParsedQrType.URL -> {
                    OutlinedTextField(
                        value = urlText,
                        onValueChange = { urlText = it },
                        label = { Text("Website Link") },
                        singleLine = true,
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = MaterialTheme.colorScheme.primary,
                            unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
                        )
                    )
                }
                ParsedQrType.PHONE -> {
                    OutlinedTextField(
                        value = phoneNumber,
                        onValueChange = { phoneNumber = it },
                        label = { Text("Phone Number") },
                        singleLine = true,
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = MaterialTheme.colorScheme.primary,
                            unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
                        )
                    )
                }
                ParsedQrType.EMAIL -> {
                    OutlinedTextField(
                        value = emailAddress,
                        onValueChange = { emailAddress = it },
                        label = { Text("Email Address") },
                        singleLine = true,
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = MaterialTheme.colorScheme.primary,
                            unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
                        )
                    )
                }
                else -> {
                    OutlinedTextField(
                        value = plainText,
                        onValueChange = { plainText = it },
                        label = { Text("Enter message or data") },
                        minLines = 3,
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = MaterialTheme.colorScheme.primary,
                            unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Swatches
            Text(
                text = "ACCENT COLOR",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                colorOptions.forEachIndexed { index, pair ->
                    val isSelected = selectedColorIndex == index
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(Color(pair.second))
                            .border(
                                width = if (isSelected) 3.dp else 1.dp,
                                color = if (isSelected) MaterialTheme.colorScheme.onBackground else Color.Transparent,
                                shape = CircleShape
                            )
                            .clickable { selectedColorIndex = index },
                        contentAlignment = Alignment.Center
                    ) {
                        if (isSelected) {
                            Icon(
                                imageVector = IosIcons.Check,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(18.dp))

        // Preview Card
        IosCard(
            modifier = Modifier.fillMaxWidth(),
            contentPadding = 20.dp
        ) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "QR CODE PREVIEW",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(14.dp))

                Box(
                    modifier = Modifier
                        .size(200.dp)
                        .clip(RoundedCornerShape(20.dp))
                        .background(Color.White)
                        .padding(14.dp),
                    contentAlignment = Alignment.Center
                ) {
                    if (qrBitmap != null) {
                        Image(
                            bitmap = qrBitmap.asImageBitmap(),
                            contentDescription = "Generated QR Code",
                            modifier = Modifier.fillMaxSize()
                        )
                    } else {
                        Text(
                            text = "Fill in the field above to generate QR code",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.Gray,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                    }
                }

                if (qrBitmap != null) {
                    Spacer(modifier = Modifier.height(16.dp))

                    Row(modifier = Modifier.fillMaxWidth()) {
                        IosButton(
                            text = "Save to Photos",
                            onClick = {
                                saveBitmapToGallery(context, qrBitmap)
                                onSaveToHistory(
                                    ScanRecord(
                                        rawContent = encodedContent,
                                        title = "Designer: ${selectedType.name}",
                                        subtitle = encodedContent.take(30),
                                        qrType = selectedType.name,
                                        scanSource = "STUDIO"
                                    )
                                )
                            },
                            modifier = Modifier.weight(1f),
                            isPrimary = true,
                            icon = {
                                Icon(
                                    imageVector = IosIcons.Gallery,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onPrimary,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        )

                        Spacer(modifier = Modifier.width(10.dp))

                        IosButton(
                            text = "Share",
                            onClick = {
                                val shareIntent = Intent(Intent.ACTION_SEND).apply {
                                    type = "text/plain"
                                    putExtra(Intent.EXTRA_TEXT, encodedContent)
                                }
                                context.startActivity(Intent.createChooser(shareIntent, "Share QR Data"))
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
                }
            }
        }
    }
}

private fun saveBitmapToGallery(context: Context, bitmap: Bitmap) {
    try {
        val filename = "SimpaQr_${System.currentTimeMillis()}.png"
        var fos: OutputStream? = null
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val contentValues = ContentValues().apply {
                put(MediaStore.MediaColumns.DISPLAY_NAME, filename)
                put(MediaStore.MediaColumns.MIME_TYPE, "image/png")
                put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_PICTURES + "/SimpaQr")
            }
            val imageUri: Uri? = context.contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)
            if (imageUri != null) {
                fos = context.contentResolver.openOutputStream(imageUri)
            }
        } else {
            val imagesDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES).toString()
            val image = java.io.File(imagesDir, filename)
            fos = java.io.FileOutputStream(image)
        }

        fos?.use {
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, it)
            Toast.makeText(context, "Saved to Photos gallery!", Toast.LENGTH_SHORT).show()
        }
    } catch (e: Exception) {
        Toast.makeText(context, "Could not save photo: ${e.message}", Toast.LENGTH_SHORT).show()
    }
}
