package com.dldmswo1209.hallymtaxi.common

import android.widget.TextView
import androidx.databinding.BindingAdapter

@BindingAdapter("time")
fun time(view: TextView, localDateTime: String?){
    localDateTime?.let{
        view.text = TimeService.currentTime(it)
    }
}

@BindingAdapter("date")
fun date(view: TextView, localDateTime: String?){
    localDateTime?.let {
        view.text = TimeService.currentDate(it)
    }
}