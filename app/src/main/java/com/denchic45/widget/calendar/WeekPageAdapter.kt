package com.denchic45.widget.calendar

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.GridView
import androidx.recyclerview.widget.RecyclerView
import com.denchic45.kts.R
import com.denchic45.widget.calendar.WeekPageAdapter.WeekHolder
import com.denchic45.widget.calendar.model.WeekItem
import org.apache.commons.lang3.time.DateUtils
import java.util.*

class WeekPageAdapter : RecyclerView.Adapter<WeekHolder>() {
    var data: MutableList<WeekItem> = mutableListOf()
    private var listener: WeekCalendarListener? = null
    private var recyclerView: RecyclerView? = null
    private var weekItemOfCheckedDayItemPos = 3
    fun setListener(listener: WeekCalendarListener?) {
        this.listener = listener
    }

    fun setCheckDay(position: Int) {
        data[weekItemOfCheckedDayItemPos].selectedDay = -1
        val week = data[position]
        week.findAndSetCurrentDay()
        notifyItemChanged(position)
        notifyItemChanged(weekItemOfCheckedDayItemPos)
        weekItemOfCheckedDayItemPos = position
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): WeekHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_week, parent, false)
        return WeekHolder(view)
    }

    override fun onAttachedToRecyclerView(recyclerView: RecyclerView) {
        super.onAttachedToRecyclerView(recyclerView)
        this.recyclerView = recyclerView
    }

    override fun onBindViewHolder(holder: WeekHolder, position: Int) {
        holder.onBind(position)
    }

    override fun getItemCount(): Int {
        return data.size
    }

    fun getItem(position: Int): WeekItem {
        return data[position]
    }

    fun notifyGriViewAdapter(position: Int) {
        if (getWeekHolder(position) != null) getWeekHolder(position)!!.notifyGridViewAdapter()
    }

    fun getWeekHolder(position: Int): WeekHolder? {
        return recyclerView!!.findViewHolderForAdapterPosition(position) as WeekHolder?
    }

    inner class WeekHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val gridView: GridView = itemView.findViewById(R.id.grid_days)
        private var adapter: DayAdapter? = null
        fun setEnable(enable: Boolean) {
            gridView.isEnabled = enable
            adapter!!.enable = enable
            gridView.invalidateViews()
        }

        fun onBind(position: Int) {
            addDaysOfWeek(data[position])
            val selectedDay = data[position].selectedDay
            if (selectedDay != -1) {
                weekItemOfCheckedDayItemPos = position
                setCheckedItem(selectedDay, true)
            }
        }

        private fun setCheckedItem(position: Int, checked: Boolean) {
            gridView.setItemChecked(position, checked)
        }

        fun notifyGridViewAdapter() {
            adapter!!.notifyDataSetChanged()
        }

        private fun addDaysOfWeek(weekItem: WeekItem) {
            val daysList = weekItem.daysOfWeek
            adapter = DayAdapter(itemView.context, R.layout.item_date, daysList)
            gridView.adapter = adapter
        }

        init {
            gridView.onItemClickListener =
                AdapterView.OnItemClickListener { _, _, _, position ->
                    if (weekItemOfCheckedDayItemPos != bindingAdapterPosition) {
                        data[weekItemOfCheckedDayItemPos].selectedDay = -1
                        notifyItemChanged(weekItemOfCheckedDayItemPos)
                        weekItemOfCheckedDayItemPos = bindingAdapterPosition
                    }
                    data[weekItemOfCheckedDayItemPos].selectedDay =position.toInt()
                    setCheckedItem(position.toInt(), true)
                    listener!!.onDaySelect(

                            adapter!!.getItem(
                                position.toInt()
                            )

                    )
                }
        }
    }
}