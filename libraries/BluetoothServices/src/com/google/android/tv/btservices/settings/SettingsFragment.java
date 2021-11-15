// Copyright 2019 Google Inc. All Rights Reserved.

package com.google.android.tv.btservices.settings;

import android.app.Fragment;
import androidx.leanback.preference.LeanbackPreferenceFragment;
import androidx.leanback.preference.LeanbackSettingsFragment;
import androidx.preference.Preference;
import androidx.preference.PreferenceDialogFragment;
import androidx.preference.PreferenceFragment;
import androidx.preference.PreferenceScreen;

public class SettingsFragment extends LeanbackSettingsFragment {

    private LeanbackPreferenceFragment mPreferenceFragment;

    public static SettingsFragment newInstance(LeanbackPreferenceFragment fragment) {
        return new SettingsFragment(fragment);
    }

    public SettingsFragment() {
        this(null);
    }

    private SettingsFragment(LeanbackPreferenceFragment fragment) {
        mPreferenceFragment = fragment;
    }

    @Override
    public void onPreferenceStartInitialScreen() {
        if (mPreferenceFragment != null) {
            startPreferenceFragment(mPreferenceFragment);
        }
    }

    @Override
    public boolean onPreferenceStartFragment(PreferenceFragment caller, Preference pref) {
        final Fragment f =
                Fragment.instantiate(getActivity(), pref.getFragment(), pref.getExtras());
        f.setTargetFragment(caller, 0);
        if (f instanceof PreferenceFragment || f instanceof PreferenceDialogFragment) {
            startPreferenceFragment(f);
        } else {
            startImmersiveFragment(f);
        }
        return true;
    }

    @Override
    public boolean onPreferenceStartScreen(PreferenceFragment preferenceFragment,
            PreferenceScreen preferenceScreen) {
        return false;
    }
}
