package com.example.itmba2_formative;

import android.content.Context;
import android.content.SharedPreferences;
import android.text.TextUtils;
import android.util.Patterns;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;

public final class HelperMethods {

    public static void showToast(Context context, String message) {
        if (context != null && !TextUtils.isEmpty(message)) {
            Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
        }
    }

    public static void showAlert(Context context, String title, String message) {
        if (context == null) return;

        new AlertDialog.Builder(context)
                .setTitle(title)
                .setMessage(message)
                .setPositiveButton("OK", null)
                .show();
    }

    public static void showConfirmationDialog(Context context, String title, String message,
                                              String positiveText, String negativeText,
                                              Runnable onConfirm, Runnable onCancel) {
        if (context == null) return;

        AlertDialog.Builder builder = new AlertDialog.Builder(context)
                .setTitle(title)
                .setMessage(message)
                .setPositiveButton(positiveText, (dialog, which) -> {
                    if (onConfirm != null) onConfirm.run();
                })
                .setNegativeButton(negativeText, (dialog, which) -> {
                    if (onCancel != null) onCancel.run();
                });
        builder.show();
    }

    public static boolean isEmpty(String str) {
        return TextUtils.isEmpty(str) || str.trim().isEmpty();
    }

    public static boolean isInvalidEmail(String email) {
        return isEmpty(email) || !Patterns.EMAIL_ADDRESS.matcher(email).matches();
    }

    public static boolean isValidPassword(String password) {
        return !isEmpty(password) &&
                password.length() >= AppConstants.Validation.MIN_PASSWORD_LENGTH &&
                password.length() <= AppConstants.Validation.MAX_PASSWORD_LENGTH;
    }

    public static boolean passwordsMatch(String password, String confirmPassword) {
        return password.equals(confirmPassword);
    }

    public static boolean isValidFullName(String fullName) {
        return !isEmpty(fullName) &&
                fullName.trim().length() >= AppConstants.Validation.MIN_FULL_NAME_LENGTH &&
                fullName.trim().length() <= AppConstants.Validation.MAX_FULL_NAME_LENGTH;
    }

    public static int getCurrentTesScore(Context context) {
        if (context == null) return 0;
        SharedPreferences prefs = getSharedPreferences(context);
        return prefs.getInt(AppConstants.PrefKeys.PREF_NAME, 0);
    }

    public static void updateTesScore(Context context, int points) {
        if (context == null) return;

        SharedPreferences prefs = getSharedPreferences(context);
        int currentScore = prefs.getInt(AppConstants.PrefKeys.PREF_TES_SCORE, 0);
        int newScore = Math.max(0, currentScore + points); // Ensure score doesn't go negative

        prefs.edit().putInt(AppConstants.PrefKeys.PREF_TES_SCORE, newScore).apply();
    }

    /**
     * Add TES points and show feedback to user
     */
    public static void addTesPointsWithFeedback(Context context, int points) {
        if (context == null || points <= 0) return;

        int currentScore = getCurrentTesScore(context);
        updateTesScore(context, points);
        int newScore = currentScore + points;

        showToast(context, "+" + points + " TES points earned! Total: " + newScore);
    }

    public static String getTesScoreBreakdown(Context context, int memoryCount) {
        int currentScore = getCurrentTesScore(context);

        return "Current TES Score: " + currentScore + "\n" +
                "Memories Created: " + memoryCount + "\n\n" +
                "Earning TES Points:\n" +
                "• New trip planned: +" + AppConstants.TesScore.NEW_TRIP + " points\n" +
                "• Memory entry: +" + AppConstants.TesScore.MEMORY_ENTRY + " points\n" +
                "• Gallery interaction: +" + AppConstants.TesScore.GALLERY_INTERACTION + " point\n" +
                "• Loyalty feature use: +" + AppConstants.TesScore.LOYALTY_FEATURE + " points";
    }

    public static String capitalizeWords(String str) {
        if (isEmpty(str)) return str;

        String[] words = str.toLowerCase().trim().split("\\s+");
        StringBuilder capitalized = new StringBuilder();

        for (String word : words) {
            if (!word.isEmpty()) {
                capitalized.append(Character.toUpperCase(word.charAt(0)))
                        .append(word.substring(1));

                // Add space if not the last word
                if (capitalized.length() < str.length()) {
                    capitalized.append(" ");
                }
            }
        }

        return capitalized.toString().trim();
    }

    private static SharedPreferences getSharedPreferences(Context context) {
        return context.getSharedPreferences(AppConstants.PrefKeys.PREF_NAME, Context.MODE_PRIVATE);
    }

    public static void saveToPreferences(Context context, String key, String value) {
        if (context == null || isEmpty(key)) return;
        getSharedPreferences(context).edit().putString(key, value).apply();
    }

    public static void setUserName(Context context, String userName) {
        saveToPreferences(context, AppConstants.PrefKeys.PREF_USER_NAME, userName);
    }
}