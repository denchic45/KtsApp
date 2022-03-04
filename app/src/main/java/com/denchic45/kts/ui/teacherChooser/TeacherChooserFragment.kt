package com.denchic45.kts.ui.teacherChooser

import androidx.fragment.app.viewModels
import com.denchic45.kts.data.model.domain.User
import com.denchic45.kts.ui.adapter.UserAdapterDelegate
import com.denchic45.kts.ui.base.chooser.ChooserFragment
import com.denchic45.kts.ui.base.chooser.ChooserViewModel
import com.denchic45.widget.extendedAdapter.AdapterDelegate
import com.denchic45.widget.extendedAdapter.DelegationAdapterExtended
import com.denchic45.widget.extendedAdapter.adapter

class TeacherChooserFragment : ChooserFragment<User>() {
    override val viewModel: TeacherChooserViewModel by viewModels { viewModelFactory }
    override val adapterDelegates: List<AdapterDelegate> = listOf(UserAdapterDelegate())

}