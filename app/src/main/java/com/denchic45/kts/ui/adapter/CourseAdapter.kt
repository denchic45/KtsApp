package com.denchic45.kts.ui.adapter

import android.animation.ArgbEvaluator
import android.animation.ObjectAnimator
import android.graphics.Color
import android.graphics.drawable.PictureDrawable
import android.view.ViewGroup
import android.view.animation.LinearInterpolator
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.denchic45.SvgColorListener
import com.denchic45.kts.R
import com.denchic45.kts.data.model.domain.CourseInfo
import com.denchic45.kts.data.model.domain.Subject
import com.denchic45.kts.data.model.domain.User
import com.denchic45.kts.databinding.ItemCourseBinding
import com.denchic45.kts.glideSvg.GlideApp
import com.denchic45.kts.utils.viewBinding

class CourseAdapter(
    val onItemClickListener: OnItemClickListener = OnItemClickListener { },
    val onItemLongClickListener: OnItemLongClickListener = OnItemLongClickListener { }
) : ListAdapter<CourseInfo, CourseAdapter.CourseHolder>(
    DIFF_CALLBACK
) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CourseHolder {
        return CourseHolder(
            parent.viewBinding(ItemCourseBinding::inflate),
            onItemClickListener,
            onItemLongClickListener
        )
    }

    override fun onBindViewHolder(holder: CourseHolder, position: Int) {
        holder.onBind(getItem(position))
    }

    class CourseHolder(
        itemCourseBinding: ItemCourseBinding,
        listener: OnItemClickListener,
        longItemClickListener: OnItemLongClickListener
    ) : BaseViewHolder<CourseInfo, ItemCourseBinding>(
        itemCourseBinding,
        listener,
        longItemClickListener
    ) {
        private val ivSubjectIcon: ImageView = binding.ivSubjectIcon
        private val ivTeacherAvatar: ImageView
        private val tvSubjectName: TextView
        private val tvTeacherFullName: TextView
        override fun onBind(item: CourseInfo) {
            val subject: Subject = item.subject
            val teacher: User = item.teacher
            val resColor: Int = itemView.context
                .resources
                .getIdentifier(subject.colorName, "color", itemView.context.packageName)
            GlideApp.with(itemView.context)
                .`as`(PictureDrawable::class.java)
                .transition(DrawableTransitionOptions.withCrossFade())
                .listener(SvgColorListener(ivSubjectIcon, resColor, itemView.context))
                .load(subject.iconUrl)
                .into(ivSubjectIcon)
            Glide.with(itemView.context)
                .load(teacher.photoUrl)
                .transition(DrawableTransitionOptions.withCrossFade(100))
                .into(ivTeacherAvatar)
            tvSubjectName.text = item.name
            tvTeacherFullName.text = teacher.fullName
        }

        fun setSelect(select: Boolean) {
            val colorLightBlue = ContextCompat.getColor(itemView.context, R.color.light_blue)
            val a: ObjectAnimator = if (select) {
                ObjectAnimator.ofInt(
                    itemView,
                    "backgroundColor",
                    Color.WHITE,
                    colorLightBlue
                )
            } else {
                ObjectAnimator.ofInt(
                    itemView,
                    "backgroundColor",
                    colorLightBlue,
                    Color.WHITE
                )
            }
            a.interpolator = LinearInterpolator()
            a.duration = 200
            a.setEvaluator(ArgbEvaluator())
            a.start()
        }

        init {
            ivTeacherAvatar = binding.ivTeacherAvatar
            tvSubjectName = binding.tvSubjectName
            tvTeacherFullName = binding.tvTeacherFullName
        }
    }

    companion object {
        private val DIFF_CALLBACK: DiffUtil.ItemCallback<CourseInfo> =
            object : DiffUtil.ItemCallback<CourseInfo>() {
                override fun areItemsTheSame(oldItem: CourseInfo, newItem: CourseInfo): Boolean {
                    return oldItem.subject.id == newItem.subject.id
                }

                override fun areContentsTheSame(oldItem: CourseInfo, newItem: CourseInfo): Boolean {
                    return oldItem == newItem
                }
            }
    }
}