package com.github.ksoichiro.android.mdslide;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class Page implements Serializable {

    public List<Content> contents;
    public Page() {
        contents = new ArrayList<Content>();
    }

    public boolean hasImage() {
        if (contents == null) {
            return false;
        }
        boolean contains = false;
        for (Content c : contents) {
            if (c.contentType == ContentType.IMG) {
                contains = true;
                break;
            }
        }
        return contains;
    }
}
