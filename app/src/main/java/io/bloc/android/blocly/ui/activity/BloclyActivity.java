package io.bloc.android.blocly.ui.activity;

import android.content.res.Configuration;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.nostra13.universalimageloader.cache.memory.impl.LruMemoryCache;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.nostra13.universalimageloader.core.assist.QueueProcessingType;

import java.util.ArrayList;

import io.bloc.android.blocly.BloclyApplication;
import io.bloc.android.blocly.R;
import io.bloc.android.blocly.api.model.RssFeed;
import io.bloc.android.blocly.api.model.RssItem;
import io.bloc.android.blocly.ui.adapter.ItemAdapter;
import io.bloc.android.blocly.ui.adapter.NavigationDrawerAdapter;

/**
 * Created by Administrator on 9/1/2015.
 */
public class BloclyActivity extends ActionBarActivity

        implements
        NavigationDrawerAdapter.NavigationDrawerAdapterDelegate,
        ItemAdapter.Delegate,
        ItemAdapter.DataSource {

    private static final String TAG = "BloclyActivity";

    private ItemAdapter mItemAdapter;
    private ActionBarDrawerToggle mDrawerToggle;
    private DrawerLayout mDrawerLayout;
    private NavigationDrawerAdapter mNavigationDrawerAdapter;
    private Menu mMenu;
    private View mOverflowButton;
    private RecyclerView mRecyclerView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_blocly);

        Toolbar toolbar = (Toolbar) findViewById(R.id.tb_activity_blocly);
        setSupportActionBar(toolbar);

        DisplayImageOptions defaultOptions = new DisplayImageOptions.Builder()
                .cacheOnDisk(true)
                .cacheInMemory(true)
                .build();

        ImageLoaderConfiguration configuration = new ImageLoaderConfiguration.Builder(this)
                .tasksProcessingOrder(QueueProcessingType.LIFO)
                .denyCacheImageMultipleSizesInMemory()
                .memoryCache(new LruMemoryCache(2 * 1024 * 1024))
                .memoryCacheSize(50 * 1024 * 1024)
                .diskCacheFileCount(100)
                .defaultDisplayImageOptions(defaultOptions)
                .build();

        ImageLoader.getInstance().init(configuration);


        mItemAdapter = new ItemAdapter();
        mItemAdapter.setDataSource(this);
        mItemAdapter.setDelegate(this);

        mRecyclerView = (RecyclerView) findViewById(R.id.rv_activity_blocly);

        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        mRecyclerView.setItemAnimator(new DefaultItemAnimator());
        mRecyclerView.setAdapter(mItemAdapter);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        mDrawerLayout = (DrawerLayout) findViewById(R.id.dl_activity_blocly);

        mDrawerToggle = new ActionBarDrawerToggle(this, mDrawerLayout, 0, 0) {
            @Override
            public void onDrawerClosed(View drawerView) {
                super.onDrawerClosed(drawerView);
                if(mOverflowButton != null) {
                    mOverflowButton.setAlpha(1f);
                    mOverflowButton.setEnabled(true);
                }
                if(mMenu == null) {
                    return;
                }
                for (int i = 0; i<mMenu.size(); i++) {
                    MenuItem item = mMenu.getItem(i);
                    item.setEnabled(true);
                    Drawable icon = item.getIcon();
                    if(icon != null) {
                        icon.setAlpha(255);
                    }
                }
            }

            @Override
            public void onDrawerOpened(View drawerView) {
                super.onDrawerOpened(drawerView);

                if(mOverflowButton != null) {
                    mOverflowButton.setEnabled(false);
                }
                if(mMenu == null) {
                    return;
                }
                for(int i = 0; i<mMenu.size(); i++) {
                    mMenu.getItem(i).setEnabled(false);
                }
            }

            @Override
            public void onDrawerSlide(View drawerView, float slideOffset) {
                super.onDrawerSlide(drawerView, slideOffset);
                if(mOverflowButton == null) {
                    ArrayList<View> foundViews = new ArrayList<View>();
                    getWindow().getDecorView().findViewsWithText(foundViews,
                            getString(R.string.abc_action_menu_overflow_description),
                            View.FIND_VIEWS_WITH_CONTENT_DESCRIPTION);
                    if(foundViews.size() > 0) {
                        mOverflowButton = foundViews.get(0);
                    }
                }

                if(mOverflowButton != null) {
                    mOverflowButton.setAlpha(1f - slideOffset);
                }
                if(mMenu == null) {
                    return;
                }
                for (int i = 0; i < mMenu.size(); i++) {
                    MenuItem item = mMenu.getItem(i);
                    Drawable icon = item.getIcon();
                    if(icon != null) {
                        icon.setAlpha((int) ((1f - slideOffset) * 255));
                    }
                }
            }
        };

        mDrawerLayout.setDrawerListener(mDrawerToggle);

        mNavigationDrawerAdapter = new NavigationDrawerAdapter();
        mNavigationDrawerAdapter.setDelegate(this);
        RecyclerView navigationRecyclerView = (RecyclerView) findViewById(R.id.rv_nav_activity_blocly);
        navigationRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        navigationRecyclerView.setItemAnimator(new DefaultItemAnimator());
        navigationRecyclerView.setAdapter(mNavigationDrawerAdapter);
    }
    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        mDrawerToggle.syncState();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        mDrawerToggle.onConfigurationChanged(newConfig);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if(mDrawerToggle.onOptionsItemSelected(item)) {
            return  true;
        }
        Toast.makeText(this, item.getTitle(), Toast.LENGTH_SHORT).show();
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        getMenuInflater().inflate(R.menu.blocly, menu);
        mMenu = menu;
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public void didSelectNavigationOption(NavigationDrawerAdapter adapter, NavigationDrawerAdapter.NavigationOption navigationOption) {
        mDrawerLayout.closeDrawers();
        Toast.makeText(this, "show the " + navigationOption.name(), Toast.LENGTH_SHORT).show();
    }

    @Override
    public void didSelectFeed(NavigationDrawerAdapter adapter, RssFeed feed) {

        mDrawerLayout.closeDrawers();
        Toast.makeText(this, "Show RSS items from " + feed.getTitle(), Toast.LENGTH_SHORT).show();

    }

    @Override
    public RssItem getRssItem(ItemAdapter itemAdapter, int position) {
        return BloclyApplication.getSharedDataSource().getItems().get(position);
    }

    @Override
    public RssFeed getRssFeed(ItemAdapter itemAdapter, int position) {
        return BloclyApplication.getSharedDataSource().getFeeds().get(0);
    }

    @Override
    public int getItemCount(ItemAdapter itemAdapter) {
        return BloclyApplication.getSharedDataSource().getItems().size();
    }

    @Override
    public void onItemClicked(ItemAdapter itemAdapter, RssItem rssItem) {
        int positionToExpand = -1;
        int positionToContract = -1;

        int selectedPosition = BloclyApplication.getSharedDataSource().getItems().
                indexOf(rssItem);

        //get the pixel length of the views in the RecyclerView

        //if there is a currently expanded item
        if (itemAdapter.getExpandedItem() != null) {
            positionToContract = BloclyApplication.getSharedDataSource().getItems().
                    indexOf(itemAdapter.getExpandedItem());

        }

        //if an item, other than the currently expanded item is selected
        if (itemAdapter.getExpandedItem() != rssItem) {
            positionToExpand = BloclyApplication.getSharedDataSource().getItems().
                    indexOf(rssItem);
            itemAdapter.setExpandedItem(rssItem);
        } else {
            itemAdapter.setExpandedItem(null);
        }
        if (positionToContract > -1) {
            itemAdapter.notifyItemChanged(positionToContract);
        }
        if (positionToExpand > -1) {
            itemAdapter.notifyItemChanged(positionToExpand);
        }




//        int viewHeight;
//
//        //cheap, crude and gets the job done to find the original height
//        int heightPos0 = mRecyclerView.getChildAt(0).getHeight();
//        int heightPos1 = mRecyclerView.getChildAt(1).getHeight();
//        viewHeight = (heightPos0 < heightPos1) ? heightPos0 : heightPos1;
//
//        //if the position to expand is not the last item in the view...
//        if(selectedPosition < itemAdapter.getItemCount() - 1) {

        //used altenativv scrollToPosition(int position, int offset) instead of scrollTo(x,y)
        //because scrollTo will not work on some Samsung devices
        //  mRecyclerView.scrollTo(selectedPosition*viewHeight, 0);

        //}


        
        //the original solution proposed would not work - new solution from :
        //  http://stackoverflow.com/questions/28428409/java-lang-unsupportedoperationexception-recyclerview-does-not-support-scrolling/28448691#28448691

        //this is a much better solution
        RecyclerView.LayoutManager manager = mRecyclerView.getLayoutManager();
        if (manager instanceof LinearLayoutManager) {
            LinearLayoutManager linearLayoutManager = (LinearLayoutManager) manager;
            linearLayoutManager.scrollToPositionWithOffset(selectedPosition, 0);
        }


    }
}
