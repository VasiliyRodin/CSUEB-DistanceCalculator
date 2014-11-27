package com.vasiliyrodin.distancecalculator;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.hardware.Camera;
import android.hardware.Camera.PictureCallback;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

/**
 * @author Vasiliy
 *
 */
public class MainActivity extends Activity implements SensorEventListener {
	private static final String HEIGHT = "Height";
	private static final String TAG = "MainActivity";
	private static final int REQUEST_CODE = 1;
	private static final String PREFS_FILE = "Prefs";
	private Camera mCamera;
	private CameraPreview mPreview;
	private float[] mGravity;
	private float[] mGeomagnetic;
	private SensorManager mSensorManager;
	private Sensor accelerometer;
	private Sensor magnetometer;
	private TextView mDistanceText;
	private double height = 5; 
	private SharedPreferences mPrefs;

	
	/**
	 * Called when picture is taken. Create Bitmap and save it a file.
	 */
	private PictureCallback capturedIt = new PictureCallback() {

	      @Override
	      public void onPictureTaken(byte[] data, Camera camera) {
	    
	      Bitmap bitmap = BitmapFactory.decodeByteArray(data , 0, data .length);
	      if(bitmap==null){
	         Toast.makeText(getApplicationContext(), "not taken", Toast.LENGTH_SHORT).show();
	      }
	      else
	      {
	    	  android.graphics.Bitmap.Config bitmapConfig =
	    		      bitmap.getConfig();
	    		  // set default bitmap config if none
	    		  if(bitmapConfig == null) {
	    		    bitmapConfig = android.graphics.Bitmap.Config.ARGB_8888;
	    		  }
	    	  bitmap = bitmap.copy(bitmapConfig, true);
	    	  drawOverlay(bitmap);
	    	  saveBitMap(bitmap);
	    	  Toast.makeText(getApplicationContext(), "taken", Toast.LENGTH_SHORT).show();    	
	      }
	      MainActivity.this.mPreview.startPreview();
	   }

	};
	
	/**
	 *	Read saved height from previous run and acquires sensors.
	 */
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
        mPrefs = getSharedPreferences(PREFS_FILE, MODE_PRIVATE);
        height = mPrefs.getFloat(HEIGHT, 5);
        setHeightButtonText();
        // get accelerometer and magnetometer sensors
        mSensorManager = (SensorManager)getSystemService(SENSOR_SERVICE);
        accelerometer = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        magnetometer = mSensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);

    }
    
    public void onClickHeight(View view){
    	startActivityForResult(new Intent("com.vasiliyrodin.distancecalculator.InputHeight"),REQUEST_CODE);
    }
    
    /**
     * Gets screen capture of the main view and saves it to a file.
     * @param view
     */
    public void onClickCapture(View view) {
    	mCamera.takePicture(null, null, capturedIt);   	
    }
    
    /**
     * Saved the bitmap taken into Distance Calculator folder.
     * @param bitmap
     */
	private void saveBitMap(Bitmap bitmap) {
    	// image naming and path  to include sd card  appending name you choose for file
    	File imageDir = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES), "DistanceCalculator");   

		OutputStream fout = null;
		
    	try {
    		imageDir.mkdirs();
    		File imageFile = new File(imageDir, createFileName());
    		
    	    fout = new FileOutputStream(imageFile);
    	    bitmap.compress(Bitmap.CompressFormat.JPEG, 90, fout);
    	    fout.flush();
    	    fout.close();
    	    sendBroadcast(new Intent(
    	    		Intent.ACTION_MEDIA_SCANNER_SCAN_FILE,
    	    		Uri.parse("file://" + imageFile)));

    	} catch (FileNotFoundException e) {
    	    // TODO Auto-generated catch block
    	    e.printStackTrace();
    	} catch (IOException e) {
    	    // TODO Auto-generated catch block
    	    e.printStackTrace();
    	}
	}
	
	/**
	 * Create unique file name based on time of capture.
	 * @return
	 */
	private String createFileName() {
		Date date = new Date();
		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd-HH-mm-ss", Locale.US);
		String s = dateFormat.format(date);
		return "dc" + s + ".jpg";
	}
	
	/**
	 * Draws height cross hair over captured photo.
	 * @param bitmap
	 */
	private void drawOverlay(Bitmap bitmap) {
		Canvas canvas = new Canvas(bitmap);
		drawText(canvas,mDistanceText.getText().toString(), canvas.getWidth(),canvas.getHeight(), Paint.Align.RIGHT);
		drawCrosshair(canvas);
		
	}

	/**
	 * Draws crosshair at the center of the canvas.
	 * @param canvas
	 */
	private void drawCrosshair(Canvas canvas) {
		Paint paint = new Paint();
		paint.setARGB(255, 255, 255, 255);
		paint.setStrokeWidth(20);
		
		float w = canvas.getWidth();
		float h = canvas.getHeight();
		
		canvas.drawLine(w/2 - h*0.025f, h/2, w/2 + h*0.025f, h/2, paint);
		canvas.drawLine(w/2 , h/2- h*0.025f, w/2 , h/2+ h*0.025f, paint);

	}
	
	/**
	 * Draws text at the specified location with given alignment.
	 * @param canvas
	 * @param text
	 * @param x
	 * @param y
	 * @param align
	 */
	private void drawText(Canvas canvas, String text , int x, int y , Paint.Align align){
 
		  // new antialised Paint
		  Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
		  // text color - #3D3D3D
		  paint.setColor(Color.WHITE);
		  paint.setStyle(Style.FILL);
		  // text size in pixels
		  paint.setTextSize(canvas.getHeight()/10);
		  paint.setTextAlign(align);
		  
		  canvas.drawText(text, x, y, paint);
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
	
	/**
	 * Opens the camera preview and starts to listen for sensors.
	 */
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
	
	/**
	 * Releases camera and stop listening for sensors and saves height.
	 */
    @Override
    protected void onPause() {
        super.onPause();
        // Because the Camera object is a shared resource, it's very
        // important to release it when the activity is paused.
        releaseCameraAndPreview();
        mSensorManager.unregisterListener(this);
        
        SharedPreferences.Editor ed = mPrefs.edit();
        ed.putFloat(HEIGHT, (float) height);
        ed.commit();
        
    }

	@Override
	public void onAccuracyChanged(Sensor arg0, int arg1) {
		
	}
	
	/**
	 * Calculates the angle changed and uses that to calculate the distance.
	 * Using the accelerometer and Magnetic Field sensors.
	 */
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
		        DecimalFormat df = new DecimalFormat("#.##");
		        mDistanceText.setText(df.format(distance));
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
				setHeightButtonText();
			}
		}
	}
	
	
	/**
	 * Sets the height button text to show current height
	 */
	private void setHeightButtonText(){
		Button b = (Button) findViewById(R.id.setHeightButton);
		b.setText("Height=" + String.valueOf(height));
		
	}
	
	/** Restores previously saved height or uses default value of 5
	 * @see android.app.Activity#onRestoreInstanceState(android.os.Bundle)
	 */
	@Override
	protected void onRestoreInstanceState(Bundle savedInstanceState) {
		Log.d(TAG,"onRestoreInstanceState Entered");
		super.onRestoreInstanceState(savedInstanceState);
		height = savedInstanceState.getDouble(HEIGHT, 5);
	}

	
	/** Saves Height to instance state. Allowing it to be saved if program closes.
	 * @see android.app.Activity#onSaveInstanceState(android.os.Bundle)
	 *  
	 */
	@Override
	protected void onSaveInstanceState(Bundle outState) {
		Log.d(TAG, "onSaveInstanceState Entered");
		super.onSaveInstanceState(outState);
		outState.putDouble(HEIGHT, height);
		
	}
	
	
	

}

