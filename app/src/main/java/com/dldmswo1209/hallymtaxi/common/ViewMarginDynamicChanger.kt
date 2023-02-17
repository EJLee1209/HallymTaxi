package com.dldmswo1209.hallymtaxi.common

import android.content.Context
import android.view.View
import androidx.constraintlayout.widget.ConstraintLayout
import com.dldmswo1209.hallymtaxi.common.keyboard.KeyboardHeightProvider
import kotlinx.coroutines.*

class ViewMarginDynamicChanger(context: Context) {
    private var keyboardHeight = 0

    init {
        KeyboardHeightProvider(context).init()
            .setHeightListener(object : KeyboardHeightProvider.HeightListener {
                override fun onHeightChanged(height: Int) {
                    keyboardHeight = height
                }
            })
    }

    @OptIn(DelicateCoroutinesApi::class)
    fun changeConstraintMarginTopBottom(
        targetView: View,
        marginBottomLarge: Int,
        marginBottomSmall: Int,
        marginTopLarge: Int,
        marginTopSmall: Int,
        keyboardIsVisible: Boolean
    ) {
        val layoutParams = targetView.layoutParams as ConstraintLayout.LayoutParams

        GlobalScope.launch(Dispatchers.Main) {
            withContext(Dispatchers.Default) {
                delay(200)
            }
            if (keyboardIsVisible) {
                layoutParams.bottomMargin = keyboardHeight + marginBottomSmall
                layoutParams.topMargin = marginTopSmall
            } else {
                layoutParams.bottomMargin = keyboardHeight + marginBottomLarge
                layoutParams.topMargin = marginTopLarge
            }
            targetView.layoutParams = layoutParams
        }
    }
}