/*
 * Copyright (C) 2022 The Nameless-AOSP Project
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

package com.android.internal.util.derp;

import android.content.ContentResolver;
import android.content.Context;
import android.database.ContentObserver;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.UserHandle;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.provider.Settings;

import java.util.ArrayList;

public class VibratorHelper {

    private final Context mContext;
    private final Vibrator mVibrator;

    private final ArrayList<String> mSettings = new ArrayList<>();

    private boolean mHapticFeedbackEnabled;
    private boolean mRegistered = false;

    private final ContentObserver mSettingsObserver = new ContentObserver(Handler.getMain()) {
        @Override
        public void onChange(boolean selfChange) {
            updateHapticFeedbackEnabled();
        }
    };

    private boolean getBoolean(ContentResolver cr, String setting) {
        return Settings.System.getIntForUser(cr,
                setting, 1, UserHandle.USER_CURRENT) != 0;
    }

    private boolean getAllSettings(ContentResolver cr) {
        if (mSettings.size() == 0) {
            return true;
        }
        for (String setting : mSettings) {
            if (!getBoolean(cr, setting)) {
                return false;
            }
        }
        return true;
    }

    private void updateHapticFeedbackEnabled() {
        if (!mRegistered) {
            return;
        }
        mHapticFeedbackEnabled = getAllSettings(mContext.getContentResolver());
    }

    public VibratorHelper(Context context, String... settings) {
        this(context, false, settings);
    }

    public VibratorHelper(Context context, boolean observe, String... settings) {
        mContext = context;
        mVibrator = context.getSystemService(Vibrator.class);

        for (String setting : settings) {
            mSettings.add(setting);
        }

        if (observe) {
            startObserving();
        } else {
            mSettingsObserver.onChange(false);
        }        
    }

    public void startObserving() {
        if (mRegistered) {
            return;
        }
        final ContentResolver cr = mContext.getContentResolver();
        for (String setting : mSettings) {
            cr.registerContentObserver(
                    Settings.System.getUriFor(setting),
                    true, mSettingsObserver);
        }
        mRegistered = true;
        mSettingsObserver.onChange(false);
    }

    public void stopObserving() {
        if (mRegistered && mSettings.size() != 0) {
            mContext.getContentResolver().unregisterContentObserver(mSettingsObserver);
            mRegistered = false;
        }
    }

    public void vibrateForDuration(final int duration) {
        vibrateForDuration(duration, false);
    }

    public void vibrateForDuration(final int duration, final boolean ignoreSettings) {
        final boolean shouldVibrate = ignoreSettings ||
                (mRegistered ? mHapticFeedbackEnabled : getAllSettings(mContext.getContentResolver()));
        if (shouldVibrate) {
            AsyncTask.execute(() ->
                    mVibrator.vibrate(VibrationEffect.createOneShot(duration, VibrationEffect.DEFAULT_AMPLITUDE)));
        }
    }

    public void vibrateForEffectId(final int duration) {
        vibrateForEffectId(duration, false);
    }

    public void vibrateForEffectId(final int effectId, final boolean ignoreSettings) {
        final boolean shouldVibrate = ignoreSettings ||
                (mRegistered ? mHapticFeedbackEnabled : getAllSettings(mContext.getContentResolver()));
        if (shouldVibrate) {
            AsyncTask.execute(() ->
                    mVibrator.vibrate(VibrationEffect.createPredefined(effectId)));
        }
    }
}
