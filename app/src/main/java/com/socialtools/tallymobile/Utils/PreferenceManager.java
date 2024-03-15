package com.socialtools.tallymobile.Utils;

import android.content.Context;
import android.content.SharedPreferences;

public class PreferenceManager {

    private final SharedPreferences sharedPreferences;

    private static final String KEY_DARK_MODE = "dark_mode";
    private static final String KEY_THEME_MODE = "theme_mode";

    public PreferenceManager(Context context){
        sharedPreferences= context.getSharedPreferences(
                Constants.KEY_PREFERENCE_NAME,Context.MODE_PRIVATE);

    }

    public void  putBoolean(String key, Boolean value){
        SharedPreferences.Editor editor= sharedPreferences.edit();
        editor.putBoolean(key,value);
        editor.apply();
    }
    public Boolean getBoolean(String key){
        return sharedPreferences.getBoolean(key,false);

    }

    public void setThemeMode(int themeMode) {
        putInt(KEY_THEME_MODE, themeMode);
    }

    public int getThemeMode() {
        return getInt(KEY_THEME_MODE, Constants.THEME_SYSTEM_DEFAULT);
    }

    // Other methods in your class...

    public void putInt(String key, int value) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putInt(key, value);
        editor.apply();
    }

    public int getInt(String key, int defaultValue) {
        return sharedPreferences.getInt(key, defaultValue);
    }

    public  void putString (String key, String value){
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(key,value);
        editor.apply();
    }
    public  String getString(String key){
        return sharedPreferences.getString(key,null);

    }



    public void clear(){
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.clear();
        editor.apply();
    }

}
