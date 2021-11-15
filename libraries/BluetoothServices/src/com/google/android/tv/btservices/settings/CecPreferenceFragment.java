// Copyright 2019 Google Inc. All Rights Reserved.

package com.google.android.tv.btservices.settings;

import android.content.Context;
import android.os.Bundle;
import androidx.leanback.preference.LeanbackPreferenceFragment;
import androidx.preference.Preference;
import androidx.preference.PreferenceGroup;
import androidx.preference.PreferenceScreen;
import androidx.preference.SwitchPreference;
import com.google.android.tv.btservices.R;
import com.google.android.tv.btservices.PowerUtils;

public class CecPreferenceFragment extends LeanbackPreferenceFragment {

    private static final String TAG = "Atom.CecPrefFragment";
    private static final String KEY_CEC_ENABLED = "cec-enabled";

    private PreferenceGroup mPrefGroup;

    public static CecPreferenceFragment newInstance() {
        return new CecPreferenceFragment();
    }

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        final Context preferenceContext = getPreferenceManager().getContext();
        mPrefGroup = getPreferenceManager().createPreferenceScreen(preferenceContext);
        mPrefGroup.setTitle(R.string.settings_hdmi_cec);
        mPrefGroup.setOrderingAsAdded(true);

        SwitchPreference cecTogglePref = new SwitchPreference(preferenceContext);
        cecTogglePref.setTitle(R.string.settings_enable_hdmi_cec);
        final boolean isEnabled = PowerUtils.isCecControlEnabled(preferenceContext);
        cecTogglePref.setChecked(isEnabled);
        cecTogglePref.setOnPreferenceChangeListener((preference, newValue) -> {
            PowerUtils.enableCecControl(preferenceContext, ((Boolean) newValue).booleanValue());
            return true;
        });
        mPrefGroup.addPreference(cecTogglePref);

        Preference explain1Pref = new Preference(preferenceContext);
        explain1Pref.setTitle(R.string.settings_cec_explain);
        mPrefGroup.addPreference(explain1Pref);
        explain1Pref.setLayoutResource(R.layout.pref_wall_of_text);
        explain1Pref.setSelectable(false);

        Preference explain2Pref = new Preference(preferenceContext);
        explain2Pref.setTitle(R.string.settings_cec_feature_names);
        mPrefGroup.addPreference(explain2Pref);
        explain2Pref.setLayoutResource(R.layout.pref_wall_of_text);
        explain2Pref.setSelectable(false);

        setPreferenceScreen((PreferenceScreen) mPrefGroup);
    }
}
