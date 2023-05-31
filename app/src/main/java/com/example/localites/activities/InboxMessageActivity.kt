package com.example.localites.activities

import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import com.bumptech.glide.Glide
import com.example.localites.R
import com.example.localites.helpers.Constants
import com.example.localites.helpers.Preferences
import com.example.localites.models.CompleteProfileModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore

class InboxMessageActivity : AppCompatActivity(), View.OnClickListener {

    var completeProfileModel: CompleteProfileModel? = null

    lateinit var firebaseAuth: FirebaseAuth
    lateinit var firebaseUser: FirebaseUser
    lateinit var firestore: FirebaseFirestore

    lateinit var toolbar: Toolbar
    lateinit var img_inbox_toolbarProfilePhoto: ImageView
    lateinit var tv_inbox_toolbarProfilename: TextView
    lateinit var img_InboxToolbarBackButton: ImageView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_inbox_message)
        completeProfileModel = intent.getParcelableExtra(Constants.completeProfile)

        firebaseAuth = FirebaseAuth.getInstance()
        firebaseUser = firebaseAuth.currentUser!!
        firestore = FirebaseFirestore.getInstance()

        initViews()

        setSupportActionBar(toolbar)

        if (completeProfileModel != null) {
            tv_inbox_toolbarProfilename.text = completeProfileModel!!.displayName

            Glide.with(this).load(completeProfileModel!!.profilePic)
                .error(R.drawable.ic_person_outlinewhite)
                .into(img_inbox_toolbarProfilePhoto)
        }

        getInboxMessages()
    }

    private fun initViews() {
        toolbar = findViewById(R.id.toolbar)
        tv_inbox_toolbarProfilename = findViewById(R.id.tv_InboxToolbarProfilename)
        img_inbox_toolbarProfilePhoto = findViewById(R.id.img_InboxToolbarProfilePhoto)
        img_InboxToolbarBackButton = findViewById(R.id.img_InboxToolbarBackButton)

        img_InboxToolbarBackButton.setOnClickListener(this)
    }

    private fun getInboxMessages() {
      /*  if (completeProfileModel != null && !completeProfileModel!!.uid.isNullOrEmpty()) {
            var mQuery = firestore.collection(Constants.inboxConversation)
                .whereEqualTo(
                    "[${Constants.inboxUser1},${Constants.inboxUser2}]", Preferences.uid
                ).whereEqualTo(
                    "${Constants.inboxUser1},${Constants.inboxUser2}",
                    completeProfileModel!!.uid
                )

            mQuery.get().addOnSuccessListener {
                if (!it.isEmpty)
                {

                }else
                {
                    //TODO: No conversation
                }
            }
        }*/
    }


    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.img_InboxToolbarBackButton -> {
                finish()
                return
            }
        }
    }

}
