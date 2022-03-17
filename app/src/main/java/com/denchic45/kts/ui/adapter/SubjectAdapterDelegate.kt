package com.denchic45.kts.ui.adapter

import android.graphics.drawable.PictureDrawable
import android.view.ViewGroup
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.denchic45.kts.SvgColorListener
import com.denchic45.kts.data.model.domain.Subject
import com.denchic45.kts.databinding.ItemIconContentBinding
import com.denchic45.kts.glideSvg.GlideApp
import com.denchic45.kts.utils.viewBinding
import com.denchic45.widget.extendedAdapter.ListItemAdapterDelegate

class SubjectAdapterDelegate :
    ListItemAdapterDelegate<Subject, SubjectAdapterDelegate.SubjectHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup): SubjectHolder {
        return SubjectHolder(
            parent.viewBinding(ItemIconContentBinding::inflate)
        )
    }

    override fun isForViewType(item: Any): Boolean = item is Subject

    override fun onBindViewHolder(item: Subject, holder: SubjectHolder) {
        return holder.onBind(item)
    }

    class SubjectHolder(
        itemIconContentBinding: ItemIconContentBinding
    ) : BaseViewHolder<Subject, ItemIconContentBinding>(itemIconContentBinding) {

        override fun onBind(item: Subject) {
            with(binding) {
                tvName.text = item.name
                val color = itemView.resources.getIdentifier(
                    item.colorName,
                    "color",
                    itemView.context.packageName
                )

                GlideApp.with(itemView.context)
                    .`as`(PictureDrawable::class.java)
                    .transition(DrawableTransitionOptions.withCrossFade())
                    .listener(
                        SvgColorListener(
                            ivIcon,
                            color,
                            itemView.context
                        )
                    )
                    .load(item.iconUrl)
                    .into(ivIcon)
            }
        }
    }
}