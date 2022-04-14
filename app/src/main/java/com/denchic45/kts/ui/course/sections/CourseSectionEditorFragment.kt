package com.denchic45.kts.ui.course.sections

import android.graphics.Canvas
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.SimpleItemAnimator
import by.kirich1409.viewbindingdelegate.viewBinding
import com.denchic45.kts.R
import com.denchic45.kts.data.UiModel
import com.denchic45.kts.data.model.domain.Section
import com.denchic45.kts.databinding.FragmentCourseSectionEditorBinding
import com.denchic45.kts.databinding.ItemAddSectionBinding
import com.denchic45.kts.databinding.ItemEditSectionBinding
import com.denchic45.kts.ui.BaseFragment
import com.denchic45.kts.ui.adapter.BaseViewHolder
import com.denchic45.kts.utils.closeKeyboard
import com.denchic45.kts.utils.setActivityTitle
import com.denchic45.kts.utils.showKeyboard
import com.denchic45.kts.utils.viewBinding
import com.denchic45.widget.extendedAdapter.ListItemAdapterDelegate
import com.denchic45.widget.extendedAdapter.adapter

class CourseSectionEditorFragment :
    BaseFragment<CourseSectionEditorViewModel, FragmentCourseSectionEditorBinding>(R.layout.fragment_course_section_editor) {


    override val binding: FragmentCourseSectionEditorBinding by viewBinding(
        FragmentCourseSectionEditorBinding::bind
    )

    override val viewModel: CourseSectionEditorViewModel by viewModels { viewModelFactory }

    companion object {
        const val COURSE_ID = "CourseSectionEditor COURSE_ID"
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setActivityTitle("Редактировать секции")

        val adapter = adapter {
            delegates(
                EditSectionAdapterDelegate(
                    renameCallback = { name, position ->
                        viewModel.onSectionRename(name, position)
                    },
                    removeCallback = { viewModel.onSectionRemove(it) },
                ), AddSectionAdapterDelegate(viewModel::onSectionAdd)
            )
            extensions {

            }
        }
        with(binding) {
            val simpleCallback = object : ItemTouchHelper.SimpleCallback(
                ItemTouchHelper.UP or ItemTouchHelper.DOWN, 0
            ) {
                override fun onMove(
                    recyclerView: RecyclerView,
                    viewHolder: RecyclerView.ViewHolder,
                    target: RecyclerView.ViewHolder
                ): Boolean {
                    val move = viewHolder is EditSectionAdapterDelegate.EditSectionHolder
                            && target is EditSectionAdapterDelegate.EditSectionHolder

                    if (move) {
                        val oldPosition = viewHolder.absoluteAdapterPosition
                        val position = target.absoluteAdapterPosition
                        viewModel.onSectionMove(oldPosition - 1, position - 1)
                    }
                    return move
                }

                override fun onChildDraw(
                    c: Canvas,
                    recyclerView: RecyclerView,
                    viewHolder: RecyclerView.ViewHolder,
                    dX: Float,
                    dY: Float,
                    actionState: Int,
                    isCurrentlyActive: Boolean
                ) {
                    super.onChildDraw(
                        c,
                        recyclerView,
                        viewHolder,
                        dX,
                        dY,
                        actionState,
                        isCurrentlyActive
                    )
                    viewHolder.itemView.isSelected =
                        actionState == ItemTouchHelper.ACTION_STATE_DRAG && isCurrentlyActive
                }

                override fun clearView(
                    recyclerView: RecyclerView,
                    viewHolder: RecyclerView.ViewHolder
                ) {
                    super.clearView(recyclerView, viewHolder)
                    viewModel.onSectionMoved()
                }

                override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {}
            }
            val itemTouchHelper = ItemTouchHelper(simpleCallback)

            itemTouchHelper.attachToRecyclerView(rvSections)

            rvSections.adapter = adapter
            (rvSections.itemAnimator as SimpleItemAnimator).supportsChangeAnimations = false
            lifecycleScope.launchWhenStarted {
                viewModel.sections.collect {
                    adapter.submit(listOf(EditSectionAdapterDelegate.AddSectionItem) + it)
                }
            }
        }
    }
}

class EditSectionAdapterDelegate(
    private val renameCallback: (name: String, position: Int) -> Unit,
    private val removeCallback: (position: Int) -> Unit
) :
    ListItemAdapterDelegate<Section, EditSectionAdapterDelegate.EditSectionHolder>() {

    object AddSectionItem : UiModel {
        override fun equals(other: Any?): Boolean = other is AddSectionItem
    }

    override fun isForViewType(item: Any): Boolean = item is Section

    override fun onBindViewHolder(item: Section, holder: EditSectionHolder) = holder.onBind(item)

    override fun onCreateViewHolder(parent: ViewGroup): EditSectionHolder {
        return EditSectionHolder(
            parent.viewBinding(ItemEditSectionBinding::inflate),
            renameCallback,
            removeCallback
        )
    }

    class EditSectionHolder(
        itemEditSectionBinding: ItemEditSectionBinding,
        private val renameCallback: (name: String, position: Int) -> Unit,
        private val removeCallback: (position: Int) -> Unit
    ) :
        BaseViewHolder<Section, ItemEditSectionBinding>(itemEditSectionBinding) {
        override fun onBind(item: Section) {
            with(binding) {

                fun closeEditing() {
                    vsName.displayedChild = 0
                    vsRemove.displayedChild = 0
                    vsEditWithDone.displayedChild = 0
                    etName.closeKeyboard()
                }

                tvName.text = item.name
                etName.setText(item.name)

                etName.setOnFocusChangeListener { _, hasFocus ->
                    if (!hasFocus) {
                        closeEditing()
                    }
                }

                ivEdit.setOnClickListener {
                    vsName.displayedChild = 1
                    vsRemove.displayedChild = 1
                    vsEditWithDone.displayedChild = 1
                    etName.showKeyboard()
                    etName.setSelection(etName.length())
                }

                ivDone.setOnClickListener {
                    tvName.text = etName.text.toString()
                    closeEditing()
                    renameCallback(etName.text.toString(), absoluteAdapterPosition - 1)
                }

                ivRemove.setOnClickListener {
                    closeEditing()
                    removeCallback(absoluteAdapterPosition - 1)
                }
            }
        }


    }
}

class AddSectionAdapterDelegate(private val doneCallback: (name: String) -> Unit) :
    ListItemAdapterDelegate<EditSectionAdapterDelegate.AddSectionItem, AddSectionAdapterDelegate.AddSectionHolder>() {
    class AddSectionHolder(
        itemAddSectionBinding: ItemAddSectionBinding,
        private val doneCallback: (name: String) -> Unit
    ) :
        BaseViewHolder<EditSectionAdapterDelegate.AddSectionItem, ItemAddSectionBinding>(
            itemAddSectionBinding
        ) {
        override fun onBind(item: EditSectionAdapterDelegate.AddSectionItem) {
            with(binding) {
                fun closeEditing() {
                    vs.displayedChild = 0
                    etName.setText("")
                    ivDone.visibility = View.INVISIBLE
                    etName.closeKeyboard()
                }
                etName.setOnFocusChangeListener { _, focus ->
                    if (focus) {
                        vs.displayedChild = 1
                        ivDone.visibility = View.VISIBLE
                    } else {
                        closeEditing()
                    }
                }
                ivClose.setOnClickListener {
                    etName.closeKeyboard()
                }
                ivAdd.setOnClickListener {

                    etName.showKeyboard()
                }
                ivDone.setOnClickListener {
                    doneCallback(etName.text.toString())
                    closeEditing()
                }
            }
        }


    }

    override fun isForViewType(item: Any): Boolean =
        item is EditSectionAdapterDelegate.AddSectionItem

    override fun onBindViewHolder(
        item: EditSectionAdapterDelegate.AddSectionItem,
        holder: AddSectionHolder
    ) = holder.onBind(item)

    override fun onCreateViewHolder(parent: ViewGroup): AddSectionHolder {
        return AddSectionHolder(parent.viewBinding(ItemAddSectionBinding::inflate), doneCallback)
    }
}