package com.dldmswo1209.hallymtaxi.common

import java.time.LocalDateTime

object TimeService {

    fun currentTime(localDateTime: String) : String{
        val splitDateTime = localDateTime.split("T")
        val time = splitDateTime[1].split(":")
        val hour = time[0]
        val min = time[1]
        var realTime : String

        if(hour.toInt() >= 12){
            val realHour = if(hour.toInt()-12 == 0) 12 else hour.toInt()-12
            realTime = "오후 ${realHour}:$min"
        }else{
            realTime = "오전 ${hour.toInt()}:$min"
        }

        return realTime
    }

    fun currentDate(localDateTime: String) : String{
        return localDateTime.split("T")[0]
    }

    fun dateTimeSplitHelper(dateTime: String, target: String) : Int{
        val splitDateTime = dateTime.split("T")
        val date = splitDateTime[0]
        val time = splitDateTime[1]
        val splitDate = date.split("-")
        val splitTime = time.split(":")
        val year = splitDate[0].toInt()
        val month = splitDate[1].toInt()
        val day = splitDate[2].toInt()
        val hour = splitTime[0].toInt()
        val min = splitTime[1].toInt()
        val sec = splitTime[2].toFloat().toInt()

        return when(target){
            "year"->{
                year
            }
            "month"->{
                month
            }
            "day"->{
                day
            }
            "hour"->{
                hour
            }
            "min"->{
                min
            }
            "sec"->{
                sec
            }
            else->{
                -1
            }
        }
    }
}