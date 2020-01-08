package com.joatsy.apps.elibrarystta.utils;

import android.content.Context;
import android.content.SharedPreferences;

public class SharedPrefs {
    SharedPreferences sp;
    SharedPreferences.Editor spEditor;

    public static final String SP_APP = "SP_APP";
    public static final String IS_LOGED_IN = "IS_LOGED_IN";
    public static final String NIM = "NIM";


    public SharedPrefs(Context context) {
        sp = context.getSharedPreferences(SP_APP, Context.MODE_PRIVATE);
        spEditor = sp.edit();
    }

    public void saveSPString(String keySP, String value) {
        spEditor.putString(keySP, value);
        spEditor.commit();
    }

    public void saveSPInt(String keySP, int value) {
        spEditor.putInt(keySP, value);
        spEditor.commit();
    }

    public void saveSPFloat(String keySP, float value) {
        spEditor.putFloat(keySP, value);
        spEditor.commit();
    }

    public void saveSPBoolean(String keySP, boolean value) {
        spEditor.putBoolean(keySP, value);
        spEditor.commit();
    }

    public void clearPref() {
        spEditor.clear();
        spEditor.commit();
        saveSPBoolean(IS_LOGED_IN, false);

    }

    public float getNIM() {
        return sp.getInt(NIM, 0);
    }

    public Boolean isLoggedIn() {
        return sp.getBoolean(IS_LOGED_IN, false);
    }


}