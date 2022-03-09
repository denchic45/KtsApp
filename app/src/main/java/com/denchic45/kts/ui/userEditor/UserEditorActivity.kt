package com.denchic45.kts.ui.userEditor

import android.os.Bundle
import android.telephony.PhoneNumberFormattingTextWatcher
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.AdapterView
import androidx.activity.viewModels
import by.kirich1409.viewbindingdelegate.viewBinding
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.denchic45.kts.CustomToolbar
import com.denchic45.kts.KeyboardManager
import com.denchic45.kts.R
import com.denchic45.kts.customPopup.ListPopupWindowAdapter
import com.denchic45.kts.data.model.domain.ListItem
import com.denchic45.kts.databinding.ActivityUserEditorBinding
import com.denchic45.kts.rx.AsyncTransformer
import com.denchic45.kts.ui.BaseActivity
import com.denchic45.kts.utils.JsonUtil
import com.google.android.material.textfield.TextInputLayout
import com.jakewharton.rxbinding4.widget.textChanges

class UserEditorActivity : BaseActivity<UserEditorViewModel, ActivityUserEditorBinding>() {

    override val binding: ActivityUserEditorBinding by viewBinding(ActivityUserEditorBinding::bind)
    override val viewModel: UserEditorViewModel by viewModels { viewModelFactory }
    private lateinit var keyboardManager: KeyboardManager
    private lateinit var menu: Menu

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_user_editor)
        keyboardManager = KeyboardManager()
        val groupAdapter: ListPopupWindowAdapter
        with(binding) {
            etGender.inputEnable(false)
            etRole.inputEnable(false)
            etPhoneNum.addTextChangedListener(PhoneNumberFormattingTextWatcher())
            groupAdapter = ListPopupWindowAdapter(this@UserEditorActivity, emptyList())
            etGroup.setAdapter(groupAdapter)
            val toolbar = findViewById<CustomToolbar>(R.id.toolbar)
            setSupportActionBar(toolbar)
            supportActionBar!!.setDisplayHomeAsUpEnabled(true)
            fab.setOnClickListener { viewModel.onFabClick() }

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

            etPhoneNum.textChanges()
                .skip(1)
                .compose(AsyncTransformer())
                .map { obj: CharSequence -> obj.toString() }
                .subscribe(viewModel::onPhoneNumType)

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
                .filter(String::isNotEmpty)
                .subscribe { s: String? -> viewModel.onPasswordType(s) }

            viewModel.fieldFirstName.observe(
                this@UserEditorActivity
            ) { firstName ->
                if (firstName != etFirstName.text.toString()) etFirstName.setText(firstName)
            }
            viewModel.fieldSurname.observe(
                this@UserEditorActivity
            ) { surname ->
                if (surname != etSurname.text.toString()) etSurname.setText(surname)
            }
            viewModel.fieldPatronymic.observe(
                this@UserEditorActivity
            ) { patronymic ->
                if (patronymic != etPatronymic.text
                        .toString()
                ) etPatronymic.setText(patronymic)
            }
            viewModel.fieldPhoneNum.observe(
                this@UserEditorActivity
            ) { phoneNum ->
                if (phoneNum != etPhoneNum.text.toString()) etPhoneNum.setText(phoneNum)
            }
            viewModel.fieldEmail.observe(
                this@UserEditorActivity
            ) { email -> if (email != etEmail.text.toString()) etEmail.setText(email) }
            viewModel.fieldGender.observe(
                this@UserEditorActivity
            ) { gender ->
                etGender.setText(
                    resources.getIdentifier(
                        gender,
                        "string",
                        packageName
                    )
                )
            }
            viewModel.fieldRole.observe(this@UserEditorActivity) { role: Int ->
                etRole.setText(resources.getString(role))
            }
            viewModel.fieldGroup.observe(this@UserEditorActivity, etGroup::setText)

            viewModel.deleteOptionVisibility.observe(
                this@UserEditorActivity
            ) { visibility: Boolean? ->
                menu.findItem(R.id.option_delete_user).isVisible = visibility!!
            }
            viewModel.fieldGroupVisibility.observe(
                this@UserEditorActivity
            ) { enabled: Boolean ->
                tilGroup.visibility = if (enabled) View.VISIBLE else View.GONE
            }
            viewModel.fieldEmailEnable.observe(this@UserEditorActivity) { enable: Boolean ->
                etEmail.isEnabled = enable
            }
            viewModel.fieldPasswordVisibility.observe(
                this@UserEditorActivity
            ) { visibility: Boolean ->
                tilPassword.visibility = if (visibility) View.VISIBLE else View.GONE
            }
            viewModel.avatarUser.observe(this@UserEditorActivity) { photoUrl ->
                Glide.with(this@UserEditorActivity)
                    .load(photoUrl)
                    .placeholder(ivAvatar.drawable)
                    .transition(DrawableTransitionOptions.withCrossFade(100))
                    .into(ivAvatar)
            }
            viewModel.fieldErrorMessage.observe(
                this@UserEditorActivity
            ) { idWithMessagePair: Pair<Int, String?> ->
                val til = findViewById<TextInputLayout>(
                    idWithMessagePair.first
                )
                til.error = idWithMessagePair.second
            }
            viewModel.fieldRoles.observe(this@UserEditorActivity) { resId ->
                val adapter = ListPopupWindowAdapter(
                    this@UserEditorActivity,
                    JsonUtil.parseToList(this@UserEditorActivity, resId)
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
            viewModel.fieldGenders.observe(this@UserEditorActivity) { genders: List<ListItem> ->
                val adapter = ListPopupWindowAdapter(this@UserEditorActivity, genders)
                etGender.setAdapter(adapter)
                etGender.onItemClickListener =
                    AdapterView.OnItemClickListener { _, _, position: Int, _ ->
                        viewModel.onGenderSelect(position)
                    }
            }
            viewModel.groupList.observe(this@UserEditorActivity) { items: List<ListItem> ->
                groupAdapter.updateList(items)
                etGroup.onItemClickListener =
                    AdapterView.OnItemClickListener { _, _, position, _ ->
                        if (groupAdapter.getItem(position).enable) {
                            viewModel.onGroupSelect(position)
                        }
                    }
            }

        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.options_user_editor, menu)
        this.menu = menu
        viewModel.onCreateOptions()
        return true
    }

    override fun onPrepareOptionsMenu(menu: Menu): Boolean {
        this.menu = menu
        return super.onPrepareOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            onBackPressed()
        } else {
            viewModel.onOptionClick(item.itemId)
        }
        return true
    }

    override fun onResume() {
        super.onResume()
        with(binding) {
            keyboardManager.registerKeyboardListener(findViewById(android.R.id.content)) {
                if (it) {
                    fab.postDelayed({ fab.hide() }, DELAY_FAB_VISIBILITY.toLong())
                } else {
                    fab.postDelayed({ fab.show() }, DELAY_FAB_VISIBILITY.toLong())
                }
            }
        }
    }

    override fun onPause() {
        super.onPause()
        keyboardManager.unregisterKeyboardListener()
    }

    override fun onBackPressed() {
        viewModel.onBackPress()
    }

    companion object {
        const val USER_ROLE = "USER_TYPE"
        const val USER_ID = "USER_ID"
        const val USER_GROUP_ID = "USER_GROUP_ID"
        const val DELAY_FAB_VISIBILITY = 300
    }
}