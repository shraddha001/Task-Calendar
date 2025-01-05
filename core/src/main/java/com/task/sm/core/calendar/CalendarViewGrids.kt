package com.task.sm.core.calendar

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.PorterDuff
import android.graphics.PorterDuffColorFilter
import android.graphics.Rect
import android.graphics.RectF
import android.os.Build
import android.text.TextPaint
import android.text.TextUtils
import android.util.AttributeSet
import android.widget.GridLayout
import android.widget.TextView
import com.task.sm.core.R
import com.task.sm.core.calendar.utils.Task
import com.task.sm.core.calendar.utils.dip
import com.task.sm.core.calendar.utils.getColorInt
import java.time.LocalDate
import java.time.YearMonth
import java.time.temporal.ChronoUnit

typealias OnDateClick = (LocalDate) -> Unit
typealias OnHeightChange = (Int, Int) -> Unit

@Suppress("DEPRECATION")
class CalendarViewGrids : GridLayout {
    private var tasks: List<Task> = arrayListOf()
    private var context: Context
    var selectedDate: LocalDate? = null
    var onDateClick: OnDateClick? = null
    private var maxColumn = mutableMapOf<Int, Float>()
    private var maxEventsPerDay = 0
    private val hiddenTasksMap = mutableMapOf<Int, List<Task>>() // Map to store hidden tasks by row
    var isHiddenTasksVisible = false // Flag to track whether hidden tasks are visible
    private var rowHasHiddenTasks = false
    var isFirstTime = false
    private var rowEventCounts = mutableMapOf<Int, Int>()
    private val rowLabelVisibility = mutableMapOf<Int, Boolean>()
    private var listOfRow: MutableMap<Int, Int> = mutableMapOf()
    private var taskForRow = mutableMapOf<Int, List<Task>>()
    private var visibleTaskMap = mutableMapOf<Int, List<Task>>()
    private var taskInRow = mutableMapOf<Int, Int>()
    var onHeightChange: OnHeightChange? = null

    constructor(context: Context) : super(context) {
        this.context = context
        init()
    }

    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs) {
        this.context = context
        init()
    }

    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    ) {
        this.context = context
        init()
    }

    private fun init() {
        // Set the number of columns in the grid layout
        columnCount = 7
        isNestedScrollingEnabled = false
    }

    fun setEvents(events: List<Task>) {
        tasks = events
        invalidate()
    }

    private fun maxEvent(canvas: Canvas) {
        val dayHeight = (canvas.height - dip(24)) / 7.toFloat()
        val availableHeightForEvents = dayHeight.toInt() - dip(24)
        val labelHeight = if (!isHiddenTasksVisible) dip(18) else 0

        maxEventsPerDay = (availableHeightForEvents - labelHeight) / dip(18)

        val tasksInRow = mutableMapOf<Int, MutableList<Task>>()
        // Iterate through tasks and group them by row
        for (task in tasks) {
            val startDateView = findCalendarDatePosition(task.startDate)
            val endDateView = findCalendarDatePosition(task.endDate)
            if (startDateView != null && endDateView != null) {
                val startRow = calculateRowIndex(startDateView)
                val endRow = calculateRowIndex(endDateView)
                for (row in 0 until 7) {
                    when {
                        startRow <= endRow && row == startRow -> {
                            task.row = row
                            tasksInRow.getOrPut(row) { mutableListOf() }.add(task.copy(row = row))
                        }

                        row != startRow && row == endRow -> {
                            tasksInRow.getOrPut(endRow) { mutableListOf() }
                                .add(task.copy(row = endRow))
                        }
                    }
                }
            }
        }
        // Iterate through rows to check if any row has tasks exceeding the limit
        for ((row, tasksForRow) in tasksInRow) {
            this.taskForRow.put(row, tasksForRow)
            this.taskInRow.put(row, tasksForRow.size)
            listOfRow[row] = row
            if (tasksForRow.size > maxEventsPerDay) {
                rowLabelVisibility.getOrPut(row) { true }
                val extraCount = tasksForRow.size - maxEventsPerDay
                val hiddenTasks = tasksForRow.subList(extraCount, tasksForRow.size)
                hiddenTasksMap[row] = hiddenTasks
                if (visibleTaskMap.containsValue(hiddenTasks)) visibleTaskMap.remove(row)
                else visibleTaskMap[row] = tasksForRow
            } else rowLabelVisibility.getOrPut(row) { false }
        }
    }

    fun toggleLabelVisibilityForRow(row: Int) {
        rowLabelVisibility[row] = !rowLabelVisibility.getOrDefault(row, false)
        invalidate()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        if (tasks.isNotEmpty()) {
            maxEvent(canvas)
            maxColumn = mutableMapOf()
            rowEventCounts = mutableMapOf()
            for (element in tasks) {
                drawEvent(element, canvas)
            }
        }
    }

    private fun drawEvent(task: Task, canvas: Canvas) {
        val startDateView = findCalendarDatePosition(task.startDate)
        val endDateView = findCalendarDatePosition(task.endDate)
        if (startDateView != null && endDateView != null) {
            // Calculate the event's position and dimensions within the calendar grid
            val startRow = calculateRowIndex(startDateView)
            val startColumn = calculateColumnIndex(startDateView)
            val endRow = calculateRowIndex(endDateView)
            val endColumn = calculateColumnIndex(endDateView)
            val isTaskIsStartInRow = !maxColumn.containsKey(startRow)
            val isTaskEndInRow = !maxColumn.containsKey(endRow)

            rowHasHiddenTasks = hiddenTasksMap.containsKey(task.row)

            // Check if this task should be hidden
            val isHiddenTask =
                rowHasHiddenTasks && !(hiddenTasksMap[task.row] ?: listOf()).contains(task)
            // Calculate the width and height of the event
            val dateDifference = ChronoUnit.DAYS.between(task.startDate, task.endDate)
            val width = (dateDifference + 1) * startDateView.width
            val height = dip(18)
            // Calculate the rounded rectangle for the background
            val x = startColumn * startDateView.width
            val y = startRow * startDateView.height

            val maxEventsInRow = 3 // Maximum number of events to display in a row

            if (task.row in rowEventCounts && (rowEventCounts[task.row] ?: 0) > maxEventsInRow &&
                isFirstTime
            ) {
                // Skip drawing this event as it exceeds the limit
                return
            }

            if (isHiddenTasksVisible || !isHiddenTask) {
                val roundedRect =
                    RectF(
                        x.toFloat() + dip(4),
                        maxColumn[startRow] ?: (y.toFloat()),
                        (x + width).toFloat(),
                        maxColumn[startRow]?.plus(height) ?: (y + height).toFloat()
                    )
                val cornerRadius = dip(12).toFloat() // Adjust the corner radius as needed

                val text = task.taskName

                // Text view to set text appearance(typeFace)
                val textView = TextView(context)
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    textView.setTextAppearance(
                        com.google.android.material.R.style.TextAppearance_Material3_TitleSmall
                    )
                } else {
                    textView.setTextAppearance(
                        context,
                        com.google.android.material.R.style.TextAppearance_Material3_TitleSmall
                    )
                }
                val scaledTextSize: Float = 12 * resources.displayMetrics.scaledDensity

                // Calculate the position and dimensions for the text
                val textPaint = TextPaint()
                textPaint.isLinearText = true
                textPaint.textSize = scaledTextSize
                textPaint.typeface = textView.typeface
                if (task.taskTextColor != 0) textPaint.color = task.taskTextColor
                else textPaint.color = context.getColorInt(R.color.color_white)
                if (task.startDate == task.endDate) textPaint.color =
                    Color.parseColor(task.taskColor)
                else textPaint.color = context.getColorInt(R.color.color_white)
                val textBounds = Rect()
                textPaint.getTextBounds(text, 0, text.length, textBounds)

                // Calculate the text position to start where the background starts
                val textX = roundedRect.left + dip(24) // space added for icon space

                // Calculate the Y-position to prevent overlapping
                val columnY = maxColumn[startRow] ?: 0f // Get the current max Y or use 0 if not set
                var textY = maxOf(columnY + dip(12), roundedRect.centerY() + dip(4))

                // Shift 1st task of every row to prevent date overlapping
                val shiftAmount =
                    if (isTaskIsStartInRow) dip(32) else dip(1)
                textY += shiftAmount
                roundedRect.offset(0f, shiftAmount.toFloat())
                // Draw the background
                val paint = Paint()
                paint.color = Color.parseColor(task.taskColor)
                if (task.taskColorAlpha != 0) paint.alpha = task.taskColorAlpha
                if (task.startDate == task.endDate) paint.alpha = 30 // Alpha set to 10%
                canvas.drawRoundRect(roundedRect, cornerRadius, cornerRadius, paint)

                // Draw rounded background when task ends without text
                val xEnd = (endColumn + 1) * endDateView.width
                val yEnd = endRow * endDateView.height

                val roundedRectEnd =
                    RectF(
                        RectF(
                            xEnd.toFloat(),
                            maxColumn[endRow] ?: yEnd.toFloat(),
                            (xEnd - width).toFloat(),
                            maxColumn[endRow]?.plus(height) ?: (yEnd + height).toFloat()
                        )
                    )
                val paintEnd = Paint()
                paintEnd.alpha = task.taskColorAlpha
                paintEnd.color = Color.parseColor(task.taskColor)
                if (startRow != endRow) {
                    val shiftEndTask = if (isTaskEndInRow) dip(32) else dip(0)
                    roundedRectEnd.offset(0f, shiftEndTask.toFloat())
                    val columnEndY = maxColumn[endRow] ?: 0f
                    val textEndY = maxOf(columnEndY, roundedRectEnd.centerY() + height)
                    canvas.drawRoundRect(roundedRectEnd, cornerRadius, cornerRadius, paintEnd)
                    maxColumn[endRow] = textEndY - textBounds.height() / 2
                }

                // Calculate the position and dimensions for the icon
                val iconDrawable = task.taskImage
                if (iconDrawable != null) {
                    iconDrawable.colorFilter = PorterDuffColorFilter(
                        if (task.iconColor != 0) task.iconColor
                        else context.getColorInt(R.color.color_black),
                        PorterDuff.Mode.SRC_ATOP
                    )
                    val iconWidth = dip(12)
                    val iconHeight = dip(12)
                    val iconLeft = roundedRect.left + dip(4)
                    val iconTop = (roundedRect.centerY() + dip(6)) - iconHeight
                    iconDrawable.setBounds(
                        iconLeft.toInt(),
                        iconTop.toInt(),
                        (iconLeft + iconWidth).toInt(),
                        (iconTop + iconHeight).toInt()
                    )
                    // Calculate the position and dimensions for the icon circle
                    val circlePaint = Paint()
                    circlePaint.color = context.getColorInt(R.color.color_white)
                    canvas.drawCircle(
                        roundedRect.left + dip(10),
                        roundedRect.centerY(),
                        dip(8).toFloat(),
                        circlePaint
                    )
                    iconDrawable.draw(canvas)
                }
                // Draw the text ensuring it ends when the background ends
                val availableWidth = if (startRow == endRow) roundedRect.right - textX
                else (endDateView.width * 7) - textX
                val ellipsizedText =
                    TextUtils.ellipsize(text, textPaint, availableWidth, TextUtils.TruncateAt.END)
                canvas.drawText(ellipsizedText.toString(), textX, textY, textPaint)
                maxColumn[startRow] = textY + textBounds.height() / 2
                if (rowHasHiddenTasks && !isHiddenTasksVisible) {
                    // Draw the "+count more" text for hidden tasks
                    val hiddenTasksCount = (taskForRow[task.row]?.size ?: 0) - maxEventsPerDay
                    if (hiddenTasksCount > 0) {
                        val hiddenText = "+$hiddenTasksCount more"
                        val hiddenTextWidth = textPaint.measureText(hiddenText)
                        textPaint.color = context.getColorInt(R.color.color_primary)
                        val hiddenTextY = textY + textBounds.height() + dip(8)
                        canvas.drawText(hiddenText, textX, hiddenTextY, textPaint)
                    }
                }
                // Check if the row needs to display the "+more" label
                rowEventCounts[task.row] = rowEventCounts.getOrDefault(task.row, 0) + 1
            }
        }
    }

    private fun findCalendarDatePosition(date: LocalDate): CalendarDateCell? {
        // Iterate over the child views to find the corresponding calendar date cell
        for (i in 0 until childCount) {
            val child = getChildAt(i)
            if (child is CalendarDateCell) {
                if (child.date() == date) {
                    return child
                }
            }
        }
        return null
    }

    private fun calculateRowIndex(cell: CalendarDateCell): Int {
        // Calculate the row index based on the position of the cell within the grid
        val cellIndex = indexOfChild(cell)
        return cellIndex / 7
    }

    private fun calculateColumnIndex(cell: CalendarDateCell): Int {
        // Calculate the column index based on the position of the cell within the grid
        val cellIndex = indexOfChild(cell)
        return cellIndex % 7
    }

    fun setDates(dates: List<LocalDate>, currentMonth: YearMonth) {
        maxColumn = mutableMapOf()
        rowEventCounts = mutableMapOf()
        removeAllViews()
        val firstDate = dates[0]
        val startDayOfWeek = firstDate.dayOfWeek.value
        val numberOfInflatedDays = currentMonth.atDay(1).dayOfWeek.value
        //  Calculate Number of rows = (Total days in the month + Number of inflated days) / days
        val totalDaysInMonth = currentMonth.atEndOfMonth().dayOfMonth
        val days = if (numberOfInflatedDays != startDayOfWeek) 6 else 7
        val rowCount = (totalDaysInMonth + numberOfInflatedDays) / days
        val columnCount = 7
        val height = dip(120)
        for (row in 0..rowCount) {
            for (column in 0 until columnCount) {
                val dateIndex = row * columnCount + column - startDayOfWeek
                if (dateIndex >= 0 && dateIndex < dates.size) {
                    val date = dates[dateIndex]
                    val calendarDateCell = CalendarDateCell(context)
                    calendarDateCell.setDate(date)
                    calendarDateCell.setOnClickListener {
                        val task = visibleTaskMap[row - 1]?.find { it.startDate.isEqual(date) }
                        if (!isHiddenTasksVisible && task?.row?.equals(row - 1) == true &&
                            rowLabelVisibility[row - 1] == true
                        ) {
                            // Toggle the visibility of hidden tasks
                            isHiddenTasksVisible = true
                            isFirstTime = false
                            toggleLabelVisibilityForRow(row - 1)
                            invalidate()
                        }
                        selectedDate = date
                        onDateClick?.invoke(date)
                    }
                    when (date) {
                        selectedDate -> {
                            // Selected date, set background
                            calendarDateCell.dateTextView.setBackgroundResource(
                                R.drawable.bg_selected_date
                            )
                            calendarDateCell.dateTextView.setTextColor(
                                context.getColorInt(R.color.color_white)
                            )
                        }

                        else -> {
                            // Other dates
                            calendarDateCell.dateTextView.setBackgroundResource(0)
                            if (date >= YearMonth.of(currentMonth.year, currentMonth.month)
                                    .atDay(1) &&
                                date <= YearMonth.of(currentMonth.year, currentMonth.month)
                                    .atEndOfMonth()
                            ) {
                                if (date.isEqual(LocalDate.now()))
                                    calendarDateCell.dateTextView.setBackgroundResource(
                                        R.drawable.bg_today
                                    )
                                else calendarDateCell.dateTextView.setBackgroundResource(0)
                                calendarDateCell.dateTextView.setTextColor(
                                    context.getColorInt(R.color.color_black)
                                )
                            } else {
                                calendarDateCell.dateTextView.setTextColor(
                                    context.getColorInt(R.color.color_disable)
                                )
                            }
                        }
                    }
                    // Set the layout parameters for the cell
                    val layoutParams = LayoutParams()
                    // Calculate the total task height for this row
                    val totalHeight = taskInRow.entries.sumOf { dip(18) }.plus(height)
                    val rowHeight = if (isHiddenTasksVisible)
                        maxOf(height, totalHeight) else height
                    layoutParams.height = rowHeight // Use the calculated maxRowHeight
                    layoutParams.rowSpec = spec(row)
                    layoutParams.columnSpec = spec(column)
                    calendarDateCell.layoutParams = layoutParams
                    setBackgroundColor(context.getColorInt(R.color.transparent))
                    addView(calendarDateCell)
                    if (date.monthValue == currentMonth.monthValue &&
                        date.year == currentMonth.year
                    ) {
                        onHeightChange?.invoke(layoutParams.height, rowCount)
                    }
                }
            }
        }
    }
}