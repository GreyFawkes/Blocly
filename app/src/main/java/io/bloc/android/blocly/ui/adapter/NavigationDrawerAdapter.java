package io.bloc.android.blocly.ui.adapter;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import io.bloc.android.blocly.BloclyApplication;
import io.bloc.android.blocly.R;
import io.bloc.android.blocly.api.model.RssFeed;

/**
 * Created by Administrator on 9/13/2015.
 */
public class NavigationDrawerAdapter extends RecyclerView.Adapter<NavigationDrawerAdapter.ViewHolder> {

    public enum NavigationOptions {
        NAVIGATION_OPTIONS_INBOX,
        NAVIGATION_OPTIONS_FAVORITES,
        NAVIGATION_OPTIONS_ARCHIVED
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup viewGroup, int position) {
        View inflate = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.rss_drawer_item, viewGroup, false);
        return new ViewHolder(inflate);
    }

    @Override
    public void onBindViewHolder(ViewHolder viewHolder, int position) {
        RssFeed rssFeed = null;

        if(position >= NavigationOptions.values().length) {
            int feedPosition = position - NavigationOptions.values().length;
            rssFeed = BloclyApplication.getSharedDataSource().getFeeds().get(feedPosition);
        }
        viewHolder.update(position, rssFeed);
    }

    @Override
    public int getItemCount() {

        return NavigationOptions.values().length
                + BloclyApplication.getSharedDataSource().getFeeds().size();
    }

    class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        View topPadding;
        TextView title;
        View bottomPadding;
        View divider;

        public ViewHolder(View itemView) {
            super(itemView);
            topPadding = itemView.findViewById(R.id.v_nav_item_top_padding);
            title = (TextView) itemView.findViewById(R.id.tv_nav_item_title);
            bottomPadding = itemView.findViewById(R.id.v_nav_item_bottom_padding);
            divider = itemView.findViewById(R.id.v_nav_item_divider);
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            Toast.makeText(v.getContext(), "Nothing... Yet!", Toast.LENGTH_SHORT).show();
        }

        void update(int position, RssFeed rssFeed) {

            boolean shouldShowTopPadding = position == NavigationOptions.NAVIGATION_OPTIONS_INBOX.ordinal()
                    || position == NavigationOptions.values().length;
            topPadding.setVisibility(shouldShowTopPadding ? View.VISIBLE : View.GONE);

            boolean shouldShowBottomPadding = position == NavigationOptions.NAVIGATION_OPTIONS_ARCHIVED.ordinal()
                    || position == getItemCount() - 1;
            bottomPadding.setVisibility(shouldShowBottomPadding ? View.VISIBLE : View.GONE);

            divider.setVisibility(position == NavigationOptions.NAVIGATION_OPTIONS_ARCHIVED.ordinal()
                ? View.VISIBLE : View.GONE);

            if(position < NavigationOptions.values().length) {
                int[] titleTexts = new int[] {
                        R.string.navigation_option_inbox,
                        R.string.navigation_option_favorites,
                        R.string.navigation_option_archived};
                title.setText(titleTexts[position]);
            } else {
                title.setText(rssFeed.getTitle());
            }

        }
    }

}
