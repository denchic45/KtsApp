package com.denchic45.kts.ui.adapter

import android.view.ViewGroup
import android.widget.CompoundButton
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.DrawableCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import com.denchic45.kts.data.model.domain.ListItem
import com.denchic45.kts.databinding.*
import com.denchic45.kts.utils.viewBinding

class ItemAdapter : ListAdapter<ListItem, BaseViewHolder<ListItem, *>>(DIFF_CALLBACK) {
    var itemClickListener: OnItemClickListener = OnItemClickListener { }
    private var itemCheckListener: OnItemCheckListener = OnItemCheckListener { _, _ -> }

    override fun getItemViewType(position: Int): Int {
        return getItem(position)!!.type
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BaseViewHolder<ListItem, *> {
        when (viewType) {
            TYPE_VIEW -> return IconItemHolder(
                parent.viewBinding(ItemIconContentBinding::inflate),
                itemClickListener
            )
            TYPE_VIEW_2 -> return IconItemHolder2(
                parent.viewBinding(ItemPopupIconContentBinding::inflate),
                itemClickListener
            )
            TYPE_HEADER -> return HeaderHolder(
                parent.viewBinding(ItemHeaderBinding::inflate)
            )
            TYPE_PROGRESS -> return ProgressItemHolder(
                parent.viewBinding(ItemIconContent2Binding::inflate), itemClickListener
            )
            TYPE_SWITCH -> return SwitchItemHolder(
                parent.viewBinding(ItemContentSwitchBinding::inflate), itemCheckListener
            )
            else -> throw IllegalStateException()
        }
    }

    override fun onBindViewHolder(holder: BaseViewHolder<ListItem, *>, position: Int) {
        holder.onBind(getItem(position))
    }

    override fun onBindViewHolder(
        holder: BaseViewHolder<ListItem, *>,
        position: Int,
        payloads: List<Any>
    ) {
        if (payloads.isEmpty()) {
            onBindViewHolder(holder, position)
        } else {
            for (payload in payloads) {
                if (holder is ProgressItemHolder) {
                    when {
                        payload === PAYLOAD.SHOW_LOADING -> {
                            holder.showLoading()
                        }
                        payload === PAYLOAD.SHOW_IMAGE -> {
                            holder.showImage()
                        }
                        payload === PAYLOAD.CHANGE_TITLE -> {
                            holder.binding.tvName.text = (getItem(position)!!.title)
                        }
                    }
                }
            }
        }
    }

    enum class PAYLOAD {
        SHOW_LOADING, SHOW_IMAGE, CHANGE_TITLE
    }

    class IconItemHolder(
        itemIconContentBinding: ItemIconContentBinding,
        listener: OnItemClickListener
    ) :
        BaseViewHolder<ListItem, ItemIconContentBinding>(itemIconContentBinding, listener) {
        override fun onBind(item: ListItem) {

            with(binding) {
                tvName.text = item.title
                val context = itemView.context
                item.icon.fold({
                    ivIcon.setImageResource(it)
                }, {

                })

                item.color.fold({
                    if (it != 0) {
                        DrawableCompat.setTint(
                            DrawableCompat.wrap(ivIcon.drawable),
                            ContextCompat.getColor(context, it)
                        )
                    }
                }, {})
            }
        }
    }

    class IconItemHolder2(
        itemPopupIconContentBinding: ItemPopupIconContentBinding,
        listener: OnItemClickListener
    ) :
        BaseViewHolder<ListItem, ItemPopupIconContentBinding>(
            itemPopupIconContentBinding,
            listener
        ) {
        override fun onBind(item: ListItem) {

            with(binding) {
                val context = itemView.context
                item.icon.fold({
                    ivIcon.setImageResource(it)
                }, {

                })

                item.color.fold({
                    if (it != 0) {
                        DrawableCompat.setTint(
                            DrawableCompat.wrap(ivIcon.drawable),
                            ContextCompat.getColor(context, it)
                        )
                    }
                }, {})
            }
        }
    }

    class ProgressItemHolder(
        itemIconContent2Binding: ItemIconContent2Binding,
        listener: OnItemClickListener
    ) :
        BaseViewHolder<ListItem, ItemIconContent2Binding>(itemIconContent2Binding, listener) {
        override fun onBind(item: ListItem) {

            with(binding) {
                val context = itemView.context
                item.icon.fold({
                    ivIcon.setImageResource(it)
                }, {

                })

                item.color.fold({
                    if (it != 0) {
                        DrawableCompat.setTint(
                            DrawableCompat.wrap(ivIcon.drawable),
                            ContextCompat.getColor(context, it)
                        )
                    }
                }, {})
            }

            if (item.content == PAYLOAD.SHOW_LOADING) {
                showLoading()
            } else {
                showImage()
            }
        }

        fun showLoading() {
            if (itemView.isClickable) {
                binding.vsIc.post { binding.vsIc.displayedChild = 1 }
                itemView.isClickable = false
            }
        }

        fun showImage() {
            if (!itemView.isClickable) {
                binding.vsIc.post { binding.vsIc.displayedChild = 0 }
                itemView.isClickable = true
            }
        }
    }

    class SwitchItemHolder(
        itemContentSwitchBinding: ItemContentSwitchBinding,
        private val listener: OnItemCheckListener
    ) : BaseViewHolder<ListItem, ItemContentSwitchBinding>(itemContentSwitchBinding) {
        override fun onBind(item: ListItem) {
            with(binding) {
                sw.setOnCheckedChangeListener { buttonView: CompoundButton, isChecked: Boolean ->
                    listener.onItemCheck(
                        absoluteAdapterPosition, isChecked
                    )
                }

                if (item.content != null) sw.isChecked = (item.content as Boolean)
            }

        }

    }

    companion object {
        const val TYPE_VIEW = 0
        const val TYPE_VIEW_2 = 1
        const val TYPE_ITEM = 2
        const val TYPE_HEADER = 3
        const val TYPE_PROGRESS = 4
        const val TYPE_SWITCH = 5
        private val DIFF_CALLBACK: DiffUtil.ItemCallback<ListItem> =
            object : DiffUtil.ItemCallback<ListItem>() {
                override fun areItemsTheSame(oldItem: ListItem, newItem: ListItem): Boolean {
                    return oldItem.id == newItem.id
                }

                override fun areContentsTheSame(oldItem: ListItem, newItem: ListItem): Boolean {
                    return oldItem.title == newItem.title && oldItem == newItem
                }
            }
    }
}