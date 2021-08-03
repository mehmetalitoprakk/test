package com.motive.cimbomes.model

class Users {
    var isim : String? = null
    var soyisim : String? = null
    var telefonNo : String? = null
    var profilePic : String? = null
    var uid :String? = null
    var fcmToken : String? = null
    var ipAdress : String? = null
    var isAdmin : Boolean? = null

    constructor()
    constructor(
        isim: String?,
        soyisim: String?,
        telefonNo: String?,
        profilePic: String?,
        uid: String?,
        fcmToken: String?,
        ipAdress: String?,
        isAdmin: Boolean?
    ) {
        this.isim = isim
        this.soyisim = soyisim
        this.telefonNo = telefonNo
        this.profilePic = profilePic
        this.uid = uid
        this.fcmToken = fcmToken
        this.ipAdress = ipAdress
        this.isAdmin = isAdmin
    }

    override fun toString(): String {
        return "Users(isim=$isim, soyisim=$soyisim, telefonNo=$telefonNo, profilePic=$profilePic, uid=$uid, fcmToken=$fcmToken, ipAdress=$ipAdress, isAdmin=$isAdmin)"
    }


}