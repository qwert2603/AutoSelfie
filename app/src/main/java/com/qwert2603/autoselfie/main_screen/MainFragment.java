package com.qwert2603.autoselfie.main_screen;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.preference.SwitchPreference;

import com.qwert2603.autoselfie.R;
import com.qwert2603.autoselfie.helpers.VkHelper;
import com.qwert2603.autoselfie.login.StartActivity;
import com.qwert2603.autoselfie.services.SelfieService;
import com.qwert2603.autoselfie.utils.LogUtils;

public class MainFragment extends PreferenceFragment implements SharedPreferences.OnSharedPreferenceChangeListener {

    private SharedPreferences mSharedPreferences;

    private String mEnabledKey;
    private String mPeriodKey;

    private SwitchPreference mSwitchPreference;
    private ListPreference mPeriodPreference;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.preferences);
        PreferenceManager.setDefaultValues(getActivity(), R.xml.preferences, false);
        mSharedPreferences = PreferenceManager.getDefaultSharedPreferences(getActivity());

        mEnabledKey = getString(R.string.preference_autoselfie_enabled);
        mPeriodKey = getString(R.string.preference_autoselfie_period);

        int[] periods = getResources().getIntArray(R.array.periods);
        String[] entries = new String[periods.length];
        String[] entryValues = new String[periods.length];
        for (int i = 0; i < periods.length; i++) {
            int period = periods[i];
            entryValues[i] = String.valueOf(period);
            entries[i] = getResources().getQuantityString(R.plurals.minutes, period, period);
        }

        mSwitchPreference = (SwitchPreference) findPreference(mEnabledKey);
        setSwitchPreferenceTitle();

        mPeriodPreference = (ListPreference) findPreference(mPeriodKey);
        mPeriodPreference.setEntries(entries);
        mPeriodPreference.setEntryValues(entryValues);
        setPeriodPreferenceSummary();

        findPreference(getString(R.string.preference_now)).setOnPreferenceClickListener(preference -> {
            SelfieService.NOW(getActivity());
            return true;
        });

        findPreference(getString(R.string.preference_log_out)).setOnPreferenceClickListener(preference -> {
            mSharedPreferences.edit().clear().apply();
            SelfieService.setNextIntentOrCancel(getActivity());
            VkHelper.logout();
            startActivity(new Intent(getActivity(), StartActivity.class));
            getActivity().finish();
            return true;
        });
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        LogUtils.d("onSharedPreferenceChanged key == " + key);
        if (key.equals(mEnabledKey)) {
            setSwitchPreferenceTitle();
        } else if (key.equals(mPeriodKey)) {
            setPeriodPreferenceSummary();
        }
        SelfieService.setNextIntentOrCancel(getActivity());
    }

    private void setSwitchPreferenceTitle() {
        boolean enabled = mSharedPreferences.getBoolean(mEnabledKey, false);
        mSwitchPreference.setTitle(getString(enabled ? R.string.autoSelfie_enabled : R.string.autoSelfie_disabled));
    }

    private void setPeriodPreferenceSummary() {
        int period = Integer.parseInt(mSharedPreferences.getString(mPeriodKey, null));
        mPeriodPreference.setSummary(getResources().getQuantityString(R.plurals.minutes, period, period));
    }

    @Override
    public void onResume() {
        super.onResume();
        mSharedPreferences.registerOnSharedPreferenceChangeListener(MainFragment.this);
    }

    @Override
    public void onPause() {
        mSharedPreferences.unregisterOnSharedPreferenceChangeListener(MainFragment.this);
        super.onPause();
    }
}
