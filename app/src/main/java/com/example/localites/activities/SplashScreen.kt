package com.example.localites.activities

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.localites.R
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class SplashScreen : AppCompatActivity() {

    private var mFirebaseAnalytics: FirebaseAnalytics? = null
    private lateinit var mAuth: FirebaseAuth
    private lateinit var mUser: FirebaseUser


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash_screen)

        mFirebaseAnalytics = FirebaseAnalytics.getInstance(this)

        CoroutineScope(Dispatchers.IO).launch {
            delay(1500)
        }
        var intent: Intent
        if (FirebaseAuth.getInstance().currentUser == null) {
            intent = Intent(this, LoginActivity::class.java)
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK)
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            startActivity(intent)
        } else {
            intent = Intent(this@SplashScreen, MainActivity::class.java)
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK)
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            startActivity(intent)
        }
        overridePendingTransition(
            R.anim.activity_in,
            R.anim.activity_out
        )
    }
}
