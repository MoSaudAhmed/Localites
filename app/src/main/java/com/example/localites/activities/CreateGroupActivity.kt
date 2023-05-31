package com.example.localites.activities

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.app.ProgressDialog
import android.content.ContentResolver
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Color
import android.location.*
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.provider.Settings
import android.text.Editable
import android.text.SpannableString
import android.text.TextPaint
import android.text.TextWatcher
import android.text.method.LinkMovementMethod
import android.text.style.ClickableSpan
import android.util.Log
import android.view.MenuItem
import android.view.View
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.cardview.widget.CardView
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import com.bumptech.glide.Glide
import com.example.localites.BuildConfig
import com.example.localites.R
import com.example.localites.helpers.*
import com.example.localites.models.CreateGroupModel
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
import com.google.android.material.textfield.TextInputLayout
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.google.gson.Gson
import com.theartofdev.edmodo.cropper.CropImage
import id.zelory.compressor.Compressor
import java.io.File
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.HashMap


class CreateGroupActivity : AppCompatActivity(), View.OnClickListener, LocationListener {

    lateinit var btn_CreateGroup_create: Button
    lateinit var tv_CreateGroupTermsAndPolicy: TextView
    lateinit var cb_CreateGroupTermsAndPrivacy: CheckBox
    lateinit var et_GroupDescription: EditText
    lateinit var textInputCreateGroupDescription: TextInputLayout
    lateinit var et_GroupLocation_country: EditText
    lateinit var et_GroupLocation_state: EditText
    lateinit var et_GroupLocation_city: EditText
    lateinit var textInputCreateGroupLocation_country: TextInputLayout
    lateinit var textInputCreateGroupLocation_state: TextInputLayout
    lateinit var textInputCreateGroupLocation_city: TextInputLayout
    lateinit var et_GroupName: EditText
    lateinit var textInputCreateGroupName: TextInputLayout
    lateinit var toolbar: Toolbar
    lateinit var tv_toolbar_title: TextView
    lateinit var registration_Gender_group: RadioGroup
    lateinit var registration_radioButtonOnlyMe: RadioButton
    lateinit var registration_radioButtonEveryone: RadioButton
    lateinit var card_CreateGroupPic: CardView
    lateinit var tv_CreateGroupPic: TextView
    lateinit var img_CreateGroupPic: ImageView
    lateinit var lay_CreateGroup: LinearLayout
    lateinit var chipsGroup_createGroup_Type: ChipGroup

    lateinit var circularProgressBar: CircularProgressBar

    private var imageFileName: String? = null
    private val CAMERA_IMAGE_CAPTURE = 1
    private val GALLERY_IMAGE_CAPTURE = 2
    private var lengthbmp: Long = 0
    private var imageBitmap: Bitmap? = null
    internal lateinit var byteImage: ByteArray
    var progressDialog: ProgressDialog? = null

    private var mStorageRef: StorageReference? = null
    lateinit var firestore: FirebaseFirestore
    lateinit var firebaseUser: FirebaseUser

    lateinit var service: LocationManager
    var location: Location? = null
    lateinit var permissions: Permissions


    var groupItemsList: MutableList<String> = ArrayList()

    var createGroupModel: CreateGroupModel? = null
    var from: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_create_group)
        initViews()
        textWatchers()
        termsAndPrivacySpannableString()
        permissions = Permissions(this)

        circularProgressBar = CircularProgressBar(this)

        createGroupModel = intent.getParcelableExtra("data")
        from = intent.getStringExtra("from")


        setSupportActionBar(toolbar)
        supportActionBar?.setHomeButtonEnabled(true)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        if (from != null && from.equals("updateGroup")) {
            tv_toolbar_title.setText("Update Group")
            btn_CreateGroup_create.text = "Update"
        } else {
            tv_toolbar_title.setText("Create Group")
        }

        mStorageRef = FirebaseStorage.getInstance().getReference()
        firestore = FirebaseFirestore.getInstance()
        firebaseUser = FirebaseAuth.getInstance().currentUser!!


        getLocation()

        if (createGroupModel != null) {
            Glide.with(this).load(createGroupModel!!.groupCoverPic).into(img_CreateGroupPic)
            et_GroupName.setText(createGroupModel!!.groupName.toString())
            et_GroupDescription.setText(createGroupModel!!.grroupDescription.toString())
            et_GroupLocation_country.setText(createGroupModel!!.groupLocationCountry.toString())
            et_GroupLocation_state.setText(createGroupModel!!.groupLocationState.toString())
            et_GroupLocation_city.setText(createGroupModel!!.groupLocationCity.toString())
            cb_CreateGroupTermsAndPrivacy.isChecked = true
            if (createGroupModel!!.onlyOwnerCanPost) {
                registration_radioButtonOnlyMe.isChecked = true
            } else {
                registration_radioButtonEveryone.isChecked = true
            }
            groupItemsList = createGroupModel!!.groupTypeList as MutableList<String>

            Log.e("GroupItemsList", Gson().toJson(groupItemsList))
            chipsGroup_createGroup_Type.removeAllViews()

        }
        addItemsToGroupType()
    }

    fun addItemsToGroupType() {
        addItemToGroup("Blogging")
        addItemToGroup("Bookmarking")
        addItemToGroup("Discussion")
        addItemToGroup("Economy")
        addItemToGroup("Friendship")
        addItemToGroup("Food")
        addItemToGroup("Fun")
        addItemToGroup("Gaming")
        addItemToGroup("Learning")
        addItemToGroup("Motives And Goals")
        addItemToGroup("Movies")
        addItemToGroup("News")
        addItemToGroup("Review")
        addItemToGroup("Sports")
        addItemToGroup("Shopping")
    }

    @SuppressLint("MissingPermission")
    private fun getLastKnownLocation() {
        var mLocationManager =
            applicationContext.getSystemService(Context.LOCATION_SERVICE) as LocationManager
        val providers: List<String> = mLocationManager.getProviders(true)
        var bestLocation: Location? = null
        for (provider in providers) {
            val l: Location = mLocationManager.getLastKnownLocation(provider) ?: continue
            if (bestLocation == null || l.accuracy < bestLocation.accuracy) {
                // Found best last known location: %s", l);
                bestLocation = l
            }
        }
        if (bestLocation != null) {
            gotLocation(bestLocation!!.latitude, bestLocation.longitude)
        }

    }

    @SuppressLint("MissingPermission")
    override fun onResume() {
        super.onResume()
        val criteria = Criteria()
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)
            != PackageManager.PERMISSION_GRANTED
        ) {
            Toast.makeText(this, "Please allow", Toast.LENGTH_LONG).show()
            permissions.checkLocationPermissions()
        } else {
            var provider = service.getBestProvider(criteria, false)
            service.requestLocationUpdates(provider, 400, 1F, this)
            getLastKnownLocation()
        }

    }

    override fun onPause() {
        super.onPause()
        service.removeUpdates(this)
    }

    fun getLocation() {

        service =
            getSystemService(Context.LOCATION_SERVICE) as LocationManager
        val enabled: Boolean = service
            .isProviderEnabled(LocationManager.GPS_PROVIDER)
        if (!enabled) {
            showGpsDialog()
        }
    }


    private fun showGpsDialog() {
        var alertDialog: AlertDialog.Builder = AlertDialog.Builder(this)
        alertDialog.setTitle("GPS is turned off")
        alertDialog.setCancelable(false)
        alertDialog.setMessage("Please turn on the GPS to fetch your current location, Else click cancel and enter manually")
        alertDialog.setNegativeButton("Go to Settings",
            object : DialogInterface.OnClickListener {
                override fun onClick(dialog: DialogInterface?, which: Int) {
                    val intent = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
                    startActivity(intent)
                }
            })

        alertDialog.setNeutralButton("Cancel", object : DialogInterface.OnClickListener {
            override fun onClick(dialog: DialogInterface?, which: Int) {
                dialog?.dismiss()
            }
        })
        alertDialog.show()
    }

    private fun initViews() {
        btn_CreateGroup_create = findViewById(R.id.btn_CreateGroup_create)
        cb_CreateGroupTermsAndPrivacy = findViewById(R.id.cb_CreateGroupTermsAndPrivacy)
        tv_CreateGroupTermsAndPolicy = findViewById(R.id.tv_CreateGroupTermsAndPolicy)
        textInputCreateGroupDescription = findViewById(R.id.textInputCreateGroupDescription)
        et_GroupDescription = findViewById(R.id.et_GroupDescription)
        et_GroupLocation_country = findViewById(R.id.et_GroupLocation_country)
        et_GroupLocation_state = findViewById(R.id.et_GroupLocation_state)
        et_GroupLocation_city = findViewById(R.id.et_GroupLocation_city)
        textInputCreateGroupLocation_country =
            findViewById(R.id.textInputCreateGroupLocation_counrty)
        textInputCreateGroupLocation_state = findViewById(R.id.textInputCreateGroupLocation_state)
        textInputCreateGroupLocation_city = findViewById(R.id.textInputCreateGroupLocation_city)
        et_GroupName = findViewById(R.id.et_GroupName)
        textInputCreateGroupName = findViewById(R.id.textInputCreateGroupName)
        toolbar = findViewById(R.id.toolbar)
        tv_toolbar_title = findViewById(R.id.tv_toolbar_title)
        registration_Gender_group = findViewById(R.id.registration_Gender_group)
        registration_radioButtonOnlyMe = findViewById(R.id.registration_radioButtonOnlyMe)
        registration_radioButtonEveryone = findViewById(R.id.registration_radioButtonEveryone)
        tv_CreateGroupPic = findViewById(R.id.tv_CreateGroupPic)
        card_CreateGroupPic = findViewById(R.id.card_CreateGroupPic)
        img_CreateGroupPic = findViewById(R.id.img_CreateGroupPic)
        lay_CreateGroup = findViewById(R.id.lay_CreateGroup)
        chipsGroup_createGroup_Type = findViewById(R.id.chipsGroup_createGroup_Type)

        btn_CreateGroup_create.setOnClickListener(this)
        card_CreateGroupPic.setOnClickListener(this)

        cb_CreateGroupTermsAndPrivacy.setOnCheckedChangeListener { buttonView, isChecked ->
            cb_CreateGroupTermsAndPrivacy.error = null
        }
        registration_Gender_group.setOnCheckedChangeListener { group, checkedId ->
            registration_radioButtonEveryone.error = null
        }
    }

    private fun textWatchers() {
        et_GroupName.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                textInputCreateGroupName.error = null
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
            }
        })
        et_GroupDescription.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                textInputCreateGroupDescription.error = null
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
            }
        })
        et_GroupLocation_country.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                textInputCreateGroupLocation_country.error = null
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
            }
        })
        et_GroupLocation_state.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                textInputCreateGroupLocation_state.error = null
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
            }
        })
        et_GroupLocation_city.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                textInputCreateGroupLocation_city.error = null
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
            }
        })
    }

    override fun onClick(v: View?) {

        when (v?.id) {

            R.id.btn_CreateGroup_create -> {
                validateAndContinue()
                return
            }
            R.id.card_CreateGroupPic -> {
                val permissions = Permissions(this@CreateGroupActivity)
                if (permissions.checkAndRequestPermissions()) {
                    imagePickerDialog()
                } else {
                    Toast.makeText(
                        this@CreateGroupActivity,
                        resources.getString(R.string.allow_premission_to_store_data),
                        Toast.LENGTH_LONG
                    ).show()
                }
                return
            }
        }
    }


    private fun addItemToGroup(text: String) {
        var chip = Chip(this)
        if (from != null && groupItemsList.contains(text)) {
            chip.setChipBackgroundColorResource(R.color.backgroundYellow)
        }

        Log.e("AddItemToGroup", text)
        chip.setCloseIconVisible(false)
        chip.setTextColor(Color.BLACK)
        chip.text = TextChanges().capitalize(text)
        chipsGroup_createGroup_Type.addView(chip)
        chip.setOnClickListener {
            if (groupItemsList.contains(chip.text.toString())) {
                chip.setChipBackgroundColorResource(R.color.background_gray)
                groupItemsList.remove(chip.text.toString())
            } else {
                chip.setChipBackgroundColorResource(R.color.backgroundYellow)
                groupItemsList.add(chip.text.toString())
            }
        }
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

    private fun imagePickerDialog() {
        val alertDialog = AlertDialog.Builder(this)
        alertDialog.setTitle("Choose an action")

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
        imageFileName = "$timeStamp.png"
        val outPutFile =
            File(ImageUtils.filePath(this@CreateGroupActivity, imageFileName!!))
        val cameraIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        val mImageCaptureUri = FileProvider.getUriForFile(
            this@CreateGroupActivity,
            BuildConfig.APPLICATION_ID + ".provider",
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

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == CAMERA_IMAGE_CAPTURE && resultCode == Activity.RESULT_OK) {
            if (imageFileName != null) {
                val f: File
                val sourceUri = Uri.fromFile(
                    File(
                        ImageUtils.filePath(
                            this@CreateGroupActivity,
                            imageFileName!!
                        )
                    )
                )
                val timeStamp =
                    SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US)
                        .format(Date())
                val editedimageFileName = timeStamp + "_.png"
                val destinationUri = Uri.fromFile(
                    File(
                        ImageUtils.filePath(
                            this@CreateGroupActivity,
                            editedimageFileName
                        )
                    )
                )
                val scheme = sourceUri.scheme
                if (scheme != null) {
                    if (scheme == ContentResolver.SCHEME_CONTENT) {
                        try {
                            val fileInputStream =
                                applicationContext.contentResolver
                                    .openInputStream(sourceUri)
                            if (fileInputStream != null) {
                                lengthbmp = fileInputStream.available() / 1024.toLong()
                            }
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                    } else if (scheme == ContentResolver.SCHEME_FILE) {
                        val path = sourceUri.path
                        try {
                            if (path != null) {
                                f = File(path)
                                lengthbmp = f.length() / 1024
                            }
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                    }
                }
                val file: File = FileUtil.from(this@CreateGroupActivity, sourceUri!!)
                val compressedImageFile =
                    Compressor(this@CreateGroupActivity).compressToFile(file)
                launchPhotoEditor(sourceUri, Uri.fromFile(compressedImageFile))
            }
        } else if (requestCode == GALLERY_IMAGE_CAPTURE && resultCode == Activity.RESULT_OK && data != null) {
            try {
                val f: File
                val imageUri = data.data
                val timeStamp =
                    SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US)
                        .format(Date())
                var scheme: String? = null
                if (imageUri != null) {
                    scheme = imageUri.scheme
                }
                if (scheme != null) {
                    if (scheme == ContentResolver.SCHEME_CONTENT) {
                        try {
                            val fileInputStream =
                                applicationContext.contentResolver
                                    .openInputStream(imageUri!!)
                            if (fileInputStream != null) {
                                lengthbmp = fileInputStream.available() / 1024.toLong()
                            }
                        } catch (e: java.lang.Exception) {
                            e.printStackTrace()
                        }
                    } else if (scheme == ContentResolver.SCHEME_FILE) {
                        val path = imageUri!!.path
                        try {
                            if (path != null) {
                                f = File(path)
                                lengthbmp = f.length() / 2014
                            }
                        } catch (e: java.lang.Exception) {
                            e.printStackTrace()
                        }
                    }
                }
                imageFileName = timeStamp + "_.png"
                val file: File = FileUtil.from(this@CreateGroupActivity, imageUri!!)
                val compressedImageFile =
                    Compressor(this@CreateGroupActivity).compressToFile(file)
                launchPhotoEditor(imageUri!!, Uri.fromFile(compressedImageFile))
            } catch (e: java.lang.Exception) {
                e.printStackTrace()
            }

        } else if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE && data != null) {

            val activityResult = CropImage.getActivityResult(data)
            if (resultCode == Activity.RESULT_OK) {

                Glide.with(this@CreateGroupActivity).load(activityResult.uri)
                    .into(img_CreateGroupPic)
                tv_CreateGroupPic.visibility = View.GONE
                try {
                    this.imageBitmap = MediaStore.Images.Media.getBitmap(
                        contentResolver,
                        activityResult.uri
                    )
                } catch (e: java.lang.Exception) {
                    e.printStackTrace()
                }
            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                val error = activityResult.error
                error.printStackTrace()
            }
        }
    }

    private fun launchPhotoEditor(sourceUri: Uri, destinationUri: Uri) {
        CropImage.activity(sourceUri)
            .setActivityTitle("")
            .setCropMenuCropButtonTitle("Done")
            .setOutputCompressQuality(100)
            .setAutoZoomEnabled(true).start(this@CreateGroupActivity)
    }

    private fun validateAndContinue() {
        if (groupItemsList.size > 0) {
            if (!et_GroupName.text.isNullOrEmpty()) {
                if (!et_GroupLocation_country.text.isNullOrEmpty()) {
                    if (!et_GroupLocation_state.text.isNullOrEmpty()) {
                        if (!et_GroupLocation_city.text.isNullOrEmpty()) {
                            if (!et_GroupDescription.text.isNullOrEmpty()) {
                                if (registration_Gender_group.checkedRadioButtonId != -1) {
                                    if (cb_CreateGroupTermsAndPrivacy.isChecked) {
                                        saveGroup()
                                    } else {
                                        cb_CreateGroupTermsAndPrivacy.error =
                                            "Please check our terms"
                                    }
                                } else {
                                    registration_radioButtonEveryone.error =
                                        "Please choose who can post"
                                }
                            } else {
                                textInputCreateGroupDescription.error = "Enter Group Decription"
                            }
                        } else {
                            textInputCreateGroupLocation_country.error = "Enter Group City"
                        }
                    } else {
                        textInputCreateGroupLocation_country.error = "Enter Group State"
                    }
                } else {
                    textInputCreateGroupLocation_country.error = "Enter Group Country"
                }
            } else {
                textInputCreateGroupName.error = "Enter Group name"
            }
        } else {
            Toast.makeText(this, "Please Select your group type", Toast.LENGTH_LONG).show()
        }
    }

    private fun saveGroup() {

        val date = TextChanges().convertTimeStampToDate()

        if (from == null) {

            val createGroupModel = CreateGroupModel()

            createGroupModel.createdDate = date
            createGroupModel.createdBy = firebaseUser.uid
            createGroupModel.groupLocationCountry =
                et_GroupLocation_country.text.toString().toLowerCase()
            createGroupModel.groupLocationState =
                et_GroupLocation_state.text.toString().toLowerCase()
            createGroupModel.groupLocationCity = et_GroupLocation_city.text.toString().toLowerCase()
            createGroupModel.groupName = et_GroupName.text.toString().toLowerCase()
            createGroupModel.grroupDescription = et_GroupDescription.text.toString()
            createGroupModel.groupTypeList = groupItemsList
            if (registration_radioButtonOnlyMe.isChecked) {
                createGroupModel.onlyOwnerCanPost = true
            } else {
                createGroupModel.onlyOwnerCanPost = false
            }


            this.createGroupModel = createGroupModel
        }
        if (imageBitmap != null) {

            progressDialog = ProgressDialog(this@CreateGroupActivity)
            progressDialog?.setTitle("Uploading Picture")
            progressDialog?.setCancelable(false)

            if (progressDialog != null) {
                progressDialog?.show()
            }

            byteImage = ImageUtils.encodeToBase64(imageBitmap!!)
            var localStorageRef =
                mStorageRef?.child(Constants.groups)
                    ?.child(firebaseUser.uid)
                    ?.child(Constants.groupsCover)
                    ?.child(date.toString().trim())
            localStorageRef?.putBytes(byteImage)?.addOnSuccessListener {
                localStorageRef?.downloadUrl?.addOnSuccessListener {
                    createGroupModel!!.groupCoverPic = it.toString()

                    saveDataAndExit(createGroupModel!!)
                    if (progressDialog != null) {
                        progressDialog?.dismiss()
                    }
                }
            }?.addOnFailureListener {
                showSnackbar(
                    this, lay_CreateGroup,
                    "Failed Uploading Picture: ${it.localizedMessage}"
                )
                if (progressDialog != null) {
                    progressDialog?.dismiss()
                }
            }?.addOnProgressListener {
                val progress = (100.0 * it.getBytesTransferred()) / it.getTotalByteCount();
                progressDialog?.setMessage("${progress.toInt()} % Uploaded...");
            }
        } else {
            saveDataAndExit(createGroupModel!!)
        }

    }

    private fun saveDataAndExit(createGroupModel: CreateGroupModel) {

        var circularProgressBar=CircularProgressBar(this)
        circularProgressBar.showProgressDialog()

        if (from == null) {
            val reference = firestore.collection(Constants.groups).document().id
            createGroupModel.groupId = reference
            firestore.collection(Constants.groups).document(reference).set(
                createGroupModel
            ).addOnSuccessListener {
                var map = HashMap<String, String>()
                map.put(reference, "")
                firestore.collection(Constants.users).document(firebaseUser.uid)
                    .collection(Constants.groups).document(reference).set(
                        map,
                        SetOptions.merge()
                    )
                    .addOnCompleteListener {
                        if (it.isSuccessful) {
                            finish()
                            var intent = Intent(this, GroupDetailsActivity::class.java)
                            intent.putExtra(Constants.groups, createGroupModel)
                            startActivity(intent)
                        } else {
                            showSnackbar(
                                this,
                                lay_CreateGroup,
                                "Error in Storing Group data. Please contact support."
                            )
                        }
                    }

                circularProgressBar.dismissProgressDialog()
            }.addOnFailureListener {
                showSnackbar(this, lay_CreateGroup, it.localizedMessage)
                circularProgressBar.dismissProgressDialog()
            }
        } else {
            firestore.collection(Constants.groups).document(createGroupModel.groupId.toString())
                .set(createGroupModel, SetOptions.merge()).addOnSuccessListener {
                    circularProgressBar.dismissProgressDialog()
                    finish()
                    var intent = Intent(this, GroupDetailsActivity::class.java)
                    intent.putExtra(Constants.groups, createGroupModel)
                    startActivity(intent)
                }.addOnFailureListener {
                    showSnackbar(
                        this,
                        lay_CreateGroup,
                        "Error in updating Group data. Please try again later."
                    )
                    circularProgressBar.dismissProgressDialog()
                }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        circularProgressBar.dismissProgressDialog()
    }

    private fun termsAndPrivacySpannableString() {
        val spannableString = SpannableString(getString(R.string.terms_conditions))

        val clickablePrivacySpan: ClickableSpan = object : ClickableSpan() {
            override fun onClick(textView: View) {
                val intent =
                    Intent(this@CreateGroupActivity, LegalPoliciesActivity::class.java)
                intent.putExtra("WebPageID", 21)
                startActivity(intent)
                overridePendingTransition(R.anim.activity_in, R.anim.activity_out)
            }

            override fun updateDrawState(ds: TextPaint) {
                super.updateDrawState(ds)
                ds.isUnderlineText = false
                ds.color = resources.getColor(R.color.colorAccent)
            }
        }

        spannableString.setSpan(
            clickablePrivacySpan,
            13,
            getString(R.string.terms_conditions).length,
            0
        )
        tv_CreateGroupTermsAndPolicy.setText(spannableString, TextView.BufferType.SPANNABLE)
        tv_CreateGroupTermsAndPolicy.setMovementMethod(LinkMovementMethod.getInstance())
    }


    private fun gotLocation(latitude: Double, longitude: Double) {

        try {
            val geocoder: Geocoder

            val addresses: List<Address>
            geocoder = Geocoder(this@CreateGroupActivity, Locale.getDefault())
            addresses = geocoder.getFromLocation(latitude, longitude, 1)

            var cityStr = addresses[0].locality
            var stateStr = addresses[0].adminArea
            var countryStr = addresses[0].countryName
            var areaname = addresses[0].subAdminArea
            var featureName = addresses[0].featureName

            if (from == null) {
                if (!cityStr.isNullOrEmpty()) {
                    et_GroupLocation_city.setText(cityStr)
                }
                if (!stateStr.isNullOrEmpty()) {
                    et_GroupLocation_state.setText(stateStr)
                }
                if (!countryStr.isNullOrEmpty()) {
                    et_GroupLocation_country.setText(countryStr)
                }
            }

        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun onLocationChanged(location: Location?) {
        gotLocation(location?.latitude!!, location?.longitude)
    }

    override fun onStatusChanged(provider: String?, status: Int, extras: Bundle?) {
    }

    override fun onProviderEnabled(provider: String?) {
        getLastKnownLocation()
    }

    override fun onProviderDisabled(provider: String?) {
    }


}
