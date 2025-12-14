#!/bin/bash

# SystemUI Modifications Implementation Script for AOSPMods Features
# This script helps implement the required SystemUI changes for AOSPMods-inspired features

set -e

echo "=== AOSPMods SystemUI Implementation Script ==="
echo "This script will help implement SystemUI modifications for LineageOS"
echo ""

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# Function to print colored output
print_status() {
    echo -e "${GREEN}[INFO]${NC} $1"
}

print_warning() {
    echo -e "${YELLOW}[WARN]${NC} $1"
}

print_error() {
    echo -e "${RED}[ERROR]${NC} $1"
}

print_step() {
    echo -e "${BLUE}[STEP]${NC} $1"
}

# Check if we're in the right directory
if [ ! -d "frameworks/base" ] || [ ! -d "packages/apps/Settings" ]; then
    print_error "Please run this script from the root of your Android source tree"
    exit 1
fi

print_status "Android source tree detected. Starting implementation..."

# Step 1: Add settings keys to Settings.java
print_step "Step 1: Adding settings keys to Settings.java"

SETTINGS_FILE="frameworks/base/core/java/android/provider/Settings.java"
if [ -f "$SETTINGS_FILE" ]; then
    print_status "Found Settings.java at $SETTINGS_FILE"

    # Create backup
    cp "$SETTINGS_FILE" "${SETTINGS_FILE}.backup"
    print_status "Created backup: ${SETTINGS_FILE}.backup"

    # Add the new settings keys
    cat >> "$SETTINGS_FILE" << 'EOF2'

// AOSPMods-inspired SystemUI Settings
public static final String STATUS_BAR_HEIGHT_FACTOR = "status_bar_height_factor";
public static final String NOTIFICATION_ICON_LIMIT = "notification_icon_limit";
public static final String COMBINED_SIGNAL_ICONS = "combined_signal_icons";
public static final String HIDE_ROAMING_STATE = "hide_roaming_state";
public static final String VOLTE_ICON_ENABLED = "volte_icon_enabled";
public static final String VOWIFI_ICON_ENABLED = "vowifi_icon_enabled";
public static final String HIDE_PRIVACY_CHIP = "hide_privacy_chip";
public static final String SYSTEM_ICONS_MULTI_ROW = "system_icons_multi_row";
public static final String NOTIFICATION_AREA_MULTI_ROW = "notification_area_multi_row";
public static final String NETWORK_ON_SB_ENABLED = "network_on_sb_enabled";

// Quick Settings
public static final String QS_VOLUME_UNMUTE_PERCENTAGE = "qs_volume_unmute_percentage";
public static final String LEVELED_FLASHLIGHT_TILE = "leveled_flashlight_tile";
public static final String FLASHLIGHT_LEVEL_GLOBAL = "flashlight_level_global";
public static final String QS_TILE_LABEL_SCALE = "qs_tile_label_scale";
public static final String QS_SECONDARY_LABEL_SCALE = "qs_secondary_label_scale";
public static final String QS_PULLDOWN_ENABLED = "qs_pulldown_enabled";
public static final String QS_PULLDOWN_PERCENTAGE = "qs_pulldown_percentage";
public static final String QS_PULLDOWN_SIDE = "qs_pulldown_side";
public static final String ONE_FINGER_PULLUP_ENABLED = "one_finger_pullup_enabled";
EOF2

    print_status "Added settings keys to Settings.java"
else
    print_error "Settings.java not found at $SETTINGS_FILE"
    exit 1
fi

# Step 2: Create SystemUI utility classes
print_step "Step 2: Creating SystemUI utility classes"

SYSTEMUI_SRC_DIR="frameworks/base/packages/SystemUI/src/com/android/systemui"
AOSPMODS_UTILS_DIR="$SYSTEMUI_SRC_DIR/aospmods"

if [ ! -d "$AOSPMODS_UTILS_DIR" ]; then
    mkdir -p "$AOSPMODS_UTILS_DIR"
    print_status "Created AOSPMods utils directory: $AOSPMODS_UTILS_DIR"
fi

# Create AOSPMods settings utility
cat > "$AOSPMODS_UTILS_DIR/AOSPModsSettings.java" << 'EOF2'
package com.android.systemui.aospmods;

import android.content.Context;
import android.provider.Settings;

import javax.inject.Inject;
import javax.inject.Singleton;

/**
 * Utility class for reading AOSPMods-related settings
 */
@Singleton
public class AOSPModsSettings {

    private final Context mContext;

    @Inject
    public AOSPModsSettings(Context context) {
        mContext = context;
    }

    // Status Bar Settings
    public int getStatusBarHeightFactor() {
        return Settings.System.getInt(mContext.getContentResolver(),
                Settings.System.STATUS_BAR_HEIGHT_FACTOR, 100);
    }

    public int getNotificationIconLimit() {
        return Settings.System.getInt(mContext.getContentResolver(),
                Settings.System.NOTIFICATION_ICON_LIMIT, 4);
    }

    public boolean isCombinedSignalIconsEnabled() {
        return Settings.System.getInt(mContext.getContentResolver(),
                Settings.System.COMBINED_SIGNAL_ICONS, 0) == 1;
    }

    public boolean isRoamingStateHidden() {
        return Settings.System.getInt(mContext.getContentResolver(),
                Settings.System.HIDE_ROAMING_STATE, 0) == 1;
    }

    public boolean isVolteIconEnabled() {
        return Settings.System.getInt(mContext.getContentResolver(),
                Settings.System.VOLTE_ICON_ENABLED, 0) == 1;
    }

    public boolean isVowifiIconEnabled() {
        return Settings.System.getInt(mContext.getContentResolver(),
                Settings.System.VOWIFI_ICON_ENABLED, 0) == 1;
    }

    public boolean isPrivacyChipHidden() {
        return Settings.System.getInt(mContext.getContentResolver(),
                Settings.System.HIDE_PRIVACY_CHIP, 0) == 1;
    }

    public boolean isSystemIconsMultiRow() {
        return Settings.System.getInt(mContext.getContentResolver(),
                Settings.System.SYSTEM_ICONS_MULTI_ROW, 0) == 1;
    }

    public boolean isNotificationAreaMultiRow() {
        return Settings.System.getInt(mContext.getContentResolver(),
                Settings.System.NOTIFICATION_AREA_MULTI_ROW, 0) == 1;
    }

    public boolean isNetworkOnSBEnabled() {
        return Settings.System.getInt(mContext.getContentResolver(),
                Settings.System.NETWORK_ON_SB_ENABLED, 0) == 1;
    }

    // Quick Settings
    public int getQsVolumeUnmutePercentage() {
        return Settings.System.getInt(mContext.getContentResolver(),
                Settings.System.QS_VOLUME_UNMUTE_PERCENTAGE, 50);
    }

    public boolean isLeveledFlashlightEnabled() {
        return Settings.System.getInt(mContext.getContentResolver(),
                Settings.System.LEVELED_FLASHLIGHT_TILE, 0) == 1;
    }

    public boolean isFlashlightLevelGlobal() {
        return Settings.System.getInt(mContext.getContentResolver(),
                Settings.System.FLASHLIGHT_LEVEL_GLOBAL, 0) == 1;
    }

    public int getQsTileLabelScale() {
        return Settings.System.getInt(mContext.getContentResolver(),
                Settings.System.QS_TILE_LABEL_SCALE, 0);
    }

    public int getQsSecondaryLabelScale() {
        return Settings.System.getInt(mContext.getContentResolver(),
                Settings.System.QS_SECONDARY_LABEL_SCALE, 0);
    }

    public boolean isQsPulldownEnabled() {
        return Settings.System.getInt(mContext.getContentResolver(),
                Settings.System.QS_PULLDOWN_ENABLED, 0) == 1;
    }

    public int getQsPulldownPercentage() {
        return Settings.System.getInt(mContext.getContentResolver(),
                Settings.System.QS_PULLDOWN_PERCENTAGE, 25);
    }

    public String getQsPulldownSide() {
        return Settings.System.getString(mContext.getContentResolver(),
                Settings.System.QS_PULLDOWN_SIDE);
    }

    public boolean isOneFingerPullupEnabled() {
        return Settings.System.getInt(mContext.getContentResolver(),
                Settings.System.ONE_FINGER_PULLUP_ENABLED, 0) == 1;
    }
}
EOF2

print_status "Created AOSPModsSettings utility class"

# Step 3: Create NetworkSpeedController
print_step "Step 3: Creating NetworkSpeedController"

cat > "$SYSTEMUI_SRC_DIR/statusbar/NetworkSpeedController.java" << 'EOF2'
package com.android.systemui.statusbar;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkCapabilities;
import android.net.TrafficStats;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;

import com.android.systemui.aospmods.AOSPModsSettings;
import com.android.systemui.dagger.qualifiers.Background;

import javax.inject.Inject;
import javax.inject.Singleton;

/**
 * Controller for network speed monitoring and display
 */
@Singleton
public class NetworkSpeedController {

    private static final int MSG_UPDATE_SPEED = 1;
    private static final long UPDATE_INTERVAL = 1000; // 1 second

    private final Context mContext;
    private final AOSPModsSettings mSettings;
    private final Handler mHandler;
    private final ConnectivityManager mConnectivityManager;

    private Callback mCallback;
    private long mLastRxBytes = 0;
    private long mLastTxBytes = 0;
    private long mLastUpdateTime = 0;
    private boolean mEnabled = false;

    public interface Callback {
        void onNetworkSpeedUpdate(String downSpeed, String upSpeed);
    }

    @Inject
    public NetworkSpeedController(
            Context context,
            AOSPModsSettings settings,
            @Background Handler handler) {
        mContext = context;
        mSettings = settings;
        mHandler = handler;
        mConnectivityManager = context.getSystemService(ConnectivityManager.class);
    }

    public void setCallback(Callback callback) {
        mCallback = callback;
    }

    public void setEnabled(boolean enabled) {
        if (mEnabled != enabled) {
            mEnabled = enabled;
            if (enabled) {
                startMonitoring();
            } else {
                stopMonitoring();
            }
        }
    }

    private void startMonitoring() {
        mLastRxBytes = TrafficStats.getTotalRxBytes();
        mLastTxBytes = TrafficStats.getTotalTxBytes();
        mLastUpdateTime = System.currentTimeMillis();
        mHandler.sendEmptyMessage(MSG_UPDATE_SPEED);
    }

    private void stopMonitoring() {
        mHandler.removeMessages(MSG_UPDATE_SPEED);
    }

    private void updateNetworkSpeed() {
        if (!mEnabled || mCallback == null) return;

        long currentRxBytes = TrafficStats.getTotalRxBytes();
        long currentTxBytes = TrafficStats.getTotalTxBytes();
        long currentTime = System.currentTimeMillis();

        long timeDiff = currentTime - mLastUpdateTime;
        if (timeDiff == 0) return;

        long rxDiff = currentRxBytes - mLastRxBytes;
        long txDiff = currentTxBytes - mLastTxBytes;

        // Calculate speeds in KB/s
        double downSpeedKB = (rxDiff * 1000.0) / (timeDiff * 1024.0);
        double upSpeedKB = (txDiff * 1000.0) / (timeDiff * 1024.0);

        String downSpeed = formatSpeed(downSpeedKB);
        String upSpeed = formatSpeed(upSpeedKB);

        mCallback.onNetworkSpeedUpdate(downSpeed, upSpeed);

        mLastRxBytes = currentRxBytes;
        mLastTxBytes = currentTxBytes;
        mLastUpdateTime = currentTime;

        // Schedule next update
        mHandler.sendEmptyMessageDelayed(MSG_UPDATE_SPEED, UPDATE_INTERVAL);
    }

    private String formatSpeed(double speedKB) {
        if (speedKB < 1024) {
            return String.format("%.1fK", speedKB);
        } else {
            return String.format("%.1fM", speedKB / 1024.0);
        }
    }

    public void handleMessage(Message msg) {
        if (msg.what == MSG_UPDATE_SPEED) {
            updateNetworkSpeed();
        }
    }
}
EOF2

print_status "Created NetworkSpeedController"

print_status "=== Implementation Script Complete ==="
print_status ""
print_status "Summary of changes made:"
print_status "✓ Added settings keys to Settings.java"
print_status "✓ Created AOSPModsSettings utility class"
print_status "✓ Created NetworkSpeedController"
print_status ""
print_warning "Manual modifications needed:"
print_warning "1. Modify StatusBar.java for height control"
print_warning "2. Modify StatusBarIconController.java for icon limiting"
print_warning "3. Modify QSPanel.java for label scaling and gestures"
print_warning "4. Modify VolumeTile.java for unmute percentage"
print_warning "5. Modify FlashlightTile.java for leveled control"
print_warning "6. Update SystemUI dependency injection"
print_warning ""
print_status "Use the detailed prompt in systemui_modifications_prompt.txt for Cursor AI guidance"
