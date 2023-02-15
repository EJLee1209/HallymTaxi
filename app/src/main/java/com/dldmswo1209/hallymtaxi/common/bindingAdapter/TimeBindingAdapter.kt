package com.dldmswo1209.hallymtaxi.common

import android.view.View
import android.widget.TextView
import androidx.databinding.BindingAdapter
import java.time.LocalDate

@BindingAdapter("time")
fun time(view: TextView, timestamp: String?){
    timestamp?.let{
        view.text = TimeService.currentTime(it, " ")
    }
}

@BindingAdapter("date")
fun date(view: TextView, timestamp: String?){
    timestamp?.let {
        view.text = TimeService.currentDate(it, " ")
    }
}

@BindingAdapter("departure_time")
fun parsingDepartureTime(view: TextView, timestamp: String?){
    timestamp?.let{
        val day = TimeService.currentDate(it, "T").split("-")[2].toInt()
        val time = TimeService.currentTime(it, "T")
        val localDay = TimeService.currentDate(LocalDate.now().toString(), "T").split("-")[2].toInt()

        if(day == localDay){
            view.text = "오늘 $time"
        }else if(day == localDay+1){
            view.text = "내일 $time"
        }
        else{
            val date = it.split("T")[0]
            view.text = "$date $time"
        }
    }
}

@BindingAdapter("dateTimeVisibility")
fun dateTimeVisibility(view: TextView, sendSuccess : Boolean){
    view.visibility = if(sendSuccess) View.VISIBLE else View.GONE
}