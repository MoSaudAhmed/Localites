package com.example.localites.models

import android.os.Parcel
import android.os.Parcelable

data class CompleteProfileModel(
    var displayName: String? = "",
    var gender: String? = "",
    var profilePic: String? = "",
    var mobile: String? = "",
    var aboutYourSelf: String? = "",
    var occupation: String? = "",
    var website: String? = "",
    var facebook: String? = "",
    var youtube: String? = "",
    var instagram: String? = "",
    var coverPic: String? = "-1",
    var uid: String? = "",
    var locationCity: String? = "",
    var locationCountry: String? = ""

) : Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readString(),
        parcel.readString(),
        parcel.readString(),
        parcel.readString(),
        parcel.readString(),
        parcel.readString(),
        parcel.readString(),
        parcel.readString(),
        parcel.readString(),
        parcel.readString(),
        parcel.readString(),
        parcel.readString(),
        parcel.readString(),
        parcel.readString()
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(displayName)
        parcel.writeString(gender)
        parcel.writeString(profilePic)
        parcel.writeString(mobile)
        parcel.writeString(aboutYourSelf)
        parcel.writeString(occupation)
        parcel.writeString(website)
        parcel.writeString(facebook)
        parcel.writeString(youtube)
        parcel.writeString(instagram)
        parcel.writeString(coverPic)
        parcel.writeString(uid)
        parcel.writeString(locationCity)
        parcel.writeString(locationCountry)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<CompleteProfileModel> {
        override fun createFromParcel(parcel: Parcel): CompleteProfileModel {
            return CompleteProfileModel(parcel)
        }

        override fun newArray(size: Int): Array<CompleteProfileModel?> {
            return arrayOfNulls(size)
        }
    }
}