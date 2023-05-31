package com.example.localites.activities

import android.app.Activity
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.PersistableBundle
import android.view.Gravity
import android.view.MenuItem
import android.view.View
import android.widget.*
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.drawerlayout.widget.DrawerLayout
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import com.example.localites.R
import com.example.localites.fragments.GroupsFragment
import com.example.localites.fragments.HomeFragment
import com.example.localites.fragments.MarketFramgnet
import com.example.localites.fragments.SurroundingFragment
import com.example.localites.helpers.Constants
import com.example.localites.helpers.Preferences
import com.example.localites.helpers.showSnackbar
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.navigation.NavigationView
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser


class MainActivity : AppCompatActivity(), View.OnClickListener,
    NavigationView.OnNavigationItemSelectedListener {

    private var mFirebaseAnalytics: FirebaseAnalytics? = null
    lateinit var firebaseUser: FirebaseUser

    private var drawerLayout: DrawerLayout? = null
    lateinit var toolbar: Toolbar
    lateinit var nav_view_left: NavigationView
    lateinit var tv_toolbarTitle: TextView

    lateinit var img_nav_editProfile: ImageView
    lateinit var img_nav_profilePic: ImageView
    lateinit var tv_nav_profileName: TextView
    lateinit var lay_navSettings: LinearLayout
    lateinit var lay_navMyinbox: LinearLayout
    lateinit var lay_navAddress: LinearLayout
    lateinit var lay_navHeaderEditArea: LinearLayout
    lateinit var bottom_navigation: BottomNavigationView
    lateinit var lay_main_frameLayout: FrameLayout

    var doubleBackToExitPressedOnce = false
    var mFragment: Fragment? = null

    lateinit var actionBarDrawerToggle: ActionBarDrawerToggle

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        initViews()
        Preferences.loadPreferences(this)

        mFirebaseAnalytics = FirebaseAnalytics.getInstance(this)
        firebaseUser = FirebaseAuth.getInstance().currentUser!!

        setSupportActionBar(toolbar)
        tv_toolbarTitle.setText(R.string.app_name)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        actionBarDrawerToggle = ActionBarDrawerToggle(
            this, drawerLayout, toolbar,
            R.string.drawer_open,
            R.string.drawer_close
        )
        toolbar.setNavigationIcon(R.drawable.ic_left_close)
        drawerLayout!!.addDrawerListener(actionBarDrawerToggle)
        supportActionBar?.setHomeButtonEnabled(true)
        nav_view_left.setNavigationItemSelectedListener(this)
        nav_view_left.setItemIconTintList(null)

        updateDetails()

        initializeBottomNavigation()

        bottom_navigation.selectedItemId = R.id.menu_bottom_Home
        mFragment = HomeFragment()
    }

    private fun initializeBottomNavigation() {
        bottom_navigation.setOnNavigationItemSelectedListener(object :
            BottomNavigationView.OnNavigationItemSelectedListener {
            override fun onNavigationItemSelected(item: MenuItem): Boolean {
                when (item.itemId) {
                    R.id.menu_bottom_Home -> {

                        replaceFragment(HomeFragment())
                        return true
                    }
                    R.id.menu_bottom_surrounding -> {
                        replaceFragment(SurroundingFragment())
                        return true
                    }
                    R.id.menu_bottom_group -> {
                        replaceFragment(GroupsFragment())
                        return true
                    }
                    R.id.menu_bottom_market -> {
                        replaceFragment(MarketFramgnet())
                        return true
                    }
                }
                return true
            }

        })
    }

    fun replaceFragment(fragment: Fragment) {

        if ((mFragment == null) || (mFragment != fragment)) {
            supportFragmentManager.beginTransaction()
                .replace(R.id.lay_main_frameLayout, fragment)
                .addToBackStack(null)
                .commit()
            mFragment = fragment
        }
    }

    override fun onResume() {
        super.onResume()
        if (!firebaseUser.isEmailVerified) {
            showEmailVerificationDialog()
        }
    }

    private fun showEmailVerificationDialog() {
        var alertDialog = AlertDialog.Builder(this)
        alertDialog.setCancelable(false)
        alertDialog.setTitle("Email not verified")
        alertDialog.setMessage("A mail will be sent to (${firebaseUser.email}). Open it up and click on the link to activate your account.")

        alertDialog.setPositiveButton("Send", null)
        alertDialog.setNeutralButton("Sign Out", null)
        val dialog = alertDialog.create()

        dialog.setOnShowListener {
            val button: Button =
                dialog.getButton(AlertDialog.BUTTON_POSITIVE)
            val buttonNeutral: Button =
                dialog.getButton(AlertDialog.BUTTON_NEUTRAL)
            button.isAllCaps = false
            buttonNeutral.isAllCaps = false

            buttonNeutral.setOnClickListener {
                logOut()
            }
            button.setOnClickListener {
                if (!firebaseUser.isEmailVerified) {
                    firebaseUser.sendEmailVerification().addOnCompleteListener {
                        if (it.isSuccessful) {
                            showSnackbar(
                                this,
                                drawerLayout!!,
                                "Sent Successful... Please check your email"
                            )
                        } else {
                            showSnackbar(this, drawerLayout!!, "${it.exception?.localizedMessage}")
                        }
                    }
                } else {
                    dialog.dismiss()
                }
            }
        }
        dialog.show()
    }


    private fun initViews() {
        nav_view_left = findViewById(R.id.nav_view_left)
        drawerLayout = findViewById(R.id.drawerLayout)
        toolbar = findViewById(R.id.toolbar)
        tv_toolbarTitle = findViewById(R.id.tv_toolbar_title)

        val headerView: View = nav_view_left.getHeaderView(0)

        img_nav_editProfile = headerView.findViewById(R.id.img_nav_editProfile)
        tv_nav_profileName = headerView.findViewById(R.id.tv_nav_profileName)
        img_nav_profilePic = headerView.findViewById(R.id.img_nav_profilePic)
        lay_navSettings = headerView.findViewById(R.id.lay_navSettings)
        lay_navAddress = headerView.findViewById(R.id.lay_navAddress)
        lay_navMyinbox = headerView.findViewById(R.id.lay_navMyinbox)
        lay_navHeaderEditArea = headerView.findViewById(R.id.lay_navHeaderEditArea)
        bottom_navigation = findViewById(R.id.bottom_navigation)
        lay_main_frameLayout = findViewById(R.id.lay_main_frameLayout)

        lay_navSettings.setOnClickListener(this)
        img_nav_editProfile.setOnClickListener(this)
        lay_navAddress.setOnClickListener(this)
        lay_navMyinbox.setOnClickListener(this)
        lay_navHeaderEditArea.setOnClickListener(this)
    }

    override fun onPostCreate(savedInstanceState: Bundle?, persistentState: PersistableBundle?) {
        super.onPostCreate(savedInstanceState, persistentState)
        actionBarDrawerToggle.syncState()
    }


    override fun onClick(v: View?) {
        when (v?.id) {

            /* R.id.nav_Requests -> {
                 Toast.makeText(this, "Requests", Toast.LENGTH_LONG).show()
                 return
             }*/
            R.id.img_nav_editProfile -> {
                drawerLayout?.closeDrawer(Gravity.LEFT)
                val intent = Intent(this, UpdateProfileActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_SINGLE_TOP
                startActivityForResult(intent, Constants.updateProfileResult)
                overridePendingTransition(R.anim.zoom_enter, R.anim.zoom_exit)
                return
            }
            R.id.lay_navSettings -> {
                startActivity(Intent(this, SettingsActivity::class.java))
                return
            }
            R.id.lay_navAddress -> {
                Toast.makeText(this, "Address Clicked", Toast.LENGTH_LONG).show()
                return
            }
            R.id.lay_navMyinbox -> {
                Toast.makeText(this, "MyCart Clicked", Toast.LENGTH_LONG).show()
                return
            }
            R.id.lay_navHeaderEditArea -> {
                img_nav_editProfile.performClick()
                return
            }
        }
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        if (item.itemId == R.id.nav_Requests) {
            Toast.makeText(this, "Requests", Toast.LENGTH_LONG).show()
        } else if (item.itemId == R.id.nav_LegalPolicies) {
            Toast.makeText(this, "Legal policies", Toast.LENGTH_LONG).show()
        } else if (item.itemId == R.id.nav_ContactUs) {
            Toast.makeText(this, "Contact Us", Toast.LENGTH_LONG).show()
        } else if (item.itemId == R.id.nav_RateUs) {
            Toast.makeText(this, "Rate Us", Toast.LENGTH_LONG).show()
        } else if (item.itemId == R.id.nav_logOut) {
            logOut()
        }
        drawerLayout?.closeDrawer(Gravity.LEFT)
        return false
    }

    private fun logOut() {
//Clear preferences and Send user to LoginPage
        FirebaseAuth.getInstance().signOut()
        GoogleSignIn.getClient(
            this,
            GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN).build()
        ).signOut().addOnCompleteListener {
            Preferences.clearPreferences(this)
            startActivity(Intent(this, LoginActivity::class.java))
        }

    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == Constants.updateProfileResult && resultCode == Activity.RESULT_OK) {
            updateDetails()
        }
    }

    private fun updateDetails() {
        Glide.with(this).load(Preferences.profilePic.toString())
            .error(R.drawable.ic_person)
            .into(img_nav_profilePic)
        if (!Preferences.displayName.isNullOrEmpty()) {
            tv_nav_profileName.setText(Preferences.displayName)
        } else {
            tv_nav_profileName.setText("n/a")
        }
    }


    override fun onBackPressed() {


        if (drawerLayout?.isDrawerOpen(Gravity.LEFT)!!) {
            drawerLayout?.closeDrawer(Gravity.LEFT)
        } else if (!mFragment?.javaClass?.simpleName.equals("HomeFragment")) {
            bottom_navigation.selectedItemId = R.id.menu_bottom_Home
        } else {
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
    
}
