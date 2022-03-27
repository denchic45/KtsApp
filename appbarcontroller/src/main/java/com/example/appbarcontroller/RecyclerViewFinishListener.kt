package com.example.appbarcontroller

import android.os.Handler
import android.os.Looper
import androidx.recyclerview.widget.RecyclerView

class RecyclerViewFinishListener(recyclerView: RecyclerView, runnable: Runnable) {
    init {
        recyclerView.postOnAnimationDelayed(object : Runnable {
            override fun run() {
                if (recyclerView.isAnimating) {
                    recyclerView.itemAnimator!!.isRunning {
                        Handler(Looper.getMainLooper()).post(
                            this
                        )
                    }
                    return
                }
                runnable.run()
            }
        }, 100)
    }
}