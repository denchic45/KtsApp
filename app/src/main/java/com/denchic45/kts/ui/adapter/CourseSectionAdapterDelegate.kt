package com.denchic45.kts.ui.adapter

import android.view.ViewGroup
import com.denchic45.kts.data.model.domain.Section
import com.denchic45.kts.databinding.ItemCourseSectionBinding
import com.denchic45.kts.util.viewBinding
import com.denchic45.widget.extendedAdapter.ListItemAdapterDelegate

class CourseSectionAdapterDelegate : ListItemAdapterDelegate<Section, SectionHolder>() {

    override fun isForViewType(item: Any): Boolean = item is Section

    override fun onBindViewHolder(item: Section, holder: SectionHolder) =
        holder.onBind(item)

    override fun onCreateViewHolder(parent: ViewGroup): SectionHolder {
        return SectionHolder(parent.viewBinding(ItemCourseSectionBinding::inflate))
    }
}

class SectionHolder(SectionBinding: ItemCourseSectionBinding) :
    BaseViewHolder<Section, ItemCourseSectionBinding>(SectionBinding) {
    override fun onBind(item: Section) {
        with(binding) {
            tvName.text = item.name
        }
    }

}