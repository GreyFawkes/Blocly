package io.bloc.android.blocly.api.model;

/**
 * Created by Administrator on 9/2/2015.
 */
public class RssFeed {
    private String title;
    private String decription;
    private String siteUrl;
    private String feedUrl;

    public RssFeed(String title, String decription, String siteUrl, String feedUrl) {
        this.title = title;
        this.decription = decription;
        this.siteUrl = siteUrl;
        this.feedUrl = feedUrl;
    }

    public String getTitle() {
        return title;
    }

    public String getDecription() {
        return decription;
    }

    public String getSiteUrl() {
        return siteUrl;
    }

    public String getFeedUrl() {
        return feedUrl;
    }
}
