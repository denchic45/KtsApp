package com.denchic45.widget.calendar;

import android.content.Context;
import android.os.Parcel;
import android.os.Parcelable;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.viewpager2.widget.ViewPager2;

import com.denchic45.kts.R;
import com.denchic45.kts.utils.ViewUtils;
import com.denchic45.widget.calendar.model.Week;

import org.apache.commons.lang3.time.DateUtils;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

public class WeekCalendarView extends LinearLayout {

    public static int CENTRAL_ITEM_POSITION = 3;
    private final WeekPageAdapter adapter = new WeekPageAdapter();
    private ViewPager2 viewPagerWeek;
    private WeekCalendarListener listener;

    private WeekCalendarListener.OnLoadListener loadListener;
    private ViewPager2.OnPageChangeCallback pageChangeCallback;


    public WeekCalendarView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        setSaveEnabled(true);
        init();
    }

    public WeekCalendarView(Context context) {
        super(context);
        setSaveEnabled(true);
        init();
    }

    @Override
    public void setEnabled(boolean enabled) {
        super.setEnabled(enabled);
        viewPagerWeek.setUserInputEnabled(enabled);
//        viewPagerWeek.post(() -> {
            WeekPageAdapter.WeekHolder weekHolder = adapter.getWeekHolder(viewPagerWeek.getCurrentItem());
            if (weekHolder != null)
                weekHolder.setEnable(enabled);
//        });
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        if (adapter.getData().isEmpty()) {
            addFirstWeeks();
            viewPagerWeek.setCurrentItem(CENTRAL_ITEM_POSITION, false);
        }
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        pageChangeCallback = null;
    }

    private void init() {
        inflate();
        setBackground(ContextCompat.getDrawable(getContext(), android.R.color.white));
        viewPagerWeek.setAdapter(adapter);
        pageChangeCallback = new ViewPager2.OnPageChangeCallback() {

            @Override
            public void onPageSelected(int position) {
                super.onPageSelected(position);
                if (listener != null)
                    listener.onWeekSelect(adapter.getData().get(position));
                adapter.notifyGriViewAdapter(position);
                if (position == adapter.getData().size() - 1) {
                    Calendar calendar = Calendar.getInstance();
                    Week week = adapter.getItem(position);
                    calendar.setTime(week.getDate(6));
                    calendar.add(Calendar.DATE, 1);
                    loadFewWeeks(calendar);
                    Toast.makeText(getContext(), "load weeks", Toast.LENGTH_SHORT).show();
                }
            }
        };
        viewPagerWeek.registerOnPageChangeCallback(pageChangeCallback);
    }

    private void addFirstWeeks() {
        Calendar calendar = Calendar.getInstance();
        calendar.setFirstDayOfWeek(Calendar.MONDAY);
        calendar.add(Calendar.WEEK_OF_MONTH, -CENTRAL_ITEM_POSITION);
        calendar.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY);
        List<Week> weekList = new ArrayList<>();
        Week week;
        while (weekList.size() < 7) {
            week = getWeek(calendar);
            if (weekList.size() == CENTRAL_ITEM_POSITION) {
                week.findAndSetCurrentDay();
            }
            weekList.add(week);
        }
        adapter.setData(weekList);
    }

    private void loadFewWeeks(Calendar calendar) {
        List<Week> weekList = adapter.getData();
        for (int i = 0; i < 7; i++) {
            weekList.add(getWeek(calendar));
        }
    }

    @NotNull
    @Contract("_ -> new")
    private Week getWeek(Calendar calendar) {
        ArrayList<Date> daysOfWeekList = new ArrayList<>();
        int selectedDay = -1;

        while (daysOfWeekList.size() < 7) {
            daysOfWeekList.add(calendar.getTime());
            calendar.add(Calendar.DAY_OF_MONTH, 1);
        }
        return new Week(daysOfWeekList, selectedDay);
    }

    private void inflate() {
        LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        inflater.inflate(R.layout.calendar_week, this);
        viewPagerWeek = findViewById(R.id.viewpager_week);
    }

    public void setListener(WeekCalendarListener listener) {
        this.listener = listener;
        adapter.setListener(listener);
    }

    public void removeListeners() {
        listener = null;
        loadListener = null;
        adapter.setListener(null);
        viewPagerWeek.setAdapter(null);
    }

    public void setLoadListener(WeekCalendarListener.OnLoadListener loadListener) {
        this.loadListener = loadListener;
    }


    @Nullable
    @Override
    protected Parcelable onSaveInstanceState() {
        Parcelable superState = super.onSaveInstanceState();
        SavedState myState = new SavedState(superState);
        myState.weekList = adapter.getData();

        return myState;
    }

    @Override
    public void onRestoreInstanceState(Parcelable state) {
        SavedState savedState = (SavedState) state;
        super.onRestoreInstanceState(savedState.getSuperState());
        adapter.setData(savedState.weekList);
    }

    public void setSelectDate(@NotNull Date selectDate) {
        Calendar calSelectedDate = Calendar.getInstance();
        Calendar calFirstDateOfCurrentWeek = Calendar.getInstance();
        calSelectedDate.setTime(selectDate);
        Week currentWeek = adapter.getItem(viewPagerWeek.getCurrentItem());
        calFirstDateOfCurrentWeek.setTime(currentWeek.getDate(0));

        int offsetWeeks = setOffsetScroll(calFirstDateOfCurrentWeek, calSelectedDate);
        listener.onDaySelect(DateUtils.truncate(selectDate, Calendar.DAY_OF_MONTH));
        adapter.setCheckDay(getOffsetPosition(offsetWeeks));
        viewPagerWeek.setCurrentItem(getOffsetPosition(offsetWeeks));
    }

    private int setOffsetScroll(@NotNull Calendar calFirstDateOfCurrentWeek, @NotNull Calendar calSelectedDate) {
        int offset = 0;
        while (diffWeeks(calFirstDateOfCurrentWeek, calSelectedDate)) {
            if (calFirstDateOfCurrentWeek.getTime().after(calSelectedDate.getTime())) {
                offset--;
            } else {
                offset++;
            }
            Week week = adapter.getItem(getOffsetPosition(offset));
            calFirstDateOfCurrentWeek.setTime(week.getDate(0));
        }
        return offset;
    }

    private boolean diffWeeks(@NotNull Calendar calFirstDateOfCurrentWeek, @NotNull Calendar calSelected) {
        return calSelected.get(Calendar.YEAR) != calFirstDateOfCurrentWeek.get(Calendar.YEAR) || calSelected.get(Calendar.WEEK_OF_YEAR) != calFirstDateOfCurrentWeek.get(Calendar.WEEK_OF_YEAR);
    }

    private int getOffsetPosition(int offsetScroll) {
        return viewPagerWeek.getCurrentItem() + offsetScroll;
    }

    private static class SavedState extends BaseSavedState {
        public static final Parcelable.Creator<SavedState> CREATOR
                = new Parcelable.Creator<SavedState>() {
            @NotNull
            @Contract("_ -> new")
            public SavedState createFromParcel(Parcel in) {
                return new SavedState(in);
            }

            @NotNull
            @Contract(value = "_ -> new", pure = true)
            public SavedState[] newArray(int size) {
                return new SavedState[size];
            }
        };
        List<Week> weekList;

        SavedState(Parcelable superState) {
            super(superState);
        }

        private SavedState(Parcel in) {
            super(in);
            //Nothing
        }

        @Override
        public void writeToParcel(Parcel out, int flags) {
            super.writeToParcel(out, flags);
            //Nothing
        }
    }
}