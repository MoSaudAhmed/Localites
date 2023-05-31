package com.example.localites.helpers

import com.example.localites.models.CreateUserModel
import com.google.firebase.firestore.FirebaseFirestore

fun SaveUserToDB(createUserModel: CreateUserModel) {

    val data = FirebaseFirestore.getInstance()

    /*data.collection(Constants.users).document(user.uid)
        .set(UserData(user.displayName.toString(), user.email.toString(), user.uid))
   */
    data.collection(Constants.users).document(createUserModel.uid)
        .set(createUserModel)

}