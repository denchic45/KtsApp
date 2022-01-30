package com.denchic45.kts.ui.adapter

import android.graphics.drawable.PictureDrawable
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.*
import androidx.recyclerview.widget.DiffUtil
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.denchic45.SvgColorListener
import com.denchic45.kts.R
import com.denchic45.kts.data.model.DomainModel
import com.denchic45.kts.data.model.domain.ListItem
import com.denchic45.kts.data.model.domain.Subject
import com.denchic45.kts.databinding.ItemIconContentBinding
import com.denchic45.kts.glideSvg.GlideApp
import com.denchic45.kts.utils.viewBinding

class SubjectAdapter : CustomAdapter<DomainModel, BaseViewHolder<DomainModel,*>> {

    constructor() : super((DIFF_CALLBACK as DiffUtil.ItemCallback<DomainModel>))
    constructor(itemClickListener: OnItemClickListener) : super(
        (DIFF_CALLBACK as DiffUtil.ItemCallback<DomainModel>),
        itemClickListener
    )

    constructor(
        itemClickListener: OnItemClickListener,
        itemLongClickListener: OnItemLongClickListener
    ) : super(
        DIFF_CALLBACK as DiffUtil.ItemCallback<DomainModel>,
        itemClickListener,
        itemLongClickListener
    )

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BaseViewHolder<DomainModel,*> {
        val inflater = LayoutInflater.from(parent.context)
        return when (viewType) {
            TYPE_SUBJECT -> {
                SubjectHolder(
                    parent.viewBinding(ItemIconContentBinding::inflate),
                    onItemClickListener,
                    onItemLongClickListener
                ) as BaseViewHolder<DomainModel,*>
            }
//            TYPE_EVENT -> {
//                EventSubjectHolder(ItemSubjectEventBinding.inflate(inflater), eventSubjectListener!!) as BaseViewHolder<DomainModel,*>
//            }
            else -> throw IllegalStateException("Unexpected value: $viewType")
        }
    }

    override fun getItemViewType(position: Int): Int {
        if (getItem(position) is Subject) {
            return TYPE_SUBJECT
        } else if (getItem(position) is ListItem) {
            return (getItem(position) as ListItem).type
        }
        return -1
    }

    override fun onBindViewHolder(holder: BaseViewHolder<DomainModel,*>, position: Int) {
        holder.onBind(getItem(position))
    }

    override fun onBindViewHolder(
        holder: BaseViewHolder<DomainModel,*>,
        position: Int,
        payloads: List<Any>
    ) {
        if (payloads.isEmpty()) {
            onBindViewHolder(holder, position)
        } else {
//            for (payload in payloads) {
//
//            }
        }
    }

    enum class PAYLOAD {
        UPDATE_ICON
    }

    internal class SubjectHolder(
        itemIconContentBinding: ItemIconContentBinding,
        itemClickListener: OnItemClickListener,
        longClickListener: OnItemLongClickListener
    ) : BaseViewHolder<Subject, ItemIconContentBinding>(itemIconContentBinding, itemClickListener, longClickListener) {
        private val ivIcon: ImageView = itemView.findViewById(R.id.iv_ic)
        private val tvName: TextView
        override fun onBind(item: Subject) {
            tvName.text = item.name
            val color = itemView.resources.getIdentifier(
                item.colorName,
                "color",
                itemView.context.packageName
            )
            GlideApp.with(itemView.context)
                .`as`(PictureDrawable::class.java)
                .transition(DrawableTransitionOptions.withCrossFade())
                .listener(SvgColorListener(ivIcon, color, itemView.context))
                .load(item.iconUrl)
                .into(ivIcon)
        }

        init {
            tvName = itemView.findViewById(R.id.tv_content)
        }
    }

//    class EventSubjectHolder(itemSubjectEventBinding: ItemSubjectEventBinding, eventSubjectListener: OnEventSubjectListener) :
//        BaseViewHolder<ListItem, ItemSubjectEventBinding >(itemSubjectEventBinding) {
//        private val llMasterContent: LinearLayout
//        private val rlDetailContent: RelativeLayout
//        private val etSubjectName: EditText
//        private val tilSubjectName: TextInputLayout
//        private val ivIcon: ImageView
//        private val adapter: ColorPickerAdapter
//        private val btnCancel: Button
//        private val btnOk: Button
//        private val rv: RecyclerView
//
//        //        private String name, iconUrl, colorName;
//
//        private val eventSubjectListener: OnEventSubjectListener
//        private lateinit var subject: Subject
//        fun updateIcon() {
//            Glide.with(itemView.context)
//                .`as`(PictureDrawable::class.java)
//                .transition(DrawableTransitionOptions.withCrossFade())
//                .listener(
//                    SvgColorListener(
//                        ivIcon,
//                        findColorId(subject.colorName),
//                        itemView.context
//                    )
//                )
//                .load(subject.iconUrl)
//                .into(ivIcon)
//        }
//
//        fun updateColor() {
//            ivIcon.post {
//                ViewUtils.paintImageView(
//                    ivIcon, findColorId(
//                        subject.colorName
//                    ), itemView.context
//                )
//            }
//        }
//
//        private fun findColorId(colorName: String): Int {
//            return colors.stream()
//                .filter { (_, title) -> title == colorName }
//                .map { (it.color as EitherResource.Id).a }
//                .findFirst()
//                .orElse(R.color.blue)
//        }
//
//        override fun onBind(item: ListItem) {
//            subject = item.content as Subject
//            subject.colorName = DEFAULT_COLOR
//            subject.iconUrl = DEFAULT_ICON
//            updateIcon()
//            updateColor()
//        }
//
//        companion object {
//            const val DEFAULT_ICON =
//                "https://firebasestorage.googleapis.com/v0/b/kts-app-2ab1f.appspot.com/o/subjects%2Fevent.svg?alt=media&token=082f0838-b01b-4a51-9c7a-f111380c5689"
//            const val DEFAULT_COLOR = "blue"
//        }
//
//        init {
//            this.eventSubjectListener = eventSubjectListener
//            llMasterContent = itemView.findViewById(R.id.ll_master_content)
//            rlDetailContent = itemView.findViewById(R.id.rl_detail_content)
//            etSubjectName = itemView.findViewById(R.id.et_subject_name)
//            tilSubjectName = itemView.findViewById(R.id.tin_subject_name)
//            ivIcon = itemView.findViewById(R.id.iv_subject_ic)
//            btnCancel = itemView.findViewById(R.id.btn_cancel)
//            btnOk = itemView.findViewById(R.id.btn_ok)
//            rv = itemView.findViewById(R.id.rv_color_picker)
//            adapter = ColorPickerAdapter()
//            adapter.list = colors
//            rv.adapter = adapter
//            adapter.setItemClickListener { position: Int ->
//                subject.colorName = colors[position].title
//                adapter.notifyDataSetChanged()
//                updateColor()
//            }
//            llMasterContent.setOnClickListener {
//                TransitionManager.beginDelayedTransition(
//                    (itemView.parent as RecyclerView), AutoTransition()
//                )
//                rlDetailContent.visibility = View.VISIBLE
//                llMasterContent.visibility = View.GONE
//            }
//            btnCancel.setOnClickListener {
//                TransitionManager.beginDelayedTransition(
//                    (itemView.parent as RecyclerView), AutoTransition()
//                )
//                rlDetailContent.visibility = View.GONE
//                llMasterContent.visibility = View.VISIBLE
//            }
//            ivIcon.setOnClickListener { eventSubjectListener.onIconClick() }
//            btnOk.setOnClickListener {
//                val name = etSubjectName.text.toString()
//                if (name.isEmpty()) {
//                    tilSubjectName.error = "Нет названия"
//                } else {
//                    subject.name = name
//                    eventSubjectListener.onEventSubjectCreate()
//                }
//            }
//        }
//    }

    companion object {
        const val TYPE_SUBJECT = 0
        const val TYPE_EVENT = 1
        private val DIFF_CALLBACK: DiffUtil.ItemCallback<out DomainModel> =
            object : DiffUtil.ItemCallback<Subject>() {
                override fun areItemsTheSame(oldItem: Subject, newItem: Subject): Boolean {
                    return oldItem.id == newItem.id
                }

                override fun areContentsTheSame(oldItem: Subject, newItem: Subject): Boolean {
                    return oldItem.name == newItem.name && oldItem.iconUrl == newItem.iconUrl && oldItem.colorName == newItem.colorName
                }
            }
    }
}