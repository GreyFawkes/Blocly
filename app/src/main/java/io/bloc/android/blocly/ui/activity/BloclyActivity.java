package io.bloc.android.blocly.ui.activity;

import android.app.Activity;
import android.os.Bundle;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.widget.ImageView;

import com.nostra13.universalimageloader.cache.memory.impl.LruMemoryCache;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.nostra13.universalimageloader.core.assist.QueueProcessingType;

import io.bloc.android.blocly.R;
import io.bloc.android.blocly.ui.adapter.ItemAdapter;

/**
 * Created by Administrator on 9/1/2015.
 */
public class BloclyActivity extends Activity{

    private static final String TAG = "Blocly-Activity";

    private ItemAdapter mItemAdapter;
    private ImageView mBackground;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_blocly);

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

        ImageLoader imageLoader = ImageLoader.getInstance();


        mBackground = (ImageView) findViewById(R.id.iv_activity_background);

        //setting the image background for imageView, using an image in drawables

//        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
//            Drawable background = getResources().getDrawable(R.drawable.smiley);
//            mBackground.setBackground(background);
//        } else {
//            Bitmap background = BitmapFactory.decodeResource(getResources(), R.drawable.smiley);
//            mBackground.setImageBitmap(background);
//        }

        //setting the background for imageView using an image off the internet using ImageLoader


        //download image off the internet
        imageLoader.displayImage("http://www.drodd.com/images12/smiley-face-clip-art15.jpg", mBackground);

        mItemAdapter = new ItemAdapter();

        RecyclerView recyclerView = (RecyclerView) findViewById(R.id.rv_activity_blocly);

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.setAdapter(mItemAdapter);

        //throw an error log here for assignment, just to show i can
        Log.i(TAG, "Hi message!!!", new Throwable());
    }
}
