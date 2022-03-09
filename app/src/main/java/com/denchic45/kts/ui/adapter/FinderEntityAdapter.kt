package com.denchic45.kts.ui.adapter

import android.content.Context
import android.content.res.ColorStateList
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.graphics.drawable.StateListDrawable
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.denchic45.kts.R
import com.denchic45.kts.data.model.domain.ListItem
import com.denchic45.kts.data.model.domain.onId
import com.denchic45.kts.databinding.ItemFinderEntityBinding
import com.denchic45.kts.ui.adapter.FinderEntityAdapter.FinderEntityHolder
import com.denchic45.kts.utils.viewBinding

class FinderEntityAdapter :
    ListAdapter<ListItem, FinderEntityHolder>(object : DiffUtil.ItemCallback<ListItem>() {
        override fun areItemsTheSame(oldItem: ListItem, newItem: ListItem): Boolean {
            return false
        }

        override fun areContentsTheSame(oldItem: ListItem, newItem: ListItem): Boolean {
            return false
        }
    }) {
    private var itemClickListener: OnItemClickListener = OnItemClickListener { }
    private var lastSelectItem = RecyclerView.NO_POSITION
    private lateinit var recyclerView: RecyclerView
    fun setItemClickListener(itemClickListener: OnItemClickListener) {
        this.itemClickListener = itemClickListener
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FinderEntityHolder {
        return FinderEntityHolder(
          parent.viewBinding(ItemFinderEntityBinding::inflate),
            itemClickListener
        )
    }

    override fun onBindViewHolder(holder: FinderEntityHolder, position: Int) {
        holder.onBind(getItem(position))
        holder.itemView.isSelected = lastSelectItem == position
    }

    override fun onBindViewHolder(holder: FinderEntityHolder, position: Int, payloads: List<Any>) {
        if (payloads.isEmpty()) onBindViewHolder(holder, position) else {
            for (payload in payloads) {
                if (payload === PAYLOAD.ENABLE_SELECT) {
                    recyclerView.post { holder.itemView.isSelected = true }
                } else if (payload === PAYLOAD.DISABLE_SELECT) {
                    recyclerView.post { holder.itemView.isSelected = false }
                }
            }
        }
    }

    override fun onAttachedToRecyclerView(recyclerView: RecyclerView) {
        super.onAttachedToRecyclerView(recyclerView)
        this.recyclerView = recyclerView
    }

    fun selectItem(position: Int) {
        notifyItemChanged(lastSelectItem, PAYLOAD.DISABLE_SELECT)
        notifyItemChanged(position, PAYLOAD.ENABLE_SELECT)
        lastSelectItem = position
    }

    private enum class PAYLOAD {
        DISABLE_SELECT, ENABLE_SELECT
    }

    class FinderEntityHolder(
        itemFinderEntityBinding: ItemFinderEntityBinding,
        itemClickListener: OnItemClickListener
    ) :
        BaseViewHolder<ListItem, ItemFinderEntityBinding>(
            itemFinderEntityBinding,
            itemClickListener
        ) {
        private val tvTitle: TextView = itemView.findViewById(R.id.textView_title)
        private val ivIcon: ImageView = itemView.findViewById(R.id.iv_icon)
        private val context: Context = itemView.context
        override fun onBind(item: ListItem) {
            item.color.onId {
//                itemView.background = AppCompatResources.getDrawable(context,R.color.bg_blue)
//                ImageViewCompat.setImageTintList(ivIcon, ContextCompat.getColorStateList(context,R.color.blu))
//                tvTitle.setTextColor(ContextCompat.getColorStateList(context,R.color.content_blue_highlight))
            }
            item.icon.onId {
                ivIcon.setImageDrawable(ContextCompat.getDrawable(context,it))
            }

            tvTitle.text = item.title
            itemView.setOnClickListener {
                if (!itemView.isSelected) onItemClickListener.onItemClick(absoluteAdapterPosition)
            }
        }

        private fun createBackgroundSelector(color: Int): StateListDrawable {
            val shape = ContextCompat.getDrawable(context, R.drawable.shape_outline_btn)
            val selectedShape = shape!!.constantState!!.newDrawable().mutate() as GradientDrawable
            selectedShape.setColor(color)
            val res = StateListDrawable()
            res.setExitFadeDuration(100)
            res.setEnterFadeDuration(100)
            res.addState(intArrayOf(android.R.attr.state_selected), selectedShape)
            res.addState(intArrayOf(), shape)
            return res
        }

        private fun createIconSelector(color: Int): ColorStateList {
            val states = arrayOf(
                intArrayOf(android.R.attr.state_selected),
                intArrayOf(-android.R.attr.state_selected)
            )
            val colors = intArrayOf(Color.WHITE, color)
            return ColorStateList(states, colors)
        }

        private fun createTitleSelector(): ColorStateList {
            val states = arrayOf(
                intArrayOf(android.R.attr.state_selected),
                intArrayOf(-android.R.attr.state_selected)
            )
            val colors = intArrayOf(Color.WHITE, Color.BLACK)
            return ColorStateList(states, colors)
        }

    }
}