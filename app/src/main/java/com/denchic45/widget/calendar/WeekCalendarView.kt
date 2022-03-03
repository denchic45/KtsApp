package com.denchic45.widget.calendar

import android.content.Context
import android.os.Parcel
import android.os.Parcelable
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.LinearLayout
import androidx.core.content.ContextCompat
import androidx.viewpager2.widget.ViewPager2
import androidx.viewpager2.widget.ViewPager2.OnPageChangeCallback
import com.denchic45.kts.R
import com.denchic45.widget.calendar.WeekCalendarListener.OnLoadListener
import com.denchic45.widget.calendar.model.WeekItem
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.ZoneId
import java.time.temporal.ChronoUnit
import kotlin.math.ceil
import kotlin.properties.Delegates

class WeekCalendarView : LinearLayout {
    private val adapter = WeekPageAdapter()
    private var viewPagerWeek: ViewPager2? = null
    private var listener: WeekCalendarListener? = null
    private var loadListener: OnLoadListener? = null
    private var pageChangeCallback: OnPageChangeCallback? = null

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {
        isSaveEnabled = true
        init()
    }

    constructor(context: Context) : super(context) {
        isSaveEnabled = true
        init()
    }

    override fun setEnabled(enabled: Boolean) {
        super.setEnabled(enabled)
        viewPagerWeek!!.isUserInputEnabled = enabled
        val weekHolder = adapter.getWeekHolder(viewPagerWeek!!.currentItem)
        weekHolder?.setEnable(enabled)
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()

    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        pageChangeCallback = null
    }

    private fun init() {
        inflate()
        background = ContextCompat.getDrawable(context, android.R.color.white)
        viewPagerWeek!!.adapter = adapter
        pageChangeCallback = object : OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                if (listener != null) listener!!.onWeekSelect(adapter.data[position])
                adapter.notifyGriViewAdapter(position)
                if (position == adapter.data.size - 1) {
                    val week = adapter.getItem(position)
                    loadFewWeeks(week[6].plusDays(1))
                }
            }
        }
        viewPagerWeek!!.registerOnPageChangeCallback(pageChangeCallback!!)


        if (adapter.data.isEmpty()) {
            addFirstWeeks()
            viewPagerWeek!!.setCurrentItem(CENTRAL_ITEM_POSITION, false)
        }
    }

    private fun addFirstWeeks() {
        val date = LocalDate.now(ZoneId.of("Europe/Moscow"))
            .with(DayOfWeek.MONDAY)
            .plusWeeks((-CENTRAL_ITEM_POSITION).toLong())

        val weekItemList: List<WeekItem> = List(6) {
            WeekItem(date.plusWeeks(it.toLong()))
        }
        weekItemList[CENTRAL_ITEM_POSITION].findAndSetCurrentDay()

        adapter.data.addAll(weekItemList)
    }

    private fun loadFewWeeks(monday: LocalDate) {
        val weekList = adapter.data
        for (date in List(6) { monday.plusWeeks(it.toLong()) }) {
            weekList.add(WeekItem(date))
        }
    }

    private fun inflate() {
        val inflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        inflater.inflate(R.layout.calendar_week, this)
        viewPagerWeek = findViewById(R.id.viewpager_week)
    }

    fun setWeekCalendarListener(listener: WeekCalendarListener) {
        this.listener = listener
        adapter.setListener(listener)
    }

    fun removeListeners() {
        listener = null
        loadListener = null
        adapter.setListener(null)
        viewPagerWeek!!.adapter = null
    }

    fun setLoadListener(loadListener: OnLoadListener) {
        this.loadListener = loadListener
    }

    override fun onSaveInstanceState(): Parcelable {
        val superState = super.onSaveInstanceState()
        val myState = SavedState(superState)
        myState.weekItemList = adapter.data
        return myState
    }

    public override fun onRestoreInstanceState(state: Parcelable) {
        val savedState = state as SavedState
        super.onRestoreInstanceState(savedState.superState)
        adapter.data.addAll(savedState.weekItemList)
    }

    fun setSelectDate(selectDate: LocalDate) {
        val currentWeek = adapter.getItem(viewPagerWeek!!.currentItem)
        val offsetWeeks = getOffsetScroll(currentWeek[0], selectDate)
        listener!!.onDaySelect(selectDate)
        adapter.setCheckDay(offsetWeeks)
        viewPagerWeek!!.currentItem = offsetWeeks
    }

    private fun getOffsetScroll(
        firstDateOfCurrentWeek: LocalDate,
        selectedDate: LocalDate
    ): Int {
        val offsetDays = ChronoUnit.DAYS.between(selectedDate, firstDateOfCurrentWeek)
        val offsetWeeks = ceil(offsetDays / 7.0)
        return (viewPagerWeek!!.currentItem - offsetWeeks).toInt()
    }

    internal class SavedState(superState: Parcelable?) : BaseSavedState(superState) {
        var weekItemList: List<WeekItem> by Delegates.notNull()

        override fun writeToParcel(out: Parcel, flags: Int) {
            super.writeToParcel(out, flags)
        }

        override fun describeContents(): Int {
            return 0
        }
    }

    companion object {
        var CENTRAL_ITEM_POSITION = 3
    }
}