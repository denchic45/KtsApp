package com.denchic45.kts.ui.adapter

import android.view.ViewGroup
import androidx.annotation.DrawableRes
import com.bumptech.glide.Glide
import com.denchic45.kts.data.model.DomainModel
import com.denchic45.kts.databinding.ItemContentSwitchBinding
import com.denchic45.kts.databinding.ItemIconContent2Binding
import com.denchic45.kts.utils.viewBinding
import com.denchic45.widget.extendedAdapter.DelegationAdapterDsl
import com.denchic45.widget.extendedAdapter.DelegationAdapterExtended
import com.denchic45.widget.extendedAdapter.ListItemAdapterDelegate

fun preferenceAdapter(buildDelegationAdapterDsl: DelegationAdapterDsl.DelegationAdapterBuilder.() -> Unit): DelegationAdapterExtended {
    val delegationAdapterBuilder = DelegationAdapterDsl.DelegationAdapterBuilder()
    delegationAdapterBuilder.delegates(
        PreferenceAdapterDelegate(), PreferenceSwitchAdapterDelegate()
    )
    delegationAdapterBuilder.changePayload = { old, new ->
        when {
            (old is PreferenceContentItem && new is PreferenceContentItem) -> {
                setOfNotNull<Any>(
                    if (old.title != new.title) "ChangeTitle" else null,
                    if (old.progress != new.progress) "ChangeProgress" else null,
                ).run {
                    ifEmpty { null }
                }

            }
            (old is PreferenceSwitchItem && new is PreferenceSwitchItem) -> {
                null
            }
            else -> throw IllegalStateException()
        }
    }
    return delegationAdapterBuilder.apply(buildDelegationAdapterDsl).build()
}

class PreferenceAdapterDelegate :
    ListItemAdapterDelegate<PreferenceContentItem, PreferenceAdapterDelegate.PreferenceItemHolder>() {

    override fun isForViewType(item: Any): Boolean {
        return item is PreferenceContentItem
    }

    override fun onBindViewHolder(item: PreferenceContentItem, holder: PreferenceItemHolder) {
        holder.onBind(item)
    }

    override fun onBindViewHolder(
        item: PreferenceContentItem,
        holder: PreferenceItemHolder,
        payload: Any
    ) {
        (payload as Set<String>).forEach {
            when (it) {
                "ChangeTitle" -> {
                    holder.changeTitle(item.title)
                }
                "ChangeProgress" -> {
                    holder.switchImageLoading(item.progress)
                }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup): PreferenceItemHolder {
        return PreferenceItemHolder(parent.viewBinding(ItemIconContent2Binding::inflate))
    }

    class PreferenceItemHolder(
        itemIconContent2Binding: ItemIconContent2Binding
    ) :
        BaseViewHolder<PreferenceContentItem, ItemIconContent2Binding>(itemIconContent2Binding) {
        override fun onBind(item: PreferenceContentItem) {
            with(binding) {
                tvName.text = item.title
                Glide.with(itemView)
                    .load(item.icon)
                    .into(ivIcon)
            }

            if (item.progress) {
                showLoading()
            } else {
                showImage()
            }
        }

        fun switchImageLoading(loading: Boolean) {
            if (loading) showLoading() else showImage()
        }

        private fun showLoading() {
            if (itemView.isEnabled) {
                binding.vsIc.post { binding.vsIc.displayedChild = 1 }
                itemView.isEnabled = false
            }
        }

        private fun showImage() {
            if (!itemView.isEnabled) {
                binding.vsIc.post { binding.vsIc.displayedChild = 0 }
                itemView.isEnabled = true
            }
        }

        fun changeTitle(title: String) {
            binding.tvName.text = title
        }
    }
}

class PreferenceSwitchAdapterDelegate :
    ListItemAdapterDelegate<PreferenceSwitchItem, PreferenceSwitchAdapterDelegate.PreferenceSwitchItemHolder>() {
    override fun isForViewType(item: Any): Boolean {
        return item is PreferenceSwitchItem
    }

    override fun onBindViewHolder(
        item: PreferenceSwitchItem,
        holder: PreferenceSwitchItemHolder
    ) {
        holder.onBind(item)
    }

    override fun onCreateViewHolder(parent: ViewGroup): PreferenceSwitchItemHolder {
        return PreferenceSwitchItemHolder(parent.viewBinding(ItemContentSwitchBinding::inflate))
    }

    class PreferenceSwitchItemHolder(itemContentSwitchBinding: ItemContentSwitchBinding) :
        BaseViewHolder<PreferenceSwitchItem, ItemContentSwitchBinding>(itemContentSwitchBinding) {
        override fun onBind(item: PreferenceSwitchItem) {
            with(binding) {
                tvName.text = item.title
                sw.isChecked = item.checked
            }
        }
    }
}

abstract class PreferenceItem : DomainModel

data class PreferenceContentItem(
    override var id: String,
    val title: String,
    @DrawableRes
    val icon: Int,
    val progress: Boolean = false
) : PreferenceItem()

data class PreferenceSwitchItem(
    override var id: String,
    val title: String,
    val checked: Boolean = false
) : PreferenceItem()