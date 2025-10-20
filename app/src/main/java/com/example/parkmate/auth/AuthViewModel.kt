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
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential.Companion.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL
import com.google.firebase.FirebaseNetworkException
import com.google.firebase.FirebaseTooManyRequestsException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthInvalidUserException
import com.google.firebase.auth.FirebaseAuthRecentLoginRequiredException
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.auth.FirebaseAuthWeakPasswordException
import com.google.firebase.auth.GoogleAuthProvider
import kotlinx.coroutines.tasks.await

import com.example.parkmate.R

import kotlinx.coroutines.launch
import java.security.MessageDigest
import java.util.UUID

class AuthViewModel(
    //private val authManager: FirebaseAuthManager,
    private val context: Context
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


/*    fun setErrorMessage(message: String?){
        errorMessage = message
    }*/

    private val credentialManager = CredentialManager.create(context)
    //private val auth: FirebaseAuth = FirebaseAuth.getInstance()

    fun updateEmail(newEmail: String) {
        email = newEmail
    }


    fun updatePassword(newPassword: String) {
        password = newPassword
    }





    fun validRegisterData(): Boolean {
        clearMessages()

        // Email validations
        checkValidEmail()

        // Password validations
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


    fun setCredentialsError() {
            errorMessage = "Credentials are not correct"
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

    fun validRegister(): Boolean{
        clearMessages()
        checkValidEmail()
        checkValidPassword()
        return mailErrorMessage == null && passwordErrorMessage == null
    }

    fun isValidEmail(): Boolean {
        clearMessages()
        checkValidEmail()
        return mailErrorMessage == null
    }

    fun signUpWithEmail() {
        clearMessages()
        if (!validRegisterData()) {
            return
        }
        /*        viewModelScope.launch {
                    isLoading = true
                    errorMessage = null
                    successMessage = null
                    isEmailVerified = false
                    val result = authManager.signUpWithEmail(email, password)
                    if (result.isSuccess) {
                        successMessage = context.getString(R.string.signup_successful)
                    } else {
                        when (val exception = result.exceptionOrNull()) {
                            is FirebaseNetworkException -> mailErrorMessage =
                                context.getString(R.string.network_error)

                            is FirebaseAuthUserCollisionException -> mailErrorMessage =
                                context.getString(R.string.email_already_in_use)

                            is FirebaseAuthInvalidCredentialsException -> mailErrorMessage =
                                context.getString(R.string.email_invalid_format)

                            is FirebaseAuthInvalidUserException -> mailErrorMessage =
                                context.getString(R.string.user_account_disabled)

                            is FirebaseAuthRecentLoginRequiredException -> mailErrorMessage =
                                context.getString(R.string.recent_login_required)

                            is FirebaseTooManyRequestsException -> mailErrorMessage =
                                context.getString(R.string.too_many_requests)

                            is FirebaseAuthWeakPasswordException -> passwordErrorMessage =
                                context.getString(R.string.password_too_weak)

                            else -> errorMessage =
                                exception?.message ?: context.getString(R.string.signup_failed)
                        }
                    }
                    isLoading = false
                }*/
    }

    fun checkEmailVerification() {
        /*        viewModelScope.launch {
                    isLoading = true
                    clearMessages()
                    val user = authManager.getCurrentUser()
                    if (user != null) {
                        try {
                            user.reload().await()
                            isEmailVerified = user.isEmailVerified
                            if (isEmailVerified) {
                                successMessage = context.getString(R.string.email_verified)
                            } else {
                                errorMessage = context.getString(R.string.please_verify_email)
                            }
                        } catch (e: Exception) {
                            errorMessage = context.getString(R.string.error) + ": ${e.message}"
                        }
                    } else {
                        errorMessage = context.getString(R.string.user_not_found_or_disabled)
                    }
                    isLoading = false
                }*/
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

    fun loginWithEmail() {
        clearMessages()
        if (!validLoginData()) {
            return
        }
        /*        viewModelScope.launch {
                    isLoading = true
                    errorMessage = null
                    successMessage = null
                    val result = authManager.loginWithEmail(email, password)
                    if (result.isSuccess) {
                        successMessage = context.getString(R.string.login_successful)
                    } else {
                        when (val exception = result.exceptionOrNull()) {
                            is Exception -> {
                                when (exception.message) {
                                    "USER_NOT_FOUND" -> {
                                        mailErrorMessage =
                                            context.getString(R.string.user_not_found_or_disabled)
                                    }

                                    "INVALID_EMAIL" -> {
                                        mailErrorMessage = context.getString(R.string.email_invalid_format)
                                    }

                                    "WRONG_PASSWORD" -> {
                                        passwordErrorMessage = context.getString(R.string.wrong_credentials)
                                    }

                                    "INVALID_CREDENTIALS" -> {
                                        errorMessage = context.getString(R.string.invalid_credentials)
                                    }

                                    "EMAIL_ALREADY_IN_USE" -> {
                                        mailErrorMessage = context.getString(R.string.email_already_in_use)
                                    }

                                    "WEAK_PASSWORD" -> {
                                        passwordErrorMessage = context.getString(R.string.password_too_weak)
                                    }

                                    "EMAIL_NOT_VERIFIED" -> {
                                        errorMessage = context.getString(R.string.please_verify_email)
                                    }

                                    else -> {
                                        Log.e(
                                            TAG,
                                            "Unexpected login error: ${exception.message}",
                                            exception
                                        )
                                        errorMessage = context.getString(R.string.login_failed)
                                    }
                                }
                            }

                            is FirebaseNetworkException -> {
                                errorMessage = context.getString(R.string.network_error)
                            }

                            is FirebaseTooManyRequestsException -> {
                                errorMessage = context.getString(R.string.too_many_requests)
                            }

                            else -> {
                                Log.e(TAG, "Unexpected login error", exception)
                                errorMessage = context.getString(R.string.login_failed)
                            }
                        }
                    }
                    isLoading = false
                }*/
    }

    fun sendPasswordResetEmail() {
        clearMessages()
        if (email.isEmpty()) {
            mailErrorMessage = context.getString(R.string.fill_email_field)
            return
        }
        /*        viewModelScope.launch {
                    isLoading = true
                    errorMessage = null
                    successMessage = null
                    val result = authManager.sendPasswordResetEmail(email)
                    if (result.isSuccess) {
                        successMessage =
                            context.getString(R.string.success_message) // "Password reset email sent successfully"
                    } else {
                        mailErrorMessage = when (result.exceptionOrNull()?.message) {
                            "There is no user record corresponding to this identifier. The user may have been deleted." -> context.getString(
                                R.string.user_not_found_or_disabled
                            )

                            else -> result.exceptionOrNull()?.message ?: context.getString(R.string.error)
                        }
                    }
                    isLoading = false
                }*/
    }


    fun signOut() {
        viewModelScope.launch {
            isLoading = true
            clearMessages()
            try {
                //authManager.signOut()
                val clearRequest = ClearCredentialStateRequest()
                credentialManager.clearCredentialState(clearRequest)
                Log.d(TAG, "Signed out successfully")
                successMessage =
                    context.getString(R.string.signed_out_successfully) // "Signed Out Successfully"
            } catch (e: Exception) {
                Log.d(TAG, "Couldn't clear user credentials: ${e.localizedMessage}", e)
                errorMessage = context.getString(R.string.error) + ": ${e.message}"
            } finally {
                isLoading = false
            }
        }
    }

    private fun createNonce(): String {
        val rawNonce = UUID.randomUUID().toString()
        val byte = rawNonce.toByteArray()
        val md = MessageDigest.getInstance("SHA-256")
        val digest = md.digest(byte)
        return digest.fold("") { str, it -> str + "%02x".format(it) }
    }

    fun signInWithGoogle() {
        viewModelScope.launch {
            isLoading = true
            clearMessages()
            try {
                val googleIdOption = GetGoogleIdOption.Builder()
                    //.setServerClientId(context.getString(R.string.default_web_client_id))
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
            errorMessage = context.getString(R.string.error) // "Invalid credential type"
        }
    }

    private fun firebaseAuthWithGoogle(idToken: String) {
        /* viewModelScope.launch {
             val credential = GoogleAuthProvider.getCredential(idToken, null)
             try {
                 auth.signInWithCredential(credential).await()
                 Log.d(TAG, "signInWithCredential:success")
                 successMessage =
                     context.getString(R.string.sign_in_with_google) // "Google Sign-In Successful"
             } catch (e: Exception) {
                 Log.w(TAG, "signInWithCredential:failure", e)
                 errorMessage = context.getString(R.string.error) + ": ${e.message}"
             } finally {
                 isLoading = false
             }
         }*/
    }




    fun clearMessages() {
        errorMessage = null
        successMessage = null
        mailErrorMessage = null
        passwordErrorMessage = null
    }
}