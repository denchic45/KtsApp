package com.denchic45;

import android.content.Context;
import android.graphics.drawable.PictureDrawable;
import android.widget.ImageView;

import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.target.Target;
import com.denchic45.kts.glideSvg.SvgSoftwareLayerSetter;
import com.denchic45.kts.utils.ViewUtils;

public class SvgColorListener extends SvgSoftwareLayerSetter {

    private final ImageView view;
    private final int color;
    private final Context context;

    public SvgColorListener(ImageView view, int color, Context context) {
        this.view = view;
        this.color = color;
        this.context = context;
    }

    @Override
    public boolean onLoadFailed(GlideException e, Object model, Target<PictureDrawable> target, boolean isFirstResource) {
        super.onLoadFailed(e, model, target, isFirstResource);
        ViewUtils.paintImageView(view, color, context);
        return false;
    }

    @Override
    public boolean onResourceReady(PictureDrawable resource, Object model, Target<PictureDrawable> target, DataSource dataSource, boolean isFirstResource) {
        super.onResourceReady(resource, model, target, dataSource, isFirstResource);
        ViewUtils.paintImageView(view, color, context);
        return false;
    }
}
