package com.dldmswo1209.hallymtaxi.data.repository

import android.util.Log
import com.dldmswo1209.hallymtaxi.data.model.User
import com.dldmswo1209.hallymtaxi.util.AuthResponse.EMAIL_EXIST
import com.dldmswo1209.hallymtaxi.util.AuthResponse.EMAIL_VALID
import com.dldmswo1209.hallymtaxi.util.FireStoreTable
import com.dldmswo1209.hallymtaxi.data.UiState
import com.dldmswo1209.hallymtaxi.data.model.SignedIn
import com.dldmswo1209.hallymtaxi.util.FireStoreTable.SIGNEDIN
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.toObject

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

    override fun checkLogged(email: String, deviceId: String, result: (UiState<String>) -> Unit) {
        fireStore.collection(SIGNEDIN).whereEqualTo("email", email)
            .get()
            .addOnSuccessListener {
                it?.let { snapshot ->
                    snapshot.documents.firstOrNull()?.let { document ->
                        val signedIn = document.toObject<SignedIn>()

                        if(signedIn != null) {
                            if(signedIn.deviceId == deviceId) {
                                result.invoke(
                                    UiState.Success("로그인 가능")
                                )
                            }else{
                                result.invoke(
                                    UiState.Failure("다른 기기에서 이미 로그인 했습니다")
                                )
                            }

                        } else {
                            result.invoke(
                                UiState.Success("로그인 가능")
                            )
                        }
                    }
                } ?: kotlin.run {
                    result.invoke(
                        UiState.Success("로그인 가능")
                    )
                }
            }
    }

    override fun loginUser(email: String, password: String, deviceId: String, result: (UiState<String>) -> Unit) {
        if(email.isEmpty() || password.isEmpty()){
            result.invoke(
                UiState.Failure("모든 정보를 입력해주세요")
            )
            return
        }
        auth.signInWithEmailAndPassword("$email@hallym.ac.kr", password).addOnSuccessListener {
            result.invoke(
                UiState.Success("로그인 성공")
            )
            fireStore.collection(SIGNEDIN).document(auth.currentUser!!.uid)
                .set(SignedIn(
                    uid = auth.currentUser!!.uid,
                    email = email,
                    deviceId = deviceId
                ))
        }.addOnFailureListener {
            result.invoke(
                UiState.Failure("이메일 또는 비밀번호를 확인해주세요")
            )
        }
    }

    override fun logoutUser(result: (UiState<String>) -> Unit) {
        val userInfo = mapOf<String, Any>(
            "fcmToken" to ""
        )
        fireStore.collection(FireStoreTable.USER).document(auth.currentUser!!.uid)
            .update(userInfo)
            .addOnSuccessListener {
                result.invoke(
                    UiState.Success("로그아웃 성공")
                )

                auth.signOut()
            }
            .addOnFailureListener {
                result.invoke(
                    UiState.Success("로그아웃 실패")
                )
            }
    }

    override fun getUserInfo(result: (UiState<User>) -> Unit) {
        if (auth.currentUser == null) {
            result.invoke(
                UiState.Failure("로그인 필요")
            )
            return
        }
        fireStore.collection(FireStoreTable.USER).document(auth.currentUser!!.uid)
            .get()
            .addOnSuccessListener {
                if(it == null){
                    result.invoke(
                        UiState.Failure("유저 정보가 없습니다")
                    )
                    return@addOnSuccessListener
                }

                it.toObject(User::class.java)?.let { user->
                    result.invoke(
                        UiState.Success(user)
                    )
                } ?: kotlin.run { result.invoke(UiState.Failure("유저 정보가 없습니다")) }
            }
            .addOnFailureListener {
                result.invoke(
                    UiState.Failure("유저 정보를 가져오지 못했습니다")
                )
            }
    }

    override fun updateUserName(newName: String, result: (UiState<String>) -> Unit) {
        if (auth.currentUser == null) {
            result.invoke(
                UiState.Failure("로그인 필요")
            )
            return
        }
        fireStore.collection(FireStoreTable.USER).document(auth.currentUser!!.uid)
            .update("name", newName)
            .addOnSuccessListener {
                result.invoke(
                    UiState.Success("이름 변경 성공")
                )
            }
            .addOnFailureListener {
                result.invoke(
                    UiState.Failure("이름 변경 실패")
                )
            }

    }

}