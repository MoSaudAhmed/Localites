package com.example.localites.activities

import android.app.Activity
import android.app.ProgressDialog
import android.content.ContentResolver
import android.content.Intent
import android.graphics.Bitmap
import android.location.Address
import android.location.Geocoder
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.View
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import androidx.core.content.FileProvider
import com.bumptech.glide.Glide
import com.example.localites.BuildConfig
import com.example.localites.R
import com.example.localites.helpers.*
import com.example.localites.models.CreateGroupPostModel
import com.google.android.gms.common.api.Status
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.api.net.PlacesClient
import com.google.android.libraries.places.widget.Autocomplete
import com.google.android.libraries.places.widget.AutocompleteActivity
import com.google.android.libraries.places.widget.model.AutocompleteActivityMode
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.theartofdev.edmodo.cropper.CropImage
import id.zelory.compressor.Compressor
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

class CreateGroupPost : AppCompatActivity(), View.OnClickListener {

    lateinit var img_groupPost_profilePic: ImageView
    lateinit var tv_groupPost_profileName: TextView
    lateinit var tv_groupPost_profileLocation: TextView
    lateinit var et_groupPost_editText: EditText
    lateinit var cardView_groupPost_image: CardView
    lateinit var img_groupPost_image: ImageView
    lateinit var img_groupPost_imageRemove: ImageView
    lateinit var img_groupPost_camera: ImageView
    lateinit var img_groupPost_gallery: ImageView
    lateinit var btn_groupPost_Post: Button

    lateinit var getLocation: GetCurrentLocation

    lateinit var permissions: Permissions

    lateinit var lay_CreateGroupPost: LinearLayout

    var progressDialog: ProgressDialog? = null
    var mStorageRef: StorageReference? = null
    lateinit var firestore: FirebaseFirestore

    var groupId = ""

    lateinit var circularProgressBar: CircularProgressBar

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_create_group_post)

        initViews()

        circularProgressBar = CircularProgressBar(this)

        groupId = intent.getStringExtra(Constants.groupId)

        firestore = FirebaseFirestore.getInstance()
        mStorageRef = FirebaseStorage.getInstance().getReference()


        if (!Preferences.locationCity.isNullOrEmpty() || !Preferences.locationCountry.isNullOrEmpty()) {
            tv_groupPost_profileLocation.text =
                Preferences.locationCity + ", " + Preferences.locationCountry
        } else {
            getLocation = GetCurrentLocation(this, tv_groupPost_profileLocation)
        }
        if (Preferences.displayName.isNullOrEmpty()) {
            tv_groupPost_profileName.text = Preferences.email
        } else {
            tv_groupPost_profileName.text = Preferences.displayName
        }
        Glide.with(this).load(Preferences.profilePic).error(R.drawable.ic_person)
            .into(img_groupPost_profilePic)

        tv_groupPost_profileLocation.setOnClickListener(this)

        permissions = Permissions(this)
    }

    private fun initViews() {
        img_groupPost_profilePic = findViewById(R.id.img_groupPost_profilePic)
        tv_groupPost_profileName = findViewById(R.id.tv_groupPost_profileName)
        tv_groupPost_profileLocation = findViewById(R.id.tv_groupPost_profileLocation)
        et_groupPost_editText = findViewById(R.id.et_groupPost_editText)
        cardView_groupPost_image = findViewById(R.id.cardView_groupPost_image)
        img_groupPost_imageRemove = findViewById(R.id.img_groupPost_imageRemove)
        img_groupPost_image = findViewById(R.id.img_groupPost_image)
        img_groupPost_camera = findViewById(R.id.img_groupPost_camera)
        img_groupPost_gallery = findViewById(R.id.img_groupPost_gallery)
        btn_groupPost_Post = findViewById(R.id.btn_groupPost_Post)
        lay_CreateGroupPost = findViewById(R.id.lay_CreateGroupPost)

        img_groupPost_gallery.setOnClickListener(this)
        img_groupPost_camera.setOnClickListener(this)
        img_groupPost_imageRemove.setOnClickListener(this)
        btn_groupPost_Post.setOnClickListener(this)
    }

    lateinit var alert: AlertDialog.Builder
    lateinit var alertDialog: AlertDialog
    lateinit var et_GroupLocationSearch_Search: EditText
    var AUTOCOMPLETE_REQUEST_CODE = 555
    var GALLERY_IMAGE_CAPTURE = 666
    var CAMERA_IMAGE_CAPTURE = 777
    private var imageFileName: String? = null

    private var lengthbmp: Long = 0
    private var imageBitmap: Bitmap? = null
    internal lateinit var byteImage: ByteArray


    override fun onClick(v: View?) {

        when (v?.id) {

            R.id.img_groupPost_imageRemove -> {
                cardView_groupPost_image.visibility = View.GONE
                img_groupPost_image.setImageBitmap(null)
                this.imageBitmap = null
                return
            }
            R.id.img_groupPost_camera -> {
                if (permissions.checkAndRequestPermissions()) {
                    takeCameraPicture()
                } else {
                    Toast.makeText(
                        this@CreateGroupPost,
                        resources.getString(R.string.allow_premission_to_store_data),
                        Toast.LENGTH_LONG
                    ).show()
                }
                return
            }
            R.id.img_groupPost_gallery -> {
                takeGalleryPicture()
                return
            }
            R.id.btn_groupPost_Post -> {
                if (!tv_groupPost_profileLocation.text.isNullOrEmpty()) {
                    if (!et_groupPost_editText.text.isNullOrEmpty()) {
                        postFeedToGroup()
                    } else {
                        Toast.makeText(
                            this@CreateGroupPost,
                            "Message Body can't be empty",
                            Toast.LENGTH_LONG
                        ).show()

                    }
                } else {
                    Toast.makeText(
                        this@CreateGroupPost,
                        "Location cant be empty",
                        Toast.LENGTH_LONG
                    ).show()
                }
                return
            }
            R.id.tv_groupPost_profileLocation -> {

                alert = AlertDialog.Builder(this)
                val layoutInflater = layoutInflater
                val dialogueview =
                    layoutInflater.inflate(R.layout.dialog_groups_location_search, null)
                alertDialog = alert.create()
                alertDialog.setView(dialogueview)

                val btn_coverSelectionDialog_cancel =
                    dialogueview.findViewById(R.id.btn_GroupLocationSearch_Cancel) as Button
                val btn_coverSelectionDialog_set =
                    dialogueview.findViewById(R.id.btn_GroupLocationSearch_Set) as Button
                et_GroupLocationSearch_Search =
                    dialogueview.findViewById(R.id.et_GroupLocationSearch_Search) as EditText
                val tv_groupSearch_title =
                    dialogueview.findViewById(R.id.tv_groupSearch_title) as TextView

                tv_groupSearch_title.text =
                    "Modify Location, This location will be shown on your post."
                et_GroupLocationSearch_Search.isFocusable = false

                if (Preferences.locationCountry.isNullOrEmpty()) {
                    GetCurrentLocation(this, et_GroupLocationSearch_Search)
                } else {
                    et_GroupLocationSearch_Search.setText(Preferences.locationCity + ", " + Preferences.locationCountry)
                }

                et_GroupLocationSearch_Search.setOnClickListener(object : View.OnClickListener {
                    override fun onClick(v: View?) {
                        Places.initialize(
                            this@CreateGroupPost,
                            "AIzaSyA_ErPfYE7TmZAGl6yuOTk-TibcONGL4Cw"
                        )
                        val placesClient: PlacesClient = Places.createClient(this@CreateGroupPost)
                        val fields: List<Place.Field> = Arrays.asList(
                            Place.Field.ID,
                            Place.Field.NAME,
                            Place.Field.ADDRESS,
                            Place.Field.LAT_LNG
                        )
                        var intent = Autocomplete.IntentBuilder(
                            AutocompleteActivityMode.FULLSCREEN, fields
                        ).build(this@CreateGroupPost)
                        startActivityForResult(intent, AUTOCOMPLETE_REQUEST_CODE)
                    }

                })


                btn_coverSelectionDialog_set.setOnClickListener {

                    if (!et_GroupLocationSearch_Search.text.isNullOrEmpty()) {
                        tv_groupPost_profileLocation.text =
                            "${et_GroupLocationSearch_Search.text}"
                        alertDialog.dismiss()
                    } else {
                        Toast.makeText(
                            this,
                            "Location should not be empty",
                            Toast.LENGTH_LONG
                        ).show()
                    }
                }
                btn_coverSelectionDialog_cancel.setOnClickListener {
                    alertDialog.dismiss()
                }
                alertDialog.show()
            }
        }
    }

    private fun postFeedToGroup() {

        val date = TextChanges().convertTimeStampToDate()

        var createGroupPostModel = CreateGroupPostModel()
        createGroupPostModel.createdBy = Preferences.uid
        try {
            val separated = tv_groupPost_profileLocation.text.split(",").toTypedArray()
            createGroupPostModel.locationCity = separated[0].trim().toLowerCase()
            createGroupPostModel.locationCountry = separated[1].trim().toLowerCase()
        } catch (e: java.lang.Exception) {
        }

        createGroupPostModel.message = et_groupPost_editText.text.toString()
        createGroupPostModel.createdDate = date
        createGroupPostModel.groupId = groupId

        if (imageBitmap != null) {

            progressDialog = ProgressDialog(this@CreateGroupPost)
            progressDialog?.setTitle("Uploading Picture")
            progressDialog?.setCancelable(false)

            if (progressDialog != null) {
                progressDialog?.show()
            }

            byteImage = ImageUtils.encodeToBase64(imageBitmap!!)
            var localStorageRef =
                mStorageRef?.child(Constants.groups)
                    ?.child(Preferences.uid.toString())
                    ?.child(Constants.groupsCover)
                    ?.child(date.toString().trim())
            localStorageRef?.putBytes(byteImage)?.addOnSuccessListener {
                localStorageRef?.downloadUrl?.addOnSuccessListener {
                    createGroupPostModel!!.groupsCover = it.toString()

                    saveDataAndExit(createGroupPostModel!!)
                    if (progressDialog != null) {
                        progressDialog?.dismiss()
                    }
                }
            }?.addOnFailureListener {
                showSnackbar(
                    this, lay_CreateGroupPost,
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
            saveDataAndExit(createGroupPostModel!!)
        }

    }

    private fun saveDataAndExit(createGroupPostModel: CreateGroupPostModel) {
        var circularProgressBar=CircularProgressBar(this)
        circularProgressBar.showProgressDialog()
        val reference = firestore.collection(Constants.allPosts).document().id
        createGroupPostModel.postId = reference
        firestore.collection(Constants.allPosts).document(reference).set(
            createGroupPostModel
        ).addOnSuccessListener {
            Toast.makeText(this@CreateGroupPost, "posted successful", Toast.LENGTH_LONG).show()
            circularProgressBar.dismissProgressDialog()
            var intent = Intent()
            intent.putExtra("post", createGroupPostModel)
            setResult(RESULT_OK, intent)
            finish()
        }.addOnFailureListener {
            showSnackbar(this, lay_CreateGroupPost, it.localizedMessage)
            circularProgressBar.dismissProgressDialog()
        }
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

    private fun takeCameraPicture() {
        val timeStamp =
            SimpleDateFormat("yyyyMMddHHmmss", Locale.US)
                .format(Date())
        imageFileName = "$timeStamp.png"
        val outPutFile =
            File(ImageUtils.filePath(this@CreateGroupPost, imageFileName!!))
        val cameraIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        val mImageCaptureUri = FileProvider.getUriForFile(
            this@CreateGroupPost,
            BuildConfig.APPLICATION_ID.toString() + ".provider",
            outPutFile
        )
        cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, mImageCaptureUri)
        startActivityForResult(cameraIntent, CAMERA_IMAGE_CAPTURE)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == AUTOCOMPLETE_REQUEST_CODE) {
            if (resultCode == Activity.RESULT_OK) {
                val place = Autocomplete.getPlaceFromIntent(data!!)

                var lat = place.latLng!!.latitude
                var lan = place.latLng!!.longitude
                try {
                    val geocoder: Geocoder

                    val addresses: List<Address>
                    geocoder = Geocoder(this@CreateGroupPost, Locale.getDefault())
                    addresses = geocoder.getFromLocation(lat, lan, 1)

                    var cityStr = addresses[0].locality
                    var stateStr = addresses[0].adminArea
                    var countryStr = addresses[0].countryName

                    if (et_GroupLocationSearch_Search != null) {
                        et_GroupLocationSearch_Search.setText(
                            "${cityStr}, ${countryStr}"
                        )
                    }

                } catch (e: java.lang.Exception) {
                    Log.e("CatchExceptionInAddress", e.localizedMessage)
                }

            } else if (resultCode === AutocompleteActivity.RESULT_ERROR) {
                val status: Status = Autocomplete.getStatusFromIntent(data!!)
                Toast.makeText(
                    this@CreateGroupPost,
                    "Error: " + status.getStatusMessage(),
                    Toast.LENGTH_LONG
                ).show()
            } else if (resultCode === Activity.RESULT_CANCELED) {
                // The user canceled the operation.
            }
        }
        if (requestCode == CAMERA_IMAGE_CAPTURE && resultCode == Activity.RESULT_OK) {
            if (imageFileName != null) {
                val f: File
                val sourceUri = Uri.fromFile(
                    File(
                        ImageUtils.filePath(
                            this@CreateGroupPost,
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
                            this@CreateGroupPost,
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
                val file: File = FileUtil.from(this@CreateGroupPost, sourceUri!!)
                val compressedImageFile =
                    Compressor(this@CreateGroupPost).compressToFile(file)
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
                val file: File = FileUtil.from(this@CreateGroupPost, imageUri!!)
                val compressedImageFile =
                    Compressor(this@CreateGroupPost).compressToFile(file)
                launchPhotoEditor(imageUri!!, Uri.fromFile(compressedImageFile))
            } catch (e: java.lang.Exception) {
                e.printStackTrace()
            }

        } else if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE && data != null) {

            val activityResult = CropImage.getActivityResult(data)
            if (resultCode == Activity.RESULT_OK) {
                cardView_groupPost_image.visibility = View.VISIBLE
                Glide.with(this@CreateGroupPost).load(activityResult.uri)
                    .into(img_groupPost_image)
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
            .setAutoZoomEnabled(true).start(this@CreateGroupPost)
    }

    override fun onDestroy() {
        super.onDestroy()
        circularProgressBar.dismissProgressDialog()
    }
}
