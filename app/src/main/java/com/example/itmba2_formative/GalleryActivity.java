package com.example.itmba2_formative;

import static com.example.itmba2_formative.HelperMethods.showToast;

import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.widget.TextView;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.itmba2_formative.objects.Memory;
import java.util.ArrayList;
import java.util.List;

public class GalleryActivity extends BaseActivity {

    private RecyclerView rvGallery;
    private TextView tvBackHome;
    private GalleryAdapter adapter;
    private DatabaseHelper dbHelper;
    private SessionManager sessionManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gallery);

        dbHelper = DatabaseHelper.getInstance(this);
        sessionManager = new SessionManager(this);

        // Handle window insets for edge-to-edge display
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.gallery_coordinator), (view, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            view.setPaddingRelative(
                    systemBars.left,
                    systemBars.top,
                    systemBars.right,
                    systemBars.bottom
            );
            return insets;
        });

        initializeViews();
        setupGalleryGrid();
        setupClickListeners();
        loadUserMemories();
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadUserMemories();
    }

    private void initializeViews() {
        rvGallery = findViewById(R.id.rv_gallery);
        tvBackHome = findViewById(R.id.tv_back_home);
    }

    private void setupGalleryGrid() {
        int spanCount = calculateOptimalSpanCount();
        GridLayoutManager layoutManager = new GridLayoutManager(this, spanCount);
        rvGallery.setLayoutManager(layoutManager);

        // Initialize adapter with empty list
        adapter = new GalleryAdapter(new ArrayList<>(), this::onPhotoClick);
        rvGallery.setAdapter(adapter);
    }

    private void loadUserMemories() {
        int userId = getCurrentUserId();

        List<Memory> memories = new ArrayList<>();

        try (Cursor cursor = dbHelper.getUserMemories(userId)) {

            if (cursor != null && cursor.moveToFirst()) {
                do {
                    // Extract memory data from cursor
                    int id = cursor.getInt(cursor.getColumnIndexOrThrow("id"));
                    String title = cursor.getString(cursor.getColumnIndexOrThrow("title"));
                    String description = cursor.getString(cursor.getColumnIndexOrThrow("description"));
                    String photoUriString = cursor.getString(cursor.getColumnIndexOrThrow("photo_uri"));
                    String musicUriString = cursor.getString(cursor.getColumnIndexOrThrow("music_uri"));
                    String location = cursor.getString(cursor.getColumnIndexOrThrow("location"));

                    // Initialize uris safely
                    Uri photoUri = null;
                    Uri musicUri = null;

                    if (photoUriString != null && !photoUriString.isEmpty()) {
                        try {
                            photoUri = Uri.parse(photoUriString);
                        } catch (Exception e) {
                            showToast(this, "Invalid photo URI for memory ID " + id);
                        }
                    }

                    if (musicUriString != null && !musicUriString.isEmpty()) {
                        try {
                            musicUri = Uri.parse(musicUriString);
                        } catch (Exception e) {
                            showToast(this, "Invalid music URI for memory ID " + id);
                        }
                    }

                    // Only add memories that have photos (since this is a gallery)
                    if (photoUri != null) {
                        Memory memory = new Memory(id, title, description, photoUri, musicUri, location);
                        memories.add(memory);
                    }

                } while (cursor.moveToNext());
            }

        } catch (Exception e) {
            showToast(this, "Error loading memories: " + e.getMessage());
        }

        adapter.updateMemories(memories);

        if (memories.isEmpty()) {
            showToast(this, "No photo memories found. Create some memories first!");
        }
    }

    private int getCurrentUserId() {
        // Try to get user ID from session manager
        if (sessionManager.isLoggedIn()) {
            return sessionManager.getLoggedInUser().getUserId();
        }

        // Fallback to SharedPreferences
        SharedPreferences prefs = getSharedPreferences("user_prefs", MODE_PRIVATE);
        return prefs.getInt("user_id", -1);
    }

    private int calculateOptimalSpanCount() {
        // Calculate span count based on screen width
        float screenWidth = getResources().getDisplayMetrics().widthPixels;
        float cardWidth = getResources().getDimensionPixelSize(R.dimen.gallery_card_width);
        return Math.max(2, Math.round(screenWidth / cardWidth));
    }

    private void setupClickListeners() {
        tvBackHome.setOnClickListener(v -> finish());
    }

    private void onPhotoClick(Memory memory) {
        Intent intent = new Intent(this, PhotoViewerActivity.class);

        // Pass memory data to photo viewer
        intent.putExtra("memory_id", memory.getMemoryId());
        if (memory.getPhotoUri() != null) {
            intent.putExtra("music_uri", memory.getMusicUri().toString());
            intent.putExtra("photo_uri", memory.getPhotoUri().toString());
        }
        intent.putExtra("title", memory.getTitle());
        intent.putExtra("description", memory.getDescription());
        intent.putExtra("location", memory.getLocation());

        startActivity(intent);
    }
}
