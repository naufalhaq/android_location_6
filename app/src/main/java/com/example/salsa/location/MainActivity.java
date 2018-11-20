package com.example.salsa.location;

import android.Manifest;
import android.animation.AnimatorInflater;
import android.animation.AnimatorSet;
import android.content.Intent;
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

import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.common.GooglePlayServicesRepairableException;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.PlaceDetectionClient;
import com.google.android.gms.location.places.PlaceLikelihood;
import com.google.android.gms.location.places.PlaceLikelihoodBufferResponse;
import com.google.android.gms.location.places.Places;
import com.google.android.gms.location.places.ui.PlacePicker;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;

public class MainActivity extends AppCompatActivity implements DapatkanAlamatTask.onTaskSelesai{

    public Button btnLoc;
    public Button btnPilihLoc;
    private Location mLastLocation;
    private LocationCallback mLocationCallback;
    private FusedLocationProviderClient mFusedLocationClient;
    public TextView mLocationTextView;
    private static final int REQUEST_LOCATION_PERMISSION = 1;
    private static final int REQUEST_PICK_PLACE = 1;
    private ImageView mAndroidImageView;
    private AnimatorSet mRotateAnim;
    private boolean mTrackingLocation;
    private PlaceDetectionClient mPlaceDetectionClient;
    private String mLastPlaceName;

    //static variabel untuk digunakan sebagai instance save agar saat berubah rotasi tidak hilang data sebelumnya
    private static String NAME ="" ;
    //static variabel untuk digunakan sebagai instance save agar saat berubah rotasi tidak hilang data sebelumnya
    private static String ADDRESS = "";
    //static variabel untuk digunakan sebagai instance save agar saat berubah rotasi tidak hilang data sebelumnya
    private static int IMG=-1;

    @Override
    protected void onSaveInstanceState(Bundle savedInstanceState) { //function digunakan agar data dari alamat sebelumnya disimpan ke dalaman Save Instance State
        super.onSaveInstanceState(savedInstanceState);
        savedInstanceState.putString("placeName",NAME);
        savedInstanceState.putString("placeAddress",ADDRESS);
        savedInstanceState.putInt("placeImage",IMG);

    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) { // function digunakn saat merestore data yang ada dalam Instancestate kedalam object yandg ditentukan
        super.onRestoreInstanceState(savedInstanceState);
        if(savedInstanceState.getString("placeName")=="")
        {
            mLocationTextView.setText("Tekan Button dibawah ini untuk mendapatkan lokasi anda");
        }
        else
        {
            mLocationTextView.setText(getString(R.string.alamat_text,savedInstanceState.getString("placeName"),savedInstanceState.getString("placeAddress"), System.currentTimeMillis()));
            mAndroidImageView.setImageResource(savedInstanceState.getInt("placeImage"));

        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // untuk mendapat informasi lokasi device
        mPlaceDetectionClient = Places.getPlaceDetectionClient(this);
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        mLocationTextView = (TextView) findViewById(R.id.textMap);
        btnLoc = (Button) findViewById(R.id.btnLocation);
        btnPilihLoc = (Button) findViewById(R.id.btnPilihLocation);
        mAndroidImageView = (ImageView) findViewById(R.id.imgMap);

        mLocationCallback = new LocationCallback(){
            @Override
            public void onLocationResult(LocationResult locationResult) {
//                super.onLocationResult(locationResult);

                // jika tracking aktif, proses reverse geocode manjadi data alamat
                if(mTrackingLocation){
                    new DapatkanAlamatTask(MainActivity.this, MainActivity.this)
                            .execute(locationResult.getLastLocation());
                }
            }
        };

        // Animasi
        mRotateAnim = (AnimatorSet) AnimatorInflater.loadAnimator(this, R.animator.rotate );
        mRotateAnim.setTarget(mAndroidImageView);

        btnLoc.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                getLocation();
//                mulaiTrackingLokasi();
                if (!mTrackingLocation){
                    mulaiTrackingLokasi();
                } else {
                    stopTrackingLokasi();
                }
            }
        });


        btnPilihLoc.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//              click handler untuk mengeksekusi placepicker
                PlacePicker.IntentBuilder builder = new PlacePicker.IntentBuilder();

                try{
                    startActivityForResult(builder.build(MainActivity.this), REQUEST_PICK_PLACE );
                } catch (GooglePlayServicesRepairableException | GooglePlayServicesNotAvailableException e){
                    e.printStackTrace();
                }
            }
        });

    }

    /**
     * Dispatch incoming result to the correct fragment.
     *
     * @param requestCode
     * @param resultCode
     * @param data
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK){
            // mendapatkan object dr placepicker
            Place place = PlacePicker.getPlace(this,data );

            setTipeLokasi(place);
            mLocationTextView.setText(
                    getString(R.string.alamat_text,
                            place.getName(),
                            place.getAddress(),
                            System.currentTimeMillis())
            );

            NAME = place.getName().toString(); // masukan data data tersebut kedalam statid variabel untuk di saveinstance agar tidak hilang
            ADDRESS = place.getAddress().toString();// masukan data data tersebut kedalam statid variabel untuk di saveinstance agar tidak hilang
            IMG = setTipeLokasi(place);// masukan data data tersebut kedalam statid variabel untuk di saveinstance agar tidak hilang
            mAndroidImageView.setImageResource(IMG);

        } else{
            mLocationTextView.setText("lokasi tidak ditemukan");

        }
    }

    private void mulaiTrackingLokasi(){
        if(ActivityCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(MainActivity.this,new String[]{Manifest.permission.ACCESS_FINE_LOCATION} ,
                    REQUEST_LOCATION_PERMISSION );
        } else {
//            Log.d("GETPERMISSION", "getLocation : permission granted");

            mFusedLocationClient.requestLocationUpdates(getLocationRequest(), mLocationCallback,null );


            mLocationTextView.setText(getString(R.string.alamat_text, "sedang mencari nama tempat",
                    "sedang mencari alamat",
                    System.currentTimeMillis()));
            mTrackingLocation = true;
            btnLoc.setText("Stop Tracking Lokasi");
            mRotateAnim.start();
        }
    }




    @Override
    public void onRequestPermissionsResult(
            int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        switch (requestCode){
            case REQUEST_LOCATION_PERMISSION:
                // jika permission diijinkan, getLocation()
                // jika tidak, tampilkan toast
                if(grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){
//                    getLocation();
                    mulaiTrackingLokasi();
                } else {
                    Toast.makeText(this, "tidak dapat permission", Toast.LENGTH_SHORT).show();
                }
                break;
        }
    }

    @Override
    public void onTaskCompleted(final String result) throws SecurityException {

//        untuk mengecek mTrackingLocatin aktif atau tidak
        if(mTrackingLocation){

            Task<PlaceLikelihoodBufferResponse> placeResult=
                    mPlaceDetectionClient.getCurrentPlace(null);
            placeResult.addOnCompleteListener(
                    new OnCompleteListener<PlaceLikelihoodBufferResponse>() {
                        @Override
                        public void onComplete(@NonNull Task<PlaceLikelihoodBufferResponse> task) {
                            // mengecek apakah task berhasil atau tidak
                            if(task.isSuccessful()){
                                // ini diisi jika berhasil
                                PlaceLikelihoodBufferResponse likelyPlaces = task.getResult();
                                float maxLikelihood = 0;
                                Place currentPlace = null;

                                // cek tempat yg dihasilkan adalah tempat yg paling mendekati(likehood)
                                for (PlaceLikelihood placeLikelihood : likelyPlaces){
                                    if(maxLikelihood < placeLikelihood.getLikelihood()){
                                        maxLikelihood = placeLikelihood.getLikelihood();
                                        currentPlace = placeLikelihood.getPlace();
                                    }

                                    // tampilan di UI
                                    if(currentPlace !=null){
                                        mLocationTextView.setText(
                                                getString(R.string.alamat_text,
                                                        currentPlace.getName(),
                                                        result,
                                                        System.currentTimeMillis())
                                        );
                                        // ubah icon berdasar tipe lokasi
                                        setTipeLokasi(currentPlace);

                                        // input data pada var di saveinstance
                                        NAME = placeLikelihood.getPlace().getName().toString();// masukan data data tersebut kedalam statid variabel untuk di saveinstance agar tidak hilang
                                        ADDRESS = placeLikelihood.getPlace().getAddress().toString();// masukan data data tersebut kedalam statid variabel untuk di saveinstance agar tidak hilang
                                        IMG = setTipeLokasi(placeLikelihood.getPlace());// masukan data data tersebut kedalam statid variabel untuk di saveinstance agar tidak hilang
                                        mAndroidImageView.setImageResource(IMG);
                                    }

                                }

                                likelyPlaces.release();
                            } else {
                                // ini juga diisi
                                mLocationTextView.setText(
                                        getString(R.string.alamat_text,
                                                "nama lokasi tidak ditemukan",
                                                result,
                                                System.currentTimeMillis())
                                );
                            }
                        }
                    }
            );

            // menampilkan semua tempat
            mPlaceDetectionClient.getCurrentPlace(null);

            // menampilkan alamat
//            mLocationTextView.setText(getString(R.string.alamat_text, result, System.currentTimeMillis()));
//            mLocationTextView.setText("coba dulu");
        }
    }

    private void stopTrackingLokasi(){
        if(mTrackingLocation){
            mTrackingLocation = false;

            // menghapus request update lokasi
            mFusedLocationClient.removeLocationUpdates(mLocationCallback);

            btnLoc.setText("Mulai Tracking Lokasi");
            mLocationTextView.setText("Tracking sedang dihentikan");
            mRotateAnim.end();
        }

    }

    // digunakan untuk menentukan frekuensi req dan tingkat akurasi dari update lokasi
    private LocationRequest getLocationRequest(){
        LocationRequest locationRequest = new LocationRequest();

        // digunakan untuk seberapa sering update lokasi yg diinginkan
        locationRequest.setInterval(10000);

        // adalah seberapa sering update lokasi dari app lain yg meminta req lokasi
        locationRequest.setFastestInterval(5000);

        // parameter untuk memilih akurasi dan akurasi tinggi menggunakan GPS
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        return locationRequest;
    }

    // menampilkan lokasi
    private int setTipeLokasi(Place currentPlace){
        int drawableID = -1;
        for (Integer placeType : currentPlace.getPlaceTypes()){
            switch (placeType){
                case Place.TYPE_MOVIE_THEATER:
                    drawableID = R.drawable.bioskop;
                    break;
                case Place.TYPE_UNIVERSITY:
                    drawableID = R.drawable.kampus;
                    break;
                case Place.TYPE_SHOPPING_MALL:
                    drawableID = R.drawable.toko;
                    break;
                case Place.TYPE_CAFE:
                    drawableID = R.drawable.warkop;
                    break;
                case Place.TYPE_BANK:
                    drawableID = R.drawable.warkop;
                    break;
            }
        }

        if(drawableID<0){
            drawableID = R.drawable.unknown;
        }
        return drawableID;
    }

}