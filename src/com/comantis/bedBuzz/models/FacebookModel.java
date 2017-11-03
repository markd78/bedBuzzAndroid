package com.comantis.bedBuzz.models;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import com.comantis.bedBuzz.VO.FriendVO;
import com.comantis.bedBuzz.service.LoginService;
import com.comantis.bedBuzz.utils.BaseDialogListener;
import com.comantis.bedBuzz.utils.BaseRequestListener;
import com.facebook.android.AsyncFacebookRunner;
import com.facebook.android.DialogError;
import com.facebook.android.Facebook;
import com.facebook.android.Facebook.DialogListener;
import com.facebook.android.FacebookError;
import com.wilson.android.library.DrawableManager;


public class FacebookModel {

	Context applicationContext;

	private static FacebookModel facebookModel = null; 
	public AsyncFacebookRunner mAsyncRunner;

	public Facebook facebook = new Facebook("144317305656742");
	String FILENAME = "AndroidSSO_data";
	private SharedPreferences mPrefs;
	public List<FriendVO> friends;

	public List<FriendVO> targetFriends;

	private boolean friendsBeingFetched = false;

	public static FacebookModel getFacebookModel() { 
		if (facebookModel == null) { 
			facebookModel = new FacebookModel(); 

		} 

		// initialize

		return facebookModel; 
	}

	public void getDetails()
	{
		getFriends();
	}

	// return true if already authorized
	public boolean authorize(Activity activity)
	{
		this.applicationContext = activity.getApplicationContext();
		mAsyncRunner = new AsyncFacebookRunner(facebook);

		/*
		 * Get existing access_token if any
		 */
		mPrefs= UserModel.getUserModel().getPreferences(activity);
		String access_token = mPrefs.getString("access_token", null);
		long expires = mPrefs.getLong("access_expires", 0);
		if(access_token != null) {
			facebook.setAccessToken(access_token);
		}
		if(expires != 0) {
			facebook.setAccessExpires(expires);
		}

		/*
		 * Only call authorize if the access_token has expired.
		 */
		if(facebook.isSessionValid()) {
			getMyFacebookID();
			getDetails();

			return true;
		}
		else
		{
			facebook.authorize(activity, new DialogListener() {
				@Override
				public void onComplete(Bundle values) {
					SharedPreferences.Editor editor = mPrefs.edit();
					editor.putString("access_token", facebook.getAccessToken());
					editor.putLong("access_expires", facebook.getAccessExpires());
					editor.commit();

					getMyFacebookID();

					Log.w(getClass().getSimpleName(), "**** fetching friends");

					// get the user's friends
					getDetails();



				}

				@Override
				public void onFacebookError(FacebookError error) 
				{
					Log.w(getClass().getSimpleName(), "**** "+error.getMessage());
				}

				@Override
				public void onError(DialogError e) 
				{
					Log.w(getClass().getSimpleName(), "**** "+e.getMessage());
				}

				@Override
				public void onCancel() 
				{
					Log.w(getClass().getSimpleName(), "**** Cancelled");
				}
			});
		}

		return false;
	}

	private boolean facebookIDFetching = false;
	
	public void getMyFacebookID()
	{
		UserModel um = UserModel.getUserModel();
		
		
		if (um.userSettings.facebookID.longValue()==-1L && !facebookIDFetching)
		{
			facebookIDFetching = true;
			Log.w(getClass().getSimpleName(), "**** Getting facebook ID");
			mAsyncRunner.request("me",new MeRequestListener());
		}
	}

	public class MeRequestListener implements
	com.facebook.android.AsyncFacebookRunner.RequestListener {

		


		@Override
		public void onComplete(String response, Object state) {
			facebookIDFetching = false;
			Log.d("Facebook-Example", "Got response: " + response);
			String jsonUser =response;

			JSONObject obj = null;
			try {
				obj = new JSONObject(jsonUser);
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			try 
			{
				UserModel um = UserModel.getUserModel();
				if (um.userSettings.facebookID==-1)
				{
					// should relogon with the users new facebook id
					um.userSettings.facebookID = Long.parseLong(obj.optString("id"));
					if (!um.isLoggingOn)
					{
						new LoginService(um.userSettings.bedBuzzID, um.userSettings.facebookID, applicationContext ).execute();
					}
				}
				
				UserModel.getUserModel().saveUserSettings(applicationContext);
				
				
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

		@Override
		public void onIOException(IOException e, Object state) {
			// TODO Auto-generated method stub

		}

		@Override
		public void onMalformedURLException(MalformedURLException e,
				Object state) {
			// TODO Auto-generated method stub

		}

		@Override
		public void onFacebookError(FacebookError e, Object state) {
			// TODO Auto-generated method stub

		}


		@Override
		public void onFileNotFoundException(FileNotFoundException e,
				Object state) {
			// TODO Auto-generated method stub
			
		}

	}

	public void sendAppRequest(Activity activity, String toUsersStr) 
	{	
		if (toUsersStr == null)
		{
			toUsersStr = "";
		}

		this.applicationContext = activity.getApplicationContext();
		Bundle params = new Bundle();
		params.putString("message", "I sent you a wake up message using BedBuzz!  You can hear it when your alarm goes off.");
		params.putString("to", toUsersStr);
		facebook.dialog(activity, "apprequests", params,new AppRequestsListener());
	}

	public void getFriends()
	{
		

		if (friends!=null || friendsBeingFetched == true)
		{
			return;
		}
		
		friendsBeingFetched = true;
		Log.w(getClass().getSimpleName(), "**** Getting facebookfriends");
		
		// get information about the currently logged in user
		String query = "SELECT uid, name,  pic_square, is_app_user FROM user WHERE uid IN (SELECT uid2 FROM friend WHERE uid1 = me()) ORDER BY last_name";
		Bundle params = new Bundle();
		params.putString("method", "fql.query");
		params.putString("query", query);
		mAsyncRunner.request(null, params,
				new FriendsRequestListener());
	}


	public static final String FRIENDS_FETCHED = "FRIENDS_FETCHED";
	/*
	 * callback after friends are fetched via me/friends or fql query.
	 */
	public class FriendsRequestListener extends BaseRequestListener {


		@Override
		public void onComplete(final String response, final Object state) {
			try {

				Log.w(getClass().getSimpleName(), "**** Getting facebookfriends - response:" +response);

				JSONArray jsonArray = new JSONArray(response);
				DrawableManager dm = new DrawableManager();

				friends = new ArrayList<FriendVO>();
				friendsBeingFetched = false;
				
				for(int i = 0; i < jsonArray.length(); i++)
				{
					JSONObject obj = (JSONObject) jsonArray.get(i);
					FriendVO friend = new FriendVO();
					friend.name = obj.getString("name");
					friend.uid = obj.getLong("uid");
					friend.isBedBuzzUser = obj.getBoolean("is_app_user");
					friend.picSmallURL = obj.getString("pic_square");

					friends.add(friend);
				}
				// broadcast intent
				// complete
				Intent i = new Intent(FRIENDS_FETCHED);
				applicationContext.sendBroadcast(i);

			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

		@Override
		public void onFacebookError(FacebookError e, Object state) {
			Toast.makeText(applicationContext, "Facebook Error: " + e.getMessage(),
					Toast.LENGTH_SHORT).show();

		}
	}

	/*
	 * callback for the apprequests dialog which sends an app request to user's
	 * friends.
	 */
	public class AppRequestsListener extends BaseDialogListener {
		@Override
		public void onComplete(Bundle values) {
			//Toast toast = Toast.makeText(applicationContext, "App request sent",
			//		Toast.LENGTH_SHORT);
			//toast.show();
		}

		@Override
		public void onFacebookError(FacebookError error) {
			Toast.makeText(applicationContext, "Facebook Error: " + error.getMessage(),
					Toast.LENGTH_SHORT).show();
		}

		@Override
		public void onCancel() {
			//Toast toast = Toast.makeText(applicationContext, "App request cancelled",
			//		Toast.LENGTH_SHORT);
			//toast.show();
		}
	}

	public FriendVO getFriend(Long fbID) {
		for (FriendVO friend : this.friends)
		{
			if (friend.uid == fbID.longValue())
			{
				return friend;
			}
		}
		
		return null;
	}

}
