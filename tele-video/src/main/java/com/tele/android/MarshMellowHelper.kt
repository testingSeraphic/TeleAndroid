package com.tele.android

import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import android.util.Log
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import java.util.ArrayList

class MarshMellowHelper {

    private val TAG = "PermissionHelper"
    private val permissionHelper: MarshMellowHelper? = null
    private var REQUEST_CODE: Int = 0
    private var activity: Activity? = null
    private var fragment: Fragment? = null
    private var permissions: Array<String>? = null
    private var mPermissionCallback: PermissionCallback? = null
    private var showRational: Boolean = false

    private constructor(activity: Activity, fragment: Fragment, permissions: Array<String>, requestCode: Int) {
        this.activity = activity
        this.fragment = fragment
        this.permissions = permissions
        this.REQUEST_CODE = requestCode
        //        checkIfPermissionPresentInAndroidManifest();
    }

    constructor(activity: Activity, permissions: Array<String>, requestCode: Int) {
        this.activity = activity
        this.permissions = permissions
        this.REQUEST_CODE = requestCode
        //        checkIfPermissionPresentInAndroidManifest();
    }

    constructor(fragment: Fragment, permissions: Array<String>, requestCode: Int) {
        this.fragment = fragment
        this.permissions = permissions
        this.REQUEST_CODE = requestCode
        //        checkIfPermissionPresentInAndroidManifest();
    }

    private fun checkIfPermissionPresentInAndroidManifest() {
        for (permission in permissions!!) {
            if (!hasPermission(permission)) {
                throw RuntimeException("Permission ($permission) Not Declared in manifest")
            }
        }
    }

    fun request(permissionCallback: PermissionCallback) {
        this.mPermissionCallback = permissionCallback
        if (!checkSelfPermission(permissions!!)) {
            showRational = shouldShowRational(permissions!!)
            if (activity != null)
                ActivityCompat.requestPermissions(activity!!, filterNotGrantedPermission(permissions!!), REQUEST_CODE)
            else
                fragment!!.requestPermissions(filterNotGrantedPermission(permissions!!), REQUEST_CODE)
        } else {
            Log.i(TAG, "PERMISSION: Permission Granted")
            if (mPermissionCallback != null)
                mPermissionCallback!!.onPermissionGranted()
        }
    }

    fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {

        if (requestCode == REQUEST_CODE) {
            var denied = false
            for (grantResult in grantResults) {
                if (grantResult != PackageManager.PERMISSION_GRANTED) {
                    denied = true
                    break
                }
            }

            if (denied) {
                val currentShowRational = shouldShowRational(permissions)
                if (!showRational && !currentShowRational) {
                    Log.d(TAG, "PERMISSION: Permission Denied By System")
                    if (mPermissionCallback != null)
                        mPermissionCallback!!.onPermissionDeniedBySystem()
                } else {
                    Log.i(TAG, "PERMISSION: Permission Denied")
                    if (mPermissionCallback != null)
                        mPermissionCallback!!.onPermissionDenied()
                }
            } else {
                Log.i(TAG, "PERMISSION: Permission Granted")
                if (mPermissionCallback != null)
                    mPermissionCallback!!.onPermissionGranted()
            }
        }
    }

    //====================================
    //====================================

    private fun <T : Context> getContext(): T {
        return if (activity != null) activity as T else fragment!!.context as T
    }

    /**
     * Return list that is not granted and we need to ask for permission
     *
     * @param permissions
     * @return
     */
    private fun filterNotGrantedPermission(permissions: Array<String>): Array<String> {
        val notGrantedPermission = ArrayList<String>()
        for (permission in permissions) {
            if (ContextCompat.checkSelfPermission(getContext(), permission) != PackageManager.PERMISSION_GRANTED) {
                notGrantedPermission.add(permission)
            }
        }
        return notGrantedPermission.toTypedArray()
    }

    /**
     * Check permission is there or not for fit_watch of permissions
     *
     * @param permissions
     * @return
     */
    private fun checkSelfPermission(permissions: Array<String>): Boolean {
        for (permission in permissions) {
            if (ContextCompat.checkSelfPermission(getContext(), permission) != PackageManager.PERMISSION_GRANTED) {
                return false
            }
        }
        return true
    }

    /**
     * Checking if there is need to show rational for fit_watch of permissions
     *
     * @param permissions
     * @return
     */
    private fun shouldShowRational(permissions: Array<String>): Boolean {
        var currentShowRational = false
        for (permission in permissions) {

            if (activity != null) {
                if (ActivityCompat.shouldShowRequestPermissionRationale(activity!!, permission)) {
                    currentShowRational = true
                    break
                }
            } else {
                if (fragment!!.shouldShowRequestPermissionRationale(permission)) {
                    currentShowRational = true
                    break
                }
            }
        }
        return currentShowRational
    }

    private fun hasPermission(permission: String): Boolean {
        try {
            val context = if (activity != null) activity else fragment!!.activity
            val info = context!!.packageManager.getPackageInfo(context.packageName, PackageManager.GET_PERMISSIONS)
            if (info.requestedPermissions != null) {
                for (p in info.requestedPermissions) {
                    if (p == permission) {
                        return true
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        return false
    }

    interface PermissionCallback {
        fun onPermissionGranted()

        fun onPermissionDenied()

        fun onPermissionDeniedBySystem()
    }
}

