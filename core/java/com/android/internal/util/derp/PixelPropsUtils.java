/*
 * Copyright (C) 2022 The Pixel Experience Project
 *               2021-2022 crDroid Android Project
 *           (C) 2023 ArrowOS
 *           (C) 2023 The LibreMobileOS Foundation
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.android.internal.util.derp;

import android.app.Application;
import android.content.Context;
import android.content.res.Configuration;
import android.os.Build;
import android.os.SystemProperties;
import android.util.Log;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * @hide
 */
public final class PixelPropsUtils {

    private static final String TAG = PixelPropsUtils.class.getSimpleName();
    private static final String DEVICE = "ro.product.device";
    private static final boolean DEBUG = false;

    private static final Map<String, Object> propsToChangeGeneric;
    private static final Map<String, Object> propsToChangePixel5a;
    private static final Map<String, Object> propsToChangePixel9ProXL;
    private static final Map<String, Object> propsToChangePixelTablet;
    private static final Map<String, Object> propsToChangePixelXL;
    private static final Map<String, ArrayList<String>> propsToKeep;

    private static final String[] pTensorCodenames = {
            "comet",
            "komodo",
            "caiman",
            "tokay",
            "akita",
            "husky",
            "shiba",
            "felix",
            "tangorpro",
            "lynx",
            "cheetah",
            "panther",
            "bluejay",
            "oriole",
            "raven"
    };

    // Packages to Spoof as Pixel 8 Pro
    private static final String[] packagesToChangePixel9ProXL = {
            "com.google.android.apps.aiwallpapers",
            "com.google.android.apps.bard",
            "com.google.android.apps.customization.pixel",
            "com.google.android.apps.emojiwallpaper",
            "com.google.android.apps.nexuslauncher",
            "com.google.android.apps.privacy.wildlife",
            "com.google.android.apps.wallpaper",
            "com.google.android.apps.wallpaper.pixel",
            "com.google.android.googlequicksearchbox",
            "com.google.android.wallpaper.effects",
            "com.google.pixel.livewallpaper"
    };

    // Packages to Spoof as Pixel 5a
    private static final String[] packagesToChangePixel5a = {
            "com.android.chrome",
            "com.android.vending",
            "com.breel.wallpapers20",
            "com.nhs.online.nhsonline",
            "com.netflix.mediaclient"
    };

    // Packages to Keep with original device
    private static final String[] packagesToKeep = {
            "com.google.android.MTCL83",
            "com.google.android.UltraCVM",
            "com.google.android.apps.cameralite",
            "com.google.android.apps.dreamlinerupdater",
            "com.google.android.apps.dreamliner",
            "com.google.android.apps.miphone.aiai.AiaiApplication",
            "com.google.android.apps.motionsense.bridge",
            "com.google.android.apps.pixelmigrate",
            "com.google.android.apps.recorder",
            "com.google.android.apps.restore",
            "com.google.android.apps.subscriptions.red",
            "com.google.android.apps.tachyon",
            "com.google.android.apps.tips",
            "com.google.android.apps.tycho",
            "com.google.android.apps.wearables.maestro.companion",
            "com.google.android.apps.youtube.kids",
            "com.google.android.apps.youtube.music",
            "com.google.android.as",
            "com.google.android.backup",
            "com.google.android.backuptransport",
            "com.google.android.dialer",
            "com.google.android.euicc",
            "com.google.android.setupwizard",
            "com.google.android.youtube",
            "com.google.ar.core",
            "com.google.intelligence.sense",
            "com.google.oslo"
    };

    private static volatile boolean sIsFinsky = false;
    private static volatile boolean sIsExcluded = false;

    static {
        propsToKeep = new HashMap<>();
        propsToKeep.put("com.google.android.settings.intelligence", new ArrayList<>(Collections.singletonList("FINGERPRINT")));
        propsToChangeGeneric = new HashMap<>();
        propsToChangeGeneric.put("TYPE", "user");
        propsToChangeGeneric.put("TAGS", "release-keys");
        propsToChangePixel9ProXL = new HashMap<>();
        propsToChangePixel9ProXL.put("BRAND", "google");
        propsToChangePixel9ProXL.put("MANUFACTURER", "Google");
        propsToChangePixel9ProXL.put("DEVICE", "komodo");
        propsToChangePixel9ProXL.put("PRODUCT", "komodo");
        propsToChangePixel9ProXL.put("HARDWARE", "komodo");
        propsToChangePixel9ProXL.put("MODEL", "Pixel 9 Pro XL");
        propsToChangePixel9ProXL.put("ID", "AD1A.240905.004");
        propsToChangePixel9ProXL.put("FINGERPRINT", "google/komodo/komodo:14/AD1A.240905.004/12196292:user/release-keys");
        propsToChangePixelTablet = new HashMap<>();
        propsToChangePixelTablet.put("BRAND", "google");
        propsToChangePixelTablet.put("MANUFACTURER", "Google");
        propsToChangePixelTablet.put("DEVICE", "tangorpro");
        propsToChangePixelTablet.put("PRODUCT", "tangorpro");
        propsToChangePixelTablet.put("HARDWARE", "tangorpro");
        propsToChangePixelTablet.put("MODEL", "Pixel Tablet");
        propsToChangePixelTablet.put("ID", "AP2A.240905.003");
        propsToChangePixelTablet.put("FINGERPRINT", "google/tangorpro/tangorpro:14/AP2A.240905.003/12231197:user/release-keys");
        propsToChangePixel5a = new HashMap<>();
        propsToChangePixel5a.put("BRAND", "google");
        propsToChangePixel5a.put("MANUFACTURER", "Google");
        propsToChangePixel5a.put("DEVICE", "barbet");
        propsToChangePixel5a.put("PRODUCT", "barbet");
        propsToChangePixel5a.put("HARDWARE", "barbet");
        propsToChangePixel5a.put("MODEL", "Pixel 5a");
        propsToChangePixel5a.put("ID", "AP2A.240805.005");
        propsToChangePixel5a.put("FINGERPRINT", "google/barbet/barbet:14/AP2A.240805.005/12025142:user/release-keys");
        propsToChangePixelXL = new HashMap<>();
        propsToChangePixelXL.put("BRAND", "google");
        propsToChangePixelXL.put("MANUFACTURER", "Google");
        propsToChangePixelXL.put("DEVICE", "marlin");
        propsToChangePixelXL.put("PRODUCT", "marlin");
        propsToChangePixelXL.put("HARDWARE", "marlin");
        propsToChangePixelXL.put("MODEL", "Pixel XL");
        propsToChangePixelXL.put("ID", "QP1A.191005.007.A3");
        propsToChangePixelXL.put("FINGERPRINT", "google/marlin/marlin:10/QP1A.191005.007.A3/5972272:user/release-keys");
    }

    public static void setProps(Context context) {
        propsToChangeGeneric.forEach((k, v) -> setPropValue(k, v));

        final String packageName = context.getPackageName();
        if (packageName == null || packageName.isEmpty()) {
            return;
        }
        if (packageName.startsWith("com.google.")
                || packageName.startsWith("com.samsung.")
                || Arrays.asList(packagesToChangePixel9ProXL).contains(packageName)
                || Arrays.asList(packagesToChangePixel5a).contains(packageName)) {

            if (Arrays.asList(packagesToKeep).contains(packageName) ||
                    packageName.startsWith("com.google.android.GoogleCamera")) {
                sIsExcluded = true;
                return;
            }

            Map<String, Object> propsToChange = new HashMap<>();

            if (packageName.equals("com.google.android.apps.photos")) {
                if (SystemProperties.getBoolean("persist.sys.pixelprops.gphotos", true)) {
                    propsToChange.putAll(propsToChangePixelXL);
                }
            } else if (packageName.equals("com.android.vending")) {
                sIsFinsky = true;
                propsToChange.putAll(propsToChangePixel5a);
            } else if (packageName.equals("com.google.android.gms")) {
                setPropValue("TIME", System.currentTimeMillis());
                final String processName = Application.getProcessName().toLowerCase();
                if (processName.contains("unstable")) {
                    spoofBuildGms();
                    return;
                }
                boolean isTensorDevice = Arrays.asList(pTensorCodenames).contains(SystemProperties.get(DEVICE));
                if (!isTensorDevice && (processName.contains("gservice")
                        || processName.contains("learning")
                        || processName.contains("persistent"))) {
                    propsToChange.putAll(propsToChangePixel5a);
                } else if (isDeviceTablet(context.getApplicationContext())) {
                    propsToChange.putAll(propsToChangePixelTablet);
                } else {
                    propsToChange.putAll(propsToChangePixel9ProXL);
                }
            } else if (Arrays.asList(packagesToChangePixel9ProXL).contains(packageName)) {
                if (isDeviceTablet(context.getApplicationContext())) {
                    propsToChange.putAll(propsToChangePixelTablet);
                } else {
                    propsToChange.putAll(propsToChangePixel9ProXL);
                }
            } else {
                propsToChange.putAll(propsToChangePixel5a);
            }

            if (DEBUG) Log.d(TAG, "Defining props for: " + packageName);
            for (Map.Entry<String, Object> prop : propsToChange.entrySet()) {
                String key = prop.getKey();
                Object value = prop.getValue();
                if (propsToKeep.containsKey(packageName) && propsToKeep.get(packageName).contains(key)) {
                    if (DEBUG) Log.d(TAG, "Not defining " + key + " prop for: " + packageName);
                    continue;
                }
                if (DEBUG) Log.d(TAG, "Defining " + key + " prop for: " + packageName);
                setPropValue(key, value);
            }
            // Set proper indexing fingerprint
            if (packageName.equals("com.google.android.settings.intelligence")) {
                setPropValue("FINGERPRINT", Build.VERSION.INCREMENTAL);
            }
        }
    }

    private static boolean isDeviceTablet(Context context) {
        if (context == null) {
            return false;
        }
        Configuration config = context.getResources().getConfiguration();
        boolean isTablet = (config.smallestScreenWidthDp >= 600);
        return isTablet;
    }

    private static void setPropValue(String key, Object value) {
        setPropValue(key, value.toString());
    }

    private static void setPropValue(String key, String value) {
        try {
            if (DEBUG) Log.d(TAG, "Defining prop " + key + " to " + value);
            Class clazz = Build.class;
            if (key.startsWith("VERSION.")) {
                clazz = Build.VERSION.class;
                key = key.substring(8);
            }
            Field field = clazz.getDeclaredField(key);
            field.setAccessible(true);
            // Cast the value to int if it's an integer field, otherwise string.
            field.set(null, field.getType().equals(Integer.TYPE) ? Integer.parseInt(value) : value);
            field.setAccessible(false);
        } catch (Exception e) {
            Log.e(TAG, "Failed to set prop " + key, e);
        }
    }

    private static void spoofBuildGms() {
        // Alter build parameters to avoid hardware attestation enforcement
        setPropValue("MANUFACTURER", "Google");
        setPropValue("MODEL", "Pixel 9");
        setPropValue("FINGERPRINT", "google/tokay_beta/tokay:Baklava/BP21.241121.009/12787338:user/release-keys");
        setPropValue("BRAND", "google");
        setPropValue("PRODUCT", "tokay_beta");
        setPropValue("DEVICE", "tokay");
        setPropValue("VERSION.RELEASE", "Baklava");
        setPropValue("ID", "BP21.241121.009");
        setPropValue("VERSION.INCREMENTAL", "12787338");
        setPropValue("TYPE", "user");
        setPropValue("TAGS", "release-keys");
        setPropValue("VERSION.SECURITY_PATCH", "2024-12-05");
        setPropValue("VERSION.DEVICE_INITIAL_SDK_INT", "34");
    }

    private static boolean isCallerSafetyNet() {
        return Arrays.stream(Thread.currentThread().getStackTrace())
                        .anyMatch(elem -> elem.getClassName().toLowerCase()
                            .contains("droidguard"));
    }

    public static void onEngineGetCertificateChain() {
        // Check stack for SafetyNet or Play Integrity
        if ((isCallerSafetyNet() || sIsFinsky) && !sIsExcluded) {
            Log.i(TAG, "Blocked key attestation");
            throw new UnsupportedOperationException();
        }
    }
}
