package com.denchic45.kts.ui.adapter

import android.content.Context
import android.graphics.drawable.PictureDrawable
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.ImageView
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.denchic45.kts.R
import com.denchic45.kts.SvgColorListener
import com.denchic45.kts.glideSvg.GlideApp

class IconPickerAdapter(context: Context) : ArrayAdapter<Uri?>(context, R.layout.item_icon) {
    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        var convertView = convertView
        if (convertView == null) {
            convertView = LayoutInflater.from(context)
                .inflate(R.layout.item_icon, parent, false)
        }
        GlideApp.with(parent.context)
            .`as`(PictureDrawable::class.java)
            .transition(DrawableTransitionOptions.withCrossFade())
            .load(getItem(position))
            .listener(
                SvgColorListener(
                    (convertView as ImageView?)!!,
                    R.color.dark_gray,
                    parent.context
                )
            )
            .into(convertView!!)
        return convertView
    }
}