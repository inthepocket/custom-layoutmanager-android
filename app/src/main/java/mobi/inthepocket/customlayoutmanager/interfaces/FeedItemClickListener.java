package mobi.inthepocket.customlayoutmanager.interfaces;

/**
 * Used to notify the Activity of click events on items in the RecyclerView.
 */
public interface FeedItemClickListener
{
    void onFeedItemClicked(final int position);
}
