package com.motive.cimbomes.model

class BlockedUsers {
    var blocked : Boolean? = null

    constructor()

    constructor(blocked: Boolean?) {
        this.blocked = blocked
    }

    override fun toString(): String {
        return "BlockedUsers(blocked=$blocked)"
    }


}