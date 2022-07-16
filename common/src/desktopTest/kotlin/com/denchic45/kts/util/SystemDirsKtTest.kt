package com.denchic45.kts.util

import kotlin.test.Test

 class SystemDirsKtTest {

    @Test
    fun appDirTest() {
        val dir = appDirectory
        println("Directory is: $dir")
    }
}