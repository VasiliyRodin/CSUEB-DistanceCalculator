package com.vasiliyrodin.distancecalculator;

import java.io.IOException;

import android.app.Activity;
import android.hardware.Camera;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

public class MainActivity extends Activity {
	private Camera mCamera;
	private CameraPreview mPreview;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //mPreview = new CameraPreview(this);
        //setContentView(mPreview);
        setContentView(R.layout.activity_main);
        mPreview = (CameraPreview) findViewById(R.id.cameraPreview);
        
    }
    
	private boolean safeCameraOpen() {
		boolean qOpened = false;
		
		try {
			releaseCameraAndPreview();
			mCamera = Camera.open();
			qOpened = (mCamera != null);
		} catch (Exception e) {
			Log.e(getString(R.string.app_name), "Failed to opent the camera");
			e.printStackTrace();
		}
		return qOpened;
	}
	
	private void releaseCameraAndPreview() {
		mPreview.setCamera(null);
		if (mCamera != null) {
			mCamera.release();
			mCamera = null;
		}
	}

	@Override
	protected void onResume() {
		super.onResume();
		safeCameraOpen();
        mPreview.setCamera(mCamera);
	}
}

