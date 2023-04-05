package com.dldmswo1209.hallymtaxi.data.repository

import android.content.Context
import android.util.Log
import com.dldmswo1209.hallymtaxi.data.model.User
import com.dldmswo1209.hallymtaxi.util.FireStoreTable
import com.dldmswo1209.hallymtaxi.data.UiState
import com.dldmswo1209.hallymtaxi.data.model.SignedIn
import com.dldmswo1209.hallymtaxi.data.model.TokenInfo
import com.dldmswo1209.hallymtaxi.util.AuthResponse
import com.dldmswo1209.hallymtaxi.util.FireStoreResponse
import com.dldmswo1209.hallymtaxi.util.ServerResponse
import com.google.firebase.FirebaseNetworkException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthException
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthInvalidUserException
import com.google.firebase.auth.FirebaseAuthRecentLoginRequiredException
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.toObject
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.launch

class AuthRepositoryImpl(
    private val auth: FirebaseAuth,
    private val fireStore: FirebaseFirestore,
    private val context: Context
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

    override fun registerUser(user: User, password: String, result: (UiState<String>) -> Unit) {
        result.invoke(UiState.Loading)

        auth.createUserWithEmailAndPassword(user.email, password)
            .addOnSuccessListener {
                auth.currentUser?.let {firebaseUser ->
                    user.uid = firebaseUser.uid

                    fireStore.collection(FireStoreTable.USER).document(user.uid)
                        .set(user)
                        .addOnSuccessListener {
                            fireStore.collection(FireStoreTable.FCMTOKENS).document(user.uid)
                                .set(TokenInfo())
                                .addOnSuccessListener {
                                    result.invoke(UiState.Success(AuthResponse.REGISTER_SUCCESS))
                                }
                                .addOnFailureListener {
                                    result.invoke(UiState.Failure(AuthResponse.SAVE_TOKEN_FAILED))
                                }
                        }
                        .addOnFailureListener {
                            result.invoke(UiState.Failure(AuthResponse.SAVE_USER_INFO_FAILED))
                        }

                } ?: kotlin.run {
                    result.invoke(UiState.Failure(FireStoreResponse.SUBSCRIBE_USER_ERROR))
                }
            }
            .addOnFailureListener {
                when(it) {
                    is FirebaseNetworkException -> {
                        result.invoke(UiState.Failure(ServerResponse.NETWORK_ERROR))
                    }
                    else -> {
                        result.invoke(UiState.Failure(AuthResponse.REGISTER_FAILED))
                    }
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

    override fun deleteAccount(result: (UiState<String>) -> Unit) {
        auth.currentUser?.let { user ->
            val uid = user.uid

            CoroutineScope(Dispatchers.IO).launch {
                async {
                    fireStore.collection(FireStoreTable.USER)
                        .document(uid)
                        .delete()
                    fireStore.collection(FireStoreTable.SIGNEDIN)
                        .document(uid)
                        .delete()
                    fireStore.collection(FireStoreTable.FCMTOKENS)
                        .document(uid)
                        .delete()
                }.join()

                user.delete()
                    .addOnSuccessListener {
                        result.invoke(UiState.Success("계정 삭제 성공"))
                    }
                    .addOnFailureListener {
                        when(it) {
                            is FirebaseAuthRecentLoginRequiredException -> {
                                val sharedPreferences = context.getSharedPreferences("loggedInfo", Context.MODE_PRIVATE)
                                val email = sharedPreferences.getString("email", null) ?: return@addOnFailureListener
                                val password = sharedPreferences.getString("password", null) ?: return@addOnFailureListener

                                auth.signInWithEmailAndPassword("${email}@hallym.ac.kr", password)
                                    .addOnSuccessListener {
                                        user.delete()
                                            .addOnSuccessListener {
                                                result.invoke(UiState.Success("계정 삭제 성공"))
                                            }
                                    }
                                    .addOnFailureListener {
                                        result.invoke(UiState.Failure("계정 삭제 실패"))
                                    }
                            }
                        }
                    }
            }

        } ?: kotlin.run {
            result.invoke(UiState.Failure("로그인이 필요합니다"))
        }



    }
}