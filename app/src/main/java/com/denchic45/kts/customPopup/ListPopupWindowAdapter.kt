package com.denchic45.kts.customPopup

import android.content.Context
import android.content.res.ColorStateList
import android.content.res.Resources.NotFoundException
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Filter
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.core.widget.ImageViewCompat
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.denchic45.kts.R
import com.denchic45.kts.data.model.domain.ListItem
import com.denchic45.kts.data.model.ui.onId
import com.denchic45.kts.data.model.ui.onUrl
import com.denchic45.kts.databinding.ItemPopupContentBinding
import com.denchic45.kts.utils.viewBinding

class ListPopupWindowAdapter(context: Context, items: List<ListItem>) : ArrayAdapter<ListItem>(
    context, 0, items
) {
    private val list: MutableList<ListItem>
    private var nameFilter: Filter = object : Filter() {
        override fun performFiltering(constraint: CharSequence?): FilterResults? {
            return null
        }

        override fun publishResults(constraint: CharSequence?, results: FilterResults?) {}
        override fun convertResultToString(resultValue: Any): String {
            val title = (resultValue as ListItem).title
            val resId =
                getContext().resources.getIdentifier(title, "string", getContext().packageName)
            return if (resId == 0) {
                title
            } else getContext().getString(resId)
        }
    }

    fun updateList(items: List<ListItem>) {
        list.clear()
        list.addAll(items)
        notifyDataSetChanged()
    }

    override fun getCount(): Int = list.size

    override fun getFilter(): Filter = nameFilter

    override fun getItem(position: Int): ListItem = list[position]

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        var convertView = convertView
        if (convertView == null) {
            val item = getItem(position)
            if (item.hasIcon()) {
                when (item.type) {
                    TYPE_NONE -> convertView = LayoutInflater.from(parent.context)
                        .inflate(R.layout.item_popup_icon_content, parent, false)
                    TYPE_AVATAR -> convertView = LayoutInflater.from(parent.context)
                        .inflate(R.layout.item_popup_avatar_content, parent, false)
                }
                val tItemWithIconHolder = ItemWithIconHolder(convertView!!)
                tItemWithIconHolder.onBind(item)
            } else {
                convertView = parent.viewBinding(ItemPopupContentBinding::inflate).root
                ItemHolder(convertView).onBind(item)
            }
        }
        return convertView
    }

    class ItemWithIconHolder(private val convertView: View) {
        private val tvTitle: TextView = convertView.findViewById(R.id.tv_name)
        private val ivIcon: ImageView = convertView.findViewById(R.id.iv_icon)
        fun onBind(item: ListItem) {
            val context = convertView.context
            try {
                tvTitle.setText(
                    context.resources.getIdentifier(
                        item.title,
                        "string",
                        context.packageName
                    )
                )
            } catch (e: NotFoundException) {
                tvTitle.text = item.title
            }

            item.icon?.onId {
                ivIcon.setImageDrawable(
                    ContextCompat.getDrawable(
                        convertView.context,
                        it
                    )
                )
            }?.onUrl {
                val iconResId = context.resources.getIdentifier(it, "drawable", context.packageName)

                if (iconResId != 0) {
                    ivIcon.setImageDrawable(
                        ContextCompat.getDrawable(convertView.context, iconResId)
                    )
                } else {
                    Glide.with(context)
                        .load(it)
                        .transition(DrawableTransitionOptions.withCrossFade(100))
                        .into(ivIcon)
                }
            }

            item.color?.onUrl {
                ColorStateList.valueOf(
                    convertView.resources
                        .getIdentifier(it, "color", convertView.context.packageName)
                )
            }?.onId {
                ImageViewCompat.setImageTintList(
                    ivIcon,
                    ColorStateList.valueOf(it)
                )
            }

            if (!item.enable) {
                convertView.isEnabled = false
                convertView.alpha = 0.5f
            }
        }

    }

    class ItemHolder(private val convertView: View) {
        private val tvTitle: TextView = convertView.findViewById(R.id.tv_name)
        fun onBind(item: ListItem) {
            val context = convertView.context
            try {
                tvTitle.setText(
                    context.resources.getIdentifier(
                        item.title,
                        "string",
                        context.packageName
                    )
                )
            } catch (e: NotFoundException) {
                tvTitle.text = item.title
            }
            if (!item.enable) {
                convertView.isEnabled = false
                convertView.alpha = 0.5f
            }
        }

    }

    companion object {
        const val TYPE_NONE = 0
        const val TYPE_AVATAR = 1
    }

    init {
        list = ArrayList(items)
    }
}