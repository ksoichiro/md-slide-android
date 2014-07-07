package com.github.ksoichiro.android.mdslide;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import com.github.ksoichiro.android.mdslide.app.PageFragment;
import com.github.ksoichiro.android.mdslide.slide.Page;

import java.util.ArrayList;
import java.util.List;

public class PageAdapter extends FragmentPagerAdapter {

    private List<Page> mPages;

    public PageAdapter(final FragmentManager fm) {
        super(fm);
        mPages = new ArrayList<Page>();
    }

    public void setPages(List<Page> pages) {
        mPages = pages;
    }

    public Fragment getItem(final int position) {
        if (position < 0 || mPages.size() <= position) {
            return null;
        }

        return PageFragment.newInstance(mPages.get(position));
    }

    @Override
    public int getCount() {
        return mPages.size();
    }
}
