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
                isPrepared = false; // Reset prepared state on error
                return false; // True if the method handled the error, false if it didn't.
            });
            mediaPlayer.prepareAsync();
            currentTrackUri = trackUri;
        } catch (IOException e) {
            Log.e(TAG, "Error initializing MediaPlayer", e);
            isPrepared = false;
        } catch (IllegalStateException e) {
            Log.e(TAG, "Error initializing MediaPlayer - Illegal State", e);
            isPrepared = false;
        }
    }

    public void playMusic() {
        if (mediaPlayer != null && isPrepared && !mediaPlayer.isPlaying()) {
            try {
                mediaPlayer.start();
            } catch (IllegalStateException e) {
                Log.e(TAG, "Error starting media player", e);
            }
        }
    }

    public void pauseMusic() {
        if (mediaPlayer != null && isPrepared && mediaPlayer.isPlaying()) {
            try {
                mediaPlayer.pause();
            } catch (IllegalStateException e) {
                Log.e(TAG, "Error pausing media player", e);
            }
        }
    }

    public void stopMusic() {
        if (mediaPlayer != null && isPrepared) {
            try {
                mediaPlayer.stop();
                isPrepared = false; // After stop, player needs to be prepared again
            } catch (IllegalStateException e) {
                Log.e(TAG, "Error stopping media player", e);
            }
        }
    }

    public void releasePlayer() {
        if (mediaPlayer != null) {
            try {
                if (mediaPlayer.isPlaying()) {
                    mediaPlayer.stop();
                }
                mediaPlayer.release();
            } catch (IllegalStateException e) {
                Log.e(TAG, "Error releasing media player", e);
            } finally {
                mediaPlayer = null;
                currentTrackUri = null;
                isPrepared = false;
            }
        }
    }

    public Uri getCurrentTrackUri() {
        return currentTrackUri;
    }

    public boolean isPrepared() {
        return isPrepared;
    }

    public boolean isPlaying() {
        if (mediaPlayer != null && isPrepared) {
            try {
                return mediaPlayer.isPlaying();
            } catch (IllegalStateException e) {
                Log.e(TAG, "Error checking if media player is playing", e);
                return false;
            }
        }
        return false;
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
