package com.dldmswo1209.hallymtaxi.data.repository

import com.dldmswo1209.hallymtaxi.data.model.User
import com.dldmswo1209.hallymtaxi.data.UiState

interface AuthRepository {
    fun checkEmail(email: String, result: (UiState<String>) -> Unit)
    fun registerUser(user: User, password: String, result: (UiState<String>) -> Unit)
    fun checkLogged(email: String, deviceId: String, result: (UiState<String>) -> Unit)
    fun loginUser(email: String, password: String, deviceId: String, result: (UiState<String>) -> Unit)
    fun logoutUser(result: (UiState<String>)-> Unit)
    fun sendPasswordResetMail(email: String, result: (UiState<String>) -> Unit)
    fun deleteAccount(result: (UiState<String>) -> Unit)
}