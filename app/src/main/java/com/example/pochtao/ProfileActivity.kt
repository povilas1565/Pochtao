package com.example.pochtao

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.example.pochtao.beans.Credentials
import com.example.pochtao.controllers.EmailController
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlin.coroutines.CoroutineContext

class ProfileActivity : AppCompatActivity(), CoroutineScope {
    override val coroutineContext: CoroutineContext
    get() = Dispatchers.Main + job

    private lateinit var job: Job
    private lateinit var etName : EditText
    private var emailController: EmailController = EmailController()
    private val getResult =
        registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()) {
            if(it.resultCode == Activity.RESULT_OK){
                if(it.data?.extras?.getBoolean("valid") == false) {
                    etName.setText(Credentials.NAME)
                } else if (it.data?.extras?.getBoolean("valid") == true) {
                    Credentials.NAME = etName.text.toString()
                }
            }
        }

    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        job = Job()
        emailController.appExecutors = AppExecutors()
        setContentView(R.layout.profile)
        etName  = findViewById(R.id.editTextPersonName)
        val username = findViewById<TextView>(R.id.usernameText)
        val btModify = findViewById<Button>(R.id.modifyBt)

        username.text = "Email : ${Credentials.EMAIL}"
        etName.setText(Credentials.NAME)
        btModify.setOnClickListener {
            if (etName.text.toString() != "") {
                val intent = Intent(this, ProfilePopupActivity::class.java)
                intent.putExtra("newName", etName.text.toString())
                getResult.launch(intent)
            } else {
                Toast.makeText(this, "Пожалуйста, введите имя!", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onDestroy() {
        job.cancel() // cancel the Job
        super.onDestroy()
    }
}
