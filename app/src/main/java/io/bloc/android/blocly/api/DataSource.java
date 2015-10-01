package io.bloc.android.blocly.api;

import android.content.ContentValues;
import android.database.sqlite.SQLiteDatabase;

import java.util.ArrayList;
import java.util.List;

import io.bloc.android.blocly.BloclyApplication;
import io.bloc.android.blocly.BuildConfig;
import io.bloc.android.blocly.R;
import io.bloc.android.blocly.api.model.RssFeed;
import io.bloc.android.blocly.api.model.RssItem;
import io.bloc.android.blocly.api.model.database.DatabaseOpenHelper;
import io.bloc.android.blocly.api.model.database.table.RssFeedTable;
import io.bloc.android.blocly.api.model.database.table.RssItemTable;
import io.bloc.android.blocly.api.network.GetFeedsNetworkRequest;

/**
 * Created by Administrator on 9/2/2015.
 */
public class DataSource {


    private DatabaseOpenHelper mDatabaseOpenHelper;
    private RssFeedTable mRssFeedTable;
    private RssItemTable mRssItemTable;
    private List<RssFeed> mFeeds;
    private List<RssItem> mItems;

    public DataSource() {
        mRssFeedTable = new RssFeedTable();
        mRssItemTable = new RssItemTable();

        mDatabaseOpenHelper = new DatabaseOpenHelper(BloclyApplication.getSharedInstance(),
                mRssFeedTable, mRssItemTable);

        mFeeds = new ArrayList<RssFeed>();
        mItems = new ArrayList<RssItem>();
        createFakeData();

        new Thread(new Runnable() {
            @Override
            public void run() {

                if(BuildConfig.DEBUG && true) {
                    BloclyApplication.getSharedInstance().deleteDatabase("blocly_db");

                    //recreate the database after delete
                    mDatabaseOpenHelper = new DatabaseOpenHelper(BloclyApplication.getSharedInstance(),
                            mRssFeedTable, mRssItemTable);
                }
                SQLiteDatabase writableDatabase = mDatabaseOpenHelper.getWritableDatabase();

                List<GetFeedsNetworkRequest.FeedResponse> feedResponses = new GetFeedsNetworkRequest("http://feeds.feedburner.com/androidcentral?format=xml")
                        .performRequest();

                GetFeedsNetworkRequest.FeedResponse response = feedResponses.get(0); // get first feed

                ContentValues feedValues = new ContentValues();

                feedValues.put("link", response.channelURL);
                feedValues.put("title", response.channelTitle);
                feedValues.put("description", response.channelDescription);
                feedValues.put("feed_url", response.channelFeedURL);

                long feedId = writableDatabase.insert("rss_feeds", null, feedValues);

                for(GetFeedsNetworkRequest.ItemResponse itemResponses : response.channelItems) {

                    ContentValues itemValues = new ContentValues();

                    itemValues.put("link", itemResponses.itemURL);
                    itemValues.put("title", itemResponses.itemTitle);
                    itemValues.put("description", itemResponses.itemDescription);
                    itemValues.put("guid", itemResponses.itemGUID);

                    //stuff for date

                    long itemPubDate = System.currentTimeMillis();

                    itemValues.put("pub_date", itemPubDate);
                    itemValues.put("enclosure", itemResponses.itemEnclosureURL);
                    itemValues.put("mime_type", itemResponses.itemEnclosureMIMEType);
                    itemValues.put("rss_feed", feedId);

                    writableDatabase.insert("rss_items", null, itemValues);

                }

            }
        }).start();
    }



    public List<RssFeed> getFeeds() {
        return mFeeds;
    }

    public List<RssItem> getItems() {
        return mItems;
    }

    void createFakeData() {
        getFeeds().add(new RssFeed("My favorite feed",
                "This feed is just increadible, I can't even begin to tell you...",
                "http://favoritefeed.net", "http://feeds.feedburner.com/favorite_feed?format=xml"));

        for (int i = 0; i < 10; i++) {
            getItems().add(new RssItem(String.valueOf(i),
                    BloclyApplication.getSharedInstance().getString(R.string.placeholder_caption) + " " + i,
                    BloclyApplication.getSharedInstance().getString(R.string.placeholder_content),
                    "http://favoritefeed.net?story_id=an-incredible-news-story",
                    "http://rs1img.memecdn.com/silly-dog_o_511213.jpg",
                    0, System.currentTimeMillis(), false, false));
        }
    }


}
