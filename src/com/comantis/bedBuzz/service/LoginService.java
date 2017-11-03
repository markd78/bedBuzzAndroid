package com.comantis.bedBuzz.service;

import java.io.IOException;
import java.io.InputStream;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.apache.commons.io.IOUtils;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.util.Log;

import com.comantis.bedBuzz.models.UserModel;
import com.comantis.bedBuzz.utils.Utilities;

public class LoginService  extends AsyncTask<Integer, Integer, Void> {
	
	public static final String LOGIN_COMPLETE = "com.comantis.bedBuzz.loginCompleted";
	
	 String url = "";
	 long bedBuzzID = -1;
	 Long facebookID ;
	 Context context;
	 
	public LoginService(long bedBuzzID, Long facebookID, Context context)
	{
	    url =  Utilities.getConnection() + "userService/LogonUser?bedBuzzID="+bedBuzzID +"&fbID="+facebookID.toString();
	    this.bedBuzzID = bedBuzzID;
	    this.facebookID = facebookID;	
	    this.context = context;
	    UserModel.getUserModel().isLoggingOn = true;
	}

	@Override
    protected Void doInBackground(Integer... integers) {
		
		 InputStream source = retrieveStream(url);
		 
		  Log.w(getClass().getSimpleName(), "Calling " + url);
		 
         String jsonStr = null;
         
         if (source == null)
         {
        	 // must be offline
        	 return null;
         }
		try {
			jsonStr = IOUtils.toString(source, "UTF-8");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		 catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		 }
         
         try {
			parseJSON(jsonStr);
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		 catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			 }

    return null;
}
	
	  private void parseJSON(String jsonStr) throws JSONException
	    {
		  Log.w(getClass().getSimpleName(), "Parsing " + jsonStr);
		  
	    	JSONObject jObject = new JSONObject(jsonStr); 
	    	
	    	long bedBuzzID = jObject.getLong("bedBuzzID");

	    	UserModel um = UserModel.getUserModel();
	    	
	    	String dateStr = "";
	    	Boolean isPaidUser = false;
	    	
	    	try {
	    	isPaidUser = jObject.getBoolean("isPaidAndroidSubscriptionUser");
	    	dateStr = jObject.getString("isAndroidPaidSubscriberThrough");
	    	}
	    	catch (JSONException e) {  

	            e.printStackTrace(); 
	        } 
	    	
	    	SimpleDateFormat dateFormat = new SimpleDateFormat("MMM dd, yyyy h:mm:ss aaa");
	    	try {
				um.userSettings.subscriberUntilDate = (Date)dateFormat.parse(dateStr);
			} catch (ParseException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} 
	    	
	    	um.userSettings.bedBuzzID = bedBuzzID;
	    	um.userSettings.isPaidUser = isPaidUser;
	    	um.saveUserSettings(context);
	    }
	  

	  private InputStream retrieveStream(String url) {
	          
	          DefaultHttpClient client = Utilities.getDefaultHTTPClient(); 
	          
	          HttpGet getRequest = new HttpGet(url);
	            
	          try {
	             
	             HttpResponse getResponse = client.execute(getRequest);
	             final int statusCode = getResponse.getStatusLine().getStatusCode();
	             
	             if (statusCode != HttpStatus.SC_OK) { 
	                Log.w(getClass().getSimpleName(), 
	                    "Error " + statusCode + " for URL " + url); 
	                
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
	      
	      protected void onPostExecute(Void result)    {
	          // complete
	    	  UserModel.getUserModel().isLoggedOn = true;
	      	Intent i = new Intent(LOGIN_COMPLETE);
	      	i.putExtra("bedBuzzID", UserModel.getUserModel().userSettings.bedBuzzID);
	          context.sendBroadcast(i);
	          
	          UserModel.getUserModel().isLoggingOn = false;
	      }
	

}
