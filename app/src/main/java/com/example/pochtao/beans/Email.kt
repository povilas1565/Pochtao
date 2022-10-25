package com.example.pochtao.beans

class Email {
    var id: Int = 0
    var from: String = ""
    var to: String = ""
    var subject: String = ""
    var body: String = ""
    var date: String = ""
    var read: Boolean = false


    override fun toString(): String {
        return "Email(id=$id, from='$from', to='$to', subject='$subject', body='$body', date='$date', read=$read)"
    }
}