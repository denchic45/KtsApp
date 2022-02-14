package com.denchic45.kts.utils

import android.widget.Toast
import androidx.annotation.StringRes
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment

fun Fragment.setActivityTitle(@StringRes id: Int) {
    (activity as AppCompatActivity?)?.supportActionBar?.title = getString(id)
}

fun Fragment.setActivityTitle(title: String) {
    (activity as AppCompatActivity?)?.supportActionBar?.title = title
}

fun Fragment.toast(message: CharSequence, duration: Int = Toast.LENGTH_SHORT) {
    requireContext().toast(message, duration)
}