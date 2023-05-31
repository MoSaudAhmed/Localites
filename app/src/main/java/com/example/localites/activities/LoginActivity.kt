package com.example.localites.activities

import android.app.Activity
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.coordinatorlayout.widget.CoordinatorLayout
import com.example.localites.R
import com.example.localites.helpers.CircularProgressBar
import com.example.localites.helpers.Constants
import com.example.localites.helpers.Preferences
import com.example.localites.helpers.showSnackbar
import com.example.localites.models.CompleteProfileModel
import com.example.localites.models.CreateUserModel
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.SignInButton
import com.google.android.gms.common.api.ApiException
import com.google.android.material.textfield.TextInputLayout
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.android.synthetic.main.activity_login.*
import kotlinx.android.synthetic.main.activity_update_profile.*


class LoginActivity : AppCompatActivity(), View.OnClickListener {

    lateinit var btn_GoogleSigniIn: SignInButton
    lateinit var btn_Login: Button
    lateinit var et_email: EditText
    lateinit var et_password: EditText
    lateinit var textInputEmail: TextInputLayout
    lateinit var textInputPassword: TextInputLayout
    lateinit var lay_LoginLayout: CoordinatorLayout
    lateinit var tv_registration: TextView
    lateinit var tv_forgotPassword: TextView

    private lateinit var auth: FirebaseAuth
    lateinit var firestore: FirebaseFirestore

    private lateinit var mGoogleSignInClient: GoogleSignInClient
    lateinit var gso: GoogleSignInOptions

    val RC_SIGN_IN = 555

    var doubleBackToExitPressedOnce = false

    var normalRegistrationResult = 666

    lateinit var circularProgressBar: CircularProgressBar

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        supportActionBar?.hide()

        initViews()
        circularProgressBar= CircularProgressBar(this)

        auth = FirebaseAuth.getInstance()


        gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()

        mGoogleSignInClient = GoogleSignIn.getClient(this, gso)


        et_password.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                textInputPassword.setError(null)
            }
        })
        et_email.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                textInputEmail.setError(null)
            }
        })
    }

    fun initViews() {
        btn_GoogleSigniIn = findViewById(R.id.btn_GoogleSigniIn)
        lay_LoginLayout = findViewById(R.id.lay_LoginLayout)
        btn_Login = findViewById(R.id.btn_Login)
        et_email = findViewById(R.id.editTextEmail)
        et_password = findViewById(R.id.editTextPassword)
        textInputEmail = findViewById(R.id.textInputEmail)
        tv_registration = findViewById(R.id.tv_registration)
        textInputPassword = findViewById(R.id.textInputPassword)
        tv_forgotPassword = findViewById(R.id.tv_forgotPassword)
        btn_GoogleSigniIn.setSize(SignInButton.SIZE_WIDE)

        btn_GoogleSigniIn.setOnClickListener(this)
        btn_Login.setOnClickListener(this)
        tv_registration.setOnClickListener(this)
        tv_forgotPassword.setOnClickListener(this)
    }

    override fun onClick(v: View?) {

        when (v?.id) {
            R.id.btn_GoogleSigniIn -> {
                googleSignIn()
                return
            }
            R.id.btn_Login -> {
                normalSignIn(editTextEmail.text.toString(), editTextPassword.text.toString())
                return
            }
            R.id.tv_registration -> {
                startActivityForResult(
                    Intent(
                        this, RegistrationActivity::class.java
                    ), normalRegistrationResult
                )
                return
            }
            R.id.tv_forgotPassword -> {
                if (et_email.text.isNotEmpty() && android.util.Patterns.EMAIL_ADDRESS.matcher(
                        et_email.text
                    ).matches()
                ) {
                    showForgotPasswordDialog()
                } else {
                    textInputEmail.error = "Please enter a valid email Address"
                }
            }
        }
    }

    private fun showForgotPasswordDialog() {
        var alertDialog = AlertDialog.Builder(this)
        alertDialog.setCancelable(true)
        alertDialog.setTitle("Reset Password")
        alertDialog.setMessage("A mail will be sent to (${et_email.text}). Open it up and click on the link to reset your password.")
        alertDialog.setPositiveButton("Send") { dialog, which ->

            var circularProgressBar=CircularProgressBar(this)
            circularProgressBar.showProgressDialog()

            auth.sendPasswordResetEmail(et_email.text.toString()).addOnCompleteListener {
                if (it.isSuccessful) {
                    showSnackbar(
                        this,
                        lay_LoginLayout!!,
                        "Sent Successful... Please check your email"
                    )
                    dialog.dismiss()
                } else {
                    showSnackbar(this, lay_LoginLayout!!, "${it.exception?.localizedMessage}")
                    dialog.dismiss()
                }
                circularProgressBar.dismissProgressDialog()
            }

        }
        val dialog = alertDialog.create()
        dialog.show()

    }

    fun normalSignIn(email: String, password: String) {
        var circularProgressBar=CircularProgressBar(this)
        circularProgressBar.showProgressDialog()
        if (email.isNotEmpty() && android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            if (password.isNotEmpty()) {
                FirebaseAuth.getInstance().signInWithEmailAndPassword(email, password)
                    .addOnCompleteListener {
                        if (it.isSuccessful) {
                            updateUI(auth.currentUser)
                        } else {
                            showSnackBar(it.exception?.localizedMessage.toString())
                        }
                    }
                circularProgressBar.dismissProgressDialog()
            } else {
                textInputPassword.setError("Please enter a valid Password")
                circularProgressBar.dismissProgressDialog()
            }
        } else {
            textInputEmail.setError("Please Enter a valid Email")
            circularProgressBar.dismissProgressDialog()
        }
    }

    private fun googleSignIn() {
        val signInIntent = mGoogleSignInClient.signInIntent
        startActivityForResult(signInIntent, RC_SIGN_IN)

    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == RC_SIGN_IN) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
            try {
                // Google Sign In was successful, authenticate with Firebase
                val account = task.getResult(ApiException::class.java)
                firebaseAuthWithGoogle(account)
            } catch (e: ApiException) {
                // Google Sign In failed, update UI appropriately

                Log.e("Login", "Google sign in failed", e)
                showSnackBar(e.localizedMessage)
                // ...
            }
        } else if (requestCode == normalRegistrationResult && resultCode == Activity.RESULT_OK) {
            showSnackBar("Registration Successful, Please Login")
        }
    }

    private fun firebaseAuthWithGoogle(acct: GoogleSignInAccount?) {

        var circularProgressBar=CircularProgressBar(this)
        circularProgressBar.showProgressDialog()

        Log.d("Login", "firebaseAuthWithGoogle:" + acct?.id!!)

        val credential = GoogleAuthProvider.getCredential(acct.idToken, null)
        auth.signInWithCredential(credential)
            .addOnCompleteListener(this) { task ->
                circularProgressBar.dismissProgressDialog()
                if (task.isSuccessful) {
                    // Sign in success, update UI with the signed-in user's information
                    Log.d("Login", "signInWithCredential:success")
                    val user = auth.currentUser
                    if (task.result?.additionalUserInfo?.isNewUser!!) {
                        createUserService(user)
                    } else {
                        updateUI(user)
                    }
                } else {
                    // If sign in fails, display a message to the user.
                    Log.w("Login", "signInWithCredential:failure", task.exception)
                    Toast.makeText(this, "Auth Failed", Toast.LENGTH_LONG).show()
                    // updateUI(null)
                    showSnackBar(task.exception?.localizedMessage.toString())
                }

                // ...
            }
    }

    private fun createUserService(user: FirebaseUser?) {
        var circularProgressBar=CircularProgressBar(this)
        circularProgressBar.showProgressDialog()
        var createUserModel = CreateUserModel(
            user?.displayName.toString(),
            user?.email.toString(),
            user?.uid.toString(),
            ""
        )
        // user?.sendEmailVerification()
        FirebaseFirestore.getInstance().collection(Constants.users)
            .document(createUserModel.uid)
            .set(createUserModel).addOnCompleteListener {
                circularProgressBar.dismissProgressDialog()
                updateUI(user)

            }
    }


    private fun updateUI(user: FirebaseUser?) {

        if (user == null) {
            showSnackBar("Soething went wrong, Please Sign-In Manually")
        } else {
            firestore = FirebaseFirestore.getInstance()
            lateinit var completeProfileModel: CompleteProfileModel

            firestore.collection(Constants.users).document(user.uid).get()
                .addOnSuccessListener { documentSnapshot ->
                    if (documentSnapshot != null && documentSnapshot.exists()) {
                        completeProfileModel =
                            documentSnapshot.toObject(CompleteProfileModel::class.java)!!
                        saveDataAndLogin(completeProfileModel, user)
                    } else {
                        createUserService(FirebaseAuth.getInstance().currentUser)
                    }
                }.addOnFailureListener {
                    showSnackbar(this, lay_updateProfile, "Error in getting your details")
                }

        }
    }


    private fun saveDataAndLogin(completeProfileModel: CompleteProfileModel, user: FirebaseUser) {
        Preferences.loadPreferences(this)
        Preferences.uid = user.uid
        Preferences.displayName = completeProfileModel.displayName
        Preferences.email = user.email
        Preferences.profilePic = completeProfileModel.profilePic
        Preferences.gender = completeProfileModel.gender
        Preferences.mobile = completeProfileModel.mobile
        Preferences.aboutYourSelf = completeProfileModel.aboutYourSelf
        Preferences.occupation = completeProfileModel.occupation
        Preferences.website = completeProfileModel.website
        Preferences.facebook = completeProfileModel.facebook
        Preferences.instagram = completeProfileModel.instagram
        Preferences.youtube = completeProfileModel.youtube
        Preferences.coverPic = completeProfileModel.coverPic
        Preferences.savePreferences(this)

        startActivity(Intent(this, MainActivity::class.java))
        overridePendingTransition(
            R.anim.activity_in,
            R.anim.activity_out
        )
    }

    fun showSnackBar(msg: String) {
        showSnackbar(this, lay_LoginLayout, "$msg")
    }

    override fun onBackPressed() {

        if (doubleBackToExitPressedOnce) {
            moveTaskToBack(true)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
                HomeActivity@ this.finishAndRemoveTask()
            else
                HomeActivity@ this.finish()
        }
        doubleBackToExitPressedOnce = true
        Toast.makeText(this, "Please click BACK again to exit", Toast.LENGTH_SHORT).show()
        Handler().postDelayed(Runnable { doubleBackToExitPressedOnce = false }, 2000)
    }

}
