package com.github.ksoichiro.android.mdslide.widget;

public enum FontType {
    NORMAL(0),
    CODE(1),
    QUOTE(2);

    private int mCode;

    private FontType(final int code) {
        mCode = code;
    }

    public int getCode() {
        return mCode;
    }

    public static FontType fontTypeFromCode(final int code) {
        for (FontType fontType : FontType.values()) {
            if (fontType.getCode() == code) {
                return fontType;
            }
        }
        return NORMAL;
    }
}
