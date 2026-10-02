package com.example.util

import android.net.Uri
import java.util.Locale

data class SecurityReport(
    val status: SecurityStatus,
    val headline: String,
    val score: Int, // 0 to 100 (100 = safest)
    val details: List<String>,
    val domain: String? = null,
    val isHttps: Boolean = false
)

enum class SecurityStatus {
    SAFE,
    CAUTION,
    DANGER,
    NEUTRAL
}

object SecurityScanner {

    private val SUSPICIOUS_TLDS = setOf(
        "top", "buzz", "xyz", "surf", "fit", "work", "loan", "zip", "mov", "gq", "cf", "ml", "tk", "click"
    )

    private val SUSPICIOUS_KEYWORDS = listOf(
        "login", "verify", "secure", "bank", "update-account", "wallet", "crypto", "free-gift", "claim"
    )

    fun inspect(content: String): SecurityReport {
        val trimmed = content.trim()
        if (!trimmed.startsWith("http://", ignoreCase = true) && !trimmed.startsWith("https://", ignoreCase = true)) {
            return SecurityReport(
                status = SecurityStatus.NEUTRAL,
                headline = "Non-URL Content",
                score = 100,
                details = listOf("This QR contains local text or structured non-web data. No external network request required.")
            )
        }

        val uri = try {
            Uri.parse(trimmed)
        } catch (e: Exception) {
            return SecurityReport(
                status = SecurityStatus.CAUTION,
                headline = "Malformed URL",
                score = 40,
                details = listOf("The URL could not be parsed reliably.")
            )
        }

        val scheme = uri.scheme?.lowercase(Locale.US) ?: ""
        val host = uri.host?.lowercase(Locale.US) ?: ""
        val path = uri.path?.lowercase(Locale.US) ?: ""
        val isHttps = scheme == "https"

        val details = mutableListOf<String>()
        var score = 100

        // 1. Check Protocol
        if (!isHttps) {
            score -= 30
            details.add("Unencrypted HTTP connection (vulnerable to eavesdropping and data interception).")
        } else {
            details.add("Encrypted connection (TLS/HTTPS enabled).")
        }

        // 2. Check for IP address in host
        val ipPattern = Regex("""^(\d{1,3}\.){3}\d{1,3}$""")
        if (ipPattern.matches(host)) {
            score -= 40
            details.add("Direct IP address used instead of domain name (common evasion technique in phishing).")
        }

        // 3. Check TLD
        val tld = host.substringAfterLast(".", "")
        if (SUSPICIOUS_TLDS.contains(tld)) {
            score -= 25
            details.add("Top-level domain (.$tld) has elevated statistical frequency in malicious spam and scam campaigns.")
        }

        // 4. Excessive subdomains or punycode
        if (host.startsWith("xn--")) {
            score -= 30
            details.add("Punycode / IDN character detected: potential lookalike homograph spoofing attack.")
        }

        val subdomainCount = host.count { it == '.' }
        if (subdomainCount > 3) {
            score -= 15
            details.add("Unusually deeply nested subdomains ($host).")
        }

        // 5. Keyword sniffing on unverified domains
        val containsKeyword = SUSPICIOUS_KEYWORDS.any { host.contains(it) || path.contains(it) }
        val isKnownMajorDomain = host.endsWith("google.com") || host.endsWith("apple.com") ||
                host.endsWith("microsoft.com") || host.endsWith("github.com") || host.endsWith("wikipedia.org")

        if (containsKeyword && !isKnownMajorDomain) {
            score -= 20
            details.add("Contains authentication or financial keywords ($path) on an unverified domain.")
        }

        // Determine final category
        val status = when {
            score >= 80 -> SecurityStatus.SAFE
            score >= 50 -> SecurityStatus.CAUTION
            else -> SecurityStatus.DANGER
        }

        val headline = when (status) {
            SecurityStatus.SAFE -> "Verified Secure Link"
            SecurityStatus.CAUTION -> "Proceed With Caution"
            SecurityStatus.DANGER -> "High Phishing Threat"
            SecurityStatus.NEUTRAL -> "Local Content"
        }

        return SecurityReport(
            status = status,
            headline = headline,
            score = score.coerceIn(0, 100),
            details = details,
            domain = host,
            isHttps = isHttps
        )
    }
}
