package com.denchic45.widget.extendedAdapter

import android.content.res.ColorStateList
import android.view.ViewGroup
import androidx.core.widget.ImageViewCompat
import com.denchic45.kts.data.model.domain.ListItem
import com.denchic45.kts.data.model.ui.onId
import com.denchic45.kts.data.model.ui.onUrl
import com.denchic45.kts.databinding.ItemIconContentBinding
import com.denchic45.kts.ui.adapter.BaseViewHolder
import com.denchic45.kts.util.viewBinding

class ItemAdapterDelegate : ListItemAdapterDelegate<ListItem, ItemAdapterDelegate.ItemHolder>() {

    class ItemHolder(
        itemIconContentBinding: ItemIconContentBinding,
    ) : BaseViewHolder<ListItem, ItemIconContentBinding>(
        itemIconContentBinding
    ) {
        override fun onBind(item: ListItem) {
            with(binding) {

                item.icon?.onId {
                    ivIcon.setImageResource(it)
                }?.onUrl {
                    val iconResId = itemView.context.resources.getIdentifier(
                        it,
                        "drawable",
                        itemView.context.packageName
                    )
                    ivIcon.setImageResource(iconResId)
                }


                tvName.text = item.title

                item.color?.onId {
                    ImageViewCompat.setImageTintList(
                        ivIcon,
                        ColorStateList.valueOf(it)
                    )
                }?.onUrl {
                    val identifier = itemView.resources
                        .getIdentifier(it, "color", itemView.context.packageName)
                    ColorStateList.valueOf(
                        identifier
                    )
                }
            }
        }
    }

    override fun isForViewType(item: Any): Boolean {
        return item is ListItem && item.type == 0
    }

    override fun onBindViewHolder(item: ListItem, holder: ItemHolder) {
        holder.onBind(item)
    }

    override fun onCreateViewHolder(parent: ViewGroup): ItemHolder {
        return ItemHolder(parent.viewBinding(ItemIconContentBinding::inflate))
    }
}