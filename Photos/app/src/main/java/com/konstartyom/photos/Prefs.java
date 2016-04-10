package com.konstartyom.photos;

import android.os.Bundle;
import android.preference.PreferenceActivity;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;

import java.util.List;

public class Prefs extends PreferenceActivity{
    @Override
    public void onCreate(Bundle savedInstanceState){
        ThemeLoader.loadAppropriateTheme(this, ThemeLoader.PREF_THEME);
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onBuildHeaders(List<Header> target) {
        super.onBuildHeaders(target);
        loadHeadersFromResource(R.xml.preference_headers, target);
    }

    @Override
    protected boolean isValidFragment(String fragmentName) {
        return Prefs1Fragment.class.getName().equals(fragmentName) ||
                PrefsCacheFragment.class.getName().equals(fragmentName);
    }

    public static class Prefs1Fragment extends PreferenceFragment {
        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);

            PreferenceManager.setDefaultValues(getActivity(),
                    R.xml.preferences, false);

            addPreferencesFromResource(R.xml.preferences);
        }
    }

    public static class PrefsCacheFragment extends PreferenceFragment {
        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);

            PreferenceManager.setDefaultValues(getActivity(),
                    R.xml.preferences_cache, false);

            addPreferencesFromResource(R.xml.preferences_cache);
        }
    }

}
