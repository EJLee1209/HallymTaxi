package com.dldmswo1209.hallymtaxi.data.repository

import com.dldmswo1209.hallymtaxi.data.model.User
import com.dldmswo1209.hallymtaxi.util.UiState

interface AuthRepository {
    fun checkEmail(email: String, result: (UiState<String>) -> Unit)
    fun registerUser(user: User, password: String, result: (Boolean) -> Unit)
    fun loginUser(email: String, password: String, result: (UiState<String>) -> Unit)

    fun logoutUser(uid: String, result: (UiState<String>)-> Unit)

    fun getUserInfo(result: (UiState<User>) -> Unit)
}