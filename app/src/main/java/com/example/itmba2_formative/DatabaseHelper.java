package com.example.itmba2_formative;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.example.itmba2_formative.models.User;

public class DatabaseHelper extends SQLiteOpenHelper {
    private static final String TAG = "DatabaseHelper";

    // Database Info
    private static final String DATABASE_NAME = "TripBuddyDB";
    private static final int DATABASE_VERSION = 1;

    // Table Names
    private static final String TABLE_USERS = "users";
    private static final String TABLE_MEMORIES = "memories";

    // Users Table Columns
    private static final String KEY_USER_ID = "user_id";
    private static final String KEY_USER_EMAIL = "email";
    private static final String KEY_USER_PASSWORD = "password";
    private static final String KEY_USER_FULL_NAME = "full_name";
    private static final String KEY_USER_CREATED_AT = "created_at";
    private static final String KEY_USER_LAST_LOGIN = "last_login";

    // Memories Table Columns
    private static final String KEY_MEMORY_ID = "id";
    private static final String KEY_TITLE = "title";
    private static final String KEY_DESCRIPTION = "description";
    private static final String KEY_PHOTO_URI = "photo_uri";
    private static final String KEY_MUSIC_URI = "music_uri";
    private static final String KEY_LOCATION = "location";
    private static final String KEY_IS_DRAFT = "is_draft";
    private static final String KEY_USER_ID_FK = "user_id";

    // Singleton instance
    private static DatabaseHelper instance;

    public static synchronized DatabaseHelper getInstance(Context context) {
        if (instance == null) {
            instance = new DatabaseHelper(context.getApplicationContext());
        }
        return instance;
    }

    private DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        // Create Users table
        String CREATE_USERS_TABLE = "CREATE TABLE " + TABLE_USERS + "("
                + KEY_USER_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + KEY_USER_EMAIL + " TEXT UNIQUE NOT NULL,"
                + KEY_USER_PASSWORD + " TEXT NOT NULL,"
                + KEY_USER_FULL_NAME + " TEXT NOT NULL,"
                + KEY_USER_CREATED_AT + " DATETIME DEFAULT CURRENT_TIMESTAMP,"
                + KEY_USER_LAST_LOGIN + " DATETIME"
                + ")";

        // Create memories table with foreign key
        String CREATE_MEMORIES_TABLE = "CREATE TABLE " + TABLE_MEMORIES + "("
                + KEY_MEMORY_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + KEY_USER_ID_FK + " INTEGER NOT NULL,"
                + KEY_TITLE + " TEXT,"
                + KEY_DESCRIPTION + " TEXT,"
                + KEY_PHOTO_URI + " TEXT,"
                + KEY_MUSIC_URI + " TEXT,"
                + KEY_LOCATION + " TEXT,"
                + KEY_IS_DRAFT + " INTEGER DEFAULT 0,"
                + "FOREIGN KEY(" + KEY_USER_ID_FK + ") REFERENCES " + TABLE_USERS + "(" + KEY_USER_ID + ")"
                + ")";

        db.execSQL(CREATE_USERS_TABLE);
        db.execSQL(CREATE_MEMORIES_TABLE);

        Log.d(TAG, "Database tables created successfully");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_MEMORIES);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_USERS);
        onCreate(db);
    }

    // User Authentication Methods

    /**
     * Create a new user account
     */
    public long createUser(String email, String password, String fullName) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();

        values.put(KEY_USER_EMAIL, email.trim().toLowerCase());
        values.put(KEY_USER_PASSWORD, password);
        values.put(KEY_USER_FULL_NAME, fullName.trim());

        long userId = db.insert(TABLE_USERS, null, values);
        db.close();

        Log.d(TAG, "User created with ID: " + userId);
        return userId;
    }

    /**
     * Authenticate user login
     */
    public User authenticateUser(String email, String password) {
        SQLiteDatabase db = this.getReadableDatabase();

        String[] columns = {KEY_USER_ID, KEY_USER_EMAIL, KEY_USER_FULL_NAME};
        String selection = KEY_USER_EMAIL + "=? AND " + KEY_USER_PASSWORD + "=?";
        String[] selectionArgs = {email.trim().toLowerCase(), password};

        Cursor cursor = db.query(TABLE_USERS, columns, selection, selectionArgs, null, null, null);

        User user = null;
        if (cursor.moveToFirst()) {
            user = new User(
                    cursor.getInt(cursor.getColumnIndexOrThrow(KEY_USER_ID)),
                    cursor.getString(cursor.getColumnIndexOrThrow(KEY_USER_EMAIL)),
                    cursor.getString(cursor.getColumnIndexOrThrow(KEY_USER_FULL_NAME))
            );

            // Update last login timestamp
            updateLastLogin(user.getUserId());

            Log.d(TAG, "User authenticated successfully: " + user.getEmail());
        }

        cursor.close();
        db.close();

        return user;
    }

    /**
     * Check if email already exists
     */
    public boolean isEmailExists(String email) {
        SQLiteDatabase db = this.getReadableDatabase();

        String[] columns = {KEY_USER_ID};
        String selection = KEY_USER_EMAIL + "=?";
        String[] selectionArgs = {email.trim().toLowerCase()};

        Cursor cursor = db.query(TABLE_USERS, columns, selection, selectionArgs, null, null, null);

        boolean exists = cursor.getCount() > 0;

        cursor.close();
        db.close();

        return exists;
    }

    /**
     * Update user's last login timestamp
     */
    private void updateLastLogin(int userId) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(KEY_USER_LAST_LOGIN, System.currentTimeMillis());

        db.update(TABLE_USERS, values, KEY_USER_ID + "=?", new String[]{String.valueOf(userId)});
        db.close();
    }

    /**
     * Add a new memory entry
     */
    public long addMemory(int userId, String title, String textContent, String imagePath,
                          String location, String date, String mood, String backgroundMusic) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();

        values.put(KEY_USER_ID_FK, userId);
        values.put(KEY_TITLE, title);
        values.put(KEY_DESCRIPTION, textContent);
        values.put(KEY_PHOTO_URI, imagePath);
        values.put(KEY_MUSIC_URI, backgroundMusic);
        values.put(KEY_LOCATION, location);
        values.put(KEY_IS_DRAFT, 0);

        long memoryId = db.insert(TABLE_MEMORIES, null, values);
        db.close();

        Log.d(TAG, "Memory added with ID: " + memoryId);
        return memoryId;
    }

    /**
     * Get all memories for a user
     */
    public Cursor getUserMemories(int userId) {
        SQLiteDatabase db = this.getReadableDatabase();

        String[] columns = {
                KEY_MEMORY_ID, KEY_TITLE, KEY_DESCRIPTION, KEY_PHOTO_URI,
                KEY_MUSIC_URI, KEY_LOCATION, KEY_IS_DRAFT
        };

        String selection = KEY_USER_ID_FK + "=?";
        String[] selectionArgs = {String.valueOf(userId)};
        String orderBy = KEY_MEMORY_ID + " DESC";

        return db.query(TABLE_MEMORIES, columns, selection, selectionArgs, null, null, orderBy);
    }

    /**
     * Get memory count for a user
     */
    public int getUserMemoryCount(int userId) {
        SQLiteDatabase db = this.getReadableDatabase();

        String[] columns = {KEY_MEMORY_ID};
        String selection = KEY_USER_ID_FK + "=?";
        String[] selectionArgs = {String.valueOf(userId)};

        Cursor cursor = db.query(TABLE_MEMORIES, columns, selection, selectionArgs, null, null, null);

        int count;
        count = cursor.getCount();
        cursor.close();

        db.close();
        return count;
    }

    /**
     * Delete a memory
     */
    public boolean deleteMemory(int memoryId, int userId) {
        SQLiteDatabase db = this.getWritableDatabase();

        String selection = KEY_MEMORY_ID + "=? AND " + KEY_USER_ID_FK + "=?";
        String[] selectionArgs = {String.valueOf(memoryId), String.valueOf(userId)};

        int rowsAffected = db.delete(TABLE_MEMORIES, selection, selectionArgs);
        db.close();

        return rowsAffected > 0;
    }

    // Utility Methods

    /**
     * Get database statistics for debugging
     */
    public String getDatabaseStats() {
        SQLiteDatabase db = this.getReadableDatabase();

        // Count users
        Cursor userCursor = db.rawQuery("SELECT COUNT(*) FROM " + TABLE_USERS, null);
        int userCount = 0;
        if (userCursor.moveToFirst()) {
            userCount = userCursor.getInt(0);
            userCursor.close();
        }

        // Count memories
        Cursor memoryCursor = db.rawQuery("SELECT COUNT(*) FROM " + TABLE_MEMORIES, null);
        int memoryCount = 0;
        if (memoryCursor.moveToFirst()) {
            memoryCount = memoryCursor.getInt(0);
            memoryCursor.close();
        }

        db.close();

        return "Users: " + userCount + ", Memories: " + memoryCount;
    }

    /**
     * Retrieves all data from specified tables and formats it as a string for debugging.
     * @return A string containing all data from the users and memories tables.
     */
    public String getDebugDatabaseDump() {
        SQLiteDatabase db = this.getReadableDatabase();
        StringBuilder dump = new StringBuilder();

        // Dump Users Table
        dump.append("--- ").append(TABLE_USERS).append(" Table ---\n");
        Cursor userCursor = db.rawQuery("SELECT * FROM " + TABLE_USERS, null);
        if (userCursor.moveToFirst()) {
            String[] columnNames = userCursor.getColumnNames();
            dump.append(String.join(" | ", columnNames)).append("\n");
            do {
                for (int i = 0; i < userCursor.getColumnCount(); i++) {
                    dump.append(userCursor.getString(i)).append(i == userCursor.getColumnCount() - 1 ? "" : " | ");
                }
                dump.append("\n");
            } while (userCursor.moveToNext());
        } else {
            dump.append("No data in ").append(TABLE_USERS).append(" table.\n");
        }
        userCursor.close();
        dump.append("\n");

        // Dump Memories Table
        dump.append("--- ").append(TABLE_MEMORIES).append(" Table ---\n");
        Cursor memoryCursor = db.rawQuery("SELECT * FROM " + TABLE_MEMORIES, null);
        if (memoryCursor.moveToFirst()) {
            String[] columnNames = memoryCursor.getColumnNames();
            dump.append(String.join(" | ", columnNames)).append("\n");
            do {
                for (int i = 0; i < memoryCursor.getColumnCount(); i++) {
                    dump.append(memoryCursor.getString(i)).append(i == memoryCursor.getColumnCount() - 1 ? "" : " | ");
                }
                dump.append("\n");
            } while (memoryCursor.moveToNext());
        } else {
            dump.append("No data in ").append(TABLE_MEMORIES).append(" table.\n");
        }
        memoryCursor.close();

        db.close();
        return dump.toString();
    }
}
