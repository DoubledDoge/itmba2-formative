package com.example.itmba2_formative;

import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.core.view.WindowInsetsControllerCompat;

/**
    Note: This BaseActivity has nothing to do with what the assignment requires. It is only there to make the app more immersive and 'premium'.
    It is not required to use this BaseActivity, but it is recommended for this reasoning.
    Warning: Very complex terminology and code here, which I tried best as I could to simplify.
 */


public abstract class BaseActivity extends AppCompatActivity {

    private WindowInsetsControllerCompat windowInsetsController;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Enable edge-to-edge display
        WindowCompat.setDecorFitsSystemWindows(getWindow(), false);

        // Get the window insets controller
        windowInsetsController = WindowCompat.getInsetsController(getWindow(), getWindow().getDecorView());

        // Configure immersive mode
        setupImmersiveMode();

        // Set up gesture detection for showing navigation
        setupGestureNavigation();
    }

    private void setupImmersiveMode() {
        if (windowInsetsController != null) {
            // Hide both status and navigation bars
            windowInsetsController.hide(WindowInsetsCompat.Type.systemBars());

            // Set the behavior for when system bars are swiped
            windowInsetsController.setSystemBarsBehavior(
                    WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
            );
        }

        // Additional flags for older API levels and compatibility
        getWindow().setFlags(
                WindowManager.LayoutParams.FLAG_FULLSCREEN |
                        WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
                WindowManager.LayoutParams.FLAG_FULLSCREEN |
                        WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS
        );
    }

    private void setupGestureNavigation() {
        View decorView = getWindow().getDecorView();

        // Set up system UI visibility change listener
        decorView.setOnSystemUiVisibilityChangeListener(visibility -> {
            // When system UI becomes visible, set a timer to hide it again
            if ((visibility & View.SYSTEM_UI_FLAG_FULLSCREEN) == 0) {
                // System UI is visible, hide it again after delay
                decorView.postDelayed(this::hideSystemUI, 3000); // 3 second delay
            }
        });
    }

    /**
     * Hide system UI (navigation and status bars)
     */
    protected void hideSystemUI() {
        if (windowInsetsController != null) {
            windowInsetsController.hide(WindowInsetsCompat.Type.systemBars());
        }

        // Fallback for older API levels
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

    /**
     * Show system UI (navigation and status bars)
     */
    protected void showSystemUI() {
        if (windowInsetsController != null) {
            windowInsetsController.show(WindowInsetsCompat.Type.systemBars());
        }

        // Fallback for older API levels
        View decorView = getWindow().getDecorView();
        decorView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_STABLE);
    }

    /**
     * Toggle system UI visibility
     */
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
            // Re-hide system UI when window regains focus
            hideSystemUI();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Ensure system UI is hidden when activity resumes
        hideSystemUI();
    }
}