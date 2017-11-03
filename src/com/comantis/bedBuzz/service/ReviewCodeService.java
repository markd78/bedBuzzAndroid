package com.comantis.bedBuzz.service;

import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.IOUtils;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;

import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.widget.Toast;

import com.comantis.bedBuzz.models.UserModel;
import com.comantis.bedBuzz.utils.Utilities;

public class ReviewCodeService  extends AsyncTask<Integer, Integer, Void> {

	 public static final String REVIEW_CODE_SUCCESS_OR_FAIL = "com.comantis.bedBuzz.reviewCodeAccepted";
	
	 private String url;
	 private Long userID;
	 private Context context;
	 private String reviewCode;

	private boolean success = false;
	
	public ReviewCodeService(Long userID, String reviewCode, Context context)
	{
		 url =  Utilities.getConnection() + "reviewCode/UpdateReviewCode";
		this.userID = userID;
		this.context = context;
		this.reviewCode = reviewCode;
	}

	@Override
	protected Void doInBackground(Integer... integers) {
		
		// Create a new HttpClient and Post Header
	    HttpClient httpclient = Utilities.getDefaultHTTPClient();
	    HttpPost httppost = new HttpPost(url);

	        // Add your data
	        List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(2);
	        nameValuePairs.add(new BasicNameValuePair("reviewCode",reviewCode));
	        nameValuePairs.add(new BasicNameValuePair("userID", userID.toString()));
	        try {
				httppost.setEntity(new UrlEncodedFormEntity(nameValuePairs));
			} catch (UnsupportedEncodingException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

	        // Execute HTTP Post Request
	        HttpResponse response = null;
			try {
				response = httpclient.execute(httppost);
			} catch (ClientProtocolException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
			
			if (response!=null)
			{
				InputStream inputStream = null;
				try {
					inputStream = response.getEntity().getContent();
				} catch (IllegalStateException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				
				 String responseStr = null;
				try {
					responseStr = IOUtils.toString(inputStream, "UTF-8");
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				
				if (responseStr.equals("true"))
				{
					UserModel um = UserModel.getUserModel();
					um.userSettings.isPaidUser = true;
					um.saveUserSettings(context);
					
					this.success = true;
				}
				else
				{
					
					
					this.success= false;
				}
			}
			
			 return null;
	}
	
	 protected void onPostExecute(Void result)    {
	        // complete
	    	Intent i = new Intent(REVIEW_CODE_SUCCESS_OR_FAIL);
	        i.putExtra("success",  this.success);
	        context.sendBroadcast(i);
	    }
	
}
