package com.vasiliyrodin.distancecalculator;

import android.app.Activity;
import android.hardware.Camera;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.widget.TextView;
import android.widget.Toast;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.content.Intent;
import android.view.View;

public class MainActivity extends Activity implements SensorEventListener {
	private static final String TAG = "MainActivity";
	private static final int REQUEST_CODE = 1;
	private Camera mCamera;
	private CameraPreview mPreview;
	private float[] mGravity;
	private float[] mGeomagnetic;
	private SensorManager mSensorManager;
	private Sensor accelerometer;
	private Sensor magnetometer;
	private TextView mDistanceText;
	private double height = 5; 
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
    	Log.d(TAG, "onCreate Entered");
        super.onCreate(savedInstanceState);
        //mPreview = new CameraPreview(this);
        //setContentView(mPreview);
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_main);
        mPreview = (CameraPreview) findViewById(R.id.cameraPreview); 
        mDistanceText = (TextView) findViewById(R.id.distance);
        // get accelerometer and magnetometer sensors
        mSensorManager = (SensorManager)getSystemService(SENSOR_SERVICE);
        accelerometer = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        magnetometer = mSensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);

    }
    
    public void onClickHeight(View view){
    	startActivityForResult(new Intent("com.vasiliyrodin.distancecalculator.InputHeight"),REQUEST_CODE);
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
		// starts preview
        mPreview.setCamera(mCamera);
        mSensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_UI);
        mSensorManager.registerListener(this, magnetometer, SensorManager.SENSOR_DELAY_UI);

	}
	
    @Override
    protected void onPause() {
        super.onPause();
        // Because the Camera object is a shared resource, it's very
        // important to release it when the activity is paused.
        releaseCameraAndPreview();
        mSensorManager.unregisterListener(this);
        
    }

	@Override
	public void onAccuracyChanged(Sensor arg0, int arg1) {
		
	}

	@Override
	public void onSensorChanged(SensorEvent event) {
		if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER)
		      mGravity = event.values;
		    if (event.sensor.getType() == Sensor.TYPE_MAGNETIC_FIELD)
		      mGeomagnetic = event.values;
		    if (mGravity != null && mGeomagnetic != null) {
		      float R[] = new float[9];
		      float I[] = new float[9];
		      boolean success = SensorManager.getRotationMatrix(R, I, mGravity, mGeomagnetic);
		      if (success) {
		        float orientation[] = new float[3];
		        SensorManager.getOrientation(R, orientation);
		        
		        //Calculate the distance
		        double distance = height*Math.tan(Math.abs(orientation[2]));
		        mDistanceText.setText(String.valueOf(distance));
		        //Log.d(TAG, "Orientation = " + orientation[0] + " " + orientation[1] + " " + orientation[2]);
		        //azimut = orientation[0]; // orientation contains: azimut, pitch and roll
		      }
		 }
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if(requestCode == REQUEST_CODE) {
			if(resultCode == RESULT_OK) {
				height = Double.valueOf(data.getData().toString());
			}
		}
	}
	
	

}

