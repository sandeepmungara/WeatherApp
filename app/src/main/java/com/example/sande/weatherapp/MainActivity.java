package com.example.sande.weatherapp;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ExecutionException;

public class MainActivity extends AppCompatActivity implements AsyncResponse {
   LocationManager locationManager;
   LocationListener locationListener;
    TextView locationTV;
    TextView temperatureTV;
    DownloadTask task;
    String END_POINT ="";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
       locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
        locationListener = new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {
                END_POINT= updateLocationInfo(location);
                }
            @Override
            public void onStatusChanged(String s, int i, Bundle bundle) {
            }
            @Override
            public void onProviderEnabled(String s) {
            }
            @Override
            public void onProviderDisabled(String s) {
            }
        };


        // if device is running SDK <24
        if (Build.VERSION.SDK_INT < 24) {
            startListening();
        }
        else {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                // ask for permission
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
            } else {
                //we have permission!
                locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, locationListener);
                Location location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                if(location!=null) {
                    updateLocationInfo(location);
                }
            }
        }
        task = new DownloadTask();
        task.delegate= this;
        task.execute(END_POINT);

        locationTV = (TextView)findViewById(R.id.location);
        temperatureTV = (TextView)findViewById(R.id.temperature);
    }


    @Override
    public void processFinish(String result)
    {
        try {

            JSONObject jsonObject = new JSONObject(result);
            String weatherInfo = jsonObject.getString("current_observation");
            Log.i("response:", weatherInfo);
            JSONObject currentObject = new JSONObject(weatherInfo);
            String  temp = currentObject.getString("temperature_string") + " " + currentObject.getString("weather");
            // String weather = currentObject.getString("weather");
            String location = currentObject.getString("display_location");
            JSONObject locationObject = new JSONObject(location);
            String state = locationObject.getString("city")+", " + locationObject.getString("state");



            locationTV.setText(state);
            temperatureTV.setText(temp);


        } catch (Exception e) {
            e.printStackTrace();
        }
    }

   @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

            startListening();
        }
    }


    public  void startListening()
    {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED)

            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, locationListener);
    }



    public String updateLocationInfo(Location location)
    {
        String zipCode ="";
        Geocoder geocoder = new Geocoder(getApplicationContext(), Locale.getDefault());
       try {
          //  latitude = location.getLatitude();
           // longitude = location.getLongitude();
            List<Address> listAddresses = geocoder.getFromLocation(location.getLatitude(),location.getLongitude(),1);
            if(listAddresses!=null && listAddresses.size()>0)
            {
                zipCode = listAddresses.get(0).getPostalCode();

               }
            //TextView addressTextView = (TextView) findViewById(R.id.addressTextView);
           // addressTextView.setText(state);
        } catch (Exception e) {

            e.printStackTrace();

        }
       END_POINT = "http://api.wunderground.com/api/a86a8a3b3cab6eb6/conditions/q/"+zipCode +".json";
       return END_POINT;
    }


}
