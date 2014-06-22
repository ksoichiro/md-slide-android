package com.github.ksoichiro.android.md2ui;

import android.app.ActionBar;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.view.ViewPager;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;


public class MainActivity extends FragmentActivity {

    private Uri mUri;
    private List<Page> mPages;
    private ViewPager mPager;
    private boolean mFullscreen;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mFullscreen = false;

        Intent intent = getIntent();
        mUri = intent.getData();
        if (mUri == null) {
            Toast.makeText(this, "Invalid URL", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
        parse();

        mPager = (ViewPager) findViewById(R.id.pager);
        PageAdapter adapter = new PageAdapter(getSupportFragmentManager());
        adapter.setPages(mPages);
        mPager.setAdapter(adapter);
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
        } else if (id == R.id.action_fullscreen) {
            setFullscreen(true);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        if (mFullscreen) {
            setFullscreen(false);
        } else {
            super.onBackPressed();
        }
    }

    private void setFullscreen(final boolean fullscreen) {
        mFullscreen = fullscreen;
        if (fullscreen) {
            ActionBar ab = getActionBar();
            if (ab != null) {
                ab.setHomeButtonEnabled(true);
                ab.setDisplayHomeAsUpEnabled(true);
                ab.hide();
            }

            final View decor = getWindow().getDecorView();
            int visibility = 0;
            if (Build.VERSION_CODES.JELLY_BEAN <= Build.VERSION.SDK_INT) {
                visibility |= View.SYSTEM_UI_FLAG_FULLSCREEN;
            }
            if (Build.VERSION_CODES.KITKAT <= Build.VERSION.SDK_INT) {
                visibility |= View.SYSTEM_UI_FLAG_IMMERSIVE;
            }
            decor.setSystemUiVisibility(visibility);
        } else {
            final View decor = getWindow().getDecorView();
            if (Build.VERSION_CODES.JELLY_BEAN <= Build.VERSION.SDK_INT) {
                decor.setSystemUiVisibility(View.SYSTEM_UI_FLAG_VISIBLE);
            }
            ActionBar ab = getActionBar();
            if (ab != null) {
                ab.show();
            }
        }
    }

    private void parse() {
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
