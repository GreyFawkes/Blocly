package io.bloc.android.blocly.api;

import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Handler;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import io.bloc.android.blocly.BloclyApplication;
import io.bloc.android.blocly.BuildConfig;
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


    public interface Callback<Result> {
        void onSuccess(Result result);
        void onError(Result result);
    }

    private DatabaseOpenHelper mDatabaseOpenHelper;
    private RssFeedTable mRssFeedTable;
    private RssItemTable mRssItemTable;
    private ExecutorService mExecutorService;


    public DataSource() {
        mRssFeedTable = new RssFeedTable();
        mRssItemTable = new RssItemTable();

        mExecutorService = Executors.newSingleThreadExecutor();
        mDatabaseOpenHelper = new DatabaseOpenHelper(BloclyApplication.getSharedInstance(),
                mRssFeedTable, mRssItemTable);

        if (BuildConfig.DEBUG && true) {
            BloclyApplication.getSharedInstance().deleteDatabase("blocly_db");
        }
    }

    public void fetchNewsFeed(final String feedUrl, final Callback<RssFeed> callback) {

        final Handler callbackThreadHandler = new Handler();
        submitTask(new Runnable() {
            @Override
            public void run() {

                Cursor existingFeedCursor = RssFeedTable.fecthFeedWithURL(mDatabaseOpenHelper.getReadableDatabase(), feedUrl);
                if(existingFeedCursor.moveToFirst()) {
                    final RssFeed fetchedFeed = feedFromCursor(existingFeedCursor);
                    existingFeedCursor.close();

                    callbackThreadHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            callback.onSuccess(fetchedFeed);
                        }
                    });
                    return;
                }

                GetFeedsNetworkRequest getFeedsNetworkRequest = new GetFeedsNetworkRequest(feedUrl);
                List<GetFeedsNetworkRequest.FeedResponse> feedResponses = getFeedsNetworkRequest.performRequest();


                ///I stopped here
                if(getFeedsNetworkRequest.getErrorCode() != 0) {
                    final String errorMessage
                }
                //I stopped here



                List<RssItem> newRssItems = new ArrayList<RssItem>();


                for( GetFeedsNetworkRequest.ItemResponse itemResponse : androidCentral.channelItems) {
                    long itemPubDate = System.currentTimeMillis();
                    DateFormat dateFormat = new SimpleDateFormat("EEE, dd MM yyyy kk:mm:ss z", Locale.ENGLISH);
                    try {
                        itemPubDate = dateFormat.parse(itemResponse.itemPubDate).getTime();
                    } catch (ParseException e) {
                        e.printStackTrace();
                    }

                    long newItemRowId = new RssItemTable.Builder()
                            .setTitle(itemResponse.itemTitle)
                            .setDescription(itemResponse.itemDescription)
                            .setEnclosure(itemResponse.itemEnclosureURL)
                            .setMIMEType(itemResponse.itemEnclosureMIMEType)
                            .setGUID(itemResponse.itemGUID)
                            .setLink(itemResponse.itemURL)
                            .setPubDate(itemResponse.itemPubDate)
                            .setRssFeed(androidCentralFeedId)
                            .insert(writableDatabase);

                    Cursor itemCursor = mRssItemTable.fetchRow(mDatabaseOpenHelper.getReadableDatabase(), newItemRowId);

                    itemCursor.moveToFirst();
                    RssItem newRssItem = itemFromCursor(itemCursor);
                    newRssItems.add(newRssItem);

                    itemCursor.close();

                }

                Cursor androidCentralCursor = mRssFeedTable.fetchRow(mDatabaseOpenHelper.getReadableDatabase(), androidCentralFeedId);
                androidCentralCursor.moveToFirst();
                RssFeed androidCentralRSSFeed = feedFromCursor(androidCentralCursor);
                androidCentralCursor.close();
                mItems.addAll(newRssItems);
                mFeeds.add(androidCentralRSSFeed);

                BloclyApplication.getSharedInstance().sendBroadcast(new Intent(ACTION_DOWNLOAD_COMPLETED));


            }
        }).start();
    }


    static RssFeed feedFromCursor(Cursor cursor) {
        return new RssFeed(RssFeedTable.getRowId(cursor), RssFeedTable.getTitle(cursor), RssFeedTable.getDescription(cursor),
                RssFeedTable.getSiteURL(cursor), RssFeedTable.getFeedURL(cursor));
    }

    static RssItem itemFromCursor(Cursor cursor) {
        return new RssItem(RssFeedTable.getRowId(cursor),RssItemTable.getGUID(cursor), RssItemTable.getTitle(cursor),
                RssItemTable.getDescription(cursor), RssItemTable.getLink(cursor),
                RssItemTable.getEnclosure(cursor), RssItemTable.getRssFeedId(cursor),
                RssItemTable.getPubDate(cursor), RssItemTable.getFavorite(cursor),
                RssItemTable.getArchived(cursor));
    }

    void submitTask(Runnable task) {

        if(mExecutorService.isShutdown() || mExecutorService.isTerminated()) {
            mExecutorService = Executors.newSingleThreadExecutor();
        }
        mExecutorService.submit(task);

    }


}
