package com.comantis.bedBuzz.activities;

import java.util.Calendar;
import java.util.Date;

import android.annotation.SuppressLint;
import android.app.ActivityManager;
import android.app.ActivityManager.RunningServiceInfo;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Point;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.BatteryManager;
import android.os.Build;
import android.os.Bundle;
import android.os.PowerManager;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.Display;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.LinearInterpolator;
import android.view.animation.TranslateAnimation;
import android.widget.ImageView;

import com.comantis.bedBuzz.R;
import com.comantis.bedBuzz.models.ClockModel;
import com.comantis.bedBuzz.models.FacebookModel;
import com.comantis.bedBuzz.models.RSSModel;
import com.comantis.bedBuzz.models.SoundManager;
import com.comantis.bedBuzz.models.ThemesModel;
import com.comantis.bedBuzz.models.UserModel;
import com.comantis.bedBuzz.models.WeatherModel;
import com.comantis.bedBuzz.service.LoginService;
import com.comantis.bedBuzz.services.BedBuzzAlarmService;
import com.comantis.bedBuzz.services.BillingService;
import com.comantis.bedBuzz.utils.FlurryEvents;
import com.flurry.android.FlurryAgent;
import com.tapfortap.AdView;
import com.tapfortap.TapForTap;

@SuppressLint({ "NewApi", "NewApi", "NewApi", "NewApi" })
public class BedBuzzAndroidActivity extends FragmentActivity {

	static final int CREATE_MESSAGE_REQUEST = 300;
	static final int CHANGE_THEME = 400;
	private String themeImageName = "";

	private PowerManager.WakeLock wakeLock ;

	private ImageView backGroundImage;
	private ImageView batteryIcon;

	private View sendMessageQuestionFrag;
	private View chooseVoiceQuestionFrag;
	private View reviewQuestionFrag;
	private View subscribeQuestionFrag;

	private View clockFrag;
	private View weatherFrag;
	private AdView adView;
	private boolean adsLoaded = false;

	
	public void onStart()
	{
		super.onStart();
		FlurryAgent.onStartSession(this, "AHV3ZMQBPKPRPKH5SH23");
		// your code
		ClockModel cm = ClockModel.getClockModel(this.getApplicationContext());
		cm.initTimer();

		UserModel um = UserModel.getUserModel();

		RSSModel rm = RSSModel.getRSSModell();
		rm.getRSSFeedsFromSettings(getApplicationContext());

		// login only if we havent done it today
		if (um.dateUserLastLoggedOn == null || !isSameDay(um.dateUserLastLoggedOn, new Date()))
		{
			um.dateUserLastLoggedOn = new Date();
			// log in to BedBuzz
			new LoginService(um.userSettings.bedBuzzID, um.userSettings.facebookID, this.getApplicationContext() ).execute();
		}

		// if the user changes the theme, need to invalidate the view to load correct theme
		if (!themeImageName.equals(um.userSettings.currentThemeImageName))
		{
			View vg = findViewById(R.id.mainLayout);
			vg.invalidate();
		}

		clockFrag = findViewById(R.id.clockFrag);
		weatherFrag = findViewById(R.id.weatherFrag);

		batteryIcon = (ImageView) findViewById(R.id.batteryIcon);



		// set the background image
		//setBackgroundImage(); // moved to oncreate

		initPopups();
	}

	public void onStop()
	{
		super.onStop();
		FlurryAgent.onEndSession(this);
		// your code
	}

	private void setView(Boolean forceChange)
	{
		if (themeImageName.equals("") || ( forceChange!=null && forceChange==Boolean.TRUE))
		{
			// make sure we have the settings
			if (UserModel.getUserModel().userSettings == null || UserModel.getUserModel().userSettings.currentThemeImageName == null)
			{
				UserModel.getUserModel().getSettings(getApplicationContext());
			}

			if (UserModel.getUserModel().userSettings.currentThemeImageName.equals("drawable/theme_image_black"))
			{
				setContentView(R.layout.main_high_contrast);
			}
			else
			{
				setContentView(R.layout.main);
			}
		}
	}

	public void reload() {

		Intent intent = getIntent();
		overridePendingTransition(0, 0);
		intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
		finish();

		overridePendingTransition(0, 0);
		startActivity(intent);
	}

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,WindowManager.LayoutParams.FLAG_FULLSCREEN);

		
		//setContentView(R.layout.main);
		if (Build.VERSION.SDK_INT >= 11)
		{
			//super.setTheme( android.R.style.Theme_Holo );
			getWindow().requestFeature(Window.FEATURE_ACTION_BAR_OVERLAY);
		}
		else
		{
			getWindow().requestFeature(Window.FEATURE_NO_TITLE);
		}

		// Substitute your real App ID here
		TapForTap.initialize(this, "e7b17ba480c9bbf04fb4c2f65b14170f");
		
		setView(Boolean.FALSE);
		// set the background image
		setBackgroundImage(); 

		// Now get the AdView and load TapForTap ads!
		adView = (AdView) findViewById(R.id.ad_view);


		ClockModel cm = ClockModel.getClockModel(this.getApplicationContext());
		cm.initTimer();

		themeImageName = UserModel.getUserModel().userSettings.currentThemeImageName;


		registerReceivers();


		UserModel um = UserModel.getUserModel();
		if (!um.isInitalized)
		{
			Boolean enableFacebook = getIntent().getBooleanExtra("isFromEnableFacebook",false);
			// if we came to this screen from enable facebook, first ask the user if they want to send a wake up message now
			if (enableFacebook && !UserModel.getUserModel().userSettings.shownSendMessageDialog)
			{
				sendMessageFirstTime();

				UserModel.getUserModel().userSettings.shownSendMessageDialog = true;
				UserModel.getUserModel().saveUserSettings(getApplicationContext());
			}

			

			ThemesModel tm = ThemesModel.getThemesModel(this.getApplicationContext());

			startService(new Intent(this.getApplicationContext(), BillingService.class));

			if (enableFacebook || um.userSettings.facebookID != -1)
			{
				FacebookModel fm = FacebookModel.getFacebookModel();
				fm.authorize(this);
			}

			um.isInitalized = true;
		}

		// set the wake lock 
		PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
		wakeLock = pm.newWakeLock(
				PowerManager.SCREEN_DIM_WAKE_LOCK | PowerManager.ACQUIRE_CAUSES_WAKEUP, "BedBuzzWakeLock");

		
		// RSSModel.getRSSModell().getURLScreenshot("http://www.google.com",this.getApplicationContext());
	}

	private void registerReceivers()
	{
		if (!loginReceiverRegistered)
		{
			mReceiver = new LoginReceiver();
			IntentFilter intentFilter = new IntentFilter(LoginService.LOGIN_COMPLETE);
			this.registerReceiver(mReceiver, intentFilter);
			this.loginReceiverRegistered = true;
		}

		if (!friendsFetchedReceiverRegistered)
		{
			friendsFetchedReceiver = new FriendsFetchedReceiver();
			IntentFilter intentFriendFetchedFilter = new IntentFilter(FacebookModel.FRIENDS_FETCHED);
			this.registerReceiver(friendsFetchedReceiver, intentFriendFetchedFilter);
			this.friendsFetchedReceiverRegistered = true;
		}
		
		if (!themeChangeReceiverRegistered)
		{
			mThemeChangeReceiver = new ThemeChangeReceiver();
			this.themeChangeReceiverRegistered = true;
			IntentFilter intentThemeChangeFilter = new IntentFilter(ThemesModel.THEME_CHANGE);
			this.registerReceiver(mThemeChangeReceiver, intentThemeChangeFilter);
		}

		if (!batteryReceiverRegistered)
		{
			mBatteryChangeReceiver = new BatteryChangeReceiver();
			this.batteryReceiverRegistered = true;
			this.registerReceiver(this.mBatteryChangeReceiver, 
					new IntentFilter(Intent.ACTION_BATTERY_CHANGED));
			
			// call it now to get immediate refresh
			this.registerReceiver(null, 
					new IntentFilter(Intent.ACTION_BATTERY_CHANGED));
		}
	}

	private void releaseBackgroundImage()
	{
		if (backGroundImage!=null)
		{
			Drawable drawable = backGroundImage.getDrawable();
			if (drawable instanceof BitmapDrawable) {
				BitmapDrawable bitmapDrawable = (BitmapDrawable) drawable;
				Bitmap bitmap = bitmapDrawable.getBitmap();

				// on OS 3+ - crashes 
				if (Build.VERSION.SDK_INT < 11)
				{
					bitmap.recycle();
				}
			}
			backGroundImage.getDrawable().setCallback(null);
		}
	}

	@Override
	public void onDestroy() {

		releaseBackgroundImage();

		super.onDestroy();
	}

	@Override 
	public void onPause()
	{
		if (loginReceiverRegistered)
		{
			loginReceiverRegistered = false;
			unregisterReceiver(mReceiver);
		}

		if (this.themeChangeReceiverRegistered)
		{
			themeChangeReceiverRegistered = false;
			unregisterReceiver(mThemeChangeReceiver);
		}

		if (this.batteryReceiverRegistered)
		{
			batteryReceiverRegistered = false;
			unregisterReceiver(mBatteryChangeReceiver);
		}
		
		if (this.friendsFetchedReceiverRegistered)
		{
			friendsFetchedReceiverRegistered = false;
			unregisterReceiver(friendsFetchedReceiver);
		}

		if (wakeLock.isHeld())
		{
			wakeLock.release();
		}

		if (adView!=null)
		{
			adView.stopLoadingAds();
			adsLoaded  = false;
		}
		
		if (!UserModel.getUserModel().isAlarmGoingOffMode)
		{
		// stop any playing sounds
		SoundManager.getSoundModel(getApplicationContext()).stopSounds();
		}
		
		super.onPause();

	}

	private View getPopupWizard()
	{
		UserModel um = UserModel.getUserModel();
		View popup = null;
		SoundManager sm = SoundManager.getSoundModel(getApplicationContext());

		// if 'send message' question not shown, then show it
		if (um.userSettings.popupMessageCount % 7 == 0 && um.userSettings.facebookID > 0 && !um.userSettings.isPaidUser && !um.justCameFromAlarm)
		{
			// send message?
			popup = sendMessageQuestionFrag;
			sm.playSendMessageQuestion();
		}
		else if (um.userSettings.popupMessageCount % 7 <= 1 && !um.userSettings.isPaidUser  && !um.justCameFromAlarm)
		{
			// in case we skipped previous.. incr the counter
			if (um.userSettings.popupMessageCount % 7 == 0)
			{
				um.userSettings.popupMessageCount++;
			}

			// change voice?
			popup = chooseVoiceQuestionFrag;
			sm.playChangeVoiceQuestion();
		}
		else if (um.userSettings.popupMessageCount  %7 == 4 && !um.userSettings.isPaidUser)
		{
			// subscribe?
			popup = subscribeQuestionFrag;
			sm.playSubscribeQuestion();
		}
		else if (!um.userSettings.userHasSeenReviewPopup && um.userSettings.alarmsGoneOffCount > 1  && !um.justCameFromAlarm)
		{
			// review
			popup = reviewQuestionFrag;
			sm.playPleaseReview();
		}
		else 
		{
			// just increment the counter
			um.userSettings.popupMessageCount++;
			
			um.saveUserSettings(getApplicationContext());
		}

		um.justCameFromAlarm = false;
		
		return popup;
	}

	public void hidePopupWizards()
	{
		// this is called whenever the user enters yes/no from the popups
		UserModel um = UserModel.getUserModel();
		um.userSettings.popupMessageCount++;
		um.saveUserSettings(getApplicationContext());

		sendMessageQuestionFrag.setVisibility(View.INVISIBLE);
		chooseVoiceQuestionFrag.setVisibility(View.INVISIBLE);
		subscribeQuestionFrag.setVisibility(View.INVISIBLE);
		reviewQuestionFrag.setVisibility(View.INVISIBLE);
	}

	private ThemeChangeReceiver mThemeChangeReceiver ;
	private Boolean themeChangeReceiverRegistered = false;

	private class ThemeChangeReceiver extends BroadcastReceiver {  

		@Override
		public void onReceive(Context context, Intent intent) 
		{

			releaseBackgroundImage();

			ViewGroup vg = (ViewGroup) findViewById(R.id.mainLayout);
			vg.invalidate();
			unregisterReceiver(mThemeChangeReceiver);
			themeChangeReceiverRegistered = false;
		}

	}
	
	private FriendsFetchedReceiver friendsFetchedReceiver ;
	private Boolean friendsFetchedReceiverRegistered = false;

	private class FriendsFetchedReceiver extends BroadcastReceiver {  

		@Override
		public void onReceive(Context context, Intent intent) 
		{
			if (jumpToMessagesAfterFBLogin )
			{
				Log.d("Debug", "Showing messaging screen");
				showMessagesScreen();  

				// reset the flag 
				jumpToMessagesAfterFBLogin = false;
			}
		}

	}

	private BatteryChangeReceiver mBatteryChangeReceiver ;
	private Boolean batteryReceiverRegistered = false;

	private class BatteryChangeReceiver extends BroadcastReceiver {  

		@Override
		public void onReceive(final Context context, Intent intent) 
		{

			int plugged = intent.getIntExtra(BatteryManager.EXTRA_PLUGGED, -1);
			boolean charging =  ((plugged == BatteryManager.BATTERY_PLUGGED_AC) || (plugged == BatteryManager.BATTERY_PLUGGED_USB));

			if (charging)
			{
				batteryIcon.clearAnimation();
				batteryIcon.setVisibility(View.INVISIBLE);
				batteryIcon.setVisibility(View.GONE);
				
				
			}
			else if (UserModel.getUserModel().userSettings.isKeepAwakeOn && !UserModel.getUserModel().isBatteryIconDismissed)
			{

				batteryIcon.setVisibility(View.VISIBLE);
				batteryIcon.bringToFront();
				makeBatteryIconBlink();
				batteryIcon.setOnClickListener(new OnClickListener() {
					@Override
					public void onClick(final View view) {
						view.clearAnimation();
						UserModel.getUserModel().isBatteryIconDismissed = true;
						batteryIcon.setVisibility(View.GONE);
						AlertDialog alertDialog = new AlertDialog.Builder(context).create();
						alertDialog.setTitle("Warning");
						alertDialog.setMessage("The device is not plugged in, but 'Prevent sleeping' is enabled.  This could deplete your battery.");
						alertDialog.setButton("OK", new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog, int which) {
								// TODO Add your code for the button here.
							} });
						alertDialog.show();
					}
				});
			}
			else
			{
				batteryIcon.clearAnimation();
				batteryIcon.setVisibility(View.INVISIBLE);
				batteryIcon.setVisibility(View.GONE);
			}

		}
	}

	private void makeBatteryIconBlink()
	{
		final Animation animation = new AlphaAnimation(1, 0); // Change alpha from fully visible to invisible
		animation.setDuration(500); // duration - half a second
		animation.setInterpolator(new LinearInterpolator()); // do not alter animation rate
		animation.setRepeatCount(Animation.INFINITE); // Repeat animation infinitely
		animation.setRepeatMode(Animation.REVERSE); // Reverse animation at the end so the button will fade back in

		batteryIcon.startAnimation(animation);

	}

	private LoginReceiver mReceiver ;
	private Boolean loginReceiverRegistered = false;

	private boolean jumpToMessagesAfterFBLogin = false;

	private class LoginReceiver extends BroadcastReceiver {  
		@Override
		public void onReceive(Context context, Intent intent) 
		{
			if (UserModel.getUserModel().isPaidSubscriptionAboutToExpire() && !UserModel.getUserModel().isPaidSubscriptionExpired())
			{
				showExpiringOrExpiredAlert("Your subscription is about to expire (on "+UserModel.getUserModel().userSettings.subscriberUntilDate.toString()
						+").  Do you want to renew now?");
			}

			if (UserModel.getUserModel().isPaidSubscriptionExpired())
			{
				showExpiringOrExpiredAlert("Your subscription expired (on "+UserModel.getUserModel().userSettings.subscriberUntilDate.toString()
						+").  Do you want to renew now?");
			}

			if (loginReceiverRegistered)
			{
				loginReceiverRegistered = false;
				unregisterReceiver(mReceiver);
			}
			
			showOrHideAds();

			showPopUpIfNeeded();
		}
	}

	private void showExpiringOrExpiredAlert(String question) {
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setMessage(question)
		.setCancelable(false)
		.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int id) {
				Intent intent = new Intent(BedBuzzAndroidActivity.this, SubscribeScreenActivity.class);
				intent.putExtra("jumpToSubscribe",true);
				startActivity(intent);
			}
		})
		.setNegativeButton("No", new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int id) {
				dialog.cancel();
			}
		});
		AlertDialog alert = builder.create();
		alert.show();
	}

	private boolean isAlarmServiceRunning() {
		ActivityManager manager = (ActivityManager) getSystemService(ACTIVITY_SERVICE);
		for (RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
			if (BedBuzzAlarmService.class.getName().equals(service.service.getClassName())) {
				return true;
			}
		}
		return false;
	}

	public void showMessagesScreen()
	{
		// if the user is not logged into facebook, ask them to log in
		FacebookModel fm = FacebookModel.getFacebookModel();
		
		if (fm.friends == null )
		{
			this.jumpToMessagesAfterFBLogin = true;
			
			fm.authorize(this);
		}
		else
		{
			Intent intent = new Intent(BedBuzzAndroidActivity.this, SelectFriendsActivity.class);
			startActivityForResult(intent,CREATE_MESSAGE_REQUEST);
		}
	}



	public void showVoicePickerScreen()
	{
		Intent intent = new Intent(BedBuzzAndroidActivity.this, VoicePickerActivity.class);
		startActivity(intent);
	}

	private void showThemePickerScreen()
	{
		Intent intent = new Intent(BedBuzzAndroidActivity.this, ThemePickerActivity.class);
		startActivityForResult(intent, CHANGE_THEME);
	}

	public void sendMessageFirstTime()
	{
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setMessage("Thanks for logging in!  Now you can send wake up messages to your friends.  Do you want to try sending your first message now?")
		.setCancelable(false)
		.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int id) {
				FlurryAgent.logEvent(FlurryEvents.SEND_MESSAGE_WIZARD_YES);
				showMessagesScreen();
			}
		})
		.setNegativeButton("No", new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int id) {
				FlurryAgent.logEvent(FlurryEvents.SEND_MESSAGE_WIZARD_NO);
				dialog.cancel();
			}
		});
		AlertDialog alert = builder.create();
		alert.show();
	}

	protected void onActivityResult(int requestCode, int resultCode,
			Intent data) {
		if (requestCode == CREATE_MESSAGE_REQUEST) {
			if (resultCode == RESULT_OK) {
				String toUsers = data.getStringExtra("ToFacebookUsers");
				// send facebook app request
				FacebookModel.getFacebookModel().sendAppRequest(BedBuzzAndroidActivity.this,toUsers);
			}
		}
		else if (requestCode == CHANGE_THEME) {
			if (resultCode == RESULT_OK) {
				reload();
			}


		}
		else
		{
			// user has just been facebook authenticated
			FacebookModel.getFacebookModel().facebook.authorizeCallback(requestCode, resultCode, data);
			FacebookModel.getFacebookModel().getMyFacebookID();
			FacebookModel.getFacebookModel().getFriends();
			

			
		}
	}
	
	private Boolean isSameDay(Date date1, Date date2)
	{
		Calendar cal1 = Calendar.getInstance();
		Calendar cal2 = Calendar.getInstance();
		cal1.setTime(date1);
		cal2.setTime(date2);
		return cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR) &&
		cal1.get(Calendar.DAY_OF_YEAR) == cal2.get(Calendar.DAY_OF_YEAR);
	}

	private void setBackgroundImage()
	{
		if (backGroundImage!=null)
		{
			this.releaseBackgroundImage();
		}

		UserModel um = UserModel.getUserModel();
		backGroundImage = (ImageView)findViewById(R.id.backgroundImage); 

		if (um.userSettings.currentThemeImageName.contains("drawable/"))
		{
			int imageResource = getResources().getIdentifier(um.userSettings.currentThemeImageName, null, getPackageName());

			Drawable d = getResources().getDrawable(imageResource);

			if (backGroundImage!=null)
			{
				backGroundImage.setImageDrawable(d);
			}
		}
		else
		{
			BitmapFactory.Options options = new BitmapFactory.Options();
			options.inSampleSize = 2;
			Drawable bmD = BitmapDrawable.createFromPath(um.userSettings.currentThemeImageName);
			if (backGroundImage!=null)
			{
				backGroundImage.setImageDrawable(bmD);
			}
		}
	}

	private void initPopups()
	{
		this.sendMessageQuestionFrag = (View)findViewById(R.id.sendMessageQuestionWizardFrag);
		sendMessageQuestionFrag.setVisibility(View.INVISIBLE);

		this.chooseVoiceQuestionFrag = (View)findViewById(R.id.changeVoiceQuestionFrag);
		chooseVoiceQuestionFrag.setVisibility(View.INVISIBLE);

		this.subscribeQuestionFrag = (View)findViewById(R.id.subscribeQuestionFrag);
		subscribeQuestionFrag.setVisibility(View.INVISIBLE);

		this.reviewQuestionFrag = (View)findViewById(R.id.reviewQuestionFrag);
		reviewQuestionFrag.setVisibility(View.INVISIBLE);

	}

	private void showPopUpIfNeeded()
	{
		if (jumpToMessagesAfterFBLogin)
		{
			return;
		}
		
		View popup = getPopupWizard();

		if (popup!=null)
		{
			popup.setVisibility(View.VISIBLE);
			animateIn(popup);
		}
	}

	private void showOrHideAds()
	{
		UserModel um = UserModel.getUserModel();
		if (adView!=null && !um.userSettings.isPaidUser)
		{

			adView.bringToFront();
			if (!adsLoaded)
			{
				adView.loadAds();
				adsLoaded = true;
			}
		}
		else if (adView!=null && um.userSettings.isPaidUser)
		{
			adView.setVisibility(View.GONE);
		}
	}
	
	@Override
	public void onResume()
	{
		System.gc();

		super.onResume();

		registerReceivers();

		clockFrag.invalidate();
		weatherFrag.invalidate();
		
		WeatherModel wm = WeatherModel.getWeatherModel();
		wm.initalize(this.getApplicationContext(), this);

		ClockModel cm = ClockModel.getClockModel(this.getApplicationContext());
		cm.updateTime(true);

		FacebookModel.getFacebookModel().facebook.extendAccessTokenIfNeeded(getApplicationContext(), null);
		UserModel um = UserModel.getUserModel();

		showOrHideAds();

		if (um.userSettings.isKeepAwakeOn && !wakeLock.isHeld())
		{
			// This will make the screen and power stay on
			wakeLock.acquire();
		}
		else if (!um.userSettings.isKeepAwakeOn && wakeLock.isHeld())
		{
			wakeLock.release();
		}

		if (um.isLoggedOn)
		{
			showPopUpIfNeeded();
		}
	}

	private int getScreenWidth()
	{
		int screenWidth = 0;

		WindowManager wm = (WindowManager) this.getApplicationContext().getSystemService(Context.WINDOW_SERVICE);

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

		return screenWidth;
	}

	private void animateIn(View view)
	{


		int screenWidth = getScreenWidth();

		int isVisiible = view.getVisibility();

		// animate the new fragment in
		Animation animation = new TranslateAnimation(screenWidth, 0,0, 0);
		animation.setDuration(1000);
		animation.setFillAfter(false);
		view.startAnimation(animation);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.main_menu, menu);
		
		if (!UserModel.getUserModel().userSettings.isPaidUser)
		{
			if (android.os.Build.VERSION.SDK_INT >= 11)
			{
				menu.add(0, R.id.subscribeMenuItem, 0, "Upgrade").setIcon(R.drawable.check_40)
				.setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM | MenuItem.SHOW_AS_ACTION_WITH_TEXT);
			}
			else
			{
				menu.add(0, R.id.subscribeMenuItem, 0, "Upgrade").setIcon(R.drawable.check_40);
			}
		}
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle item selection
		switch (item.getItemId()) {
		case R.id.add_alarmMenuItem:
			showAlarmsScreen();
			return true;
		case R.id.sendMessage:
			showMessagesScreen();
			return true;
		case R.id.voice:
			showVoicePickerScreen();
			return true;
		case R.id.themeMenuItem:
			showThemePickerScreen();
			return true;
		case R.id.rssFeedMenuChoice:
			if (UserModel.getUserModel().userSettings.isPaidUser)
			{
				showRSSListScreen();
			}
			else
			{
				showSubscribeScreen();
			}
			return true;
		case R.id.settingMenuChoice:
			showSettingsScreen();
			return true;
		case R.id.subscribeMenuItem:
			showSubscribeScreen();
			return true;
		case android.R.id.home:
			// app icon in action bar clicked; go home
			Intent intent = new Intent(this, BedBuzzAndroidActivity.class);
			intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
			startActivity(intent);
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}

	}

	private void showRSSListScreen() {
		Intent intent = new Intent(BedBuzzAndroidActivity.this, RSSListActivity.class);
		startActivity(intent);

	}

	public void showSubscribeScreen() {
		Intent intent = new Intent(BedBuzzAndroidActivity.this, SubscribeScreenActivity.class);
		startActivity(intent);

	}

	private void showSettingsScreen() {
		Intent intent = new Intent(BedBuzzAndroidActivity.this, SettingsActivity.class);
		startActivity(intent);

	}

	private void showAlarmsScreen() {
		Intent intent = new Intent(BedBuzzAndroidActivity.this, AlarmListView.class);
		startActivity(intent);
	}

	public void showReviewScreen() {
		Intent intent = new Intent(Intent.ACTION_VIEW);
		intent.setData(Uri.parse("market://details?id=com.comantis.bedBuzz"));
		startActivity(intent);
	}
}