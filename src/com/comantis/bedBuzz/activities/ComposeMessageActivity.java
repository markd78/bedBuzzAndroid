package com.comantis.bedBuzz.activities;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.media.MediaRecorder;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Base64;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ViewFlipper;

import com.comantis.bedBuzz.R;
import com.comantis.bedBuzz.VO.FriendVO;
import com.comantis.bedBuzz.VO.MessageVO;
import com.comantis.bedBuzz.enums.VoiceType;
import com.comantis.bedBuzz.models.FacebookModel;
import com.comantis.bedBuzz.models.SoundManager;
import com.comantis.bedBuzz.models.UserModel;
import com.comantis.bedBuzz.service.DownloadFile;
import com.comantis.bedBuzz.service.SendMessageService;
import com.comantis.bedBuzz.service.iSpeechService;
import com.flurry.android.FlurryAgent;
import com.wilson.android.library.DrawableManager;

public class ComposeMessageActivity extends Activity {

	DrawableManager dm;
	private boolean receiverRegistered = false;

	private boolean isRecording = false;

	private Timer updateMicLevelTimer;

	private FileReceiver mReceiver ;
	private CountDownPlayedReceiver countdownPlayedReceiver;
	//Context context;
	VoiceType selectedVoice;
	EditText messageTxt;
	RadioButton usFemaleRdo;
	RadioButton usMaleRdo;
	RadioButton ukFemaleRdo;
	Boolean areSomeFriendsNotBedBuzzUsers = false;
	Button sendBtn;
	LinearLayout messageCreditsLayout;
	TextView numberOfCreditsTxt;
	Button testBtn;
	ViewFlipper messageTypeViewFlipper;
	RadioButton fromMicMessageTypeRdo;
	RadioButton fromTTSMessageTypeRdo;
	ProgressBar micLevelProgBar;
	TextView micLevelLbl;
	RadioGroup messageTypeRadioGroup;
	EditText displayedMessageTxt;
	TextView recordingProgessLbl;

	public static String messageFromMicFileName = Environment.getExternalStorageDirectory() +"/bedBuzz/messageFromMic.mp4";

	Button recordBtn;

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
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.compose_message);
		//context = this.getApplicationContext();

		dm = new DrawableManager();

		mReceiver = new FileReceiver();

		countdownPlayedReceiver = new CountDownPlayedReceiver();
		
		receiverRegistered = true;
		IntentFilter intentFilter = new IntentFilter(DownloadFile.FILE_DOWNLOADED);
		this.registerReceiver(mReceiver, intentFilter);
		
		IntentFilter countDownIntentFilter = new IntentFilter(SoundManager.COUNTDOWN_NUMBER_PLAYED);
		this.registerReceiver(countdownPlayedReceiver, countDownIntentFilter);

		testBtn = (Button)findViewById(R.id.TestMessageBtn); 
		testBtn.setOnClickListener(testBtnClick); 

		sendBtn = (Button)findViewById(R.id.SendMessageBtn); 
		sendBtn.setOnClickListener(sendBtnClick); 

		numberOfCreditsTxt = (TextView)findViewById(R.id.messageCountLbl); 

		messageCreditsLayout = (LinearLayout)findViewById(R.id.messageCreditsLayout); 

		messageTxt = (EditText)findViewById(R.id.messageTxt); 
		
		micLevelProgBar = (ProgressBar)findViewById(R.id.micLevelProgressBar);

		messageTypeViewFlipper = (ViewFlipper)findViewById(R.id.messageTypeViewFlipper); 

		usFemaleRdo = (RadioButton)findViewById(R.id.usfemaleToggleBtn); 
		usMaleRdo = (RadioButton)findViewById(R.id.usmaleToggleBtn); 
		ukFemaleRdo = (RadioButton)findViewById(R.id.ukfemaleToggleBtn); 
		
		recordingProgessLbl = (TextView)findViewById(R.id.recordingLbl); 

		fromMicMessageTypeRdo  = (RadioButton)findViewById(R.id.fromMicMessageTypeBtn); 
		fromTTSMessageTypeRdo = (RadioButton)findViewById(R.id.fromTTSMessageTypeBtn); 
		this.displayedMessageTxt = (EditText)findViewById(R.id.displayedMessageTxt);
		messageTypeRadioGroup = (RadioGroup)findViewById(R.id.messageTypeRdoGroup);

		micLevelLbl = (TextView)findViewById(R.id.micLevelLbl);
		
		recordBtn = (Button)findViewById(R.id.recordBtn);

		recordBtn.setOnClickListener(recordBtnClick);


		messageTypeRadioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener()
		{
			public void onCheckedChanged(RadioGroup rGroup, int checkedId)
			{
				if (fromMicMessageTypeRdo.isChecked())
				{
					messageTypeViewFlipper.setDisplayedChild(0);
				}
				else
				{
					messageTypeViewFlipper.setDisplayedChild(1);
				}
			}
		});

		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
			getActionBar().setHomeButtonEnabled(true);
		}

		messageTxt.addTextChangedListener(new TextWatcher(){
			public void afterTextChanged(Editable s) {
				toggleTestSendButtons();
			}
			public void beforeTextChanged(CharSequence s, int start, int count, int after){}
			public void onTextChanged(CharSequence s, int start, int before, int count){}
		}); 

		// add the target friends to the 'to' list
		addToTargetList();

		toggleTestSendButtons();

		UserModel um = UserModel.getUserModel();
		if (!um.userSettings.hasPlayedComposeMessageHelp)
		{
			SoundManager.getSoundModel(this.getApplicationContext()).playComposeMessageHelp();
			um.userSettings.hasPlayedComposeMessageHelp = true;
			um.saveUserSettings(this.getApplicationContext());
		}

	}

	private void toggleTestSendButtons()
	{
		if (messageTxt.getText().length() >0 )
		{
			sendBtn.setEnabled(true);
			testBtn.setEnabled(true);
		}
		else
		{
			sendBtn.setEnabled(false);
			testBtn.setEnabled(false);
		}
	}

	private void showOrHideSendCreditLayout()
	{
		UserModel um = UserModel.getUserModel();
		if (um.userSettings.isPaidUser)
		{
			messageCreditsLayout.setVisibility(View.INVISIBLE);
		}
		else
		{
			messageCreditsLayout.setVisibility(View.VISIBLE);

			if (um.userSettings.sendMessageCredits < 0)
			{
				um.userSettings.sendMessageCredits = 0;
				um.saveUserSettings(this.getApplicationContext());
			}

			if (um.userSettings.sendMessageCredits == 0)
			{
				numberOfCreditsTxt.setTextColor(Color.RED);
			}
			else
			{
				numberOfCreditsTxt.setTextColor(Color.WHITE);
			}

			String creditsStr = " "+Integer.toString(um.userSettings.sendMessageCredits);
			numberOfCreditsTxt.setText(creditsStr);
		}
	}

	public void onResume()
	{
		super.onResume();

		toggleTestSendButtons();

		recordingProgessLbl.setVisibility(View.INVISIBLE);
		this.micLevelProgBar.setVisibility(View.INVISIBLE);
		this.micLevelLbl.setVisibility(View.INVISIBLE);
		
		if (!receiverRegistered)
		{
			IntentFilter intentFilter = new IntentFilter(DownloadFile.FILE_DOWNLOADED);

			this.registerReceiver(mReceiver, intentFilter);
			
			IntentFilter countDownIntentFilter = new IntentFilter(SoundManager.COUNTDOWN_NUMBER_PLAYED);
			this.registerReceiver(countdownPlayedReceiver, countDownIntentFilter);
		}

		for (FriendVO friend : FacebookModel.getFacebookModel().targetFriends )
		{
			if (!friend.isBedBuzzUser)
			{
				this.areSomeFriendsNotBedBuzzUsers = true;
				break;
			}
		}

		TextView infoTxt = (TextView)findViewById(R.id.infoTxt); 


		if (areSomeFriendsNotBedBuzzUsers)
		{
			infoTxt.setText("Some of your selected friends don't have BedBuzz. After pressing Send, a dialog will appear allowing you to send them an invite.");
		}

		UserModel um = UserModel.getUserModel();
		if (um.userSettings.sendMessageCredits == 0 &&
				!um.userSettings.isPaidUser)
		{
			sendBtn.setText("Buy Credit");
			sendBtn.setOnClickListener(buyBtnClick); 
		}
		else
		{
			sendBtn.setText("Send");
			sendBtn.setOnClickListener(sendBtnClick); 
		}

		showOrHideSendCreditLayout();
	}

	private OnClickListener recordBtnClick = new OnClickListener() {

		@Override
		public void onClick(View v) {

			if (!isRecording)
			{
				
				testBtn.setEnabled(false);
				sendBtn.setEnabled(false);
				recordBtn.setText("Stop");
				isRecording = true;
				startCountdown();
			}
			else
			{
				recordBtn.setText("Re-record");
				isRecording = false;
				stopRecording();
				testBtn.setEnabled(true);
				sendBtn.setEnabled(true);
			}

		}

		private void startCountdown() {
			recordingCountDownNumber = 3;
			recordingProgessLbl.setText("Recording in "+(recordingCountDownNumber)+"...");
			recordingProgessLbl.setVisibility(View.VISIBLE);
			SoundManager.getSoundModel(getApplicationContext()).playCountdown();
			
		}
	};

	private MediaRecorder recorder;

	public double getAmplitude() {
		if (recorder != null)
			return  (recorder.getMaxAmplitude()/2700.0);
		else
			return 0;

	}

	public void updateLevels() 
	{
		micLevelProgBar.setMax(40000);
		updateMicLevelTimer = new Timer();
		updateMicLevelTimer.schedule(new TimerTask() {			
			@Override
			public void run() {
				UpdateMicLevelTimer();
			}

		}, 0, 100);
	}

	private void UpdateMicLevelTimer()
	{
		//This method is called directly by the timer
		//and runs in the same thread as the timer.

		//We call the method that will work with the UI
		//through the runOnUiThread method.
		this.runOnUiThread(UpdateMicLevelTimer_Tick);
	}


	private Runnable UpdateMicLevelTimer_Tick = new Runnable() {
		public void run() {

			//This method runs in the same thread as the UI.    	       
			if (recorder!=null)
			{
				int recorderLevel = recorder.getMaxAmplitude();
			
				Log.d("recorderlevel","recorder level = "+recorderLevel);
				
				micLevelProgBar.setProgress(recorderLevel);
			}
		}
	};

	private void startRecording() {
		recorder = new MediaRecorder();

		recorder.setAudioSource(MediaRecorder.AudioSource.MIC);

		


		recorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);
		recorder.setAudioEncoder(MediaRecorder.AudioEncoder.AAC);
		recorder.setOutputFile(messageFromMicFileName);
		recorder.setOnErrorListener(errorListener);
		recorder.setOnInfoListener(infoListener);
		try {
			recorder.prepare();
			recorder.start();
			
			updateLevels();
			
		} catch (IllegalStateException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private void stopRecording() {
		if (null != recorder) {
			recorder.stop();
			recorder.reset();
			recorder.release();
			recorder = null;
			
			recordingProgessLbl.setVisibility(View.INVISIBLE);
			this.micLevelProgBar.setVisibility(View.INVISIBLE);
			this.micLevelLbl.setVisibility(View.INVISIBLE);
			updateMicLevelTimer.cancel();
		}
	}

	private MediaRecorder.OnErrorListener errorListener = new MediaRecorder.OnErrorListener() {
		@Override
		public void onError(MediaRecorder mr, int what, int extra) {
			Toast.makeText(ComposeMessageActivity.this, "Error: " + what + ", " + extra, Toast.LENGTH_SHORT).show();
		}
	};

	private MediaRecorder.OnInfoListener infoListener = new MediaRecorder.OnInfoListener() {
		@Override
		public void onInfo(MediaRecorder mr, int what, int extra) {
			Toast.makeText(ComposeMessageActivity.this, "Warning: " + what + ", " + extra, Toast.LENGTH_SHORT).show();
		}
	};

	private OnClickListener buyBtnClick = new OnClickListener() {

		@Override
		public void onClick(View v) {

			// receiver gets deregistered for some reason here
			receiverRegistered = false;

			Intent intent = new Intent(ComposeMessageActivity.this, BuyorSubscribeActivity.class);
			intent.putExtra("purchaseID", "com.comantis.bedbuzz.5sendmessagecredits");
			startActivity(intent);
		}
	};

	private OnClickListener testBtnClick = new OnClickListener() {



		@Override
		public void onClick(View v) {

			UserModel um = UserModel.getUserModel();

			if (fromTTSMessageTypeRdo.isChecked())
			{
				if (usFemaleRdo.isChecked())
				{
					new iSpeechService().getSound("testMessage.mp3", "message from "+um.userSettings.userFullName +".  " + messageTxt.getText(), VoiceType.USFEMALE, v.getContext());
				}
				else if (ukFemaleRdo.isChecked())
				{
					new iSpeechService().getSound("testMessage.mp3", "message from "+um.userSettings.userFullName +".  " + messageTxt.getText(), VoiceType.UKFEMALE, v.getContext());
				}
				else if (usMaleRdo.isChecked())
				{
					new iSpeechService().getSound("testMessage.mp3", "message from "+um.userSettings.userFullName +".  " + messageTxt.getText(), VoiceType.USMALE, v.getContext());
				}
			}
			else
			{
				SoundManager.getSoundModel(getApplicationContext()).playMessageFromMic();
			}

		}



	};

	public byte[] inputStreamToByteArray(InputStream inStream) throws IOException {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		byte[] buffer = new byte[8192];
		int bytesRead;
		while ((bytesRead = inStream.read(buffer)) > 0) {
			baos.write(buffer, 0, bytesRead);
		}
		return baos.toByteArray();
	}

	private OnClickListener sendBtnClick = new OnClickListener() {

		@Override
		public void onClick(View v) {

			UserModel um = UserModel.getUserModel();

			MessageVO message = new MessageVO();

			if (fromMicMessageTypeRdo.isChecked())
			{
				File temp = new File(messageFromMicFileName);

				FileInputStream fis = null;
				try {
					fis = new FileInputStream(temp);
				} catch (FileNotFoundException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

				try {
					message.base64Str = Base64.encodeToString(inputStreamToByteArray(fis),Base64.DEFAULT);
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

				message.messageText = displayedMessageTxt.getText().toString();
			}
			else
			{
				message.base64Str = null;
				message.messageText = "message from "+um.userSettings.userFullName +".  " + messageTxt.getText();

				if (usFemaleRdo.isChecked())
				{
					message.voiceName = "usenglishfemale1";
				}
				else if (ukFemaleRdo.isChecked())
				{
					message.voiceName = "ukenglishfemale1";

				}
				else if (usMaleRdo.isChecked())
				{
					message.voiceName = "usenglishmale1";
				}


			}

			message.senderID = um.userSettings.facebookID;

			message.targets = new ArrayList<Long>();
			String toUsersStr = "";

			FacebookModel fm = FacebookModel.getFacebookModel();
			for (FriendVO friend : fm.targetFriends )
			{
				message.targets.add(friend.uid);
				toUsersStr += friend.uid +",";
			}

			if (toUsersStr.length() > 0)
			{
				toUsersStr = toUsersStr.substring(0, toUsersStr.length()-1);
			}

			//new MessagingService().sendMessage(message );

			new SendMessageService(message).execute();

			um.userSettings.sendMessageCredits--;
			um.saveUserSettings(v.getContext());

			Intent intent=new Intent();
			intent.putExtra("ToFacebookUsers",  toUsersStr);
			intent.putExtra("frictionless",  "1");

			if (getParent() == null) {
				setResult(Activity.RESULT_OK, intent);
			}
			else {
				getParent().setResult(Activity.RESULT_OK, intent);
			}
			finish();

			// end this activity
			//finish();
		}



	};
	public int recordingCountDownNumber =3;

	public void onPause()
	{
		super.onPause();
		if(this.receiverRegistered)
		{
			this.unregisterReceiver(mReceiver);
			this.unregisterReceiver(countdownPlayedReceiver);
		}
	}


	private class FileReceiver extends BroadcastReceiver {     
		@Override
		public void onReceive(Context context, Intent intent) {
			String fileName = intent.getStringExtra("filename");

			// play the file
			if (fileName.contentEquals("testMessage.mp3"))
			{
				SoundManager.getSoundModel(context).playTestMessage(fileName);
			}
		}
	}
	
	private class CountDownPlayedReceiver extends BroadcastReceiver {     
		@Override
		public void onReceive(Context context, Intent intent) {
			
			recordingProgessLbl.setText("Recording in "+(recordingCountDownNumber-1)+"...");
			    
			    if (recordingCountDownNumber == 1)
			    {
			    	recordingProgessLbl.setText("Recording in progress!");
			    	micLevelProgBar.setVisibility(View.VISIBLE);
			    	micLevelLbl.setVisibility(View.VISIBLE);
			       startRecording();
			    }
			    recordingCountDownNumber--;
			
		}
	}

	public void addToTargetList() {
		FacebookModel fm = FacebookModel.getFacebookModel();
		for (FriendVO friend : fm.targetFriends )
		{
			ImageView image = new ImageView(this);
			LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(100, 100);
			image.setLayoutParams(layoutParams);
			
			dm.fetchDrawableOnThread(friend.picSmallURL, image);
			LinearLayout horizontalScrollView = (LinearLayout)findViewById(R.id.target_friends_list);
			
			horizontalScrollView.addView(image);
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
		Intent intent = new Intent(ComposeMessageActivity.this, SubscribeScreenActivity.class);
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
