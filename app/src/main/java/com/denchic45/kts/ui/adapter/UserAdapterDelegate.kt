package com.denchic45.kts.ui.adapter

import android.view.View
import android.view.ViewGroup
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.denchic45.kts.data.model.domain.User
import com.denchic45.kts.databinding.ItemUserBinding
import com.denchic45.kts.utils.viewBinding
import com.denchic45.widget.extendedAdapter.ListItemAdapterDelegate

class UserAdapterDelegate : ListItemAdapterDelegate<User, UserAdapterDelegate.UserHolder>() {

    override fun isForViewType(item: Any): Boolean = item is User

    override fun onBindViewHolder(item: User, holder: UserHolder) {
        holder.onBind(item)
    }

    override fun onCreateViewHolder(parent: ViewGroup): UserHolder {
        return UserHolder(parent.viewBinding(ItemUserBinding::inflate))
    }

    class UserHolder(itemUserBinding: ItemUserBinding) :
        BaseViewHolder<User, ItemUserBinding>(itemUserBinding) {

        override fun onBind(item: User) {
            with(binding) {
                Glide.with(itemView)
                    .load(item.photoUrl)
                    .transition(DrawableTransitionOptions.withCrossFade(100))
                    .into(ivAvatar)
                tvName.text = String.format("%s %s", item.firstName, item.surname)
                tvUserRole.visibility = View.VISIBLE
                when (item.role) {
                    User.DEPUTY_MONITOR -> tvUserRole.text = "Зам. старосты"
                    User.CLASS_MONITOR -> tvUserRole.text = "Староста"
                    else -> tvUserRole.visibility = View.GONE
                }
            }
        }
    }
}