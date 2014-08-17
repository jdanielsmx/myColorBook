package com.example.mycolorbook;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.ListIterator;
import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.android.CameraBridgeViewBase.CvCameraViewFrame;
import org.opencv.android.CameraBridgeViewBase.CvCameraViewListener2;
import org.opencv.android.Utils;
import org.opencv.core.Mat;
import org.opencv.highgui.Highgui;
import org.opencv.imgproc.Imgproc;
import android.support.v7.app.ActionBarActivity;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Bitmap;
import android.hardware.Camera.Size;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.SubMenu;
import android.view.SurfaceView;
import android.view.View;
import android.view.WindowManager;
import android.view.View.OnTouchListener;
import android.widget.Toast;

public class TakeImageActivity extends ActionBarActivity implements CvCameraViewListener2, OnTouchListener {
    private static final String TAG = "TakeImageActivity::Activity";
    
    private Mat mIntermediateMat;
    private Mat rgba;
    private Mat rgbaInnerWindow;
    private ColorBookCameraView mOpenCvCameraView;
    private List<Size> mResolutionList;
    private MenuItem[] mEffectMenuItems;
    private SubMenu mColorEffectsMenu;
    private MenuItem[] mResolutionMenuItems;
    private SubMenu mResolutionMenu;
    private Intent intentFileName;
    
    private BaseLoaderCallback mLoaderCallback = new BaseLoaderCallback(this) {
        @Override
        public void onManagerConnected(int status) {
            switch (status) {
                case LoaderCallbackInterface.SUCCESS:
                {
                    Log.i(TAG, "OpenCV loaded successfully");
                    mOpenCvCameraView.enableView();
                    mOpenCvCameraView.setOnTouchListener(TakeImageActivity.this);
                } break;
                default:
                {
                    super.onManagerConnected(status);
                } break;
            }
        }
    };
    
    public TakeImageActivity()
    {
    	Log.i(TAG, "Instantiated new " + this.getClass());
    }
    
	@Override
	protected void onCreate(Bundle savedInstanceState) {
        Log.i(TAG, "called onCreate");
        
        super.onCreate(savedInstanceState);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        setContentView(R.layout.activity_take_image);
        
        mOpenCvCameraView = (ColorBookCameraView) findViewById(R.id.colorbookcameraview_activity_java_surface_view);

        mOpenCvCameraView.setVisibility(SurfaceView.VISIBLE);

        mOpenCvCameraView.setCvCameraViewListener(this);
	}
	
    @Override
    public void onPause()
    {
        super.onPause();
        if (mOpenCvCameraView != null)
            mOpenCvCameraView.disableView();
    }

    @Override
    public void onResume()
    {
        super.onResume();
        OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_2_4_3, this, mLoaderCallback);
    }

    public void onDestroy() {
        super.onDestroy();
        if (mOpenCvCameraView != null)
            mOpenCvCameraView.disableView();
    }

    public void onCameraViewStarted(int width, int height) {
    	mIntermediateMat = new Mat();
    }

    public void onCameraViewStopped() {
        // Explicitly deallocate Mats
        if (mIntermediateMat != null)
            mIntermediateMat.release();

        mIntermediateMat = null;
    }
    
    public Mat onCameraFrame(CvCameraViewFrame inputFrame) {
		
        rgba = inputFrame.rgba();
        /*
        org.opencv.core.Size sizeRgba = rgba.size();                
        int rows = (int) sizeRgba.height;
        int cols = (int) sizeRgba.width;
        int left = cols / 8;
        int top = rows / 8;
        int width = cols * 8/10;//3 / 4;
        int height = rows * 8/10;//3 / 4;    	
    	*/
    	rgbaInnerWindow = getInnerWindow(rgba);//rgba;//.submat(top, top + height, left, left + width);
        Imgproc.Canny(rgbaInnerWindow, mIntermediateMat, 80, 90);        
        Imgproc.cvtColor(mIntermediateMat, rgbaInnerWindow, Imgproc.COLOR_GRAY2BGRA, 4);
        rgbaInnerWindow.release();        
    	
        //return inputFrame.rgba();
        return rgba;
    }  
    
    private Mat getInnerWindow(Mat myMat)
    {        
        org.opencv.core.Size sizeRgba = rgba.size();                
        int rows = (int) sizeRgba.height;
        int cols = (int) sizeRgba.width;
        int left = cols / 8;//8;
        int top = rows / 8;//8;
        int width = cols * 3/4;//8/10;//3 / 4;
        int height = rows * 3/4;//8/10;//3 / 4;    	
    	
    	Mat tempMat = myMat.submat(top, top + height, left, left + width);
    	return tempMat;
    }

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
        List<String> effects = mOpenCvCameraView.getEffectList();

        if (effects == null) {
            Log.e(TAG, "Color effects are not supported by device!");
            return true;
        }

        mColorEffectsMenu = menu.addSubMenu("Color Effect");
        mEffectMenuItems = new MenuItem[effects.size()];

        int idx = 0;
        ListIterator<String> effectItr = effects.listIterator();
        while(effectItr.hasNext()) {
           String element = effectItr.next();
           mEffectMenuItems[idx] = mColorEffectsMenu.add(1, idx, Menu.NONE, element);
           idx++;
        }

        mResolutionMenu = menu.addSubMenu("Resolution");
        mResolutionList = mOpenCvCameraView.getResolutionList();
        mResolutionMenuItems = new MenuItem[mResolutionList.size()];

        ListIterator<Size> resolutionItr = mResolutionList.listIterator();
        idx = 0;
        while(resolutionItr.hasNext()) {
            Size element = resolutionItr.next();
            mResolutionMenuItems[idx] = mResolutionMenu.add(2, idx, Menu.NONE,
                    Integer.valueOf(element.width).toString() + "x" + Integer.valueOf(element.height).toString());
            idx++;
         }

        return true;
	}
	
    public boolean onOptionsItemSelected(MenuItem item) {
        Log.i(TAG, "called onOptionsItemSelected; selected item: " + item);
        if (item.getGroupId() == 1)
        {
            mOpenCvCameraView.setEffect((String) item.getTitle());
            Toast.makeText(this, mOpenCvCameraView.getEffect(), Toast.LENGTH_SHORT).show();
        }
        else if (item.getGroupId() == 2)
        {
            int id = item.getItemId();
            Size resolution = mResolutionList.get(id);
            mOpenCvCameraView.setResolution(resolution);
            resolution = mOpenCvCameraView.getResolution();
            String caption = Integer.valueOf(resolution.width).toString() + "x" + Integer.valueOf(resolution.height).toString();
            Toast.makeText(this, caption, Toast.LENGTH_SHORT).show();
        }

        return true;
    }

    @SuppressLint("SimpleDateFormat")
    @Override
    public boolean onTouch(View v, MotionEvent event) {
        Log.i(TAG,"onTouch event");
        
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss");
        String currentDateandTime = sdf.format(new Date());
        String fileName = Environment.getExternalStorageDirectory().getPath() +
                               "/myColorBook_" + currentDateandTime + ".jpg";
        //mOpenCvCameraView.takePicture(fileName);
        
        //Write the bitmap to external storage. 
        
        //File path = Environment.getExternalStoragePublicDirectory( 
          //                            Environment.DIRECTORY_PICTURES); 
        //File file = new File(path, "CannyPicture_" + new 
        //SimpleDateFormat ("yyyyMMddHHmmss").format(new Date()) + ".jpg"); 
        //SaveImage(file);
        Highgui.imwrite(fileName,getInnerWindow(rgba));
        Toast.makeText(this, fileName + " saved", Toast.LENGTH_SHORT).show();
        intentFileName = new Intent();
        intentFileName.putExtra(MainActivity.FILENAME, fileName);
        setResult(RESULT_OK,intentFileName);
        return false;
    }
    
    private void SaveImage(File myFile)
    {
    	org.opencv.core.Size sizeRgba = rgba.size(); 
    	Bitmap myBitmap =Bitmap.createBitmap((int)sizeRgba.width,(int)sizeRgba.height,Bitmap.Config.ARGB_8888);
    	Utils.matToBitmap(rgba, myBitmap);
    	try
    	{
    		FileOutputStream fOut= new FileOutputStream(myFile);
    		myBitmap.compress(Bitmap.CompressFormat.JPEG, 100, fOut);
    		fOut.flush();
    		fOut.close();
    		Toast.makeText(this, myFile.toString()+" saved", Toast.LENGTH_SHORT).show();
    	} catch(IOException e)
    	{
    		Log.w(TAG, "Error writing");
    	}
    	
    }
}
