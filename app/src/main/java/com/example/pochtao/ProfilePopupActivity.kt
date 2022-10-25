package com.example.pochtao

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.ArgbEvaluator
import android.animation.ValueAnimator
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.animation.DecelerateInterpolator
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatEditText
import androidx.cardview.widget.CardView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.graphics.ColorUtils
import com.example.pochtao.beans.Credentials
import com.example.pochtao.beans.MailboxBean
import com.example.pochtao.controllers.RetrofitController
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlin.coroutines.CoroutineContext

class ProfilePopupActivity : AppCompatActivity(), CoroutineScope {
    override val coroutineContext: CoroutineContext
        get() = Dispatchers.Main + job

    private lateinit var job: Job
    private lateinit var popup_window_background: ConstraintLayout
    private lateinit var popup_window_view_with_border: CardView
    private lateinit var popup_window_button : Button
    private lateinit var popup_window_text : AppCompatEditText
    private lateinit var newName : String
    private var retrofitController : RetrofitController = RetrofitController()

    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        overridePendingTransition(0, 0)
        job = Job()
        setContentView(R.layout.profile_modification_popup)
        newName = intent.extras?.getString("newName") ?: ""

        val alpha = 100 //between 0-255
        val alphaColor = ColorUtils.setAlphaComponent(Color.parseColor("#000000"), alpha)
        val colorAnimation = ValueAnimator.ofObject(ArgbEvaluator(), Color.TRANSPARENT, alphaColor)
        popup_window_background = findViewById(R.id.popup_window_background)
        popup_window_view_with_border = findViewById(R.id.popup_window_view_with_border)
        popup_window_button = findViewById(R.id.popup_window_button)
        popup_window_text = findViewById(R.id.popup_window_text)
        colorAnimation.duration = 500 // milliseconds
        colorAnimation.addUpdateListener { animator ->
            popup_window_background.setBackgroundColor(animator.animatedValue as Int)
        }
        colorAnimation.start()

        popup_window_view_with_border.alpha = 0f
        popup_window_view_with_border.animate().alpha(1f).setDuration(500).setInterpolator(
            DecelerateInterpolator()
        ).start()

        popup_window_button.setOnClickListener {
            onBtPress()
        }
    }

    private fun onBtPress() {
        launch {
            val returnIntent = Intent()
            val result = retrofitController.service.modifyMailbox(MailboxBean(Credentials.EMAIL, popup_window_text.text.toString(), newName))
            if (result.success == true) {
                Toast.makeText(this@ProfilePopupActivity, "Имя изменено правильно", Toast.LENGTH_SHORT).show()
                returnIntent.putExtra("valid", true)
            } else {
                Toast.makeText(this@ProfilePopupActivity, "Был введён неверный пароль", Toast.LENGTH_SHORT).show()
                returnIntent.putExtra("valid", false)
            }

            // Fade animation for the background of Popup Window when you press the back button
            val alpha = 100 // between 0-255
            val alphaColor = ColorUtils.setAlphaComponent(Color.parseColor("#000000"), alpha)
            val colorAnimation = ValueAnimator.ofObject(ArgbEvaluator(), alphaColor, Color.TRANSPARENT)
            colorAnimation.duration = 500 // milliseconds
            colorAnimation.addUpdateListener { animator ->
                popup_window_background.setBackgroundColor(
                    animator.animatedValue as Int
                )
            }

            // Fade animation for the Popup Window when you press the back button
            popup_window_view_with_border.animate().alpha(0f).setDuration(500).setInterpolator(
                DecelerateInterpolator()
            ).start()

            // After animation finish, close the Activity
            colorAnimation.addListener(object : AnimatorListenerAdapter() {
                override fun onAnimationEnd(animation: Animator) {
                    setResult(Activity.RESULT_OK, returnIntent)
                    finish()
                    overridePendingTransition(0, 0)
                }
            })
            colorAnimation.start()
        }
    }

    override fun onDestroy() {
        job.cancel() // cancel the Job
        super.onDestroy()
    }
}
