package com.denchic45.kts.customPopup

import android.content.Context
import android.content.res.ColorStateList
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Filter
import androidx.core.content.ContextCompat
import androidx.core.widget.ImageViewCompat
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.denchic45.kts.data.model.domain.OptionItem
import com.denchic45.kts.data.model.ui.UiText
import com.denchic45.kts.data.model.ui.onId
import com.denchic45.kts.data.model.ui.onString
import com.denchic45.kts.data.model.ui.onUrl
import com.denchic45.kts.databinding.ItemPopupIconContentBinding
import com.denchic45.kts.util.viewBinding

class OptionsPopupAdapter(context: Context, items: List<OptionItem>) : ArrayAdapter<OptionItem>(
    context, 0, items
) {
    private val list: MutableList<OptionItem>
    private var nameFilter: Filter = object : Filter() {
        override fun performFiltering(constraint: CharSequence?): FilterResults? {
            return null
        }

        override fun publishResults(constraint: CharSequence?, results: FilterResults?) {}

        override fun convertResultToString(resultValue: Any): String {
            return when (val title = (resultValue as OptionItem).title) {
                is UiText.StringText -> title.value
                is UiText.IdText -> getContext().getString(title.value)
                else -> throw IllegalStateException()
            }
        }
    }

    fun updateList(items: List<OptionItem>) {
        list.clear()
        list.addAll(items)
        notifyDataSetChanged()
    }

    override fun getCount(): Int = list.size

    override fun getFilter(): Filter = nameFilter

    override fun getItem(position: Int): OptionItem = list[position]

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        var convertView = convertView
        if (convertView == null) {
            val item = getItem(position)
            val viewBinding = parent.viewBinding(ItemPopupIconContentBinding::inflate)
            convertView = viewBinding.root
            ItemWithIconHolder(viewBinding).onBind(item)
        }
        return convertView
    }

    class ItemWithIconHolder(
        private val itemPopupIconContentBinding: ItemPopupIconContentBinding
    ) {

        fun onBind(item: OptionItem) {
            with(itemPopupIconContentBinding) {
                val context = root.context
                item.title
                    .onString {
                        tvName.text = it
                    }
                    .onId {
                        tvName.setText(it)
                    }

                item.icon?.onId {
                    ivIcon.visibility = View.VISIBLE
                    ivIcon.setImageDrawable(
                        ContextCompat.getDrawable(context, it)
                    )
                }?.onUrl {
                    ivIcon.visibility = View.VISIBLE
                    Glide.with(context)
                        .load(it)
                        .transition(DrawableTransitionOptions.withCrossFade(100))
                        .into(ivIcon)
                } ?: run {
                    ivIcon.visibility = View.GONE
                }

                item.color?.onUrl {
                    ColorStateList.valueOf(
                        context.resources
                            .getIdentifier(it, "color", context.packageName)
                    )
                }?.onId {
                    ImageViewCompat.setImageTintList(
                        ivIcon,
                        ColorStateList.valueOf(it)
                    )
                }

                if (!item.enable) {
                    root.isEnabled = false
                    root.alpha = 0.5f
                }
            }
        }

    }

    init {
        list = ArrayList(items)
    }
}