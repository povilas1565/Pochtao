package com.example.pochtao

import AllEmailsController
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.pochtao.beans.Credentials
import com.example.pochtao.beans.MailboxBean
import com.example.pochtao.controllers.EmailController
import com.example.pochtao.controllers.RetrofitController
import com.example.pochtao.controllers.SignUp
import com.example.pochtao.enums.Mailboxes
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlin.coroutines.CoroutineContext

class MainActivity : AppCompatActivity(), CoroutineScope {
    override val coroutineContext: CoroutineContext
        get() = Dispatchers.Main + job

    private lateinit var job: Job
    var emailController: EmailController = EmailController()
    var retrofitController: RetrofitController = RetrofitController()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        job = Job()
        emailController.appExecutors = AppExecutors()
        setContentView(R.layout.connection)

        val etEmail: EditText = findViewById(R.id.etEmail)
        val etPassword: EditText = findViewById(R.id.etPassword)

        val btLogin = findViewById<Button>(R.id.btLogin)
        val btSignup = findViewById<Button>(R.id.btSignup)

        btLogin.setOnClickListener {
            launch {
                //check that the email and password fields are filled in
                if (etEmail.text.toString() != "" || etPassword.text.toString() != "") {
                    //api call
                    val result = retrofitController.service.verifyMailbox(
                        MailboxBean(
                            etEmail.text.toString(),
                            etPassword.text.toString()
                        )
                    )
                    // in case the entered identifiers are correct
                    if (result.success == true) {
                        //defined session data about the user
                        Credentials.EMAIL = result.username ?: ""
                        Credentials.PASSWORD = result.password ?: ""
                        Credentials.NAME = result.name ?: ""
                        //access to the allMails view of the logged-in user
                        val intent = Intent(this@MainActivity, AllEmailsController::class.java)
                        intent.putExtra("mailbox_type", Mailboxes.INBOX)
                        startActivity(intent)
                        // in case the entered identifiers are not correct
                    } else {
                        //return the problem to the user
                        Toast.makeText(this@MainActivity, "Неверная информация.", Toast.LENGTH_LONG).show()
                    }
                }
                else{
                    //return the problem to the user
                    Toast.makeText(this@MainActivity, "Пожалуйста, заполните все поля перед потверждением.", Toast.LENGTH_LONG).show()
                }
            }
        }

        //access to the SignUp view to create an account
        btSignup.setOnClickListener {
            val intent = Intent(this, SignUp::class.java)
            startActivity(intent)
        }
    }

    override fun onDestroy() {
        job.cancel() // cancel the Job
        super.onDestroy()
    }
}
