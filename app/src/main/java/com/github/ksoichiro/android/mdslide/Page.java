package com.github.ksoichiro.android.mdslide;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class Page implements Serializable {

    public List<Content> contents;
    public Page() {
        contents = new ArrayList<Content>();
    }

}
