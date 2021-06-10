package com.example.spotiflag

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Window
import android.widget.Button

class ErrorActivity : AppCompatActivity() {
    private val backHomeButton: Button by lazy {
        findViewById(R.id.backhome_button)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        setContentView(R.layout.activity_error)
    }

    override fun onStart() {
        super.onStart()

        backHomeButton.setOnClickListener {
            backHomeButton.isEnabled = false

            val newActivity = Intent(this@ErrorActivity, HomeActivity::class.java)
            startActivity(newActivity)
            finish()
        }
    }
}