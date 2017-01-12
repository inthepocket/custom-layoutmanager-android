package mobi.inthepocket.customlayoutmanager;

import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;

import java.util.ArrayList;

import mobi.inthepocket.research.layoutmanager.R;
import mobi.inthepocket.customlayoutmanager.adapters.BasicAdapter;
import mobi.inthepocket.customlayoutmanager.decorators.BasicDecorator;
import mobi.inthepocket.customlayoutmanager.interfaces.FeedItemClickListener;
import mobi.inthepocket.customlayoutmanager.layoutmanagers.AdLayoutManager;

public class MainActivity extends AppCompatActivity implements FeedItemClickListener
{
    private RecyclerView recyclerView;
    private BasicAdapter adapter;
    private AdLayoutManager adLayoutManager;

    private ArrayList<String> items;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        adapter = new BasicAdapter(this, this);

        adLayoutManager = new AdLayoutManager(adapter.getLayoutInfoLookup());

        recyclerView = (RecyclerView) findViewById(R.id.recyclerview_main);
        final FloatingActionButton buttonInsert = (FloatingActionButton) findViewById(R.id.button_insert);
        buttonInsert.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                addItem();
            }
        });

        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(adLayoutManager);
        recyclerView.addItemDecoration(new BasicDecorator(this));

        items = getFeedContent();

        adapter.setItems(items);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        final MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        switch (item.getItemId())
        {
            case R.id.menu_add:
                addItem();
                return true;
            case R.id.menu_scroll:
                scrollToTop();
                return true;
            case R.id.menu_smooth_scroll:
                smoothScrollToTop();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void addItem()
    {
        final int insertPosition = adLayoutManager.findFirstCompletelyVisibleItemPosition();
        items.add(insertPosition, "wide");
        adapter.notifyItemInserted(insertPosition);
    }

    private void scrollToTop()
    {
        recyclerView.scrollToPosition(0);
    }

    private void smoothScrollToTop()
    {
        recyclerView.smoothScrollToPosition(0);
    }

    @Override
    public void onFeedItemClicked(int position)
    {
        items.remove(position);
        adapter.notifyItemRemoved(position);
    }

    private ArrayList<String> getFeedContent()
    {
        final ArrayList<String> items = new ArrayList<>();

        items.add("wide");
        items.add("wide");
        items.add("wide");
        // duo
        items.add("text left");
        items.add("text right");
        // trio left
        items.add("text right");
        items.add("text tall left");
        items.add("text right");
        // duo
        items.add("text left");
        items.add("text right");
        items.add("wide");
        // duo
        items.add("text left");
        items.add("text right");
        // trio right
        items.add("text left");
        items.add("text tall right");
        items.add("text left");
        // duo
        items.add("text left");
        items.add("text right");
        items.add("wide");
        items.add("wide");
        items.add("wide");
        // duo
        items.add("text left");
        items.add("text right");
        // trio right
        items.add("text left");
        items.add("text tall right");
        items.add("text left");
        items.add("wide");
        items.add("wide");
        items.add("wide");
        // trio left
        items.add("text right");
        items.add("text tall left");
        items.add("text right");
        // trio right
        items.add("text left");
        items.add("text tall right");
        items.add("text left");
        // trio right
        items.add("text left");
        items.add("text tall right");
        items.add("text left");
        // trio left
        items.add("text right");
        items.add("text tall left");
        items.add("text right");
        // trio right
        items.add("text left");
        items.add("text tall right");
        items.add("text left");
        // duo
        items.add("text left");
        items.add("text right");
        // trio left
        items.add("text right");
        items.add("text tall left");
        items.add("text right");
        // duo
        items.add("text left");
        items.add("text right");
        // trio right
        items.add("text left");
        items.add("text tall right");
        items.add("text left");
        // duo
        items.add("text left");
        items.add("text right");
        // trio left
        items.add("text left");
        items.add("text tall right");
        items.add("text left");
        // wide
        items.add("wide");
        // wide
        items.add("wide");
        // trio right
        items.add("text left");
        items.add("text tall right");
        items.add("text left");
        // duo
        items.add("text left");
        items.add("text right");
        // duo
        items.add("text left");
        items.add("text right");

        return items;
    }
}
