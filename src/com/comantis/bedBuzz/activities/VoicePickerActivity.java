package com.comantis.bedBuzz.activities;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;

import com.comantis.bedBuzz.R;
import com.comantis.bedBuzz.enums.VoiceType;
import com.comantis.bedBuzz.models.SoundManager;
import com.comantis.bedBuzz.models.UserModel;
import com.comantis.bedBuzz.service.DownloadFile;
import com.comantis.bedBuzz.service.iSpeechService;
import com.flurry.android.FlurryAgent;

public class VoicePickerActivity extends Activity {

	VoiceType selectedVoice;
	EditText nameTxt;
	EditText messageTxt;
	RadioButton usFemaleRdo;
	RadioButton usMaleRdo;
	RadioButton ukFemaleRdo;
	int receiveCount;
	Context context;
	Button saveBtn;
	Button testBtn;
	
	TextView plzWaitLbl;
	ProgressBar plzWaitSpinner;
	
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
	  
	  private void registerReceivers()
	  {
		  if (!fileReceiverRegistered)
		  {
			  mReceiver = new FileReceiver();
			IntentFilter intentFilter = new IntentFilter(DownloadFile.FILE_DOWNLOADED);
			this.registerReceiver(mReceiver, intentFilter);
			this.fileReceiverRegistered = true;
		  }
	  }
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		this.context = this.getApplicationContext();

		registerReceivers();
		
		
		setContentView(R.layout.voice_picker_view);

		UserModel um = UserModel.getUserModel();

		nameTxt = (EditText)findViewById(R.id.nameInVoiceMenuTxt); 
		nameTxt.setText(um.userSettings.userFullName);

		testBtn = (Button)findViewById(R.id.TestVoiceBtn); 
		testBtn.setOnClickListener(testBtnClick); 

		saveBtn = (Button)findViewById(R.id.SaveVoiceBtn); 
		saveBtn.setOnClickListener(saveBtnClick); 

		messageTxt = (EditText)findViewById(R.id.additionalMessageInVoiceMenuTxt); 
		messageTxt.setText(um.userSettings.additionalMessage);
		
		usFemaleRdo = (RadioButton)findViewById(R.id.usfemaleToggleInVoiceMenuBtn); 
		usMaleRdo = (RadioButton)findViewById(R.id.usmaleToggleInVoiceMenuBtn); 
		ukFemaleRdo = (RadioButton)findViewById(R.id.ukfemaleToggleInVoiceMenuBtn); 
		
		plzWaitSpinner = (ProgressBar)findViewById(R.id.plzWaitSpinner);
		plzWaitLbl = (TextView)findViewById(R.id.plzWaitLbl);
		
		if (um.userSettings.voiceName.equals("usenglishfemale1"))
		{
			 usFemaleRdo.setChecked(true);
			 
		}
		else if (um.userSettings.voiceName.equals("usenglishmale1"))
		{
			 usMaleRdo.setChecked(true);
		}
		else
		{
			ukFemaleRdo.setChecked(true);
		}

		disablePlzWait();
		
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
		    getActionBar().setHomeButtonEnabled(true);
		}
	}

	private void disablePlzWait()
	{
		plzWaitSpinner.setVisibility(View.INVISIBLE);
		plzWaitLbl.setVisibility(View.INVISIBLE);
		testBtn.setEnabled(true);
		saveBtn.setEnabled(true);
	}
	
	private void enablePlzWait()
	{
		plzWaitSpinner.setVisibility(View.VISIBLE);
		plzWaitLbl.setVisibility(View.VISIBLE);
		testBtn.setEnabled(false);
		saveBtn.setEnabled(false);
	}
	
	public void onResume()
	 {
		 super.onResume();
		 
		 registerReceivers();
		 
		 UserModel um = UserModel.getUserModel();
		 if (um.userSettings.changeVoiceNameCredits == 0 &&
					!um.userSettings.isPaidUser)
			{
				saveBtn.setText("Save / Buy Credit");
				saveBtn.setOnClickListener(buyBtnClick); 
			}
		 else
		 {
			 saveBtn.setText("Save");
			 saveBtn.setOnClickListener(saveBtnClick); 
		 }
		 
		 disablePlzWait();
	 }
	
	public void onPause()
	{
		super.onPause();
		if(this.fileReceiverRegistered)
		{
			this.unregisterReceiver(mReceiver);
			fileReceiverRegistered = false;
		}
	}
	
	private void generateFiles(Boolean forTest, VoiceType voiceType)
	{
		enablePlzWait();
		
		receiveCount = 0;
		String morningFileName;
		String afternoonFileName;
		String eveningFileName;
		String messageFileName;
		
		// generate the files
		if (forTest)
		{
			morningFileName ="goodMorningTest.mp3";
			afternoonFileName = "goodAfternoonTest.mp3";
			eveningFileName = "goodEveningTest.mp3";
			messageFileName = "additionalMessageTest.mp3";
				
		}
		else
		{
			morningFileName ="goodMorning.mp3";
			afternoonFileName = "goodAfternoon.mp3";
			eveningFileName = "goodEvening.mp3";
			messageFileName = "additionalMessage.mp3";
		}
		
			new iSpeechService().getSound(morningFileName, "Good Morning "+nameTxt.getText().toString(), voiceType, context);
			new iSpeechService().getSound(afternoonFileName, "Good Afternoon "+nameTxt.getText().toString(), voiceType, context);
			new iSpeechService().getSound(eveningFileName, "Good Evening "+nameTxt.getText().toString(), voiceType, context);
	
			if (messageTxt.getText().toString().length()>0)
			{
				new iSpeechService().getSound(messageFileName, messageTxt.getText().toString(), voiceType, context);
			}
			else
			{
				receiveCount++;
			}
		
	}

	private OnClickListener testBtnClick = new OnClickListener() {

		@Override
		public void onClick(View v) {
			if (usFemaleRdo.isChecked())
			{
				generateFiles(true,VoiceType.USFEMALE);
			}
			else if (usMaleRdo.isChecked())
			{
				generateFiles(true,VoiceType.USMALE);
			}
			else
			{
				generateFiles(true,VoiceType.UKFEMALE);
			}


		}



	};

private OnClickListener buyBtnClick = new OnClickListener() {
		
		@Override
		public void onClick(View v) {

		Intent intent = new Intent(VoicePickerActivity.this, BuyorSubscribeActivity.class);
		intent.putExtra("purchaseID", "com.comantis.bedbuzz.changevoicecredit");
	  	   startActivity(intent);
		}
	};
	
	private OnClickListener saveBtnClick = new OnClickListener() {

		@Override
		public void onClick(View v) {

			UserModel um = UserModel.getUserModel();

			if (usFemaleRdo.isChecked())
			{
				generateFiles(false,VoiceType.USFEMALE);
				um.userSettings.voiceName = "usenglishfemale1";
			}
			else if (usMaleRdo.isChecked())
			{
				generateFiles(false,VoiceType.USMALE);
				um.userSettings.voiceName = "usenglishmale1";
			}
			else
			{
				generateFiles(false,VoiceType.UKFEMALE);
				um.userSettings.voiceName = "ukenglishfemale1";
			}

			um.userSettings.userFullName = nameTxt.getText().toString();
			um.userSettings.additionalMessage = messageTxt.getText().toString();
			
			if (!um.userSettings.isPaidUser)
			{
				um.userSettings.changeVoiceNameCredits --;
			}
			
			if (um.userSettings.changeVoiceNameCredits < 0)
			{
				um.userSettings.changeVoiceNameCredits = 0;
			}
			
			um.saveUserSettings(context);

			Toast toast = Toast.makeText(context, "Voice settings saved!", Toast.LENGTH_SHORT);
			toast.show();
			
			finish();
		}
	};

	private FileReceiver mReceiver ;
	private Boolean fileReceiverRegistered = false;
	
	private class FileReceiver extends BroadcastReceiver {     
		@Override
		public void onReceive(Context context, Intent intent) {
			String fileName = intent.getStringExtra("filename");
			if (fileName.indexOf("Test")!=-1)
			{
				receiveCount++;
				if (receiveCount == 4)
				{
					disablePlzWait();
					
					// play the test message
					SoundManager sm = SoundManager.getSoundModel(context);
					sm.playTest();
					
					UserModel.getUserModel().saveUserSettings(getApplicationContext());
				}
			}
		}
	}
	
	
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
		Intent intent = new Intent(VoicePickerActivity.this, SubscribeScreenActivity.class);
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
