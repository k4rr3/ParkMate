package com.example.parkmate.auth


import android.content.Context
import android.util.Log
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthException
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthInvalidUserException
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.auth.FirebaseAuthWeakPasswordException
import com.google.firebase.auth.FirebaseUser
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

class FirebaseAuthManager(private val context: Context) {

    private val auth: FirebaseAuth by lazy {
        FirebaseAuth.getInstance()
    }

    companion object {
        private const val TAG = "FirebaseAuthManager"
    }

    // Email/Password Sign-Up with Email Verification
    suspend fun signUpWithEmail(email: String, password: String): Result<String> {
        if (email.isBlank() || password.length < 6) {
            return Result.failure(IllegalArgumentException("Email must not be empty and password must be at least 6 characters"))
        }

        return suspendCoroutine { continuation ->
            auth.createUserWithEmailAndPassword(email, password)
                .addOnSuccessListener { authResult ->
                    authResult.user?.sendEmailVerification()?.addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            continuation.resume(Result.success(authResult.user?.uid ?: ""))
                        } else {
                            continuation.resume(Result.failure(task.exception ?: Exception("Failed to send verification email")))
                        }
                    }
                }
                .addOnFailureListener { e ->
                    continuation.resume(Result.failure(e))
                }
        }
    }

    // Email/Password Login
    suspend fun loginWithEmail(email: String, password: String): Result<Unit> {
        if (email.isBlank() || password.isBlank()) {
            return Result.failure(IllegalArgumentException("Email and password must not be empty"))
        }
        return suspendCoroutine { continuation ->
            auth.signInWithEmailAndPassword(email, password)
                .addOnSuccessListener {
                    val user = auth.currentUser
                    if (user?.isEmailVerified == true) {
                        continuation.resume(Result.success(Unit))
                    } else {
                        continuation.resume(Result.failure(Exception("EMAIL_NOT_VERIFIED")))
                    }
                }
                .addOnFailureListener { e ->
                    Log.e(TAG, "Login failed: ${e.javaClass.simpleName}, errorCode: ${if (e is FirebaseAuthException) e.errorCode else "N/A"}, message: ${e.message}")
                    when (e) {
                        is FirebaseAuthInvalidUserException -> {
                            continuation.resume(Result.failure(Exception("USER_NOT_FOUND")))
                        }
                        is FirebaseAuthInvalidCredentialsException -> {
                            when (e.errorCode) {
                                "ERROR_INVALID_EMAIL" -> {
                                    continuation.resume(Result.failure(Exception("INVALID_EMAIL")))
                                }
                                "ERROR_WRONG_PASSWORD" -> {
                                    continuation.resume(Result.failure(Exception("WRONG_PASSWORD")))
                                }
                                else -> {
                                    continuation.resume(Result.failure(Exception("INVALID_CREDENTIALS")))
                                }
                            }
                        }
                        is FirebaseAuthUserCollisionException -> {
                            continuation.resume(Result.failure(Exception("EMAIL_ALREADY_IN_USE")))
                        }
                        is FirebaseAuthWeakPasswordException -> {
                            continuation.resume(Result.failure(Exception("WEAK_PASSWORD")))
                        }
                        else -> {
                            continuation.resume(Result.failure(Exception("LOGIN_FAILED: ${e.message}")))
                        }
                    }
                }
        }
    }

    // Send Password Reset Email
    suspend fun sendPasswordResetEmail(email: String): Result<Unit> {
        if (email.isBlank()) {
            return Result.failure(IllegalArgumentException("Email must not be empty"))
        }
        return suspendCoroutine { continuation ->
            auth.sendPasswordResetEmail(email)
                .addOnSuccessListener {
                    continuation.resume(Result.success(Unit))
                }
                .addOnFailureListener { e ->
                    continuation.resume(Result.failure(e))
                }
        }
    }

    // Delete User
    suspend fun deleteCurrentUser(): Result<Unit> {
        val user = auth.currentUser ?: return Result.failure(IllegalStateException("No user signed in"))
        return suspendCoroutine { continuation ->
            user.delete()
                .addOnSuccessListener {
                    continuation.resume(Result.success(Unit))
                }
                .addOnFailureListener { e ->
                    continuation.resume(Result.failure(e))
                }
        }
    }

    // Sign Out
    suspend fun signOut() {
        auth.signOut()
    }

    suspend fun getUserId(): String? {
        return auth.currentUser?.uid
    }

    fun getCurrentUser(): FirebaseUser? {
        return auth.currentUser
    }
}