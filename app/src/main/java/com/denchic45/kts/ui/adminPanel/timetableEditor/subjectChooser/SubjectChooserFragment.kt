package com.denchic45.kts.ui.adminPanel.timetableEditor.subjectChooser

import androidx.fragment.app.viewModels
import com.denchic45.kts.ui.adapter.SubjectAdapterDelegate
import com.denchic45.kts.ui.base.chooser.ChooserFragment
import com.denchic45.widget.extendedAdapter.AdapterDelegate

class SubjectChooserFragment : ChooserFragment<SubjectChooserViewModel>() {
    override val viewModel: SubjectChooserViewModel by viewModels { viewModelFactory }
    override fun adapterDelegates(): List<AdapterDelegate> = listOf(SubjectAdapterDelegate())
}