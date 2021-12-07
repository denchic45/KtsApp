package com.denchic45.kts.ui.adapter;

import android.content.Context;
import android.graphics.drawable.PictureDrawable;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.denchic45.SvgColorListener;
import com.denchic45.kts.R;
import com.denchic45.kts.glideSvg.GlideApp;

import static com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions.withCrossFade;

public class IconPickerAdapter extends ArrayAdapter<Uri> {
    public IconPickerAdapter(@NonNull Context context) {
        super(context, R.layout.item_icon);
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext())
                    .inflate(R.layout.item_icon, parent, false);
        }

        GlideApp.with(parent.getContext())
                .as(PictureDrawable.class)
                .transition(withCrossFade())
                .load(getItem(position))
                .listener(new SvgColorListener((ImageView) convertView, R.color.dark_gray, parent.getContext()))
                .into((ImageView) convertView);
        return convertView;
    }
}
