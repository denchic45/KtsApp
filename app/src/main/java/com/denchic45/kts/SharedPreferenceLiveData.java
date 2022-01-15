package com.denchic45.kts;

import android.content.SharedPreferences;
import android.util.Log;

import androidx.lifecycle.LiveData;

public abstract class SharedPreferenceLiveData<T> extends LiveData<T> {

    public T defValue;
    SharedPreferences sharedPrefs;
    String key;
    private final SharedPreferences.OnSharedPreferenceChangeListener preferenceChangeListener = new SharedPreferences.OnSharedPreferenceChangeListener() {
        @Override
        public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
            Log.d("lol", "onSharedPreferenceChanged: " + key);
            if (SharedPreferenceLiveData.this.key.equals(key)) {
                setValue(getValue(key, defValue));
            }
        }
    };

    public SharedPreferenceLiveData(SharedPreferences prefs, String key, T defValue) {
        this.sharedPrefs = prefs;
        this.key = key;
        this.defValue = defValue;
    }

    abstract T getValue(String key, T defValue);

    @Override
    protected void onActive() {
        super.onActive();
        setValue(getValue(key, defValue));
        sharedPrefs.registerOnSharedPreferenceChangeListener(preferenceChangeListener);
    }

    @Override
    protected void onInactive() {

        sharedPrefs.unregisterOnSharedPreferenceChangeListener(preferenceChangeListener);
        super.onInactive();
    }

    public SharedPreferenceLiveData<Boolean> getLiveData(String key, Boolean defaultValue) {
        return new SharedPreferenceBooleanLiveData(sharedPrefs, key, defaultValue);
    }

    public SharedPreferenceLiveData<String> getLiveData(String key, String defaultValue) {
        return new SharedPreferenceStringLiveData(sharedPrefs, key, defaultValue);
    }

    public static class SharedPreferenceBooleanLiveData extends SharedPreferenceLiveData<Boolean> {

        public SharedPreferenceBooleanLiveData(SharedPreferences prefs, String key, Boolean defValue) {
            super(prefs, key, defValue);
        }

        @Override
        Boolean getValue(String key, Boolean defValue) {
            return sharedPrefs.getBoolean(key, defValue);
        }
    }

    public static class SharedPreferenceStringLiveData extends SharedPreferenceLiveData<String> {

        public SharedPreferenceStringLiveData(SharedPreferences prefs, String key, String defValue) {
            super(prefs, key, defValue);
        }

        @Override
        String getValue(String key, String defValue) {
            return sharedPrefs.getString(key, defValue);
        }
    }

    public static class SharedPreferenceIntegerLiveData extends SharedPreferenceLiveData<Integer> {
        public SharedPreferenceIntegerLiveData(SharedPreferences prefs, String key, int defValue) {
            super(prefs, key, defValue);
        }

        @Override
        Integer getValue(String key, Integer defValue) {
            return sharedPrefs.getInt(key, defValue);
        }
    }

    public static class SharedPreferenceLongLiveData extends SharedPreferenceLiveData<Long> {
        public SharedPreferenceLongLiveData(SharedPreferences prefs, String key, long defValue) {
            super(prefs, key, defValue);
        }

        @Override
        Long getValue(String key, Long defValue) {
            return sharedPrefs.getLong(key, defValue);
        }
    }

    @Override
    protected void finalize() throws Throwable {
        super.finalize();
        Log.d("lol", "finalize prefs: ");
    }
}
