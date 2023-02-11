package com.dldmswo1209.hallymtaxi.pagingSource

import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.dldmswo1209.hallymtaxi.model.CarPoolRoom
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.QuerySnapshot
import kotlinx.coroutines.tasks.await

class FirestorePagingSource(
    private val query: Query
): PagingSource<QuerySnapshot, CarPoolRoom>() {
    override fun getRefreshKey(state: PagingState<QuerySnapshot, CarPoolRoom>): QuerySnapshot? {
        return null
    }

    override suspend fun load(params: LoadParams<QuerySnapshot>): LoadResult<QuerySnapshot, CarPoolRoom> {
        return try{
            val currentPage = params.key ?: query.get().await()
            val lastVisibleCarPoolRoom = currentPage.documents[currentPage.size() - 1]
            val nextPage = query.startAfter(lastVisibleCarPoolRoom).get().await()
            LoadResult.Page(
                data = currentPage.toObjects(CarPoolRoom::class.java),
                prevKey = null,
                nextKey = nextPage
            )
        }catch (e: Exception){
            LoadResult.Error(e)
        }
    }
}

const val PAGE_SIZE = 3