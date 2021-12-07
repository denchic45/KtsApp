package com.denchic45.kts.ui.adapter

import android.view.ViewGroup
import com.denchic45.kts.data.model.domain.CourseSection
import com.denchic45.kts.databinding.ItemCourseSectionBinding
import com.denchic45.kts.utils.viewBinding
import com.denchic45.widget.extendedAdapter.ListItemAdapterDelegate

class CourseSectionAdapterDelegate : ListItemAdapterDelegate<CourseSection, CourseSectionHolder>() {

    override fun isForViewType(item: Any): Boolean = item is CourseSection

    override fun onBindViewHolder(item: CourseSection, holder: CourseSectionHolder) =
        holder.onBind(item)

    override fun onCreateViewHolder(parent: ViewGroup): CourseSectionHolder {
        return CourseSectionHolder(parent.viewBinding(ItemCourseSectionBinding::inflate))
    }
}

class CourseSectionHolder(courseSectionBinding: ItemCourseSectionBinding) :
    BaseViewHolder<CourseSection, ItemCourseSectionBinding>(courseSectionBinding) {
    override fun onBind(item: CourseSection) {
        with(binding) {
            tvName.text = item.name
        }
    }

}