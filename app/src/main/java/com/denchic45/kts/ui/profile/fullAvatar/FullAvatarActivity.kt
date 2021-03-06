package com.denchic45.kts.ui.profile.fullAvatar

import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
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