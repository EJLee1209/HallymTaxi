package com.dldmswo1209.hallymtaxi.common

import android.widget.TextView
import androidx.databinding.BindingAdapter
import com.dldmswo1209.hallymtaxi.model.GENDER_OPTION_FEMALE
import com.dldmswo1209.hallymtaxi.model.GENDER_OPTION_MALE
import com.dldmswo1209.hallymtaxi.model.GENDER_OPTION_NONE

@BindingAdapter("genderOption")
fun genderOption(view: TextView, option: String){
    view.text = when(option){
        GENDER_OPTION_MALE -> {
            "여성끼리 탑승하기"
        }
        GENDER_OPTION_FEMALE -> {
            "남성끼리 탑승하기"
        }
        else -> {
            "상관없이 탑승하기"
        }
    }
}