package com.comantis.bedBuzz.service;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Picture;
import android.graphics.Point;
import android.os.AsyncTask;
import android.util.Log;
import android.view.Display;
import android.view.WindowManager;
import android.webkit.WebView;
import android.webkit.WebView.PictureListener;
import android.webkit.WebViewClient;

import com.comantis.bedBuzz.VO.RSSClip;
import com.comantis.bedBuzz.models.RSSModel;

public class CreateScreenshot extends AsyncTask<Integer, Integer, Void>{
   
    public static final String SCREENSHOT_CREATED = "com.comantis.bedBuzz.createScreenshot";
    
	SharedPreferences settings;
    SharedPreferences.Editor editor;
    String fileName;
    String url;
    Context context;
    StringBuffer buffer;
    String soundFilename;
    int imageHeight= 0 ;
    int imageWidth= 0 ;
    
    public  CreateScreenshot(String fileName, String soundFilename, String url, Context context){
        this.url = url;
        this.fileName = fileName;
        this.context = context;
        this.soundFilename = soundFilename;
        int currentapiVersion = android.os.Build.VERSION.SDK_INT;
        
        WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        if (currentapiVersion < 13)
        {
        	Display display = wm.getDefaultDisplay(); 
        	
        	imageHeight = display.getHeight(); 
        	imageWidth = display.getWidth();  
        	
        }
        else
        {
	        Display display = wm.getDefaultDisplay();
	        Point size = new Point();
	        display.getSize(size);
	        imageWidth = size.x;
	        imageHeight  = size.y;  
        }
    }

    @Override
    protected Void doInBackground(Integer... integers) {
       
    	getURLScreenshot(url,context);
    	
        return null;
    }
    protected void onPostExecute(Void result)    {
    	
    	getBitmap(new WebView(context), imageWidth,imageHeight,url,buffer.toString(), this.fileName);
        // complete
    	Log.d("SCREENSHOT_CREATED", "SCREENSHOT_CREATED : "+ this.fileName);
    	Intent i = new Intent(SCREENSHOT_CREATED);
        i.putExtra("filename",  this.fileName);
        context.sendBroadcast(i);
        
        for (RSSClip clip:RSSModel.getRSSModell().rssClips)
        {
        	if (clip.link.equals(url))
        	{
        		clip.imageFilename = fileName;
        		 break;
        	}
        }
       
    }
    
    public void getBitmap(final WebView w, int containerWidth, int containerHeight, final String baseURL, final String content, final String filename) {
		final Bitmap b = Bitmap.createBitmap(containerWidth, containerHeight, Bitmap.Config.ARGB_8888);
		w.setWebViewClient(new WebViewClient() {
			@Override
			public void onPageFinished(WebView view, String url) {
				//ready.set(true);
			}
		});
		w.setPictureListener(new PictureListener() {
			@Override
			public void onNewPicture(WebView view, Picture picture) {
				
					final Canvas c = new Canvas(b);
					view.draw(c);
					w.setPictureListener(null);

					FileOutputStream out = null;
					try {
						out = new FileOutputStream(filename);
					} catch (FileNotFoundException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					b.compress(Bitmap.CompressFormat.PNG, 70, out); 

					try {
						out.close();
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			
		});
		w.layout(0, 0, imageWidth*2, imageHeight*2);
		w.loadDataWithBaseURL(baseURL, content, "text/html", "UTF-8", null);



		

	}

	public void getURLScreenshot(String url, Context context)
	{
		URL u = null;
		try {
			u = new URL(url);
		} catch (MalformedURLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		URLConnection conn = null;
		try {
			conn = u.openConnection();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		BufferedReader in = null;
		try {
			in = new BufferedReader(
					new InputStreamReader(
							conn.getInputStream()));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		buffer = new StringBuffer();
		String inputLine;
		try {
			while ((inputLine = in.readLine()) != null) 
				buffer.append(inputLine);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		try {
			in.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		
	}
}
