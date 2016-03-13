package com.konstartyom.photos;

import android.app.Activity;
import android.preference.PreferenceManager;

public class ThemeLoader {
    public static final int PREF_THEME = 1;
    public static final int MAIN_THEME = 0;

    public static String loadAppropriateTheme(Activity activity, int type){
        String theme = getTheme(activity);
        switch(type) {
            case PREF_THEME:
                if (theme.equals("dark")) {
                    activity.setTheme(R.style.PrefThemeDark);
                } else {
                    activity.setTheme(R.style.PrefTheme);
                }
                break;
            default:
                if(theme.equals("dark")) {
                    activity.setTheme(R.style.AppThemeDark);
                } else{
                    activity.setTheme(R.style.AppTheme);
                }
        }
        return theme;
    }

    public static String getTheme(Activity activity){
        return PreferenceManager.getDefaultSharedPreferences(activity)
                .getString("pref_theme", "light");
    }
}
