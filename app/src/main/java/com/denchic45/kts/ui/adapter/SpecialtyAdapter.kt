package com.denchic45.kts.ui.adapter

import android.annotation.SuppressLint
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import com.denchic45.kts.R
import com.denchic45.kts.data.model.domain.Specialty
import com.denchic45.kts.databinding.ItemIconContentBinding
import com.denchic45.kts.utils.viewBinding

class SpecialtyAdapter(itemClickListener: OnItemClickListener) :
    CustomAdapter<Specialty, SpecialtyAdapter.SpecialtyHolder>(
        DIFF_CALLBACK, itemClickListener
    ) {
    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): SpecialtyHolder {
        return SpecialtyHolder(parent.viewBinding(ItemIconContentBinding::inflate), onItemClickListener)
    }

    override fun onBindViewHolder(
        holder: SpecialtyHolder,
        position: Int
    ) {
        holder.onBind(getItem(position))
    }

    class SpecialtyHolder(
        itemIconContentBinding: ItemIconContentBinding,
        itemClickListener: OnItemClickListener
    ) :
        BaseViewHolder<Specialty, ItemIconContentBinding>(
            itemIconContentBinding,
            itemClickListener
        ) {
        private val tvName: TextView = itemView.findViewById(R.id.tv_name)
        private val ivIcon: ImageView = itemView.findViewById(R.id.iv_icon)
        override fun onBind(item: Specialty) {
            tvName.text = item.name
        }

        init {
            ivIcon.setImageDrawable(
                ContextCompat.getDrawable(
                    itemView.context,
                    R.drawable.ic_specialty
                )
            )
        }
    }

    companion object {
        private val DIFF_CALLBACK: DiffUtil.ItemCallback<Specialty> =
            object : DiffUtil.ItemCallback<Specialty>() {
                override fun areItemsTheSame(oldItem: Specialty, newItem: Specialty): Boolean {
                    return oldItem.id == newItem.id
                }

                @SuppressLint("DiffUtilEquals")
                override fun areContentsTheSame(
                    oldItem: Specialty,
                    newItem: Specialty
                ): Boolean {
                    return oldItem === newItem
                }
            }
    }
}