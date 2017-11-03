package com.comantis.bedBuzz.activities;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Point;
import android.os.Bundle;
import android.os.PowerManager;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.Display;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.SeekBar;

import com.comantis.bedBuzz.R;
import com.comantis.bedBuzz.models.ClockModel;
import com.comantis.bedBuzz.models.MessagingModel;
import com.comantis.bedBuzz.models.RSSModel;
import com.comantis.bedBuzz.models.SoundManager;
import com.comantis.bedBuzz.models.UserModel;
import com.comantis.bedBuzz.service.FetchMessagesService;
import com.comantis.bedBuzz.service.RSSService;
import com.comantis.bedBuzz.services.BedBuzzAlarmService;
import com.comantis.bedBuzz.utils.FlurryEvents;
import com.flurry.android.FlurryAgent;

public class AlarmGoingOff extends FragmentActivity implements SeekBar.OnSeekBarChangeListener{

	Button snoozeBtn;
	//	Context context;
	TranslateAnimation  moveLefttoRight;
	ImageView rightArrow;
	View newsFragment;
	View messageViewFragment;
	
	private boolean isVisible = false;

	public void onStart()
	{
		super.onStart();
		FlurryAgent.onStartSession(this, "AHV3ZMQBPKPRPKH5SH23");
		FlurryAgent.logEvent(FlurryEvents.ALARM_GOING_OFF);
	}

	public void onStop()
	{
		super.onStop();
		FlurryAgent.onEndSession(this);
		// your code
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		// context = this.getApplicationContext();

		ClockModel cm = ClockModel.getClockModel(this.getApplicationContext());
		cm.initTimer();
		UserModel um = UserModel.getUserModel();

		
		// if we are already initialized, don't refetch the rss feeds
		registerIntentReceivers();
		
		if (savedInstanceState==null)
		{
			// fetch the rss clips
			RSSModel.getRSSModell().fetchRSSClips(getApplicationContext());

			// get the user messages
			if (um.userSettings.facebookID!=-1)
			{
				new FetchMessagesService(um.userSettings.facebookID,false,this.getApplicationContext()).execute();
			}
		}
		if (savedInstanceState == null)
		{
			UserModel.getUserModel().isSnoozing = false;
		}
		else
		{
			UserModel.getUserModel().isSnoozing = !savedInstanceState.getBoolean("snoozeEnabled",false);
		}


		if (!isVisible)
		{
			isVisible = true;
		}
		else
		{
			return;
		}
		setContentView(R.layout.alarm_going_off);

		final Window win = getWindow();
		win.addFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED
				| WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD);
		// Turn on the screen unless we are being launched from the AlarmAlert
		// subclass.
		win.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
				| WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON
				| WindowManager.LayoutParams.FLAG_ALLOW_LOCK_WHILE_SCREEN_ON);

		// always keep awake in this mode
		PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
		PowerManager.WakeLock wakeLock = pm.newWakeLock(
				PowerManager.SCREEN_DIM_WAKE_LOCK, "BedBuzzWakeLock");
		if (!wakeLock.isHeld())
		{
			// This will make the screen and power stay on
			wakeLock.acquire();
		}

		SeekBar slider = (SeekBar)findViewById(R.id.seekBar1); 
		slider.setOnSeekBarChangeListener(this);

		rightArrow = (ImageView)findViewById(R.id.rightArrow); 

		moveLefttoRight = new TranslateAnimation(Animation.RELATIVE_TO_SELF, slider.getLayoutParams().width - rightArrow.getLayoutParams().width, 0, 0);
		moveLefttoRight.setDuration(2000);
		moveLefttoRight.setFillAfter(false);
		moveLefttoRight.setRepeatCount(Animation.INFINITE);
		moveLefttoRight.setRepeatMode(Animation.RESTART);


		rightArrow.startAnimation(moveLefttoRight);

		newsFragment = (View)findViewById(R.id.newsFrag); 
		newsFragment.setVisibility(View.INVISIBLE);

		messageViewFragment = (View)findViewById(R.id.messageFrag); 
		messageViewFragment.setVisibility(View.INVISIBLE);
		
		snoozeBtn = (Button)findViewById(R.id.snoozeBtn); 

		if (um.isSnoozing)
		{
			snoozeBtn.setEnabled( false);
			snoozeBtn.setText("Snoozing");
		}
		snoozeBtn.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				snoozeBtn.setText("Snoozing");
				snoozeBtn.setEnabled(false);
				UserModel.getUserModel().isSnoozing = true;

				// stop the sounds
				SoundManager.getSoundModel(v.getContext()).stopSounds();

				// set the snooze
				AlarmManager mgr=(AlarmManager)getSystemService(Context.ALARM_SERVICE);


				Intent myIntent = new Intent(v.getContext(), BedBuzzAlarmService.class);

				int intentID = 99999;

				PendingIntent pi = PendingIntent.getService(v.getContext(), intentID, myIntent, 0);

				// snooze for a minute
				long alarmMS = System.currentTimeMillis() + (UserModel.getUserModel().userSettings.snoozeLength * 60*1000);
				mgr.set(AlarmManager.RTC_WAKEUP,
						alarmMS,
						pi);
			}
		});
	}

	private void registerIntentReceivers()
	{
		if (!messageReceiverRegistered)
		{
			messageReceivedReceiver = new MessageReceivedReceiver();
			IntentFilter intentFilter = new IntentFilter(FetchMessagesService.MESSAGES_RECEIVED);
			this.registerReceiver(messageReceivedReceiver, intentFilter);
			this.messageReceiverRegistered = true;
		}

		if (!rssReceiverRegistered)
		{
			rssReceivedReceiver = new RSSReceivedReceiver();
			IntentFilter intentRSSFilter = new IntentFilter(RSSService.RSS_DOWNLOAD_COMPLETE);
			this.registerReceiver(rssReceivedReceiver, intentRSSFilter);
			this.rssReceiverRegistered = true;
		}

		if (!rssClipPlayingReceiverRegistered)
		{
			rssClipPlayingReceiver = new RSSClipPlayingReceiver();
			IntentFilter intentRSSPlayingFilter = new IntentFilter(SoundManager.RSS_CLIP_PLAYING);
			this.registerReceiver(rssClipPlayingReceiver, intentRSSPlayingFilter);
			this.rssClipPlayingReceiverRegistered = true;
		}
		
		if (!messagePlayingReceiverRegistered)
		{
			messagePlayingReceiver = new MessagePlayingReceiver();
			IntentFilter messagePlayingFilter = new IntentFilter(SoundManager.MESSAGE_PLAYING);
			this.registerReceiver(messagePlayingReceiver, messagePlayingFilter);
			this.messagePlayingReceiverRegistered = true;
		}
	}

	private MessagePlayingReceiver messagePlayingReceiver ;
	private Boolean messagePlayingReceiverRegistered = false;

	private class MessagePlayingReceiver extends BroadcastReceiver {  
		@Override
		public void onReceive(Context context, Intent intent) 
		{
			messageViewFragment.setVisibility(View.VISIBLE);
		}
	}
	
	private MessageReceivedReceiver messageReceivedReceiver ;
	private Boolean messageReceiverRegistered = false;

	private class MessageReceivedReceiver extends BroadcastReceiver {  
		@Override
		public void onReceive(Context context, Intent intent) 
		{
			SoundManager.getSoundModel(context).addMessagesToPlayQueue();

			if (messageReceiverRegistered)
			{
				messageReceiverRegistered = false;
				try 
				{
					unregisterReceiver(messageReceivedReceiver);
				}
				catch (Exception ex)
				{
					
				}
			}
		}
	}

	private RSSReceivedReceiver rssReceivedReceiver ;
	private Boolean rssReceiverRegistered = false;

	private class RSSReceivedReceiver extends BroadcastReceiver {  
		@Override
		public void onReceive(Context context, Intent intent) 
		{
			boolean shouldAutoPlay = false;
			SoundManager sm = SoundManager.getSoundModel(context);
			//RSSModel.getRSSModell().fetchRSSClipScreenshots(getApplicationContext());
			if (sm.getNumberOfSoundsInPlayQueue() < 1 && !UserModel.getUserModel().isSnoozing)
			{
				shouldAutoPlay = true;
			}

			sm.addRSSClipsToPlayQueue();

			if (rssReceiverRegistered)
			{
				rssReceiverRegistered = false;
				try 
				{
					unregisterReceiver(rssReceivedReceiver);
				}
				catch (Exception ex)
				{
					Log.d("error","tried to unregister rssReceivedReceiver when not registered");
				}
			}

			if (shouldAutoPlay)
			{
				sm.playRSSClips();
			}
		}
	}

	private RSSClipPlayingReceiver rssClipPlayingReceiver ;
	private Boolean rssClipPlayingReceiverRegistered = false;

	private class RSSClipPlayingReceiver extends BroadcastReceiver {  
		@Override
		public void onReceive(Context context, Intent intent) 
		{
			int screenWidth = 0;

			WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);

			Display display = wm.getDefaultDisplay(); 

			if (android.os.Build.VERSION.SDK_INT < 13)
			{
				screenWidth = display.getWidth();  
			}
			else
			{
				Point size = new Point();
				display.getSize(size);
				screenWidth = size.x;
			}

			newsFragment.setVisibility(View.VISIBLE);

			
			// animate the new fragment in
			Animation animation = new TranslateAnimation(screenWidth, 0,0, 0);
			animation.setDuration(1000);
			animation.setFillAfter(false);
			newsFragment.startAnimation(animation);

		}
	}

	@Override
	public void onSaveInstanceState(Bundle savedInstanceState) {
		// Save UI state changes to the savedInstanceState.
		// This bundle will be passed to onCreate if the process is
		// killed and restarted.
		savedInstanceState.putBoolean("snoozeEnabled", this.snoozeBtn.isEnabled());
	}

	@Override 
	public void onPause()
	{
		try {

			if (messageReceiverRegistered)
			{
				this.unregisterReceiver(messageReceivedReceiver);
				messageReceiverRegistered = false;
			}

			if (rssReceiverRegistered)
			{
				unregisterReceiver(rssReceivedReceiver);
				rssReceiverRegistered = false;
			}

			if(this.rssClipPlayingReceiverRegistered)
			{
				unregisterReceiver(rssClipPlayingReceiver);
				rssClipPlayingReceiverRegistered = false;
			}
			if (this.messagePlayingReceiverRegistered)
			{
				unregisterReceiver(messagePlayingReceiver);
				messagePlayingReceiverRegistered = false;
			}
			
		} catch (Exception e) {
			e.printStackTrace();
		}

		super.onPause();

	}

	@Override
	public void onResume()
	{
		super.onResume();

		registerIntentReceivers();

		if (!UserModel.getUserModel().isSnoozing)
		{
			snoozeBtn.setText("Snooze");
			snoozeBtn.setEnabled(true);
		}

	}

	@Override
	public void onProgressChanged(SeekBar seekBar, int progress, boolean fromTouch) {
		if (progress == 100)
		{
			// stop the sounds
			SoundManager.getSoundModel(this).stopSounds();

			SoundManager.getSoundModel(this)._currentAlarm = null;
			// if we are snoozing, cancel the intent
			AlarmManager mgr=(AlarmManager)getSystemService(Context.ALARM_SERVICE);
			Intent myIntent = new Intent(seekBar.getContext(), BedBuzzAlarmService.class);
			int intentID = 99999;
			PendingIntent pi = PendingIntent.getService(seekBar.getContext(), intentID, myIntent, 0);

			// Cancel alarms
			try {
				mgr.cancel(pi);
			} catch (Exception e) {
				e.printStackTrace();
			}

			isVisible = false;

			MessagingModel mm = MessagingModel.getMessagingModel();

			// if the user has messages, and they are not played yet, play them now
			if (mm.messagesToPlay!=null && mm.messagesToPlay.size() > 0)
			{
				SoundManager.getSoundModel(this).playMessages();
			}

			UserModel.getUserModel().isAlarmGoingOffMode = false;
			UserModel.getUserModel().isSnoozing = false;
			UserModel.getUserModel().userSettings.alarmsGoneOffCount++;
			UserModel.getUserModel().saveUserSettings(getApplicationContext());
			UserModel.getUserModel().justCameFromAlarm = true;
			// finish this activity
			finish();

			Intent dialogIntent = new Intent(getBaseContext(), BedBuzzAndroidActivity.class);

			dialogIntent.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT | Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_SINGLE_TOP);
			getApplication().startActivity(dialogIntent);

			UserModel um = UserModel.getUserModel();

			// release wake lock if needed
			PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
			PowerManager.WakeLock wakeLock = pm.newWakeLock(
					PowerManager.SCREEN_DIM_WAKE_LOCK, "BedBuzzWakeLock");
			if (!um.userSettings.isKeepAwakeOn && wakeLock.isHeld())
			{
				wakeLock.release();
			}

		}

	}

	@Override
	public void onStartTrackingTouch(SeekBar arg0) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onStopTrackingTouch(SeekBar arg0) {
		// TODO Auto-generated method stub

	}


}
