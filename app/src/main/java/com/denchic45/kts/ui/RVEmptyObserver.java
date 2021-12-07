package com.denchic45.kts.ui;

import android.view.View;

import androidx.recyclerview.widget.RecyclerView;

public class RVEmptyObserver extends RecyclerView.AdapterDataObserver {
    private final View emptyView;
    private final RecyclerView recyclerView;

    public RVEmptyObserver(RecyclerView rv, View ev) {
        this.recyclerView = rv;
        this.emptyView = ev;
        checkIfEmpty();
    }

    private void checkIfEmpty() {
        if (emptyView != null && recyclerView.getAdapter() != null) {
            boolean emptyViewVisible = recyclerView.getAdapter().getItemCount() == 0;
            emptyView.setVisibility(emptyViewVisible ? View.VISIBLE : View.GONE);
            recyclerView.setVisibility(emptyViewVisible ? View.GONE : View.VISIBLE);
        }
    }

    public void onChanged() {
        checkIfEmpty();
    }

    public void onItemRangeInserted(int positionStart, int itemCount) {

//        checkIfEmpty();
    }

    public void onItemRangeRemoved(int positionStart, int itemCount) {
//        checkIfEmpty();
    }
}
