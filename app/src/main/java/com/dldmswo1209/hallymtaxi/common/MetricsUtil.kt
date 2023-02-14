package com.dldmswo1209.hallymtaxi.common

import android.content.Context
import android.content.res.Resources
import android.util.DisplayMetrics

object MetricsUtil {

    fun convertDpToPixel(dp: Int, context: Context?): Int {
        return if (context != null) {
            val resources = context.resources
            val metrics = resources.displayMetrics
            dp * (metrics.densityDpi / DisplayMetrics.DENSITY_DEFAULT)
        } else {
            val metrics = Resources.getSystem().displayMetrics
            dp * (metrics.densityDpi / DisplayMetrics.DENSITY_DEFAULT)
        }
    }


    fun convertPixelsToDp(px: Int, context: Context?): Int {
        return if (context != null) {
            val resources = context.resources
            val metrics = resources.displayMetrics
            px / (metrics.densityDpi / DisplayMetrics.DENSITY_DEFAULT)
        } else {
            val metrics = Resources.getSystem().displayMetrics
            px / (metrics.densityDpi / DisplayMetrics.DENSITY_DEFAULT)
        }
    }
}