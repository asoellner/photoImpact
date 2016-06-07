package com.soellner.photoimpact;

import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Environment;
import android.os.Message;
import android.os.StrictMode;
import android.preference.PreferenceActivity;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RemoteViews;
import android.widget.TextView;
import android.widget.Toast;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.Base64;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.soellner.photoimpact.photoimpact.R;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.soellner.photoimpact.service.photo.MultipartUtility;

import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.web.client.RestTemplate;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.URL;
import java.net.URLConnection;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import javax.ws.rs.core.MediaType;

import cz.msebera.android.httpclient.Header;


public class MainActivity extends AppCompatActivity {
    protected static final int SUCCESS_MESSAGE = 0;
    protected static final int FAILURE_MESSAGE = 1;
    protected static final int START_MESSAGE = 2;
    protected static final int FINISH_MESSAGE = 3;
    protected static final int PROGRESS_MESSAGE = 4;
    protected static final int RETRY_MESSAGE = 5;
    protected static final int CANCEL_MESSAGE = 6;
    private static final String LOG_TAG = "AsyncHttpRH";
    static final int REQUEST_TAKE_PHOTO = 1;
    private File _fullImage;


    static final int REQUEST_IMAGE_CAPTURE = 1;
    private Bitmap _bitmap;

    private Button _uploadButton;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();

        StrictMode.setThreadPolicy(policy);


        _uploadButton = (Button) findViewById(R.id.uploadButton);
        if (savedInstanceState != null) {

            savedInstanceState.getByteArray("bitmap");

            ImageView imageView = (ImageView) findViewById(R.id.uploadedImage);
            _bitmap = savedInstanceState.getParcelable("bitmap");
            //_orgBitmap = savedInstanceState.getParcelable("bitmap");
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
                    try {

                        FileInputStream fis = new FileInputStream(_fullImage);
                        Bitmap bm = BitmapFactory.decodeStream(fis);

                        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
                        bm.compress(Bitmap.CompressFormat.JPEG, 100, byteArrayOutputStream);
                        byte[] byteArray = byteArrayOutputStream.toByteArray();

                        String imageEncoded = Base64.encodeToString(byteArray, Base64.DEFAULT);


                        JSONObject obj = new JSONObject();

                        obj.put("image", imageEncoded);


                        URL url = new URL("http://192.168.1.124:8080/SampleApp/greeting/crunchifyService");
                        URLConnection connection = url.openConnection();
                        connection.setDoOutput(true);
                        connection.setRequestProperty("Content-Type", "application/json");
                        connection.setConnectTimeout(5000);
                        connection.setReadTimeout(5000);
                        OutputStreamWriter out = new OutputStreamWriter(connection.getOutputStream());
                        out.write(obj.toString());
                        out.close();

                        BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));

                        while (in.readLine() != null) {
                        }
                        System.out.println("\nCrunchify REST Service Invoked Successfully..");
                        in.close();


                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    Toast.makeText(getApplicationContext(), "Upload Done", Toast.LENGTH_LONG).show();
                    return;
                }

                /*
                //take Photo
                Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                // Ensure that there's a camera activity to handle the intent
                if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
                    // Create the File where the photo should go
                    File photoFile = null;
                    try {
                        photoFile = createImageFile();
                    } catch (IOException ex) {
                        // Error occurred while creating the File

                    }
                    // Continue only if the File was successfully created
                    if (photoFile != null) {
                        takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT,
                                Uri.fromFile(photoFile));
                        startActivityForResult(takePictureIntent, REQUEST_TAKE_PHOTO);
                    }
                }

*/
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

            if (_bitmap != null) {
                _uploadButton.setText("Upload Photo");

            }

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


    private File convertBitmapToFile(Bitmap bitmap) {
        File file = new File(this.getCacheDir(), "temp.jpg");
        try {
            file.createNewFile();

            //Convert bitmap to byte array

            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.PNG, 0, bos);
            byte[] bitmapdata = bos.toByteArray();

            //write the bytes in file
            FileOutputStream fos = new FileOutputStream(file);
            fos.write(bitmapdata);
            fos.flush();
            fos.close();
            return file;

        } catch (IOException e) {
            e.printStackTrace();
        }

        return null;
    }

    private File createImageFile() throws IOException {
        // Create an image file name
        String mCurrentPhotoPath;

        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_PICTURES);
        //   _fullImage = File.createTempFile(
        //          imageFileName,  /* prefix */
        //         ".jpg",         /* suffix */
        //        storageDir      /* directory */
        //);
        _fullImage = new File(this.getCacheDir(), imageFileName);

        // Save a file: path for use with ACTION_VIEW intents
        mCurrentPhotoPath = "file:" + _fullImage.getAbsolutePath();
        return _fullImage;
    }
}
