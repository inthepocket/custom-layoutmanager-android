package mobi.inthepocket.customlayoutmanager.adapters;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;

import mobi.inthepocket.customlayoutmanager.R;
import mobi.inthepocket.customlayoutmanager.enums.LayoutGravity;
import mobi.inthepocket.customlayoutmanager.enums.SpanCount;
import mobi.inthepocket.customlayoutmanager.interfaces.FeedItemClickListener;
import mobi.inthepocket.customlayoutmanager.interfaces.LayoutInfoLookup;
import mobi.inthepocket.customlayoutmanager.viewholders.BaseViewHolder;
import mobi.inthepocket.customlayoutmanager.viewholders.PictureViewHolder;
import mobi.inthepocket.customlayoutmanager.viewholders.TallTextViewHolder;
import mobi.inthepocket.customlayoutmanager.viewholders.TextViewHolder;
import mobi.inthepocket.customlayoutmanager.viewholders.WideViewHolder;

import static mobi.inthepocket.customlayoutmanager.enums.SpanCount.ONE;
import static mobi.inthepocket.customlayoutmanager.enums.SpanCount.TWO;


/**
 * Standard RecyclerView adapter with multiple View types.
 * Uses {@link LayoutInfoLookup} to supply the LayoutManager with the necessary info.
 */
public class BasicAdapter extends RecyclerView.Adapter
{
    private static final int VIEWTYPE_PICTURE = 0;
    private static final int VIEWTYPE_TEXT = 1;
    private static final int VIEWTYPE_TEXT_TALL = 2;
    private static final int VIEWTYPE_WIDE = 3;

    private ArrayList<String> items;
    private LayoutInflater layoutInflater;

    private FeedItemClickListener listener;

    public BasicAdapter(Context context, FeedItemClickListener listener)
    {
        layoutInflater = LayoutInflater.from(context);
        this.listener = listener;
    }

    public void setItems(ArrayList<String> items)
    {
        this.items = items;
        notifyDataSetChanged();
    }

    @Override
    public int getItemViewType(int position)
    {
        if (items.get(position).contains("wide"))
        {
            return VIEWTYPE_WIDE;
        }
        else if (items.get(position).contains("picture"))
        {
            return VIEWTYPE_PICTURE;
        }
        else if (items.get(position).contains("tall"))
        {
            return VIEWTYPE_TEXT_TALL;
        }
        else
        {
            return VIEWTYPE_TEXT;
        }
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType)
    {
        View view = null;
        RecyclerView.ViewHolder viewHolder = null;

        switch (viewType)
        {
            case VIEWTYPE_PICTURE:
                view = layoutInflater.inflate(R.layout.listitem_picture, parent, false);
                viewHolder = new PictureViewHolder(view);
                break;
            case VIEWTYPE_TEXT:
                view = layoutInflater.inflate(R.layout.listitem_text, parent, false);
                viewHolder = new TextViewHolder(view);
                break;
            case VIEWTYPE_TEXT_TALL:
                view = layoutInflater.inflate(R.layout.listitem_text_tall, parent, false);
                viewHolder = new TallTextViewHolder(view);
                break;
            case VIEWTYPE_WIDE:
                view = layoutInflater.inflate(R.layout.listitem_wide, parent, false);
                viewHolder = new WideViewHolder(view, listener);
                break;
        }

        return viewHolder;
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position)
    {
        if (holder instanceof BaseViewHolder)
        {
            ((BaseViewHolder) holder).bindData(items.get(position) + ": " + position);
        }
    }

    @Override
    public int getItemCount()
    {
        return items == null ? 0 : items.size();
    }

    public LayoutInfoLookup getLayoutInfoLookup()
    {
        return layoutInfoLookup;
    }

    private final LayoutInfoLookup layoutInfoLookup = new LayoutInfoLookup()
    {
        @Override
        public SpanCount getRowSpan(int position)
        {
            switch (getItemViewType(position))
            {
                case VIEWTYPE_TEXT_TALL:
                    return TWO;
                default:
                    return ONE;
            }
        }

        @Override
        public SpanCount getColumnSpan(int position)
        {
            switch (getItemViewType(position))
            {
                case VIEWTYPE_WIDE:
                    return TWO;
                default:
                    return ONE;
            }
        }

        @Override
        public boolean useViewSize(int position)
        {
            switch (getItemViewType(position))
            {
                case VIEWTYPE_WIDE:
                    return true;
                default:
                    return false;
            }
        }

        @Override
        public LayoutGravity getGravity(int position)
        {
            return items.get(position).contains("right") ? LayoutGravity.RIGHT : LayoutGravity.LEFT;
        }
    };

}
