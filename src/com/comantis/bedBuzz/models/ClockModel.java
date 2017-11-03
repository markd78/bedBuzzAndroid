package com.comantis.bedBuzz.models;

import java.util.Date;

import android.content.Context;
import android.content.Intent;
import android.os.Handler;

public class ClockModel {

	private static ClockModel clockModel = null; 

	public static String NEW_MINUTE = "new_minute";
	
	private static Context context;

	public static ClockModel getClockModel(Context appContext) { 
		if (clockModel == null) { 
			clockModel = new ClockModel(); 
		} 
		
		context = appContext;

		return clockModel;
	}

	public Integer minutes = 0;
	public Integer hour = 0;

	public void updateTime(boolean forceRefresh)
	{
		Date now = new Date();
		int minsNow = now.getMinutes();
		int hoursNow = now.getHours();

		// start 1 second timer
		if (minsNow != minutes || forceRefresh)
		{
			minutes = minsNow;
			hour = hoursNow;
			
			 // complete
	    	Intent i = new Intent(NEW_MINUTE);
	    	i.putExtra("hour", hour);
	    	i.putExtra("minutes", minutes);
	    	
	        context.sendBroadcast(i);
		}
	}

	private Handler mHandler = new Handler();
	private Long mStartTime = 0L;
	private Boolean isTimerStarted = false;

	public void initTimer() {
		if (mStartTime == 0L && !isTimerStarted) {
			mStartTime = System.currentTimeMillis();
			mHandler.removeCallbacks(mUpdateTimeTask);
			mHandler.postDelayed(mUpdateTimeTask, 100);
			isTimerStarted = true;
			
			// and refresh the time for right now
			updateTime(false);
		}
	}



	private Runnable mUpdateTimeTask = new Runnable() {
		public void run() {
			
			updateTime(false);

			mHandler.postDelayed(this,
					 1000);
		}
	};

}
