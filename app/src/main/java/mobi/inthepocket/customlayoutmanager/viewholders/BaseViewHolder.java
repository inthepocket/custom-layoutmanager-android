package mobi.inthepocket.customlayoutmanager.viewholders;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.TextView;

import mobi.inthepocket.research.layoutmanager.R;

public abstract class BaseViewHolder extends RecyclerView.ViewHolder
{
    private TextView textView;

    BaseViewHolder(View itemView)
    {
        super(itemView);

        textView = (TextView) itemView.findViewById(R.id.textview_item);
    }

    public void bindData(String data)
    {
        textView.setText(data);

    }
}
