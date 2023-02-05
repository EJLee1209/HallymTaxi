package com.dldmswo1209.hallymtaxi.common

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.dldmswo1209.hallymtaxi.repository.MainRepository
import com.dldmswo1209.hallymtaxi.repository.RoomRepository
import com.dldmswo1209.hallymtaxi.repository.ServerRepository
import com.dldmswo1209.hallymtaxi.vm.MainViewModel
import com.dldmswo1209.hallymtaxi.vm.WelcomeViewModel

class ViewModelFactory(val application: Application):ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return when{
            modelClass.isAssignableFrom(WelcomeViewModel::class.java) -> {
                val welcomeRepository = ServerRepository()
                WelcomeViewModel(welcomeRepository, application) as T
            }
            modelClass.isAssignableFrom(MainViewModel::class.java) ->{
                val mainRepository = MainRepository(application.applicationContext)
                val roomRepository = RoomRepository(application.applicationContext)
                val serverRepository = ServerRepository()
                MainViewModel(mainRepository, roomRepository, serverRepository, application) as T
            }

            else->{
                throw IllegalArgumentException("Failed to create ViewModel : ${modelClass.name}")
            }
        }
    }
}