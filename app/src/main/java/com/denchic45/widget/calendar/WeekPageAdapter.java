package com.denchic45.widget.calendar;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.GridView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.denchic45.kts.R;
import com.denchic45.widget.calendar.model.Week;

import org.apache.commons.lang3.time.DateUtils;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

public class WeekPageAdapter extends RecyclerView.Adapter<WeekPageAdapter.WeekHolder> {

    private List<Week> weekList = new ArrayList<>();
    private WeekCalendarListener listener;
    private RecyclerView recyclerView;

    private int weekItemOfCheckedDayItemPos = 3;

    public List<Week> getData() {
        return weekList;
    }

    public void setData(List<Week> weekList) {
        this.weekList = weekList;
    }

    public void setListener(WeekCalendarListener listener) {
        this.listener = listener;
    }

    public void setCheckDay(int position) {
        weekList.get(weekItemOfCheckedDayItemPos).setSelectedDay(-1);
        Week week = weekList.get(position);
        week.findAndSetCurrentDay();
        notifyItemChanged(position);
        notifyItemChanged(weekItemOfCheckedDayItemPos);
        weekItemOfCheckedDayItemPos = position;
    }

    @NonNull
    @Override
    public WeekHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_week, parent, false);
        return new WeekHolder(view);
    }

    @Override
    public void onAttachedToRecyclerView(@NonNull RecyclerView recyclerView) {
        super.onAttachedToRecyclerView(recyclerView);
        this.recyclerView = recyclerView;
    }


    @Override
    public void onBindViewHolder(@NonNull WeekHolder holder, int position) {
        holder.onBind(position);
    }

    @Override
    public long getItemId(int position) {
        return super.getItemId(position);
    }

    @Override
    public int getItemCount() {
        return weekList.size();
    }

    public Week getItem(int position) {
        return weekList.get(position);
    }

    public void notifyGriViewAdapter(int position) {
        if (getWeekHolder(position) != null)
            getWeekHolder(position).notifyGridViewAdapter();
    }

    public WeekHolder getWeekHolder(int position) {
        return (WeekHolder) recyclerView.findViewHolderForAdapterPosition(position);
    }

    class WeekHolder extends RecyclerView.ViewHolder {

        private final GridView gridView;
        private DayAdapter adapter;

        public WeekHolder(@NonNull final View itemView) {
            super(itemView);
            gridView = itemView.findViewById(R.id.grid_days);

            gridView.setOnItemClickListener((adapterView, view, position, l) -> {
                if (weekItemOfCheckedDayItemPos != getBindingAdapterPosition()) {
                    weekList.get(weekItemOfCheckedDayItemPos).setSelectedDay(-1);
                    notifyItemChanged(weekItemOfCheckedDayItemPos);
                    weekItemOfCheckedDayItemPos = getBindingAdapterPosition();
                }
                weekList.get(weekItemOfCheckedDayItemPos).setSelectedDay(position);
                setCheckedItem(position, true);
                listener.onDaySelect(DateUtils.truncate(adapter.getItem(position), Calendar.DAY_OF_MONTH));
            });
        }

        public void setEnable(boolean enable) {
            gridView.setEnabled(enable);
            adapter.setEnable(enable);
            gridView.invalidateViews();
        }

        public void onBind(int position) {
            addDaysOfWeek(weekList.get(position));
            int selectedDay = weekList.get(position).getSelectedDay();
            if (selectedDay != -1) {
                weekItemOfCheckedDayItemPos = position;
                setCheckedItem(selectedDay, true);
            }
        }

        public void setCheckedItem(int position, boolean checked) {
            gridView.setItemChecked(position, checked);
        }

        public void notifyGridViewAdapter() {
            adapter.notifyDataSetChanged();
        }


        public void addDaysOfWeek(@NotNull Week week) {
            List<Date> daysList = week.getDayOfWeekList();
            adapter = new DayAdapter(itemView.getContext(), R.layout.item_date, daysList);
            gridView.setAdapter(adapter);
        }
    }
}
