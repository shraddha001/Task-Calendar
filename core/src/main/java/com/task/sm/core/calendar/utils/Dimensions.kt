package com.task.sm.core.calendar.utils

import android.content.Context
import android.view.View

/**
 * Returns dip(dp) dimension value in pixels
 */
fun View.dip(value: Int): Int = context.dip(value)

fun Context.dip(value: Int): Int = (value * resources.displayMetrics.density).toInt()