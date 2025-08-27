package com.example.itmba2_formative;

/**
 * Application-wide constants used throughout the TripBuddy app.
 * This class contains all static final values that don't change during runtime.
 */
public final class AppConstants {

    public static final class PrefKeys {
        public static final String PREF_NAME = "TripBuddyPrefs";
        public static final String KEY_USER_ID = "userId";
        public static final String PREF_USER_NAME = "userName";
        public static final String KEY_USER_EMAIL = "userEmail";
        public static final String KEY_IS_LOGGED_IN = "isLoggedIn";
        public static final String PREF_APP_THEME = "appTheme";
        public static final String PREF_BACKGROUND_MUSIC = "backgroundMusicEnabled";
        public static final String PREF_APP_LANGUAGE = "appLanguage"; // Unused since I'm not translating this app
    }

    public static final class ThemeOptions {
        public static final String LIGHT = "Light";
        public static final String DARK = "Dark";
    }

    public static final class Validation {
        public static final int MIN_PASSWORD_LENGTH = 6;
        public static final int MAX_PASSWORD_LENGTH = 128;
        public static final int MIN_FULL_NAME_LENGTH = 2;
        public static final int MAX_FULL_NAME_LENGTH = 50;
    }
}
