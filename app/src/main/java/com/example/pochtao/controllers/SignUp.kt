package com.example.pochtao.controllers
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.pochtao.AppExecutors
import com.example.pochtao.MainActivity
import com.example.pochtao.R
import com.example.pochtao.beans.MailboxBean
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlin.coroutines.CoroutineContext

class SignUp : AppCompatActivity(), CoroutineScope {
    override val coroutineContext: CoroutineContext
        get() = Dispatchers.Main + job

    private lateinit var job: Job
    var retrofitController: RetrofitController = RetrofitController()
    var emailController: EmailController = EmailController()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        job = Job()
        emailController.appExecutors = AppExecutors()
        setContentView(R.layout.register)

        //initialisation
        val editTextName: EditText = findViewById(R.id.editTextName)
        val editTextEmail: EditText = findViewById(R.id.editTextEmail)
        val editTextPassword: EditText = findViewById(R.id.editTextPassword)
        val editTextPasswordConfirm: EditText = findViewById(R.id.editTextPasswordConfirm)
        val buttonSignUp = findViewById<Button>(R.id.buttonSignUp)
        val intent = Intent(this, MainActivity::class.java)

        buttonSignUp.setOnClickListener { //sign up function
            launch {
                //Condition
                if (editTextEmail.text.toString() != "" || editTextPassword.text.toString() != ""
                    || editTextName.text.toString() != "" || editTextPasswordConfirm.text.toString() != "" || editTextPassword.text.toString() == editTextPasswordConfirm.text.toString()
                ) {
                    val result = retrofitController.service.createMailbox(
                        MailboxBean(
                            editTextEmail.text.toString(),
                            editTextPassword.text.toString(),
                            editTextName.text.toString(),
                        )
                    )
                    //if success
                    if (result.success == true) {
                        startActivity(intent)
                    } else {
                        Toast.makeText(this@SignUp, "Неверная информация.", Toast.LENGTH_LONG)
                            .show()
                    }
                }
                    else
                        Toast.makeText(
                            this@SignUp,
                            "Пожалуйста, заполните все поля перед подтверждением.",
                            Toast.LENGTH_LONG
                        ).show()
                    }
                }
            }
        }


