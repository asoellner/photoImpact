package com.soellner.photoimpact;

import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Message;
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

import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.web.client.RestTemplate;

import java.io.ByteArrayOutputStream;
import java.util.Arrays;

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
    ;
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
                    new HttpRequestTask().execute();

/*

                    AsyncHttpClient client = new AsyncHttpClient();
                    client.get("http://172.20.3.52:8080/SampleApp/greeting/testRest", new JsonHttpResponseHandler() {
                        @Override
                        public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                            System.err.println("");
                        }

*/
/*
                        @Override
                        public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                            String jsonString = new String(responseBody);
                            try {
                                JSONObject jsonObject = new JSONObject(jsonString);
                                System.err.println("");
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }
                        *//*


                        @Override
                        public void onFinish() {
                            super.onFinish();
                        }

                        @Override
                        protected void handleMessage(Message message) {
                            //super.handleMessage(message);
                            Object[] response;
                            try {
                                switch (message.what) {
                                    case SUCCESS_MESSAGE:
                                        response = (Object[]) message.obj;
                                        if (response != null && response.length >= 3) {
                                            onSuccess((Integer) response[0], (Header[]) response[1], (byte[]) response[2]);
                                        } else {
                                            AsyncHttpClient.log.e(LOG_TAG, "SUCCESS_MESSAGE didn't got enough params");
                                        }
                                        break;
                                    case FAILURE_MESSAGE:
                                        response = (Object[]) message.obj;
                                        if (response != null && response.length >= 4) {
                                            onFailure((Integer) response[0], (Header[]) response[1], (byte[]) response[2], (Throwable) response[3]);
                                        } else {
                                            AsyncHttpClient.log.e(LOG_TAG, "FAILURE_MESSAGE didn't got enough params");
                                        }
                                        break;
                                    case START_MESSAGE:
                                        onStart();
                                        break;
                                    case FINISH_MESSAGE:
                                        onFinish();
                                        break;
                                    case PROGRESS_MESSAGE:
                                        response = (Object[]) message.obj;
                                        if (response != null && response.length >= 2) {
                                            try {
                                                onProgress((Long) response[0], (Long) response[1]);
                                            } catch (Throwable t) {
                                                AsyncHttpClient.log.e(LOG_TAG, "custom onProgress contains an error", t);
                                            }
                                        } else {
                                            AsyncHttpClient.log.e(LOG_TAG, "PROGRESS_MESSAGE didn't got enough params");
                                        }
                                        break;
                                    case RETRY_MESSAGE:
                                        response = (Object[]) message.obj;
                                        if (response != null && response.length == 1) {
                                            onRetry((Integer) response[0]);
                                        } else {
                                            AsyncHttpClient.log.e(LOG_TAG, "RETRY_MESSAGE didn't get enough params");
                                        }
                                        break;
                                    case CANCEL_MESSAGE:
                                        onCancel();
                                        break;
                                }
                            } catch (Throwable error) {
                                onUserException(error);
                            }


                        }

                        @Override
                        public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject errorResponse) {
                            System.err.println("");
                        }
                    });
*/


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


    private class HttpRequestTask extends AsyncTask<Void, Void, Greeting > {
        @Override
        protected Greeting  doInBackground(Void... params) {
            try {
                final String url = "http://172.20.3.52:8080/SampleApp/greeting/testRest";




                Bitmap image = BitmapFactory.decodeResource(getResources(), R.drawable.myImage);
                ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
                image.compress(Bitmap.CompressFormat.PNG, 100, byteArrayOutputStream);
                String encodedString = Base64.encodeToString(byteArrayOutputStream.toByteArray(), Base64.DEFAULT);
                reqParams.put("image",encodedString);
                client.post(IMAGE_POST_URL, reqParams, new AsyncHttpResponseHandler() {....});


                http://stackoverflow.com/questions/16939241/send-data-to-rest-service-from-java

                RestTemplate restTemplate = new RestTemplate();
                restTemplate.getMessageConverters().add(new MappingJackson2HttpMessageConverter());
                Greeting  greeting = restTemplate.getForObject(url, Greeting.class);
                return greeting;

            } catch (Exception e) {
                Log.e("MainActivity", e.getMessage(), e);
            }

            return null;
        }

        @Override
        protected void onPostExecute(Greeting  greeting) {
            System.err.println("");
            //Toast.makeText(getApplicationContext(), greeting.getContent(), Toast.LENGTH_LONG).show();

            /*
            TextView greetingIdText = (TextView) findViewById(R.id.id_value);
            TextView greetingContentText = (TextView) findViewById(R.id.content_value);
            greetingIdText.setText(greeting.getId());
            greetingContentText.setText(greeting.getContent());
            */
        }

    }

}
