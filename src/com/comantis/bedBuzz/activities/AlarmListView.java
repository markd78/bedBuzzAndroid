package com.comantis.bedBuzz.activities;

import java.util.List;

import android.app.ListActivity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.ToggleButton;

import com.comantis.bedBuzz.R;
import com.comantis.bedBuzz.VO.AlarmVO;
import com.comantis.bedBuzz.models.AlarmsModel;
import com.comantis.bedBuzz.models.UserModel;
import com.comantis.bedBuzz.utils.Utilities;
import com.flurry.android.FlurryAgent;

public class AlarmListView extends ListActivity   implements OnItemClickListener  {

	Context appContext;

	AlarmListAdapter adapter;

	ListView list;
	


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

		appContext = this.getApplicationContext();

		AlarmsModel.getAlarmsModel().getAlarms(appContext);

		setContentView(R.layout.alarm_list_view);
		populateList();

		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
		    getActionBar().setHomeButtonEnabled(true);
		}
		
		// ListView lv = getListView();
		// lv.setTextFilterEnabled(true);



		Button addAlarmbtn = (Button)findViewById(R.id.add_alarmBtn); 
		addAlarmbtn.setOnClickListener(addAlarmbtnClick); 

	}

	public void onRestart() {
		super.onRestart();
		populateList();
	}

	private void populateList() 
	{

		adapter = new AlarmListAdapter(this, AlarmsModel.getAlarmsModel().alarms);


		list=getListView();
		list.setOnItemClickListener(this);
		list.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
		setListAdapter(adapter);

	}

	private class AlarmListAdapter extends ArrayAdapter<AlarmVO> {

		private List<AlarmVO> alarms;

		public AlarmListAdapter(Context context,  List<AlarmVO> alarms) {
			super(context,  R.layout.alarm_item, alarms);
			this.alarms = alarms;
		}
		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			View v = convertView;

			if (v == null) {
				LayoutInflater vi = (LayoutInflater)getSystemService(Context.LAYOUT_INFLATER_SERVICE);
				v = vi.inflate(R.layout.alarm_item, null);

				ViewHolder holder = new ViewHolder();

				holder.alarmNameLbl = (TextView) v.findViewById(R.id.alarmNameLbl);
				holder.alarmTimeLbl = (TextView)  v.findViewById(R.id.alarmTimeLbl);
				holder.saturday = (TextView) v.findViewById(R.id.saturdayLbl);
				holder.sunday = (TextView) v.findViewById(R.id.sundayLbl);
				holder.monday = (TextView) v.findViewById(R.id.mondayLbl);
				holder.tuesday = (TextView) v.findViewById(R.id.tuesdayLbl);
				holder.wednesday = (TextView) v.findViewById(R.id.wednesdayLbl);
				holder.thursday = (TextView) v.findViewById(R.id.thursdayLbl);
				holder.friday = (TextView) v.findViewById(R.id.fridayLbl);

				holder.enableToggle = (ToggleButton) v.findViewById(R.id.alarmEnableToggle);


				v.setTag(holder);
			}

			ViewHolder holder = (ViewHolder) v.getTag();
			AlarmVO currentAlarm = alarms.get(position);

			if (currentAlarm != null) {
				holder.alarmNameLbl.setText(currentAlarm.alarmName);
				holder.alarmTimeLbl.setText(Utilities.getTimeStringFromHoursAndMins(currentAlarm.hour, currentAlarm.mins, false));
				holder.enableToggle.setChecked(currentAlarm.enabled);
				
				if (currentAlarm.enabled)
				{
					holder.alarmTimeLbl.setTextColor(Color.GREEN);
				}
				else
				{
					holder.alarmTimeLbl.setTextColor(Color.GRAY);
				}
				
				if (currentAlarm.isSaturday())
				{
					holder.saturday.setVisibility(View.VISIBLE);
				}
				else
				{
					holder.saturday.setVisibility(View.INVISIBLE);
				}

				if (currentAlarm.isSunday())
				{
					holder.sunday.setVisibility(View.VISIBLE);
				}
				else
				{
					holder.sunday.setVisibility(View.INVISIBLE);
				}

				if (currentAlarm.isMonday())
				{
					holder.monday.setVisibility(View.VISIBLE);
				}
				else
				{
					holder.monday.setVisibility(View.INVISIBLE);
				}
				if (currentAlarm.isTuesday())
				{
					holder.tuesday.setVisibility(View.VISIBLE);
				}
				else
				{
					holder.tuesday.setVisibility(View.INVISIBLE);
				}
				if (currentAlarm.isWednesday())
				{
					holder.wednesday.setVisibility(View.VISIBLE);
				}
				else
				{
					holder.wednesday.setVisibility(View.INVISIBLE);
				}
				if (currentAlarm.isThursday())
				{
					holder.thursday.setVisibility(View.VISIBLE);
				}
				else
				{
					holder.thursday.setVisibility(View.INVISIBLE);
				}
				if (currentAlarm.isFriday())
				{
					holder.friday.setVisibility(View.VISIBLE);
				}
				else
				{
					holder.friday.setVisibility(View.INVISIBLE);
				}

				holder.enableToggle.setTag(position);

				holder.enableToggle.setOnClickListener( new OnClickListener() {

					@Override
					public void onClick(View v) {
						Integer pos=(Integer)v.getTag();
						AlarmVO alarm=  alarms.get(pos);
						alarm.enabled = !alarm.enabled;
						AlarmsModel.getAlarmsModel().saveAlarms(getApplicationContext());
						
						adapter.notifyDataSetChanged();
					}
				});
			}
			return v;
		}
	}

	class ViewHolder {
		TextView alarmNameLbl;
		TextView alarmTimeLbl;
		TextView sunday;
		TextView monday;
		TextView tuesday;
		TextView wednesday;
		TextView thursday;
		TextView friday;
		TextView saturday;
		ToggleButton enableToggle;
	}

	private OnClickListener addAlarmbtnClick = new OnClickListener() {

		@Override
		public void onClick(View v) {

			AlarmsModel am = AlarmsModel.getAlarmsModel();
			AlarmVO defaultAlarm= am.getDefaultAlarm();
			am.isAddingNewAlarm = true;
			am.alarmCurrentlyEditing = defaultAlarm;

			//4- do some thing use full in here
			Intent intent = new Intent(AlarmListView.this, AlarmDetailView.class);
			startActivity(intent);
		}
	};

	@Override
	public void onItemClick(AdapterView<?> arg0, View v, int position, long arg3) {
		AlarmsModel am = AlarmsModel.getAlarmsModel();
		am.alarmCurrentlyEditing = am.alarms.get(position);
		am.isAddingNewAlarm = false;
		Intent intent = new Intent(AlarmListView.this, AlarmDetailView.class);
		startActivity(intent);
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.main_menu, menu);
     
		
		return true;
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
		Intent intent = new Intent(AlarmListView.this, SubscribeScreenActivity.class);
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
