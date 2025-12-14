// viewmodel/ProfileViewModel.kt
package com.example.parkmate.viewmodel

import androidx.compose.runtime.mutableStateMapOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.parkmate.data.models.User
import com.example.parkmate.data.preferences.UserPreferences
import com.example.parkmate.data.repository.FirestoreRepository
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.ListenerRegistration
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val repo: FirestoreRepository,
    private val userPreferences: UserPreferences  // Now properly injectable
) : ViewModel() {

    private val auth = FirebaseAuth.getInstance()

    // Public read-only user state
    private val _user = MutableStateFlow<User?>(null)
    val user: StateFlow<User?> = _user.asStateFlow()

    // Editable fields
    private val _name = MutableStateFlow("")
    private val _email = MutableStateFlow("")
    private val _phone = MutableStateFlow("")

    val name: StateFlow<String> = _name.asStateFlow()
    val email: StateFlow<String> = _email.asStateFlow()
    val phone: StateFlow<String> = _phone.asStateFlow()

    // Validation
    val emailValid: StateFlow<Boolean> = email
        .map { android.util.Patterns.EMAIL_ADDRESS.matcher(it).matches() }
        .stateIn(viewModelScope, SharingStarted.Eagerly, true)

    val phoneValid: StateFlow<Boolean> = phone
        .map { it.matches(Regex("^[0-9]{9,15}$")) }
        .stateIn(viewModelScope, SharingStarted.Eagerly, true)

    // Editing state
    val editingStates = mutableStateMapOf(
        "name" to false,
        "email" to false,
        "phone" to false
    )

    private var listener: ListenerRegistration? = null

    init {
        startUserListener()
    }

    private fun startUserListener() {
        val uid = auth.currentUser?.uid ?: return
        listener = repo.listenUser(uid) { user ->
            _user.value = user
            user?.let {
                _name.value = it.name
                _email.value = it.email
                _phone.value = it.phone
            }
        }
    }

    // Public update functions
    fun updateName(value: String) { _name.value = value }
    fun updateEmail(value: String) { _email.value = value }
    fun updatePhone(value: String) { _phone.value = value }

    // Save all changes
    fun saveChanges(onResult: (Boolean) -> Unit) {
        if (!emailValid.value || !phoneValid.value) {
            onResult(false)
            return
        }

        val uid = auth.currentUser?.uid ?: run {
            onResult(false)
            return
        }

        viewModelScope.launch {
            val updates = mapOf(
                "name" to _name.value,
                "email" to _email.value,
                "phone" to _phone.value
            )
            val success = repo.updateUser(uid, updates)
            onResult(success)
        }
    }

    // Save single field
    fun saveSingleField(field: String, value: String, onResult: (Boolean) -> Unit = {}) {
        val uid = auth.currentUser?.uid ?: run {
            onResult(false)
            return
        }

        viewModelScope.launch {
            val success = repo.updateUserField(uid, field, value)
            onResult(success)
        }
    }

    // Sign out
    fun signOut(onSignedOut: () -> Unit = {}) {
        viewModelScope.launch {
            auth.signOut()
            // Clear the "has logged in ever" flag
            userPreferences.setUserHasLoggedIn(false)
            listener?.remove()
            listener = null
            onSignedOut()
        }
    }

    fun exitEditingAll() {
        editingStates.keys.forEach { editingStates[it] = false }
    }

    override fun onCleared() {
        listener?.remove()
        super.onCleared()
    }


}