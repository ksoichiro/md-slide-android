package com.github.ksoichiro.android.mdslide.widget.transition;

import android.support.v4.view.ViewPager;
import android.view.View;

public class FadePageTransformer implements ViewPager.PageTransformer {

    @Override
    public void transformPage(View page, float position) {
        float alpha = 0;
        int pageWidth = page.getWidth();
        if (-1 < position && position < 0) {
            alpha = position + 1;
        } else if (0 <= position && position <= 1) {
            alpha = 1 - position;
        }
        page.setTranslationX(pageWidth * -position);
        page.setAlpha(alpha);
    }

}
