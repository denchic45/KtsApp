package com.denchic45.kts.ui.teacherChooser

import androidx.fragment.app.viewModels
import com.denchic45.kts.ui.adapter.UserAdapterDelegate
import com.denchic45.kts.ui.base.chooser.ChooserFragment
import com.denchic45.widget.extendedAdapter.AdapterDelegate

class TeacherChooserFragment :
    ChooserFragment<TeacherChooserViewModel>() {
    override val viewModel: TeacherChooserViewModel by viewModels { viewModelFactory }
    override fun adapterDelegates(): List<AdapterDelegate> = listOf(UserAdapterDelegate())
}