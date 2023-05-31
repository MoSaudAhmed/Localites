package com.example.localites.helpers

import android.content.Context
import androidx.preference.PreferenceManager

object Preferences {
    var uid: String? = ""
    var displayName: String? = ""
    var email: String? = ""
    var profilePic: String? = ""
    var gender: String? = ""
    var mobile: String? = ""
    var aboutYourSelf: String? = ""
    var occupation: String? = ""
    var website: String? = ""
    var facebook: String? = ""
    var instagram: String? = ""
    var youtube: String? = ""
    var locationCountry: String? = ""
    var locationCity: String? = ""
    var locationState: String? = ""
    var coverPic: String? = "-1"
    var groupLocationCountry: String? = ""
    var groupLocationCity: String? = ""
    var followers: Long = 0

    fun savePreferences(context: Context?) {
        val preferences = PreferenceManager.getDefaultSharedPreferences(context)
        val editor = preferences.edit()
        editor.putString(Constants.uid, uid)
        editor.putString(Constants.displayName, displayName)
        editor.putString(Constants.email, email)
        editor.putString(Constants.profilePic, profilePic)
        editor.putString(Constants.gender, gender)
        editor.putString(Constants.mobile, mobile)
        editor.putString(Constants.aboutYourSelf, aboutYourSelf)
        editor.putString(Constants.occupation, occupation)
        editor.putString(Constants.website, website)
        editor.putString(Constants.facebook, facebook)
        editor.putString(Constants.instagram, instagram)
        editor.putString(Constants.youtube, youtube)
        editor.putString(Constants.locationCountry, locationCountry)
        editor.putString(Constants.locationCity, locationCity)
        editor.putString(Constants.locationState, locationState)
        editor.putString(Constants.coverPic, coverPic)
        editor.putString(Constants.groupLocationCountry, groupLocationCountry)
        editor.putString(Constants.groupLocationCity, groupLocationCity)
        editor.putLong(Constants.followers, followers)

        editor.apply()

    }

    fun loadPreferences(context: Context?) {
        val preferences = PreferenceManager.getDefaultSharedPreferences(context)
        uid = preferences.getString(Constants.uid, "")
        displayName = preferences.getString(Constants.displayName, "")
        email = preferences.getString(Constants.email, "")
        profilePic = preferences.getString(Constants.profilePic, "")
        gender = preferences.getString(Constants.gender, "")
        mobile = preferences.getString(Constants.mobile, "")
        aboutYourSelf = preferences.getString(Constants.aboutYourSelf, "")
        occupation = preferences.getString(Constants.occupation, "")
        website = preferences.getString(Constants.website, "")
        facebook = preferences.getString(Constants.facebook, "")
        instagram = preferences.getString(Constants.instagram, "")
        youtube = preferences.getString(Constants.youtube, "")
        locationCountry = preferences.getString(Constants.locationCountry, "")
        locationCity = preferences.getString(Constants.locationCity, "")
        locationState = preferences.getString(Constants.locationState, "")
        coverPic = preferences.getString(Constants.coverPic, "")
        groupLocationCountry = preferences.getString(Constants.groupLocationCountry, "")
        groupLocationCity = preferences.getString(Constants.groupLocationCity, "")
        followers = preferences.getLong(Constants.followers, 0)

    }

    fun clearPreferences(context: Context?) {
        val preferences = PreferenceManager.getDefaultSharedPreferences(context)
        val editor = preferences.edit()
        editor.clear()
        editor.apply()
    }
}