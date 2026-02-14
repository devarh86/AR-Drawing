package com.example.ads.crosspromo.helper

import android.content.Context
import android.graphics.drawable.AnimationDrawable
import android.graphics.drawable.Drawable
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.transition.Transition
import com.example.ads.crosspromo.api.retrofit.model.CrossPromoItem
import com.google.errorprone.annotations.Keep


@Keep
class ExtendedAnimationDrawable(
    context: Context,
    private val aniDrawable: List<CrossPromoItem>,
    private val onFrameChanged: (iconLink: String, adType: String, appName: String, adAppName: String) -> Unit,
    private val onInitializationCompleted: (extendedAnimationDrawable: ExtendedAnimationDrawable) -> Unit
) :
    AnimationDrawable() {

    override fun selectDrawable(idx: Int): Boolean {
        val result = super.selectDrawable(idx)
        aniDrawable[idx].apply {
            onFrameChanged.invoke(
                link ?: "",
                adType ?: "",
                appPackage ?: "PhotoOnCake",
                title ?: ""
            )
        }
        return result
    }

    var counter = 0

    init {
        loadAnimationImage(context, counter)
    }

    private fun loadAnimationImage(context: Context, counter: Int) {
        if (context is AppCompatActivity) {
            if (aniDrawable.size > counter) {
                Glide.with(context)
                    .asDrawable()
                    .load(aniDrawable[counter].adFile)
                    .into(object : CustomTarget<Drawable>() {
                        override fun onResourceReady(
                            resource: Drawable,
                            transition: Transition<in Drawable>?
                        ) {
                            aniDrawable[counter].adFrequency?.let {
                                addFrame(resource, (it.toFloat() * 1000).toInt())
                            }
                            this@ExtendedAnimationDrawable.counter++
                            if (this@ExtendedAnimationDrawable.counter < aniDrawable.size) loadAnimationImage(
                                context,
                                this@ExtendedAnimationDrawable.counter
                            )
                            if (numberOfFrames == aniDrawable.size) onInitializationCompleted.invoke(
                                this@ExtendedAnimationDrawable
                            )
                        }

                        override fun onLoadCleared(placeholder: Drawable?) {}
                    })
            }
        }
    }
}