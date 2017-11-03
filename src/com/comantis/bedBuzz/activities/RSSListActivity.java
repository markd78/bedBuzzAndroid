package com.comantis.bedBuzz.activities;

import java.util.ArrayList;
import java.util.HashMap;

import android.app.ListActivity;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.ListView;
import android.widget.SimpleAdapter;

import com.comantis.bedBuzz.R;
import com.comantis.bedBuzz.VO.RSSFeedVO;
import com.comantis.bedBuzz.models.RSSModel;
import com.comantis.bedBuzz.models.UserModel;
import com.flurry.android.FlurryAgent;


public class RSSListActivity extends ListActivity {
	Context appContext;

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

		RSSModel.getRSSModell().getRSSFeedsFromSettings(appContext);


		populateList();

		setContentView(R.layout.rss_list_view);

		ListView lv = getListView();
		lv.setTextFilterEnabled(true);

		lv.setOnItemClickListener(new OnItemClickListener() {
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {

				RSSModel rm = RSSModel.getRSSModell();
				rm.isAddingNewFeed = false;
				rm.feedCurrentlyEditing = rm.rssFeeds.get(position);
				
				Intent intent = new Intent(RSSListActivity.this, RSSDetailView.class);
				startActivity(intent);
			}
		});
		
		Button addRSSFeedbtn = (Button)findViewById(R.id.add_rssBtn); 
		addRSSFeedbtn.setOnClickListener(addRSSFeedbtnClicked); 
		
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
		    getActionBar().setHomeButtonEnabled(true);
		}
	}


public void onRestart() {
	super.onRestart();
	populateList();
}

private void populateList() 
{
	// initialize the List of Maps
	ArrayList<HashMap<String,String>> list = new ArrayList<HashMap<String,String>>();


	// iterate over all messages
	// create a map for each message
	// fill the map with data
	for (RSSFeedVO feed: RSSModel.getRSSModell().rssFeeds) {
		HashMap<String,String> map = new HashMap<String,String>();
		map.put("rssName",feed.rssFeedName);
		list.add(map);
	}

	SimpleAdapter adapter = new SimpleAdapter(
			this.appContext,
			list,
			R.layout.rss_list_item,
			new String[] {"rssName"},
			new int[] {R.id.rssName}

	);


	setListAdapter(adapter);

	//  setListAdapter(new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1 , COUNTRIES));

}

private OnClickListener addRSSFeedbtnClicked = new OnClickListener() {

	@Override
	public void onClick(View v) {

		RSSModel rm = RSSModel.getRSSModell();
		RSSFeedVO defaultRSS = rm.getDefaultRSS();
		rm.isAddingNewFeed = true;
		rm.feedCurrentlyEditing = defaultRSS;

		Intent intent = new Intent(RSSListActivity.this, RSSDetailView.class);
		startActivity(intent);
	}
};

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
	Intent intent = new Intent(RSSListActivity.this, SubscribeScreenActivity.class);
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
