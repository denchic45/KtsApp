package com.denchic45.kts.ui.adapter

import android.annotation.SuppressLint
import android.content.res.ColorStateList
import android.graphics.Color
import android.graphics.drawable.PictureDrawable
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.CompoundButton
import android.widget.ImageView
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import androidx.transition.AutoTransition
import androidx.transition.Transition
import androidx.transition.TransitionManager
import androidx.transition.TransitionSet
import androidx.viewbinding.ViewBinding
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.denchic45.SvgColorListener
import com.denchic45.kts.LessonTimeCalculator
import com.denchic45.kts.R
import com.denchic45.kts.data.model.DomainModel
import com.denchic45.kts.data.model.domain.*
import com.denchic45.kts.data.model.room.EventEntity
import com.denchic45.kts.databinding.*
import com.denchic45.kts.glideSvg.GlideApp
import com.denchic45.kts.ui.adapter.EventAdapter.OnCreateLessonClickListener
import com.denchic45.kts.ui.adapter.EventAdapter.OnEditEventItemClickListener
import com.denchic45.kts.utils.Dimensions
import com.denchic45.kts.utils.viewBinding
import com.denchic45.widget.transition.Elevation
import com.denchic45.widget.transition.Rotation
import java.util.*

class EventAdapter(
    private val lessonTime: Int,
    private val groupVisibility: Boolean,
    var onEditEventItemClickListener: OnEditEventItemClickListener = OnEditEventItemClickListener {},
    var onLessonItemClickListener: OnLessonItemClickListener = object :
        OnLessonItemClickListener() {},
    var onCreateLessonClickListener: OnCreateLessonClickListener = OnCreateLessonClickListener {},
    var onItemTouchListener: OnItemTouchListener = OnItemTouchListener { }
) :
    ListAdapter<DomainModel, BaseViewHolder<DomainModel, *>>(
        DIFF_CALLBACK
    ) {
    private var enableEditMode = false

    fun enableEditMode() {
        enableEditMode = true
        Handler(Looper.getMainLooper()).postDelayed({
            notifyItemRangeChanged(
                0,
                itemCount,
                PAYLOAD.ENABLE_EDIT_MODE
            )
        }, 300)
    }

    fun disableEditMode() {
        enableEditMode = false
        Handler(Looper.getMainLooper()).postDelayed({
            notifyItemRangeChanged(
                0,
                itemCount,
                PAYLOAD.DISABLE_EDIT_MODE
            )
        }, 300)
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): BaseViewHolder<DomainModel, *> {
        when (viewType) {
            TYPE_LESSON -> {
                return LessonHolder(
                    parent.viewBinding(ItemLessonBinding::inflate),
                    onLessonItemClickListener,
                    onEditEventItemClickListener,
                    onItemTouchListener,
                    lessonTime,
                    groupVisibility
                ) as BaseViewHolder<DomainModel, *>
            }
            TYPE_HEADER -> {
                return HeaderHolder(parent.viewBinding(ItemHeaderBinding::inflate)) as BaseViewHolder<DomainModel, *>
            }
            TYPE_CREATE -> {
                return LessonCreateHolder(
                    parent.viewBinding(ItemIconContentBinding::inflate),
                    onCreateLessonClickListener
                ) as BaseViewHolder<DomainModel, *>
            }
            TYPE_EMPTY -> {
                return EmptyEventHolder(
                    parent.viewBinding(ItemEventEmptyBinding::inflate),
                    onEditEventItemClickListener,
                    onItemTouchListener,
                    lessonTime
                ) as BaseViewHolder<DomainModel, *>
            }
            TYPE_SIMPLE -> {
                return SimpleEventHolder(
                    parent.viewBinding(ItemEventBinding::inflate),
                    onEditEventItemClickListener,
                    onItemTouchListener,
                    lessonTime
                ) as BaseViewHolder<DomainModel, *>
            }
            else -> throw IllegalStateException(viewType.toString())
        }
    }

    override fun onBindViewHolder(holder: BaseViewHolder<DomainModel, *>, position: Int) {
        if (holder is EventHolder) {
            (holder as EventHolder).setEnableEditMode(enableEditMode)
        }
        holder.onBind(getItem(position))
    }

    override fun getItemViewType(position: Int): Int {
        if (getItem(position) is Event) {
            if ((getItem(position) as Event?)!!.details.type == EventEntity.TYPE.LESSON) return TYPE_LESSON
            if ((getItem(position) as Event?)!!.details.type == EventEntity.TYPE.SIMPLE) return TYPE_SIMPLE
            if ((getItem(position) as Event?)!!.details.type == EventEntity.TYPE.EMPTY) return TYPE_EMPTY
        } else if (getItem(position) is ListItem) {
            return (getItem(position) as ListItem?)!!.type
        }
        throw IllegalStateException()
    }

    override fun onBindViewHolder(
        holder: BaseViewHolder<DomainModel, *>,
        position: Int,
        payloads: List<Any>
    ) {
        if (payloads.isEmpty()) {
            onBindViewHolder(holder, position)
        } else {
            for (payload in payloads) {
                if (holder is EventHolder) {
                    if (holder as BaseViewHolder<Event, ItemLessonBinding> is LessonHolder) {
                        if (payload === PAYLOAD.EXPAND) {
                            (holder as LessonHolder).expandContent()
                        }
                    }
                    when {
                        payload === PAYLOAD.ENABLE_EDIT_MODE -> {
                            holder.enableEditMode()
                        }
                        payload === PAYLOAD.DISABLE_EDIT_MODE -> {
                            holder.disableEditMode()
                        }
                        payload === PAYLOAD.UPDATE_ORDER -> {
                            holder.updateOrder((getItem(position) as Event?)!!.order)
                        }
                    }
                }
            }
        }
    }

    enum class PAYLOAD {
        ENABLE_EDIT_MODE, DISABLE_EDIT_MODE, EXPAND, UPDATE_ORDER
    }

    fun interface OnCreateLessonClickListener {
        fun onLessonCreateClick(position: Int)
    }

    fun interface OnEditEventItemClickListener {
        fun onLessonEditClick(position: Int)
    }

    fun interface OnLessonHomeworkItemClickListener {
        fun onHomeworkChecked(checked: Boolean)
    }

    fun interface OnLessonItemClickListener2 {
        fun onLessonClick(position: Int)
    }

    abstract class OnLessonItemClickListener : OnItemClickListener {
        override fun onItemClick(position: Int) {}
        open fun onHomeworkChecked(checked: Boolean) {}
    }

    abstract class EventHolder<VB : ViewBinding> @SuppressLint("ClickableViewAccessibility") constructor(
        itemEventBinding: VB,
        onEditEventItemClickListener: OnEditEventItemClickListener?,
        itemTouchListener: OnItemTouchListener?,
        private val lessonTime: Int
    ) : BaseViewHolder<Event, VB>(itemEventBinding) {
        protected val ivEdit: ImageView = itemView.findViewById(R.id.iv_lesson_edit)
        private val ivDrag: ImageView = itemView.findViewById(R.id.iv_lesson_drag)
        private val tvOrder: TextView =
            itemView.findViewById(R.id.textView_lesson_order)
        protected val tvTime: TextView = itemView.findViewById(R.id.textView_lesson_time)
        protected val ivIcon: ImageView? =
            itemView.findViewById(R.id.imageView_lesson_icon)
        protected val tvTitle: TextView = itemView.findViewById(R.id.textView_lesson_name)
        override fun onBind(item: Event) {
            tvTime.text = LessonTimeCalculator().getCalculatedTime(item.order, lessonTime)
            tvOrder.text = item.order.toString()
        }

        fun setEnableEditMode(enableEditMode: Boolean) {
            if (enableEditMode) {
                enableEditMode()
            } else {
                disableEditMode()
            }
        }


        fun setTitle(title: String?) {
            tvTitle.text = title
        }

        fun setIcon(url: String?, color: String?) {
            ivIcon?.let {
                GlideApp.with(itemView.context)
                    .`as`(PictureDrawable::class.java)
                    .transition(DrawableTransitionOptions.withCrossFade())
                    .listener(
                        SvgColorListener(
                            ivIcon,
                            itemView.resources
                                .getIdentifier(color, "color", itemView.context.packageName),
                            itemView.context
                        )
                    )
                    .load(url)
                    .into(it)
            }

        }

        fun enableEditMode() {
            ivEdit.visibility = View.VISIBLE
            ivDrag.visibility = View.VISIBLE
        }

        fun disableEditMode() {
            ivEdit.visibility = View.GONE
            ivDrag.visibility = View.GONE
        }

        fun updateOrder(order: Int) {
            tvOrder.text = order.toString()
        }

        init {
            ivEdit.setOnClickListener {
                onEditEventItemClickListener!!.onLessonEditClick(absoluteAdapterPosition)
            }
            ivDrag.setOnTouchListener { _: View?, event: MotionEvent ->
                if (event.action == MotionEvent.ACTION_DOWN) {
                    itemTouchListener!!.onTouch(this)
                }
                false
            }
        }
    }

    class LessonHolder(
        itemLessonBinding: ItemLessonBinding,
        private val lessonItemClickListener: OnLessonItemClickListener?,
        onEditEventItemClickListener: OnEditEventItemClickListener?,
        itemTouchListener: OnItemTouchListener?,
        lessonTime: Int,
        private val groupVisibility: Boolean
    ) : EventHolder<ItemLessonBinding>(
        itemLessonBinding,
        onEditEventItemClickListener,
        itemTouchListener,
        lessonTime
    ) {
        private val ivArrow: ImageView = binding.llLessonHeader.ivLessonExpand
        private var color = 0
        private var task: Task? = null
        private var subject: Subject? = null
        private var group: Group? = null
        private lateinit var event: Event
        private val lessonExpandableContentBinding = binding.llLessonExpandableContent

        override fun onBind(item: Event) {
            super.onBind(item)
            event = item
            val lesson: Lesson = item.details as Lesson
            task = lesson.task
            subject = lesson.subject
            group = item.group
            setLessonContentVisibility()
            collapseContent()
            val adapter = UserAdapter()
            adapter.submitList(ArrayList<DomainModel>(lesson.teachers))
            with(lessonExpandableContentBinding) {
                rvTeachers.adapter = adapter
            }

        }

        private fun setLessonContentVisibility() {
            val contentVisibility = if (event.isEmpty) View.GONE else View.VISIBLE
            ivIcon?.visibility = contentVisibility
            binding.llLessonHeader.ivLessonExpand.visibility = contentVisibility
            tvTitle.visibility = contentVisibility
            tvTime.visibility = contentVisibility
            binding.llLessonHeader.llGroup.visibility = contentVisibility
            if (event.isEmpty) {
                setTitle(null)
                itemView.setOnClickListener(null)
            } else {
                setTextRoom()
                setHomeworkVisibility()
                setGroupVisibility()
                setTitle(subject!!.name)
                itemView.setOnClickListener { view: View? ->
                    val transitionSet = TransitionSet()
                    val rotation = Rotation()
                    rotation.addTarget(binding.llLessonHeader.ivLessonExpand)
                    transitionSet.addTransition(rotation)
                    val elevation = Elevation()
                    elevation.addTarget(itemView)
                    transitionSet.addTransition(elevation)
                    transitionSet.addTransition(AutoTransition())
                    transitionSet.addListener(object : Transition.TransitionListener {
                        override fun onTransitionStart(transition: Transition) {
                            itemView.isClickable = false
                        }

                        override fun onTransitionEnd(transition: Transition) {
                            itemView.isClickable = true
                            itemView.isPressed = false
                        }

                        override fun onTransitionCancel(transition: Transition) {}
                        override fun onTransitionPause(transition: Transition) {}
                        override fun onTransitionResume(transition: Transition) {}
                    })
                    TransitionManager.beginDelayedTransition(
                        (itemView.parent as RecyclerView),
                        transitionSet
                    )
                    if (lessonExpandableContentBinding.root.visibility == View.GONE) {
                        expandContent()
                    } else {
                        collapseContent()
                    }
                }
                color = itemView.resources.getIdentifier(
                    subject!!.colorName,
                    "color",
                    itemView.context.packageName
                )
                setCheckboxColor()
                setIcon(subject!!.iconUrl, subject!!.colorName)
            }
        }

        private fun setGroupVisibility() {
            binding.llLessonHeader.llGroup.visibility =
                if (groupVisibility) View.VISIBLE else View.GONE
            if (groupVisibility) binding.llLessonHeader.tvGroupName.text = group!!.name
        }

        private fun setTextRoom() {
            lessonExpandableContentBinding.etRoom.text =
                if (event.room == null) "Аудитория не указана" else event.room
        }

        fun setHomeworkVisibility() {
            task?.let {
                lessonExpandableContentBinding.tvHomeworkContent.text = it.description
//                lessonExpandableContentBinding.cbHomeworkCompleted.isChecked = it.completed
                lessonExpandableContentBinding.cbHomeworkCompleted.isEnabled = true
                lessonExpandableContentBinding.cbHomeworkCompleted.setOnCheckedChangeListener { compoundButton: CompoundButton, checked: Boolean ->
                    if (lessonExpandableContentBinding.cbHomeworkCompleted.isShown) {
                        if (lessonItemClickListener != null) {
                            Log.d("lol", "CHECK HOMEWORK: ")
                            (onItemClickListener as OnLessonItemClickListener).onHomeworkChecked(
                                checked
                            )
                        }
                    }
                }
            } ?: run { lessonExpandableContentBinding.rlHomework.visibility = View.GONE }
        }

        fun setCheckboxColor() {
            lessonExpandableContentBinding.cbHomeworkCompleted.buttonTintList = ColorStateList(
                arrayOf(
                    intArrayOf(-android.R.attr.state_checked),
                    intArrayOf(android.R.attr.state_checked)
                ), intArrayOf(
                    Color.GRAY,
                    ContextCompat.getColor(itemView.context, color)
                )
            )
        }

        fun expandContent() {
            (itemView as CardView).cardElevation =
                Dimensions.dpToPx(4, itemView.getContext()).toFloat()
            ivArrow.rotation = 180f
            lessonExpandableContentBinding.root.visibility = View.VISIBLE
        }

        private fun collapseContent() {
            (itemView as CardView).cardElevation = 0f
            binding.llLessonHeader.ivLessonExpand.rotation = 0f
            lessonExpandableContentBinding.root.visibility = View.GONE
        }
    }

    class LessonCreateHolder(
        itemLessonCreateBinding: ItemIconContentBinding,
        listener: OnCreateLessonClickListener
    ) :
        BaseViewHolder<ListItem, ItemIconContentBinding>(itemLessonCreateBinding) {
        override fun onBind(item: ListItem) {
            // Nothing
        }

        init {
            val ivIcon = itemView.findViewById<ImageView>(R.id.iv_ic)
            val tvTitle: TextView = itemView.findViewById(R.id.tv_content)
            itemView.setOnClickListener { v: View? ->
                listener.onLessonCreateClick(absoluteAdapterPosition)
            }
            ivIcon.setImageDrawable(ContextCompat.getDrawable(itemView.context, R.drawable.ic_add))
            tvTitle.text = "Добавить урок"
        }
    }

    internal class SimpleEventHolder(
        itemEventBinding: ItemEventBinding,
        onEditEventItemClickListener: OnEditEventItemClickListener,
        itemTouchListener: OnItemTouchListener,
        lessonTime: Int
    ) : EventHolder<ItemEventBinding>(
        itemEventBinding,
        onEditEventItemClickListener,
        itemTouchListener,
        lessonTime
    ) {
        override fun onBind(item: Event) {
            super.onBind(item)
            val details: SimpleEventDetails = item.details as SimpleEventDetails
            setTitle(details.name)
            setIcon(details.iconUrl, details.color)
        }

        init {
            itemView.findViewById<View>(R.id.ll_group).visibility = View.GONE
            itemView.findViewById<View>(R.id.iv_lesson_expand).visibility = View.GONE
        }
    }

    internal class EmptyEventHolder(
        itemEventEmptyBinding: ItemEventEmptyBinding,
        onEditEventItemClickListener: OnEditEventItemClickListener?,
        itemTouchListener: OnItemTouchListener?,
        lessonTime: Int
    ) : EventHolder<ItemEventEmptyBinding>(
        itemEventEmptyBinding,
        onEditEventItemClickListener,
        itemTouchListener,
        lessonTime
    )

    companion object {
        const val TYPE_HEADER = 0
        const val TYPE_LESSON = 1
        const val TYPE_SIMPLE = 2
        const val TYPE_CREATE = 10
        const val TYPE_EMPTY = 11
        private val DIFF_CALLBACK: DiffUtil.ItemCallback<DomainModel> =
            object : DiffUtil.ItemCallback<DomainModel>() {
                override fun areItemsTheSame(oldItem: DomainModel, newItem: DomainModel): Boolean {
                    return oldItem.id == newItem.id
                }

                override fun areContentsTheSame(
                    oldItem: DomainModel,
                    newItem: DomainModel
                ): Boolean {
                    return if (oldItem is Event && newItem is Event) {
                        oldItem == newItem
                    } else oldItem is ListItem && newItem is ListItem
                            && oldItem.type == TYPE_CREATE && newItem.type == TYPE_CREATE
                }

                override fun getChangePayload(oldItem: DomainModel, newItem: DomainModel): Any? {
                    if (oldItem is Event && newItem is Event) {
                        if (oldItem.date == newItem.date &&
                            oldItem.room == newItem.room && oldItem.details == newItem.details && oldItem.order != newItem.order
                        ) {
                            return PAYLOAD.UPDATE_ORDER
                        }
                    }
                    return null
                }
            }
    }
}