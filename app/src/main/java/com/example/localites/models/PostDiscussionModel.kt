package com.example.localites.models

import android.os.Parcel
import android.os.Parcelable

data class PostDiscussionModel(
    var displayName: String? = "",
    var profilePic: String? = "",
    var reply: String? = "",
    var date: String? = "",
    var uid: String? = ""
):Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readString(),
        parcel.readString(),
        parcel.readString(),
        parcel.readString(),
        parcel.readString()
    ) {
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(displayName)
        parcel.writeString(profilePic)
        parcel.writeString(reply)
        parcel.writeString(date)
        parcel.writeString(uid)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<PostDiscussionModel> {
        override fun createFromParcel(parcel: Parcel): PostDiscussionModel {
            return PostDiscussionModel(parcel)
        }

        override fun newArray(size: Int): Array<PostDiscussionModel?> {
            return arrayOfNulls(size)
        }
    }
}