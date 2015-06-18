package com.codegy.aerlink.cameraremote;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.wearable.view.WatchViewStub;
import android.widget.ImageView;
import com.codegy.aerlink.Constants;
import com.codegy.aerlink.R;

public class CameraImageActivity extends Activity {

    private ImageView mCameraImageView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_camera_image);


        if (getIntent() != null) {
            final Bitmap cameraImage = getIntent().getParcelableExtra(Constants.IE_CAMERA_IMAGE);

            if (cameraImage != null) {
                final WatchViewStub stub = (WatchViewStub) findViewById(R.id.watch_view_stub);
                stub.setOnLayoutInflatedListener(new WatchViewStub.OnLayoutInflatedListener() {
                    @Override
                    public void onLayoutInflated(WatchViewStub stub) {
                        mCameraImageView = (ImageView) stub.findViewById(R.id.cameraImageView);
                        mCameraImageView.setImageBitmap(cameraImage);
                    }
                });
            }
            else {
                finish();
            }
        }
        else {
            finish();
        }
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);

        if (intent != null) {
            final Bitmap cameraImage = intent.getParcelableExtra(Constants.IE_CAMERA_IMAGE);

            if (cameraImage != null) {
                mCameraImageView.setImageBitmap(cameraImage);
            }
        }
    }
}
