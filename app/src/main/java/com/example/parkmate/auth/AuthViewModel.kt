// auth/AuthViewModel.kt (Final Version)
package com.example.parkmate.auth

import android.content.Context
import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.credentials.ClearCredentialStateRequest
import androidx.credentials.Credential
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialException
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.parkmate.R
import com.example.parkmate.data.models.User
import com.example.parkmate.data.repository.FirestoreRepository
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential.Companion.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthInvalidUserException
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.auth.FirebaseAuthWeakPasswordException
import com.google.firebase.auth.GoogleAuthProvider
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.security.MessageDigest
import java.util.UUID
import javax.inject.Inject

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val firestoreRepository: FirestoreRepository,
    @ApplicationContext private val context: Context  // Added @ApplicationContext
) : ViewModel() {
    companion object {
        private const val TAG = "AuthViewModel"
    }

    var email by mutableStateOf("")
        private set
    var password by mutableStateOf("")
        private set
    var name by mutableStateOf("")
        private set
    var isLoading by mutableStateOf(false)
        private set
    var errorMessage by mutableStateOf<String?>(null)
        private set
    var successMessage by mutableStateOf<String?>(null)
        private set
    var isEmailVerified by mutableStateOf(false)
        private set
    var mailErrorMessage by mutableStateOf<String?>(null)
        private set
    var passwordErrorMessage by mutableStateOf<String?>(null)
        private set

    private val credentialManager = CredentialManager.create(context)
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()

    // ... all your existing methods remain exactly the same ...
    // No changes needed to the method implementations

    fun updateEmail(newEmail: String) {
        email = newEmail
    }

    fun updatePassword(newPassword: String) {
        password = newPassword
    }

    fun updateName(newName: String) {
        name = newName
    }

    // Create user in Firestore after successful authentication
    private suspend fun createUserInFirestore(uid: String, email: String, name: String = "") {
        try {
            val user = User(
                uid = uid,
                email = email,
                name = if (name.isNotEmpty()) name else email.substringBefore("@"),
                phone = "",
                premium = false,
                vehicleID = emptyList(),
                PaymentMethod = emptyMap()
            )

            val success = firestoreRepository.createUser(user)
            if (!success) {
                Log.e(TAG, "Failed to create user in Firestore")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error creating user in Firestore: ${e.message}")
        }
    }

    // Email/Password Sign-Up
    fun signUpWithEmail() {
        clearMessages()
        if (!validRegisterData()) {
            return
        }

        viewModelScope.launch {
            isLoading = true
            errorMessage = null
            successMessage = null

            try {
                val authResult = auth.createUserWithEmailAndPassword(email, password).await()
                val user = authResult.user

                if (user != null) {
                    // Create user in Firestore
                    createUserInFirestore(user.uid, email, name)

                    // Send verification email
                    user.sendEmailVerification().await()

                    successMessage = context.getString(R.string.signup_successful)
                } else {
                    errorMessage = context.getString(R.string.signup_failed)
                }
            } catch (e: Exception) {
                errorMessage = when (e) {
                    is FirebaseAuthInvalidCredentialsException -> context.getString(R.string.email_invalid_format)
                    is FirebaseAuthUserCollisionException -> context.getString(R.string.email_already_in_use)
                    is FirebaseAuthWeakPasswordException -> context.getString(R.string.password_too_weak)
                    else -> context.getString(R.string.signup_failed) + ": ${e.message}"
                }
            } finally {
                isLoading = false
            }
        }
    }

    // Email/Password Login
    fun loginWithEmail() {
        clearMessages()
        if (!validLoginData()) {
            return
        }

        viewModelScope.launch {
            isLoading = true
            errorMessage = null
            successMessage = null

            try {
                val authResult = auth.signInWithEmailAndPassword(email, password).await()
                val user = authResult.user

                if (user != null) {
                    if (user.isEmailVerified) {
                        successMessage = context.getString(R.string.login_successful)
                        // User data will be loaded from Firestore in other viewmodels
                    } else {
                        errorMessage = context.getString(R.string.please_verify_email)
                        auth.signOut() // Sign out if email not verified
                    }
                } else {
                    errorMessage = context.getString(R.string.login_failed)
                }
            } catch (e: Exception) {
                errorMessage = when (e) {
                    is FirebaseAuthInvalidUserException -> context.getString(R.string.user_not_found_or_disabled)
                    is FirebaseAuthInvalidCredentialsException -> context.getString(R.string.wrong_credentials)
                    else -> context.getString(R.string.login_failed) + ": ${e.message}"
                }
            } finally {
                isLoading = false
            }
        }
    }

    // Google Sign-In
    fun signInWithGoogle() {
        viewModelScope.launch {
            isLoading = true
            clearMessages()
            try {
                val googleIdOption = GetGoogleIdOption.Builder()

                    .setServerClientId(context.getString(R.string.server_client_id))
                    .setFilterByAuthorizedAccounts(false)
                    .setNonce(createNonce())
                    .build()
                val request = GetCredentialRequest.Builder()
                    .addCredentialOption(googleIdOption)
                    .build()
                val result = credentialManager.getCredential(context = context, request = request)
                handleSignIn(result.credential)
            } catch (e: GetCredentialException) {
                Log.d(TAG, "Couldn't retrieve user's credentials: ${e.localizedMessage}")
                if (!e.localizedMessage.contains("activity is cancelled by the user")) {
                    errorMessage = context.getString(R.string.error) + ": ${e.localizedMessage}"
                }
            } finally {
                isLoading = false
            }
        }
    }

    private fun handleSignIn(credential: Credential) {
        if (credential is CustomCredential && credential.type == TYPE_GOOGLE_ID_TOKEN_CREDENTIAL) {
            val googleIdTokenCredential = GoogleIdTokenCredential.createFrom(credential.data)
            firebaseAuthWithGoogle(googleIdTokenCredential.idToken)
        } else {
            Log.w(TAG, "Credential is not of type Google ID!")
            errorMessage = context.getString(R.string.error)
        }
    }

    private fun firebaseAuthWithGoogle(idToken: String) {
        viewModelScope.launch {
            val credential = GoogleAuthProvider.getCredential(idToken, null)
            try {
                val authResult = auth.signInWithCredential(credential).await()
                val user = authResult.user

                if (user != null) {
                    // Check if user exists in Firestore, if not create them
                    val existingUser = firestoreRepository.getUser(user.uid)
                    if (existingUser == null) {
                        createUserInFirestore(user.uid, user.email ?: "", user.displayName ?: "")
                    }

                    successMessage = context.getString(R.string.sign_in_with_google)
                    Log.d(TAG, "Google sign-in successful")
                }
            } catch (e: Exception) {
                Log.w(TAG, "Google sign-in failed", e)
                errorMessage = context.getString(R.string.error) + ": ${e.message}"
            } finally {
                isLoading = false
            }
        }
    }

    // Password Reset
    fun sendPasswordResetEmail() {
        clearMessages()
        if (email.isEmpty()) {
            mailErrorMessage = context.getString(R.string.fill_email_field)
            return
        }

        viewModelScope.launch {
            isLoading = true
            errorMessage = null
            successMessage = null

            try {
                auth.sendPasswordResetEmail(email).await()
                successMessage = context.getString(R.string.password_reset_email_sent)
            } catch (e: Exception) {
                mailErrorMessage = when {
                    e is FirebaseAuthInvalidUserException -> context.getString(R.string.user_not_found_or_disabled)
                    else -> context.getString(R.string.error) + ": ${e.message}"
                }
            } finally {
                isLoading = false
            }
        }
    }

    // Sign Out
    fun signOut() {
        viewModelScope.launch {
            isLoading = true
            clearMessages()
            try {
                auth.signOut()
                val clearRequest = ClearCredentialStateRequest()
                credentialManager.clearCredentialState(clearRequest)
                successMessage = context.getString(R.string.signed_out_successfully)
                Log.d(TAG, "Signed out successfully")
            } catch (e: Exception) {
                Log.d(TAG, "Couldn't clear user credentials: ${e.localizedMessage}", e)
                errorMessage = context.getString(R.string.error) + ": ${e.message}"
            } finally {
                isLoading = false
            }
        }
    }

    // Get current user ID
    fun getCurrentUserId(): String? {
        return auth.currentUser?.uid
    }

    // Check if user is logged in
    fun isUserLoggedIn(): Boolean {
        return auth.currentUser != null
    }

    // Validation methods
    fun validRegisterData(): Boolean {
        clearMessages()
        checkValidEmail()
        checkValidPassword()
        return mailErrorMessage == null && passwordErrorMessage == null
    }

    fun checkValidPassword() {
        if (password.isEmpty()) {
            passwordErrorMessage = context.getString(R.string.password_can_not_be_empty)
        } else if (password.length < 8) {
            passwordErrorMessage = context.getString(R.string.password_too_short)
        } else if (password.length > 128) {
            passwordErrorMessage = context.getString(R.string.password_too_long)
        } else if (password.contains(" ")) {
            passwordErrorMessage = context.getString(R.string.password_contains_spaces)
        }
    }

    fun checkValidEmail() {
        if (email.isEmpty()) {
            mailErrorMessage = context.getString(R.string.email_can_not_be_empty)
        } else if (email.length > 100) {
            mailErrorMessage = context.getString(R.string.email_too_long)
        } else if (!email.matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$".toRegex())) {
            mailErrorMessage = context.getString(R.string.email_invalid_format)
        } else if (email.startsWith(".") || email.endsWith(".")) {
            mailErrorMessage = context.getString(R.string.email_invalid_dot_position)
        }
    }

    fun validLoginData(): Boolean {
        clearMessages()
        if (email.isEmpty()) {
            mailErrorMessage = context.getString(R.string.email_can_not_be_empty)
        } else if (!email.matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$".toRegex())) {
            mailErrorMessage = context.getString(R.string.email_invalid_format)
        }
        if (password.isEmpty()) {
            passwordErrorMessage = context.getString(R.string.password_can_not_be_empty)
        }
        return mailErrorMessage == null && passwordErrorMessage == null
    }

    private fun createNonce(): String {
        val rawNonce = UUID.randomUUID().toString()
        val byte = rawNonce.toByteArray()
        val md = MessageDigest.getInstance("SHA-256")
        val digest = md.digest(byte)
        return digest.fold("") { str, it -> str + "%02x".format(it) }
    }

    fun clearMessages() {
        errorMessage = null
        successMessage = null
        mailErrorMessage = null
        passwordErrorMessage = null
    }

    fun setCredentialsError() {
        errorMessage = "Credentials are not correct"
    }
}