package com.dldmswo1209.hallymtaxi.data.repository

import android.util.Log
import com.dldmswo1209.hallymtaxi.data.model.User
import com.dldmswo1209.hallymtaxi.util.FireStoreTable
import com.dldmswo1209.hallymtaxi.data.UiState
import com.dldmswo1209.hallymtaxi.data.model.SignedIn
import com.dldmswo1209.hallymtaxi.data.model.TokenInfo
import com.dldmswo1209.hallymtaxi.util.AuthResponse
import com.dldmswo1209.hallymtaxi.util.ServerResponse
import com.google.firebase.FirebaseNetworkException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthInvalidUserException
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.toObject

class AuthRepositoryImpl(
    private val auth: FirebaseAuth,
    private val fireStore: FirebaseFirestore
) : AuthRepository {
    override fun checkEmail(email: String, result: (UiState<String>) -> Unit) {
        auth.createUserWithEmailAndPassword(email, "123456").addOnSuccessListener {
            result.invoke(
                UiState.Success(AuthResponse.EMAIL_VALID)
            )

            auth.currentUser?.delete()

        }.addOnFailureListener {
            when(it) {
                is FirebaseAuthInvalidCredentialsException -> {
                    result.invoke(UiState.Failure(AuthResponse.MAIL_BADLY_FORMATTED))
                }
                is FirebaseNetworkException -> {
                    result.invoke(UiState.Failure(ServerResponse.NETWORK_ERROR))
                }
                is FirebaseAuthUserCollisionException -> {
                    result.invoke(UiState.Failure(AuthResponse.EMAIL_EXIST))
                }
                else -> {
                    result.invoke(UiState.Failure(ServerResponse.NETWORK_ERROR))
                }
            }
        }
    }

    override fun registerUser(user: User, password: String, result: (Boolean) -> Unit) {
        auth.createUserWithEmailAndPassword(user.email, password).addOnCompleteListener {
            if(it.isSuccessful){
                auth.currentUser?.let {firebaseUser ->
                    user.uid = firebaseUser.uid

                    fireStore.collection(FireStoreTable.USER).document(user.uid).set(user)
                    fireStore.collection(FireStoreTable.FCMTOKENS).document(user.uid).set(TokenInfo())
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
        fireStore.collection(FireStoreTable.SIGNEDIN).whereEqualTo("email", email)
            .get()
            .addOnSuccessListener {
                it?.let { snapshot ->
                    snapshot.documents.firstOrNull()?.let { document ->
                        val signedIn = document.toObject<SignedIn>()

                        if(signedIn != null) {
                            if(signedIn.deviceId == deviceId) {
                                result.invoke(
                                    UiState.Success(AuthResponse.LOGIN_POSSIBLE)
                                )
                            }else{
                                result.invoke(
                                    UiState.Failure(AuthResponse.LOGIN_IMPOSSIBLE)
                                )
                            }

                        } else {
                            result.invoke(
                                UiState.Success(AuthResponse.LOGIN_POSSIBLE)
                            )
                        }
                    }
                } ?: kotlin.run {
                    result.invoke(
                        UiState.Success(AuthResponse.LOGIN_POSSIBLE)
                    )
                }
            }
            .addOnFailureListener {
                result.invoke(UiState.Failure(ServerResponse.NETWORK_ERROR))
            }
    }

    override fun loginUser(email: String, password: String, deviceId: String, result: (UiState<String>) -> Unit) {
        if(email.isEmpty() || password.isEmpty()){
            result.invoke(
                UiState.Failure(AuthResponse.LOGIN_EMPTY)
            )
            return
        }
        auth.signInWithEmailAndPassword("$email@hallym.ac.kr", password).addOnSuccessListener {
            result.invoke(
                UiState.Success(AuthResponse.LOGIN_SUCCESS)
            )
            fireStore.collection(FireStoreTable.SIGNEDIN).document(auth.currentUser!!.uid)
                .set(SignedIn(
                    uid = auth.currentUser!!.uid,
                    email = email,
                    deviceId = deviceId
                ))
        }.addOnFailureListener {
            when(it) {
                is FirebaseAuthInvalidCredentialsException -> {
                    result.invoke(UiState.Failure(AuthResponse.LOGIN_FAILED))
                }
                is FirebaseAuthInvalidUserException -> {
                    result.invoke(UiState.Failure(AuthResponse.MAIL_NO_USER_RECORD))
                }
                is FirebaseNetworkException -> {
                    result.invoke(UiState.Failure(ServerResponse.NETWORK_ERROR))
                }
                else -> {
                    result.invoke(UiState.Failure(AuthResponse.LOGIN_FAILED))
                }
            }
        }
    }

    override fun logoutUser(result: (UiState<String>) -> Unit) {
        fireStore.collection(FireStoreTable.FCMTOKENS).document(auth.currentUser!!.uid)
            .update(mapOf(FireStoreTable.FIELD_TOKEN to ""))
            .addOnSuccessListener {
                result.invoke(
                    UiState.Success(AuthResponse.LOGOUT_SUCCESS)
                )

                auth.signOut()
            }
            .addOnFailureListener {
                result.invoke(
                    UiState.Failure(AuthResponse.LOGOUT_FAILED)
                )
            }
    }

    override fun sendPasswordResetMail(email: String, result: (UiState<String>) -> Unit) {
        if(email.isEmpty()) {
            result.invoke(UiState.Failure(AuthResponse.SEND_PASSWORD_RESET_MAIL_EMPTY))
            return
        }

        auth.sendPasswordResetEmail("$email@hallym.ac.kr")
            .addOnSuccessListener {
                result.invoke(UiState.Success(AuthResponse.SEND_PASSWORD_RESET_MAIL_SUCCESS))
            }
            .addOnFailureListener {
                when(it) {
                    is FirebaseAuthInvalidCredentialsException -> {
                        result.invoke(UiState.Failure(AuthResponse.MAIL_BADLY_FORMATTED))
                    }
                    is FirebaseAuthInvalidUserException -> {
                        result.invoke(UiState.Failure(AuthResponse.MAIL_NO_USER_RECORD))
                    }
                    is FirebaseNetworkException -> {
                        result.invoke(UiState.Failure(ServerResponse.NETWORK_ERROR))
                    }
                    else -> {
                        result.invoke(UiState.Failure(AuthResponse.SEND_PASSWORD_RESET_UNKNOWN_ERROR))
                    }
                }
            }
    }

}