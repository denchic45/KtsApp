package com.example.appbarcontroller.appbarcontroller

import android.animation.AnimatorInflater
import android.animation.StateListAnimator
import android.view.LayoutInflater
import android.view.View
import androidx.annotation.IdRes
import androidx.annotation.LayoutRes
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.example.appbarcontroller.R
import com.example.appbarcontroller.RecyclerViewFinishListener
import com.google.android.material.appbar.AppBarLayout
import org.jetbrains.annotations.Contract
import java.lang.ref.WeakReference

class AppBarController private constructor(
    activity: AppCompatActivity,
    appBarLayout: AppBarLayout
) {
    private val appBarLayout: AppBarLayout
    private val activity: AppCompatActivity
    private val views: MutableMap<Int, View> = HashMap()
    private val stateListAnimator: StateListAnimator
    lateinit var toolbar: Toolbar
        private set

    fun addView(@LayoutRes layoutId: Int): View {
        val view = LayoutInflater.from(activity).inflate(layoutId, appBarLayout, false)
        appBarLayout.addView(view)
        val viewId = view.id
        views[viewId] = view
        return view
    }

    fun addView(view: View, @AppBarLayout.LayoutParams.ScrollFlags scrollFlags: Int) {
        addView(view)
        setScrollFlags(view, scrollFlags)
    }

    fun addView(layoutId: Int, @AppBarLayout.LayoutParams.ScrollFlags scrollFlags: Int) {
        val view = addView(layoutId)
        setScrollFlags(view, scrollFlags)
    }

    fun addView(view: View) {
        appBarLayout.addView(view)
        val id = view.id
        views[id] = view
    }

    var toolbarScrollFlags: Int
        get() = (toolbar.layoutParams as AppBarLayout.LayoutParams).scrollFlags
        set(scrollFlags) {
            setScrollFlags(toolbar, scrollFlags)
        }

    private fun setScrollFlags(
        view: View,
        @AppBarLayout.LayoutParams.ScrollFlags scrollFlags: Int
    ) {
        val params = AppBarLayout.LayoutParams(view.layoutParams)
        params.scrollFlags = scrollFlags
        view.layoutParams = params
    }

    fun setExpanded(expand: Boolean, animate: Boolean) {
        appBarLayout.setExpanded(expand, animate)
    }

    fun setExpandableIfViewCanScroll(view: View, lifecycleOwner: LifecycleOwner) {
        val viewWeakReference = WeakReference(view)
        if (viewWeakReference.get() is RecyclerView) {
            expandAppbarIfNecessary((viewWeakReference.get() as RecyclerView?)!!)
            val adapter = (viewWeakReference.get() as RecyclerView?)!!.adapter
            val observer: RecyclerView.AdapterDataObserver =
                object : RecyclerView.AdapterDataObserver() {
                    override fun onChanged() {
                        expandAppbarIfNecessary((viewWeakReference.get() as RecyclerView?)!!)
                        super.onChanged()
                    }

                    override fun onItemRangeInserted(positionStart: Int, itemCount: Int) {
                        expandAppbarIfNecessary((viewWeakReference.get() as RecyclerView?)!!)
                        super.onItemRangeInserted(positionStart, itemCount)
                    }

                    override fun onItemRangeRemoved(positionStart: Int, itemCount: Int) {
                        expandAppbarIfNecessary((viewWeakReference.get() as RecyclerView?)!!)
                        super.onItemRangeRemoved(positionStart, itemCount)
                    }

                    override fun onItemRangeMoved(
                        fromPosition: Int,
                        toPosition: Int,
                        itemCount: Int
                    ) {
                        expandAppbarIfNecessary((viewWeakReference.get() as RecyclerView?)!!)
                        super.onItemRangeMoved(fromPosition, toPosition, itemCount)
                    }
                }
            assert(adapter != null)
            adapter!!.registerAdapterDataObserver(observer)
            lifecycleOwner.lifecycle.addObserver(object : LifecycleEventObserver {
                override fun onStateChanged(source: LifecycleOwner, event: Lifecycle.Event) {
                    if (event == Lifecycle.Event.ON_DESTROY) {
                        adapter.unregisterAdapterDataObserver(observer)
                        lifecycleOwner.lifecycle.removeObserver(this)
                        viewWeakReference.clear()
                        removeDragCallback()
                    }
                }
            })
        }
    }

    private fun expandAppbarIfNecessary(recyclerView: RecyclerView) {
        RecyclerViewFinishListener(recyclerView) {
            val params = appBarLayout.layoutParams as CoordinatorLayout.LayoutParams
            val behavior = (params.behavior as AppBarLayout.Behavior?)!!
            behavior.setDragCallback(object : AppBarLayout.Behavior.DragCallback() {
                override fun canDrag(appBarLayout: AppBarLayout): Boolean {
                    return recyclerView.isNestedScrollingEnabled
                }
            })
            val canScrollDown = recyclerView.canScrollVertically(1)
            val canScrollUp = recyclerView.canScrollVertically(-1)
            if (!canScrollDown && !canScrollUp) {
                appBarLayout.setExpanded(true, true)
                recyclerView.isNestedScrollingEnabled = false
            } else {
                recyclerView.isNestedScrollingEnabled = true
            }
        }
    }

    private fun removeDragCallback() {
        val params = appBarLayout.layoutParams as CoordinatorLayout.LayoutParams
        val behavior = (params.behavior as AppBarLayout.Behavior?)!!
        behavior.setDragCallback(null)
    }

    private fun checkScrollableRecyclerView(
        recyclerView: RecyclerView,
        adapter: RecyclerView.Adapter<*>?
    ): Boolean {
        if (adapter != null) {
            if (adapter.itemCount == 0) {
                return false
            }
            val layoutManager = recyclerView.layoutManager
            if (layoutManager != null) {
                var lastVisibleItem = 0
                var firstVisibleItem = 0
                if (layoutManager is LinearLayoutManager) {
                    val linearLayoutManager = layoutManager
                    lastVisibleItem =
                        Math.abs(linearLayoutManager.findLastCompletelyVisibleItemPosition())
                    firstVisibleItem =
                        Math.abs(linearLayoutManager.findFirstCompletelyVisibleItemPosition())
                } else if (layoutManager is StaggeredGridLayoutManager) {
                    val staggeredGridLayoutManager = layoutManager
                    val lastItems =
                        staggeredGridLayoutManager.findLastCompletelyVisibleItemPositions(
                            IntArray(staggeredGridLayoutManager.spanCount)
                        )
                    val firstItems =
                        staggeredGridLayoutManager.findFirstCompletelyVisibleItemPositions(
                            IntArray(staggeredGridLayoutManager.spanCount)
                        )
                    lastVisibleItem = Math.abs(lastItems[lastItems.size - 1])
                    firstVisibleItem = Math.abs(firstItems[firstItems.size - 1])
                }
                return lastVisibleItem < adapter.itemCount - 1 || firstVisibleItem > 0
            } else if (adapter.itemCount == 0) {
                return false
            }
        }
        return false
    }

    fun <T : View?> getView(@IdRes viewId: Int): T? {
        return views[viewId] as T?
    }

    private fun findToolbar() {
        for (i in 0 until appBarLayout.childCount) {
            val view = appBarLayout.getChildAt(i)
            if (view is Toolbar) {
                toolbar = view
                activity.setSupportActionBar(toolbar)
            }
        }
    }

    private fun observeLifecycle(lifecycle: Lifecycle) {
        lifecycle.addObserver(LifecycleEventObserver { _, event: Lifecycle.Event ->
            if (event == Lifecycle.Event.ON_DESTROY) {
                controllers.remove(lifecycle)
            }
        })
    }

    fun setToolbar(@LayoutRes resId: Int) {
        val view = LayoutInflater.from(appBarLayout.context).inflate(resId, null)
        if (hasToolbar() && toolbar.javaClass == view.javaClass) {
            return
        }
        if (view is Toolbar) {
            if (hasToolbar()) {
                appBarLayout.removeView(toolbar)
            }
            toolbar = view
            appBarLayout.addView(toolbar)
            activity.setSupportActionBar(toolbar)
        } else {
            throw RuntimeException("View does not instance of toolbar: " + view.javaClass)
        }
    }

    fun hasToolbar(): Boolean {
        return toolbar != null
    }

    fun setTitle(title: String) {
        activity.title = title
    }

    fun removeView(view: View) {
        views.remove(view.id)
        appBarLayout.removeView(view)
    }

    fun showToolbar() {
        appBarLayout.setExpanded(true, true)
    }

    fun setLiftOnScroll(lifOnScroll: Boolean) {
        appBarLayout.isLiftOnScroll = lifOnScroll
        if (!lifOnScroll) appBarLayout.stateListAnimator = AnimatorInflater.loadStateListAnimator(
            appBarLayout.context,
            R.animator.appbar_elevation_off
        ) else appBarLayout.stateListAnimator = stateListAnimator
    }

    enum class EXPAND {
        IF_CAN_SCROLL, ALWAYS, DISABLE
    }

    companion object {
        private val controllers = HashMap<Lifecycle, AppBarController>()
        fun findController(lifecycleOwner: LifecycleOwner): AppBarController {
            return controllers[lifecycleOwner.lifecycle]
                ?: throw RuntimeException("Controller does not exist")
        }

        @Contract("_, _ -> new")
        fun create(activity: AppCompatActivity, appBarLayout: AppBarLayout): AppBarController {
            return AppBarController(activity, appBarLayout)
        }
    }

    init {
        val lifecycle = activity.lifecycle
        controllers[lifecycle] = this
        this.appBarLayout = appBarLayout
        stateListAnimator = appBarLayout.stateListAnimator
        this.activity = activity
        observeLifecycle(lifecycle)
        findToolbar()
    }
}