package com.github.ksoichiro.android.mdslide;

import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.StrictMode;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class PageFragment extends Fragment {

    public static final String ARG_PAGE = "page";

    public static PageFragment newInstance(Page page) {
        Bundle args = new Bundle();
        args.putSerializable(ARG_PAGE, page);
        PageFragment fragment = new PageFragment();
        fragment.setArguments(args);
        StrictMode.setThreadPolicy(new StrictMode.ThreadPolicy.Builder().detectAll().penaltyLog().build());
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

        RelativeLayout root = (RelativeLayout) view.findViewById(R.id.root);
        root.removeAllViews();
        TextView pn = (TextView) inflater.inflate(R.layout.page_number, null);
        pn.setText(String.valueOf(p.number));
        RelativeLayout.LayoutParams pnParams = new RelativeLayout.LayoutParams(
                RelativeLayout.LayoutParams.WRAP_CONTENT,
                RelativeLayout.LayoutParams.WRAP_CONTENT);
        pnParams.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
        pnParams.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
        root.addView(pn, pnParams);

        View parent;
        RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(
                RelativeLayout.LayoutParams.MATCH_PARENT,
                RelativeLayout.LayoutParams.MATCH_PARENT);
        if (p.contents.size() == 1) {
            parent = inflater.inflate(R.layout.parent_title, null);
            params.width = RelativeLayout.LayoutParams.WRAP_CONTENT;
        } else if (p.hasImage()) {
            parent = inflater.inflate(R.layout.parent_2row, null);
        } else {
            parent = inflater.inflate(R.layout.parent_default, null);
        }
        root.addView(parent, params);
        LinearLayout parentContent = (LinearLayout) parent.findViewById(R.id.parent);
        if (p.contents.size() == 1) {
            parentContent.setGravity(Gravity.CENTER);
        }
        LinearLayout parentContentRight = (LinearLayout) parent.findViewById(R.id.parent_right);

        for (Content content : p.contents) {
            int resId = 0;
            switch (content.contentType) {
                case H1:
                    resId = R.layout.parts_h1;
                    break;
                case H2:
                    resId = R.layout.parts_h2;
                    break;
                case H3:
                    resId = R.layout.parts_h3;
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
                case CODE:
                    resId = R.layout.parts_code;
                    break;
                case IMG:
                    resId = R.layout.parts_img;
                    break;
                case P:
                default:
                    resId = R.layout.parts_p;
                    break;
            }
            View layout = inflater.inflate(resId, null);
            TextView tv = (TextView) layout.findViewById(R.id.text);
            if (tv != null) {
                tv.setText(stripLink(content.content));
                parentContent.addView(layout);
            } else {
                ImageView img = (ImageView) layout.findViewById(R.id.img);
                if (img != null) {
                    setImage(img, content);
                    LinearLayout.LayoutParams params2 = new LinearLayout.LayoutParams(
                            LinearLayout.LayoutParams.MATCH_PARENT,
                            LinearLayout.LayoutParams.MATCH_PARENT);
                    parentContentRight.addView(layout, params2);
                }
            }
        }

        return root;
    }

    // Link is useless for viewing, so strip them.
    private String stripLink(final String s) {
        if (TextUtils.isEmpty(s)) {
            return s;
        }
        String result = s;
        Pattern p = Pattern.compile("^(.*)\\[([^\\]]*)\\]\\(([^\\)]*)\\)(.*)$");
        Matcher m = p.matcher(s);
        if (m.find()) {
            result = m.group(1) + m.group(2) + m.group(4);
        }
        return result;
    }

    private void setImage(ImageView img, Content content) {
        if (img == null) {
            return;
        }

        String strUrl = (String) content.attributes.get("url");
        Log.v("", "Loading image: " + strUrl);
        LoadImageTask task = new LoadImageTask();
        task.setImageView(img);
        task.execute(strUrl);
    }

    public class LoadImageTask extends AsyncTask<String, Void, Drawable> {
        private ImageView mImg;

        public void setImageView(final ImageView img) {
            mImg = img;
        }

        @Override
        protected Drawable doInBackground(String... strings) {
            String strUrl = strings[0];
            try {
                App app = (App) getActivity().getApplication();
                if (app.imageCache.containsKey(strUrl)) {
                    return app.imageCache.get(strUrl);
                }
                URL url = new URL(strUrl);
                InputStream in = url.openStream();
                Drawable d = Drawable.createFromStream(in, "image");
                app.imageCache.put(strUrl, d);
                return d;
            } catch (IOException e) {
                Log.w("", "Loading image failed", e);
            }
            return null;
        }

        @Override
        protected void onPostExecute(Drawable drawable) {
            super.onPostExecute(drawable);
            mImg.setImageDrawable(drawable);
        }
    }
}
