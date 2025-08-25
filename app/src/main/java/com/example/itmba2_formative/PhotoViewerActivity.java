package com.example.itmba2_formative;

import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.viewpager2.widget.ViewPager2;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class PhotoViewerActivity extends BaseActivity {

    private ViewPager2 vpPhotoViewer;
    private TextView tvPhotoTitle, tvPhotoDescription, tvPhotoLocation, tvPhotoDate;
    private TextView btnCloseViewer, btnPlayMusic;
    private LinearLayout llTopControls, llBottomControls, llMusicControls;
    private TextView tvTrackInfo;
    private boolean isPlaying = false;
    private MusicManager musicManager;
    private Uri memoryMusicUri;
    private Uri memoryPhotoUri;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_photo_viewer);

        WindowCompat.setDecorFitsSystemWindows(getWindow(), false);

        initializeViews();
        hideSystemUI();
        setupClickListeners();
        loadMemoryDetails();

        musicManager = MusicManager.getInstance();
    }

    private void initializeViews() {
        vpPhotoViewer = findViewById(R.id.vp_photo_viewer);
        ImageView ivMainPhoto = new ImageView(this);
        ivMainPhoto.setScaleType(ImageView.ScaleType.MATRIX);
        ivMainPhoto.setAdjustViewBounds(true);

        tvPhotoTitle = findViewById(R.id.tv_photo_title);
        tvPhotoDescription = findViewById(R.id.tv_photo_description);
        tvPhotoLocation = findViewById(R.id.tv_photo_location);
        tvPhotoDate = findViewById(R.id.tv_photo_date);
        btnCloseViewer = findViewById(R.id.btn_close_viewer);
        btnPlayMusic = findViewById(R.id.btn_play_music);
        llTopControls = findViewById(R.id.ll_top_controls);
        llBottomControls = findViewById(R.id.ll_bottom_controls);
        llMusicControls = findViewById(R.id.ll_music_controls);
        tvTrackInfo = findViewById(R.id.tv_track_info);

        // Handle system insets
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.photo_viewer_coordinator), (view, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            llTopControls.setPaddingRelative(
                    systemBars.left,
                    systemBars.top,
                    systemBars.right,
                    llTopControls.getPaddingBottom()
            );
            llBottomControls.setPaddingRelative(
                    systemBars.left,
                    llBottomControls.getPaddingTop(),
                    systemBars.right,
                    systemBars.bottom
            );
            return insets;
        });
    }

    private void setupClickListeners() {
        btnCloseViewer.setOnClickListener(v -> finish());

        btnPlayMusic.setOnClickListener(v -> {
            if (memoryMusicUri != null) {
                if (!musicManager.isPrepared() || !memoryMusicUri.equals(musicManager.getCurrentTrackUri())) {
                    initializeMusic();
                } else {
                    if (isPlaying) {
                        musicManager.pauseMusic();
                        isPlaying = false;
                    } else {
                        musicManager.playMusic();
                        isPlaying = true;
                    }
                    updateMusicButton();
                }
            }
        });

        // Add click listener to photo area to toggle controls
        findViewById(R.id.photo_viewer_coordinator).setOnClickListener(v -> toggleControls());

        // Set up ViewPager2 with single photo for now
        if (vpPhotoViewer != null) {
            vpPhotoViewer.setOnClickListener(v -> toggleControls());
        }
    }

    private void toggleControls() {
        boolean isVisible = llTopControls.getVisibility() == View.VISIBLE;
        int newVisibility = isVisible ? View.GONE : View.VISIBLE;

        llTopControls.setVisibility(newVisibility);
        llBottomControls.setVisibility(newVisibility);

        // Use BaseActivity methods to control system UI
        if (isVisible) {
            hideSystemUI();
        } else {
            showSystemUI();
        }
    }

    private void initializeMusic() {
        if (memoryMusicUri == null) return;

        musicManager.initializePlayer(this, memoryMusicUri);
        musicManager.setOnPreparedCallback(() -> {
            musicManager.playMusic();
            isPlaying = true;
            updateMusicButton();
        });
        musicManager.setOnCompletionCallback(() -> {
            isPlaying = false;
            updateMusicButton();
        });
    }

    private void updateMusicButton() {
        btnPlayMusic.setText(isPlaying ? R.string.pause_icon : R.string.play_icon);
    }

    private void loadMemoryDetails() {
        long memoryId = getIntent().getLongExtra("memory_id", -1);
        if (memoryId == -1) {
            finish();
            return;
        }

        // Get memory data from intent extras
        String title = getIntent().getStringExtra("memory_title");
        String description = getIntent().getStringExtra("memory_description");
        String location = getIntent().getStringExtra("memory_location");
        String photoUriString = getIntent().getStringExtra("memory_photo_uri");
        String musicUriString = getIntent().getStringExtra("memory_music_uri");

        // Parse URIs
        if (photoUriString != null) {
            try {
                memoryPhotoUri = Uri.parse(photoUriString);
            } catch (Exception e) {
                HelperMethods.showToast(this, "Error loading photo");
                finish();
                return;
            }
        }

        if (musicUriString != null) {
            try {
                memoryMusicUri = Uri.parse(musicUriString);
            } catch (Exception e) {
                // Music URI parsing failed - continue without music
                memoryMusicUri = null;
            }
        }

        // Set memory details in UI
        tvPhotoTitle.setText(title != null && !title.isEmpty() ? title : "Untitled Memory");
        tvPhotoDescription.setText(description != null && !description.isEmpty() ? description : "");

        if (location != null && !location.isEmpty()) {
            tvPhotoLocation.setText(getString(R.string.icon_map) + " " + location);
            tvPhotoLocation.setVisibility(View.VISIBLE);
        } else {
            tvPhotoLocation.setVisibility(View.GONE);
        }

        // Format current date for display
        SimpleDateFormat dateFormat = new SimpleDateFormat("MMMM dd, yyyy", Locale.getDefault());
        tvPhotoDate.setText(dateFormat.format(new Date()));

        // Load main photo
        loadPhotoIntoViewer();

        // Setup music controls if music is available
        if (memoryMusicUri != null) {
            llMusicControls.setVisibility(View.VISIBLE);
            tvTrackInfo.setText(getString(R.string.icon_music) + " Background Music");
        } else {
            llMusicControls.setVisibility(View.GONE);
        }
    }

    private void loadPhotoIntoViewer() {
        if (memoryPhotoUri != null) {
            try {
                // For now, we'll create a simple single-photo viewer
                // Replace ViewPager2 with ImageView for simplicity
                ImageView photoView = new ImageView(this);
                photoView.setLayoutParams(vpPhotoViewer.getLayoutParams());
                photoView.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
                photoView.setAdjustViewBounds(true);
                photoView.setImageURI(memoryPhotoUri);
                photoView.setOnClickListener(v -> toggleControls());

                // Replace ViewPager2 with our ImageView
                if (vpPhotoViewer.getParent() instanceof android.view.ViewGroup) {
                    android.view.ViewGroup parent = (android.view.ViewGroup) vpPhotoViewer.getParent();
                    int index = parent.indexOfChild(vpPhotoViewer);
                    parent.removeView(vpPhotoViewer);
                    parent.addView(photoView, index);
                }
            } catch (SecurityException e) {
                HelperMethods.showToast(this, "Permission denied to access photo");
                finish();
            }
        } else {
            HelperMethods.showToast(this, "No photo to display");
            finish();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (isFinishing()) {
            musicManager.releasePlayer();
        } else if (isPlaying) {
            musicManager.pauseMusic();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (isPlaying && memoryMusicUri != null && musicManager.isPrepared()) {
            musicManager.playMusic();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        musicManager.releasePlayer();
    }

    @Override
    public void finish() {
        musicManager.releasePlayer();
        super.finish();
        overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
    }
}
