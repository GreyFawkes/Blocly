package io.bloc.android.blocly.api;

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
import io.bloc.android.blocly.api.network.NetworkRequest;

/**
 * Created by Administrator on 9/2/2015.
 */
public class DataSource {


    public interface Callback<Result> {
        void onSuccess(Result result);
        void onError(String errorMessage);
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
            SQLiteDatabase writableDatabase = mDatabaseOpenHelper.getWritableDatabase();
            new RssFeedTable.Builder()
                    .setTitle("AndroidCentral")
                    .setDescription("AndroidCentral - Android News, Tips, and stuff!")
                    .setSiteURL("http://www.androidcentral.com")
                    .setFeedURL("http://feeds.feedburner.com/androidcentral?format=xml")
                    .insert(writableDatabase);
            new RssFeedTable.Builder()
                    .setTitle("IGN")
                    .setDescription("IGN All")
                    .setSiteURL("http://www.ign.com")
                    .setFeedURL("http://feed.ign.com/ign/all?format=xml")
                    .insert(writableDatabase);
            new RssFeedTable.Builder()
                    .setTitle("Kotaku")
                    .setDescription("Game news, reviews, and awesomeness")
                    .setSiteURL("http://kotaku.com")
                    .setFeedURL("http://feed.gawker.com/kotaku/full#_ga=1.41426146.1734638996.1420673722")
                    .insert(writableDatabase);
        }
    }

    public void fetchRSSItemWithId(final long rowId, final Callback<RssItem> callback) {
        final Handler callbackHandlerThread = new Handler();
        submitTask(new Runnable() {
            @Override
            public void run() {
                Cursor cursor = mRssItemTable.fetchRow(mDatabaseOpenHelper.getReadableDatabase(), rowId);
                if(cursor.moveToFirst()) {
                    final RssItem rssItem = itemFromCursor(cursor);
                    cursor.close();
                    callbackHandlerThread.post(new Runnable() {
                        @Override
                        public void run() {
                            callback.onSuccess(rssItem);
                        }
                    });
                } else {
                    callbackHandlerThread.post(new Runnable() {
                        @Override
                        public void run() {
                            callback.onError("RSS item not found for row Id (" + rowId + ")");
                        }
                    });
                }


            }
        });
    }


    public void fetchFeedWithId(final long rowId, final Callback<RssFeed> callback) {
        final Handler callbackThreadHandler = new Handler();
        submitTask(new Runnable() {
            @Override
            public void run() {
                Cursor cursor = mRssFeedTable.fetchRow(mDatabaseOpenHelper.getReadableDatabase(), rowId);
                if (cursor.moveToFirst()) {
                    final RssFeed rssFeed = feedFromCursor(cursor);
                    cursor.close();
                    callbackThreadHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            callback.onSuccess(rssFeed);
                        }
                    });
                } else {
                    callbackThreadHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            callback.onError("Rss feed not found for row Id (" + rowId + ")");
                        }
                    });
                }
            }
        });
    }

    public void fetchAllFeeds(final Callback<List<RssFeed>> callback) {
        final Handler callbackThreadHandler = new Handler();
        submitTask(new Runnable() {
            @Override
            public void run() {
                final List<RssFeed> resultFeeds = new ArrayList<RssFeed>();
                Cursor cursor = mRssFeedTable.fetchAllFeeds(mDatabaseOpenHelper.getReadableDatabase());
                if (cursor.moveToFirst()) {
                    do {
                        resultFeeds.add(feedFromCursor(cursor));
                    } while (cursor.moveToNext());
                    cursor.close();
                }
                callbackThreadHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        callback.onSuccess(resultFeeds);
                    }
                });
            }
        });
    }

    public void fetchNewItemsForFeed(final RssFeed rssFeed, final Callback<List<RssItem>> callback) {
        final Handler callbackThreadHandler = new Handler();
        submitTask(new Runnable() {
            @Override
            public void run() {
                GetFeedsNetworkRequest getFeedsNetworkRequest = new GetFeedsNetworkRequest(rssFeed.getFeedUrl());
                final List<RssItem> newItems = new ArrayList<RssItem>();
                List<GetFeedsNetworkRequest.FeedResponse> feedResponses = getFeedsNetworkRequest.performRequest();
                if(checkForError(getFeedsNetworkRequest, callbackThreadHandler, callback)) {
                    return;
                }
                GetFeedsNetworkRequest.FeedResponse feedResponse = feedResponses.get(0);
                for (GetFeedsNetworkRequest.ItemResponse itemResponse : feedResponse.channelItems) {

                    if(RssItemTable.hasItem(mDatabaseOpenHelper.getReadableDatabase(), itemResponse.itemGUID)) {
                        continue;
                    }
                    long newItemRowId = insertResponseToDatabase(rssFeed.getRowId(), itemResponse);
                    Cursor newItemCursor = mRssItemTable.fetchRow(mDatabaseOpenHelper.getReadableDatabase(), newItemRowId);
                    newItemCursor.moveToFirst();
                    newItems.add(itemFromCursor(newItemCursor));
                    newItemCursor.close();
                }
                callbackThreadHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        callback.onSuccess(newItems);
                    }
                });
            }
        });
    }

    public void fetchNewFeed(final String feedUrl, final Callback<RssFeed> callback) {

        final Handler callbackThreadHandler = new Handler();
        submitTask(new Runnable() {
            @Override
            public void run() {

                Cursor existingFeedCursor = RssFeedTable.fecthFeedWithURL(mDatabaseOpenHelper.getReadableDatabase(), feedUrl);
                if (existingFeedCursor.moveToFirst()) {
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

               if(checkForError(getFeedsNetworkRequest, callbackThreadHandler, callback)) {
                   return;
               }

                GetFeedsNetworkRequest.FeedResponse newFeedResponse = feedResponses.get(0);
                long newFeedId = new RssFeedTable.Builder()
                        .setFeedURL(newFeedResponse.channelFeedURL)
                        .setSiteURL(newFeedResponse.channelURL)
                        .setTitle(newFeedResponse.channelTitle)
                        .setDescription(newFeedResponse.channelDescription)
                        .insert(mDatabaseOpenHelper.getWritableDatabase());

                for (GetFeedsNetworkRequest.ItemResponse itemResponse : newFeedResponse.channelItems) {
                    insertResponseToDatabase(newFeedId, itemResponse);

                }

                Cursor newFeedCursor = mRssFeedTable.fetchRow(mDatabaseOpenHelper.getReadableDatabase(), newFeedId);
                newFeedCursor.moveToFirst();
                final RssFeed fetchedFeed = feedFromCursor(newFeedCursor);
                newFeedCursor.close();
                callbackThreadHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        callback.onSuccess(fetchedFeed);
                    }
                });
            }
        });
    }

    public void fetchItemsForFeed(final RssFeed rssFeed, final Callback<List<RssItem>> callback) {
        final Handler callbackThreadHandler = new Handler();
        submitTask(new Runnable() {
            @Override
            public void run() {
                final List<RssItem> resultList = new ArrayList<RssItem>();
                Cursor cursor = RssItemTable.fetchItemsForFeed(
                        mDatabaseOpenHelper.getReadableDatabase(),
                        rssFeed.getRowId());

                if(cursor.moveToFirst()) {
                    do {
                        resultList.add(itemFromCursor(cursor));
                    } while (cursor.moveToNext());
                    cursor.close();
                }

                callbackThreadHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        callback.onSuccess(resultList);
                    }
                });
            }
        });
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

    boolean checkForError(GetFeedsNetworkRequest getFeedsNetworkRequest,
                          Handler callbackThreadHandler, final Callback<?> callback) {

        if(getFeedsNetworkRequest.getErrorCode() != 0) {
            final String errorMessage;
            if(getFeedsNetworkRequest.getErrorCode() == NetworkRequest.ERROR_IO) {
                errorMessage = "Network error";
            } else if (getFeedsNetworkRequest.getErrorCode() == NetworkRequest.ERROR_MALFORMED_URL) {
                errorMessage = "Malformed URL error";
            } else if (getFeedsNetworkRequest.getErrorCode() == getFeedsNetworkRequest.ERROR_PARSING) {
                errorMessage = "Error parsing feed";
            } else {
                errorMessage = "Error unknown";
            }
            callbackThreadHandler.post(new Runnable() {
                @Override
                public void run() {
                    callback.onError(errorMessage);
                }
            });
        }
        return getFeedsNetworkRequest.getErrorCode() != 0;
    }

    long insertResponseToDatabase(long feedId, GetFeedsNetworkRequest.ItemResponse itemResponse) {
        long itemPubDate = System.currentTimeMillis();
        DateFormat dateFormat = new SimpleDateFormat("EEE, dd MMM yyyy kk:mm:ss z", Locale.ENGLISH);
        try {
            itemPubDate = dateFormat.parse(itemResponse.itemPubDate).getTime();
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return new RssItemTable.Builder()
                .setTitle(itemResponse.itemTitle)
                .setDescription(itemResponse.itemDescription)
                .setEnclosure(itemResponse.itemEnclosureURL)
                .setMIMEType(itemResponse.itemEnclosureMIMEType)
                .setLink(itemResponse.itemURL)
                .setGUID(itemResponse.itemGUID)
                .setPubDate(itemPubDate)
                .setRssFeed(feedId)
                .insert(mDatabaseOpenHelper.getWritableDatabase());
    }


}
