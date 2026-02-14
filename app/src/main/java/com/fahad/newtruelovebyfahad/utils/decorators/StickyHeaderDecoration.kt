package com.fahad.newtruelovebyfahad.utils.decorators

import android.graphics.Canvas
import android.view.LayoutInflater
import android.view.View
import android.view.View.MeasureSpec
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.ItemDecoration
import androidx.recyclerview.widget.RecyclerView.State
import com.fahad.newtruelovebyfahad.databinding.ViewStickyHeaderBinding
import com.fahad.newtruelovebyfahad.ui.fragments.home.adapter.FramesRV

class StickyHeaderDecoration(root: View, private val showAd: (value: Boolean) -> Unit) :
    ItemDecoration() {

    var stopCalculations = false
    private val headerBinding by lazy { ViewStickyHeaderBinding.inflate(LayoutInflater.from(root.context)) }

    private val headerView: View
        get() = headerBinding.root

    private var startLeft = 0
    private var startRight = 0
    private var isAdViewVisibleInRV = true
    private var topChild: View? = null
    private var secondChild: View? = null

    override fun onDrawOver(canvas: Canvas, parent: RecyclerView, state: State) {
        super.onDrawOver(canvas, parent, state)
        if (!stopCalculations) {
            topChild = parent.getChildAt(3)
            secondChild = parent.getChildAt(5)
            topChild?.let { topChild ->
                parent.getChildAdapterPosition(topChild)
                    .let { topChildPosition ->
                        if (topChildPosition > 2) {
                            isAdViewVisibleInRV =
                                parent.findViewHolderForAdapterPosition(topChildPosition)
                                    ?.run { this is FramesRV.AdViewHolder } == true
                            showAd.invoke(!isAdViewVisibleInRV)
                        }
                        //layoutHeaderView(topChild)
                        //canvas.drawHeaderView(topChild, secondChild)
                    }
            }
        }
    }

    private fun layoutHeaderView(topView: View?) {
        topView?.let {
            headerView.measure(
                MeasureSpec.makeMeasureSpec(topView.width, MeasureSpec.EXACTLY),
                MeasureSpec.makeMeasureSpec(MeasureSpec.UNSPECIFIED, MeasureSpec.UNSPECIFIED)
            )
            headerView.layout(startLeft, 0, startRight, headerView.measuredHeight)
        }
    }

    private fun Canvas.drawHeaderView(topView: View?, secondChild: View?) {
        save()
        translate(startLeft.toFloat(), calculateHeaderTop(topView, secondChild))
        headerView.draw(this)
        restore()
    }

    private fun calculateHeaderTop(topView: View?, secondChild: View?): Float =
        secondChild?.let { secondView ->
            val threshold = headerView.bottom
            if (secondView.findViewById<View>(headerView.id)?.visibility != View.GONE && isAdViewVisibleInRV) {
                (secondView.top - threshold).toFloat()
            } else 0f
        } ?: 0f
}
