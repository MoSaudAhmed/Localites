package com.example.localites.activities

import android.content.Intent
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.MenuItem
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.Spinner
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.cardview.widget.CardView
import androidx.core.content.FileProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.localites.BuildConfig
import com.example.localites.R
import com.example.localites.adapters.MarketAdapter
import com.example.localites.helpers.ImageUtils
import com.example.localites.interfaces.MarketCallbacks
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import java.io.File
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

class CreateMarketAdd : AppCompatActivity(), View.OnClickListener, MarketCallbacks {

    lateinit var toolbar: Toolbar
    lateinit var tv_toolbar_title: TextView
    lateinit var rv_createMarket_ad: RecyclerView
    lateinit var cardView_createMarkedAd_Submit: CardView
    lateinit var spinner_createMarketAd_type: Spinner
    lateinit var et_createMarketAd_location: EditText
    lateinit var et_createMarketAd_description: EditText
    lateinit var et_createMarketAd_title: EditText

    lateinit var firebaseAuth: FirebaseAuth
    lateinit var firebaseUser: FirebaseUser

    var picturesList: MutableList<String> = ArrayList()
    lateinit var marketAdapter: MarketAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_create_market_add)

        firebaseAuth = FirebaseAuth.getInstance()
        firebaseUser = firebaseAuth.currentUser!!

        initViews()

        setSupportActionBar(toolbar)
        supportActionBar!!.setHomeButtonEnabled(true)
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)
        tv_toolbar_title.text = "Create Ad"

        marketAdapter = MarketAdapter(picturesList, this, this)

        rv_createMarket_ad.layoutManager =
            LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        rv_createMarket_ad.setHasFixedSize(true)
        rv_createMarket_ad.adapter = marketAdapter

    }

    private fun initViews() {
        toolbar = findViewById(R.id.toolbar)
        toolbar = findViewById(R.id.toolbar)
        tv_toolbar_title = findViewById(R.id.tv_toolbar_title)
        rv_createMarket_ad = findViewById(R.id.rv_createMarket_ad)
        cardView_createMarkedAd_Submit = findViewById(R.id.cardView_createMarkedAd_Submit)
        spinner_createMarketAd_type = findViewById(R.id.spinner_createMarketAd_type)
        et_createMarketAd_location = findViewById(R.id.et_createMarketAd_location)
        et_createMarketAd_description = findViewById(R.id.et_createMarketAd_description)
        et_createMarketAd_title = findViewById(R.id.et_createMarketAd_title)

    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> {
                finish()
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }


    override fun onClick(v: View?) {
        when (v?.id) {

        }
    }

    override fun onPictureRemoveClicked(position: Int) {
        Log.e("ItemClickedElse", "${picturesList.size} , $position")
    }

    override fun onPictureReplaceClicked(position: Int) {
        TODO("Not yet implemented")
    }

    override fun onPictureAddClicked(position: Int) {
        Log.e("ItemClickedElse", "${picturesList.size} , $position")

        imagePickerDialog()
    }


    fun imagePickerDialog() {
        var alertDialog = AlertDialog.Builder(this)
        alertDialog.setTitle("Choose an action and Select upto 3 Images")

        alertDialog.setPositiveButton("Camera", null)
        alertDialog.setNeutralButton("Gallery", null)
        val dialog = alertDialog.create()

        dialog.setOnShowListener {
            val buttonPositive: Button =
                dialog.getButton(AlertDialog.BUTTON_POSITIVE)
            val buttonNeutral: Button =
                dialog.getButton(AlertDialog.BUTTON_NEUTRAL)
            buttonPositive.isAllCaps = false
            buttonNeutral.isAllCaps = false

            buttonNeutral.setOnClickListener {
                dialog.dismiss()
                takeGalleryPicture()
                //Gallery
            }
            buttonPositive.setOnClickListener {
                dialog.dismiss()
                //Camera
                takeCameraPicture()
            }
        }

        dialog.show()
    }


    private fun takeCameraPicture() {
        val timeStamp =
            SimpleDateFormat("yyyyMMddHHmmss", Locale.US)
                .format(Date())
        var imageFileName = "$timeStamp.png"
        val outPutFile =
            File(ImageUtils.filePath(this@CreateMarketAdd, imageFileName!!))
        val cameraIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        val mImageCaptureUri = FileProvider.getUriForFile(
            this@CreateMarketAdd,
            BuildConfig.APPLICATION_ID.toString() + ".provider",
            outPutFile
        )
        cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, mImageCaptureUri)
        startActivityForResult(cameraIntent, CAMERA_IMAGE_CAPTURE)
    }

    private fun takeGalleryPicture() {
        val intent = Intent()
        intent.type = "image/*"
        intent.action = Intent.ACTION_GET_CONTENT
        startActivityForResult(
            Intent.createChooser(intent, "Select Picture"),
            GALLERY_IMAGE_CAPTURE
        )
    }
}
