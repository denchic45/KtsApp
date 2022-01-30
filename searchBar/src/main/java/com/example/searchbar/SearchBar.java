package com.example.searchbar;

import android.animation.AnimatorSet;
import android.animation.PropertyValuesHolder;
import android.animation.ValueAnimator;
import android.app.Activity;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.res.TypedArray;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.DecelerateInterpolator;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.ViewSwitcher;

import androidx.annotation.IntDef;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.StringRes;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.graphics.drawable.DrawerArrowDrawable;
import androidx.appcompat.widget.Toolbar;
import androidx.cardview.widget.CardView;
import androidx.core.content.res.ResourcesCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import com.example.utils.DimensionUtils;

import org.jetbrains.annotations.NotNull;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

public class SearchBar extends LinearLayout {

    public static final int LEFT_ACTION_MODE_NONE = 0;
    public static final int LEFT_ACTION_MODE_MENU = 1;
    public static final int LEFT_ACTION_MODE_SEARCH = 2;

    public static final int EXPANDABLE_ENABLE = 0;
    public static final int EXPANDABLE_ALWAYS_COLLAPSE = 1;
    public static final int EXPANDABLE_ALWAYS_EXPAND = 2;
    public static final int EXPANDABLE_NO_ANIMATION = 3;

    public static final int DELAY_EXPAND_ANIMATION = 300;

    private OnQueryTextListener onQueryTextListener;
    private TextView textView;
    private EditText editText;
    private CardView cardView;

    private InputMethodManager lManager;
    private ViewSwitcher swInputText;
    private Toolbar toolbar;
    private ActionBarDrawerToggle toggle;
    private DrawerLayout drawerLayout;
    private DrawerArrowDrawable menuIcon;
    private int fontResId;
    private float letterSpacing;
    private String hint;

    private Drawable leftIcon, leftIconBack;

    private boolean inputMode;
    private int leftActionMode = LEFT_ACTION_MODE_MENU;
    private int expandable;
    private boolean expanded;
    private int menuId;
    private OnInputModeListener inputModeListener;
    private int contentLayoutId;
    private boolean ignore;

    public SearchBar(Context context) {
        super(context);
        initView(context);
    }

    public SearchBar(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        initAttributes(attrs);
        initView(context);
    }

    public SearchBar(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initAttributes(attrs);
        initView(context);
    }

    public SearchBar(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        initAttributes(attrs);
        initView(context);
    }

    private void initAttributes(AttributeSet attrs) {
        if (attrs == null) {
            return;
        }
        TypedArray ta = getContext().obtainStyledAttributes(attrs, R.styleable.SearchBar);

        leftActionMode = ta.getInt(R.styleable.SearchBar_searchBar_leftActionMode, LEFT_ACTION_MODE_MENU);
        leftIcon = ta.getDrawable(R.styleable.SearchBar_searchBar_leftIcon);

        contentLayoutId = ta.getResourceId(R.styleable.SearchBar_searchBar_content_layer, 0);

        menuId = ta.getResourceId(R.styleable.SearchBar_menu, 0);

        hint = ta.getString(R.styleable.SearchBar_android_hint);
        fontResId = ta.getResourceId(R.styleable.SearchBar_android_fontFamily, 0);
        letterSpacing = ta.getFloat(R.styleable.SearchBar_android_letterSpacing, 0f);
        expandable = ta.getInt(R.styleable.SearchBar_expandable, EXPANDABLE_ENABLE);
        ta.recycle();
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        addTopMargin(true);
    }

    private void addTopMargin(boolean add) {
        if (contentLayoutId != 0) {
            ViewParent parent = getParent();
            View rootView = ((View) parent).getRootView();
            ViewGroup contentView = rootView.findViewById(contentLayoutId);

            int top = DimensionUtils.dpToPx(8, getContext());

            MarginLayoutParams params =
                    (MarginLayoutParams) contentView.getLayoutParams();

            params.topMargin = add ? -top : 0;
            contentView.setLayoutParams(params);
        }
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        onQueryTextListener = null;
        addTopMargin(false);
    }

    private void initView(@NotNull Context context) {
        lManager = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);

        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        inflater.inflate(R.layout.search_bar, this);

        toolbar = findViewById(R.id.MaterialToolbar);
        swInputText = findViewById(R.id.viewSwitcher_search_input);
        cardView = findViewById(R.id.cardView_search);
        textView = findViewById(R.id.textView_search);
        editText = findViewById(R.id.editText_search);
        menuIcon = new DrawerArrowDrawable(context);

        textView.setLetterSpacing(letterSpacing);
        editText.setLetterSpacing(letterSpacing);

        if (menuId != 0)
            toolbar.inflateMenu(menuId);

        if (fontResId != 0) {
            Typeface font = ResourcesCompat.getFont(getContext(), fontResId);
            textView.setTypeface(font);
            editText.setTypeface(font);
        }

        if (hint != null) {
            textView.setHint(hint);
            editText.setHint(hint);
        }

        refreshLeftIcon();
        setToolbarLeftIconListener();

        if (expandable == EXPANDABLE_ALWAYS_EXPAND) {
            noAnimateExpanding(true);
        } else if (expandable == EXPANDABLE_ALWAYS_COLLAPSE) {
            noAnimateExpanding(false);
        }

        toolbar.setOnClickListener(v -> setInputMode(true));

        editText.setOnEditorActionListener((v, actionId, event) -> {
            if (ignore) return false;
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                if (onQueryTextListener != null) {
                    onQueryTextListener.onQueryTextSubmit(editText.getText().toString());
                }
                lManager.hideSoftInputFromWindow(editText.getWindowToken(), 0);
                lManager.hideSoftInputFromWindow(editText.getWindowToken(), 0);
                return true;
            }
            return false;
        });
    }

    private void setToolbarLeftIconListener() {
        toolbar.setNavigationOnClickListener(v -> setInputMode(!inputMode));
    }

    private void animateProgressLeftIcon(float progress) {
        ValueAnimator anim = ValueAnimator.ofFloat(menuIcon.getProgress(), progress);
        anim.addUpdateListener(valueAnimator ->
                menuIcon.setProgress((Float) valueAnimator.getAnimatedValue()));
        anim.setInterpolator(new DecelerateInterpolator());
        anim.setDuration(300);
        anim.start();
    }

    @Nullable
    private Activity getActivity() {
        Context context = getContext();
        while (context instanceof ContextWrapper) {
            if (context instanceof Activity) {
                return (Activity) context;
            }
            context = ((ContextWrapper) context).getBaseContext();
        }
        return null;
    }

    private void refreshLeftIcon() {
        if (inputMode) {
            setLeftIconBack();
        } else {
            setLeftIcon();
        }
    }

    private void setLeftIconBack() {

        if (drawerLayout != null) {
            setToolbarLeftIconListener();
        }

        if (leftIconBack != null) {
            toolbar.setNavigationIcon(leftIconBack);
            return;
        }

        switch (leftActionMode) {

            case LEFT_ACTION_MODE_MENU:
                animateProgressLeftIcon(1);
                break;
            case LEFT_ACTION_MODE_SEARCH:
            default:
                toolbar.setNavigationIcon(R.drawable.ic_arrow_back);
        }
    }

    private void setLeftIcon() {

        if (leftIcon != null) {
            toolbar.setNavigationIcon(leftIcon);
            return;
        }

        switch (leftActionMode) {
            case LEFT_ACTION_MODE_NONE:
                toolbar.setNavigationIcon(null);
                break;
            case LEFT_ACTION_MODE_MENU:
                toolbar.setNavigationIcon(menuIcon);
                if (drawerLayout != null) {
                    toggle = new ActionBarDrawerToggle(
                            getActivity(), drawerLayout, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
                }
                animateProgressLeftIcon(0);
                break;
            case LEFT_ACTION_MODE_SEARCH:
                toolbar.setNavigationIcon(ResourcesCompat.getDrawable(getResources(), R.drawable.ic_search, null));
                break;
        }
    }

    public void setLeftIcon(Drawable drawable) {
        toolbar.setNavigationIcon(drawable);
    }

    public void setLeftIcon(int resId) {
        toolbar.setNavigationIcon(resId);
    }

    public void attachNavigationDrawer(@NonNull DrawerLayout drawerLayout) {
        this.drawerLayout = drawerLayout;
        toggle = new ActionBarDrawerToggle(
                getActivity(), drawerLayout, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        toggle.syncState();
        menuIcon = toggle.getDrawerArrowDrawable();
        if (!inputMode)
            drawerLayout.addDrawerListener(toggle);
    }

    public void detachNavigationDrawer() {
        drawerLayout.removeDrawerListener(toggle);
    }

    public void setInputMode(boolean inputMode) {
        this.inputMode = inputMode;
        notifyInputModeListener();

        swInputText.showNext();
        toolbar.setClickable(!this.inputMode);
        if (this.inputMode) {
            editText.requestFocusFromTouch();
            lManager.showSoftInput(editText, 0);
        } else {
            final String inputtedText = editText.getText().toString().trim();
//            editText.setText(inputtedText);
            textView.setText(inputtedText);
            lManager.hideSoftInputFromWindow(editText.getWindowToken(), 0);
        }

        refreshLeftIcon();
        setExpand(inputMode);
    }

    private void notifyInputModeListener() {
        if (inputModeListener != null) {
            if (inputMode) {
                inputModeListener.onEnable();
            } else {
                inputModeListener.onDisable();
            }
        }
    }

    public void setExpandable(@Expandable int expandable) {
        if (expandable == EXPANDABLE_ALWAYS_EXPAND && !expanded) {
            animateExpanding(true);
        } else if (expandable == EXPANDABLE_ALWAYS_COLLAPSE && expanded) {
            animateExpanding(false);
        }
        this.expandable = expandable;
    }

    public void setExpand(boolean inputEnable) {
        expanded = inputEnable;
        switch (expandable) {
            case EXPANDABLE_ENABLE:
                animateExpanding(inputEnable);
                break;
            case EXPANDABLE_NO_ANIMATION:
                noAnimateExpanding(inputEnable);
                break;
        }
    }

    public void setHint(String hint) {
        textView.setHint(hint);
        editText.setHint(hint);
    }

    public void setHint(@StringRes int resid) {
        textView.setHint(resid);
        editText.setHint(resid);
    }

    public void setText(String text) {
        textView.setText(text);
        editText.setText(text);
    }

    public void setText(@StringRes int resid) {
        textView.setText(resid);
        editText.setText(resid);
    }

    public void clearText() {
        textView.setText(null);
        editText.setText(null);
    }

    private void noAnimateExpanding(boolean expand) {
        MarginLayoutParams layoutParams = ((MarginLayoutParams) cardView.getLayoutParams());
        if (expand) {
            cardView.setRadius(0);
            layoutParams.leftMargin = DimensionUtils.dpToPx(0, getContext());
            layoutParams.rightMargin = DimensionUtils.dpToPx(0, getContext());
            layoutParams.topMargin = DimensionUtils.dpToPx(0, getContext());
            layoutParams.bottomMargin = DimensionUtils.dpToPx(2, getContext());
        } else {
            cardView.setRadius(TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 6, getContext().getResources().getDisplayMetrics()));
            layoutParams.leftMargin = DimensionUtils.dpToPx(16, getContext());
            layoutParams.rightMargin = DimensionUtils.dpToPx(16, getContext());
            layoutParams.topMargin = DimensionUtils.dpToPx(6, getContext());
            layoutParams.bottomMargin = DimensionUtils.dpToPx(6, getContext());
        }
        cardView.setLayoutParams(layoutParams);
    }

    public void inflateMenu(int menuResId) {
        toolbar.getMenu().clear();
        toolbar.inflateMenu(menuResId);
    }

    private void animateExpanding(boolean inputEnable) {
        ValueAnimator marginsAnimator, radiusAnimator;
        if (inputEnable) {
            marginsAnimator = createMarginsAnimator(
                    DimensionUtils.dpToPx(0, getContext()),
                    DimensionUtils.dpToPx(0, getContext()),
                    DimensionUtils.dpToPx(0, getContext()),
                    DimensionUtils.dpToPx(2, getContext())
            );
            radiusAnimator = createRadiusAnimator(1f);
        } else {
            marginsAnimator = createMarginsAnimator(
                    DimensionUtils.dpToPx(16, getContext()),
                    DimensionUtils.dpToPx(16, getContext()),
                    DimensionUtils.dpToPx(6, getContext()),
                    DimensionUtils.dpToPx(6, getContext())
            );
            radiusAnimator = createRadiusAnimator(TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 6f, getContext().getResources().getDisplayMetrics()));
        }

        AnimatorSet animatorSet = new AnimatorSet();
        animatorSet.playTogether(marginsAnimator, radiusAnimator);
        animatorSet.setDuration(300);
        animatorSet.setInterpolator(new AccelerateDecelerateInterpolator());
        animatorSet.start();
    }

    @NotNull
    private ValueAnimator createMarginsAnimator(int left, int right, int top, int bottom) {
        MarginLayoutParams layoutParams = ((MarginLayoutParams) cardView.getLayoutParams());
        PropertyValuesHolder leftMargin = PropertyValuesHolder.ofInt("left", layoutParams.leftMargin, left);
        PropertyValuesHolder rightMargin = PropertyValuesHolder.ofInt("right", layoutParams.rightMargin, right);
        PropertyValuesHolder topMargin = PropertyValuesHolder.ofInt("top", layoutParams.topMargin, top);
        PropertyValuesHolder bottomMargin = PropertyValuesHolder.ofInt("bottom", layoutParams.bottomMargin, bottom);
        ValueAnimator valueAnimator = ValueAnimator.ofPropertyValuesHolder(leftMargin, rightMargin, topMargin, bottomMargin);
        valueAnimator.addUpdateListener(animation -> {
            layoutParams.leftMargin = (Integer) valueAnimator.getAnimatedValue("left");
            layoutParams.rightMargin = (Integer) valueAnimator.getAnimatedValue("right");
            layoutParams.topMargin = (Integer) valueAnimator.getAnimatedValue("top");
            layoutParams.bottomMargin = (Integer) valueAnimator.getAnimatedValue("bottom");
            cardView.setLayoutParams(layoutParams);
        });
        return valueAnimator;
    }

    @NotNull
    private ValueAnimator createRadiusAnimator(float radius) {
        ValueAnimator valueAnimator = ValueAnimator.ofFloat(cardView.getRadius(), radius);
        valueAnimator.addUpdateListener(animation -> cardView.setRadius((Float) valueAnimator.getAnimatedValue()));
        return valueAnimator;
    }

    public void setOnInputModeListener(OnInputModeListener listener) {
        inputModeListener = listener;
    }

    public void setOnMenuItemClickListener(Toolbar.OnMenuItemClickListener listener) {
        toolbar.setOnMenuItemClickListener(listener);
    }

    public void setOnQueryTextListener(OnQueryTextListener listener) {
        onQueryTextListener = listener;
        editText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (ignore) return;
                listener.onQueryTextChange(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
    }

    public void setIgnoreText(boolean ignore) {
        this.ignore = ignore;
    }

    @IntDef({EXPANDABLE_ALWAYS_COLLAPSE, EXPANDABLE_ALWAYS_EXPAND, EXPANDABLE_ENABLE, EXPANDABLE_NO_ANIMATION})
    @Retention(RetentionPolicy.SOURCE)
    public @interface Expandable {
    }

    public interface OnInputModeListener {

        void onEnable();

        void onDisable();
    }

    public abstract static class OnQueryTextListener {

        public void onQueryTextSubmit(String query) {
        }

        public void onQueryTextChange(String newText) {
        }
    }

}
