package com.github.ksoichiro.android.mdslide;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

public class Content implements Serializable {

    public ContentType contentType;
    public String content;
    public Map<String, Object> attributes;

    public Content() {
        contentType = ContentType.P;
        content = "";
        attributes = new HashMap<String, Object>();
    }
}
