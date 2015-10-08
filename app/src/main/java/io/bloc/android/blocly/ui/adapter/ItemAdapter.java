package io.bloc.android.blocly.ui.adapter;

import android.animation.ValueAnimator;
import android.graphics.Bitmap;
import android.graphics.Outline;
import android.os.Build;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewOutlineProvider;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.FailReason;
import com.nostra13.universalimageloader.core.listener.ImageLoadingListener;

import java.lang.ref.WeakReference;
import java.util.HashMap;
import java.util.Map;

import io.bloc.android.blocly.R;
import io.bloc.android.blocly.api.UIUtils;
import io.bloc.android.blocly.api.model.RssFeed;
import io.bloc.android.blocly.api.model.RssItem;

/**
 * Created by Administrator on 9/2/2015.
 */
public class ItemAdapter extends RecyclerView.Adapter<ItemAdapter.ItemAdapterViewHolder>{

    public interface DataSource {

        RssItem getRssItem(ItemAdapter itemAdapter, int position);
        RssFeed getRssFeed(ItemAdapter itemAdapter, int position);
        int getItemCount(ItemAdapter itemAdapter);
    }
    public interface Delegate {

        void onItemClicked(ItemAdapter itemAdapter, RssItem rssItem);
        void onVisitClicked(ItemAdapter itemAdapter, RssItem rssItem);
    }

    private static final String TAG = ItemAdapter.class.getSimpleName();

    private Map<Long, Integer> rssFeedToColor = new HashMap<Long, Integer>();

    private RssItem mExpandedItem = null;
    private WeakReference<Delegate> delegate;
    private WeakReference<DataSource> dataSource;

    private int collapsedHeight;
    private int expandedHeight;

    public int getCollapsedHeight(){
        return collapsedHeight;
    }

    private void setCollapsedHeight(int height) {
        collapsedHeight = height;
    }

    public int getExpandedHeight() {
        return expandedHeight;
    }

    private void setExpandedHeight(int height) {
        expandedHeight = height;
    }

    @Override
    public ItemAdapterViewHolder onCreateViewHolder(ViewGroup viewGroup, int index){
        View inflate = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.rss_item, viewGroup, false);
        return new ItemAdapterViewHolder(inflate);
    }

    @Override
    public void onBindViewHolder(ItemAdapterViewHolder itemAdapterViewHolder, int index) {
//        io.bloc.android.blocly.api.DataSource sharedDataSource = BloclyApplication.getSharedDataSource();
//        itemAdapterViewHolder.update(sharedDataSource.getFeeds().get(0), sharedDataSource.getItems().get(index));
        if(getDataSource() == null) {
            return;
        }

        RssItem rssItem = getDataSource().getRssItem(this, index);
        RssFeed rssFeed = getDataSource().getRssFeed(this, index);
        itemAdapterViewHolder.update(rssFeed, rssItem);
    }

    @Override
    public int getItemCount() {
        //return BloclyApplication.getSharedDataSource().getItems().size();
        if(getDataSource() == null) {
            return 0;
        }
        return getDataSource().getItemCount(this);
    }

    public DataSource getDataSource() {
        if(dataSource == null) {
            return null;
        }
        return dataSource.get();
    }

    public void setDataSource(DataSource dataSource) {
        this.dataSource = new WeakReference<DataSource>(dataSource);
    }

    public Delegate getDelegate() {
        if(delegate == null) {
            return null;
        }
        return delegate.get();
    }

    public void setDelegate(Delegate delegate) {
        this.delegate = new WeakReference<Delegate>(delegate);
    }

    public RssItem getExpandedItem() {
        return mExpandedItem;
    }

    public void setExpandedItem(RssItem expandedItem) {
        mExpandedItem = expandedItem;
    }

    class ItemAdapterViewHolder extends RecyclerView.ViewHolder implements ImageLoadingListener, View.OnClickListener, CompoundButton.OnCheckedChangeListener {

        boolean onTablet;
        boolean contentExpanded;

        TextView title;
        TextView content;

        TextView feed;
        View headerWrapper;
        ImageView headerImage;
        CheckBox archiveCheckbox;
        CheckBox favoriteCheckbox;

        View expandedContentWrapper;
        TextView expandedContent;
        TextView visitSite;

        TextView callout;


        RssItem rssItem;

        public ItemAdapterViewHolder(View itemView) {
            super(itemView);

            title = (TextView) itemView.findViewById(R.id.tv_rss_item_title);

            content = (TextView) itemView.findViewById(R.id.tv_rss_item_content);

            if(itemView.findViewById(R.id.tv_rss_feed_title) != null) {
                feed = (TextView) itemView.findViewById(R.id.tv_rss_feed_title);
                headerWrapper = itemView.findViewById(R.id.fl_rss_item_image_header);
                headerImage = (ImageView) itemView.findViewById(R.id.iv_rss_item_image);
                archiveCheckbox = (CheckBox) itemView.findViewById(R.id.cb_rss_item_check_mark);
                favoriteCheckbox = (CheckBox) itemView.findViewById(R.id.cb_rss_item_favorite_star);
                expandedContentWrapper = itemView.findViewById(R.id.ll_rss_item_expanded_content_wrapper);
                expandedContent = (TextView) itemView.findViewById(R.id.tv_rss_item_content_full);
                visitSite = (TextView) itemView.findViewById(R.id.tv_rss_item_visit_site);

                visitSite.setOnClickListener(this);
                archiveCheckbox.setOnCheckedChangeListener(this);
                favoriteCheckbox.setOnCheckedChangeListener(this);
            } else {
                onTablet = true;
                callout = (TextView) itemView.findViewById(R.id.tv_rss_item_callout);

                if(Build.VERSION.SDK_INT >= 21) {
                    callout.setOutlineProvider(new ViewOutlineProvider() {
                        @Override
                        public void getOutline(View view, Outline outline) {
                            outline.setOval(0, 0, view.getWidth(), view.getHeight());
                        }
                    });
                    callout.setClipToOutline(true);
                }
            }
            itemView.setOnClickListener(this);
        }

        void update(RssFeed rssFeed, RssItem rssItem) {
            this.rssItem = rssItem;

            title.setText(rssItem.getTitle());
            content.setText(rssItem.getDescription());
            if(onTablet) {
                callout.setText("" + Character.toUpperCase(rssFeed.getTitle().charAt(0)));
                Integer color = rssFeedToColor.get(rssFeed.getRowId());
                if(color == null) {
                    color = UIUtils.generatedRandomColor(itemView.getResources().getColor(android.R.color.white));
                    rssFeedToColor.put(rssFeed.getRowId(), color);
                }
                callout.setBackgroundColor(color);
                return;
            }
            feed.setText(rssFeed.getTitle());
            expandedContent.setText(rssItem.getDescription());
            if(rssItem.getImageUrl() != null) {

                headerWrapper.setVisibility(View.VISIBLE);
                headerImage.setVisibility(View.INVISIBLE);

                ImageLoader.getInstance().loadImage(rssItem.getImageUrl(), this);
            } else {
                headerWrapper.setVisibility(View.GONE);
            }

            animateContent(getExpandedItem() == rssItem);
        }

        @Override
        public void onLoadingStarted(String imageUri, View view) {

        }

        @Override
        public void onLoadingFailed(String imageUri, View view, FailReason failReason) {
            Log.e(TAG, "onLoadingFailed: " + failReason.toString() + " for URL: " + imageUri);
        }

        @Override
        public void onLoadingComplete(String imageUri, View view, Bitmap loadedImage) {

            if(imageUri.equals(rssItem.getImageUrl())){
                headerImage.setImageBitmap(loadedImage);
                headerImage.setVisibility(View.VISIBLE);
            }
        }

        @Override
        public void onLoadingCancelled(String imageUri, View view) {

            ImageLoader.getInstance().loadImage(imageUri, this);

        }

        @Override
        public void onClick(View v) {
            Log.i(TAG, "message");
           if(v == itemView) {
//               contentExpanded = !contentExpanded;
//               expandedContentWrapper.setVisibility(contentExpanded ? View.VISIBLE : View.GONE);
//               content.setVisibility(contentExpanded ? View.GONE : View.VISIBLE);
//               animateContent(!contentExpanded);
               if(getDelegate() != null) {
                   getDelegate().onItemClicked(ItemAdapter.this, rssItem);
               }
           } else {
              // Toast.makeText(v.getContext(), "Visit " + rssItem.getUrl(), Toast.LENGTH_SHORT).show();
               if(getDelegate() != null) {
                   getDelegate().onVisitClicked(ItemAdapter.this, rssItem);
               }
           }
        }

        @Override
        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
            Log.v(TAG, "Checked changed to: " + isChecked);
        }

        private void animateContent(final boolean expand) {

            if((expand && contentExpanded) || (!expand && !contentExpanded)) {
                return;
            }

            int startingHeight = expandedContentWrapper.getMeasuredHeight();
            int finalHeight = content.getMeasuredHeight();

            if(expand){
                setCollapsedHeight(itemView.getHeight());
                startingHeight = finalHeight;
                expandedContentWrapper.setAlpha(0f);
                expandedContentWrapper.setVisibility(View.VISIBLE);

                expandedContentWrapper.measure(
                        View.MeasureSpec.makeMeasureSpec(content.getWidth(), View.MeasureSpec.EXACTLY),
                        ViewGroup.LayoutParams.WRAP_CONTENT
                );

                finalHeight = expandedContentWrapper.getMeasuredHeight();

            } else {
                content.setVisibility(View.VISIBLE);
            }

            startAnimator(startingHeight, finalHeight, new ValueAnimator.AnimatorUpdateListener() {

                @Override
                public void onAnimationUpdate(ValueAnimator animation) {

                    float animatedFraction = animation.getAnimatedFraction();
                    float wrapperAlpha = expand ? animatedFraction : 1f - animatedFraction;
                    float contentAlpha = 1f - wrapperAlpha;

                    expandedContentWrapper.setAlpha(wrapperAlpha);
                    content.setAlpha(contentAlpha);

                    expandedContentWrapper.getLayoutParams().height = animatedFraction == 1f ?
                            ViewGroup.LayoutParams.WRAP_CONTENT :
                            (Integer) animation.getAnimatedValue();

                    expandedContentWrapper.requestLayout();
                    if (animatedFraction == 1f) {
                        if(expand) {
                            content.setVisibility(View.GONE);
                            setExpandedHeight(itemView.getHeight());
                        } else {
                            expandedContentWrapper.setVisibility(View.GONE);
                        }
                    }

                }
            });

            contentExpanded = expand;

        }

        private void startAnimator(int start, int end, ValueAnimator.AnimatorUpdateListener animatorUpdateListener) {
            ValueAnimator valueAnimator = ValueAnimator.ofInt(start, end);
            valueAnimator.addUpdateListener(animatorUpdateListener);
            valueAnimator.setDuration(itemView.getResources().getInteger(android.R.integer.config_mediumAnimTime));
            valueAnimator.setInterpolator(new AccelerateDecelerateInterpolator());
            valueAnimator.start();
        }


    }


}
