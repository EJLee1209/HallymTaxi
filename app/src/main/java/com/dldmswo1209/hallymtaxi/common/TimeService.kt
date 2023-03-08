package com.dldmswo1209.hallymtaxi.common

import java.time.LocalDateTime


object TimeService {
    fun currentTime(timestamp: String, delimiter: String) : String{
        val splitDateTime = timestamp.split(delimiter)
        val (hour, min) = splitDateTime[1].split(":")

        val realTime : String = if(hour.toInt() >= 12){
            val realHour = if(hour.toInt()-12 == 0) 12 else hour.toInt()-12
            "오후 ${realHour.intToStringWithFillZero()}:${min.toInt().intToStringWithFillZero()}"
        }else{
            "오전 ${hour.toInt().intToStringWithFillZero()}:${min.toInt().intToStringWithFillZero()}"
        }

        return realTime
    }

    fun currentDate(timestamp: String, delimiter: String) : String{
        return timestamp.split(delimiter)[0]
    }

    fun isBefore(timestamp: String, delimiter: String) : Boolean{
        val (year, month,day) = currentDate(timestamp, delimiter).split("-")
        val (hour, min) = timestamp.split(delimiter)[1].split(":")
        val localDateTime = LocalDateTime.now()

        return localDateTime.isBefore(LocalDateTime.of(year.toInt(), month.toInt(), day.toInt(), hour.toInt(), min.toInt()))
    }

    fun dateTimeSplitHelper(timestamp: String, target: String) : Int{
        val (date, time) = timestamp.split(" ")
        val (year, month, day) = date.split("-")
        val (hour, min, sec) = time.split(":")

        return when(target){
            "year"->{
                year.toInt()
            }
            "month"->{
                month.toInt()
            }
            "day"->{
                day.toInt()
            }
            "hour"->{
                hour.toInt()
            }
            "min"->{
                min.toInt()
            }
            "sec"->{
                sec.toFloat().toInt()
            }
            else->{
                -1
            }
        }
    }
}