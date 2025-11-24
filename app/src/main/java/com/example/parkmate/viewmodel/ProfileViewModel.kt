package com.example.parkmate.viewmodel

import androidx.compose.runtime.mutableStateMapOf
import androidx.lifecycle.ViewModel
import com.example.parkmate.data.models.User
import com.example.parkmate.data.repository.FirestoreRepository
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.ListenerRegistration
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import javax.inject.Inject

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val repo: FirestoreRepository
) : ViewModel() {

    private val auth: FirebaseAuth = FirebaseAuth.getInstance()

    private val _user = MutableStateFlow<User?>(null)
    val user = _user.asStateFlow()

    private var listener: ListenerRegistration? = null

    // Editable fields
    val name = MutableStateFlow("")
    val email = MutableStateFlow("")
    val phone = MutableStateFlow("")

    // Validation states
    val emailValid = email.map { isValidEmail(it) }
        .stateIn(scope = kotlinx.coroutines.CoroutineScope(kotlinx.coroutines.Dispatchers.Default),
            started = SharingStarted.Eagerly,
            initialValue = true
        )

    val phoneValid = phone.map { isValidPhone(it) }
        .stateIn(scope = kotlinx.coroutines.CoroutineScope(kotlinx.coroutines.Dispatchers.Default),
            started = SharingStarted.Eagerly,
            initialValue = true
        )

    init {
        startUserListener()
    }

    private fun startUserListener() {
        val uid = auth.currentUser?.uid ?: return
        listener = repo.listenUser(uid) { newUser ->
            _user.value = newUser

            newUser?.let {
                name.value = it.name
                email.value = it.email
                phone.value = it.phone
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        listener?.remove()
    }

    suspend fun saveChanges(): Boolean {
        if (!emailValid.value || !phoneValid.value) return false

        val uid = auth.currentUser?.uid ?: return false
        val updates = mapOf(
            "name" to name.value,
            "email" to email.value,
            "phone" to phone.value
        )
        return repo.updateUser(uid, updates)
    }

    suspend fun saveSingleField(field: String, value: String): Boolean {
        val uid = auth.currentUser?.uid ?: return false
        return repo.updateUserField(uid, field, value)
    }

    fun signOut() {
        auth.signOut()
        listener?.remove()
        listener = null
    }

    /** VALIDATION HELPERS */
    private fun isValidEmail(value: String): Boolean =
        android.util.Patterns.EMAIL_ADDRESS.matcher(value).matches()

    private fun isValidPhone(value: String): Boolean =
        value.matches(Regex("^[0-9]{9,15}$"))

    // Track editing state for each field
    val editingStates = mutableStateMapOf(
        "name" to false,
        "email" to false,
        "phone" to false
    )

    // Helper to exit editing for all fields
    fun exitEditingAll() {
        editingStates.keys.forEach { key -> editingStates[key] = false }
    }

}
