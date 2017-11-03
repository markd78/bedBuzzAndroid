package com.comantis.bedBuzz.activities;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.app.ListActivity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.comantis.bedBuzz.R;
import com.comantis.bedBuzz.VO.FriendVO;
import com.comantis.bedBuzz.models.FacebookModel;
import com.comantis.bedBuzz.models.SoundManager;
import com.comantis.bedBuzz.models.UserModel;
import com.flurry.android.FlurryAgent;
import com.wilson.android.library.DrawableManager;

public class SelectFriendsActivity extends ListActivity  implements OnItemClickListener{

	Context appContext;
	DrawableManager dm;
	FriendAdapter adapter;
	Button composeBtn ;

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

		setContentView(R.layout.friends_list_view);
		appContext = this.getApplicationContext();

		FacebookModel fm = FacebookModel.getFacebookModel();

	/*	if (fm.friends == null || fm.friends.size() == 0)
		{
			// still fetching friends, pop up please wait, register for the 'received friends' broadcast 
			mReceiver = new FriendsReceiver();
			friendsReceiverRegistered = true;

			IntentFilter intentFilter = new IntentFilter(FacebookModel.FRIENDS_FETCHED);
			this.registerReceiver(mReceiver, intentFilter);
			return;
		}*/

		fm.targetFriends = new ArrayList <FriendVO> ();

		for (FriendVO f : fm.friends)
		{
			f.isTargetForMessage = false;
		}

		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
		    getActionBar().setHomeButtonEnabled(true);
		}
		
		UserModel um = UserModel.getUserModel();
		if (!um.userSettings.hasPlayedSelectFriendsHelp)
		{
			SoundManager.getSoundModel(appContext).playSelectFriendsHelp();
			um.userSettings.hasPlayedSelectFriendsHelp = true;
			um.saveUserSettings(appContext);
		}
	}

	private FriendsReceiver mReceiver;
	private boolean friendsReceiverRegistered = false;
	private class FriendsReceiver extends BroadcastReceiver {     


		@Override
		public void onReceive(Context context, Intent intent) {

			FacebookModel fm = FacebookModel.getFacebookModel();
			fm.targetFriends = new ArrayList <FriendVO> ();

			for (FriendVO f : fm.friends)
			{
				f.isTargetForMessage = false;
			}

			try
			{
				if (friendsReceiverRegistered)
				{
					unregisterReceiver(mReceiver);
					friendsReceiverRegistered = false;
				}
			}
			catch (Exception ex)
			{

			}
			onResume();
		}
	}

	@Override
	public void onResume() {
		super.onResume();

		if (FacebookModel.getFacebookModel().friends!=null)
		{
			dm = new DrawableManager();
			populateList();
			FacebookModel.getFacebookModel().targetFriends = new ArrayList<FriendVO>();

			composeBtn = (Button)findViewById(R.id.composeBtn); 
			composeBtn.setOnClickListener(composeBtnClick); 

			if (FacebookModel.getFacebookModel().targetFriends == null || FacebookModel.getFacebookModel().targetFriends.size() == 0)
			{
				composeBtn.setEnabled(false);
			}
		}
	}

	private OnClickListener composeBtnClick = new OnClickListener() {

		@Override
		public void onClick(View v) {
			Intent intent = new Intent(SelectFriendsActivity.this, ComposeMessageActivity.class);
			startActivityForResult(intent,0);
		}



	};

	@Override 
	public void onPause() {
		super.onPause();
		try
		{
			if (friendsReceiverRegistered)
			{
				unregisterReceiver(mReceiver);
				friendsReceiverRegistered = false;
			}
		}
		catch (Exception ex)
		{

		}
	}

	@Override
	public void onRestart() {
		super.onRestart();
		populateList();
		FacebookModel.getFacebookModel().targetFriends = new ArrayList<FriendVO>();
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode,
			Intent data) {
		if (requestCode == 0) {
			if (resultCode == RESULT_OK) {
				Intent intent=new Intent();
				String toUsers = data.getStringExtra("ToFacebookUsers");
				intent.putExtra("ToFacebookUsers", toUsers);
				if (getParent() == null) {
					setResult(Activity.RESULT_OK, intent);
				}
				else {
					getParent().setResult(Activity.RESULT_OK, intent);
				}
				finish();
			}
		}
	}

	private void populateList() 
	{

		adapter = new FriendAdapter(this, FacebookModel.getFacebookModel().friends);


		ListView list=getListView();
		list.setOnItemClickListener(this);
		list.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);
		setListAdapter(adapter);

	}

	@Override
	public void onItemClick(AdapterView<?> arg0, View v, int position, long arg3) {

		FacebookModel fm = FacebookModel.getFacebookModel();

		FriendVO f = FacebookModel.getFacebookModel().friends.get(position);
		// toggle isTarget
		f.isTargetForMessage = !f.isTargetForMessage;

		if (f.isTargetForMessage)
		{
			fm.targetFriends.add(f);
		}
		else
		{
			fm.targetFriends.remove(f);
		}

		if (FacebookModel.getFacebookModel().targetFriends == null || FacebookModel.getFacebookModel().targetFriends.size() == 0)
		{
			composeBtn.setEnabled(false);
		}
		else
		{
			composeBtn.setEnabled(true);
		}

		adapter.notifyDataSetChanged();
	}

	private class FriendAdapter extends ArrayAdapter<FriendVO> {

		private List<FriendVO> friends;

		public FriendAdapter(Context context,  List<FriendVO> friends) {
			super(context,  R.layout.friend_item, friends);
			this.friends = friends;
		}
		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			View v = convertView;
			if (v == null) {
				LayoutInflater vi = (LayoutInflater)getSystemService(Context.LAYOUT_INFLATER_SERVICE);
				v = vi.inflate(R.layout.friend_item, null);

				ViewHolder holder = new ViewHolder();
				holder.profile_pic = (ImageView) v.findViewById(R.id.friendPic);
				holder.name = (TextView) v.findViewById(R.id.friendName);
				holder.include = (CheckBox) v.findViewById(R.id.targetChk);
				v.setTag(holder);
			}

			FriendVO f = friends.get(position);
			ViewHolder holder = (ViewHolder) v.getTag();

			if (f != null) {

				holder.name.setText(f.name);

				dm.fetchDrawableOnThread(f.picSmallURL, holder.profile_pic);

				holder.include.setChecked(f.isTargetForMessage);
			}

			return v;
		}
	}

	class ViewHolder {
		ImageView profile_pic;
		TextView name;
		CheckBox include;
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
		Intent intent = new Intent(SelectFriendsActivity.this, SubscribeScreenActivity.class);
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
