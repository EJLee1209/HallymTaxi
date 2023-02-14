package com.dldmswo1209.hallymtaxi.data.repository

import com.dldmswo1209.hallymtaxi.data.model.User
import com.dldmswo1209.hallymtaxi.util.AuthResponse.EMAIL_EXIST
import com.dldmswo1209.hallymtaxi.util.AuthResponse.EMAIL_VALID
import com.dldmswo1209.hallymtaxi.util.FireStoreTable
import com.dldmswo1209.hallymtaxi.util.UiState
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class AuthRepositoryImpl(
    private val auth: FirebaseAuth,
    private val fireStore: FirebaseFirestore
) : AuthRepository {
    override fun checkEmail(email: String, result: (UiState<String>) -> Unit) {
        auth.createUserWithEmailAndPassword(email, "123456").addOnSuccessListener {
            result.invoke(
                UiState.Success(EMAIL_VALID)
            )

            auth.currentUser?.delete()

        }.addOnFailureListener {
            result.invoke(
                UiState.Failure(EMAIL_EXIST)
            )
        }
    }

    override fun registerUser(user: User, password: String, result: (Boolean) -> Unit) {
        auth.createUserWithEmailAndPassword(user.email, password).addOnCompleteListener {
            if(it.isSuccessful){
                auth.currentUser?.let {firebaseUser ->
                    user.uid = firebaseUser.uid
                    fireStore.collection(FireStoreTable.USER).document(user.uid).set(user)
                    result.invoke(true)
                } ?: kotlin.run {
                    result.invoke(false)
                }

            }else{
                result.invoke(false)
            }
        }
    }

    override fun loginUser(email: String, password: String, result: (UiState<String>) -> Unit) {
        if(email.isEmpty() || password.isEmpty()){
            result.invoke(
                UiState.Failure("모든 정보를 입력해주세요")
            )
        }
        auth.signInWithEmailAndPassword("$email@hallym.ac.kr", password).addOnSuccessListener {
            result.invoke(
                UiState.Success("로그인 성공")
            )
        }.addOnFailureListener {
            result.invoke(
                UiState.Failure("로그인 실패, 이메일 또는 비밀번호를 확인해주세요")
            )
        }
    }

}