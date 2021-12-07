package com.denchic45.widget.extendedAdapter

import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView

interface AdapterDelegate {

    fun isForViewType(items: List<Any>, position: Int): Boolean

    fun onCreateViewHolder(parent: ViewGroup): RecyclerView.ViewHolder

    fun onBindViewHolder(holder: RecyclerView.ViewHolder, items: List<Any>, position: Int)

    fun onBindViewHolder(holder: RecyclerView.ViewHolder, items: List<Any>, position: Int, payloads: MutableList<Any>)
}