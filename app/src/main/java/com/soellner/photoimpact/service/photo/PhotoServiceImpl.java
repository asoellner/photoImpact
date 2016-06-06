package com.soellner.photoimpact.service.photo;

import android.graphics.Bitmap;

import org.glassfish.jersey.client.ClientConfig;
import org.glassfish.jersey.client.ClientResponse;

import java.io.File;


import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;

/**
 * Created by asoel on 06.06.2016.
 */
public class PhotoServiceImpl implements PhotoService {


    @Override
    public void upload(Bitmap bitmap) {

        Client client = ClientBuilder.newClient();
        WebTarget webTarget = client.target("localhost:8080/SampleApp/upload");
        String response = webTarget.request().get(String.class);

        final File fileToUpload = new File("C:/Users/Public/Pictures/Desert.jpg");

        final FormDataMultiPart multiPart = new FormDataMultiPart();
        if (fileToUpload != null) {
            multiPart.bodyPart(new FileDataBodyPart("file", fileToUpload,
                    MediaType.APPLICATION_OCTET_STREAM_TYPE));
        }

        final ClientResponse clientResp = resource.type(
                MediaType.MULTIPART_FORM_DATA_TYPE).post(ClientResponse.class,
                multiPart);
        System.out.println("Response: " + clientResp.getClientResponseStatus());

        client.destroy();
    }
}
