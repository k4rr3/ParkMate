package com.example.parkmate.auth


import android.content.Context
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthInvalidUserException
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.auth.FirebaseAuthWeakPasswordException
import com.google.firebase.auth.FirebaseUser
import kotlinx.coroutines.tasks.await
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
    suspend fun loginWithEmail(
        email: String,
        password: String
    ): Result<Unit> {
        if (email.isBlank() || password.isBlank()) {
            return Result.failure(
                IllegalArgumentException("Email and password must not be empty")
            )
        }

        return runCatching {
            val result = auth
                .signInWithEmailAndPassword(email, password)
                .await()

            val user = result.user
                ?: error("USER_NOT_FOUND")

            if (!user.isEmailVerified) {
                error("EMAIL_NOT_VERIFIED")
            }
        }.mapCatching {
            Unit
        }.recoverCatching { throwable ->
            throw mapAuthException(throwable)
        }
    }
    private fun mapAuthException(throwable: Throwable): Exception {
        return when (throwable) {
            is FirebaseAuthInvalidUserException ->
                Exception("USER_NOT_FOUND")

            is FirebaseAuthInvalidCredentialsException ->
                when (throwable.errorCode) {
                    "ERROR_INVALID_EMAIL" ->
                        Exception("INVALID_EMAIL")
                    "ERROR_WRONG_PASSWORD" ->
                        Exception("WRONG_PASSWORD")
                    else ->
                        Exception("INVALID_CREDENTIALS")
                }

            is FirebaseAuthUserCollisionException ->
                Exception("EMAIL_ALREADY_IN_USE")

            is FirebaseAuthWeakPasswordException ->
                Exception("WEAK_PASSWORD")

            else ->
                Exception(throwable.message ?: "LOGIN_FAILED")
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