package mobi.inthepocket.customlayoutmanager.interfaces;

import mobi.inthepocket.customlayoutmanager.enums.LayoutGravity;
import mobi.inthepocket.customlayoutmanager.enums.SpanCount;
import mobi.inthepocket.customlayoutmanager.layoutmanagers.AdLayoutManager;

/**
 * Used by the {@link AdLayoutManager}
 * to retrieve information required to correctly display each item in the feed.
 */
public interface LayoutInfoLookup
{
    /**
     * The number of rows occupied by this item.
     * The large item in a trio will have 2, the rest 1.
     */
    SpanCount getRowSpan(final int position);

    /**
     * The number of columns occupied by this item.
     */
    SpanCount getColumnSpan(final int position);

    /**
     * Whether to let this View determine its own size or to give it a specific size in the LayoutManager.
     *
     * @return true if the View's XML will determine the size. False if the size will be determined by the LayoutManager.
     */
    boolean useViewSize(final int position);

    /**
     * Set whether the item should be placed in the left or right column.
     */
    LayoutGravity getGravity(final int position);
}
