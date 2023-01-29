package com.dldmswo1209.hallymtaxi.common

import android.content.Context
import android.widget.FrameLayout
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog

object BottomSheetBehaviorSetting {

    fun bottomSheetBehaviorSetting(context: Context, theme: Int) : BottomSheetDialog{
        val bottomSheetDialog = BottomSheetDialog(context, theme)
        bottomSheetDialog.setOnShowListener { dialog ->
            val bottomSheet =
                (dialog as BottomSheetDialog).findViewById<FrameLayout>(com.google.android.material.R.id.design_bottom_sheet) ?: return@setOnShowListener
            BottomSheetBehavior.from(bottomSheet).apply {
                state = BottomSheetBehavior.STATE_SETTLING // 다이얼로그를 컨텐츠 크기에 맞춰서 보여줌
                isDraggable = false // 드래그 금지
            }

        }
        return bottomSheetDialog
    }
}