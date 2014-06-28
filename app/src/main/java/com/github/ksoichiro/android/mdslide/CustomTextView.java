package com.github.ksoichiro.android.mdslide;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.TypedArray;
import android.graphics.Typeface;
import android.preference.PreferenceManager;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.Log;
import android.widget.TextView;

import java.util.HashMap;
import java.util.Map;

public class CustomTextView extends TextView {
    public static Map<String, Typeface> typeFaces;
    public static String overrideFont;
    public static String overrideFontForCodes;

    static {
        typeFaces = new HashMap<String, Typeface>();
    }

    public CustomTextView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(attrs, defStyle);
    }

    public CustomTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(attrs, 0);

    }

    public CustomTextView(Context context) {
        super(context);
        init(null, 0);
    }

    private void init(AttributeSet attrs, int defStyle) {
        String attrFontName = null;
        boolean isSourceCode = false;
        if (attrs != null) {
            TypedArray a = getContext().obtainStyledAttributes(attrs, R.styleable.CustomTextView, defStyle, 0);
            attrFontName = a.getString(R.styleable.CustomTextView_fontName);
            isSourceCode = a.getBoolean(R.styleable.CustomTextView_sourceCode, false);
            a.recycle();
        }

        String fontName = null;
        if (!isSourceCode && !TextUtils.isEmpty(overrideFont)) {
            fontName = overrideFont;
        } else if (isSourceCode && !TextUtils.isEmpty(overrideFontForCodes)) {
            fontName = overrideFontForCodes;
        } else if (!TextUtils.isEmpty(attrFontName)) {
            fontName = attrFontName;
        }

        if (!TextUtils.isEmpty(fontName)) {
            if (typeFaces.containsKey(fontName)) {
                setTypeface(typeFaces.get(fontName));
            } else {
                Typeface myTypeface = Typeface.createFromAsset(getContext().getAssets(), "fonts/" + fontName);
                setTypeface(myTypeface);
                typeFaces.put(fontName, myTypeface);
            }
        }
    }

}
