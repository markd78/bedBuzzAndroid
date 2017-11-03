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
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.comantis.bedBuzz.R;
import com.comantis.bedBuzz.models.SoundManager;
import com.comantis.bedBuzz.models.UserModel;
import com.comantis.bedBuzz.service.ReviewCodeService;
import com.flurry.android.FlurryAgent;

public class SettingsActivity extends Activity implements RadioButton.OnCheckedChangeListener {

	 RadioButton useCelciusRdo;
	 RadioButton useFarenheightRdo;
	 TextView userIDTxt;
	 Spinner spinner;
	 EditText reviewCodeTxt;
	 Context context;
	 Button reviewCodeBtn;
	 ProgressBar reviewCodeWaitSpinner;
	
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
		  if (!receiverRegistered)
		  {
			  mReceiver = new ReviewCodeReceiver();

			receiverRegistered = true;
			IntentFilter intentFilter = new IntentFilter(ReviewCodeService.REVIEW_CODE_SUCCESS_OR_FAIL);
			this.registerReceiver(mReceiver, intentFilter);
		  }
	  }
	 

	  @Override 
		public void onResume() {
		  
		  registerReceivers();
		  
			super.onResume();
			
	  }
	  
	@Override
	public void onCreate(Bundle savedInstanceState) {
	  super.onCreate(savedInstanceState);
	  setContentView(R.layout.settings_view);
	  
	  this.context  = this.getApplicationContext();
	  
	  registerReceivers();
	  
	  useCelciusRdo = (RadioButton)findViewById(R.id.celciusRdo); 
	  useFarenheightRdo = (RadioButton)findViewById(R.id.fahrenheitRdo); 
	  userIDTxt = (TextView)findViewById(R.id.userIDTxt);
	  UserModel um = UserModel.getUserModel();
	  useCelciusRdo.setChecked(um.userSettings.isCelcius);
	  useFarenheightRdo.setChecked(!um.userSettings.isCelcius);
	  
	  useCelciusRdo.setOnCheckedChangeListener(this);
	  
	  userIDTxt.setText(Long.toString(um.userSettings.bedBuzzID));
	  
	  ToggleButton useWakeLock = (ToggleButton)findViewById(R.id.useWakeLockTgl); 
	  
	  reviewCodeWaitSpinner = (ProgressBar)findViewById(R.id.reviewCodeWaitSpinner); 
	  reviewCodeWaitSpinner.setVisibility(View.INVISIBLE);
	  
	  
	  reviewCodeTxt = (EditText)findViewById(R.id.reviewCodeTxt);
	  
	   reviewCodeBtn = (Button)findViewById(R.id.submitReviewCodeBtn); 
	  reviewCodeBtn.setOnClickListener(new OnClickListener() {     
		  
		  

		  @Override
		  public void onClick(View v) 
		  {
			  reviewCodeWaitSpinner.setVisibility(View.VISIBLE);
			  reviewCodeBtn.setVisibility(View.INVISIBLE);
			  UserModel um = UserModel.getUserModel();
		     new ReviewCodeService(new Long(um.userSettings.bedBuzzID), reviewCodeTxt.getText().toString(), context).execute();
		  }    
		});
	  
	  useWakeLock.setChecked(um.userSettings.isKeepAwakeOn);
	  
	  useWakeLock.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
		    @Override
		    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
		        // Save the state here
		    	 UserModel um = UserModel.getUserModel();
		    	 um.userSettings.isKeepAwakeOn = isChecked;
		    	 um.saveUserSettings(getApplicationContext());
		    }
		});
	  
	  ToggleButton use24Hr = (ToggleButton)findViewById(R.id.use24HrTgl); 
	  
	  use24Hr.setChecked(um.userSettings.is24HrMode);
	  
	  use24Hr.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
		    @Override
		    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
		        // Save the state here
		    	 UserModel um = UserModel.getUserModel();
		    	 um.userSettings.is24HrMode = isChecked;
		    	 um.saveUserSettings(getApplicationContext());
		    }
		});
	  
	   spinner = (Spinner) findViewById(R.id.snoozeLengthDropDown);
	    ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(
	            this, R.array.snooze_length_array, android.R.layout.simple_spinner_item);
	    adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
	    spinner.setAdapter(adapter);
	    spinner.setOnItemSelectedListener(new SnoozeLengthItemSelectedListener());
	    selectSnoozeDropDownValue();
	    
	    String snoozeDropDownPrompt = um.userSettings.snoozeLength + "minutes";
	    spinner.setPrompt(snoozeDropDownPrompt);
	    
	    TextView expiresLbl = (TextView)findViewById(R.id.expiresLbl); 
	    TextView expiresDateLbl = (TextView)findViewById(R.id.expiresDateLbl); 
	    
	    if (um.userSettings.isPaidUser && um.userSettings.subscriberUntilDate !=null)
	    {
	    	expiresDateLbl.setText(um.userSettings.subscriberUntilDate.toString());
	    }
	    else
	    {
	    	expiresLbl.setVisibility(View.GONE);
	    	expiresDateLbl.setVisibility(View.GONE);
	    }
	    
	    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
	        getActionBar().setHomeButtonEnabled(true);
	    }
	}
	
	private boolean receiverRegistered = false;
	private ReviewCodeReceiver mReceiver ;
	
	@Override 
	public void onPause()
	{
		try {
		
			if (receiverRegistered)
			{
				this.unregisterReceiver(mReceiver);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		                       
		super.onPause();

	}
	
	private void selectSnoozeDropDownValue()
	{
		UserModel um = UserModel.getUserModel();
		int pos = 0;
        switch (um.userSettings.snoozeLength)
        {
        case 3:
        	pos = 0;
        	break;
        case 4:
        	pos = 1;
        	break;
        case 5:
        	pos = 2;
        	break;
        case 6:
        	pos = 3;
        	break;
        case 7:
        	pos = 4;
        	break;
        case 8:
        	pos = 5;
        	break;
        case 9:
        	pos = 6;
        	break;
        case 10:
        	pos = 7;
        	break;
       default:
    	   break;
        }
        spinner.setSelection(pos);
        String snoozeDropDownPrompt = um.userSettings.snoozeLength + "minutes";
	    spinner.setPrompt(snoozeDropDownPrompt);
	}
	
	public class SnoozeLengthItemSelectedListener implements OnItemSelectedListener {

	    public void onItemSelected(AdapterView<?> parent,
	        View view, int pos, long id) {
	      
	      	UserModel um = UserModel.getUserModel();
	        switch (pos)
	        {
	        case 0:
	        	um.userSettings.snoozeLength = 3;
	        	break;
	        case 1:
	        	um.userSettings.snoozeLength = 4;
	        	break;
	        case 2:
	        	um.userSettings.snoozeLength = 5;
	        	break;
	        case 3:
	        	um.userSettings.snoozeLength = 6;
	        	break;
	        case 4:
	        	um.userSettings.snoozeLength = 7;
	        	break;
	        case 5:
	        	um.userSettings.snoozeLength = 8;
	        	break;
	        case 6:
	        	um.userSettings.snoozeLength = 9;
	        	break;
	        case 7:
	        	um.userSettings.snoozeLength = 10;
	        	break;
	       default:
	    	   break;
	        }
	        um.saveUserSettings(getApplicationContext());
	        
	        String snoozeDropDownPrompt = um.userSettings.snoozeLength + "minutes";
		    spinner.setPrompt(snoozeDropDownPrompt);
	    }

	    public void onNothingSelected(AdapterView parent) {
	      // Do nothing.
	    }
	}

	private class ReviewCodeReceiver extends BroadcastReceiver {     
		@Override
		public void onReceive(Context context, Intent intent) {

			boolean success = intent.getBooleanExtra("success", false);
			if (success)
			{
				// the review code is accepted
				Toast toast = Toast.makeText(context.getApplicationContext(), "Thanks for taking the time to review this app!  All premium features are unlocked", 3000);
				toast.show();
				
				SoundManager.getSoundModel(getApplicationContext()).playThanksForReviewing();
				finish();
			}
			else
			{
				Toast toast = Toast.makeText(context.getApplicationContext(), "The review code is invalid, or has already been used", 3000);
				toast.show();
				
				reviewCodeWaitSpinner.setVisibility(View.INVISIBLE);
				  reviewCodeBtn.setVisibility(View.VISIBLE);
			}
		}
	}
	
	@Override
	public void onCheckedChanged(CompoundButton rdoBtn, boolean checked) {
		 UserModel um = UserModel.getUserModel();
		
		 um.userSettings.isCelcius = useCelciusRdo.isChecked();
		 um.saveUserSettings(getApplicationContext());
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
		Intent intent = new Intent(SettingsActivity.this, SubscribeScreenActivity.class);
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
