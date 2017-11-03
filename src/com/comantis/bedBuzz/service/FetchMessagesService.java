package com.comantis.bedBuzz.service;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
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
import android.util.Log;

import com.comantis.bedBuzz.VO.MessageVO;
import com.comantis.bedBuzz.models.MessagingModel;
import com.comantis.bedBuzz.models.UserModel;
import com.comantis.bedBuzz.utils.Base64;
import com.comantis.bedBuzz.utils.Base64DecoderException;
import com.comantis.bedBuzz.utils.Utilities;

public class FetchMessagesService  extends AsyncTask<Integer, Integer, Void> {

	public static final String MESSAGES_RECEIVED = "com.comantis.bedBuzz.messagesReceived";

	String url = "";
	long bedBuzzID = -1;
	Boolean isOlderThanOneDay ;
	Context context;

	public FetchMessagesService(Long userID, Boolean isOlderThanOneDay, Context context)
	{
		url = Utilities.getConnection() + "messages/GetMessages?userID="+userID+"&getMessageOverOneDayOld="+isOlderThanOneDay;
		this.bedBuzzID = userID;
		this.isOlderThanOneDay = isOlderThanOneDay;	
		this.context = context;
	}

	@Override
	protected Void doInBackground(Integer... integers) {

		InputStream source = retrieveStream(url);

		Log.w(getClass().getSimpleName(), "Calling " + url);

		String jsonStr = "";
		try {
			/*InputStream in = source;
			InputStreamReader is = new InputStreamReader(in);
			BufferedReader br = new BufferedReader(is);
			String read = br.readLine();

			while(read != null) {
			    System.out.println(jsonStr);
			    read = br.readLine();
			    jsonStr += read;
			}*/
			
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
		}


		return null;
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

	private void parseJSON(String jsonStr) throws JSONException
	{
		Log.w(getClass().getSimpleName(), "Parsing " + jsonStr);

		// we will update the messages from right here
		JSONArray messages = new JSONArray(jsonStr); 
		
		MessagingModel messagingModel = MessagingModel.getMessagingModel();
		
		messagingModel.messagesToPlay = new ArrayList<MessageVO>();
		
		for(int i = 0 ; i < messages.length(); i++){
			MessageVO message = new MessageVO();
			JSONObject messageObj = messages.getJSONObject(i);
			String base64Sound = messageObj.getString("soundBase64String");
			
			try {
			message.voiceName = messageObj.getString("voiceName");
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			try {
			message.messageText =messageObj.getString("voiceText");
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			try {
				message.sound  = Base64.decode(base64Sound);

			} catch (Base64DecoderException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			message.isRead = false;
			message.messageBodyID = messageObj.getJSONObject("key").getLong("id");
			
			message.senderID = messageObj.getLong("messageFromFBID");
			
			messagingModel.messagesToPlay.add(message);
		}
		
	    // let alarm know that messaging have been fetched
      	Intent i = new Intent(MESSAGES_RECEIVED);
          context.sendBroadcast(i);

	}
}
