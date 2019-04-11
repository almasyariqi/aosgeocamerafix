package com.aoschallenge.aosgeocamerafix;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Location;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnSuccessListener;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

import static android.os.Environment.getExternalStoragePublicDirectory;

public class MainActivity extends AppCompatActivity {
    DatabaseHelper db;

    Button OpenCam;
    ImageView PhotoContainer;
    String pathToFile;
    TextView latitude, longitude;


    private FusedLocationProviderClient client;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        OpenCam = findViewById(R.id.btnOpenCam);
        db = new DatabaseHelper(this);

        client = LocationServices.getFusedLocationProviderClient(this);

        if (Build.VERSION.SDK_INT >= 23) {
            requestPermission();
        }

        PhotoContainer = findViewById(R.id.PhotoContainer);
        latitude = findViewById(R.id.text_latt);
        longitude = findViewById(R.id.text_longt);

        OpenCam.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ambilGambarKamera();
                getLokasi();
            }
        });
    }

    private void requestPermission(){
        requestPermissions(new String[]{Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.ACCESS_FINE_LOCATION}, 2);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(resultCode == RESULT_OK){
            if(requestCode == 1){
                Bitmap bitmap = BitmapFactory.decodeFile(pathToFile);
                PhotoContainer.setImageBitmap(bitmap);
                ambilDataLokasi();
            }
        }
    }

    private void ambilGambarKamera() {
        Intent ambilGambar = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (ambilGambar.resolveActivity(getPackageManager()) != null){
            File FilePhoto = null;
            FilePhoto = simpanPhoto();

            if(FilePhoto!=null){
                pathToFile = FilePhoto.getAbsolutePath();
                Uri photoURI = FileProvider.getUriForFile(MainActivity.this, "com.aoschallenge.aosgeocamerafix.fileprovider", FilePhoto);
                ambilGambar.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                startActivityForResult(ambilGambar, 1);
            }
        }
    }

    private void getLokasi(){
        if (ActivityCompat.checkSelfPermission(MainActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        client.getLastLocation().addOnSuccessListener(MainActivity.this, new OnSuccessListener<Location>() {
            @Override
            public void onSuccess(Location location) {
                if(location != null){
                    String lattnya = String.valueOf(location.getLatitude());
                    String longtnya = String.valueOf(location.getLongitude());
                    db.insertLokasi(lattnya, longtnya);
                }
            }

        });
    }

    private void ambilDataLokasi(){
        Cursor cursor = db.alldata();
        if(cursor.getCount()==0){
            Toast.makeText(this,"Tidak Ada Data Lokasi!", Toast.LENGTH_SHORT).show();
        } else {
            while (cursor.moveToNext()){
                String dataDBLatt = cursor.getString(0);
                String dataDBLongt = cursor.getString(1);
                latitude.setText("Latitude : "+dataDBLatt);
                longitude.setText("Longitude : "+dataDBLongt);
            }
        }
    }

    private File simpanPhoto() {
        String namaPhoto = new SimpleDateFormat("ddMMyyyy").format(new Date());
        File StorageDir = getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
        File Photonya = null;
        try {
            Photonya = File.createTempFile(namaPhoto, ".jpg", StorageDir);
        } catch (IOException e) {
            Log.d("logsaya", "Exception: "+ e.toString());
        }
        return Photonya;
    }
}
