package com.example.localites.helpers

import android.content.Context
import android.view.View
import com.google.android.material.snackbar.Snackbar

fun showSnackbar(context: Context, layout: View, msg: String) {
    var snackbar = Snackbar.make(
        layout,
        "$msg", Snackbar.LENGTH_INDEFINITE
    )
    snackbar.setAction("Ok") {
        snackbar.dismiss()
    }
/*    snackbar.setBackgroundTint(ContextCompat.getColor(context, R.color.backgroundYellow))
    snackbar.setTextColor(ContextCompat.getColor(context, android.R.color.black))*/
    snackbar.show()
}