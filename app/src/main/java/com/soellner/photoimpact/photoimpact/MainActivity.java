package com.soellner.photoimpact.photoimpact;

import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RemoteViews;
import android.widget.Toast;

import java.io.ByteArrayOutputStream;

public class MainActivity extends AppCompatActivity {
    static final int REQUEST_IMAGE_CAPTURE = 1;
    private Bitmap _bitmap;
    private Button _uploadButton;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);
        _uploadButton = (Button) findViewById(R.id.uploadButton);
        if (savedInstanceState != null) {

            savedInstanceState.getByteArray("bitmap");

            ImageView imageView = (ImageView) findViewById(R.id.uploadedImage);
            _bitmap = savedInstanceState.getParcelable("bitmap");
            if (_bitmap != null) {
                imageView.setImageBitmap(_bitmap);
                _uploadButton.setText("Upload Photo");
            } else {
                imageView.setImageBitmap(null);
            }

        }


        assert _uploadButton != null;
        _uploadButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                if (_bitmap != null) {
                    //Upload Photo
                    Toast.makeText(getApplicationContext(), "Upload Started", Toast.LENGTH_LONG).show();
                    return;
                }

                //take Photo
                Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
                    startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
                }


            }
        });


    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
            Bundle extras = data.getExtras();
            _bitmap = (Bitmap) extras.get("data");
            ImageView imageView = (ImageView) findViewById(R.id.uploadedImage);
            imageView.setImageBitmap(_bitmap);
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        if (_bitmap == null) {
            return;
        }
        super.onSaveInstanceState(outState);
        outState.putParcelable("bitmap", _bitmap);
    }
}
