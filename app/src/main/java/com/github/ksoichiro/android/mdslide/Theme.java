package com.github.ksoichiro.android.mdslide;

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
