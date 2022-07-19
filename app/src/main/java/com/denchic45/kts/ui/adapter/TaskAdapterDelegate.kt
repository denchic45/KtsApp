package com.denchic45.kts.ui.adapter

import android.view.ViewGroup
import com.denchic45.kts.domain.model.Task
import com.denchic45.kts.databinding.ItemTaskBinding
import com.denchic45.kts.util.toString
import com.denchic45.kts.util.viewBinding
import com.denchic45.widget.extendedAdapter.ListItemAdapterDelegate

class TaskAdapterDelegate : ListItemAdapterDelegate<Task, TaskHolder>() {

    override fun isForViewType(item: Any): Boolean = item is Task

    override fun onBindViewHolder(item: Task, holder: TaskHolder) = holder.onBind(item)

    override fun onCreateViewHolder(parent: ViewGroup): TaskHolder {
        return TaskHolder(parent.viewBinding(ItemTaskBinding::inflate))
    }
}

class TaskHolder(itemTaskBinding: ItemTaskBinding) :
    BaseViewHolder<Task, ItemTaskBinding>(itemTaskBinding) {
    override fun onBind(item: Task) {
        with(binding) {
            tvTitle.text = item.name
            tvSubtitle.text = item.completionDate?.toString("dd MMM HH:mm")
                ?: run { "Без срока сдачи" }
        }
    }

}