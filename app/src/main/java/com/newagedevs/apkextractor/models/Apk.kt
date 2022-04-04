package com.newagedevs.apkextractor.models

import android.content.pm.ApplicationInfo

data class Apk(val appInfo: ApplicationInfo,
               val appName: String,
               val packageName: String? = "",
               val version: String? = "")