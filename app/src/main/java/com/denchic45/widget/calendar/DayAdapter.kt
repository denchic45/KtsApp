package com.denchic45.widget.calendar

import android.content.Context
import android.widget.ArrayAdapter
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.denchic45.kts.R
import android.widget.TextView
import com.denchic45.kts.utils.toDate
import com.denchic45.kts.utils.toString
import java.time.LocalDate
import java.util.*

class DayAdapter(context: Context, resource: Int, private val dayOfWeekList: List<LocalDate>) :
    ArrayAdapter<LocalDate>(context, resource, dayOfWeekList) {
    private val inflater: LayoutInflater = LayoutInflater.from(context)
//    private val calendar = Calendar.getInstance()
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
//        calendar.time = dayOfWeekList[position].toDate()
        (view as TextView?)!!.text = dayOfWeekList[position].toString("d M")
        return view
    }

}