/*
 * Copyright (C) 2015 The Dirty Unicorns Project
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

package com.android.systemui.qs.tiles;

import android.content.Context;
import android.content.ComponentName;
import android.content.Intent;
import android.os.Handler;
import android.os.Looper;
import android.service.quicksettings.Tile;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.Nullable;

import com.android.internal.logging.MetricsLogger;
import com.android.internal.logging.nano.MetricsProto.MetricsEvent;
import com.android.internal.util.derp.derpUtils;
import com.android.systemui.SysUIToast;
import com.android.systemui.R;
import com.android.systemui.dagger.qualifiers.Background;
import com.android.systemui.dagger.qualifiers.Main;
import com.android.systemui.plugins.ActivityStarter;
import com.android.systemui.plugins.FalsingManager;
import com.android.systemui.plugins.qs.QSIconView;
import com.android.systemui.plugins.qs.QSTile;
import com.android.systemui.plugins.qs.QSTile.State;
import com.android.systemui.plugins.statusbar.StatusBarStateController;
import com.android.systemui.qs.QSHost;
import com.android.systemui.qs.QsEventLogger;
import com.android.systemui.qs.logging.QSLogger;
import com.android.systemui.qs.tileimpl.QSTileImpl;
import com.android.systemui.statusbar.policy.KeyguardStateController;

import javax.inject.Inject;

public class DerpSpaceTile extends SecureQSTile<State> {

    public static final String TILE_SPEC = "derpspace";

    private boolean mListening;
    private final ActivityStarter mActivityStarter;

    private static final String TAG = "DerpSpaceTile";

    private static final String DERPSPACE_PKG_NAME = "com.android.settings";
    private static final String OTA_PKG_NAME = "org.lineageos.updater";

    private static final Intent DERPSPACE_INTENT = new Intent()
            .setComponent(new ComponentName(DERPSPACE_PKG_NAME,
                "com.android.settings.Settings$DerpSpaceSettingsActivity"));
    private static final Intent OTA_INTENT = new Intent()
            .setComponent(new ComponentName(OTA_PKG_NAME,
                "org.lineageos.updater.UpdatesActivity"));

    @Inject
    public DerpSpaceTile(
            QSHost host,
            QsEventLogger uiEventLogger,
            @Background Looper backgroundLooper,
            @Main Handler mainHandler,
            FalsingManager falsingManager,
            MetricsLogger metricsLogger,
            StatusBarStateController statusBarStateController,
            ActivityStarter activityStarter,
            QSLogger qsLogger,
            KeyguardStateController keyguardStateController
    ) {
        super(host, uiEventLogger, backgroundLooper, mainHandler, falsingManager, metricsLogger,
                statusBarStateController, activityStarter, qsLogger, keyguardStateController);
        mActivityStarter = activityStarter;
    }

    @Override
    public State newTileState() {
        State state = new State();
        state.handlesLongClick = isOTABundled() ? true : false;
        return state;
    }

    @Override
    protected void handleClick(@Nullable View view, boolean keyguardShowing) {
        if (checkKeyguard(view, keyguardShowing)) {
            return;
        }

        startDerpSpace();
        refreshState();
    }

    @Override
    public Intent getLongClickIntent() {
        if (isOTABundled()) {
            return OTA_INTENT;
        }
        showNotSupportedToast();
        return null;
    }

    @Override
    protected void handleSecondaryClick(@Nullable View view) {
        if (isOTABundled()) {
            startDerpFestOTA();
        }
    }

    @Override
    public CharSequence getTileLabel() {
        return mContext.getString(R.string.quick_derpspace_label);
    }

    protected void startDerpSpace() {
        mActivityStarter.postStartActivityDismissingKeyguard(DERPSPACE_INTENT, 0);
    }

    protected void startDerpFestOTA() {
        mActivityStarter.postStartActivityDismissingKeyguard(OTA_INTENT, 0);
    }

    private void showNotSupportedToast() {
        // Collapse the panels, so the user can see the toast.
        SysUIToast.makeText(mContext, mContext.getString(
                R.string.quick_derpspace_toast),
                Toast.LENGTH_LONG).show();
    }

    private boolean isOTABundled() {
        return derpUtils.isPackageAvailable(mContext, OTA_PKG_NAME);
    }

    private boolean isDERPSPACEAvailable() {
        boolean isInstalled = false;
        boolean isNotHidden = false;
        isInstalled = derpUtils.isPackageInstalled(mContext, DERPSPACE_PKG_NAME);
        isNotHidden = derpUtils.isPackageAvailable(mContext, DERPSPACE_PKG_NAME);
        return isInstalled || isNotHidden;
    }

    @Override
    public boolean isAvailable() {
        return isDERPSPACEAvailable();
    }

    @Override
    protected void handleUpdateState(State state, Object arg) {
        state.icon = ResourceIcon.get(R.drawable.ic_qs_derpspace);
        state.label = mContext.getString(R.string.quick_derpspace_label);
        state.state = Tile.STATE_ACTIVE;
    }

    @Override
    public void handleSetListening(boolean listening) {
        if (mListening == listening) return;
        mListening = listening;
    }

    @Override
    public int getMetricsCategory() {
        return MetricsEvent.DERP;
    }
}
