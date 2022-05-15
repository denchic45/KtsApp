package com.denchic45.kts.ui.userEditor

import android.os.Bundle
import android.view.MenuItem
import android.view.View
import android.widget.AdapterView
import androidx.annotation.RestrictTo
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.navArgs
import by.kirich1409.viewbindingdelegate.viewBinding
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.denchic45.kts.KeyboardManager
import com.denchic45.kts.R
import com.denchic45.kts.customPopup.ListPopupWindowAdapter
import com.denchic45.kts.data.model.domain.ListItem
import com.denchic45.kts.databinding.FragmentUserEditorBinding
import com.denchic45.kts.rx.AsyncTransformer
import com.denchic45.kts.ui.BaseFragment
import com.denchic45.kts.ui.HasNavArgs
import com.denchic45.kts.utils.JsonUtil
import com.denchic45.kts.utils.collectWhenResumed
import com.denchic45.kts.utils.collectWhenStarted
import com.google.android.material.textfield.TextInputLayout
import com.jakewharton.rxbinding4.widget.textChanges
import kotlinx.coroutines.flow.filter

class UserEditorFragment : BaseFragment<UserEditorViewModel, FragmentUserEditorBinding>(
    R.layout.fragment_user_editor,
    R.menu.options_user_editor
), HasNavArgs<UserEditorFragmentArgs> {

    override val navArgs: UserEditorFragmentArgs by navArgs()
    override val binding: FragmentUserEditorBinding by viewBinding(FragmentUserEditorBinding::bind)
    override val viewModel: UserEditorViewModel by viewModels { viewModelFactory }
    private lateinit var keyboardManager: KeyboardManager

    @RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        keyboardManager = KeyboardManager()
        val groupAdapter: ListPopupWindowAdapter
        with(binding) {
            groupAdapter = ListPopupWindowAdapter(requireContext(), emptyList())

            etFirstName.textChanges()
                .skip(1)
                .compose(AsyncTransformer())
                .map { obj: CharSequence -> obj.toString() }
                .subscribe(viewModel::onFirstNameType)

            etSurname.textChanges()
                .skip(1)
                .compose(AsyncTransformer())
                .map { obj: CharSequence -> obj.toString() }
                .subscribe(viewModel::onSurnameType)

            etPatronymic.textChanges()
                .skip(1)
                .compose(AsyncTransformer())
                .map { obj: CharSequence -> obj.toString() }
                .subscribe(viewModel::onPatronymicType)

            etEmail.textChanges()
                .skip(1)
                .compose(AsyncTransformer())
                .map { obj: CharSequence -> obj.toString() }
                .subscribe(viewModel::onEmailType)

            etGroup.textChanges()
                .skip(1)
                .compose(AsyncTransformer())
                .map { obj: CharSequence -> obj.toString() }
                .filter(String::isNotEmpty)
                .subscribe(viewModel::onGroupNameType)

            etPassword.textChanges()
                .compose(AsyncTransformer())
                .map { obj: CharSequence -> obj.toString() }
                .doOnNext { btnGenerate.isEnabled = it.isEmpty() }
                .filter(String::isNotEmpty)
                .subscribe { s -> viewModel.onPasswordType(s) }

            tilGroup.setEndIconOnClickListener { viewModel.onSelectGroupClick() }

            etGroup.setOnFocusChangeListener { _, hasFocus ->
                if (hasFocus) viewModel.onSelectGroupClick()
            }

            btnGenerate.setOnClickListener { viewModel.onPasswordGenerateClick() }

            etGroup.setOnClickListener { viewModel.onSelectGroupClick() }

            viewModel.firstNameField.collectWhenStarted(
                lifecycleScope
            ) { firstName ->
                if (firstName != etFirstName.text.toString()) etFirstName.setText(firstName)
            }

            viewModel.surnameField.collectWhenStarted(
                lifecycleScope
            ) { surname ->
                if (surname != etSurname.text.toString()) etSurname.setText(surname)
            }

            viewModel.patronymicField.collectWhenStarted(
                lifecycleScope
            ) { patronymic ->
                if (patronymic != etPatronymic.text
                        .toString()
                ) etPatronymic.setText(patronymic)
            }

            viewModel.emailField.collectWhenStarted(
                lifecycleScope
            ) { email -> if (email != etEmail.text.toString()) etEmail.setText(email) }

            viewModel.genderField.filter(String::isNotEmpty).collectWhenStarted(
                lifecycleScope
            ) { gender ->
                etGender.setText(
                    resources.getIdentifier(
                        gender,
                        "string",
                        requireContext().packageName
                    )
                )
            }

            viewModel.passwordField.collectWhenStarted(lifecycleScope) {
                etPassword.setText(it)
            }

            viewModel.roleField.collectWhenStarted(lifecycleScope) { role ->
                etRole.setText(resources.getString(role))
            }

            viewModel.groupField.collectWhenResumed(lifecycleScope) {
                etGroup.setText(it)
            }

            viewModel.accountFieldsVisibility.collectWhenStarted(
                lifecycleScope
            ) { visibility: Boolean ->
                llAccount.visibility = if (visibility) View.VISIBLE else View.GONE
            }

            viewModel.avatarUser.collectWhenStarted(lifecycleScope) { photoUrl ->
                Glide.with(requireContext())
                    .load(photoUrl)
                    .placeholder(ivAvatar.drawable)
                    .transition(DrawableTransitionOptions.withCrossFade(100))
                    .into(ivAvatar)
            }

            viewModel.errorMessageField.observe(
                viewLifecycleOwner
            ) { idWithMessagePair: Pair<Int, String?> ->
                val til = binding.root.findViewById<TextInputLayout>(
                    idWithMessagePair.first
                )
                til.error = idWithMessagePair.second
            }
            viewModel.roles.collectWhenStarted(lifecycleScope) { resId ->
                val adapter = ListPopupWindowAdapter(
                    requireContext(),
                    JsonUtil.parseToList(requireContext(), resId)
                )
                etRole.setAdapter(adapter)
                etRole.onItemClickListener =
                    AdapterView.OnItemClickListener { _, _, position: Int, _ ->
                        val item = adapter.getItem(position)
                        if (item.enable) {
                            viewModel.onRoleSelect(item)
                        }
                    }
            }
            viewModel.genders.observe(viewLifecycleOwner) { genders: List<ListItem> ->
                val adapter = ListPopupWindowAdapter(requireContext(), genders)
                etGender.setAdapter(adapter)
                etGender.onItemClickListener =
                    AdapterView.OnItemClickListener { _, _, position, _ ->
                        viewModel.onGenderSelect(position)
                    }
            }

            viewModel.groupList.observe(viewLifecycleOwner) { items: List<ListItem> ->
                groupAdapter.updateList(items)
            }

            viewModel.roleFieldVisibility.collectWhenStarted(lifecycleScope) {
                tilRole.visibility = if (it) View.VISIBLE else View.GONE
            }

            viewModel.groupFieldVisibility.collectWhenStarted(lifecycleScope) {
                tilGroup.visibility = if (it) View.VISIBLE else View.GONE
            }
        }
    }


    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            viewModel.onBackPress()
        } else {
            viewModel.onOptionClick(item.itemId)
        }
        return true
    }

    override fun onPause() {
        super.onPause()
        keyboardManager.unregisterKeyboardListener()
    }

    companion object {
        const val USER_ROLE = "USER_ROLE"
        const val USER_ID = "USER_ID"
        const val USER_GROUP_ID = "USER_GROUP_ID"
    }
}