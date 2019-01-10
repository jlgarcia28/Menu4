/*
 * Copyright (C) 2017 Sunday (https://github.com/zj565061763)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.sd.lib.swipemenu;

import android.content.Context;
import android.support.v4.view.ViewCompat;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.View;

import com.sd.lib.swipemenu.gesture.FGestureManager;
import com.sd.lib.swipemenu.gesture.FTouchHelper;


public class FSwipeMenu extends BaseSwipeMenu
{
    private FGestureManager mGestureManager;

    public FSwipeMenu(Context context, AttributeSet attrs)
    {
        super(context, attrs);
    }

    @Override
    public void setDebug(boolean debug)
    {
        super.setDebug(debug);
        getGestureManager().setDebug(debug);
    }

    private FGestureManager getGestureManager()
    {
        if (mGestureManager == null)
        {
            mGestureManager = new FGestureManager(this, new FGestureManager.Callback()
            {
                @Override
                public boolean shouldInterceptEvent(MotionEvent event)
                {
                    return canPull();
                }

                @Override
                public boolean shouldConsumeEvent(MotionEvent event)
                {
                    return mGestureManager.getTagHolder().isTagIntercept() || canPull();
                }

                @Override
                public void onEventConsume(MotionEvent event)
                {
                    final int delta = getMenuDirection().isHorizontal()
                            ? (int) getGestureManager().getTouchHelper().getDeltaX()
                            : (int) getGestureManager().getTouchHelper().getDeltaY();

                    moveView(delta, true);
                }

                @Override
                public void onEventFinish(VelocityTracker velocityTracker, MotionEvent event)
                {
                    if (mGestureManager.getLifecycleInfo().isCancelConsumeEvent())
                        return;

                    if (mGestureManager.getLifecycleInfo().hasConsumeEvent())
                    {
                        velocityTracker.computeCurrentVelocity(1000);
                        final int velocity = getMenuDirection().isHorizontal()
                                ? (int) velocityTracker.getXVelocity()
                                : (int) velocityTracker.getYVelocity();

                        dealDragFinish(velocity);
                    }
                }

                @Override
                public void onCancelConsumeEvent()
                {
                    if (mIsDebug)
                        Log.i(SwipeMenu.class.getSimpleName(), "onCancelConsumeEvent isViewIdle:" + isViewIdle());
                }

                @Override
                public void onStateChanged(FGestureManager.State oldState, FGestureManager.State newState)
                {
                    switch (newState)
                    {
                        case Consume:
                            setScrollState(ScrollState.Drag);
                            break;
                        case Fling:
                            setScrollState(ScrollState.Fling);
                            ViewCompat.postInvalidateOnAnimation(FSwipeMenu.this);
                            break;
                        case Idle:
                            setScrollState(ScrollState.Idle);
                            break;
                    }
                }

                @Override
                public void onScrollerCompute(int lastX, int lastY, int currX, int currY)
                {
                    final int delta = getMenuDirection().isHorizontal() ? (currX - lastX) : (currY - lastY);
                    moveView(delta, false);
                }
            });
            mGestureManager.getTagHolder().setCallback(new FGestureManager.TagHolder.Callback()
            {
                @Override
                public void onTagInterceptChanged(boolean tag)
                {
                    FTouchHelper.requestDisallowInterceptTouchEvent(FSwipeMenu.this, tag);
                }

                @Override
                public void onTagConsumeChanged(boolean tag)
                {
                    FTouchHelper.requestDisallowInterceptTouchEvent(FSwipeMenu.this, tag);
                }
            });
        }
        return mGestureManager;
    }

    @Override
    public boolean setState(State state, boolean anim)
    {
        getGestureManager().cancelConsumeEvent();
        return super.setState(state, anim);
    }

    @Override
    protected void onMenuDirectionChanged(Direction direction)
    {
        getGestureManager().getScroller().setMaxScrollDistance(getMaxScrollDistance());
    }

    @Override
    protected void abortAnimation()
    {
        getGestureManager().getScroller().abortAnimation();
    }

    @Override
    protected boolean smoothScroll(int start, int end)
    {
        if (getMenuDirection().isHorizontal())
            return getGestureManager().getScroller().scrollToX(start, end, -1);
        else
            return getGestureManager().getScroller().scrollToY(start, end, -1);
    }

    private boolean canPull()
    {
        final boolean checkViewIdle = isViewIdle();
        if (!checkViewIdle)
            return false;

        final double degreeX = getGestureManager().getTouchHelper().getDegreeXFromDown();
        if (degreeX < 45)
        {
            // horizontal
            final int delta = (int) getGestureManager().getTouchHelper().getDeltaXFromDown();
            return mHorizontalPullHelper.canPull(delta, getState());
        } else
        {
            // vertical
            final int delta = (int) getGestureManager().getTouchHelper().getDeltaYFromDown();
            return mVerticalPullHelper.canPull(delta, getState());
        }
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev)
    {
        return getGestureManager().onInterceptTouchEvent(ev);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event)
    {
        return getGestureManager().onTouchEvent(event);
    }

    @Override
    public void computeScroll()
    {
        if (getGestureManager().getScroller().computeScrollOffset())
            ViewCompat.postInvalidateOnAnimation(this);
    }

    @Override
    protected boolean isViewIdle()
    {
        return getGestureManager().getState() == FGestureManager.State.Idle;
    }

    @Override
    protected void onDetachedFromWindow()
    {
        super.onDetachedFromWindow();
        getGestureManager().getScroller().abortAnimation();
    }

    //---------- PullHelper Start ----------

    public abstract class PullHelper
    {
        public final boolean canPull(int delta, State state)
        {
            final View contentView = getContentView();
            if (contentView == null)
                return false;

            if (delta == 0)
                return false;

            if (!checkState(state, delta))
                return false;

            if (!checkScrollToBound(contentView, delta))
                return false;

            Direction direction = null;
            if (state == State.Close)
                direction = getMenuDirectionForCloseToOpen(delta);
            else if (state == getOpenStateCanPull(delta))
                direction = getMenuDirectionForOpenState(state);

            if (direction == null)
                throw new NullPointerException("direction was not found when start pull");

            final View view = getMenuView(direction);
            if (view == null)
                return false;

            setMenuDirection(direction);
            return true;
        }

        protected abstract boolean checkState(State state, int delta);

        protected abstract boolean checkScrollToBound(View view, int delta);

        protected abstract State getOpenStateCanPull(int delta);

        protected abstract Direction getMenuDirectionForCloseToOpen(int delta);

        protected abstract Direction getMenuDirectionForOpenState(State state);
    }

    private final PullHelper mHorizontalPullHelper = new PullHelper()
    {
        @Override
        protected boolean checkState(State state, int delta)
        {
            if (state == State.Close)
                return true;

            if (state == State.OpenLeft && delta < 0)
                return true;

            if (state == State.OpenRight && delta > 0)
                return true;

            return false;
        }

        @Override
        protected boolean checkScrollToBound(View view, int delta)
        {
            return delta < 0 ? FTouchHelper.isScrollToRight(view) : FTouchHelper.isScrollToLeft(view);
        }

        @Override
        protected Direction getMenuDirectionForCloseToOpen(int delta)
        {
            return delta < 0 ? Direction.Right : Direction.Left;
        }

        @Override
        protected State getOpenStateCanPull(int delta)
        {
            return delta < 0 ? State.OpenLeft : State.OpenRight;
        }

        @Override
        protected Direction getMenuDirectionForOpenState(State state)
        {
            if (state == State.OpenLeft)
                return Direction.Left;
            else if (state == State.OpenRight)
                return Direction.Right;
            else
                throw new RuntimeException();
        }
    };

    private final PullHelper mVerticalPullHelper = new PullHelper()
    {
        @Override
        protected boolean checkState(State state, int delta)
        {
            if (state == State.Close)
                return true;

            if (state == State.OpenTop && delta < 0)
                return true;

            if (state == State.OpenBottom && delta > 0)
                return true;

            return false;
        }

        @Override
        protected boolean checkScrollToBound(View view, int delta)
        {
            return delta < 0 ? FTouchHelper.isScrollToBottom(view) : FTouchHelper.isScrollToTop(view);
        }

        @Override
        protected Direction getMenuDirectionForCloseToOpen(int delta)
        {
            return delta < 0 ? Direction.Bottom : Direction.Top;
        }

        @Override
        protected State getOpenStateCanPull(int delta)
        {
            return delta < 0 ? State.OpenTop : State.OpenBottom;
        }

        @Override
        protected Direction getMenuDirectionForOpenState(State state)
        {
            if (state == State.OpenTop)
                return Direction.Top;
            else if (state == State.OpenBottom)
                return Direction.Bottom;
            else
                throw new RuntimeException();
        }
    };

    //---------- PullHelper end ----------
}
