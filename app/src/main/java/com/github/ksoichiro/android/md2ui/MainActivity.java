package com.github.ksoichiro.android.md2ui;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;


public class MainActivity extends Activity {

    private Uri mUri;
    private List<Page> mPages;
    private int mCurrentPage;

    public static class Page {
        public List<Content> contents;
        public Page() {
            contents = new ArrayList<Content>();
        }
    }
    public static class Content {
        public ContentType contentType;
        public String content;
    }
    public static enum ContentType {
        H1,
        H2,
        LI,
        LI2,
        LI3,
        P;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Intent intent = getIntent();
        mUri = intent.getData();
        if (mUri == null) {
            Toast.makeText(this, "Invalid URL", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
        parse();

        layoutPage(mCurrentPage);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        if (0 < mCurrentPage) {
            layoutPage(mCurrentPage - 1);
        } else {
            super.onBackPressed();
        }
    }

    private void parse() {
        mCurrentPage = 0;
        mPages = new ArrayList<Page>();

        List<String> lines = readFile(mUri);
        Log.i("", "url: " + mUri);
        Page page = new Page();
        for (String line : lines) {
            Log.i("", "line: " + line);
            if (line.startsWith("---")) {
                // New page
                mPages.add(page);
                page = new Page();
                continue;
            }
            Content content = new Content();
            content.content = line;
            if (line.startsWith("# ")) {
                // H1
                content.contentType = ContentType.H1;
                content.content = line.substring(2);
            } else if (line.startsWith("## ")) {
                // H2
                content.contentType = ContentType.H2;
                content.content = line.substring(3);
            } else if (line.startsWith("* ") || line.startsWith("- ")) {
                // ul/li
                content.contentType = ContentType.LI;
                content.content = line.substring(2);
            } else if (line.startsWith("    * ") || line.startsWith("    - ")) {
                // ul/li(2)
                content.contentType = ContentType.LI2;
                content.content = line.substring(6);
            } else if (line.startsWith("        * ") || line.startsWith("        - ")) {
                // ul/li(2)
                content.contentType = ContentType.LI3;
                content.content = line.substring(10);
            } else if (!TextUtils.isEmpty(line)) {
                // p
                content.contentType = ContentType.P;
            } else {
                // ignore
                continue;
            }
            page.contents.add(content);
        }
        mPages.add(page);
    }

    private void layoutPage(final int page) {
        if (page < 0 || mPages == null || mPages.size() <= page) {
            return;
        }

        LinearLayout root = (LinearLayout) findViewById(R.id.root);
        root.removeAllViews();

        root.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                layoutPage(page + 1);
            }
        });
        Page p = mPages.get(page);
        LayoutInflater inflater = LayoutInflater.from(this);
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
        LinearLayout parentContent = (LinearLayout) findViewById(R.id.parent);
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

        mCurrentPage = page;
    }

    private List<String> readFile(Uri uri) {
        final String charset = "UTF-8";
        List<String> lines = new ArrayList<String>();
        BufferedReader in = null;
        try {
            in = new BufferedReader(new InputStreamReader(
                    new FileInputStream(new File(uri.getPath())),
                    Charset.forName(charset)));
            String line;
            while ((line = in.readLine()) != null) {
                lines.add(line);
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (in != null) {
                try {
                    in.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return lines;
    }
}
