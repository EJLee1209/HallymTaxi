package com.dldmswo1209.hallymtaxi.data.repository

import com.dldmswo1209.hallymtaxi.data.model.CarPoolRoom
import com.dldmswo1209.hallymtaxi.data.model.User
import com.dldmswo1209.hallymtaxi.util.FireStoreResponse
import com.dldmswo1209.hallymtaxi.util.FireStoreTable
import com.dldmswo1209.hallymtaxi.data.UiState
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreException
import com.google.firebase.messaging.FirebaseMessaging

class FireStoreRepositoryImpl(
    private val fireStore: FirebaseFirestore,
    private val auth: FirebaseAuth
): FireStoreRepository {
    override fun createRoom(room: CarPoolRoom, user: User, result: (UiState<CarPoolRoom>) -> Unit) {
        val ref = fireStore.collection(FireStoreTable.ROOM).document()

        room.roomId = ref.id
        ref.set(room)
            .addOnSuccessListener {
                result.invoke(UiState.Success(room))
            }
            .addOnFailureListener {
                result.invoke(UiState.Failure("채팅방 생성 실패"))
            }
    }

    override fun joinRoom(room: CarPoolRoom, user: User, result: (UiState<String>) -> Unit) {
        if(room.participants.contains(user)){
            // 이미 방에 속해 있음
            result.invoke(UiState.Success(FireStoreResponse.JOIN_ROOM_ALREADY_JOINED))
            return
        }else{
            if(room.userMaxCount > room.userCount){
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
                                transaction.update(docRef, "participants", FieldValue.arrayUnion(user))
                            }else{
                                throw FirebaseFirestoreException("userCount limit over or closed room", FirebaseFirestoreException.Code.ABORTED)
                            }
                        }
                            .addOnSuccessListener {
                                // 입장 성공
                                room.participants.add(user)
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

            }else{
                result.invoke(
                    UiState.Failure("인원 초과 입니다")
                )
            }
        }
    }

    override fun exitRoom(user: User, room: CarPoolRoom, result: (UiState<String>) -> Unit) {
        val docRef = fireStore.collection("Room").document(room.roomId)
        fireStore.runTransaction{transaction->
            val snapshot = transaction.get(docRef)
            val userCount = snapshot.getLong("userCount")?.minus(1) ?: return@runTransaction

            if(userCount > 0){
                transaction.update(docRef,"userCount",userCount)
                transaction.update(docRef, "participants", FieldValue.arrayRemove(user))
            }else{
                fireStore.collection("Room").document(room.roomId).delete()
            }
            result.invoke(UiState.Success("채팅방 퇴장"))
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
            fireStore.collection(FireStoreTable.USER).document(auth.currentUser!!.uid)
                .update(mapOf("fcmToken" to task.result))
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
}