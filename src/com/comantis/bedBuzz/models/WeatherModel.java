package com.comantis.bedBuzz.models;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Map;

import org.xmlpull.v1.XmlPullParserException;

import android.content.Context;
import android.os.Handler;

import com.comantis.bedBuzz.VO.WeatherLookupVO;
import com.comantis.bedBuzz.VO.WeatherVO;
import com.comantis.bedBuzz.managers.WeatherManager;

public class WeatherModel {

	private static WeatherModel weatherModel = null; 
	public static WeatherModel getWeatherModel() { 
	  if (weatherModel == null) { 
		  weatherModel = new WeatherModel(); 
	  } 
	  
	  // initialize
	  
	  return weatherModel; 
	}
	
	private Context context;
	private Context activityContext;
	private boolean isInitalized =false;
	
	public void initalize(Context context, Context activityCtx) 
	{
		this.context = context;
		
		if (activityCtx!=null)
		{
			this.activityContext = activityCtx;
		}
		
		WeatherManager wm = null;
		
		if (!isInitalized )
		{
			initTimer();
		
			wm = new WeatherManager(context);
			
			try {
				wm.parseWeatherXML(context);
			} catch (XmlPullParserException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			isInitalized = true;
		}
		else
		{
			wm = new WeatherManager(context);
		}
		
		wm.getWeatherFromService(context,activityContext);
		
	}
	
	public  Map<Integer,WeatherLookupVO> codesMap;
	public WeatherVO currentWeather;
	public Calendar weatherWasLastFetchedAt;
	
	public boolean getIsDark() {
		
		SimpleDateFormat dateFormat = new SimpleDateFormat("HH:mm");
	 Boolean isDark = false;
			try {
				Date sunriseDate = dateFormat.parse(currentWeather.sunriseTimeStr);
				Date sunsetDate = dateFormat.parse(currentWeather.sunsetTimeStr);
				
				int sunriseHour = sunriseDate.getHours();
				int sunriseMins = sunriseDate.getMinutes();
				
				Date time = new Date();
				int hourNow = time.getHours();
				int minNow = time.getMinutes();
				
				if (hourNow < sunriseHour)
				{
					isDark = true;
				}
				
				if (hourNow == sunriseHour && minNow < sunriseMins)
				{
					isDark = true;
				}
				
				if (hourNow >sunsetDate.getHours())
				{
					isDark = true;
				}
				
				if (hourNow == sunsetDate.getHours() && minNow > sunsetDate.getMinutes())
				{
					isDark = true;
				}
				
			} catch (ParseException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			return isDark;
		
	}
	
	private Handler mHandler = new Handler();
	private Long mStartTime = 0L;
	private Boolean isTimerStarted = false;
	private static int WEATHER_REFRESH_TIME = 1000*60*60;
	//private static int WEATHER_REFRESH_TIME = 1000*60;
	// every 1 hrs, update the weather
	public void initTimer() {
		if (mStartTime == 0L && !isTimerStarted) {
			mStartTime = System.currentTimeMillis();
			mHandler.removeCallbacks(mUpdateTimeTask);
			mHandler.postDelayed(mUpdateTimeTask, WEATHER_REFRESH_TIME);
			isTimerStarted = true;
		}
	}



	private Runnable mUpdateTimeTask = new Runnable() {
		public void run() {
			
			updateWeather();

			mHandler.postDelayed(this,
					WEATHER_REFRESH_TIME);
		}

		private void updateWeather() {
			new WeatherManager().getWeatherFromService(context,activityContext);
			
		}
	};
}
