package com.github.ksoichiro.android.mdslide.app;

import android.app.Application;
import android.graphics.drawable.Drawable;

import java.util.HashMap;
import java.util.Map;

public class App extends Application {
    public Map<String, Drawable> imageCache;

    @Override
    public void onCreate() {
        super.onCreate();
        imageCache = new HashMap<String, Drawable>();
    }

}
