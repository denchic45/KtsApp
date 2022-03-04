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
import java.time.LocalDate

class WeekPageAdapter(
    private var listener: WeekCalendarListener
) : RecyclerView.Adapter<WeekHolder>() {
    var data: MutableList<WeekItem> = mutableListOf()
    private var recyclerView: RecyclerView? = null
    private var weekItemOfCheckedDayItemPos = 3

    fun setCheckDay(weekPosition: Int, date: LocalDate) {
        val dayOfWeek = date.dayOfWeek.value - 1
        if (data[weekItemOfCheckedDayItemPos].selectedDay == dayOfWeek)
            return
        data[weekItemOfCheckedDayItemPos].selectedDay = -1
        val week = data[weekPosition]

        week.selectedDay = dayOfWeek
        notifyItemChanged(weekPosition)
        notifyItemChanged(weekItemOfCheckedDayItemPos)
        weekItemOfCheckedDayItemPos = weekPosition
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

    override fun getItemCount(): Int = data.size

    fun getItem(position: Int): WeekItem {
        return data[position]
    }

    fun notifyGriViewAdapter(position: Int) {
        recyclerView!!.postDelayed({
            if (getWeekHolder(position) != null)
                getWeekHolder(position)?.notifyGridViewAdapter()
        }, 300)

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
            }
            setCheckedItem(selectedDay)
        }

        private fun setCheckedItem(selectedDay: Int) {
            gridView.setItemChecked(selectedDay, true)
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
                    data[weekItemOfCheckedDayItemPos].selectedDay = position.toInt()
                    listener.onDaySelect(
                        adapter!!.getItem(
                            position.toInt()
                        )!!
                    )
                }
        }
    }

}