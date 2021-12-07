package com.denchic45.kts.ui.group.editor

import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.NavController
import androidx.navigation.Navigation.findNavController
import by.kirich1409.viewbindingdelegate.viewBinding
import com.denchic45.kts.R
import com.denchic45.kts.databinding.ActivityGroupEditorBinding
import com.denchic45.kts.di.viewmodel.ViewModelFactory
import com.denchic45.kts.ui.BaseActivity
import com.example.appbarcontroller.appbarcontroller.AppBarController
import javax.inject.Inject

class GroupEditorActivity : AppCompatActivity(R.layout.activity_group_editor) {

    val binding: ActivityGroupEditorBinding by viewBinding(ActivityGroupEditorBinding::bind)
    val viewModel: GroupEditorViewModel by viewModels ()

    private var menu: Menu? = null
    private var navController: NavController? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding.apply {
            AppBarController.create(this@GroupEditorActivity, appBar)
            setSupportActionBar(toolbar)
        }
        viewModel.finish.observe(this, { finish() })
        navController = findNavController(this, R.id.nav_host_fragment)

        supportActionBar!!.setDisplayHomeAsUpEnabled(true)
        viewModel.finish.observe(this, { finish() })
        viewModel.deleteOptionVisibility.observe(
            this,
            { visibility: Boolean? ->
                menu!!.findItem(R.id.option_delete_group).isVisible = visibility!!
            })

    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.options_group_editor, menu)
        this.menu = menu
        viewModel.onCreateOptions()
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            onBackPressed()
        } else {
            viewModel.onOptionClick(item.itemId)
        }
        return true
    }

    override fun onBackPressed() {
        if (!navController!!.navigateUp()) viewModel.onBackPress()
    }

    companion object {
        const val GROUP_UUID = "GROUP_UUID"
    }
}