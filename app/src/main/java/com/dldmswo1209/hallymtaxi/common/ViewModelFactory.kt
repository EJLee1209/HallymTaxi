package com.dldmswo1209.hallymtaxi.common

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.dldmswo1209.hallymtaxi.data.repository.MainRepository
import com.dldmswo1209.hallymtaxi.data.repository.RoomRepository
import com.dldmswo1209.hallymtaxi.vm.MainViewModel

class ViewModelFactory(val application: Application):ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return when{

            modelClass.isAssignableFrom(MainViewModel::class.java) ->{
                val mainRepository = MainRepository(application.applicationContext)
                val roomRepository = RoomRepository(application.applicationContext)
                MainViewModel(mainRepository, roomRepository, application) as T
            }

            else->{
                throw IllegalArgumentException("Failed to create ViewModel : ${modelClass.name}")
            }
        }
    }
}