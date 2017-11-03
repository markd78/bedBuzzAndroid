package com.comantis.bedBuzz.activities;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.TimePicker;

import com.comantis.bedBuzz.R;
import com.comantis.bedBuzz.VO.AlarmVO;
import com.comantis.bedBuzz.models.AlarmsModel;
import com.comantis.bedBuzz.models.UserModel;
import com.flurry.android.FlurryAgent;

public class AlarmDetailView extends Activity implements TimePicker.OnTimeChangedListener {

	public AlarmVO currentAlarm;

	//public Context context;
	
	CheckBox sundayChk ;
	  CheckBox mondayChk ;
	  CheckBox tuesdayChk ;
	  CheckBox wednesdayChk ;
	  CheckBox thursdayChk ;
	  CheckBox fridayChk ;
	  CheckBox saturdayChk ;
	
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
		//context = this.getApplicationContext();
	  currentAlarm = AlarmsModel.getAlarmsModel().alarmCurrentlyEditing;
	 
	  super.onCreate(savedInstanceState);
	  setContentView(R.layout.alarm_detail_view);
	  
	  EditText alarmNameTxt = (EditText)findViewById(R.id.alarmNameTxt);
	  alarmNameTxt.addTextChangedListener(onAlarmNameTextChanged);

	  if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
		    getActionBar().setHomeButtonEnabled(true);
		}
	  
	   sundayChk = (CheckBox)findViewById(R.id.sundayChk);
	   mondayChk = (CheckBox)findViewById(R.id.mondayChk);
	   tuesdayChk = (CheckBox)findViewById(R.id.tuesdayChk);
	   wednesdayChk = (CheckBox)findViewById(R.id.wednesdayChk);
	   thursdayChk = (CheckBox)findViewById(R.id.thursdayChk);
	   fridayChk = (CheckBox)findViewById(R.id.fridayChk);
	   saturdayChk = (CheckBox)findViewById(R.id.saturdayChk);
	   
	   Button selectmusicbtn = (Button)findViewById(R.id.selectMusicBtn); 
	   selectmusicbtn.setOnClickListener(selectMusicClick); 

	   Button saveBtn = (Button)findViewById(R.id.SaveAlarmBtn); 
	   saveBtn.setOnClickListener(new View.OnClickListener() {
           public void onClick(View v) {
               AlarmsModel am = AlarmsModel.getAlarmsModel();
               if (am.isAddingNewAlarm)
               {
            	   am.alarms.add(am.alarmCurrentlyEditing);
               }
               
               am.saveAlarms(v.getContext());
               finish();
           }
	   });
	   
	   Button cancelBtn = (Button)findViewById(R.id.CancelAlarmBtn); 

	   AlarmsModel am = AlarmsModel.getAlarmsModel();
	   if (am.isAddingNewAlarm)
	   {
		   cancelBtn.setOnClickListener(new View.OnClickListener() {
	           public void onClick(View v) {
	               finish();
	           }
		   });
	   }
	   else
	   {
		   cancelBtn.setText("Delete");
		   cancelBtn.setOnClickListener(new View.OnClickListener() {
	           public void onClick(View v) {
	        	   AlarmsModel am = AlarmsModel.getAlarmsModel();
	        	   am.alarms.remove(am.alarmCurrentlyEditing);
	        	   am.saveAlarms(v.getContext());
	        	   finish();
	        	   
	           }
		   });
	   }
	   
	   updateDisplay();
		  
	  sundayChk.setOnCheckedChangeListener(onCheckChange);
	  mondayChk.setOnCheckedChangeListener(onCheckChange);
	  tuesdayChk.setOnCheckedChangeListener(onCheckChange);
	  wednesdayChk.setOnCheckedChangeListener(onCheckChange);
	  thursdayChk.setOnCheckedChangeListener(onCheckChange);
	  fridayChk.setOnCheckedChangeListener(onCheckChange);
	  saturdayChk.setOnCheckedChangeListener(onCheckChange);
	  
	}
	
	@Override 
	public void onResume() {
		super.onResume();

		  updateDisplay();
	}
	
	private OnCheckedChangeListener onCheckChange = new OnCheckedChangeListener()
    {

		@Override
		public void onCheckedChanged(CompoundButton checkBox, boolean arg1) {
			currentAlarm.setMonday(mondayChk.isChecked());
			currentAlarm.setTuesday(tuesdayChk.isChecked());
			currentAlarm.setWednesday(wednesdayChk.isChecked());
			currentAlarm.setThursday(thursdayChk.isChecked());
			currentAlarm.setFriday(fridayChk.isChecked());
			currentAlarm.setSaturday(saturdayChk.isChecked());
			currentAlarm.setSunday(sundayChk.isChecked());
		}
		
    };
	
	private void updateDisplay() 
	{
		if (currentAlarm==null)
		{
			return;
		}
		
		 EditText alarmNameTxt = (EditText)findViewById(R.id.alarmNameTxt);
		  alarmNameTxt.setText(currentAlarm.alarmName);
		  
		  TimePicker timePicker = (TimePicker)findViewById(R.id.alarmTimePicker);
		  timePicker.setCurrentHour(currentAlarm.hour);
		  timePicker.setCurrentMinute(currentAlarm.mins);
		  timePicker.setOnTimeChangedListener(this);
		  
		  sundayChk.setChecked(currentAlarm.isSunday()) ;
		  mondayChk.setChecked(currentAlarm.isMonday()) ;
		  tuesdayChk.setChecked(currentAlarm.isTuesday()) ;
		  wednesdayChk.setChecked(currentAlarm.isWednesday()) ;
		  thursdayChk.setChecked(currentAlarm.isThursday()) ;
		  fridayChk.setChecked(currentAlarm.isFriday()) ;
		  saturdayChk.setChecked(currentAlarm.isSaturday()) ;
		  
		  if (this.currentAlarm.musicFile != null && currentAlarm.musicFile != "")
		  {
			  TextView musicFileTxt = (TextView)findViewById(R.id.musicFile);
			  musicFileTxt.setText(currentAlarm.musicFile);
		  }
		  
	}
	
	private TextWatcher onAlarmNameTextChanged = new TextWatcher(){
        public void afterTextChanged(Editable s) {
        	currentAlarm.alarmName = s.toString();
        	
	
           
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
	

	@Override
	public void onTimeChanged(TimePicker view, int hourOfDay, int minute) {
		AlarmsModel am = AlarmsModel.getAlarmsModel();
        am.alarmCurrentlyEditing.hour = hourOfDay;
        am.alarmCurrentlyEditing.mins = minute;
		
	}
	
	 private OnClickListener selectMusicClick = new OnClickListener() {

		@Override
		public void onClick(View v) {
			Intent i = new Intent();           
			i.setAction(Intent.ACTION_GET_CONTENT);
			i.setType("audio/*");
			((Activity)v.getContext()).startActivityForResult(Intent.createChooser(i, "Select audio file"),0);
		}
		
		
			
		};
	        
		protected void onActivityResult(int requestCode, int resultCode, Intent data)
	    {

	        super.onActivityResult(requestCode, resultCode, data);
		 
	       System.out.println("ON ACTIVITY " + resultCode);
	       if (requestCode == 0 && resultCode == Activity.RESULT_OK)
	        {
	    	   Uri selectedImage = data.getData();
			     
	    	   this.currentAlarm.musicFile = getRealPathFromURI(selectedImage);
	    	   
	        }
	       
	       this.updateDisplay();
	    }
	 
	// And to convert the image URI to the direct file system path of the image file
	 public String getRealPathFromURI(Uri contentUri) {

	         // can post image
	         String [] proj={MediaStore.Images.Media.DATA};
	         Cursor cursor = managedQuery( contentUri,
	                         proj, // Which columns to return
	                         null,       // WHERE clause; which rows to return (all rows)
	                         null,       // WHERE clause selection arguments (none)
	                         null); // Order-by clause (ascending by name)
	         int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
	         cursor.moveToFirst();

	         return cursor.getString(column_index);
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
			Intent intent = new Intent(AlarmDetailView.this, SubscribeScreenActivity.class);
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
