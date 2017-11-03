package com.comantis.bedBuzz.fragments;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.comantis.bedBuzz.R;
import com.comantis.bedBuzz.models.AlarmsModel;
import com.comantis.bedBuzz.models.ClockModel;
import com.comantis.bedBuzz.models.SoundManager;
import com.comantis.bedBuzz.models.UserModel;
import com.comantis.bedBuzz.utils.Utilities;

public class ClockFragment extends Fragment implements OnClickListener {
	
	TextView hoursTxtView;
	TextView minsTxtView;
	TextView amPMTxtView;
	TextView dateTxtView;
	
	@Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View cf = null;
        UserModel um = UserModel.getUserModel();
        
        if (um.isAlarmGoingOffMode)
        {
        	cf  =  inflater.inflate(R.layout.clock_fragment_alarm_mode, container, false);
        }
        else if (um.userSettings.currentThemeImageName.equals("drawable/theme_image_black"))
        {
        	cf  =  inflater.inflate(R.layout.clock_fragment_hc, container, false);
        }
        else
        {
        	cf  =  inflater.inflate(R.layout.clock_fragment, container, false);
        }
        RelativeLayout clock = (RelativeLayout) cf.findViewById(R.id.clockView); // main layout
        hoursTxtView = (TextView) cf.findViewById(R.id.hourTxtView);
        minsTxtView = (TextView) cf.findViewById(R.id.minuteTxtView);
        amPMTxtView = (TextView) cf.findViewById(R.id.amPMTxtView);
        dateTxtView = (TextView) cf.findViewById(R.id.dateTxtView);
        
      
        
        registerIntentAndRefreshClock();
        
        clock.setOnClickListener(this);
        
        
        return cf;
   }
	
	public void setFonts()
	{
		UserModel um = UserModel.getUserModel();
		
		  if (um.userSettings.currentThemeImageName.equals("drawable/theme_image_black") &&!um.isAlarmGoingOffMode)
	        {
	        	// use digital clock font
	        	Typeface tf = Typeface.createFromAsset(this.getActivity().getAssets(),
	            "fonts/LiquidCrystal-Bold.otf");
	        	hoursTxtView.setTypeface(tf);
	        	minsTxtView.setTypeface(tf);
	        	amPMTxtView.setTypeface(tf);
	        	dateTxtView.setTypeface(tf);
	        }
	        else
	        {
	        	Typeface tf = Typeface.DEFAULT;
	        	hoursTxtView.setTypeface(tf);
	        	minsTxtView.setTypeface(tf);
	        	amPMTxtView.setTypeface(tf);
	        	dateTxtView.setTypeface(tf);
	        }
	}
	
	private void registerIntentAndRefreshClock()
	{
		if (!timeReceiverRegistered)
		{
			 // register for update time intent
	        IntentFilter intentFilter = new IntentFilter(ClockModel.NEW_MINUTE);
	        this.getActivity().registerReceiver(mReceiver, intentFilter);
	        timeReceiverRegistered = true;
	        ClockModel cm = ClockModel.getClockModel(this.getActivity().getApplicationContext());
	        
	        updateView(cm.hour,cm.minutes);
		}
	}
	
	@Override
	public void onResume() {
		super.onResume();
		
		setFonts();
		
		registerIntentAndRefreshClock();
	}
	
	private void updateView(Integer hour, Integer mins)
	{
		UserModel um = UserModel.getUserModel();
		
		hoursTxtView.setText(Utilities.getFormatterHour(hour,um.userSettings.is24HrMode).toString());
		minsTxtView.setText(Utilities.getFormattedMins(hour,mins,um.userSettings.is24HrMode));
		dateTxtView.setText(Utilities.getFormattedDate());
		if (um.userSettings.is24HrMode)
		{
			amPMTxtView.setVisibility(View.GONE);
		}
		else
		{
			amPMTxtView.setVisibility(View.VISIBLE);
			
			if (hour>=12)
			{
				amPMTxtView.setText("PM");
			}
			else
			{
				amPMTxtView.setText("AM");
			}
		}
		
	}
	
	TimeUpdatedReceiver mReceiver = new TimeUpdatedReceiver();
	Boolean timeReceiverRegistered = false;
	
	 public class TimeUpdatedReceiver extends BroadcastReceiver {
		 
	        @Override
	        public void onReceive(Context context, Intent intent) {
	           
	        	int hour = intent.getIntExtra("hour", 0);
	        	int mins = intent.getIntExtra("minutes", 0);
	        	updateView(hour, mins);
	        }

			

	    }

	 @Override 
		public void onPause()
		{
			if (timeReceiverRegistered)
			{
				timeReceiverRegistered = false;
				this.getActivity().unregisterReceiver(mReceiver);
			}

			super.onPause();

		}
	
	@Override
	public void onClick(View vw) {
		 SoundManager.getSoundModel(this.getActivity().getApplicationContext()).playTime();
	}
        
       
    
}
