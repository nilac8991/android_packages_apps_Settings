/*
 * Copyright (C) 2017 Xperia Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.android.settings.xosp;

import android.content.ContentResolver;
import android.content.Context;
import android.content.res.Resources;
import android.os.Bundle;
import android.os.RemoteException;
import android.os.Handler;
import android.os.UserHandle;
import android.os.UserManager;
import android.provider.SearchIndexableResource;
import android.provider.Settings;
import android.support.v7.preference.Preference;
import android.support.v7.preference.ListPreference;
import android.support.v7.preference.Preference.OnPreferenceChangeListener;
import android.support.v7.preference.PreferenceCategory;
import android.support.v7.preference.PreferenceScreen;
import android.support.v14.preference.SwitchPreference;
import android.util.Log;
import android.text.TextUtils;

import com.android.settings.R;
import com.android.settings.search.BaseSearchIndexProvider;
import com.android.settings.search.Indexable;
import com.android.settings.SettingsPreferenceFragment;
import com.android.settings.Utils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import com.android.internal.logging.MetricsProto.MetricsEvent;

//import com.android.internal.util.xosp.RebornUtils;

public class ButtonsPersonalizations extends SettingsPreferenceFragment implements
        OnPreferenceChangeListener {

    private static final String KEY_NAVIGATION_BAR = "navigation_bar";
    private static final String EMPTY_STRING = "";

    private Handler mHandler;

    private SwitchPreference mNavigationBar;

    private int mDeviceHardwareKeys;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        addPreferencesFromResource(R.xml.xosp_buttons_cat);

        mHandler = new Handler();

        final Resources res = getActivity().getResources();
        final ContentResolver resolver = getActivity().getContentResolver();
        final PreferenceScreen prefScreen = getPreferenceScreen();

        mDeviceHardwareKeys = res.getInteger(
                com.android.internal.R.integer.config_deviceHardwareKeys);

        /* Navigation Bar */
        mNavigationBar = (SwitchPreference) findPreference(KEY_NAVIGATION_BAR);
        if (mNavigationBar != null) {
            if (mDeviceHardwareKeys != 0) {
                mNavigationBar.setOnPreferenceChangeListener(this);
            } else {
                mNavigationBar = null;
                removePreference(KEY_NAVIGATION_BAR);
            }
        }
    }

    @Override
    protected int getMetricsCategory() {
        return MetricsEvent.XOSP;
    }

    private boolean handleOnPreferenceTreeClick(Preference preference) {
        if (preference != null && preference == mNavigationBar) {
            mNavigationBar.setEnabled(false);
            mHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    mNavigationBar.setEnabled(true);
                }
            }, 1000);
            return true;
        }
        return false;
    }

    private String getSystemPreferenceString(Preference preference) {
        if (preference == null) {
            return EMPTY_STRING;
        } else if (preference == mNavigationBar) {
            return Settings.System.NAVIGATION_BAR_ENABLED;
        }

        return EMPTY_STRING;
    }

    @Override
    public void onResume() {
        super.onResume();
        reload();
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        final boolean handled = handleOnPreferenceChange(preference, newValue);
        if (handled) {
            reload();
        }
        return handled;
    }

    @Override
    public boolean onPreferenceTreeClick(Preference preference) {
        final boolean handled = handleOnPreferenceTreeClick(preference);
        // return super.onPreferenceTreeClick(preferenceScreen, preference);
        return handled;
    }

    private void reload() {
        final ContentResolver resolver = getActivity().getContentResolver();
        final Resources res = getActivity().getResources();

        final boolean defaultToNavigationBar = res.getBoolean(com.android.internal.R.bool.config_defaultToNavigationBar);
        final boolean navigationBarEnabled = Settings.System.getIntForUser(resolver,
                Settings.System.NAVIGATION_BAR_ENABLED, defaultToNavigationBar ? 1 : 0, UserHandle.USER_CURRENT) != 0;

        if (mNavigationBar != null) {
            mNavigationBar.setChecked(navigationBarEnabled);
        }
    }

    private boolean handleOnPreferenceChange(Preference preference, Object newValue) {
        final String setting = getSystemPreferenceString(preference);

        if (TextUtils.isEmpty(setting)) {
            // No system setting.
            return false;
        }

        if (preference != null && preference instanceof SwitchPreference) {
            boolean state = false;
            if (newValue instanceof Boolean) {
                state = (Boolean) newValue;
            } else if (newValue instanceof String) {
                state = Integer.valueOf((String) newValue) != 0;
            }
            Settings.System.putIntForUser(getContentResolver(), setting, state ? 1 : 0,
                    UserHandle.USER_CURRENT);
        }

        return true;
    }
}
