package com.example.localites.models

data class CreateUserModel(
    val displayName: String = "",
    val email: String = "",
    val uid: String = "",
    val gender: String = ""
)