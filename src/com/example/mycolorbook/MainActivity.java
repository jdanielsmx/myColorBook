package com.example.mycolorbook;

import android.support.v7.app.ActionBarActivity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

public class MainActivity extends ActionBarActivity {

	protected static final String FILENAME = "FILENAME";
	static final int REQUEST_CODE_FILENAME = 1;
	TextView tvFilename;
	private String strFilename;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		strFilename="";
		tvFilename=(TextView)findViewById(R.id.tvFilename);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();
		if (id == R.id.action_settings) {
			return true;
		}
		return super.onOptionsItemSelected(item);
	}
	
	/** Called when the user clicks the Take image button */
	public void openTakeImageActivity(View view) {
	    // Do something in response to button
		Intent intent = new Intent(this, TakeImageActivity.class);
		//startActivity(intent);
		startActivityForResult(intent, REQUEST_CODE_FILENAME);
	}
	
	/** Called when the user clicks the Paint image button */
	public void openPaintImageActivity(View view) {
	    // Do something in response to button
		Intent intent = new Intent(this, PaintImageActivity.class);
		intent.putExtra(FILENAME, strFilename);
		startActivity(intent);
	}
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
	    // Check which request we're responding to
	    if (requestCode == REQUEST_CODE_FILENAME) {
	        // Make sure the request was successful
	        if (resultCode == RESULT_OK) {
	            // The user picked a contact.
	            // The Intent's data Uri identifies which contact was selected.

	            // Do something with the contact here (bigger example below)
	        	strFilename=data.getStringExtra(FILENAME);
	        	tvFilename.setText(strFilename);
	        }
	    }
	}
}
