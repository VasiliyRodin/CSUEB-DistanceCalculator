package com.vasiliyrodin.distancecalculator;

import android.app.Activity;
import android.hardware.Camera;
import android.os.Bundle;
import android.util.Log;

public class MainActivity extends Activity {
	private static final String TAG = "MainActivity";
	private Camera mCamera;
	private CameraPreview mPreview;

    @Override
    public void onCreate(Bundle savedInstanceState) {
    	Log.d(TAG, "onCreate Entered");
        super.onCreate(savedInstanceState);
        //mPreview = new CameraPreview(this);
        //setContentView(mPreview);
        setContentView(R.layout.activity_main);
        mPreview = (CameraPreview) findViewById(R.id.cameraPreview);    
    }
    
	private boolean safeCameraOpen() {
		Log.d(TAG, "safeCameraOpen Entered");
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
		Log.d(TAG, "releaseCameraAndPreview Entered");
		mPreview.setCamera(null);
		if (mCamera != null) {
			mCamera.release();
			mCamera = null;
		}
	}

	@Override
	protected void onResume() {
		Log.d(TAG, "onResume Entered");
		super.onResume();
		safeCameraOpen();
        mPreview.setCamera(mCamera);
	}
	
    @Override
    protected void onPause() {
        super.onPause();

        // Because the Camera object is a shared resource, it's very
        // important to release it when the activity is paused.
        releaseCameraAndPreview();
        
    }

}

