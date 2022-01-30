package com.denchic45.kts.ui.adapter

import android.annotation.SuppressLint
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import com.denchic45.kts.R
import com.denchic45.kts.data.model.DomainModel
import com.denchic45.kts.data.model.domain.Group
import com.denchic45.kts.data.model.domain.Specialty
import com.denchic45.kts.databinding.ItemIconContentBinding
import com.denchic45.kts.databinding.ItemSpecialtyBinding
import com.denchic45.kts.utils.viewBinding

class GroupAdapter : CustomAdapter<DomainModel, BaseViewHolder<DomainModel, *>> {
    private lateinit var specialtyItemClickListener: OnItemClickListener

    constructor() : super(DIFF_CALLBACK)
    constructor(itemClickListener: OnItemClickListener) : super(
        DIFF_CALLBACK,
        itemClickListener
    )

    constructor(
        itemClickListener: OnItemClickListener,
        itemLongClickListener: OnItemLongClickListener
    ) : super(
        DIFF_CALLBACK, itemClickListener, itemLongClickListener
    )

    fun setSpecialtyItemClickListener(specialtyItemClickListener: OnItemClickListener) {
        this.specialtyItemClickListener = specialtyItemClickListener
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): BaseViewHolder<DomainModel, *> {

        return when (viewType) {
            TYPE_GROUP -> {
                GroupHolder(
                    parent.viewBinding(ItemIconContentBinding::inflate),
                    onItemClickListener,
                    onItemLongClickListener
                ) as BaseViewHolder<DomainModel, *>
            }
            TYPE_SPECIALTY -> {
                SpecialtyHolder(
                    parent.viewBinding(ItemSpecialtyBinding::inflate),
                    specialtyItemClickListener
                ) as BaseViewHolder<DomainModel, *>
            }
            else -> throw IllegalStateException("Unexpected value: $viewType")
        }
    }

    override fun getItemViewType(position: Int): Int {
        val o = getItem(position)
        if (o is Group) {
            return TYPE_GROUP
        } else if (o is Specialty) {
            return TYPE_SPECIALTY
        }
        return 0
    }

    override fun onBindViewHolder(holder: BaseViewHolder<DomainModel, *>, position: Int) {
        holder.onBind(getItem(position))
    }

    internal class SpecialtyHolder(
        itemSpecialtyBinding: ItemSpecialtyBinding,
        itemClickListener: OnItemClickListener
    ) :
        BaseViewHolder<Specialty, ItemSpecialtyBinding>(itemSpecialtyBinding, itemClickListener) {
        private val tvName: TextView = itemView.findViewById(R.id.textView_specialty_name)
        private val ivArrow: ImageView = itemView.findViewById(R.id.imageView_specialty_arrow)
        private var isExpand = false
        override fun onBind(item: Specialty) {
            tvName.text = item.name
            itemView.setOnClickListener {
                expandArrow(if (isExpand) 0 else 180)
                onItemClickListener.onItemClick(bindingAdapterPosition)
                isExpand = !isExpand
            }
        }

        private fun expandArrow(rotate: Int) {
            ivArrow.animate().rotation(rotate.toFloat()).setDuration(300).start()
        }

    }

    class GroupHolder(
        itemIconContentBinding: ItemIconContentBinding,
        clickListener: OnItemClickListener,
        longClickListener: OnItemLongClickListener
    ) : BaseViewHolder<Group, ItemIconContentBinding>(
        itemIconContentBinding,
        clickListener,
        longClickListener
    ) {
        private val tvName: TextView = itemView.findViewById(R.id.tv_content)
        private val ivIcon: ImageView
        override fun onBind(item: Group) {
            tvName.text = item.name
        }

        init {
            ivIcon = itemView.findViewById(R.id.iv_ic)
            ivIcon.setImageDrawable(
                ContextCompat.getDrawable(
                    itemView.context,
                    R.drawable.ic_group
                )
            )
        }
    }

    companion object {
        const val TYPE_GROUP = 1
        const val TYPE_SPECIALTY = 2
        private val DIFF_CALLBACK: DiffUtil.ItemCallback<DomainModel> =
            object : DiffUtil.ItemCallback<DomainModel>() {
                override fun areItemsTheSame(oldItem: DomainModel, newItem: DomainModel): Boolean {
                    return oldItem.id == newItem.id
                }

                @SuppressLint("DiffUtilEquals")
                override fun areContentsTheSame(
                    oldItem: DomainModel,
                    newItem: DomainModel
                ): Boolean {
                    return oldItem === newItem
                }
            }
    }
}