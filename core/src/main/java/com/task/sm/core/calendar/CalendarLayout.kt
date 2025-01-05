package com.task.sm.core.calendar

import android.content.Context
import android.util.AttributeSet
import android.widget.AdapterView
import android.widget.AutoCompleteTextView
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.LinearLayout
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.viewpager.widget.ViewPager
import com.google.android.material.textview.MaterialTextView
import com.task.sm.core.R
import com.task.sm.core.calendar.utils.Task
import java.time.LocalDate
import java.time.YearMonth

typealias OnMonthScroll = (YearMonth) -> Unit
typealias OnDateSelected = (LocalDate) -> Unit
typealias OnHeightChangeCallBack = (Int, Int) -> Unit
typealias OnHeightExpandedCallBack = (Int, Int) -> Unit

class CalendarLayout : FrameLayout {
    private lateinit var context: Context

    private lateinit var ivArrowLeft: ImageView
    private lateinit var ivArrowRight: ImageView

    private lateinit var tvMonth: MaterialTextView
    private lateinit var dropDown: AutoCompleteTextView
    private var years: MutableList<Int> = mutableListOf()
    private lateinit var adapter: YearSelectionAdapter
    private var listOfTask: ArrayList<Task> = arrayListOf()
    lateinit var calendarPagerAdapter: CalendarPagerAdapter
    lateinit var pager: CustomViewPager
    var currentPosition = -1
    var onMonthScroll: OnMonthScroll? = null
    var minYear: YearMonth? = null
    var maxYear: YearMonth? = null
    var onDateSelected: OnDateSelected? = null
    var onHeightChangeCallBack: OnHeightChangeCallBack? = null
    var onHeightExpanded: OnHeightExpandedCallBack? = null
    lateinit var mainView: LinearLayout
    lateinit var monthView: ConstraintLayout

    constructor(context: Context) : super(context) {
        init()
    }

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {
        this.context = context
        init(attrs)
    }

    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    ) {
        this.context = context
        init(attrs)
    }

    private fun init(attrs: AttributeSet? = null) {
        val view = inflate(context, R.layout.layout_calendar, this)
        ivArrowLeft = view.findViewById(R.id.ivArrowLeft)
        ivArrowRight = view.findViewById(R.id.ivArrowRight)
        tvMonth = view.findViewById(R.id.tvMonth)
        dropDown = view.findViewById(R.id.dropdown)
        pager = view.findViewById(R.id.pager)
        mainView = view.findViewById(R.id.mainView)
        monthView = view.findViewById(R.id.monthView)
        val yearMonth: YearMonth = YearMonth.now()
        calendarPagerAdapter = CalendarPagerAdapter(context)
        calendarPagerAdapter.onDateSelected = {
            onDateSelected?.invoke(it)
        }
        val firstYear = getYearMonth()[0]
        val year = YearMonth.now().year - firstYear.year
        val total = (year * 12) + YearMonth.now().monthValue
        currentPosition = total
        calendarPagerAdapter.listOfYearMonth.clear()
        calendarPagerAdapter.listOfYearMonth.addAll(getYearMonth())
        pager.adapter = calendarPagerAdapter
        calendarPagerAdapter.isFirstTime = true
        pager.setCurrentItem(currentPosition - 1, false)
        tvMonth.text = buildString {
            append(yearMonth.month.name.lowercase())
            append(" ")
            append(yearMonth.year)
        }.capitalizeWords()
        ivArrowLeft.setOnClickListener {
            val currentItem = pager.currentItem - 1
            pager.setCurrentItem(currentItem, false)
        }
        ivArrowRight.setOnClickListener {
            val currentItem = pager.currentItem + 1
            pager.setCurrentItem(currentItem, false)
        }
        adapter = YearSelectionAdapter(
            context, R.layout.item_year_selection,
            years
        )
        pager.setPagingEnabled(true)
        pager.addOnPageChangeListener(viewPagerPageChangeListener)
    }

    private var viewPagerPageChangeListener: ViewPager.OnPageChangeListener =
        object : ViewPager.OnPageChangeListener {
            override fun onPageSelected(position: Int) {
                val yearMonth = calendarPagerAdapter.listOfYearMonth[position]
                tvMonth.text = buildString {
                    append(yearMonth.month.name.lowercase())
                    append(" ")
                    append(yearMonth.year)
                }.capitalizeWords()
                onMonthScroll?.invoke(yearMonth)
                val dates =
                    calendarPagerAdapter.generateCalendarDates(
                        calendarPagerAdapter.listOfYearMonth[position]
                    )
                val firstDate = dates[0]
                val startDayOfWeek = firstDate.dayOfWeek.value
                val numberOfInflatedDays = yearMonth.atDay(1).dayOfWeek.value
                //  Calculate Number of rows = (Total days in the month + Number of inflated days) / days
                val totalDaysInMonth = yearMonth.atEndOfMonth().dayOfMonth
                val days = if (numberOfInflatedDays != startDayOfWeek) 6 else 7
                val rowCount = (totalDaysInMonth + numberOfInflatedDays) / days
                mainView.post {
                    mainView.measuredHeight.let { onHeightChangeCallBack?.invoke(it, rowCount) }
                }
                calendarPagerAdapter.onHeightChangeCallBack = { height, _ ->
                    onHeightChangeCallBack?.invoke(height, rowCount)
                }
                calendarPagerAdapter.onHeightExpandedCallBack = { height, mrowCount ->
                    onHeightExpanded?.invoke(height, mrowCount)
                }
                calendarPagerAdapter.isHiddenTaskVisible = false
                calendarPagerAdapter.isFirstTime = true
            }

            override fun onPageScrolled(arg0: Int, arg1: Float, arg2: Int) {}

            override fun onPageScrollStateChanged(arg0: Int) {}
        }

    fun setYearDropdownVisible(isVisible: Boolean, yearList: MutableList<Int>) {
        dropDown.isVisible = isVisible
        adapter = YearSelectionAdapter(
            context, R.layout.item_year_selection,
            yearList
        )
        dropDown.isVisible = isVisible
        val today = LocalDate.now()
        if (years.isEmpty()) {
            for (year in (today.year - 8) until (today.year + 10))
                years.add(year)
        }
        dropDown.setOnDismissListener {
            setArrow(false)
        }
        adapter.setDropDownViewResource(R.layout.item_year_selection)
        dropDown.setAdapter(adapter)

        tvMonth.setOnClickListener {
            setArrow(true)
            dropDown.showDropDown()
            val index = years.indexOf(adapter.selectedYear)
            if (index > 0)
                dropDown.listSelection = index - 1
        }
        dropDown.setOnClickListener {
            setArrow(true)
            dropDown.showDropDown()
            val index = years.indexOf(adapter.selectedYear)
            if (index > 0)
                dropDown.listSelection = index - 1
        }

        dropDown.onItemClickListener =
            AdapterView.OnItemClickListener { _, _, i, _ ->
                try {
                    adapter.selectedYear = years[i]
                    val firstYear = getYearMonth()[0]
                    val totalYear = adapter.selectedYear - firstYear.year
                    val total = (totalYear * 12) + 1
                    currentPosition = total
                    calendarPagerAdapter.isHiddenTaskVisible = false
                    calendarPagerAdapter.isFirstTime = true
                    val yearMonth = YearMonth.of(adapter.selectedYear, 1)
                    tvMonth.text = buildString {
                        append(yearMonth.month.name.lowercase())
                        append(" ")
                        append(yearMonth.year)
                    }.capitalizeWords()
                    pager.setCurrentItem(currentPosition - 1, false)
                    dropDown.text = null
                } catch (exc: Exception) {
                    exc.printStackTrace()
                }
            }
    }

    private fun setArrow(isClick: Boolean) {
        if (isClick) {
            dropDown.background =
                ContextCompat.getDrawable(context, R.drawable.ic_arrow_up)
        } else {
            dropDown.background =
                ContextCompat.getDrawable(context, R.drawable.ic_arrow_down)
        }
    }

    fun setTaskList(list: List<Task>) {
        calendarPagerAdapter.listOfTask = list
    }

    fun updateCalendar() {
        calendarPagerAdapter.notifyDataSetChanged()
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
        while (!date.isAfter(previousMonthEndDate)) {
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

    private fun setMinAndMaxYear(minYear: YearMonth, maxYear: YearMonth) {
        this.minYear = minYear
        this.maxYear = maxYear
    }

    private fun getYearMonth(): ArrayList<YearMonth> {
        val list: ArrayList<YearMonth> = arrayListOf()
        val year = LocalDate.now().year
        val startYear: YearMonth = minYear ?: YearMonth.of((year - 8), 1)
        val endYear: YearMonth = maxYear ?: YearMonth.of((year + 10), 1)
        val totalYear = ((endYear.year - startYear.year) * 12) - 1
        for (i in 0..totalYear) {
            list.add(YearMonth.from(startYear.plusMonths(i.toLong())))
        }
        return list
    }

    fun String.capitalizeWords(): String =
        split(" ").joinToString(" ") { w -> w.replaceFirstChar { c -> c.uppercaseChar() } }
}