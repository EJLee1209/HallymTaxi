package com.dldmswo1209.hallymtaxi.common

import android.annotation.SuppressLint
import android.app.Application
import android.content.Context
import android.content.res.Resources
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.NumberPicker
import android.widget.TimePicker
import androidx.lifecycle.AndroidViewModel
import java.text.DecimalFormat

fun EditText.setFocusAndShowKeyboard(context: Context) {
    this.requestFocus()
    setSelection(this.text.length)
    this.postDelayed({
        val inputMethodManager =
            context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        inputMethodManager.showSoftInput(this, InputMethodManager.SHOW_FORCED)
    }, 100)
}

fun EditText.clearFocusAndHideKeyboard(context: Context) {
    this.clearFocus()
    this.postDelayed({
        val inputMethodManager =
            context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        inputMethodManager.hideSoftInputFromWindow(this.windowToken, 0)
    }, 30)
}

val AndroidViewModel.context: Context
    get() = getApplication<Application>().applicationContext


private val INTERVAL = 5
private val FORMATTER: DecimalFormat = DecimalFormat("00")

@SuppressLint("DiscouragedApi")
fun TimePicker.setMinutePicker() {
    val picker = this

    val numValues = 60 / INTERVAL
    val displayedValues = arrayOfNulls<String>(numValues)
    for (i in 0 until numValues) {
        displayedValues[i] = FORMATTER.format(i * INTERVAL)
    }
    val minute: View =
        picker.findViewById(Resources.getSystem().getIdentifier("minute", "id", "android"))
    if (minute is NumberPicker) {
        minute.minValue = 0
        minute.maxValue = numValues - 1
        minute.displayedValues = displayedValues
    }
}

@SuppressLint("DiscouragedApi")
fun TimePicker.getMinute(): Int {
    val picker = this
    val minutePicker: NumberPicker =
        picker.findViewById(Resources.getSystem().getIdentifier("minute", "id", "android"))

    return minutePicker.value * INTERVAL
}
