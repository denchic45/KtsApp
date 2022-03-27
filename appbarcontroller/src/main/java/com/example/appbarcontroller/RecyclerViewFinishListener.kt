package com.example.appbarcontroller;

import android.os.Handler;
import android.os.Looper;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

public class RecyclerViewFinishListener {

    public RecyclerViewFinishListener(@NonNull RecyclerView recyclerView, Runnable runnable) {
        recyclerView.postOnAnimationDelayed(new Runnable() {
            @Override
            public void run() {
                if (recyclerView.isAnimating()) {
                    recyclerView.getItemAnimator().isRunning(()
                            -> new Handler(Looper.getMainLooper()).post(this));
                    return;
                }
                runnable.run();
            }
        },100);
    }
}
