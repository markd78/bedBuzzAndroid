package com.comantis.bedBuzz.activities;


import java.util.ArrayList;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.TextView;

import com.comantis.bedBuzz.R;
import com.comantis.bedBuzz.VO.RSSClip;
import com.comantis.bedBuzz.enums.VoiceType;
import com.comantis.bedBuzz.models.RSSModel;
import com.comantis.bedBuzz.models.SoundManager;
import com.comantis.bedBuzz.models.UserModel;
import com.comantis.bedBuzz.service.RSSService;
import com.comantis.bedBuzz.utils.Utilities;
import com.flurry.android.FlurryAgent;

public class RSSDetailView extends Activity {


	private Context context;
	private RadioButton usFemaleRdo;
	private RadioButton usMaleRdo;
	private RadioButton ukFemaleRdo;
	private EditText rssURLTxt;
	private EditText rssNameTxt;
	private Button saveBtn;
	private Button testBtn;
	private TextView plzWaitLbl;
	private ProgressBar plzWaitSpinner;
	private Button cancelBtn;

	public void onStart()
	  {
	     super.onStart();
	     FlurryAgent.onStartSession(this, "AHV3ZMQBPKPRPKH5SH23");
	     // your code
	  }
	  
	  public void onStop()
	  {
	     super.onStop();
	     FlurryAgent.onEndSession(this);
	     // your code
	  }
	
	  @Override 
		public void onResume() {
			super.onResume();
			
			if (!isRSSReceiverRegistered)
			{
				isRSSReceiverRegistered = true;
				mReceiver = new RSSReceiver();
				IntentFilter intentFilter = new IntentFilter(RSSService.RSS_DOWNLOAD_COMPLETE);
				this.registerReceiver(mReceiver, intentFilter);
			}
	  }
	  
	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.rss_detail_view);
		context = this.getApplicationContext();

		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
		    getActionBar().setHomeButtonEnabled(true);
		}

		usFemaleRdo = (RadioButton)findViewById(R.id.usfemaleToggleInFeedBtn); 
		usMaleRdo = (RadioButton)findViewById(R.id.usmaleToggleInFeedBtn); 
		ukFemaleRdo = (RadioButton)findViewById(R.id.ukfemaleToggleInFeedBtn); 
		rssURLTxt = (EditText)findViewById(R.id.feedURLTxt); 
		rssNameTxt = (EditText)findViewById(R.id.feedNameTxt); 
		testBtn = (Button)findViewById(R.id.TestFeedBtn); 
		saveBtn = (Button)findViewById(R.id.SaveFeedBtn); 

		plzWaitSpinner = (ProgressBar)findViewById(R.id.plzWaitSpinner);
		plzWaitLbl = (TextView)findViewById(R.id.plzWaitLbl);

		RSSModel rm = RSSModel.getRSSModell();

		this.rssNameTxt.setText(rm.feedCurrentlyEditing.rssFeedName);
		this.rssURLTxt.setText(rm.feedCurrentlyEditing.rssFeedURL);

		testBtn.setEnabled(false);
		saveBtn.setEnabled(false);


		rssURLTxt.addTextChangedListener(new TextWatcher(){
			public void afterTextChanged(Editable s) {
				if (s.toString().length() > 4)
				{
					testBtn.setEnabled(true);
				}
				else
				{
					testBtn.setEnabled(false);
				}
			}
			public void beforeTextChanged(CharSequence s, int start, int count, int after){}
			public void onTextChanged(CharSequence s, int start, int before, int count){}
		}); 


		saveBtn.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				RSSModel rm = RSSModel.getRSSModell();
				rm.feedCurrentlyEditing.rssFeedName= rssNameTxt.getEditableText().toString();
				rm.feedCurrentlyEditing.rssFeedURL = rssURLTxt.getEditableText().toString();
				
				String voiceNameTxt = "";
				if (usFemaleRdo.isChecked())
				{
					voiceNameTxt =Utilities.getVoiceNameTxtforVoiceType( VoiceType.USFEMALE);
				}
				else if (usMaleRdo.isChecked())
				{
					voiceNameTxt =Utilities.getVoiceNameTxtforVoiceType(VoiceType.USMALE);
				}
				else
				{
					voiceNameTxt =Utilities.getVoiceNameTxtforVoiceType(VoiceType.UKFEMALE);
				}
				
				rm.feedCurrentlyEditing.voiceName = voiceNameTxt;
				
				if (rm.isAddingNewFeed)
				{
					rm.rssFeeds.add(rm.feedCurrentlyEditing);
				}

				rm.saveFeeds(context);
				finish();
			}
		});

		cancelBtn = (Button)findViewById(R.id.CancelSaveFeedBtn);

		if (!rm.isAddingNewFeed)
		{
			cancelBtn.setText("Delete");
		}

		cancelBtn.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				RSSModel rm = RSSModel.getRSSModell();
				if (!rm.isAddingNewFeed)
				{
					rm.rssFeeds.remove(rm.feedCurrentlyEditing);
					rm.saveFeeds(getApplicationContext());
				}
				
				finish();
				

			}
		});


		testBtn.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {

				enablePlzWait();
				
				RSSModel.getRSSModell().rssClips = new ArrayList<RSSClip>();

				rssURLTxt = (EditText)findViewById(R.id.feedURLTxt); 
				String url = rssURLTxt.getText().toString();

				if (usFemaleRdo.isChecked())
				{
					new RSSService(url,Utilities.getVoiceNameTxtforVoiceType(VoiceType.USFEMALE), context).execute();
				}
				else if (usMaleRdo.isChecked())
				{
					new RSSService(url, Utilities.getVoiceNameTxtforVoiceType(VoiceType.USMALE), context).execute();
				}
				else
				{
					new RSSService(url, Utilities.getVoiceNameTxtforVoiceType(VoiceType.UKFEMALE), context).execute();
				}


			}
		});


		disablePlzWait();
		
		if (rssURLTxt.getText().length() > 4)
		{
			testBtn.setEnabled(true);
		}
		else
		{
			testBtn.setEnabled(false);
		}
		
		// save button should only enable when they type something in
		saveBtn.setEnabled(false);
	}

	private void disablePlzWait()
	{
		plzWaitSpinner.setVisibility(View.INVISIBLE);
		plzWaitLbl.setVisibility(View.INVISIBLE);
		cancelBtn.setEnabled(true);
		testBtn.setEnabled(true);
		saveBtn.setEnabled(true);
	}

	private void enablePlzWait()
	{
		plzWaitSpinner.setVisibility(View.VISIBLE);
		plzWaitLbl.setVisibility(View.VISIBLE);
		cancelBtn.setEnabled(false);
		testBtn.setEnabled(false);
		saveBtn.setEnabled(false);
	}


	private RSSReceiver mReceiver ;

	@Override
	public void onPause()
	{
		SoundManager.getSoundModel(getApplicationContext()).stopSounds();
		super.onPause();
		if (isRSSReceiverRegistered)
		{
			this.unregisterReceiver(mReceiver);
		}
		this.isRSSReceiverRegistered = false;
	}

	private Boolean isRSSReceiverRegistered = false;
	private class RSSReceiver extends BroadcastReceiver {     
		@Override
		public void onReceive(Context context, Intent intent) {
			RSSModel rssModel = RSSModel.getRSSModell();
			if (rssModel.rssClips.size() > 0)
			{
				disablePlzWait();
				SoundManager sm = SoundManager.getSoundModel(getApplicationContext());
				sm.playRSSClips();
				saveBtn.setEnabled(true);
			}
		}
	}

private static final int UPGRADE_MENU_ITEM = Menu.FIRST;
	
	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {
		menu.clear();
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
		return super.onPrepareOptionsMenu(menu);
	}
	
	private void showSubscribeScreen() {
		Intent intent = new Intent(RSSDetailView.this, SubscribeScreenActivity.class);
		startActivity(intent);

	}
	
	 @Override
		public boolean onOptionsItemSelected(MenuItem item) {
			// Handle item selection
			switch (item.getItemId()) {
			 case android.R.id.home:
		            // app icon in action bar clicked; go home
		            Intent intent = new Intent(this, BedBuzzAndroidActivity.class);
		            intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT | Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_SINGLE_TOP);
		    		startActivity(intent);
		            return true;
			 case R.id.subscribeMenuItem:
					showSubscribeScreen();
					return true;
			default:
				return super.onOptionsItemSelected(item);
			}
	 }
}
