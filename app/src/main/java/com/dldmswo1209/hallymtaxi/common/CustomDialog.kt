package com.dldmswo1209.hallymtaxi.common

import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import com.dldmswo1209.hallymtaxi.databinding.DialogAlreadyExistEmailBinding

class CustomDialog(
    val title: String,
    val content: String,
    val negativeButton: String = "취소",
    val positiveButton: String = "확인",
    val negativeButtonVisible: Boolean = false,
    val negativeCallback: ()->Unit = {},
    val positiveCallback: ()->Unit = {}
) : DialogFragment() {
    private lateinit var binding : DialogAlreadyExistEmailBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = DialogAlreadyExistEmailBinding.inflate(inflater, container, false)
        if(negativeButtonVisible) binding.btnNegative.visibility = View.VISIBLE
        dialog?.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT)) // 배경 둥글게 하려면 이거 해야함
        dialog?.setCanceledOnTouchOutside(false)
        binding.fragment = this
        return binding.root
    }

    fun onPositiveButtonClick(){
        dialog?.dismiss()
        positiveCallback()
    }

    fun onNegativeButtonClick(){
        dialog?.dismiss()
        negativeCallback()
    }


}