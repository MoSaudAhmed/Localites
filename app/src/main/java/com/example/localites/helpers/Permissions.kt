package com.example.localites.helpers

import android.Manifest
import android.R
import android.app.Activity
import android.app.AlertDialog
import android.content.pm.PackageManager
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import java.util.*

class Permissions(val context: Activity) {
    private fun checkCameraPermission(): Boolean {
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA)
            != PackageManager.PERMISSION_GRANTED
        ) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(
                    context,
                    Manifest.permission.CAMERA
                )
            ) {
                AlertDialog.Builder(context) //, R.style.TranslucentDialog
                    .setTitle("Permission Required")
                    .setMessage("This permission was denied earlier by you. This permission is required to access your camera to capture your details.So, in order to use this feature please allow this permission by clicking ok.")
                    .setPositiveButton(
                        R.string.yes
                    ) { dialog, which ->
                        dialog.dismiss()
                        ActivityCompat.requestPermissions(
                            context,
                            arrayOf(Manifest.permission.CAMERA),
                            1
                        )
                    }
                    .setNegativeButton(
                        R.string.no
                    ) { dialog, which -> dialog.dismiss() }
                    .setIcon(R.drawable.ic_dialog_alert)
                    .show()
            } else {
                // Just ask for the permission for first time. This block will come into play when user is trying to use feature which requires permission grant.
                //So for the first time user will be into this else block. So just ask for the permission you need by showing default permission dialog
                ActivityCompat.requestPermissions(
                    context,
                    arrayOf(Manifest.permission.CAMERA),
                    1
                )
            }
        } else {
            // If permission is already granted by user then we will be into this else block. So do whatever is required here
//            Toast.makeText(context,"Permission Aleardy granted",Toast.LENGTH_LONG).show();
            return true
        }
        return false
    }

    public fun checkStoragePermission(): Boolean {
        if (ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
            )
            != PackageManager.PERMISSION_GRANTED
        ) {
            // This condition checks whether user has earlier denied the permission or not just by clicking on deny in the permission dialog.
            //Remember not on the never ask check box then deny in the permission dialog
            if (ActivityCompat.shouldShowRequestPermissionRationale(
                    context,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE
                )
            ) {
                /*Show an explanation to user why this permission in needed by us. Here using alert dialog to show the permission use.
                 */
                AlertDialog.Builder(context) //, R.style.TranslucentDialog
                    .setTitle("Permission Required")
                    .setMessage("This permission was denied earlier by you. This permission is required to access your storage to read/write your details.So, in order to use this feature please allow this permission by clicking ok.")
                    .setPositiveButton(
                        R.string.yes
                    ) { dialog, which ->
                        dialog.dismiss()
                        ActivityCompat.requestPermissions(
                            context,
                            arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE),
                            2
                        )
                    }
                    .setNegativeButton(
                        R.string.no
                    ) { dialog, which -> dialog.dismiss() }
                    .setIcon(R.drawable.ic_dialog_alert)
                    .show()
            } else {
                // Just ask for the permission for first time. This block will come into play when user is trying to use feature which requires permission grant.
                //So for the first time user will be into this else block. So just ask for the permission you need by showing default permission dialog
                ActivityCompat.requestPermissions(
                    context,
                    arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE),
                    2
                )
            }
        } else {
            // If permission is already granted by user then we will be into this else block. So do whatever is required here
//            Toast.makeText(context,"Permission Aleardy granted",Toast.LENGTH_LONG).show();
            return true
        }
        return false
    }

    fun checkAllPermissions(): Boolean {
        return checkCameraPermission() && checkStoragePermission()
    }

    fun checkAndRequestPermissions(): Boolean {
        val permissionSendMessage = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.CAMERA
        )
        val locationPermission = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
        )
        val readStoragePermission = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.READ_EXTERNAL_STORAGE
        )
        val listPermissionsNeeded: MutableList<String> =
            ArrayList()
        if (locationPermission != PackageManager.PERMISSION_GRANTED) {
            listPermissionsNeeded.add(Manifest.permission.WRITE_EXTERNAL_STORAGE)
        }
        if (permissionSendMessage != PackageManager.PERMISSION_GRANTED) {
            listPermissionsNeeded.add(Manifest.permission.CAMERA)
        }
        if (readStoragePermission != PackageManager.PERMISSION_GRANTED) {
            listPermissionsNeeded.add(Manifest.permission.READ_EXTERNAL_STORAGE)
        }
        if (!listPermissionsNeeded.isEmpty()) {
            ActivityCompat.requestPermissions(
                context,
                listPermissionsNeeded.toTypedArray(),
                PERMISSION_REQUEST_ID
            )
            return false
        }
        return true
    }

    fun checkLocationPermissions(): Boolean {
        val permissionSendMessage = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_COARSE_LOCATION
        )
        val locationPermission = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_COARSE_LOCATION
        )
        val listPermissionsNeeded: MutableList<String> =
            ArrayList()
        if (locationPermission != PackageManager.PERMISSION_GRANTED) {
            listPermissionsNeeded.add(Manifest.permission.ACCESS_COARSE_LOCATION)
        }
        if (permissionSendMessage != PackageManager.PERMISSION_GRANTED) {
            listPermissionsNeeded.add(Manifest.permission.ACCESS_COARSE_LOCATION)
        }
        if (!listPermissionsNeeded.isEmpty()) {
            ActivityCompat.requestPermissions(
                context,
                listPermissionsNeeded.toTypedArray(),
                PERMISSION_REQUEST_ID
            )
            return false
        }
        return true
    }

    companion object {
        private const val PERMISSION_REQUEST_ID = 1
    }

}