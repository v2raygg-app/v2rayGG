package com.github.v2raygg.helper

import android.content.pm.PackageManager
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.github.v2raygg.R
import com.github.v2raygg.enums.PermissionType
import com.github.v2raygg.extension.toast

/**
 * Helper for requesting permissions.
 */
class PermissionHelper(private val activity: AppCompatActivity) {
    private var permissionCallback: ((Boolean) -> Unit)? = null

    private val permissionLauncher: ActivityResultLauncher<String> =
        activity.registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
            permissionCallback?.invoke(isGranted)
            permissionCallback = null
        }

    /**
     * Check the permission and request it if not granted.
     *
     * @param permissionType the type of permission
     * @param onGranted called when permission is granted (called immediately if already granted)
     */
    fun request(permissionType: PermissionType, onGranted: () -> Unit) {
        val permission = permissionType.getPermission()
        if (ContextCompat.checkSelfPermission(activity, permission) == PackageManager.PERMISSION_GRANTED) {
            onGranted()
        } else {
            permissionCallback = { isGranted ->
                if (isGranted) {
                    onGranted()
                } else {
                    val message = "${activity.getString(R.string.toast_permission_denied)}  ${permissionType.getLabel()}"
                    activity.toast(message)
                }
            }
            permissionLauncher.launch(permission)
        }
    }
}