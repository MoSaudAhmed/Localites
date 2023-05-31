package com.example.localites.activities

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.SpannableString
import android.text.TextPaint
import android.text.TextWatcher
import android.text.method.LinkMovementMethod
import android.text.style.ClickableSpan
import android.util.Log
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.coordinatorlayout.widget.CoordinatorLayout
import com.example.localites.R
import com.example.localites.helpers.CircularProgressBar
import com.example.localites.helpers.Constants
import com.example.localites.helpers.showSnackbar
import com.example.localites.models.CreateUserModel
import com.google.android.material.checkbox.MaterialCheckBox
import com.google.android.material.textfield.TextInputLayout
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class RegistrationActivity : AppCompatActivity(), View.OnClickListener {

    lateinit var tv_alreadyHaveAnAccount: TextView
    lateinit var et_registerName: EditText
    lateinit var textInputLayout_registerName: TextInputLayout
    lateinit var et_registerEmail: EditText
    lateinit var textInputLayout_registerEmail: TextInputLayout
    lateinit var et_registerPassword: EditText
    lateinit var textInputLayout_registerPassword: TextInputLayout
    lateinit var et_registerConfirmPassword: EditText
    lateinit var textInputLayout_registerConfirmPassword: TextInputLayout
    lateinit var btn_Register: Button
    lateinit var lay_RegisterLayout: CoordinatorLayout
    lateinit var registration_Gender_group: RadioGroup
    lateinit var registration_radioButtonMale: RadioButton
    lateinit var registration_radioButtonFemale: RadioButton
    lateinit var cbTermsAndPrivacy: MaterialCheckBox
    lateinit var tvTermsAndPolicy: TextView
    lateinit var btn_reg_back: Button

    lateinit var circularProgressBar: CircularProgressBar

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_registration)

        initViews()

        circularProgressBar= CircularProgressBar(this)

        termsAndPrivacySpannableString()

        et_registerName.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                textInputLayout_registerName.error = null
            }
        })
        et_registerEmail.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                textInputLayout_registerEmail.error = null
            }
        })
        et_registerPassword.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                textInputLayout_registerPassword.error = null
                textInputLayout_registerConfirmPassword.error = null
            }
        })
        et_registerConfirmPassword.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                textInputLayout_registerConfirmPassword.error = null
            }
        })

    }

    private fun initViews() {
        tv_alreadyHaveAnAccount = findViewById(R.id.tv_alreadyHaveAnAccount)
        et_registerName = findViewById(R.id.et_registerName)
        et_registerEmail = findViewById(R.id.et_registerEmail)
        et_registerPassword = findViewById(R.id.et_registerPassword)
        et_registerConfirmPassword = findViewById(R.id.et_registerConfirmPassword)
        textInputLayout_registerName = findViewById(R.id.textInputLayout_registerName)
        textInputLayout_registerEmail = findViewById(R.id.textInputLayout_registerEmail)
        textInputLayout_registerPassword = findViewById(R.id.textInputLayout_registerPassword)
        textInputLayout_registerConfirmPassword =
            findViewById(R.id.textInputLayout_registerConfirmPassword)
        btn_Register = findViewById(R.id.btn_Register)
        lay_RegisterLayout = findViewById(R.id.lay_RegisterLayout)
        registration_Gender_group = findViewById(R.id.registration_Gender_group)
        registration_radioButtonMale = findViewById(R.id.registration_radioButtonMale)
        registration_radioButtonFemale = findViewById(R.id.registration_radioButtonFemale)
        cbTermsAndPrivacy = findViewById(R.id.cbTermsAndPrivacy)
        tvTermsAndPolicy = findViewById(R.id.tvTermsAndPolicy)
        btn_reg_back = findViewById(R.id.btn_reg_back)

        tv_alreadyHaveAnAccount.setOnClickListener(this)
        btn_Register.setOnClickListener(this)
        btn_reg_back.setOnClickListener(this)
        registration_Gender_group.setOnCheckedChangeListener { group, checkedId ->
            registration_radioButtonFemale.error = null
        }

        cbTermsAndPrivacy.setOnCheckedChangeListener { buttonView, isChecked ->
            cbTermsAndPrivacy.error = null
        }

    }

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.tv_alreadyHaveAnAccount -> {
                finish()
                return
            }
            R.id.btn_Register -> {
                validateAndRegister()
                return
            }
            R.id.btn_reg_back -> {
                finish()
            }
        }
    }

    private fun validateAndRegister() {
        if (et_registerName.text.isNotEmpty()) {
            if (et_registerEmail.text.isNotEmpty() && android.util.Patterns.EMAIL_ADDRESS.matcher(
                    et_registerEmail.text
                ).matches()
            ) {
                if (registration_Gender_group.checkedRadioButtonId != -1) {
                    if (et_registerPassword.text.isNotEmpty()) {

                        if (et_registerConfirmPassword.text.isNotEmpty()) {

                            if (et_registerPassword.text.toString()
                                    .equals(et_registerConfirmPassword.text.toString())
                            ) {
                                if (cbTermsAndPrivacy.isChecked) {
                                    registerUser()
                                } else {
                                    cbTermsAndPrivacy.error = "Please accept our Terms"
                                }
                            } else {
                                Log.e(
                                    "DoNotMatch",
                                    "${et_registerPassword.text} ${et_registerConfirmPassword.text}"
                                )
                                textInputLayout_registerConfirmPassword.error =
                                    "Passwords do not match"
                            }
                        } else {
                            textInputLayout_registerConfirmPassword.error =
                                "Please confirm your password"
                        }
                    } else {
                        textInputLayout_registerPassword.error = "Please enter a valid Password"
                    }
                } else {
                    registration_radioButtonFemale.error = "Please choose your Gender"
                }

            } else {
                textInputLayout_registerEmail.error = "Please enter a valid Email Address"
            }
        } else {
            textInputLayout_registerName.error = "Please enter a valid name"
        }
    }

    private fun registerUser() {
        var circularProgressBar=CircularProgressBar(this)
        circularProgressBar.showProgressDialog()
        FirebaseAuth.getInstance().createUserWithEmailAndPassword(
            et_registerEmail.text.toString(),
            et_registerPassword.text.toString()
        ).addOnCompleteListener {
            if (it.isSuccessful) {
                var gender = ""

                if (registration_radioButtonMale.isChecked) {
                    gender = "Male"
                } else {
                    gender = "Female"
                }

                if (it.result?.additionalUserInfo?.isNewUser!!) {
                    val user = FirebaseAuth.getInstance().currentUser
                    val userData = CreateUserModel(
                        et_registerName.text.toString(),
                        user?.email.toString(),
                        user?.uid.toString(),
                        gender
                    )
                    user?.sendEmailVerification()
                    FirebaseFirestore.getInstance().collection(Constants.users)
                        .document(userData.uid)
                        .set(userData).addOnCompleteListener {
                            FirebaseAuth.getInstance().signOut()
                            setResult(Activity.RESULT_OK)
                            finish()
                        }
                } else {
                    FirebaseAuth.getInstance().signOut()
                    setResult(Activity.RESULT_OK)
                    finish()
                }
                circularProgressBar.dismissProgressDialog()
            } else {
                showSnackbar(this, lay_RegisterLayout, it.exception?.localizedMessage.toString())
                circularProgressBar.dismissProgressDialog()
            }
        }
    }

    private fun termsAndPrivacySpannableString() {
        val spannableString = SpannableString(getString(R.string.terms_conditions))

        // Click listener for Privacy policy
        val clickablePrivacySpan: ClickableSpan = object : ClickableSpan() {
            override fun onClick(textView: View) {
                val intent =
                    Intent(this@RegistrationActivity, LegalPoliciesActivity::class.java)
                //     intent.putExtra("WebPageID", 22);
                intent.putExtra("WebPageID", 20)
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
        tvTermsAndPolicy.setText(spannableString, TextView.BufferType.SPANNABLE)
        tvTermsAndPolicy.setMovementMethod(LinkMovementMethod.getInstance())
    }

}
