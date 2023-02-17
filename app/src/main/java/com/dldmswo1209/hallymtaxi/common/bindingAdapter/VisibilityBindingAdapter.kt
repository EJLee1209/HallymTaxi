package com.dldmswo1209.hallymtaxi.common

import android.view.View
import android.widget.ProgressBar
import android.widget.TextView
import androidx.databinding.BindingAdapter
import com.dldmswo1209.hallymtaxi.data.model.SEND_STATE_FAIL
import com.dldmswo1209.hallymtaxi.data.model.SEND_STATE_SUCCESS

@BindingAdapter("isNewMessage")
fun isNewMessage(view: View, isNewMessage: Boolean){
    if(isNewMessage) view.visibility = View.VISIBLE else view.visibility = View.GONE
}

@BindingAdapter("progressVisibility")
fun progressVisibility(view: ProgressBar, sendSuccess: String){
    view.visibility = if(sendSuccess == SEND_STATE_SUCCESS || sendSuccess == SEND_STATE_FAIL) View.GONE else View.VISIBLE
}

@BindingAdapter("dateTimeVisibility")
fun dateTimeVisibility(view: TextView, sendSuccess : String){
    view.visibility = if(sendSuccess == SEND_STATE_SUCCESS) View.VISIBLE else View.GONE
}

@BindingAdapter("cancelButtonVisibility")
fun cancelButtonVisibility(view: View, sendSuccess: String){
    view.visibility = if(sendSuccess == SEND_STATE_FAIL) View.VISIBLE else View.GONE
}