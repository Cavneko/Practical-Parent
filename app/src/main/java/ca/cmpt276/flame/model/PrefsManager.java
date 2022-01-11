package ca.cmpt276.flame.model;

import android.content.SharedPreferences;

import com.google.gson.Gson;

/**
 * PrefsManager is a static class that provides utility methods for getting objects
 * from SharedPreferences and persisting objects to SharedPreferences. It is used
 * by all model classes that need to be persisted.
 */
public class PrefsManager {
    private static SharedPreferences sharedPrefs;

    private PrefsManager() {
        // disallow instances of PrefsManager
    }

    public static void init(SharedPreferences sharedPrefs) {
        PrefsManager.sharedPrefs = sharedPrefs;
    }

    protected static Object restoreObj(String sharedPrefsKey, Class<?> objClass) {
        checkSharedPrefsNotNull();
        String json = sharedPrefs.getString(sharedPrefsKey, "{}");
        return (new Gson()).fromJson(json, objClass);
    }

    protected static void persistObj(String sharedPrefsKey, Object obj) {
        checkSharedPrefsNotNull();
        SharedPreferences.Editor editor = sharedPrefs.edit();
        String json = (new Gson()).toJson(obj);
        editor.putString(sharedPrefsKey, json);
        editor.apply();
    }

    protected static SharedPreferences getSharedPrefs() {
        checkSharedPrefsNotNull();
        return sharedPrefs;
    }

    private static void checkSharedPrefsNotNull() {
        if(sharedPrefs == null) {
            throw new IllegalStateException("PrefsManager requires initialization before use");
        }
    }
}
