package com.example.itmba2_formative;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.appcompat.app.AlertDialog;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

// Import SessionManager and User
import com.example.itmba2_formative.models.User;

public class PhotoViewerActivity extends BaseActivity {

    private ImageView ivPhotoViewer;
    private TextView btnCloseViewer;
    private TextView tvPhotoTitle;
    private TextView tvPhotoDescription;
    private TextView tvPhotoLocation;
    private TextView btnDeletePhoto;

    private LinearLayout llMusicPlaybackControlsViewer;
    private TextView btnPlayMusicViewer;
    private TextView btnPauseMusicViewer;

    private DatabaseHelper dbHelper;
    private int memoryId = -1;
    private int currentUserId = -1;

    private MusicManager musicManager;
    private Uri musicUri; // Specific to the memory being viewed

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_photo_viewer);

        dbHelper = DatabaseHelper.getInstance(this);
        musicManager = MusicManager.getInstance();

        SessionManager sessionManager = new SessionManager(this);
        User currentUser = sessionManager.getLoggedInUser();

        if (currentUser != null) {
            currentUserId = currentUser.getUserId();
        } else {
            HelperMethods.showToast(this, getString(R.string.error_user_not_found));
        }

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.photo_viewer_coordinator), (view, insets) -> {
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
        setupClickListeners();
        loadPhotoData();
    }

    private void initializeViews() {
        ivPhotoViewer = findViewById(R.id.iv_photo_viewer);
        btnCloseViewer = findViewById(R.id.btn_close_viewer);
        tvPhotoTitle = findViewById(R.id.tv_photo_title);
        tvPhotoDescription = findViewById(R.id.tv_photo_description);
        tvPhotoLocation = findViewById(R.id.tv_photo_location);
        btnDeletePhoto = findViewById(R.id.btn_delete_photo);

        llMusicPlaybackControlsViewer = findViewById(R.id.ll_music_playback_controls_viewer);
        btnPlayMusicViewer = findViewById(R.id.btn_play_music_viewer);
        btnPauseMusicViewer = findViewById(R.id.btn_pause_music_viewer);
    }

    private boolean isMusicGloballyEnabled() {
        return HelperMethods.getBooleanFromPreferences(this, AppConstants.PrefKeys.PREF_BACKGROUND_MUSIC, true);
    }

    private boolean canPlayMusicForThisMemory() {
        return isMusicGloballyEnabled() && musicUri != null;
    }

    private void setupClickListeners() {
        btnCloseViewer.setOnClickListener(v -> finish());
        btnDeletePhoto.setOnClickListener(v -> {
            if (memoryId != -1 && currentUserId != -1) {
                showDeleteConfirmationDialog();
            } else {
                HelperMethods.showToast(this, getString(R.string.error_memory_or_user_id_not_found));
            }
        });

        btnPlayMusicViewer.setOnClickListener(v -> {
            if (canPlayMusicForThisMemory() && musicManager != null) {
                if (!musicManager.isPrepared() || (musicManager.getCurrentTrackUri() != null && !musicManager.getCurrentTrackUri().equals(musicUri)) ) {
                    musicManager.initializePlayer(PhotoViewerActivity.this, musicUri);
                    musicManager.setOnPreparedCallback(() -> {
                        if (canPlayMusicForThisMemory()) { // Re-check in case preference changed
                            musicManager.playMusic();
                            updateMusicPlaybackControls(true);
                        }
                    });
                } else {
                    musicManager.playMusic();
                    updateMusicPlaybackControls(true);
                }
            } else {
                updateMusicPlaybackControls(false); // Ensure controls are hidden if music not allowed
            }
        });

        btnPauseMusicViewer.setOnClickListener(v -> {
            if (musicManager != null && musicManager.isPlaying()) {
                musicManager.pauseMusic();
                updateMusicPlaybackControls(false);
            }
        });
    }

    private void showDeleteConfirmationDialog() {
        new AlertDialog.Builder(this)
                .setTitle(R.string.delete_memory_dialog_title)
                .setMessage(R.string.delete_memory_dialog_message)
                .setPositiveButton(R.string.delete, (dialog, which) -> deletePhotoFromDatabase())
                .setNegativeButton(R.string.action_cancel, null)
                .show();
    }

    private void deletePhotoFromDatabase() {
        boolean deleted = dbHelper.deleteMemory(memoryId, currentUserId);
        if (deleted) {
            HelperMethods.showToast(this, getString(R.string.memory_deleted_successfully));
            Intent resultIntent = new Intent();
            resultIntent.putExtra("memory_deleted_id", memoryId);
            setResult(RESULT_OK, resultIntent);
            finish();
        } else {
            HelperMethods.showToast(this, getString(R.string.memory_delete_failed));
        }
    }

    @SuppressLint("SetTextI18n")
    private void loadPhotoData() {
        Intent intent = getIntent();
        if (intent != null) {
            memoryId = intent.getIntExtra("memory_id", -1);
            String photoUriString = intent.getStringExtra("photo_uri");
            String title = intent.getStringExtra("title");
            String description = intent.getStringExtra("description");
            String location = intent.getStringExtra("location");
            String musicUriString = intent.getStringExtra("music_uri");

            if (photoUriString != null) {
                try {
                    Uri photoUri = Uri.parse(photoUriString);
                    if (ivPhotoViewer != null) {
                        ivPhotoViewer.setImageURI(photoUri);
                    }
                } catch (Exception e) {
                    HelperMethods.showToast(this, getString(R.string.loading_photo_error));
                }
            }

            tvPhotoTitle.setText(title != null && !title.isEmpty() ? title : getString(R.string.untitled_memory));
            tvPhotoDescription.setText(description != null && !description.isEmpty() ? description : getString(R.string.no_description));
            if (location != null && !location.isEmpty()) {
                tvPhotoLocation.setText("📍 " + location);
                tvPhotoLocation.setVisibility(View.VISIBLE);
            } else {
                tvPhotoLocation.setVisibility(View.GONE);
            }

            if (musicUriString != null && !musicUriString.isEmpty()) {
                try {
                    musicUri = Uri.parse(musicUriString);
                } catch (Exception e) {
                    musicUri = null; // Ensure musicUri is null if parsing fails
                    HelperMethods.showToast(this, getString(R.string.loading_music_error));
                }
            } else {
                musicUri = null;
            }

            if (canPlayMusicForThisMemory()) {
                // Auto-play logic only if music is allowed for this memory
                if (musicManager.getCurrentTrackUri() == null || !musicManager.getCurrentTrackUri().equals(musicUri) || !musicManager.isPrepared()) {
                    musicManager.initializePlayer(this, musicUri);
                    musicManager.setOnPreparedCallback(() -> {
                        if (musicManager.isPrepared() && canPlayMusicForThisMemory()) { // Re-check
                            musicManager.playMusic();
                            updateMusicPlaybackControls(true);
                        }
                    });
                } else if (musicManager.isPrepared() && !musicManager.isPlaying()) {
                    musicManager.playMusic();
                    updateMusicPlaybackControls(true);
                } else {
                     // Covers case where music is already playing (e.g. on rotation) or prepared but paused.
                    updateMusicPlaybackControls(musicManager.isPlaying());
                }
            } else {
                // Music not allowed or no music URI for this memory
                if (musicManager != null && musicUri != null && musicManager.getCurrentTrackUri() != null && musicManager.getCurrentTrackUri().equals(musicUri) && musicManager.isPlaying()) {
                    musicManager.pauseMusic(); // Pause if it was playing for this specific memory
                }
                updateMusicPlaybackControls(false); // This will hide controls
            }

            btnDeletePhoto.setEnabled(memoryId != -1 && currentUserId != -1);

        } else {
            HelperMethods.showToast(this, getString(R.string.error_no_data_received));
            finish();
        }
    }

    private void updateMusicPlaybackControls(boolean isPlaying) {
        if (!canPlayMusicForThisMemory()) {
            llMusicPlaybackControlsViewer.setVisibility(View.GONE);
            return;
        }

        llMusicPlaybackControlsViewer.setVisibility(View.VISIBLE);
        if (isPlaying) {
            btnPlayMusicViewer.setVisibility(View.GONE);
            btnPauseMusicViewer.setVisibility(View.VISIBLE);
        } else {
            btnPlayMusicViewer.setVisibility(View.VISIBLE);
            btnPauseMusicViewer.setVisibility(View.GONE);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        // Pause music only if it's currently playing for *this* memory's track
        if (musicManager != null && musicUri != null &&
            musicManager.getCurrentTrackUri() != null &&
            musicManager.getCurrentTrackUri().equals(musicUri) &&
            musicManager.isPlaying() && !isFinishing()) {
            musicManager.pauseMusic();
            updateMusicPlaybackControls(false);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (canPlayMusicForThisMemory() && musicManager != null) {
            // If the player is prepared for the current track and was paused, resume it.
            if (musicManager.isPrepared() &&
                musicManager.getCurrentTrackUri() != null &&
                musicManager.getCurrentTrackUri().equals(musicUri) &&
                !musicManager.isPlaying()) {
                musicManager.playMusic();
                updateMusicPlaybackControls(true);
            } else if (musicManager.isPlaying() &&
                       musicManager.getCurrentTrackUri() != null &&
                       musicManager.getCurrentTrackUri().equals(musicUri)) {
                updateMusicPlaybackControls(true); // Ensure controls are correct if already playing our track
            } else if (musicManager.getCurrentTrackUri() == null || !musicManager.getCurrentTrackUri().equals(musicUri) || !musicManager.isPrepared()){
                // If a different track is loaded, or not prepared for our track,
                // loadPhotoData's auto-play logic might re-initialize if needed, or user can press play.
                // For now, just ensure controls are in a consistent state (e.g., play shown).
                updateMusicPlaybackControls(false);
            } else {
                 updateMusicPlaybackControls(false); // Default to paused state if not actively playing our track
            }
        } else {
            // Music not allowed for this memory, ensure controls are hidden and music is paused if it's ours
             if (musicManager != null && musicUri != null &&
                musicManager.getCurrentTrackUri() != null &&
                musicManager.getCurrentTrackUri().equals(musicUri) &&
                musicManager.isPlaying()) {
                musicManager.pauseMusic();
            }
            updateMusicPlaybackControls(false);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Release player only if it was playing the track for this specific PhotoViewerActivity instance
        // or if the activity is finishing (to ensure cleanup).
        if (musicManager != null) {
            if (musicUri != null && musicManager.getCurrentTrackUri() != null && musicUri.equals(musicManager.getCurrentTrackUri())) {
                 musicManager.releasePlayer(); // Release if it's our track
            } else if (isFinishing() && musicManager.getCurrentTrackUri() == null){
                // If finishing and no specific track is associated with musicManager (e.g. it was never initialized for this viewer)
                // it might be an edge case, but generally, releasePlayer in onDestroy is for the *current* context.
                // If another activity is managing a different track, this could be problematic.
                // A better approach would be for MusicManager to perhaps have a ref count or more sophisticated track management if used globally.
                // For this activity, only release if it's "our" player instance/track.
                // The current MusicManager is a singleton, so releasing it here might affect other parts if not handled carefully.
                // Let's assume for now the intention is to stop/release the specific track associated with this viewer.
                // The existing logic already checks musicUri.equals(musicManager.getCurrentTrackUri())
                // If it's finishing and it IS our track, it's covered by the first condition.
                // If it's finishing and it's NOT our track, we should NOT release it here.
            }
             // If we are finishing and the music manager is playing *our* track, the first condition handles it.
            // If we are finishing and the music manager is playing *another* track, we should not release it.
            // If we are finishing and the music manager is not playing, but holds *our* track, first condition handles it.
            // The `else if (isFinishing())` in original code was too broad.
            // We only care about releasing if it's *our* music that needs cleanup.
        }
    }
}
