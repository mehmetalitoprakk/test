package com.motive.cimbomes.model

class GroupMembers {
    var groupAdmin : Boolean? = null
    var uid : String? = null
    var name : String? = null
    var surname : String? = null
    var number : String? = null
    var typing : Boolean? = null

    constructor(){}
    constructor(
        groupAdmin: Boolean?,
        uid: String?,
        name: String?,
        surname: String?,
        number: String?,
        typing: Boolean?
    ) {
        this.groupAdmin = groupAdmin
        this.uid = uid
        this.name = name
        this.surname = surname
        this.number = number
        this.typing = typing
    }

    override fun toString(): String {
        return "GroupMembers(groupAdmin=$groupAdmin, uid=$uid, name=$name, surname=$surname, number=$number, typing=$typing)"
    }


}