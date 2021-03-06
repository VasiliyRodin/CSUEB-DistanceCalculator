package com.vasiliyrodin.distancecalculator;

import java.io.IOException;
import java.util.List;

import android.content.Context;
import android.hardware.Camera;
import android.hardware.Camera.Size;
import android.util.AttributeSet;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

public class CameraPreview extends SurfaceView implements SurfaceHolder.Callback {
	
	private static final String TAG = "CameraPreview";
	SurfaceHolder mHolder;
	private Camera mCamera;
	public CameraPreview(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		initialize(context);
	}

	public CameraPreview(Context context, AttributeSet attrs) {
		super(context, attrs);
		initialize(context);
	}

	public CameraPreview(Context context) {
		super(context);
		initialize(context);
	}

	private void initialize(Context context) {
		mHolder = getHolder();
		mHolder.addCallback(this);
		mHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
	}

	@Override
	protected void onLayout(boolean changed, int l, int t, int r, int b) {
		// TODO Auto-generated method stub

	}

	@Override
	public void surfaceCreated(SurfaceHolder holder) {
		Log.d(TAG, "surfaceCreated Entered");
		try {
            if (mCamera != null) {
                mCamera.setPreviewDisplay(holder);
            }
        } catch (IOException exception) {
            Log.e(TAG, "IOException caused by setPreviewDisplay()", exception);
        }
		
	}

	@Override
	public void surfaceChanged(SurfaceHolder holder, int format, int width,
			int height) {
		Log.d(TAG, "surfaceChanged Entered");
		
        startPreview();
		
	}

	public void startPreview() {
		if (mCamera == null)
			return;

        if (mHolder.getSurface() == null){
          // preview surface does not exist
          return;
        }

		// stop preview before making changes
        try {
            mCamera.stopPreview();
        } catch (Exception e){
          // ignore: tried to stop a non-existent preview
        }
		
		try { 
			mCamera.setPreviewDisplay(mHolder);
			mCamera.startPreview();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void surfaceDestroyed(SurfaceHolder holder) {
		Log.d(TAG, "surfaceDestroyed Entered");
		if(mCamera != null) {
			mCamera.stopPreview();
		}
		
	}
	public void setCamera(Camera camera) {
		Log.d(TAG, "setCamera Entered");
		if (mCamera == camera) { return; }
		
		stopPreviewAndFreeCamera();
		
		mCamera = camera;
		
		if(mCamera != null) {
			List<Size> localSizes = mCamera.getParameters().getSupportedPreviewSizes();
			requestLayout();
		}
	}

	private void stopPreviewAndFreeCamera() {
		Log.d(TAG, "stopPreviewAndFreeCamera Entered");
		if(mCamera != null) {
			mCamera.stopPreview();
			
			//mCamera.release();
			mCamera = null;
		}
	}

}
