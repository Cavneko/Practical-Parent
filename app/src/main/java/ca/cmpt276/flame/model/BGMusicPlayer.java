package ca.cmpt276.flame.model;

import android.content.Context;
import android.content.SharedPreferences;
import android.media.MediaPlayer;

import ca.cmpt276.flame.R;

/**
 * SoundPlayer is a static class for playing the different sounds in the application.
 * It is responsible for playing sounds which include a coin spin, peaceful background music,
 * and chirping upon timeout timer finishing
 */
public class BGMusicPlayer {
    private static final float MUSIC_VOLUME = 0.5f;
    private static MediaPlayer bgMusic;
    private static boolean isMusicEnabled = true;

    private static final String SHARED_PREFS_KEY = "is_music_enabled";
    private static SharedPreferences sharedPrefs;

    private BGMusicPlayer() {
        // static class: prevent other classes from creating new ones
    }

    public static void init(Context context) {
        if(BGMusicPlayer.sharedPrefs == null) {
            BGMusicPlayer.sharedPrefs =  PrefsManager.getSharedPrefs();
            isMusicEnabled = sharedPrefs.getBoolean(SHARED_PREFS_KEY, true);
        }

        if (bgMusic == null) {
            bgMusic = MediaPlayer.create(context, R.raw.serenity);
            bgMusic.setVolume(MUSIC_VOLUME, MUSIC_VOLUME);
            bgMusic.setLooping(true);
        }
    }

    public static void playBgMusic() {
        if (bgMusic != null) {
            bgMusic.seekTo(0);
            bgMusic.start();
        }
    }

    public static void pauseBgMusic() {
        if (bgMusic != null && bgMusic.isPlaying()) {
            bgMusic.pause();
        }
    }

    public static void resumeBgMusic() {
        if (bgMusic != null && isMusicEnabled) {
            bgMusic.start();
        }
    }

    public static boolean isMusicEnabled() {
        return isMusicEnabled;
    }

    public static void setIsMusicEnabled(boolean isMusicEnabled) {
        BGMusicPlayer.isMusicEnabled = isMusicEnabled;
        persistToSharedPrefs();
    }

    private static void persistToSharedPrefs() {
        SharedPreferences.Editor editor = sharedPrefs.edit();
        editor.putBoolean(SHARED_PREFS_KEY, isMusicEnabled);
        editor.apply();
    }
}
