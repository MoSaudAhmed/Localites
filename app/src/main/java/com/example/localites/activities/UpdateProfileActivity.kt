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
import android.location.*
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.provider.Settings
import android.text.TextUtils
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.cardview.widget.CardView
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.core.widget.NestedScrollView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.PagerSnapHelper
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.localites.BuildConfig
import com.example.localites.R
import com.example.localites.adapters.HorizontalCoverAdapter
import com.example.localites.helpers.*
import com.example.localites.interfaces.CoverCallback
import com.example.localites.models.CompleteProfileModel
import com.google.android.gms.common.api.Status
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.api.net.PlacesClient
import com.google.android.libraries.places.widget.Autocomplete
import com.google.android.libraries.places.widget.AutocompleteActivity
import com.google.android.libraries.places.widget.model.AutocompleteActivityMode
import com.google.android.material.textfield.TextInputLayout
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.theartofdev.edmodo.cropper.CropImage
import de.hdodenhof.circleimageview.CircleImageView
import id.zelory.compressor.Compressor
import java.io.File
import java.text.SimpleDateFormat
import java.util.*


class UpdateProfileActivity : AppCompatActivity(), View.OnClickListener, CoverCallback,
    LocationListener {


    lateinit var toolbar: Toolbar
    lateinit var tv_toolbarTitle: TextView

    lateinit var img_updateProfilePic: CircleImageView
    lateinit var lay_updateProfile: CoordinatorLayout
    lateinit var et_updateProfile_name: EditText
    lateinit var TextInput_updateProfile_name: TextInputLayout
    lateinit var registration_Gender_group: RadioGroup
    lateinit var registration_radioButtonMale: RadioButton
    lateinit var registration_radioButtonFemale: RadioButton
    lateinit var et_updateProfile_yourSelf: EditText
    lateinit var et_updateProfile_mobile: EditText
    lateinit var et_updateProfile_Occupation: EditText
    lateinit var et_updateProfile_Website: EditText
    lateinit var et_updateProfile_Facebook: EditText
    lateinit var et_updateProfile_Instagram: EditText
    lateinit var et_updateProfile_Youtube: EditText
    lateinit var lay_updateProfile_pic: RelativeLayout
    lateinit var card_viewProfileAsOthers: CardView
    lateinit var tv_updateProfile_location: TextView
    lateinit var tv_updateProfile_email: TextView
    lateinit var updateProfile_nestedScrollView: NestedScrollView
    lateinit var img_coverChange: ImageView
    lateinit var lay_updateProfile_anchor: RelativeLayout

    lateinit var completeProfileModel: CompleteProfileModel

    lateinit var firebaseAuth: FirebaseAuth
    lateinit var firebaseUser: FirebaseUser
    lateinit var firestore: FirebaseFirestore
    private var mStorageRef: StorageReference? = null

    private var imageFileName: String? = null
    private val CAMERA_IMAGE_CAPTURE = 1
    private val GALLERY_IMAGE_CAPTURE = 2
    private var lengthbmp: Long = 0
    private var imageBitmap: Bitmap? = null
    internal lateinit var byteImage: ByteArray
    var progressDialog: ProgressDialog? = null

    lateinit var alert: AlertDialog.Builder
    lateinit var alertDialog: AlertDialog

    val AUTOCOMPLETE_REQUEST_CODE = 101;

    lateinit var service: LocationManager
    var location: Location? = null
    lateinit var permissions: Permissions

    var cityStr = ""
    var stateStr = ""
    var countryStr = ""

    lateinit var circularProgressBar: CircularProgressBar

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_update_profile)
        permissions = Permissions(this)
        initView()

        circularProgressBar = CircularProgressBar(this)

        completeProfileModel = CompleteProfileModel()

        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setHomeButtonEnabled(true)

        tv_toolbarTitle.setText("Update Profile")

        firebaseAuth = FirebaseAuth.getInstance()
        firebaseUser = firebaseAuth.currentUser!!
        firestore = FirebaseFirestore.getInstance()
        mStorageRef = FirebaseStorage.getInstance().getReference()

        getFollowers()

        fillDetails()
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

    @SuppressLint("MissingPermission")
    override fun onResume() {
        super.onResume()
        //Check if location is already set
        if (Preferences.locationCountry.isNullOrEmpty()) {
            service =
                getSystemService(Context.LOCATION_SERVICE) as LocationManager
            val enabled: Boolean = service
                .isProviderEnabled(LocationManager.GPS_PROVIDER)
            if (!enabled) {
                showGpsDialog()
            }
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
                bestLocation = l
            }
        }
        if (bestLocation != null) {
            gotLocation(bestLocation!!.latitude, bestLocation.longitude)
        }

    }


    private fun gotLocation(latitude: Double, longitude: Double) {
        try {
            val geocoder: Geocoder

            val addresses: List<Address>
            geocoder = Geocoder(this, Locale.getDefault())
            addresses =
                geocoder.getFromLocation(latitude, longitude, 1)

            cityStr = addresses[0].locality
            stateStr = addresses[0].adminArea
            countryStr = addresses[0].countryName

            var completeLocation = ""

            if (!cityStr.isNullOrEmpty()) {
                completeLocation = cityStr
            }
/*            if (!stateStr.isNullOrEmpty()) {
                Preferences.locationState = stateStr
            }*/
            if (!countryStr.isNullOrEmpty()) {
                completeLocation = "$completeLocation, $countryStr"
            }
            tv_updateProfile_location.text = completeLocation

            Log.e("GetLocationCalled", cityStr + stateStr + countryStr)

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


    private fun initView() {

        toolbar = findViewById(R.id.toolbar)
        tv_toolbarTitle = findViewById(R.id.tv_toolbar_title)
        lay_updateProfile = findViewById(R.id.lay_updateProfile)
        img_updateProfilePic = findViewById(R.id.img_updateProfilePic)
        et_updateProfile_name = findViewById(R.id.et_updateProfile_name)
        TextInput_updateProfile_name = findViewById(R.id.TextInput_updateProfile_name)
        registration_Gender_group = findViewById(R.id.registration_Gender_group)
        registration_radioButtonMale = findViewById(R.id.registration_radioButtonMale)
        registration_radioButtonFemale = findViewById(R.id.registration_radioButtonFemale)
        et_updateProfile_yourSelf = findViewById(R.id.et_updateProfile_yourSelf)
        et_updateProfile_mobile = findViewById(R.id.et_updateProfile_mobile)
        et_updateProfile_Occupation = findViewById(R.id.et_updateProfile_Occupation)
        et_updateProfile_Website = findViewById(R.id.et_updateProfile_Website)
        et_updateProfile_Facebook = findViewById(R.id.et_updateProfile_Facebook)
        et_updateProfile_Instagram = findViewById(R.id.et_updateProfile_Instagram)
        et_updateProfile_Youtube = findViewById(R.id.et_updateProfile_Youtube)
        lay_updateProfile_pic = findViewById(R.id.lay_updateProfile_pic)
        card_viewProfileAsOthers = findViewById(R.id.card_viewProfileAsOthers)
        tv_updateProfile_location = findViewById(R.id.tv_updateProfile_location)
        tv_updateProfile_email = findViewById(R.id.tv_updateProfile_email)
        updateProfile_nestedScrollView = findViewById(R.id.updateProfile_nestedScrollView)
        img_coverChange = findViewById(R.id.img_coverChange)
        lay_updateProfile_anchor = findViewById(R.id.lay_updateProfile_anchor)

        card_viewProfileAsOthers.setOnClickListener(this)
        lay_updateProfile_pic.setOnClickListener(this)
        img_coverChange.setOnClickListener(this)

        updateProfile_nestedScrollView.setOnScrollChangeListener { v: NestedScrollView?, scrollX: Int, scrollY: Int, oldScrollX: Int, oldScrollY: Int ->
            if (scrollY > oldScrollY) {
                lay_updateProfile_pic.animate().scaleX(0.4F).scaleY(0.4F).setDuration(300).start()
                img_coverChange.animate().scaleX(0.0F).scaleY(0.0F).setDuration(300).start()
                img_coverChange.isClickable = false
            } else if (oldScrollY > scrollY) {
                lay_updateProfile_pic.animate().scaleX(1F).scaleY(1F).setDuration(300).start()
                img_coverChange.animate().scaleX(1F).scaleY(1F).setDuration(300).start()
                img_coverChange.isClickable = true
            }
            if (scrollY == 0) {
                lay_updateProfile_pic.animate().scaleX(1F).scaleY(1F).setDuration(300).start()
                img_coverChange.animate().scaleX(1F).scaleY(1F).setDuration(300).start()
                img_coverChange.isClickable = true
            }
        }

        tv_updateProfile_location.setOnClickListener(this)
    }

    private fun fillDetails() {


        if (Preferences.displayName!!.isNotEmpty()) {
            et_updateProfile_name.setText(Preferences.displayName)
        } else {
            et_updateProfile_name.setText("n / a")
        }
        if (Preferences.gender!!.isNotEmpty()) {
            if (Preferences.gender.equals("Male")) {
                registration_radioButtonMale.isChecked = true
            } else {
                registration_radioButtonFemale.isChecked = true
            }
        }

        Glide.with(this).load(Preferences.profilePic.toString())
            .error(R.drawable.ic_person)
            .into(img_updateProfilePic)

        if (Preferences.mobile!!.isNotEmpty()) {
            et_updateProfile_mobile.setText(Preferences.mobile)
        } else {
            et_updateProfile_mobile.setText("n / a")
        }
        if (Preferences.aboutYourSelf!!.isNotEmpty()) {
            et_updateProfile_yourSelf.setText(Preferences.aboutYourSelf)
        } else {
            et_updateProfile_yourSelf.setText("n / a")
        }
        if (Preferences.occupation!!.isNotEmpty()) {
            et_updateProfile_Occupation.setText(Preferences.occupation)
        } else {
            et_updateProfile_Occupation.setText("n / a")
        }
        if (Preferences.website!!.isNotEmpty()) {
            et_updateProfile_Website.setText(Preferences.website)
        } else {
            et_updateProfile_Website.setText("n / a")
        }
        if (Preferences.facebook!!.isNotEmpty()) {
            et_updateProfile_Facebook.setText(Preferences.facebook)
        }
        if (Preferences.instagram!!.isNotEmpty()) {
            et_updateProfile_Instagram.setText(Preferences.instagram)
        }
        if (Preferences.youtube!!.isNotEmpty()) {
            et_updateProfile_Youtube.setText(Preferences.youtube)
        }
        if (Preferences.locationCountry!!.isNotEmpty() || Preferences.locationCity!!.isNotEmpty()) {
            tv_updateProfile_location.setText("${Preferences.locationCountry},  ${Preferences.locationCity}")
        } else {
            tv_updateProfile_location.setText("n / a")
        }
        if (Preferences.email!!.isNotEmpty()) {
            tv_updateProfile_email.setText(Preferences.email)
        } else {
            tv_updateProfile_email.setText("n / a")
        }


        if (!Preferences.coverPic.isNullOrEmpty()) {
            if (Integer.parseInt(Preferences.coverPic!!) != -1) {
                lay_updateProfile_anchor.setBackgroundResource(
                    Constants.coverImagesList.get(
                        Integer.parseInt(Preferences.coverPic!!)
                    )
                )
                completeProfileModel.coverPic = Preferences.coverPic
            }
        }

    }


    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.card_viewProfileAsOthers -> {

                if (et_updateProfile_name.text.isNotEmpty()) {
                    completeProfileModel.displayName = et_updateProfile_name.text.toString()
                }
                if (registration_Gender_group.checkedRadioButtonId != -1) {
                    if (registration_radioButtonMale.isChecked) {
                        completeProfileModel.gender = Constants.genderMale
                    } else {
                        completeProfileModel.gender = Constants.genderFemale
                    }
                }
                if (et_updateProfile_mobile.text.isNotEmpty()) {
                    completeProfileModel.mobile = et_updateProfile_mobile.text.toString()
                }
                if (et_updateProfile_yourSelf.text.isNotEmpty()) {
                    completeProfileModel.aboutYourSelf = et_updateProfile_yourSelf.text.toString()
                }
                if (et_updateProfile_Occupation.text!!.isNotEmpty()) {
                    completeProfileModel.occupation = et_updateProfile_Occupation.text.toString()
                }
                if (et_updateProfile_Website.text.isNotEmpty()) {
                    completeProfileModel.website = et_updateProfile_Website.text.toString()
                }
                if (et_updateProfile_Facebook.text.isNotEmpty()) {
                    completeProfileModel.facebook = et_updateProfile_Facebook.text.toString()
                }
                if (et_updateProfile_Instagram.text.isNotEmpty()) {
                    completeProfileModel.instagram = et_updateProfile_Instagram.text.toString()
                }
                if (et_updateProfile_Youtube.text.isNotEmpty()) {
                    completeProfileModel.youtube = et_updateProfile_Youtube.text.toString()
                }

                if (imageBitmap != null) {
                    completeProfileModel.profilePic =
                        ImageUtils.encodeToBase64(imageBitmap!!).toString()
                }

                completeProfileModel.uid = firebaseUser.uid

                val intent = Intent(this, ProfileDetails::class.java)
                intent.putExtra("profile", completeProfileModel)
                intent.putExtra(Constants.uid, firebaseUser.uid)
                startActivity(intent)
                //overridePendingTransition(R.anim.zoom_enter, R.anim.zoom_exit)
                return
            }
            R.id.lay_updateProfile_pic -> {
                val permissions = Permissions(this@UpdateProfileActivity)
                if (permissions.checkAndRequestPermissions()) {
                    imagePickerDialog()
                } else {
                    Toast.makeText(
                        this@UpdateProfileActivity,
                        resources.getString(R.string.allow_premission_to_store_data),
                        Toast.LENGTH_LONG
                    ).show()
                }

                return

            }
            R.id.img_coverChange -> {

                alert = AlertDialog.Builder(this@UpdateProfileActivity)
                val layoutInflater = layoutInflater
                val dialogueview = layoutInflater.inflate(R.layout.cover_selection_dialog, null)
                alertDialog = alert.create()
                alertDialog.setView(dialogueview)
                val cover_selectionDialog_recycerview =
                    dialogueview.findViewById(R.id.cover_selectionDialog_recycerview) as RecyclerView
                val btn_coverSelectionDialog_Reset =
                    dialogueview.findViewById(R.id.btn_coverSelectionDialog_Reset) as Button
                val btn_coverSelectionDialog_cancel =
                    dialogueview.findViewById(R.id.btn_coverSelectionDialog_cancel) as Button

                val horizontalCoverAdapter = HorizontalCoverAdapter(Constants.coverImagesList, this)
                cover_selectionDialog_recycerview.layoutManager =
                    LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
                cover_selectionDialog_recycerview.setHasFixedSize(true)
                var snapHelper = PagerSnapHelper()
                snapHelper.attachToRecyclerView(cover_selectionDialog_recycerview)
                cover_selectionDialog_recycerview.adapter = horizontalCoverAdapter

                btn_coverSelectionDialog_Reset.setOnClickListener {
                    lay_updateProfile_anchor.setBackgroundResource(0)
                    lay_updateProfile_anchor.setBackgroundColor(resources.getColor(R.color.background_gray))
                    completeProfileModel.coverPic = (-1).toString()
                    alertDialog.dismiss()
                }
                btn_coverSelectionDialog_cancel.setOnClickListener {
                    alertDialog.dismiss()
                }

                horizontalCoverAdapter.notifyDataSetChanged()
                alertDialog.show()

                return
            }
            R.id.tv_updateProfile_location -> {

                Places.initialize(
                    this.applicationContext,
                    "AIzaSyA_ErPfYE7TmZAGl6yuOTk-TibcONGL4Cw"
                )
                val placesClient: PlacesClient = Places.createClient(this)
                val fields: List<Place.Field> = Arrays.asList(
                    Place.Field.ID,
                    Place.Field.NAME,
                    Place.Field.ADDRESS,
                    Place.Field.LAT_LNG
                )
                var intent = Autocomplete.IntentBuilder(
                    AutocompleteActivityMode.FULLSCREEN, fields
                ).build(this)
                startActivityForResult(intent, AUTOCOMPLETE_REQUEST_CODE)

                return
            }
        }

    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.updateprofile_menu, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> {
                onBackPressed()
                return true
            }
            R.id.menu_save -> {
                prepareDataToSave()
                return true
            }
            else -> {
                return super.onOptionsItemSelected(item)
            }
        }
    }


    var totalFollowers = 0

    fun getFollowers() {
        var circularProgressBar=CircularProgressBar(this)
        circularProgressBar.showProgressDialog()
        firestore.collection(Constants.users).document(firebaseUser.uid).get()
            .addOnSuccessListener {
                if (it.contains(Constants.followers)) {
                    totalFollowers = (it.get(Constants.followers) as Long).toInt()
                }
                circularProgressBar.dismissProgressDialog()
            }.addOnFailureListener {
                circularProgressBar.dismissProgressDialog()
            }
    }

    private fun prepareDataToSave() {

        Preferences.locationCity = cityStr
        Preferences.locationState = stateStr
        Preferences.locationCountry = countryStr

        completeProfileModel.locationCity = Preferences.locationCity
        completeProfileModel.locationCountry = Preferences.locationCountry

        completeProfileModel.uid = firebaseUser.uid

        Preferences.uid = firebaseUser.uid

        if (et_updateProfile_name.text.isNotEmpty()) {
            completeProfileModel.displayName = et_updateProfile_name.text.toString()
            Preferences.displayName = completeProfileModel.displayName
        }
        if (registration_Gender_group.checkedRadioButtonId != -1) {
            if (registration_radioButtonMale.isChecked) {
                completeProfileModel.gender = Constants.genderMale
            } else {
                completeProfileModel.gender = Constants.genderFemale
            }
            Preferences.gender = completeProfileModel.gender
        }
        if (et_updateProfile_mobile.text.isNotEmpty()) {
            completeProfileModel.mobile = et_updateProfile_mobile.text.toString()
            Preferences.mobile = completeProfileModel.mobile
        }
        if (et_updateProfile_yourSelf.text.isNotEmpty()) {
            completeProfileModel.aboutYourSelf = et_updateProfile_yourSelf.text.toString()
            Preferences.aboutYourSelf = completeProfileModel.aboutYourSelf
        }
        if (et_updateProfile_Occupation.text!!.isNotEmpty()) {
            completeProfileModel.occupation = et_updateProfile_Occupation.text.toString()
            Preferences.occupation = completeProfileModel.occupation
        }
        if (et_updateProfile_Website.text.isNotEmpty()) {
            completeProfileModel.website = et_updateProfile_Website.text.toString()
            Preferences.website = completeProfileModel.website
        }
        if (et_updateProfile_Facebook.text.isNotEmpty()) {
            completeProfileModel.facebook = et_updateProfile_Facebook.text.toString()
            Preferences.facebook = completeProfileModel.facebook
        }
        if (et_updateProfile_Instagram.text.isNotEmpty()) {
            completeProfileModel.instagram = et_updateProfile_Instagram.text.toString()
            Preferences.instagram = completeProfileModel.instagram
        }
        if (et_updateProfile_Youtube.text.isNotEmpty()) {
            completeProfileModel.youtube = et_updateProfile_Youtube.text.toString()
            Preferences.youtube = completeProfileModel.youtube
        }

        if (!completeProfileModel.coverPic.isNullOrEmpty()) {
            Preferences.coverPic = completeProfileModel.coverPic
        }
        completeProfileModel.profilePic = Preferences.profilePic

        if (imageBitmap != null) {

            progressDialog = ProgressDialog(this@UpdateProfileActivity)
            progressDialog?.setTitle("Uploading Picture")
            progressDialog?.setCancelable(false)

            byteImage = ImageUtils.encodeToBase64(imageBitmap!!)
            var localStorageRef =
                mStorageRef?.child(Constants.users)?.child(firebaseUser.uid)
                    ?.child(Constants.profilePic)
            localStorageRef?.putBytes(byteImage)?.addOnSuccessListener {
                localStorageRef?.downloadUrl?.addOnSuccessListener {
                    Preferences.profilePic = it.toString()
                    completeProfileModel.profilePic = it.toString()

                    saveDataAndExit()
                    if (progressDialog != null) {
                        progressDialog?.dismiss()
                    }
                }
            }?.addOnFailureListener {
                showSnackbar(
                    this,
                    lay_updateProfile,
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
            saveDataAndExit()
        }
        if (progressDialog != null) {
            progressDialog?.show()
        }
    }

    private fun saveDataAndExit() {
        var circularProgressBar=CircularProgressBar(this)
        circularProgressBar.showProgressDialog()
        firestore.collection(Constants.users).document(firebaseUser.uid).set(
            completeProfileModel,
            SetOptions.merge()
        ).addOnSuccessListener {
            circularProgressBar.dismissProgressDialog()
            Preferences.savePreferences(this)
            Toast.makeText(this, "Profile Updated", Toast.LENGTH_LONG).show()
            setResult(Activity.RESULT_OK)
            finish()
        }.addOnFailureListener {
            circularProgressBar.dismissProgressDialog()
            showSnackbar(this, lay_updateProfile, it.localizedMessage)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        if (progressDialog != null) {
            progressDialog?.dismiss()
        }
        circularProgressBar.dismissProgressDialog()
    }

    fun imagePickerDialog() {
        var alertDialog = AlertDialog.Builder(this)
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
            File(ImageUtils.filePath(this@UpdateProfileActivity, imageFileName!!))
        val cameraIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        val mImageCaptureUri = FileProvider.getUriForFile(
            this@UpdateProfileActivity,
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

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == CAMERA_IMAGE_CAPTURE && resultCode == Activity.RESULT_OK) {
            if (imageFileName != null) {
                val f: File
                val sourceUri = Uri.fromFile(
                    File(
                        ImageUtils.filePath(
                            this@UpdateProfileActivity,
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
                            this@UpdateProfileActivity,
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
                val file: File = FileUtil.from(this@UpdateProfileActivity, sourceUri!!)
                val compressedImageFile =
                    Compressor(this@UpdateProfileActivity).compressToFile(file)
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
                val file: File = FileUtil.from(this@UpdateProfileActivity, imageUri!!)
                val compressedImageFile =
                    Compressor(this@UpdateProfileActivity).compressToFile(file)
                launchPhotoEditor(imageUri!!, Uri.fromFile(compressedImageFile))
            } catch (e: java.lang.Exception) {
                e.printStackTrace()
            }

        } else if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE && data != null) {

            val activityResult = CropImage.getActivityResult(data)
            if (resultCode == Activity.RESULT_OK) {

                Glide.with(this@UpdateProfileActivity).load(activityResult.uri)
                    .into(img_updateProfilePic)
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

        if (requestCode == AUTOCOMPLETE_REQUEST_CODE) {
            if (resultCode == Activity.RESULT_OK) {
                val place = Autocomplete.getPlaceFromIntent(data!!)

                var lat = place.latLng!!.latitude
                var lan = place.latLng!!.longitude
                try {
                    val geocoder: Geocoder

                    val addresses: List<Address>
                    geocoder = Geocoder(this@UpdateProfileActivity, Locale.getDefault())
                    addresses = geocoder.getFromLocation(lat, lan, 1)

                    var cityStr = addresses[0].locality
                    var stateStr = addresses[0].adminArea
                    var countryStr = addresses[0].countryName


                    Preferences.locationState = stateStr

                    if (cityStr != null && !TextUtils.isEmpty(cityStr)) {
                        Preferences.locationCity = cityStr
                    } else {
                        Preferences.locationCity = stateStr
                    }
                    Preferences.locationCountry = countryStr

                    tv_updateProfile_location.text =
                        "${Preferences.locationCity}, ${Preferences.locationCountry}"

                } catch (e: java.lang.Exception) {
                    Log.e("CatchExceptionInAddress", e.localizedMessage)
                }

            } else if (resultCode === AutocompleteActivity.RESULT_ERROR) {
                val status: Status = Autocomplete.getStatusFromIntent(data!!)
                Toast.makeText(
                    this@UpdateProfileActivity,
                    "Error: " + status.getStatusMessage(),
                    Toast.LENGTH_LONG
                ).show()
            } else if (resultCode === Activity.RESULT_CANCELED) {
                // The user canceled the operation.
            }
        }
    }

    private fun launchPhotoEditor(sourceUri: Uri, destinationUri: Uri) {
        CropImage.activity(sourceUri)
            .setActivityTitle("")
            .setCropMenuCropButtonTitle("Done")
            .setOutputCompressQuality(100)
            .setAutoZoomEnabled(true).start(this@UpdateProfileActivity)
    }

    override fun coverSelected(position: Int) {
        alertDialog.dismiss()
        completeProfileModel.coverPic = position.toString()
        lay_updateProfile_anchor.setBackgroundResource(Constants.coverImagesList.get(position))
    }
}
