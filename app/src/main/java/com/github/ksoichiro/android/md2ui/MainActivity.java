package com.github.ksoichiro.android.md2ui;

import android.app.ActionBar;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
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

    private List<Page> mPages;
    private ViewPager mPager;
    private boolean mFullscreen;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        Theme theme = Theme.BLACK;
        try {
            theme = Theme.valueOf(prefs.getString(SettingsActivity.PREF_THEME, "BLACK"));
        } catch (IllegalArgumentException e) {
            Log.e("Theme", "Illegal theme: " + prefs.getString(SettingsActivity.PREF_THEME, "BLACK"), e);
        }
        setTheme(theme.getThemeResId());

        setContentView(R.layout.activity_main);
        mFullscreen = false;

        Intent intent = getIntent();
        Uri uri = intent.getData();
        if (uri == null) {
            Toast.makeText(this, "Invalid URL", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
        mPages = new Parser(uri).parse();

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
            startActivity(new Intent(this, SettingsActivity.class));
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
}
