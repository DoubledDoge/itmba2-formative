package com.example.itmba2_formative;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.example.itmba2_formative.objects.User;
import com.example.itmba2_formative.objects.Trip;

public class DatabaseHelper extends SQLiteOpenHelper {
    private static final String TAG = "DatabaseHelper";

    // Database Info
    private static final String DATABASE_NAME = "TripBuddyDB";
    private static final int DATABASE_VERSION = 2;

    // Table Names
    private static final String TABLE_USERS = "users";
    private static final String TABLE_MEMORIES = "memories";
    private static final String TABLE_TRIPS = "trips";

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

    // Trips Table Columns
    private static final String KEY_TRIP_ID = "trip_id";
    private static final String KEY_DESTINATION = "destination";
    private static final String KEY_START_DATE = "start_date";
    private static final String KEY_END_DATE = "end_date";
    private static final String KEY_TRAVELERS_COUNT = "travelers_count";
    private static final String KEY_TOTAL_COST = "total_cost";
    private static final String KEY_ACTIVITIES_SELECTED = "activities_selected";
    private static final String KEY_CUSTOM_EXPENSES = "custom_expenses";
    private static final String KEY_TRIP_NOTES = "trip_notes";
    private static final String KEY_HAS_LOYALTY_DISCOUNT = "has_loyalty_discount";
    private static final String KEY_DISCOUNT_AMOUNT = "discount_amount";
    private static final String KEY_TRIP_CREATED_AT = "created_at";
    private static final String KEY_TRIP_USER_ID_FK = "user_id";

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
        String CREATE_USERS_TABLE = "CREATE TABLE " + TABLE_USERS + "("
                + KEY_USER_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + KEY_USER_EMAIL + " TEXT UNIQUE NOT NULL,"
                + KEY_USER_PASSWORD + " TEXT NOT NULL,"
                + KEY_USER_FULL_NAME + " TEXT NOT NULL,"
                + KEY_USER_CREATED_AT + " DATETIME DEFAULT CURRENT_TIMESTAMP,"
                + KEY_USER_LAST_LOGIN + " DATETIME"
                + ")";

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

        String CREATE_TRIPS_TABLE = "CREATE TABLE " + TABLE_TRIPS + "(" +
                KEY_TRIP_ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
                KEY_TRIP_USER_ID_FK + " INTEGER NOT NULL," +
                KEY_DESTINATION + " TEXT NOT NULL," +
                KEY_START_DATE + " TEXT," +
                KEY_END_DATE + " TEXT," +
                KEY_TRAVELERS_COUNT + " INTEGER," +
                KEY_TOTAL_COST + " REAL," +
                KEY_ACTIVITIES_SELECTED + " TEXT," +
                KEY_CUSTOM_EXPENSES + " TEXT," +
                KEY_TRIP_NOTES + " TEXT," +
                KEY_HAS_LOYALTY_DISCOUNT + " INTEGER DEFAULT 0," +
                KEY_DISCOUNT_AMOUNT + " REAL DEFAULT 0," +
                KEY_TRIP_CREATED_AT + " DATETIME DEFAULT CURRENT_TIMESTAMP," +
                "FOREIGN KEY(" + KEY_TRIP_USER_ID_FK + ") REFERENCES " + TABLE_USERS + "(" + KEY_USER_ID + ")" +
                ")";
        db.execSQL(CREATE_TRIPS_TABLE);

        Log.d(TAG, "Database tables created successfully");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_TRIPS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_MEMORIES);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_USERS);
        onCreate(db);
    }

    public long createUser(String email, String password, String fullName) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();

        values.put(KEY_USER_EMAIL, email.trim().toLowerCase());
        values.put(KEY_USER_PASSWORD, password);
        values.put(KEY_USER_FULL_NAME, fullName.trim());

        long userId = db.insert(TABLE_USERS, null, values);
        db.close();

        return userId;
    }

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

        }

        cursor.close();
        db.close();

        return user;
    }

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

    private void updateLastLogin(int userId) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(KEY_USER_LAST_LOGIN, System.currentTimeMillis());

        db.update(TABLE_USERS, values, KEY_USER_ID + "=?", new String[]{String.valueOf(userId)});
        db.close();
    }

    public long addMemory(int userId, String title, String textContent, String imagePath,
                          String location, String backgroundMusic) {
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

        return memoryId;
    }

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

    public boolean deleteMemory(int memoryId, int userId) {
        SQLiteDatabase db = this.getWritableDatabase();

        String selection = KEY_MEMORY_ID + "=? AND " + KEY_USER_ID_FK + "=?";
        String[] selectionArgs = {String.valueOf(memoryId), String.valueOf(userId)};

        int rowsAffected = db.delete(TABLE_MEMORIES, selection, selectionArgs);
        db.close();

        return rowsAffected > 0;
    }

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
        dump.append("\n");

        // Dump Trips Table
        dump.append("--- ").append(TABLE_TRIPS).append(" Table ---\n");
        Cursor tripCursor = db.rawQuery("SELECT * FROM " + TABLE_TRIPS, null);
        if (tripCursor.moveToFirst()) {
            String[] columnNames = tripCursor.getColumnNames();
            dump.append(String.join(" | ", columnNames)).append("\n");
            do {
                for (int i = 0; i < tripCursor.getColumnCount(); i++) {
                    dump.append(tripCursor.getString(i)).append(i == tripCursor.getColumnCount() - 1 ? "" : " | ");
                }
                dump.append("\n");
            } while (tripCursor.moveToNext());
        } else {
            dump.append("No data in ").append(TABLE_TRIPS).append(" table.\n");
        }
        tripCursor.close();

        db.close();
        return dump.toString();
    }
    public long addTrip(int userId, String destination, String startDate, String endDate,
                         int travelersCount, double totalCost, String activitiesSelected,
                         String customExpenses, String notes, boolean hasLoyaltyDiscount,
                         double discountAmount) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(KEY_TRIP_USER_ID_FK, userId);
        values.put(KEY_DESTINATION, destination);
        values.put(KEY_START_DATE, startDate);
        values.put(KEY_END_DATE, endDate);
        values.put(KEY_TRAVELERS_COUNT, travelersCount);
        values.put(KEY_TOTAL_COST, totalCost);
        values.put(KEY_ACTIVITIES_SELECTED, activitiesSelected);
        values.put(KEY_CUSTOM_EXPENSES, customExpenses);
        values.put(KEY_TRIP_NOTES, notes);
        values.put(KEY_HAS_LOYALTY_DISCOUNT, hasLoyaltyDiscount ? 1 : 0);
        values.put(KEY_DISCOUNT_AMOUNT, discountAmount);

        long id = db.insert(TABLE_TRIPS, null, values);
        db.close();
        return id;
    }

    public Trip getMostRecentTripObject(int userId) {
        SQLiteDatabase db = this.getReadableDatabase();
        Trip trip = null;
        String query = "SELECT * FROM " + TABLE_TRIPS + " WHERE " + KEY_TRIP_USER_ID_FK + "=? ORDER BY " + KEY_TRIP_ID + " DESC LIMIT 1";
        try (Cursor c = db.rawQuery(query, new String[]{String.valueOf(userId)})) {
            if (c.moveToFirst()) {
                String destination = c.getString(c.getColumnIndexOrThrow(KEY_DESTINATION));
                String startDate = c.getString(c.getColumnIndexOrThrow(KEY_START_DATE));
                String endDate = c.getString(c.getColumnIndexOrThrow(KEY_END_DATE));
                int travelersCount = c.getInt(c.getColumnIndexOrThrow(KEY_TRAVELERS_COUNT));
                String activities = c.getString(c.getColumnIndexOrThrow(KEY_ACTIVITIES_SELECTED));
                String customExpenses = c.getString(c.getColumnIndexOrThrow(KEY_CUSTOM_EXPENSES));
                String notes = c.getString(c.getColumnIndexOrThrow(KEY_TRIP_NOTES));

                trip = new Trip(destination, startDate, endDate, travelersCount, activities, customExpenses, notes);
            }
        } catch (Exception ignored) {
        } finally {
            db.close();
        }
        return trip;
    }
}
