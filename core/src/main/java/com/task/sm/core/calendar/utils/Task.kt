package com.task.sm.core.calendar.utils

import android.graphics.drawable.Drawable
import java.time.LocalDate

data class Task (
    val startDate: LocalDate,
    val endDate: LocalDate,
    val taskName: String,
    var taskColor: String,
    var taskImage: Drawable? = null,
    var taskColorAlpha: Int = 0,
    var taskTextColor: Int = 0,
    var iconColor: Int = 0,
    var row: Int = -1,
    var isSpaceAvailable: Boolean = false
)