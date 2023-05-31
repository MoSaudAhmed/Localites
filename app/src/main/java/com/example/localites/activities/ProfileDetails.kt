package com.example.localites.activities

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.cardview.widget.CardView
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.core.widget.NestedScrollView
import com.bumptech.glide.Glide
import com.example.localites.R
import com.example.localites.helpers.*
import com.example.localites.models.CompleteProfileModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import kotlinx.android.synthetic.main.activity_profile_details.*


class ProfileDetails : AppCompatActivity(), View.OnClickListener {

    lateinit var toolbar: Toolbar
    lateinit var tv_toolbarTitle: TextView

    lateinit var lay_profileDetails: CoordinatorLayout
    lateinit var profileDetails_nestedScrollView: NestedScrollView
    lateinit var tv_profileDetails_location: TextView
    lateinit var tv_profileDetails_email: TextView
    lateinit var et_profileDetails_name: TextView
    lateinit var et_profileDetails_gender: TextView
    lateinit var tv_profileDetails_mobile: TextView
    lateinit var et_profileDetails_yourSelf: TextView
    lateinit var tv_profileDetails_Occupation: TextView
    lateinit var tv_profileDetails_Website: TextView
    lateinit var et_profileDetails_Facebook: TextView
    lateinit var et_profileDetails_Instagram: TextView
    lateinit var et_profileDetails_Youtube: TextView

    lateinit var card_ProfileDetails_report: CardView
    lateinit var lay_profileDetails_anchor: LinearLayout

    lateinit var tv_profileDetails_followers: TextView
    lateinit var img_ProfileDetails_follow: ImageView
    lateinit var img_ProfileDetails_message: ImageView

    var completeProfileModel: CompleteProfileModel? = null
    var userUID: String? = null
    var isFollowing = false

    lateinit var firebaseAuth: FirebaseAuth
    lateinit var firebaseUser: FirebaseUser
    lateinit var firestore: FirebaseFirestore

    lateinit var circularProgressBar: CircularProgressBar

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile_details)
        overridePendingTransition(R.anim.activity_in, R.anim.activity_out)
        completeProfileModel = intent.getParcelableExtra("profile")

        userUID = intent.getStringExtra(Constants.uid)

        initViews()
        circularProgressBar = CircularProgressBar(this)

        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setHomeButtonEnabled(true)

        tv_toolbarTitle.setText("Profile Details")

        firebaseAuth = FirebaseAuth.getInstance()
        firebaseUser = firebaseAuth.currentUser!!
        firestore = FirebaseFirestore.getInstance()

        if (completeProfileModel != null) {
            updateProfile(completeProfileModel!!)
        } else {
            getUserData()
        }

    }

    private fun isFollowing(completeProfileModel: CompleteProfileModel) {

        var docRef = firestore.collection(Constants.users).document(firebaseUser.uid)
            .collection(Constants.followers).document(completeProfileModel!!.uid.toString())
        docRef.get()
            .addOnSuccessListener {
                if (it.exists()) {
                    isFollowing = true
                    img_ProfileDetails_follow.setImageDrawable(resources.getDrawable(R.drawable.ic_follow))
                } else {
                    img_ProfileDetails_follow.setImageDrawable(resources.getDrawable(R.drawable.ic_unfollow))
                }
            }.addOnFailureListener {
                img_ProfileDetails_follow.setImageDrawable(resources.getDrawable(R.drawable.ic_unfollow))
            }
    }

    private fun initViews() {
        toolbar = findViewById(R.id.toolbar)
        tv_toolbarTitle = findViewById(R.id.tv_toolbar_title)

        lay_profileDetails = findViewById(R.id.lay_profileDetails)
        profileDetails_nestedScrollView = findViewById(R.id.profileDetails_nestedScrollView)
        tv_profileDetails_location = findViewById(R.id.tv_profileDetails_location)
        tv_profileDetails_email = findViewById(R.id.tv_profileDetails_email)
        et_profileDetails_name = findViewById(R.id.et_profileDetails_name)
        et_profileDetails_gender = findViewById(R.id.et_profileDetails_gender)
        tv_profileDetails_mobile = findViewById(R.id.tv_profileDetails_mobile)
        et_profileDetails_yourSelf = findViewById(R.id.et_profileDetails_yourSelf)
        tv_profileDetails_Occupation = findViewById(R.id.tv_profileDetails_Occupation)
        tv_profileDetails_Website = findViewById(R.id.tv_profileDetails_Website)
        et_profileDetails_Facebook = findViewById(R.id.et_profileDetails_Facebook)
        et_profileDetails_Instagram = findViewById(R.id.et_profileDetails_Instagram)
        et_profileDetails_Youtube = findViewById(R.id.et_profileDetails_Youtube)
        card_ProfileDetails_report = findViewById(R.id.card_ProfileDetails_report)
        lay_profileDetails_anchor = findViewById(R.id.lay_profileDetails_anchor)
        tv_profileDetails_followers = findViewById(R.id.tv_profileDetails_followers)
        img_ProfileDetails_follow = findViewById(R.id.img_ProfileDetails_follow)
        img_ProfileDetails_message = findViewById(R.id.img_ProfileDetails_message)

        img_ProfileDetails_follow.setOnClickListener(this)
        img_ProfileDetails_message.setOnClickListener(this)
        card_ProfileDetails_report.setOnClickListener(this)

        profileDetails_nestedScrollView.setOnScrollChangeListener { v: NestedScrollView?, scrollX: Int, scrollY: Int, oldScrollX: Int, oldScrollY: Int ->
            if (scrollY > oldScrollY) {
                lay_profileDetails_pic.animate().scaleX(0.4F).scaleY(0.4F).setDuration(300).start()
                img_ProfileDetails_follow.animate().scaleX(0.4F).scaleY(0.4F).setDuration(300)
                    .start()
                img_ProfileDetails_message.animate().scaleX(0.4F).scaleY(0.4F).setDuration(300)
                    .start()
                card_ProfileDetails_report.animate().scaleX(0.4F).scaleY(0.4F).setDuration(300)
                    .start()
            } else if (oldScrollY > scrollY) {
                lay_profileDetails_pic.animate().scaleX(1F).scaleY(1F).setDuration(300).start()
                img_ProfileDetails_follow.animate().scaleX(1F).scaleY(1F).setDuration(300).start()
                img_ProfileDetails_message.animate().scaleX(1F).scaleY(1F).setDuration(300).start()
                card_ProfileDetails_report.animate().scaleX(1F).scaleY(1F).setDuration(300).start()
            }
            if (scrollY == 0) {
                lay_profileDetails_pic.animate().scaleX(1F).scaleY(1F).setDuration(300).start()
                img_ProfileDetails_follow.animate().scaleX(1F).scaleY(1F).setDuration(300).start()
                img_ProfileDetails_message.animate().scaleX(1F).scaleY(1F).setDuration(300).start()
                card_ProfileDetails_report.animate().scaleX(1F).scaleY(1F).setDuration(300).start()
            }
        }
    }

    private fun getUserData() {
        var circularProgressBar=CircularProgressBar(this)
        circularProgressBar.showProgressDialog()
        if (userUID != null) {
            firestore.collection(Constants.users).document(userUID.toString()).get()
                .addOnSuccessListener { documentSnapshot ->
                    if (documentSnapshot != null && documentSnapshot.exists()) {
                        completeProfileModel =
                            documentSnapshot.toObject(CompleteProfileModel::class.java)!!
                        updateProfile(completeProfileModel!!)

                    } else {
                        showSnackbar(this, lay_profileDetails, "User details not found")
                    }
                    circularProgressBar.dismissProgressDialog()
                }.addOnFailureListener {
                    circularProgressBar.dismissProgressDialog()
                    showSnackbar(this, lay_profileDetails, "User details not found")
                }
        } else {
            showSnackbar(this, lay_profileDetails, "User details not found")
            circularProgressBar.dismissProgressDialog()
        }
    }

    fun updateProfile(completeProfileModel: CompleteProfileModel) {

        if (!completeProfileModel.uid.toString().equals(firebaseUser.uid)) {
            isFollowing(completeProfileModel)
        }
        getFollowers()

        if (Preferences.locationCountry?.isNotEmpty()!! || Preferences.locationCity?.isNotEmpty()!!) {
            tv_profileDetails_location.setText("${Preferences.locationCountry}, ${Preferences.locationCity}")
        } else {
            tv_profileDetails_location.text = "n / a"
        }
        if (Preferences.email?.isNotEmpty()!!) {
            tv_profileDetails_email.setText("${Preferences.email}")
        } else {
            tv_profileDetails_email.setText("n / a")
        }
        if (completeProfileModel.displayName?.isNotEmpty()!!) {
            et_profileDetails_name.setText("${TextChanges().capitalize(completeProfileModel.displayName.toString())}")
        } else {
            et_profileDetails_name.setText("n / a")
        }
        if (completeProfileModel.gender?.isNotEmpty()!!) {
            et_profileDetails_gender.setText("${TextChanges().capitalize(completeProfileModel.gender.toString())}")
        } else {
            et_profileDetails_gender.setText("n / a")
        }
        if (completeProfileModel.mobile?.isNotEmpty()!!) {
            tv_profileDetails_mobile.setText("${completeProfileModel.mobile}")
        } else {
            tv_profileDetails_mobile.setText("n / a")
        }
        if (completeProfileModel.aboutYourSelf?.isNotEmpty()!!) {
            et_profileDetails_yourSelf.setText("${completeProfileModel.aboutYourSelf}")
        } else {
            et_profileDetails_yourSelf.setText("n / a")
        }
        if (completeProfileModel.occupation?.isNotEmpty()!!) {
            tv_profileDetails_Occupation.setText("${TextChanges().capitalize(completeProfileModel.occupation.toString())}")
        } else {
            tv_profileDetails_Occupation.setText("n / a")
        }
        if (completeProfileModel.website?.isNotEmpty()!!) {
            tv_profileDetails_Website.setText("${completeProfileModel.website}")
        } else {
            tv_profileDetails_Website.setText("n / a")
            tv_profileDetails_Website.setTextColor(resources.getColor(android.R.color.black))
        }

        if (completeProfileModel.facebook?.isNotEmpty()!!) {
            if (completeProfileModel.facebook!!.contains(Constants.facebookLink)) {
                completeProfileModel.facebook!!.replace(Constants.facebookLink, "")
            }
            et_profileDetails_Facebook.setText("${Constants.facebookLink}${completeProfileModel.facebook}")
        } else {
            et_profileDetails_Facebook.setText("${Constants.facebookLink}")
        }
        if (completeProfileModel.instagram?.isNotEmpty()!!) {
            if (completeProfileModel.instagram!!.contains(Constants.instagramLink)) {
                completeProfileModel.instagram!!.replace(Constants.instagramLink, "")
            }
            et_profileDetails_Instagram.setText("${Constants.instagramLink}${completeProfileModel.instagram}")
        } else {
            et_profileDetails_Instagram.setText("${Constants.instagramLink}")
        }
        if (completeProfileModel.youtube?.isNotEmpty()!!) {
            if (completeProfileModel.youtube!!.contains(Constants.youtubeLink)) {
                completeProfileModel.youtube!!.replace(Constants.youtubeLink, "")
            }
            et_profileDetails_Youtube.setText("${Constants.youtubeLink}${completeProfileModel.youtube}")
        } else {
            et_profileDetails_Instagram.setText("${Constants.youtubeLink}")
        }

        if (completeProfileModel.uid.toString().equals(firebaseUser.uid.toString())) {
            Glide.with(this)
                .load(Preferences.profilePic)
                .error(R.drawable.ic_person)
                .into(img_profileDetailsPic)
        } else {
            Glide.with(this).load(completeProfileModel.profilePic).error(R.drawable.ic_person)
                .into(img_profileDetailsPic)
        }

        if (!completeProfileModel.coverPic.isNullOrEmpty()) {
            if (Integer.parseInt(completeProfileModel.coverPic!!) != -1) {
                lay_profileDetails_anchor.setBackgroundResource(
                    Constants.coverImagesList.get(
                        Integer.parseInt(completeProfileModel.coverPic!!)
                    )
                )
            }
        }
        if (completeProfileModel!!.facebook!!.isNotEmpty()) {
            et_profileDetails_Facebook.setOnClickListener(this)
        }
        if (completeProfileModel!!.instagram!!.isNotEmpty()) {
            et_profileDetails_Instagram.setOnClickListener(this)
        }
        if (completeProfileModel!!.youtube!!.isNotEmpty()) {
            et_profileDetails_Youtube.setOnClickListener(this)
        }
        if (completeProfileModel.website!!.isNotEmpty()) {
            tv_profileDetails_Website.setOnClickListener(this)
        }
    }

    var totalFollowers = 0

    private fun getFollowers() {
        var ref =
            firestore.collection(Constants.users).document(completeProfileModel!!.uid.toString())
        ref.get().addOnSuccessListener {
            if (it.contains(Constants.followers)) {
                tv_profileDetails_followers.text = "${it.get(Constants.followers)} followers"
                totalFollowers = (it.get(Constants.followers) as Long).toInt()

            } else {
                tv_profileDetails_followers.text = "0 followers"
            }
            if (it.contains(Constants.email)) {
                tv_profileDetails_email.text = it.get(Constants.email).toString()
            } else {
                tv_profileDetails_email.text = "n / a"
            }
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> {
                onBackPressed()
                return true
            }
            else -> {
                return super.onOptionsItemSelected(item)
            }
        }
    }

    private fun inboxClicked() {
        if (completeProfileModel!!.uid.toString().equals(firebaseUser.uid)) {
            showSnackbar(this, lay_profileDetails, "Can't message yourself")
            return
        }
        var intent = Intent(this, InboxMessageActivity::class.java)
        intent.putExtra(Constants.completeProfile, completeProfileModel)
        startActivity(intent)
    }

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.img_ProfileDetails_follow -> {
                if (userUID.toString().equals(firebaseUser.uid.toString())) {
                    showSnackbar(this, lay_profileDetails, "Can't Follow your own profile")
                } else {
                    if (!isFollowing) {
                        followCalled()
                    } else {
                        unfollowCalled()
                    }
                }
                return
            }
            R.id.img_ProfileDetails_message -> {
                inboxClicked()
                return
            }

            R.id.card_ProfileDetails_report -> {
                if (userUID.toString().equals(firebaseUser.uid.toString())) {
                    showSnackbar(this, lay_profileDetails, "Can't report on your own profile")
                } else {
                    Toast.makeText(this, "Not yet implemented", Toast.LENGTH_LONG).show()
                }
                return
            }
            R.id.et_profileDetails_Facebook -> {
                onAccountLinksClicked(et_profileDetails_Facebook.text.toString())
            }
            R.id.et_profileDetails_Instagram -> {
                onAccountLinksClicked(et_profileDetails_Instagram.text.toString())
            }
            R.id.et_profileDetails_Youtube -> {
                onAccountLinksClicked(et_profileDetails_Youtube.text.toString())
            }
            R.id.tv_profileDetails_Website -> {
                onAccountLinksClicked(tv_profileDetails_Website.text.toString())
            }
        }
    }

    fun onAccountLinksClicked(link: String) {
        var updatedLink: String = ""
        if (!link.startsWith("https:")) {
            updatedLink = "https://" + link
        } else {
            updatedLink = link
        }
        startActivity(
            Intent(
                Intent.ACTION_VIEW,
                Uri.parse(updatedLink)
            )
        )
    }

    private fun followCalled() {
        var circularProgressBar=CircularProgressBar(this)
        circularProgressBar.showProgressDialog()

        var map = HashMap<String, String>()
        map.put(completeProfileModel!!.uid.toString(), "")
        firestore.collection(Constants.users).document(firebaseUser.uid.toString())
            .collection(Constants.followers).document(completeProfileModel!!.uid.toString()).set(
                map, SetOptions.merge()
            ).addOnSuccessListener {
                isFollowing = true
                img_ProfileDetails_follow.setImageDrawable(resources.getDrawable(R.drawable.ic_follow))
                var userFollowIncrementRef =
                    firestore.collection(Constants.users)
                        .document(completeProfileModel!!.uid.toString())
                userFollowIncrementRef.update(Constants.followers, FieldValue.increment(1))
                tv_profileDetails_followers.text = "${totalFollowers + 1} followers"

                circularProgressBar.dismissProgressDialog()
            }.addOnFailureListener {
                Toast.makeText(this, "Something went wrong", Toast.LENGTH_LONG).show()
                circularProgressBar.dismissProgressDialog()
            }

    }

    private fun unfollowCalled() {
        var circularProgressBar=CircularProgressBar(this)
        circularProgressBar.showProgressDialog()
        var myUnfollowRef =
            firestore.collection(Constants.users).document(firebaseUser.uid.toString())
                .collection(Constants.followers).document(completeProfileModel!!.uid.toString())
        myUnfollowRef.get().addOnSuccessListener {
            if (it.exists()) {
                myUnfollowRef.delete()
                var userFollowIncrementRef =
                    firestore.collection(Constants.users)
                        .document(completeProfileModel!!.uid.toString())
                userFollowIncrementRef.update(Constants.followers, FieldValue.increment(-1))

            }
            isFollowing = false
            img_ProfileDetails_follow.setImageDrawable(resources.getDrawable(R.drawable.ic_unfollow))
            tv_profileDetails_followers.text = "${totalFollowers - 1} followers"
            circularProgressBar.dismissProgressDialog()
        }.addOnFailureListener {
            Toast.makeText(this, "Something went wrong", Toast.LENGTH_LONG)
                .show()
            circularProgressBar.dismissProgressDialog()
        }
    }
}
