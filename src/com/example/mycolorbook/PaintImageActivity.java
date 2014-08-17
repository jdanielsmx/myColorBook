package com.example.mycolorbook;

import java.io.File;

import org.opencv.android.Utils;
import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.Scalar;
import org.opencv.highgui.Highgui;
import org.opencv.imgproc.Imgproc;

import android.support.v7.app.ActionBarActivity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ImageView;

public class PaintImageActivity extends ActionBarActivity {

	private String strFilename;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_paint_image);
		
		Intent intent = getIntent();
		strFilename = intent.getStringExtra("FILENAME");
		LoadImage();
	}
	
	private void LoadImage()
	{
		/*
		File imgFile = new  File(strFilename);
		if(imgFile.exists()){

		    Bitmap myBitmap = BitmapFactory.decodeFile(imgFile.getAbsolutePath());

		    ImageView myImage = (ImageView) findViewById(R.id.imageView1);
		    myImage.setImageBitmap(myBitmap);
		}
		*/
		File file = new File(strFilename);	   	    
	    if(file.exists())
	    {

	    	Mat m = Highgui.imread(file.getAbsolutePath());
	    	Mat mIntermediateMat = new Mat();
	    	Imgproc.cvtColor(m, m, Imgproc.COLOR_BGR2GRAY);
	    	//Imgproc.Canny(m, mIntermediateMat, 80, 90);
	    	/*
		    Mat invertcolormatrix= new Mat(mIntermediateMat.rows(),mIntermediateMat.cols(), 
		    		mIntermediateMat.type(), new Scalar(255,255,255));

		    Core.subtract(invertcolormatrix, mIntermediateMat, mIntermediateMat);
	    	*/
	    	Mat mInv = m;//mIntermediateMat; //invertcolormatrix;
	    	Bitmap bm = Bitmap.createBitmap(mInv.cols(), mInv.rows(),Bitmap.Config.ARGB_8888);
	        Utils.matToBitmap(mInv, bm);
		    ImageView myImage = (ImageView) findViewById(R.id.imageView1);
		    myImage.setImageBitmap(bm);
		    

		    
	    }
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		menu.addSubMenu("Color Effect");
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.paint_image, menu);
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
}
