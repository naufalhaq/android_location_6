package com.example.salsa.location;

import android.Manifest;
import android.animation.AnimatorInflater;
import android.animation.AnimatorSet;
import android.content.pm.PackageManager;
import android.location.Location;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnSuccessListener;

public class MainActivity extends AppCompatActivity implements DapatkanAlamatTask.onTaskSelesai{
    private Button mLocationButton;
    private TextView mLocationTextView;
    private ImageView mAndroidImageView;
    private AnimatorSet mRotateAnim;
    private Location mLastLocation;
    private boolean mTrackingLocation;
    private FusedLocationProviderClient mFusedLocationClient;
    private LocationCallback mLocationCallback;


    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        mLocationTextView = (TextView) findViewById(R.id.textview_location);
        mAndroidImageView = (ImageView) findViewById(R.id.imageview_android);

        mRotateAnim = (AnimatorSet) AnimatorInflater.loadAnimator(this,R.animator.rotate);
        mRotateAnim.setTarget(mAndroidImageView);
        mLocationButton = (Button) findViewById(R.id.button_location);
        mLocationButton.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                if (!mTrackingLocation)
                {
                    mulaiTrackingLokasi();
                }
                else
                {
                    stopTrackingLokasi();
                }
            }
        });

        mLocationCallback = new LocationCallback()
        {
            public void onLocationResult(LocationResult locationResult)
            {
                if (mTrackingLocation)
                {
                    new DapatkanAlamatTask(MainActivity.this,MainActivity.this).execute(locationResult.getLastLocation());
                }
            }
        };
    }

    private void mulaiTrackingLokasi()
    {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED)
        {
            ActivityCompat.requestPermissions(this,new String[]{Manifest.permission.ACCESS_FINE_LOCATION},REQUEST_LOCATION_PERMISSION);
        }
        else
        {
            mFusedLocationClient.requestLocationUpdates(getLocationRequest(),mLocationCallback,null);
            mLocationTextView.setText(getString(R.string.alamat_text,"Sedang mencari alamt",System.currentTimeMillis()));
            mTrackingLocation=true;
            mLocationButton.setText("Stop Traking Lokasi");
            mRotateAnim.start();
        }

    }

    private void stopTrackingLokasi()
    {
        if (mTrackingLocation)
        {
            mTrackingLocation=false;
            mFusedLocationClient.removeLocationUpdates(mLocationCallback);
            mLocationButton.setText("Mulai Tracking Lokasi");
            mLocationTextView.setText("Tracking sedang dihetikan");
            mRotateAnim.end();
        }
    }

    private static final int REQUEST_LOCATION_PERMISSION = 1;

    public void setRequestPermissionResult(int requestCode, @NonNull String[]permission, @NonNull int[]grantResult)
    {
        switch (requestCode)
        {
            case REQUEST_LOCATION_PERMISSION:
                if (grantResult.length>0 && grantResult[0] == PackageManager.PERMISSION_GRANTED)
                {
                    mulaiTrackingLokasi();
                }
                else
                {
                    Toast.makeText(this,"Permission Bapaknya gak dapet masbro, mesakke",Toast.LENGTH_SHORT).show();
                }
                break;
        }
    }

    public void onTaskCompleted(String result)
    {
        if (mTrackingLocation)
        {
            //update UI tampilan hasil alamat
            mLocationTextView.setText(getString(R.string.alamat_text,result,System.currentTimeMillis()));
        }
    }

    private LocationRequest getLocationRequest()
    {
        LocationRequest locationRequest = new LocationRequest();
        locationRequest.setInterval(1000);
        locationRequest.setFastestInterval(5000);
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        return locationRequest;
    }
}
