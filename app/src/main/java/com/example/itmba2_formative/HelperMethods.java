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

    public static String capitalizeWords(String str) {
        if (isEmpty(str)) return str;

        String[] words = str.toLowerCase().trim().split("\\s+");
        StringBuilder capitalized = new StringBuilder();

        for (String word : words) {
            if (!word.isEmpty()) {
                capitalized.append(Character.toUpperCase(word.charAt(0)))
                        .append(word.substring(1));
                if (capitalized.length() < str.length() && str.charAt(capitalized.length()) == ' ') {
                    capitalized.append(" ");
                }
            }
        }
        return capitalized.toString().trim();
    }

    public static SharedPreferences getSharedPreferences(Context context) {
        return context.getSharedPreferences(AppConstants.PrefKeys.PREF_NAME, Context.MODE_PRIVATE);
    }

    public static boolean getBooleanFromPreferences(Context context, String key, boolean defaultValue) {
        if (context == null || isEmpty(key)) return defaultValue;
        return getSharedPreferences(context).getBoolean(key, defaultValue);
    }

}
