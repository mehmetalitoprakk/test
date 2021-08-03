package com.motive.cimbomes.model

class Konusma {
    var goruldu : Boolean? = null
    var son_mesaj : String? = null
    var time : Long? = null
    var user_id : String? = null


    constructor(){}

    constructor(goruldu: Boolean?, son_mesaj: String?, time: Long?) {
        this.goruldu = goruldu
        this.son_mesaj = son_mesaj
        this.time = time
    }

    constructor(goruldu: Boolean?, son_mesaj: String?, time: Long?, user_id: String?) {
        this.goruldu = goruldu
        this.son_mesaj = son_mesaj
        this.time = time
        this.user_id = user_id
    }


    override fun toString(): String {
        return "Konusma(goruldu=$goruldu, son_mesaj=$son_mesaj, time=$time)"
    }


}



