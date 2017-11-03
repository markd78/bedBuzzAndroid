package com.comantis.bedBuzz.fragments;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.comantis.bedBuzz.R;
import com.comantis.bedBuzz.activities.Welcome_FirstScreenActivityWithWizard;
import com.comantis.bedBuzz.enums.VoiceType;
import com.comantis.bedBuzz.models.UserModel;
import com.comantis.bedBuzz.service.DownloadFile;
import com.comantis.bedBuzz.service.iSpeechService;
import com.comantis.bedBuzz.utils.FlurryEvents;
import com.flurry.android.FlurryAgent;

public class WelcomeEnterNameFragment extends Fragment {

	Button nextBtn;
	Context context;
	private int receiveCount = 0;
	private Welcome_FirstScreenActivityWithWizard parentActivity;
	ProgressBar plzWaitSpinner;
	TextView plzWaitTxt;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View cf  =  inflater.inflate(R.layout.welcome_wizard_enter_name_fragment, container, false);
		FlurryAgent.logEvent(FlurryEvents.ENTER_NAME_WIZARD);
		this.parentActivity = (Welcome_FirstScreenActivityWithWizard) this.getActivity();


		Window window = parentActivity.getWindow();
		window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);

		context = parentActivity.getApplicationContext();
		

		nextBtn = (Button)cf.findViewById(R.id.nextBtn_name); 
		nextBtn.setEnabled(false);

		EditText nameTxt = (EditText)cf.findViewById(R.id.nameTxt);
		nameTxt.addTextChangedListener(onNameTxtChanged);
		
		 plzWaitSpinner = (ProgressBar)cf.findViewById(R.id.plzWaitProgressBar);
		 plzWaitTxt = (TextView)cf.findViewById(R.id.plzWaitTxt);

		 plzWaitSpinner.setVisibility(View.INVISIBLE);
		 plzWaitTxt.setVisibility(View.INVISIBLE);
		 
		nextBtn.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				UserModel um = UserModel.getUserModel();

				mReceiver = new FileReceiver();
				IntentFilter intentFilter = new IntentFilter(DownloadFile.FILE_DOWNLOADED);
				parentActivity.registerReceiver(mReceiver, intentFilter);
				
				// save the name
				um.saveUserSettings(context);
				
				 plzWaitSpinner.setVisibility(View.VISIBLE);
				 plzWaitTxt.setVisibility(View.VISIBLE);
				 nextBtn.setEnabled(false);

				// generate the files
				new iSpeechService().getSound("goodMorning.mp3", "Good Morning "+um.userSettings.userFullName, VoiceType.UKFEMALE, context);
				new iSpeechService().getSound("goodAfternoon.mp3", "Good Afternoon "+um.userSettings.userFullName, VoiceType.UKFEMALE, context);
				new iSpeechService().getSound("goodEvening.mp3", "Good Evening "+um.userSettings.userFullName, VoiceType.UKFEMALE, context);
			}
		});

		receiveCount = 0;
		
		return cf;
	}

	private FileReceiver mReceiver ;

	private class FileReceiver extends BroadcastReceiver {     
		@Override
		public void onReceive(Context context, Intent intent) {
			String fileName = intent.getStringExtra("filename");
			receiveCount++;
			if (receiveCount == 3)
			{
				 plzWaitSpinner.setVisibility(View.INVISIBLE);
				 plzWaitTxt.setVisibility(View.INVISIBLE);
				parentActivity.flipToNext();
			}
		}
	}

	@Override
	public void onPause() {

		if (mReceiver != null){
			parentActivity.unregisterReceiver(mReceiver);
			mReceiver = null;
		}

		super.onPause();
	}

	private TextWatcher onNameTxtChanged = new TextWatcher(){
		public void afterTextChanged(Editable s) {
			UserModel.getUserModel().userSettings.userFullName = s.toString();

			if (s.length() > 0 )
			{
				nextBtn.setEnabled(true);
			}
			else
			{
				nextBtn.setEnabled(false);
			}


		}

		@Override
		public void beforeTextChanged(CharSequence arg0, int arg1, int arg2,
				int arg3) {
			// TODO Auto-generated method stub

		}

		@Override
		public void onTextChanged(CharSequence arg0, int arg1, int arg2,
				int arg3) {
			// TODO Auto-generated method stub

		}


	};
}
