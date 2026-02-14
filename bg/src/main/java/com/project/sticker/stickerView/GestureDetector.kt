package com.project.core.customView.stickerView

import android.view.GestureDetector.SimpleOnGestureListener
import android.view.MotionEvent


class GestureDetector(private var listener: DetectTap) : SimpleOnGestureListener() {

    override fun onDown(e: MotionEvent): Boolean {
        return true
    }

    // event when double tap occurs
    override fun onDoubleTap(e: MotionEvent): Boolean {

        listener.doubleTap()
        return true
    }

    override fun onSingleTapUp(e: MotionEvent): Boolean {
        listener.singleTap()
        return super.onSingleTapUp(e)
    }

    interface DetectTap {

        fun singleTap()
        fun doubleTap()
    }
}