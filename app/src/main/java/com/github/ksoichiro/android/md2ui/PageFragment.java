package com.github.ksoichiro.android.md2ui;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

public class PageFragment extends Fragment {

    public static final String ARG_PAGE = "page";

    public static PageFragment newInstance(Page page) {
        Bundle args = new Bundle();
        args.putSerializable(ARG_PAGE, page);
        PageFragment fragment = new PageFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(final LayoutInflater inflater, final ViewGroup container,
                             final Bundle savedInstanceState) {
        Bundle args = getArguments();
        Page page = (Page) args.getSerializable(ARG_PAGE);
        View v = layoutPage(inflater, page);
        return v;
    }

    private View layoutPage(LayoutInflater inflater, final Page p) {
        View view = inflater.inflate(R.layout.fragment_page, null);
        if (p == null) {
            return null;
        }

        LinearLayout root = (LinearLayout) view.findViewById(R.id.root);
        root.removeAllViews();

        View parent;
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.MATCH_PARENT);
        if (p.contents.size() == 1) {
            parent = inflater.inflate(R.layout.parent_title, null);
            params.width = LinearLayout.LayoutParams.WRAP_CONTENT;
        } else {
            parent = inflater.inflate(R.layout.parent_default, null);
        }
        root.addView(parent, params);
        LinearLayout parentContent = (LinearLayout) parent.findViewById(R.id.parent);
        if (p.contents.size() == 1) {
            parentContent.setGravity(Gravity.CENTER);
        }

        for (Content content : p.contents) {
            int resId = 0;
            switch (content.contentType) {
                case H1:
                    resId = R.layout.parts_h1;
                    break;
                case H2:
                    resId = R.layout.parts_h2;
                    break;
                case LI:
                    resId = R.layout.parts_li;
                    break;
                case LI2:
                    resId = R.layout.parts_li2;
                    break;
                case LI3:
                    resId = R.layout.parts_li3;
                    break;
                case P:
                    resId = R.layout.parts_p;
                    break;
            }
            View layout = inflater.inflate(resId, null);
            TextView tv = (TextView) layout.findViewById(R.id.text);
            tv.setText(content.content);
            parentContent.addView(layout);
        }

        return root;
    }

}
