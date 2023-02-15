package com.dldmswo1209.hallymtaxi.common.bindingAdapter

import android.view.View
import android.widget.ProgressBar
import androidx.databinding.BindingAdapter

@BindingAdapter("progressVisibility")
fun progressVisibility(view: ProgressBar, sendSuccess: Boolean){
    view.visibility = if(sendSuccess) View.GONE else View.VISIBLE
}
