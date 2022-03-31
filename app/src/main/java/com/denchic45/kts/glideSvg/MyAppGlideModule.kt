package com.denchic45.kts.glideSvg

import android.content.Context
import com.bumptech.glide.module.AppGlideModule
import com.bumptech.glide.Glide
import com.caverock.androidsvg.SVG
import android.graphics.drawable.PictureDrawable
import com.bumptech.glide.Registry
import com.bumptech.glide.annotation.GlideModule
import com.denchic45.kts.glideSvg.SvgDrawableTranscoder
import com.denchic45.kts.glideSvg.SvgDecoder
import java.io.InputStream

@GlideModule
class MyAppGlideModule : AppGlideModule() {
    override fun registerComponents(
        context: Context, glide: Glide,
        registry: Registry
    ) {
        registry.register(SVG::class.java, PictureDrawable::class.java, SvgDrawableTranscoder())
            .append(InputStream::class.java, SVG::class.java, SvgDecoder())
    }

    // Disable manifest parsing to avoid adding similar modules twice.
    override fun isManifestParsingEnabled(): Boolean {
        return false
    }
}