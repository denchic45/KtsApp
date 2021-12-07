package com.denchic45.kts.ui.adapter

import android.content.res.ColorStateList
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CompoundButton
import android.widget.RadioButton
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.denchic45.kts.R
import com.denchic45.kts.data.model.domain.ListItem
import com.denchic45.kts.data.model.domain.onId
import com.denchic45.kts.databinding.ItemColorBinding
import com.denchic45.kts.ui.adapter.ColorPickerAdapter.ColorHolder
import com.denchic45.kts.utils.viewBinding
import java.util.*

class ColorPickerAdapter : RecyclerView.Adapter<ColorHolder>() {
    private var current = 0
    var list: List<ListItem> = ArrayList()
    private var itemClickListener: OnItemClickListener = OnItemClickListener {  }
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

    inner class ColorHolder(itemColorBinding: ItemColorBinding, itemClickListener: OnItemClickListener) :
        BaseViewHolder<ListItem,ItemColorBinding>(itemColorBinding) {
        private val rb: RadioButton = itemView.findViewById(R.id.rb_color)
        override fun onBind(item: ListItem) {
            rb.isChecked = current == adapterPosition
            item.color.onId {
                rb.backgroundTintList =
                    ColorStateList.valueOf(ContextCompat.getColor(itemView.context, it))
            }
        }

        init {
            rb.setOnCheckedChangeListener { buttonView: CompoundButton, isChecked: Boolean ->
                if (rb.isShown) {
                    current = adapterPosition
                    itemClickListener.onItemClick(current)
                }
            }
        }
    }
}