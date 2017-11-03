package com.comantis.bedBuzz.models;

import java.util.Calendar;
import java.util.Date;

import android.content.Context;
import android.content.SharedPreferences;

import com.comantis.bedBuzz.VO.UserSettingsVO;
import com.comantis.bedBuzz.utils.ObjectSerializer;

public class UserModel {
	private static UserModel userModel = null; 
	public static UserModel getUserModel() { 
	  if (userModel == null) { 
		  userModel = new UserModel(); 
		  
		 
	  } 
	  
	  // initialize
	  
	  return userModel; 
	}
	
	public UserSettingsVO userSettings;
	
	public Date dateUserLastLoggedOn ;
	
	public Boolean isInitalized = false;
	
	public Boolean isSnoozing = false;

	public Boolean isAlarmGoingOffMode = false;

	public boolean isLoggedOn = false;

	public boolean isLoggingOn;

	public boolean justCameFromAlarm = false;

	public boolean isBatteryIconDismissed = false;
	
	public SharedPreferences getPreferences(Context context)
	{
		return context.getSharedPreferences("sharedPrefs", 0);
	      
	}
	
	public void saveUserSettings(Context context)
	{
			SharedPreferences settings = getPreferences(context);
			
		      SharedPreferences.Editor editor = settings.edit();
		      try {
				editor.putString("settings", ObjectSerializer.serialize(userSettings));
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			editor.commit();
	}
	
	public UserSettingsVO getSettings(Context applicationContext)
	{
		userSettings = new UserSettingsVO();

        //      load tasks from preference
            SharedPreferences settings = getPreferences(applicationContext);

            try {
            	userSettings = (UserSettingsVO) ObjectSerializer.deserialize(settings.getString("settings", ObjectSerializer.serialize(new UserSettingsVO())));
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
        
			if (userSettings.voiceName == null)
			{
				userSettings.voiceName = "english.ukfemale";
			}
			
			if (userSettings.is24HrMode == null)
			{
				userSettings.is24HrMode = false;
			}
			
			if (userSettings.bedBuzzID == 0)
			{
				userSettings.bedBuzzID = -1;
			}
			
			if (userSettings.facebookID == null)
			{
				userSettings.facebookID = new Long(-1);
			}
			
			if (userSettings.shouldGreetWithName == null)
			{
				userSettings.shouldGreetWithName = true;
			}
			
			if ( userSettings.currentThemeImageName==null)
			{
				userSettings.currentThemeImageName="drawable/theme_image_sunrise";
			}
			
			if ( userSettings.themeName==null)
			{
				userSettings.themeName="Sunrise 1";
			}
			
			if ( userSettings.isCelcius==null)
			{
				userSettings.isCelcius= false;
			}
			
			if ( userSettings.isKeepAwakeOn==null)
			{
				userSettings.isKeepAwakeOn= true;
			}
			
			if (userSettings.shownSendMessageDialog == null)
			{
				userSettings.shownSendMessageDialog = true;
			}
			
			if (  userSettings.snoozeLength == 0 )
			{
				userSettings.snoozeLength = 5;
			}
			
			if (userSettings.hasSeenSendMessageQuestion == null)
			{
				userSettings.hasSeenSendMessageQuestion = false;
			}
			
			if (userSettings.userHasSeenReviewPopup == null)
			{
				userSettings.userHasSeenReviewPopup = false;
			}
			
			if (userSettings.isPaidUser == null)
			{
				userSettings.isPaidUser = false;
			}
			
			if (userSettings.hasPlayedSelectFriendsHelp == null)
			{
				userSettings.hasPlayedSelectFriendsHelp = false;
			}
			
			if (userSettings.hasPlayedComposeMessageHelp == null)
			{
				userSettings.hasPlayedComposeMessageHelp = false;
			}
			
			return userSettings;
	}
	
	public Boolean isPaidSubscriptionAboutToExpire()
	{
		// get date now
		Calendar oneWeekFromNow = Calendar.getInstance();
		oneWeekFromNow.add(7, Calendar.DATE);
		
		Date subscribeTillDate = this.userSettings.subscriberUntilDate;
		Date dateOneWeekFromNow = oneWeekFromNow.getTime(); 
		
		if (subscribeTillDate!=null && dateOneWeekFromNow!=null && dateOneWeekFromNow.after(subscribeTillDate))
		{
			return true;
		}
		return false;
	}
	
	public Boolean isPaidSubscriptionExpired()
	{
		// get date now
		Calendar now = Calendar.getInstance();
		
		Date subscribeTillDate = this.userSettings.subscriberUntilDate;
		Date dateNow = now.getTime(); 
		
		if (subscribeTillDate!=null && dateNow.after(subscribeTillDate))
		{
			return true;
		}
		return false;
	}
}
