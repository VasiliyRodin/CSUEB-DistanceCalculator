package com.vasiliyrodin.distancecalculator;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
public class InputHeight extends Activity{
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.input_height);
	}

	public void onClickOKButton(View view) {
		Intent data = new Intent();
		
		//Get the EditText view
		EditText heightText = (EditText) findViewById(R.id.heightText);
		
		// set the data to pass back
		data.setData(Uri.parse(heightText.getText().toString()));
		setResult(RESULT_OK, data);
		
		finish();
	}
}
