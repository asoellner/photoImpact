package com.soellner.photoimpact;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Typeface;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Environment;
import android.os.StrictMode;
import android.provider.MediaStore;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.loopj.android.http.Base64;
import com.soellner.photoimpact.photoimpact.R;

import org.glassfish.hk2.api.Metadata;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;

import java.net.URL;
import java.net.URLConnection;
import java.sql.Timestamp;
import java.util.NoSuchElementException;


public class MainActivity extends AppCompatActivity {

    public static final String FILE_SEPARATOR = System.getProperty("file.separator");

    static final int REQUEST_IMAGE_CAPTURE = 1;
    static final int REQUEST_IMAGE_PICK = 2;
    static final int REQUEST_PERMISSION = 3;
    private Bitmap _bitmap;

    private Button _takePhotoButton;
    private Button _uploadButton;
    private Uri _mImageUri;

    //keller
    //private String SERVER_URL="http://192.168.1.124:8080/SampleApp/greeting/crunchifyService";

    //henny
    //private String SERVER_URL = "http://192.168.1.139:8080/SampleApp/greeting/crunchifyService";

    //work
    private String SERVER_URL = "http://172.20.3.52:8080/SampleApp/main/uploadImage";


    Integer _count = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


        setContentView(R.layout.activity_main);
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();

        StrictMode.setThreadPolicy(policy);


        //check permissions
        if (ActivityCompat.checkSelfPermission(getBaseContext(), Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(getBaseContext(), Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                    REQUEST_PERMISSION);

            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                    REQUEST_PERMISSION);
        }


        _takePhotoButton = (Button) findViewById(R.id.takePhotoButton);
        _uploadButton = (Button) findViewById(R.id.uploadButton);
        assert _uploadButton != null;

        _uploadButton.setEnabled(false);

        if (savedInstanceState != null) {


            if (_mImageUri != null) {
                showTakenPhoto();
            }

            _mImageUri = savedInstanceState.getParcelable("mImageUri");
            if (_mImageUri != null) {
                showTakenPhoto();
            }


        }

        addTakePhotoListener();
        addPickPhotoListener();
        addUploadPhotoListener();


    }

    private void addPickPhotoListener() {
        Button pickButton = (Button) findViewById(R.id.pickPhotoButton);
        assert pickButton != null;
        pickButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                choosePhoto();
            }


        });
    }

    private void addUploadPhotoListener() {
        assert _uploadButton != null;
        _uploadButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                if (_bitmap != null) {
                    new UploadTask().execute();

                }

            }
        });
    }

    private void addTakePhotoListener() {
        assert _takePhotoButton != null;
        _takePhotoButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                takePhoto();
            }
        });


    }

    //called after camera intent finished
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent intent) {


        //photo taken from cam
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
            if (_mImageUri != null) {
                showTakenPhoto();
            }
        }

        //photo chosen
        if (requestCode == REQUEST_IMAGE_PICK && resultCode == RESULT_OK) {
            _mImageUri = intent.getData();


            try {
                _bitmap = MediaStore.Images.Media.getBitmap(
                        getContentResolver(), _mImageUri);
                ImageView imageView = (ImageView) findViewById(R.id.uploadedImage);


                assert _bitmap != null;
                String[] filePathColumn = {MediaStore.Images.Media.DATA};

                Cursor cursor = getContentResolver().query(
                        _mImageUri, filePathColumn, null, null, null);
                cursor.moveToFirst();

                int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
                String filePath = cursor.getString(columnIndex);
                cursor.close();
                rotateImage(filePath);
                imageView.setImageBitmap(scaleBitmap(_bitmap, 350));
                _uploadButton.setEnabled(true);
            } catch (FileNotFoundException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }


    }

    private void showTakenPhoto() {

        try {
            _bitmap = MediaStore.Images.Media.getBitmap(getApplicationContext().getContentResolver(), _mImageUri);


            if (_bitmap != null) {

                //get EXIF Infos
                File photoFile = new File(_mImageUri.getPath());
                //ExifInterface exif = new ExifInterface(photoFile.toString());

                //rotate image

                rotateImage(photoFile.getPath());


                assert _bitmap != null;
                ImageView imageView = (ImageView) findViewById(R.id.uploadedImage);
                imageView.setImageBitmap(scaleBitmap(_bitmap, 350));
                _uploadButton.setEnabled(true);

            }
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    private void rotateImage(String path) throws IOException {


        int orientation = ExifInterface.ORIENTATION_NORMAL;
        // File photoFile = new File(_mImageUri.getPath());
        ExifInterface exif = new ExifInterface(path);


        orientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL);

        switch (orientation) {
            case ExifInterface.ORIENTATION_ROTATE_90:
                _bitmap = rotateBitmap(_bitmap, 90);
                break;
            case ExifInterface.ORIENTATION_ROTATE_180:
                _bitmap = rotateBitmap(_bitmap, 180);
                break;

            case ExifInterface.ORIENTATION_ROTATE_270:
                _bitmap = rotateBitmap(_bitmap, 270);
                break;
        }

    }

    public static Bitmap rotateBitmap(Bitmap bitmap, int degrees) {
        Matrix matrix = new Matrix();
        matrix.postRotate(degrees);

        //BitmapFactory.Options opts = new BitmapFactory.Options();
        //opts.inJustDecodeBounds = false;
        //opts.inPreferredConfig = Bitmap.Config.RGB_565;
        //opts.inDither = true;
        return Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
    }


    private Bitmap scaleBitmap(Bitmap bitmap, int size) {

        int width = 0;

        try {
            width = bitmap.getWidth();
        } catch (NullPointerException e) {
            throw new NoSuchElementException("Can't find bitmap on given view/drawable");
        }

        int height = bitmap.getHeight();
        int bounding = dpToPx(size);
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
        // BitmapDrawable result = new BitmapDrawable(scaledBitmap);


        Log.i("Test", "scaled width = " + Integer.toString(width));
        Log.i("Test", "scaled height = " + Integer.toString(height));

        return scaledBitmap;
    }


    @Override
    protected void onSaveInstanceState(Bundle outState) {
        if (_mImageUri == null) {
            return;
        }

        showTakenPhoto();

        super.onSaveInstanceState(outState);
        outState.putParcelable("mImageUri", _mImageUri);
    }


    private void takePhoto() {

        try {

            //checkPermisions
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {

                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                        REQUEST_PERMISSION);

            }
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {

                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                        REQUEST_PERMISSION);

            }

            File userdir = Environment.getExternalStorageDirectory();
            File filesDir = new File(userdir.getAbsolutePath() + FILE_SEPARATOR + "PhotoImpact" + FILE_SEPARATOR + "DCIM");


            if (!filesDir.exists() && !filesDir.mkdirs()) {
                Log.d("TAG", "Can't create directory to save image.");
                return;
            }

            int time = (int) (System.currentTimeMillis());
            Timestamp tsTemp = new Timestamp(time);
            long ts = tsTemp.getTime();

            String photoFileName = "Photo_" + ts + ".jpg";
            File photoFile = new File(filesDir.getAbsolutePath(), photoFileName);
            _mImageUri = Uri.fromFile(photoFile);


            Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            //_mImageUri = Uri.fromFile(photoFile);
            //cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, _mImageUri);
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


    private int dpToPx(int dp) {
        float density = getApplicationContext().getResources().getDisplayMetrics().density;
        return Math.round((float) dp * density);
    }

    private class UploadTask extends AsyncTask<String, Void, String> {
        ProgressDialog pDialog;
        private byte[] _byteArray;

        @Override
        protected void onPreExecute() {
            try {
                pDialog = new ProgressDialog(MainActivity.this);
                InputStream imageStream = getContentResolver().openInputStream(_mImageUri);
                //Bitmap bm = BitmapFactory.decodeStream(imageStream);
                //resize bitmap
                Bitmap bm = scaleBitmap(_bitmap, 200);
                //Bitmap bm = scaleBitmap(_bitmap, 400);
                //MainActivity.rotateBitmap(bm)

                ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
                bm.compress(Bitmap.CompressFormat.JPEG, 100, byteArrayOutputStream);
                _byteArray = byteArrayOutputStream.toByteArray();

                String size = humanReadableByteCount(_byteArray.length, true);

                pDialog.setMessage("Uploading " + size);

            } catch (Exception e) {
                e.printStackTrace();
            }


            pDialog.show();
        }

        @Override
        protected String doInBackground(String... params) {

            try {


                String[] filePathColumn = {MediaStore.Images.Media.DATA};

                Cursor cursor = getContentResolver().query(
                        _mImageUri, filePathColumn, null, null, null);
                cursor.moveToFirst();

                int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
                String filePath = cursor.getString(columnIndex);
                cursor.close();
                //rotateImage(filePath);
                /*
                InputStream imageStream = getContentResolver().openInputStream(_mImageUri);
                Bitmap bm = BitmapFactory.decodeStream(imageStream);

                //resize bitmap
                bm = scaleBitmap(bm, 600);

                ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
                bm.compress(Bitmap.CompressFormat.JPEG, 100, byteArrayOutputStream);
                byte[] byteArray = byteArrayOutputStream.toByteArray();
*/
                String imageEncoded = Base64.encodeToString(_byteArray, Base64.DEFAULT);
                //File photoFile = new File(_mImageUri.getPath());
                ExifInterface exif = new ExifInterface(filePath);

                JSONObject obj = new JSONObject();

                obj.put("image", imageEncoded);

                obj.put("TAG_DATETIME", getTagString(ExifInterface.TAG_DATETIME, exif));

                String latidue = getTagString(ExifInterface.TAG_GPS_LATITUDE, exif);
                String longitude = getTagString(ExifInterface.TAG_GPS_LONGITUDE, exif);
                if (latidue != null && longitude != null) {
                    if (latidue.contains("/")) {
                        obj.put("TAG_GPS_LATITUDE", converGPS(latidue));
                        obj.put("TAG_GPS_LONGITUDE", converGPS(longitude));
                    } else {
                        obj.put("TAG_GPS_LATITUDE", getTagString(ExifInterface.TAG_GPS_LATITUDE, exif));
                        //obj.put("TAG_GPS_LATITUDE_REF", getTagString(ExifInterface.TAG_GPS_LATITUDE_REF, exif));
                        obj.put("TAG_GPS_LONGITUDE", getTagString(ExifInterface.TAG_GPS_LONGITUDE, exif));
                        //obj.put("TAG_GPS_LONGITUDE_REF", getTagString(ExifInterface.TAG_GPS_LONGITUDE_REF, exif));
                    }



                } else {
                    obj.put("TAG_GPS_LATITUDE", "");

                    obj.put("TAG_GPS_LONGITUDE", "");
                }


              /*

                String myAttribute="Exif information ---\n";
                myAttribute += getTagString(ExifInterface.TAG_DATETIME, exif);
                myAttribute += getTagString(ExifInterface.TAG_FLASH, exif);
                myAttribute += getTagString(ExifInterface.TAG_GPS_LATITUDE, exif);
                myAttribute += getTagString(ExifInterface.TAG_GPS_LATITUDE_REF, exif);
                myAttribute += getTagString(ExifInterface.TAG_GPS_LONGITUDE, exif);
                myAttribute += getTagString(ExifInterface.TAG_GPS_LONGITUDE_REF, exif);
                myAttribute += getTagString(ExifInterface.TAG_IMAGE_LENGTH, exif);
                myAttribute += getTagString(ExifInterface.TAG_IMAGE_WIDTH, exif);
                myAttribute += getTagString(ExifInterface.TAG_MAKE, exif);
                myAttribute += getTagString(ExifInterface.TAG_MODEL, exif);
                myAttribute += getTagString(ExifInterface.TAG_ORIENTATION, exif);
                myAttribute += getTagString(ExifInterface.TAG_WHITE_BALANCE, exif);
*/


                //URL url = new URL("http://192.168.1.124:8080/SampleApp/greeting/crunchifyService");
                URL url = new URL(SERVER_URL);

                //URL url = new URL("http://172.20.3.52:8080/SampleApp/greeting/crunchifyService");
                URLConnection connection = url.openConnection();
                connection.setDoOutput(true);
                connection.setRequestProperty("Content-Type", "application/json");
                connection.setConnectTimeout(10000);
                connection.setReadTimeout(10000);
                OutputStreamWriter out = new OutputStreamWriter(connection.getOutputStream());
                out.write(obj.toString());
                out.close();

                BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));

                System.out.println("\nCrunchify REST Service Invoked Successfully..");
                in.close();


            } catch (Exception e) {
                e.printStackTrace();
            }


            return null;
        }

        private Float converGPS(String coordinate) {
            Float result = null;
            String[] DMS = coordinate.split(",", 3);

            String[] stringD = DMS[0].split("/", 2);
            Double D0 = new Double(stringD[0]);
            Double D1 = new Double(stringD[1]);
            Double FloatD = D0 / D1;

            String[] stringM = DMS[1].split("/", 2);
            Double M0 = new Double(stringM[0]);
            Double M1 = new Double(stringM[1]);
            Double FloatM = M0 / M1;

            String[] stringS = DMS[2].split("/", 2);
            Double S0 = new Double(stringS[0]);
            Double S1 = new Double(stringS[1]);
            Double FloatS = S0 / S1;

            result = new Float(FloatD + (FloatM / 60) + (FloatS / 3600));

            return result;
        }

        protected void onPostExecute(String params) {
            super.onPostExecute(params);
            pDialog.dismiss();
            TextView resultText = (TextView) findViewById(R.id.resultText);
            resultText.setTextColor(Color.rgb(0, 240, 0));
            resultText.setTypeface(null, Typeface.BOLD);
            resultText.setText("Upload successful");


        }

    }

    public static String humanReadableByteCount(long bytes, boolean si) {
        int unit = si ? 1000 : 1024;
        if (bytes < unit) return bytes + " B";
        int exp = (int) (Math.log(bytes) / Math.log(unit));
        String pre = (si ? "kMGTPE" : "KMGTPE").charAt(exp - 1) + (si ? "" : "i");
        return String.format("%.1f %sB", bytes / Math.pow(unit, exp), pre);
    }

    private String getTagString(String tag, ExifInterface exif) {
        return (exif.getAttribute(tag));
    }

}
