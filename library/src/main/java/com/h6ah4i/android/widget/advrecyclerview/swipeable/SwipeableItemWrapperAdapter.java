/*
 *    Copyright (C) 2015 Haruki Hasegawa
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */

package com.h6ah4i.android.widget.advrecyclerview.swipeable;

import android.support.v4.view.ViewCompat;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;

import com.h6ah4i.android.widget.advrecyclerview.utils.BaseWrapperAdapter;
import com.h6ah4i.android.widget.advrecyclerview.utils.WrapperAdapterUtils;

public class SwipeableItemWrapperAdapter<VH extends RecyclerView.ViewHolder> extends BaseWrapperAdapter<VH> {
    private static final String TAG = "SwipeableRecyclerViewAdapter";

    private static final int STATE_FLAG_INITIAL_VALUE = -1;

    private static final boolean LOCAL_LOGV = false;
    private static final boolean LOCAL_LOGD = false;

    private SwipeableItemAdapter mSwipeableItemAdapter;
    private RecyclerViewSwipeManager mSwipeManager;
    private int mSwipingItemPosition = RecyclerView.NO_POSITION;

    public SwipeableItemWrapperAdapter(RecyclerViewSwipeManager manager, RecyclerView.Adapter<VH> adapter) {
        super(adapter);

        mSwipeableItemAdapter = getSwipeableItemAdapter(adapter);
        if (mSwipeableItemAdapter == null) {
            throw new IllegalArgumentException("adapter does not implement SwipeableItemAdapter");
        }

        if (manager == null) {
            throw new IllegalArgumentException("manager cannot be null");
        }

        mSwipeManager = manager;
    }

    @Override
    protected void onRelease() {
        super.onRelease();

        mSwipeableItemAdapter = null;
        mSwipeManager = null;
        mSwipingItemPosition = RecyclerView.NO_POSITION;
    }

    @Override
    public void onViewRecycled(VH holder) {
        super.onViewRecycled(holder);

        // reset SwipeableItemViewHolder state
        if (holder instanceof SwipeableItemViewHolder) {
            ((SwipeableItemViewHolder) holder).setSwipeItemSlideAmount(0.0f);

            View containerView = ((SwipeableItemViewHolder) holder).getSwipeableContainerView();

            if (containerView != null) {
                ViewCompat.animate(containerView).cancel();
                ViewCompat.setTranslationX(containerView, 0.0f);
            }
        }
    }

    @Override
    public VH onCreateViewHolder(ViewGroup parent, int viewType) {
        final VH holder = super.onCreateViewHolder(parent, viewType);

        if (holder instanceof SwipeableItemViewHolder) {
            ((SwipeableItemViewHolder) holder).setSwipeStateFlags(STATE_FLAG_INITIAL_VALUE);
        }

        return holder;
    }

    @Override
    public void onDetachedFromRecyclerView(RecyclerView recyclerView) {
        super.onDetachedFromRecyclerView(recyclerView);
    }

    @Override
    public void onAttachedToRecyclerView(RecyclerView recyclerView) {
        super.onAttachedToRecyclerView(recyclerView);
    }

    @Override
    public void onBindViewHolder(VH holder, int position) {
        float prevSwipeItemSlieAmount = 0;

        if (holder instanceof SwipeableItemViewHolder) {
            prevSwipeItemSlieAmount = ((SwipeableItemViewHolder) holder).getSwipeItemSlideAmount();
        }

        if (isSwiping()) {
            int flags = RecyclerViewSwipeManager.STATE_FLAG_SWIPING;

            if (position == mSwipingItemPosition) {
                flags |= RecyclerViewSwipeManager.STATE_FLAG_IS_ACTIVE;
            }

            safeUpdateFlags(holder, flags);
            super.onBindViewHolder(holder, position);
        } else {
            safeUpdateFlags(holder, 0);
            super.onBindViewHolder(holder, position);
        }

        if (holder instanceof SwipeableItemViewHolder) {
            final float swipeItemSlideAmount = ((SwipeableItemViewHolder) holder).getSwipeItemSlideAmount();

            if ((prevSwipeItemSlieAmount != swipeItemSlideAmount) ||
                    !(mSwipeManager.isSwiping() || mSwipeManager.isAnimationRunning(holder))) {
                mSwipeManager.applySlideItem(holder, prevSwipeItemSlieAmount, swipeItemSlideAmount, true);
            }
        }
    }

    @Override
    protected void onHandleWrappedAdapterChanged() {
        if (isSwiping()) {
            cancelSwipe();
        } else {
            super.onHandleWrappedAdapterChanged();
        }
    }

    @Override
    protected void onHandleWrappedAdapterItemRangeChanged(int positionStart, int itemCount) {
        if (isSwiping()) {
            cancelSwipe();
        } else {
            super.onHandleWrappedAdapterItemRangeChanged(positionStart, itemCount);
        }
    }

    @Override
    protected void onHandleWrappedAdapterItemRangeInserted(int positionStart, int itemCount) {
        if (isSwiping()) {
            cancelSwipe();
        } else {
            super.onHandleWrappedAdapterItemRangeInserted(positionStart, itemCount);
        }
    }

    @Override
    protected void onHandleWrappedAdapterItemRangeRemoved(int positionStart, int itemCount) {
        if (isSwiping()) {
            cancelSwipe();
        } else {
            super.onHandleWrappedAdapterItemRangeRemoved(positionStart, itemCount);
        }
    }

    @Override
    protected void onHandleWrappedAdapterRangeMoved(int fromPosition, int toPosition, int itemCount) {
        if (isSwiping()) {
            cancelSwipe();
        } else {
            super.onHandleWrappedAdapterRangeMoved(fromPosition, toPosition, itemCount);
        }
    }

    private void cancelSwipe() {
        if (mSwipeManager != null) {
            mSwipeManager.cancelSwipe();
        }
    }

    // NOTE: This method is called from RecyclerViewDragDropManager
    /*package*/ int getSwipeReactionType(RecyclerView.ViewHolder holder, int x, int y) {
        if (LOCAL_LOGV) {
            Log.v(TAG, "getSwipeReactionType(holder = " + holder + ", x = " + x + ", y = " + y + ")");
        }

        return mSwipeableItemAdapter.onGetSwipeReactionType(holder, x, y);
    }

    // NOTE: This method is called from RecyclerViewDragDropManager
    /*package*/ void setSwipeBackgroundDrawable(RecyclerView.ViewHolder holder, int type) {
        if (LOCAL_LOGV) {
            Log.v(TAG, "setSwipeBackgroundDrawable(holder = " + holder + ", type = " + type + ")");
        }

        mSwipeableItemAdapter.onSetSwipeBackground(holder, type);
    }

    // NOTE: This method is called from RecyclerViewDragDropManager
    /*package*/ void onSwipeItemStarted(RecyclerViewSwipeManager manager, RecyclerView.ViewHolder holder) {
        if (LOCAL_LOGD) {
            Log.d(TAG, "onSwipeItemStarted(holder = " + holder + ")");
        }

        mSwipingItemPosition = holder.getPosition();

        notifyDataSetChanged();
    }

    // NOTE: This method is called from RecyclerViewDragDropManager
    /*package*/ int onSwipeItemFinished(RecyclerView.ViewHolder holder, int result) {
        if (LOCAL_LOGD) {
            Log.d(TAG, "onSwipeItemFinished(holder = " + holder + ", result = " + result + ")");
        }

        mSwipingItemPosition = RecyclerView.NO_POSITION;

        return mSwipeableItemAdapter.onSwipeItem(holder, result);
    }

    /*package*/ void onSwipeItemFinished2(RecyclerView.ViewHolder holder, int result, int afterReaction) {

        ((SwipeableItemViewHolder) holder).setSwipeResult(result);
        ((SwipeableItemViewHolder) holder).setAfterSwipeReaction(afterReaction);
        ((SwipeableItemViewHolder) holder).setSwipeItemSlideAmount(getSwipeAmountFromAfterReaction(result, afterReaction));

        mSwipeableItemAdapter.onPerformAfterSwipeReaction(holder, result, afterReaction);
        notifyDataSetChanged();
    }

    protected boolean isSwiping() {
        return (mSwipingItemPosition != RecyclerView.NO_POSITION);
    }

    private static float getSwipeAmountFromAfterReaction(int result, int afterReaction) {
        switch (afterReaction) {
            case RecyclerViewSwipeManager.AFTER_SWIPE_REACTION_DEFAULT:
                return 0.0f;
            case RecyclerViewSwipeManager.AFTER_SWIPE_REACTION_MOVE_TO_SWIPED_DIRECTION:
            case RecyclerViewSwipeManager.AFTER_SWIPE_REACTION_REMOVE_ITEM:
                return (result == RecyclerViewSwipeManager.RESULT_SWIPED_LEFT)
                        ? RecyclerViewSwipeManager.OUTSIDE_OF_THE_WINDOW_LEFT
                        : RecyclerViewSwipeManager.OUTSIDE_OF_THE_WINDOW_RIGHT;
            default:
                return 0.0f;
        }
    }

    private static void safeUpdateFlags(RecyclerView.ViewHolder holder, int flags) {
        if (!(holder instanceof SwipeableItemViewHolder)) {
            return;
        }

        final SwipeableItemViewHolder holder2 = (SwipeableItemViewHolder) holder;

        final int curFlags = holder2.getSwipeStateFlags();
        final int mask = ~RecyclerViewSwipeManager.STATE_FLAG_IS_UPDATED;

        // append UPDATED flag
        if ((curFlags == STATE_FLAG_INITIAL_VALUE) || (((curFlags ^ flags) & mask) != 0)) {
            flags |= RecyclerViewSwipeManager.STATE_FLAG_IS_UPDATED;
        }

        ((SwipeableItemViewHolder) holder).setSwipeStateFlags(flags);
    }

    private static SwipeableItemAdapter getSwipeableItemAdapter(RecyclerView.Adapter adapter) {
        return WrapperAdapterUtils.findWrappedAdapter(adapter, SwipeableItemAdapter.class);
    }
}
