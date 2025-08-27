package com.example.itmba2_formative;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.TextView;

import androidx.appcompat.widget.SwitchCompat;
import androidx.appcompat.widget.Toolbar;
import androidx.appcompat.app.AppCompatDelegate;

public class ProfileActivity extends BaseActivity {

    private SwitchCompat switchTheme, switchMusic;
    private TextView textViewLanguage;
    private SharedPreferences prefs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        Toolbar toolbar = findViewById(R.id.toolbar_profile);
        setSupportActionBar(toolbar);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Settings");
        }

        prefs = getSharedPreferences(AppConstants.PrefKeys.PREF_NAME, MODE_PRIVATE);

        switchTheme = findViewById(R.id.switch_theme);
        switchMusic = findViewById(R.id.switch_music);

        textViewLanguage = findViewById(R.id.tv_language_setting);

        loadPreferences();
        setupListeners();
    }

    private void loadPreferences() {
        String currentTheme = prefs.getString(AppConstants.PrefKeys.PREF_APP_THEME, AppConstants.ThemeOptions.LIGHT);
        switchTheme.setChecked(AppConstants.ThemeOptions.DARK.equals(currentTheme));

        boolean musicEnabled = prefs.getBoolean(AppConstants.PrefKeys.PREF_BACKGROUND_MUSIC, true);
        switchMusic.setChecked(musicEnabled);
    }

    private void setupListeners() {
        switchTheme.setOnCheckedChangeListener((buttonView, isChecked) -> {
            String theme = isChecked ? AppConstants.ThemeOptions.DARK : AppConstants.ThemeOptions.LIGHT;
            prefs.edit().putString(AppConstants.PrefKeys.PREF_APP_THEME, theme).apply();
            applyTheme(theme);
            HelperMethods.showToast(this, "Theme changed to " + theme);
        });

        switchMusic.setOnCheckedChangeListener((buttonView, isChecked) -> {
            prefs.edit().putBoolean(AppConstants.PrefKeys.PREF_BACKGROUND_MUSIC, isChecked).apply();
            if (isChecked) {
                HelperMethods.showToast(this, "Background music enabled");
            } else {
                HelperMethods.showToast(this, "Background music disabled");
            }
        });

        textViewLanguage.setOnClickListener(v -> {
            HelperMethods.showToast(this, "Sorry, I'm not a translator.");
        });
    }

    private void applyTheme(String theme) {
        if (AppConstants.ThemeOptions.DARK.equals(theme)) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
        } else {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        }
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(android.view.MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
