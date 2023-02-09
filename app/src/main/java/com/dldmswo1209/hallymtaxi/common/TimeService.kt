package com.dldmswo1209.hallymtaxi.common


object TimeService {
    fun currentTime(timestamp: String, delimiter: String) : String{
        val splitDateTime = timestamp.split(delimiter)
        val (hour, min) = splitDateTime[1].split(":")

        val realTime : String = if(hour.toInt() >= 12){
            val realHour = if(hour.toInt()-12 == 0) 12 else hour.toInt()-12
            "오후 ${realHour}:$min"
        }else{
            "오전 ${hour.toInt()}:$min"
        }

        return realTime
    }

    fun currentDate(timestamp: String, delimiter: String) : String{
        return timestamp.split(delimiter)[0]
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