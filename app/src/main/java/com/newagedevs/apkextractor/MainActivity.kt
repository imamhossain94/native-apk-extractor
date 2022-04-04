package com.newagedevs.apkextractor

import android.annotation.SuppressLint
import android.content.ActivityNotFoundException
import android.content.Intent
import android.content.pm.ApplicationInfo
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import android.view.View
import android.widget.ProgressBar
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.snackbar.Snackbar
import com.newagedevs.apkextractor.adapters.ApkListAdapter
import com.newagedevs.apkextractor.models.Apk
import com.newagedevs.apkextractor.utils.Utilities
import org.jetbrains.anko.doAsync
import org.jetbrains.anko.find
import org.jetbrains.anko.uiThread

class MainActivity : AppCompatActivity(), ApkListAdapter.OnContextItemClickListener {

    private lateinit var progressBar: ProgressBar
    private val apkList = ArrayList<Apk>()
    private lateinit var contextItemPackageName: String

    private lateinit var mAdapter: ApkListAdapter
    private lateinit var mLinearLayoutManager: LinearLayoutManager
    lateinit var mRecyclerView: RecyclerView


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        progressBar = find(R.id.progress)
        Utilities.checkPermission(this)

        mRecyclerView = find(R.id.apk_list_rv)
        mLinearLayoutManager = LinearLayoutManager(this)
        mAdapter = ApkListAdapter(apkList, this)

        mRecyclerView.layoutManager = mLinearLayoutManager
        mRecyclerView.adapter = mAdapter

        loadApk()

    }

    @SuppressLint("NotifyDataSetChanged", "QueryPermissionsNeeded")
    private fun loadApk() {
        doAsync {
            val allPackages: List<PackageInfo> = packageManager.getInstalledPackages(0) //PackageManager.GET_META_DATA


            Log.d("Xyz", allPackages.size.toString())

            allPackages.forEach {
                val applicationInfo: ApplicationInfo = it.applicationInfo

                val userApk = Apk(
                    applicationInfo,
                    packageManager.getApplicationLabel(applicationInfo).toString(),
                    it.packageName,
                    it.versionName)
                apkList.add(userApk)
            }

            uiThread {
                mAdapter.notifyDataSetChanged()
                progressBar.visibility = View.GONE
            }
        }
    }


    @SuppressLint("QueryPermissionsNeeded")
    fun installedApps() {
        val packList = packageManager.getInstalledPackages(0)
        for (i in packList.indices) {
            val packInfo = packList[i]
            if (packInfo.applicationInfo.flags and ApplicationInfo.FLAG_SYSTEM == 0) {
                val appName = packInfo.applicationInfo.loadLabel(packageManager).toString()
                Log.e("App № $i", appName)
            }
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            Utilities.STORAGE_PERMISSION_CODE -> {
                if ((grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                    Utilities.makeAppDir()
                } else {
                    Snackbar.make(find(android.R.id.content), "Permission required to extract apk", Snackbar.LENGTH_LONG).show()
                }
            }
        }
    }

    override fun onContextItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.action_launch -> {
                try {
                    startActivity(packageManager.getLaunchIntentForPackage(contextItemPackageName))
                } catch (e: Exception) {
                    Snackbar.make(find(android.R.id.content), "Can't open this app", Snackbar.LENGTH_SHORT).show()
                }
            }
            R.id.action_playstore -> {
                try {
                    startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=$contextItemPackageName")))
                } catch (e: ActivityNotFoundException) {
                    startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=$contextItemPackageName")))
                }

            }
        }
        return true
    }

    override fun onItemClicked(packageName: String) {
        contextItemPackageName = packageName
    }
}