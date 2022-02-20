package com.denchic45.kts.ui.group.editor

import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.NavController
import androidx.navigation.Navigation.findNavController
import by.kirich1409.viewbindingdelegate.viewBinding
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.denchic45.EventObserver
import com.denchic45.kts.R
import com.denchic45.kts.customPopup.ListPopupWindowAdapter
import com.denchic45.kts.data.model.domain.ListItem
import com.denchic45.kts.data.model.domain.User
import com.denchic45.kts.databinding.FragmentGroupEditorBinding
import com.denchic45.kts.rx.EditTextTransformer
import com.denchic45.kts.ui.confirm.ConfirmDialog
import com.denchic45.kts.utils.setActivityTitle
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.textfield.TextInputLayout
import com.jakewharton.rxbinding4.widget.textChanges
import java.util.*

class GroupEditorFragment : Fragment(R.layout.fragment_group_editor) {

    val binding: FragmentGroupEditorBinding by viewBinding(FragmentGroupEditorBinding::bind)
    val viewModel: GroupEditorViewModel by activityViewModels()
    private val viewBinding: FragmentGroupEditorBinding by viewBinding(FragmentGroupEditorBinding::bind)
    private var specialtyAdapter: ListPopupWindowAdapter? = null
    private var navController: NavController? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        navController = findNavController(view)
        viewModel //need for init before in activity
        val curatorHeader = view.findViewById<TextView>(R.id.tv_header)
        specialtyAdapter = ListPopupWindowAdapter(requireContext(), ArrayList())
        viewBinding.apply {
            etSpecialty.setAdapter(specialtyAdapter)
            etCourse.setAdapter(ListPopupWindowAdapter(requireContext(), viewModel.courseList))
            etCourse.setOnItemClickListener { _: AdapterView<*>?, _: View?, position: Int, _: Long ->
                viewModel.onCourseSelect(position)
            }
            etSpecialty.setOnItemClickListener { _: AdapterView<*>?, _: View?, position: Int, _: Long ->
                viewModel.onSpecialtySelect(
                    position
                )
            }
            fab.setOnClickListener { viewModel.onFabClick() }
            rlCurator.setOnClickListener { viewModel.onCuratorClick() }
            curatorHeader.text = "Куратор"
            etSpecialty.textChanges()
                .compose(EditTextTransformer())
                .subscribe { specialtyName: String -> viewModel.onSpecialtyNameType(specialtyName) }
            etGroupName.textChanges()
                .compose(EditTextTransformer())
                .subscribe { name: String -> viewModel.onGroupNameType(name) }

            viewModel.showMessageId.observe(
                viewLifecycleOwner,
                EventObserver { message: Int ->
                    Snackbar.make(fab, message, Snackbar.LENGTH_LONG).show()
                })

            viewModel.enableSpecialtyField.observe(viewLifecycleOwner) { enable: Boolean ->
                etSpecialty.isEnabled = enable
            }
            viewModel.toolbarTitle.observe(
                viewLifecycleOwner
            ) { title: String -> setActivityTitle(title) }
            viewModel.openChoiceOfCurator.observe(
                viewLifecycleOwner
            ) { navController!!.navigate(R.id.action_groupEditorFragment_to_choiceOfCuratorFragment) }
            viewModel.curatorField.observe(viewLifecycleOwner) { user: User ->
                Glide.with(this@GroupEditorFragment)
                    .load(user.photoUrl)
                    .transition(DrawableTransitionOptions.withCrossFade(100))
                    .into(ivCuratorAvatar)
                tvCuratorName.text = user.fullName
            }
            viewModel.nameField.observe(
                viewLifecycleOwner
            ) { text: String -> if (etGroupName.text.toString() != text) etGroupName.setText(text) }
            viewModel.specialtyField.observe(
                viewLifecycleOwner
            ) { text: String -> if (etSpecialty.text.toString() != text) etSpecialty.setText(text) }
            viewModel.courseField.observe(viewLifecycleOwner) { courseResName: String? ->
                val resId =
                    resources.getIdentifier(courseResName, "string", requireActivity().packageName)
                etCourse.setText(resId)
            }
        }

        viewModel.openConfirmation.observe(
            viewLifecycleOwner
        ) { titleWithSubtitlePair: Pair<String, String> ->
            val dialog = ConfirmDialog.newInstance(
                titleWithSubtitlePair.first,
                titleWithSubtitlePair.second
            )
            dialog.show(childFragmentManager, null)
        }
        viewModel.fieldErrorMessage.observe(viewLifecycleOwner) { idWithMessagePair: Pair<Int, String?> ->
            val textInputLayout: TextInputLayout = requireActivity().findViewById(
                idWithMessagePair.first
            )
            if (idWithMessagePair.second != null) {
                textInputLayout.error = idWithMessagePair.second
            } else {
                textInputLayout.error = null
            }
        }
        viewModel.showMessageId.observe(viewLifecycleOwner) { resId: Int ->
            Toast.makeText(
                context, resources.getString(
                    resId
                ), Toast.LENGTH_SHORT
            ).show()
        }
        viewModel.showSpecialties.observe(
            viewLifecycleOwner
        ) { listItems: List<ListItem> -> specialtyAdapter!!.updateList(listItems) }
    }

    companion object {
        const val GROUP_ID = "GROUP_UUID"
    }
}