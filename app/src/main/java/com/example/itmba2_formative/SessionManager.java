package com.example.itmba2_formative;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;

import com.example.itmba2_formative.models.User;

public class SessionManager {
    private final SharedPreferences pref;
    private final SharedPreferences.Editor editor;
    private final Context context;

    public SessionManager(Context context) {
        this.context = context;
        pref = context.getSharedPreferences(AppConstants.PrefKeys.PREF_NAME, Context.MODE_PRIVATE);
        editor = pref.edit();
    }
    public void createLoginSession(User user) {
        editor.putBoolean(AppConstants.PrefKeys.KEY_IS_LOGGED_IN, true);
        editor.putInt(AppConstants.PrefKeys.KEY_USER_ID, user.getUserId());
        editor.putString(AppConstants.PrefKeys.KEY_USER_EMAIL, user.getEmail());
        editor.putString(AppConstants.PrefKeys.PREF_USER_NAME, user.getFullName());
        editor.commit();
    }

    public boolean isLoggedIn() {
        return pref.getBoolean(AppConstants.PrefKeys.KEY_IS_LOGGED_IN, false);
    }

    public User getLoggedInUser() {
        if (isLoggedIn()) {
            return new User(
                    pref.getInt(AppConstants.PrefKeys.KEY_USER_ID, -1),
                    pref.getString(AppConstants.PrefKeys.KEY_USER_EMAIL, ""),
                    pref.getString(AppConstants.PrefKeys.PREF_USER_NAME, "")
            );
        }
        return null;
    }

    public void logoutUser() {
        editor.clear();
        editor.commit();

        // Redirect to main activity
        Intent intent = new Intent(context, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(intent);
    }

}