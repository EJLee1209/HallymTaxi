package com.dldmswo1209.hallymtaxi.ui.dialog

import android.app.Dialog
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import com.dldmswo1209.hallymtaxi.R
import kotlinx.coroutines.*

class LoadingDialog(context: Context): Dialog(context) {

    init{
        setCanceledOnTouchOutside(false)
        setContentView(R.layout.dialog_loading)
        window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT)) // 배경 둥글게 하려면 이거 해야함
    }
}