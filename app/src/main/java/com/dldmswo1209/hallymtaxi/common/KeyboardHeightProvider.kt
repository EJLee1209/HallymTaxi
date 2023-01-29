package com.dldmswo1209.hallymtaxi.common

import android.app.Activity
import android.content.Context
import android.graphics.Rect
import android.graphics.drawable.ColorDrawable
import android.view.Gravity
import android.view.View
import android.view.ViewTreeObserver.OnGlobalLayoutListener
import android.view.WindowManager.LayoutParams
import android.widget.PopupWindow


class KeyboardHeightProvider(private val context: Context) : PopupWindow(context),
    OnGlobalLayoutListener {
    private val rootView: View = View(context)
    private var listener: HeightListener? = null
    private var heightMax = 0

    init {
        contentView = rootView
        rootView.viewTreeObserver.addOnGlobalLayoutListener(this)
        setBackgroundDrawable(ColorDrawable(0))
        width = 0
        height = LayoutParams.MATCH_PARENT
        softInputMode = LayoutParams.SOFT_INPUT_ADJUST_RESIZE
        inputMethodMode = INPUT_METHOD_NEEDED
    }

    fun init(): KeyboardHeightProvider {
        if (!isShowing) {
            val view: View = (context as Activity).window.decorView
            view.post(Runnable { showAtLocation(view, Gravity.NO_GRAVITY, 0, 0) })
        }
        return this
    }

    fun setHeightListener(listener: HeightListener?): KeyboardHeightProvider {
        this.listener = listener
        return this
    }

    override fun onGlobalLayout() {
        val rect = Rect()
        rootView.getWindowVisibleDisplayFrame(rect)
        if (rect.bottom > heightMax) {
            heightMax = rect.bottom
        }
        val keyboardHeight: Int = heightMax - rect.bottom
        if (listener != null) {
            listener!!.onHeightChanged(keyboardHeight)
        }
    }

    interface HeightListener {
        fun onHeightChanged(height: Int)
    }
}