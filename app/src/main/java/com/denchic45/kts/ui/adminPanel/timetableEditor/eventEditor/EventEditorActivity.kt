package com.denchic45.kts.ui.adminPanel.timetableEditor.eventEditor

import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.widget.Toolbar
import by.kirich1409.viewbindingdelegate.viewBinding
import com.denchic45.kts.R
import com.denchic45.kts.databinding.ActivityEventEditorBinding
import com.denchic45.kts.ui.base.BaseActivity
import com.example.appbarcontroller.appbarcontroller.AppBarController

class EventEditorActivity :
    BaseActivity<EventEditorViewModel, ActivityEventEditorBinding>(R.layout.activity_event_editor) {
    override val viewModel: EventEditorViewModel by viewModels { viewModelFactory }
    override val binding: ActivityEventEditorBinding by viewBinding(ActivityEventEditorBinding::bind)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        AppBarController.create(this, findViewById(R.id.app_bar))
        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)
    }
}