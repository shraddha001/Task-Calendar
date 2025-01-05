package com.task.sm.core.calendar

import android.content.Context
import android.util.AttributeSet
import android.view.Display
import android.view.View
import android.view.WindowManager
import android.widget.RelativeLayout
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.isVisible
import com.google.android.material.textview.MaterialTextView
import com.task.sm.core.R
import java.time.LocalDate

class CalendarDateCell : ConstraintLayout {
    private var date: LocalDate = LocalDate.now()
    lateinit var dateTextView: MaterialTextView
    lateinit var mainView: RelativeLayout

    constructor(context: Context?) : super(context!!) {
        init()
    }

    constructor(context: Context?, attrs: AttributeSet?) : super(
        context!!, attrs
    ) {
        init()
    }

    constructor(context: Context?, attrs: AttributeSet?, defStyleAttr: Int) : super(
        context!!, attrs, defStyleAttr
    ) {
        init()
    }

    fun date(): LocalDate {
        return this.date
    }

    fun setDate(date: LocalDate) {
        this.date = date
        val dateString = date.dayOfMonth.toString()
        dateTextView.text = dateString
    }

    private fun init() {
        // Customize the appearance of the calendar date cell
        val itemView = inflate(context, R.layout.layout_calendar_day, this)
        dateTextView = itemView.findViewById(R.id.calendarDayText)
        mainView = itemView.findViewById(R.id.rlLayout)
        val bottomDivider = itemView.findViewById<View>(R.id.bottomDivider)
        bottomDivider.isVisible = true
        mainView.layoutParams.width = screenWidth(context) / 7
        mainView.setBackgroundResource(0)
    }

    private fun screenWidth(context: Context): Int {
        val wm = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
        val display: Display = wm.defaultDisplay
        return display.width
    }

    private fun screenHeight(context: Context): Int {
        val wm = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
        val display: Display = wm.defaultDisplay
        return display.height
    }

    fun increaseHeight(height: Int) {
        mainView.minimumHeight = height
    }

    fun setCalenderCellHeight(height: Int) {
        mainView.layoutParams.height = height
    }
}
