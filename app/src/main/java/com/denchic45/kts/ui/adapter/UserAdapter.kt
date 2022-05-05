package com.denchic45.kts.ui.adapter

import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.denchic45.kts.R
import com.denchic45.kts.data.model.DomainModel
import com.denchic45.kts.data.model.domain.ListItem
import com.denchic45.kts.data.model.domain.User
import com.denchic45.kts.databinding.ItemHeaderBinding
import com.denchic45.kts.databinding.ItemIconContentBinding
import com.denchic45.kts.databinding.ItemUserBinding
import com.denchic45.kts.ui.adapter.ItemAdapter.IconItemHolder
import com.denchic45.kts.utils.viewBinding

class UserAdapter(
    override val onItemClickListener: OnItemClickListener = OnItemClickListener { },
    override val onItemLongClickListener: OnItemLongClickListener = OnItemLongClickListener { }
) : CustomAdapter<DomainModel, BaseViewHolder<DomainModel, *>>(DIFF_CALLBACK) {

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): BaseViewHolder<DomainModel, *> {
        return if (viewType == ItemAdapter.TYPE_VIEW) UserHolder(
          parent.viewBinding(ItemUserBinding::inflate), onItemClickListener, onItemLongClickListener
        ) as BaseViewHolder<DomainModel, *> else (if (viewType == ItemAdapter.TYPE_HEADER) HeaderHolder(
           parent.viewBinding(ItemHeaderBinding::inflate)
        ) as BaseViewHolder<DomainModel, *> else IconItemHolder(
          parent.viewBinding(ItemIconContentBinding::inflate),
            onItemClickListener
        )) as BaseViewHolder<DomainModel, *>
    }

    override fun onBindViewHolder(holder: BaseViewHolder<DomainModel, *>, position: Int) {
        holder.onBind(getItem(position))
    }

    override fun getItemViewType(position: Int): Int {
        val item = getItem(position)
        return if (item is User) {
            ItemAdapter.TYPE_VIEW
        } else {
            (item as ListItem?)!!.type
        }
    }

    class UserHolder(
        itemUserBinding: ItemUserBinding,
        clickListener: OnItemClickListener,
        longClickListener: OnItemLongClickListener
    ) : BaseViewHolder<User, ItemUserBinding>(itemUserBinding, clickListener, longClickListener) {
        private val ivAvatar: ImageView = itemView.findViewById(R.id.iv_avatar)
        private val tvName: TextView = itemView.findViewById(R.id.tv_name)
        private val tvAdditionally: TextView = itemView.findViewById(R.id.tv_user_role)
        override fun onBind(user: User) {
            Glide.with(itemView)
                .load(user.photoUrl)
                .transition(DrawableTransitionOptions.withCrossFade(100))
                .into(ivAvatar)
            tvName.text = String.format("%s %s", user.firstName, user.surname)
            tvAdditionally.visibility = View.VISIBLE
            when (user.role) {
//                User.DEPUTY_MONITOR -> tvAdditionally.text = "Зам. старосты"
//                User.CLASS_MONITOR -> tvAdditionally.text = "Староста"
                else -> tvAdditionally.visibility = View.GONE
            }
        }
    }

    companion object {
        private val DIFF_CALLBACK: DiffUtil.ItemCallback<DomainModel> =
            object : DiffUtil.ItemCallback<DomainModel>() {
                override fun areItemsTheSame(oldItem: DomainModel, newItem: DomainModel): Boolean {
                    return oldItem.id == newItem.id
                }

                override fun areContentsTheSame(
                    oldItem: DomainModel,
                    newItem: DomainModel
                ): Boolean {
                    return if (oldItem is User && newItem is User) {
                        oldItem.fullName == newItem.fullName && oldItem.role == newItem.role && oldItem.photoUrl == newItem.photoUrl
                    } else true
                }
            }
    }
}