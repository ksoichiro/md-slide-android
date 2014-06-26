package com.github.ksoichiro.android.mdslide.widget.transition;

import android.support.v4.view.ViewPager;
import android.view.View;

public class PushPageTransformer implements ViewPager.PageTransformer {

    private static final float MIN_SCALE = 0.75f;

    @Override
    public void transformPage(View page, float position) {
        int pageWidth = page.getWidth();
        if (position <= -1) {
            page.setAlpha(0);
        } else if (-1 < position && position < 0) {
            page.setAlpha(position + 1);

            float scaleFactor = MIN_SCALE
                    + (1 - MIN_SCALE) * (1 - Math.abs(position));
            page.setScaleX(scaleFactor);
            page.setScaleY(scaleFactor);

            page.setTranslationX(pageWidth * -position);
        }
    }

}
