package com.example.parkmate.ui.screens.admin

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.parkmate.data.models.User
import com.example.parkmate.data.repository.FirestoreRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AdminViewModel @Inject constructor(
    private val repository: FirestoreRepository
) : ViewModel() {

    private val _users = MutableStateFlow<List<User>>(emptyList())
    val users: StateFlow<List<User>> = _users

    init {
        observeUsers()
    }

    private fun observeUsers() {
        repository.getAllUsersRealtime { fetched ->
            _users.value = fetched
        }
    }

    fun deleteUser(uid: String) {
        viewModelScope.launch {
            repository.deleteUser(uid)
        }
    }

    fun togglePremium(uid: String, isPremium: Boolean) {
        viewModelScope.launch {
            repository.updateUserField(uid, "premium", !isPremium)
        }
    }
}
