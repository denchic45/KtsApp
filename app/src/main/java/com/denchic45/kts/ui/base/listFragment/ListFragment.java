package com.denchic45.kts.ui.base.listFragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;

import com.denchic45.kts.R;

import org.jetbrains.annotations.NotNull;

public class ListFragment<T extends RecyclerView.Adapter<?>> extends Fragment {

    private RecyclerView recyclerView;
    private T adapter;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    public void setAdapter(T adapter) {
        this.adapter = adapter;
        recyclerView.setAdapter(adapter);
    }

    public RecyclerView getRecyclerView() {
        return recyclerView;
    }

    @Override
    public View onCreateView(@NotNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        recyclerView = (RecyclerView) inflater.inflate(R.layout.fragment_list, container, false);
        return recyclerView;
    }

    public T getAdapter() {
        return adapter;
    }
}