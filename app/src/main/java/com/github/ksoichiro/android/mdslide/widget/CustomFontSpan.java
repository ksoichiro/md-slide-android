package com.github.ksoichiro.android.mdslide.widget;

import android.graphics.Typeface;
import android.text.TextPaint;
import android.text.style.TypefaceSpan;

public class CustomFontSpan extends TypefaceSpan {
    private Typeface mTypeface;

    public CustomFontSpan(final Typeface typeface) {
        super("sans-serif");
        mTypeface = typeface;
    }

    @Override
    public void updateDrawState(TextPaint ds) {
        update(ds);
    }

    @Override
    public void updateMeasureState(TextPaint paint) {
        update(paint);
    }

    void update(TextPaint paint) {
        int oldStyle;
        Typeface old = paint.getTypeface();
        if (old == null) {
            oldStyle = 0;
        } else {
            oldStyle = old.getStyle();
        }

        int fake = oldStyle & ~mTypeface.getStyle();
        if ((fake & Typeface.BOLD) != 0) {
            paint.setFakeBoldText(true);
        }

        if ((fake & Typeface.ITALIC) != 0) {
            paint.setTextSkewX(-0.25f);
        }

        paint.setTypeface(mTypeface);
    }
}
