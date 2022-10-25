package com.example.pochtao.beans

class MailboxBean {
    var username : String
    var password : String
    var name : String?

    constructor(username : String, password : String, name : String? = null) {
        this.username = username
        this.password = password
        this.name = name
    }
}