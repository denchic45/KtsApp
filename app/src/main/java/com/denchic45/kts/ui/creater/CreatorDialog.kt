package com.denchic45.kts.ui.creater

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.DrawableCompat
import androidx.core.os.bundleOf
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.RecyclerView
import com.denchic45.kts.R
import com.denchic45.kts.data.model.domain.ListItem
import com.denchic45.kts.data.model.domain.onId
import com.denchic45.kts.databinding.ItemEntityBinding
import com.denchic45.kts.di.viewmodel.ViewModelFactory
import com.denchic45.kts.ui.HasViewModel
import com.denchic45.kts.ui.adapter.BaseViewHolder
import com.denchic45.kts.ui.adapter.OnItemClickListener
import com.denchic45.kts.ui.courseEditor.CourseEditorActivity
import com.denchic45.kts.ui.group.editor.GroupEditorActivity
import com.denchic45.kts.ui.specialtyEditor.SpecialtyEditorDialog
import com.denchic45.kts.ui.subjectEditor.SubjectEditorDialog
import com.denchic45.kts.ui.userEditor.UserEditorActivity
import com.denchic45.kts.utils.viewBinding
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

class CreatorDialog : BottomSheetDialogFragment(), HasViewModel<CreatorViewModel> {
    private var adapter: ItemAdapter? = null

    override lateinit var viewModelFactory: ViewModelFactory<CreatorViewModel>
    override val viewModel: CreatorViewModel by viewModels { viewModelFactory }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val root = inflater.inflate(R.layout.fragment_creator, container, false)
        adapter = ItemAdapter()
        root.findViewById<RecyclerView>(R.id.recyclerview_creator).adapter = adapter
        return root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        adapter!!.setData(viewModel.createEntityList())
        adapter!!.setOnItemClickListener { position: Int -> viewModel.onEntityClick(position) }
        viewModel.openUserEditor.observe(viewLifecycleOwner, { args: Map<String, String> ->
            val intent = Intent(activity, UserEditorActivity::class.java)
            args.forEach { (name: String?, value: String?) -> intent.putExtra(name, value) }
            startActivity(intent)
        })
        viewModel.openGroupEditor.observe(viewLifecycleOwner, {
            startActivity(Intent(activity, GroupEditorActivity::class.java))
        })
        viewModel.openSubjectEditor.observe(viewLifecycleOwner, {
            SubjectEditorDialog.newInstance(null).show(
                parentFragmentManager, null
            )
        })
        viewModel.openSpecialtyEditor.observe(viewLifecycleOwner, {
            SpecialtyEditorDialog.newInstance(null).show(
               parentFragmentManager, null
            )
        })
        viewModel.openCourseEditor.observe(viewLifecycleOwner, {
            findNavController().navigate(
                R.id.action_global_courseEditorFragment,
                bundleOf(CourseEditorActivity.COURSE_ID to it)
            )
        })
        viewModel.finish.observe(viewLifecycleOwner, { dismiss() })
    }

    override fun getTheme(): Int = R.style.BaseBottomSheetMenu
    

    private class ItemAdapter : RecyclerView.Adapter<EntityHolder>() {
        private var listItems: List<ListItem> = emptyList()
        private var itemClickListener: OnItemClickListener = OnItemClickListener { }
        fun setOnItemClickListener(itemClickListener: OnItemClickListener) {
            this.itemClickListener = itemClickListener
        }

        fun setData(listItems: List<ListItem>) {
            this.listItems = listItems
        }

        override fun onCreateViewHolder(
            parent: ViewGroup,
            viewType: Int
        ): EntityHolder {
            return EntityHolder(parent.viewBinding(ItemEntityBinding::inflate), itemClickListener)
        }

        override fun onBindViewHolder(holder: EntityHolder, position: Int) {
            holder.onBind(listItems[position])
        }

        override fun getItemCount(): Int = ITEM_COUNT


        companion object {
            const val ITEM_COUNT = 6
        }
    }

    private class EntityHolder(
        itemEntityBinding: ItemEntityBinding,
        itemClickListener: OnItemClickListener
    ) : BaseViewHolder<ListItem, ItemEntityBinding>(itemEntityBinding, itemClickListener) {
        private val tvTitle: TextView = itemView.findViewById(R.id.textView_title)
        private val ivIcon: ImageView = itemView.findViewById(R.id.iv_ic)
        override fun onBind(item: ListItem) {
            tvTitle.text = item.title
            item.icon.onId {
                val icon = ContextCompat.getDrawable(itemView.context, it)!!
                DrawableCompat.setTint(
                    icon,
                    ContextCompat.getColor(itemView.context, R.color.blue)
                )
                ivIcon.setImageDrawable(icon)
            }
        }

    }
}