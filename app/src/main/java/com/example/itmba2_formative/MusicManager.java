package com.example.itmba2_formative;

import android.content.Context;
import android.media.MediaPlayer;
import android.net.Uri;
import android.util.Log;
import java.io.IOException;

/**
 * Utility class to manage background music playback across the app
 */
public class MusicManager {
    private static final String TAG = "MusicManager";
    private static MusicManager instance;
    private MediaPlayer mediaPlayer;
    private Uri currentTrackUri;
    private boolean isPrepared = false;

    private MusicManager() {
        // Private constructor to enforce singleton pattern
    }

    public static synchronized MusicManager getInstance() {
        if (instance == null) {
            instance = new MusicManager();
        }
        return instance;
    }

    public void initializePlayer(Context context, Uri trackUri) {
        if (mediaPlayer != null) {
            releasePlayer();
        }

        try {
            mediaPlayer = new MediaPlayer();
            mediaPlayer.setDataSource(context, trackUri);
            mediaPlayer.setOnPreparedListener(mp -> {
                isPrepared = true;
                if (onPreparedCallback != null) {
                    onPreparedCallback.onPrepared();
                }
            });
            mediaPlayer.setOnCompletionListener(mp -> {
                if (onCompletionCallback != null) {
                    onCompletionCallback.onCompletion();
                }
            });
            mediaPlayer.setOnErrorListener((mp, what, extra) -> {
                Log.e(TAG, "MediaPlayer error: " + what + ", " + extra);
                return false;
            });
            mediaPlayer.prepareAsync();
            currentTrackUri = trackUri;
        } catch (IOException e) {
            Log.e(TAG, "Error initializing MediaPlayer", e);
        }
    }

    public void playMusic() {
        if (mediaPlayer != null && isPrepared) {
            mediaPlayer.start();
        }
    }

    public void pauseMusic() {
        if (mediaPlayer != null && mediaPlayer.isPlaying()) {
            mediaPlayer.pause();
        }
    }

    public void stopMusic() {
        if (mediaPlayer != null) {
            mediaPlayer.stop();
            isPrepared = false;
        }
    }

    public void releasePlayer() {
        if (mediaPlayer != null) {
            mediaPlayer.release();
            mediaPlayer = null;
            currentTrackUri = null;
            isPrepared = false;
        }
    }

    public Uri getCurrentTrackUri() {
        return currentTrackUri;
    }

    public boolean isPrepared() {
        return isPrepared;
    }

    // Callback interfaces
    private OnPreparedCallback onPreparedCallback;
    private OnCompletionCallback onCompletionCallback;

    public interface OnPreparedCallback {
        void onPrepared();
    }

    public interface OnCompletionCallback {
        void onCompletion();
    }

    public void setOnPreparedCallback(OnPreparedCallback callback) {
        this.onPreparedCallback = callback;
    }

    public void setOnCompletionCallback(OnCompletionCallback callback) {
        this.onCompletionCallback = callback;
    }
}
