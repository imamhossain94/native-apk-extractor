package com.newagedevs.apkextractor.utils

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.net.Uri
import android.os.Environment
import android.os.FileUtils
import android.util.Log
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.android.material.snackbar.Snackbar
import com.newagedevs.apkextractor.MainActivity
import com.newagedevs.apkextractor.models.Apk
import java.io.File



class Utilities {

    companion object {

        const val STORAGE_PERMISSION_CODE = 1008

        fun checkPermission(activity: AppCompatActivity): Boolean {
            var permissionGranted = false

            if (ContextCompat.checkSelfPermission(activity, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                if (ActivityCompat.shouldShowRequestPermissionRationale(activity,
                                Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                    val rootView: View = (activity as MainActivity).window.decorView.findViewById(android.R.id.content)
                    Snackbar.make(rootView, "Storage permission required", Snackbar.LENGTH_LONG)
                            .setAction("Allow") {
                                ActivityCompat.requestPermissions(activity, arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE), STORAGE_PERMISSION_CODE)
                            }
                            .setActionTextColor(Color.WHITE)
                            .show()
                } else {
                    ActivityCompat.requestPermissions(activity, arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE), STORAGE_PERMISSION_CODE)
                }
            } else {
                permissionGranted = true
            }

            return permissionGranted
        }

        fun checkExternalStorage(): Boolean {
            return Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)
        }

        fun getAppFolder(): File? {
            var file: File? = null
            if (checkExternalStorage()) {
                file = File(Environment.getExternalStorageDirectory(), "ApkExtractor")
                return file
            }
            return file
        }

        fun makeAppDir() {
            val file = getAppFolder()
            if (file != null && !file.exists()) {
                file.mkdir()
            }
        }

        fun extractApk(apk: Apk): Boolean {
            makeAppDir()
            var extracted = true
            val originalFile = File(apk.appInfo.sourceDir)
            val extractedFile: File = getApkFile(apk)

            extracted = try {
                //FileUtils.copyFile(originalFile, extractedFile)
                originalFile.copyTo(extractedFile)
                true
            } catch (e: Exception) {
                Log.d("test", "problem - " + e.message)
                false
            }

            return extracted
        }

        fun getApkFile(apk: Apk): File {
            val fileName = getAppFolder()?.path + File.separator + apk.appName + "_" + apk.version + ".apk"
            return File(fileName)
        }

        fun getShareableIntent(apk: Apk): Intent {
            extractApk(apk)
            val file = getApkFile(apk)
            val shareIntent = Intent()
            shareIntent.action = Intent.ACTION_SEND
            shareIntent.putExtra(Intent.EXTRA_STREAM, Uri.fromFile(file))
            shareIntent.type = "application/vnd.android.package-archive"
            shareIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)

            return shareIntent
        }
    }
}