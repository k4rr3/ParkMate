package com.example.parkmate.mock

data class User(
    val id: String,
    val name: String,
    val email: String,
    val status: UserStatus
)

enum class UserStatus {
    ACTIVE,
    PENDING,
    INACTIVE,
    ADMIN
}

// Sample data for the UI
val sampleUsers = listOf(
    User(
        id = "1",
        name = "Sarah Johnson",
        email = "sarah.j@gmail.com",
        status = UserStatus.ACTIVE
    ),
    User(
        id = "2",
        name = "Mike Chen",
        email = "mike.chen@gmail.com",
        status = UserStatus.PENDING
    ),
    User(
        id = "3",
        name = "Alex Rodriguez",
        email = "alex.r@gmail.com",
        status = UserStatus.INACTIVE
    ),
    User(
        id = "4",
        name = "Emma Wilson",
        email = "emma.w@gmail.com",
        status = UserStatus.ACTIVE
    ),
    User(
        id = "5",
        name = "David Kim",
        email = "david.k@gmail.com",
        status = UserStatus.ADMIN
    )
)
