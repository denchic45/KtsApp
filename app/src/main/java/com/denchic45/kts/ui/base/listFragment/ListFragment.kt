package com.denchic45.kts.ui.base.listFragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.RecyclerView
import com.denchic45.kts.R

open class ListFragment<T : RecyclerView.Adapter<*>?> : Fragment() {
    var recyclerView: RecyclerView? = null
        private set
    var adapter: T? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        recyclerView = inflater.inflate(R.layout.fragment_list, container, false) as RecyclerView
        return recyclerView
    }
}