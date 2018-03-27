package com.fangelo.plunderous.client.android

import android.content.pm.PackageManager
import com.fangelo.plunderous.client.PlatformAdapter

class AndroidPlatformAdapter(launcher: AndroidLauncher) : PlatformAdapter() {
    init {
        try {
            this.version = launcher.packageManager.getPackageInfo(launcher.getPackageName(), 0).versionName + " (code " +
                    launcher.packageManager.getPackageInfo(launcher.getPackageName(), 0).versionCode + ")"
        } catch (ex: PackageManager.NameNotFoundException) {
            this.version = ""
        }


    }
}