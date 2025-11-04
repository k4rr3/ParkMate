package com.example.parkmate.auth


import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

class AuthViewModelFactory(
    private val context: Context
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(AuthViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            //return AuthViewModel(FirebaseAuthManager(context), context) as T
            return AuthViewModel( context) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}