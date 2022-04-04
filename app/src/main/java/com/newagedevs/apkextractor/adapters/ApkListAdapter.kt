package com.newagedevs.apkextractor.adapters

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.snackbar.Snackbar
import com.newagedevs.apkextractor.MainActivity
import com.newagedevs.apkextractor.R
import com.newagedevs.apkextractor.models.Apk
import com.newagedevs.apkextractor.utils.Utilities
import org.jetbrains.anko.find
import java.util.*


class ApkListAdapter(private var apkList: ArrayList<Apk>, private val context: Context) : RecyclerView.Adapter<ApkListAdapter.ApkListViewHolder>() {

    var mItemClickListener: OnContextItemClickListener? = null

    init {
        mItemClickListener = context as MainActivity
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ApkListViewHolder {
        return ApkListViewHolder(LayoutInflater.from(context).inflate(R.layout.layout_apk_item, parent, false), context, apkList)
    }

    override fun onBindViewHolder(holder: ApkListViewHolder, position: Int) {
        holder.mIconImageView.setImageDrawable(context.packageManager.getApplicationIcon(apkList[position].appInfo))
        holder.mLabelTextView.text = context.packageManager.getApplicationLabel(apkList[position].appInfo).toString()
        holder.mPackageTextView.text = apkList[position].packageName
    }

    override fun getItemCount(): Int {
        return apkList.size
    }

    inner class ApkListViewHolder(view: View, context: Context, apkList: ArrayList<Apk>) : RecyclerView.ViewHolder(view) {

        val mIconImageView: ImageView = view.find(R.id.apk_icon_iv)
        val mLabelTextView: TextView = view.find(R.id.apk_label_tv)
        val mPackageTextView: TextView = view.find(R.id.apk_package_tv)
        private val mExtractBtn: Button = view.find(R.id.extract_btn)
        private val mShareBtn: Button = view.find(R.id.share_btn)
        private val mUninstallBtn: Button = view.find(R.id.uninstall_btn)
        private val mMenuBtn: ImageButton = view.find(R.id.menu_btn)

        init {

            (context as Activity).registerForContextMenu(mMenuBtn)

            mExtractBtn.setOnClickListener {
                if (Utilities.checkPermission(context as MainActivity)) {
                    Utilities.extractApk(apkList[adapterPosition])
                    val rootView: View = context.window.decorView.find(android.R.id.content)
                    Snackbar.make(rootView, "${apkList[adapterPosition].appName} apk extracted successfully", Snackbar.LENGTH_LONG).show()
                }
            }

            mShareBtn.setOnClickListener {
                if (Utilities.checkPermission(context as MainActivity)) {
                    val intent = Utilities.getShareableIntent(apkList[adapterPosition])
                    context.startActivity(Intent.createChooser(intent, "Share the apk using"))
                }
            }

            mUninstallBtn.setOnClickListener {
                val uninstallIntent = Intent(Intent.ACTION_UNINSTALL_PACKAGE)
                uninstallIntent.data = Uri.parse("package:" + apkList[adapterPosition].packageName)
                uninstallIntent.putExtra(Intent.EXTRA_RETURN_RESULT, true)
                context.startActivity(uninstallIntent)
            }

            mMenuBtn.setOnClickListener {
                mItemClickListener?.onItemClicked(apkList[adapterPosition].packageName!!)
                context.openContextMenu(mMenuBtn)
                mMenuBtn.setOnCreateContextMenuListener { menu, _, _ ->
                    context.menuInflater.inflate(R.menu.context_menu, menu)
                }
            }
        }
    }

    interface OnContextItemClickListener {
        fun onItemClicked(packageName: String)
    }
}