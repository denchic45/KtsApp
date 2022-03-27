package com.denchic45.widget;

import android.content.Context;
import android.content.res.TypedArray;
import android.os.Handler;
import android.os.Looper;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ViewFlipper;

import androidx.annotation.LayoutRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;

import com.denchic45.kts.R;

import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;

public class ListStateLayout extends FrameLayout {

    public static final String EMPTY_VIEW = "EMPTY_VIEW";
    public static final String ERROR_VIEW = "ERROR_VIEW";
    public static final String LOADING_VIEW = "LOADING_VIEW";
    public static final String NETWORK_VIEW = "NETWORK_VIEW";
    public static final String START_VIEW = "START_VIEW";
    public static final int ONE_CHILD_VIEW_AND_VIEWFLIPPER = 2;
    private static final String NULL_VIEW = "NULL_VIEW";
    private final Map<String, View> stateViews = new HashMap<>();
    private View contentView;
    private ViewFlipper viewFlipper;
    private boolean showContent;

    public ListStateLayout(@NonNull Context context) {
        super(context);
        init();
    }

    public ListStateLayout(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
        initAttributes(attrs);
    }

    public ListStateLayout(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
        initAttributes(attrs);
    }

    public ListStateLayout(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init();
        initAttributes(attrs);
    }

    private void init() {

        post(() -> {
            if (getChildCount() != ONE_CHILD_VIEW_AND_VIEWFLIPPER) {
                throw new IllegalStateException("Must have only 1 view");
            }
            contentView = getChildAt(1);
        });
        viewFlipper = new ViewFlipper(getContext());
        viewFlipper.setLayoutParams(new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        addView(viewFlipper);
        addView(new View(getContext()), NULL_VIEW);
        setDefaultAnimation();
    }

    private void initAttributes(AttributeSet attrs) {
        if (attrs == null) {
            return;
        }
        TypedArray ta = getContext().obtainStyledAttributes(attrs, R.styleable.ListStateLayout);

        int emptyViewId = ta.getResourceId(R.styleable.ListStateLayout_emptyView, -1);
        if (emptyViewId > -1) {
            addView(inflateView(emptyViewId), EMPTY_VIEW);
        }

        int loadingViewId = ta.getResourceId(R.styleable.ListStateLayout_loadingView, -1);
        if (loadingViewId > -1) {
            addView(inflateView(loadingViewId), LOADING_VIEW);
        }

        int errorViewId = ta.getResourceId(R.styleable.ListStateLayout_errorView, -1);
        if (errorViewId > -1) {
            addView(inflateView(errorViewId), ERROR_VIEW);
        }

        int networkViewId = ta.getResourceId(R.styleable.ListStateLayout_networkView, -1);
        if (networkViewId > -1) {
            addView(inflateView(networkViewId), NETWORK_VIEW);
        }
        ta.recycle();
    }

    public void showView(String viewKey) {
        showContent = false;
        if (stateViews.containsKey(viewKey) && viewFlipper.getDisplayedChild() != viewFlipper.indexOfChild(stateViews.get(viewKey))) {
            if (contentView instanceof RecyclerView) {
                contentView.post(new Runnable() {
                    @Override
                    public void run() {
                        if (((RecyclerView) contentView).isAnimating()) {
                            ((RecyclerView) contentView).getItemAnimator().isRunning(()
                                    -> new Handler(Looper.getMainLooper()).post(this));
                            return;
                        }
                        if (!showContent)
                            viewFlipper.setDisplayedChild(viewFlipper.indexOfChild(stateViews.get(viewKey)));
                    }
                });
            } else {
                viewFlipper.setDisplayedChild(viewFlipper.indexOfChild(stateViews.get(viewKey)));
            }
        }
    }

    public void showList() {
        if (showContent)
            return;
        showContent = true;
        viewFlipper.setDisplayedChild(0);

    }

    public ListChangeListener getCommitCallback(RecyclerView.Adapter<?> adapter) {
        return new ListChangeListener(adapter);
    }

    private void setDefaultAnimation() {
        viewFlipper.setInAnimation(getContext(), R.anim.fade_in);
        viewFlipper.setOutAnimation(getContext(), R.anim.fade_out);
    }

    public void addView(@LayoutRes int layoutId, String viewKey) {
        if (layoutId != 0)
            addView(inflateView(layoutId), viewKey);
    }

    public void addView(View view, String viewKey) {
        if (view != null) {
            viewFlipper.addView(view);
            stateViews.put(viewKey, view);
        }
    }

    public View getStateView(String viewKey) {
        return stateViews.get(viewKey);
    }

    private View inflateView(@LayoutRes int layoutId) {
        return LayoutInflater.from(getContext()).inflate(layoutId, null);
    }

    private void checkAndShowEmptyView(RecyclerView.@NotNull Adapter<?> adapter) {
        if (adapter.getItemCount() == 0) {
            showView(EMPTY_VIEW);
        } else {
            showList();
        }
    }

    public void listenAdapterDataObserver(RecyclerView.@NotNull Adapter<?> adapter) {
        adapter.registerAdapterDataObserver(new RecyclerView.AdapterDataObserver() {
            @Override
            public void onChanged() {
                checkAndShowEmptyView(adapter);
            }

            @Override
            public void onItemRangeChanged(int positionStart, int itemCount) {
                checkAndShowEmptyView(adapter);
            }

            @Override
            public void onItemRangeChanged(int positionStart, int itemCount, @Nullable Object payload) {
                super.onItemRangeChanged(positionStart, itemCount, payload);
            }

            @Override
            public void onItemRangeInserted(int positionStart, int itemCount) {
                checkAndShowEmptyView(adapter);
            }

            @Override
            public void onItemRangeRemoved(int positionStart, int itemCount) {
                checkAndShowEmptyView(adapter);
            }

            @Override
            public void onItemRangeMoved(int fromPosition, int toPosition, int itemCount) {
                checkAndShowEmptyView(adapter);
            }
        });
    }


    private class ListChangeListener implements Runnable {

        private final RecyclerView.Adapter<?> adapter;

        public ListChangeListener(RecyclerView.Adapter<?> adapter) {
            this.adapter = adapter;
        }

        @Override
        public void run() {
            checkAndShowEmptyView(adapter);
        }
    }
}