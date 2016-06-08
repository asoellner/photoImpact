package com.soellner.photoimpact;

import android.content.ContentResolver;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
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

import java.net.URI;
import java.net.URL;
import java.net.URLConnection;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.NoSuchElementException;

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
    public static final String FILE_SEPARATOR = System.getProperty("file.separator");

    static final int REQUEST_IMAGE_CAPTURE = 1;
    static final int REQUEST_IMAGE_PICK = 2;
    private Bitmap _bitmap;

    private Button _uploadButton;
    private Uri _mImageUri;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();

        StrictMode.setThreadPolicy(policy);


        _uploadButton = (Button) findViewById(R.id.uploadButton);

        if (savedInstanceState != null) {


            if (_mImageUri != null) {
                setUploadImage();
            }

            _mImageUri = savedInstanceState.getParcelable("mImageUri");
            if (_mImageUri != null) {
                setUploadImage();
            }
           /*
            ImageView imageView = (ImageView) findViewById(R.id.uploadedImage);
            _bitmap = savedInstanceState.getParcelable("bitmap");
            //_orgBitmap = savedInstanceState.getParcelable("bitmap");
            if (_bitmap != null) {
                imageView.setImageBitmap(_bitmap);
                _uploadButton.setText("Upload Photo");
            } else {
                imageView.setImageBitmap(null);
            }
*/

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


                takePhoto();

            }
        });


    }

    //called after camera intent finished
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent intent) {
        if (_mImageUri == null) {
            return;
        }


        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK)
            this.getContentResolver().notifyChange(_mImageUri, null);

        if (_mImageUri != null) {
            setUploadImage();
        }


    }

    private void setUploadImage() {
        ContentResolver cr = this.getContentResolver();
        try {
            _bitmap = MediaStore.Images.Media.getBitmap(cr, _mImageUri);
        } catch (IOException e) {
            e.printStackTrace();
        }

        if (_bitmap != null) {
            /*
            int width = _bitmap.getWidth();
            int height = _bitmap.getHeight();
            float scaleWidth = ((float) 300) / width;
            float scaleHeight = ((float) 250) / height;
            // CREATE A MATRIX FOR THE MANIPULATION
            Matrix matrix = new Matrix();
            // RESIZE THE BIT MAP
            matrix.postScale(scaleWidth, scaleHeight);

            // "RECREATE" THE NEW BITMAP
            Bitmap resizedBitmap = Bitmap.createBitmap(
                    _bitmap, 0, 0, width, height, matrix, false);
            _bitmap.recycle();
*/
            ImageView imageView = (ImageView) findViewById(R.id.uploadedImage);
            imageView.setImageBitmap(scaleBitmap(_bitmap).to);


            _uploadButton.setText("Upload Photo");

        }
    }

    private BitmapDrawable scaleBitmap(Bitmap bitmap) {

        int width = 0;

        try {
            width = bitmap.getWidth();
        } catch (NullPointerException e) {
            throw new NoSuchElementException("Can't find bitmap on given view/drawable");
        }

        int height = bitmap.getHeight();
        int bounding = dpToPx(250);
        Log.i("Test", "original width = " + Integer.toString(width));
        Log.i("Test", "original height = " + Integer.toString(height));
        Log.i("Test", "bounding = " + Integer.toString(bounding));

        // Determine how much to scale: the dimension requiring less scaling is
        // closer to the its side. This way the image always stays inside your
        // bounding box AND either x/y axis touches it.
        float xScale = ((float) bounding) / width;
        float yScale = ((float) bounding) / height;
        float scale = (xScale <= yScale) ? xScale : yScale;
        Log.i("Test", "xScale = " + Float.toString(xScale));
        Log.i("Test", "yScale = " + Float.toString(yScale));
        Log.i("Test", "scale = " + Float.toString(scale));

        // Create a matrix for the scaling and add the scaling data
        Matrix matrix = new Matrix();
        matrix.postScale(scale, scale);

        // Create a new bitmap and convert it to a format understood by the ImageView
        Bitmap scaledBitmap = Bitmap.createBitmap(bitmap, 0, 0, width, height, matrix, true);
        width = scaledBitmap.getWidth(); // re-use
        height = scaledBitmap.getHeight(); // re-use
        BitmapDrawable result = new BitmapDrawable(scaledBitmap);
        Log.i("Test", "scaled width = " + Integer.toString(width));
        Log.i("Test", "scaled height = " + Integer.toString(height));
    }


    @Override
    protected void onSaveInstanceState(Bundle outState) {
        if (_mImageUri == null) {
            return;
        }

        setUploadImage();

        super.onSaveInstanceState(outState);
        outState.putParcelable("mImageUri", _mImageUri);
    }


    private void takePhoto() {

        try {
            File tempDir = Environment.getExternalStorageDirectory();
            File filesDir = new File(tempDir.getAbsolutePath() + FILE_SEPARATOR + "PhotoImpact" + FILE_SEPARATOR + "DCIM");
            if (!filesDir.exists()) {
                filesDir.mkdir();
            }

            int time = (int) (System.currentTimeMillis());
            Timestamp tsTemp = new Timestamp(time);
            long ts = tsTemp.getTime();

            String photoFileName = "Photo_" + ts + ".jpg";
            File photoFile = new File(filesDir.getAbsolutePath(), photoFileName);
            _mImageUri = Uri.fromFile(photoFile);

            Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            //_mImageUri = Uri.fromFile(photoFile);
            cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, _mImageUri);
            //start camera intent
            startActivityForResult(cameraIntent, REQUEST_IMAGE_CAPTURE);


        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    private void choosePhoto() {
        Intent pickPhoto = new Intent(Intent.ACTION_PICK,
                android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(pickPhoto, REQUEST_IMAGE_PICK);
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

    private int dpToPx(int dp) {
        float density = getApplicationContext().getResources().getDisplayMetrics().density;
        return Math.round((float)dp * density);
    }

}
