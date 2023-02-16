package com.dldmswo1209.hallymtaxi.vm

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.dldmswo1209.hallymtaxi.data.model.User
import com.dldmswo1209.hallymtaxi.data.model.VerifyInfo
import com.dldmswo1209.hallymtaxi.data.repository.AuthRepository
import com.dldmswo1209.hallymtaxi.data.repository.ServerRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject
import com.dldmswo1209.hallymtaxi.util.UiState
import kotlinx.coroutines.Dispatchers

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val serverRepository: ServerRepository,
    private val authRepository: AuthRepository,
    application: Application
): AndroidViewModel(application) {

    private var _isSentMail = MutableLiveData<UiState<VerifyInfo>>()
    val isSentMail : LiveData<UiState<VerifyInfo>> = _isSentMail

    private var _checkEmail = MutableLiveData<UiState<String>>()
    val checkEmail : LiveData<UiState<String>> = _checkEmail

    private var _isVerified = MutableLiveData<UiState<VerifyInfo>>()
    val isVerified : LiveData<UiState<VerifyInfo>> = _isVerified

    private var _isCreatedUser = MutableLiveData<Boolean>()
    val isCreatedUser : LiveData<Boolean> = _isCreatedUser

    private var _login = MutableLiveData<UiState<String>>()
    val login : LiveData<UiState<String>> = _login

    var codeEffectiveTime = 300

    fun continueTimer(){
        if(codeEffectiveTime > 0) codeEffectiveTime--
    }

    fun resetTimer(){
        codeEffectiveTime = 300
    }

    fun checkEmail(email: String){
        _checkEmail.postValue(UiState.Loading)
        authRepository.checkEmail(email){ _checkEmail.postValue(it) }
    }

    fun sendVerifyMail(email: String) = viewModelScope.launch(Dispatchers.IO) {
        _isSentMail.postValue(UiState.Loading)
        serverRepository.sendVerifyMail(email){
            _isSentMail.postValue(it)
        }
    }

    fun requestVerify(email: String, code: String) = viewModelScope.launch(Dispatchers.IO) {
        _isVerified.postValue(UiState.Loading)
        serverRepository.requestVerify(email, code){
            _isVerified.postValue(it)
        }
    }

    fun createUser(user: User, password: String){
        authRepository.registerUser(user, password){
            _isCreatedUser.postValue(it)
        }
    }

    fun login(email: String, password: String){
        _login.postValue(UiState.Loading)
        authRepository.loginUser(email, password){
            _login.postValue(it)
        }
    }

}