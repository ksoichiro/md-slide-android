package com.github.ksoichiro.android.mdslide.slide;

import java.io.Serializable;

public class ContentText implements Serializable {
    public String text;
    public ContentTextType type;

    public ContentText(String text) {
        this.text = text;
        this.type = ContentTextType.TEXT;
    }

    public ContentText(String text, ContentTextType contentTextType) {
        this.text = text;
        this.type = contentTextType;
    }
}
