package io.bloc.android.blocly.api;

import java.util.ArrayList;
import java.util.List;

import io.bloc.android.blocly.BloclyApplication;
import io.bloc.android.blocly.R;
import io.bloc.android.blocly.api.model.RssFeed;
import io.bloc.android.blocly.api.model.RssItem;
import io.bloc.android.blocly.api.network.GetFeedsNetworkRequest;

/**
 * Created by Administrator on 9/2/2015.
 */
public class DataSource {

    private List<RssFeed> mFeeds;
    private List<RssItem> mItems;

    private List<GetFeedsNetworkRequest.FeedResponse> mList;

    public DataSource() {
        mFeeds = new ArrayList<RssFeed>();
        mItems = new ArrayList<RssItem>();
        //createFakeData();

       Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                mList = new GetFeedsNetworkRequest("http://feeds.feedburner.com/androidcentral?format=xml")
                        .performRequest();
                createData();
            }
        });
        thread.start();

    }



    public List<RssFeed> getFeeds() {
        return mFeeds;
    }

    public List<RssItem> getItems() {
        return mItems;
    }

       void createData() {
        for(int i = 0; i < mList.size(); i++) {

            GetFeedsNetworkRequest.FeedResponse feedResponse = mList.get(0);
            getFeeds().add(new RssFeed(feedResponse.channelTitle,
                    feedResponse.channelDescription,
                    feedResponse.channelURL,
                    feedResponse.channelFeedURL));

            List<GetFeedsNetworkRequest.ItemResponse> itemResponsesList = feedResponse.channelItems;

            for(int j = 0; j < itemResponsesList.size(); j++) {
                getItems().add(new RssItem( itemResponsesList.get(j).itemGUID,
                        itemResponsesList.get(j).itemTitle,
                        itemResponsesList.get(j).itemDescription,
                        itemResponsesList.get(j).itemURL,
                        itemResponsesList.get(j).itemEnclosureURL,
                        i,
                        Long.getLong(itemResponsesList.get(j).itemPubDate, System.currentTimeMillis()),
                        false, false));
            }
        }
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
