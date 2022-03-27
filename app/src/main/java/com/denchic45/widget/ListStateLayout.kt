package com.denchic45.widget

import android.content.Context
import android.os.Handler
import android.os.Looper
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.ViewFlipper
import androidx.annotation.LayoutRes
import androidx.recyclerview.widget.RecyclerView
import com.denchic45.kts.R

class ListStateLayout : FrameLayout {
    private val stateViews: MutableMap<String, View> = HashMap()
    private var contentView: View? = null
    private var viewFlipper: ViewFlipper? = null
    private var showContent = false

    constructor(context: Context) : super(context) {
        init()
    }

    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs) {
        init()
        initAttributes(attrs)
    }

    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    ) {
        init()
        initAttributes(attrs)
    }

    constructor(
        context: Context,
        attrs: AttributeSet?,
        defStyleAttr: Int,
        defStyleRes: Int
    ) : super(context, attrs, defStyleAttr, defStyleRes) {
        init()
        initAttributes(attrs)
    }

    private fun init() {
        post {
            check(childCount == ONE_CHILD_VIEW_AND_VIEWFLIPPER) { "Must have only 1 view" }
            contentView = getChildAt(1)
        }
        viewFlipper = ViewFlipper(context)
        viewFlipper!!.layoutParams = LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.MATCH_PARENT
        )
        addView(viewFlipper)
        addView(View(context), NULL_VIEW)
        setDefaultAnimation()
    }

    private fun initAttributes(attrs: AttributeSet?) {
        if (attrs == null) {
            return
        }
        val ta = context.obtainStyledAttributes(attrs, R.styleable.ListStateLayout)
        val emptyViewId = ta.getResourceId(R.styleable.ListStateLayout_emptyView, -1)
        if (emptyViewId > -1) {
            addView(inflateView(emptyViewId), EMPTY_VIEW)
        }
        val loadingViewId = ta.getResourceId(R.styleable.ListStateLayout_loadingView, -1)
        if (loadingViewId > -1) {
            addView(inflateView(loadingViewId), LOADING_VIEW)
        }
        val errorViewId = ta.getResourceId(R.styleable.ListStateLayout_errorView, -1)
        if (errorViewId > -1) {
            addView(inflateView(errorViewId), ERROR_VIEW)
        }
        val networkViewId = ta.getResourceId(R.styleable.ListStateLayout_networkView, -1)
        if (networkViewId > -1) {
            addView(inflateView(networkViewId), NETWORK_VIEW)
        }
        ta.recycle()
    }

    fun showView(viewKey: String) {
        showContent = false
        if (stateViews.containsKey(viewKey) && viewFlipper!!.displayedChild != viewFlipper!!.indexOfChild(
                stateViews[viewKey]
            )
        ) {
            if (contentView is RecyclerView) {
                (contentView as RecyclerView).post(object : Runnable {
                    override fun run() {
                        if ((contentView as RecyclerView).isAnimating) {
                            (contentView as RecyclerView).itemAnimator!!
                                .isRunning { Handler(Looper.getMainLooper()).post(this) }
                            return
                        }
                        if (!showContent) viewFlipper!!.displayedChild = viewFlipper!!.indexOfChild(
                            stateViews[viewKey]
                        )
                    }
                })
            } else {
                viewFlipper!!.displayedChild = viewFlipper!!.indexOfChild(stateViews[viewKey])
            }
        }
    }

    fun showList() {
        if (showContent) return
        showContent = true
        viewFlipper!!.displayedChild = 0
    }

    fun getCommitCallback(adapter: RecyclerView.Adapter<*>): ListChangeListener {
        return ListChangeListener(adapter)
    }

    private fun setDefaultAnimation() {
        viewFlipper!!.setInAnimation(context, R.anim.fade_in)
        viewFlipper!!.setOutAnimation(context, R.anim.fade_out)
    }

    fun addView(@LayoutRes layoutId: Int, viewKey: String) {
        if (layoutId != 0) addView(inflateView(layoutId), viewKey)
    }

    fun addView(view: View?, viewKey: String) {
        if (view != null) {
            viewFlipper!!.addView(view)
            stateViews[viewKey] = view
        }
    }

    fun getStateView(viewKey: String): View? {
        return stateViews[viewKey]
    }

    private fun inflateView(@LayoutRes layoutId: Int): View {
        return LayoutInflater.from(context).inflate(layoutId, null)
    }

    private fun checkAndShowEmptyView(adapter: RecyclerView.Adapter<*>) {
        if (adapter.itemCount == 0) {
            showView(EMPTY_VIEW)
        } else {
            showList()
        }
    }

    fun listenAdapterDataObserver(adapter: RecyclerView.Adapter<*>) {
        adapter.registerAdapterDataObserver(object : RecyclerView.AdapterDataObserver() {
            override fun onChanged() {
                checkAndShowEmptyView(adapter)
            }

            override fun onItemRangeChanged(positionStart: Int, itemCount: Int) {
                checkAndShowEmptyView(adapter)
            }

            override fun onItemRangeInserted(positionStart: Int, itemCount: Int) {
                checkAndShowEmptyView(adapter)
            }

            override fun onItemRangeRemoved(positionStart: Int, itemCount: Int) {
                checkAndShowEmptyView(adapter)
            }

            override fun onItemRangeMoved(fromPosition: Int, toPosition: Int, itemCount: Int) {
                checkAndShowEmptyView(adapter)
            }
        })
    }

    inner class ListChangeListener(private val adapter: RecyclerView.Adapter<*>) : Runnable {
        override fun run() {
            checkAndShowEmptyView(adapter)
        }
    }

    companion object {
        const val EMPTY_VIEW = "EMPTY_VIEW"
        const val ERROR_VIEW = "ERROR_VIEW"
        const val LOADING_VIEW = "LOADING_VIEW"
        const val NETWORK_VIEW = "NETWORK_VIEW"
        const val START_VIEW = "START_VIEW"
        const val ONE_CHILD_VIEW_AND_VIEWFLIPPER = 2
        private const val NULL_VIEW = "NULL_VIEW"
    }
}