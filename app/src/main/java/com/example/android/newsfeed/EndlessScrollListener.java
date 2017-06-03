package com.example.android.newsfeed;

import android.widget.AbsListView;

/**
 * Created by Pallavi J on 02-06-2017.
 */

public abstract class EndlessScrollListener implements AbsListView.OnScrollListener{
    private int mVisibleThreshold = 5;
    private int mCurrentPage = 0;
    private int mPreviousTotalItemCount = 0;
    private boolean mLoading = true;
    private boolean userScrolled = false;

    public  EndlessScrollListener() {
    }

    @Override
    public void onScrollStateChanged(AbsListView view, int scrollState) {

        if (scrollState == AbsListView.OnScrollListener.SCROLL_STATE_IDLE) {
            userScrolled = false;
        } else if (scrollState == AbsListView.OnScrollListener.SCROLL_STATE_FLING ||
                scrollState == AbsListView.OnScrollListener.SCROLL_STATE_TOUCH_SCROLL) {
            userScrolled = true;
        }
    }

    @Override
    public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
        //Dnt count the footer
        if (userScrolled){
            totalItemCount = totalItemCount - 1;
            if( totalItemCount < mPreviousTotalItemCount) {
                mCurrentPage = 0;
                mPreviousTotalItemCount = totalItemCount;

                if (totalItemCount == 0){
                    this.mLoading = true;
                }
            }

            if(mLoading && (totalItemCount > mPreviousTotalItemCount)){
                mLoading =false;
                mPreviousTotalItemCount = totalItemCount;
                mCurrentPage++;
            }

            if ( !mLoading && (firstVisibleItem + visibleItemCount + mVisibleThreshold) >= totalItemCount ) {
                mLoading = onLoadMore(mCurrentPage + 1, totalItemCount);
            }
        }
    }

    // Defines the process for actually loading more data based on page
    // Returns true if more data is being loaded; returns false if there is no more data to load.
    public abstract boolean onLoadMore(int page, int totalItemsCount);

    // Call this method whenever performing new searches
    public void resetState() {
        this.mCurrentPage = 0;
        this.mPreviousTotalItemCount = 0;
        this.mLoading = true;
    }

}
