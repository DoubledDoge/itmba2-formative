package com.example.itmba2_formative;

import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;
import androidx.appcompat.app.AlertDialog;
import androidx.cardview.widget.CardView;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.itmba2_formative.models.User;

public class HomeActivity extends BaseActivity {

    // UI Components
    private TextView tvUserName, tvTesScore, tvProfile;
    private CardView cvMemories, cvGallery, cvBudget, cvDatabase, cvTesScore;

    // Database and Session Management
    private DatabaseHelper dbHelper;
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
        dbHelper = DatabaseHelper.getInstance(this);
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
        tvTesScore = findViewById(R.id.tv_tes_score);
        tvProfile = findViewById(R.id.tv_profile);

        // Feature cards
        cvMemories = findViewById(R.id.cv_memories);
        cvGallery = findViewById(R.id.cv_gallery);
        cvBudget = findViewById(R.id.cv_budget);
        cvDatabase = findViewById(R.id.cv_database);
        cvTesScore = findViewById(R.id.cv_tes_score);
    }

    private void loadUserData() {
        User currentUser = sessionManager.getLoggedInUser();
        if (currentUser != null) {
            tvUserName.setText(currentUser.getFullName());
            HelperMethods.setUserName(this, currentUser.getFullName());
        }

        int tesScore = HelperMethods.getCurrentTesScore(this);
        tvTesScore.setText(String.valueOf(tesScore));
    }

    private void setupClickListeners() {

        tvProfile.setOnClickListener(v -> showProfileMenu());

        cvMemories.setOnClickListener(v -> handleFeatureClick(
                MemoryActivity.class,
                AppConstants.TesScore.MEMORY_ENTRY
        ));

        cvGallery.setOnClickListener(v -> handleFeatureClick(
                GalleryActivity.class,
                AppConstants.TesScore.GALLERY_INTERACTION
        ));

        cvBudget.setOnClickListener(v -> handleFeatureClick(
                BudgetActivity.class,
                AppConstants.TesScore.NEW_TRIP
        ));

        cvDatabase.setOnClickListener(v -> navigateToActivity(DatabaseActivity.class));
        cvTesScore.setOnClickListener(v -> showTesScoreBreakdown());
    }

    private void handleFeatureClick(Class<?> activityClass, int tesPoints) {
        HelperMethods.addTesPointsWithFeedback(this, tesPoints);
        updateTesScoreDisplay();
        navigateToActivity(activityClass);
    }

    private void updateTesScoreDisplay() {
        int currentScore = HelperMethods.getCurrentTesScore(this);
        tvTesScore.setText(String.valueOf(currentScore));
    }

    private void showProfileMenu() {
        User currentUser = sessionManager.getLoggedInUser();
        String userName = currentUser != null ? currentUser.getFullName() : "User";

        new AlertDialog.Builder(this)
                .setTitle("Profile & Settings")
                .setMessage("Logged in as: " + userName + "\n\nWhat would you like to do?")
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

    /**
     * Show TES score breakdown dialog using helper methods
     */
    private void showTesScoreBreakdown() {
        User currentUser = sessionManager.getLoggedInUser();
        int memoryCount = 0;

        if (currentUser != null) {
            memoryCount = dbHelper.getUserMemoryCount(currentUser.getUserId());
        }

        String breakdownMessage = HelperMethods.getTesScoreBreakdown(this, memoryCount);
        HelperMethods.showAlert(this, "TripBuddy Engagement Score", breakdownMessage);
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