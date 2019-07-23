package com.heiko.camera;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * 相机配置存储
 *
 * @author Heiko
 * @date 2019/7/23
 */
class CameraStore {
    private final String SP_NAME = "picture_selector_camera";
    private SharedPreferences sp;

    public CameraStore(Context context) {
        if (sp == null) {
            sp = context.getSharedPreferences(SP_NAME, Context.MODE_PRIVATE);
        }
    }

    private SharedPreferences getSp() {
        return sp;
    }

    public void putString(String key, String value) {
        getSp().edit().putString(key, value).commit();
    }

    public void putInt(String key, int value) {
        getSp().edit().putInt(key, value).apply();
    }

    public int getInt(String key, int value) {
        return getSp().getInt(key, value);
    }

    public void putLong(String key, long value) {
        getSp().edit().putLong(key, value).apply();
    }

    public long getLong(String key, long value) {
        return getSp().getLong(key, value);
    }

    public void putFloat(String key, float value) {
        getSp().edit().putFloat(key, value).apply();
    }

    public float getFloat(String key, float value) {
        return getSp().getFloat(key, value);
    }


    public String getString(String key) {
        return getSp().getString(key, "");
    }

    public String getString(String key, String def) {
        return getSp().getString(key, def);
    }

    public void putBoolean(String key, boolean value) {
        getSp().edit().putBoolean(key, value).apply();
    }

    public boolean getBoolean(String key, boolean def) {
        return getSp().getBoolean(key, def);
    }

    public void put(String key, String value) {
        getSp().edit().putString(key, value).apply();
    }
}
