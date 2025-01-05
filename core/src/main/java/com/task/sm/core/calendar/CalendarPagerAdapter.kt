package com.task.sm.core.calendar

import android.content.Context
import android.os.Build
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.viewpager.widget.PagerAdapter
import com.google.android.material.textview.MaterialTextView
import com.task.sm.core.R
import com.task.sm.core.calendar.utils.Task
import com.task.sm.core.calendar.utils.getColorInt
import java.time.LocalDate
import java.time.YearMonth

class CalendarPagerAdapter(
    val context: Context
) : PagerAdapter() {
    //    var yearMonth: YearMonth = YearMonth.now()
    val listOfYearMonth: ArrayList<YearMonth> = arrayListOf()
    var listOfTask: List<Task> = listOf()
    var isHiddenTaskVisible: Boolean = false
    var isFirstTime: Boolean = true
    var onDateSelected: OnDateSelected? = null
    var onHeightChangeCallBack: OnHeightChangeCallBack? = null
    var onHeightExpandedCallBack: OnHeightExpandedCallBack? = null

    override fun getCount(): Int {
        return listOfYearMonth.size
    }

    override fun getItemPosition(`object`: Any): Int {
        return POSITION_NONE
    }

    override fun isViewFromObject(view: View, `object`: Any): Boolean {
        return view === `object`
    }

    override fun instantiateItem(container: ViewGroup, position: Int): Any {
        val layoutInflater = LayoutInflater.from(context)
        val view = layoutInflater.inflate(R.layout.item_calendar_pager, container, false)
        container.addView(view)
        val calendarView = view.findViewById<CalendarViewGrids>(R.id.grids)
        val weekLayout = view.findViewById<LinearLayout>(R.id.weekLayout)
        val mainView = view.findViewById<LinearLayout>(R.id.llView)
        calendarView.setEvents(listOfTask)
        calendarView.setDates(
            generateCalendarDates(listOfYearMonth[position]),
            listOfYearMonth[position]
        )
        mainView.post {
            onHeightChangeCallBack?.invoke(weekLayout.measuredHeight, 7)
        }
        calendarView.onHeightChange = { cellHeight, rowCount ->
            onHeightExpandedCallBack?.invoke(cellHeight, rowCount)
        }
        calendarView.onDateClick = {
            calendarView.selectedDate = it
            calendarView.setDates(
                generateCalendarDates(listOfYearMonth[position]),
                listOfYearMonth[position]
            )
            onDateSelected?.invoke(calendarView.selectedDate ?: it)
        }
        calendarView.isHiddenTasksVisible = isHiddenTaskVisible
        calendarView.isFirstTime = isFirstTime
        updateWeekView(listOfYearMonth[position], weekLayout)
        return view
    }

    private fun updateWeekView(yearMonth: YearMonth, weekLayout: LinearLayout) {
        weekLayout.removeAllViews()
        for (i in 0..6) {
            val itemView: View = LayoutInflater.from(context).inflate(
                R.layout.layout_calendar_week_day,
                weekLayout,
                false
            )
            val textView = itemView.findViewById<MaterialTextView>(R.id.weekDay)
            when (i) {
                0 -> {
                    textView.text = context.getString(R.string.sunday)
                }

                1 -> {
                    textView.text = context.getString(R.string.monday)
                }

                2 -> {
                    textView.text = context.getString(R.string.tuesday)
                }

                3 -> {
                    textView.text = context.getString(R.string.wednesday)
                }

                4 -> {
                    textView.text = context.getString(R.string.thursday)
                }

                5 -> {
                    textView.text = context.getString(R.string.friday)
                }

                6 -> {
                    textView.text = context.getString(R.string.saturday)
                }
            }
            if (LocalDate.now().dayOfWeek.value == i && yearMonth.month == LocalDate.now().month &&
                yearMonth.year == LocalDate.now().year
            ) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    textView.setTextAppearance(
                        com.google.android.material.R.style.Widget_MaterialComponents_MaterialCalendar_DayOfWeekLabel
                    )
                } else textView.setTextAppearance(
                    context,
                    com.google.android.material.R.style.Widget_MaterialComponents_MaterialCalendar_DayOfWeekLabel
                )
                textView.setTextColor(context.getColorInt(R.color.color_primary))
            } else {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    textView.setTextAppearance(
                        com.google.android.material.R.style.MaterialAlertDialog_Material3_Body_Text
                    )
                } else textView.setTextAppearance(
                    context,
                    com.google.android.material.R.style.MaterialAlertDialog_Material3_Body_Text
                )
                textView.setTextColor(context.getColorInt(R.color.color_black))
            }
            weekLayout.addView(itemView)
        }
    }

    fun generateCalendarDates(yearMonth: YearMonth): List<LocalDate> {
        val startDate = yearMonth.atDay(1)
        val endDate = yearMonth.atEndOfMonth()
        // Calculate the dates from the previous month
        val previousMonthEndDate = startDate.minusDays(1)
        val previousMonthStartDate =
            previousMonthEndDate.minusDays((startDate.dayOfWeek.value - 1).toLong())

        // Calculate the dates from the next month
        val nextMonthStartDate = endDate.plusDays(1)
        val nextMonthEndDate = nextMonthStartDate.plusDays((12 - endDate.dayOfWeek.value).toLong())

        // Generate the list of dates
        val calendarDates = mutableListOf<LocalDate>()

        var date = previousMonthStartDate
        while (!date.isAfter(previousMonthEndDate) &&
            startDate.dayOfWeek.value != previousMonthStartDate.dayOfWeek.value
        ) {
            calendarDates.add(date)
            date = date.plusDays(1)
        }

        date = startDate
        while (!date.isAfter(endDate)) {
            calendarDates.add(date)
            date = date.plusDays(1)
        }

        date = nextMonthStartDate
        while (!date.isAfter(nextMonthEndDate)) {
            calendarDates.add(date)
            date = date.plusDays(1)
        }
        return calendarDates
    }

    override fun destroyItem(container: ViewGroup, position: Int, `object`: Any) {
        container.removeView(`object` as LinearLayout)
    }
}
