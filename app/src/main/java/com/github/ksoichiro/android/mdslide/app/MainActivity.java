package com.github.ksoichiro.android.mdslide.app;

import android.app.ActionBar;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.ActionBarDrawerToggle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.DrawerLayout;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.github.ksoichiro.android.mdslide.widget.CustomTextView;
import com.github.ksoichiro.android.mdslide.slide.Page;
import com.github.ksoichiro.android.mdslide.PageAdapter;
import com.github.ksoichiro.android.mdslide.markdown.Parser;
import com.github.ksoichiro.android.mdslide.R;
import com.github.ksoichiro.android.mdslide.style.Theme;
import com.github.ksoichiro.android.mdslide.style.Transition;
import com.github.ksoichiro.android.mdslide.widget.transition.FadePageTransformer;
import com.github.ksoichiro.android.mdslide.widget.transition.PopPageTransformer;
import com.github.ksoichiro.android.mdslide.widget.transition.PushPageTransformer;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;


public class MainActivity extends FragmentActivity {

    public static final String EXTRA_INITIAL_PAGE = "initialPage";
    private static final int REQUEST_CODE_SETTINGS = 1;
    private InputStream mIn;
    private int mCurrentPage;
    private Uri mUri;
    private List<Page> mPages;
    private ViewPager mPager;
    private boolean mFullscreen;
    private DrawerLayout mDrawer;
    private ActionBarDrawerToggle mDrawerToggle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        overridePendingTransition(R.anim.fade_in, R.anim.fade_out);

        updatedDefaults();

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        CustomTextView.overrideFont = prefs.getString(SettingsActivity.PREF_FONT, "");
        CustomTextView.overrideFontForCodes = prefs.getString(SettingsActivity.PREF_FONT_FOR_CODES, "");
        CustomTextView.overrideFontForQuotes = prefs.getString(SettingsActivity.PREF_FONT_FOR_QUOTES, "");
        PageFragment.buretteString = prefs.getString(SettingsActivity.PREF_BURETTE, "");

        Theme theme = Theme.BLACK;
        try {
            theme = Theme.valueOf(prefs.getString(SettingsActivity.PREF_THEME, "BLACK"));
        } catch (IllegalArgumentException e) {
            Log.e("Theme", "Illegal theme: " + prefs.getString(SettingsActivity.PREF_THEME, "BLACK"), e);
        }
        setTheme(theme.getThemeResId());

        setContentView(R.layout.activity_main);
        mFullscreen = false;

        ActionBar ab = getActionBar();
        if (ab != null) {
            View v = LayoutInflater.from(this).inflate(R.layout.actionbar, null);
            TextView titleTxtView = (TextView) v.findViewById(R.id.title);
            titleTxtView.setText(getTitle());

            ab.setDisplayShowTitleEnabled(false);
            ab.setDisplayShowCustomEnabled(true);
            ab.setDisplayHomeAsUpEnabled(true);
            ab.setCustomView(v);
        }

        mDrawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        mDrawerToggle = new ActionBarDrawerToggle(this, mDrawer,
                R.drawable.ic_drawer, 0, 0) {
            @Override
            public void onDrawerOpened(View drawerView) {
                super.onDrawerOpened(drawerView);
            }

            @Override
            public void onDrawerClosed(View drawerView) {
                super.onDrawerClosed(drawerView);
            }
        };
        View leftDrawer = findViewById(R.id.left_drawer);
        LinearLayout examplesLayout = (LinearLayout) leftDrawer.findViewById(R.id.examples);
        LayoutInflater inflater = LayoutInflater.from(this);
        try {
            for (String file : getAssets().list("slides")) {
                if (!file.endsWith(".md")) {
                    continue;
                }
                String showName = file.replaceAll(".md", "");
                View row = inflater.inflate(R.layout.drawer_example_row, null);
                Button item = (Button) row.findViewById(R.id.button_item);
                item.setText(showName);
                final String path = file;
                item.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        showExample(path);
                    }
                });
                examplesLayout.addView(row);
                View divider = inflater.inflate(R.layout.divider, null);
                examplesLayout.addView(divider);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        mDrawer.setDrawerListener(mDrawerToggle);

        Intent intent = getIntent();
        Uri uri = intent.getData();
        if (uri == null) {
            try {
                String path = intent.getStringExtra(Intent.EXTRA_TEXT);
                if (TextUtils.isEmpty(path)) {
                    path = "slides/introduction.md";
                }
                mIn = getResources().getAssets().open(path);
                mPages = new Parser(mIn).parse();
            } catch (IOException e) {
                return;
            }
        } else {
            mUri = uri;
            mPages = new Parser(uri).parse();
        }

        mPager = (ViewPager) findViewById(R.id.pager);
        PageAdapter adapter = new PageAdapter(getSupportFragmentManager());
        adapter.setPages(mPages);
        mPager.setAdapter(adapter);
        if (intent != null && intent.hasExtra(EXTRA_INITIAL_PAGE)) {
            int initialPage = intent.getIntExtra(EXTRA_INITIAL_PAGE, 0);
            if (0 <= initialPage && initialPage < mPages.size()) {
                mPager.setCurrentItem(initialPage);
            }
        }

        Transition transition = Transition.DEFAULT;
        try {
            transition = Transition.valueOf(prefs.getString(SettingsActivity.PREF_TRANSITION, "DEFAULT"));
        } catch (IllegalArgumentException e) {
            Log.e("Theme", "Illegal transition: " + prefs.getString(SettingsActivity.PREF_TRANSITION, "DEFAULT"), e);
        }
        switch (transition) {
            case FADE:
                mPager.setPageTransformer(false, new FadePageTransformer());
                break;
            case PUSH:
                findViewById(R.id.background).setBackgroundColor(Color.BLACK);
                mPager.setPageTransformer(false, new PushPageTransformer());
                break;
            case POP:
                findViewById(R.id.background).setBackgroundColor(Color.BLACK);
                mPager.setPageTransformer(true, new PopPageTransformer());
                break;
            case DEFAULT:
            default:
                break;
        }
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        mDrawerToggle.syncState();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        mDrawerToggle.onConfigurationChanged(newConfig);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE_SETTINGS) {
            if (resultCode == RESULT_OK) {
                reload();
            }
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        switch (id) {
            case R.id.action_settings:
                showSettings();
                return true;
            case R.id.action_fullscreen:
                setFullscreen(true);
                return true;
            case R.id.action_first_page:
                mPager.setCurrentItem(0);
                return true;
            case R.id.action_last_page:
                mPager.setCurrentItem(mPager.getAdapter().getCount() - 1);
                return true;
            case R.id.action_reload:
                reload();
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
        findViewById(android.R.id.content).setKeepScreenOn(mFullscreen);
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

    private void updatedDefaults() {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        SharedPreferences.Editor editor = prefs.edit();
        if (!prefs.contains(SettingsActivity.PREF_THEME)) {
            editor.putString(SettingsActivity.PREF_THEME, getString(R.string.theme_white));
        }
        if (!prefs.contains(SettingsActivity.PREF_TRANSITION)) {
            editor.putString(SettingsActivity.PREF_TRANSITION, getString(R.string.transition_push));
        }
        if (!prefs.contains(SettingsActivity.PREF_FONT)) {
            editor.putString(SettingsActivity.PREF_FONT, getString(R.string.font_name_roboto_black));
        }
        if (!prefs.contains(SettingsActivity.PREF_FONT_FOR_CODES)) {
            editor.putString(SettingsActivity.PREF_FONT_FOR_CODES, getString(R.string.font_name_source_code_pro_regular));
        }
        if (!prefs.contains(SettingsActivity.PREF_FONT_FOR_QUOTES)) {
            editor.putString(SettingsActivity.PREF_FONT_FOR_QUOTES, getString(R.string.font_name_lato_regular_italic));
        }
        if (!prefs.contains(SettingsActivity.PREF_BURETTE)) {
            editor.putString(SettingsActivity.PREF_BURETTE, getString(R.string.burette_point));
        }
        if (!prefs.contains(SettingsActivity.PREF_SHOW_PAGE_NUMBER)) {
            editor.putBoolean(SettingsActivity.PREF_SHOW_PAGE_NUMBER, false);
        }
        editor.commit();
    }

    private void reload() {
        Intent intent = new Intent(this, MainActivity.class);
        intent.setAction(Intent.ACTION_VIEW);
        intent.addCategory(Intent.CATEGORY_DEFAULT);
        intent.setData(mUri);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        intent.putExtra(EXTRA_INITIAL_PAGE, mPager.getCurrentItem());
        startActivity(intent);
    }

    private void showSettings() {
        startActivityForResult(new Intent(this, SettingsActivity.class), REQUEST_CODE_SETTINGS);
    }

    private void showExample(final String exampleMdName) {
        Intent intent = new Intent(this, MainActivity.class);
        intent.setAction(Intent.ACTION_VIEW);
        intent.addCategory(Intent.CATEGORY_DEFAULT);
        intent.putExtra(Intent.EXTRA_TEXT, "slides/" + exampleMdName);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        intent.putExtra(EXTRA_INITIAL_PAGE, mPager.getCurrentItem());
        startActivity(intent);
    }
}
