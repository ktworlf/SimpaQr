package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.data.model.ParsedQrType
import com.example.util.QrTypeParser
import com.example.util.SecurityScanner
import com.example.util.SecurityStatus
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class ExampleRobolectricTest {

    @Test
    fun readStringFromContext() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val appName = context.getString(R.string.app_name)
        assertEquals("SimpaQr", appName)
    }

    @Test
    fun testUpiQrParsing() {
        val raw = "upi://pay?pa=merchant@ybl&pn=Coffee+Shop&am=150.00"
        val parsed = QrTypeParser.parse(raw)
        assertEquals(ParsedQrType.UPI, parsed.type)
        assertEquals("merchant@ybl", parsed.upiPayeeId)
        assertEquals("Coffee Shop", parsed.upiPayeeName)
        assertEquals("150.00", parsed.upiAmount)
    }

    @Test
    fun testWifiQrParsing() {
        val raw = "WIFI:S:HomeNetwork;T:WPA;P:Secret123;;"
        val parsed = QrTypeParser.parse(raw)
        assertEquals(ParsedQrType.WIFI, parsed.type)
        assertEquals("HomeNetwork", parsed.wifiSsid)
        assertEquals("Secret123", parsed.wifiPass)
    }

    @Test
    fun testSecurityScannerPhishingDetection() {
        val raw = "http://192.168.1.1/login-update-account"
        val report = SecurityScanner.inspect(raw)
        assertEquals(SecurityStatus.DANGER, report.status)
    }

    @Test
    fun testSecurityScannerSafeHttps() {
        val raw = "https://github.com/auraqr/auraqr-about"
        val report = SecurityScanner.inspect(raw)
        assertEquals(SecurityStatus.SAFE, report.status)
    }
}
