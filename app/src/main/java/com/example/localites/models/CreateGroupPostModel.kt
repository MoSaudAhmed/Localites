package com.example.localites.models

import android.os.Parcel
import android.os.Parcelable

data class CreateGroupPostModel(
    var createdBy: String? = "",
    var locationCity: String? = "",
    var locationCountry: String? = "",
    var groupsCover: String? = "",
    var message: String? = "",
    var createdDate: String? = "",
    var postId: String? = "",
    var groupId: String? = ""

) : Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readString(),
        parcel.readString(),
        parcel.readString(),
        parcel.readString(),
        parcel.readString(),
        parcel.readString(),
        parcel.readString(),
        parcel.readString()
    ) {
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(createdBy)
        parcel.writeString(locationCity)
        parcel.writeString(locationCountry)
        parcel.writeString(groupsCover)
        parcel.writeString(message)
        parcel.writeString(createdDate)
        parcel.writeString(postId)
        parcel.writeString(groupId)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<CreateGroupPostModel> {
        override fun createFromParcel(parcel: Parcel): CreateGroupPostModel {
            return CreateGroupPostModel(parcel)
        }

        override fun newArray(size: Int): Array<CreateGroupPostModel?> {
            return arrayOfNulls(size)
        }
    }
}