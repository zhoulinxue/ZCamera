package org.zhx.common.camera.demo;

import android.net.Uri;
import android.os.Bundle;
import android.view.WindowManager;


import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.exifinterface.media.ExifInterface;

import com.davemorrissey.labs.subscaleview.ImageSource;
import com.davemorrissey.labs.subscaleview.SubsamplingScaleImageView;

import org.zhx.common.util.ZCameraLog;

import java.io.InputStream;

public class ShowImageActivity extends AppCompatActivity {
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show_image);
        getWindow().addFlags((WindowManager.LayoutParams.FLAG_FULLSCREEN));
        SubsamplingScaleImageView showImage = findViewById(R.id.z_base_camera_showImg_big);
        String path = getIntent().getStringExtra("bitmap");

        Uri uri = Uri.parse(path);
        try {
            InputStream inStream = getContentResolver().openInputStream(uri);
            ExifInterface exifInterface = new ExifInterface(inStream);

            String orientation = exifInterface.getAttribute(ExifInterface.TAG_ORIENTATION);
            String dateTime = exifInterface.getAttribute(ExifInterface.TAG_DATETIME);
            String make = exifInterface.getAttribute(ExifInterface.TAG_MAKE);
            String model = exifInterface.getAttribute(ExifInterface.TAG_MODEL);
            String flash = exifInterface.getAttribute(ExifInterface.TAG_FLASH);
            String imageLength = exifInterface.getAttribute(ExifInterface.TAG_IMAGE_LENGTH);
            String imageWidth = exifInterface.getAttribute(ExifInterface.TAG_IMAGE_WIDTH);
            String latitude = exifInterface.getAttribute(ExifInterface.TAG_GPS_LATITUDE);
            String longitude = exifInterface.getAttribute(ExifInterface.TAG_GPS_LONGITUDE);
            String latitudeRef = exifInterface.getAttribute(ExifInterface.TAG_GPS_LATITUDE_REF);
            String longitudeRef = exifInterface.getAttribute(ExifInterface.TAG_GPS_LONGITUDE_REF);
            String exposureTime = exifInterface.getAttribute(ExifInterface.TAG_EXPOSURE_TIME);

            String dateTimeDigitized = exifInterface.getAttribute(ExifInterface.TAG_DATETIME_DIGITIZED);
            String subSecTime = exifInterface.getAttribute(ExifInterface.TAG_SUBSEC_TIME);
            String altitude = exifInterface.getAttribute(ExifInterface.TAG_GPS_ALTITUDE);
            String altitudeRef = exifInterface.getAttribute(ExifInterface.TAG_GPS_ALTITUDE_REF);
            String gpsTimeStamp = exifInterface.getAttribute(ExifInterface.TAG_GPS_TIMESTAMP);
            String gpsDateStamp = exifInterface.getAttribute(ExifInterface.TAG_GPS_DATESTAMP);
            String whiteBalance = exifInterface.getAttribute(ExifInterface.TAG_WHITE_BALANCE);
            String focalLength = exifInterface.getAttribute(ExifInterface.TAG_FOCAL_LENGTH);
            String processingMethod = exifInterface.getAttribute(ExifInterface.TAG_GPS_PROCESSING_METHOD);

            ZCameraLog.e("TAG", "## orientation=" + orientation);
            ZCameraLog.e("TAG", "## dateTime=" + dateTime);
            ZCameraLog.e("TAG", "## make=" + make);
            ZCameraLog.e("TAG", "## model=" + model);
            ZCameraLog.e("TAG", "## flash=" + flash);
            ZCameraLog.e("TAG", "## imageLength=" + imageLength);
            ZCameraLog.e("TAG", "## imageWidth=" + imageWidth);
            ZCameraLog.e("TAG", "## latitude=" + latitude);
            ZCameraLog.e("TAG", "## longitude=" + longitude);
            ZCameraLog.e("TAG", "## latitudeRef=" + latitudeRef);
            ZCameraLog.e("TAG", "## longitudeRef=" + longitudeRef);
            ZCameraLog.e("TAG", "## exposureTime=" + exposureTime);
            ZCameraLog.e("TAG", "## dateTimeDigitized=" + dateTimeDigitized);
            ZCameraLog.e("TAG", "## subSecTime=" + subSecTime);
            ZCameraLog.e("TAG", "## altitude=" + altitude);
            ZCameraLog.e("TAG", "## altitudeRef=" + altitudeRef);
            ZCameraLog.e("TAG", "## gpsTimeStamp=" + gpsTimeStamp);
            ZCameraLog.e("TAG", "## gpsDateStamp=" + gpsDateStamp);
            ZCameraLog.e("TAG", "## whiteBalance=" + whiteBalance);
            ZCameraLog.e("TAG", "## focalLength=" + focalLength);
            ZCameraLog.e("TAG", "## processingMethod=" + processingMethod);
        } catch (Exception e) {
            e.printStackTrace();
        }

        showImage.setImage(ImageSource.uri(uri));
    }
}
