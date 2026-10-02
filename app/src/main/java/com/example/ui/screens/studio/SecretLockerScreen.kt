package com.example.ui.screens.studio

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Color as AndroidColor
import android.util.Base64
import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
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
import androidx.compose.foundation.text.KeyboardOptions
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import com.example.data.model.ScanRecord
import com.example.ui.components.IosButton
import com.example.ui.components.IosCard
import com.example.ui.components.IosIcons
import com.example.util.QrCodeUtil

@Composable
fun SecretLockerScreen(
    onBack: () -> Unit,
    onSaveToHistory: (ScanRecord) -> Unit
) {
    val context = LocalContext.current

    var secretMessage by remember { mutableStateOf("") }
    var pinCode by remember { mutableStateOf("") }

    val encryptedPayload = remember(secretMessage, pinCode) {
        if (secretMessage.isBlank() || pinCode.isBlank()) ""
        else {
            val keyBytes = pinCode.toByteArray()
            val textBytes = secretMessage.toByteArray()
            val enc = ByteArray(textBytes.size)
            for (i in textBytes.indices) {
                enc[i] = (textBytes[i].toInt() xor keyBytes[i % keyBytes.size].toInt()).toByte()
            }
            "SIMPA_ENC:" + Base64.encodeToString(enc, Base64.NO_WRAP)
        }
    }

    val qrBitmap: Bitmap? = remember(encryptedPayload) {
        if (encryptedPayload.isNotBlank()) {
            QrCodeUtil.generateQrBitmap(
                content = encryptedPayload,
                size = 512,
                foregroundColor = AndroidColor.parseColor("#4F46E5"),
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
                    text = "Secret Crypto Locker",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Text(
                    text = "PIN-encrypted offline confidential QR codes",
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
                value = secretMessage,
                onValueChange = { secretMessage = it },
                label = { Text("Confidential Note / Password / Seed") },
                minLines = 3,
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
                )
            )

            Spacer(modifier = Modifier.height(10.dp))

            OutlinedTextField(
                value = pinCode,
                onValueChange = { pinCode = it },
                label = { Text("4 to 8 Digit Encryption PIN") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                visualTransformation = PasswordVisualTransformation(),
                singleLine = true,
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
                )
            )
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
                    text = "ENCRYPTED QR PREVIEW",
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
                            contentDescription = "Encrypted QR",
                            modifier = Modifier.fillMaxSize()
                        )
                    } else {
                        Text(
                            text = "Enter text & PIN to generate encrypted QR",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.Gray,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                    }
                }

                if (encryptedPayload.isNotBlank()) {
                    Spacer(modifier = Modifier.height(16.dp))

                    IosButton(
                        text = "Save Encrypted QR",
                        onClick = {
                            onSaveToHistory(
                                ScanRecord(
                                    rawContent = encryptedPayload,
                                    title = "Secret Encrypted Note",
                                    subtitle = "Locked with PIN",
                                    qrType = "TEXT",
                                    scanSource = "STUDIO"
                                )
                            )
                            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                                type = "text/plain"
                                putExtra(Intent.EXTRA_TEXT, encryptedPayload)
                            }
                            context.startActivity(Intent.createChooser(shareIntent, "Share Encrypted QR"))
                        },
                        modifier = Modifier.fillMaxWidth(),
                        isPrimary = true,
                        icon = {
                            Icon(
                                imageVector = IosIcons.Lock,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onPrimary,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    )
                }
            }
        }
    }
}
