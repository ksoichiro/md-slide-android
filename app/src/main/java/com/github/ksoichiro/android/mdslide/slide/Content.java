package com.github.ksoichiro.android.mdslide.slide;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Content implements Serializable {

    public ContentType contentType;
    public List<ContentText> contentTexts;
    public Map<String, Object> attributes;

    public Content() {
        contentType = ContentType.P;
        attributes = new HashMap<String, Object>();
        initContentTexts();
    }

    public void initContentTexts() {
        contentTexts = new ArrayList<ContentText>();
    }

    public void addContentText(ContentText contentText) {
        contentTexts.add(contentText);
    }
}
