package com.task.sm.core.calendar

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import androidx.core.content.ContextCompat.getDrawable
import com.google.android.material.textview.MaterialTextView
import com.task.sm.core.R

class YearSelectionAdapter(
    context: Context,
    resourceId: Int,
    private val list: List<Int>
) : ArrayAdapter<Int>(context, resourceId, list) {
    var mcontext: Context
    var selectedYear = -1

    init {
        this.mcontext = context
    }

    override fun getDropDownView(
        position: Int,
        convertView: View?,
        parent: ViewGroup?
    ): View {
        return getCustomView(position, convertView, parent, mcontext)
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        return getCustomView(position, convertView, parent, mcontext)
    }

    private fun getCustomView(
        position: Int,
        convertView: View?,
        parent: ViewGroup?,
        context: Context
    ): View {
        val inflater =
            context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        val row: View = inflater.inflate(R.layout.item_year_selection, parent, false)
        val label = row.findViewById<MaterialTextView>(R.id.tvYear)
        label.text = "${list[position]}"
        when {
            position == 0 && selectedYear == list[position] -> {
                label.background = getDrawable(context, R.drawable.bg_spinner_top_item_selection)
            }

            position == list.size - 1 && selectedYear == list[position] -> {
                label.background = getDrawable(context, R.drawable.bg_spinner_bottom_item_selection)
            }

            selectedYear == list[position] && position != 0 && position != list.size - 1 -> {
                label.background = getDrawable(context, R.drawable.bg_spinner_item_selection)
            }
        }
        return row
    }
}
