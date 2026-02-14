package com.fahad.newtruelovebyfahad.utils.tabsync

import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.LinearSmoothScroller
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.SmoothScroller
import com.fahad.newtruelovebyfahad.utils.tabsync.adapters.CategoryTagsAdapter

/**
 * This class is made to provide the ability to sync between RecyclerView's specific items with
 * TabLayout tabs.
 *
 * @param mRecyclerView     The RecyclerView that is going to be synced with the TabLayout
 * @param mTabLayout        The TabLayout that is going to be synced with the RecyclerView specific
 *                          items.
 * @param mIndices          The indices of the RecyclerView's items that is going to be playing a
 *                          role of "check points" for the syncing operation.
 * @param mIsSmoothScroll   Defines the ability of smooth scroll when clicking the tabs of the
 *                          TabLayout.
 */
class TabbedListMediator(
    private val mRecyclerView: RecyclerView,
    private val mTabRv: RecyclerView,
    private var mIndices: List<Int>,
    private var mIsSmoothScroll: Boolean = true
) {

    private var mIsAttached = false

    private var mRecyclerState = RecyclerView.SCROLL_STATE_IDLE
    private var mTabClickFlag = false

    private val smoothScroller: SmoothScroller =
        object : LinearSmoothScroller(mRecyclerView.context) {
            override fun getVerticalSnapPreference(): Int {
                return SNAP_TO_START
            }
        }

    private var tabViewCompositeClickListener: TabViewCompositeClickListener =
        TabViewCompositeClickListener(mTabRv)

    /**
     * Calling this method will ensure that the data that has been provided to the mediator is
     * valid for use, and start syncing between the the RecyclerView and the TabLayout.
     *
     * Call this method when you have:
     *      1- provided a RecyclerView Adapter,
     *      2- provided a TabLayout with the appropriate number of tabs,
     *      3- provided indices of the recyclerview items that you are syncing the tabs with. (You
     *         need to be providing indices of at most the number of Tabs inflated in the TabLayout.)
     */
    fun attach() {
        mRecyclerView.adapter
            ?: throw RuntimeException("Cannot attach with no Adapter provided to RecyclerView")

        if (mTabRv.adapter?.itemCount == 0)
//            throw RuntimeException("Cannot attach with no tabs provided to TabLayout")

        if (mIndices.size > (mTabRv.adapter?.itemCount ?: 0))
//            throw RuntimeException("Cannot attach using more indices than the available tabs")

        notifyIndicesChanged()
        mIsAttached = true
    }

    /**
     * Calling this method will ensure to stop the synchronization between the RecyclerView and
     * the TabLayout.
     */

    fun detach() {
        clearListeners()
        mIsAttached = false
    }

    /**
     * This method will ensure that the synchronization is up-to-date with the data provided.
     */
    private fun reAttach() {
        detach()
        attach()
    }

    /**
     * Calling this method will
     */
    fun updateMediatorWithNewIndices(newIndices: List<Int>): TabbedListMediator {
        mIndices = newIndices

        if (mIsAttached) {
            reAttach()
        }

        return this
    }

    /**
     * This method will ensure that any listeners that have been added by the mediator will be
     * removed, including the one listener from
     * @see TabbedListMediator#addOnViewOfTabClickListener((TabLayout.Tab, int) -> Unit)
     */

    private fun clearListeners() {
        mRecyclerView.clearOnScrollListeners()
        for (i in 0 until (mTabRv.adapter?.itemCount ?: 0)) {
            mTabRv.findViewHolderForAdapterPosition(i)?.itemView?.setOnClickListener(null)
        }
        for (i in tabViewCompositeClickListener.getListeners().indices) {
            tabViewCompositeClickListener.getListeners().toMutableList().removeAt(i)
        }
        // mTabRv.removeOnTabSelectedListener(onTabSelectedListener)
        mRecyclerView.removeOnScrollListener(onScrollListener)
    }

    /**
     * This method will attach the listeners required to make the synchronization possible.
     */

    private fun notifyIndicesChanged() {
        tabViewCompositeClickListener.addListener { _, _ -> mTabClickFlag = true }
        tabViewCompositeClickListener.build()
        //mTabRv.addOnTabSelectedListener(onTabSelectedListener)
        mRecyclerView.addOnScrollListener(onScrollListener)
    }

    val onTabSelectedListener = object : OnTabSelectedListener {
        override fun onTabSelected(tab: RecyclerView.ViewHolder) {
            //if (!mTabClickFlag) return

            val position = tab.absoluteAdapterPosition
            if (mIsSmoothScroll) {
                smoothScroller.targetPosition = mIndices[position]
                mRecyclerView.layoutManager?.startSmoothScroll(smoothScroller)
            } else {
                (mRecyclerView.layoutManager as LinearLayoutManager?)?.scrollToPositionWithOffset(
                    mIndices[position],
                    0
                )
                mTabClickFlag = false
            }
        }
    }

    interface OnTabSelectedListener {
        fun onTabSelected(tab: RecyclerView.ViewHolder)
    }

    private val onScrollListener = object : RecyclerView.OnScrollListener() {
        override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
            mRecyclerState = newState
            if (mIsSmoothScroll && newState == RecyclerView.SCROLL_STATE_IDLE) {
                mTabClickFlag = false
            }
        }

        override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
            super.onScrolled(recyclerView, dx, dy)
            if (mTabClickFlag) {
                return
            }

            val linearLayoutManager: LinearLayoutManager =
                recyclerView.layoutManager as LinearLayoutManager?
                    ?: throw RuntimeException("No LinearLayoutManager attached to the RecyclerView.")

            var itemPosition =
                linearLayoutManager.findFirstCompletelyVisibleItemPosition()

            if (itemPosition == -1) {
                itemPosition =
                    linearLayoutManager.findFirstVisibleItemPosition()
            }

            if (mRecyclerState == RecyclerView.SCROLL_STATE_DRAGGING
                || mRecyclerState == RecyclerView.SCROLL_STATE_SETTLING
            ) {
                for (i in mIndices.indices) {
                    if (itemPosition == mIndices[i]) {
                        if (mTabRv.adapter is CategoryTagsAdapter) {
                            if ((mTabRv.adapter as CategoryTagsAdapter).mSelected != i) {
                                (mTabRv.adapter as CategoryTagsAdapter).mPrevious =
                                    (mTabRv.adapter as CategoryTagsAdapter).mSelected
                                (mTabRv.adapter as CategoryTagsAdapter).mSelected = i
                                mTabRv.adapter?.notifyItemChanged(i)
                                mTabRv.adapter?.notifyItemChanged((mTabRv.adapter as CategoryTagsAdapter).mPrevious)
                            }
                            if (linearLayoutManager.findLastCompletelyVisibleItemPosition() == mIndices[mIndices.size - 1]) {
                                if ((mTabRv.adapter as CategoryTagsAdapter).mSelected != mIndices.size - 1) {
                                    (mTabRv.adapter as CategoryTagsAdapter).mPrevious =
                                        (mTabRv.adapter as CategoryTagsAdapter).mSelected
                                    (mTabRv.adapter as CategoryTagsAdapter).mSelected =
                                        mIndices.size - 1
                                    mTabRv.adapter?.notifyItemChanged(mIndices.size - 1)
                                    mTabRv.adapter?.notifyItemChanged((mTabRv.adapter as CategoryTagsAdapter).mPrevious)
                                }
                                return
                            }
                        }
                    }
                }
            }
        }
    }

    /**
     * @return the state of the mediator, either attached or not.
     */

    fun isAttached(): Boolean {
        return mIsAttached
    }

    /**
     * @return the state of the mediator, is smooth scrolling or not.
     */

    fun isSmoothScroll(): Boolean {
        return mIsSmoothScroll
    }

    /**
     * @param smooth sets up the mediator with smooth scrolling
     */

    fun setSmoothScroll(smooth: Boolean) {
        mIsSmoothScroll = smooth
    }

    /**
     * @param listener the listener the will applied on "the view" of the tab. This method is useful
     * when attaching a click listener on the tabs of the TabLayout.
     * Note that this method is REQUIRED in case of the need of adding a click listener on the view
     * of a tab layout. Since the mediator uses a click flag @see TabbedListMediator#mTabClickFlag
     * it's taking the place of the normal on click listener, and thus the need of the composite click
     * listener pattern, so adding listeners should be done using this method.
     */

    fun addOnViewOfTabClickListener(
        listener: (tab: RecyclerView.ViewHolder, position: Int) -> Unit
    ) {
        tabViewCompositeClickListener.addListener(listener)
        if (mIsAttached) {
            notifyIndicesChanged()
        }
    }
}