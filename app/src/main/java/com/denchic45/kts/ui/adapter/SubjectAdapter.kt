package com.denchic45.kts.ui.adapter

import android.graphics.drawable.PictureDrawable
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.denchic45.kts.SvgColorListener
import com.denchic45.kts.R
import com.denchic45.kts.domain.DomainModel
import com.denchic45.kts.data.model.domain.ListItem
import com.denchic45.kts.data.model.domain.Subject
import com.denchic45.kts.databinding.ItemIconContentBinding
import com.denchic45.kts.glideSvg.GlideApp
import com.denchic45.kts.util.viewBinding

class SubjectAdapter : CustomAdapter<DomainModel, BaseViewHolder<DomainModel, *>> {

    constructor(itemClickListener: OnItemClickListener) : super(
        (DIFF_CALLBACK as DiffUtil.ItemCallback<DomainModel>),
        itemClickListener
    )

    constructor(
        itemClickListener: OnItemClickListener,
        itemLongClickListener: OnItemLongClickListener
    ) : super(
        DIFF_CALLBACK as DiffUtil.ItemCallback<DomainModel>,
        itemClickListener,
        itemLongClickListener
    )

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): BaseViewHolder<DomainModel, *> {
        return when (viewType) {
            TYPE_SUBJECT -> {
                SubjectHolder(
                    parent.viewBinding(ItemIconContentBinding::inflate),
                    onItemClickListener,
                    onItemLongClickListener
                ) as BaseViewHolder<DomainModel, *>
            }
            else -> throw IllegalStateException("Unexpected value: $viewType")
        }
    }

    override fun getItemViewType(position: Int): Int {
        if (getItem(position) is Subject) {
            return TYPE_SUBJECT
        } else if (getItem(position) is ListItem) {
            return (getItem(position) as ListItem).type
        }
        return -1
    }

    override fun onBindViewHolder(holder: BaseViewHolder<DomainModel, *>, position: Int) {
        holder.onBind(getItem(position))
    }

    internal class SubjectHolder(
        itemIconContentBinding: ItemIconContentBinding,
        itemClickListener: OnItemClickListener,
        longClickListener: OnItemLongClickListener
    ) : BaseViewHolder<Subject, ItemIconContentBinding>(
        itemIconContentBinding,
        itemClickListener,
        longClickListener
    ) {
        private val ivIcon: ImageView = itemView.findViewById(R.id.iv_icon)
        private val tvName: TextView = itemView.findViewById(R.id.tv_name)
        override fun onBind(item: Subject) {
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

    companion object {
        const val TYPE_SUBJECT = 0
        private val DIFF_CALLBACK: DiffUtil.ItemCallback<out DomainModel> =
            object : DiffUtil.ItemCallback<Subject>() {
                override fun areItemsTheSame(oldItem: Subject, newItem: Subject): Boolean {
                    return oldItem.id == newItem.id
                }

                override fun areContentsTheSame(oldItem: Subject, newItem: Subject): Boolean {
                    return oldItem.name == newItem.name && oldItem.iconUrl == newItem.iconUrl && oldItem.colorName == newItem.colorName
                }
            }
    }
}