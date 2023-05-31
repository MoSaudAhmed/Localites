package com.example.localites.activities

import android.os.Bundle
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.localites.R
import com.example.localites.helpers.Constants
import com.example.localites.models.CompleteProfileModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore

class InboxActivity : AppCompatActivity() {

    lateinit var tv_InboxToolbarProfilename: TextView
    lateinit var img_InboxToolbarProfilePhoto: ImageView
    lateinit var img_InboxToolbarBackButton: ImageView

    lateinit var completeProfileModel: CompleteProfileModel

    lateinit var firestore: FirebaseFirestore
    lateinit var firebaseUser: FirebaseUser

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_inbox)

        completeProfileModel = intent.getParcelableExtra(Constants.completeProfile)

        initViews()
        firestore = FirebaseFirestore.getInstance()
        firebaseUser = FirebaseAuth.getInstance().currentUser!!

        if (completeProfileModel != null) {
            getInboxReference()
        }
    }

    private fun getInboxReference() {

        Toast.makeText(this, "Load inbox reference here", Toast.LENGTH_LONG).show()
    }

    private fun initViews() {

        img_InboxToolbarBackButton = findViewById(R.id.img_InboxToolbarBackButton)
        img_InboxToolbarProfilePhoto = findViewById(R.id.img_InboxToolbarProfilePhoto)
        tv_InboxToolbarProfilename = findViewById(R.id.tv_InboxToolbarProfilename)
    }
}
