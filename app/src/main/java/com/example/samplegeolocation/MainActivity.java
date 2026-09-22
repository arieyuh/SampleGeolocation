package com.example.samplegeolocation;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.Manifest;
import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;

import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnSuccessListener;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    FusedLocationProviderClient fusedLocationProviderClient;

    TextView latitude, longitude, address, city, country;
    Button getLocation;
    Switch darkModeSwitch;
    SharedPreferences sharedPreferences;

    private final static int REQUEST_CODE = 100;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Initialize UI elements
        latitude = findViewById(R.id.lattitude);
        longitude = findViewById(R.id.longitude);
        address = findViewById(R.id.address);
        city = findViewById(R.id.city);
        country = findViewById(R.id.country);
        getLocation = findViewById(R.id.getLocation);

        // Initialize dark mode switch and load saved preference
        darkModeSwitch = findViewById(R.id.darkModeSwitch);
        sharedPreferences = getSharedPreferences("AppSettings", MODE_PRIVATE);

        boolean isDarkMode = sharedPreferences.getBoolean("darkMode", false);
        darkModeSwitch.setChecked(isDarkMode);
        applyDarkMode(isDarkMode);

        darkModeSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putBoolean("darkMode", isChecked);
            editor.apply();
            applyDarkMode(isChecked);
        });

        // Initialize location services
        fusedLocationProviderClient =
                LocationServices.getFusedLocationProviderClient(this);

        // When the button is clicked
        getLocation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getLastLocation();
            }
        });
    }

    // Switch the app theme between light and dark, and remember the setting
    private void applyDarkMode(boolean isDarkMode) {
        if (isDarkMode) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
        } else {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        }
    }

    // Get the last known location
    private void getLastLocation() {

        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED) {

            fusedLocationProviderClient.getLastLocation()
                    .addOnSuccessListener(new OnSuccessListener<Location>() {
                        @Override
                        public void onSuccess(Location location) {

                            if (location != null) {

                                // Get latitude and longitude
                                double lat = location.getLatitude();
                                double lon = location.getLongitude();

                                Log.d("Location", "Latitude: " + lat);
                                Log.d("Location", "Longitude: " + lon);

                                // Display latitude and longitude
                                latitude.setText("Latitude: " + lat);
                                longitude.setText("Longitude: " + lon);

                                // Convert coordinates into an address
                                try {

                                    Geocoder geocoder = new Geocoder(
                                            MainActivity.this,
                                            Locale.getDefault()
                                    );

                                    List<Address> addresses =
                                            geocoder.getFromLocation(lat, lon, 1);

                                    if (addresses != null && !addresses.isEmpty()) {

                                        Address locationAddress = addresses.get(0);

                                        address.setText(
                                                "Address: " +
                                                        locationAddress.getAddressLine(0)
                                        );

                                        city.setText(
                                                "City: " +
                                                        locationAddress.getLocality()
                                        );

                                        country.setText(
                                                "Country: " +
                                                        locationAddress.getCountryName()
                                        );

                                        Log.d(
                                                "Location",
                                                "Address: " + locationAddress
                                        );

                                    }

                                } catch (IOException e) {

                                    e.printStackTrace();

                                    Toast.makeText(
                                            MainActivity.this,
                                            "Error fetching address",
                                            Toast.LENGTH_SHORT
                                    ).show();
                                }

                            } else {

                                Toast.makeText(
                                        MainActivity.this,
                                        "Unable to fetch location",
                                        Toast.LENGTH_SHORT
                                ).show();
                            }
                        }
                    });

        } else {

            // Permission has not been granted
            askPermission();
        }
    }

    // Ask the user for location permission
    private void askPermission() {

        ActivityCompat.requestPermissions(
                MainActivity.this,
                new String[]{
                        Manifest.permission.ACCESS_FINE_LOCATION,
                        Manifest.permission.ACCESS_COARSE_LOCATION
                },
                REQUEST_CODE
        );
    }

    // Handle the permission result
    @Override
    public void onRequestPermissionsResult(
            int requestCode,
            @NonNull String[] permissions,
            @NonNull int[] grantResults
    ) {

        super.onRequestPermissionsResult(
                requestCode,
                permissions,
                grantResults
        );

        if (requestCode == REQUEST_CODE) {

            if (grantResults.length > 0 &&
                    grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                // Permission granted
                getLastLocation();

            } else {

                Toast.makeText(
                        MainActivity.this,
                        "Please provide the required permission",
                        Toast.LENGTH_SHORT
                ).show();
            }
        }
    }
}
