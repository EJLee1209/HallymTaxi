package com.dldmswo1209.hallymtaxi.common

import android.widget.TextView
import androidx.databinding.BindingAdapter
import com.google.firebase.Timestamp

@BindingAdapter("time")
fun time(view: TextView, timestamp: String?){
    timestamp?.let{
        view.text = TimeService.currentTime(it)
    }
}

@BindingAdapter("date")
fun date(view: TextView, timestamp: String?){
    timestamp?.let {
        view.text = TimeService.currentDate(it)
    }
}