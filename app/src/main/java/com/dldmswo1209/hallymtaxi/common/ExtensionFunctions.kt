package com.dldmswo1209.hallymtaxi.common

import android.annotation.SuppressLint
import android.app.Application
import android.content.Context
import android.content.res.Resources
import android.os.Bundle
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.NumberPicker
import android.widget.TimePicker
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.annotation.IdRes
import androidx.fragment.app.Fragment
import androidx.lifecycle.AndroidViewModel
import androidx.navigation.NavController
import androidx.navigation.NavGraph
import androidx.navigation.fragment.findNavController
import java.text.DecimalFormat
import java.text.SimpleDateFormat
import java.util.*

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


fun Date.dateToString(format: String = "yyyy-MM-dd HH:mm:ss", local : Locale = Locale.getDefault()): String{
    val formatter = SimpleDateFormat(format, local)
    formatter.timeZone = TimeZone.getTimeZone("Asia/Seoul")
    return formatter.format(this)
}


fun Fragment.toast(msg: String){
    Toast.makeText(requireActivity(), msg, Toast.LENGTH_SHORT).show()
}

val Int.dp: Int
    get() = (this * Resources.getSystem().displayMetrics.density + 0.5f).toInt()

fun NavController.navigateSafe(@IdRes resId: Int, args: Bundle? = null) {
    val destinationId = currentDestination?.getAction(resId)?.destinationId.orEmpty()
    currentDestination?.let { node ->
        val currentNode = when (node) {
            is NavGraph -> node
            else -> node.parent
        }
        if (destinationId != 0) {
            currentNode?.findNode(destinationId)?.let { navigate(resId, args) }
        }
    }
}

fun Int?.orEmpty(default: Int = 0): Int {
    return this ?: default
}

fun Fragment.registerBackPressedCallback(){
    val callback = object : OnBackPressedCallback(true) {
        override fun handleOnBackPressed() {
            findNavController().navigateUp()
        }
    }
    requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner, callback)
}

fun Fragment.registerBackPressedFinishActivityCallback(){
    val callback = object : OnBackPressedCallback(true) {
        override fun handleOnBackPressed() {
            requireActivity().finish()
        }
    }
    requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner, callback)
}

