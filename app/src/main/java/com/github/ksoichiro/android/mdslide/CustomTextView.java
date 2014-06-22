package com.github.ksoichiro.android.mdslide;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.widget.TextView;

import java.util.HashMap;
import java.util.Map;

public class CustomTextView extends TextView {
    public static Map<String, Typeface> typeFaces;

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
        if (attrs != null) {
            TypedArray a = getContext().obtainStyledAttributes(attrs, R.styleable.CustomTextView, defStyle, 0);
            String fontName = a.getString(R.styleable.CustomTextView_fontName);
            if (fontName != null) {
                if (typeFaces.containsKey(fontName)) {
                    setTypeface(typeFaces.get(fontName));
                } else {
                    Typeface myTypeface = Typeface.createFromAsset(getContext().getAssets(), "fonts/" + fontName);
                    setTypeface(myTypeface);
                    typeFaces.put(fontName, myTypeface);
                }
            }
            a.recycle();
        }
    }

}
