package com.task.sm.taskcalendar

import android.os.Bundle
import android.util.Log
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.task.sm.core.calendar.CalendarLayout
import com.task.sm.core.calendar.utils.Task
import com.task.sm.core.calendar.utils.getColorInt
import java.time.LocalDate

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        val calendarLayout = findViewById<CalendarLayout>(R.id.calendarView)
        calendarLayout.setTaskList(getTaskList())
        calendarLayout.setYearDropdownVisible(true, getCalendarYear())
        calendarLayout.onMonthScroll = {
            Log.d(javaClass.name, "________________________${it.year} >>> ${it.month}")
        }
        calendarLayout.onDateSelected = {
            Log.d(javaClass.name, "____________________>>$it")
        }
        calendarLayout.onHeightChangeCallBack = { cellHeight, rowCount ->
            Log.d(javaClass.name, "______________________$cellHeight")
        }
        calendarLayout.onHeightExpanded = { cellHeight, _ ->
            Log.d(javaClass.name, "______________________$cellHeight")
        }
    }

    private fun getTaskList(): List<Task> {
        val list: ArrayList<Task> = arrayListOf()
        list.add(
            Task(
                startDate = LocalDate.of(2025, 1, 1),
                endDate = LocalDate.of(2025, 1, 1),
                taskName = "Grocery Items",
                taskColor = "#4E8420",
                taskImage = getDrawable(R.drawable.ic_check),
                iconColor = getColorInt(R.color.success)
            )
        )
        list.add(
            Task(
                startDate = LocalDate.of(2025, 1, 1),
                endDate = LocalDate.of(2025, 1, 2),
                taskName = "My Birthday",
                taskColor = "#4E8420",
                iconColor = getColorInt(R.color.success),
                taskImage = getDrawable(R.drawable.ic_check)
            )
        )
        list.add(
            Task(
                startDate = LocalDate.of(2025, 1, 8),
                endDate = LocalDate.of(2025, 1, 8),
                taskName = "Job Interview",
                taskColor = "#EE5A44",
                taskImage = getDrawable(R.drawable.ic_warning),
                iconColor = getColorInt(R.color.warning)
            )
        )
        list.add(
            Task(
                startDate = LocalDate.of(2025, 1, 11),
                endDate = LocalDate.of(2025, 1, 15),
                taskName = "Exam Schedule",
                taskColor = "#079CA6",
                taskImage = getDrawable(R.drawable.ic_note),
                iconColor = getColorInt(R.color.error)
            )
        )
        list.add(
            Task(
                startDate = LocalDate.of(2025, 1, 12),
                endDate = LocalDate.of(2025, 1, 15),
                taskName = "Holidays Starts",
                taskColor = "#4E8420",
                taskImage = getDrawable(R.drawable.ic_check)
            )
        )
        list.add(
            Task(
                startDate = LocalDate.of(2025, 1, 20),
                endDate = LocalDate.of(2025, 1, 25),
                taskName = "Test's Schedule",
                taskColor = "#4E8420",
                taskImage = getDrawable(R.drawable.ic_check)
            )
        )
        return list.sortedBy { it.startDate }
    }

    private fun getCalendarYear(): java.util.ArrayList<Int> {
        val years = java.util.ArrayList<Int>()
        val year = LocalDate.now().year
        for (i in (year - 8)..(year + 10)) years.add(i)
        return years
    }
}