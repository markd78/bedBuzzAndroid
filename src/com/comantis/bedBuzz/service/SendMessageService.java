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

import android.content.Context;
import android.os.AsyncTask;

public class SendMessageService extends AsyncTask<Integer, Integer, Void> {

	String url;
	MessageVO message;
	
	public SendMessageService(MessageVO message)
	{
		 url =  Utilities.getConnection() + "messages/AddMessageWithFormat";
		this.message = message;
	}

	@Override
	protected Void doInBackground(Integer... integers) {
		
		String senderIDStr = message.senderID.toString();
		
		String userArrStr = "[";
		
		// CONVERT target array into string
		for (int i=0; i<message.targets.size(); i++)
		{
			userArrStr += message.targets.get(i).toString();
			if (i + 1 != message.targets.size())
			{
				userArrStr = userArrStr + ",";
			}
			
		}
		
		userArrStr = userArrStr + "]";
		
		
		// Create a new HttpClient and Post Header
	    HttpClient httpclient = Utilities.getDefaultHTTPClient();
	    HttpPost httppost = new HttpPost(url);

	        // Add your data
	        List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(2);
	        nameValuePairs.add(new BasicNameValuePair("receiverIDs", userArrStr));
	        nameValuePairs.add(new BasicNameValuePair("senderID", senderIDStr));
	        nameValuePairs.add(new BasicNameValuePair("voiceName", message.voiceName));
	        nameValuePairs.add(new BasicNameValuePair("voiceText", message.messageText));
	        
	        if (message.base64Str !=null)
	        {
	        	nameValuePairs.add(new BasicNameValuePair("soundBase64String", message.base64Str));
	        	nameValuePairs.add(new BasicNameValuePair("format", "aac"));
	        }
	        else
	        {
	        	nameValuePairs.add(new BasicNameValuePair("format", "text"));
	        }
	        
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
