package com.example.itmba2_formative;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import android.content.SharedPreferences;
import androidx.activity.OnBackPressedCallback;
import com.example.itmba2_formative.objects.Memory;
import android.media.MediaMetadataRetriever;
import android.graphics.BitmapFactory;
import android.graphics.Bitmap;
import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Build;
import androidx.core.content.ContextCompat;
import java.util.ArrayList;
import java.util.List;

public class MemoryActivity extends BaseActivity {
    private LinearLayout llMusicPlaceholder, llSelectedMusic, llMusicControls, llPhotoPlaceholder, llSelectedPhoto;
    private TextView tvPlayMusic, tvPauseMusic, tvStopMusic, tvTrackTitle, tvTrackArtist, tvRemoveTrack, tvBackHome, tvRemovePhoto;
    private MusicManager musicManager;
    private DatabaseHelper dbHelper;
    private ImageView ivMemoryPhoto, ivAlbumArt;
    private Uri selectedPhotoUri;
    private EditText etMemoryTitle, etMemoryDescription, etMemoryLocation;
    private Button btnSaveDraft, btnCreateMemory;
    private boolean isPickingPhoto = false, isPickingMusic = false;

    private final ActivityResultLauncher<Intent> pickMusicLauncher = registerForActivityResult(
        new ActivityResultContracts.StartActivityForResult(),
        result -> {
            if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                Uri musicUri = result.getData().getData();
                if (musicUri != null) {
                    handleMusicSelection(musicUri);
                }
            }
        }
    );

    private final ActivityResultLauncher<Intent> pickPhotoLauncher = registerForActivityResult(
        new ActivityResultContracts.StartActivityForResult(),
        result -> {
            if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                Uri photoUri = result.getData().getData();
                if (photoUri != null) {
                    handlePhotoSelection(photoUri);
                }
            }
        }
    );

    private final ActivityResultLauncher<String[]> requestPermissionLauncher =
        registerForActivityResult(new ActivityResultContracts.RequestMultiplePermissions(),
            permissions -> {
                boolean allGranted = true;
                for (Boolean isGranted : permissions.values()) {
                    if (!isGranted) {
                        allGranted = false;
                        break;
                    }
                }
                if (allGranted) {
                    if (isPickingPhoto) {
                        launchPhotoPicker();
                    } else if (isPickingMusic) {
                        launchMusicPicker();
                    }
                } else {
                    showToast("Permissions are required to select media files");
                }
                isPickingPhoto = false;
                isPickingMusic = false;
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_memory);

        dbHelper = DatabaseHelper.getInstance(this);
        musicManager = MusicManager.getInstance();
        initializeViews();
        setupClickListeners();

        // Handle back navigation
        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                if (hasUnsavedChanges()) {
                    new AlertDialog.Builder(MemoryActivity.this)
                        .setTitle(R.string.unsaved_changes)
                        .setMessage(R.string.save_draft_prompt)
                        .setPositiveButton(R.string.save_draft, (dialog, which) -> saveMemory(true))
                        .setNegativeButton(R.string.action_discard, (dialog, which) -> finish())
                        .setNeutralButton(R.string.action_cancel, (dialog, which) -> dialog.dismiss())
                        .show();
                } else {
                    finish();
                }
            }
        });
    }

    private void initializeViews() {
        llMusicPlaceholder = findViewById(R.id.ll_music_placeholder);
        llSelectedMusic = findViewById(R.id.ll_selected_music);
        llMusicControls = findViewById(R.id.ll_music_controls);
        tvPlayMusic = findViewById(R.id.tv_music_play);
        tvPauseMusic = findViewById(R.id.tv_music_pause);
        tvStopMusic = findViewById(R.id.btn_music_stop);
        tvTrackTitle = findViewById(R.id.tv_track_title);
        tvTrackArtist = findViewById(R.id.tv_track_artist);
        tvRemoveTrack = findViewById(R.id.btn_remove_track);
        tvBackHome = findViewById(R.id.tv_back_home);

        llPhotoPlaceholder = findViewById(R.id.ll_photo_placeholder);
        llSelectedPhoto = findViewById(R.id.ll_selected_photo);
        ivMemoryPhoto = findViewById(R.id.iv_memory_photo);
        tvRemovePhoto = findViewById(R.id.btn_remove_photo);

        etMemoryTitle = findViewById(R.id.et_memory_title);
        etMemoryDescription = findViewById(R.id.et_memory_description);
        etMemoryLocation = findViewById(R.id.et_memory_location);
        btnSaveDraft = findViewById(R.id.btn_save_draft);
        btnCreateMemory = findViewById(R.id.btn_create_memory);

        ivAlbumArt = findViewById(R.id.iv_album_art);
    }

    private void setupClickListeners() {
        llMusicPlaceholder.setOnClickListener(v -> openMusicPicker());

        tvPlayMusic.setOnClickListener(v -> {
            musicManager.playMusic();
            updatePlaybackControls(true);
        });

        tvPauseMusic.setOnClickListener(v -> {
            musicManager.pauseMusic();
            updatePlaybackControls(false);
        });

        tvStopMusic.setOnClickListener(v -> {
            musicManager.stopMusic();
            updatePlaybackControls(false);
        });

        tvRemoveTrack.setOnClickListener(v -> removeSelectedMusic());

        tvBackHome.setOnClickListener(v -> {
            // Cleanup music
            musicManager.releasePlayer();
            finish();
        });

        llPhotoPlaceholder.setOnClickListener(v -> openPhotoPicker());
        tvRemovePhoto.setOnClickListener(v -> removeSelectedPhoto());

        btnSaveDraft.setOnClickListener(v -> saveMemory(true));
        btnCreateMemory.setOnClickListener(v -> saveMemory(false));
    }

    private void openPhotoPicker() {
        isPickingPhoto = true;
        if (checkAndRequestPermissions()) {
            launchPhotoPicker();
        }
    }

    private void openMusicPicker() {
        isPickingMusic = true;
        if (checkAndRequestPermissions()) {
            launchMusicPicker();
        }
    }

    private boolean checkAndRequestPermissions() {
        List<String> permissions = getStrings();

        List<String> permissionsToRequest = new ArrayList<>();
        for (String permission : permissions) {
            if (ContextCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED) {
                permissionsToRequest.add(permission);
            }
        }

        if (!permissionsToRequest.isEmpty()) {
            requestPermissionLauncher.launch(permissionsToRequest.toArray(new String[0]));
            return false;
        }

        return true;
    }

    @NonNull
    private static List<String> getStrings() {
        List<String> permissions = new ArrayList<>();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            permissions.add(Manifest.permission.READ_MEDIA_IMAGES);
            permissions.add(Manifest.permission.READ_MEDIA_AUDIO);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) { // Android 14+
                permissions.add(Manifest.permission.READ_MEDIA_VISUAL_USER_SELECTED);
            }
        } else {
            // For Android versions below 13
            permissions.add(Manifest.permission.READ_EXTERNAL_STORAGE);
        }
        return permissions;
    }

    private void launchPhotoPicker() {
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("image/*");
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        intent.addFlags(Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION);
        pickPhotoLauncher.launch(intent);
    }

    private void launchMusicPicker() {
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("audio/*");
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        intent.addFlags(Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION);
        pickMusicLauncher.launch(intent);
    }

    private void handleMusicSelection(Uri musicUri) {
        // Update UI to show selected track
        llMusicPlaceholder.setVisibility(View.GONE);
        llSelectedMusic.setVisibility(View.VISIBLE);
        llMusicControls.setVisibility(View.VISIBLE);

        // Initialize music player
        musicManager.initializePlayer(this, musicUri);
        musicManager.setOnPreparedCallback(() -> {
            tvPlayMusic.setEnabled(true);
            // Auto-play when track is ready
            musicManager.playMusic();
            updatePlaybackControls(true);
        });

        // Get track metadata and album art
        try (MediaMetadataRetriever retriever = new MediaMetadataRetriever()) {
            retriever.setDataSource(this, musicUri);

            // Get track title and artist from metadata
            String title = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_TITLE);
            String artist = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ARTIST);

            // Set track info, use fallbacks if metadata is not available
            tvTrackTitle.setText(title != null ? title : musicUri.getLastPathSegment());
            tvTrackArtist.setText(artist != null ? artist : getString(R.string.unknown_artist));

            // Get and set album art
            byte[] albumArtData = retriever.getEmbeddedPicture();
            if (albumArtData != null) {
                Bitmap bitmap = BitmapFactory.decodeByteArray(albumArtData, 0, albumArtData.length);
                ivAlbumArt.setImageBitmap(bitmap);
            } else {
                // Set default album art background
                ivAlbumArt.setImageResource(R.color.primary_color);
            }
        } catch (Exception e) {
            tvTrackTitle.setText(musicUri.getLastPathSegment());
            tvTrackArtist.setText(R.string.unknown_artist);
            ivAlbumArt.setImageResource(R.color.primary_color);
        }
    }

    private void handlePhotoSelection(Uri photoUri) {
        selectedPhotoUri = photoUri;

        // Update UI
        llPhotoPlaceholder.setVisibility(View.GONE);
        llSelectedPhoto.setVisibility(View.VISIBLE);

        // Load the selected image into the ImageView
        ivMemoryPhoto.setImageURI(photoUri);
    }

    private void removeSelectedMusic() {
        musicManager.releasePlayer();

        llMusicPlaceholder.setVisibility(View.VISIBLE);
        llSelectedMusic.setVisibility(View.GONE);
        llMusicControls.setVisibility(View.GONE);

        // Reset album art
        ivAlbumArt.setImageResource(R.color.primary_color);
    }

    private void removeSelectedPhoto() {
        selectedPhotoUri = null;
        ivMemoryPhoto.setImageDrawable(null);

        llPhotoPlaceholder.setVisibility(View.VISIBLE);
        llSelectedPhoto.setVisibility(View.GONE);
    }

    private void updatePlaybackControls(boolean isPlaying) {
        tvPlayMusic.setVisibility(isPlaying ? View.GONE : View.VISIBLE);
        tvPauseMusic.setVisibility(isPlaying ? View.VISIBLE : View.GONE);
    }

    private void saveMemory(boolean asDraft) {

        // Validate required fields if not saving as draft
        if (!asDraft) {
            if (etMemoryTitle.getText().toString().trim().isEmpty()) {
                etMemoryTitle.setError(getString(R.string.title_required));
                return;
            }
            if (selectedPhotoUri == null) {
                showToast(getString(R.string.photo_required));
                return;
            }
        }

        // Memory object
        Memory memory = new Memory(
            0,
            etMemoryTitle.getText().toString().trim(),
            etMemoryDescription.getText().toString().trim(),
            selectedPhotoUri,
            musicManager.getCurrentTrackUri(),
            etMemoryLocation.getText().toString().trim()
        );

        boolean saved = saveMemoryToDatabase(memory);

        if (saved) {
            showToast(getString(asDraft ? R.string.save_draft_success : R.string.save_success));
            finish();
        } else {
            showToast(getString(R.string.save_error));
        }
    }

    private boolean saveMemoryToDatabase(Memory memory) {
        SharedPreferences prefs = getSharedPreferences("user_prefs", MODE_PRIVATE);
        int userId = prefs.getInt("user_id", 1);

        // Take persistent permissions for photo
        if (memory.getPhotoUri() != null) {
            try {
                final int takeFlags = Intent.FLAG_GRANT_READ_URI_PERMISSION;
                getContentResolver().takePersistableUriPermission(memory.getPhotoUri(), takeFlags);
            } catch (SecurityException e) {
                showToast(getString(R.string.permission_denied_photo_uri) + ": " + e.getMessage());
                return false;
            }
        }

        // Take persistent permissions for music
        if (memory.getMusicUri() != null) {
            try {
                final int takeFlags = Intent.FLAG_GRANT_READ_URI_PERMISSION;
                getContentResolver().takePersistableUriPermission(memory.getMusicUri(), takeFlags);
            } catch (SecurityException e) {
                showToast(getString(R.string.permission_denied_music_uri) + ": " + e.getMessage());
                return false;
            }
        }

        long result = dbHelper.addMemory(userId,
            memory.getTitle(),
            memory.getDescription(),
            memory.getPhotoUri() != null ? memory.getPhotoUri().toString() : null,
            memory.getLocation(),
                // unused
                // unused
            memory.getMusicUri() != null ? memory.getMusicUri().toString() : null);
        return result > 0;
    }

    private void showToast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }

    private boolean hasUnsavedChanges() {
        return !etMemoryTitle.getText().toString().trim().isEmpty() ||
               !etMemoryDescription.getText().toString().trim().isEmpty() ||
               !etMemoryLocation.getText().toString().trim().isEmpty() ||
               selectedPhotoUri != null ||
               musicManager.getCurrentTrackUri() != null;
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (isFinishing()) {
            musicManager.releasePlayer();
        } else {
            musicManager.pauseMusic();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        musicManager.releasePlayer();
    }
}
