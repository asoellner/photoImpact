package com.soellner.photoimpact.service.photo;

import android.graphics.Bitmap;
import android.util.Log;


import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;


/**
 * Created by asoel on 06.06.2016.
 */
public class PhotoServiceImpl implements PhotoService {


    @Override
    public void uploadWithRest(Bitmap bitmap) {

    }



    @Override
    public void upload(Bitmap bitmap) {
        try {

            String charset = "UTF-8";
            File uploadFile = convertBitmapToFile(bitmap);
            String requestURL = "http://localhost:8080";

            MultipartUtility multipart = new MultipartUtility(requestURL, charset);

//            multipart.addHeaderField("User-Agent", "CodeJava");
//            multipart.addHeaderField("Test-Header", "Header-Value");

            multipart.addFormField("user_id", "asoel");
            multipart.addFormField("user_name", "Alexander");

            multipart.addFilePart("image", uploadFile);

            List<String> response = multipart.finish();

            Log.v("rht", "SERVER REPLIED:");

            for (String line : response) {
                Log.v("rht", "Line : " + line);

            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }


    private File convertBitmapToFile(Bitmap bitmap) {
        File file = new File("");
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
}