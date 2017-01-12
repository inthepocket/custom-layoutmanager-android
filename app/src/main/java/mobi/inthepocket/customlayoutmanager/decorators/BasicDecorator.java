package mobi.inthepocket.customlayoutmanager.decorators;

import android.content.Context;
import android.graphics.Rect;
import android.support.v7.widget.RecyclerView;
import android.view.View;

import mobi.inthepocket.research.layoutmanager.R;
import mobi.inthepocket.customlayoutmanager.layoutmanagers.AdLayoutManager;

/**
 * Simple {@link android.support.v7.widget.RecyclerView.ItemDecoration} to demonstrate decorations support
 * in the {@link AdLayoutManager}.
 */
public class BasicDecorator extends RecyclerView.ItemDecoration
{
    private final float margin;

    public BasicDecorator(final Context context)
    {
        margin = context.getResources().getDimension(R.dimen.decorator_margin);
    }

    @Override
    public void getItemOffsets(Rect outRect, View view, RecyclerView parent, RecyclerView.State state)
    {
        outRect.left = (int) margin;
        outRect.top = (int) margin;
        outRect.right = (int) margin;
        outRect.bottom = (int) margin;
    }
}
