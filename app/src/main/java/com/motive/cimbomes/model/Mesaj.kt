package com.motive.cimbomes.model

class Mesaj {
    var mesaj : String? = null
    var goruldu : Boolean? = null
    var time : Long? = null
    var type : String? = null
    var user_id : String? = null
    var mesajResim : String? = null
    var video : String? = null


    constructor(){}
    constructor(
        mesaj: String?,
        goruldu: Boolean?,
        time: Long?,
        type: String?,
        user_id: String?,
        mesajResim: String?,
        video: String?
    ) {
        this.mesaj = mesaj
        this.goruldu = goruldu
        this.time = time
        this.type = type
        this.user_id = user_id
        this.mesajResim = mesajResim
        this.video = video
    }

    override fun toString(): String {
        return "Mesaj(mesaj=$mesaj, goruldu=$goruldu, time=$time, type=$type, user_id=$user_id, mesajResim=$mesajResim, video=$video)"
    }


}