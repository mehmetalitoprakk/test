package com.motive.cimbomes.model

class GroupMembers {
    var groupAdmin : Boolean? = null
    var uid : String? = null
    var name : String? = null
    var surname : String? = null
    var number : String? = null
    var typing : Boolean? = null
    var image : String? = null

    constructor(){}
    constructor(
        groupAdmin: Boolean?,
        uid: String?,
        name: String?,
        surname: String?,
        number: String?,
        typing: Boolean?,
        image: String?
    ) {
        this.groupAdmin = groupAdmin
        this.uid = uid
        this.name = name
        this.surname = surname
        this.number = number
        this.typing = typing
        this.image = image
    }

    override fun toString(): String {
        return "GroupMembers(groupAdmin=$groupAdmin, uid=$uid, name=$name, surname=$surname, number=$number, typing=$typing, image=$image)"
    }


}