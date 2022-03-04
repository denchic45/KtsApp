package com.denchic45.widget.calendar

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.TextView
import com.denchic45.kts.R
import com.denchic45.kts.utils.toString
import java.time.LocalDate

class DayAdapter(context: Context, resource: Int, private val dayOfWeekList: List<LocalDate>) :
    ArrayAdapter<LocalDate>(context, resource, dayOfWeekList) {
    private val inflater: LayoutInflater = LayoutInflater.from(context)
    var enable = true

    override fun getView(position: Int, view: View?, parent: ViewGroup): View {
        var view = view
        if (view == null) {
            view = inflater.inflate(R.layout.item_date, parent, false)
        }
        if (!enable) {
            view!!.alpha = 0.5f
        } else {
            view!!.alpha = 1f
        }
        (view as TextView?)!!.text = dayOfWeekList[position].toString("d")
        return view
    }

}