package com.comantis.bedBuzz.fragments;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Typeface;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.comantis.bedBuzz.R;
import com.comantis.bedBuzz.VO.WeatherLookupVO;
import com.comantis.bedBuzz.VO.WeatherVO;
import com.comantis.bedBuzz.models.SoundManager;
import com.comantis.bedBuzz.models.UserModel;
import com.comantis.bedBuzz.models.WeatherModel;
import com.comantis.bedBuzz.service.WUndergroundService;

public class WeatherFragment extends Fragment implements OnClickListener {
	private View cf;
	private TextView weatherTemp;
	private TextView scaleTxt;
	private ProgressBar weatherLoadingSpinner;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		// Inflate the layout for this fragment
		UserModel um = UserModel.getUserModel();

		if (um.isAlarmGoingOffMode)
		{
			cf  =  inflater.inflate(R.layout.weather_fragment_alarm_mode, container, false);
		}
		else  if (um.userSettings.currentThemeImageName.equals("drawable/theme_image_black"))
		{
			cf  =  inflater.inflate(R.layout.weather_fragment_hc, container, false);
		}
		else
		{
			cf  =  inflater.inflate(R.layout.weather_fragment, container, false);
		}

		ViewGroup weather = (ViewGroup) cf.findViewById(R.id.weatherView); // main layout
		weatherLoadingSpinner = (ProgressBar) cf.findViewById(R.id.weatherLoadingSpinner);
		weather.setOnClickListener(this);


		registerReceivers();
		

		weatherTemp = (TextView) cf.findViewById(R.id.tempTV);
		scaleTxt = (TextView) cf.findViewById(R.id.degreesTV);

		// weather could already be loaded
		WeatherModel wm = WeatherModel.getWeatherModel();
		updateView(wm.currentWeather,this.getActivity().getApplicationContext());
		
		if (um.userSettings.currentThemeImageName.equals("drawable/theme_image_black"))
		{
			// use digital clock font
			Typeface tf = Typeface.createFromAsset(this.getActivity().getAssets(),
			"fonts/LiquidCrystal-Bold.otf");
			weatherTemp.setTypeface(tf);
			scaleTxt.setTypeface(tf);
		}

		return cf;
	}

	WeatherUpdatedReceiver mReceiver = new WeatherUpdatedReceiver();
	Boolean weatherReceiverRegister = false;

	private int getDrawableResourceIDForWeather(int weatherCode, boolean isNight, Context context)
	{
		WeatherModel wm = WeatherModel.getWeatherModel();

		WeatherLookupVO weatherLookup = wm.codesMap.get(weatherCode);

		int resId = -1;

		if (isNight)
		{
			resId = context.getResources().getIdentifier(weatherLookup.nightIcon , "drawable",  context.getPackageName());

		}
		else
		{
			resId = context.getResources().getIdentifier(weatherLookup.dayIcon , "drawable",  context.getPackageName());
		}


		return resId;
	}

	private void registerReceivers()
	{
		if (!weatherReceiverRegister)
		{
			// register for update weather intent
			IntentFilter intentFilter = new IntentFilter(WUndergroundService.WEATHER_UPDATED);
			this.getActivity().registerReceiver(mReceiver, intentFilter);
			weatherReceiverRegister = true;
		}
	}
	
	@Override 
	public void onPause()
	{
		if (weatherReceiverRegister)
		{
			weatherReceiverRegister = false;
			this.getActivity().unregisterReceiver(mReceiver);
		}

		super.onPause();

	}
	
	@Override
	public void onResume()
	{
		System.gc();

		registerReceivers();
		
		WeatherModel wm = WeatherModel.getWeatherModel();
		updateView(wm.currentWeather,this.getActivity().getApplicationContext());

		if (!isOnline())
		{
			weatherLoadingSpinner.setVisibility(View.GONE);
		}
		
		super.onResume();
	}

	public boolean isOnline() {
		ConnectivityManager cm =
			(ConnectivityManager) this.getActivity().getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo netInfo = cm.getActiveNetworkInfo();
		if (netInfo != null && netInfo.isConnected()) {
			return true;
		}
		return false;
	}
	
	private void updateView(WeatherVO weather, Context context) {
		// TODO Auto-generated method stub
		if (weather!=null)
		{

			String temp;
			String degreesScale;

			if (UserModel.getUserModel().userSettings.isCelcius)
			{
				temp = Integer.toString( weather.currentTempC );
				degreesScale = "¡C";
			}
			else
			{
				temp = Integer.toString( weather.currentTempF );
				degreesScale = "¡F";
			}

			weatherTemp.setText(temp);
			scaleTxt.setText(degreesScale);
			weatherLoadingSpinner.setVisibility(View.GONE);

			ImageView weatherImage = (ImageView) cf.findViewById(R.id.imageView1);
			WeatherModel wm = WeatherModel.getWeatherModel();
			weatherImage.setImageResource(getDrawableResourceIDForWeather(weather.currentCode, wm.getIsDark(), context));
		}
		else
		{
			Log.d("null error", "weather is null - failing silently");
		}
	}

	public class WeatherUpdatedReceiver extends BroadcastReceiver {

		@Override
		public void onReceive(Context context, Intent intent) {

			Bundle b = intent.getExtras();
			WeatherVO weather = (WeatherVO) b.get("weather");
			updateView(weather, context);
		}



	}

	@Override
	public void onClick(View vw) {

		if (WeatherModel.getWeatherModel().currentWeather!=null)
		{
			SoundManager.getSoundModel( this.getActivity().getApplicationContext()).playWeather();
		}
	}




}
