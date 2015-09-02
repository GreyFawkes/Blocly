package io.bloc.android.blocly.ui.activity;

import android.app.Activity;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import io.bloc.android.blocly.BloclyApplication;
import io.bloc.android.blocly.R;

/**
 * Created by Administrator on 9/1/2015.
 */
public class BloclyActivity extends Activity{

    TextView hello;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_blocly);
        Toast.makeText(this,
                BloclyApplication.getSharedDataSource().getFeeds().get(0).getTitle(),
                Toast.LENGTH_LONG).show();

            //get the id of the textView then change the text to the same as the toast text.
        hello = (TextView) findViewById(R.id.hello_world);
        hello.setText(BloclyApplication.getSharedDataSource().getFeeds().get(0).getTitle());

    }
}
