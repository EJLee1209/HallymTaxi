package com.dldmswo1209.hallymtaxi.data.repository

import android.util.Log
import com.dldmswo1209.hallymtaxi.data.model.CarPoolRoom
import com.dldmswo1209.hallymtaxi.data.model.User
import com.dldmswo1209.hallymtaxi.util.FireStoreResponse
import com.dldmswo1209.hallymtaxi.util.FireStoreTable
import com.dldmswo1209.hallymtaxi.data.UiState
import com.dldmswo1209.hallymtaxi.data.model.SignedIn
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreException
import com.google.firebase.firestore.ktx.toObject
import com.google.firebase.messaging.FirebaseMessaging
import kotlinx.coroutines.cancel
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow

class FireStoreRepositoryImpl(
    private val fireStore: FirebaseFirestore,
    private val auth: FirebaseAuth
): FireStoreRepository {
    override fun subscribeUser(): Flow<User> {
        return callbackFlow {
            val listenerRegistration = fireStore.collection(FireStoreTable.USER).document(auth.currentUser!!.uid)
                .addSnapshotListener { snapshot, error->
                    if(error != null){
                        cancel(
                            message = FireStoreResponse.SUBSCRIBE_USER_ERROR,
                            cause = error
                        )
                        return@addSnapshotListener
                    }
                    val user = snapshot?.toObject<User>()
                    user?.let { trySend(it) }
                }
            awaitClose {
                listenerRegistration.remove()
            }
        }
    }

    override fun subscribeMyRoom(): Flow<CarPoolRoom> {
        return callbackFlow {
            auth.currentUser?.let { user ->
                val listenerRegistration = fireStore.collection(FireStoreTable.ROOM)
                    .whereArrayContains(FireStoreTable.FIELD_PARTICIPANTS , user.uid)
                    .addSnapshotListener { value, error ->
                        if(error != null){
                            cancel(
                                message = FireStoreResponse.SUBSCRIBE_ROOM_ERROR,
                                cause = error
                            )
                            return@addSnapshotListener
                        }
                        value?.let { querySnapshot ->
                            if(!querySnapshot.isEmpty) {
                                val data = querySnapshot.first().toObject<CarPoolRoom>()
                                trySend(data)
                            }else{
                                this.trySend(CarPoolRoom())
                            }
                        }

                    }
                awaitClose {
                    listenerRegistration.remove()
                }
            } ?: kotlin.run {
                trySend(CarPoolRoom())
            }
        }
    }

    override fun subscribeParticipantsTokens(roomId: String): Flow<List<String>> {
        return callbackFlow {
            val listenerRegistration = fireStore.collection(FireStoreTable.FCMTOKENS)
                .whereEqualTo(FireStoreTable.FIELD_ROOM_ID, roomId)
                .addSnapshotListener { value, error ->
                    if(error != null){
                        cancel(
                            message = FireStoreResponse.SUBSCRIBE_TOKENS,
                            cause = error
                        )
                        return@addSnapshotListener
                    }
                    value?.let { querySnapshot ->
                        val tokens = mutableListOf<String>()
                        querySnapshot.documents.forEach { document ->
                            document.getString(FireStoreTable.FIELD_TOKEN)?.let {
                                tokens.add(it)
                            }
                        }
                        trySend(tokens)
                    }
                }

            awaitClose {
                listenerRegistration.remove()
            }
        }
    }

    override fun findUserName(uid: String, result: (UiState<String>) -> Unit) {
        fireStore.collection(FireStoreTable.USER).document(uid)
            .get()
            .addOnSuccessListener { documentSnapshot ->
                documentSnapshot.getString(FireStoreTable.FIELD_NAME)?.let { name ->
                    result.invoke(UiState.Success(name))
                } ?: kotlin.run {
                    result.invoke(UiState.Failure(FireStoreResponse.FIND_NAME_EMPTY))
                }
            }
            .addOnFailureListener {
                result.invoke(UiState.Failure(FireStoreResponse.FIND_NAME_FAILED))
            }
    }

    override fun getParticipantsTokens(roomId: String, result: (UiState<List<String>>) -> Unit) {
        fireStore.collection(FireStoreTable.FCMTOKENS)
            .whereEqualTo(FireStoreTable.FIELD_ROOM_ID, roomId)
            .get()
            .addOnSuccessListener { querySnapshot ->
                querySnapshot?.let {
                    val tokens = mutableListOf<String>()
                    it.documents.forEach { document ->
                        document.getString(FireStoreTable.FIELD_TOKEN)?.let { token ->
                            tokens.add(token)
                        }
                    }
                    result.invoke(UiState.Success(tokens))
                } ?: kotlin.run {
                    result.invoke(UiState.Failure(FireStoreResponse.GET_TOKENS_EMPTY))
                }
            }
            .addOnFailureListener {
                result.invoke(UiState.Failure(FireStoreResponse.GET_TOKENS_FAILED))
            }
    }
    override fun createRoom(room: CarPoolRoom, result: (UiState<CarPoolRoom>) -> Unit) {
        val ref = fireStore.collection(FireStoreTable.ROOM).document()

        room.roomId = ref.id
        ref.set(room)
            .addOnSuccessListener {
                fireStore.collection(FireStoreTable.FCMTOKENS).document(auth.currentUser!!.uid)
                    .update(mapOf(FireStoreTable.FIELD_ROOM_ID to room.roomId))
                    .addOnSuccessListener {
                        result.invoke(UiState.Success(room))
                    }
                    .addOnFailureListener {
                        result.invoke(UiState.Failure(FireStoreResponse.CREATE_ROOM_FAILED))
                    }
            }
            .addOnFailureListener {
                result.invoke(UiState.Failure(FireStoreResponse.CREATE_ROOM_FAILED))
            }
    }

    override fun joinRoom(room: CarPoolRoom, result: (UiState<String>) -> Unit) {
        if(auth.currentUser == null) {
            result.invoke(UiState.Failure(FireStoreResponse.SUBSCRIBE_USER_ERROR))
            return
        }

        if(room.participants.contains(auth.currentUser!!.uid)){
            // 이미 방에 속해 있음
            result.invoke(UiState.Success(FireStoreResponse.JOIN_ROOM_ALREADY_JOINED))
            return
        }else{
            val docRef = fireStore.collection(FireStoreTable.ROOM).document(room.roomId)
            docRef.get().addOnSuccessListener { documentSnapshot->
                if(documentSnapshot.data == null){
                    result.invoke(
                        UiState.Failure(FireStoreResponse.JOIN_ROOM_CANT_FIND)
                    )
                    return@addOnSuccessListener
                }else{
                    // transaction 을 사용해서 하나의 document 에 접근 하려는 다수의 작업들을 통제
                    // 예를 들어 인원제한이 3명인 채팅방에 현재 2명이 입장해 있고, 2명 이상의 유저가 동시에 채팅방 입장을 하려는 경우
                    // 먼저 입장이 완료된 유저만 성공시키고, 나머지 유저는 실패 처리
                    fireStore.runTransaction {transaction->
                        val snapshot = transaction.get(docRef)
                        val newCount = snapshot.getLong(FireStoreTable.FIELD_USER_COUNT)?.plus(1) ?: return@runTransaction
                        val closed = snapshot.getBoolean(FireStoreTable.FIELD_CLOSED) ?: return@runTransaction

                        if(newCount <= room.userMaxCount && !closed){
                            transaction.update(docRef, FireStoreTable.FIELD_USER_COUNT, FieldValue.increment(1))
                            transaction.update(docRef, FireStoreTable.FIELD_PARTICIPANTS, FieldValue.arrayUnion(auth.currentUser!!.uid))
                        }else{
                            if(closed)
                                throw FirebaseFirestoreException(FireStoreResponse.JOIN_ROOM_INACTIVE, FirebaseFirestoreException.Code.ABORTED)
                            else if(newCount > room.userMaxCount)
                                throw FirebaseFirestoreException(FireStoreResponse.JOIN_ROOM_COUNT_OVER, FirebaseFirestoreException.Code.ABORTED)
                        }
                    }
                        .addOnSuccessListener {
                            // 입장 성공
                            fireStore.collection(FireStoreTable.FCMTOKENS).document(auth.currentUser!!.uid)
                                .update(mapOf(FireStoreTable.FIELD_ROOM_ID to room.roomId))
                                .addOnSuccessListener {
                                    result.invoke(UiState.Success(FireStoreResponse.JOIN_ROOM_SUCCESS))
                                }
                                .addOnFailureListener {
                                    result.invoke(
                                        UiState.Failure(FireStoreResponse.JOIN_ROOM_FAILED)
                                    )
                                }
                        }
                        .addOnFailureListener {
                            // 입장 실패
                            result.invoke(
                                UiState.Failure(it.message)
                            )
                        }
                }
            }
        }
    }

    override fun exitRoom(room: CarPoolRoom, result: (UiState<String>) -> Unit) {
        if(auth.currentUser == null) {
            result.invoke(UiState.Failure(FireStoreResponse.SUBSCRIBE_USER_ERROR))
            return
        }

        val docRef = fireStore.collection(FireStoreTable.ROOM).document(room.roomId)
        fireStore.runTransaction{transaction->
            val snapshot = transaction.get(docRef)
            val userCount = snapshot.getLong(FireStoreTable.FIELD_USER_COUNT)?.minus(1) ?: return@runTransaction

            if(userCount > 0){
                transaction.update(docRef,FireStoreTable.FIELD_USER_COUNT,userCount)
                transaction.update(docRef, FireStoreTable.FIELD_PARTICIPANTS, FieldValue.arrayRemove(auth.currentUser!!.uid))
            }else{
                fireStore.collection(FireStoreTable.ROOM).document(room.roomId).delete()
                result.invoke(UiState.Success(FireStoreResponse.EXIT_ROOM_SUCCESS))
            }
        }
            .addOnSuccessListener {
                fireStore.collection(FireStoreTable.FCMTOKENS).document(auth.currentUser!!.uid)
                    .update(mapOf(FireStoreTable.FIELD_ROOM_ID to ""))
                    .addOnSuccessListener {
                        result.invoke(UiState.Success(FireStoreResponse.EXIT_ROOM_SUCCESS))
                    }
            }
    }

    override fun deactivateRoom(roomId: String, result: (UiState<String>) -> Unit) {
        fireStore.collection(FireStoreTable.ROOM).document(roomId)
            .update(FireStoreTable.FIELD_CLOSED, true)
            .addOnSuccessListener {
                result.invoke(
                    UiState.Success(FireStoreResponse.DEACTIVATE_ROOM_SUCCESS)
                )
            }
            .addOnFailureListener {
                result.invoke(
                    UiState.Failure(FireStoreResponse.DEACTIVATE_ROOM_FAILED)
                )
            }
    }

    override fun updateFcmToken(result: (UiState<String>)->Unit) {
        FirebaseMessaging.getInstance().token.addOnCompleteListener { task ->
            if (!task.isSuccessful) {
                result.invoke(
                    UiState.Failure(FireStoreResponse.UPDATE_TOKEN_TASK_FAILED)
                )
                return@addOnCompleteListener
            }
            if(auth.currentUser == null){
                result.invoke(
                    UiState.Failure(FireStoreResponse.SUBSCRIBE_USER_ERROR)
                )
                return@addOnCompleteListener
            }

            fireStore.collection(FireStoreTable.FCMTOKENS).document(auth.currentUser!!.uid)
                .update(mapOf(FireStoreTable.FIELD_TOKEN to task.result))
                .addOnSuccessListener {
                    result.invoke(
                        UiState.Success(task.result)
                    )
                }
                .addOnFailureListener {
                    result.invoke(
                        UiState.Failure(task.result)
                    )
                }

        }
            .addOnFailureListener {
                result.invoke(
                    UiState.Failure(FireStoreResponse.UPDATE_TOKEN_TASK_FAILED)
                )
            }
    }

    override fun monitoringLoggedIn(): Flow<SignedIn> {
        return callbackFlow {
            val listenerRegistration = fireStore.collection(FireStoreTable.SIGNEDIN).document(auth.currentUser!!.uid)
                .addSnapshotListener { snapshot, error->
                    if(error != null){
                        cancel(
                            message = FireStoreResponse.MONITORING_ERROR,
                            cause = error
                        )
                        return@addSnapshotListener
                    }
                    val signedIn = snapshot?.toObject<SignedIn>()
                    signedIn?.let { trySend(it) }
                }
            awaitClose {
                listenerRegistration.remove()
            }
        }
    }
}