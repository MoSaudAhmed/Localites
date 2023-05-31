package com.example.localites.models

import android.os.Parcel
import android.os.Parcelable

data class CreateGroupModel(

    var followers: Int? = 1,
    var groupId: String? = "",
    var groupName: String? = "",
    var grroupDescription: String? = "",
    var groupLocationCountry: String? = "",
    var groupLocationState: String? = "",
    var groupLocationCity: String? = "",
    var groupCoverPic: String? = "",
    var createdBy: String? = "",
    var createdDate: String? = "",
    var onlyOwnerCanPost: Boolean = false,
    var groupTypeList: List<String>? = null
) : Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readValue(Int::class.java.classLoader) as? Int,
        parcel.readString(),
        parcel.readString(),
        parcel.readString(),
        parcel.readString(),
        parcel.readString(),
        parcel.readString(),
        parcel.readString(),
        parcel.readString(),
        parcel.readString(),
        parcel.readByte() != 0.toByte(),
        parcel.createStringArrayList()
    ) {
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeValue(followers)
        parcel.writeString(groupId)
        parcel.writeString(groupName)
        parcel.writeString(grroupDescription)
        parcel.writeString(groupLocationCountry)
        parcel.writeString(groupLocationState)
        parcel.writeString(groupLocationCity)
        parcel.writeString(groupCoverPic)
        parcel.writeString(createdBy)
        parcel.writeString(createdDate)
        parcel.writeByte(if (onlyOwnerCanPost) 1 else 0)
        parcel.writeStringList(groupTypeList)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<CreateGroupModel> {
        override fun createFromParcel(parcel: Parcel): CreateGroupModel {
            return CreateGroupModel(parcel)
        }

        override fun newArray(size: Int): Array<CreateGroupModel?> {
            return arrayOfNulls(size)
        }
    }
}