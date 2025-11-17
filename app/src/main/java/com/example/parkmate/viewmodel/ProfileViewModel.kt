package com.example.parkmate.viewmodel

import androidx.lifecycle.ViewModel
import com.example.parkmate.data.models.User
import com.example.parkmate.data.repository.FirestoreRepository
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.ListenerRegistration
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val repo: FirestoreRepository
) : ViewModel() {

    private val auth: FirebaseAuth = FirebaseAuth.getInstance()

    private val _user = MutableStateFlow<User?>(null)
    val user = _user.asStateFlow()

    private var listener: ListenerRegistration? = null

    init {
        startUserListener()
    }

    private fun startUserListener() {
        val uid = auth.currentUser?.uid ?: return
        listener = repo.listenUser(uid) { newUser ->
            _user.value = newUser
        }
    }

    override fun onCleared() {
        super.onCleared()
        listener?.remove()
    }

    fun signOut() {
        auth.signOut()
        listener?.remove()
        listener = null
    }
}
