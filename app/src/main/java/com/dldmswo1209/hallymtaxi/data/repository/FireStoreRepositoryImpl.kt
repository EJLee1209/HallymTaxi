package com.dldmswo1209.hallymtaxi.data.repository

import android.util.Log
import com.dldmswo1209.hallymtaxi.data.model.CarPoolRoom
import com.dldmswo1209.hallymtaxi.data.model.User
import com.dldmswo1209.hallymtaxi.util.FireStoreResponse
import com.dldmswo1209.hallymtaxi.util.FireStoreTable
import com.dldmswo1209.hallymtaxi.data.UiState
import com.dldmswo1209.hallymtaxi.data.model.SignedIn
import com.dldmswo1209.hallymtaxi.data.model.TokenInfo
import com.dldmswo1209.hallymtaxi.util.FireStoreTable.SIGNEDIN
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
        Log.d("testt", "registration subscribeUser")
        return callbackFlow {
            val listenerRegistration = fireStore.collection(FireStoreTable.USER).document(auth.currentUser!!.uid)
                .addSnapshotListener { snapshot, error->
                    if(error != null){
                        cancel(
                            message = "유저 정보를 가져오는데 에러가 발생했습니다",
                            cause = error
                        )
                        return@addSnapshotListener
                    }
                    val user = snapshot?.toObject<User>()
                    user?.let { trySend(it) }
                }
            awaitClose {
                listenerRegistration.remove()
                Log.d("testt", "remove subscribeUser")
            }
        }
    }

    override fun subscribeMyRoom(): Flow<CarPoolRoom> {
        return callbackFlow {
            auth.currentUser?.let { user ->
                val listenerRegistration = fireStore.collection(FireStoreTable.ROOM)
                    .whereArrayContains("participants", user.uid)
                    .addSnapshotListener { value, error ->
                        if(error != null){
                            cancel(
                                message = "방 정보를 가져오는데 에러가 발생했습니다",
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
                .whereEqualTo("roomId", roomId)
                .addSnapshotListener { value, error ->
                    if(error != null){
                        cancel(
                            message = "참여자 토큰 정보를 가져오는데 에러가 발생했습니다",
                            cause = error
                        )
                        return@addSnapshotListener
                    }
                    value?.let { querySnapshot ->
                        val tokens = mutableListOf<String>()
                        querySnapshot.documents.forEach { document ->
                            document.getString("token")?.let {
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
                documentSnapshot.getString("name")?.let { name ->
                    result.invoke(UiState.Success(name))
                } ?: kotlin.run {
                    result.invoke(UiState.Failure("이름 없음"))
                }
            }
            .addOnFailureListener {
                result.invoke(UiState.Failure("이름 가져 오기 실패"))
            }
    }

    override fun getParticipantsTokens(roomId: String, result: (UiState<List<String>>) -> Unit) {
        fireStore.collection(FireStoreTable.FCMTOKENS)
            .whereEqualTo("roomId", roomId)
            .get()
            .addOnSuccessListener { querySnapshot ->
                querySnapshot?.let {
                    val tokens = mutableListOf<String>()
                    it.documents.forEach { document ->
                        document.getString("token")?.let { token ->
                            tokens.add(token)
                        }
                    }
                    result.invoke(UiState.Success(tokens))
                } ?: kotlin.run {
                    result.invoke(UiState.Failure("빈 쿼리입니다"))
                }
            }
            .addOnFailureListener {
                result.invoke(UiState.Failure("쿼리 실패"))
            }
    }

    override fun getMyRoom(result: (UiState<CarPoolRoom>) -> Unit) {
        if(auth.currentUser == null) {
            result.invoke(UiState.Failure("현재 유저 정보를 불러올 수 없습니다"))
            return
        }

        fireStore.collection(FireStoreTable.ROOM)
            .whereArrayContains("participants", auth.currentUser!!.uid)
            .get()
            .addOnSuccessListener { querySnapshot ->
                querySnapshot?.let {
                    val snapshot = it.documents.firstOrNull()
                    snapshot?.toObject<CarPoolRoom>()?.let { room ->
                        result.invoke(UiState.Success(room))
                    } ?: kotlin.run {
                        result.invoke(UiState.Failure("현재 참여 중인 채팅방이 없습니다"))
                    }
                } ?: kotlin.run {
                    result.invoke(UiState.Failure("빈 쿼리입니다"))
                }
            }
            .addOnFailureListener {
                result.invoke(UiState.Failure("방 정보를 가져오지 못했습니다"))
            }
    }

    override fun createRoom(room: CarPoolRoom, result: (UiState<CarPoolRoom>) -> Unit) {
        val ref = fireStore.collection(FireStoreTable.ROOM).document()

        room.roomId = ref.id
        ref.set(room)
            .addOnSuccessListener {
                fireStore.collection(FireStoreTable.FCMTOKENS).document(auth.currentUser!!.uid)
                    .update(mapOf("roomId" to room.roomId))
                result.invoke(UiState.Success(room))
            }
            .addOnFailureListener {
                result.invoke(UiState.Failure("채팅방 생성 실패"))
            }
    }

    override fun joinRoom(room: CarPoolRoom, result: (UiState<String>) -> Unit) {
        if(auth.currentUser == null) {
            result.invoke(UiState.Failure("현재 유저 정보를 불러올 수 없습니다"))
            return
        }

        if(room.participants.contains(auth.currentUser!!.uid)){
            // 이미 방에 속해 있음
            result.invoke(UiState.Success(FireStoreResponse.JOIN_ROOM_ALREADY_JOINED))
            return
        }else{
            val docRef = fireStore.collection("Room").document(room.roomId)
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
                        val newCount = snapshot.getLong("userCount")?.plus(1) ?: return@runTransaction
                        val closed = snapshot.getBoolean("closed") ?: return@runTransaction

                        if(newCount <= room.userMaxCount && !closed){
                            transaction.update(docRef, "userCount", FieldValue.increment(1))
                            transaction.update(docRef, "participants", FieldValue.arrayUnion(auth.currentUser!!.uid))
                        }else{
                            throw FirebaseFirestoreException("userCount limit over or closed room", FirebaseFirestoreException.Code.ABORTED)
                        }
                    }
                        .addOnSuccessListener {
                            // 입장 성공
                            fireStore.collection(FireStoreTable.FCMTOKENS).document(auth.currentUser!!.uid)
                                .update(mapOf("roomId" to room.roomId))

                            result.invoke(UiState.Success(FireStoreResponse.JOIN_ROOM_SUCCESS))
                        }
                        .addOnFailureListener {
                            // 입장 실패
                            result.invoke(
                                UiState.Failure(FireStoreResponse.JOIN_ROOM_COUNT_OVER)
                            )
                        }
                }
            }
        }
    }

    override fun exitRoom(room: CarPoolRoom, result: (UiState<String>) -> Unit) {
        if(auth.currentUser == null) {
            result.invoke(UiState.Failure("현재 유저 정보를 불러올 수 없습니다"))
            return
        }

        val docRef = fireStore.collection("Room").document(room.roomId)
        fireStore.runTransaction{transaction->
            val snapshot = transaction.get(docRef)
            val userCount = snapshot.getLong("userCount")?.minus(1) ?: return@runTransaction

            if(userCount > 0){
                transaction.update(docRef,"userCount",userCount)
                transaction.update(docRef, "participants", FieldValue.arrayRemove(auth.currentUser!!.uid))
            }else{
                fireStore.collection("Room").document(room.roomId).delete()
            }
            result.invoke(UiState.Success("채팅방 퇴장"))
        }
            .addOnSuccessListener {
                fireStore.collection("FcmTokens").document(auth.currentUser!!.uid)
                    .update(mapOf("roomId" to ""))
            }
    }

    override fun deactivateRoom(roomId: String, result: (UiState<String>) -> Unit) {
        fireStore.collection("Room").document(roomId)
            .update("closed", true)
            .addOnSuccessListener {
                result.invoke(
                    UiState.Success("채팅방이 마감 되었습니다")
                )
            }
            .addOnFailureListener {
                result.invoke(
                    UiState.Failure("채팅방을 마감할 수 없습니다")
                )
            }
    }

    override fun updateFcmToken(result: (UiState<String>)->Unit) {
        FirebaseMessaging.getInstance().token.addOnCompleteListener { task ->
            if (!task.isSuccessful) {
                result.invoke(
                    UiState.Failure(task.result)
                )
                return@addOnCompleteListener
            }
            if(auth.currentUser == null){
                result.invoke(
                    UiState.Failure("로그인 필요")
                )
                return@addOnCompleteListener
            }

            fireStore.collection("FcmTokens").document(auth.currentUser!!.uid)
                .update(mapOf("token" to task.result))
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
                    UiState.Failure("기기의 토큰 값을 가져오지 못했습니다")
                )
            }
    }

    override fun monitoringLoggedIn(): Flow<SignedIn> {
        Log.d("testt", "registration monitoringLoggedIn")
        return callbackFlow {
            val listenerRegistration = fireStore.collection(SIGNEDIN).document(auth.currentUser!!.uid)
                .addSnapshotListener { snapshot, error->
                    if(error != null){
                        cancel(
                            message = "모니터링 에러가 발생했습니다",
                            cause = error
                        )
                        return@addSnapshotListener
                    }
                    val signedIn = snapshot?.toObject<SignedIn>()
                    signedIn?.let { trySend(it) }
                }

            awaitClose {
                listenerRegistration.remove()
                Log.d("testt", "remove monitoringLoggedIn")
            }
        }
    }
}