package com.example.util

import com.example.data.model.ParsedQrData
import com.example.data.model.ParsedQrType
import java.util.regex.Pattern

object QrTypeParser {

    fun parse(raw: String): ParsedQrData {
        val trimmed = raw.trim()

        // 1. WiFi: WIFI:S:MySSID;T:WPA;P:MyPass;;
        if (trimmed.startsWith("WIFI:", ignoreCase = true)) {
            val ssid = extractField(trimmed, "S:")
            val pass = extractField(trimmed, "P:")
            val type = extractField(trimmed, "T:").ifEmpty { "WPA" }
            return ParsedQrData(
                type = ParsedQrType.WIFI,
                title = "Wi-Fi Network",
                displayValue = ssid.ifEmpty { "Wi-Fi Network" },
                secondaryValue = "Security: $type • Password: ${if (pass.isEmpty()) "None" else "••••••••"}",
                rawValue = trimmed,
                wifiSsid = ssid,
                wifiPass = pass,
                wifiType = type
            )
        }

        // 2. vCard: BEGIN:VCARD
        if (trimmed.startsWith("BEGIN:VCARD", ignoreCase = true) || trimmed.startsWith("MECARD:", ignoreCase = true)) {
            val name = extractVCardField(trimmed, "FN:")
                .ifEmpty { extractVCardField(trimmed, "N:") }
                .ifEmpty { extractField(trimmed, "N:") }
            val phone = extractVCardField(trimmed, "TEL:")
                .ifEmpty { extractField(trimmed, "TEL:") }
            val email = extractVCardField(trimmed, "EMAIL:")
                .ifEmpty { extractField(trimmed, "EMAIL:") }

            return ParsedQrData(
                type = ParsedQrType.CONTACT,
                title = "Contact Card",
                displayValue = name.ifEmpty { "Contact Information" },
                secondaryValue = listOfNotNull(
                    phone.takeIf { it.isNotEmpty() }?.let { "Tel: $it" },
                    email.takeIf { it.isNotEmpty() }?.let { "Email: $it" }
                ).joinToString(" • "),
                rawValue = trimmed,
                contactName = name,
                contactPhone = phone,
                contactEmail = email
            )
        }

        // 3. UPI / PhonePe / Payment: upi://pay?...
        if (trimmed.startsWith("upi://pay", ignoreCase = true) || trimmed.startsWith("phonepe://", ignoreCase = true) || trimmed.startsWith("paytmmp://", ignoreCase = true)) {
            val uri = try { android.net.Uri.parse(trimmed) } catch (e: Exception) { null }
            val payeeId = uri?.getQueryParameter("pa") ?: ""
            val payeeName = uri?.getQueryParameter("pn")?.replace("+", " ") ?: ""
            val amount = uri?.getQueryParameter("am") ?: ""
            val titleText = if (payeeName.isNotEmpty()) payeeName else if (payeeId.isNotEmpty()) payeeId else "UPI Payment"
            val secondary = listOfNotNull(
                payeeId.takeIf { it.isNotEmpty() }?.let { "UPI ID: $it" },
                amount.takeIf { it.isNotEmpty() }?.let { "Amount: ₹$it" }
            ).joinToString(" • ")

            return ParsedQrData(
                type = ParsedQrType.UPI,
                title = "UPI Payment",
                displayValue = titleText,
                secondaryValue = secondary.ifEmpty { "1-Tap Pay via PhonePe / GPay / Paytm" },
                rawValue = trimmed,
                upiPayeeId = payeeId,
                upiPayeeName = payeeName,
                upiAmount = amount
            )
        }

        // 4. URLs
        if (trimmed.startsWith("http://", ignoreCase = true) || trimmed.startsWith("https://", ignoreCase = true)) {
            val cleanHost = try {
                val uri = android.net.Uri.parse(trimmed)
                uri.host ?: trimmed
            } catch (e: Exception) {
                trimmed
            }
            return ParsedQrData(
                type = ParsedQrType.URL,
                title = "Website Link",
                displayValue = cleanHost,
                secondaryValue = trimmed,
                rawValue = trimmed,
                url = trimmed
            )
        }

        // 4. Email: mailto: or MATMSG:
        if (trimmed.startsWith("mailto:", ignoreCase = true) || trimmed.startsWith("MATMSG:", ignoreCase = true)) {
            val email = if (trimmed.startsWith("mailto:", ignoreCase = true)) {
                trimmed.removePrefix("mailto:").substringBefore("?")
            } else {
                extractField(trimmed, "TO:")
            }
            return ParsedQrData(
                type = ParsedQrType.EMAIL,
                title = "Email Address",
                displayValue = email.ifEmpty { trimmed },
                secondaryValue = "Tap to compose email",
                rawValue = trimmed,
                emailAddress = email
            )
        }

        // 5. Phone: tel:
        if (trimmed.startsWith("tel:", ignoreCase = true)) {
            val phone = trimmed.removePrefix("tel:")
            return ParsedQrData(
                type = ParsedQrType.PHONE,
                title = "Phone Number",
                displayValue = phone,
                secondaryValue = "Tap to call contact",
                rawValue = trimmed,
                phoneNumber = phone
            )
        }

        // 6. SMS: smsto: or sms:
        if (trimmed.startsWith("smsto:", ignoreCase = true) || trimmed.startsWith("sms:", ignoreCase = true)) {
            val phone = trimmed.substringAfter(":").substringBefore(":")
            val body = if (trimmed.count { it == ':' } >= 2) trimmed.substringAfterLast(":") else ""
            return ParsedQrData(
                type = ParsedQrType.SMS,
                title = "SMS Message",
                displayValue = phone,
                secondaryValue = if (body.isNotEmpty()) "Message: $body" else "Tap to send message",
                rawValue = trimmed,
                phoneNumber = phone
            )
        }

        // 7. Geo: geo:lat,lng
        if (trimmed.startsWith("geo:", ignoreCase = true)) {
            val coords = trimmed.removePrefix("geo:").substringBefore("?").split(",")
            val lat = coords.getOrNull(0)?.toDoubleOrNull()
            val lng = coords.getOrNull(1)?.toDoubleOrNull()
            return ParsedQrData(
                type = ParsedQrType.GEO,
                title = "Map Location",
                displayValue = "Coordinates: ${lat ?: 0.0}, ${lng ?: 0.0}",
                secondaryValue = "Tap to open in Maps",
                rawValue = trimmed,
                latitude = lat,
                longitude = lng
            )
        }

        // 8. Plain text or generic fallback
        val firstLine = trimmed.lines().firstOrNull() ?: trimmed
        return ParsedQrData(
            type = ParsedQrType.TEXT,
            title = "Plain Text",
            displayValue = if (firstLine.length > 50) firstLine.take(47) + "..." else firstLine,
            secondaryValue = "${trimmed.length} characters",
            rawValue = trimmed
        )
    }

    private fun extractField(content: String, prefix: String): String {
        val startIndex = content.indexOf(prefix, ignoreCase = true)
        if (startIndex == -1) return ""
        val valueStart = startIndex + prefix.length
        val endIndex = content.indexOf(';', valueStart).takeIf { it != -1 } ?: content.length
        return content.substring(valueStart, endIndex).replace("\\;", ";").replace("\\:", ":")
    }

    private fun extractVCardField(content: String, prefix: String): String {
        for (line in content.lines()) {
            val trimmedLine = line.trim()
            if (trimmedLine.startsWith(prefix, ignoreCase = true)) {
                return trimmedLine.substring(prefix.length).trim().replace(";", " ")
            }
        }
        return ""
    }
}
