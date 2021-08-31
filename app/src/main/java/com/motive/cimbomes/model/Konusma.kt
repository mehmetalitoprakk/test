package com.motive.cimbomes.model

class Konusma {
    var goruldu : Boolean? = null
    var son_mesaj : String? = null
    var time : Long? = null
    var user_id : String? = null
    var name : String? = null


    constructor()
    constructor(
        goruldu: Boolean?,
        son_mesaj: String?,
        time: Long?,
        user_id: String?,
        name: String?
    ) {
        this.goruldu = goruldu
        this.son_mesaj = son_mesaj
        this.time = time
        this.user_id = user_id
        this.name = name
    }

    override fun toString(): String {
        return "Konusma(goruldu=$goruldu, son_mesaj=$son_mesaj, time=$time, user_id=$user_id, name=$name)"
    }


}



