package com.github.ksoichiro.android.mdslide;

import java.io.Serializable;

public class Content implements Serializable {

    public ContentType contentType;
    public String content;

    public Content() {
        contentType = ContentType.P;
        content = "";
    }
}
