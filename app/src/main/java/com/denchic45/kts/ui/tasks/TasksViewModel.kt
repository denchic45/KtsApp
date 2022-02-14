package com.denchic45.kts.ui.tasks

import com.denchic45.kts.ui.base.BaseViewModel
import javax.inject.Inject

class TasksViewModel @Inject constructor(

) : BaseViewModel() {

    val tabNames: Array<String> = arrayOf("Предстоящие","Просроченные","Выполненные")

    fun onOptionClick(itemId: Int) {
        TODO("Not yet implemented")
    }
}