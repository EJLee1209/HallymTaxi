package com.dldmswo1209.hallymtaxi.common

import android.widget.TextView
import androidx.databinding.BindingAdapter

@BindingAdapter("distance")
fun distanceAdapter(view: TextView, distance: Int){
    view.text = if(distance < 1000) "${distance}m" else "%.1fkm".format((distance/1000f))
}