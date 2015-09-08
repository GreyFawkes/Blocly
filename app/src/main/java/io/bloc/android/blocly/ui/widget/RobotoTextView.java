package io.bloc.android.blocly.ui.widget;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.util.Log;
import android.widget.TextView;

import java.util.HashMap;
import java.util.Map;

import io.bloc.android.blocly.R;

/**
 * Created by Administrator on 9/3/2015.
 */
public class RobotoTextView extends TextView{

    private static Map<String, Typeface> sTypefaces = new HashMap<String, Typeface>();
    private static final String TAG = "RobotoTextView";

    public RobotoTextView(Context context) {
        super(context);
    }

    public RobotoTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
        extractFont(attrs);
    }

    public RobotoTextView(Context context, AttributeSet attrs, int defStyleAttr){
        super(context, attrs, defStyleAttr);
        extractFont(attrs);
    }

    void extractFont(AttributeSet attrs) {

        if(isInEditMode()) {
            return;
        }
        if(attrs == null) {
            return;
        }

        TypedArray typedArray = getContext().getTheme().obtainStyledAttributes(
                attrs, R.styleable.Roboto, 0, 0);


        int robotoFontIndex = typedArray.getInteger(R.styleable.Roboto_robotoFont, -1);
        boolean robotoCondensedBoolean = typedArray.getBoolean(R.styleable.Roboto_condensed, false);
        int robotoFontStyleValue = typedArray.getInteger(R.styleable.Roboto_robotoStyle, -1);


        typedArray.recycle();

        String[] stringArray = getResources().getStringArray(R.array.roboto_font_file_names);
        if(robotoFontIndex < -1 || robotoFontIndex >= stringArray.length) {
            return;
        }

        String robotoFont;
        Typeface robotoTypeface = null;

         Log.i(TAG, "font index value : " + String.valueOf(robotoFontIndex));

        if(robotoFontIndex != -1) {
            robotoFont = stringArray[robotoFontIndex];
        } else if (robotoFontIndex == -1 && robotoFontStyleValue > 0){


            Log.i(TAG, "font style value : " + String.valueOf(robotoFontStyleValue));
            Log.i(TAG, "font condensed boolean : " + String.valueOf(robotoCondensedBoolean));

            //magic happens here to make the attribute work
            if(robotoCondensedBoolean) {

                if(robotoFontStyleValue < 8) {
                    robotoFont = stringArray[robotoFontStyleValue + 10];
                } else {
                    robotoFont = stringArray[stringArray.length-1];
                }
            } else {
                robotoFont = stringArray[robotoFontStyleValue];
            }


        } else {
            return;
        }

        if (sTypefaces.containsKey(robotoFont)) {
            robotoTypeface = sTypefaces.get(robotoFont);
        } else {
            robotoTypeface = Typeface.createFromAsset(getResources().getAssets(), "fonts/RobotoTTF/" + robotoFont);
            sTypefaces.put(robotoFont, robotoTypeface);
        }

        setTypeface(robotoTypeface);

    }

}
