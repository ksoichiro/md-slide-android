package com.github.ksoichiro.android.mdslide.slide;

import com.github.ksoichiro.android.mdslide.slide.Content;
import com.github.ksoichiro.android.mdslide.slide.ContentType;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class Page implements Serializable {

    public int number;
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

    public boolean hasImageOnLeft() {
        if (contents == null) {
            return false;
        }
        boolean onLeft = false;
        for (Content c : contents) {
            if (c.contentType == ContentType.IMG) {
                onLeft = true;
                if (!c.attributes.containsKey("params")) {
                    continue;
                }
                for (String param : (String[]) c.attributes.get("params")) {
                    if ("right".equals(param)) {
                        onLeft = false;
                        break;
                    }
                }
                break;
            }
        }
        return onLeft;
    }
}
