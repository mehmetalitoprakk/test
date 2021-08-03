package com.motive.cimbomes.model

class Contact {

    var name: String? = null
    var number: String? = null
    var image:String? = null
    var kullaniyorMu:Boolean? = null
    var uid:String? = null

    constructor()
    constructor(
        name: String?,
        number: String?,
        image: String?,
        kullaniyorMu: Boolean?,
        uid: String?
    ) {
        this.name = name
        this.number = number
        this.image = image
        this.kullaniyorMu = kullaniyorMu
        this.uid = uid
    }

    override fun toString(): String {
        return "Contact(name=$name, number=$number, image=$image, kullaniyorMu=$kullaniyorMu, uid=$uid)"
    }


}
