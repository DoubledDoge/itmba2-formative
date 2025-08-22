package com.example.itmba2_formative;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;

public class HomeActivity extends AppCompatActivity {

    // UI Components
    private TextView tvUserName, tvTesScore, tvProfile;
    private MaterialCardView cvMemories,  cvGallery, cvBudget, cvDatabase, cvTesScore;
    private MaterialButton btnQuickMemory, btnQuickTrip;

    // SharedPreferences for user data
    private SharedPreferences userPrefs;
    private static final String PREF_USER_NAME = "user_name";
    private static final String PREF_TES_SCORE = "tes_score";

    // TES Score multipliers (as defined in requirements)
    private static final int TES_NEW_TRIP = 5;
    private static final int TES_MEMORY_ENTRY = 3;
    private static final int TES_GALLERY_INTERACTION = 1;
    private static final int TES_LOYALTY_FEATURE = 4;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        // Initialize SharedPreferences
        userPrefs = getSharedPreferences("TripBuddyPrefs", MODE_PRIVATE);

        // Initialize views
        initializeViews();

        // Load user data
        loadUserData();

        // Set up click listeners
        setupClickListeners();
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

        // Quick action buttons
        btnQuickMemory = findViewById(R.id.btn_quick_memory);
        btnQuickTrip = findViewById(R.id.btn_quick_trip);
    }

    private void loadUserData() {
        // Load user name
        String userName = userPrefs.getString(PREF_USER_NAME, "Traveler");
        tvUserName.setText(userName);

        // Load and display TES score
        int tesScore = userPrefs.getInt(PREF_TES_SCORE, 0);
        tvTesScore.setText(String.valueOf(tesScore));
    }

    private void setupClickListeners() {
        tvProfile.setOnClickListener(v -> navigateToActivity(ProfileActivity.class));

        cvMemories.setOnClickListener(v -> {
            updateTesScore(TES_MEMORY_ENTRY);
            navigateToActivity(MemoryActivity.class);
        });

        cvGallery.setOnClickListener(v -> {
            updateTesScore(TES_GALLERY_INTERACTION);
            navigateToActivity(GalleryActivity.class);
        });

        cvBudget.setOnClickListener(v -> {
            updateTesScore(TES_NEW_TRIP);
            navigateToActivity(BudgetActivity.class);
        });

        cvDatabase.setOnClickListener(v -> navigateToActivity(DatabaseActivity.class));

        cvTesScore.setOnClickListener(v -> showTesScoreBreakdown());

        btnQuickMemory.setOnClickListener(v -> {
            updateTesScore(TES_MEMORY_ENTRY);
            navigateToActivity(MemoryActivity.class);
        });

        btnQuickTrip.setOnClickListener(v -> {
            updateTesScore(TES_NEW_TRIP);
            navigateToActivity(BudgetActivity.class);
        });
    }

    private void navigateToActivity(Class<?> activityClass) {
        try {
            Intent intent = new Intent(this, activityClass);
            startActivity(intent);
        } catch (Exception e) {
            // Activity not implemented yet
            String activityName = activityClass.getSimpleName();
            showToast(activityName + " coming soon!");
        }
    }

    /**
     * Update TES score and refresh display
     */
    private void updateTesScore(int points) {
        int currentScore = userPrefs.getInt(PREF_TES_SCORE, 0);
        int newScore = currentScore + points;

        // Save updated score
        userPrefs.edit()
                .putInt(PREF_TES_SCORE, newScore)
                .apply();

        // Update display
        tvTesScore.setText(String.valueOf(newScore));

        // Show feedback to user
        showToast("+" + points + " TES points earned! Total: " + newScore);
    }

    /**
     * Show TES score breakdown dialog
     */
    private void showTesScoreBreakdown() {
        // TODO: Implement detailed TES score breakdown dialog
        int currentScore = userPrefs.getInt(PREF_TES_SCORE, 0);
        String message = "Current TES Score: " + currentScore + "\n\n" +
                "Earning TES Points:\n" +
                "• New trip planned: +" + TES_NEW_TRIP + " points\n" +
                "• Memory entry: +" + TES_MEMORY_ENTRY + " points\n" +
                "• Gallery interaction: +" + TES_GALLERY_INTERACTION + " point\n" +
                "• Loyalty feature use: +" + TES_LOYALTY_FEATURE + " points";

        showAlert("TripBuddy Engagement Score", message);
    }

    /**
     * Utility method to show toast messages
     */
    private void showToast(String message) {
        android.widget.Toast.makeText(this, message, android.widget.Toast.LENGTH_SHORT).show();
    }

    /**
     * Utility method to show alert dialogs
     */
    private void showAlert(String title, String message) {
        new androidx.appcompat.app.AlertDialog.Builder(this)
                .setTitle(title)
                .setMessage(message)
                .setPositiveButton("OK", null)
                .show();
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadUserData();
    }

    /**
     * Public method to set user name (called from AuthActivity)
     */
    public static void setUserName(android.content.Context context, String userName) {
        android.content.SharedPreferences prefs = context.getSharedPreferences("TripBuddyPrefs", MODE_PRIVATE);
        prefs.edit().putString(PREF_USER_NAME, userName).apply();
    }

    /**
     * Public method to get current TES score
     */
    public static int getCurrentTesScore(android.content.Context context) {
        android.content.SharedPreferences prefs = context.getSharedPreferences("TripBuddyPrefs", MODE_PRIVATE);
        return prefs.getInt(PREF_TES_SCORE, 0);
    }

    /**
     * Public method to add TES points from other activities
     */
    public static void addTesPoints(android.content.Context context, int points) {
        android.content.SharedPreferences prefs = context.getSharedPreferences("TripBuddyPrefs", MODE_PRIVATE);
        int currentScore = prefs.getInt(PREF_TES_SCORE, 0);
        prefs.edit().putInt(PREF_TES_SCORE, currentScore + points).apply();
    }
}