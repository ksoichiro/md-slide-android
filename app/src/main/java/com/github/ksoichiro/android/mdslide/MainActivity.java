package com.github.ksoichiro.android.mdslide;

import android.app.ActionBar;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.AssetManager;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.FragmentActivity;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.Toast;

import com.github.ksoichiro.android.mdslide.widget.transition.FadePageTransformer;
import com.github.ksoichiro.android.mdslide.widget.transition.PopPageTransformer;
import com.github.ksoichiro.android.mdslide.widget.transition.PushPageTransformer;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;


public class MainActivity extends FragmentActivity {

    private InputStream mIn;
    private Uri mUri;
    private List<Page> mPages;
    private ViewPager mPager;
    private boolean mFullscreen;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        updatedDefaults();

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        CustomTextView.overrideFont = prefs.getString(SettingsActivity.PREF_FONT, "");
        CustomTextView.overrideFontForCodes = prefs.getString(SettingsActivity.PREF_FONT_FOR_CODES, "");
        CustomTextView.overrideFontForQuotes = prefs.getString(SettingsActivity.PREF_FONT_FOR_QUOTES, "");

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
            try {
                mIn = getResources().getAssets().open("slides/default.md");
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
        switch (id) {
            case R.id.action_settings:
                startActivity(new Intent(this, SettingsActivity.class));
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
        startActivity(intent);
    }
}
