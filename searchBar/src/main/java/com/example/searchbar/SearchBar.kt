package com.example.searchbar

import android.animation.AnimatorSet
import android.animation.PropertyValuesHolder
import android.animation.ValueAnimator
import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.graphics.drawable.Drawable
import android.text.Editable
import android.text.TextWatcher
import android.util.AttributeSet
import android.util.TypedValue
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AccelerateDecelerateInterpolator
import android.view.animation.DecelerateInterpolator
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.ViewSwitcher
import androidx.annotation.IntDef
import androidx.annotation.StringRes
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.graphics.drawable.DrawerArrowDrawable
import androidx.appcompat.widget.Toolbar
import androidx.cardview.widget.CardView
import androidx.core.content.res.ResourcesCompat
import androidx.drawerlayout.widget.DrawerLayout
import com.example.utils.DimensionUtils.dpToPx

class SearchBar : LinearLayout {
    private var onQueryTextListener: OnQueryTextListener? = null
    private lateinit var textView: TextView
    private lateinit var editText: EditText
    private var cardView: CardView? = null
    private var lManager: InputMethodManager? = null
    private var swInputText: ViewSwitcher? = null
    private lateinit var toolbar: Toolbar
    private var toggle: ActionBarDrawerToggle? = null
    private var drawerLayout: DrawerLayout? = null
    private var menuIcon: DrawerArrowDrawable? = null
    private var fontResId = 0
    private var letterSpacing = 0f
    private var hint: String? = null
    private var leftIcon: Drawable? = null
    private val leftIconBack: Drawable? = null
    private var inputMode = false
    private var leftActionMode = LEFT_ACTION_MODE_MENU
    private var expandable = 0
    private var expanded = false
    private var menuId = 0
    private var inputModeListener: OnInputModeListener? = null
    private var contentLayoutId = 0
    private var ignore = false

    constructor(context: Context) : super(context) {
        initView(context)
    }

    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs) {
        initAttributes(attrs)
        initView(context)
    }

    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    ) {
        initAttributes(attrs)
        initView(context)
    }

    constructor(
        context: Context,
        attrs: AttributeSet?,
        defStyleAttr: Int,
        defStyleRes: Int
    ) : super(context, attrs, defStyleAttr, defStyleRes) {
        initAttributes(attrs)
        initView(context)
    }

    private fun initAttributes(attrs: AttributeSet?) {
        if (attrs == null) {
            return
        }
        val ta = context.obtainStyledAttributes(attrs, R.styleable.SearchBar)
        leftActionMode =
            ta.getInt(R.styleable.SearchBar_searchBar_leftActionMode, LEFT_ACTION_MODE_MENU)
        leftIcon = ta.getDrawable(R.styleable.SearchBar_searchBar_leftIcon)
        contentLayoutId = ta.getResourceId(R.styleable.SearchBar_searchBar_content_layer, 0)
        menuId = ta.getResourceId(R.styleable.SearchBar_menu, 0)
        hint = ta.getString(R.styleable.SearchBar_android_hint)
        fontResId = ta.getResourceId(R.styleable.SearchBar_android_fontFamily, 0)
        letterSpacing = ta.getFloat(R.styleable.SearchBar_android_letterSpacing, 0f)
        expandable = ta.getInt(R.styleable.SearchBar_expandable, EXPANDABLE_ENABLE)
        ta.recycle()
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        addTopMargin(true)
    }

    private fun addTopMargin(add: Boolean) {
        if (contentLayoutId != 0) {
            val parent = parent
            val rootView = (parent as View).rootView
            val contentView = rootView.findViewById<ViewGroup>(contentLayoutId)
            val top = dpToPx(8, context)
            val params = contentView.layoutParams as MarginLayoutParams
            params.topMargin = if (add) -top else 0
            contentView.layoutParams = params
        }
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        onQueryTextListener = null
        addTopMargin(false)
    }

    private fun initView(context: Context) {
        lManager = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        val inflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        inflater.inflate(R.layout.search_bar, this)
        toolbar = findViewById(R.id.MaterialToolbar)
        swInputText = findViewById(R.id.viewSwitcher_search_input)
        cardView = findViewById(R.id.cardView_search)
        textView = findViewById(R.id.textView_search)
        editText = findViewById(R.id.editText_search)
        menuIcon = DrawerArrowDrawable(context)
        textView.letterSpacing = letterSpacing
        editText.letterSpacing = letterSpacing
        if (menuId != 0) toolbar.inflateMenu(menuId)
        if (fontResId != 0) {
            val font = ResourcesCompat.getFont(getContext(), fontResId)
            textView.typeface = font
            editText.typeface = font
        }
        if (hint != null) {
            textView.hint = hint
            editText.hint = hint
        }
        refreshLeftIcon()
        setToolbarLeftIconListener()
        if (expandable == EXPANDABLE_ALWAYS_EXPAND) {
            noAnimateExpanding(true)
        } else if (expandable == EXPANDABLE_ALWAYS_COLLAPSE) {
            noAnimateExpanding(false)
        }
        toolbar.setOnClickListener { setInputMode(true) }
        editText.setOnEditorActionListener { _: TextView?, actionId: Int, _: KeyEvent? ->
            if (ignore) return@setOnEditorActionListener false
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                if (onQueryTextListener != null) {
                    onQueryTextListener!!.onQueryTextSubmit(editText.text.toString())
                }
                lManager!!.hideSoftInputFromWindow(editText.windowToken, 0)
                lManager!!.hideSoftInputFromWindow(editText.windowToken, 0)
                return@setOnEditorActionListener true
            }
            false
        }
    }

    private fun setToolbarLeftIconListener() {
        toolbar.setNavigationOnClickListener { v: View? -> setInputMode(!inputMode) }
    }

    private fun animateProgressLeftIcon(progress: Float) {
        val anim = ValueAnimator.ofFloat(menuIcon!!.progress, progress)
        anim.addUpdateListener { valueAnimator: ValueAnimator ->
            menuIcon!!.progress = (valueAnimator.animatedValue as Float)
        }
        anim.interpolator = DecelerateInterpolator()
        anim.duration = 300
        anim.start()
    }

    private val activity: Activity?
        get() {
            var context = context
            while (context is ContextWrapper) {
                if (context is Activity) {
                    return context
                }
                context = context.baseContext
            }
            return null
        }

    private fun refreshLeftIcon() {
        if (inputMode) {
            setLeftIconBack()
        } else {
            setLeftIcon()
        }
    }

    private fun setLeftIconBack() {
        if (drawerLayout != null) {
            setToolbarLeftIconListener()
        }
        if (leftIconBack != null) {
            toolbar.navigationIcon = leftIconBack
            return
        }
        when (leftActionMode) {
            LEFT_ACTION_MODE_MENU -> animateProgressLeftIcon(1f)
            LEFT_ACTION_MODE_SEARCH -> toolbar.setNavigationIcon(R.drawable.ic_arrow_back)
            else -> toolbar.setNavigationIcon(R.drawable.ic_arrow_back)
        }
    }

    private fun setLeftIcon() {
        if (leftIcon != null) {
            toolbar.navigationIcon = leftIcon
            return
        }
        when (leftActionMode) {
            LEFT_ACTION_MODE_NONE -> toolbar.navigationIcon = null
            LEFT_ACTION_MODE_MENU -> {
                toolbar.navigationIcon = menuIcon
                if (drawerLayout != null) {
                    toggle = ActionBarDrawerToggle(
                        activity,
                        drawerLayout,
                        toolbar,
                        R.string.navigation_drawer_open,
                        R.string.navigation_drawer_close
                    )
                }
                animateProgressLeftIcon(0f)
            }
            LEFT_ACTION_MODE_SEARCH -> toolbar.navigationIcon =
                ResourcesCompat.getDrawable(resources, R.drawable.ic_search, null)
        }
    }

    fun setLeftIcon(drawable: Drawable?) {
        toolbar.navigationIcon = drawable
    }

    fun setLeftIcon(resId: Int) {
        toolbar.setNavigationIcon(resId)
    }

    fun attachNavigationDrawer(drawerLayout: DrawerLayout) {
        this.drawerLayout = drawerLayout
        toggle = ActionBarDrawerToggle(
            activity,
            drawerLayout,
            toolbar,
            R.string.navigation_drawer_open,
            R.string.navigation_drawer_close
        )
        toggle!!.syncState()
        menuIcon = toggle!!.drawerArrowDrawable
        if (!inputMode) drawerLayout.addDrawerListener(toggle!!)
    }

    fun detachNavigationDrawer() {
        drawerLayout!!.removeDrawerListener(toggle!!)
    }

    fun setInputMode(inputMode: Boolean) {
        this.inputMode = inputMode
        notifyInputModeListener()
        swInputText!!.showNext()
        toolbar.isClickable = !this.inputMode
        if (this.inputMode) {
            editText.requestFocusFromTouch()
            lManager!!.showSoftInput(editText, 0)
        } else {
            val inputtedText = editText.text.toString().trim { it <= ' ' }
            //            editText.setText(inputtedText);
            textView.text = inputtedText
            lManager!!.hideSoftInputFromWindow(editText.windowToken, 0)
        }
        refreshLeftIcon()
        setExpand(inputMode)
    }

    private fun notifyInputModeListener() {
        if (inputModeListener != null) {
            if (inputMode) {
                inputModeListener!!.onEnable()
            } else {
                inputModeListener!!.onDisable()
            }
        }
    }

    fun setExpandable(@Expandable expandable: Int) {
        if (expandable == EXPANDABLE_ALWAYS_EXPAND && !expanded) {
            animateExpanding(true)
        } else if (expandable == EXPANDABLE_ALWAYS_COLLAPSE && expanded) {
            animateExpanding(false)
        }
        this.expandable = expandable
    }

    fun setExpand(inputEnable: Boolean) {
        expanded = inputEnable
        when (expandable) {
            EXPANDABLE_ENABLE -> animateExpanding(inputEnable)
            EXPANDABLE_NO_ANIMATION -> noAnimateExpanding(inputEnable)
        }
    }

    fun setHint(hint: String?) {
        textView.hint = hint
        editText.hint = hint
    }

    fun setHint(@StringRes resid: Int) {
        textView.setHint(resid)
        editText.setHint(resid)
    }

    fun setText(text: String?) {
        textView.text = text
        editText.setText(text)
    }

    fun setText(@StringRes resid: Int) {
        textView.setText(resid)
        editText.setText(resid)
    }

    fun clearText() {
        textView.text = null
        editText.text = null
    }

    private fun noAnimateExpanding(expand: Boolean) {
        val layoutParams = cardView!!.layoutParams as MarginLayoutParams
        if (expand) {
            cardView!!.radius = 0f
            layoutParams.leftMargin = dpToPx(0, context)
            layoutParams.rightMargin = dpToPx(0, context)
            layoutParams.topMargin = dpToPx(0, context)
            layoutParams.bottomMargin = dpToPx(2, context)
        } else {
            cardView!!.radius =
                TypedValue.applyDimension(
                    TypedValue.COMPLEX_UNIT_DIP,
                    6f,
                    context.resources.displayMetrics
                )
            layoutParams.leftMargin = dpToPx(16, context)
            layoutParams.rightMargin = dpToPx(16, context)
            layoutParams.topMargin = dpToPx(6, context)
            layoutParams.bottomMargin = dpToPx(6, context)
        }
        cardView!!.layoutParams = layoutParams
    }

    fun inflateMenu(menuResId: Int) {
        toolbar.menu.clear()
        toolbar.inflateMenu(menuResId)
    }

    private fun animateExpanding(inputEnable: Boolean) {
        val marginsAnimator: ValueAnimator
        val radiusAnimator: ValueAnimator
        if (inputEnable) {
            marginsAnimator = createMarginsAnimator(
                dpToPx(0, context),
                dpToPx(0, context),
                dpToPx(0, context),
                dpToPx(2, context)
            )
            radiusAnimator = createRadiusAnimator(1f)
        } else {
            marginsAnimator = createMarginsAnimator(
                dpToPx(16, context),
                dpToPx(16, context),
                dpToPx(6, context),
                dpToPx(6, context)
            )
            radiusAnimator = createRadiusAnimator(
                TypedValue.applyDimension(
                    TypedValue.COMPLEX_UNIT_DIP,
                    6f,
                    context.resources.displayMetrics
                )
            )
        }
        val animatorSet = AnimatorSet()
        animatorSet.playTogether(marginsAnimator, radiusAnimator)
        animatorSet.duration = 300
        animatorSet.interpolator = AccelerateDecelerateInterpolator()
        animatorSet.start()
    }

    private fun createMarginsAnimator(left: Int, right: Int, top: Int, bottom: Int): ValueAnimator {
        val layoutParams = cardView!!.layoutParams as MarginLayoutParams
        val leftMargin = PropertyValuesHolder.ofInt("left", layoutParams.leftMargin, left)
        val rightMargin = PropertyValuesHolder.ofInt("right", layoutParams.rightMargin, right)
        val topMargin = PropertyValuesHolder.ofInt("top", layoutParams.topMargin, top)
        val bottomMargin = PropertyValuesHolder.ofInt("bottom", layoutParams.bottomMargin, bottom)
        val valueAnimator =
            ValueAnimator.ofPropertyValuesHolder(leftMargin, rightMargin, topMargin, bottomMargin)
        valueAnimator.addUpdateListener { animation: ValueAnimator? ->
            layoutParams.leftMargin = (valueAnimator.getAnimatedValue("left") as Int)
            layoutParams.rightMargin = (valueAnimator.getAnimatedValue("right") as Int)
            layoutParams.topMargin = (valueAnimator.getAnimatedValue("top") as Int)
            layoutParams.bottomMargin = (valueAnimator.getAnimatedValue("bottom") as Int)
            cardView!!.layoutParams = layoutParams
        }
        return valueAnimator
    }

    private fun createRadiusAnimator(radius: Float): ValueAnimator {
        val valueAnimator = ValueAnimator.ofFloat(cardView!!.radius, radius)
        valueAnimator.addUpdateListener { animation: ValueAnimator? ->
            cardView!!.radius = (valueAnimator.animatedValue as Float)
        }
        return valueAnimator
    }

    fun setOnInputModeListener(listener: OnInputModeListener?) {
        inputModeListener = listener
    }

    fun setOnMenuItemClickListener(listener: Toolbar.OnMenuItemClickListener?) {
        toolbar.setOnMenuItemClickListener(listener)
    }

    fun setOnQueryTextListener(listener: OnQueryTextListener) {
        onQueryTextListener = listener
        editText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                if (ignore) return
                listener.onQueryTextChange(s.toString())
            }

            override fun afterTextChanged(s: Editable) {}
        })
    }

    fun setIgnoreText(ignore: Boolean) {
        this.ignore = ignore
    }

    @IntDef(
        EXPANDABLE_ALWAYS_COLLAPSE,
        EXPANDABLE_ALWAYS_EXPAND,
        EXPANDABLE_ENABLE,
        EXPANDABLE_NO_ANIMATION
    )
    @Retention(AnnotationRetention.SOURCE)
    annotation class Expandable
    interface OnInputModeListener {
        fun onEnable()
        fun onDisable()
    }

    abstract class OnQueryTextListener {
        open fun onQueryTextSubmit(query: String) {}
        open fun onQueryTextChange(newText: String) {}
    }

    companion object {
        const val LEFT_ACTION_MODE_NONE = 0
        const val LEFT_ACTION_MODE_MENU = 1
        const val LEFT_ACTION_MODE_SEARCH = 2
        const val EXPANDABLE_ENABLE = 0
        const val EXPANDABLE_ALWAYS_COLLAPSE = 1
        const val EXPANDABLE_ALWAYS_EXPAND = 2
        const val EXPANDABLE_NO_ANIMATION = 3
        const val DELAY_EXPAND_ANIMATION = 300
    }
}