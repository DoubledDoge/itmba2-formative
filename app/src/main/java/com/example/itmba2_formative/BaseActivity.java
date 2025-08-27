package com.example.itmba2_formative;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.core.view.WindowInsetsControllerCompat;

public abstract class BaseActivity extends AppCompatActivity {

    private WindowInsetsControllerCompat windowInsetsController;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        applyAppTheme(); // Apply theme before super.onCreate()
        super.onCreate(savedInstanceState);

        WindowCompat.setDecorFitsSystemWindows(getWindow(), false);
        windowInsetsController = WindowCompat.getInsetsController(getWindow(), getWindow().getDecorView());
        setupImmersiveMode();
        setupGestureNavigation();
    }

    private void applyAppTheme() {
        SharedPreferences prefs = getSharedPreferences(AppConstants.PrefKeys.PREF_NAME, MODE_PRIVATE);
        String theme = prefs.getString(AppConstants.PrefKeys.PREF_APP_THEME, "Light"); // Default to Light
        if ("Dark".equals(theme)) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
        } else {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        }
    }

    private void setupImmersiveMode() {
        if (windowInsetsController != null) {
            windowInsetsController.hide(WindowInsetsCompat.Type.systemBars());
            windowInsetsController.setSystemBarsBehavior(
                    WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
            );
        }
        getWindow().setFlags(
                WindowManager.LayoutParams.FLAG_FULLSCREEN |
                        WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
                WindowManager.LayoutParams.FLAG_FULLSCREEN |
                        WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS
        );
    }

    private void setupGestureNavigation() {
        View decorView = getWindow().getDecorView();
        decorView.setOnSystemUiVisibilityChangeListener(visibility -> {
            if ((visibility & View.SYSTEM_UI_FLAG_FULLSCREEN) == 0) {
                decorView.postDelayed(this::hideSystemUI, 3000);
            }
        });
    }

    protected void hideSystemUI() {
        if (windowInsetsController != null) {
            windowInsetsController.hide(WindowInsetsCompat.Type.systemBars());
        }
        View decorView = getWindow().getDecorView();
        decorView.setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY |
                        View.SYSTEM_UI_FLAG_LAYOUT_STABLE |
                        View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION |
                        View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN |
                        View.SYSTEM_UI_FLAG_HIDE_NAVIGATION |
                        View.SYSTEM_UI_FLAG_FULLSCREEN
        );
    }

    protected void showSystemUI() {
        if (windowInsetsController != null) {
            windowInsetsController.show(WindowInsetsCompat.Type.systemBars());
        }
        View decorView = getWindow().getDecorView();
        decorView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_STABLE);
    }

    protected void toggleSystemUI() {
        View decorView = getWindow().getDecorView();
        int uiOptions = decorView.getSystemUiVisibility();
        if ((uiOptions & View.SYSTEM_UI_FLAG_FULLSCREEN) == 0) {
            hideSystemUI();
        } else {
            showSystemUI();
        }
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if (hasFocus) {
            hideSystemUI();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        hideSystemUI();
    }
}
