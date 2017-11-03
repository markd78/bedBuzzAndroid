package com.comantis.bedBuzz.services;



import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

import com.comantis.bedBuzz.activities.AlarmGoingOff;
import com.comantis.bedBuzz.models.AlarmsModel;
import com.comantis.bedBuzz.models.SoundManager;
import com.comantis.bedBuzz.models.ThemesModel;
import com.comantis.bedBuzz.models.UserModel;
import com.comantis.bedBuzz.models.WeatherModel;
import com.comantis.bedBuzz.service.WUndergroundService;



public class BedBuzzAlarmService extends Service {



	@Override

	public void onCreate() {

		// TODO Auto-generated method stub

		Log.w(getClass().getSimpleName(), "***** MyAlarmService.onCreate()");
	}



	@Override

	public IBinder onBind(Intent intent) {

		// TODO Auto-generated method stub

		Log.w(getClass().getSimpleName(), "***** MyAlarmService.onBind()");

		return null;

	}



	@Override

	public void onDestroy() {

		// TODO Auto-generated method stub

		super.onDestroy();

		Log.w(getClass().getSimpleName(), "***** MyAlarmService.onDestroy()");

	}



	@Override

	public void onStart(Intent intent, int startId) {

		// TODO Auto-generated method stub

		super.onStart(intent, startId);
		
		if (intent == null)
			return;

		// load user settings
		UserModel.getUserModel().getSettings(getApplicationContext());

		Log.i(getClass().getSimpleName(), "***** MyAlarmService.onStart() - Received start id " + startId + ": " + intent);

		if (!isRegistered)
		{
			// get the weather first
			mReceiver = new WeatherReceiver();
			IntentFilter intentFilter = new IntentFilter(WUndergroundService.WEATHER_UPDATED);
			this.registerReceiver(mReceiver, intentFilter);
			isRegistered = true;

			
			// load weather and themes
			WeatherModel wm = WeatherModel.getWeatherModel();
			wm.initalize(this.getApplicationContext(),null);
			
			if (!isOnline())
			{
				playAlarm(this.getApplicationContext());
			}
		}

	}

	public boolean isOnline() {
		ConnectivityManager cm =
			(ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo netInfo = cm.getActiveNetworkInfo();
		if (netInfo != null && netInfo.isConnected()) {
			return true;
		}
		return false;
	}

	@Override

	public boolean onUnbind(Intent intent) {

		// TODO Auto-generated method stub
		Log.w(getClass().getSimpleName(), "***** MyAlarmService.onUnbind()");
		return super.onUnbind(intent);

	}


	private WeatherReceiver mReceiver ;
	private Boolean isRegistered = false;

	public class WeatherReceiver extends BroadcastReceiver {

		@Override
		public void onReceive(Context context, Intent intent) {

			ThemesModel.getThemesModel(context);

			if (isRegistered)
			{
				unregisterReceiver(mReceiver);
				isRegistered = false; 
			}

			Log.w(getClass().getSimpleName(), "***** weatherReceived");
			//Toast.makeText(context, "MyAlarmService.onStart()", Toast.LENGTH_LONG).show();

			
			playAlarm(context);
		}



	}
	
	private void playAlarm(Context context)
	{
		Intent dialogIntent = new Intent(getBaseContext(), AlarmGoingOff.class);
		dialogIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
		getApplication().startActivity(dialogIntent);

		UserModel.getUserModel().isSnoozing = false;
		UserModel.getUserModel().isAlarmGoingOffMode = true;
		SoundManager.getSoundModel(context).playAlarm(AlarmsModel.getAlarmsModel().getAlarmForNow());
	}

}

