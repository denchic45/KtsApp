package com.denchic45.kts.ui.adapter

import android.view.View
import android.view.ViewGroup
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.denchic45.kts.data.model.ui.UserItem
import com.denchic45.kts.databinding.ItemUserBinding
import com.denchic45.kts.utils.viewBinding
import com.denchic45.widget.extendedAdapter.ListItemAdapterDelegate

class UserAdapterDelegate : ListItemAdapterDelegate<UserItem, UserAdapterDelegate.UserHolder>() {

    override fun isForViewType(item: Any): Boolean = item is UserItem

    override fun onBindViewHolder(item: UserItem, holder: UserHolder) {
        holder.onBind(item)
    }

    override fun onCreateViewHolder(parent: ViewGroup): UserHolder {
        return UserHolder(parent.viewBinding(ItemUserBinding::inflate))
    }

    class UserHolder(itemUserBinding: ItemUserBinding) :
        BaseViewHolder<UserItem, ItemUserBinding>(itemUserBinding) {

        override fun onBind(item: UserItem) {
            with(binding) {
                Glide.with(itemView)
                    .load(item.photoUrl)
                    .transition(DrawableTransitionOptions.withCrossFade(100))
                    .into(ivAvatar)

                tvName.text = item.title

                item.subtitle?.let {
                    tvUserRole.visibility = View.VISIBLE
                    tvUserRole.text = it
                } ?: run {
                    tvUserRole.visibility = View.GONE
                }
            }
        }
    }
}