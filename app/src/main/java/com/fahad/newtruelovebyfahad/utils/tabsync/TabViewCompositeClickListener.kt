package com.fahad.newtruelovebyfahad.utils.tabsync

import androidx.recyclerview.widget.RecyclerView
import com.project.common.utils.setOnSingleClickListener

class TabViewCompositeClickListener(private val mTabLayout: RecyclerView) {

    private val listeners: MutableList<(tab: RecyclerView.ViewHolder, position: Int) -> Unit> =
        ArrayList()

    fun addListener(listener: (tab: RecyclerView.ViewHolder, position: Int) -> Unit) {
        listeners.add(listener)
    }

    fun removeListener(listener: (tab: RecyclerView.ViewHolder, position: Int) -> Unit) {
        listeners.remove(listener)
    }

    fun build() {
        for (i in 0 until (mTabLayout.adapter?.itemCount ?: 0)) {
            mTabLayout.findViewHolderForAdapterPosition(i)?.itemView?.setOnSingleClickListener {
                for (listener in listeners) {
                    listener(mTabLayout.findViewHolderForAdapterPosition(i)!!, i)
                }
            }
        }
    }

    fun getListeners(): List<(tab: RecyclerView.ViewHolder, position: Int) -> Unit> {
        return listeners
    }
}