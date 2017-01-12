package mobi.inthepocket.customlayoutmanager.layoutmanagers;

import android.graphics.PointF;
import android.graphics.Rect;
import android.support.v7.widget.LinearSmoothScroller;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;

import mobi.inthepocket.customlayoutmanager.enums.LayoutGravity;
import mobi.inthepocket.customlayoutmanager.interfaces.LayoutInfoLookup;

import static mobi.inthepocket.customlayoutmanager.enums.SpanCount.TWO;


/**
 * Adapter that lays out views in two columns. Views can span one or two rows or columns.
 * Valid sizes are 1x1, 1x2, 2x1.
 * Valid "groups" are UNO (2x1),  DUO (1x1 + 1x1) and TRIO (1x1 + 1x2 + 1x1) (both left and right variant).
 * <p>
 * Views need to be in an order that will not create layout gaps.
 * It is for example not allowed to create a feed order where View 5 and 7 are on screen, but 6 is offscreen.
 */
public class AdLayoutManager extends RecyclerView.LayoutManager
{
    // First adapter position currently visible.
    private int firstPosition;

    private LayoutInfoLookup layoutInfoLookup;

    // Top of the first item in each column.
    // New Views added when scrolling up will be placed with their bottoms aligns with the top of this previous View.
    private int topLeft, topRight;
    // Bottom of the last item in each column.
    // New Views added when scrolling down will be placed so that their top aligns with the bottom of this previous View.
    private int bottomLeft, bottomRight;

    // Fixed ratios to determine the Views' height based on the screen or column width.
    private float ratioWide;
    private float ratioStandard;
    private float ratioTall;

    /**
     * @param layoutInfoLookup The {@link LayoutInfoLookup} to use to retrieve info for the items in the adapter.
     */
    public AdLayoutManager(LayoutInfoLookup layoutInfoLookup)
    {
        ratioWide = 1.31f;
        ratioStandard = 1f;
        // Twice the height of a standard tile
        ratioTall = ratioStandard / 2;

        this.layoutInfoLookup = layoutInfoLookup;
    }

    @Override
    public RecyclerView.LayoutParams generateDefaultLayoutParams()
    {
        return new RecyclerView.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
    }

    @Override
    public void onLayoutChildren(RecyclerView.Recycler recycler, RecyclerView.State state)
    {
        // Having the large item in a trio as firstPosition here causes several issues.
        // Ugly fix applied here shifts firstPosition back by one so we start from the first item of the trio.
        // This makes sure we can use the child at 0 for both left and right position values.
        if (layoutInfoLookup.getRowSpan(firstPosition) == TWO)
        {
            firstPosition--;
        }

        // Check if this is the initial layout or if there are already child Views attached.
        final View oldTopView = getChildAt(0);
        if (oldTopView == null)
        {
            // Clean initial layout. Use the default start values.
            topLeft = topRight = bottomLeft = bottomRight = getPaddingTop();
        }
        else
        {
            // onLayoutChildren can also be called for situations other than the initial layout:
            // a child View requested a new layout, notifyDataSetChanged was called on the adapter, scrollToPosition was used,....
            // We record the top/bottom values here so we can detach all child Views and then lay them out again in the same position.
            topLeft = topRight = bottomLeft = bottomRight = getDecoratedTop(oldTopView);
        }

        final int parentBottom = getHeight() - getPaddingBottom();

        detachAndScrapAttachedViews(recycler);

        final int count = state.getItemCount();

        // Keep adding views until we run out of items or until the visible area has been filled with Views.
        for (int i = 0; firstPosition + i < count && (bottomRight < parentBottom || bottomLeft < parentBottom); i++)
        {
            final int currentPosition = firstPosition + i;

            addViewForPosition(recycler, currentPosition, true);
        }
    }

    @Override
    public boolean canScrollVertically()
    {
        return true;
    }

    /**
     * Scrolls the currently attached child Views up or down after checking the distance they can be moved.
     * Empty space created by this scroll event will be filled with new Views.
     *
     * @param dy       The distance of the scroll event. Child Views should be moved this distance if possible.
     * @param recycler Recycler to retrieve new Views from.
     * @param state    Current RecyclerView state.
     * @return The distance the child Views were actually moved.
     */
    @Override
    public int scrollVerticallyBy(final int dy, final RecyclerView.Recycler recycler, final RecyclerView.State state)
    {
        if (getChildCount() == 0)
        {
            return 0;
        }

        int scrolled = 0;

        if (dy < 0)
        {
            while (scrolled > dy)
            {
                // Scrolling up

                // Compare the top views in each column. Use the one that is going to cause gaps first.
                final int top = Math.max(topLeft, topRight);
                // Check distance between top of first child and top of screen
                final int hangingTop = Math.max(-top, 0);
                // Calculate how far we will actually scroll
                // We will either scroll the full input distance or just enough to not display an empty gap
                // In the second scenario we will scroll part of the way, add a new view, and scroll the rest of the dy in the next loop pass
                final int scrollBy = Math.min(scrolled - dy, hangingTop);
                // Determine how much distance we still have to scroll after completing this current loop
                scrolled -= scrollBy;
                // Scroll all children up
                scrollChildViews(scrollBy);

                // We've scrolled beyond the visible views, make and layout the next one(s)
                if (firstPosition > 0 && scrolled > dy)
                {
                    // We're adding one new View above the content which means the firstPosition gets decremented by one.
                    firstPosition--;

                    addViewForPosition(recycler, firstPosition, false);
                }
                else
                {
                    break;
                }
            }
        }
        else if (dy > 0)
        {
            // Scrolling down

            final int parentHeight = getHeight();
            while (scrolled < dy)
            {
                // Compare the bottom views in each column. Use the one that is going to cause gaps first.
                final int top = Math.min(bottomLeft, bottomRight);
                // Check distance between bottom of last child and bottom of screen
                final int hangingBottom = Math.max(top - parentHeight, 0);
                // Calculate how far we will actually scroll
                // We will either scroll the full input distance or just enough to not display an empty gap
                // In the second scenario we will scroll part of the way, add a new view, and scroll the rest of the dy in the next loop pass
                final int scrollBy = -Math.min(dy - scrolled, hangingBottom);
                // Determine how much distance we still have to scroll after completing this current loop
                scrolled -= scrollBy;

                // Scroll all children down
                scrollChildViews(scrollBy);

                if (scrolled < dy && getItemCount() > firstPosition + getChildCount())
                {
                    // No firstPosition changes are done here. Adding a View at the bottom does not mean one went offscreen at the top.
                    // Incrementing of firstPosition is done in the recycleViewsOutOfBounds method.
                    final int currentPosition = firstPosition + getChildCount();
                    addViewForPosition(recycler, currentPosition, true);
                }
                else
                {
                    break;
                }
            }
        }

        // Scroll event handled. Check which Views were moved completely off screen and remove them from the RecyclerView.
        recycleViewsOutOfBounds(recycler);

        // Let the RecyclerView know how much we actually scrolled.
        // If this value is less than the input dy, edge glow effects will be shown to indicate the edge of the content was reached.
        return scrolled;
    }

    /**
     * Move child Views the desired direction and distance.
     */
    private void scrollChildViews(final int offset)
    {
        offsetChildrenVertical(offset);

        // Update the top and bottom values for the new View positions
        topLeft += offset;
        topRight += offset;
        bottomLeft += offset;
        bottomRight += offset;
    }

    /**
     * Lays out the view at the selected position.
     *
     * @param recycler      The recycler to retrieve the View from.
     * @param position      The adapter position of the view to lay out.
     * @param scrollingDown Whether this was triggered by scrolling down (true) or up (false).
     */
    private void addViewForPosition(final RecyclerView.Recycler recycler, int position, final boolean scrollingDown)
    {
        if (layoutInfoLookup.getRowSpan(position) == TWO)
        {
            // 1 column x 2 rows
            addTallView(recycler, position, scrollingDown);
        }
        else if (layoutInfoLookup.getColumnSpan(position) == TWO)
        {
            // 2 columns x 1 row
            addFullWidthView(recycler, position, scrollingDown);
        }
        else
        {
            // 1 column by 1 row
            addStandardView(recycler, position, scrollingDown);
        }
    }

    /**
     * Add a View that spans both columns.
     *
     * @param recycler      The Recycler to add the new view to.
     * @param index         The adapter index of the item to add.
     * @param scrollingDown Whether this was triggered by scrolling down (true) or (up).
     */
    private void addFullWidthView(final RecyclerView.Recycler recycler, final int index, boolean scrollingDown)
    {
        int top = 0;
        int bottom = 0;

        if (scrollingDown)
        {
            top = Math.max(bottomLeft, bottomRight);
        }
        else
        {
            bottom = Math.min(topLeft, topRight);
        }

        // Supports both dynamic View size (from XML) and fixed size from ratio (calculated by this LayoutManager)
        if (layoutInfoLookup.useViewSize(index))
        {
            final View v = recycler.getViewForPosition(index);

            addView(v, scrollingDown ? getChildCount() : 0);
            measureChildWithMargins(v, 0, 0);

            if (scrollingDown)
            {
                bottom = top + getDecoratedMeasuredHeight(v);
            }
            else
            {
                top = bottom - getDecoratedMeasuredHeight(v);
            }

            layoutDecorated(v, getRecyclerViewLeft(), top, getRecyclerViewRight(), bottom);
        }
        else
        {
            final int tileHeight = (int) (getWidth() / ratioWide);

            if (scrollingDown)
            {
                bottom = top + tileHeight;
            }
            else
            {
                top = bottom - tileHeight;
            }

            measureAndAddViewAtIndex(recycler, index, scrollingDown ? getChildCount() : 0, getRecyclerViewLeft(), top, getRecyclerViewRight(), bottom, 0);
        }

        // View is full width, its values count for both the left and right column.
        if (scrollingDown)
        {
            bottomLeft = bottom;
            bottomRight = bottom;
        }
        else
        {
            topLeft = top;
            topRight = top;
        }
    }


    /**
     * Add a View that is 1 column wide and 1 row high.
     *
     * @param recycler      The Recycler to add the new views to.
     * @param index         The adapter position of the item.
     * @param scrollingDown Whether this was triggered by scrolling down (true) or (up).
     */
    private void addStandardView(final RecyclerView.Recycler recycler, final int index, final boolean scrollingDown)
    {
        addHalfWidthView(recycler, index, ratioStandard, scrollingDown);
    }

    /**
     * Add a View that is 1 column wide and 2 rows high.
     *
     * @param recycler      The Recycler to add the new views to.
     * @param index         The adapter position of the item.
     * @param scrollingDown Whether this was triggered by scrolling down (true) or (up).
     */
    private void addTallView(final RecyclerView.Recycler recycler, final int index, final boolean scrollingDown)
    {
        addHalfWidthView(recycler, index, ratioTall, scrollingDown);
    }

    /**
     * Adds a view that is 1 column wide.
     *
     * @param recycler      The Recycler to add the new views to.
     * @param index         The adapter position of the item.
     * @param ratio         The ratio of this view.
     * @param scrollingDown Whether this was triggered by scrolling down (true) or up (false).
     */
    private void addHalfWidthView(final RecyclerView.Recycler recycler, final int index, final float ratio, final boolean scrollingDown)
    {
        // A half width View will be laid out in either the left or right column.
        final boolean isLeft = layoutInfoLookup.getGravity(index) == LayoutGravity.LEFT;

        final int tileHeight = (int) (getWidth() / 2 / ratio);

        int left, top, right, bottom;

        if (scrollingDown)
        {
            top = isLeft ? bottomLeft : bottomRight;
            bottom = top + tileHeight;
        }
        else
        {
            bottom = isLeft ? topLeft : topRight;
            top = bottom - tileHeight;
        }

        int middle = (getRecyclerViewLeft() + getRecyclerViewRight()) / 2;

        left = isLeft ? getRecyclerViewLeft() : middle;
        right = isLeft ? middle : getRecyclerViewRight();

        measureAndAddViewAtIndex(recycler, index, scrollingDown ? getChildCount() : 0, left, top, right, bottom, right - left);

        if (scrollingDown)
        {
            if (layoutInfoLookup.getGravity(index) == LayoutGravity.LEFT)
            {
                bottomLeft = bottom;
            }
            else
            {
                bottomRight = bottom;
            }
        }
        else
        {
            if (layoutInfoLookup.getGravity(index) == LayoutGravity.LEFT)
            {
                topLeft = top;
            }
            else
            {
                topRight = top;
            }
        }
    }


    /**
     * Measures a view and adds it to the recycler at the specified index.
     *
     * @param recycler      The Recycler to retrieve the View from.
     * @param adapterIndex  The adapter index of the item to add.
     * @param index         The View index where this view will be added in the RecyclerView viewgroup.
     * @param left          Left side of this View.
     * @param top           Top of this View.
     * @param bottom        Bottom of this View.
     * @param occupiedWidth The amount of horizontal space already occupied (by other Views) and therefore not available to this view.
     */
    private void measureAndAddViewAtIndex(final RecyclerView.Recycler recycler, final int adapterIndex, final int index, final int left, final int top, final int right, final int bottom, final int occupiedWidth)
    {
        final View view = recycler.getViewForPosition(adapterIndex);

        addView(view, index);
        measureChildWithMarginsAndDesiredHeight(view, bottom - top, occupiedWidth);
        layoutDecorated(view, left, top, right, bottom);
    }

    /**
     * Largely copied from the default {@link RecyclerView.LayoutManager#measureChildWithMargins(View, int, int)} method,
     * but changed to use a custom, calculated height instead of a View's default XML height.
     * <p>
     * The heightUsed parameter has been omitted because it gets ignored for vertical feeds.
     *
     * @param child         The child View to measure.
     * @param desiredHeight The height this View should be given.
     * @param occupiedWidth The amount of horizontal space already occupied (by other Views) and therefore not available to this view.
     */
    private void measureChildWithMarginsAndDesiredHeight(View child, int desiredHeight, int occupiedWidth)
    {
        final RecyclerView.LayoutParams lp = (RecyclerView.LayoutParams) child.getLayoutParams();


        // The measureChildWithMargins method uses a private method to get the item decorations, we solve it like this:
        final Rect decorations = new Rect();
        calculateItemDecorationsForChild(child, decorations);
        occupiedWidth += decorations.left + decorations.right;
        final int heightUsed = decorations.top + decorations.bottom;


        final int widthSpec = getChildMeasureSpec(getWidth(),
                getWidthMode(),
                getPaddingLeft() + getPaddingRight() + lp.leftMargin + lp.rightMargin + occupiedWidth,
                lp.width,
                canScrollHorizontally());
        // The standard measureChildWithMargins method uses the height from the View's LayoutParams, but we want to be able to supply our own, calculated height.
        final int heightSpec = getChildMeasureSpec(getHeight(),
                getHeightMode(),
                getPaddingTop() + getPaddingBottom() + lp.topMargin + lp.bottomMargin + heightUsed,
                desiredHeight - heightUsed,
                canScrollVertically());

        // The measureChildWithMargins method has an extra shouldMeasureChild check and only measures the view if required.
        // That method is private, so we measure the view every time. :(
        child.measure(widthSpec, heightSpec);
    }

    /**
     * Removes and recyclers views that are no longer on screen.
     *
     * @param recycler The Recycler to recycle Views into.
     */
    private void recycleViewsOutOfBounds(RecyclerView.Recycler recycler)
    {
        boolean removedTop = false;
        boolean removedBottom = false;

        final int childCount = getChildCount();
        boolean foundFirstVisibleView = false;
        int firstVisibleView = 0;
        int lastVisibleView = 0;
        for (int i = 0; i < childCount; i++)
        {
            final View v = getChildAt(i);
            if (v.hasFocus() || isVisible(v))
            {
                if (!foundFirstVisibleView)
                {
                    firstVisibleView = i;
                    foundFirstVisibleView = true;
                }
                lastVisibleView = i;
            }
        }
        for (int i = childCount - 1; i > lastVisibleView; i--)
        {
            removedBottom = true;
            removeAndRecycleViewAt(i, recycler);
        }
        for (int i = firstVisibleView - 1; i >= 0; i--)
        {
            removedTop = true;
            removeAndRecycleViewAt(i, recycler);
        }
        if (getChildCount() == 0)
        {
            firstPosition = 0;
        }
        else
        {
            firstPosition += firstVisibleView;
        }

        if (removedBottom)
        {
            updateBottomValues();
        }
        else if (removedTop)
        {
            updateTopValues();
        }
    }

    /**
     * @param v The View to check the visibility for.
     * @return true if the View is at least partially visible.
     */
    private boolean isVisible(final View v)
    {
        final int parentWidth = getWidth();
        final int parentHeight = getHeight();

        return getDecoratedRight(v) >= 0 &&
                getDecoratedLeft(v) <= parentWidth &&
                getDecoratedBottom(v) >= 0 &&
                getDecoratedTop(v) <= parentHeight;
    }

    /**
     * Updates the top of the first Views in each column.
     */
    private void updateTopValues()
    {
        updateColumnValues(true);
    }

    /**
     * Updates the bottom of the last Views in each column.
     */
    private void updateBottomValues()
    {
        updateColumnValues(false);
    }

    /**
     * Updates the top/bottom of the first/last Views in each column.
     *
     * @param updateTopValues True if used for top, false if used for bottom.
     */
    private void updateColumnValues(final boolean updateTopValues)
    {
        boolean foundLeft = false;
        boolean foundRight = false;

        final int startIndex = updateTopValues ? 0 : getChildCount() - 1;
        final int endIndex = updateTopValues ? getChildCount() - 1 : 0;
        final int step = updateTopValues ? 1 : -1;

        for (int i = startIndex; i != endIndex; i += step)
        {
            final View checkingView = getChildAt(i);
            final int adapterPosition = getPosition(checkingView);

            if (layoutInfoLookup.getColumnSpan(adapterPosition) == TWO)
            {
                if (updateTopValues)
                {
                    topLeft = topRight = getDecoratedTop(checkingView);
                }
                else
                {
                    bottomLeft = bottomRight = getDecoratedBottom(checkingView);
                }

                break;
            }
            else
            {
                if (layoutInfoLookup.getGravity(adapterPosition) == LayoutGravity.LEFT)
                {
                    if (updateTopValues)
                    {
                        topLeft = getDecoratedTop(checkingView);
                    }
                    else
                    {
                        bottomLeft = getDecoratedBottom(checkingView);
                    }

                    foundLeft = true;
                }
                else
                {
                    if (updateTopValues)
                    {
                        topRight = getDecoratedTop(checkingView);
                    }
                    else
                    {
                        bottomRight = getDecoratedBottom(checkingView);
                    }

                    foundRight = true;
                }

                if (foundRight && foundLeft)
                {
                    break;
                }
            }
        }
    }

    @Override
    public void onItemsAdded(RecyclerView recyclerView, int positionStart, int itemCount)
    {
        super.onItemsAdded(recyclerView, positionStart, itemCount);

        if(positionStart < firstPosition)
        {
            firstPosition += itemCount;
        }
    }

    @Override
    public void onItemsRemoved(RecyclerView recyclerView, int positionStart, int itemCount)
    {
        super.onItemsRemoved(recyclerView, positionStart, itemCount);

        if(positionStart < firstPosition)
        {
            firstPosition -= itemCount;
        }
    }

    /**
     * Jumps to the requested position. Not animated.
     *
     * @param position The adapter position to jump to.
     */
    @Override
    public void scrollToPosition(final int position)
    {
        firstPosition = position;

        // Remove all Views so scroll offset is reset and our target View gets its top aligned with the top of the RecyclerView.
        removeAllViews();
        requestLayout();
    }

    /**
     * Scrolls to the requested position. Animated.
     */
    @Override
    public void smoothScrollToPosition(RecyclerView recyclerView, RecyclerView.State state, int position)
    {
        final LinearSmoothScroller linearSmoothScroller = new LinearSmoothScroller(recyclerView.getContext())
        {
            @Override
            public PointF computeScrollVectorForPosition(int targetPosition)
            {
                if (getChildCount() == 0)
                {
                    return null;
                }

                // Determine which direction we need to scroll in (-1 is up, 1 is down)
                final int firstChildPos = getAdapterIndexForViewIndex(0);
                final int direction = targetPosition < firstChildPos ? -1 : 1;

                // Only need to scroll in the y direction.
                return new PointF(0, direction);
            }

            @Override
            protected int getVerticalSnapPreference()
            {
                return SNAP_TO_START;
            }
        };
        linearSmoothScroller.setTargetPosition(position);
        startSmoothScroll(linearSmoothScroller);
    }

    /**
     * @return The adapter position of the first View that is (partially) visible on screen.
     */
    public int findFirstVisibleItemPosition()
    {
        return getChildCount() > 0 ? getPosition(getChildAt(0)) : 0;
    }

    /**
     * @return The adapter position of the first View that is completely visible on screen.
     */
    public int findFirstCompletelyVisibleItemPosition()
    {
        if (getChildCount() > 0)
        {
            for (int i = 0; i < getChildCount(); i++)
            {
                final View v = getChildAt(i);
                if (getDecoratedTop(v) >= getPaddingTop() && getDecoratedBottom(v) <= getHeight() - getPaddingBottom())
                {
                    return getPosition(v);
                }
            }
        }

        return 0;
    }

    /**
     * @return The adapter position of the last View that is (partially) visible on screen.
     */
    public int findLastVisibleItemPosition()
    {
        return getChildCount() > 0 ? getPosition(getChildAt(getChildCount() - 1)) : 0;
    }

    /**
     * @return The adapter position of the first View that is completely visible on screen.
     */
    public int findLastCompletelyVisibleItemPosition()
    {
        if (getChildCount() > 0)
        {
            for (int i = getChildCount() - 1; i >= 0; i--)
            {
                final View v = getChildAt(i);
                if (getDecoratedTop(v) >= getPaddingTop() && getDecoratedBottom(v) <= getHeight() - getPaddingBottom())
                {
                    return getPosition(v);
                }
            }
        }
        return 0;
    }

    /**
     * @return The left side of the parent RecyclerView.
     */
    private int getRecyclerViewLeft()

    {
        return getPaddingLeft();
    }

    /**
     * @return The right side of the parent RecyclerView.
     */
    private int getRecyclerViewRight()
    {
        return getWidth() - getPaddingRight();
    }

    /**
     * Converts the View index to the adapter index.
     *
     * @param viewIndex The childView index in the parent RecyclerView ViewGroup.
     * @return The adapter index for the View found at this position.
     */
    private int getAdapterIndexForViewIndex(final int viewIndex)
    {
        return getPosition(getChildAt(viewIndex));
    }
}
