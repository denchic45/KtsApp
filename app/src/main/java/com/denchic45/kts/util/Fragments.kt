package com.denchic45.kts.util

import android.widget.Toast
import androidx.annotation.StringRes
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.denchic45.kts.BuildConfig

fun Fragment.setActivityTitle(@StringRes id: Int) {
    (activity as AppCompatActivity?)?.supportActionBar?.title = getString(id)
}

fun Fragment.setActivityTitle(title: String) {
    (activity as AppCompatActivity?)?.supportActionBar?.title = title
}

fun Fragment.toast(message: CharSequence, duration: Int = Toast.LENGTH_SHORT) {
    requireContext().toast(message, duration)
}

fun Fragment.toast(messageRes: Int, duration: Int = Toast.LENGTH_SHORT) {
    requireContext().toast(messageRes, duration)
}

fun Fragment.debugToast(message: CharSequence, duration: Int = Toast.LENGTH_SHORT) {
    if (BuildConfig.DEBUG)
        requireContext().toast(message, duration)
}

fun Fragment.debugToast(messageRes: Int, duration: Int = Toast.LENGTH_SHORT) {
    if (BuildConfig.DEBUG)
        requireContext().toast(messageRes, duration)
}