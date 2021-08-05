package com.motive.cimbomes.utils

import android.app.Activity
import android.content.Context
import android.net.Uri
import android.os.AsyncTask
import android.os.Environment
import android.util.Log
import androidx.fragment.app.FragmentManager
import androidx.loader.content.AsyncTaskLoader
import com.iceteck.silicompressorr.SiliCompressor
import com.motive.cimbomes.activity.ChatActivity
import com.motive.cimbomes.fragments.ProgressFragment
import java.io.File

internal class CompressSiliVideo(fm : FragmentManager, ctx : Context, var activity: Activity) : AsyncTask<String,String,String>() {

    var fragmentManager = fm
    var context = ctx
    var mActivity = activity
    var compressFragment = ProgressFragment()

    override fun onPreExecute() {
        compressFragment.show(fragmentManager,"compressDialog")
        compressFragment.isCancelable = false
        super.onPreExecute()
    }

    override fun doInBackground(vararg params: String?): String? {
        var yeniOlusturulanDosyaninKlasoru = File(Environment.getExternalStorageDirectory().absolutePath)
        Log.e("KONTROL","DO İT BACKTODUNDA GİRDİ")

        if (yeniOlusturulanDosyaninKlasoru.isDirectory || yeniOlusturulanDosyaninKlasoru.mkdirs()){
            var uri = Uri.parse(params[0])
            Log.e("KONTROL","İF BLOGUNA GİRDİ GİRDİ")
            var yeniDosyaPath = SiliCompressor.with(context).compressVideo(uri,yeniOlusturulanDosyaninKlasoru.path)
            return yeniDosyaPath
        }
        return null



    }

    override fun onPostExecute(yeniDosyaPath: String?) {
        if (!yeniDosyaPath.isNullOrEmpty()){
            Log.e("KONTROL","ife girdi")
            compressFragment.dismiss()
            (mActivity as ChatActivity).uploadStorageVideo(yeniDosyaPath)
        }
        Log.e("KONTROL","null döndü")
        super.onPostExecute(yeniDosyaPath)


    }


}