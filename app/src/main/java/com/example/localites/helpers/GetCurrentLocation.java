package com.example.localites.helpers;

import android.Manifest;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.os.Build;
import android.os.Handler;
import android.provider.Settings;
import android.text.TextUtils;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;

import java.util.List;
import java.util.Locale;

public class GetCurrentLocation {

    Context context;

    EditText view;
    ProgressDialog progressDialogBar;
    TextView textView;

    public GetCurrentLocation(Context context, EditText view) {

        this.context = context;
        this.view = view;
        progressDialogBar = new ProgressDialog(context);
        progressDialogBar.setCancelable(true);

        obtieneLocalizacion();
    }

    public GetCurrentLocation(Context context, TextView view) {

        this.context = context;
        this.textView = view;
        progressDialogBar = new ProgressDialog(context);
        progressDialogBar.setCancelable(true);

        obtieneLocalizacion();
    }

    void obtieneLocalizacion() {

        if (isLocationEnabled(context)) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                if (context.checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && context.checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

                    Permissions permissions = new Permissions((Activity) context);
                    permissions.checkLocationPermissions();
                } else {
                    if (progressDialogBar != null) {
                        progressDialogBar.setMessage("Fetching Location...");
                        progressDialogBar.show();
                    }
                    FusedLocationProviderClient fusedLocationProviderClient;
                    fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(context);
                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            if (progressDialogBar != null)
                                if (progressDialogBar.isShowing()) {
                                    progressDialogBar.dismiss();
                                    Toast.makeText(context, "Error in getting location, Please enter manually", Toast.LENGTH_LONG).show();
                                }
                        }
                    }, 5000);
                    fusedLocationProviderClient.getLastLocation().addOnSuccessListener(new OnSuccessListener<Location>() {
                        @Override
                        public void onSuccess(Location location) {
                            if (location != null) {
                                String latitude = String.valueOf(location.getLatitude());
                                String longitude = String.valueOf(location.getLongitude());

                                if (view != null || textView != null) {
                                    getAddress(latitude, longitude);
                                }

                            } else {
                                Toast.makeText(context, "Error in getting location, Please enter manually", Toast.LENGTH_SHORT).show();
                                progressDialogBar.dismiss();
                            }
                        }
                    });
                    fusedLocationProviderClient.getLastLocation().addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {

                            Toast.makeText(context, "Error in getting location, Please enter manually", Toast.LENGTH_SHORT).show();
                            if (progressDialogBar != null) {
                                progressDialogBar.dismiss();
                            }
                        }
                    });
                }
            }
        } else {
            AlertDialog.Builder alertDialog = new AlertDialog.Builder(context);
            alertDialog.setTitle("GPS is turned off");
            alertDialog.setCancelable(false);
            alertDialog.setMessage("Please turn on the GPS to fetch your current location");

            alertDialog.setNegativeButton("Go to Settings", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                    Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                    context.startActivity(intent);
                }
            });
            alertDialog.setNeutralButton("Cancel", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                }
            });

            alertDialog.show();
        }

    }


    private void getAddress(String latitude, String longitude) {
        try {
            Geocoder geocoder;

            List<Address> addresses;
            geocoder = new Geocoder(context, Locale.getDefault());

            addresses = geocoder.getFromLocation(Double.valueOf(latitude), Double.valueOf(longitude), 1);

            String cityStr = addresses.get(0).getLocality();
            String countryStr = addresses.get(0).getCountryName();

            String locationStr;


            if (!TextUtils.isEmpty(cityStr)) {
                locationStr = cityStr + ", " + countryStr;
                if (view != null) {
                    view.setText(locationStr);
                } else if (textView != null) {
                    textView.setText(locationStr);
                }
                if (progressDialogBar != null) {
                    progressDialogBar.dismiss();
                }
            } else {
                if (progressDialogBar != null) {
                    progressDialogBar.dismiss();
                }
                Toast.makeText(context, "Problem in finding Location", Toast.LENGTH_SHORT).show();
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static Boolean isLocationEnabled(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            LocationManager lm = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
            return lm.isLocationEnabled();
        } else {
            int mode = Settings.Secure.getInt(context.getContentResolver(), Settings.Secure.LOCATION_MODE,
                    Settings.Secure.LOCATION_MODE_OFF);
            return (mode != Settings.Secure.LOCATION_MODE_OFF);


        }
    }
}
