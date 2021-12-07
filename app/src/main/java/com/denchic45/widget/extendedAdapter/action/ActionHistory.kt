package com.denchic45.widget.extendedAdapter.action

import java.util.*

class ActionHistory {
    private val stack: Stack<Action> = Stack()

    fun add(action: Action) {
        stack.add(action.apply { execute() })
    }

    fun undo() {
        stack.pop().apply { undo() }
    }
}