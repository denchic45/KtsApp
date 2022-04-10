package com.denchic45.kts

import android.content.Context
import android.view.View
import android.view.ViewTreeObserver.OnGlobalLayoutListener
import android.view.inputmethod.InputMethodManager

class KeyboardManager {
    private var mContentView: View? = null
    private var mOnGlobalLayoutListener: OnGlobalLayoutListener? = null
    private var mIsKeyboardVisible = false
    val keyBoardVisible: Boolean
        get() = mIsKeyboardVisible

    fun registerKeyboardListener(view: View, listener: (Boolean) -> Unit) {
        mContentView = view
        unregisterKeyboardListener()
        mOnGlobalLayoutListener = object : OnGlobalLayoutListener {
            private var mPreviousHeight = 0
            override fun onGlobalLayout() {
                val newHeight = mContentView!!.height
                if (mPreviousHeight != 0) {
                    if (mPreviousHeight < newHeight) {
                        if (mIsKeyboardVisible) {
                            mIsKeyboardVisible = false
                            listener(false)
                        }
                    } else if (mPreviousHeight > newHeight) {

                        val imm = mContentView!!.context
                            .getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                        val isAcceptingText = imm.isAcceptingText
                        if (isAcceptingText) {
                            mIsKeyboardVisible = true
                        }
                        if (mIsKeyboardVisible) {
                            listener(true)
                        }
                    }
                }
                mPreviousHeight = newHeight
            }
        }
        mContentView!!.viewTreeObserver.addOnGlobalLayoutListener(mOnGlobalLayoutListener)
    }

    fun unregisterKeyboardListener() {
        if (mOnGlobalLayoutListener != null) {
            mContentView!!.viewTreeObserver.removeOnGlobalLayoutListener(mOnGlobalLayoutListener)
        }
    }
}