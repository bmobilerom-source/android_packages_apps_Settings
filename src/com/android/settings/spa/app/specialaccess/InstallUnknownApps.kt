/*
 * Copyright (C) 2022 The Android Open Source Project
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

package com.android.settings.spa.app.specialaccess

import android.Manifest
import android.app.AppGlobals
import android.app.AppOpsManager
import android.app.AppOpsManager.MODE_DEFAULT
import android.content.Context
import android.content.pm.ApplicationInfo
import android.content.res.Resources
import android.os.UserManager
import android.text.TextUtils
import androidx.compose.runtime.Composable
import com.android.settings.R
import com.android.settings.applications.specialaccess.InstallAppWhitelistController
import com.android.settingslib.spa.lifecycle.collectAsCallbackWithLifecycle
import com.android.settingslib.spaprivileged.model.app.AppOps
import com.android.settingslib.spaprivileged.model.app.AppOpsController
import com.android.settingslib.spaprivileged.model.app.AppRecord
import com.android.settingslib.spaprivileged.model.app.userId
import com.android.settingslib.spaprivileged.template.app.TogglePermissionAppListModel
import com.android.settingslib.spaprivileged.template.app.TogglePermissionAppListProvider
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map

object InstallUnknownAppsListProvider : TogglePermissionAppListProvider {
    override val permissionType = "InstallUnknownApps"
    override fun createModel(context: Context) = InstallUnknownAppsListModel(context)
}

data class InstallUnknownAppsRecord(
    override val app: ApplicationInfo,
    val appOpsController: AppOpsController,
) : AppRecord

class InstallUnknownAppsListModel(private val context: Context) :
    TogglePermissionAppListModel<InstallUnknownAppsRecord> {
    override val pageTitleResId = com.android.settingslib.R.string.install_other_apps
    override val switchTitleResId = R.string.external_source_switch_title
    override val footerResId = R.string.install_all_warning
    override val switchRestrictionKeys =
        listOf(
            UserManager.DISALLOW_INSTALL_UNKNOWN_SOURCES,
            UserManager.DISALLOW_INSTALL_UNKNOWN_SOURCES_GLOBALLY,
        )
    override val switchifBlockedByAdminOverrideCheckedValueTo = false
    override val enhancedConfirmationKey: String = AppOpsManager.OPSTR_REQUEST_INSTALL_PACKAGES

    override fun transformItem(app: ApplicationInfo) =
        InstallUnknownAppsRecord(
            app = app,
            appOpsController = AppOpsController(context = context, app = app, appOps = APP_OPS),
        )

    override fun filter(
        userIdFlow: Flow<Int>,
        recordListFlow: Flow<List<InstallUnknownAppsRecord>>,
    ) =
        userIdFlow.map(::getPotentialPackageNames).combine(recordListFlow) {
            potentialPackageNames,
            recordList ->
            // Show all apps, but non-whitelisted ones will be disabled via isChangeable()
            recordList.filter { record ->
                // Only show apps that are potential app sources (have REQUEST_INSTALL_PACKAGES permission)
                record.appOpsController.getMode() != MODE_DEFAULT ||
                    record.app.packageName in potentialPackageNames
            }
        }

    @Composable
    override fun isAllowed(record: InstallUnknownAppsRecord) =
        record.appOpsController.isAllowed.collectAsCallbackWithLifecycle()

    override fun isChangeable(record: InstallUnknownAppsRecord): Boolean {
        val potentialPackageNames = getPotentialPackageNames(record.app.userId)
        // First check if app is a potential app source
        val isPotentialSource = record.appOpsController.getMode() != MODE_DEFAULT ||
            record.app.packageName in potentialPackageNames
        if (!isPotentialSource) {
            return false
        }
        // If whitelist is enabled, check if package is whitelisted
        if (InstallAppWhitelistController.isWhitelistEnabled(context)) {
            return isPackageAllowedToInstallApps(record.app.packageName)
        }
        return true
    }

    override fun setAllowed(record: InstallUnknownAppsRecord, newAllowed: Boolean) {
        // Block granting permission if whitelist is enabled and package is not whitelisted
        if (newAllowed && InstallAppWhitelistController.isWhitelistEnabled(context)) {
            if (!isPackageAllowedToInstallApps(record.app.packageName)) {
                // Don't grant permission for non-whitelisted apps
                return
            }
        }
        record.appOpsController.setAllowed(newAllowed)
    }

    companion object {
        private val APP_OPS = AppOps(AppOpsManager.OP_REQUEST_INSTALL_PACKAGES)

        private fun getPotentialPackageNames(userId: Int): Set<String> =
            AppGlobals.getPackageManager()
                .getAppOpPermissionPackages(Manifest.permission.REQUEST_INSTALL_PACKAGES, userId)
                .toSet()
    }

    /**
     * Check if the package is allowed to install apps from unknown sources.
     * Only packages in the whitelist (config_allowed_install_app_packages) are allowed.
     * If the whitelist is empty or the toggle is disabled, all packages are allowed (backward compatibility).
     */
    private fun isPackageAllowedToInstallApps(packageName: String): Boolean {
        if (packageName.isEmpty()) {
            return false
        }
        try {
            // Check if whitelist restriction is enabled
            if (!InstallAppWhitelistController.isWhitelistEnabled(context)) {
                // If toggle is disabled, allow all (backward compatibility)
                return true
            }
            val res: Resources = context.resources
            val allowedPackages = res.getStringArray(R.array.config_allowed_install_app_packages)
            // If whitelist is empty, allow all (backward compatibility)
            if (allowedPackages == null || allowedPackages.isEmpty()) {
                return true
            }
            // Check if current package is in whitelist
            for (allowedPackage in allowedPackages) {
                if (TextUtils.equals(packageName, allowedPackage)) {
                    return true
                }
            }
            return false
        } catch (e: Resources.NotFoundException) {
            // If resource not found, allow all (backward compatibility)
            return true
        }
    }
}
