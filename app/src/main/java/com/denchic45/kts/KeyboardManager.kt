package com.denchic45.kts

import android.content.Context
import android.util.Log
import android.view.View
import android.view.ViewTreeObserver.OnGlobalLayoutListener
import android.view.inputmethod.InputMethodManager

class KeyboardManager {
    private var mContentView: View? = null
    private var mOnGlobalLayoutListener: OnGlobalLayoutListener? = null
    private var mIsKeyboardVisible = false
    val keyBoardVisible: Boolean
        get() {
         return mIsKeyboardVisible
        }

    fun registerKeyboardListener(view: View, listener: (Boolean)->Unit) {
        mContentView = view
        unregisterKeyboardListener()
        mOnGlobalLayoutListener = object : OnGlobalLayoutListener {
            private var mPreviousHeight = 0
            override fun onGlobalLayout() {
                val newHeight = mContentView!!.height
                if (mPreviousHeight != 0) {
                    if (mPreviousHeight < newHeight) {
                        // In this place keyboard is hidden but navigation bar is appeared
                        // will hide it
                        Log.d(TAG, "onLayoutChangedDown")
                        if (mIsKeyboardVisible) {
                            mIsKeyboardVisible = false
                            listener(false)
                        }
                    } else if (mPreviousHeight > newHeight) {
                        // This block will be called when navigation bar is appeared
                        // There are two cases:
                        // 1. When something modal view (like dialog) is appeared
                        // 2. When keyboard is appeared
                        Log.d(TAG, "onLayoutChangedUp")

                        // Will ask InputMethodManager.isAcceptingText() to detect if keyboard appeared or not.
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

//    interface OnKeyboardListener {
//        fun onKeyboardVisible()
//        fun onKeyboardHidden()
//    }

    companion object {
        private val TAG = KeyboardManager::class.java.simpleName
    }
}