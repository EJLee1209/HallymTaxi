package com.dldmswo1209.hallymtaxi.vm

import android.app.Application
import android.content.Context
import android.content.Intent
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dldmswo1209.hallymtaxi.common.context
import com.dldmswo1209.hallymtaxi.model.User
import com.dldmswo1209.hallymtaxi.model.VerifyInfo
import com.dldmswo1209.hallymtaxi.repository.WelcomeRepository
import com.dldmswo1209.hallymtaxi.ui.MainActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.launch

class WelcomeViewModel(
    private val welcomeRepository: WelcomeRepository,
    application: Application
): AndroidViewModel(application) {

    private val auth = FirebaseAuth.getInstance()
    private val fireStore = FirebaseFirestore.getInstance()

    private var _existUser = MutableLiveData<Boolean>()
    val existUser : LiveData<Boolean> = _existUser

    private var _isSent = MutableLiveData<VerifyInfo>()
    val isSent : LiveData<VerifyInfo> = _isSent

    private var _isVerifiedUser = MutableLiveData<VerifyInfo>()
    val isVerifiedUser: LiveData<VerifyInfo> = _isVerifiedUser

    private var _isVerified = MutableLiveData<VerifyInfo>()
    val isVerified : LiveData<VerifyInfo> = _isVerified

    private var _isCreatedUser = MutableLiveData<Boolean>()
    val isCreatedUser : LiveData<Boolean> = _isCreatedUser

    private var _loginResult = MutableLiveData<Boolean>()
    val loginResult : LiveData<Boolean> = _loginResult

    var codeEffectiveTime = 300

    fun continueTimer(){
        if(codeEffectiveTime > 0) codeEffectiveTime--
    }

    fun resetTimer(){
        codeEffectiveTime = 300
    }

    fun sendVerifyMail(email: String) {
        auth.createUserWithEmailAndPassword(email, "123456").addOnSuccessListener {
            // 해당 이메일 정보로 존재하는 계정이 없음
            viewModelScope.launch {
                _isSent.value = welcomeRepository.sendVerifyMail(email)
            }
            auth.currentUser?.delete()

        }.addOnFailureListener {
            _existUser.value = true // 이미 계정이 존재
        }

    }

    fun requestVerify(email: String, code: String) = viewModelScope.launch {
        _isVerified.value = welcomeRepository.requestVerify(email, code)
    }

    fun confirmVerified(email: String) = viewModelScope.launch {
        _isVerifiedUser.value = welcomeRepository.confirmVerified(email)
    }

    fun createUser(user: User, password: String){
        auth.createUserWithEmailAndPassword(user.email, password).addOnCompleteListener {
            if(it.isSuccessful){
                user.uid = auth.currentUser?.uid ?: kotlin.run {
                    // 회원가입 실패 처리(현재 유저의 uid를 가져오지 못함)
                    _isCreatedUser.value = false
                    return@addOnCompleteListener
                }
                fireStore.collection("User").document(user.uid).set(user)
                
                _isCreatedUser.value = true
            }else{
                _isCreatedUser.value = false
            }
        }
    }

    fun login(email: String, password: String){
        if(email.isEmpty() || password.isEmpty()){
            _loginResult.value = false
            return
        }
        auth.signInWithEmailAndPassword("$email@hallym.ac.kr", password).addOnSuccessListener {
            val uid = auth.currentUser?.uid

            context.getSharedPreferences("login", Context.MODE_PRIVATE)
                .edit()
                .putString("uid", uid)
                .apply()
            _loginResult.value = true
        }.addOnFailureListener {
            _loginResult.value = false
        }
    }

}