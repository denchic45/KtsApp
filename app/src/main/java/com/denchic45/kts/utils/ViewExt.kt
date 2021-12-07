package com.denchic45.kts.utils

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.viewbinding.ViewBinding

fun View.getLayoutInflater(attachToParent : Boolean = false): LayoutInflater = LayoutInflater.from(context)


inline fun <T : ViewBinding> ViewGroup.viewBinding(crossinline bindingInflater: (LayoutInflater, ViewGroup, Boolean) -> T, attachToParent: Boolean = false) =
    bindingInflater.invoke(LayoutInflater.from(this.context), this, attachToParent)