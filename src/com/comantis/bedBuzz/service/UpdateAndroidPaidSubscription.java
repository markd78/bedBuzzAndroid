package com.comantis.bedBuzz.service;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;

import android.content.Context;
import android.os.AsyncTask;

import com.comantis.bedBuzz.models.UserModel;
import com.comantis.bedBuzz.utils.Utilities;

public class UpdateAndroidPaidSubscription extends AsyncTask<Integer, Integer, Void>  {

	private Long userID;
	private Integer numberOfMonths;
	private Context context;
	private String url;
	
	public UpdateAndroidPaidSubscription(Long userID, Integer numberOfMonths,
			Context context) {
		 url =  Utilities.getConnection() + "subscribe/UpdateAndroidPaidSubscription";
		this.userID = userID;
		this.numberOfMonths = numberOfMonths;
		this.context = context;
	


	}
	
	@Override
	protected Void doInBackground(Integer... integers) {
	// Create a new HttpClient and Post Header
	    HttpClient httpclient = Utilities.getDefaultHTTPClient();
	    HttpPost httppost = new HttpPost(url);

	        // Add your data
	        List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(2);
	        nameValuePairs.add(new BasicNameValuePair("numberOfMonths", numberOfMonths.toString()));
	        nameValuePairs.add(new BasicNameValuePair("bedBuzzUserID", userID.toString()));
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
				String responseStr = response.toString();
				
				UserModel um = UserModel.getUserModel();
				um.userSettings.isPaidUser = true;
				um.saveUserSettings(context);
			}
			
			return null;
	}

	
}
