//package com.example.kts.ui.timetable;
//
//import android.util.Log;
//import android.view.LayoutInflater;
//import android.view.View;
//import android.view.ViewGroup;
//
//import androidx.annotation.NonNull;
//import androidx.recyclerview.widget.DividerItemDecoration;
//import androidx.recyclerview.widget.LinearLayoutManager;
//import androidx.recyclerview.widget.RecyclerView;
//
//import com.example.kts.R;
//import com.example.kts.data.model.TimetablePage;
//
//import java.util.ArrayList;
//import java.util.List;
//
//public class TimetablePageAdapter extends RecyclerView.Adapter<TimetablePageAdapter.TimetablePageHolder> {
//
//    List<TimetablePage> pageList;
//
//    public TimetablePageAdapter() {
//        pageList = new ArrayList<>();
//    }
//
//    public List<TimetablePage> getData() {
//        return pageList;
//    }
//
//    public void setData(List<TimetablePage> pageList) {
//        this.pageList = pageList;
//        Log.d("lol", "setData: ");
//    }
//
//    @NonNull
//    @Override
//    public TimetablePageHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
//        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_timetable, parent, false);
//        return new TimetablePageHolder(view);
//    }
//
//    @Override
//    public void onBindViewHolder(@NonNull TimetablePageHolder holder, int position) {
//        holder.adapter.setData(pageList.get(position).getLessonList());
//        holder.adapter.notifyDataSetChanged();
//    }
//
//    @Override
//    public int getItemCount() {
//        return pageList.size();
//    }
//
//    class TimetablePageHolder extends RecyclerView.ViewHolder {
//        RecyclerView recyclerView;
//        private LessonAdapter adapter = new LessonAdapter();
//
//        public TimetablePageHolder(@NonNull View itemView) {
//            super(itemView);
//            recyclerView = itemView.findViewById(R.id.recyclerview_lessons);
//            recyclerView.setLayoutManager(new LinearLayoutManager(itemView.getContext()));
//            recyclerView.addItemDecoration(new DividerItemDecoration(itemView.getContext(), DividerItemDecoration.VERTICAL));
//            recyclerView.setAdapter(adapter);
//        }
//    }
//}
