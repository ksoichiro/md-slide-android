package com.github.ksoichiro.android.mdslide.app;

import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.TextUtils;
import android.text.style.StyleSpan;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.github.ksoichiro.android.mdslide.slide.Content;
import com.github.ksoichiro.android.mdslide.slide.ContentText;
import com.github.ksoichiro.android.mdslide.widget.CustomFontSpan;
import com.github.ksoichiro.android.mdslide.widget.CustomTextView;
import com.github.ksoichiro.android.mdslide.slide.Page;
import com.github.ksoichiro.android.mdslide.R;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class PageFragment extends Fragment {

    public static final String ARG_PAGE = "page";
    public static String buretteString = "";

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

        RelativeLayout root = (RelativeLayout) view.findViewById(R.id.root);
        root.removeAllViews();
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getActivity());
        if (prefs.getBoolean(SettingsActivity.PREF_SHOW_PAGE_NUMBER, false)) {
            TextView pn = (TextView) inflater.inflate(R.layout.page_number, null);
            pn.setText(String.valueOf(p.number));
            RelativeLayout.LayoutParams pnParams = new RelativeLayout.LayoutParams(
                    RelativeLayout.LayoutParams.WRAP_CONTENT,
                    RelativeLayout.LayoutParams.WRAP_CONTENT);
            pnParams.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
            pnParams.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
            root.addView(pn, pnParams);
        }

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
            boolean hasBurette = false;
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
                    hasBurette = true;
                    break;
                case LI2:
                    resId = R.layout.parts_li2;
                    hasBurette = true;
                    break;
                case LI3:
                    resId = R.layout.parts_li3;
                    hasBurette = true;
                    break;
                case CODE:
                    resId = R.layout.parts_code;
                    break;
                case QUOTE:
                    resId = R.layout.parts_quote;
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
            if (hasBurette) {
                TextView buretteView = (TextView) layout.findViewById(R.id.burette);
                if (TextUtils.isEmpty(buretteString)) {
                    buretteString = PreferenceManager.getDefaultSharedPreferences(
                            getActivity()).getString(SettingsActivity.PREF_BURETTE, getString(R.string.burette_point));
                }
                buretteView.setText(buretteString);
            }
            TextView tv = (TextView) layout.findViewById(R.id.text);
            if (tv != null) {
                if (0 < content.contentTexts.size()) {
                    SpannableStringBuilder sb = new SpannableStringBuilder();
                    int start;
                    for (ContentText contentText : content.contentTexts) {
                        switch (contentText.type) {
                            case CODE:
                                String fontName = "source-code-pro/SourceCodePro-Regular.otf";
                                if (!TextUtils.isEmpty(CustomTextView.overrideFontForCodes)) {
                                    fontName = CustomTextView.overrideFontForCodes;
                                }
                                Typeface tf = Typeface.createFromAsset(getActivity().getAssets(), "fonts/" + fontName);
                                CustomFontSpan span = new CustomFontSpan(tf);
                                start = sb.length();
                                sb.append(stripLink(contentText.text));
                                sb.setSpan(span, start, sb.length(),
                                        Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                                break;
                            case EMPHASIS:
                            case EMPHASIS2:
                                StyleSpan emSpan = new StyleSpan(Typeface.ITALIC);
                                start = sb.length();
                                sb.append(stripLink(contentText.text));
                                sb.setSpan(emSpan, start, sb.length(),
                                        Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                                break;
                            case STRONG:
                            case STRONG2:
                                StyleSpan strongSpan = new StyleSpan(Typeface.BOLD);
                                start = sb.length();
                                sb.append(stripLink(contentText.text));
                                sb.setSpan(strongSpan, start, sb.length(),
                                        Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                                break;
                            case EMPHASIS_STRONG:
                            case STRONG_EMPHASIS:
                            case EMPHASIS2_STRONG2:
                                StyleSpan strongEmSpan = new StyleSpan(Typeface.BOLD_ITALIC);
                                start = sb.length();
                                sb.append(stripLink(contentText.text));
                                sb.setSpan(strongEmSpan, start, sb.length(),
                                        Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                                break;
                            case TEXT:
                            default:
                                sb.append(stripLink(contentText.text));
                        }
                    }
                    tv.setText(sb);
                    if (p.hasImageOnLeft()) {
                        parentContentRight.addView(layout);
                    } else {
                        parentContent.addView(layout);
                    }
                }
            } else {
                ImageView img = (ImageView) layout.findViewById(R.id.img);
                if (img != null) {
                    setImage(img, content);
                    LinearLayout.LayoutParams params2 = new LinearLayout.LayoutParams(
                            LinearLayout.LayoutParams.MATCH_PARENT,
                            LinearLayout.LayoutParams.MATCH_PARENT);
                    if (p.hasImageOnLeft()) {
                        parentContent.addView(layout, params2);
                    } else {
                        parentContentRight.addView(layout, params2);
                    }
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
