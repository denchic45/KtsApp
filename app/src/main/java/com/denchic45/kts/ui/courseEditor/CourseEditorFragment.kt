package com.denchic45.kts.ui.courseEditor

import android.content.Context
import android.graphics.drawable.PictureDrawable
import android.os.Bundle
import android.view.*
import android.view.View.OnFocusChangeListener
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.*
import android.widget.TextView.OnEditorActionListener
import androidx.appcompat.widget.ListPopupWindow
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import by.kirich1409.viewbindingdelegate.viewBinding
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.denchic45.SvgColorListener
import com.denchic45.kts.R
import com.denchic45.kts.customPopup.ListPopupWindowAdapter
import com.denchic45.kts.data.model.domain.ListItem
import com.denchic45.kts.data.model.domain.User
import com.denchic45.kts.databinding.FragmentCourseEditorBinding
import com.denchic45.kts.databinding.ItemGroupInCourseBinding
import com.denchic45.kts.glideSvg.GlideApp
import com.denchic45.kts.rx.EditTextTransformer
import com.denchic45.kts.ui.BaseFragment
import com.denchic45.kts.ui.adapter.BaseViewHolder
import com.denchic45.kts.ui.confirm.ConfirmDialog
import com.denchic45.kts.utils.setActivityTitle
import com.denchic45.kts.utils.viewBinding
import com.denchic45.widget.extendedAdapter.ItemAdapterDelegate
import com.denchic45.widget.extendedAdapter.ListItemAdapterDelegate
import com.denchic45.widget.extendedAdapter.adapter
import com.denchic45.widget.extendedAdapter.extension.clickBuilder
import com.google.android.material.snackbar.Snackbar
import com.jakewharton.rxbinding4.widget.textChanges
import io.reactivex.rxjava3.internal.util.AppendOnlyLinkedArrayList.NonThrowingPredicate

class CourseEditorFragment :
    BaseFragment<CourseEditorViewModel, FragmentCourseEditorBinding>(R.layout.fragment_course_editor) {
    override val viewModel: CourseEditorViewModel by viewModels { viewModelFactory }
    private var imm: InputMethodManager? = null
    private var popupWindow: ListPopupWindow? = null
    private lateinit var menu: Menu

    override val binding: FragmentCourseEditorBinding by viewBinding(FragmentCourseEditorBinding::bind)

    private fun closeKeyboard(view: View) {
        imm!!.hideSoftInputFromWindow(view.windowToken, 0)
    }

    private val adapter = adapter {
        delegates(CourseGroupsAdapterDelegate(), ItemAdapterDelegate())
        extensions {
            clickBuilder<ItemAdapterDelegate.ItemHolder> {
                onClick = { viewModel.onGroupAddClick() }
            }
            clickBuilder<CourseGroupsAdapterDelegate.GroupHolder> {
                view = { it.itemGroupInCourseBinding.ivRemove }
                onClick = { position ->
                    viewModel.onGroupRemoveClick(position)
                }
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        this.menu = menu
        inflater.inflate(R.menu.options_course_editor, menu)
        viewModel.onCreateOptions()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        viewModel.onOptionClick(item.itemId)
        return super.onOptionsItemSelected(item)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        popupWindow = ListPopupWindow(requireActivity())
        imm = requireActivity().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        binding.apply {
            tvSubjectName.setOnClickListener { viewModel.onSubjectNameClick() }
            tvTeacherName.setOnClickListener { viewModel.onTeacherNameClick() }
            ivSubjectEdit.setOnClickListener { viewModel.onSubjectEditClick() }
            ivTeacherEdit.setOnClickListener { viewModel.onTeacherEditClick() }

            rvGroups.adapter = adapter

            etSubjectName.onFocusChangeListener =
                OnFocusChangeListener { _: View?, focus: Boolean ->
                    viewModel.onSubjectNameFocusChange(focus)
                }
            etTeacherName.onFocusChangeListener =
                OnFocusChangeListener { _: View?, focus: Boolean ->
                    viewModel.onTeacherNameFocusChange(focus)
                }

            etCourseName.textChanges()
                .compose(EditTextTransformer())
                .subscribe { viewModel.onCourseNameType(it) }

            etSubjectName.setOnEditorActionListener(OnEditorActionListener { v: TextView?, actionId: Int, event: KeyEvent? ->
                if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                    viewModel.onSubjectNameType(etSubjectName.text.toString())
                    closeKeyboard(etSubjectName)
                    return@OnEditorActionListener true
                }
                false
            })
            etTeacherName.setOnEditorActionListener(OnEditorActionListener { v: TextView?, actionId: Int, event: KeyEvent? ->
                if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                    viewModel.onTeacherNameType(etTeacherName.text.toString())
                    closeKeyboard(etTeacherName)
                    return@OnEditorActionListener true
                }
                false
            })

            viewModel.optionVisibility.observe(
                viewLifecycleOwner
            ) { idAndVisiblePair: Pair<Int, Boolean> ->
                val menuItem = menu.findItem(
                    idAndVisiblePair.first
                )
                if (menuItem != null) menuItem.isVisible = idAndVisiblePair.second
            }

            viewModel.groupList.observe(viewLifecycleOwner) {
                adapter.submit(it)
            }

            viewModel.title.observe(viewLifecycleOwner, this@CourseEditorFragment::setActivityTitle)

            viewModel.nameField.observe(viewLifecycleOwner) {
                if (etCourseName.text.toString() != it) etCourseName.setText(it)
            }

            viewModel.subjectNameTypeEnable.observe(viewLifecycleOwner) { visible: Boolean ->
                vsSubjectName.displayedChild = if (visible) 1 else 0
                ivSubjectEdit.setImageResource(if (visible) R.drawable.ic_arrow_left else R.drawable.ic_edit)
                if (visible) {
                    etSubjectName.requestFocus()
                    imm!!.showSoftInput(etSubjectName, 0)
                } else {
                    closeKeyboard(etSubjectName)
                }
            }

            viewModel.teacherNameTypeEnable.observe(
                viewLifecycleOwner
            ) { visible: Boolean ->
                vsTeacherName.displayedChild = if (visible) 1 else 0
                ivTeacherEdit.setImageResource(if (visible) R.drawable.ic_arrow_left else R.drawable.ic_edit)
                if (visible) {
                    etTeacherName.requestFocus()
                    imm!!.showSoftInput(etTeacherName, InputMethodManager.HIDE_IMPLICIT_ONLY)
                } else {
                    closeKeyboard(etTeacherName)
                }
            }
            viewModel.selectSubject.observe(viewLifecycleOwner) { (_, name, iconUrl, colorName) ->
                val resColor = requireActivity()
                    .resources
                    .getIdentifier(colorName, "color", requireContext().packageName)
                GlideApp.with(requireActivity())
                    .`as`(PictureDrawable::class.java)
                    .transition(DrawableTransitionOptions.withCrossFade())
                    .listener(SvgColorListener(ivSubjectIcon, resColor, activity))
                    .load(iconUrl)
                    .into(ivSubjectIcon)
                tvSubjectName.text = name
                etSubjectName.setText("")
            }

            viewModel.finish.observe(viewLifecycleOwner) {
                findNavController().popBackStack()
            }

            viewModel.selectTeacher.observe(viewLifecycleOwner) { teacher: User ->
                Glide.with(requireActivity())
                    .load(teacher.photoUrl)
                    .transition(DrawableTransitionOptions.withCrossFade(100))
                    .into(ivTeacherAvatar)
                tvTeacherName.text = teacher.fullName
                etTeacherName.setText("")
            }
            viewModel.showFoundTeachers.observe(viewLifecycleOwner) { items: List<ListItem> ->
                popupWindow!!.anchorView = etTeacherName
                popupWindow!!.setAdapter(ListPopupWindowAdapter(requireContext(), items))
                popupWindow!!.setOnItemClickListener { _: AdapterView<*>?, _: View?, position: Int, _: Long ->
                    popupWindow!!.dismiss()
                    viewModel.onTeacherSelect(position)
                }
                popupWindow!!.show()
            }
            viewModel.showMessageRes.observe(viewLifecycleOwner) { resId: Int? ->
                Toast.makeText(
                    context, getString(
                        resId!!
                    ), Toast.LENGTH_SHORT
                ).show()
            }
            viewModel.showFoundSubjects.observe(viewLifecycleOwner) { items: List<ListItem> ->
                popupWindow!!.anchorView = etSubjectName
                popupWindow!!.setAdapter(ListPopupWindowAdapter(requireContext(), items))
                popupWindow!!.setOnItemClickListener { _: AdapterView<*>?, view1: View?, position: Int, _: Long ->
                    popupWindow!!.dismiss()
                    viewModel.onSubjectSelect(position)
                }
                popupWindow!!.show()
            }
            viewModel.openConfirmation.observe(viewLifecycleOwner) { titleWithSubtitlePair: Pair<String, String> ->
                val dialog = ConfirmDialog.newInstance(
                    titleWithSubtitlePair.first,
                    titleWithSubtitlePair.second
                )
                dialog.show(childFragmentManager, null)
            }
            viewModel.openChoiceOfGroup.observe(viewLifecycleOwner) {
                findNavController().navigate(R.id.action_global_choiceOfGroupFragment)
            }

            viewModel.finish.observe(viewLifecycleOwner) { findNavController().popBackStack() }

            viewModel.optionVisibility.observe(viewLifecycleOwner) { (id, visibility) ->
                menu.findItem(id)?.isVisible = visibility
            }

            viewModel.showMessage.observe(viewLifecycleOwner) {
                Snackbar.make(binding.root, it, Snackbar.LENGTH_SHORT).show()
            }

            etSubjectName.textChanges()
                .compose(EditTextTransformer())
                .filter(NonThrowingPredicate { charSequence: CharSequence -> charSequence.length > 1 && etSubjectName.hasFocus() } as NonThrowingPredicate<CharSequence>)
                .subscribe { subjectName: String? ->
                    viewModel.onSubjectNameType(
                        subjectName!!
                    )
                }
            etTeacherName.textChanges()
                .compose(EditTextTransformer())
                .filter(NonThrowingPredicate { charSequence: CharSequence -> charSequence.length > 3 && etTeacherName.hasFocus() } as NonThrowingPredicate<CharSequence>)
                .subscribe { teacherName: String -> viewModel.onTeacherNameType(teacherName) }
        }
    }

    companion object {
        const val COURSE_ID = "CourseEditorFragment COURSE_UUID"
    }
}

class CourseGroupsAdapterDelegate :
    ListItemAdapterDelegate<ListItem, CourseGroupsAdapterDelegate.GroupHolder>() {

    class GroupHolder(val itemGroupInCourseBinding: ItemGroupInCourseBinding) :
        BaseViewHolder<ListItem, ItemGroupInCourseBinding>(itemGroupInCourseBinding) {
        override fun onBind(item: ListItem) {
            with(binding) {
                tvContent.text = item.title
            }
        }
    }

    override fun isForViewType(item: Any): Boolean {
        return item is ListItem && item.type == 1
    }

    override fun onBindViewHolder(item: ListItem, holder: GroupHolder) {
        holder.onBind(item)
    }

    override fun onCreateViewHolder(parent: ViewGroup): GroupHolder {
        return GroupHolder(parent.viewBinding(ItemGroupInCourseBinding::inflate))
    }
}
