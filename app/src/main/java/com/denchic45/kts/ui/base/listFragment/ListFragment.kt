package com.denchic45.kts.ui.base.listFragment

import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.RecyclerView
import by.kirich1409.viewbindingdelegate.viewBinding
import com.denchic45.kts.R
import com.denchic45.kts.databinding.FragmentListBinding

open class ListFragment<T : RecyclerView.Adapter<*>?> : Fragment(R.layout.fragment_list) {
    val binding: FragmentListBinding by viewBinding(FragmentListBinding::bind)
    var adapter: T? = null
        set(value) {
            field = value
            binding.rv.adapter = adapter
        }
}