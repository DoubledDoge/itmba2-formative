package com.example.itmba2_formative;

import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;
import androidx.appcompat.app.AlertDialog;
import androidx.cardview.widget.CardView;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.itmba2_formative.objects.User;

public class HomeActivity extends BaseActivity {

    // UI Components
    private TextView tvUserName, tvProfile;
    private CardView cvMemories, cvGallery, cvBudget, cvDatabase;

    private SessionManager sessionManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        // Handle window insets for edge-to-edge display
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.home_coordinator), (view, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            view.setPaddingRelative(
                systemBars.left,
                systemBars.top,
                systemBars.right,
                systemBars.bottom
            );
            return insets;
        });

        initializeDependencies();

        if (invalidateUserSession()) {
            return;
        }

        initializeViews();
        loadUserData();
        setupClickListeners();
    }

    private void initializeDependencies() {
        DatabaseHelper.getInstance(this);
        sessionManager = new SessionManager(this);
    }

    private boolean invalidateUserSession() {
        if (!sessionManager.isLoggedIn()) {
            redirectToAuth();
            return true;
        }
        return false;
    }

    private void initializeViews() {
        // Text views
        tvUserName = findViewById(R.id.tv_user_name);
        tvProfile = findViewById(R.id.tv_profile);

        // Feature cards
        cvMemories = findViewById(R.id.cv_memories);
        cvGallery = findViewById(R.id.cv_gallery);
        cvBudget = findViewById(R.id.cv_budget);
        cvDatabase = findViewById(R.id.cv_database);
    }

    private void loadUserData() {
        User currentUser = sessionManager.getLoggedInUser();
        if (currentUser != null) {
            tvUserName.setText(currentUser.getFullName());
        }
    }

    private void setupClickListeners() {
        tvProfile.setOnClickListener(v -> showProfileMenu());

        cvMemories.setOnClickListener(v -> navigateToActivity(MemoryActivity.class));
        cvGallery.setOnClickListener(v -> navigateToActivity(GalleryActivity.class));
        cvBudget.setOnClickListener(v -> navigateToActivity(BudgetActivity.class));
        cvDatabase.setOnClickListener(v -> navigateToActivity(DatabaseActivity.class));
    }

    private void showProfileMenu() {
        sessionManager.getLoggedInUser();

        new AlertDialog.Builder(this)
                .setTitle("Profile & Settings")
                .setMessage("What would you like to do?")
                .setPositiveButton("Logout", (dialog, which) -> showLogoutConfirmation())
                .setNeutralButton("Profile Settings", (dialog, which) -> navigateToActivity(ProfileActivity.class))
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void showLogoutConfirmation() {
        HelperMethods.showConfirmationDialog(
                this,
                "Logout",
                "Are you sure you want to logout?",
                "Yes, Logout",
                "Cancel",
                this::performLogout,
                null
        );
    }

    private void performLogout() {
        sessionManager.logoutUser();
        HelperMethods.showToast(this, getString(R.string.logout_success));
        redirectToAuth();
    }

    private void redirectToAuth() {
        Intent intent = new Intent(this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    private void navigateToActivity(Class<?> activityClass) {
        try {
            Intent intent = new Intent(this, activityClass);
            startActivity(intent);
        } catch (Exception e) {
            String activityName = activityClass.getSimpleName();
            HelperMethods.showToast(this, activityName + " coming soon!");
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (invalidateUserSession()) {
            return;
        }
        loadUserData();
    }
}
