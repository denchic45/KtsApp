package com.denchic45.kts.ui.adapter

import android.content.res.ColorStateList
import android.view.ViewGroup
import android.widget.RadioButton
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.denchic45.kts.R
import com.denchic45.kts.databinding.ItemColorBinding
import com.denchic45.kts.ui.adapter.ColorPickerAdapter.ColorHolder
import com.denchic45.kts.utils.viewBinding

class ColorPickerAdapter : RecyclerView.Adapter<ColorHolder>() {
    private var current = 0
    var list: List<Int> = listOf()
    private var itemClickListener: OnItemClickListener = OnItemClickListener { }
    fun setItemClickListener(itemClickListener: OnItemClickListener) {
        this.itemClickListener = itemClickListener
    }

    fun setCurrent(current: Int) {
        this.current = current
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ColorHolder {
        return ColorHolder(parent.viewBinding(ItemColorBinding::inflate), itemClickListener)
    }

    override fun onBindViewHolder(holder: ColorHolder, position: Int) {
        holder.onBind(list[position])
    }

    override fun getItemCount(): Int {
        return list.size
    }

    inner class ColorHolder(
        itemColorBinding: ItemColorBinding,
        itemClickListener: OnItemClickListener
    ) :
        BaseViewHolder<Int, ItemColorBinding>(itemColorBinding) {
        private val rb: RadioButton = itemView.findViewById(R.id.rb_color)
        override fun onBind(item: Int) {
            rb.isChecked = current == absoluteAdapterPosition
            rb.backgroundTintList =
                ColorStateList.valueOf(ContextCompat.getColor(itemView.context, item))
        }

        init {
            rb.setOnCheckedChangeListener { _, _ ->
                if (rb.isShown) {
                    current = absoluteAdapterPosition
                    itemClickListener.onItemClick(current)
                }
            }
        }
    }
}