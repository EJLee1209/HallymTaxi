package com.dldmswo1209.hallymtaxi.common

import android.view.View
import androidx.databinding.BindingAdapter

@BindingAdapter("isNewMessage")
fun isNewMessage(view: View, isNewMessage: Boolean){
    if(isNewMessage) view.visibility = View.VISIBLE else view.visibility = View.GONE
}