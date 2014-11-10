package com.vasiliyrodin.distancecalculator;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.Rect;
import android.hardware.Camera;
import android.hardware.Camera.PictureCallback;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.content.Intent;
import android.content.res.Resources;
import android.view.View;

/**
 * @author Vasiliy
 *
 */
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
	    	  drawText(bitmap,mDistanceText.getText().toString());
	    	  saveBitMap(bitmap);
	    	  Toast.makeText(getApplicationContext(), "taken", Toast.LENGTH_SHORT).show();    	
	      }
	      MainActivity.this.mPreview.startPreview();
	   }
	};
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
    	
    	// create bitmap screen capture
    	/*Bitmap bitmap;
    	View v1 = findViewById(R.id.mainView);
    	v1.setDrawingCacheEnabled(true);
    	bitmap = Bitmap.createBitmap(v1.getDrawingCache());
    	v1.setDrawingCacheEnabled(false);
    	*/
    	
    	mCamera.takePicture(null, null, capturedIt);
    	
    	
    }

	private void saveBitMap(Bitmap bitmap) {
    	// image naming and path  to include sd card  appending name you choose for file
    	File imageFile = new File(Environment.getExternalStorageDirectory(), "distanceCalculator.jpg");   

		OutputStream fout = null;

    	try {
    	    fout = new FileOutputStream(imageFile);
    	    bitmap.compress(Bitmap.CompressFormat.JPEG, 90, fout);
    	    fout.flush();
    	    fout.close();

    	} catch (FileNotFoundException e) {
    	    // TODO Auto-generated catch block
    	    e.printStackTrace();
    	} catch (IOException e) {
    	    // TODO Auto-generated catch block
    	    e.printStackTrace();
    	}
	}
	
	private void drawText(Bitmap bitmap, String text){
 
		  Canvas canvas = new Canvas(bitmap);
		  // new antialised Paint
		  Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
		  // text color - #3D3D3D
		  paint.setColor(Color.WHITE);
		  paint.setStyle(Style.FILL);
		  // text size in pixels
		  paint.setTextSize(bitmap.getHeight()/10);
		  paint.setTextAlign(Paint.Align.RIGHT);
		  // text shadow
		  //paint.setShadowLayer(1f, 0f, 1f, Color.WHITE);
		 
		  // draw text to the Canvas center
		  // Rect bounds = new Rect();
		  // paint.getTextBounds(text, 0, text.length(), bounds);
		  int x = bitmap.getWidth();
		  int y = bitmap.getHeight();
		 
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
	

}

