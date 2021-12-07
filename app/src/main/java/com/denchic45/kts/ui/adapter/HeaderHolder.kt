package com.denchic45.kts.ui.adapter

import com.denchic45.kts.data.model.domain.ListItem
import com.denchic45.kts.databinding.ItemHeaderBinding

class HeaderHolder(itemHeaderBinding: ItemHeaderBinding) :
    BaseViewHolder<ListItem, ItemHeaderBinding>(itemHeaderBinding) {
    override fun onBind(item: ListItem) {
        binding.tvHeader.text = item.title
    }
}