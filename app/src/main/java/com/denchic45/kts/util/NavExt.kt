package com.denchic45.kts.util

import androidx.annotation.IdRes
import androidx.fragment.app.FragmentActivity
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.fragment.findNavController

fun FragmentActivity.findFragmentContainerNavController(@IdRes host: Int): NavController {
    try {
        val navHostFragment = supportFragmentManager.findFragmentById(host) as NavHostFragment
        return navHostFragment.findNavController()
    } catch (e: Exception) {
        throw IllegalStateException("Activity $this does not have a NavController set on $host")
    }
}