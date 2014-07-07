package com.github.ksoichiro.android.mdslide.style;

import com.github.ksoichiro.android.mdslide.R;

public enum Theme {
    BLACK(R.style.AppTheme),
    WHITE(R.style.AppThemeWhite);

    private int mResId;
    private Theme(int resId) {
        mResId = resId;
    }
    public int getThemeResId() {
        return mResId;
    }
}
