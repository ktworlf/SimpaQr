package com.example.data.remote

import android.content.Context
import com.example.data.model.ScanRecord
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

data class AuthResultState(
    val success: Boolean,
    val userEmail: String? = null,
    val displayName: String? = null,
    val errorMessage: String? = null
)

object FirebaseManager {

    private fun isFirebaseAvailable(context: Context): Boolean {
        return try {
            FirebaseApp.getApps(context).isNotEmpty()
        } catch (e: Exception) {
            false
        }
    }

    fun getCurrentUser(context: Context): FirebaseUser? {
        return try {
            if (isFirebaseAvailable(context)) {
                FirebaseAuth.getInstance().currentUser
            } else {
                null
            }
        } catch (e: Exception) {
            null
        }
    }

    suspend fun signInWithEmail(email: String, pass: String): AuthResultState {
        return try {
            val auth = FirebaseAuth.getInstance()
            val result = auth.signInWithEmailAndPassword(email, pass).await()
            val user = result.user
            AuthResultState(
                success = true,
                userEmail = user?.email,
                displayName = user?.displayName ?: user?.email?.substringBefore("@")
            )
        } catch (e: Exception) {
            AuthResultState(
                success = false,
                errorMessage = e.localizedMessage ?: "Sign in failed"
            )
        }
    }

    suspend fun signUpWithEmail(email: String, pass: String, name: String): AuthResultState {
        return try {
            val auth = FirebaseAuth.getInstance()
            val result = auth.createUserWithEmailAndPassword(email, pass).await()
            val user = result.user
            AuthResultState(
                success = true,
                userEmail = user?.email,
                displayName = name.ifEmpty { user?.email?.substringBefore("@") }
            )
        } catch (e: Exception) {
            AuthResultState(
                success = false,
                errorMessage = e.localizedMessage ?: "Account creation failed"
            )
        }
    }

    suspend fun signInAnonymously(): AuthResultState {
        return try {
            val auth = FirebaseAuth.getInstance()
            val result = auth.signInAnonymously().await()
            val user = result.user
            AuthResultState(
                success = true,
                userEmail = "guest_${user?.uid?.take(6)}@auraqr.app",
                displayName = "Pro Member"
            )
        } catch (e: Exception) {
            AuthResultState(
                success = false,
                errorMessage = e.localizedMessage ?: "Guest login failed"
            )
        }
    }

    fun signOut() {
        try {
            FirebaseAuth.getInstance().signOut()
        } catch (e: Exception) {
            // Ignore
        }
    }

    suspend fun syncScanToFirestore(scan: ScanRecord) {
        try {
            val user = FirebaseAuth.getInstance().currentUser ?: return
            val db = FirebaseFirestore.getInstance()
            val scanMap = hashMapOf(
                "rawContent" to scan.rawContent,
                "title" to scan.title,
                "subtitle" to scan.subtitle,
                "qrType" to scan.qrType,
                "formatName" to scan.formatName,
                "timestamp" to scan.timestamp,
                "isFavorite" to scan.isFavorite,
                "scanSource" to scan.scanSource
            )
            db.collection("users")
                .document(user.uid)
                .collection("scans")
                .document(scan.id.toString())
                .set(scanMap)
                .await()
        } catch (e: Exception) {
            // Offline or unconfigured - silent fallback
        }
    }
}
