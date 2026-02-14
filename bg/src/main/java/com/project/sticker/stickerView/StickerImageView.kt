package com.project.core.customView.stickerView

import android.content.Context
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.util.AttributeSet
import android.view.View
import android.widget.ImageView

class StickerImageView : StickerView {
    var ownerId: String? = null
    private var iv_main: ImageView? = null

    override val mainView: View?
        get() = if (iv_main == null) {
            iv_main = ImageView(context)
            iv_main?.scaleType = ImageView.ScaleType.FIT_XY
            iv_main
        } else
            iv_main

    constructor(context: Context) : super(context) {

    }

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs)
    constructor(context: Context, attrs: AttributeSet, defStyle: Int) : super(
        context,
        attrs,
        defStyle
    )

    fun getMainView(): ImageView? {
        return iv_main
    }

    fun setImageResource(res_id: Int) {
        iv_main?.setImageResource(res_id)
    }

    fun setImageDrawable(drawable: Drawable?) {
        iv_main?.setImageDrawable(drawable)
    }

    var imageBitmap: Bitmap?
        get() = (iv_main?.drawable as BitmapDrawable).bitmap
        set(bmp) {
            iv_main?.setImageBitmap(bmp)
        }
}