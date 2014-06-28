package com.github.ksoichiro.android.mdslide;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Typeface;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.widget.TextView;

import java.util.HashMap;
import java.util.Map;

public class CustomTextView extends TextView {
    public static Map<String, Typeface> typeFaces;
    public static String overrideFont;
    public static String overrideFontForCodes;
    public static String overrideFontForQuotes;

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
        FontType fontType = FontType.NORMAL;
        if (attrs != null) {
            TypedArray a = getContext().obtainStyledAttributes(attrs, R.styleable.CustomTextView, defStyle, 0);
            attrFontName = a.getString(R.styleable.CustomTextView_fontName);
            fontType = FontType.fontTypeFromCode(
                    a.getInt(R.styleable.CustomTextView_fontType, FontType.NORMAL.getCode()));
            a.recycle();
        }

        String fontName = null;
        if (fontType == FontType.NORMAL && !TextUtils.isEmpty(overrideFont)) {
            fontName = overrideFont;
        } else if (fontType == FontType.CODE && !TextUtils.isEmpty(overrideFontForCodes)) {
            fontName = overrideFontForCodes;
        } else if (fontType == FontType.QUOTE && !TextUtils.isEmpty(overrideFontForQuotes)) {
            fontName = overrideFontForQuotes;
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
