/*
 * Copyright (C) 2021 The Android Open Source Project
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

package com.android.settingslib;

import android.content.Context;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.provider.Settings;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.Switch;

import androidx.annotation.Keep;
import androidx.annotation.Nullable;
import androidx.annotation.VisibleForTesting;
import androidx.preference.PreferenceViewHolder;

import com.android.settingslib.RestrictedLockUtils.EnforcedAdmin;
import com.android.settingslib.core.instrumentation.SettingsJankMonitor;

/**
 * A custom preference that provides inline switch toggle. It has a mandatory field for title, and
 * optional fields for icon and sub-text. And it can be restricted by admin state.
 */
public class PrimarySwitchPreference extends RestrictedPreference {

    private Switch mSwitch;
    private boolean mChecked;
    private boolean mCheckedSet;
    private boolean mEnableSwitch = true;

    private final Context mContext;
    private final Vibrator mVibrator;

    public PrimarySwitchPreference(Context context, AttributeSet attrs,
            int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        mVibrator = (Vibrator) context.getSystemService(Context.VIBRATOR_SERVICE);
        mContext = context;
    }

    public PrimarySwitchPreference(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        mVibrator = (Vibrator) context.getSystemService(Context.VIBRATOR_SERVICE);
        mContext = context;
    }

    public PrimarySwitchPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
        mVibrator = (Vibrator) context.getSystemService(Context.VIBRATOR_SERVICE);
        mContext = context;
    }

    public PrimarySwitchPreference(Context context) {
        super(context);
        mVibrator = (Vibrator) context.getSystemService(Context.VIBRATOR_SERVICE);
        mContext = context;
    }

    @Override
    protected int getSecondTargetResId() {
        return R.layout.preference_widget_primary_switch;
    }

    @Override
    public void onBindViewHolder(PreferenceViewHolder holder) {
        super.onBindViewHolder(holder);
        mSwitch = (Switch) holder.findViewById(R.id.switchWidget);
        if (mSwitch != null) {
            mSwitch.setOnClickListener(v -> {
                if (mSwitch != null && !mSwitch.isEnabled()) {
                    return;
                }
                final boolean newChecked = !mChecked;
                if (callChangeListener(newChecked)) {
                    SettingsJankMonitor.detectToggleJank(getKey(), mSwitch);
                    setChecked(newChecked);
                    persistBoolean(newChecked);
                }
                if (Settings.System.getInt(mContext.getContentResolver(),
                        Settings.System.HAPTIC_FEEDBACK_ENABLED, 1) != 0) {
                    mVibrator.vibrate(VibrationEffect.get(VibrationEffect.EFFECT_CLICK));
                }
            });

            // Consumes move events to ignore drag actions.
            mSwitch.setOnTouchListener((v, event) -> {
                return event.getActionMasked() == MotionEvent.ACTION_MOVE;
            });

            mSwitch.setContentDescription(getTitle());
            mSwitch.setChecked(mChecked);
            mSwitch.setEnabled(mEnableSwitch);
        }
    }

    public boolean isChecked() {
        return mSwitch != null && mChecked;
    }

    /**
     * Used to validate the state of mChecked and mCheckedSet when testing, without requiring
     * that a ViewHolder be bound to the object.
     */
    @Keep
    @Nullable
    public Boolean getCheckedState() {
        return mCheckedSet ? mChecked : null;
    }

    /**
     * Set the checked status to be {@code checked}.
     *
     * @param checked The new checked status
     */
    public void setChecked(boolean checked) {
        // Always set checked the first time; don't assume the field's default of false.
        final boolean changed = mChecked != checked;
        if (changed || !mCheckedSet) {
            mChecked = checked;
            mCheckedSet = true;
            if (mSwitch != null) {
                mSwitch.setChecked(checked);
            }
        }
    }

    /**
     * Set the Switch to be the status of {@code enabled}.
     *
     * @param enabled The new enabled status
     */
    public void setSwitchEnabled(boolean enabled) {
        mEnableSwitch = enabled;
        if (mSwitch != null) {
            mSwitch.setEnabled(enabled);
        }
    }

    @VisibleForTesting(otherwise = VisibleForTesting.NONE)
    public boolean isSwitchEnabled() {
        return mEnableSwitch;
    }

    /**
     * If admin is not null, disables the switch.
     * Otherwise, keep it enabled.
     */
    public void setDisabledByAdmin(EnforcedAdmin admin) {
        super.setDisabledByAdmin(admin);
        setSwitchEnabled(admin == null);
    }

    public Switch getSwitch() {
        return mSwitch;
    }

    @Override
    protected boolean shouldHideSecondTarget() {
        return getSecondTargetResId() == 0;
    }
}
