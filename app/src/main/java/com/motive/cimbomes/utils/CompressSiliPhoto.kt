package com.motive.cimbomes.utils

import android.app.Activity
import android.content.Context
import android.os.AsyncTask
import android.os.Environment
import android.util.Log
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import com.iceteck.silicompressorr.SiliCompressor
import com.motive.cimbomes.activity.ChatActivity
import com.motive.cimbomes.fragments.ProgressFragment
import java.io.File

internal class CompressSiliPhoto(fm : FragmentManager,ctx : Context,var activity: Activity) : AsyncTask<String,String,String>() {

    var fragmentManager = fm
    var context = ctx
    var mActivity = activity
    var compressFragment = ProgressFragment()



    override fun doInBackground(vararg params: String?): String {
        var yeniOlusanDosyaninKlasoru = File(Environment.getExternalStorageDirectory().absolutePath+"DCIM/Cimbomes/")
        var yeniDosyaYolu = SiliCompressor.with(context).compress(params[0],yeniOlusanDosyaninKlasoru)
        return yeniDosyaYolu
    }

    override fun onPreExecute() {

        compressFragment.show(fragmentManager,"compressDialog")
        compressFragment.isCancelable = false


        super.onPreExecute()
    }

    override fun onPostExecute(result: String?) {
        Log.e("DOSYAPATH",result!!)
        compressFragment.dismiss()
        (mActivity as ChatActivity).uploadStorage(result)

        super.onPostExecute(result)
    }


}