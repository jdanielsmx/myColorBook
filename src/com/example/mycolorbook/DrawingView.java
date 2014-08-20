package com.example.mycolorbook;


import java.io.File;

import org.opencv.android.Utils;
import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.Point;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.highgui.Highgui;
import org.opencv.imgproc.Imgproc;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.MotionEvent;
import android.view.View;

/**
 * This is demo code to accompany the Mobiletuts+ tutorial series:
 * - Android SDK: Create a Drawing App
 * 
 * Sue Smith
 * August 2013
 *
 */
public class DrawingView extends View {

	//drawing path
	private Path drawPath;
	//drawing and canvas paint
	private Paint drawPaint, canvasPaint;
	//initial color
	private int paintColor = 0xFF660000;
	//canvas
	private Canvas drawCanvas;
	//canvas bitmap
	private Bitmap canvasBitmap;
	//brush sizes
	private float brushSize, lastBrushSize;
	//erase flag
	private boolean erase=false;
	//image passed as canvas
	private String strFilename;
	//bitmap created from filename
	private Bitmap bm;
	//flag to indicate flood-fill
	private boolean floodfill=false;
	private Mat floodMat;
	private Mat maskMat;
	private Point startPoint;
	private Scalar myScalar;

	public DrawingView(Context context, AttributeSet attrs){
		super(context, attrs);
		setupDrawing();
	}

	//setup drawing
	private void setupDrawing(){

		//prepare for drawing and setup paint stroke properties
		brushSize = getResources().getInteger(R.integer.medium_size);
		lastBrushSize = brushSize;
		drawPath = new Path();
		drawPaint = new Paint();
		drawPaint.setColor(paintColor);
		drawPaint.setAntiAlias(true);
		drawPaint.setStrokeWidth(brushSize);
		drawPaint.setStyle(Paint.Style.STROKE);
		drawPaint.setStrokeJoin(Paint.Join.ROUND);
		drawPaint.setStrokeCap(Paint.Cap.ROUND);
		canvasPaint = new Paint(Paint.DITHER_FLAG);
	}

	//size assigned to view
	@Override
	protected void onSizeChanged(int w, int h, int oldw, int oldh) {
		super.onSizeChanged(w, h, oldw, oldh);
		if (bm!= null)
			canvasBitmap = bm;//Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
		else
			canvasBitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
		drawCanvas = new Canvas(canvasBitmap);
	}

	//draw the view - will be called after touch event
	@Override
	protected void onDraw(Canvas canvas) {
		if (!floodfill)
		{
			canvas.drawBitmap(canvasBitmap, 0, 0, canvasPaint);
			canvas.drawPath(drawPath, drawPaint);
		}
		else
		{						
			
			Imgproc.floodFill(floodMat, maskMat, startPoint, null);
			//Imgproc.floodFill(image, mask, seedPoint, newVal, rect, loDiff, upDiff, flags);
		}
	}

	//register user touches as drawing action
	@Override
	public boolean onTouchEvent(MotionEvent event) {
		float touchX = event.getX();
		float touchY = event.getY();
		//respond to down, move and up events
		switch (event.getAction()) {
		case MotionEvent.ACTION_DOWN:
			drawPath.moveTo(touchX, touchY);
			break;
		case MotionEvent.ACTION_MOVE:
			drawPath.lineTo(touchX, touchY);
			break;
		case MotionEvent.ACTION_UP:
			drawPath.lineTo(touchX, touchY);
			drawCanvas.drawPath(drawPath, drawPaint);
			drawPath.reset();
			break;
		default:
			return false;
		}
		if (floodfill)
		{
			floodMat = new Mat();			
			startPoint = new Point();
			startPoint.x=(double)touchX;
			startPoint.y=(double)touchY;
			Utils.bitmapToMat(canvasBitmap, floodMat);
			maskMat = Mat.zeros(floodMat.rows()+2, floodMat.cols()+2, 255);
		}
		//redraw
		invalidate();
		return true;

	}
	
	public void setFilename(String strFile)
	{
		strFilename = strFile;
		
		File file = new File(strFilename);	   	    
	    if(file.exists())
	    {

	    	Mat m = Equalize(Highgui.imread(file.getAbsolutePath()));//Equalize();
	    	Mat mCannyMat = new Mat();
	    	Imgproc.Canny(getInnerWindow(m), mCannyMat, 80, 90);
	    	//Mat mGaussianMat = new Mat();
	    	//Imgproc.GaussianBlur(mCannyMat, mGaussianMat, new Size(9,9), 0);
	    	//Mat mGray2BGRAMat = new Mat();
	    	//Imgproc.cvtColor(mCannyMat, mGray2BGRAMat, Imgproc.COLOR_GRAY2BGRA, 4);
	    	
	    	
	    	//close edges
	    	Mat mErodeMat = new Mat();
	    	Mat mDilateMat = new Mat();
	    		    	
	    	Imgproc.dilate(mCannyMat, mDilateMat, Imgproc.getStructuringElement(Imgproc.MORPH_DILATE, new Size(5, 5)));
	    	Imgproc.erode(mDilateMat, mErodeMat, Imgproc.getStructuringElement(Imgproc.MORPH_CROSS, new Size(3,3))); 	
	    	
	    	//invert the matrix to show white-background/black-edges
		    Mat invertcolormatrix= new Mat(mErodeMat.rows(),mErodeMat.cols(), mErodeMat.type(), new Scalar(255,255,255));
		    Core.subtract(invertcolormatrix, mErodeMat, mErodeMat);	    	
	    	Mat mInv = mErodeMat;//mGray2BGRAMat;
	    	bm = Bitmap.createBitmap(mInv.cols(), mInv.rows(),Bitmap.Config.ARGB_8888);
	        Utils.matToBitmap(mInv, bm);

	    	/*
	    	Mat mResult = ImproveImage(mGray2BGRAMat);
	    	bm = Bitmap.createBitmap(mResult.cols(), mResult.rows(),Bitmap.Config.ARGB_8888);
	        Utils.matToBitmap(mResult, bm);	        
	    	*/
	        Matrix matrix = new Matrix();
	        matrix.postRotate(90);
	        Bitmap rotBM= Bitmap.createBitmap(bm , 0, 0, bm.getWidth(), bm.getHeight(), matrix, true);
	        // create a new bitmap from the original using the matrix to transform the result
	        bm = Bitmap.createScaledBitmap(rotBM, 1050, 1185, false);	    
	    }
	}
	
	private Mat ImproveImage(Mat mInput)
	{
		Mat mIntermediateMat = new Mat();
		// 1) Apply gaussian blur to remove noise
		Imgproc.GaussianBlur(mInput, mIntermediateMat, new Size(11,11), 0);

		// 2) AdaptiveThreshold -> classify as either black or white
		Imgproc.adaptiveThreshold(mIntermediateMat, mIntermediateMat, 255, Imgproc.ADAPTIVE_THRESH_MEAN_C, Imgproc.THRESH_BINARY, 5, 2);

		// 3) Invert the image -> so most of the image is black
		Core.bitwise_not(mIntermediateMat, mIntermediateMat);

		// 4) Dilate -> fill the image using the MORPH_DILATE
		Mat kernel = Imgproc.getStructuringElement(Imgproc.MORPH_DILATE, new Size(3,3), new Point(1,1));
		Imgproc.dilate(mIntermediateMat, mIntermediateMat, kernel);
		
		return mIntermediateMat;
	}
	
	private Mat Equalize(Mat myMat)
	{
		Mat grayMat = new Mat();
		Mat bwMat = new Mat();
		Imgproc.cvtColor(myMat, grayMat, Imgproc.COLOR_RGB2GRAY);
		Imgproc.equalizeHist(grayMat, grayMat);
		Imgproc.threshold(grayMat, bwMat, 127.5, 255.0, Imgproc.THRESH_OTSU);
		return bwMat;
	}
	
    private Mat getInnerWindow(Mat myMat)
    {                    
        int rows = myMat.rows();
        int cols = myMat.cols();
        int left = cols / 8;
        int top = rows / 8;
        int width = cols * 3/4;
        int height = rows * 3/4;    	    	
    	Mat tempMat = myMat.submat(top, top + height, left, left + width);
    	return tempMat;
    }

	//update color
	public void setColor(String newColor){
		invalidate();
		paintColor = Color.parseColor(newColor);
		drawPaint.setColor(paintColor);
	}

	//set brush size
	public void setBrushSize(float newSize){
		float pixelAmount = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 
				newSize, getResources().getDisplayMetrics());
		brushSize=pixelAmount;
		drawPaint.setStrokeWidth(brushSize);
	}

	//get and set last brush size
	public void setLastBrushSize(float lastSize){
		lastBrushSize=lastSize;
	}
	public float getLastBrushSize(){
		return lastBrushSize;
	}

	//set erase true or false
	public void setErase(boolean isErase){
		erase=isErase;
		if(erase) drawPaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.CLEAR));
		else drawPaint.setXfermode(null);
	}
	
	public void setFill(boolean isFill){
		floodfill=isFill;
	}

	//start new drawing
	public void startNew(){
		drawCanvas.drawColor(0, PorterDuff.Mode.CLEAR);
		invalidate();
	}
}
