package com.denchic45.kts.customPopup;

import android.content.Context;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.FrameLayout;

import androidx.appcompat.widget.ListPopupWindow;

import com.denchic45.kts.data.model.domain.ListItem;
import com.denchic45.kts.ui.adapter.OnItemClickListener;

import org.jetbrains.annotations.NotNull;

import java.util.List;

public final class PopupBuilder {

    public static int STANDARD_MENU_SiZE;

    @NotNull
    public ListPopupWindow setPopupMenu(@NotNull View anchorView, List<ListItem> items, OnItemClickListener listener) {
        Context context = anchorView.getContext();
        ListPopupWindow popupWindow = new ListPopupWindow(context);
        popupWindow.setAnchorView(anchorView);
        ListPopupWindowAdapter adapter = new ListPopupWindowAdapter(context, items);
        popupWindow.setAdapter(adapter);
        popupWindow.setOnItemClickListener((parent, view1, position, id) -> {
            popupWindow.dismiss();
            listener.onItemClick(position);
        });
        STANDARD_MENU_SiZE = measureContentWidth(adapter, context);
        return popupWindow;
    }

    private int measureContentWidth(@NotNull ArrayAdapter<?> listAdapter, Context context) {
        ViewGroup mMeasureParent = null;
        int maxWidth = 0;
        View itemView = null;
        int itemType = 0;

        final int widthMeasureSpec = View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED);
        final int heightMeasureSpec = View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED);
        final int count = listAdapter.getCount();
        for (int i = 0; i < count; i++) {
            final int positionType = listAdapter.getItemViewType(i);
            if (positionType != itemType) {
                itemType = positionType;
                itemView = null;
            }

            if (mMeasureParent == null) {
                mMeasureParent = new FrameLayout(context);
            }

            itemView = listAdapter.getView(i, itemView, mMeasureParent);
            itemView.measure(widthMeasureSpec, heightMeasureSpec);

            final int itemWidth = itemView.getMeasuredWidth();

            if (itemWidth > maxWidth) {
                maxWidth = itemWidth;
            }
        }

        for (int i=0; i < listAdapter.getCount(); i++) {
            View listItem = listAdapter.getView(i, null, mMeasureParent);
            listItem.measure(0, 0);
            int width = listItem.getMeasuredWidth();
            if (width > maxWidth) {
                maxWidth = width;
            }
        }

        return maxWidth;
    }

    public int dpToPx(int dp,  @NotNull Context context) {
        DisplayMetrics displayMetrics = context.getResources().getDisplayMetrics();
        return Math.round(dp * (displayMetrics.xdpi / DisplayMetrics.DENSITY_DEFAULT));
    }
}
