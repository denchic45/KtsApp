//package com.denchic45.kts.ui.courseEditor
//
//import android.os.Bundle
//import android.view.Menu
//import android.view.MenuItem
//import androidx.activity.viewModels
//import androidx.appcompat.app.AppCompatActivity
//import androidx.navigation.Navigation
//import by.kirich1409.viewbindingdelegate.viewBinding
//import com.denchic45.kts.R
//import com.denchic45.kts.databinding.ActivityCourseEditorBinding
//import com.example.appbarcontroller.appbarcontroller.AppBarController
//import com.google.android.material.snackbar.Snackbar
//
//
//class CourseEditorActivity : AppCompatActivity(R.layout.activity_course_editor) {
//
//    private val viewBinding: ActivityCourseEditorBinding by viewBinding(ActivityCourseEditorBinding::bind)
//    private val viewModel: CourseEditorViewModel by viewModels()
//    private var menu: Menu? = null
//
//    override fun onCreate(savedInstanceState: Bundle?) {
//        super.onCreate(savedInstanceState)
////        val navController = Navigation.findNavController(this, R.id.nav_host_fragment)
//        viewBinding.apply {
//            setSupportActionBar(toolbar)
//            AppBarController.create(this@CourseEditorActivity, appBar)
//            setSupportActionBar(toolbar)
//        }
//        supportActionBar!!.setDisplayHomeAsUpEnabled(true)
//        viewModel.finish.observe(this) { finish() }
//
//        viewModel.optionVisibility.observe(this) { (id, visibility)->
//            menu?.findItem(id)?.isVisible = visibility
//        }
//
//        viewModel.showMessage.observe(this) {
//            Snackbar.make(viewBinding.root, it, Snackbar.LENGTH_SHORT).show()
//        }
//    }
//
//    override fun onCreateOptionsMenu(menu: Menu): Boolean {
//        menuInflater.inflate(R.menu.options_course_editor, menu)
//        this@CourseEditorActivity.menu = menu
//        viewModel.onCreateOptions()
//        return true
//    }
//
//    override fun onOptionsItemSelected(item: MenuItem): Boolean {
//        viewModel.onOptionClick(item.itemId)
//        return true
//    }
//
//
//}