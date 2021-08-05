package com.motive.cimbomes.utils

import android.util.Log
import java.io.File

class DosyaIslemleri {

    companion object {
        fun klasordekiDosyalarigetir(klasorAdi:String) : ArrayList<String>{
            var tumdosyalar = ArrayList<String>()
            var file=File(klasorAdi)
            var klasordekiTumDosyalar = file.listFiles()
            Log.e("KONTROL","Fonksiyona girdi")




            //parametre olarak gönderilern klasör yolunda elemen olup olmadıgı kontrol edildi
            if (klasordekiTumDosyalar != null){
                for (i in 0..klasordekiTumDosyalar.size-1){
                    if (klasordekiTumDosyalar[i].isFile){


                        var okunanDosyaYolu = klasordekiTumDosyalar[i].absolutePath
                        var dosyaturu = okunanDosyaYolu.substring(okunanDosyaYolu.lastIndexOf("."))
                        Log.e("KONTROL","OKUNAN VERİ DOSYA TURU"+dosyaturu)
                        if (dosyaturu.equals(".jpeg") || dosyaturu.equals(".jpg") || dosyaturu.equals(".png") || dosyaturu.equals(".mp4")){
                            tumdosyalar.add(okunanDosyaYolu)
                            Log.e("KONTROL","arrayliste eklenen dosya"+okunanDosyaYolu)
                        }
                    }


                }
            }



            return tumdosyalar
        }
    }
}