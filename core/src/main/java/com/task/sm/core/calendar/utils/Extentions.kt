package com.task.sm.core.calendar.utils

import android.app.Activity
import android.content.Context
import androidx.annotation.ColorRes
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment

fun Context.getColorInt(@ColorRes colorId: Int) = colorId.toColor(this)
private fun @receiver:ColorRes Int.toColor(context: Context) = ContextCompat.getColor(context, this)

fun Activity.getColorInt(@ColorRes colorId: Int) = colorId.toColor(this)