package com.comantis.bedBuzz.models;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.SystemClock;
import android.util.Log;

import com.comantis.bedBuzz.VO.AlarmVO;
import com.comantis.bedBuzz.services.BedBuzzAlarmService;
import com.comantis.bedBuzz.utils.DaysOfWeek;
import com.comantis.bedBuzz.utils.ObjectSerializer;

public class AlarmsModel {
	private static AlarmsModel alarmsModel = null; 
	
	public AlarmVO alarmCurrentlyEditing;
	public AlarmVO alarmRevertTo;
	public boolean isAddingNewAlarm;
	
	public static AlarmsModel getAlarmsModel() { 
	  if (alarmsModel == null) { 
		  alarmsModel = new AlarmsModel(); 
	  } 
	  
	  // initialize
	  
	  return alarmsModel; 
	}
	
	public ArrayList<AlarmVO> alarms = new ArrayList<AlarmVO>();
	public AlarmVO getDefaultAlarm() {
		
		AlarmVO defaultAlarm = new AlarmVO();
		defaultAlarm.enabled = true;
		defaultAlarm.alarmName = "Alarm "+(this.alarms.size()+1);
		defaultAlarm.hour = 7;
		defaultAlarm.mins = 0;
		defaultAlarm.setSunday(true);
		defaultAlarm.setMonday(true);
		defaultAlarm.setTuesday(true);
		defaultAlarm.setWednesday(true);
		defaultAlarm.setThursday(true);
		defaultAlarm.setFriday(true);
		defaultAlarm.setSaturday(true);
		defaultAlarm.alarmID = this.alarms.size()+1;
		return defaultAlarm;
	}
	
	private int getNumOfEnabledAlarms()
	{
		int count = 0;
		for (AlarmVO alarm : this.alarms)
		{
			if (alarm.enabled)
			{
				count++;
			}
		}
		
		return count;
	}
	
	public void saveAlarms(Context applicationContext)
	{
		UserModel um = UserModel.getUserModel();
		SharedPreferences settings = um.getPreferences(applicationContext);
		
	      SharedPreferences.Editor editor = settings.edit();
	      try {
			editor.putString("alarms", ObjectSerializer.serialize(alarms));
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		editor.commit();
		
		// now set the alarm manager
		setAlarmManager(applicationContext);
		
		if (getNumOfEnabledAlarms()>0)
		{
			Intent alarmChanged = new Intent("android.intent.action.ALARM_CHANGED");
			alarmChanged.putExtra("alarmSet", true/*false if you want to hide it*/);
			applicationContext.sendBroadcast(alarmChanged);
		}
		else
		{
			Intent alarmChanged = new Intent("android.intent.action.ALARM_CHANGED");
			alarmChanged.putExtra("alarmSet", false/*false if you want to hide it*/);
			applicationContext.sendBroadcast(alarmChanged);
		}
	}
	
	private void setAlarmManager(Context context) 
	{
		cancelAllAlarms(context);
		setAlarms(context);
	}
	
	private void setAlarms(Context context) {
		 for (AlarmVO alarm : alarms)
		 {
			 if (!alarm.enabled)
			 {
				 continue;
			 }
			 
			 for (int day = 0; day < 7; day++)
			 {
				 if (day ==0 && !alarm.isSunday())
				 {
					 continue;
				 }
				 if (day ==1 && !alarm.isMonday())
				 {
					 continue;
				 }
				 if (day ==2 && !alarm.isTuesday())
				 {
					 continue;
				 }
				 if (day ==3 && !alarm.isWednesday())
				 {
					 continue;
				 }
				 if (day ==4 && !alarm.isThursday())
				 {
					 continue;
				 }
				 if (day ==5 && !alarm.isFriday())
				 {
					 continue;
				 }
				 if (day ==6 && !alarm.isSaturday())
				 {
					 continue;
				 }
				 
			 AlarmManager mgr=(AlarmManager)context.getSystemService(Context.ALARM_SERVICE);
			    
			    
			 Intent myIntent = new Intent(context, BedBuzzAlarmService.class);
			 
			 int intentID = alarm.alarmID + ((day+1) * 10000);
			 
			 PendingIntent pi = PendingIntent.getService(context, intentID, myIntent, 0);
			    
			    // 604 800 000 in a week
			    Long alarmMS = getFixedMillisForAlarm(alarm,day);
			    
			    Log.d("Setting Alarm", "for "+alarmMS.toString() +"ms");			    
			    
			    mgr.setRepeating(AlarmManager.RTC_WAKEUP,
			    		alarmMS,
			    		 604800000L,
	                      pi);
			 }
		 }
		
	}
	
	// returns number of milliseconds since epoch for alarm
	 private Long getFixedMillisForAlarm(AlarmVO alarm, int day) {
		 
		 Calendar c = Calendar.getInstance();
		 int today_dayofWeek = c.get(Calendar.DAY_OF_WEEK);
		 int alarmDateOfMonth = -1;
		 
		 Calendar alarmDate = Calendar.getInstance();
		 boolean alarmTodayButPassed = alarmIsTodayButHasPassed(alarm,day+1);
		 if (alarmTodayButPassed)
		 {
			 // get date next (DAY_OF_WEEK)
			 alarmDate = getDateOfNextWeekday(day);
			 alarmDate.add(Calendar.DATE, 7);
		 }
		 else if (today_dayofWeek != day+1)
		 {
			// get date next (DAY_OF_WEEK)
			 alarmDate = getDateOfNextWeekday(day);
		 }
		 else // today
		 {
			 alarmDateOfMonth = c.get(Calendar.DATE);
		 }
		 
		 Log.d("setting for date", Integer.toString(alarmDate.get(Calendar.DAY_OF_MONTH)));
		 
	       c.set(alarmDate.get(Calendar.YEAR), alarmDate.get(Calendar.MONTH), alarmDate.get(Calendar.DAY_OF_MONTH), alarm.hour, alarm.mins, 0);
	       
	       Log.d("setting", c.getTime().toString());
	       
	       return new Long(c.getTimeInMillis());
	 }
	 
	 private boolean alarmIsTodayButHasPassed(AlarmVO alarm, int dayLookingAt) {
		
		 Calendar c = Calendar.getInstance();
		 
		 int hourToday = c.get(Calendar.HOUR_OF_DAY);
		 int minsToday = c.get(Calendar.MINUTE);
		 int dayToday = c.get(Calendar.DAY_OF_WEEK);
		 
		 if (alarm.isSunday() && dayToday == 1 && dayLookingAt == 1)
		 {
			
			 
			 if ((alarm.hour < hourToday) || (alarm.hour == hourToday && alarm.mins < minsToday))
			{
				 return true;
			}
		 }
		 
		 if (alarm.isMonday()&& dayToday == 2 && dayLookingAt == 2)
		 {
			if (alarm.hour < hourToday)
			{
				Log.d("alarm.hour < hourToday","0");
			}

			 if (alarm.hour == hourToday)
			 {
				 Log.d("alarm.hour == hourToday","0");
			 }
			 
			 if (alarm.mins < minsToday)
			 {
				 Log.d("alarm.mins < minsToday","0");
			 }
			 
			 if (alarm.hour == hourToday && alarm.mins < minsToday)
			 {
				 Log.d("alarm.hour == hourToday && alarm.mins < minsToday","0");
			 }
			 
			 if ((alarm.hour < hourToday) || 
					 (alarm.hour == hourToday && alarm.mins < minsToday))
				{
					 return true;
				}
		 }
		 
		 if (alarm.isTuesday()&& dayToday == 3 && dayLookingAt == 3)
		 {
			 if ((alarm.hour < hourToday) || (alarm.hour == hourToday && alarm.mins < minsToday))
			{
				 return true;
			}
		 }
		 
		 if (alarm.isWednesday() && dayToday == 4 && dayLookingAt == 4)
		 {
			 if ((alarm.hour < hourToday) || (alarm.hour == hourToday && alarm.mins < minsToday))
			{
				 return true;
			}
		 }
		 
		 if (alarm.isThursday() && dayToday == 5 && dayLookingAt == 5)
		 {
			 if ((alarm.hour < hourToday) || (alarm.hour == hourToday && alarm.mins < minsToday))
			{
				 return true;
			}
		 }
		 
		 if (alarm.isFriday() && dayToday == 6 && dayLookingAt == 6)
		 {
			 if ((alarm.hour < hourToday) || (alarm.hour == hourToday && alarm.mins < minsToday))
			{
				 return true;
			}
		 }
		 
		 if (alarm.isSaturday() && dayToday == 7 && dayLookingAt == 7)
		 {
			 if ((alarm.hour < hourToday) || (alarm.hour == hourToday && alarm.mins < minsToday))
			{
				 return true;
			}
		 }
		 
		return false;
	}

	private Calendar getDateOfNextWeekday(int day)
	 {
		
		Calendar calendar = Calendar.getInstance();  
		int weekday = calendar.get(Calendar.DAY_OF_WEEK);  
		int days = (day+1) - weekday;  
		if (days < 0)  
		{  
		    days += 7;  
		}  
		calendar.add(Calendar.DAY_OF_YEAR, days); 
		
		 return calendar;
	 }
	    
	private void cancelAllAlarms(Context context) {
		
		 AlarmManager mgr= (AlarmManager)context.getSystemService(Context.ALARM_SERVICE);

		 for (AlarmVO alarm : alarms)
		 {
			 Intent myIntent = new Intent(context, BedBuzzAlarmService.class);
	
			 for (int day = 1; day < 8; day ++)
			 {
				 int intentID = alarm.alarmID + (day * 10000);
				 PendingIntent pi = PendingIntent.getService(context, intentID, myIntent, 0);
				 
				    // Cancel alarms
				    try {
				        mgr.cancel(pi);
				    } catch (Exception e) {
				    	e.printStackTrace();
				    }
				 }
		 }
		    
	}
	
	public ArrayList<AlarmVO> getAlarms(Context applicationContext)
	{
            alarms = new ArrayList<AlarmVO>();

        //      load tasks from preference
            SharedPreferences settings = UserModel.getUserModel().getPreferences(applicationContext);

            try {
				alarms = (ArrayList<AlarmVO>) ObjectSerializer.deserialize(settings.getString("alarms", ObjectSerializer.serialize(new ArrayList<AlarmVO>())));
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
        
			return alarms;
	}

	public AlarmVO getAlarmForNow() {
		
		Date now = new Date();
		for (AlarmVO alarm : this.alarms)
		{
			if (alarm.enabled)
			{
				if ((now.getDay() == 0 && alarm.isSunday())
					|| (now.getDay() == 1 && alarm.isMonday())
					|| (now.getDay() == 2 && alarm.isTuesday())
					|| (now.getDay() == 3 && alarm.isWednesday())
					|| (now.getDay() == 4 && alarm.isThursday())
					|| (now.getDay() == 5 && alarm.isFriday())
					|| (now.getDay() == 6 && alarm.isSaturday()))
				{
					if (now.getHours() == alarm.hour && now.getMinutes() == alarm.mins)
					{
						return alarm;
					}
				}
			}
		}
		
		if (alarms.size() == 0)
		{
			return null;
		}
		return alarms.get(0);
	}
}
