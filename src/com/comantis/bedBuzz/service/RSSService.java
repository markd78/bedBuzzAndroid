package com.comantis.bedBuzz.service;

import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;

import org.apache.commons.io.IOUtils;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.util.Base64;
import android.util.Log;
import android.widget.Toast;

import com.comantis.bedBuzz.VO.RSSClip;
import com.comantis.bedBuzz.enums.VoiceType;
import com.comantis.bedBuzz.models.RSSModel;
import com.comantis.bedBuzz.utils.Utilities;

public class RSSService  extends AsyncTask<Integer, Integer, Void> {

	public static final String RSS_DOWNLOAD_COMPLETE = "com.comantis.bedBuzz.rssDownloadCompleted";
	
	private String url;
	private String rssURL;
	private Context context;
	private String voiceName;
	
	public RSSService(String rssURL, String voiceName, Context context)
	{
		 this.rssURL = rssURL;
	    this.context = context;
		
		String encodedURL = null;
		try {
			encodedURL = URLEncoder.encode(rssURL, "utf-8");
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		 url =  Utilities.getConnection() + "rssFeed/GetRSSClips?rssURL="+encodedURL +"&voiceName="+voiceName;
	}
	
	 private InputStream retrieveStream(String url) {
         
         DefaultHttpClient client = Utilities.getDefaultHTTPClient(); 
         
         HttpGet getRequest = new HttpGet(url);
           
         try {
            
            HttpResponse getResponse = client.execute(getRequest);
            final int statusCode = getResponse.getStatusLine().getStatusCode();
            
            if (statusCode != HttpStatus.SC_OK) { 
            	RSSModel rssModel = RSSModel.getRSSModell();
    	    	
    	    	rssModel.rssClips = new ArrayList<RSSClip>();
               Log.w(getClass().getSimpleName(), 
                   "Error " + statusCode + " for URL " + url); 
               Toast.makeText(context, "Could not parse RSS feed.  Please check the url and try again", Toast.LENGTH_SHORT);
               return null;
            }

            HttpEntity getResponseEntity = getResponse.getEntity();
            return getResponseEntity.getContent();
            
         } 
         catch (IOException e) {
            getRequest.abort();
            Log.w(getClass().getSimpleName(), "Error for URL " + url, e);
         }
         
         return null;
         
      }
	
	@Override
    protected Void doInBackground(Integer... integers) {
		
		 InputStream source = retrieveStream(url);
         
         String jsonStr = null;
		try {
			jsonStr = IOUtils.toString(source, "UTF-8");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
         
         try {
			parseJSON(jsonStr);
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	

    return null;
}
	
	  private void parseJSON(String jsonStr) throws Exception
	    {
		    
		    if (jsonStr.indexOf("<title>Status page</title>")!=-1)
		    {
		    	throw new Exception("Not RSS Error");
		    }
		  
	    	JSONArray jArray = new JSONArray(jsonStr);
	    	
	    	RSSModel rssModel = RSSModel.getRSSModell();
	    	
	    	
	    	
	    	for (int i = 0; i < jArray.length(); ++i) {
	    		JSONObject rssClipJSONObj = (JSONObject) jArray.get(i);
	    		RSSClip rssClip = new RSSClip();
	    		String base64Sound = rssClipJSONObj.getString("base64OfSound");
	    		
	    		rssClip.headline = rssClipJSONObj.getJSONObject("title").getString("value");
	    		rssClip.link = rssClipJSONObj.getJSONObject("link").getString("value");
	    		
	    		rssClip.sound  = Base64.decode(base64Sound, Base64.DEFAULT);
	    		
	    		rssModel.rssClips.add(rssClip);
	    	}
	    }
	  
	  protected void onPostExecute(Void result)    {
          // complete
      	Intent i = new Intent(RSS_DOWNLOAD_COMPLETE);
          context.sendBroadcast(i);
      }
}
