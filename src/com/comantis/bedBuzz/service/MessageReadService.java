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
import org.apache.http.message.BasicNameValuePair;

import com.comantis.bedBuzz.VO.MessageVO;
import com.comantis.bedBuzz.utils.Utilities;

import android.os.AsyncTask;

public class MessageReadService extends AsyncTask<Integer, Integer, Void>  {

	MessageVO message;
	Long fbUserID;
	String url;
	
	public MessageReadService(MessageVO message, Long fbUserID)
	{
		this.message = message;
		this.fbUserID = fbUserID;
		 url =  Utilities.getConnection() + "messages/MessageRead";
	}
	
	@Override
	protected Void doInBackground(Integer... integers) {
		
	
		
		// Create a new HttpClient and Post Header
	    HttpClient httpclient = Utilities.getDefaultHTTPClient();
	    HttpPost httppost = new HttpPost(url);

	   // Add your data
	   List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(2);
	   nameValuePairs.add(new BasicNameValuePair("messageBodyID", message.messageBodyID.toString()));
	   nameValuePairs.add(new BasicNameValuePair("userID", fbUserID.toString()));
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
		}
		
		return null;
	}
	
}
