package com.example.pochtao.controllers

import android.content.Intent
import android.os.Bundle
import android.os.PersistableBundle
import android.widget.Button
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.pochtao.R
import com.example.pochtao.enums.Mailboxes
import kotlinx.android.synthetic.main.email_details.*

class EmailDetails :  AppCompatActivity() {

    lateinit var tv_from : TextView
    lateinit var tv_subject : TextView
    lateinit var tv_content : TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.email_details)

        //to load intent extra
        val content = intent.extras?.get("content")
        val subject = intent.extras?.get("subject")
        val from = intent.extras?.get("from")

        //TextView
        tv_from = findViewById(R.id.tv_message_from)
        tv_from.text = from.toString()
        tv_subject = findViewById(R.id.tv_message_subject)
        tv_subject.text = subject.toString()
        tv_content = findViewById(R.id.tv_message_content)
        tv_content.text = content.toString()


        //Button
        val btReply : Button = findViewById(R.id.bt_reply)
        btReply.setOnClickListener {
            val intent = Intent(this, SendMail::class.java)
            intent.putExtra("to",from.toString())
            startActivity(intent)
        }
        val btClose : ImageButton = findViewById(R.id.bt_close)
        btClose.setOnClickListener{
            this.finish()
        }

    }
}