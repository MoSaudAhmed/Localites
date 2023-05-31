package com.example.localites.helpers;

import android.content.Context;

import com.example.localites.R;


public class CircularProgressBar {
    private final Context context;
    private android.app.ProgressDialog progress;

    public CircularProgressBar(Context context) {

        this.context = context;
    }

    public void showProgressDialog() {

        progress = new android.app.ProgressDialog(context, R.style.AppCompatAlertDialogStyle);
        progress.setInverseBackgroundForced(true);
        // progress.setIndeterminateDrawable(context.getResources().getDrawable(R.drawable.progress_animation_dialog));
        progress.setMessage("Loading...");
        progress.setIndeterminate(true);
        progress.setCancelable(false);
        if (progress != null) {
            progress.show();
        }
    }

    public void dismissProgressDialog() {

        if (progress != null && progress.isShowing()) {
            progress.dismiss();
        }

    }
}

