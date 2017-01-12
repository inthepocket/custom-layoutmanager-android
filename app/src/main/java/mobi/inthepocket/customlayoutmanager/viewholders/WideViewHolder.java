package mobi.inthepocket.customlayoutmanager.viewholders;

import android.view.View;

import mobi.inthepocket.customlayoutmanager.interfaces.FeedItemClickListener;

/**
 * 2 colums wide.
 */
public class WideViewHolder extends BaseViewHolder
{
    public WideViewHolder(View itemView, final FeedItemClickListener listener)
    {
        super(itemView);
        itemView.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                listener.onFeedItemClicked(getAdapterPosition());
            }
        });
    }
}

