package com.denchic45.kts.ui.profile.fullAvatar

import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.widget.Toolbar

import com.denchic45.kts.R
import com.denchic45.kts.di.viewmodel.ViewModelFactory
import com.denchic45.kts.ui.avatar.FullImageActivity
import dagger.android.AndroidInjection
import javax.inject.Inject

class FullAvatarActivity : FullImageActivity() {
    private var menu: Menu? = null
    var toolbar: Toolbar? = null

    @Inject
    lateinit var viewModelFactory: ViewModelFactory<FullAvatarViewModel>
    private val viewModel: FullAvatarViewModel by viewModels { viewModelFactory }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        AndroidInjection.inject(this)
        toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar!!.title = ""
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)
        viewModel.optionVisibility.observe(
            this
        ) { idAndVisibility: Pair<Int, Boolean> ->
            menu!!.findItem(
                idAndVisibility.first
            ).isVisible = idAndVisibility.second
        }
        viewModel.showMessage.observe(
            this
        ) { s: String? -> Toast.makeText(this, s, Toast.LENGTH_SHORT).show() }
        viewModel.showMessageRes.observe(
            this
        ) { s: Int? -> Toast.makeText(this, s!!, Toast.LENGTH_SHORT).show() }
        viewModel.finish.observe(this) { supportFinishAfterTransition() }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.options_avatar, menu)
        this.menu = menu
        viewModel.onCreateOptions()
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        viewModel.onOptionClick(item.itemId)
        return super.onOptionsItemSelected(item)
    }
}